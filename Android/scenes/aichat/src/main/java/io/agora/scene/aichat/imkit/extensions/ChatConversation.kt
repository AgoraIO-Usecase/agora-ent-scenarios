package io.agora.scene.aichat.imkit.extensions

import io.agora.scene.aichat.imkit.ChatConversation
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
    pinnedTime = pinnedTime
)