package io.agora.scene.aichat.chat.logic

import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatCallback
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatGroup
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageStatus
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.callback.IHandleChatResultView
import io.agora.scene.aichat.imkit.extensions.addUserInfo
import io.agora.scene.aichat.imkit.extensions.isChatroom
import io.agora.scene.aichat.imkit.extensions.isGroupChat
import io.agora.scene.aichat.imkit.extensions.send
import io.agora.scene.aichat.imkit.helper.EaseAtMessageHelper
import io.agora.scene.aichat.imkit.model.EaseChatType
import io.agora.scene.aichat.imkit.model.EaseLoadDataType
import io.agora.scene.aichat.imkit.model.getConversationType
import io.agora.scene.aichat.imkit.model.getFullInfo
import io.agora.scene.aichat.imkit.provider.getSyncProfile
import io.agora.scene.aichat.imkit.provider.getSyncUser
import io.agora.scene.aichat.imkit.widget.messageLayout.IChatMessageListResultView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        when (mChatType) {
            EaseChatType.SINGLE_CHAT -> {
                EaseIM.getUserProvider()?.getSyncUser(mConversationId)?.getFullInfo() ?: mConversationId
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
        return ""
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
            if (it.isGroupChat) {
                if (EaseAtMessageHelper.get().containsAtUsername(content)) {
                    sendAtMessage(content)
                    return@safeInConvScope
                }
            }
            val message: ChatMessage? = ChatMessage.createTextSendMessage(content, it.conversationId())
            sendMessage(message)
        }
    }

    private fun sendAtMessage(content: String?) {
        safeInConvScope {
            if (!it.isGroupChat) {
                inMainScope {
                    view?.onErrorBeforeSending(ChatError.INVALID_PARAM, "Not group chat.")
                }
                return@safeInConvScope
            }
            val group: ChatGroup? = ChatClient.getInstance().groupManager().getGroup(it.conversationId())
            if (group == null) {
                inMainScope {
                    view?.onErrorBeforeSending(ChatError.INVALID_PARAM, "Group: ${it.conversationId()} is null.")
                }
                return@safeInConvScope
            }
            val message = ChatMessage.createTextSendMessage(content, it.conversationId())
            if (ChatClient.getInstance().currentUser == group.owner
                && EaseAtMessageHelper.get().containsAtAll(content)
            ) {
                message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL)
            } else {
                message.setAttribute(
                    EaseConstant.MESSAGE_ATTR_AT_MSG,
                    EaseAtMessageHelper.get().atListToJsonArray(
                        EaseAtMessageHelper.get().getAtMessageUsernames(content!!)
                    )
                )
            }
            sendMessage(message)
        }
    }

    fun sendMessage(message: ChatMessage?, isCheck: Boolean = true, callback: ChatCallback? = null) {
        safeInConvScope {
            if (message == null) {
                inMainScope {
                    view?.onErrorBeforeSending(ChatError.MESSAGE_INVALID, "Message is null.")
                    return@inMainScope
                }
            }
            message?.run {
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