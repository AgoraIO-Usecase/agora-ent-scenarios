package io.agora.scene.aichat.service

import io.agora.scene.aichat.service.api.AIApiException

sealed class AIResultState<out T> {
    companion object {
        fun <T> onSuccess(data: T): AIResultState<T> = Success(data)
        fun <T> onLoading(loadingMessage: String): AIResultState<T> = Loading(loadingMessage)
        fun <T> onError(error: AIApiException): AIResultState<T> = Error(error)
    }

    data class Loading(val loadingMessage: String) : AIResultState<Nothing>()
    data class Success<out T>(val data: T) : AIResultState<T>()
    data class Error(val error: AIApiException) : AIResultState<Nothing>()
}