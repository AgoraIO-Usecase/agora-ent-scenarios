package io.agora.scene.aichat.imkit.callback

import io.agora.scene.aichat.imkit.ChatMessage
import org.json.JSONObject

interface IHandleChatResultView  {

    /**
     * Callback when ack conversation read successfully.
     */
    fun ackConversationReadSuccess(){}

    /**
     * Callback when ack conversation read failed.
     */
    fun ackConversationReadFail(code: Int,  message: String?){}

    /**
     * Callback when send group read ack successfully.
     */
    fun ackGroupMessageReadSuccess(){}

    /**
     * Callback when send group read ack failed.
     * @param code
     * @param message
     */
    fun ackGroupMessageReadFail(code: Int,  message: String?){}

    /**
     * Callback when send message read ack successfully.
     */
    fun ackMessageReadSuccess(){}

    /**
     * Callback when send message read ack failed.
     * @param code
     * @param message
     */
    fun ackMessageReadFail(code: Int,  message: String?){}

    /**
     * Failed to generate video cover
     * @param message
     */
    fun createThumbFileFail(message: String?){}

    /**
     * Before sending a message, add message attributes, such as setting ext, etc.
     * @param message
     */
    fun addMsgAttrBeforeSend(message: ChatMessage?){}

    /**
     * Has a error before sending a message.
     * @param code Error code.
     * @param message Error message.
     */
    fun onErrorBeforeSending(code: Int, message: String?){}

    /**
     * Delete message successfully.
     * @param message
     */
    fun deleteMessageSuccess(message: ChatMessage?){}

    /**
     * Failed to delete message.
     */
    fun deleteMessageFail(message: ChatMessage?, code: Int, errorMsg: String?){}

    /**
     * Delete message list successfully.
     */
    fun deleteMessageListSuccess(){}

    /**
     * Failed to delete message list.
     */
    fun deleteMessageListFail(code: Int, errorMsg: String?){}

    /**
     * Complete withdrawal message
     * @param originalMessage The message was unsent
     * @param notification  The notification message
     */
    fun recallMessageFinish(originalMessage: ChatMessage?, notification: ChatMessage?){}

    /**
     * Failed to withdraw the message
     * @param code
     * @param message
     */
    fun recallMessageFail(code: Int, message: String?){}

    /**
     * message send success
     * @param message
     */
    fun onSendMessageSuccess(message: ChatMessage?){}

    /**
     * message send fail
     * @param message
     * @param code
     * @param error
     */
    fun onSendMessageError(message: ChatMessage?, code: Int, error: String?){}

    /**
     * message in sending progress
     * @param message
     * @param progress
     */
    fun onSendMessageInProgress(message: ChatMessage?, progress: Int){}

    /**
     * Complete the message sending action
     * @param message
     */
    fun sendMessageFinish(message: ChatMessage?){}

    /**
     * add reaction success
     *
     * @param message
     */
    fun addReactionMessageSuccess(message: ChatMessage?){}

    /**
     * add reaction fail
     *
     * @param message
     * @param code
     * @param error
     */
    fun addReactionMessageFail(message: ChatMessage?, code: Int, error: String?){}

    /**
     * remove reaction success
     *
     * @param message
     */
    fun removeReactionMessageSuccess(message: ChatMessage?){}

    /**
     * remove reaction fail
     *
     * @param message
     * @param code
     * @param error
     */
    fun removeReactionMessageFail(message: ChatMessage?, code: Int, error: String?){}

    /**
     * modify message success
     * @param messageModified
     */
    fun onModifyMessageSuccess(messageModified: ChatMessage?){}

    /**
     * modify message failure
     * @param messageId
     * @param code
     * @param error
     */
    fun onModifyMessageFailure(messageId: String?, code: Int, error: String?){}

    /**
     * create reply message ext success.
     * @param extObject reply ext object.
     */
    fun createReplyMessageExtSuccess(extObject: JSONObject?){}

    /**
     * create reply message ext fail.
     * @param code  error code.
     * @param error error message.
     */
    fun createReplyMessageExtFail(code: Int, error: String?){}

    /**
     * report message success.
     * @param msgId msgId.
     */
    fun onReportMessageSuccess(msgId:String){}

    /**
     * report message fail.
     * @param msgId msgId.
     * @param code  error code.
     * @param error error message.
     */
    fun onReportMessageFail(msgId: String, code: Int, error: String){}

    /**
     * hide translation message.
     */
    fun onHideTranslationMessage(message: ChatMessage?){}

    /**
     * translation message success.
     * @param message
     */
    fun onTranslationMessageSuccess(message: ChatMessage?){}

    /**
     * translation message fail.
     * @param code  error code.
     * @param error error message.
     */
    fun onTranslationMessageFail(code: Int, error: String){}

    /**
     * Message forwarded successfully.
     * @param message The forwarded message.
     */
    fun onForwardMessageSuccess(message: ChatMessage?){}

    /**
     * Message forwarding failed.
     * @param message The message to be forwarded.
     * @param code  error code.
     * @param error error message.
     */
    fun onForwardMessageFail(message: ChatMessage?, code: Int, error: String?){}

    /**
     * Sent a combine message successfully.
     * @param message The combine message.
     */
    fun onSendCombineMessageSuccess(message: ChatMessage?){}

    /**
     * Failed to send a combine message.
     * @param message The message to be sent.
     * @param code  error code.
     * @param error error message.
     */
    fun onSendCombineMessageFail(message: ChatMessage?, code: Int, error: String?){}

    /**
     * Pin message success
     */
    fun onPinMessageSuccess(message: ChatMessage?){}

    /**
     * Pin message fail
     * @param code  error code.
     * @param error error message.
     */
    fun onPinMessageFail(code: Int, error: String?){}

    /**
     * unPin message success
     */
    fun onUnPinMessageSuccess(message: ChatMessage?) {}

    /**
     * unPin message fail
     * @param code  error code.
     * @param error error message.
     */
    fun onUnPinMessageFail(code: Int, error: String?){}

    /**
     * Fetch pin message from server success
     * @param value pin message list
     */
    fun onFetchPinMessageFromServerSuccess(value:MutableList<ChatMessage>?){}

    /**
     * Fetch pin message from server fail
     * @param code  error code.
     * @param error error message.
     */
    fun onFetchPinMessageFromServerFail(code: Int, error: String?){}

}