package io.agora.scene.aichat.imkit.provider

import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.impl.OnValueSuccess
import io.agora.scene.aichat.imkit.model.EaseProfile
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface EaseUserProfileProvider {
    /**
     * Get [EaseProfile] by userId from user.
     * @param userId
     * @return  The object provider by user.
     */
    fun getUser(userId: String?): EaseProfile?

    /**
     * Fetch users info from server and callback to UI SDK.
     * @param userIds  The user list stop scrolling,
     *              and the visible items which do not have profile will be fetched.
     * @param onValueSuccess The callback of success called by user.
     */
    fun fetchUsers(userIds: List<String>, onValueSuccess: OnValueSuccess<List<EaseProfile>>)
}

/**
 * Suspended function for fetching user information.
 */
suspend fun EaseUserProfileProvider.fetchUsersBySuspend(userIds: List<String>?): List<EaseProfile> {
    return suspendCoroutine { continuation ->
        userIds?.let {
            fetchUsers(it, onValueSuccess = { map ->
                continuation.resume(map)
            })
        }
    }
}

/**
 * Get user info by cache or sync method provided by user.
 */
fun EaseUserProfileProvider.getSyncUser(userId: String?): EaseProfile? {
    var user = EaseIM.getCache().getUser(userId)
    if (user == null) {
        user = getUser(userId)
        if (user != null && !userId.isNullOrEmpty()) {
            EaseIM.getCache().insertUser(user)
        }
    }
    return user
}