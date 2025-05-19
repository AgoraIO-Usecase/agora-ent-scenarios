package io.agora.scene.aichat.imkit.model

import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.provider.getSyncUser
import org.json.JSONObject

/**
 * It is a bean for profile provider interface.
 * @param id The id, it can be the userId or the group id.
 * @param name The name of the user or the group.
 * @param avatar The avatar of the user or the group.
 * @param sign The sign of the user.
 * @param ext The ext of the user.
 *
 */
data class EaseProfile constructor(
    val id: String,
    var name: String? = null,
    var avatar: String? = null,
    var sign: String? = null,
    var voiceId: String? = null, // 用户属性中birth字段存的是voiceId
    var ext: String? = null,
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
}

fun EaseProfile.getAllGroupAgents(): List<EaseProfile> {
    val bots = getBotIds()
    val easeProfileList = mutableListOf<EaseProfile>()
    bots.forEach {
        EaseIM.getUserProvider().getSyncUser(it)?.let { profile ->
            easeProfileList.add(profile)
        }
    }
    return easeProfileList
}

fun EaseProfile.getPrompt(): String {
    var prompt = ""
    try {
        ext?.let {
            val js = JSONObject(it)
            prompt = js.optString("prompt")
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return prompt
}

fun EaseProfile.getBots(): String {
    var botIds = ""
    try {
        ext?.let {
            val js = JSONObject(it)
            botIds = js.optString("botIds")
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return botIds
}

fun EaseProfile.getBotIds(): List<String> {
    var botIds = ""
    try {
        ext?.let {
            val js = JSONObject(it)
            botIds = js.optString("botIds")
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return botIds.split(",")
}

fun EaseProfile.isPublicAgent(): Boolean {
    return id.contains("common-agent")
}

fun EaseProfile.isUserAgent(): Boolean {
    return id.contains("user-agent")
}

fun EaseProfile.isGroupAgent(): Boolean {
    return id.contains("user-group")
}

fun EaseProfile.isChat(): Boolean {
    return isPublicAgent() || isUserAgent()
}

fun EaseProfile.isGroup(): Boolean {
    return isGroupAgent()
}

fun EaseProfile.getGroupAvatars(): List<String> {
    return avatar?.split(",") ?: emptyList()
}