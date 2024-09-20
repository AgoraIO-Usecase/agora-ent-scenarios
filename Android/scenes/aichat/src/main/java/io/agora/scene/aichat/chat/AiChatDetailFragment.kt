package io.agora.scene.aichat.chat

import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.logic.AIChatViewModel
import io.agora.scene.aichat.databinding.AichatFragmentChatDetailBinding
import io.agora.scene.aichat.ext.getIdentifier
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.imkit.ChatCmdMessageBody
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageListener
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.IHandleChatResultView
import io.agora.scene.aichat.imkit.extensions.createReceiveLoadingMessage
import io.agora.scene.aichat.imkit.widget.EaseChatPrimaryMenuListener
import io.agora.scene.aichat.imkit.widget.EaseInputMenuStyle
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.widget.toast.CustomToast

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
                binding.chatChatInputMenu.translationY = -keypadHeight.toFloat()
                binding.chatChatInputMenu.onShowKeyboardStatus()
            } else {
                binding.chatChatInputMenu.translationY = 0f
                binding.chatChatInputMenu.onHideKeyboardStatus()
            }
        }

        if (mAIChatViewModel.isChat()) {
            binding.titleView.tvTitle.text = mAIChatViewModel.getChatName()
            binding.titleView.tvSubTitle.isVisible = false
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
        } else {
            binding.titleView.tvTitle.text = mAIChatViewModel.getChatName()
            binding.titleView.tvSubTitle.isVisible = true
            binding.titleView.tvSubTitle.text =
                mAIChatViewModel.getChatSign() ?: getString(R.string.aichat_empty_description)

            val groupAvatar = mAIChatViewModel.getGroupAvatars()
            if (groupAvatar.isEmpty()) {
                binding.titleView.groupAvatarImage.ivBaseImageView?.setImageResource(R.drawable.aichat_agent_avatar_2)
                binding.titleView.groupAvatarImage.ivOverlayImageView?.setImageResource(R.drawable.aichat_agent_avatar_2)
            } else if (groupAvatar.size == 1) {
                binding.titleView.groupAvatarImage.ivBaseImageView?.loadCircleImage(groupAvatar[0])
                binding.titleView.groupAvatarImage.ivOverlayImageView?.setImageResource(R.drawable.aichat_agent_avatar_2)
            } else {
                binding.titleView.groupAvatarImage.ivBaseImageView?.loadCircleImage(groupAvatar[0])
                binding.titleView.groupAvatarImage.ivOverlayImageView?.loadCircleImage(groupAvatar[1])
            }
            binding.titleView.groupAvatarImage.ivBaseImageView?.strokeColor = ColorStateList.valueOf(0x092874)
            binding.titleView.groupAvatarImage.ivOverlayImageView?.strokeColor = ColorStateList.valueOf(0x092874)
        }
        binding.layoutChatMessage.init(mAIChatViewModel.mConversationId, mAIChatViewModel.mConversationType)
    }

    override fun initListener() {
        super.initListener()
        binding.rootView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (oldBottom!=-1 && oldBottom>bottom) {
                binding.layoutChatMessage.refreshToLatest()
            }
        }
        binding.titleView.setBackClickListener {
            activity?.finish()
        }
        binding.chatChatInputMenu.setMenuShowType(
            if (mAIChatViewModel.isChat()) EaseInputMenuStyle.Single else EaseInputMenuStyle.Group
        )
        binding.chatChatInputMenu.setEaseChatPrimaryMenuListener(object : EaseChatPrimaryMenuListener {

            override fun afterTextChanged(s: Editable?) {

            }

            override fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                return false
            }

            override fun onSendBtnClicked(content: String?) {
                if (content.isNullOrBlank()) {
                    CustomToast.show("请输入聊天内容")
                    return
                }
                mAIChatViewModel.sendTextMessage(content)
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
//                val navOptions = NavOptions.Builder()
//                    .setEnterAnim(R.anim.aichat_slide_in_bottom)
//                    .setExitAnim(R.anim.aichat_slide_out_top)
//                    .setPopEnterAnim(R.anim.aichat_slide_in_bottom)
//                    .setPopExitAnim(R.anim.aichat_slide_out_top)
//                    .build()
            }

            override fun onToggleVoiceBtnClicked() {
                if (activity is AiChatActivity) {
                    (activity as AiChatActivity).toggleSelfAudio(true, callback = {

                    })
                }
            }

            override fun onEditTextHasFocus(hasFocus: Boolean) {

            }

            override fun onCancelRecordingAction() {
               CustomToast.show("取消发送录音")
            }

            override fun onSendRecordingAction() {
                CustomToast.show("发送录音")
            }
        })
        EaseIM.addChatMessageListener(chatMessageListener)
    }

    override fun requestData() {
        super.requestData()
        (activity as? AiChatActivity)?.toggleSelfAudio(true) {
            mAIChatViewModel.initRtcEngine()
        }
        binding.layoutChatMessage.loadData()
    }

    override fun onDestroyView() {
        EaseIM.removeChatMessageListener(chatMessageListener)
        super.onDestroyView()
    }

    override fun onSendMessageSuccess(message: ChatMessage?) {
        message?.let {
            if (it.conversationId() == mAIChatViewModel.mConversationId) {
                binding.layoutChatMessage.refreshToLatest()
                binding.layoutChatMessage.addMessageToLast(message.createReceiveLoadingMessage())
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
                //getChatMessageListLayout().setSendOrReceiveMessage(messages[0])
                binding.layoutChatMessage.refreshToLatest()
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
            messages ?: return
            for (msg in messages) {
                val body = msg.body as ChatCmdMessageBody
                Log.i(TAG, "Receive cmd message: " + body.action() + " - " + body.isDeliverOnlineOnly)
//                context.mainScope().launch {
//                    if (TextUtils.equals(msg.from, conversationId)) {
//                        listener?.onPeerTyping(body.action())
//                        typingHandler?.let {
//                            it.removeMessages(MSG_OTHER_TYPING_END)
//                            it.sendEmptyMessageDelayed(MSG_OTHER_TYPING_END, OTHER_TYPING_SHOW_TIME.toLong())
//                        }
//                    }
//                }
            }
        }
    }
}