package io.agora.scene.aichat.imkit.model

import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.provider.getSyncUser
import org.json.JSONObject

/**
 * It is a bean for profile provider interface.
 * @param id The id, it can be the userId or the group id.
 * @param name The name of the user or the group.
 * @param avatar The avatar of the user or the group.
 * @param sign The sign of the user.
 * @param prompt The ext of the user.
 *
 */
open class EaseProfile constructor(
    val id: String,
    open var name: String? = null,
    open var avatar: String? = null,
    open var sign: String? = null,
    open var prompt: String? = null
) {
    private var _timestamp: Long = 0L

    internal fun setTimestamp(timestamp: Long) {
        _timestamp = timestamp
    }

    internal fun getTimestamp(): Long {
        return _timestamp
    }

    fun getNotEmptyName(): String {
        return name?.ifEmpty { id } ?: id
    }

    fun getChatBackground(): String {
        val avatar = avatar ?: return ""
        val avatarSuffix = avatar.split("/").last().split(".").first()
        val backgroundSuffix = avatarSuffix.replace("avatar", "background")
        return "aichat_$backgroundSuffix"
    }

    companion object {

        /**
         * Get the group member information from the cache or the user provider.
         * @param groupId The group id.
         * @param userId The user id.
         * @return The group member information.
         */
        fun getGroupMember(groupId: String?, userId: String?): EaseProfile? {
            return EaseIM.getUserProvider()?.getSyncUser(userId)
        }
    }
}

/**
 * Get more information of the user from user provider.
 */
fun EaseProfile.getFullInfo(): EaseProfile {
    if (name.isNullOrEmpty() || avatar.isNullOrEmpty()) {
        EaseIM.getUserProvider()?.getSyncUser(id)?.let {
            if (name.isNullOrEmpty()) {
                name = it.name
            }
            if (avatar.isNullOrEmpty()) {
                avatar = it.avatar
            }
        }
    }
    return this
}