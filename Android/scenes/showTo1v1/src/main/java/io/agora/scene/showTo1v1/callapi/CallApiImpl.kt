package io.agora.scene.showTo1v1.callapi

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtm.*
import io.agora.scene.showTo1v1.BuildConfig
import io.agora.scene.showTo1v1.callapi.extension.cloneConfig
import org.json.JSONObject
import java.util.*

enum class CallAutoSubscribeType(val value: Int) {
    None(0),
    Video(1),
    AudioVideo(2),
}
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

/*
 * 被叫呼叫中加入RTC的策略
 */
enum class CalleeJoinRTCPolicy(val value: Int) {
    Calling(0),      //在接到呼叫时即加入频道并推送音视频流，被叫时费用较高但出图更快
    Accepted(1)      //在点击接受后才加入频道并推送音视频流，被叫时费用较低但出图较慢
}

class CallApiImpl constructor(
    context: Context
): ICallApi, RtmEventListener, CallMessageListener, IRtcEngineEventHandler() {

    companion object {
        val calleeJoinRTCPolicy = CalleeJoinRTCPolicy.Calling
        const val kReportCategory = "2_Android_1.0.0"
        const val kPublisher = "publisher"
        const val kCostTimeMap = "costTimeMap"    //呼叫时的耗时信息，会在connected时抛出分步耗时
        const val kRemoteUserId = "remoteUserId"
        const val kFromUserId = "fromUserId"
        const val kFromRoomId = "fromRoomId"
        const val kFromUserExtension = "fromUserExtension"

        //⚠️不允许修改下列两项值，客户可能会根据该rejectReason/call busy 来做业务判断(例如用户忙)
        const val kRejectReason = "rejectReason"
        const val kRejectReasonCallBusy = "The user is currently busy"
        //是否内部拒绝，收到内部拒绝目前标记为对端call busy
        const val kRejectByInternal = "rejectByInternal"

        const val kHangupReason = "hangupReason"
    }

    private val kCallTimeoutInterval: Long = 15000
    private val kCurrentMessageVersion = "1.0"
    private val kMessageAction = "message_action"
    private val kMessageVersion = "message_version"
    private val kMessageTs = "message_timestamp"
    private val kCallId = "callId"

    private val TAG = "CallApiImpl_LOG"
    private val delegates = mutableListOf<ICallApiListener>()
    private val rtcProxy = CallProxy()
    private val localFrameProxy: CallLocalFirstFrameProxy by lazy { CallLocalFirstFrameProxy(this) }
    private var config: CallConfig? = null
    private var prepareConfig: PrepareConfig? = null
    private var messageManager: CallMessageManager? = null
        set(value) {
            val oldValue = field
            field = value
            oldValue?.deinitialize()
        }
    private var connectInfo = CallConnectInfo()
    private var reportInfoList = listOf<CallReportInfo>()
    private var isChannelJoined = false

    var tempRemoteCanvasView = TextureView(context)
    var tempLocalCanvasView = TextureView(context)

    /// 当前状态
    private var state: CallStateType = CallStateType.Idle
        set(value) {
            val prevState = field
            field = value
            if (prevState == value) { return }
            tempRemoteCanvasView.alpha = 0f
            when(value) {
                CallStateType.Calling -> {
                    // 如果prepareConfig?.callTimeoutSeconds == 0，内部不做超时
                    val timeout = prepareConfig?.callTimeoutMillisecond ?: 0L
                    if (timeout <= 0L) {
                        return
                    }
                    // 开启定时器，如果超时无响应，调用no response
                    connectInfo.scheduledTimer({
                        _cancelCall {  }
                        _updateAndNotifyState(CallStateType.Prepared, CallStateReason.CallingTimeout)
                        _notifyEvent(CallEvent.CallingTimeout)
                    }, timeout)
                }
                CallStateType.Prepared -> {
                    connectInfo.scheduledTimer(null)
                    if (prevState != CallStateType.Idle) {
                        _prepareForCall(prepareConfig!!) {
                        }
                    }
                }
                CallStateType.Connecting -> {
                    _updateAutoSubscribe(CallAutoSubscribeType.AudioVideo)
                }
                CallStateType.Connected -> {
                    tempRemoteCanvasView.alpha = 1f
                    connectInfo.scheduledTimer(null)
                }
                CallStateType.Idle, CallStateType.Failed -> {
                    _leaveRTC()
                    connectInfo.clean()
                    config = null
                    isPreparing = false
                    messageManager = null
                }
                else -> {}
            }
        }
    /// join channel ex的connection，用来leave channel ex和判断是否已经加入ex channel
    private var rtcConnection: RtcConnection? = null
    //加入RTC完成回调
    private var joinRtcCompletion: ((AGError?) -> Unit)? = null
    //首帧出图回调
    private var firstFrameCompletion: (() -> Unit)? = null

    private var isPreparing = false

    init {
        callPrint("init-- CallApiImpl")
        rtcProxy.addListener(this)
    }
    //获取ntp时间
    private fun _getTimeInMs(): Long {
        return System.currentTimeMillis()
    }

    private fun _getCost(ts: Int? = null): Long {
        val cts = connectInfo.callTs ?: return 0
        return if (ts != null) {
            ts - cts
        } else {
            _getTimeInMs() - cts
        }
    }

    private fun _messageDic(action: CallAction): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            kMessageAction to action.value,
            kMessageVersion to kCurrentMessageVersion,
            kMessageTs to _getTimeInMs(),
            kFromUserId to (config?.userId ?: 0),
            kCallId to connectInfo.callId
        )
        prepareConfig?.userExtension?.let {
            map[kFromUserExtension] = it
        }
        return map
    }

    private fun _callMessageDic(remoteUserId: Int, fromRoomId: String): Map<String, Any> {
        val message = _messageDic(CallAction.Call).toMutableMap()
        message[kRemoteUserId] = remoteUserId
        message[kFromRoomId] = fromRoomId
        return message
    }

    private fun _rejectMessageDic(reason: String?, rejectByInternal: Boolean): Map<String, Any> {
        val message = _messageDic(CallAction.Reject).toMutableMap()
        message[kRejectReason] = reason ?: ""
        message[kRejectByInternal] = if (rejectByInternal) 1 else 0
        return message
    }

    private fun _hangupMessageDic(reason: String?): Map<String, Any> {
        val message = _messageDic(CallAction.Hangup).toMutableMap()
        message[kHangupReason] = reason ?: ""
        return message
    }

    private fun _notifyTokenPrivilegeWillExpire() {
        delegates.forEach { listener ->
            listener.tokenPrivilegeWillExpire()
        }
    }

    private fun checkConnectedSuccess(reason: CallStateReason) {
        if (connectInfo.isRetrieveFirstFrame && state == CallStateType.Connecting) else {return}
        /*
         1.因为被叫提前加频道并订阅流和推流，导致双端收到视频首帧可能会比被叫点accept(变成connecting)比更早
         2.由于匹配1v1时双端都会收到onCall，此时A发起accept，B收到了onAccept+A首帧，会导致B未接受即进入了connected状态
         因此:
         变成connecting: 需要同时检查是否变成了“远端已接受” + “本地已接受(或已发起呼叫)”
         变成connected: 需要同时检查是否是"connecting状态" + “收到首帧”
         */
        _changeToConnectedState(reason)
    }

    private fun _changeToConnectedState(reason: CallStateReason) {
        val eventInfo = mapOf(
            kFromRoomId to (connectInfo.callingRoomId ?: ""),
            kFromRoomId to (connectInfo.callingRoomId ?: ""),
            kFromUserId to (connectInfo.callingUserId ?: 0),
            kRemoteUserId to (config?.userId ?: 0),
            kCostTimeMap to connectInfo.callCostMap
        )
        _updateAndNotifyState(CallStateType.Connected, reason, eventInfo = eventInfo)
//        _notifyEvent(event: CallReason.RecvRemoteFirstFrame, elapsed: elapsed)
    }
    //外部状态通知
    private fun _updateAndNotifyState(state: CallStateType,
                                      stateReason: CallStateReason = CallStateReason.None,
                                      eventReason: String = "",
                                      eventInfo: Map<String, Any> = emptyMap()) {
        callPrint("call change[${connectInfo.callId}] state: $state, stateReason: '$stateReason', eventReason: $eventReason")
        this.state = state
        delegates.forEach {
            it.onCallStateChanged(state, stateReason, eventReason, eventInfo)
        }
    }

    private fun _notifySendMessageErrorEvent(error: AGError, reason: String?) {
        _notifyErrorEvent(
            CallErrorEvent.SendMessageFail,
            errorType = CallErrorCodeType.Rtm,
            errorCode = error.code,
            message = "${reason ?: ""}${error.msg}"
        )
    }

    private fun _notifyRtcOccurErrorEvent(errorCode: Int, message: String? = null) {
        _notifyErrorEvent(
            CallErrorEvent.RtcOccurError,
            errorType =  CallErrorCodeType.Rtc,
            errorCode =  errorCode,
            message =  message
        )
    }

    private fun _notifyErrorEvent(
        errorEvent: CallErrorEvent,
        errorType: CallErrorCodeType,
        errorCode: Int,
        message: String?) {
        callPrint("call change[${connectInfo.callId} errorEvent: ${errorEvent.value}, errorType: ${errorType.value}, errorCode: ${errorCode}, message: ${message ?: ""}")
        delegates.forEach { listener ->
            listener.onCallError(errorEvent, errorType, errorCode, message)
        }
    }

    private fun _notifyEvent(event: CallEvent, eventReason: String? = null) {
        callPrint("call change[${connectInfo.callId}] event: ${event.value} reason: '$eventReason'")
        config?.let { config ->
            var reason = ""
            if (eventReason != null) {
                reason = "&reason=$eventReason"
            }
            _reportEvent("event=${event.value}&userId=${config.userId}&state=${state.name}$reason", 0)
        } ?: callWarningPrint("_notifyEvent config == null")
        delegates.forEach { listener ->
            listener.onCallEventChanged(event)
        }
        when (event) {
            CallEvent.RemoteUserRecvCall -> _reportCostEvent(CallConnectCostType.RemoteUserRecvCall)
            CallEvent.RemoteJoin -> _reportCostEvent(CallConnectCostType.RemoteUserJoinChannel)
            CallEvent.LocalJoin -> _reportCostEvent(CallConnectCostType.LocalUserJoinChannel)
            CallEvent.RemoteAccepted -> {
                _reportCostEvent(CallConnectCostType.AcceptCall)
                checkConnectedSuccess(CallStateReason.RemoteAccepted)
            }
            CallEvent.LocalAccepted -> {
                _reportCostEvent(CallConnectCostType.AcceptCall)
                checkConnectedSuccess(CallStateReason.LocalAccepted)
            }
            CallEvent.RecvRemoteFirstFrame -> {
                _reportCostEvent(CallConnectCostType.RecvFirstFrame)
                checkConnectedSuccess(CallStateReason.RecvRemoteFirstFrame)
            }
            else -> {}
        }
    }

    private fun _prepareForCall(prepareConfig: PrepareConfig, completion: ((AGError?) -> Unit)?) {
        val cfg = config
        if (cfg == null) {
            val reason = "config is Empty"
            callWarningPrint(reason)
            completion?.invoke(AGError(reason, -1))
            return
        }
        if (isPreparing) {
            val reason = "is already in preparing"
            callWarningPrint(reason)
            completion?.invoke(AGError(reason, -1))
            return
        }
        var enableLoginRtm = true
        when (state) {
            CallStateType.Calling, CallStateType.Connecting, CallStateType.Connected -> {
                val reason = "currently busy"
                callWarningPrint(reason)
                completion?.invoke(AGError(reason, -1))
                return
            }
            CallStateType.Prepared -> {
                enableLoginRtm = false
            }
            CallStateType.Failed, CallStateType.Idle -> {
            }
        }
        connectInfo.clean()

        val tag = UUID.randomUUID().toString()
        callPrint("prepareForCall[$tag]")
        this.prepareConfig = prepareConfig.cloneConfig()

        //join rtc if need
        if (prepareConfig.autoJoinRTC) {
            _joinRTCWithMediaOptions(prepareConfig.roomId, Constants.CLIENT_ROLE_AUDIENCE, CallAutoSubscribeType.Video) { err ->
                callWarningPrint("prepareForCall[$tag] joinRTC completion: ${err?.msg ?: "success"}")
                if (err != null) {
                    _notifyRtcOccurErrorEvent(err.code, err.msg)
                } else {
                    _notifyEvent(CallEvent.JoinRTCSuccessed)
                }
            }
        } else {
            _leaveRTC()
        }
        //login rtm if need
        if (enableLoginRtm) {
            isPreparing = true
            val messageManager = CallMessageManager(cfg, this)
            this.messageManager = messageManager

            messageManager.initialize(prepareConfig) { err ->
                isPreparing = false
                callWarningPrint("prepareForCall[$tag] rtmInitialize completion: ${err?.msg ?: "success"})")
                if (err != null) {
                    _notifyErrorEvent(CallErrorEvent.RtmSetupFail, CallErrorCodeType.Rtm, err.code, err.msg)
                } else {
                    _notifyEvent(CallEvent.RtmSetupSuccessed)
                }
                completion?.invoke(err)
            }
        } else {
            completion?.invoke(null)
        }
        // 和iOS不同，Android先将渲染视图TextureView添加进传进来的容器
        setupTextureView()
    }
    private fun setupTextureView() {
        val prepareConfig = prepareConfig ?: return
        runOnUiThread {
            // 添加远端渲染视图
            prepareConfig.remoteView?.let { remoteView ->
                (tempRemoteCanvasView.parent as? ViewGroup)?.let { parentView ->
                    if (parentView != remoteView) {
                        parentView.removeView(tempRemoteCanvasView)
                    }
                }
                if (remoteView.indexOfChild(tempRemoteCanvasView) == -1) {
                    tempRemoteCanvasView.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    remoteView.addView(tempRemoteCanvasView)
                } else {
                    callWarningPrint("remote view not found in connected state!")
                }
            }
            // 添加本地渲染视图
            prepareConfig.localView?.let { localView ->
                (tempLocalCanvasView.parent as? ViewGroup)?.let { parentView ->
                    if (parentView != localView) {
                        parentView.removeView(tempLocalCanvasView)
                    }
                }
                if (localView.indexOfChild(tempLocalCanvasView) == -1) {
                    tempLocalCanvasView.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    localView.addView(tempLocalCanvasView)
                } else {
                    callWarningPrint("remote view not found in connected state!")
                }
            }
        }
    }
    private fun _deinitialize() {
        _updateAndNotifyState(CallStateType.Idle)
        _notifyEvent(CallEvent.Deinitialize)
    }
    private fun _setupRemoteVideo(uid: Int, view: TextureView) {
        val engine = config?.rtcEngine ?: return
        val connection = rtcConnection ?: run {
            callWarningPrint("_setupRemoteVideo fail: connection or engine is empty")
            return
        }
        val videoCanvas = VideoCanvas(view)
        videoCanvas.uid = uid
        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN
        videoCanvas.mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
        val ret = engine.setupRemoteVideoEx(videoCanvas, connection)
        callPrint("_setupRemoteVideo ret: $ret, channelId: ${connection.channelId}, uid: $uid")
    }

    private fun _setupLocalVideo(uid: Int, view: TextureView) {
        val engine = config?.rtcEngine ?: run {
            callWarningPrint("_setupLocalVideo fail: engine is empty")
            return
        }
        config?.rtcEngine?.addHandler(localFrameProxy)

        val videoCanvas = VideoCanvas(view)
        videoCanvas.uid = uid
        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN
        videoCanvas.mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO

        engine.setDefaultAudioRoutetoSpeakerphone(true)
        engine.setupLocalVideo(videoCanvas)
        val ret = engine.startPreview()
        if (ret != 0) {
            _notifyErrorEvent(CallErrorEvent.StartCaptureFail, CallErrorCodeType.Rtc, ret, null)
        }
    }

    /// 判断当前加入的RTC频道和传入的房间id是否一致
    /// - Parameter roomId: <#roomId description#>
    /// - Returns: <#description#>
    private fun _isCurrentRTCChannel(roomId: String): Boolean {
        return rtcConnection?.channelId == roomId
    }

    /// 当前RTC频道是否加入成功或者正在加入中
    /// - Returns: <#description#>
    private fun _isChannelJoinedOrJoining(): Boolean {
        return rtcConnection != null
    }

    /// 是否初始化完成
    /// - Returns: <#description#>
    private fun _isInitialized(): Boolean {
        return when (state) {
            CallStateType.Idle, CallStateType.Failed -> false
            else -> true
        }
    }

    /// 是否可以继续呼叫
    /// - Parameter callerUserId: <#callerUserId description#>
    /// - Returns: <#description#>
    private fun _isCallActive(callerUserId: Int): Boolean {
        when (state) {
            CallStateType.Prepared -> return true
            CallStateType.Idle, CallStateType.Failed -> return false
            CallStateType.Calling, CallStateType.Connecting, CallStateType.Connected -> {
                if ((connectInfo.callingUserId ?: 0) == callerUserId) {
                    return true
                }
            }
        }
        return false
    }

    private fun _isCallingUser(message: Map<String, Any>) : Boolean {
        val fromUserId = message[kFromUserId] as? Int ?: return false
        if (connectInfo.callingUserId != fromUserId) return false
        return true
    }

    private fun _joinRTCWithMediaOptions(roomId: String, role: Int, subscribeType: CallAutoSubscribeType, completion: ((AGError?) -> Unit)) {
        if (!_isCurrentRTCChannel(roomId)) {
            _leaveRTC()
        }
        val isChannelJoinedOrJoining = _isChannelJoinedOrJoining()
        if (isChannelJoinedOrJoining) {
            completion.invoke(null)
        } else {
            _joinRTC(roomId){ error ->
                completion.invoke(error)
            }
        }
        //没有加入频道又是观众的情况下，不需要update role，join默认就是观众和不推流
        if (isChannelJoinedOrJoining || role == Constants.CLIENT_ROLE_BROADCASTER) {
            _updateRole(role)
        }
        _updateAutoSubscribe(subscribeType)
    }

    private fun _joinRTCAsBroadcaster(roomId: String) {
        _joinRTCWithMediaOptions(roomId, Constants.CLIENT_ROLE_BROADCASTER, CallAutoSubscribeType.Video) { error ->
            if (error != null) {
                _notifyRtcOccurErrorEvent(error.code, error.msg)
            } else {
                _notifyEvent(CallEvent.JoinRTCSuccessed)
            }
        }
        setupCanvas()
    }

    private fun _joinRTC(roomId: String, completion:((AGError?) -> Unit)?) {
        val config = this.config
        val rtcToken = prepareConfig?.rtcToken
        if (config == null || rtcToken == null) {
            completion?.invoke(AGError("config is empty", -1))
            return
        }
        val connection = RtcConnection(roomId, config.userId)
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.publishCameraTrack = false
        mediaOptions.publishMicrophoneTrack = false
        mediaOptions.autoSubscribeAudio = false
        mediaOptions.autoSubscribeVideo = false
        val ret: Int = config.rtcEngine?.joinChannelEx(rtcToken, connection, mediaOptions, rtcProxy) ?: 0
        callPrint("joinRTC channel roomId: $roomId uid: ${config.userId} ret = $ret")
        rtcConnection = connection
        joinRtcCompletion = {
            completion?.invoke(null)
        }
        firstFrameCompletion = {
            connectInfo.isRetrieveFirstFrame = true
            _notifyEvent(CallEvent.RecvRemoteFirstFrame)
        }
        if (ret != Constants.ERR_OK) {
            _notifyRtcOccurErrorEvent(ret)
        }
    }

    /// 切换主播和观众角色
    /// - Parameter role: <#role description#>
    private fun _updateRole(role: Int) {
        val config = config
        val connection = rtcConnection
        if (config == null || connection == null) { return}
        callPrint("_updateRole: $role")

        //需要先开启音视频，使用enableLocalAudio而不是enableAudio，否则会导致外部mute的频道变成unmute
        if (role == Constants.CLIENT_ROLE_BROADCASTER) {
            config.rtcEngine?.enableLocalAudio(true)
            config.rtcEngine?.enableLocalVideo(true)
        } else {
            config.rtcEngine?.enableLocalAudio(false)
            config.rtcEngine?.enableLocalVideo(false)
        }
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.clientRoleType = role
        val isBroadcaster = (role == Constants.CLIENT_ROLE_BROADCASTER)
        mediaOptions.publishCameraTrack = isBroadcaster
        mediaOptions.publishMicrophoneTrack = isBroadcaster
        config.rtcEngine?.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    /// 更换订阅音视频流策略
    /// - Parameter type: <#type description#>
    private fun _updateAutoSubscribe(type: CallAutoSubscribeType) {
        val config = config ?: run { return }
        val connection = rtcConnection ?: run { return }
        callPrint("_updateAutoSubscribe: ${type.value}")
        val mediaOptions = ChannelMediaOptions()
        when (type) {
            CallAutoSubscribeType.None -> {
                mediaOptions.autoSubscribeAudio = false
                mediaOptions.autoSubscribeVideo = false
            }
            CallAutoSubscribeType.Video -> {
                mediaOptions.autoSubscribeAudio = false
                mediaOptions.autoSubscribeVideo = true
            }
            CallAutoSubscribeType.AudioVideo -> {
                mediaOptions.autoSubscribeAudio = true
                mediaOptions.autoSubscribeVideo = true
            }
        }
        config.rtcEngine?.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    private fun _leaveRTC() {
        joinRtcCompletion = null
        val connection = rtcConnection ?: run {
            callWarningPrint("leave RTC channel failed, not joined the channel")
            return
        }
        config?.rtcEngine?.stopPreview()
        val ret = config?.rtcEngine?.leaveChannelEx(connection)
        callPrint("leave RTC channel[${ret ?: -1}]")
        rtcConnection = null
    }

    private fun setupCanvas() {
        val config = config ?: return
        _setupLocalVideo(config.userId, tempLocalCanvasView)
        val callingUserId = connectInfo.callingUserId ?: run {
            callWarningPrint("join rtc fail: callingUserId == nil")
            return
        }
        _setupRemoteVideo(callingUserId, tempRemoteCanvasView)
    }

    private fun _flushReport() {
        reportInfoList.forEach { info ->
            _sendCustomReportMessage(info.msgId, info.category, info.event, info.label, info.value)
        }
        reportInfoList = emptyList()
    }
    private fun _reportCostEvent(type: CallConnectCostType) {
        val cost = _getCost()
        connectInfo.callCostMap[type.value] = cost
        _reportEvent(type.value, cost.toInt())
    }

    private fun _reportMethod(event: String, label: String = "") {
        val msgId = "scenarioAPI"
        callPrint("_reportMethod event: $event")
        var subEvent = event
        val range = event.indexOf("(")
        if (range != -1) {
            subEvent = event.substring(0, range)
        }
        var labelValue = "callId=${connectInfo.callId}&ts=${_getTimeInMs()}"
        if (label.isNotEmpty()) {
            labelValue = "$label&$labelValue"
        }
        if (isChannelJoined) {
            _sendCustomReportMessage(msgId, kReportCategory, subEvent, labelValue, 0)
            return
        }

        val info = CallReportInfo(msgId, kReportCategory, subEvent, labelValue, 0)
        val temp = reportInfoList.toMutableList()
        temp.add(info)
        reportInfoList = temp.takeLast(10)
        // callPrint("sendCustomReportMessage not join channel cache it! event: $subEvent label: $labelValue")
    }

    private fun _reportEvent(key: String, value: Int) {
        val config = config ?: return
        val msgId = "uid=${config.userId}&roomId=${connectInfo.callingRoomId ?: ""}"
        val label = "callId=${connectInfo.callId})&ts=${_getTimeInMs()}"
        if (isChannelJoined) {
            _sendCustomReportMessage(msgId, kReportCategory, key, label, value)
            return
        }
        val info = CallReportInfo(msgId, kReportCategory, key, label, value)
        val temp = reportInfoList.toMutableList()
        temp.add(info)
        reportInfoList = temp.takeLast(10)
//        callPrint("sendCustomReportMessage not join channel cache it! msgId: $msgId category: $category event: $key label: ${connectInfo.callId} value: $value")
    }

    private fun _sendCustomReportMessage(msgId: String,
                                         category: String,
                                         event: String,
                                         label: String,
                                         value: Int) {
        val c = config
        if (c != null && isChannelJoined && rtcConnection != null) else { return }
        val ret = c.rtcEngine?.sendCustomReportMessageEx(msgId, category, event, label, value, rtcConnection)
        if (BuildConfig.DEBUG) {
            callPrint("sendCustomReportMessage[$ret] msgId:$msgId event:$event label:$label value: $value")
        }
    }

    //MARK: on Message
    private fun _processRespEvent(reason: CallAction, message: Map<String, Any>) {
        when (reason) {
            CallAction.Call ->          _onCall(message)
            CallAction.CancelCall ->    _onCancel(message)
            CallAction.Reject ->        _onReject(message)
            CallAction.Accept ->        _onAccept(message)
            CallAction.Hangup ->        _onHangup(message)
            else -> {}
        }
    }

    private fun _cancelCall(message: Map<String, Any>? = null, completion: ((AGError?) -> Unit)? = null) {
        val userId = connectInfo.callingUserId
        if (userId == null) {
            completion?.invoke(AGError("cancelCall fail! callingRoomId is empty", -1))
            callWarningPrint("cancelCall fail! callingRoomId is empty")
            return
        }
        val msg = message ?: _messageDic(CallAction.CancelCall)
        messageManager?.sendMessage(userId.toString(), msg) { err ->
            completion?.invoke(err)
            if (err != null) {
                _notifySendMessageErrorEvent(err, "cancel call fail: ")
            }
        }
    }

    private fun _reject(remoteUserId: Int, message: Map<String, Any>, completion: ((AGError?) -> Unit)? = null) {
        messageManager?.sendMessage(remoteUserId.toString(), message, completion)
    }

    private fun _hangup(remoteUserId: Int, message: Map<String, Any>? = null, completion: ((AGError?) -> Unit)? = null) {
        messageManager?.sendMessage(remoteUserId.toString(), message ?: _messageDic(CallAction.Hangup), completion)
    }

    //收到呼叫消息
    private fun _onCall(message: Map<String, Any>) {
        val fromRoomId = message[kFromRoomId] as String
        val fromUserId = message[kFromUserId] as Int
        val callId = message[kCallId] as String

        var enableNotify = true
        var autoAccept = false //prepareConfig?.autoAccept ?: false
        when (state) {
            CallStateType.Idle, CallStateType.Failed -> {
                // not reachable
//            _reject(remoteUserId: fromUserId, reason: kRejectReasonCallBusy, true)
                return
            }
            CallStateType.Calling, CallStateType.Connecting, CallStateType.Connected -> {
                if ((connectInfo.callingUserId ?: 0) != fromUserId) {
                    val message = _rejectMessageDic(kRejectReasonCallBusy, rejectByInternal = true)
                    _reject(fromUserId, message)
                    return
                }
                if (state == CallStateType.Calling) {
                    enableNotify = false
                } else {
                    autoAccept = true
                }
            }
            else -> {}
        }

        connectInfo.set(fromUserId, fromRoomId, callId)
        if (enableNotify) {
            _updateAndNotifyState(CallStateType.Calling, CallStateReason.None, eventInfo = message)
            _notifyEvent(CallEvent.OnCalling)
        }
        if(calleeJoinRTCPolicy == CalleeJoinRTCPolicy.Calling) {
            _joinRTCAsBroadcaster(fromRoomId)
        }

        if (!autoAccept) {
            return
        }
        accept(fromUserId) { err ->
        }
    }

    private fun _onCancel(message: Map<String, Any>) {
        //如果不是来自的正在呼叫的用户的操作，不处理
        if (!_isCallingUser(message)) return
        _updateAndNotifyState(CallStateType.Prepared, CallStateReason.RemoteCancel, eventInfo = message)
        _notifyEvent(CallEvent.RemoteCancel)
    }

    private fun _onReject(message: Map<String, Any>) {
        if (!_isCallingUser(message)) return
        var stateReason: CallStateReason =  CallStateReason.RemoteRejected
        var callEvent: CallEvent = CallEvent.RemoteRejected
        val rejectByInternal = message[kRejectByInternal]
        if (rejectByInternal == 1) {
            stateReason = CallStateReason.RemoteCallBusy
            callEvent = CallEvent.RemoteCallBusy
        }

        _updateAndNotifyState(CallStateType.Prepared, stateReason, eventInfo = message)
        _notifyEvent(callEvent)
    }

    private fun _onAccept(message: Map<String, Any>) {
        //需要是calling状态，并且来自呼叫的用户的请求
        if (!_isCallingUser(message) || state != CallStateType.Calling) return
        //并且是isLocalAccepted（发起呼叫或者已经accept过了），否则认为本地没有同意
        if (connectInfo.isLocalAccepted) {
            _updateAndNotifyState(CallStateType.Connecting, CallStateReason.RemoteAccepted, eventInfo = message)
        }
        _notifyEvent(CallEvent.RemoteAccepted)
    }

    private fun _onHangup(message: Map<String, Any>) {
        if (!_isCallingUser(message)) return

        _updateAndNotifyState(CallStateType.Prepared, CallStateReason.RemoteHangup, eventInfo = message)
        _notifyEvent(CallEvent.RemoteHangup)
    }

    //MARK: CallApiProtocol
    override fun getCallId(): String {
        _reportMethod("getCallId")
        return connectInfo.callId
    }

    override fun initialize(config: CallConfig) {
        _reportMethod("initialize", "appId=${config.appId}&userId=${config.userId}")
        if (state != CallStateType.Idle) {
            callWarningPrint("must invoke 'deinitialize' to clean state")
            return
        }
        this.config = config.cloneConfig()
    }

    override fun deinitialize(completion: (() -> Unit)) {
        _reportMethod("deinitialize")
        when (state) {
            CallStateType.Calling -> {
                cancelCall { err ->
                    _deinitialize()
                    completion.invoke()
                }
            }
            CallStateType.Connecting, CallStateType.Connected -> {
                val callingUserId = connectInfo.callingUserId ?: 0
                _hangup(callingUserId) { err ->
                    _deinitialize()
                    completion.invoke()
                }
            }
            else -> {
                _deinitialize()
                completion.invoke()
            }
        }
    }

    override fun renewToken(rtcToken: String, rtmToken: String) {
        _reportMethod("renewToken", "&rtcToken=${rtcToken}&rtmToken=${rtmToken}")
        val roomId = prepareConfig?.roomId
        if (roomId == null) {
            callWarningPrint("renewToken failed, roomid missmatch")
            return
        }
        prepareConfig?.rtcToken = rtcToken
        prepareConfig?.rtmToken = rtmToken
        callPrint("renewToken with roomId[$roomId]")
        messageManager?.renewToken(rtcToken, rtmToken)
        val connection = rtcConnection ?: return
        val options = ChannelMediaOptions()
        options.token = rtcToken
        val ret = this.config?.rtcEngine?.updateChannelMediaOptionsEx(options, connection)
        callPrint("rtc[$roomId] renewToken ret = ${ret ?: -1}")
    }

    override fun onFirstLocalVideoFramePublished(source: Constants.VideoSourceType?, elapsed: Int) {
        super.onFirstLocalVideoFramePublished(source, elapsed)
        _notifyEvent(event = CallEvent.PublishFirstLocalVideoFrame, eventReason = "elapsed: ${elapsed}ms")
    }

    override fun onFirstLocalVideoFrame(
        source: Constants.VideoSourceType?,
        width: Int,
        height: Int,
        elapsed: Int
    ) {
        super.onFirstLocalVideoFrame(source, width, height, elapsed)
        _notifyEvent(event = CallEvent.CaptureFirstLocalVideoFrame, eventReason = "elapsed: ${elapsed}ms")
        config?.rtcEngine?.removeHandler(localFrameProxy)
    }

    override fun prepareForCall(prepareConfig: PrepareConfig, completion: ((AGError?) -> Unit)?) {
        _reportMethod("prepareForCall", "roomId=${prepareConfig.roomId}&autoJoinRTC=${prepareConfig.autoJoinRTC}")
        _prepareForCall(prepareConfig) { err ->
            if (err != null) {
                _updateAndNotifyState(CallStateType.Failed, CallStateReason.RtmSetupFailed, err.msg)
                completion?.invoke(err)
                return@_prepareForCall
            }
            _updateAndNotifyState(CallStateType.Prepared)
            completion?.invoke(null)
        }
    }

    override fun addListener(listener: ICallApiListener) {
        _reportMethod("addListener")
        if (delegates.contains(listener)) { return }
        delegates.add(listener)
    }

    override fun removeListener(listener: ICallApiListener) {
        _reportMethod("removeListener")
        delegates.remove(listener)
    }

    override fun call(remoteUserId: Int, completion: ((AGError?) -> Unit)?) {
        val fromRoomId = prepareConfig?.roomId
        val fromUserId = config?.userId
        if (fromRoomId == null || fromUserId == null) {
            val reason = "call fail! config or roomId is empty"
            completion?.invoke(AGError(reason, -1))
            callWarningPrint(reason)
            return
        }
        if (state != CallStateType.Prepared) {
            val reason = "call fail! state busy or not initialized"
            completion?.invoke(AGError(reason, -1))
            callWarningPrint(reason)
            return
        }
        //发送呼叫消息
        connectInfo.set(remoteUserId, fromRoomId, UUID.randomUUID().toString(), isLocalAccepted = true)
        //ensure that the report log contains a call
        _reportMethod("call", "remoteUserId=$remoteUserId")

        val message = _callMessageDic(remoteUserId, fromRoomId)
        messageManager?.sendMessage(remoteUserId.toString(), message) { err ->
            completion?.invoke(err)
            if (err != null) {
                //_updateAndNotifyState(CallStateType.Prepared, CallReason.MessageFailed, err.msg)
                _notifySendMessageErrorEvent(err, "call fail: ")
                //return@sendMessage
            } else {
                _notifyEvent(CallEvent.RemoteUserRecvCall)
            }
        }
        _updateAndNotifyState(CallStateType.Calling, eventInfo = message)
        _notifyEvent(CallEvent.OnCalling)
        _joinRTCAsBroadcaster(fromRoomId)
    }

    override fun cancelCall(completion: ((AGError?) -> Unit)?) {
        _reportMethod("cancelCall")
        val message = _messageDic(CallAction.CancelCall)
        _cancelCall(message, completion)
        _updateAndNotifyState(CallStateType.Prepared, CallStateReason.LocalCancel, eventInfo = message)
        _notifyEvent(CallEvent.LocalCancel)
    }

    //接受
    override fun accept(remoteUserId: Int, completion: ((AGError?) -> Unit)?) {
        _reportMethod("accept", "remoteUserId=$remoteUserId")
        val fromUserId = config?.userId
        val roomId = connectInfo.callingRoomId
        if (fromUserId == null || roomId == null) {
            val errReason = "accept fail! current userId or roomId is empty"
            completion?.invoke(AGError(errReason, -1))
            return
        }
        //查询是否是calling状态，如果是prapared，表示可能被主叫取消了
        if (state == CallStateType.Calling) else {
            val errReason = "accept fail! current state is $state not calling"
            completion?.invoke(AGError(errReason, -1))
            _notifyEvent(CallEvent.StateMismatch, errReason)
            return
        }

        // accept内默认启动一次采集+推流
        rtcConnection?.let {
            config?.rtcEngine?.startPreview()
            val mediaOptions = ChannelMediaOptions()
            mediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            mediaOptions.publishCameraTrack = true
            mediaOptions.publishMicrophoneTrack = true
            config?.rtcEngine?.updateChannelMediaOptionsEx(mediaOptions, it)
        }

        connectInfo.set(userId = remoteUserId, roomId = roomId, isLocalAccepted = true)

        //先查询presence里是不是正在呼叫的被叫是自己，如果是则不再发送消息
        val message = _messageDic(CallAction.Accept)
        messageManager?.sendMessage(remoteUserId.toString(), message) { err ->
            completion?.invoke(err)
            if (err != null) {
                _notifySendMessageErrorEvent(err, "accept fail: ")
            }
        }
        _updateAndNotifyState(CallStateType.Connecting, CallStateReason.LocalAccepted, eventInfo = message)
        _notifyEvent(CallEvent.LocalAccepted)

        if (calleeJoinRTCPolicy == CalleeJoinRTCPolicy.Accepted) {
            _joinRTCAsBroadcaster(roomId)
        }
    }

    //拒绝
    override fun reject(remoteUserId: Int, reason: String?, completion: ((AGError?) -> Unit)?) {
        _reportMethod("reject", "remoteUserId=$remoteUserId&reason=$reason")
        val message = _rejectMessageDic(reason, rejectByInternal = false)
        _reject(remoteUserId, message) { error ->
            completion?.invoke(error)
            if (error != null) {
                _notifySendMessageErrorEvent(error, "reject fail: ")
            }
        }
        _updateAndNotifyState(CallStateType.Prepared, CallStateReason.LocalRejected, eventInfo = message)
        _notifyEvent(CallEvent.LocalRejected)
    }

    //挂断
    override fun hangup(remoteUserId: Int, reason: String?, completion: ((AGError?) -> Unit)?) {
        _reportMethod("hangup", "remoteUserId=$remoteUserId")
        val message = _hangupMessageDic(reason)
        _hangup(remoteUserId, message = message) { error ->
            completion?.invoke(error)
            if (error != null) {
                _notifySendMessageErrorEvent(error, "hangup fail: ")
            }
        }
        _updateAndNotifyState(CallStateType.Prepared, CallStateReason.LocalHangup, eventInfo = message)
        _notifyEvent(CallEvent.LocalHangup)
    }
//    override fun addRTCListener(listener: IRtcEngineEventHandler) {
//        _reportMethod("addRTCListener")
//        rtcProxy.addListener(listener)
//    }
//    override fun removeRTCListener(listener: IRtcEngineEventHandler) {
//        _reportMethod( "removeRTCListener")
//        rtcProxy.removeListener(listener)
//    }

    //MARK: AgoraRtmClientDelegate
    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        _notifyTokenPrivilegeWillExpire()
    }
    override fun onConnectionFail() {
        _updateAndNotifyState(CallStateType.Failed, CallStateReason.RtmLost)
        _notifyEvent(CallEvent.RtmLost)
    }
    override fun onMessageEvent(event: MessageEvent?) {
        val message = event?.message?.data as? ByteArray ?: return
        val jsonString = String(message, Charsets.UTF_8)
        val map = jsonStringToMap(jsonString)
        val messageAction = map[kMessageAction] as? Int ?: 0
        val msgTs = map[kMessageTs] as? Long
        val userId = map[kFromUserId] as? Int
        val messageVersion = map[kMessageVersion] as? String
        if (messageVersion == null || msgTs == null || userId == null) {
            callWarningPrint("fail to parse message: $jsonString")
            return
        }
        //TODO: compatible other message version
        if (kCurrentMessageVersion != messageVersion)  { return }
        callPrint("on event message: $jsonString")
        _processRespEvent(CallAction.fromValue(messageAction), map)
    }
    override fun debugInfo(message: String, logLevel: Int) {
        callPrint(message)
    }
    override fun onPresenceEvent(event: PresenceEvent?) {}
    override fun onTopicEvent(event: TopicEvent?) {}
    override fun onLockEvent(event: LockEvent?) {}
    override fun onStorageEvent(event: StorageEvent?) {}
    // IRtcEngineEventHandler
    override fun onConnectionStateChanged(state: Int, reason: Int) {
        callPrint("connectionChangedTo state: $state reason: $reason")
    }
    override fun onUserJoined(uid: Int, elapsed: Int) {
        callPrint("didJoinedOfUid: $uid elapsed: $elapsed")
        if (connectInfo.callingUserId == uid) else return
        _notifyEvent(CallEvent.RemoteJoin)
    }
    override fun onUserOffline(uid: Int, reason: Int) {
        callPrint("didOfflineOfUid: $uid")
        if (connectInfo.callingUserId != uid) { return }
        _notifyEvent(CallEvent.RemoteLeave)
    }
    override fun onLeaveChannel(stats: RtcStats?) {
        callPrint("didLeaveChannel: $stats")
        isChannelJoined = false
        /*
         由于leave rtc到didLeaveChannelWith是异步的
         这里rtcConnection = nil会导致leave之后马上join，didLeaveChannelWith会在join之后错误的置空了rtc connection
         */
        //rtcConnection = null
        _notifyEvent(CallEvent.LocalLeave)
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        callPrint("join RTC channel, didJoinChannel: $uid, channel: $channel elapsed: $elapsed")
        if (uid == config?.userId) else { return }
        isChannelJoined = true
        _flushReport()
        runOnUiThread {
            joinRtcCompletion?.invoke(null)
            joinRtcCompletion = null
            _notifyEvent(CallEvent.LocalJoin)
        }
    }

    override fun onError(err: Int) {
//        callWarningPrint("didOccurError: $err")
//        joinRtcCompletion?.invoke(AGError("join RTC fail", err))
//        joinRtcCompletion = null
        _notifyRtcOccurErrorEvent(err)
    }

    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
        val channelId = prepareConfig?.roomId ?: ""
        if (uid == connectInfo.callingUserId) else {return}
        callPrint("didLiveRtcRemoteVideoStateChanged channelId: $channelId/${connectInfo.callingRoomId ?: ""} uid: $uid/${connectInfo.callingUserId ?: 0} state: $state reason: $reason")
        if ((state == 2) && (reason == 6 || reason == 4 || reason == 3 )) {
            runOnUiThread {
                firstFrameCompletion?.invoke()
            }
        }
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

    private fun callPrint(message: String, logLevel: CallLogLevel = CallLogLevel.Normal) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "[CallApi]$message");
        } else {
            delegates.forEach { listener ->
                listener.callDebugInfo(message, logLevel)
            }
        }
    }

    private fun callWarningPrint(message: String) {
        delegates.forEach { listener ->
            listener.callDebugInfo(message, CallLogLevel.Warning)
        }
        callPrint("[Warning]$message")
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            runnable.run()
        } else {
            mHandler.post(runnable)
        }
    }
}

