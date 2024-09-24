package io.agora.scene.aichat

import androidx.lifecycle.ViewModel
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.fetchUsersBySuspend
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.aiChatService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class AIBaseViewModel : ViewModel() {
    val loadingChange: UiLoadingChange by lazy { UiLoadingChange() }

    inner class UiLoadingChange {
        //显示加载框
        val showDialog by lazy { UnPeekLiveData<Boolean>() }

        //隐藏
        val dismissDialog by lazy { UnPeekLiveData<Boolean>() }
    }

    /**
     * 获取公开智能体列表
     *
     * @return
     */
    protected suspend fun fetchPublicAgent(force: Boolean = false): List<EaseProfile> = withContext(Dispatchers.IO) {
        val agentResult = aiChatService.fetchPublicAgent()
        val agentList = agentResult.data?.sortedBy { it.index }
        if (agentResult.isSuccess && !agentList.isNullOrEmpty()) {

        } else {
            throw AIApiException(agentResult.code ?: -1, agentResult.message ?: "")
        }
        val publicAgentIds = agentList.map { it.username }

        // 获取本地缓存的智能体
        if (AIChatHelper.instance().isPublicAgentLoaded && !force) {
            val exitsList =
                AIChatHelper.instance().publicAgentList.filter { agent -> publicAgentIds.contains(agent.id) }
            if (exitsList.size == publicAgentIds.size) {
                return@withContext exitsList
            }
        }

        val easeProfileMap: Map<String, EaseProfile> =
            EaseIM.getUserProvider().fetchUsersBySuspend(agentList.map { it.username }).associateBy { it.id }

        val easeProfileList = mutableListOf<EaseProfile>()
        for (i in agentList.indices) {
            val agent = agentList[i]
            val userInfo = easeProfileMap[agent.username]
            val aiAgentModel = userInfo ?: EaseProfile(agent.username)
            easeProfileList.add(aiAgentModel)
        }
        if (easeProfileList.size == publicAgentIds.size) {
            AIChatHelper.instance().isPublicAgentLoaded = true
        }
        easeProfileList
    }

    /**
     * 获取用户创建的智能体列表
     *
     * @return
     */
    protected suspend fun fetchUserAgent(force: Boolean = false): List<EaseProfile> = withContext(Dispatchers.IO) {
        if (AIChatHelper.instance().isUserAgentLoaded && !force) {
            return@withContext AIChatHelper.instance().userAgentList
        } else {
            val conServerList = ChatClient.getInstance()
                .contactManager().allContactsFromServer.filter { it.contains("user-agent-${EaseIM.getCurrentUser().id}") }
            val easeServerList = EaseIM.getUserProvider().fetchUsersBySuspend(conServerList).filter {
                it.id.contains("user-agent-${EaseIM.getCurrentUser().id}")
            }
            if (easeServerList.size == conServerList.size) {
                AIChatHelper.instance().isUserAgentLoaded = true
            }
            return@withContext easeServerList
        }
    }

    /**
     * 获取群聊创建的智能体列表
     *
     * @return
     */
    protected suspend fun fetchGroupAgent(force: Boolean = false): List<EaseProfile> = withContext(Dispatchers.IO) {
        if (AIChatHelper.instance().isGroupAgentLoaded && !force) {
            return@withContext AIChatHelper.instance().groupAgentList
        } else {
            val conServerList = ChatClient.getInstance()
                .contactManager().allContactsFromServer.filter { it.contains("user-group-${EaseIM.getCurrentUser()
                    .id}") }
            val easeServerList = EaseIM.getUserProvider().fetchUsersBySuspend(conServerList).filter {
                it.id.contains("user-group-${EaseIM.getCurrentUser().id}")
            }
            if (easeServerList.size == conServerList.size) {
                AIChatHelper.instance().isGroupAgentLoaded = true
            }
            return@withContext easeServerList
        }
    }

    protected suspend fun fetchForceAllAgent(): List<EaseProfile> = withContext(Dispatchers.IO) {
        val publicList = fetchPublicAgent(true)
        val conServerList = ChatClient.getInstance()
            .contactManager().allContactsFromServer.filter {
                it.contains("user-group-${EaseIM.getCurrentUser().id}")
                        || it.contains("user-agent-${EaseIM.getCurrentUser().id}")
            }
        val easeServerList = EaseIM.getUserProvider().fetchUsersBySuspend(conServerList).filter {
            it.id.contains("user-group-${EaseIM.getCurrentUser().id}") || it.id.contains("user-agent-${EaseIM
                .getCurrentUser().id}")
        }
        return@withContext publicList + easeServerList
    }
}