package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.aichat.list.logic.api.AIAgentService
import io.agora.scene.aichat.list.logic.model.AIAgentModel
import io.agora.scene.aichat.ext.request

class AIAgentViewModel : ViewModel() {

    //公开智能体
    val publicAIAgentLiveData: MutableLiveData<List<AIAgentModel>> = MutableLiveData()

    //我创建的智能体
    val privateAIAgentLiveData: MutableLiveData<List<AIAgentModel>> = MutableLiveData()

    fun getPublicAgent() {
        request(
            block = { AIAgentService.requestPublicBot() },
            onSuccess = {
                publicAIAgentLiveData.value = it
            },
            onError = {
                publicAIAgentLiveData.value = emptyList()
            }
        )
    }

    fun getPrivateAgent() {

    }
}