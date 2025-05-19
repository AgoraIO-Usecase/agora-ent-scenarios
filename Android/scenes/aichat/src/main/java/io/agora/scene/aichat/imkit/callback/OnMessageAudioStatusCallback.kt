package io.agora.scene.aichat.imkit.callback

import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatAudioStatus

/**
 * On message audio status callback
 *
 * @constructor Create empty On message audio status listener
 */
interface OnMessageAudioStatusCallback {

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