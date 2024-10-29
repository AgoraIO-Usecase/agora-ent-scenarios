package io.agora.scene.aichat.create.logic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.R
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.AIChatProtocolService
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ContactItem constructor(
    val userId: String,
    val name: String,
    val avatar: String? = null,
    val isPublic: Boolean = false,
    var isCheck: Boolean = false
)

fun ContactItem.toProfile(): EaseProfile {
    return EaseProfile(userId, name, avatar)
}

fun EaseProfile.toContactItem(): ContactItem {
    return ContactItem(this.id, this.name ?: "", this.avatar ?: "", id.contains("common-agent"))
}

class AiChatGroupCreateViewModel : AIBaseViewModel() {

    companion object {
        const val MAX_SELECT_COUNT = 5
    }

    private val chatProtocolService by lazy { AIChatProtocolService.instance() }

    private val selfItem =
        ContactItem(EaseIM.getCurrentUser().id, EaseIM.getCurrentUser().name ?: "", EaseIM.getCurrentUser().avatar)
    private val placeHolder = ContactItem("", "placeholder")

    private val _selectDatas by lazy { MutableStateFlow(listOf(selfItem, placeHolder)) }
    val selectDatas: MutableStateFlow<List<ContactItem>> = _selectDatas

    private val _contacts by lazy { MutableStateFlow(emptyList<ContactItem>()) }
    val contacts: MutableStateFlow<List<ContactItem>> = _contacts

    // 创建群聊
    val _createGroupLiveData: MutableLiveData<String> = MutableLiveData<String>()

    val createGroupLiveData: LiveData<String> = _createGroupLiveData

    init {
        viewModelScope.launch {
            runCatching {
                fetchAllContacts()
            }.onSuccess {
                contacts.value = it
            }.onFailure {
                CustomToast.show("获取用户列表失败 ${it.message}")
            }
        }
    }


    private suspend fun fetchAllContacts(): List<ContactItem> = withContext(Dispatchers.IO) {
        val allAgents = mutableListOf<EaseProfile>()
        allAgents.addAll(chatProtocolService.fetchPublicAgent())
        allAgents.addAll(chatProtocolService.fetchUserAgent(true))
        allAgents.map { it.toContactItem() }
    }

    override fun onCleared() {
        super.onCleared()
    }


    // 更新数据
    fun updateContactByKey(key: String, select: Boolean) {
        val list = _contacts.value.toMutableList()
        val item = list.find { it.userId == key }
        if (item != null) {
            if (!item.isCheck) {
                val count = contacts.value.filter { it.isCheck }.count()
                if (count >= MAX_SELECT_COUNT) {
                    CustomToast.show(
                        AgoraApplication.the().getString(R.string.aichat_group_create_desc, MAX_SELECT_COUNT)
                    )
                    return
                }
            }
            item.isCheck = select
            _contacts.value = list

            //更新选择的用户
            val list = mutableListOf<ContactItem>()
            list.add(selfItem)
            val contactItemList: List<ContactItem> = contacts.value.filter { it.isCheck }
            list.addAll(contactItemList)
            val count = contactItemList.count()
            if (count < MAX_SELECT_COUNT) {
                list.add(placeHolder)
            }
            _selectDatas.value = list
        }
    }

    fun createGroup(groupName: String, list: List<EaseProfile>) {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                chatProtocolService.createGroupAgent(groupName, list)
            }.onSuccess {
                loadingChange.dismissDialog.postValue(true)
                _createGroupLiveData.postValue(it)
            }.onFailure {
                loadingChange.dismissDialog.postValue(true)
                _createGroupLiveData.postValue("")
                CustomToast.show("创建群聊失败 ${it.message}")
            }
        }
    }
}