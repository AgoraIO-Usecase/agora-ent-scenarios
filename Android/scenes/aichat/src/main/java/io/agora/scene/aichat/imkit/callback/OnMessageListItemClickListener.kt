package io.agora.scene.aichat.imkit.callback

import android.view.View
import io.agora.scene.aichat.imkit.ChatMessage

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
    fun onBubbleClick(message: ChatMessage?): Boolean

    /**
     * Long click the bubble.
     * @param v The view of bubble
     * @param message
     */
    fun onBubbleLongClick(v: View?, message: ChatMessage?): Boolean

    /**
     * Click the resend view.
     * @param message
     * @return
     */
    fun onResendClick(message: ChatMessage?): Boolean

    /**
     * Click the user avatar.
     * @param userId
     */
    fun onUserAvatarClick(userId: String?)

    /**
     * Long click the user avatar.
     * @param userId
     */
    fun onUserAvatarLongClick(userId: String?)

}