package io.agora.scene.aichat.create.event

data class UnreadMessageEvent constructor(val conversationId: Long, val newMessage: Boolean)