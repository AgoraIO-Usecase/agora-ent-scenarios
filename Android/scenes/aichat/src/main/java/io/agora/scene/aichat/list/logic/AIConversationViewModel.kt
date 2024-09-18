package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.createAgentOrGroupSuccessMessage
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.helper.EasePreferenceManager
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.fetchUsersBySuspend
import io.agora.scene.aichat.imkit.supends.deleteConversationFromServer
import io.agora.scene.aichat.imkit.supends.fetchConversationsFromServer
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.AICreateUserReq
import io.agora.scene.aichat.service.api.CreateUserType
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AIConversationViewModel : AIBaseViewModel() {

    companion object {
        private const val TAG = "AIConversationViewModel"
    }

    //会话列表
    val chatConversationListLivedata: MutableLiveData<List<EaseConversation>> = MutableLiveData()

    // 删除会话列表
    val deleteConversationLivedata: MutableLiveData<Pair<Int, Boolean>> = MutableLiveData()

    // 获取会话列表
    fun getConversationList() {
        viewModelScope.launch {
            runCatching {
                fetchConversation()
            }.onSuccess {
                chatConversationListLivedata.postValue(it)
            }.onFailure {
                //CustomToast.showError("获取会话列表失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }


    private suspend fun fetchConversation(): List<EaseConversation> = withContext(Dispatchers.IO) {
        val conversationList = mutableListOf<EaseConversation>()
        val hasLoaded: Boolean = EasePreferenceManager.getInstance().isLoadedConversationsFromServer()
        if (hasLoaded) {
            AILogger.d(TAG, "conversation loadData from server")
            val conList = ChatClient.getInstance().chatManager().allConversationsBySort
                // Filter system message and empty conversations.
                ?.filter {
                    it.conversationId() != EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID
                }?.map {
                    it.parse()
                }
            if (conList != null) conversationList.addAll(conList)
        } else {
            AILogger.d(TAG, "conversation loadData from server")
            var cursor: String? = null
            do {
                val result = ChatClient.getInstance().chatManager().fetchConversationsFromServer(50, cursor)
                val conversations = result.data
                cursor = result.cursor
            } while (!cursor.isNullOrEmpty())
            EasePreferenceManager.getInstance().setLoadedConversationsFromServer(true)
            val conList = ChatClient.getInstance().chatManager().allConversationsBySort
                // Filter system message and empty conversations.
                ?.filter {
                    it.conversationId() != EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID
                }?.map {
                    it.parse()
                }
            if (conList != null) conversationList.addAll(conList)
        }

        // 获取会话列表用户信息
        EaseIM.getUserProvider()?.fetchUsersBySuspend(conversationList.map {
            if (it.lastMessage?.isSend() == true) ChatClient.getInstance().currentUser
            else it.lastMessage?.from ?: ""
        })?.associate { it.id to it } ?: throw ChatException(-1, "get user info error")
        conversationList
    }

    // 删除会话
    fun deleteConversation(position: Int, conversation: EaseConversation) {
        viewModelScope.launch {
            runCatching {
                deleteConversation(conversation, false)
            }.onSuccess {
                val isSuccess = it == ChatError.EM_NO_ERROR
                deleteConversationLivedata.postValue(position to isSuccess)
            }.onFailure {
                CustomToast.showError("删除会话失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
                deleteConversationLivedata.postValue(position to false)
            }
        }
    }

    private suspend fun deleteConversation(conversation: EaseConversation, isDeleteLocalOnly: Boolean = true) =
        withContext(Dispatchers.IO) {
            conversation.run {
                if (isDeleteLocalOnly) {
                    val result = ChatClient.getInstance().chatManager().deleteConversation(conversationId, true)
                    if (result) {
                        ChatError.EM_NO_ERROR
                    } else {
                        ChatError.INVALID_CONVERSATION
                    }
                } else {
                    ChatClient.getInstance().chatManager()
                        .deleteConversationFromServer(conversationId, conversationType, true)
                }
            }
        }
}