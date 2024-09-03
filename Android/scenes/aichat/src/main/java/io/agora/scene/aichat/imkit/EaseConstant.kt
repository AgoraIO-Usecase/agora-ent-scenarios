package io.agora.scene.aichat.imkit

object EaseConstant {
    const val EXTRA_CHAT_TYPE = "chatType"
    const val EXTRA_CONVERSATION_ID = "conversationId"
    const val EXTRA_IS_FROM_SERVER = "isRoaming"
    const val EXTRA_SEARCH_MSG_ID = "extra_search_msg_id"
    const val EXTRA_CHAT_COMBINE_MESSAGE = "chatCombineMessage"

    const val MESSAGE_ATTR_IS_BIG_EXPRESSION = "em_is_big_expression"

    const val MESSAGE_ATTR_AT_MSG = "em_at_list"

    const val MESSAGE_ATTR_VALUE_AT_MSG_ALL = "ALL"

    const val MESSAGE_ATTR_THREAD_FLAG_PARENT_ID = "ease_parent_id"
    const val MESSAGE_ATTR_THREAD_FLAG_PARENT_MSG_ID = "ease_parent_msg_id"

    const val MESSAGE_EXT_USER_INFO_KEY = "ease_chat_uikit_user_info"

    const val MESSAGE_EXT_USER_INFO_NICKNAME_KEY = "nickname"

    const val MESSAGE_EXT_USER_INFO_AVATAR_KEY = "avatarURL"

    const val MESSAGE_EXT_USER_INFO_REMARK_KEY = "remark"

    const val MESSAGE_ATTR_EXPRESSION_ID = "em_expression_id"

    const val MESSAGE_TYPE_RECALL = "message_recall"
    const val MESSAGE_TYPE_CONTACT_NOTIFY = "message_contact_notify"

    // reply
    const val QUOTE_MSG_ID = "msgID"
    const val QUOTE_MSG_PREVIEW = "msgPreview"
    const val QUOTE_MSG_SENDER = "msgSender"
    const val QUOTE_MSG_TYPE = "msgType"
    const val QUOTE_MSG_QUOTE = "msgQuote"

    // user card
    const val USER_CARD_EVENT = "userCard"
    const val USER_CARD_ID = "uid"
    const val USER_CARD_NICK = "nickname"
    const val USER_CARD_AVATAR = "avatar"

    const val MESSAGE_CUSTOM_ALERT = "ease_message_alert"
    const val MESSAGE_CUSTOM_ALERT_TYPE = "ease_message_alert_type"
    const val MESSAGE_CUSTOM_ALERT_CONTENT = "ease_message_alert_content"

    const val GROUP_MEMBER_ATTRIBUTE_NICKNAME = "nickname"
    const val GROUP_WELCOME_MESSAGE = "group_welcome_message"
    const val GROUP_WELCOME_MESSAGE_GROUP_NAME = "group_welcome_message_group_name"

    // event
    const val EVENT_UPDATE_GROUP_NAME = "event_update_group_name"
    const val EVENT_UPDATE_GROUP_DESCRIPTION = "event_update_group_description"
    const val EVENT_UPDATE_GROUP_OWNER = "event_update_group_owner"
    const val EVENT_REMOVE_GROUP_MEMBER = "event_remove_group_member"

    const val SYSTEM_MESSAGE_FROM = "from"
    const val SYSTEM_MESSAGE_REASON = "reason"
    const val SYSTEM_MESSAGE_STATUS = "status"

    const val SYSTEM_MESSAGE_EXPIRED = "expired"

    const val DEFAULT_SYSTEM_MESSAGE_ID = "em_system"

    // translation status
    const val TRANSLATION_STATUS = "chat_uikit_translation_status"

    const val API_ASYNC_ADD_CONTACT  = "asyncAddContact"
    const val API_TRANSLATION_MESSAGE ="translationMessage"

    // multiple select
    const val EASE_MULTIPLE_SELECT = "ease_multiple_select"

    // thread
    const val THREAD_PARENT_ID = "ease_thread_parent_id"
    const val THREAD_CHAT_THREAD_ID = "ease_thread_id"
    const val THREAD_TOPIC_MESSAGE_ID = "ease_thread_topic_msg_id"
    const val THREAD_MESSAGE_ID = "ease_thread_msg_id"
    const val THREAD_NOTIFICATION_TYPE = "ease_thread_notification_type"

    //url preview
    const val MESSAGE_URL_PREVIEW = "ease_chat_uikit_text_url_preview"
    const val MESSAGE_URL_PREVIEW_URL = "url"
    const val MESSAGE_URL_PREVIEW_TITLE = "title"
    const val MESSAGE_URL_PREVIEW_DESCRIPTION = "description"
    const val MESSAGE_URL_PREVIEW_IMAGE_URL = "imageUrl"


    //  Date format
    const val DEFAULT_CONV_TODAY_FORMAT = "HH:mm"
    const val DEFAULT_CONV_OTHER_DAY_FORMAT = "MMM dd"
    const val DEFAULT_CONV_OTHER_YEAR_FORMAT = "MMM dd, yyyy"
    const val DEFAULT_CHAT_TODAY_FORMAT = "HH:mm"
    const val DEFAULT_CHAT_OTHER_DAY_FORMAT = "MMM dd, HH:mm"
    const val DEFAULT_CHAT_OTHER_YEAR_FORMAT = "MMM dd, yyyy HH:mm"
}

