package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AILogger
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.helper.EasePreferenceManager
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.aichat.imkit.supends.fetchConversationsFromServer
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AIConversationViewModel : ViewModel() {

    companion object {
        private const val TAG = "AIConversationViewModel"
    }

    //会话列表
    val chatConversationListLivedata: MutableLiveData<List<EaseConversation>> = MutableLiveData()

    fun getConversationList() {
        viewModelScope.launch {
            runCatching {
                fetchConversation()
            }.onSuccess {
                chatConversationListLivedata.postValue(it)
            }.onFailure {
                CustomToast.showError("获取会话列表失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
            }
        }
    }


    private suspend fun fetchConversation(): List<EaseConversation> =
        withContext(Dispatchers.IO) {
            val hasLoaded: Boolean = EasePreferenceManager.getInstance().isLoadedConversationsFromServer()
            if (hasLoaded) {
                AILogger.d(TAG, "loadData from server")
                ChatClient.getInstance().chatManager().allConversationsBySort
                    // Filter system message and empty conversations.
                    ?.filter {
                        it.conversationId() != EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID
                    }
                    ?.map {
                        it.parse()
                    } ?: listOf()
            } else {
                AILogger.d(TAG, "loadData from server")
                var cursor: String? = null
                do {
                    val result = ChatClient.getInstance().chatManager().fetchConversationsFromServer(50, cursor)
                    val conversations = result.data

                    cursor = result.cursor
                } while (!cursor.isNullOrEmpty())
                EasePreferenceManager.getInstance().setLoadedConversationsFromServer(true)
                ChatClient.getInstance().chatManager().allConversationsBySort
                    // Filter system message and empty conversations.
                    ?.filter {
                        it.conversationId() != EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID
                    }
                    ?.map {
                        it.parse()
                    } ?: listOf()
            }
        }
}