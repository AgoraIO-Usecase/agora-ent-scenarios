package io.agora.scene.aichat.imkit

import io.agora.scene.aichat.imkit.model.EaseGroupProfile
import io.agora.scene.aichat.imkit.model.EaseProfile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EaseIMCache {
    private val userMap: ConcurrentMap<String, EaseProfile> = ConcurrentHashMap()
    // Cache the group info. The key is the groupId, the value is the group info.
    private val groupMap: ConcurrentMap<String, EaseGroupProfile> = ConcurrentHashMap()
    // Cache the userinfo parsed by message ext. The key is the userId, the value is the userinfo.
    private val messageUserMap: ConcurrentMap<String, EaseProfile> = ConcurrentHashMap()

    companion object {
        private const val TAG = "EaseIMCache"
    }

    fun init() {
        clear()
    }

    fun insertUser(user: EaseProfile) {
        userMap[user.id] = user
    }

    /**
     * Insert or update the group info to the cache.
     * @param groupId The group id.
     * @param profile The group info.
     */
    fun insertGroup(groupId: String?, profile: EaseGroupProfile?) {
        if (groupId.isNullOrEmpty()) {
            ChatLog.e(TAG, "insertGroup: groupId is null or empty")
            return
        }
        groupMap[groupId] = profile
    }

    fun getUser(userId: String?): EaseProfile? {
        if (userId.isNullOrEmpty()) {
            return null
        }
        return userMap[userId]
    }

    /**
     * Get the group info by groupId.
     * @param groupId The group id.
     * @return The group info.
     */
    fun getGroup(groupId: String?): EaseGroupProfile? {
        if (groupId.isNullOrEmpty()) {
            return null
        }
        return groupMap[groupId]
    }

    /**
     * Insert message userinfo to cache.
     */
    @Synchronized
    fun insertMessageUser(userId: String, profile: EaseProfile) {
        if (messageUserMap.containsKey(userId)) {
            if (messageUserMap[userId]!!.getTimestamp() < profile.getTimestamp()) {
                return
            }
        }
        messageUserMap[userId] = profile
    }

    /**
     * Get userinfo cache by userId.
     */
    fun getMessageUserInfo(userId: String?): EaseProfile? {
        if (userId.isNullOrEmpty() || !messageUserMap.containsKey(userId)) return null
        return messageUserMap[userId]
    }

    fun clear() {
        userMap.clear()
        groupMap.clear()
        messageUserMap.clear()
    }

    fun updateProfiles(profiles: List<EaseGroupProfile>) {
        if (profiles.isNotEmpty()) {
            profiles.forEach {
                groupMap[it.id] = it
            }
        }
    }

    fun updateUsers(users: List<EaseProfile>) {
        if (users.isNotEmpty()) {
            users.forEach {
                userMap[it.id] = it
            }
        }
    }
}