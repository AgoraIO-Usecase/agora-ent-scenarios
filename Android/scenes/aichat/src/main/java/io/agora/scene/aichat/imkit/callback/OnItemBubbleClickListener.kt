package io.agora.scene.aichat.imkit.callback

import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.widget.chatrow.EaseChatAudioStatus

interface OnItemBubbleClickListener {
    fun onBubbleClick(message: ChatMessage?)

    fun onBottomBubbleClick(message: ChatMessage?, audioStatus: EaseChatAudioStatus)
}