package io.agora.scene.aichat.imkit.extensions

import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatConversationType
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
    isPinned = false,
    pinnedTime = -1L
)

/**
 * Whether the conversation is group chat.
 */
val ChatConversation.isGroupChat: Boolean
    get() = type == ChatConversationType.GroupChat

/**
 * Whether the conversation is chat room.
 */
val ChatConversation.isChatroom: Boolean
    get() = type == ChatConversationType.ChatRoom