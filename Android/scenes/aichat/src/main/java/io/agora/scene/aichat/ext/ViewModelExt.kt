package io.agora.scene.aichat.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import io.agora.scene.aichat.service.AIExceptionHandle
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.AIBaseResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class AIBaseViewModel : ViewModel() {
    val loadingChange: UiLoadingChange by lazy { UiLoadingChange() }

    inner class UiLoadingChange {
        //显示加载框
        val showDialog by lazy { UnPeekLiveData<Boolean>() }

        //隐藏
        val dismissDialog by lazy { UnPeekLiveData<Boolean>() }
    }
}

fun <T> AIBaseViewModel.request(
    block: suspend () -> AIBaseResponse<T>,
    isShowDialog: Boolean = false,
    onSuccess: (T?) -> Unit,
    onError: (AIApiException) -> Unit = {},
): Job {
    return viewModelScope.launch {
        runCatching {
            if (isShowDialog) loadingChange.showDialog.postValue(true)
            block()
        }.onSuccess { response ->
            //网络请求成功 关闭弹窗
            if (isShowDialog) loadingChange.dismissDialog.postValue(false)
            runCatching {
                //校验请求结果码是否正确，不正确会抛出异常走下面的onFailure
                if (response.isSuccess) {
                    onSuccess(response.data)
                } else {
                    onError(AIApiException(response.code ?: -100, response.message))
                }
            }.onFailure { exception ->
                //失败回调
                onError(AIApiException(response.code ?: -100, exception.localizedMessage))
            }
        }.onFailure { exception ->
            //网络请求异常 关闭弹窗
            if (isShowDialog) loadingChange.dismissDialog.postValue(false)
            //失败回调
            onError(AIExceptionHandle.handleException(exception))
        }
    }
}