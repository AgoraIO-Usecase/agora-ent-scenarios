package io.agora.imkitmanager.service.callback

import io.agora.chat.ChatMessage
import io.agora.imkitmanager.model.AUIChatEntity
import io.agora.imkitmanager.model.AgoraChatMessage

interface AUIChatMsgCallback {
    fun onResult(error: Exception?, message: AgoraChatMessage?) {}
    fun onEntityResult(error: Exception?, message: AUIChatEntity?) {}
    fun onOriginalResult(error: Exception?, message: ChatMessage?) {}
}