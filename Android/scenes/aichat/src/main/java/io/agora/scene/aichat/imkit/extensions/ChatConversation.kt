package io.agora.scene.aichat.imkit.extensions

import io.agora.scene.aichat.R
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatCustomMessageBody
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageStatus
import io.agora.scene.aichat.imkit.ChatMessageType
import io.agora.scene.aichat.imkit.ChatTextMessageBody
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.aichat.imkit.model.EaseProfile

/**
 * Convert [ChatConversation] to [EaseConversation].
 */
fun ChatConversation.parse() = EaseConversation(
    conversationId = conversationId(),
    conversationType = type,
    unreadMsgCount = unreadMsgCount,
    lastMessage = lastMessage,
    timestamp = lastMessage?.msgTime ?: 0,
    isPinned = isPinned,
    pinnedTime = pinnedTime,
    extField = extField
)

fun ChatConversation.createAgentOrGroupSuccessMessage(isGroup: Boolean = false): ChatMessage? {
    val context = EaseIM.getContext() ?: return null
    return ChatMessage.createSendMessage(ChatMessageType.CUSTOM).let {
        it.from = EaseIM.getCurrentUser().id
        it.to = conversationId()
        it.chatType = ChatType.Chat
        val body = ChatCustomMessageBody(EaseConstant.MESSAGE_CUSTOM_ALERT)
        mutableMapOf(
            EaseConstant.MESSAGE_CUSTOM_ALERT_TYPE to EaseConstant.CHAT_WELCOME_MESSAGE,
            EaseConstant.MESSAGE_CUSTOM_ALERT_CONTENT to
                    if (isGroup) context.getString(R.string.aichat_new_group_welcome)
                    else context.getString(R.string.aichat_new_agent_welcome),
        ).let { map ->
            body.params = map
        }
        it.body = body
        it.setStatus(ChatMessageStatus.SUCCESS)
        it
    }
}

fun ChatConversation.saveGreetingMessage(info: EaseProfile, force: Boolean = false): ChatMessage? {
    if (lastMessage != null && !force) return null
    val context = EaseIM.getContext() ?: return null
    val message = if (info.id.contains("common-agent-001")) {
        context.getString(R.string.aichat_assistant_greeting)
    } else if (info.id.contains("common-agent-002")) {
        context.getString(R.string.aichat_programming_greeting)
    } else if (info.id.contains("common-agent-003")) {
        context.getString(R.string.aichat_attorney_greeting)
    } else if (info.id.contains("common-agent-004")) {
        context.getString(R.string.aichat_practitioner_greeting)
    } else {
        context.getString(R.string.aichat_common_greeting, info.name ?: "")
    }
    return ChatMessage.createReceiveMessage(ChatMessageType.TXT).let {
        it.from = conversationId()
        it.to = EaseIM.getCurrentUser().id
        it.chatType = ChatType.Chat
        val currentTimeMillis = System.currentTimeMillis()
        it.setLocalTime(currentTimeMillis)
        it.msgTime = currentTimeMillis
        it.body = ChatTextMessageBody(message)
        it.setStatus(ChatMessageStatus.SUCCESS)
        it.isUnread = false
        it
    }
}