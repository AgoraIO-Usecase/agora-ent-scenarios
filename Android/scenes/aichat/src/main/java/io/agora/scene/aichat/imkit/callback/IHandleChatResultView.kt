package io.agora.scene.aichat.imkit.callback

import io.agora.scene.aichat.imkit.ChatMessage

interface IHandleChatResultView  {

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
     * On request tts success
     *
     * @param message
     */
    fun onRequestTtsSuccess(message: ChatMessage?){}

    /**
     * On request tts failed
     *
     * @param message
     */
    fun onRequestTtsFailed(message: ChatMessage?){}

    /**
     * On player audio success
     *
     * @param message
     */
    fun onPlayerAudioSuccess(message: ChatMessage?){}

    /**
     * On player audio failed
     *
     * @param message
     */
    fun onPlayerAudioFailed(message: ChatMessage?){}
}