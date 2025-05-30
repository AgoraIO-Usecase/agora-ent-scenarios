package io.agora.scene.aichat.imkit.extensions

import android.util.Log
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseIM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch

fun <T> Flow<T>.catchChatException(action: suspend FlowCollector<T>.(ChatException) -> Unit): Flow<T> {
    return this.catch { e ->
        if (EaseIM.DEBUG) {
            Log.e("EaseIM", "catchChatException: ${e.message}")
        }
        if (e is ChatException) {
            action.invoke(this, e)
        } else {
            Log.e("catchChatException", "catchChatException: ${e.message}")
        }
    }
}
suspend fun <T> SharedFlow<T>.collectWithCheckErrorCode(checked: (T) -> Unit): Nothing {
    collect{
        if (it is Int && it == ChatError.EM_NO_ERROR) {
            checked.invoke(it)
        } else {
            if (EaseIM.DEBUG) {
                Log.d("flow", "collectWithCheckErrorCode: execute to no check scope")
            }
        }
    }
}
