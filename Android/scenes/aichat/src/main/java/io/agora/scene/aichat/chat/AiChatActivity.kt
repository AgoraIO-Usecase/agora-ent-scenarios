package io.agora.scene.aichat.chat

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.agora.ConversationListener
import io.agora.scene.aichat.chat.logic.AIChatViewModel
import io.agora.scene.aichat.databinding.AichatChatActivityBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.imkit.ChatCmdMessageBody
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageListener
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.IHandleChatResultView
import io.agora.scene.aichat.imkit.extensions.createReceiveLoadingMessage
import io.agora.scene.aichat.imkit.model.EaseChatType
import io.agora.scene.aichat.imkit.model.EaseLoadDataType
import io.agora.scene.aichat.imkit.widget.EaseChatPrimaryMenuListener
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.toast.CustomToast
import org.json.JSONObject

class AiChatActivity : BaseViewBindingActivity<AichatChatActivityBinding>(), IHandleChatResultView {

    companion object {

        private val TAG = AiChatActivity::class.java.simpleName

        const val EXTRA_CHAT_TYPE = "chatType"
        const val EXTRA_CONVERSATION_ID = "conversationId"

        fun start(context: Context, conversationId: String, chatType: EaseChatType) {
            Intent(context, AiChatActivity::class.java).apply {
                putExtra(EXTRA_CONVERSATION_ID, conversationId)
                putExtra(EXTRA_CHAT_TYPE, chatType.ordinal)
                context.startActivity(this)
            }
        }
    }

    private val mAIChatViewModel: AIChatViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID) ?: ""
                val chatType =
                    EaseChatType.values()[intent.getIntExtra(EXTRA_CHAT_TYPE, EaseChatType.SINGLE_CHAT.ordinal)]
                return AIChatViewModel(conversationId, chatType) as T
            }
        })[AIChatViewModel::class.java]
    }

    private var toggleAudioRun: Runnable? = null

    fun toggleSelfAudio(isOpen: Boolean, callback: () -> Unit) {
        if (isOpen) {
            toggleAudioRun = Runnable {
                callback.invoke()
            }
            requestRecordPermission(true)
        } else {
            callback.invoke()
        }
    }

    override fun getPermissions() {
        toggleAudioRun?.let {
            it.run()
            toggleAudioRun = null
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) {
            launchAppSetting(permission)
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): AichatChatActivityBinding {
        return AichatChatActivityBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (mAIChatViewModel.mConversationId.isEmpty()) {
            finish()
            return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener(binding.rootView)
        binding.rootView.viewTreeObserver.addOnGlobalLayoutListener {
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
        binding.titleView.setTitle(mAIChatViewModel.getChatTitle())
        binding.titleView.commonImage.loadCircleImage(mAIChatViewModel.getTitleAvatar())
        binding.layoutChatMessage.init(mAIChatViewModel.mConversationId, mAIChatViewModel.mChatType)

        mAIChatViewModel.attach(this)
        mAIChatViewModel.init(EaseLoadDataType.LOCAL)

    }

    override fun requestData() {
        super.requestData()
        binding.layoutChatMessage.loadData()
    }

    override fun initListener() {
        super.initListener()
        binding.titleView.setBackClickListener {
            finish()
        }
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
                CustomToast.show("onCallBtnClicked")
            }

            override fun onToggleVoiceBtnClicked() {
                CustomToast.show("onToggleVoiceBtnClicked")
            }

            override fun onEditTextHasFocus(hasFocus: Boolean) {

            }
        })
        EaseIM.addChatMessageListener(chatMessageListener)
        EaseIM.addConversationListener(conversationListener)
    }

    override fun ackConversationReadSuccess() {
    }

    override fun ackConversationReadFail(code: Int, message: String?) {
    }

    override fun ackGroupMessageReadSuccess() {
    }

    override fun ackGroupMessageReadFail(code: Int, message: String?) {
    }

    override fun ackMessageReadSuccess() {
    }

    override fun ackMessageReadFail(code: Int, message: String?) {
    }

    override fun createThumbFileFail(message: String?) {
    }

    override fun addMsgAttrBeforeSend(message: ChatMessage?) {
    }

    override fun onErrorBeforeSending(code: Int, message: String?) {
    }

    override fun deleteMessageSuccess(message: ChatMessage?) {
    }

    override fun deleteMessageFail(message: ChatMessage?, code: Int, errorMsg: String?) {
    }

    override fun deleteMessageListSuccess() {
    }

    override fun deleteMessageListFail(code: Int, errorMsg: String?) {
    }

    override fun recallMessageFinish(originalMessage: ChatMessage?, notification: ChatMessage?) {
    }

    override fun recallMessageFail(code: Int, message: String?) {
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
//                if (loadDataType == EaseLoadDataType.THREAD){
//                    chatMessageListLayout?.isNeedScrollToBottomWhenViewChange(true)
//                }
                binding.layoutChatMessage.refreshToLatest()
            }
        }
    }

    override fun addReactionMessageSuccess(message: ChatMessage?) {
    }

    override fun addReactionMessageFail(message: ChatMessage?, code: Int, error: String?) {
    }

    override fun removeReactionMessageSuccess(message: ChatMessage?) {
    }

    override fun removeReactionMessageFail(message: ChatMessage?, code: Int, error: String?) {
    }

    override fun onModifyMessageSuccess(messageModified: ChatMessage?) {
    }

    override fun onModifyMessageFailure(messageId: String?, code: Int, error: String?) {
    }

    override fun createReplyMessageExtSuccess(extObject: JSONObject?) {
    }

    override fun createReplyMessageExtFail(code: Int, error: String?) {
    }

    override fun onForwardMessageSuccess(message: ChatMessage?) {
    }

    override fun onForwardMessageFail(message: ChatMessage?, code: Int, error: String?) {
    }

    override fun onSendCombineMessageSuccess(message: ChatMessage?) {
    }

    override fun onSendCombineMessageFail(message: ChatMessage?, code: Int, error: String?) {
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

    private val conversationListener = object : ConversationListener {
        override fun onConversationUpdate() {

        }

        override fun onConversationRead(from: String?, to: String?) {
            binding.layoutChatMessage.notifyDataSetChanged()
        }
    }
}