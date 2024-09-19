package io.agora.scene.aichat.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
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