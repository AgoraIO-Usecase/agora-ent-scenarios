package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.supends.removeContact
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 智能体
 *
 * @constructor Create empty A i agent view model
 */
class AIAgentViewModel : AIBaseViewModel() {

    companion object {
        const val TAG = "AIAgentViewModel"
    }

    //公开智能体
    val publicAIAgentLiveData: MutableLiveData<List<EaseProfile>> = MutableLiveData()

    //我创建的智能体
    val privateAIAgentLiveData: MutableLiveData<List<EaseProfile>> = MutableLiveData()

    // 删除创建的智能体
    val deleteAgentLivedata: MutableLiveData<Pair<Int, Boolean>> = MutableLiveData()

    // 获取公开智能体
    fun getPublicAgent(isForce:Boolean= false) {
        loadingChange.showDialog.postValue(true)
        viewModelScope.launch {
            runCatching {
                fetchPublicAgent(isForce)
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
    fun getUserAgent(isForce:Boolean= false) {
        loadingChange.showDialog.postValue(true)
        viewModelScope.launch {
            runCatching {
                fetchUserAgent(isForce)
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
                deleteAgent(easeProfile)
            }.onSuccess {
                deleteAgentLivedata.postValue(position to it)
                loadingChange.dismissDialog.postValue(true)
            }.onFailure {
                CustomToast.showError("删除智能体 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
                deleteAgentLivedata.postValue(position to false)
                loadingChange.dismissDialog.postValue(true)
            }
        }
    }

    private suspend fun deleteAgent(easeProfile: EaseProfile) = withContext(Dispatchers.IO) {
        val result = ChatClient.getInstance().contactManager().removeContact(easeProfile.id, false)
        return@withContext result == ChatError.EM_NO_ERROR
//        val toDeleteUsername = easeProfile.id.substringAfterLast("-")
        val response =
            aiChatService.deleteChatUser(username = EaseIM.getCurrentUser().id, toDeleteUsername = easeProfile.id)
        return@withContext response.isSuccess
    }
}