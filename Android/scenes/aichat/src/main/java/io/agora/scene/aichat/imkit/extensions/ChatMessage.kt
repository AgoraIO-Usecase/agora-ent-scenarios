package io.agora.scene.aichat.imkit.extensions

import android.content.Context
import io.agora.scene.aichat.imkit.impl.CallbackImpl
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatCustomMessageBody
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.ChatLog
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
fun ChatMessage.send(
    onSuccess: OnSuccess = {}, onError: OnError = { _, _ -> }, onProgress: OnProgress = {}
) {
    // Set the message status callback by ChatMessage.
    // Should set callback before send message.
    setMessageStatusCallback(CallbackImpl(onSuccess, onError, onProgress))
    ChatClient.getInstance().chatManager().sendMessage(this)
}

/**
 * Set parent message id attribute for chat thread message.
 * @param parentId The parent id, usually is the group that the chat thread belongs to.
 * @param parentMsgId The parent message id, usually is the group message id which created the chat thread.
 */
fun ChatMessage.setParentInfo(parentId: String?, parentMsgId: String?): Boolean {
    if (isChatThreadMessage && (parentId.isNullOrEmpty().not() || parentMsgId.isNullOrEmpty().not())) {
        if (parentId.isNullOrEmpty().not()) {
            setAttribute(EaseConstant.MESSAGE_ATTR_THREAD_FLAG_PARENT_ID, parentId)
        }
        if (parentMsgId.isNullOrEmpty().not()) {
            setAttribute(EaseConstant.MESSAGE_ATTR_THREAD_FLAG_PARENT_MSG_ID, parentMsgId)
        }
        return true
    }
    return false
}

/**
 * Get parent id attribute for chat thread message.
 * @return The parent id.
 */
fun ChatMessage.getParentId(): String? {
    if (isChatThreadMessage.not()) {
        return null
    }
    val parentId = getStringAttribute(EaseConstant.MESSAGE_ATTR_THREAD_FLAG_PARENT_ID, "")
    return if (parentId.isNullOrEmpty()) {
        ChatClient.getInstance().chatManager().getMessage(getParentMessageId())?.conversationId()
    } else {
        parentId
    }
}

/**
 * Get parent message id attribute for chat thread message.
 * @return The parent message id.
 */
fun ChatMessage.getParentMessageId(): String? {
    if (isChatThreadMessage.not()) {
        return null
    }
    return getStringAttribute(EaseConstant.MESSAGE_ATTR_THREAD_FLAG_PARENT_MSG_ID, "")
}

internal fun ChatMessage.getMessageDigest(context: Context): String {
    return when (type) {
        ChatMessageType.TXT -> {
            (body as ChatTextMessageBody).let {
                getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false).let { isBigExp ->
                    if (isBigExp) {
                        if (it.message.isNullOrEmpty()) {
                            "未知消息"
                        } else {
                            it.message
                        }
                    } else {
                        it.message
                    }
                } ?: it.message
            }
        }

        else -> {
            "未知消息"
        }
    }
}

internal fun ChatMessage.getSyncUserFromProvider(): EaseProfile? {
    return if (chatType == ChatType.Chat) {
        if (direct() == ChatMessageDirection.RECEIVE) {
            // Get user info from user profile provider.
            EaseIM.getUserProvider()?.getSyncUser(from)
        } else {
            EaseIM.getCurrentUser()
        }
    } else if (chatType == ChatType.GroupChat) {
        if (direct() == ChatMessageDirection.RECEIVE) {
            // Get user info from cache first.
            // Then get user info from user provider.
            EaseProfile.getGroupMember(conversationId(), from)
        } else {
            EaseIM.getCurrentUser()
        }
    } else {
        null
    }
}

/**
 * Create a local message when unsent a message or receive a unsent message.
 */
internal fun ChatMessage.createUnsentMessage(isReceive: Boolean = false): ChatMessage {
    val msgNotification = if (isReceive) {
        ChatMessage.createReceiveMessage(ChatMessageType.TXT)
    } else {
        ChatMessage.createSendMessage(ChatMessageType.TXT)
    }

    val text: String = if (isSend()) {
        "你撤回了一条消息"
    } else {
        "${getUserInfo()?.getRemarkOrName() ?: from} 撤回了一条消息"
    }
    val txtBody = ChatTextMessageBody(
        text
    )
    msgNotification.msgId = msgId
    msgNotification.addBody(txtBody)
    msgNotification.to = to
    msgNotification.from = from
    msgNotification.msgTime = msgTime
    msgNotification.chatType = chatType
    msgNotification.setLocalTime(localTime())
    msgNotification.setAttribute(EaseConstant.MESSAGE_TYPE_RECALL, true)
    msgNotification.setStatus(ChatMessageStatus.SUCCESS)
    msgNotification.setIsChatThreadMessage(isChatThreadMessage)
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
    EaseIM.getUserProvider()?.getSyncUser(from)?.let {
        return it
    }
    var profile: EaseProfile? = EaseProfile(from)
    try {
        getJSONObjectAttribute(EaseConstant.MESSAGE_EXT_USER_INFO_KEY)?.let { info ->
            profile = EaseProfile(
                id = from,
                name = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_NICKNAME_KEY),
                avatar = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_AVATAR_KEY),
                remark = info.optString(EaseConstant.MESSAGE_EXT_USER_INFO_REMARK_KEY)
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
    return type == ChatMessageType.TXT
            && isSend()
            && isSuccess()
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

internal fun ChatMessage.isUnsentMessage(): Boolean {
    return if (type == ChatMessageType.TXT) {
        getBooleanAttribute(EaseConstant.MESSAGE_TYPE_RECALL, false)
    } else {
        false
    }
}

/**
 * Judge whether the message is a reply message.
 */
internal fun ChatMessage.isReplyMessage(jsonResult: (JSONObject) -> Unit = {}): Boolean {
    if (ext() != null && !ext().containsKey(EaseConstant.QUOTE_MSG_QUOTE)) {
        return false
    }
    val jsonObject: JSONObject? = try {
        val msgQuote = getStringAttribute(EaseConstant.QUOTE_MSG_QUOTE, null)
        if (msgQuote.isNullOrEmpty()) {
            getJSONObjectAttribute(EaseConstant.QUOTE_MSG_QUOTE)
        } else {
            JSONObject(msgQuote)
        }
    } catch (e: ChatException) {
        ChatLog.e(
            "isReplyMessage",
            "error message: " + e.description
        )
        null
    }
    if (jsonObject == null) {
        ChatLog.e(
            "isReplyMessage",
            "error message: jsonObject is null"
        )
        return false
    }
    jsonResult(jsonObject)
    return true
}

internal fun ChatMessage.hasThreadChat(): Boolean {
    return chatThread != null
}