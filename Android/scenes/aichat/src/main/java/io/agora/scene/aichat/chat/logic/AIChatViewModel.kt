package io.agora.scene.aichat.chat.logic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.chat.Conversation
import io.agora.hy.extension.ExtensionManager
import io.agora.hyextension.AIChatAudioTextConvertorDelegate
import io.agora.hyextension.AIChatAudioTextConvertorService
import io.agora.hyextension.LanguageConvertType
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.Constants.MediaPlayerError
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IMediaExtensionObserver
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.AIChatProtocolService
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
import io.agora.scene.aichat.service.api.StartVoiceCallReq
import io.agora.scene.aichat.service.api.UpdateVoiceCallReq
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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

    private val chatProtocolService by lazy { AIChatProtocolService.instance() }

    private val mWorkingExecutor = Executors.newSingleThreadExecutor()

    private var mRtcEngine: RtcEngineEx? = null

    private var _conversation: ChatConversation? = null

    private var view: IHandleChatResultView? = null

    private val mSttChannelId by lazy { "aiChat_${EaseIM.getCurrentUser().id}" }

    private var mMediaPlayer: IMediaPlayer? = null

    // 在播放的消息，当前只能一条消息播放
    var mAudioPlayingMessage: ChatMessage? = null
        private set(value) {
            field = value
        }

    private val mediaPlayerObserver = object : AIMediaPlayerObserver() {
        override fun onPlayerStateChanged(state: MediaPlayerState?, error: MediaPlayerError?) {
            super.onPlayerStateChanged(state, error)
            Log.d("onPlayerStateChanged", "$state $error")
            mAudioPlayingMessage?.let {
                _audioPlayStatusLiveData.postValue(Pair(it, state ?: MediaPlayerState.PLAYER_STATE_UNKNOWN))
            }
            when (state) {
                MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                    mMediaPlayer?.play()
                }

                MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                    mAudioPlayingMessage = null
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

    /**
     * 是否在语音通话
     */
    var mIsVoiceCalling = false
        private set(value) {
            field = value
        }

    // tts 语音转文字 first：message,second:audioPath
    private val _audioPathLivedata: MutableLiveData<Pair<ChatMessage, String>> = MutableLiveData()
    val audioPathLivedata: LiveData<Pair<ChatMessage, String>> get() = _audioPathLivedata

    // 播放状态
    private val _audioPlayStatusLiveData: MutableLiveData<Pair<ChatMessage, MediaPlayerState>> = MutableLiveData()
    val audioPlayStatusLiveData: LiveData<Pair<ChatMessage, MediaPlayerState>> get() = _audioPlayStatusLiveData

    // 启动语音通话Agent
    private val _startVoiceCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()
    val startVoiceCallAgentLivedata: LiveData<Boolean> get() = _startVoiceCallAgentLivedata

    // 停止语音通话Agent
    private val _stopVoiceCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()
    val stopVoiceCallAgentLivedata: LiveData<Boolean> get() = _stopVoiceCallAgentLivedata

    // 打断语音通话Agent
    private val _interruptionVoiceCallAgentLivedata: MutableLiveData<Boolean> = MutableLiveData()
    val interruptionVoiceCallAgentLivedata: LiveData<Boolean> get() = _interruptionVoiceCallAgentLivedata

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

    // remote agent rct uid
    private var agentRtcUid: Int = 0

    init {
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

    fun isChat(): Boolean {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.isChat() ?: true
    }

    fun isGroup(): Boolean {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.isGroup() ?: true
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

    fun init() {
        _conversation = ChatClient.getInstance().chatManager().getConversation(mConversationId, mConversationType, true)
    }

    // 发送 text
    private val sendTextScheduler: RequestScheduler by lazy { RequestScheduler() }

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

    // 收到消息处理
    fun onMessageReceived(message: MutableList<ChatMessage>) {
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
        val config = RtcEngineConfig()
        config.mContext = AgoraApplication.the()
        config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
        config.addExtension(ExtensionManager.EXTENSION_NAME)
        config.addExtension("agora_ai_echo_cancellation_extension")
        config.addExtension("agora_ai_noise_suppression_extension")
        config.mExtensionObserver = mMediaExtensionObserver
        //Name of dynamic link library is provided by plug-in vendor,
        //e.g. libagora-bytedance.so whose EXTENSION_NAME should be "agora-bytedance"
        //and one or more plug-ins can be added
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onError(err: Int) {
                super.onError(err)
                AILogger.d(TAG, "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)
                mAudioTextConvertorService?.startService(
                    AIChatCenter.mXFAppId,
                    AIChatCenter.mXFAppKey,
                    AIChatCenter.mXFAppSecret,
                    LanguageConvertType.NORMAL
                )
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                super.onUserJoined(uid, elapsed)
                agentRtcUid = uid
                AILogger.d(TAG, "onUserJoined:$uid")
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
                inMainScope {
                    if (agentRtcUid == uid) {
                        // 正在通话中，agent 离开频道了，重新发起
                        if (mIsVoiceCalling) {
                            voiceCallResume()
                        } else {
                            agentRtcUid = 0
                        }
                    }

                }
                AILogger.d(TAG, "onUserOffline:$uid")
            }

            override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
                super.onAudioVolumeIndication(speakers, totalVolume)
                speakers ?: return

                speakers.forEach { speaker ->
                    if (speaker.uid == 0) {
                        _localVolumeLivedata.postValue(speaker.volume)
                        Log.d(TAG, "onAudioVolumeIndication | localVolume: ${speaker.volume}")
                    } else if (agentRtcUid == speaker.uid) {
                        _remoteVolumeLivedata.postValue(speaker.volume)
                        Log.d(TAG, "onAudioVolumeIndication | remoteVolume: ${speaker.volume}")
                    }
                }
            }
        }
        mRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
            enableExtension(
                ExtensionManager.EXTENSION_VENDOR_NAME,
                ExtensionManager.EXTENSION_AUDIO_FILTER_NAME, true
            )
            setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
            setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT)
            enableAudio()
            // 降噪
            setParameters("{\"che.audio.sf.nsEnable\":1}")
            setParameters("{\"che.audio.sf.ainsToLoadFlag\":1}")
            setParameters("{\"che.audio.sf.nsngAlgRoute\":12}")
            setParameters("{\"che.audio.sf.nsngPredefAgg\":10}")
        }
        mRtcEngine?.let {
            if (mAudioTextConvertorService == null) {
                mAudioTextConvertorService = AIChatAudioTextConvertorService(it)
                mAudioTextConvertorService?.addDelegate(delegate)
            }
            joinRtcChannel()
        }
    }

    private val mMediaExtensionObserver: IMediaExtensionObserver = object : IMediaExtensionObserver {
        override fun onEvent(provider: String, extension: String, key: String, value: String) {
            Log.i(TAG, "onEvent | provider: $provider, extension: $extension, key: $key, value: $value")
            if (ExtensionManager.EXTENSION_VENDOR_NAME != provider || ExtensionManager.EXTENSION_AUDIO_FILTER_NAME != extension) {
                return
            }
            mAudioTextConvertorService?.onEvent(key, value)
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

    private fun joinRtcChannel() {
        val rtcEngine = mRtcEngine ?: return
        val option = ChannelMediaOptions()
        option.publishCameraTrack = false
        option.publishMicrophoneTrack = false
        option.autoSubscribeVideo = false
        option.autoSubscribeAudio = false
        option.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE

        rtcEngine.joinChannel(null, mSttChannelId, AIChatCenter.mRtcUid, option)
        Log.d(TAG, "joinRtcChannel | rtcUid: ${AIChatCenter.mRtcUid}")
    }

    private fun leaveRtcChannel() {
        val rtcEngine = mRtcEngine ?: return
        rtcEngine.leaveChannel()
    }

    private fun updateRole(role: Int) {
        val rtcEngine = mRtcEngine ?: return
        val option = ChannelMediaOptions()
        option.publishMicrophoneTrack = role == Constants.CLIENT_ROLE_BROADCASTER
        option.autoSubscribeAudio = role == Constants.CLIENT_ROLE_BROADCASTER
        option.clientRoleType = role
        rtcEngine.updateChannelMediaOptions(option)
        if (role == Constants.CLIENT_ROLE_BROADCASTER) {
            rtcEngine.enableAudioVolumeIndication(500, 3, true)
        }
    }

    fun destroyRtcEngine() {
        mAudioTextConvertorService?.let {
            it.stopService()
            it.removeAllDelegates()
            mAudioTextConvertorService = null
        }
        mMediaPlayer?.let {
            it.unRegisterPlayerObserver(mediaPlayerObserver)
            it.destroy()
            mMediaPlayer = null
        }
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
            it.muteLocalAudioStream(!unMute)
            if (unMute) {
                CustomToast.showCenter(R.string.aichat_mic_enable)
            } else {
                CustomToast.showCenter(R.string.aichat_mic_disable)
            }
        }
    }

    /**
     * 文字转语音
     *
     * @param message
     */
    fun requestTts(message: ChatMessage) {
        viewModelScope.launch {
            runCatching {
                chatProtocolService.requestTts(message)
            }.onSuccess { audioPath ->
                _audioPathLivedata.postValue(Pair(message, audioPath))
            }.onFailure {
                CustomToast.show(R.string.aichat_tts_stt_failed)
            }
        }
    }

    /**
     * 播放语音
     *
     * @param message
     * @param force 是否强制播放，如果正在播放则暂停之前的
     * @return 正在播放
     */
    fun playAudio(message: ChatMessage, force: Boolean = false): Boolean {
        if (mAudioPlayingMessage != null && !force) return false
        checkCreateMpk()
        val audioPath = EaseIM.getCache().getAudiPath(mConversationId, message.msgId) ?: return false
        mMediaPlayer?.stop()
        val ret = mMediaPlayer?.open(audioPath, 0)
        if (ret == Constants.ERR_OK) {
            mAudioPlayingMessage = message
            return true
        }
        return false
    }

    /**
     * 启动录音
     *
     */
    fun startVoiceConvertor() {
        updateRole(Constants.CLIENT_ROLE_BROADCASTER)
        mAudioTextConvertorService?.startConvertor()
    }

    /**
     * 结束录音
     *
     */
    fun flushVoiceConvertor() {
        mAudioTextConvertorService?.flushConvertor()
    }

    /**
     * 取消录音
     *
     */
    fun cancelVoiceConvertor() {
        updateRole(Constants.CLIENT_ROLE_AUDIENCE)
        mAudioTextConvertorService?.stopConvertor()
    }

    // 启动语音通话
    fun voiceCallStart() {
        updateRole(Constants.CLIENT_ROLE_BROADCASTER)
        viewModelScope.launch {
            runCatching {
                suspendVoiceCallStart()
            }.onSuccess {
                _startVoiceCallAgentLivedata.postValue(it)
                mIsVoiceCalling = it
                voiceCallPing()
            }.onFailure {
                updateRole(Constants.CLIENT_ROLE_AUDIENCE)
                _startVoiceCallAgentLivedata.postValue(false)
                CustomToast.showCenter("启动语音通话失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    fun voiceCallResume() {
        updateRole(Constants.CLIENT_ROLE_BROADCASTER)
        viewModelScope.launch {
            runCatching {
                suspendVoiceCallStart()
            }.onSuccess {
                mIsVoiceCalling = it
            }.onFailure {
                updateRole(Constants.CLIENT_ROLE_AUDIENCE)
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
            AgoraApplication.the().getString(R.string.aichat_common_greeting, getChatName())
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
        val response = aiChatService.startVoiceCall(channelName = mSttChannelId, req = req)
        response.isSuccess
    }

    // ping
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
                    _closeInterruptCallAgentLivedata.postValue(isSuccess)
                    if (isSuccess) {
                        CustomToast.showCenter(R.string.aichat_voice_interruption_disable)
                    } else {
                        CustomToast.showCenter(R.string.aichat_voice_interruption_disable_error)
                    }
                } else {
                    _openInterruptCallAgentLivedata.postValue(isSuccess)
                    if (isSuccess) {
                        CustomToast.showCenter(R.string.aichat_voice_interruption_enable)
                    } else {
                        CustomToast.showCenter(R.string.aichat_voice_interruption_enable_error)
                    }
                }
            }.onFailure {
                mFlushAllowed = oldFlushAllowed
                if (oldFlushAllowed) {
                    _closeInterruptCallAgentLivedata.postValue(false)
                    CustomToast.showCenter(R.string.aichat_voice_interruption_disable_error)
                } else {
                    _openInterruptCallAgentLivedata.postValue(false)
                    CustomToast.showCenter(R.string.aichat_voice_interruption_enable_error)
                }
                it.printStackTrace()
            }
        }
    }

    private suspend fun suspendUpdateInterruptConfig(flushAllowed: Boolean) = withContext(Dispatchers.IO) {
        val response =
            aiChatService.updateVoiceCall(channelName = mSttChannelId, req = UpdateVoiceCallReq(flushAllowed))
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
                _interruptionVoiceCallAgentLivedata.postValue(isSuccess)
                if (isSuccess) {
                    CustomToast.showCenter(R.string.aichat_interrupted)
                }
            }.onFailure {
                _interruptionVoiceCallAgentLivedata.postValue(false)
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
        viewModelScope.launch {
            runCatching {
                suspendVoiceCallStop()
            }.onSuccess { isSuccess ->
                _stopVoiceCallAgentLivedata.postValue(isSuccess)
                mFlushAllowed = false
                mIsVoiceCalling = false
                pingVoiceCallScheduler?.cancelTask()
            }.onFailure {
                _stopVoiceCallAgentLivedata.postValue(false)
                CustomToast.showCenter("停止语音通话失败 ${it.message}")
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
    private var job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    private var timerJob: Job? = null // 定时器任务
    private var hasReceivedCallback = false

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

    fun sendRequest(request: () -> Unit, onTimeout: () -> Unit) {
        request.invoke()
        hasReceivedCallback = false
        // 启动定时器，5 秒后触发超时
        timerJob = CoroutineScope(Dispatchers.Main).launch {
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

    fun cancelTask() {
        job.cancelChildren() // 仅取消当前任务的子任务
    }

    // 确保取消所有协程时释放资源
    fun cancelScheduler() {
        coroutineContext.cancel()
    }
}