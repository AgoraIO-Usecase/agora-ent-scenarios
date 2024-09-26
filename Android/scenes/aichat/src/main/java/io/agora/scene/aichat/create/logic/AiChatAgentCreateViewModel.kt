package io.agora.scene.aichat.create.logic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.agora.scene.aichat.AIBaseViewModel
import io.agora.scene.aichat.AIChatProtocolService
import io.agora.scene.widget.toast.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class PreviewAvatarItem constructor(
    val name: String = "",
    val avatar: String = "",
    val background: String = "",
    var voiceId: String = "",
)

class AiChatAgentCreateViewModel : AIBaseViewModel() {

    companion object {

        val avatar1 = "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/aichat/avatar/avatar1.png"
        val background1 = "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/aichat/bg/bg1.jpg"
    }

    private val chatProtocolService by lazy { AIChatProtocolService.instance() }

    private val _curPreviewAvatar by lazy { MutableStateFlow(PreviewAvatarItem()) }
    val curPreviewAvatar: MutableStateFlow<PreviewAvatarItem> = _curPreviewAvatar

    private val previewAvatarList = mutableListOf<PreviewAvatarItem>()

    // 创建智能体
    val createAgentLiveData: MutableLiveData<String> = MutableLiveData()

    // 我创建的智能体
    private val _mineCreateAgentLiveData by lazy { MutableStateFlow(0) }
    val mineCreateAgentLiveData: MutableStateFlow<Int> = _mineCreateAgentLiveData

    init {
        viewModelScope.launch {
            runCatching {
                fetchPreviewAvatars()
            }.onSuccess {
                previewAvatarList.clear()
                previewAvatarList.addAll(it)
                _curPreviewAvatar.value = it[0]
            }.onFailure {
                CustomToast.show("获取用户列表失败 ${it.message}")
            }
        }
        viewModelScope.launch {
            runCatching {
                chatProtocolService.fetchUserAgent(true)
            }.onSuccess {
                _mineCreateAgentLiveData.value = it.size
            }.onFailure {
                _mineCreateAgentLiveData.value = 0
            }
        }
    }

    // 默认 10个头像与背景
    private suspend fun fetchPreviewAvatars(): List<PreviewAvatarItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<PreviewAvatarItem>()
        list.add(PreviewAvatarItem("kid_avt", avatar1, background1, voiceId = "female-shaonv"))
        list.add(
            PreviewAvatarItem(
                "guy_avt", avatar1.replace("avatar1", "avatar2"),
                background1.replace("bg1", "bg2"), voiceId = "audiobook_male_1"
            )
        )
        list.add(
            PreviewAvatarItem(
                "girl_avt", avatar1.replace("avatar1", "avatar3"),
                background1.replace("bg1", "bg3"), voiceId = "audiobook_male_1"
            )
        )
        list.add(
            PreviewAvatarItem(
                "rich_avt", avatar1.replace("avatar1", "avatar4"),
                background1.replace("bg1", "bg4"), voiceId = "male-qn-badao"
            )
        )
        list.add(
            PreviewAvatarItem(
                "socrates_avt", avatar1.replace("avatar1", "avatar5"),
                background1.replace("bg1", "bg5"), voiceId = "audiobook_male_2"
            )
        )
        list.add(
            PreviewAvatarItem(
                "robot_avt", avatar1.replace("avatar1", "avatar6"),
                background1.replace("bg1", "bg6"), voiceId = "clever_boy"
            )
        )
        list.add(
            PreviewAvatarItem(
                "mage_avt", avatar1.replace("avatar1", "avatar7"),
                background1.replace("bg1", "bg7"), voiceId = "male-qn-jingying-jingpin"
            )
        )

        list.add(
            PreviewAvatarItem(
                "superhero_avt", avatar1.replace("avatar1", "avatar8"),
                background1.replace("bg1", "bg8"), voiceId = "audiobook_female_1"
            )
        )
        list.add(
            PreviewAvatarItem(
                "punk_avt", avatar1.replace("avatar1", "avatar9"),
                background1.replace("bg1", "bg9"), voiceId = "audiobook_female_1"
            )
        )
        list.add(
            PreviewAvatarItem(
                "luna_avt", avatar1.replace("avatar1", "avatar10"),
                background1.replace("bg1", "bg10"), voiceId = "cute_boy"
            )
        )
        list
    }

    override fun onCleared() {
        super.onCleared()
    }

    // 随机头像
    fun randomAvatar() {
        val size = previewAvatarList.size
        _curPreviewAvatar.value = previewAvatarList[Random.nextInt(size)]
    }

    // 创建智能体
    fun createAgent(previewAvatar: PreviewAvatarItem, nickname: String, sign: String, prompt: String) {
        viewModelScope.launch {
            runCatching {
                loadingChange.showDialog.postValue(true)
                chatProtocolService.createUserAgent(previewAvatar, nickname, sign, prompt)
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
}