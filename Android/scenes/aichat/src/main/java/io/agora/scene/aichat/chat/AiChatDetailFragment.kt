package io.agora.scene.aichat.chat

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.agora.hyextension.AIChatAudioTextConvertorDelegate
import io.agora.mediaplayer.Constants
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.logic.AIChatViewModel
import io.agora.scene.aichat.chat.logic.AIChatViewModel.Companion
import io.agora.scene.aichat.create.QuickAdapter
import io.agora.scene.aichat.databinding.AichatFragmentChatDetailBinding
import io.agora.scene.aichat.databinding.AichatItemChatBottomGroupAgentBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.ext.mainScope
import io.agora.scene.aichat.groupmanager.AiChatGroupManagerActivity
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatCmdMessageBody
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageListener
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.IHandleChatResultView
import io.agora.scene.aichat.imkit.callback.OnMessageListItemClickListener
import io.agora.scene.aichat.imkit.extensions.createReceiveLoadingMessage
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.widget.EaseChatPrimaryMenuListener
import io.agora.scene.aichat.imkit.widget.EaseInputMenuStyle
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatAudioStatus
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AiChatDetailFragment : BaseViewBindingFragment<AichatFragmentChatDetailBinding>(), IHandleChatResultView {

    companion object {

        private val TAG = AiChatDetailFragment::class.java.simpleName
    }

    private val mAIChatViewModel: AIChatViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val conversationId = arguments?.getString(AiChatActivity.EXTRA_CONVERSATION_ID) ?: ""
                return AIChatViewModel(conversationId) as T
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentChatDetailBinding {
        return AichatFragmentChatDetailBinding.inflate(inflater, container, false)
    }

    /**
     * 按住录音提示
     */
    private var hideRecorderLayoutTips: Job? = null

    private var isKeyboardShow = false

    // 底部群智能体
    private val groupAgentDataList by lazy { mutableListOf<EaseProfile>() }

    // 当前选中的智能体
    private var groupAgentSelectPosition = 0

    // 智能体正在思考
    private var agentIsThinking = false

    private val groupAgentAdapter by lazy {
        object : QuickAdapter<AichatItemChatBottomGroupAgentBinding, EaseProfile>(
            AichatItemChatBottomGroupAgentBinding::inflate,
            groupAgentDataList
        ) {
            override fun onBind(
                binding: AichatItemChatBottomGroupAgentBinding,
                datas: List<EaseProfile>,
                position: Int
            ) {
                val item = datas[position]
                binding.ivAgentAvatar.loadCircleImage(item.avatar ?: "")
                if (groupAgentSelectPosition == position) {
                    binding.ivAgentSelect.isVisible = true
                } else {
                    binding.ivAgentSelect.isVisible = false
                    binding.ivAgentAvatar.alpha = if (agentIsThinking) 0.3f else 1f
                }
            }

            fun getSelectAgent(): EaseProfile? {
                if (groupAgentSelectPosition in 0 until groupAgentDataList.size) {
                    return groupAgentDataList[groupAgentSelectPosition]
                }
                return null
            }
        }

    }

    override fun initView() {
        super.initView()
        mAIChatViewModel.attach(this)
        mAIChatViewModel.init()

        binding.rootView.viewTreeObserver.addOnGlobalLayoutListener {
            if (isRemoving) return@addOnGlobalLayoutListener
            val rect = Rect()
            binding.rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = binding.rootView.height
            val keypadHeight = screenHeight - rect.bottom - binding.rootView.paddingBottom

            if (keypadHeight > screenHeight * 0.3) {
                if (isKeyboardShow) return@addOnGlobalLayoutListener
                binding.layoutChatMessage.translationY = -keypadHeight.toFloat()
                binding.layoutChatMessage.scrollToBottom(false)
                binding.chatInputMenu.translationY = -keypadHeight.toFloat()
                if (mAIChatViewModel.isGroup()) {
                    binding.rvGroupAgentList.translationY = -keypadHeight.toFloat()
                }
                binding.chatInputMenu.onShowKeyboardStatus()
                isKeyboardShow = true
            } else {
                if (!isKeyboardShow) return@addOnGlobalLayoutListener
                binding.layoutChatMessage.translationY = 0f
                binding.chatInputMenu.translationY = 0f
                if (mAIChatViewModel.isGroup()) {
                    binding.rvGroupAgentList.translationY = 0f
                }
                binding.chatInputMenu.onHideKeyboardStatus()
                isKeyboardShow = false
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mAIChatViewModel.currentRoomLiveData.observe(viewLifecycleOwner) { currentUser ->
                    if (currentUser != null) {
                        loadData()
                    } else {
                        mAIChatViewModel.destroyRtcEngine()
                        activity?.finish()
                    }
                }
            }
        }
        mAIChatViewModel.audioPathLivedata.observe(viewLifecycleOwner) {
            val audioPath = it.second
            if (audioPath.isNotEmpty()) {
                val canPlay = mAIChatViewModel.playAudio(it.first)
                if (canPlay) {
                    binding.layoutChatMessage.setAudioPaying(it.first, true)
                } else {
                    binding.layoutChatMessage.setAudioRecognizing(it.first, false)
                }
            } else {
                binding.layoutChatMessage.setAudioRecognizing(it.first, false)
            }
        }
        mAIChatViewModel.audioPlayStatusLiveData.observe(viewLifecycleOwner) {
            val playState = it.second
            if (playState == Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED) {
                binding.layoutChatMessage.setAudioPaying(it.first, false)
            }
        }
    }


    private fun loadData() {
        if (mAIChatViewModel.isChat()) {
            binding.titleView.tvTitle.text = mAIChatViewModel.getChatName()
            binding.titleView.tvSubTitle.isVisible = true
            binding.titleView.tvSubTitle.text =
                mAIChatViewModel.getChatSign() ?: getString(R.string.aichat_empty_description)
            binding.titleView.ivMoreIcon.isVisible = false
            binding.titleView.chatAvatarImage.isVisible = true
            binding.titleView.groupAvatarImage.isVisible = false
            binding.titleView.chatAvatarImage.loadCircleImage(mAIChatViewModel.getChatAvatar())
            Glide.with(this)
                .load(mAIChatViewModel.getAgentBgUrlByAvatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存原始图片和变换后的图片
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        // 使用 Drawable，例如设置到 ImageView
                        binding.rootView.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 清理资源或处理占位符
                    }
                })
            binding.rvGroupAgentList.isVisible = false
        } else {
            binding.titleView.tvTitle.text = mAIChatViewModel.getChatName()
            binding.titleView.tvSubTitle.isVisible = false
            binding.titleView.ivMoreIcon.isVisible = true
            binding.titleView.ivMoreIcon.setOnClickListener {
                activity?.let {
                    AiChatGroupManagerActivity.start(it, mAIChatViewModel.mConversationId)
                }
            }
            binding.titleView.chatAvatarImage.isVisible = false
            binding.titleView.groupAvatarImage.isVisible = true
            val groupAvatar = mAIChatViewModel.getGroupAvatars()
            if (groupAvatar.isEmpty()) {
                binding.titleView.groupAvatarImage.ivBaseImageView?.setImageResource(R.drawable.aichat_default_bot_avatar)
                binding.titleView.groupAvatarImage.ivOverlayImageView?.setImageResource(R.drawable.aichat_default_bot_avatar)
            } else if (groupAvatar.size == 1) {
                binding.titleView.groupAvatarImage.ivBaseImageView?.loadCircleImage(groupAvatar[0])
                binding.titleView.groupAvatarImage.ivOverlayImageView?.setImageResource(R.drawable.aichat_default_bot_avatar)
            } else {
                binding.titleView.groupAvatarImage.ivBaseImageView?.loadCircleImage(groupAvatar[0])
                binding.titleView.groupAvatarImage.ivOverlayImageView?.loadCircleImage(groupAvatar[1])
            }
            binding.titleView.groupAvatarImage.ivBaseImageView?.strokeColor = ColorStateList.valueOf(0x092874)
            binding.titleView.groupAvatarImage.ivOverlayImageView?.strokeColor = ColorStateList.valueOf(0x092874)
            binding.rootView.setBackgroundResource(io.agora.scene.widget.R.mipmap.app_room_bg)

            groupAgentDataList.clear()
            groupAgentDataList.addAll(mAIChatViewModel.getAllGroupAgents())
            binding.rvGroupAgentList.adapter = groupAgentAdapter
            groupAgentAdapter.onItemClickListener = { datas, position ->
                groupAgentSelectPosition = position
                groupAgentAdapter.notifyDataSetChanged()
            }
            binding.rvGroupAgentList.isVisible = groupAgentDataList.size > 1
        }
        binding.layoutChatMessage.init(mAIChatViewModel.mConversationId, mAIChatViewModel.mConversationType)
        binding.layoutChatMessage.loadData()
    }

    private val audioTextConvertorDelegate = object : AIChatAudioTextConvertorDelegate {
        override fun convertResultHandler(result: String?, error: Exception?) {
            Log.i(TAG, "convertResultHandler | result: $result")

            result?.let {
                val content = it.take(300)
                if (content.isEmpty()) return@let
                mAIChatViewModel.sendTextMessage(content, groupAgentAdapter.getSelectAgent()?.id, onTimeout = {
                    // 超时，恢复可输入状态
                    binding.chatInputMenu.isEnabled = true
                    binding.chatInputMenu.alpha = 1f
                    binding.viewBottomOverlay.isVisible = false
                    agentIsThinking = false
                })
            }
            error?.let {
                CustomToast.show(R.string.aichat_tts_stt_failed)
            }
        }

        override fun convertAudioVolumeHandler(totalVolume: Int) {
//            Log.i(TAG, "convertAudioVolumeHandler | totalVolume: $totalVolume")
        }

        override fun onTimeoutHandler() {
            Log.i(TAG, "onTimeoutHandler")
        }
    }

    override fun initListener() {
        super.initListener()
        binding.titleView.setBackClickListener {
            mAIChatViewModel.destroyRtcEngine()
            activity?.finish()
        }
        binding.viewBottomOverlay.setOnClickListener {
            CustomToast.show(R.string.aichat_agent_answering_tips)
        }
        binding.chatInputMenu.setMenuShowType(
            if (mAIChatViewModel.isChat()) EaseInputMenuStyle.Single else EaseInputMenuStyle.Group
        )
        binding.chatInputMenu.setEaseChatPrimaryMenuListener(object : EaseChatPrimaryMenuListener {

            override fun afterTextChanged(s: Editable?) {

            }

            override fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                return false
            }

            override fun onSendBtnClicked(content: String?) {
                if (content.isNullOrBlank()) {
                    CustomToast.show(R.string.aichat_input_content)
                    return
                }
                mAIChatViewModel.sendTextMessage(content, groupAgentAdapter.getSelectAgent()?.id, onTimeout = {
                    // 超时，恢复可输入状态
                    binding.chatInputMenu.isEnabled = true
                    binding.chatInputMenu.alpha = 1f
                    binding.viewBottomOverlay.isVisible = false
                    agentIsThinking = false
                })
            }

            override fun onCallBtnClicked() {
                if (activity is AiChatActivity) {
                    (activity as AiChatActivity).toggleSelfAudio(true, callback = {
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(AiChatActivity.VOICE_CALL_TYPE, true)
                            .build()
                        findNavController().navigate(AiChatActivity.VOICE_CALL_TYPE, navOptions)
                    })
                }
            }

            override fun onToggleVoiceBtnClicked() {
                hideRecorderLayoutTips?.cancel()
                binding.layoutSpeakerTips.isVisible = false
                if (activity is AiChatActivity) {
                    (activity as AiChatActivity).toggleSelfAudio(true, callback = {

                    })
                }
            }

            override fun onEditTextHasFocus(hasFocus: Boolean) {

            }

            override fun onRecorderBtnClicked() {
                hideRecorderLayoutTips?.cancel()
                showSpeakTipWithAnimation(binding.layoutSpeakerTips)
            }

            override fun onStartRecordingAction() {
                mAIChatViewModel.startVoiceConvertor()
            }

            override fun onCancelRecordingAction() {
                mAIChatViewModel.cancelVoiceConvertor()
            }

            override fun onSendRecordingAction() {
                mAIChatViewModel.flushVoiceConvertor()
            }
        })
        EaseIM.addChatMessageListener(chatMessageListener)

        binding.layoutChatMessage.setOnMessageListItemClickListener(object : OnMessageListItemClickListener {

            override fun onResendClick(message: ChatMessage?): Boolean {
                mAIChatViewModel.resendMessage(message)
                return true
            }

            override fun onBottomBubbleClick(message: ChatMessage?, audioStatus: EaseChatAudioStatus): Boolean {
                message ?: return false
                when (audioStatus) {
                    EaseChatAudioStatus.START_RECOGNITION -> {
                        // 点击开始识别，请求 tts 并且状态修改为识别中
                        binding.layoutChatMessage.setAudioRecognizing(message, true)
                        mAIChatViewModel.requestTts(message)
                        return true
                    }

                    EaseChatAudioStatus.RECOGNIZING -> {
                        // nothing
                        return true
                    }

                    EaseChatAudioStatus.START_PLAY -> {
                        // 点击播放，需要先暂停当前播放的
                        mAIChatViewModel.mAudioPlayingMessage?.let { audioPlayingMessage ->
                            binding.layoutChatMessage.setAudioPaying(audioPlayingMessage, false)
                        }
                        val canPlay = mAIChatViewModel.playAudio(message, true)
                        // 点击开始播放，播放audio 并且状态修改为播放中
                        if (canPlay) {
                            binding.layoutChatMessage.setAudioPaying(message, true)
                        }
                        return true
                    }

                    EaseChatAudioStatus.PLAYING -> {
                        // nothing
                        return true
                    }

                    else -> {
                        return super.onBottomBubbleClick(message, audioStatus)
                    }
                }
            }
        })
    }

    private fun showSpeakTipWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = layout.height.toFloat()

        layout.visibility = View.VISIBLE
        // 放大动画
        ObjectAnimator.ofFloat(layout, "scaleX", 0f, 1f).apply {
            duration = 300 // 动画时长
            interpolator = DecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(layout, "scaleY", 0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }

        // 取消之前的协程任务
        hideRecorderLayoutTips?.cancel()

        // 启动新的协程来延迟 1 秒后隐藏
        hideRecorderLayoutTips = CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            hideSpeakTipWithAnimation(layout) // 隐藏并缩小 TextView
        }
    }

    private fun hideSpeakTipWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = layout.height.toFloat()
        // 缩小动画
        ObjectAnimator.ofFloat(layout, "scaleX", 1f, 0f).apply {
            duration = 300 // 动画时长
            interpolator = DecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(layout, "scaleY", 1f, 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }

        // 动画结束后隐藏 TextView
        CoroutineScope(Dispatchers.Main).launch {
            delay(300) // 等待动画结束
            layout.visibility = View.GONE
        }
    }

    override fun requestData() {
        super.requestData()
        (activity as? AiChatActivity)?.toggleSelfAudio(true) {
            mAIChatViewModel.initRtcEngine(audioTextConvertorDelegate)
        }
    }

    override fun onDestroyView() {
        EaseIM.removeChatMessageListener(chatMessageListener)
        super.onDestroyView()
    }

    override fun onSendMessageSuccess(message: ChatMessage?) {
        message?.let {
            if (it.conversationId() == mAIChatViewModel.mConversationId) {
                binding.layoutChatMessage.scrollToBottom(true)
                binding.layoutChatMessage.addMessageToLast(message.createReceiveLoadingMessage(groupAgentAdapter.getSelectAgent()?.id))
                binding.chatInputMenu.isEnabled = false
                binding.chatInputMenu.alpha = 0.3f
                binding.viewBottomOverlay.isVisible = true
                agentIsThinking = true
                if (mAIChatViewModel.isGroup()) {
                    groupAgentAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onSendMessageError(message: ChatMessage?, code: Int, error: String?) {
        message?.let {
            if (it.conversationId() == mAIChatViewModel.mConversationId) {
                binding.layoutChatMessage.refreshMessage(message)
            }
        }
    }

    override fun onSendMessageInProgress(message: ChatMessage?, progress: Int) {
        message?.let {
            if (it.conversationId() == mAIChatViewModel.mConversationId) {
                binding.layoutChatMessage.refreshMessage(message)
            }
        }
    }

    override fun sendMessageFinish(message: ChatMessage?) {
        message?.let {
            if (it.conversationId() == mAIChatViewModel.mConversationId) {
                binding.layoutChatMessage.refreshToLatest()
            }
        }
    }


    private val chatMessageListener = object : ChatMessageListener {
        override fun onMessageReceived(messages: MutableList<io.agora.chat.ChatMessage>?) {
            messages ?: return
            var refresh = false
            for (message in messages) {
                // group message
                val username: String? =
                    if (message.chatType === ChatType.GroupChat || message.chatType === ChatType.ChatRoom) {
                        message.to
                    } else {
                        // single chat message
                        message.from
                    }
                // if the message is for current conversation
                if (username == mAIChatViewModel.mConversationId ||
                    message.to.equals(mAIChatViewModel.mConversationId) ||
                    message.conversationId().equals(mAIChatViewModel.mConversationId)
                ) {
                    refresh = true
                }
            }
            if (refresh && messages.isNotEmpty()) {
                binding.layoutChatMessage.refreshToLatest()
            }
            // 收到信息了
            mAIChatViewModel.onMessageReceived(messages)
        }

        override fun onMessageContentChanged(
            messageModified: io.agora.chat.ChatMessage?, operatorId: String?, operationTime: Long
        ) {
            super.onMessageContentChanged(messageModified, operatorId, operationTime)
            messageModified?.let {
                if (it.conversationId() == mAIChatViewModel.mConversationId) {
                    binding.layoutChatMessage.refreshMessage(it)
                }
            }
        }

        override fun onCmdMessageReceived(messages: MutableList<io.agora.chat.ChatMessage>?) {
            super.onCmdMessageReceived(messages)
            messages ?: return
            context?.mainScope()?.launch {
                for (msg in messages) {
                    val body = msg.body as ChatCmdMessageBody
                    Log.i(TAG, "Receive cmd message: " + body.action() + " - " + body.isDeliverOnlineOnly)
                    // 消息编辑结束
                    if (msg.conversationId() == mAIChatViewModel.mConversationId && body.action() == "AIChatEditEnd") {
                        binding.chatInputMenu.isEnabled = true
                        binding.chatInputMenu.alpha = 1f
                        binding.viewBottomOverlay.isVisible = false
                        agentIsThinking = false
                        if (mAIChatViewModel.isGroup()) {
                            groupAgentAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }
}