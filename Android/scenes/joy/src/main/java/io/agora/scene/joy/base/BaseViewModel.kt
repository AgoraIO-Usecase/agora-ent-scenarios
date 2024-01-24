package io.agora.scene.joy.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    val loadingLiveData = SingleLiveEvent<Boolean>()
    val errorLiveData = SingleLiveEvent<Throwable>()

    fun launch(
        block: suspend () -> Unit,
        error: suspend (Throwable) -> Unit,
        complete: suspend () -> Unit
    ) {
        loadingLiveData.postValue(true)
        scope.launch(Dispatchers.Main) {
            try {
                block()
            } catch (e: Exception) {
                error(e)
            } finally {
                complete()
            }
        }
    }
}