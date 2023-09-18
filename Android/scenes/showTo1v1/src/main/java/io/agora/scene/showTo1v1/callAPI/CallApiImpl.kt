package io.agora.scene.showTo1v1.callAPI

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.TextureView
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtm2.*
import org.json.JSONObject
import java.util.UUID

enum class CallAction(val value: Int) {
    Call(0),
    CancelCall(1),
    Accept(2),
    Reject(3),
    Hangup(4);

    companion object {
        fun fromValue(value: Int): CallAction {
            return CallAction.values().find { it.value == value } ?: Call
        }
    }
}
enum class CallCostType(val value: String) {
    RecvCalling("recvCalling"),
    AcceptCall("acceptCall"),
    LocalUserJoinChannel("localUserJoinChannel"),
    RemoteUserJoinChannel("remoteUserJoinChannel"),
    RecvFirstFrame("recvFirstFrame")
}

class CallApiImpl(
    private val context: Context
): ICallApi, RtmEventListener, CallMessageListener, IRtcEngineEventHandler() {

    companion object {
        val kPublisher = "publisher"
        val kDebugInfo = "debugInfo"    //测试信息，目前是会在主叫onBegin时抛出分步耗时
        val kDebugInfoMap = "debugInfoMap"    //测试信息，目前是会在主叫onBegin时抛出分步耗时
        val kRemoteUserId = "remoteUserId"
        val kFromUserId = "fromUserId"
        val kFromRoomId = "fromRoomId"
        val kFromUserExtension = "fromUserExtension"
    }

    private val kRetryCount: Int = 3
    private val kCallTimeoutInterval: Long = 15000
    private val kCurrentMessageVersion = "1.0"
    private val kMessageAction = "message_action"
    private val kMessageVersion = "message_version"
    private val kMessageTs = "message_timestamp"

    private val kCallId = "callId"
    private val kCalleeState = "state"      //当前呼叫状态
    private val kDebugInfo = "debugInfo"
    private val kDebugInfoMap = "debugInfoMap"    //测试信息，目前是会在主叫onBegin时抛出分步耗时

    private val TAG = "CallApiImpl_LOG"
    private val delegates = mutableListOf<ICallApiListener>()

    private val rtcProxy = CallProxy()
    private var config: CallConfig? = null
    private var tokenConfig: CallTokenConfig? = null
    private var messageManager: CallMessageManager? = null
    private var prepareConfig: PrepareConfig? = null
    private var callId: String = ""
    private var recvMessageTsMap = mutableMapOf<Int, Long>()

    private var reportInfoList = mutableListOf<CallReportInfo>()
    private var isChannelJoined = false
        set(value) {
            field = value
            if (!isChannelJoined) { return }
            _flushReport()
        }
    private val mHandler = Handler(Looper.getMainLooper())
    private var timeOutRunnable: Runnable? = null
    /// 当前状态
    private var state: CallStateType = CallStateType.Idle
        set(value) {
            if (field == value) { return }
            field = value

            when(value) {
                CallStateType.Calling -> {
                    //开启定时器，如果超时无响应，调用no response
                    val runnable = Runnable {
                        _notifyState(CallStateType.Prepared, CallReason.CallingTimeout)
                        _notifyEvent(CallEvent.CallingTimeout)
                    }
                    mHandler.postDelayed(runnable, kCallTimeoutInterval)
                    timeOutRunnable = runnable
                }
                CallStateType.Idle, CallStateType.Prepared, CallStateType.Failed, CallStateType.Connected -> {
                    timeOutRunnable?.let { mHandler.removeCallbacks(it) }
                    timeOutRunnable = null
                }
                else -> {}
            }
        }

    //加入RTC完成回调
    private var joinRtcCompletion: ((AGError?) -> Unit)? = null
    //首帧出图回调
    private var firstFrameCompletion: (() -> Unit)? = null
    //呼叫中的频道名
    private var callingRoomId: String? = null
    //呼叫中的远端用户
    private var callingUserId: Int? = null

    private var isPreparing = false
    private var preparedTs: Long = 0

    private var rtcConnection: RtcConnection? = null

    //呼叫开始的时间
    private var callTs: Long? = null
        set(value) {
            field = value
            callCost = ""
            callCostMap.clear()
        }

    //呼叫耗时的debug信息
    private var callCost: String = ""
    private val callCostMap = mutableMapOf<String, Long>()

    private fun _messageDic(action: CallAction): Map<String, Any> {
        val dic: MutableMap<String, Any> = mutableMapOf()
        dic[kMessageAction] = action.value
        dic[kMessageVersion] = kCurrentMessageVersion
        dic[kMessageTs] = _getNtpTimeInMs()
        dic[kFromUserId] = config?.userId ?: 0
        if (callId.isNotBlank()) {
            dic[kCallId] = callId
        }
        val ext = config?.userExtension
        if (ext != null) {
            dic[kFromUserExtension] = ext
        }
        return dic
    }
    //获取ntp时间
    private fun _getNtpTimeInMs(): Long {
        var localNtpTime = config?.rtcEngine?.ntpWallTimeInMs ?: 0
        if (localNtpTime == "0".toLong()) {
            localNtpTime = System.currentTimeMillis()
        }
        return localNtpTime
    }

    private fun _getCost(ts: Int? = null): Long {
        val cts = callTs ?: return 0
        return if (ts != null) {
            ts - cts
        } else {
            _getNtpTimeInMs() - cts
        }
    }

    private fun timeProfiling(message: String, ts: Int? = null) {
        val cost = _getCost(ts)
        callCostMap[message] = (callCostMap[message] ?: 0) + cost
        val msg = "$message: $cost ms"
        Log.d(TAG, msg)
        callCost = "$callCost\n$msg"
    }

    private fun _processState(prevState: CallStateType,
                              state: CallStateType,
                              stateReason: CallReason,
                              eventReason: String,
                              elapsed: Long) {
        if (prevState != state && state == CallStateType.Idle) {
            _leaveRTC(force = true)
            _cleanCallCache()
            config = null
            tokenConfig = null
            messageManager?.logout()
            messageManager = null
            rtcProxy.removeAllListeners()
            delegates.clear()
        } else if (prevState != CallStateType.Idle && state == CallStateType.Prepared) {
            _leaveRTC()
            _cleanCallCache()
        }
    }

    private fun _notifyTokenPrivilegeWillExpire() {
        delegates.forEach { listener ->
            listener.tokenPrivilegeWillExpire()
        }
    }

    private fun _notifyState(state: CallStateType, stateReason: CallReason = CallReason.None, eventReason: String = "", isLocalUser: Boolean = true, elapsed: Long = 0, eventInfo: Map<String, Any> = emptyMap()) {
        Log.d(TAG, "_notifyState  $state, stateReason: $stateReason, eventReason: $eventReason, elapsed: $elapsed ms, eventInfo: $eventInfo")
        _processState(this.state, state, stateReason, eventReason, elapsed)
        this.state = state
        runOnUiThread {
            delegates.forEach {
                it.onCallStateChanged(state, stateReason, eventReason, elapsed, eventInfo)
            }
        }
    }

    private fun _notifyEvent(event: CallEvent, elapsed: Long = 0) {
        config?.let { config ->
            val key = "event=${event.value}&userId=${config.userId}&role=${config.role.name}&state=${state.name}"
            _reportEvent(key, _getNtpTimeInMs().toInt(), "")
        } ?: Log.d(TAG, "_notifyEvent config == null")

        runOnUiThread {
            delegates.forEach { listener ->
                listener.onCallEventChanged(event, elapsed)
            }
        }

        when (event) {
            CallEvent.RemoteJoin -> _reportCostEvent(CallCostType.RemoteUserJoinChannel)
            CallEvent.LocalJoin -> _reportCostEvent(CallCostType.LocalUserJoinChannel)
            CallEvent.RemoteAccepted -> _reportCostEvent(CallCostType.AcceptCall)
            CallEvent.OnCalling -> _reportCostEvent(CallCostType.RecvCalling)
            CallEvent.RecvRemoteFirstFrame -> _reportCostEvent(CallCostType.RecvFirstFrame)
            else -> {}
        }
    }

    private fun _prepareForCall(prepareConfig: PrepareConfig, retryCount: Int = 3, completion: ((AGError?) -> Unit)?) {
        if (this.state == CallStateType.Prepared) {
            Log.e(TAG, "is already in 'prepared' state")
            completion?.invoke(AGError("is already in 'prepared' state", -1))
            return
        }
        val msgManager = messageManager ?: run {
            val reason = "not init"
            Log.e(TAG, reason)
            completion?.invoke(AGError(reason, -1))
            return
        }
        if (isPreparing) {
            val reason = "is already in preparing"
            Log.d(TAG, reason)
            completion?.invoke(AGError(reason, -1))
            return
        }
        this.prepareConfig = prepareConfig
        var rtmError: AGError? = null
        var rtcError: AGError? = null
        isPreparing = true
        val tag = UUID.randomUUID().toString()
        Log.d(TAG, "prepareForCall[$tag]")
        val startTime = System.currentTimeMillis()
        val runnable = Runnable {
            if (rtmError != null || rtcError != null) {
                val error = rtmError ?: rtcError
                if (retryCount <= 1) {
                    isPreparing = false
                    runOnUiThread{ completion?.invoke(error) }
                    _notifyState(
                        CallStateType.Failed,
                        if (error == rtmError) CallReason.RtmSetupFailed else CallReason.JoinRTCFailed,
                        error?.msg ?: "error")
                } else {
                    isPreparing = false
                    _prepareForCall(prepareConfig, retryCount - 1, completion)
                    _notifyEvent(CallEvent.RtmSetupSuccessed)
                }
                return@Runnable
            }
            preparedTs = _getNtpTimeInMs()
            isPreparing = false
            _notifyState(CallStateType.Prepared, elapsed = System.currentTimeMillis() - startTime)
            runOnUiThread{ completion?.invoke(null) }
        }
        Log.d(TAG, "prepareForCall")
        msgManager.rtmInitialize(prepareConfig, tokenConfig) { err ->
            rtmError = err
            Log.d(TAG, "prepareForCall[$tag] rtmInitialize completion: ${err?.msg ?: "success"})")
            runnable.run()
        }
        if (prepareConfig.autoJoinRTC) {
            _joinRTC(tokenConfig?.roomId ?: "", tokenConfig?.rtcToken ?: "", joinOnly = true) { err ->
                rtcError = err
                Log.d(TAG, "prepareForCall[$tag] joinRTC completion: ${err?.msg ?: "success"}")
                runnable.run()
            }
        }
    }
    private fun _deinitialize() {
        isPreparing = false
        _notifyState(CallStateType.Idle)
        _notifyEvent(CallEvent.Deinitialize)
    }
    private fun _setupRemoteVideo(roomId: String, uid: Int, view: TextureView) {
        val engine = config?.rtcEngine ?: return
        val connection = rtcConnection ?: return
        val videoCanvas = VideoCanvas(view)
        videoCanvas.uid = uid
        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN
        videoCanvas.mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
        val ret = engine.setupRemoteVideoEx(videoCanvas, connection)
        Log.d(TAG, "_setupRemoteVideo ret: $ret, roomId: $roomId, uid: $uid")
    }

    private fun _setupLocalVideo(uid: Int, view: TextureView) {
        val engine = config?.rtcEngine ?: run {
            Log.d(TAG, "_setupLocalVideo fail: engine is empty")
            return
        }
        val videoCanvas = VideoCanvas(view)
        videoCanvas.uid = uid
        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN
        videoCanvas.mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO

        engine.setDefaultAudioRoutetoSpeakerphone(true)
        engine.setupLocalVideo(videoCanvas)
        engine.startPreview()
    }

    private fun _joinRTCAndNotify(roomId: String,
                                  token: String,
                                  retryCount: Int = 3,
                                  joinOnly: Boolean = false,
                                  completion: ((AGError?) -> Unit)? = null) {
        _joinRTC(roomId, token, retryCount, joinOnly) { err ->
            if (err != null) {
                _notifyState(CallStateType.Failed, CallReason.JoinRTCFailed, err.msg)
                _notifyEvent(CallEvent.JoinRTCFailed)
            } else {
                _notifyEvent(CallEvent.JoinRTCSuccessed)
            }
            completion?.invoke(err)
        }
    }

    private fun _joinRTC(roomId: String, token: String, retryCount: Int = 3, joinOnly: Boolean = false, completion:((AGError?) -> Unit)?) {
        val engine = config?.rtcEngine ?: return
        val config = this.config ?: run {
            val errReason = "config is empty"
            completion?.invoke(AGError(errReason, -1))
            return
        }
        val c = rtcConnection
        if (c != null) {
            Log.e(TAG, "rtc join already")
            if (c.channelId == roomId) {
                val mediaOptions = ChannelMediaOptions()
                mediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                mediaOptions.publishCameraTrack = !joinOnly
                mediaOptions.publishMicrophoneTrack = !joinOnly
                mediaOptions.autoSubscribeAudio = !joinOnly
                mediaOptions.autoSubscribeVideo = !joinOnly
                config.rtcEngine?.updateChannelMediaOptionsEx(mediaOptions, c)
                completion?.invoke(AGError("rtc join already", -1))
                _setupLocalVideo(config.userId, config.localView)
                return
            }
            Log.d(TAG, " mismatch channel, leave first! target: $roomId current: ${c.channelId}")
            engine.leaveChannelEx(c)
            rtcConnection = null
        }
        //需要先开启音视频，使用enableLocalAudio而不是enableAudio，否则会导致外部mute的频道变成unmute
        engine.enableLocalVideo(true)
        engine.enableLocalAudio(true)

        val mediaOptions = ChannelMediaOptions()
        mediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        mediaOptions.publishCameraTrack = !joinOnly
        mediaOptions.publishMicrophoneTrack = !joinOnly
        mediaOptions.autoSubscribeAudio = !joinOnly
        mediaOptions.autoSubscribeVideo = !joinOnly

        val connection = RtcConnection(roomId, config.userId)
        rtcConnection = connection

        rtcProxy.addListener(this)
        val ret: Int = engine.joinChannelEx(token, connection, mediaOptions, rtcProxy)
        joinRtcCompletion = { err ->
            if (err == null) {
                completion?.invoke(null)
                timeProfiling("4.呼叫-加入房间成功")
            } else if (err.code == ErrorCode.ERR_TOKEN_EXPIRED) {
                completion?.invoke(err)
                this._notifyTokenPrivilegeWillExpire()
            } else {
                rtcConnection = null
                if (retryCount <= 1) {
                    completion?.invoke(err)
                } else {
                    _joinRTCAndNotify(roomId, token, retryCount - 1, completion = completion)
                }
            }
        }
        val startTime = System.currentTimeMillis()
        firstFrameCompletion = {
            val eventInfo = mapOf(
                Pair(kFromRoomId, callingRoomId ?: ""),
                Pair(kFromUserId, callingUserId ?: 0),
                Pair(kRemoteUserId, config.userId),
                Pair(kDebugInfo, callCost),
                Pair(kDebugInfoMap, callCostMap.toMap())
            )
            val elapsed = System.currentTimeMillis() - startTime
            _notifyState(CallStateType.Connecting, CallReason.RecvRemoteFirstFrame, elapsed = elapsed)
            _notifyState(CallStateType.Connected, CallReason.RecvRemoteFirstFrame, elapsed = elapsed, eventInfo = eventInfo)
            _notifyEvent(CallEvent.RecvRemoteFirstFrame, elapsed)
        }
        if (ret != Constants.ERR_OK) {
            completion?.invoke(AGError("join rtc failed!", ret))
            return
        }
        runOnUiThread {
            _setupLocalVideo(config.userId, config.localView)
        }
    }

    private fun _leaveRTC(force: Boolean = false) {
        joinRtcCompletion = null
        val connection = rtcConnection ?: run {
            Log.d(TAG, "leave RTC channel failed, not joined the channel")
            return
        }
        config?.rtcEngine?.stopPreview()
        if (!force && (prepareConfig?.autoJoinRTC == true)) {
            //如果默认加入RTC，则不退出，只停止首发流
            val mediaOptions = ChannelMediaOptions()
            mediaOptions.autoSubscribeVideo = false
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.publishMicrophoneTrack = false
            mediaOptions.publishCustomAudioTrack = false
            config?.rtcEngine?.updateChannelMediaOptionsEx(mediaOptions, connection)
        } else {
            config?.rtcEngine?.leaveChannelEx(connection)
            rtcConnection = null
        }
    }

    private fun _cleanCallCache() {
        callingRoomId = null
        callingUserId = null
        callTs = null
        timeOutRunnable?.let { mHandler.removeCallbacks(it) }
        timeOutRunnable = null

        callId = ""
    }
    private fun _flushReport() {
        reportInfoList.forEach { info ->
            _sendCustomReportMessage(info.msgId, info.category, info.event, info.label, info.value)
        }
        reportInfoList.clear()
    }
    private fun _reportCostEvent(type: CallCostType) {
        _reportEvent(type.value, _getCost().toInt(), "")
    }

    private fun _reportMethod(event: String, label: String = "") {
        val msgId = "scenarioAPI"
        val category = "3_Android_0.2.0"
        if (isChannelJoined) {
            _sendCustomReportMessage(msgId, category, event, label, 0)
            return
        }
        val info = CallReportInfo(msgId, category, event, label, 0)
        reportInfoList.add(info)
        Log.d(TAG, "sendCustomReportMessage not join channel cache it! event: $event label: $label")
    }

    private fun _reportEvent(key: String, value: Int, messageId: String) {
        val cfg = config ?: return
        val engine = cfg.rtcEngine ?: return
        val msgId = "uid=${cfg.userId}&messageId=$messageId"
        val category = "${cfg.mode.value}"
        if (isChannelJoined) {
            _sendCustomReportMessage(msgId, category, key, callId, value)
            return
        }
        val info = CallReportInfo(msgId, category, key, callId, value)
        reportInfoList.add(info)
        Log.d(TAG, "sendCustomReportMessage not join channel cache it! msgId: $msgId category: $category event: $key label: $callId value: $value")
    }

    private fun _sendCustomReportMessage(msgId: String,
                                         category: String,
                                         event: String,
                                         label: String,
                                         value: Int) {
        val c = config
        if (c != null && isChannelJoined && rtcConnection != null) else { return }
        val ret = c.rtcEngine?.sendCustomReportMessageEx(msgId, category, event, label, value, rtcConnection)
        Log.d(TAG, "sendCustomReportMessage msgId: $msgId category: $category event: $event label: $label value: $value : $ret")
    }

    //MARK: on Message
    private fun _process(reason: CallAction, message: Map<String, Any>) {
        when (reason) {
            CallAction.Call -> {
                _onCall(message)
                return
            }
            CallAction.CancelCall -> _onCancel(message)
            CallAction.Reject -> _onReject(message)
            CallAction.Accept -> {
                _onAccept(message)
                return
            }
            CallAction.Hangup -> _onHangup(message)
            else -> {}
        }
    }
    private fun _reject(roomId: String, remoteUserId: Int, reason: String?, completion: ((AGError?, Map<String, Any>) -> Unit)? = null) {
        val userId = config?.userId
        val fromRoomId = tokenConfig?.roomId
        if (userId == null || fromRoomId == null) {
            completion?.invoke(AGError("reject fail! current userId or roomId is empty", -1), emptyMap())
            Log.e(TAG, "reject fail! current userId or roomId is empty")
            return
        }
        val message = _messageDic(CallAction.Reject).toMutableMap()
        message[kRemoteUserId] = remoteUserId
        message[kFromRoomId] = fromRoomId
        messageManager?.sendMessage(roomId, fromRoomId, message.toMap()) { error ->
            runOnUiThread { completion?.invoke(error, message) }
        }
        _notifyEvent(CallEvent.LocalRejected)
    }

    private fun _hangup(roomId: String, completion: ((AGError?, Map<String, Any>) -> Unit)? = null) {
        val fromRoomId = tokenConfig?.roomId ?: run {
            completion?.invoke(AGError("reject fail! current roomId is empty", -1), emptyMap())
            Log.d(TAG, "reject fail! current roomId is empty")
            return
        }
        val message = _messageDic(CallAction.Hangup)
        messageManager?.sendMessage(roomId, fromRoomId, message) { err ->
            runOnUiThread { completion?.invoke(err, message) }
        }
    }

    //收到呼叫消息
    private fun _onCall(fromRoomId: String, fromUserId: Int, callId: String, userExtension: Map<String, Any>) {
        //如果不是prepared状态或者不是接收的正在接听的用户的呼叫
        if (!(state == CallStateType.Prepared || callingUserId == fromUserId)) {
            _reject(fromRoomId, fromUserId, "callee is currently on call")
            return
        }
        if (callingUserId == fromUserId && callingRoomId != fromRoomId) {
            //如果主叫换了room id呼叫
            _reject(fromRoomId, fromUserId, "callee is being occupied by another channel of the caller")
            _notifyState(CallStateType.Prepared, CallReason.CancelByCallerRecall)
            _notifyEvent(CallEvent.CancelByCallerRecall)
            return
        }
        this.callId = callId
        val eventInfo = mapOf(
            Pair(kFromRoomId, fromRoomId),
            Pair(kFromUserId, fromUserId),
            Pair(kRemoteUserId, config?.userId ?: 0),
            Pair(kFromUserExtension, userExtension)
        )
        _notifyState(CallStateType.Calling, CallReason.None, eventInfo = eventInfo)
        _notifyEvent(CallEvent.OnCalling)
        if (config?.autoAccept == false) {
            callingRoomId = fromRoomId
            callingUserId = fromUserId
            return
        }
        accept(fromRoomId, fromUserId, tokenConfig?.rtcToken ?: "") {
        }
    }
    private fun _onCall(message: Map<String, Any>) {
        val fromRoomId = message[kFromRoomId] as String
        val fromUserId = message[kFromUserId] as Int
        val callId = message[kCallId] as String
        val userExtJson = message[kFromUserExtension] as? JSONObject
        val userExtMap = mutableMapOf<String, Any>()
        if (userExtJson != null) {
            for (key in userExtJson.keys()) {
                userExtMap[key] = userExtJson.get(key)
            }
        }
        _onCall(fromRoomId, fromUserId, callId, userExtMap.toMap())
    }

    private fun _onCancel(message: Map<String, Any>) {
        val fromUserId = message[kFromUserId] as? Int ?: 0
        //如果不是接收的正在接听的用户的呼叫
        if (callingUserId != fromUserId) {
            return
        }
        _notifyState(CallStateType.Prepared, CallReason.RemoteCancel, eventInfo = message)
        _notifyEvent(CallEvent.RemoteCancel)
    }

    private fun _onReject(message: Map<String, Any>) {
        _notifyState(CallStateType.Prepared, CallReason.RemoteRejected, eventInfo = message)
        _notifyEvent(CallEvent.RemoteRejected)
    }

    private fun _onAccept(message: Map<String, Any>) {
        timeProfiling("2.呼叫-被叫收到呼叫", message[kMessageTs] as? Int)
        timeProfiling("3.呼叫-收到被叫同意")
        if (state != CallStateType.Calling) {
            return
        }
        val elapsed = _getNtpTimeInMs() - (callTs ?: 0)
        _notifyState(CallStateType.Connecting, CallReason.RemoteAccepted, elapsed = elapsed)
        _notifyEvent(CallEvent.RemoteAccepted, elapsed)
    }

    private fun _onHangup(message: Map<String, Any>) {
        val fromUserId = message[kFromUserId] as? Int
        if (fromUserId != callingUserId) {
            return
        }
        _notifyState(CallStateType.Prepared, CallReason.RemoteHangup)
        _notifyEvent(CallEvent.RemoteHangup)
    }

    //MARK: CallApiProtocol
    override fun getCallId(): String {
        _reportMethod("getCallId")
        return callId
    }

    override fun initialize(config: CallConfig,
                            token: CallTokenConfig,
                            completion: (AGError?) -> Unit) {
        _reportMethod("initialize", "appId=${config.appId}&userId=${config.userId}&mode=${config.mode.value}&role=${config.role.value}&autoAccept=${config.autoAccept}&roomId=${token.roomId}&rtcToken=${token.rtcToken}&rtmToken=${token.rtmToken}")
        if (state != CallStateType.Idle) {
            Log.e(TAG, "must invoke 'deinitialize' to clean state")
            return
        }
        this.config = config
        tokenConfig = token

        this.messageManager?.logout()
        this.messageManager = CallMessageManager(context, config, this, this)
        //纯1v1需要设置成caller
        if (config.mode == CallMode.Pure1v1 && config.role == CallRole.CALLEE) {
            config.role = CallRole.CALLER
        }
        //被叫需要自动加入rtm
        if (config.role == CallRole.CALLER) {
            completion(null)
            return
        }
        prepareForCall(PrepareConfig.calleeConfig()) { error ->
            completion(error)
        }
    }

    override fun deinitialize(completion: (() -> Unit)) {
        _reportMethod("deinitialize")
        Log.d(TAG, "deinitialize")
        val callingRoomId = this.callingRoomId
        if (callingRoomId != null) {
            var roomId = callingRoomId
            //秀场转1v1主叫挂断的是房主id
            _hangup(roomId) { err, msg ->
                _deinitialize()
                completion.invoke()
            }
        } else {
            _deinitialize()
            completion.invoke()
        }
    }

    override fun renewToken(config: CallTokenConfig) {
        _reportMethod("renewToken", "roomId=${config.roomId}&rtcToken=${config.rtcToken}&rtmToken=${config.rtmToken}")
        if (tokenConfig?.roomId != config.roomId) {
            Log.e(TAG, "renewToken failed, roomid missmatch")
        }
        Log.e(TAG, "renewToken with roomId[${config.roomId}]")
        this.tokenConfig = config
        messageManager?.renewToken(config.rtmToken)
        val connection = rtcConnection
        if (connection == null || connection.channelId != config.roomId) {
            Log.e(TAG, "renewToken fail! connection.channelId[${rtcConnection?.channelId}] != config.roomId[${config.roomId}]")
            return
        }
        val options = ChannelMediaOptions()
        options.token = config.rtcToken
        val ret = this.config?.rtcEngine?.updateChannelMediaOptionsEx(options, connection)
        Log.e(TAG, "rtc[${config.roomId}] renewToken ret = $ret")
    }

    override fun renewRemoteCallerChannelToken(roomId: String, token: String) {
        _reportMethod("renewRemoteCallerChannelToken", "roomId=$roomId&token=$token")
        val connection = rtcConnection
        val engine = config?.rtcEngine
        if (connection != null && connection.channelId == roomId && engine != null) else {
            return
        }
        val options = ChannelMediaOptions()
        options.token = token
        val ret = engine.updateChannelMediaOptionsEx(options, connection)
        Log.e(TAG, "rtc[$roomId] renewRemoteCallerChannelToken ret = $ret")
    }

    override fun prepareForCall(prepareConfig: PrepareConfig, completion: ((AGError?) -> Unit)?) {
        _reportMethod("prepareForCall", "autoLoginRTM=${prepareConfig.autoLoginRTM}&autoSubscribeRTM=${prepareConfig.autoSubscribeRTM}&autoJoinRTC=${prepareConfig.autoJoinRTC}")
        _prepareForCall(prepareConfig, 3, completion)
    }

    override fun addListener(listener: ICallApiListener) {
        _reportMethod("addListener")
        if (delegates.contains(listener)) {
            return
        }
        delegates.add(listener)
    }

    override fun removeListener(listener: ICallApiListener) {
        _reportMethod("removeListener")
        delegates.remove(listener)
    }

    override fun call(roomId: String, remoteUserId: Int, completion: ((AGError?) -> Unit)?) {
        _reportMethod("call", "roomId=$roomId&remoteUserId=$remoteUserId")
        val fromRoomId = tokenConfig?.roomId ?: run {
            runOnUiThread { completion?.invoke(AGError("call fail! config or roomId is empty", -1)) }
            Log.e(TAG, "call fail! config or roomId is empty")
            return
        }
        if (messageManager?.isSubscribedRTM == false) {
            Log.e(TAG, "call need to init rtm")
            _prepareForCall(PrepareConfig.calleeConfig()) { error ->
                if (error != null) {
                    runOnUiThread { completion?.invoke(error) }
                } else {
                    call(roomId, remoteUserId, completion)
                }
            }
            return
        }
        callTs = _getNtpTimeInMs()
        //发送呼叫消息
        callId = UUID.randomUUID().toString()
        val message = _messageDic(CallAction.Call).toMutableMap()
        message[kRemoteUserId] = remoteUserId
        message[kFromRoomId] = fromRoomId
        messageManager?.sendMessage(roomId, fromRoomId, message.toMap()) { err ->
            if (err != null) {
                _notifyState(CallStateType.Prepared, CallReason.MessageFailed, err.msg)
                _notifyEvent(CallEvent.MessageFailed)
                return@sendMessage
            }
            timeProfiling("1.呼叫-呼叫回调")
        }
        _notifyState(CallStateType.Calling, eventInfo = message)
        _notifyEvent(CallEvent.OnCalling)

        callingRoomId = roomId
        callingUserId = remoteUserId
        //不等响应即加入频道，加快join速度，失败则leave
        _joinRTCAndNotify(fromRoomId, tokenConfig?.rtcToken ?: "")
    }

    override fun cancelCall(completion: ((AGError?) -> Unit)?) {
        _reportMethod("cancelCall")
        val roomId = callingRoomId ?: run {
            Log.e(TAG, "cancelCall fail! callingRoomId is empty")
            completion?.invoke(AGError("cancelCall fail! callingRoomId is empty", -1))
            return
        }
        val fromRoomId = tokenConfig?.roomId ?: run {
            Log.e(TAG, "cancelCall fail! callingRoomId is empty")
            completion?.invoke(AGError("cancelCall fail! callingRoomId is empty", -1))
            return
        }
        val message = _messageDic(CallAction.CancelCall)
        messageManager?.sendMessage(roomId, fromRoomId, message) { err ->
        }
        _notifyState(CallStateType.Prepared, CallReason.LocalCancel)
        _notifyEvent(CallEvent.LocalCancel)
    }
    //接受
    override fun accept(roomId: String, remoteUserId: Int, rtcToken: String, completion: ((AGError?) -> Unit)?) {
        _reportMethod("accept", "roomId=$roomId&remoteUserId=$remoteUserId&rtcToken=$rtcToken")
        val fromRoomId = tokenConfig?.roomId ?: run {
            val errReason = "accept fail! current userId or roomId is empty"
            completion?.invoke(AGError(errReason, -1))
            Log.e(TAG, errReason)
            _notifyState(CallStateType.Prepared, CallReason.MessageFailed, errReason)
            _notifyEvent(CallEvent.MessageFailed)
            return
        }
        //查询是否是calling状态，如果是prapared，表示可能被取消了
        if (state == CallStateType.Calling) else {
            val errReason = "accept fail! current state is not calling"
            completion?.invoke(AGError(errReason, -1))
            Log.e(TAG, errReason)
            _notifyState(CallStateType.Prepared, CallReason.None, errReason)
            _notifyEvent(CallEvent.StateMismatch)
            return
        }
        //先查询presence里是不是正在呼叫的被叫是自己，如果是则不再发送消息
        val message = _messageDic(CallAction.Accept).toMutableMap()
        message[kRemoteUserId] = remoteUserId
        message[kFromRoomId] = fromRoomId
        messageManager?.sendMessage(roomId, fromRoomId, message.toMap()) { err ->
        }
        _notifyState(CallStateType.Connecting, CallReason.LocalAccepted, eventInfo = message)
        _notifyEvent(CallEvent.LocalAccepted)

        callingRoomId = roomId
        callingUserId = remoteUserId

        callTs = _getNtpTimeInMs()
        //不等响应即加入频道，加快join速度，失败则leave
        _joinRTCAndNotify(roomId, rtcToken)
    }

    override fun reject(roomId: String,
                        remoteUserId: Int,
                        reason: String?,
                        completion: ((AGError?) -> Unit)?) {
        _reportMethod("reject", "roomId=$roomId&remoteUserId=$remoteUserId&reason=$reason")
        _reject(roomId, remoteUserId, reason)
        _notifyState(CallStateType.Prepared, CallReason.LocalRejected)
        _notifyEvent(CallEvent.LocalRejected)
    }

    override fun hangup(roomId: String, completion: ((AGError?) -> Unit)?) {
        _reportMethod("hangup", "roomId=$roomId")
        _hangup(roomId)
        _notifyState(CallStateType.Prepared, CallReason.LocalHangup)
        _notifyEvent(CallEvent.LocalHangup)
    }
    override fun addRTCListener(listener: IRtcEngineEventHandler) {
        _reportMethod("addRTCListener")
        rtcProxy.addListener(listener)
    }
    override fun removeRTCListener(listener: IRtcEngineEventHandler) {
        _reportMethod( "removeRTCListener")
        rtcProxy.removeListener(listener)
    }

    //MARK: AgoraRtmClientDelegate
    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        _notifyTokenPrivilegeWillExpire()
    }
    override fun onMessageEvent(event: MessageEvent?) {
        val message = event?.message?.data as? ByteArray ?: return
        val jsonString = String(message, Charsets.UTF_8)
        Log.d(TAG, "on event message: $jsonString")
        val map = jsonStringToMap(jsonString)
        val messageAction = map[kMessageAction] as? Int ?: 0
        val msgTs = map[kMessageTs] as? Long
        val userId = map[kFromUserId] as? Int
        val messageVersion = map[kMessageVersion] as? String
        if (messageVersion == null || msgTs == null || userId == null) {
            Log.e(TAG, "fail to parse message: $jsonString")
            return
        }
        if (kCurrentMessageVersion != messageVersion)  {
            //TODO: compatible other message version
            return
        }
        val origMsgTs = recvMessageTsMap[userId] ?: 0
        //对应用户的消息拦截老的消息
        if (origMsgTs > msgTs) {
            Log.d(TAG, "ignore old message by user[$userId], msg: $jsonString")
            return
        }
        //prepared完成之前的消息全部忽略
        if (preparedTs > msgTs) {
            Log.d(TAG, "ignore old message before prepare state, msg: $jsonString")
            return
        }
        recvMessageTsMap[userId] = msgTs
        _process(CallAction.fromValue(messageAction), map)
    }
    override fun onPresenceEvent(event: PresenceEvent?) {}
    override fun onTopicEvent(event: TopicEvent?) {}
    override fun onLockEvent(event: LockEvent?) {}
    override fun onStorageEvent(event: StorageEvent?) {}
    override fun onConnectionStateChange(
        channelName: String?,
        state: RtmConstants.RtmConnectionState?,
        reason: RtmConstants.RtmConnectionChangeReason?) {}

    // IRtcEngineEventHandler
    override fun onConnectionStateChanged(state: Int, reason: Int) {
        super.onConnectionStateChanged(state, reason)
        Log.d(TAG, "connectionChangedTo state: $state reason: $reason")
    }
    override fun onUserJoined(uid: Int, elapsed: Int) {
        super.onUserJoined(uid, elapsed)
        Log.d(TAG, "didJoinedOfUid: $uid elapsed: $elapsed")
        if (callingUserId == uid) else return
        val roomId = callingRoomId ?: ""
        val view = config?.remoteView
        if (roomId.isEmpty() || view == null) {
            return
        }
        timeProfiling("5.呼叫-对端 [$uid] 加入房间")
        _setupRemoteVideo(roomId, uid, view)

        _notifyEvent(CallEvent.RemoteJoin, _getNtpTimeInMs() - (callTs ?: 0))
    }
    override fun onUserOffline(uid: Int, reason: Int) {
        Log.d(TAG, "didOfflineOfUid: $uid")
        if (callingUserId != uid) {
            return
        }
        _notifyEvent(CallEvent.RemoteLeave)
    }
    override fun onLeaveChannel(stats: RtcStats?) {
        Log.d(TAG, "didLeaveChannel: $stats")
        isChannelJoined = false
        _notifyEvent(CallEvent.LocalLeave)
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        super.onJoinChannelSuccess(channel, uid, elapsed)
        Log.d(TAG, "onJoinChannelSuccess: $uid elapsed: $elapsed")
        if (uid == config?.userId) else {
            return
        }
        isChannelJoined = true
        joinRtcCompletion?.invoke(null)
        joinRtcCompletion = null

        _notifyEvent(CallEvent.LocalJoin, _getNtpTimeInMs() - (callTs ?: 0))
    }

    override fun onError(err: Int) {
        super.onError(err)
        Log.d(TAG, "rtc join channel onError: $err")
        joinRtcCompletion?.invoke(AGError("join RTC fail", err))
        joinRtcCompletion = null
    }

    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
        val channelId = tokenConfig?.roomId ?: ""
        if (uid == callingUserId) else {return}
        Log.d(TAG, "didLiveRtcRemoteVideoStateChanged channelId: $channelId/${callingRoomId ?: ""} uid: $uid/${callingUserId ?: 0} state: $state reason: $reason")
        if ((state == 2) && (reason == 6 || reason == 4 || reason == 3 )) {
            timeProfiling("6.呼叫-收到对端[$uid] 首帧")
            firstFrameCompletion?.invoke()
        }
    }

    // CallMessageListener
    override fun onMissReceipts(message: Map<String, Any>) {
        Log.d(TAG, "onMissReceipts: $message")
        _notifyEvent(CallEvent.MissingReceipts)
    }

    private fun jsonStringToMap(jsonString: String): Map<String, Any> {
        val json = JSONObject(jsonString)
        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.get(key)
        }
        return map
    }

    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            runnable.run()
        } else {
            mHandler.post(runnable)
        }
    }
}

