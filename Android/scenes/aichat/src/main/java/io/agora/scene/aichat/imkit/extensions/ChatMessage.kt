package io.agora.scene.aichat.imkit.extensions

import io.agora.scene.aichat.imkit.impl.CallbackImpl
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatCustomMessageBody
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatMessageDirection
import io.agora.scene.aichat.imkit.ChatMessageStatus
import io.agora.scene.aichat.imkit.ChatMessageType
import io.agora.scene.aichat.imkit.ChatTextMessageBody
import io.agora.scene.aichat.imkit.ChatType
import io.agora.scene.aichat.imkit.helper.DateFormatHelper
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.impl.OnError
import io.agora.scene.aichat.imkit.impl.OnProgress
import io.agora.scene.aichat.imkit.impl.OnSuccess
import io.agora.scene.aichat.imkit.provider.getSyncUser
import org.json.JSONObject


/**
 * Send message to server.
 * @param onSuccess The callback when send message success.
 * @param onError The callback when send message error.
 * @param onProgress The callback when send message progress.
 */
fun ChatMessage.send(onSuccess: OnSuccess = {}, onError: OnError = { _, _ -> }, onProgress: OnProgress = {}) {
    // Set the message status callback by ChatMessage.
    // Should set callback before send message.
    setMessageStatusCallback(CallbackImpl(onSuccess, onError, onProgress))
    ChatClient.getInstance().chatManager().sendMessage(this)
}

internal fun ChatMessage.getMessageDigest(): String {
    return when (type) {
        ChatMessageType.TXT -> {
            (body as ChatTextMessageBody).let {
                it.message
            }
        }

        ChatMessageType.CUSTOM -> {
            if (isAlertMessage()) {
                (body as ChatCustomMessageBody).params[EaseConstant.MESSAGE_CUSTOM_ALERT_CONTENT] ?: ""
            } else {
                ""
            }
        }

        else -> {
            ""
        }
    }
}

internal fun ChatMessage.getSyncUserFromProvider(): EaseProfile? {
    return if (chatType == ChatType.Chat) {
        if (direct() == ChatMessageDirection.RECEIVE) {
            EaseIM.getUserProvider().getSyncUser(from)
        } else {
            EaseIM.getCurrentUser()
        }
    } else {
        null
    }
}

/**
 * Create a local message
 */
internal fun ChatMessage.createReceiveLoadingMessage(): ChatMessage {
    val msgNotification = ChatMessage.createReceiveMessage(ChatMessageType.CUSTOM)

    val customBody = ChatCustomMessageBody(EaseConstant.MESSAGE_CUSTOM_LOADING)
    msgNotification.msgId = System.currentTimeMillis().toString()
    msgNotification.addBody(customBody)
    msgNotification.to = this.to
    msgNotification.from = this.from
    msgNotification.msgTime = System.currentTimeMillis()
    msgNotification.chatType = io.agora.chat.ChatMessage.ChatType.Chat
    msgNotification.setLocalTime(System.currentTimeMillis())
    msgNotification.setStatus(ChatMessageStatus.SUCCESS)
    msgNotification.setIsChatThreadMessage(false)
    return msgNotification
}

/**
 * Get the timestamp of the message based on the chat options.
 * @return The timestamp of the message.
 */
fun ChatMessage.getTimestamp(): Long {
    return if (ChatClient.getInstance().options.isSortMessageByServerTime) msgTime else localTime()
}

/**
 * Get the String timestamp from [ChatMessage].
 */
fun ChatMessage.getDateFormat(isChat: Boolean = false): String? {
    val timestamp = getTimestamp()
    if (isChat) {
        return if (DateFormatHelper.isSameDay(timestamp)) {
            DateFormatHelper.timestampToDateString(
                timestamp, EaseConstant.DEFAULT_CHAT_TODAY_FORMAT
            )
        } else if (DateFormatHelper.isSameYear(timestamp)) {
            DateFormatHelper.timestampToDateString(
                timestamp, EaseConstant.DEFAULT_CHAT_OTHER_DAY_FORMAT
            )
        } else {
            DateFormatHelper.timestampToDateString(
                timestamp, EaseConstant.DEFAULT_CHAT_OTHER_YEAR_FORMAT
            )
        }
    } else {
        return if (DateFormatHelper.isSameDay(timestamp)) {
            DateFormatHelper.timestampToDateString(
                timestamp, EaseConstant.DEFAULT_CONV_TODAY_FORMAT
            )
        } else if (DateFormatHelper.isSameYear(timestamp)) {
            DateFormatHelper.timestampToDateString(
                timestamp, EaseConstant.DEFAULT_CONV_OTHER_DAY_FORMAT
            )
        } else {
            DateFormatHelper.timestampToDateString(
                timestamp, EaseConstant.DEFAULT_CONV_OTHER_YEAR_FORMAT
            )
        }
    }
}

