package io.agora.scene.aichat.chat.logic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.chat.Conversation
import io.agora.hy.extension.ExtensionManager
import io.agora.hyextension.AIChatAudioTextConvertorService
import io.agora.hyextension.LanguageConvertType
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IMediaExtensionObserver
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.R
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatCallback
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageStatus
import io.agora.scene.aichat.imkit.ChatTextMessageBody
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.IHandleChatResultView
import io.agora.scene.aichat.imkit.extensions.addUserInfo
import io.agora.scene.aichat.imkit.extensions.getChatPrompt
import io.agora.scene.aichat.imkit.extensions.getChatVoiceId
import io.agora.scene.aichat.imkit.extensions.getUserInfo
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.extensions.send
import io.agora.scene.aichat.imkit.model.getChatAvatar
import io.agora.scene.aichat.imkit.model.getGroupAvatars
import io.agora.scene.aichat.imkit.model.getName
import io.agora.scene.aichat.imkit.model.getSign
import io.agora.scene.aichat.imkit.model.isChat
import io.agora.scene.aichat.imkit.provider.getSyncUser
import io.agora.scene.aichat.service.api.StartVoiceCallReq
import io.agora.scene.aichat.service.api.UpdateVoiceCallReq
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * Ai chat view model
 *
 * @property mConversationId
 * @property mConversationType always [ConversationType.Chat]
 * @constructor Create empty A i chat view model
 */
