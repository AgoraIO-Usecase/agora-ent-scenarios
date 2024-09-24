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

fun ChatConversation.createAgentOrGroupSuccessMessage(
    isGroup: Boolean = false,
    name: String = ""
): ChatMessage? {
    EaseIM.getContext()?.let { context ->
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
    return null
}

fun ChatConversation.saveGreetingMessage(
    message: String = ""
): ChatMessage? {
    if (lastMessage != null) return null
    EaseIM.getContext()?.let { context ->
        return ChatMessage.createReceiveMessage(ChatMessageType.TXT).let {
            it.from = conversationId()
            it.to = EaseIM.getCurrentUser().id
            it.chatType = ChatType.Chat
            it.body = ChatTextMessageBody(message)
            it.setStatus(ChatMessageStatus.SUCCESS)
            it
        }
    }
    return null
}