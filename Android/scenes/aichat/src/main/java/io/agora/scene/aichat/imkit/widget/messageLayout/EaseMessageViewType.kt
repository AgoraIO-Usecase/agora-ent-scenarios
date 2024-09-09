package io.agora.scene.aichat.imkit.widget.messageLayout

enum class EaseMessageViewType(val value: Int) {
    VIEW_TYPE_MESSAGE_TXT_ME(0),
    VIEW_TYPE_MESSAGE_TXT_OTHER(1),
    VIEW_TYPE_MESSAGE_IMAGE_ME(2),
    VIEW_TYPE_MESSAGE_IMAGE_OTHER(3),
    VIEW_TYPE_MESSAGE_VIDEO_ME(4),
    VIEW_TYPE_MESSAGE_VIDEO_OTHER(5),
    VIEW_TYPE_MESSAGE_LOCATION_ME(6),
    VIEW_TYPE_MESSAGE_LOCATION_OTHER(7),
    VIEW_TYPE_MESSAGE_VOICE_ME(8),
    VIEW_TYPE_MESSAGE_VOICE_OTHER(9),
    VIEW_TYPE_MESSAGE_FILE_ME(10),
    VIEW_TYPE_MESSAGE_FILE_OTHER(11),
    VIEW_TYPE_MESSAGE_CMD_ME(12),
    VIEW_TYPE_MESSAGE_CMD_OTHER(13),
    VIEW_TYPE_MESSAGE_CUSTOM_ME(14),
    VIEW_TYPE_MESSAGE_CUSTOM_OTHER(15),
    VIEW_TYPE_MESSAGE_COMBINE_ME(16),
    VIEW_TYPE_MESSAGE_COMBINE_OTHER(17),
    VIEW_TYPE_MESSAGE_UNSENT_ME(18),
    VIEW_TYPE_MESSAGE_UNSENT_OTHER(19),
    VIEW_TYPE_MESSAGE_CHAT_THREAD_NOTIFY(20),
    VIEW_TYPE_MESSAGE_USER_CARD_ME(22),
    VIEW_TYPE_MESSAGE_USER_CARD_OTHER(23),
    VIEW_TYPE_MESSAGE_ALERT(60),
    VIEW_TYPE_MESSAGE_UNKNOWN_ME(98),
    VIEW_TYPE_MESSAGE_UNKNOWN_OTHER(99);

    companion object {
        fun from(value: Int): EaseMessageViewType {
            val types = values()
            val length = types.size
            for (i in 0 until length) {
                val type = types[i]
                if (type.value == value) {
                    return type
                }
            }
            return VIEW_TYPE_MESSAGE_UNKNOWN_OTHER
        }
    }
}