package io.agora.scene.aichat.list.logic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.ext.getRandomString
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseProfile
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
import org.json.JSONObject
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AIUserViewModel : AIBaseViewModel() {

    val loginChatLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val createAgentLiveData: MutableLiveData<String> = MutableLiveData()

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

    fun createAgent(avatarUrl: String, nickname: String, sign: String, prompt: String) {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                createAgentAndAgent(avatarUrl, nickname, sign, prompt)
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

    private suspend fun createAgentAndAgent(
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
        val resultUserName = if (createAgent.isSuccess || createAgent.code == 1201) {
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
        val updateUser = aiChatService.updateMetadata(username = resultUserName, fields = userEx)
        if (!updateUser.isSuccess) {
            throw AIApiException(updateUser.code ?: -1, updateUser.message ?: "")
        }
        resultUserName
    }
}