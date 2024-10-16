package io.agora.scene.aichat.list.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.AIChatProtocolService
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.launch

/**
 * 智能体
 *
 * @constructor Create empty A i agent view model
 */
class AIAgentViewModel : AIBaseViewModel() {

    private val chatProtocolService by lazy { AIChatProtocolService.instance() }

    companion object {
        const val TAG = "AIAgentViewModel"
    }

    //公开智能体
    val publicAIAgentLiveData: MutableLiveData<List<EaseProfile>> = MutableLiveData()

    //我创建的智能体
    val privateAIAgentLiveData: MutableLiveData<List<EaseProfile>> = MutableLiveData()

    // 删除创建的智能体
    private val _deleteAgentLivedata: MutableLiveData<Pair<Int, Boolean>> = MutableLiveData()
    val deleteAgentLivedata: LiveData<Pair<Int, Boolean>> get() = _deleteAgentLivedata

    // 获取公开智能体
    fun getPublicAgent(isForce: Boolean = false) {
        loadingChange.showDialog.postValue(true)
        viewModelScope.launch {
            runCatching {
                chatProtocolService.fetchPublicAgent(isForce)
            }.onSuccess {
                publicAIAgentLiveData.postValue(it)
                loadingChange.dismissDialog.postValue(false)
            }.onFailure {
                loadingChange.dismissDialog.postValue(false)
                CustomToast.showError("获取公共智能体失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    // 获取创建的智能体
    fun getUserAgent(isForce: Boolean = false) {
        loadingChange.showDialog.postValue(true)
        viewModelScope.launch {
            runCatching {
                chatProtocolService.fetchUserAgent(isForce)
            }.onSuccess {
                privateAIAgentLiveData.postValue(it)
                loadingChange.dismissDialog.postValue(false)
            }.onFailure {
                loadingChange.dismissDialog.postValue(false)
                CustomToast.showError("获取公共智能体失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    // 删除创建的智能体
    fun deleteAgent(position: Int, easeProfile: EaseProfile) {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                chatProtocolService.deleteAgent(easeProfile.id)
            }.onSuccess {
                _deleteAgentLivedata.postValue(position to it)
                loadingChange.dismissDialog.postValue(true)
            }.onFailure {
                CustomToast.showError("删除智能体 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
                _deleteAgentLivedata.postValue(position to false)
                loadingChange.dismissDialog.postValue(true)
            }
        }
    }
}