package io.agora.scene.aichat.list.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.scene.aichat.imkit.ChatConversation

class AIConversationViewModel : ViewModel() {

    //会话列表
    val chatConversationListLivedata: MutableLiveData<List<ChatConversation>> = MutableLiveData()

    fun getConversationList() {
//        request(
//            block = {
//                // TODO:
//            },
//            onSuccess = {
//                chatConversationListLivedata.value = it
//            },
//            onError = {
//                chatConversationListLivedata.value = emptyList()
//            }
//        )
    }
}