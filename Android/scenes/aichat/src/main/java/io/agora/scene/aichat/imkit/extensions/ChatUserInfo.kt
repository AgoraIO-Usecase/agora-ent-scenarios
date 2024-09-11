package io.agora.scene.aichat.imkit.extensions

import io.agora.scene.aichat.imkit.ChatUserInfo
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