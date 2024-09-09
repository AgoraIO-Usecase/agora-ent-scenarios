package io.agora.scene.aichat.list.logic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.chat.UserInfo
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.impl.CallbackImpl
import io.agora.scene.aichat.imkit.impl.ValueCallbackImpl
import io.agora.scene.aichat.list.logic.model.AIAgentModel
import io.agora.scene.aichat.list.logic.model.toAIAgentModel
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.AICreateTokenReq
import io.agora.scene.aichat.service.api.AICreateUserReq
import io.agora.scene.aichat.service.api.CreateUserType
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.base.BuildConfig
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AIAgentViewModel : AIBaseViewModel() {

    val loginChatLiveData: MutableLiveData<Boolean> = MutableLiveData()

    //公开智能体
    val publicAIAgentLiveData: MutableLiveData<List<AIAgentModel>> = MutableLiveData()

    //我创建的智能体
    val privateAIAgentLiveData: MutableLiveData<List<AIAgentModel>> = MutableLiveData()

    /**
     * Check login im
     *
     * @param chatUserName
     */
    fun checkLoginIM(chatUserName: String) {
        loadingChange.showDialog.postValue(true)
        viewModelScope.launch {
            runCatching {
                registerChatUserAndLogin(chatUserName)
            }.onSuccess {
                loginChatLiveData.postValue(true)
                loadingChange.dismissDialog.postValue(false)
            }.onFailure {
                CustomToast.showError("登录IM失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
                loginChatLiveData.postValue(false)
                loadingChange.dismissDialog.postValue(false)
            }
        }
    }

    private suspend fun registerChatUserAndLogin(chatUserName: String): Boolean = withContext(Dispatchers.IO) {
        val createUser = aiChatService.createChatUser(req = AICreateUserReq(chatUserName, CreateUserType.User))
        if (createUser.isSuccess || createUser.code == 1201) {

        } else {
            throw AIApiException(createUser.code ?: -1, createUser.message ?: "")
        }

        val createToken = aiChatService.generateChatToken(
            req = AICreateTokenReq(
                channelName = "",
                uid = chatUserName,
                appCert = BuildConfig.AGORA_APP_CERTIFICATE
            )
        )
        if (createToken.isSuccess && createToken.data?.token != null) {
            val token = createToken.data!!.token
            Log.d("token", token)
            val newToken = token.replace("\\", "")
            Log.d("token", newToken)
            AIChatCenter.mRtcToken = token
            AIChatCenter.mChatToken = token
        } else {
            throw AIApiException(createToken.code ?: -1, createToken.message ?: "")
        }
        val loginRet = suspendCoroutine<Int> { continuation ->
            EaseIM.loginWithAgoraToken(
                chatUserName, AIChatCenter.mChatToken, onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { code, error ->
                    continuation.resumeWithException(ChatException(code, error))
                })
        }
        loginRet == ChatError.EM_NO_ERROR
    }

    fun getPublicAgent() {
        viewModelScope.launch {
            runCatching {
                fetchAgentAndLoadUser()
            }.onSuccess {
                publicAIAgentLiveData.postValue(it)
            }.onFailure {
                CustomToast.showError("获取公共智能体失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }

    private suspend fun fetchAgentAndLoadUser(): List<AIAgentModel> = withContext(Dispatchers.IO) {
        val agentResult = aiChatService.fetchPublicAgent()
        val agentList = agentResult.data
        if (agentResult.isSuccess && agentList != null) {

        } else {
            throw AIApiException(agentResult.code ?: -1, agentResult.message ?: "")
        }

        val userInfoMap = suspendCoroutine<Map<String, UserInfo>> { continuation ->
            ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(
                agentList.map { it.username }.toTypedArray(), ValueCallbackImpl(onSuccess = {
                    continuation.resume(it)
                },
                    onError = { code, error ->
                        continuation.resumeWithException(ChatException(code, error))
                    })
            )
        }
        val botAgentList = mutableListOf<AIAgentModel>()
        for (i in agentList.indices) {
            val agent = agentList[i]
            val userInfo = userInfoMap[agent.username]
            val aiAgentModel = userInfo?.toAIAgentModel(agent) ?: agent.toAIAgentModel()
            botAgentList.add(aiAgentModel)
        }
        botAgentList
    }

    fun getPrivateAgent() {
        // TODO:
    }
}