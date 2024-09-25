package io.agora.scene.aichat.groupmanager.logic

import androidx.lifecycle.viewModelScope
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.R
import io.agora.scene.aichat.create.logic.ContactItem
import io.agora.scene.aichat.create.logic.toContactItem
import io.agora.scene.aichat.groupmanager.logic.AIChatGroupManagerViewModel.Companion.ADD_PLACEHOLDER
import io.agora.scene.aichat.groupmanager.logic.AIChatGroupManagerViewModel.Companion.DELETE_PLACEHOLDER
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.model.getAllGroupAgents
import io.agora.scene.aichat.imkit.provider.getSyncUser
import io.agora.scene.aichat.imkit.supends.removeContact
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

fun ContactItem.isAddPlaceHolder(): Boolean {
    return userId.isEmpty() && name == ADD_PLACEHOLDER
}

fun ContactItem.isDeletePlaceHolder(): Boolean {
    return userId.isEmpty() && name == DELETE_PLACEHOLDER
}

class AIChatGroupManagerViewModel constructor(val mConversationId: String) : AIBaseViewModel() {

    companion object {
        const val MIN_SELECT_COUNT = 1
        const val MAX_SELECT_COUNT = 5

        const val ADD_PLACEHOLDER = "addPlaceholder"
        const val DELETE_PLACEHOLDER = "deletePlaceholder"
    }

    private val selfItem =
        ContactItem(EaseIM.getCurrentUser().id, EaseIM.getCurrentUser().name ?: "", EaseIM.getCurrentUser().avatar)

    private val addPlaceHolder = ContactItem("", ADD_PLACEHOLDER)
    private val deletePlaceHolder = ContactItem("", DELETE_PLACEHOLDER)

    // 所有成员
    private val _contacts by lazy { UnPeekLiveData(emptyList<ContactItem>()) }

    // 群成员
    private val _groupMemberDatas by lazy { UnPeekLiveData(listOf(selfItem)) }
    val groupMemberDatas: UnPeekLiveData<List<ContactItem>> = _groupMemberDatas

    // 可以添加的成员
    private val _canAddContacts by lazy { UnPeekLiveData(emptyList<ContactItem>()) }
    val canAddContacts: UnPeekLiveData<List<ContactItem>> = _canAddContacts

    private val _selectAddDatas by lazy { UnPeekLiveData(emptyList<ContactItem>()) }
    val selectAddDatas: UnPeekLiveData<List<ContactItem>> = _selectAddDatas

    // 可以删除的成员
    private val _canDeleteContacts by lazy { UnPeekLiveData(emptyList<ContactItem>()) }
    val canDeleteContacts: UnPeekLiveData<List<ContactItem>> = _canDeleteContacts

    private val _selectDeleteDatas by lazy { UnPeekLiveData(emptyList<ContactItem>()) }
    val selectDeleteDatas: UnPeekLiveData<List<ContactItem>> = _selectDeleteDatas

    // 更新群聊名称
    val updateGroupLiveData: UnPeekLiveData<Boolean> = UnPeekLiveData()

    val addGroupAgentLiveData: UnPeekLiveData<Boolean> = UnPeekLiveData()

    val deleteGroupAgentLiveData: UnPeekLiveData<Boolean> = UnPeekLiveData()

    // 删除群聊
    val deleteGroupLivedata: UnPeekLiveData<Boolean> = UnPeekLiveData()

    init {
        viewModelScope.launch {
            runCatching {
                initGroupAgents()
            }.onSuccess {
                _groupMemberDatas.value = it
            }.onFailure {
            }
        }
        viewModelScope.launch {
            runCatching {
                fetchAllContacts()
            }.onSuccess {
                _contacts.value = it
            }.onFailure {
            }
        }
    }

    fun getChatName(): String {
        return EaseIM.getUserProvider().getSyncUser(mConversationId)?.getNotEmptyName() ?: mConversationId
    }

    private suspend fun initGroupAgents(): List<ContactItem> = withContext(Dispatchers.IO) {
        val groupAgents = EaseIM.getUserProvider().getSyncUser(mConversationId)?.getAllGroupAgents() ?: emptyList()
        val contactSelectList = groupAgents.map { it.toContactItem() }

        val list = mutableListOf<ContactItem>()
        list.add(selfItem)
        list.addAll(contactSelectList)

        val count = contactSelectList.count()
        if (count < MAX_SELECT_COUNT) {
            list.add(addPlaceHolder)
        }
        if (count > MIN_SELECT_COUNT) {
            list.add(deletePlaceHolder)
        }
        list
    }

    private suspend fun fetchAllContacts(): List<ContactItem> = withContext(Dispatchers.IO) {
        val allAgents = mutableListOf<EaseProfile>()
        allAgents.addAll(fetchPublicAgent())
        allAgents.addAll(fetchUserAgent(true))
        val contactList = allAgents.map { it.toContactItem() }
        contactList
    }

    override fun onCleared() {
        super.onCleared()
    }

    // 可添加的群智能体
    fun fetchCanAddContacts() {
        val canAddList = _contacts.value?.filter { easeProfile ->
            groupContacts.none { contact -> contact.userId == easeProfile.userId }
        } ?: emptyList()
        _canAddContacts.value = canAddList
    }

    val groupContacts: List<ContactItem>
        get() {
            return EaseIM.getUserProvider().getSyncUser(mConversationId)?.getAllGroupAgents()
                ?.map { it.toContactItem() } ?: emptyList()
        }

    // 可删除的群智能体
    fun fetchDeleteContacts() {
        val canDeleteList = groupContacts
        _canDeleteContacts.postValue(canDeleteList)
    }

