package io.agora.scene.aichat.imkit.supends

import io.agora.scene.aichat.imkit.ChatCursorResult
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatThread
import io.agora.scene.aichat.imkit.ChatThreadManager
import io.agora.scene.aichat.imkit.impl.CallbackImpl
import io.agora.scene.aichat.imkit.impl.ValueCallbackImpl
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Suspend method for [ChatThreadManager].[createThread]
 * @param parentId The parent ID, which is the group ID.
 * @param msgId The ID of the parent message.
 * @param chatThreadName The name of the new message thread. It can contain a maximum of 64
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.createThread(
    parentId: String,
    msgId: String,
    chatThreadName: String,
): ChatThread {
    return suspendCoroutine { continuation ->
        createChatThread(
            parentId, msgId, chatThreadName, ValueCallbackImpl<ChatThread>(
                onSuccess = {
                    continuation.resume(it)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}

/**
 * Suspend method for [ChatThreadManager].[getThreadFromServer]
 * @param chatThreadId The message thread ID.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.getThreadFromServer(
    chatThreadId: String,
): ChatThread {
    return suspendCoroutine { continuation ->
        getChatThreadFromServer(
            chatThreadId, ValueCallbackImpl<ChatThread>(
                onSuccess = {
                    continuation.resume(it)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}

/**
 * Suspend method for [ChatThreadManager].[getThreadsFromServer]
 * @param parentId The parent ID, which is the group ID.
 * @param limit
 * @param cursor The position from which to start getting data.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.getThreadsFromServer(
    parentId: String,
    limit: Int,
    cursor: String
): ChatCursorResult<ChatThread> {
    return suspendCoroutine { continuation ->
        getChatThreadsFromServer(
            parentId, limit, cursor, ValueCallbackImpl<ChatCursorResult<ChatThread>>(
                onSuccess = {
                    continuation.resume(it)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}

/**
 * Suspend method for [ChatThreadManager].[joinThread]
 * @param chatThreadId The message thread ID.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.joinThread(
    chatThreadId: String,
): ChatThread {
    return suspendCoroutine { continuation ->
        joinChatThread(
            chatThreadId, ValueCallbackImpl<ChatThread>(
                onSuccess = {
                    continuation.resume(it)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}


/**
 * Suspend method for [ChatThreadManager].[destroyThread]
 * @param chatThreadId The message thread ID.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.destroyThread(
    chatThreadId: String,
): Int {
    return suspendCoroutine { continuation ->
        destroyChatThread(
            chatThreadId, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}


/**
 * Suspend method for [ChatThreadManager].[leaveThread]
 * @param chatThreadId The message thread ID.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.leaveThread(
    chatThreadId: String,
): Int {
    return suspendCoroutine { continuation ->
        leaveChatThread(
            chatThreadId, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}


/**
 * Suspend method for [ChatThreadManager].[updateThreadName]
 * @param chatThreadId The message thread ID.
 * @param chatThreadName The new message thread name. It can contain a maximum of 64
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.updateThreadName(
    chatThreadId: String,
    chatThreadName: String,
): Int {
    return suspendCoroutine { continuation ->
        updateChatThreadName(
            chatThreadId, chatThreadName, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}


/**
 * Suspend method for [ChatThreadManager].[removeMemberFromThread]
 * @param chatThreadId The message thread ID.
 * @param member The user ID of the member to be removed from the message thread.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.removeMemberFromThread(
    chatThreadId: String,
    member: String,
): Int {
    return suspendCoroutine { continuation ->
        removeMemberFromChatThread(
            chatThreadId, member, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}


/**
 * Suspend method for [ChatThreadManager].[getThreadMembers]
 * @param chatThreadId The message thread ID.
 * @param limit The number of members that you expect to get on each page. The value range is [1,50].
 * @param cursor The position from which to start getting data.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.getThreadMembers(
    chatThreadId: String,
    limit: Int,
    cursor: String,
): ChatCursorResult<String> {
    return suspendCoroutine { continuation ->
        getChatThreadMembers(
            chatThreadId, limit, cursor, ValueCallbackImpl<ChatCursorResult<String>>(
                onSuccess = {
                    continuation.resume(it)
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}


/**
 * Suspend method for [ChatThreadManager].[getJoinedThreadsFromServer]
 * @param parentId The parent ID, which is the group ID.
 * @param limit The number of members that you expect to get on each page. The value range is [1,50].
 * @param cursor The position from which to start getting data.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.getJoinedThreadsFromServer(
    parentId: String? = null,
    limit: Int,
    cursor: String,
): ChatCursorResult<ChatThread> {
    return suspendCoroutine { continuation ->
        if (parentId.isNullOrEmpty()) {
            getJoinedChatThreadsFromServer(
                limit, cursor, ValueCallbackImpl<ChatCursorResult<ChatThread>>(
                    onSuccess = {
                        continuation.resume(it)
                    },
                    onError = { code, message ->
                        continuation.resumeWithException(ChatException(code, message))
                    })
            )
        } else {
            getJoinedChatThreadsFromServer(
                parentId, limit, cursor, ValueCallbackImpl<ChatCursorResult<ChatThread>>(
                    onSuccess = {
                        continuation.resume(it)
                    },
                    onError = { code, message ->
                        continuation.resumeWithException(ChatException(code, message))
                    })
            )
        }
    }
}


/**
 * Suspend method for [ChatThreadManager].[getThreadLatestMessage]
 * @param chatThreadIds The list of message thread IDs to query. You can pass a maximum of 20 message thread IDs each time.
 * @return [ChatError] The error code of the request.
 */
suspend fun ChatThreadManager.getThreadLatestMessage(
    chatThreadIds: List<String>,
): MutableMap<String, ChatMessage> {
    return suspendCoroutine { continuation ->
        getChatThreadLatestMessage(
            chatThreadIds, ValueCallbackImpl<Map<String, ChatMessage>>(
                onSuccess = {
                    continuation.resume(it.toMutableMap())
                },
                onError = { code, message ->
                    continuation.resumeWithException(ChatException(code, message))
                })
        )
    }
}

