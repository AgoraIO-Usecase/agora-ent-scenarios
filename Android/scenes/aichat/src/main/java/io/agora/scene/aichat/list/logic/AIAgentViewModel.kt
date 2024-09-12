package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.helper.EasePreferenceManager
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.fetchUsersBySuspend
import io.agora.scene.aichat.imkit.supends.deleteConversationFromServer
import io.agora.scene.aichat.imkit.supends.removeContact
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    fun getPublicAgent() {
        viewModelScope.launch {
            runCatching {
                fetchPublicAgent()
            }.onSuccess {
                publicAIAgentLiveData.postValue(it)
            }.onFailure {
                CustomToast.showError("获取公共智能体失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    private suspend fun fetchPublicAgent(): List<EaseProfile> = withContext(Dispatchers.IO) {
        val agentResult = aiChatService.fetchPublicAgent()
        val agentList = agentResult.data?.sortedBy { it.index }
        if (agentResult.isSuccess && !agentList.isNullOrEmpty()) {
        } else {
            throw AIApiException(agentResult.code ?: -1, agentResult.message ?: "")
        }
        val easeProfileMap: Map<String, EaseProfile> = EaseIM.getUserProvider()
            ?.fetchUsersBySuspend(agentList.map { it.username })
            ?.associate { it.id to it } ?: throw ChatException(-1, "get user info error")

        val easeProfileList = mutableListOf<EaseProfile>()
        for (i in agentList.indices) {
            val agent = agentList[i]
            val userInfo = easeProfileMap[agent.username]
            val aiAgentModel = userInfo ?: EaseProfile(agent.username)
            easeProfileList.add(aiAgentModel)
        }
        easeProfileList
    }

    // 获取创建的智能体
    fun getPrivateAgent() {
        viewModelScope.launch {
            runCatching {
                fetchPrivateAgent()
            }.onSuccess {
                privateAIAgentLiveData.postValue(it)
            }.onFailure {
                CustomToast.showError("获取公共智能体失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    private suspend fun fetchPrivateAgent(): List<EaseProfile> = withContext(Dispatchers.IO) {
        val conversationList = mutableListOf<EaseProfile>()
        val hasLoaded: Boolean = EasePreferenceManager.getInstance().isLoadedContactFromServer()

        if (hasLoaded) {
            AILogger.d(TAG, "contact loadData from server")
            val conList = ChatClient.getInstance().contactManager().allContactsFromServer
            val easeProfileList = EaseIM.getUserProvider()?.fetchUsersBySuspend(conList)
            easeProfileList?.let {
                conversationList.addAll(easeProfileList)
            }

        } else {
            AILogger.d(TAG, "contact loadData from server")
            val conList = ChatClient.getInstance().contactManager().contactsFromLocal
            val easeProfileList = EaseIM.getUserProvider()?.fetchUsersBySuspend(conList)
            easeProfileList?.let {
                conversationList.addAll(easeProfileList)
            }
        }
        conversationList
    }

    // 删除创建的智能体
    fun deleteAgent(position: Int, easeProfile: EaseProfile) {
        viewModelScope.launch {
            runCatching {
                deleteAgent(easeProfile)
            }.onSuccess {
                deleteAgentLivedata.postValue(position to it)
            }.onFailure {
                CustomToast.showError("删除智能体 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
                deleteAgentLivedata.postValue(position to false)
            }
        }
    }

    private suspend fun deleteAgent(easeProfile: EaseProfile) = withContext(Dispatchers.IO) {
        val result = ChatClient.getInstance().contactManager().removeContact(easeProfile.id, false)
        return@withContext result == ChatError.EM_NO_ERROR
//        val toDeleteUsername = easeProfile.id.substringAfterLast("-")
//        val response = aiChatService.deleteChatUser(
//            username = EaseIM.getCurrentUser().id,
//            toDeleteUsername = toDeleteUsername
//        )
//        return@withContext response.isSuccess
    }
}