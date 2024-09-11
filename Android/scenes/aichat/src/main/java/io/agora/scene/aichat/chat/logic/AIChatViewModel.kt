package io.agora.scene.aichat.chat.logic

import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatCallback
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageStatus
import io.agora.scene.aichat.imkit.ChatTextMessageBody
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.IHandleChatResultView
import io.agora.scene.aichat.imkit.extensions.addUserInfo
import io.agora.scene.aichat.imkit.extensions.getUserInfo
import io.agora.scene.aichat.imkit.extensions.isChatroom
import io.agora.scene.aichat.imkit.extensions.isGroupChat
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.extensions.send
import io.agora.scene.aichat.imkit.model.EaseChatType
import io.agora.scene.aichat.imkit.model.EaseLoadDataType
import io.agora.scene.aichat.imkit.model.getConversationType
import io.agora.scene.aichat.imkit.provider.getSyncProfile
import io.agora.scene.aichat.imkit.provider.getSyncUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class AIChatViewModel constructor(val mConversationId: String, val mChatType: EaseChatType) : AIBaseViewModel() {

    private var _conversation: ChatConversation? = null
    private var _loadDataType: EaseLoadDataType? = null

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

    fun getChatTitle(): String {
        return when (mChatType) {
            EaseChatType.SINGLE_CHAT -> {
                EaseIM.getUserProvider()?.getSyncUser(mConversationId)?.getRemarkOrName() ?: mConversationId
            }

            EaseChatType.GROUP_CHAT -> {
                EaseIM.getGroupProfileProvider()?.getSyncProfile(mConversationId)?.name
                    ?: ChatClient.getInstance().groupManager().getGroup(mConversationId)?.groupName
                    ?: mConversationId
            }

            EaseChatType.CHATROOM -> {
                ChatClient.getInstance().chatroomManager().getChatRoom(mConversationId)?.name ?: mConversationId
            }
        }
    }

    fun getTitleAvatar(): String {
        return when (mChatType) {
            EaseChatType.SINGLE_CHAT -> EaseIM.getUserProvider()?.getSyncUser(mConversationId)?.avatar ?: ""
            EaseChatType.GROUP_CHAT -> EaseIM.getGroupProfileProvider()?.getSyncProfile(mConversationId)?.avatar ?: ""
            EaseChatType.CHATROOM -> ""
        }
    }

    fun getChatBgByAvatar(): String {
        return EaseIM.getUserProvider()?.getSyncUser(mConversationId)?.getChatBackground() ?: ""
    }

    fun init(loadDataType: EaseLoadDataType) {
        _loadDataType = loadDataType
        _conversation = ChatClient.getInstance().chatManager().getConversation(
            mConversationId,
            mChatType.getConversationType(),
            true,
            loadDataType == EaseLoadDataType.THREAD
        )

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

    private fun sendMessage(message: ChatMessage, isCheck: Boolean = true, callback: ChatCallback? = null) {
        safeInConvScope {
            message.run {
                if (isCheck) {
                    if (it.isGroupChat) {
                        chatType = ChatType.GroupChat
                    } else if (it.isChatroom) {
                        chatType = ChatType.ChatRoom
                    }
                    setIsChatThreadMessage(it.isChatThread)
                }
                EaseIM.getCurrentUser()?.let { profile ->
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