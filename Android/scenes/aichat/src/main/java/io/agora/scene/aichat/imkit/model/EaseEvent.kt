package io.agora.scene.aichat.imkit.model

import java.io.Serializable

/**
 * Event class.
 */
class EaseEvent @JvmOverloads constructor(
    var event: String?,
    var type: TYPE,
    var message: String? = null,
    var refresh: Boolean = true
) : Serializable {

    val isMessageChange: Boolean
        get() = type == TYPE.MESSAGE
    val isGroupChange: Boolean
        get() = type == TYPE.GROUP
    val isContactChange: Boolean
        get() = type == TYPE.CONTACT
    val isNotifyChange: Boolean
        get() = type == TYPE.NOTIFY
    val isAccountChange: Boolean
        get() = type == TYPE.ACCOUNT
    val isConversationChange: Boolean
        get() = type == TYPE.CONVERSATION
    val  isReactionChange: Boolean
        get() = type == TYPE.REACTION
    val isPresenceChange:Boolean
        get() = type == TYPE.PRESENCE
    val isSilentChange: Boolean
        get() = type == TYPE.SILENT
    val isThreadChange:Boolean
        get() = type == TYPE.THREAD

    enum class TYPE {
        /**
         * Group event type.
         */
        GROUP,

        /**
         * Contact event type.
         */
        CONTACT,

        /**
         * Message event type.
         */
        MESSAGE,

        /**
         * Conversation event type.
         */
        CONVERSATION,

        /**
         * Notify event type.
         */
        NOTIFY,

        /**
         * Chat room event type.
         */
        CHAT_ROOM,

        /**
         * Account event type. For example, user logout.
         */
        ACCOUNT,

        /**
         * Reaction event type.
         */
        REACTION,

        /**
         * Presence event type.
         */
        PRESENCE,

        /**
         * Message reminder event.
         */
        SILENT,

        /**
         * Group member attribute event.
         */
        ATTRIBUTE,

        /**
         * thread event.
         */
        THREAD,
    }

    enum class EVENT {
        LEAVE,
        DESTROY,
        ADD,
        REMOVE,
        UPDATE,
        LOGOUT;

        operator fun plus(type: TYPE): String {
            return this.name + "/" + type.name
        }
    }

}