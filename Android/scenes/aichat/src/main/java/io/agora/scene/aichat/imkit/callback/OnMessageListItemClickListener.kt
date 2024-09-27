package io.agora.scene.aichat.imkit.callback

import android.view.View
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatAudioStatus

/**
 * Item click listener for message list
 */
interface OnMessageListItemClickListener {
    /**
     * Click the bubble.
     * If you want handle it, return true and add you owner logic.
     * @param message
     * @return
     */
    fun onBubbleClick(message: ChatMessage?): Boolean{
        return false
    }

    /**
     * On bottom bubble click
     *
     * @param message
     * @return
     */
    fun onBottomBubbleClick(message: ChatMessage?,audioStatus: EaseChatAudioStatus): Boolean{
        return false
    }

    /**
     * Long click the bubble.
     * @param v The view of bubble
     * @param message
     */
    fun onBubbleLongClick(v: View?, message: ChatMessage?): Boolean{
        return false
    }

    /**
     * Click the resend view.
     * @param message
     * @return
     */
    fun onResendClick(message: ChatMessage?): Boolean{
        return false
    }

    /**
     * Click the user avatar.
     * @param userId
     */
    fun onUserAvatarClick(userId: String?){}

    /**
     * Long click the user avatar.
     * @param userId
     */
    fun onUserAvatarLongClick(userId: String?){}

}