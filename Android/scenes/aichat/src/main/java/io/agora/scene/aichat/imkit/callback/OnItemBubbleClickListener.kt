package io.agora.scene.aichat.imkit.callback

import io.agora.scene.aichat.imkit.ChatMessage

interface OnItemBubbleClickListener {
    fun onBubbleClick(message: ChatMessage?)
}