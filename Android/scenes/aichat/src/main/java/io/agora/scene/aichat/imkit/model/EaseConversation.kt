package io.agora.scene.aichat.imkit.model

import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.EaseIM
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
 */
data class EaseConversation(
    val conversationId: String,
    val conversationType: ChatConversationType,
    val unreadMsgCount: Int,
    val lastMessage: ChatMessage?,
    val timestamp: Long,
    val isPinned: Boolean = false,
    val pinnedTime: Long = -1L,
): Serializable, Comparable<EaseConversation> {
    private var isSelected: Boolean = false
    private var onSelectedListener: ((Boolean) -> Unit)? = null

    fun isSelected(): Boolean {
        return isSelected
    }

    fun setSelected(isSelected: Boolean) {
        this.isSelected = isSelected
        onSelectedListener?.invoke(isSelected)
    }

    /**
     * Set the listener of conversation selected event.
     */
    internal fun setOnSelectedListener(onSelected: (Boolean) -> Unit) {
        this.onSelectedListener = onSelected
    }

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

/**
 * Get the bean of [ChatConversation] by conversation id.
 */
fun EaseConversation.chatConversation(): ChatConversation? = ChatClient.getInstance().chatManager().getConversation(conversationId)

/**
 * Get chat type from conversation type.
 */
fun EaseConversation.getChatType(): EaseChatType {
    val conversation = chatConversation()
    return if (chatConversation()?.isGroup == true) {
        if (conversation?.type === ChatConversationType.ChatRoom) {
            EaseChatType.CHATROOM
        } else {
            EaseChatType.GROUP_CHAT
        }
    } else {
        EaseChatType.SINGLE_CHAT
    }
}

fun EaseConversation.isGroupChat() = conversationType == ChatConversationType.GroupChat

fun EaseConversation.isChatRoom() = conversationType == ChatConversationType.ChatRoom

fun EaseConversation.isChat() = conversationType == ChatConversationType.Chat
