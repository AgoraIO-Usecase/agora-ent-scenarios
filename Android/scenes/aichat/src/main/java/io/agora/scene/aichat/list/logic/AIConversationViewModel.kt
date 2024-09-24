package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.helper.EasePreferenceManager
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.aichat.imkit.supends.deleteConversationFromServer
import io.agora.scene.aichat.imkit.supends.fetchConversationsFromServer
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIConversationViewModel : AIBaseViewModel() {

    companion object {
        private const val TAG = "AIConversationViewModel"
    }

    //会话列表
    val chatConversationListLivedata: MutableLiveData<List<EaseConversation>> = MutableLiveData()

    // 删除会话列表
    val deleteConversationLivedata: MutableLiveData<Pair<Int, Boolean>> = MutableLiveData()

    // 获取会话列表
    fun getConversationList(isForce: Boolean = false) {
        loadingChange.showDialog.postValue(true)
        viewModelScope.launch {
            runCatching {
                fetchConversation(isForce)
            }.onSuccess {
                chatConversationListLivedata.postValue(it)
                loadingChange.dismissDialog.postValue(false)
            }.onFailure {
                //打印错误栈信息
                it.printStackTrace()
                loadingChange.dismissDialog.postValue(false)
            }
        }
    }


    private suspend fun fetchConversation(isForce: Boolean = false): List<EaseConversation> =
        withContext(Dispatchers.IO) {
            val conversationList = mutableListOf<EaseConversation>()
            val hasLoaded: Boolean = EasePreferenceManager.getInstance().isLoadedConversationsFromServer()
            if (hasLoaded && !isForce) {
                AILogger.d(TAG, "conversation loadData from local")
                val conList = ChatClient.getInstance()
                    .chatManager().allConversationsBySort?.filter { it.conversationId() != EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID }
                    ?.map { it.parse() }
                if (conList != null) conversationList.addAll(conList)
            } else {
                val allAgentList = async { fetchForceAllAgent() }.await()
                AILogger.d(TAG, "conversation loadData from server")
                var cursor: String? = null
                do {
                    val result = ChatClient.getInstance().chatManager().fetchConversationsFromServer(50, cursor)
                    val conversations = result.data
                    cursor = result.cursor
                } while (!cursor.isNullOrEmpty())
                val conList = ChatClient.getInstance()
                    .chatManager().allConversationsBySort?.filter { it.conversationId() != EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID }
                    ?.map { it.parse() }
                if (conList != null) conversationList.addAll(conList)
            }
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
                    ChatClient.getInstance().chatManager().deleteConversation(conversationId, true)
                    ChatClient.getInstance().chatManager()
                        .deleteConversationFromServer(conversationId, conversationType, true)
                }
            }
        }
}