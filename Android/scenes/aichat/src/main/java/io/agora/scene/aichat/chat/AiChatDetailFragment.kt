package io.agora.scene.aichat.chat

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.agora.hyextension.AIChatAudioTextConvertorDelegate
import io.agora.mediaplayer.Constants
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.logic.AIChatViewModel
import io.agora.scene.aichat.create.QuickAdapter
import io.agora.scene.aichat.databinding.AichatFragmentChatDetailBinding
import io.agora.scene.aichat.databinding.AichatItemChatBottomGroupAgentBinding
import io.agora.scene.aichat.ext.copyTextToClipboard
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.ext.setGradientBackground
import io.agora.scene.aichat.groupmanager.AiChatGroupManagerActivity
import io.agora.scene.aichat.imkit.ChatCmdMessageBody
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageListener
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.IHandleChatResultView
import io.agora.scene.aichat.imkit.callback.OnMessageListItemClickListener
import io.agora.scene.aichat.imkit.extensions.createReceiveLoadingMessage
import io.agora.scene.aichat.imkit.extensions.getKeyData
import io.agora.scene.aichat.imkit.extensions.isReceive
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.widget.EaseChatPrimaryMenuListener
import io.agora.scene.aichat.imkit.widget.EaseInputMenuStyle
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatAudioStatus
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.utils.dp
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

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

    // 定义 ActivityResultLauncher，使用 StartActivityForResult Contract
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            mAIChatViewModel.initCurrentRoom()
        }
    }

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
        mAIChatViewModel.initCurrentRoom()

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

        mAIChatViewModel.currentRoomLiveData.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                loadData()
            } else {
                mAIChatViewModel.reset()
                activity?.finish()
            }
        }
        mAIChatViewModel.audioPathLivedata.observe(viewLifecycleOwner) {
            it ?: return@observe
            val audioPath = it.second
            if (audioPath.isNotEmpty() && mAIChatViewModel.mSttMessage == it.first) {
                val canPlay = mAIChatViewModel.playAudio(it.first)
                if (canPlay) {
                    binding.layoutChatMessage.setAudioPaying(it.first, true)
                } else {
                    binding.layoutChatMessage.setAudioReset(it.first)
                }
            } else {
                binding.layoutChatMessage.setAudioReset(it.first)
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
            binding.chatInputMenu.setMenuShowType(EaseInputMenuStyle.Single)
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
//                        binding.rootView.background = resource
                        binding.ivAgentBg.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 清理资源或处理占位符
                    }
                })
            binding.rvGroupAgentList.isVisible = false
        } else {
            binding.chatInputMenu.setMenuShowType(EaseInputMenuStyle.Group)
            binding.titleView.tvTitle.text = mAIChatViewModel.getChatName()
            binding.titleView.tvSubTitle.isVisible = false
            binding.titleView.ivMoreIcon.isVisible = true
            binding.titleView.setMoreClickListener {
                activity?.let { context ->
                    val intent = Intent(context, AiChatGroupManagerActivity::class.java).apply {
                        putExtra(AiChatGroupManagerActivity.EXTRA_CONVERSATION_ID, mAIChatViewModel.mConversationId)
                    }
                    startForResult.launch(intent)
                }
            }
            binding.titleView.chatAvatarImage.isVisible = false
            binding.titleView.groupAvatarImage.isVisible = true
            val groupAvatar = mAIChatViewModel.getGroupAvatars()
            binding.titleView.setGroupAvatarMargin(1.dp.toInt())

            binding.titleView.groupAvatarImage.ivBaseImageView.apply {
                if (groupAvatar.isEmpty()) {
                    setImageResource(R.drawable.aichat_default_bot_avatar)
                } else {
                    loadCircleImage(groupAvatar[0])
                }
            }
            binding.titleView.groupAvatarImage.ivBaseImageViewBg.apply {
                setGradientBackground(
                    intArrayOf(
                        Color.parseColor("#092874"), // 起始颜色
                        Color.parseColor("#092874") // 结束颜色
                    )
                )
            }
            binding.titleView.groupAvatarImage.ivOverlayImageView.apply {
                if (groupAvatar.size <= 1) {
                    setImageResource(R.drawable.aichat_default_bot_avatar)
                } else {
                    loadCircleImage(groupAvatar[1])
                }
            }
            binding.titleView.groupAvatarImage.ivOverlayImageViewBg.apply {
                setGradientBackground(
                    intArrayOf(
                        Color.parseColor("#092874"), // 起始颜色
                        Color.parseColor("#092874") // 结束颜色
                    )
                )
            }

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
            if (error == null) {
                AILogger.d(TAG, "convertResultHandler result:$result")
            } else {
                AILogger.e(TAG, "convertResultHandler error:${error.message}")
            }

            result?.let {
                val content = it.take(300)
                if (content.isEmpty()) return@let
                mAIChatViewModel.sendTextMessage(content, groupAgentAdapter.getSelectAgent()?.id, onTimeout = {
                    // 超时，恢复可输入状态
                    resetChatInputMenu(true)
                })
            }
            error?.let {
                CustomToast.show(R.string.aichat_tts_stt_failed)
            }
        }

        override fun onTimeoutHandler() {
            AILogger.d(TAG, "audioTextConvertorDelegate onTimeoutHandler")
        }

        override fun onLogHandler(log: String, isError: Boolean) {
            if (isError) {
                AILogger.e(TAG, log)
            } else {
                AILogger.d(TAG, log)
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.titleView.setBackClickListener {
            mAIChatViewModel.reset()
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
                    resetChatInputMenu(true)
                    if (!isRemoving) {
                        binding.layoutChatMessage.refreshMessages()
                    }
                })
            }

            override fun onCallBtnClicked() {
                if (activity is AiChatActivity) {
                    (activity as AiChatActivity).toggleSelfAudio(true, callback = {
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(AiChatActivity.VOICE_CALL_TYPE, true)
                            .build()
                        mAIChatViewModel.stopAudio()
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

            override fun onBubbleLongClick(v: View?, message: ChatMessage?): Boolean {
                val msg = message ?: return false
                if (msg.isReceive()) {
                    val textContext = msg.getKeyData()
                    context?.copyTextToClipboard(textContext, true)
                } else {
                    context?.copyTextToClipboard("messageId:${msg.msgId}", true)
                }
                return true
            }

            override fun onBottomBubbleClick(message: ChatMessage?, audioStatus: EaseChatAudioStatus): Boolean {
                message ?: return false
                when (audioStatus) {
                    EaseChatAudioStatus.START_RECOGNITION -> {
                        // 点击识别暂停其他消息
                        mAIChatViewModel.mSttMessage?.let { sttMessage ->
                            binding.layoutChatMessage.setAudioReset(sttMessage)
                        }
                        mAIChatViewModel.stopAudio()
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
                        // 点击播放，需要先暂停其他消息
                        mAIChatViewModel.mSttMessage?.let { sttMessage ->
                            binding.layoutChatMessage.setAudioReset(sttMessage)
                        }
                        val canPlay = mAIChatViewModel.playAudio(message)
                        // 点击开始播放，播放audio 并且状态修改为播放中
                        if (canPlay) {
                            binding.layoutChatMessage.setAudioPaying(message, true)
                        }
                        return true
                    }

                    EaseChatAudioStatus.PLAYING -> {
                        // 正在播放，需要先暂停
                        mAIChatViewModel.mSttMessage?.let { sttMessage ->
                            binding.layoutChatMessage.setAudioReset(sttMessage)
                        }
                        mAIChatViewModel.stopAudio()
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

        playScaleAnimation(layout, 0f, 1f)

        // 取消之前的协程任务
        hideRecorderLayoutTips?.cancel()

        // 启动新的协程来延迟 1 秒后隐藏
        hideRecorderLayoutTips = viewLifecycleOwner.lifecycleScope.launch {
            delay(3000)
            if (isActive && layout.isShown) {
                hideSpeakTipWithAnimation(layout) // 隐藏并缩小 TextView
            }
        }
    }

    private fun hideSpeakTipWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = layout.height.toFloat()

        playScaleAnimation(layout, 1f, 0f)

        // 动画结束后隐藏 TextView
        viewLifecycleOwner.lifecycleScope.launch {
            delay(300) // 等待动画结束
            if (isActive && layout.isShown) {
                withContext(Dispatchers.Main) {
                    layout.visibility = View.GONE
                }
            }
        }
    }

    // 抽取的缩放动画函数
    private fun playScaleAnimation(layout: FrameLayout, startScale: Float, endScale: Float) {
        val scaleXAnimator = ObjectAnimator.ofFloat(layout, "scaleX", startScale, endScale).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }

        val scaleYAnimator = ObjectAnimator.ofFloat(layout, "scaleY", startScale, endScale).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }

        // 并行播放 scaleX 和 scaleY 动画
        AnimatorSet().apply {
            playTogether(scaleXAnimator, scaleYAnimator)
            start()
        }
    }

    override fun requestData() {
        super.requestData()
        (activity as? AiChatActivity)?.toggleSelfAudio(true) {
            mAIChatViewModel.initRtcEngine(audioTextConvertorDelegate)
        }
    }

    override fun onResume() {
        super.onResume()
        // 不拦截返回键，使用默认的返回栈处理
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 这里调用 findNavController() 进行导航返回
                if (!findNavController().navigateUp()) {
                    mAIChatViewModel.reset()
                    activity?.finish()
                }
            }
        })
    }

    override fun onDestroyView() {
        EaseIM.removeChatMessageListener(chatMessageListener)
        super.onDestroyView()
    }

    override fun onSendMessageSuccess(message: ChatMessage?) {
        message?.let {
            if (it.conversationId() == mAIChatViewModel.mConversationId) {
                binding.layoutChatMessage.scrollToBottom(true)
                binding.layoutChatMessage.addMessageToLast(
                    createReceiveLoadingMessage(
                        mAIChatViewModel.mConversationId,
                        groupAgentAdapter.getSelectAgent()?.id
                    )
                )
                resetChatInputMenu(false)
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
//                binding.layoutChatMessage.refreshToLatest()
            }
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
            messages?.forEach { msg ->
                val body = msg.body as ChatCmdMessageBody
                // 消息编辑结束
                if (msg.conversationId() == mAIChatViewModel.mConversationId && body.action() == "AIChatEditEnd") {
                    AILogger.d(TAG, "Receive AIChatEditEnd: msgId:${msg.msgId}")

                    var editEndMsgId = ""
                    runCatching {
                        msg.attributes?.get("ai_chat")?.let { aiChat ->
                            val js = JSONObject(aiChat.toString())
                            editEndMsgId = js.optString("edit_end_message_id", "")
                        }
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (isRemoving) return@launch
                        binding.layoutChatMessage.refreshToLatest()
                        mAIChatViewModel.onMessageReceivedChatEditEnd()
                        resetChatInputMenu(true)
                    }
                }
            }
        }
    }

    private fun resetChatInputMenu(enable: Boolean) {
        if (isRemoving) return
        binding.chatInputMenu.isEnabled = enable
        binding.chatInputMenu.alpha = if (enable) 1f else 0.3f
        binding.viewBottomOverlay.isVisible = !enable
        agentIsThinking = !enable
        if (mAIChatViewModel.isGroup()) {
            groupAgentAdapter.notifyDataSetChanged()
        }
    }
}