/**
 * Check whether the message is a user card message.
 */
fun ChatMessage.isUserCardMessage(): Boolean {
    val event = (body as? ChatCustomMessageBody)?.event() ?: ""
    return event == EaseConstant.USER_CARD_EVENT
}

/**
 * Check whether the message is a alert message.
 */
fun ChatMessage.isAlertMessage(): Boolean {
    val event = (body as? ChatCustomMessageBody)?.event() ?: ""
    return event == EaseConstant.MESSAGE_CUSTOM_ALERT
}

/**
 * Get user card info from message.
 */
fun ChatMessage.getUserCardInfo(): EaseProfile? {
    if (isUserCardMessage()) {
        (body as? ChatCustomMessageBody)?.let {
            val params: Map<String, String> = it.params
            val uId = params[EaseConstant.USER_CARD_ID]
            val nickname = params[EaseConstant.USER_CARD_NICK]
            val headUrl = params[EaseConstant.USER_CARD_AVATAR]
            if (uId.isNullOrEmpty()) return null
            return EaseProfile(uId, nickname, headUrl)
        }
    }
    return null
}

internal fun ChatMessage.isGroupChat(): Boolean {
    return chatType == ChatType.GroupChat
}

internal fun ChatMessage.isSingleChat(): Boolean {
    return chatType == ChatType.Chat
}

/**
 * Check whether the message is sent by current user.
 */
internal fun ChatMessage.isSend(): Boolean {
    return direct() == ChatMessageDirection.SEND
}

internal fun ChatMessage.isReceive(): Boolean {
    return direct() == ChatMessageDirection.RECEIVE
}

/**
 * Add userinfo to message when sending message.
 */
internal fun ChatMessage.addUserInfo(nickname: String?, avatarUrl: String?, remark: String? = null) {
    if (nickname.isNullOrEmpty() && avatarUrl.isNullOrEmpty() && remark.isNullOrEmpty()) {
        return
    }
    val info = JSONObject()
    if (!nickname.isNullOrEmpty()) info.put(EaseConstant.MESSAGE_EXT_USER_INFO_NICKNAME_KEY, nickname)
    if (!avatarUrl.isNullOrEmpty()) info.put(EaseConstant.MESSAGE_EXT_USER_INFO_AVATAR_KEY, avatarUrl)
    if (!remark.isNullOrEmpty()) info.put(EaseConstant.MESSAGE_EXT_USER_INFO_REMARK_KEY, remark)
    setAttribute(EaseConstant.MESSAGE_EXT_USER_INFO_KEY, info)
}

/**
 * Parse userinfo from message when receiving a message.
 */
internal fun ChatMessage.getUserInfo(updateCache: Boolean = false): EaseProfile? {
    EaseIM.getUserProvider().getSyncUser(from)?.let {
        return it
    }
    var profile: EaseProfile? = EaseProfile(from)
    try {
        getJSONObjectAttribute(EaseConstant.MESSAGE_EXT_USER_INFO_KEY)?.let { info ->
            profile = EaseProfile(
                id = from,
                name = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_NICKNAME_KEY),
                avatar = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_AVATAR_KEY),
            )
            profile?.setTimestamp(msgTime)
            EaseIM.getCache().insertMessageUser(from, profile!!)
            profile
        } ?: kotlin.run {
            EaseIM.getCache().getMessageUserInfo(from)
        }
    } catch (e: ChatException) {
        profile = EaseIM.getCache().getMessageUserInfo(from)
    }
    if (profile == null) {
        profile = EaseProfile(from)
    }
    return profile
}

/**
 * Check whether the message is a silent message.
 */
internal fun ChatMessage.isSilentMessage(): Boolean {
    return getBooleanAttribute("em_ignore_notification", false)
}

/**
 * Check whether the message can be edited.
 */
internal fun ChatMessage.canEdit(): Boolean {
    return type == ChatMessageType.TXT && isSend() && isSuccess()
}

internal fun ChatMessage.isSuccess(): Boolean {
    return status() == ChatMessageStatus.SUCCESS
}

internal fun ChatMessage.isFail(): Boolean {
    return status() == ChatMessageStatus.FAIL
}

internal fun ChatMessage.inProgress(): Boolean {
    return status() == ChatMessageStatus.INPROGRESS
}

/**
 * Check if the message id is valid.
 */
internal fun isMessageIdValid(messageId: String?): Boolean {
    // If the message id is null or empty, return true.
    if (messageId.isNullOrEmpty()) {
        return true
    }
    ChatClient.getInstance().chatManager().getMessage(messageId)?.let {
        return true
    } ?: return false
}