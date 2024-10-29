package io.agora.scene.aichat.chat.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.chat.Conversation
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.DataStreamConfig
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.R
import io.agora.scene.aichat.ext.MD5
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.IHandleChatResultView
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.model.getPrompt
import io.agora.scene.aichat.imkit.provider.fetchUsersBySuspend
import io.agora.scene.aichat.imkit.provider.getSyncUser
import io.agora.scene.aichat.service.AIAgentManager
import io.agora.scene.aichat.service.api.AIChatService
import io.agora.scene.aichat.service.api.StartVoiceCallReq
import io.agora.scene.aichat.service.api.StartVoiceCallResult
import io.agora.scene.aichat.service.api.UpdateVoiceCallReq
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID
import kotlin.coroutines.CoroutineContext

/**
 * Ai chat view model
 *
 * @property mConversationId
 * @property mConversationType always [ConversationType.Chat]
 * @constructor Create empty A i chat view model
 */
class AIVoiceCallViewModel constructor(
    val mConversationId: String,
    val mConversationType: ChatConversationType = Conversation.ConversationType.Chat
) : AIBaseViewModel() {

    companion object {
        private const val TAG = "AIVoiceCallViewModel"
        private const val DATA_STREAM_FLUSH = "flush"
        private const val DATA_STREAM_REQUEST_TYPE = 0
        private const val DATA_STREAM_RESPONSE_TYPE = 1
    }

    private val aiChatService: AIChatService by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        AIAgentManager.getApi(AIChatService::class.java)
    }

    private var mRtcEngine: RtcEngineEx? = null

    private var _conversation: ChatConversation? = null

    private var view: IHandleChatResultView? = null

    // 语音通话频道
    private var mVoiceCallChannelId: String = ""

    private var mVoiceCallDataStreamId: Int = 0

    /**
     * 麦克风开关
     */
    var mMicOn = true
        private set(value) {
            field = value
        }

    /**
     * 是否允许语音打断，默认开启
     */
    var mFlushAllowed = true
        private set(value) {
            field = value
        }

    /**
     * 是否在语音通话
     */
    var mIsVoiceCalling = false
        private set(value) {
            field = value
        }

    // 房间详情，即用户信息
    private val _currentRoomLiveData: MutableLiveData<EaseProfile?> = MutableLiveData()

    // 房间详情，即用户信息
    val currentRoomLiveData: LiveData<EaseProfile?> get() = _currentRoomLiveData

    // 启动语音通话Agent
    private val _startVoiceCallAgentLivedata: MutableLiveData<StartVoiceCallResult> = MutableLiveData()
    val startVoiceCallAgentLivedata: LiveData<StartVoiceCallResult> get() = _startVoiceCallAgentLivedata

    // 开启语音打断
    private val _openInterruptCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()
    val openInterruptCallAgentLivedata: LiveData<Boolean> get() = _openInterruptCallAgentLivedata

    // 关闭语音打断
    private val _closeInterruptCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()
    val closeInterruptCallAgentLivedata: LiveData<Boolean> get() = _closeInterruptCallAgentLivedata

    // 远端语音音量
    private val _remoteVolumeLivedata: MutableLiveData<Int> = MutableLiveData()
    val remoteVolumeLivedata: LiveData<Int> get() = _remoteVolumeLivedata

    // 本地语音音量
    private val _localVolumeLivedata: MutableLiveData<Int> = MutableLiveData()
    val localVolumeLivedata: LiveData<Int> get() = _localVolumeLivedata

    fun attach(handleChatResultView: IHandleChatResultView) {
        this.view = handleChatResultView
    }

    fun initCurrentRoom() {
        if (_conversation == null) {
            _conversation =
                ChatClient.getInstance().chatManager()?.getConversation(mConversationId, mConversationType, true)
        }
        if (_conversation == null) {
            CustomToast.show("获取会话异常")
        }
        viewModelScope.launch {
            runCatching {
                featCurrentRoom()
            }.onSuccess {
                if (it != null) {
                    _currentRoomLiveData.postValue(it)
                } else {
                    _currentRoomLiveData.postValue(null)
                    CustomToast.show("获取数据失败")
                }
            }.onFailure {
                _currentRoomLiveData.postValue(null)
                CustomToast.show("获取数据失败 ${it.message}")
            }
        }
    }

    private fun inMainScope(scope: () -> Unit) {
        viewModelScope.launch(context = Dispatchers.Main) {
            scope()
        }
    }

    fun getChatName(): String {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.name ?: ""
    }

    fun getChatAvatar(): String {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.avatar ?: ""
    }


    fun getAgentBgUrlByAvatar(): String {
        val avatarUrl = getChatAvatar()
        return avatarUrl.replace("avatar", "bg").replace("png", "jpg")
    }

    private suspend fun featCurrentRoom(): EaseProfile? = withContext(Dispatchers.IO) {
        val easeServerList = EaseIM.getUserProvider().fetchUsersBySuspend(listOf(mConversationId))
        return@withContext easeServerList.firstOrNull()
    }

    // remote agent rct uid
    private var mAgentRtcUid: Int = 0
    private var mVoiceRtcConnection: RtcConnection? = null

    // 加入语音通话频道
    private fun joinVoiceCallChannel(voiceCallChannelId: String) {
        mRtcEngine = AIRtcEngineInstance.rtcEngine
        val rtcEngine = mRtcEngine ?: return
        val option = ChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = true
        option.autoSubscribeVideo = false
        option.autoSubscribeAudio = true
        option.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

        mVoiceRtcConnection = RtcConnection(voiceCallChannelId, AIChatCenter.mRtcUid)
        val ret = rtcEngine.joinChannelEx(null, mVoiceRtcConnection, option, object : IRtcEngineEventHandler() {

            override fun onError(err: Int) {
                super.onError(err)
                AILogger.d(TAG, "voiceCall Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
            }

            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)

                AILogger.d(TAG, "voiceCall onJoinChannelSuccess:$uid")
                AIRtcEngineInstance.addParameters()
                mVoiceRtcConnection?.let { rtcConnection ->
                    renewInnerDataStreamId(rtcConnection)
                    rtcEngine.enableAudioVolumeIndicationEx(500, 3, true, rtcConnection)
                }
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                super.onUserJoined(uid, elapsed)
                if (mAgentRtcUid == 0) {
                    mAgentRtcUid = uid
                }
                AILogger.d(TAG, "voiceCall onUserJoined:$uid")
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
                AILogger.d(TAG, "voiceCall onUserOffline:$uid")
                inMainScope {
                    // 智能体退出
                    if (mAgentRtcUid == uid) {
                        mAgentRtcUid = 0
                        // 正在通话中，agent 离开频道了，恢复语音通话
                        if (mIsVoiceCalling) {
                            AILogger.d(TAG, "onUserOffline:$uid resume start agent voice call")
                            viewModelScope.launch {
                                runCatching {
                                    suspendVoiceCallStart()
                                }.onSuccess {

                                }.onFailure {
                                    //打印错误栈信息
                                    it.printStackTrace()
                                }
                            }
                        }
                    }

                }
            }

            override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
                super.onAudioVolumeIndication(speakers, totalVolume)
                speakers ?: return

                speakers.forEach { speaker ->
                    if (speaker.uid == 0) {
                        _localVolumeLivedata.postValue(speaker.volume)
                    } else if (mAgentRtcUid == speaker.uid) {
                        _remoteVolumeLivedata.postValue(speaker.volume)
                    }
                }
            }

            override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                super.onStreamMessage(uid, streamId, data)
                val strMsg = String(data ?: byteArrayOf())
                inMainScope {
                    try {
                        val jsonMsg = JSONObject(strMsg)
                        if (jsonMsg.getString("cmdType") == DATA_STREAM_FLUSH) { // 中断通话
                            AILogger.d(TAG, "voiceCall onStreamMessage | uid:$uid,streamId:$streamId, $strMsg")
                            val messageId = jsonMsg.getString("messageId")
                            val type = jsonMsg.getInt("type")
                            val to = jsonMsg.getString("to")
                            // 是否是发给当前用户中断语音的消息
                            if (messageId == interruptDataStreamMsgId && to == AIChatCenter.mRtcUid.toString()
                                && type == DATA_STREAM_RESPONSE_TYPE
                            ) {
                                CustomToast.showCenter(R.string.aichat_interrupted)
                            }
                        }
                    } catch (e: Exception) {
                        AILogger.e(TAG, "voiceCall onStreamMessage | parserError ${e.message}")
                    }
                }
            }

            override fun onAudioMetadataReceived(uid: Int, data: ByteArray?) {
                super.onAudioMetadataReceived(uid, data)
            }
        })
        AILogger.d(
            TAG, "joinVoiceCallChannel | ret:$ret, channelId:$voiceCallChannelId, rtcUid: ${AIChatCenter.mRtcUid}"
        )
    }

    private fun renewInnerDataStreamId(rtcConnection: RtcConnection) {
        val rtcEngine = mRtcEngine ?: return
        val innerCfg = DataStreamConfig().apply {
            syncWithAudio = true
            ordered = false
        }
        this.mVoiceCallDataStreamId = rtcEngine.createDataStreamEx(innerCfg, rtcConnection)
    }

    /**
     * 开关麦克风
     *
     * @param publishMic
     */
    fun micUnMute(publishMic: Boolean) {
        val rtcEngine = mRtcEngine ?: return
        val rtcConnection = mVoiceRtcConnection ?: return
        mMicOn = publishMic
        rtcEngine.muteLocalAudioStreamEx(!publishMic, rtcConnection)
        val option = ChannelMediaOptions().apply {
            publishMicrophoneTrack = publishMic
        }
        rtcEngine.updateChannelMediaOptionsEx(option, rtcConnection)
        if (publishMic) {
            CustomToast.showCenter(R.string.aichat_mic_enable)
        } else {
            CustomToast.showCenter(R.string.aichat_mic_disable)
        }
    }

    private fun checkVoiceChannelId(force: Boolean = true): String {
        if (mVoiceCallChannelId.isEmpty() || force) {
            val suffix = (mConversationId + "_" + UUID.randomUUID().toString()).MD5()
            mVoiceCallChannelId = "aiChat_${EaseIM.getCurrentUser().id}_$suffix"
        }
        return mVoiceCallChannelId
    }

    // 启动语音通话
    fun voiceCallStart() {
        resetLivedata()
        // 每次启动通话重新命名
        val voiceCallChannelId = checkVoiceChannelId()
        joinVoiceCallChannel(voiceCallChannelId)
        viewModelScope.launch {
            runCatching {
                suspendVoiceCallStart()
            }.onSuccess {
                if (it.isSuccess && it.data != null) {
                    mIsVoiceCalling = true
                    voiceCallPing(voiceCallChannelId)
                    updateInterruptConfig(mFlushAllowed, true)
                    _startVoiceCallAgentLivedata.postValue(it.data)
                } else {
                    _startVoiceCallAgentLivedata.postValue(null)
                }
            }.onFailure {
                _startVoiceCallAgentLivedata.postValue(null)
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    private fun resetLivedata() {
        _startVoiceCallAgentLivedata.value = null
        _openInterruptCallAgentLivedata.value = null
        _closeInterruptCallAgentLivedata.value = null
        _localVolumeLivedata.value = null
        _remoteVolumeLivedata.value = null
    }

    /**
     * 启动语音通话
     *
     */
    private suspend fun suspendVoiceCallStart() = withContext(Dispatchers.IO) {
        val conversation = _conversation ?: throw IllegalStateException("conversation is null")
        val greeting = if (conversation.conversationId().contains("common-agent")) {
            AgoraApplication.the().getString(R.string.aichat_common_agent_greeting)
        } else {
            AgoraApplication.the().getString(R.string.aichat_user_agent_greeting1)
        }

        val prompt = EaseIM.getUserProvider().getSyncUser(conversation.conversationId())?.getPrompt() ?: ""
        val voiceId = EaseIM.getUserProvider().getSyncUser(conversation.conversationId())?.voiceId ?: "female-shaonv"
        val req = StartVoiceCallReq(
            uid = AIChatCenter.mRtcUid,
            voiceId = voiceId,
            prompt = prompt,
            greeting = greeting,
            systemName = getChatName()
        )
        AILogger.d(TAG, "startVoiceCall called channelId:$mVoiceCallChannelId")
        val response = aiChatService.startVoiceCall(channelName = mVoiceCallChannelId, req = req)
        AILogger.d(TAG, "startVoiceCall $response")
        if (!response.isSuccess) {
            CustomToast.showCenter("启动语音通话失败 ${response.message}")
        }
        response
    }

    // ping
    private var pingVoiceCallScheduler: AiVoiceRequestScheduler? = null

    /**
     * Ping语音通话
     *
     */
    private fun voiceCallPing(voiceCallChannelId: String) {
        viewModelScope.launch {
            runCatching {
                suspendVoiceCallPing(voiceCallChannelId)
            }.onSuccess {

            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private suspend fun suspendVoiceCallPing(voiceCallChannelId: String) {
        if (pingVoiceCallScheduler == null) {
            pingVoiceCallScheduler = AiVoiceRequestScheduler()
        }
        pingVoiceCallScheduler?.startSendingRequests(3000) {
            aiChatService.voiceCallPing(channelName = voiceCallChannelId)
        }
        AILogger.d(TAG, "startSendingPing called channelId:$voiceCallChannelId")
    }

    /**
     * 是否允许打断语音
     *
     * @param isFlushAllowed 允许打断语音
     */
    fun updateInterruptConfig(isFlushAllowed: Boolean, force: Boolean = false) {
        if (mFlushAllowed == isFlushAllowed && !force) return
        val showTips = !force
        val oldFlushAllowed = mFlushAllowed
        mFlushAllowed = isFlushAllowed
        viewModelScope.launch {
            runCatching {
                suspendUpdateInterruptConfig(mFlushAllowed)
            }.onSuccess { isSuccess ->
                if (!isFlushAllowed) {
                    _closeInterruptCallAgentLivedata.postValue(isSuccess)
                    if (showTips) {
                        CustomToast.showCenter(if (isSuccess) R.string.aichat_voice_interruption_disable else R.string.aichat_voice_interruption_disable_error)
                    }
                } else {
                    _openInterruptCallAgentLivedata.postValue(isSuccess)
                    if (showTips) {
                        CustomToast.showCenter(if (isSuccess) R.string.aichat_voice_interruption_enable else R.string.aichat_voice_interruption_enable_error)
                    }
                }
            }.onFailure {
                mFlushAllowed = oldFlushAllowed
                if (isFlushAllowed) {
                    _openInterruptCallAgentLivedata.postValue(false)
                } else {
                    _closeInterruptCallAgentLivedata.postValue(false)
                }
                if (showTips) {
                    CustomToast.showCenter(if (isFlushAllowed) R.string.aichat_voice_interruption_enable_error else R.string.aichat_voice_interruption_disable_error)
                }
            }
        }
    }

    private suspend fun suspendUpdateInterruptConfig(flushAllowed: Boolean) = withContext(Dispatchers.IO) {
        AILogger.d(TAG, "updateVoiceCall called channelId:$mVoiceCallChannelId")
        val response =
            aiChatService.updateVoiceCall(channelName = mVoiceCallChannelId, req = UpdateVoiceCallReq(flushAllowed))
        AILogger.d(TAG, "updateVoiceCall $response")
        response.isSuccess
    }

    private var interruptDataStreamMsgId = ""

    /**
     * Interruption voice call
     * data stream
     */
    fun interruptionVoiceCall() {
        val rtcEngine = mRtcEngine ?: return
        val rtcConnection = mVoiceRtcConnection ?: return
        interruptDataStreamMsgId = UUID.randomUUID().toString().replace("-", "")
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmdType"] = DATA_STREAM_FLUSH
        msg["messageId"] = interruptDataStreamMsgId
        msg["to"] = mAgentRtcUid.toString()
        msg["type"] = DATA_STREAM_REQUEST_TYPE // 消息类型,0:表示请求，1:表示响应
        msg["payload"] = mutableMapOf<String, Any>() // 用于扩充数据需要
        val jsonMsg = JSONObject(msg)
        val ret = rtcEngine.sendStreamMessageEx(mVoiceCallDataStreamId, jsonMsg.toString().toByteArray(), rtcConnection)
        AILogger.d(TAG, "interruptionVoiceCall: ret: $ret, $jsonMsg ")
    }

    /**
     * 挂断语音通话
     *
     */
    fun voiceCallHangup() {
        mRtcEngine?.let { rtcEngineEx ->
            val rtcConnection = mVoiceRtcConnection ?: return
            mMicOn = true
            rtcEngineEx.muteLocalAudioStreamEx(true, rtcConnection)
            rtcEngineEx.adjustRecordingSignalVolumeEx(100, rtcConnection)
            rtcEngineEx.leaveChannelEx(rtcConnection)
            mVoiceRtcConnection = null


        }
        val voiceCallChannelId = mVoiceCallChannelId
        if (mIsVoiceCalling) {
            viewModelScope.launch {
                runCatching {
                    suspendVoiceCallStop(voiceCallChannelId)
                }.onSuccess {
                }.onFailure {
                    //打印错误栈信息
                    it.printStackTrace()
                }
            }
        }
        resetVoiceCall()
    }

    private fun resetVoiceCall() {
        mVoiceCallChannelId = ""
        mAgentRtcUid = 0
        mFlushAllowed = true
        mIsVoiceCalling = false
        pingVoiceCallScheduler?.cancelScheduler()
    }

    private suspend fun suspendVoiceCallStop(voiceCallChannelId: String) = withContext(Dispatchers.IO) {
        AILogger.d(TAG, "stopVoiceCall called channelId:$voiceCallChannelId")
        aiChatService.stopVoiceCall(channelName = voiceCallChannelId)
    }
}

class AiVoiceRequestScheduler : CoroutineScope {
    private var job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    // 定时发送请求
    fun startSendingRequests(intervalMillis: Long = 1000L, sendRequest: suspend () -> Unit) {
        launch {
            while (isActive) {
                try {
                    sendRequest()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(intervalMillis)
            }
        }
    }


    fun cancelTask() {
        job.cancelChildren() // 仅取消当前任务的子任务
    }

    // 确保取消所有协程时释放资源
    fun cancelScheduler() {
        coroutineContext.cancel()
    }
}