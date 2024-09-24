package io.agora.scene.aichat.imkit

import io.agora.scene.aichat.imkit.model.EaseProfile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EaseIMCache {
    private val userMap: ConcurrentMap<String, EaseProfile> = ConcurrentHashMap()
    private val messageUserMap: ConcurrentMap<String, EaseProfile> = ConcurrentHashMap()

    companion object {
        private const val TAG = "EaseIMCache"
    }

    fun init() {
        clear()
    }

    fun getAllUsers(): List<EaseProfile> {
        return userMap.values.toList()
    }

    fun insertUser(user: EaseProfile) {
        userMap[user.id] = user
    }

    fun getUser(userId: String?): EaseProfile? {
        if (userId.isNullOrEmpty()) {
            return null
        }
        return userMap[userId]
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
        messageUserMap.clear()
    }

    fun updateUsers(users: List<EaseProfile>) {
        if (users.isNotEmpty()) {
            users.forEach {
                userMap[it.id] = it
            }
        }
    }
}