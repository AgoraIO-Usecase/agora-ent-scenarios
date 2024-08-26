package io.agora.scene.aichat.list.event

data class UnreadMessageEvent constructor(val conversationId: Long, val newMessage: Boolean)