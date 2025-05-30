package io.agora.scene.aichat.imkit

import io.agora.scene.aichat.imkit.helper.EasePreferenceManager
import io.agora.scene.aichat.imkit.model.EaseProfile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EaseIMCache {
    companion object {
        private const val TAG = "EaseIMCache"
    }

    private val userMap: ConcurrentMap<String, EaseProfile> = ConcurrentHashMap()
    private val messageUserMap: ConcurrentMap<String, EaseProfile> = ConcurrentHashMap()
    private val messageAudioMap: ConcurrentMap<String, HashMap<String, String>> = ConcurrentHashMap()

    fun init() {
        clear()
    }

    fun insertMessageAudio(conversationId: String, messageId: String, audioPath: String) {
        messageAudioMap.computeIfAbsent(conversationId) { HashMap() }.put(messageId, audioPath)
        EasePreferenceManager.getInstance().storeChatMessageAudio(conversationId, messageId, audioPath)
    }

    fun reloadMessageAudioList(conversationId: String) {
        // 从 Preference 中加载数据
        val audioMap = EasePreferenceManager.getInstance().loadedChatMessageAudioList(conversationId)
        messageAudioMap[conversationId] = audioMap.toMutableMap() as HashMap<String, String>
    }

    fun getAudiPath(conversationId: String, messageId: String): String? {
        return messageAudioMap[conversationId]?.get(messageId)
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

    fun removeUser(userId: String) {
        userMap.remove(userId)
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