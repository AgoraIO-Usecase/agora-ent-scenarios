package io.agora.scene.aichat.imkit.callback

import android.view.View
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.ChatThread

interface OnMessageChatThreadClickListener {
    fun onThreadViewItemClick(view: View, thread: ChatThread?, topicMsg: ChatMessage)
}