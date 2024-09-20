package io.agora.scene.aichat.create.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AIChatHelper
import io.agora.scene.aichat.R
import io.agora.scene.aichat.ext.AIBaseViewModel
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.createAgentOrGroupSuccessMessage
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.AICreateUserReq
import io.agora.scene.aichat.service.api.CreateUserType
import io.agora.scene.aichat.service.api.aiChatService
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

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

    private val selfItem =
        ContactItem(EaseIM.getCurrentUser().id, EaseIM.getCurrentUser().name ?: "", EaseIM.getCurrentUser().avatar)
    private val placeHolder = ContactItem("", "placeholder")

    private val _selectDatas by lazy { MutableStateFlow(listOf(selfItem, placeHolder)) }
    val selectDatas: MutableStateFlow<List<ContactItem>> = _selectDatas

    private val _contacts by lazy { MutableStateFlow(emptyList<ContactItem>()) }
    val contacts: MutableStateFlow<List<ContactItem>> = _contacts

    // 创建群聊
    val createGroupLiveData: MutableLiveData<String> = MutableLiveData()

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
        val contacts = AIChatHelper.getInstance().getDataModel().getAllContacts()
        contacts.filter {
            it.key.contains("common-agent") or it.key.contains("user-agent")
        }.map { it.value.toContactItem() }
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
                suspendCreateGroup(groupName, list)
            }.onSuccess {
                loadingChange.dismissDialog.postValue(true)
                createGroupLiveData.postValue(it)
            }.onFailure {
                loadingChange.dismissDialog.postValue(true)
                createGroupLiveData.postValue("")
                CustomToast.show("创建群聊失败 ${it.message}")
            }
        }
    }

    private suspend fun suspendCreateGroup(groupName: String, list: List<EaseProfile>): String =
        withContext(Dispatchers.IO) {
            val createGroup =
                aiChatService.createChatUser(req = AICreateUserReq(EaseIM.getCurrentUser().id, CreateUserType.Group))
            val resultUsername: String = if (createGroup.isSuccess || createGroup.code == 1201) {
                createGroup.data?.username ?: throw AIApiException(-1, "Username is null")
            } else {
                throw AIApiException(createGroup.code ?: -1, createGroup.message ?: "")
            }
            val groupAvatar = EaseIM.getCurrentUser().avatar + "," + list.last().avatar
            // 更新用户元数据
            val userEx = mutableMapOf<String, String>()
            userEx["nickname"] = groupName
            userEx["avatarurl"] = groupAvatar

            val extJSONObject = JSONObject()
            val botIds = list.map { it.id }.joinToString { "," }
            extJSONObject.putOpt("botIds", botIds)
            extJSONObject.putOpt("groupName", groupName)
            extJSONObject.putOpt("groupIcon", groupAvatar)
            extJSONObject.putOpt("bot_group", true)

            userEx["ext"] = JSONObject().putOpt(resultUsername,extJSONObject).toString()
            val updateUser = aiChatService.updateMetadata(username = resultUsername, fields = userEx)
            if (!updateUser.isSuccess) {
                throw AIApiException(updateUser.code ?: -1, updateUser.message ?: "")
            }

            val conversation =
                ChatClient.getInstance().chatManager().getConversation(resultUsername, ChatConversationType.Chat, true)

            conversation.extField = extJSONObject.toString()
            ChatClient.getInstance().chatManager().saveMessage(
                conversation.createAgentOrGroupSuccessMessage( true)
            )
            resultUsername
        }
}