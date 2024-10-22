package io.agora.scene.aichat.chat.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.chat.Conversation
import io.agora.hyextension.AIChatAudioTextConvertorDelegate
import io.agora.hyextension.AIChatAudioTextConvertorService
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.AIChatProtocolService
import io.agora.scene.aichat.AIChatProtocolService.Companion
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.R
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
import io.agora.scene.aichat.imkit.extensions.getMsgSendUser
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.extensions.isSuccess
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.extensions.send
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.model.getAllGroupAgents
import io.agora.scene.aichat.imkit.model.getGroupAvatars
import io.agora.scene.aichat.imkit.model.getPrompt
import io.agora.scene.aichat.imkit.model.isChat
import io.agora.scene.aichat.imkit.model.isGroup
import io.agora.scene.aichat.imkit.provider.fetchUsersBySuspend
import io.agora.scene.aichat.imkit.provider.getSyncUser
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

/**
 * Ai chat view model
 *
 * @property mConversationId
 * @property mConversationType always [ConversationType.Chat]
 * @constructor Create empty A i chat view model
 */
class AIChatDetailViewModel constructor(
    val mConversationId: String,
    val mConversationType: ChatConversationType = Conversation.ConversationType.Chat
) : AIBaseViewModel() {

    companion object {
        private const val TAG = "AIChatDetailViewModel"
        private const val DATA_STREAM_FLUSH = "flush"
        private const val DATA_STREAM_REQUEST_TYPE = 0
        private const val DATA_STREAM_RESPONSE_TYPE = 1
    }

    private val chatProtocolService by lazy { AIChatProtocolService.instance() }

    private var mRtcEngine: RtcEngineEx? = null

    private var _conversation: ChatConversation? = null

    private var view: IHandleChatResultView? = null

    private var mMediaPlayer: IMediaPlayer? = null

    // 当前操作的文字转语音消息
    var mTtsMessage: ChatMessage? = null
        private set(value) {
            field = value
        }

    private val mediaPlayerObserver = object : AIMediaPlayerObserver() {
        override fun onPlayerStateChanged(
            state: MediaPlayerState?,
            error: io.agora.mediaplayer.Constants.MediaPlayerError?
        ) {
            super.onPlayerStateChanged(state, error)
            mTtsMessage?.let {
                _audioPlayStatusLiveData.postValue(Pair(it, state ?: MediaPlayerState.PLAYER_STATE_UNKNOWN))
            }
            when (state) {
                MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                    mMediaPlayer?.play()
                }

                MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                    mTtsMessage = null
                }

                else -> {}
            }
        }
    }

    private fun checkCreateMpk() {
        if (mMediaPlayer == null) {
            mMediaPlayer = mRtcEngine?.createMediaPlayer()
            mMediaPlayer?.registerPlayerObserver(mediaPlayerObserver)
        }
    }

    // tts 语音转文字 first：message,second:audioPath
    private val _audioPathLivedata: MutableLiveData<Pair<ChatMessage, String>> = MutableLiveData()
    val audioPathLivedata: LiveData<Pair<ChatMessage, String>> get() = _audioPathLivedata

    // 播放状态
    private val _audioPlayStatusLiveData: MutableLiveData<Pair<ChatMessage, MediaPlayerState>> = MutableLiveData()
    val audioPlayStatusLiveData: LiveData<Pair<ChatMessage, MediaPlayerState>> get() = _audioPlayStatusLiveData

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

    // 房间详情，即用户信息
    private val _currentRoomLiveData: MutableLiveData<EaseProfile?> = MutableLiveData()

    // 房间详情，即用户信息
    val currentRoomLiveData: LiveData<EaseProfile?> get() = _currentRoomLiveData

    fun isChat(): Boolean {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.isChat() ?: false
    }

    fun isGroup(): Boolean {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.isGroup() ?: false
    }

    fun isPublicAgent(): Boolean {
        return easeConversation?.conversationId?.contains("common-agent") ?: false
    }

    fun getChatName(): String {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.name ?: ""
    }

    fun getChatSign(): String? {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.sign
    }

    fun getChatAvatar(): String {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.avatar ?: ""
    }

    fun getGroupAvatars(): List<String> {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.getGroupAvatars() ?: emptyList()
    }

    fun getAgentBgUrlByAvatar(): String {
        val avatarUrl = getChatAvatar()
        return avatarUrl.replace("avatar", "bg").replace("png", "jpg")
    }

    fun getAllGroupAgents(): List<EaseProfile> {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.getAllGroupAgents() ?: emptyList()
    }

    fun initCurrentRoom() {
        if (_conversation == null) {
            _conversation =
                ChatClient.getInstance().chatManager()?.getConversation(mConversationId, mConversationType, true)
        }
        if (_conversation == null) {
            _currentRoomLiveData.postValue(null)
            CustomToast.show("获取会话异常")
        }
        viewModelScope.launch {
            runCatching {
                EaseIM.getCache().reloadMessageAudioList(mConversationId)
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

    private suspend fun featCurrentRoom(): EaseProfile? = withContext(Dispatchers.IO) {
        val easeServerList = EaseIM.getUserProvider().fetchUsersBySuspend(listOf(mConversationId))
        return@withContext easeServerList.firstOrNull()
    }

    // 发送 text
    private val sendTextScheduler: AiDetailRequestScheduler by lazy { AiDetailRequestScheduler() }

    // 发送消息
    fun sendTextMessage(content: String, toUserId: String? = null, onTimeout: () -> Unit) {
        safeInConvScope {
            val message: ChatMessage = ChatMessage.createTextSendMessage(content, it.conversationId())
            sendTextScheduler.sendRequest(
                request = {
                    sendMessage(message, toUserId)
                },
                onTimeout = {
                    onTimeout.invoke()
                })
        }
    }

    // 开始收到消息
    fun onMessageStartReceivedMessage() {
        sendTextScheduler.onCallbackReceived()
    }

    private fun sendMessage(message: ChatMessage, toUserId: String? = null, callback: ChatCallback? = null) {
        safeInConvScope {
            message.run {
                EaseIM.getCurrentUser().let { profile ->
                    addUserInfo(profile.name, profile.avatar)
                }
                view?.addMsgAttrBeforeSend(message)
                setAttribute("ai_chat", JSONObject(getMessageAIChatEx(toUserId)))
                setAttribute("em_ignore_notification", true)

                AILogger.d(TAG, "sendTextMessage: ${message.body} ${message.ext()}")

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

    private fun getMessageAIChatEx(toUserId: String? = null): Map<String, Any> {
        val conversation = _conversation ?: return emptyMap()
        val messageList =
            conversation.allMessages.takeLast(10).filter { it.body is ChatTextMessageBody && it.isSuccess() }
        val contextList = mutableListOf<Map<String, String>>()
        messageList.forEach { message ->
            val textBody = message.body as? ChatTextMessageBody // 类型安全转换
            textBody?.let {
                val role = if (message.isSend()) "user" else "assistant"
                val name = message.getMsgSendUser().name
                val content = it.message
                contextList.add(mapOf("role" to role, "name" to (name ?: ""), "content" to content))
            }
        }
        var prompt = EaseIM.getUserProvider().getSyncUser(mConversationId)?.getPrompt() ?: ""

        var systemName = EaseIM.getUserProvider().getSyncUser(mConversationId)?.name ?: ""
        val userMeta = mutableMapOf<String, String>()
        toUserId?.let {
            userMeta["botId"] = it
            prompt = EaseIM.getUserProvider().getSyncUser(it)?.getPrompt() ?: ""
            systemName = EaseIM.getUserProvider().getSyncUser(it)?.name ?: ""
        }
        return mapOf("prompt" to prompt, "system_name" to systemName, "context" to contextList, "user_meta" to userMeta)
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

    private var mAudioTextConvertorService: AIChatAudioTextConvertorService? = null

    fun initRtcEngine(delegate: AIChatAudioTextConvertorDelegate) {
        mRtcEngine = AIRtcEngineInstance.rtcEngine
        mRtcEngine?.let {
            if (mAudioTextConvertorService == null) {
                mAudioTextConvertorService = AIChatAudioTextConvertorService(it)
                mAudioTextConvertorService?.addDelegate(delegate)
            }
            AIRtcEngineInstance.mAudioTextConvertorService = mAudioTextConvertorService
            joinRtcSttChannel()
        }
    }

    // 加入 stt 频道
    private fun joinRtcSttChannel() {
        val rtcEngine = mRtcEngine ?: return
        val option = ChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = false
        option.autoSubscribeVideo = false
        option.autoSubscribeAudio = false
        option.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

        // stt 频道
        val mSttChannelId = "aiChat_${EaseIM.getCurrentUser().id}"
        val ret = rtcEngine.joinChannel(null, mSttChannelId, AIChatCenter.mRtcUid, option)
        AILogger.d(TAG, "joinRtcSttChannel | ret:$ret, rtcUid: ${AIChatCenter.mRtcUid}")
    }

    private fun updateMic(publishMic: Boolean) {
        val rtcEngine = mRtcEngine ?: return
        val option = ChannelMediaOptions()
        option.publishMicrophoneTrack = publishMic
        rtcEngine.updateChannelMediaOptions(option)
        rtcEngine.muteLocalAudioStream(!publishMic)
    }

    fun reset() {
        sendTextScheduler.cancelScheduler()

        mAudioTextConvertorService?.let {
            it.stopService()
            it.removeAllDelegates()
            mAudioTextConvertorService = null
        }
        mMediaPlayer?.let {
            it.unRegisterPlayerObserver(mediaPlayerObserver)
            it.stop()
            it.destroy()
            mMediaPlayer = null
        }
        AIRtcEngineInstance.reset()
        mRtcEngine = null
    }

    /**
     * 文字转语音
     *
     * @param message
     */
    fun requestTts(message: ChatMessage) {
        mTtsMessage = message
        viewModelScope.launch {
            runCatching {
                chatProtocolService.requestTts(message)
            }.onSuccess { audioPath ->
                _audioPathLivedata.postValue(Pair(message, audioPath))
            }.onFailure {
                _audioPathLivedata.postValue(Pair(message, ""))
                CustomToast.show(R.string.aichat_tts_stt_failed)
            }
        }
    }

    fun stopAudio() {
        mMediaPlayer?.stop()
    }

    /**
     * 播放语音
     *
     * @param message
     * @return 正在播放
     */
    fun playAudio(message: ChatMessage): Boolean {
        mTtsMessage = message
        checkCreateMpk()
        val audioPath = EaseIM.getCache().getAudiPath(mConversationId, message.msgId) ?: return false
        mMediaPlayer?.stop()
        val ret = mMediaPlayer?.open(audioPath, 0)

        return ret == Constants.ERR_OK
    }

    /**
     * 启动录音
     *
     */
    fun startVoiceConvertor() {
        updateMic(true)
        mAudioTextConvertorService?.startConvertor()
        AILogger.d(TAG, "startVoiceConvertor called")
    }

    /**
     * 结束录音
     *
     */
    fun flushVoiceConvertor() {
        mAudioTextConvertorService?.flushConvertor()
        updateMic(false)
        AILogger.d(TAG, "flushConvertor called")
    }

    /**
     * 取消录音
     *
     */
    fun cancelVoiceConvertor() {
        mAudioTextConvertorService?.stopConvertor()
        updateMic(false)

        AILogger.d(TAG, "cancelVoiceConvertor called")
    }
}

class AiDetailRequestScheduler : CoroutineScope {
    private var job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    private var timerJob: Job? = null // 定时器任务
    private var hasReceivedCallback = false

    fun sendRequest(request: () -> Unit, onTimeout: () -> Unit) {
        request.invoke()
        hasReceivedCallback = false
        timerJob?.cancel()
        // 启动定时器，5 秒后触发超时
        timerJob = launch(Dispatchers.Main) {
            delay(5000L) // 等待 5 秒
            if (!hasReceivedCallback) {
                onTimeout.invoke()
            }
        }
    }

    fun onCallbackReceived() {
        hasReceivedCallback = true
        // 回调 B 收到后取消定时器任务，避免触发超时逻辑
        timerJob?.cancel()
    }

    // 确保取消所有协程时释放资源
    fun cancelScheduler() {
        timerJob?.cancel()
        coroutineContext.cancel()
    }
}