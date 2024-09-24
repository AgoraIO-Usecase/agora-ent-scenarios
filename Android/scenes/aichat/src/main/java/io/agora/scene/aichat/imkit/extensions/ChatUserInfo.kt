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
    ext = ext,
)