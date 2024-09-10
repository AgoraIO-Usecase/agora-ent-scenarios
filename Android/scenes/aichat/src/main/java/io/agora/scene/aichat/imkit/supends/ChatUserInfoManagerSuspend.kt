package io.agora.scene.aichat.imkit.supends

import io.agora.chat.UserInfo
import io.agora.chat.UserInfoManager
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.impl.ValueCallbackImpl
import io.agora.scene.aichat.imkit.model.EaseUser
import io.agora.scene.aichat.imkit.model.toProfile
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun UserInfoManager.fetchUserInfoByUserId(userIds: List<String>): Map<String, EaseUser> {
    return suspendCoroutine { continuation ->
        fetchUserInfoByUserId(
            userIds.toTypedArray(), ValueCallbackImpl(onSuccess = { userInfoMap ->
                val easeUserMap = userInfoMap.mapValues { (_, value) -> value.parse() }
                val easeUserList = easeUserMap.map { (_, value) -> value.toProfile() }
                EaseIM.getCache().updateUsers(easeUserList)
                continuation.resume(easeUserMap)
            },
                onError = { code, error ->
                    continuation.resumeWithException(ChatException(code, error))
                })
        )
    }
}