package io.agora.scene.aichat.imkit.supends

import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.ChatUserInfo
import io.agora.scene.aichat.imkit.ChatUserInfoManager
import io.agora.scene.aichat.imkit.ChatUserInfoType
import io.agora.scene.aichat.imkit.impl.ValueCallbackImpl
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspend method for [ChatUserInfoManager.updateOwnInfoByAttribute]
 * @param type The type of the attribute to be updated
 * @param value The value of the attribute to be updated
 */
suspend fun ChatUserInfoManager.updateOwnAttribute(type: ChatUserInfoType, value: String): Int {
    return suspendCoroutine { continuation ->
        updateOwnInfoByAttribute(type, value, ValueCallbackImpl<String>(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatUserInfoManager.fetchUserInfoByUserId]
 * @param userIds The user id list
 * @return The user information map
 */
suspend fun ChatUserInfoManager.fetchUserInfo(userIds: List<String>): Map<String, ChatUserInfo> {
    return suspendCoroutine { continuation ->
        fetchUserInfoByUserId(userIds.toTypedArray(), ValueCallbackImpl(
            onSuccess = { value ->
                value?.let {
                    continuation.resume(it)
                } ?: continuation.resume(emptyMap())
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}

/**
 * Suspend method for [ChatUserInfoManager.fetchUserInfoByAttribute]
 * @param userIds The user id list
 * @param attributes The attribute list
 * @return The user information map
 */
suspend fun ChatUserInfoManager.fetUserInfo(userIds: List<String>, attributes: List<ChatUserInfoType>): Map<String, ChatUserInfo> {
    return suspendCoroutine { continuation ->
        fetchUserInfoByAttribute(userIds.toTypedArray(), attributes.toTypedArray(), ValueCallbackImpl(
            onSuccess = { value ->
                value?.let {
                    continuation.resume(it)
                } ?: continuation.resume(emptyMap())
            },
            onError = { code, message -> continuation.resumeWithException(ChatException(code, message)) }
        ))
    }
}