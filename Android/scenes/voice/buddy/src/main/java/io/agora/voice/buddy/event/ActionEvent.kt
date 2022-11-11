package io.agora.voice.buddy.event

data class ActionEvent(
    val invoker: String? = "",
    val command: String? = "",
    val action: String? = "",
    val receiver: String? = "",
    val params: MutableMap<String, Any?>? = null,
    val ext: MutableMap<String, Any?>? = null
)
