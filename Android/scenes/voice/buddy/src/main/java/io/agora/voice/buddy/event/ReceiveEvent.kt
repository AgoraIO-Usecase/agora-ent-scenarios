package io.agora.voice.buddy.event

data class ReceiveEvent(
    val action: String? = null,
    val invoker: String? = null,
    val receiver: String? = null,
    val data: MutableMap<String, Any?>? = null,
    val ext: MutableMap<String, Any?>? = null
)
