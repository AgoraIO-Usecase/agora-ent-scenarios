package io.agora.scene.aichat.chat.logic

import androidx.lifecycle.viewModelScope
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
import io.agora.scene.aichat.imkit.extensions.getUserInfo
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.extensions.send
import io.agora.scene.aichat.imkit.model.getConversationName
import io.agora.scene.aichat.imkit.model.getGroupAvatars
import io.agora.scene.aichat.imkit.model.isChat
import io.agora.scene.aichat.imkit.provider.getSyncUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Ai chat view model
 *
 * @property mConversationId
 * @property mConversationType always [ConversationType.Chat]
 * @constructor Create empty A i chat view model
 */
class AIChatViewModel constructor(val mConversationId: String, val mConversationType: ChatConversationType) :
    AIBaseViewModel() {

    private var _conversation: ChatConversation? = null

    private var view: IHandleChatResultView? = null

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

    fun getChatTitle(): String {
        return easeConversation?.getConversationName() ?: mConversationId
    }

    fun getChatAvatar(): String {
        return easeConversation?.getConversationName() ?: ""
    }

    fun getGroupAvatars(): List<String> {
        return easeConversation?.getGroupAvatars() ?: emptyList<String>()
    }

    fun getChatBgByAvatar(): String {
        return EaseIM.getUserProvider()?.getSyncUser(mConversationId)?.getChatBackground() ?: ""
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
}