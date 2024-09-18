package io.agora.scene.aichat.imkit.extensions

import io.agora.scene.aichat.imkit.ChatUserInfo
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.aichat.imkit.model.EaseProfile
import org.json.JSONObject

/**
 * It is a file used to convert Chat SDK classes into easeui SDK classes.
 */

/**
 * Convert [ChatUserInfo] to [EaseProfile].
 */
internal fun ChatUserInfo.parse() = EaseProfile(
    id = userId,
    name = nickname,
    avatar = avatarUrl,
    sign = signature,
    voiceId = birth,
    prompt = this.getPrompt(),
)

internal fun ChatUserInfo.getPrompt(): String {
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

internal fun ChatUserInfo.getBotIds(): List<String> {
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

internal fun ChatUserInfo.isChat(): Boolean {
    var isChat = true
    runCatching {
        ext?.let {
            val js = JSONObject(it)
            isChat = !js.optBoolean("bot_group", false)
        }
    }.getOrElse {
        isChat = true
    }
    return isChat
}

internal fun ChatUserInfo.isGroup(): Boolean {
    var isGroup = false
    try {
        ext?.let {
            val js = JSONObject(it)
            isGroup = js.optBoolean("bot_group", false)
        }
    } catch (ex: Exception) {
        isGroup = false
    }
    return isGroup
}