class AIChatViewModel constructor(
    val mConversationId: String,
    val mConversationType: ChatConversationType = Conversation.ConversationType.Chat
) : AIBaseViewModel() {

    companion object {
        private const val TAG = "AIChatViewModel"
    }

    private val mWorkingExecutor = Executors.newSingleThreadExecutor()

    private var mRtcEngine: RtcEngineEx? = null

    private var _conversation: ChatConversation? = null

    private var view: IHandleChatResultView? = null

    private val mSttChannelId by lazy { "aiChat_${EaseIM.getCurrentUser().id}" }

    private val mRtcConnection by lazy {  RtcConnection(mSttChannelId, AIChatCenter.mRtcUid)}

    /**
     * 麦克风开关
     */
    var mMicOn = true
        private set(value) {
            field = value
        }

    /**
     * 是否允许语音打断
     */
    var mFlushAllowed = false
        private set(value) {
            field = value
        }

    // 启动语音通话Agent
    val startVoiceCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()

    // 停止语音通话Agent
    val stopVoiceCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()

    // 打断语音通话Agent
    val interruptionVoiceCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()

    // 开启语音打断
    val openInterruptCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()

    // 关闭语音打断
    val closeInterruptCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()

    fun attach(handleChatResultView: IHandleChatResultView) {
        this.view = handleChatResultView
    }

    private inline fun safeInConvScope(scope: (ChatConversation) -> Unit) {
        if (_conversation == null) {
            inMainScope {
                view?.onErrorBeforeSending(ChatError.INVALID_PARAM, "Conversation is null.")
            }
            return
        }
        _conversation?.let {
            scope(it)
        }
    }

    private fun inMainScope(scope: () -> Unit) {
        viewModelScope.launch(context = Dispatchers.Main) {
            scope()
        }
    }

    private val easeConversation by lazy {
        _conversation?.parse()
    }

    fun isChat(): Boolean {
        return easeConversation?.isChat() ?: true
    }

    fun isPublicAgent(): Boolean {
        return easeConversation?.conversationId?.contains("common-agent") ?: false
    }

    fun getChatName(): String {
        return easeConversation?.getName() ?: mConversationId
    }

    fun getChatSign(): String? {
        return easeConversation?.getSign()
    }

    fun getChatAvatar(): String {
        return easeConversation?.getChatAvatar() ?: ""
    }

    fun getGroupAvatars(): List<String> {
        return easeConversation?.getGroupAvatars() ?: emptyList()
    }

    fun getAgentBgUrlByAvatar(): String {
        val avatarUrl = getChatAvatar()
        return avatarUrl.replace("avatar", "bg").replace("png", "jpg")
    }

    fun init() {
        _conversation = ChatClient.getInstance().chatManager().getConversation(mConversationId, mConversationType, true)
    }

    fun sendTextMessage(content: String) {
        safeInConvScope {
            val message: ChatMessage = ChatMessage.createTextSendMessage(content, it.conversationId())
            sendMessage(message)
        }
    }

    private fun getMessageAIChatEx(): Map<String, Any> {
        val conversation = _conversation ?: return emptyMap()
        val messageList = conversation.allMessages.takeLast(10)
        val contextList = mutableListOf<Map<String, String>>()
        messageList.forEach { message ->
            val textBody = message.body as? ChatTextMessageBody // 类型安全转换
            if (textBody != null) {
                val role = if (message.isSend()) "user" else "assistant"
                val name = if (message.isSend()) EaseIM.getCurrentUser()?.name else message.getUserInfo()?.name
                val content = textBody.message
                contextList.add(mapOf("role" to role, "name" to (name ?: ""), "content" to content))
            }
        }
        val prompt = EaseIM.getUserProvider()?.getSyncUser(mConversationId)?.prompt ?: ""
        return mapOf("prompt" to prompt, "context" to contextList, "user_meta" to emptyMap<String, Any>())
    }

    private fun sendMessage(message: ChatMessage, callback: ChatCallback? = null) {
        safeInConvScope {
            message.run {
                EaseIM.getCurrentUser().let { profile ->
                    addUserInfo(profile.name, profile.avatar)
                }
                view?.addMsgAttrBeforeSend(message)
                setAttribute("ai_chat", JSONObject(getMessageAIChatEx()))
                setAttribute("em_ignore_notification", true)
                message.send(onSuccess = {
                    inMainScope {
                        callback?.onSuccess() ?: view?.onSendMessageSuccess(message)
                    }
                }, onError = { code, error ->
                    inMainScope {
                        callback?.onError(code, error) ?: view?.onSendMessageError(message, code, error)
                    }
                }, onProgress = {
                    inMainScope {
                        callback?.onProgress(it, "") ?: view?.onSendMessageInProgress(message, it)
                    }
                })
                inMainScope {
                    view?.sendMessageFinish(message)
                }
            }
        }
    }


    fun resendMessage(message: ChatMessage?) {
        safeInConvScope {
            message?.let {
                it.setStatus(ChatMessageStatus.CREATE)
                val currentTimeMillis = System.currentTimeMillis()
                it.setLocalTime(currentTimeMillis)
                it.msgTime = currentTimeMillis
                ChatClient.getInstance().chatManager().updateMessage(it)
                sendMessage(it)
            }
        }
    }

    private var innerAiChatAudioTextConvertorService: AIChatAudioTextConvertorService? = null

    val aiChatAudioTextConvertorService: AIChatAudioTextConvertorService
        get() {
            if (innerAiChatAudioTextConvertorService == null) {
                innerAiChatAudioTextConvertorService = AIChatAudioTextConvertorService()
                aiChatAudioTextConvertorService.startService(
                    AIChatCenter.mXFAppId,
                    AIChatCenter.mXFAppKey,
                    AIChatCenter.mXFAppSecret,
                    LanguageConvertType.NORMAL,
                    mRtcEngine!!
                )
            }
            return innerAiChatAudioTextConvertorService!!
        }

    fun initRtcEngine() {
        val config = RtcEngineConfig()
        config.mContext = AgoraApplication.the()
        config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
        config.mExtensionObserver = mMediaExtensionObserver
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onError(err: Int) {
                super.onError(err)
                AILogger.d(TAG, "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
            }
        }
        mRtcEngine = (RtcEngine.create(config) as RtcEngineEx)
        joinRtcChannel()
    }

    private val mMediaExtensionObserver: IMediaExtensionObserver = object : IMediaExtensionObserver {
        override fun onEvent(provider: String, extension: String, key: String, value: String) {
            Log.i(TAG, "onEvent | provider: $provider, extension: $extension, key: $key, value: $value")
            if (ExtensionManager.EXTENSION_VENDOR_NAME != provider || ExtensionManager.EXTENSION_AUDIO_FILTER_NAME != extension) {
                return
            }
            aiChatAudioTextConvertorService.onEvent(key, value)
        }

        override fun onStarted(provider: String, extension: String) {
            Log.i(TAG, "onStarted | provider: $provider, extension: $extension")
        }

        override fun onStopped(provider: String, extension: String) {
            Log.i(TAG, "onStarted | provider: $provider, extension: $extension")
        }

        override fun onError(provider: String, extension: String, errCode: Int, errMsg: String) {
            Log.e(TAG, "onStarted | provider: $provider, extension: $extension, errCode: $errCode, errMsg: $errMsg")
        }
    }

    private val mRtcEventHandlerEx = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            super.onLeaveChannel(stats)
        }

        override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
            super.onAudioVolumeIndication(speakers, totalVolume)


            speakers ?: return
            viewModelScope.launch {
                // 自己
                val currentSpeaker = speakers.firstOrNull { it.uid == 0 } ?: return@launch
            }
