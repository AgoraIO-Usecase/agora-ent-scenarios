package io.agora.scene.aichat.imkit.model

import io.agora.scene.aichat.create.logic.PreviewAvatarItem
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.provider.getSyncUser
import org.json.JSONObject
import java.io.Serializable

/**
 * The class is used to display the conversation information.
 * @param conversationId The conversation id.
 * @param conversationType The conversation type.
 * @param unreadMsgCount The unread message count.
 * @param lastMessage The last message.
 * @param timestamp The last message timestamp.
 * @param isPinned The conversation is pinned or not.
 * @param pinnedTime The pinned time.
 * @param extField The extField of conversation.
 */
data class EaseConversation constructor(
    val conversationId: String,
    val conversationType: ChatConversationType,
    val unreadMsgCount: Int,
    val lastMessage: ChatMessage?,
    val timestamp: Long,
    val extField: String?,
    val conversationUser: EaseProfile?,
) : Serializable, Comparable<EaseConversation> {

    override fun compareTo(other: EaseConversation): Int {
        return (other.timestamp - timestamp).toInt()
    }
}

internal fun EaseConversation.getGroupLastUser(): String {
    var lastUserId = ""
    val isGroup = EaseIM.getUserProvider().getSyncUser(conversationId)?.isGroup() ?: false
    if (isGroup) {
        if (lastMessage?.isSend() == true) {
            lastUserId = EaseIM.getCurrentUser().id
        } else {
            runCatching {
                lastMessage?.attributes?.get("ai_chat")?.let { aiChat ->
                    val js = JSONObject(aiChat.toString())
                    val userMeta = js.optString("user_meta", "")
                    lastUserId = JSONObject(userMeta).optString("botId", "")
                } ?: ""
            }.getOrElse {
                it.printStackTrace()
                lastUserId = ""
            }
        }
    }
    return EaseIM.getUserProvider().getSyncUser(lastUserId)?.getNotEmptyName() ?: lastUserId
}

internal fun EaseConversation.isPublicAgent(): Boolean {
    return conversationId.contains("common-agent")
}

internal fun EaseConversation.isUserAgent(): Boolean {
    return conversationId.contains("user-agent")
}

internal fun EaseConversation.isGroupAgent(): Boolean {
    return conversationId.contains("user-group")
}