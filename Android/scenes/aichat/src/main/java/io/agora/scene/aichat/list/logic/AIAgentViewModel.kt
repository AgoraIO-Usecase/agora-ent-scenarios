package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.list.logic.model.AIAgentModel
import io.agora.scene.aichat.ext.request
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.AICreateTokenReq
import io.agora.scene.aichat.service.api.AICreateUserReq
import io.agora.scene.aichat.service.api.AICreateUserType
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.launch

class AIAgentViewModel : AIBaseViewModel() {

    val loginChatLiveData: MutableLiveData<Boolean> = MutableLiveData()

    //公开智能体
    val publicAIAgentLiveData: MutableLiveData<List<AIAgentModel>> = MutableLiveData()

    //我创建的智能体
    val privateAIAgentLiveData: MutableLiveData<List<AIAgentModel>> = MutableLiveData()

    fun registerChatUserAndLogin() {
        loadingChange.showDialog.postValue(true)
        request(
            block = {
                val createUser = aiChatService.createChatUser(
                    req = AICreateUserReq(AIChatCenter.mChatUsername, AICreateUserType.User.ordinal)
                )
                if (createUser.isSuccess || createUser.code == 1201) {
                    aiChatService.generateChatToken(req = AICreateTokenReq("", AIChatCenter.mChatUsername))
                } else {
                    throw AIApiException(createUser.code ?: -1, createUser.message ?: "")
                }
            },
            isShowDialog = false,
            onSuccess = {
                it?.token?.let { token ->
                    AIChatCenter.mRtcToken = token
                    AIChatCenter.mChatToken = token
                    innerLoginChat(AIChatCenter.mChatUsername, token)
                }?:run {
                    loadingChange.dismissDialog.postValue(false)
                    CustomToast.showError("获取 token 失败 token is null")
                }
            },
            onError = { apiException ->
                loadingChange.dismissDialog.postValue(false)
                CustomToast.showError("创建用户失败 ${apiException.errCode} ${apiException.message ?: ""}")
            }
        )
    }

    private fun innerLoginChat(userName: String, token: String) {
        viewModelScope.launch {
            EaseIM.loginWithToken(userName, token, onSuccess = {
                val currentUser = EaseIM.getCurrentUser()
                if (currentUser == null) {
                    CustomToast.show("IM Chat登录失败: chatUser is null")
                } else {
                    loadAllConversationsAndGroups()
                    loginChatLiveData.postValue(true)
                }
                loadingChange.dismissDialog.postValue(false)
            }, onError = { code, error ->
                CustomToast.show("IM Chat登录失败: code:$code error:$error")
                loginChatLiveData.postValue(false)
                loadingChange.dismissDialog.postValue(false)
            })
        }
    }

    /**
     * 从本地数据库加载所有的对话及群组
     */
    private fun loadAllConversationsAndGroups() {
        // 从本地数据库加载所有的对话及群组
        ChatClient.getInstance().chatManager().loadAllConversations()
        ChatClient.getInstance().groupManager().loadAllGroups()
    }


    fun getPublicAgent() {
        request(
            block = {
                aiChatService.fetchPublicAgent()
            },
            onSuccess = {
                publicAIAgentLiveData.value = emptyList()
            },
            onError = {
                publicAIAgentLiveData.value = emptyList()
            }
        )
    }

    fun getPrivateAgent() {
//        request(
//            block = { AIChatService.requestPrivateBot() },
//            onSuccess = {
//                privateAIAgentLiveData.value = it
//            },
//            onError = {
//                privateAIAgentLiveData.value = emptyList()
//            }
//        )
    }

    fun reset(){
        EaseIM.logout(true,{})
        EaseIM.releaseGlobalListener()
    }
}