//            Log.i(TAG, "mRtcEventHandlerEx | totalVolume: $totalVolume")
        }
    }

    private fun joinRtcChannel() {
        val rtcEngine = mRtcEngine ?: return
        val option = ChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = false
        option.autoSubscribeVideo = false
        option.autoSubscribeAudio = false
        option.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE

        rtcEngine.joinChannelEx(null, mRtcConnection, option, aiChatAudioTextConvertorService)
    }

    private fun leaveRtcChannel() {
        val rtcEngine = mRtcEngine ?: return
        rtcEngine.leaveChannelEx(mRtcConnection)
    }

    private fun updateRole(role: Int) {
        val rtcEngine = mRtcEngine ?: return
        val option = ChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = role == Constants.CLIENT_ROLE_BROADCASTER
        option.autoSubscribeVideo = false
        option.autoSubscribeAudio = role == Constants.CLIENT_ROLE_BROADCASTER
        option.clientRoleType = role
        rtcEngine.updateChannelMediaOptionsEx(option, mRtcConnection)
        if (role == Constants.CLIENT_ROLE_BROADCASTER) {
            rtcEngine.enableAudioVolumeIndicationEx(50, 10, true, mRtcConnection)
        }
    }

    fun destroyRtcEngine() {
        mRtcEngine?.let {
            leaveRtcChannel()
            mWorkingExecutor.execute { RtcEngineEx.destroy() }
            mRtcEngine = null
        }
    }

    /**
     * 开关麦克风
     *
     * @param unMute
     */
    fun micUnMute(unMute: Boolean) {
        mRtcEngine?.let {
            it.muteLocalAudioStreamEx(!unMute, mRtcConnection)
            if (unMute) {
                CustomToast.show(R.string.aichat_mic_enable)
            } else {
                CustomToast.show(R.string.aichat_mic_disable)
            }
        }
    }

    /**
     * 启动录音
     *
     */
    fun startVoiceConvertor() {
        updateRole(Constants.CLIENT_ROLE_BROADCASTER)
        mRtcEngine?.addHandlerEx(mRtcEventHandlerEx, mRtcConnection)
        aiChatAudioTextConvertorService.startConvertor()
    }

    /**
     * 结束录音
     *
     */
    fun flushVoiceConvertor() {
        aiChatAudioTextConvertorService.flushConvertor()
    }

    /**
     * 取消录音
     *
     */
    fun cancelVoiceConvertor() {
        updateRole(Constants.CLIENT_ROLE_AUDIENCE)
        mRtcEngine?.removeHandlerEx(mRtcEventHandlerEx, mRtcConnection)
        aiChatAudioTextConvertorService.stopConvertor()
    }

    // 启动语音通话
    fun voiceCallStart() {
        updateRole(Constants.CLIENT_ROLE_BROADCASTER)
        mRtcEngine?.addHandlerEx(mRtcEventHandlerEx, mRtcConnection)
        viewModelScope.launch {
            runCatching {
                suspendVoiceCallStart()
            }.onSuccess {
                startVoiceCallAgentLivedata.postValue(it)
                voiceCallPing()
            }.onFailure {
                updateRole(Constants.CLIENT_ROLE_AUDIENCE)
                startVoiceCallAgentLivedata.postValue(false)
                CustomToast.showError("启动语音通话失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    /**
     * 启动语音通话
     *
     */
    private suspend fun suspendVoiceCallStart() = withContext(Dispatchers.IO) {
        val conversation = _conversation ?: throw IllegalStateException("conversation is null")
        val greeting = if (conversation.conversationId().contains("common-agent-001")) {
            AgoraApplication.the().getString(R.string.aichat_assistant_greeting)
        } else if (conversation.conversationId().contains("common-agent-002")) {
            AgoraApplication.the().getString(R.string.aichat_programming_greeting)
        } else if (conversation.conversationId().contains("common-agent-003")) {
            AgoraApplication.the().getString(R.string.aichat_attorney_greeting)
        } else if (conversation.conversationId().contains("common-agent-004")) {
            AgoraApplication.the().getString(R.string.aichat_practitioner_greeting)
        } else {
            AgoraApplication.the().getString(R.string.aichat_common_greeting)
        }

        val req = StartVoiceCallReq(
            uid = AIChatCenter.mRtcUid,
            voiceId = conversation.getChatVoiceId(),
            prompt = conversation.getChatPrompt(),
            greeting = greeting
        )
        val response = aiChatService.startVoiceCall(channelName = mSttChannelId, req = req)
        response.isSuccess
    }

    private var pingVoiceCallScheduler: RequestScheduler? = null

    /**
     * Ping语音通话
     *
     */
    private fun voiceCallPing() {
        viewModelScope.launch {
            runCatching {
                suspendVoiceCallPing()
            }.onSuccess {

            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    private suspend fun suspendVoiceCallPing() {
        if (pingVoiceCallScheduler == null) {
            pingVoiceCallScheduler = RequestScheduler()
        }
        pingVoiceCallScheduler?.startSendingRequests(5000) {
            aiChatService.voiceCallPing(channelName = mSttChannelId)
        }
    }

    /**
     * 是否允许打断语音
     *
     * @param isFlushAllowed 允许打断语音
     */
    fun updateInterruptConfig(isFlushAllowed: Boolean) {
        if (mFlushAllowed == isFlushAllowed) return
        val oldFlushAllowed = mFlushAllowed
        mFlushAllowed = !mFlushAllowed
        viewModelScope.launch {
            runCatching {
                suspendUpdateInterruptConfig(mFlushAllowed)
            }.onSuccess { isSuccess ->
                if (oldFlushAllowed) {
                    closeInterruptCallAgentLivedata.postValue(isSuccess)
                    if (isSuccess) {
                        CustomToast.show(R.string.aichat_voice_interruption_disable)
                    }
                } else {
                    openInterruptCallAgentLivedata.postValue(isSuccess)
                    if (isSuccess) {
                        CustomToast.show(R.string.aichat_voice_interruption_enable)
                    }
                }
            }.onFailure {
                mFlushAllowed = oldFlushAllowed
                if (oldFlushAllowed) {
                    closeInterruptCallAgentLivedata.postValue(false)
                } else {
                    openInterruptCallAgentLivedata.postValue(false)
                }
                it.printStackTrace()
            }
        }
    }

    private suspend fun suspendUpdateInterruptConfig(flushAllowed: Boolean) = withContext(Dispatchers.IO) {
        val response = aiChatService.updateVoiceCall(channelName = mSttChannelId, req = UpdateVoiceCallReq(flushAllowed))
        response.isSuccess
    }

    /**
     * 打断语音
     *
     */
    fun interruptionVoiceCall() {
        viewModelScope.launch {
            runCatching {
                suspendInterruptionVoiceCall()
            }.onSuccess { isSuccess ->
                interruptionVoiceCallAgentLivedata.postValue(isSuccess)
                if (isSuccess) {
                    CustomToast.show(R.string.aichat_interrupted)
                }
            }.onFailure {
                interruptionVoiceCallAgentLivedata.postValue(false)
                it.printStackTrace()
            }
        }
    }

    private suspend fun suspendInterruptionVoiceCall() = withContext(Dispatchers.IO) {
        val response = aiChatService.interruptVoiceCall(channelName = mSttChannelId)
        response.isSuccess
    }

    /**
     * 挂断语音通话
     *
     */
    fun voiceCallHangup() {
        updateRole(Constants.CLIENT_ROLE_AUDIENCE)
        mRtcEngine?.removeHandlerEx(mRtcEventHandlerEx, mRtcConnection)
        viewModelScope.launch {
            runCatching {
                suspendVoiceCallStop()
            }.onSuccess { isSuccess ->
                stopVoiceCallAgentLivedata.postValue(isSuccess)
                pingVoiceCallScheduler?.stopSendingRequests()
            }.onFailure {
                stopVoiceCallAgentLivedata.postValue(false)
                CustomToast.showError("停止语音通话失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    private suspend fun suspendVoiceCallStop() = withContext(Dispatchers.IO) {
        val response = aiChatService.stopVoiceCall(channelName = mSttChannelId)
        response.isSuccess
    }
}

class RequestScheduler : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

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

    fun stopSendingRequests() {
        job.cancel() // Cancel the coroutine
    }
}