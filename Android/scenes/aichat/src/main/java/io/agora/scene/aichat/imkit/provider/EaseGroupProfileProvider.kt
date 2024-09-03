package io.agora.scene.aichat.imkit.provider

import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.impl.OnValueSuccess
import io.agora.scene.aichat.imkit.model.EaseGroupProfile
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Group profile provider.
 */
interface EaseGroupProfileProvider {
    /**
     * return EaseProfile for input group id
     * @param id    The group id.
     * @return
     */
    fun getGroup(id: String?): EaseGroupProfile?

    /**
     * Fetch profiles from server and callback to UI SDK.
     * @param groupIds  The conversation list or group list stop scrolling, and the visible items which do not have profile will be called.
     * @param onValueSuccess The callback of success called by developer.
     */
    fun fetchGroups(groupIds: List<String>, onValueSuccess: OnValueSuccess<List<EaseGroupProfile>>)
}

/**
 * Suspended function for fetching profiles.
 */
suspend fun EaseGroupProfileProvider.fetchProfilesBySuspend(groupIds: List<String>): List<EaseGroupProfile> {
    return suspendCoroutine { continuation ->
        fetchGroups(groupIds, onValueSuccess = { map ->
            continuation.resume(map)
        })
    }
}

/**
 * Get profile by cache or sync method provided by developer.
 */
fun EaseGroupProfileProvider.getSyncProfile(id: String?): EaseGroupProfile? {
    var profile = EaseIM.getCache().getGroup(id)
    if (profile == null) {
        profile = getGroup(id)
        if (profile != null && !id.isNullOrEmpty()) {
            EaseIM.getCache().insertGroup(id, profile)
        }
    }
    return profile
}