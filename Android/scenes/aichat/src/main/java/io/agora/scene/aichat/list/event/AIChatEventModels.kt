package io.agora.scene.aichat.list.event

data class UnreadMessageEvent constructor(val conversationId: String, val newMessage: Boolean)