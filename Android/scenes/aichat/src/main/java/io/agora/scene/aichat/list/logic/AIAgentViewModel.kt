package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AIChatHelper
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.createAgentOrGroupSuccessMessage
import io.agora.scene.aichat.imkit.helper.EasePreferenceManager
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.fetchUsersBySuspend
import io.agora.scene.aichat.imkit.supends.removeContact
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.AICreateUserReq
import io.agora.scene.aichat.service.api.CreateUserType
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AIAgentViewModel : AIBaseViewModel() {

    companion object {
        const val TAG = "AIAgentViewModel"
    }

    //公开智能体
    val publicAIAgentLiveData: MutableLiveData<List<EaseProfile>> = MutableLiveData()

    //我创建的智能体
    val privateAIAgentLiveData: MutableLiveData<List<EaseProfile>> = MutableLiveData()

    // 创建智能体
    val createAgentLiveData: MutableLiveData<String> = MutableLiveData()

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
            AIChatHelper.getInstance().getDataModel().updatePublicAgentList(agentList.map { it.username })
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
            AILogger.d(TAG, "contact loadData from local")
            val conLocalList = ChatClient.getInstance().contactManager().contactsFromLocal
            val easeLocalList = EaseIM.getUserProvider()?.fetchUsersBySuspend(conLocalList)
            if (!easeLocalList.isNullOrEmpty()) {
                conversationList.addAll(easeLocalList)
                return@withContext conversationList
            }
        }
        AILogger.d(TAG, "contact loadData from server")
        val conServerList = ChatClient.getInstance().contactManager().allContactsFromServer
        val easeServerList = EaseIM.getUserProvider()?.fetchUsersBySuspend(conServerList)
        EasePreferenceManager.getInstance().setLoadedContactFromServer(true)
        return@withContext easeServerList ?: emptyList()
    }

    // 创建智能体
    fun createAgent(avatarUrl: String, nickname: String, sign: String, prompt: String) {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                createPrivateAgent(avatarUrl, nickname, sign, prompt)
            }.onSuccess {
                loadingChange.dismissDialog.postValue(false)
                createAgentLiveData.postValue(it)
            }.onFailure {
                loadingChange.dismissDialog.postValue(false)
                CustomToast.showError("创建智能体失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    private suspend fun createPrivateAgent(
        avatarUrl: String,
        nickname: String,
        sign: String,
        prompt: String
    ): String = withContext(Dispatchers.IO) {
//        val username = "staging-user-agent-" + EaseIM.getCurrentUser().id + "-" + getRandomString(8).lowercase(Locale.ROOT)

        val username = EaseIM.getCurrentUser().id
        val requestUser = AICreateUserReq(username, CreateUserType.Agent)

        // 创建智能体
        val createAgent = aiChatService.createChatUser(req = requestUser)
        val resultUsername = if (createAgent.isSuccess || createAgent.code == 1201) {
            createAgent.data?.username ?: throw AIApiException(-1, "Username is null")
        } else {
            throw AIApiException(createAgent.code ?: -1, createAgent.message ?: "")
        }

        // 创建智能体后自动添加好友
//        val ownerUsername = EaseIM.getCurrentUser().id
//        val addAgent = aiChatService.addChatUser(ownerUsername = ownerUsername, friendUsername = resultUserName)
//        if (!addAgent.isSuccess) {
//            throw AIApiException(addAgent.code ?: -1, addAgent.message ?: "")
//        }

        // 更新用户元数据
        val userEx = mutableMapOf<String, String>()
        userEx["nickname"] = nickname
        userEx["avatarurl"] = avatarUrl
        userEx["sign"] = sign
        userEx["ext"] = JSONObject().putOpt("prompt", prompt).toString()
        val updateUser = aiChatService.updateMetadata(username = resultUsername, fields = userEx)
        if (!updateUser.isSuccess) {
            throw AIApiException(updateUser.code ?: -1, updateUser.message ?: "")
        }
        val conversation =
            ChatClient.getInstance().chatManager().getConversation(resultUsername, ChatConversationType.Chat, true)

        ChatClient.getInstance().chatManager().saveMessage(
            conversation.createAgentOrGroupSuccessMessage(resultUsername, false, nickname)
        )
        resultUsername
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
//        val response = aiChatService.deleteChatUser(
//            username = EaseIM.getCurrentUser().id,
//            toDeleteUsername = toDeleteUsername
//        )
//        return@withContext response.isSuccess
    }
}