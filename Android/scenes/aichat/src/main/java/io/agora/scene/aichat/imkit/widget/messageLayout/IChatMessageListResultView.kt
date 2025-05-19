package io.agora.scene.aichat.imkit.widget.messageLayout

import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatSearchDirection
import io.agora.scene.aichat.imkit.Chatroom

interface IChatMessageListResultView {

    /**
     * Join chatroom successfully.
     * @param value The chatroom object.
     */
    fun joinChatRoomSuccess(value: Chatroom?)

    /**
     * Join chatroom failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun joinChatRoomFail(error: Int, errorMsg: String?)

    /**
     * Leave chatroom successfully.
     */
    fun leaveChatRoomSuccess()

    /**
     * Leave chatroom failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun leaveChatRoomFail(error: Int, errorMsg: String?)

    /**
     * Get all messages successfully. The messages are from the api [ChatConversation.getAllMessages].
     */
    fun getAllMessagesSuccess(messages: List<ChatMessage>)

    /**
     * Get all messages failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun getAllMessagesFail(error: Int, errorMsg: String?)

    /**
     * Load messages from database successfully.
     * @param messages The message list loaded from database.
     */
    fun loadLocalMessagesSuccess(messages: List<ChatMessage>)

    /**
     * Load messages from database failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun loadLocalMessagesFail(error: Int, errorMsg: String?)

    /**
     * Load more messages from database successfully.
     * @param messages The message list loaded from database.
     */
    fun loadMoreLocalMessagesSuccess(messages: List<ChatMessage>)

    /**
     * Load more messages from database failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun loadMoreLocalMessagesFail(error: Int, errorMsg: String?)

    /**
     * Fetch messages from chat server successfully.
     * @param messages The message list loaded from chat server.
     */
    fun fetchRoamMessagesSuccess(messages: List<ChatMessage>)

    /**
     * Fetch messages from chat server failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun fetchRoamMessagesFail(error: Int, errorMsg: String?)

    /**
     * Fetch more messages from chat server successfully.
     * @param messages The message list loaded from chat server.
     */
    fun fetchMoreRoamMessagesSuccess(messages: List<ChatMessage>)

    /**
     * Fetch more messages from chat server failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun fetchMoreRoamMessagesFail(error: Int, errorMsg: String?)

    /**
     * Load local history messages from database successfully.
     * @param messages The message list loaded from database.
     * @param direction The direction of loading. [ChatSearchDirection.UP] or [ChatSearchDirection.DOWN].
     */
    fun loadLocalHistoryMessagesSuccess(messages: List<ChatMessage>, direction: ChatSearchDirection)

    /**
     * Load local history messages from database failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun loadLocalHistoryMessagesFail(error: Int, errorMsg: String?)

    /**
     * Load more retrieval messages successfully.
     * @param messages
     */
    fun loadMoreRetrievalsMessagesSuccess(messages: List<ChatMessage>)

    /**
     * Remove message successfully.
     * @param message The message to be removed.
     */
    fun removeMessageSuccess(message: ChatMessage?)

    /**
     * Remove message failed.
     * @param error The error code.
     * @param errorMsg The error message.
     */
    fun removeMessageFail(error: Int, errorMsg: String?)
}