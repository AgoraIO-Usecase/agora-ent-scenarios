package io.agora.scene.aichat.imkit.supends

import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatCursorResult
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.ChatManager
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageBody
import io.agora.scene.aichat.imkit.ChatMessageReaction
import io.agora.scene.aichat.imkit.ChatSearchDirection
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.impl.CallbackImpl
import io.agora.scene.aichat.imkit.impl.ValueCallbackImpl
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspend method for [ChatManager.fetchConversationsFromServer(Int, String, ValueCallback)]
 *
 * @param limit The number of conversations that you expect to get on each page. The value range is [1,50].
 * @param cursor The position from which to start to get data. If you pass in `null` or an empty string (""),
 *                  the SDK retrieves conversations from the latest active one.
 * @return [ChatCursorResult] The result of the request.
 */
suspend fun ChatManager.fetchConversationsFromServer(limit: Int, cursor: String?): ChatCursorResult<ChatConversation> {
    return suspendCoroutine { continuation ->
        asyncFetchConversationsFromServer(limit, cursor, ValueCallbackImpl<ChatCursorResult<ChatConversation>>(
                onSuccess = { value ->
                    continuation.resume(value)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncFetchPinnedConversationsFromServer(Int, String, ValueCallback)]
 * @param limit The number of conversations that you expect to get on each page. The value range is [1,50].
 * @param cursor The position from which to start to get data. If you pass in `null` or an empty string (""),
 *                 the SDK retrieves the pinned conversations from the latest pinned one.
 * @return [ChatCursorResult] The result of the request.
 */
suspend fun ChatManager.fetchPinedConversationsFromServer(limit: Int, cursor: String?): ChatCursorResult<ChatConversation> {
    return suspendCoroutine { continuation ->
        asyncFetchPinnedConversationsFromServer(limit, cursor, ValueCallbackImpl<ChatCursorResult<ChatConversation>>(
                onSuccess = { value ->
                    continuation.resume(value)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncPinConversation(String, Boolean, Callback)]
 *
 * @param conversationId The id of the conversation to be pinned.
 * @param isPinned Whether to pin or unpin the conversation.
 * @return [ChatError] The result of the request.
 */
suspend fun ChatManager.pinConversation(conversationId: String?, isPinned: Boolean): Int {
    return suspendCoroutine { continuation ->
        asyncPinConversation(conversationId, isPinned, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.deleteConversationFromServer(String, ChatConversationType, Boolean, Callback)]
 *
 * @param conversationId The id of the conversation to be deleted.
 * @param conversationType The type of the conversation to be deleted.
 * @param isDeleteServerMessages Whether to delete the messages of the conversation on the server.
 * @return [ChatError] The result of the request.
 */
suspend fun ChatManager.deleteConversationFromServer(
    conversationId: String?,
    conversationType: ChatConversationType,
    isDeleteServerMessages: Boolean
): Int {
    return suspendCoroutine { continuation ->
        deleteConversationFromServer(conversationId,
            conversationType,
            isDeleteServerMessages,
            CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncFetchHistoryMessage]
 *
 * @param conversationId
 * @param conversationType
 * @param startMsgId
 * @param pageSize
 * @param direction
 */
suspend fun ChatManager.fetchHistoryMessages(
    conversationId: String?,
    conversationType: ChatConversationType,
    startMsgId: String?,
    pageSize: Int,
    direction: ChatSearchDirection
): ChatCursorResult<ChatMessage> {
    return suspendCoroutine { continuation ->
        asyncFetchHistoryMessage(conversationId,
            conversationType,
            pageSize,
            startMsgId,
            direction,
            ValueCallbackImpl<ChatCursorResult<ChatMessage>>(
                onSuccess = { value ->
                    continuation.resume(value)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncRecallMessage]
 *
 * @param message
 */
suspend fun ChatManager.recallChatMessage(message: ChatMessage?): Int {
    return suspendCoroutine { continuation ->
        asyncRecallMessage(message, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncModifyMessage]
 *
 * @param messageId
 * @param messageBodyModified
 */
suspend fun ChatManager.modifyMessage(messageId: String?, messageBodyModified: ChatMessageBody?): ChatMessage {
    return suspendCoroutine { continuation ->
        asyncModifyMessage(messageId, messageBodyModified, ValueCallbackImpl<ChatMessage>(
                onSuccess = {
                    continuation.resume(it)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncAddReaction]
 * @param messageId
 * @param reaction
 */
suspend fun ChatManager.addMessageReaction(messageId: String?, reaction: String?): Int {
    return suspendCoroutine { continuation ->
        asyncAddReaction(messageId, reaction, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncRemoveReaction]
 * @param messageId
 * @param reaction
 */
suspend fun ChatManager.removeMessageReaction(messageId: String?, reaction: String?): Int {
    return suspendCoroutine { continuation ->
        asyncRemoveReaction(messageId, reaction, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.ackConversationRead]
 */
suspend fun ChatManager.ackConversationToRead(conversationId: String?): Int {
    return suspendCoroutine { continuation ->
        try {
            ackConversationRead(conversationId)
            continuation.resume(ChatError.EM_NO_ERROR)
        } catch (e: ChatException) {
            continuation.resumeWithException(ChatException(e.errorCode, e.message))
        }
    }
}

/**
 * Suspend method for [ChatManager.ackGroupMessageRead]
 */
suspend fun ChatManager.ackGroupMessageToRead(conversationId: String?, messageId: String?, ext: String?): Int {
    return suspendCoroutine { continuation ->
        try {
            ackGroupMessageRead(conversationId, messageId, ext)
            continuation.resume(ChatError.EM_NO_ERROR)
        } catch (e: ChatException) {
            continuation.resumeWithException(ChatException(e.errorCode, e.message))
        }
    }
}

/**
 * Suspend method for [ChatManager.ackMessageRead]
 */
suspend fun ChatManager.ackMessageToRead(conversationId: String?, messageId: String?): Int {
    return suspendCoroutine { continuation ->
        try {
            ackMessageRead(conversationId, messageId)
            continuation.resume(ChatError.EM_NO_ERROR)
        } catch (e: ChatException) {
            continuation.resumeWithException(ChatException(e.errorCode, e.message))
        }
    }
}

/**
 * Suspend method for [ChatManager.downloadAttachment]
 */
suspend fun ChatManager.downloadAttachmentBySuspend(message: ChatMessage?): Pair<Int, Int> {
    return suspendCoroutine { continuation ->
        if (message == null) {
            continuation.resumeWithException(ChatException(ChatError.MESSAGE_INVALID, "message is null."))
        } else {
            message.setMessageStatusCallback(
                CallbackImpl(
                    onSuccess = {
                        continuation.resume(Pair(ChatError.EM_NO_ERROR, 100))
                    },
                    onError = { code, message ->
                        continuation.resumeWithException(ChatException(code, message))
                    },
                    onProgress = { progress ->
                        continuation.resume(Pair(-1, progress))
                    }
                )
            )
            downloadAttachment(message)
        }
    }
}

/**
 * Suspend method for [ChatManager.downloadThumbnail]
 */
suspend fun ChatManager.downloadThumbnailBySuspend(message: ChatMessage?): Pair<Int, Int> {
    return suspendCoroutine { continuation ->
        if (message == null) {
            continuation.resumeWithException(ChatException(ChatError.MESSAGE_INVALID, "message is null."))
        } else {
            message.setMessageStatusCallback(
                CallbackImpl(
                    onSuccess = {
                        continuation.resume(Pair(ChatError.EM_NO_ERROR, 100))
                    },
                    onError = { code, message ->
                        continuation.resumeWithException(ChatException(code, message))
                    },
                    onProgress = { progress ->
                        continuation.resume(Pair(-1, progress))
                    }
                )
            )
            downloadThumbnail(message)
        }
    }
}

/**
 * Suspend method for [ChatManager.asyncReportMessage]
 */
suspend fun ChatManager.reportChatMessage(messageId: String?,tag:String,reason:String?=""):Int{
    return suspendCoroutine { continuation ->
        asyncReportMessage(messageId,tag,reason, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncGetReactionList]
 */
suspend fun ChatManager.fetchReactionListBySuspend(
    messageIdList: List<String>
   , chatType: ChatType
   , groupId: String? = null): Map<String, List<ChatMessageReaction>> {
    return suspendCoroutine { continuation ->
        asyncGetReactionList(messageIdList, chatType, groupId, ValueCallbackImpl(
                onSuccess = { value ->
                    continuation.resume(value)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

/**
 * Suspend method for [ChatManager.asyncGetReactionDetail]
 */
suspend fun ChatManager.fetchReactionDetailBySuspend(
    messageId: String?
    , reaction: String
    , cursor: String?
    , pageSize: Int): ChatCursorResult<ChatMessageReaction> {
    return suspendCoroutine { continuation ->
        asyncGetReactionDetail(messageId, reaction, cursor, pageSize, ValueCallbackImpl(
                onSuccess = { value ->
                    continuation.resume(value)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                }
            )
        )
    }
}

suspend fun ChatManager.translationChatMessage(message:ChatMessage,languages:MutableList<String>):ChatMessage{
    return suspendCoroutine { continuation ->
        translateMessage(message,languages,ValueCallbackImpl(
            onSuccess = {
                continuation.resume(it)
            },
            onError = {code,message ->
                continuation.resumeWithException(ChatException(code, message))
            },
        ))
    }
}

suspend fun ChatManager.searchMessage(
    keywords:String,
    timeStamp:Long,
    maxCount:Int,
    from:String?,
    direction:ChatSearchDirection,
):List<ChatMessage>{
    return suspendCoroutine { continuation ->
        continuation.resume(searchMsgFromDB(keywords, timeStamp, maxCount, from, direction))
    }
}