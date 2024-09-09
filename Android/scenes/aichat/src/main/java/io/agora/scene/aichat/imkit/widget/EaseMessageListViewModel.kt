package io.agora.scene.aichat.imkit.widget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.ChatManager
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageStatus
import io.agora.scene.aichat.imkit.ChatSearchDirection
import io.agora.scene.aichat.imkit.ChatroomManager
import io.agora.scene.aichat.imkit.extensions.catchChatException
import io.agora.scene.aichat.imkit.extensions.isMessageIdValid
import io.agora.scene.aichat.imkit.supends.deleteMessage
import io.agora.scene.aichat.imkit.supends.fetchHistoryMessages
import io.agora.scene.aichat.imkit.supends.joinChatroom
import io.agora.scene.aichat.imkit.supends.leaveChatroom
import io.agora.scene.aichat.imkit.widget.messageLayout.IChatMessageListRequest
import io.agora.scene.aichat.imkit.widget.messageLayout.IChatMessageListResultView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class EaseMessageListViewModel(
    override var pageSize: Int = 10
) : ViewModel(), IChatMessageListRequest {

    private var _conversation: ChatConversation? = null

    override var messageCursor: String? = null

    private var view: IChatMessageListResultView? = null

    private val chatManager: ChatManager = ChatClient.getInstance().chatManager()
    private val chatroomManager: ChatroomManager = ChatClient.getInstance().chatroomManager()

    override fun attach(chatMessageListLayout: IChatMessageListResultView) {
        this.view = chatMessageListLayout
    }

    override fun detach() {
        this.view = null
    }

    override fun onCleared() {
        detach()
        super.onCleared()
    }

    override fun setupWithConversation(conversation: ChatConversation?) {
        _conversation = conversation
        _conversation?.let {
            // Chat thread conversation should clear cache data.
            if (it.isChatThread) it.clear()
        }
    }

    override fun joinChatroom(roomId: String) {
        viewModelScope.launch {
            flow {
                emit(
                    withContext(Dispatchers.IO) {
                        chatroomManager.joinChatroom(roomId)
                    }
                )
            }
                .catchChatException { e ->
                    view?.joinChatRoomFail(e.errorCode, e.description)
                }
                .collect {
                    view?.joinChatRoomSuccess(it)
                }
        }
    }

    override fun leaveChatroom(roomId: String) {
        viewModelScope.launch {
            flow {
                emit(
                    withContext(Dispatchers.IO) {
                        chatroomManager.leaveChatroom(roomId)
                    }
                )
            }
                .catchChatException { e ->
                    view?.leaveChatRoomFail(e.errorCode, e.description)
                }
                .collect {
                    view?.leaveChatRoomSuccess()
                }
        }
    }

    override fun getAllCacheMessages() {
        viewModelScope.launch {
            if (_conversation == null) {
                view?.getAllMessagesFail(ChatError.INVALID_PARAM, "The conversation is null.")
                return@launch
            }
            _conversation?.run {
                view?.getAllMessagesSuccess(allMessages)
            }
        }
    }

    override fun loadLocalMessages(direction: ChatSearchDirection) {
        viewModelScope.launch {
            messageCursor = ""
            flow {
                emit(
                    loadLocalMessages(_conversation, messageCursor, pageSize, direction)
                )
            }
                .catchChatException { e ->
                    view?.loadLocalMessagesFail(e.errorCode, e.description)
                }
                .collect {
                    messageCursor = if (it.firstMessageId()?.isEmpty() == true) messageCursor else it.firstMessageId()
                    view?.loadLocalMessagesSuccess(it)
                }
        }
    }

    override fun loadMoreLocalMessages(
        startMsgId: String?,
        direction: ChatSearchDirection
    ) {
        viewModelScope.launch {
            val startMessageId = if (startMsgId.isNullOrEmpty()) {
                if (messageCursor.isNullOrEmpty()) {
                    _conversation?.allMessages?.getFirstMessageId() ?: messageCursor
                } else messageCursor
            } else startMsgId
            flow {
                emit(loadLocalMessages(_conversation, startMessageId, pageSize, direction))
            }
                .catchChatException { e ->
                    view?.loadMoreLocalMessagesFail(e.errorCode, e.description)
                }
                .collect {
                    messageCursor = if (it.firstMessageId()?.isEmpty() == true) messageCursor else it.firstMessageId()
                    view?.loadMoreLocalMessagesSuccess(it)
                }
        }
    }

    override fun fetchRoamMessages(direction: ChatSearchDirection) {
        viewModelScope.launch {
            messageCursor = ""
            flow {
                emit(
                    fetchRoamMessages(_conversation, messageCursor, pageSize, direction)
                )
            }
                .catchChatException { e ->
                    view?.fetchRoamMessagesFail(e.errorCode, e.description)
                }
                .collect {
                    messageCursor = if (it.firstMessageId()?.isEmpty() == true) messageCursor else it.firstMessageId()
                    _conversation?.loadMoreMsgFromDB("", pageSize, direction)
                    view?.fetchRoamMessagesSuccess(it)
                }
        }
    }

    override fun fetchMoreRoamMessages(startMsgId: String?, direction: ChatSearchDirection) {
        viewModelScope.launch {
            val startMessageId = if (startMsgId.isNullOrEmpty()) messageCursor else startMsgId
            flow {
                emit(
                    fetchRoamMessages(_conversation, startMessageId, pageSize, direction)
                )
            }
                .catchChatException { e ->
                    view?.fetchMoreRoamMessagesFail(e.errorCode, e.description)
                }
                .collect {
                    messageCursor = if (it.firstMessageId()?.isEmpty() == true) messageCursor else it.firstMessageId()
                    _conversation?.loadMoreMsgFromDB(startMessageId, pageSize, direction)
                    view?.fetchMoreRoamMessagesSuccess(it)
                }
        }
    }

    override fun loadLocalHistoryMessages(
        startMsgId: String?,
        direction: ChatSearchDirection,
        isFirst: Boolean
    ) {
        viewModelScope.launch {
            if (isFirst) {
                if (_conversation == null) {
                    view?.loadLocalHistoryMessagesFail(
                        ChatError.INVALID_PARAM,
                        "Should first set up with conversation."
                    )
                    return@launch
                }
                if (!isMessageIdValid(startMsgId)) {
                    view?.loadLocalHistoryMessagesFail(ChatError.MESSAGE_INVALID, "Invalid message id.")
                    return@launch
                }
                val message = ChatClient.getInstance().chatManager().getMessage(startMsgId)
                if (message == null) {
                    view?.loadLocalHistoryMessagesFail(ChatError.MESSAGE_INVALID, "Not found the message: $startMsgId.")
                    return@launch
                }
                flow {
                    emit(
                        searchMessagesByTimestamp(_conversation, message.msgTime - 1, pageSize, direction)
                    )
                }
            } else {
                flow {
                    emit(
                        loadLocalMessages(_conversation, startMsgId, pageSize, direction)
                    )
                }
            }
                .catchChatException { e ->
                    view?.loadLocalHistoryMessagesFail(e.errorCode, e.description)
                }
                .collect {
                    view?.loadLocalHistoryMessagesSuccess(it, direction)
                }
        }
    }

    override fun loadMoreRetrievalsMessages(msgId: String?, pageSize: Int) {
        viewModelScope.launch {
            flow {
                emit(
                    loadLocalMessages(_conversation, msgId, pageSize, ChatSearchDirection.UP)
                )
            }
                .catchChatException {}
                .collect {
                    view?.loadMoreRetrievalsMessagesSuccess(it)
                }
        }
    }

    override fun removeMessage(message: ChatMessage?, isDeleteServerMessage: Boolean) {
        if (message == null) {
            Log.e(TAG, "removeMessage: The message is null.")
            view?.removeMessageFail(ChatError.MESSAGE_INVALID, "The message is null.")
            return
        }
        if (_conversation == null) {
            Log.e(TAG, "removeMessage: The conversation is null.")
            view?.removeMessageFail(ChatError.INVALID_PARAM, "The conversation is null.")
            return
        }
        viewModelScope.launch {
            if (isDeleteServerMessage) {
                flow {
                    emit(
                        _conversation?.deleteMessage(mutableListOf(message.msgId))
                    )
                }
                    .catchChatException { e ->
                        view?.removeMessageFail(e.errorCode, e.description)
                    }
                    .collect {
                        _conversation?.removeMessage(message.msgId)
                        view?.removeMessageSuccess(message)
                    }
            } else {
                _conversation?.removeMessage(message.msgId)
                view?.removeMessageSuccess(message)
            }
        }
    }

    companion object {
        private val TAG = EaseMessageListViewModel::class.java.simpleName
    }

    private fun List<ChatMessage>.firstMessageId(): String? {
        return if (isEmpty()) "" else first().msgId
    }

    private fun List<ChatMessage>.getFirstMessageId(): String? {
        return if (isEmpty()) "" else first().msgId
    }

    private suspend fun loadLocalMessages(
        conversation: ChatConversation?,
        startMsgId: String?,
        pageSize: Int,
        direction: ChatSearchDirection
    ): List<ChatMessage> = withContext(Dispatchers.IO) {
        if (conversation == null) {
            throw ChatException(ChatError.INVALID_PARAM, "Should first set up with conversation.")
        }
        if (!isMessageIdValid(startMsgId)) {
            throw ChatException(ChatError.MESSAGE_INVALID, "Invalid message id.")
        }
        conversation.loadMoreMsgFromDB(startMsgId, pageSize, direction).map {
            if (it.status() == ChatMessageStatus.CREATE) {
                it.setStatus(ChatMessageStatus.FAIL)
            }
            it
        }
    }

    suspend fun fetchRoamMessages(
        conversation: ChatConversation?,
        startMsgId: String?,
        pageSize: Int,
        direction: ChatSearchDirection
    ) = withContext(Dispatchers.IO) {
        if (conversation == null) {
            throw ChatException(ChatError.INVALID_PARAM, "Should first set up with conversation.")
        }
        if (!isMessageIdValid(startMsgId)) {
            throw ChatException(ChatError.MESSAGE_INVALID, "Invalid message id.")
        }
        chatManager.fetchHistoryMessages(
            conversation.conversationId(), conversation.type, startMsgId, pageSize,
            direction
        ).data
    }

    suspend fun searchMessagesByTimestamp(
        conversation: ChatConversation?,
        timestamp: Long,
        pageSize: Int,
        direction: ChatSearchDirection
    ) =
        withContext(Dispatchers.IO) {
            if (conversation == null) {
                throw ChatException(ChatError.INVALID_PARAM, "Should first set up with conversation.")
            }
            conversation.searchMsgFromDB(timestamp, pageSize, direction).map {
                if (it.status() == ChatMessageStatus.CREATE) {
                    it.setStatus(ChatMessageStatus.FAIL)
                }
                it
            }
        }


}