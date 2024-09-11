package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversation
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatException
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.isSend
import io.agora.scene.aichat.imkit.helper.EasePreferenceManager
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.fetchUsersBySuspend
import io.agora.scene.aichat.imkit.supends.deleteConversationFromServer
import io.agora.scene.aichat.imkit.supends.fetchConversationsFromServer
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIConversationViewModel : ViewModel() {

    companion object {
        private const val TAG = "AIConversationViewModel"
    }

    //会话列表
    val chatConversationListLivedata: MutableLiveData<List<ChatConversation>> = MutableLiveData()

    // 删除会话列表
    val deleteConversationLivedata: MutableLiveData<Pair<Int, Boolean>> = MutableLiveData()

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


    private suspend fun fetchConversation(): List<ChatConversation> = withContext(Dispatchers.IO) {
        val conversationList = mutableListOf<ChatConversation>()
        val hasLoaded: Boolean = EasePreferenceManager.getInstance().isLoadedConversationsFromServer()
        if (hasLoaded) {
            AILogger.d(TAG, "loadData from server")
            val conList = ChatClient.getInstance().chatManager().allConversationsBySort
                // Filter system message and empty conversations.
                ?.filter {
                    it.conversationId() != EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID
                }
            if (conList != null) conversationList.addAll(conList)
        } else {
            AILogger.d(TAG, "loadData from server")
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
                }
            if (conList != null) conversationList.addAll(conList)
        }

        // 获取会话列表用户信息
        EaseIM.getUserProvider()?.fetchUsersBySuspend(conversationList.map {
            if (it.lastMessage.isSend()) ChatClient.getInstance().currentUser
            else it.lastMessage.from
        })?.associate { it.id to it } ?: throw ChatException(-1, "get user info error")

        conversationList
    }

    fun deleteConversation(position: Int, conversation: ChatConversation) {
        viewModelScope.launch {
            runCatching {
                deleteConversation(conversation, true)
            }.onSuccess {
                deleteConversationLivedata.postValue(position to true)
            }.onFailure {
                CustomToast.showError("删除会话失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
                deleteConversationLivedata.postValue(position to false)
            }
        }
    }

    private suspend fun deleteConversation(conversation: ChatConversation, isDeleteLocalOnly: Boolean = true) =
        withContext(Dispatchers.IO) {
            conversation.run {
                if (isDeleteLocalOnly) {
                    val result = ChatClient.getInstance().chatManager().deleteConversation(conversationId(), true)
                    if (result) {
                        ChatError.EM_NO_ERROR
                    } else {
                        ChatError.INVALID_CONVERSATION
                    }
                } else {
                    ChatClient.getInstance().chatManager().deleteConversationFromServer(conversationId(), type, true)
                }
            }
        }
}