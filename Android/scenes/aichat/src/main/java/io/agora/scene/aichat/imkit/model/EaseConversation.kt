package io.agora.scene.aichat.imkit.model

import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.isReceive
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.provider.getSyncUser
import org.json.JSONObject
import java.io.Serializable
import java.util.Arrays

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
    val isPinned: Boolean,
    val pinnedTime: Long,
    val extField: String?,
) : Serializable, Comparable<EaseConversation> {

    override fun compareTo(other: EaseConversation): Int {
        return if (other.isPinned && !isPinned) {
            1
        } else if (!other.isPinned && isPinned) {
            -1
        } else {
            (other.timestamp - timestamp).toInt()
        }
    }
}

internal fun EaseConversation.isChat(): Boolean {
    var isChat = true
     runCatching {
        extField?.let {
            val js = JSONObject(it)
            isChat = !js.optBoolean("bot_group", false)
        }
    }.getOrElse {
         isChat = true
    }
    return isChat
}

internal fun EaseConversation.isGroup(): Boolean {
    var isGroup = false
    try {
        extField?.let {
            val js = JSONObject(it)
            isGroup = js.optBoolean("bot_group", false)
        }
    } catch (ex: Exception) {
        isGroup = false
    }
    return isGroup
}


// 获取会话名称, 如果是群组，则返回群组名称，否则返回用户昵称
internal fun EaseConversation.getConversationName(): String {
    val name = if (isChat()) {
        EaseIM.getUserProvider()?.getSyncUser(conversationId)?.getNotEmptyName() ?: conversationId
    } else {
        runCatching {
            extField?.let {
                val js = JSONObject(it)
                js.optString("groupName", conversationId)
            } ?: conversationId
        }.getOrElse {
            it.printStackTrace()
            conversationId  // 如果发生异常，默认认为是单聊
        }
    }
    return name
}

// 获取会话头像
internal fun EaseConversation.getChatAvatar(): String {
    return EaseIM.getUserProvider()?.getSyncUser(conversationId)?.avatar ?: ""
}

// 获取会话头像，如果是群组，则返回群组头像，否则返回用户头像
internal fun EaseConversation.getGroupAvatars(): List<String> {
    val avatarList = mutableListOf<String>()
    if (isGroup()) {
        val groupAvatar = runCatching {
            extField?.let {
                val js = JSONObject(it)
                js.optString("groupIcon", "")
            } ?: ""
        }.getOrElse {
            it.printStackTrace()
            ""
        }
        avatarList.addAll(groupAvatar.split(","))
    }
    return avatarList
}

internal fun EaseConversation.getGroupLastUser(): String {
    var lastUserId = ""
    if (isGroup()) {
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
    return EaseIM.getUserProvider()?.getSyncUser(lastUserId)?.getNotEmptyName() ?: lastUserId
}