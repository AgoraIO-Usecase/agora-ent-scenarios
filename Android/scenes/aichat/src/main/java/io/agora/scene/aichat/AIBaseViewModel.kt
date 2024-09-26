package io.agora.scene.aichat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class AIBaseViewModel : ViewModel() {
    val loadingChange: UiLoadingChange by lazy { UiLoadingChange() }

    inner class UiLoadingChange {
        //显示加载框
        val showDialog by lazy { MutableLiveData<Boolean>() }

        //隐藏
        val dismissDialog by lazy { MutableLiveData<Boolean>() }
    }
}