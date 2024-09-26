package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.AIChatProtocolService
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.launch

class AIConversationViewModel : AIBaseViewModel() {

    companion object {
        private const val TAG = "AIConversationViewModel"
    }

    private val chatProtocolService by lazy { AIChatProtocolService.instance() }

    //会话列表
    val chatConversationListLivedata: MutableLiveData<List<EaseConversation>> = MutableLiveData()

    // 删除会话列表
    val deleteConversationLivedata: MutableLiveData<Pair<Int, Boolean>> = MutableLiveData()

    // 获取会话列表
    fun getConversationList(isForce: Boolean = false) {
        loadingChange.showDialog.postValue(true)
        viewModelScope.launch {
            runCatching {
                chatProtocolService.fetchConversation(isForce)
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

    // 删除会话
    fun deleteConversation(position: Int, conversation: EaseConversation) {
        viewModelScope.launch {
            runCatching {
                chatProtocolService.deleteConversation(conversation.conversationId)
            }.onSuccess {
                deleteConversationLivedata.postValue(position to it)
            }.onFailure {
                CustomToast.showError("删除会话失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
                deleteConversationLivedata.postValue(position to false)
            }
        }
    }
}