    // 更新数据
    fun updateAddContactByKey(key: String, select: Boolean) {
        val list = canAddContacts.value?.toList() ?: emptyList()
        val item = list.find { it.userId == key }
        if (item != null) {
            if (!item.isCheck) {
                val count = list.count { it.isCheck }
                if (count >= MAX_SELECT_COUNT) {
                    CustomToast.show(
                        AgoraApplication.the().getString(R.string.aichat_group_create_desc, MAX_SELECT_COUNT)
                    )
                    return
                }
            }
            item.isCheck = select
            val ll = mutableListOf<ContactItem>().apply { addAll(list) }
            _canAddContacts.value = ll

            _selectAddDatas.value = canAddContacts.value?.filter { it.isCheck } ?: emptyList()
        }
    }

    fun updateDeleteContactByKey(key: String, select: Boolean) {
        val list = canDeleteContacts.value?.toList() ?: emptyList()
        val item = list.find { it.userId == key }
        if (item != null) {
            if (!item.isCheck) {
                val count = list.count { it.isCheck }
                if (list.size - count <= MIN_SELECT_COUNT) {
                    CustomToast.show(
                        AgoraApplication.the().getString(R.string.aichat_group_min_desc, MIN_SELECT_COUNT)
                    )
                    return
                }
            }
            item.isCheck = select
            _canDeleteContacts.postValue(list)

            _selectDeleteDatas.postValue(list.filter { it.isCheck })
        }
    }

    /**
     * Edit group name
     *
     * @param groupName
     */
    fun editGroupName(groupName: String) {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                suspendUpdateGroupName(groupName)
            }.onSuccess {
                loadingChange.dismissDialog.postValue(true)
                updateGroupLiveData.postValue(it)
            }.onFailure {
                loadingChange.dismissDialog.postValue(true)
                updateGroupLiveData.postValue(false)
                CustomToast.show("更新群聊名称失败 ${it.message}")
            }
        }
    }

    private suspend fun suspendUpdateGroupName(groupName: String): Boolean = withContext(Dispatchers.IO) {
        val groupProfile =
            EaseIM.getUserProvider().getSyncUser(mConversationId) ?: throw AIApiException(-1, "group not found")
        // 更新用户元数据
        val userEx = mutableMapOf<String, String>()
        userEx["nickname"] = groupName

        val extJSONObject = JSONObject()
        extJSONObject.putOpt("groupName", groupName)


        val ext = groupProfile.ext ?: JSONObject()

        userEx["ext"] = JSONObject().putOpt(mConversationId, extJSONObject).toString()
        val updateUser = aiChatService.updateMetadata(username = mConversationId, fields = userEx)
        if (!updateUser.isSuccess) {
            throw AIApiException(updateUser.code ?: -1, updateUser.message ?: "")
        }
        updateUser.isSuccess
    }

    fun editAddGroupAgent(list: List<ContactItem>) {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                suspendUpdateGroupAgents(list)
            }.onSuccess {
                loadingChange.dismissDialog.postValue(true)
                addGroupAgentLiveData.postValue(it)
            }.onFailure {
                loadingChange.dismissDialog.postValue(true)
                addGroupAgentLiveData.postValue(false)
                CustomToast.show("更新群聊名称失败 ${it.message}")
            }
        }
    }

    fun editDeleteGroupAgent(list: List<ContactItem>) {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                suspendUpdateGroupAgents(list)
            }.onSuccess {
                loadingChange.dismissDialog.postValue(true)
                deleteGroupAgentLiveData.postValue(it)
            }.onFailure {
                loadingChange.dismissDialog.postValue(true)
                deleteGroupAgentLiveData.postValue(false)
                CustomToast.show("更新群聊名称失败 ${it.message}")
            }
        }
    }

    private suspend fun suspendUpdateGroupAgents(list: List<ContactItem>): Boolean = withContext(Dispatchers.IO) {
        // 更新用户元数据
        val userEx = mutableMapOf<String, String>()

        val groupAvatar = EaseIM.getCurrentUser().avatar + "," + list.last().avatar

        val extJSONObject = JSONObject()
        val botIds = list.map { it.userId }.joinToString(",")
        extJSONObject.putOpt("botIds", botIds)
        extJSONObject.putOpt("groupIcon", groupAvatar)

        userEx["ext"] = JSONObject().putOpt(mConversationId, extJSONObject).toString()
        val updateUser = aiChatService.updateMetadata(username = mConversationId, fields = userEx)
        if (!updateUser.isSuccess) {
            throw AIApiException(updateUser.code ?: -1, updateUser.message ?: "")
        }
        updateUser.isSuccess
    }

    // 删除群聊
    fun deleteGroup() {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                deleteAgent(mConversationId)
            }.onSuccess {
                loadingChange.dismissDialog.postValue(true)
                deleteGroupLivedata.postValue(it)
            }.onFailure {
                CustomToast.showError("删除群聊失败 ${it.message}")
                //打印错误栈信息
                it.printStackTrace()
                deleteGroupLivedata.postValue(false)
                loadingChange.dismissDialog.postValue(true)
            }
        }
    }

    private suspend fun deleteAgent(conversationId: String) = withContext(Dispatchers.IO) {
        val result = ChatClient.getInstance().contactManager().removeContact(conversationId, false)
        return@withContext result == ChatError.EM_NO_ERROR
        val response =
            aiChatService.deleteChatUser(username = EaseIM.getCurrentUser().id, toDeleteUsername = conversationId)
        return@withContext response.isSuccess
    }
}