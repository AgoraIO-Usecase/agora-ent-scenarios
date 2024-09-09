package io.agora.scene.aichat.imkit.widget.messageLayout

import android.view.ViewGroup
import io.agora.scene.aichat.imkit.ChatCustomMessageBody
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageDirection
import io.agora.scene.aichat.imkit.ChatMessageType
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatRowText
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatRowThreadNotify
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatRowUnknown

object EaseChatViewHolderFactory {
    fun createViewHolder(
        parent: ViewGroup,
        viewType: EaseMessageViewType
    ): EaseBaseRecyclerViewAdapter.ViewHolder<ChatMessage> {
        return when (viewType) {
            EaseMessageViewType.VIEW_TYPE_MESSAGE_TXT_ME, EaseMessageViewType.VIEW_TYPE_MESSAGE_TXT_OTHER -> EaseChatRowViewHolder(
                EaseChatRowText(
                    parent.context,
                    isSender = viewType == EaseMessageViewType.VIEW_TYPE_MESSAGE_TXT_ME
                )
            )

            EaseMessageViewType.VIEW_TYPE_MESSAGE_CHAT_THREAD_NOTIFY -> EaseThreadNotifyViewHolder(
                EaseChatRowThreadNotify(parent.context)
            )

            else -> EaseUnknownViewHolder(EaseChatRowUnknown(parent.context, isSender = false))
        }
    }

    fun getViewType(message: ChatMessage?): Int {
        return message?.let { getChatType(it).value } ?: EaseMessageViewType.VIEW_TYPE_MESSAGE_UNKNOWN_OTHER.value
    }

    fun getChatType(message: ChatMessage): EaseMessageViewType {
        val type: EaseMessageViewType
        val messageType = message.type
        val direct = message.direct()
        type = if (messageType == ChatMessageType.TXT) {
            val isThreadNotify: Boolean =
                message.getBooleanAttribute(EaseConstant.THREAD_NOTIFICATION_TYPE, false)
            val isRecallMessage: Boolean =
                message.getBooleanAttribute(EaseConstant.MESSAGE_TYPE_RECALL, false)
            val isContactNotify: Boolean =
                message.getBooleanAttribute(EaseConstant.MESSAGE_TYPE_CONTACT_NOTIFY, false)
            if (isThreadNotify) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_CHAT_THREAD_NOTIFY
            } else if (isRecallMessage || isContactNotify) {
                if (direct == ChatMessageDirection.SEND) {
                    EaseMessageViewType.VIEW_TYPE_MESSAGE_UNSENT_ME
                } else {
                    EaseMessageViewType.VIEW_TYPE_MESSAGE_UNSENT_OTHER
                }
            } else {
                if (direct == ChatMessageDirection.SEND) {
                    EaseMessageViewType.VIEW_TYPE_MESSAGE_TXT_ME
                } else {
                    EaseMessageViewType.VIEW_TYPE_MESSAGE_TXT_OTHER
                }
            }
        } else if (messageType == ChatMessageType.IMAGE) {
            if (direct == ChatMessageDirection.SEND) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_IMAGE_ME
            } else {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_IMAGE_OTHER
            }
        } else if (messageType == ChatMessageType.VIDEO) {
            if (direct == ChatMessageDirection.SEND) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_VIDEO_ME
            } else {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_VIDEO_OTHER
            }
        } else if (messageType == ChatMessageType.LOCATION) {
            if (direct == ChatMessageDirection.SEND) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_LOCATION_ME
            } else {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_LOCATION_OTHER
            }
        } else if (messageType == ChatMessageType.VOICE) {
            if (direct == ChatMessageDirection.SEND) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_VOICE_ME
            } else {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_VOICE_OTHER
            }
        } else if (messageType == ChatMessageType.FILE) {
            if (direct == ChatMessageDirection.SEND) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_FILE_ME
            } else {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_FILE_OTHER
            }
        } else if (messageType == ChatMessageType.CMD) {
            if (direct == ChatMessageDirection.SEND) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_CMD_ME
            } else {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_CMD_OTHER
            }
        } else if (messageType == ChatMessageType.CUSTOM) {
            val event = (message.body as? ChatCustomMessageBody)?.event() ?: ""
            if (event == EaseConstant.USER_CARD_EVENT) {
                if (direct == ChatMessageDirection.SEND) {
                    EaseMessageViewType.VIEW_TYPE_MESSAGE_USER_CARD_ME
                } else {
                    EaseMessageViewType.VIEW_TYPE_MESSAGE_USER_CARD_OTHER
                }
            } else if (event == EaseConstant.MESSAGE_CUSTOM_ALERT) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_ALERT
            } else {
                if (direct == ChatMessageDirection.SEND) {
                    EaseMessageViewType.VIEW_TYPE_MESSAGE_CUSTOM_ME
                } else {
                    EaseMessageViewType.VIEW_TYPE_MESSAGE_CUSTOM_OTHER
                }
            }
        } else if (messageType == ChatMessageType.COMBINE) {
            if (direct == ChatMessageDirection.SEND) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_COMBINE_ME
            } else {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_COMBINE_OTHER
            }
        } else {
            if (direct == ChatMessageDirection.SEND) {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_UNKNOWN_ME
            } else {
                EaseMessageViewType.VIEW_TYPE_MESSAGE_UNKNOWN_OTHER
            }
        }
        return type
    }
}