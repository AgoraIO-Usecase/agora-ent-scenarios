package io.agora.scene.aichat.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.base.api.base.BaseResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun <T> ViewModel.request(
    block: suspend () -> BaseResponse<T>,
    onSuccess: (T) -> Unit,
    onError: (Exception) -> Unit = {},
): Job {
    return viewModelScope.launch {
        runCatching {
            //请求体
            block()
        }.onSuccess { response ->
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                if (response.isSuccess) {
                    response.data?.let {
                        onSuccess(it)
                    } ?: run {
                        onError(Exception("Response data is null"))
                    }
                } else {
                    onError(Exception("Error: ${response.message} (Code: ${response.code})"))
                }
            }.onFailure { exception ->
                //打印错误栈信息
                exception.printStackTrace()
                //失败回调
                onError(Exception("Request failed due to: ${exception.localizedMessage}"))
            }
        }.onFailure { exception ->
            //打印错误栈信息
            exception.printStackTrace()
            //失败回调
            onError(Exception("Request failed due to: ${exception.localizedMessage}"))
        }
    }
}