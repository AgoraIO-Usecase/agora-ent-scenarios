package io.agora.scene.aichat.service.api

data class AIApiException(var errCode: Int, var msg: String?) : RuntimeException(msg)