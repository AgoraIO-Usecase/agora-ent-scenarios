package io.agora.scene.aichat

import android.content.Context
import io.agora.chat.Conversation.ConversationType
import io.agora.scene.aichat.create.logic.PreviewAvatarItem
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConversationType
import io.agora.scene.aichat.imkit.ChatError
import io.agora.scene.aichat.imkit.ChatMessage
import io.agora.scene.aichat.imkit.EaseConstant
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.createAgentOrGroupSuccessMessage
import io.agora.scene.aichat.imkit.extensions.getMessageDigest
import io.agora.scene.aichat.imkit.extensions.getUser
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.extensions.saveGreetingMessage
import io.agora.scene.aichat.imkit.helper.EasePreferenceManager
import io.agora.scene.aichat.imkit.impl.EaseContactListener
import io.agora.scene.aichat.imkit.impl.EaseConversationListener
import io.agora.scene.aichat.imkit.impl.OnValueSuccess
import io.agora.scene.aichat.imkit.model.EaseConversation
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.EaseUserProfileProvider
import io.agora.scene.aichat.imkit.provider.fetchUsersBySuspend
import io.agora.scene.aichat.imkit.supends.deleteConversationFromServer
import io.agora.scene.aichat.imkit.supends.fetchConversationsFromServer
import io.agora.scene.aichat.imkit.supends.fetchUserInfo
import io.agora.scene.aichat.service.api.AIApiException
import io.agora.scene.aichat.service.api.AICreateUserReq
import io.agora.scene.aichat.service.api.CreateUserType
import io.agora.scene.aichat.service.api.TTSReq
import io.agora.scene.aichat.service.api.aiChatService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AIChatProtocolService private constructor() {

    companion object {
        private const val TAG = "AIChatProtocolService"
        private var sInstance: AIChatProtocolService? = null
        fun instance(): AIChatProtocolService {
            if (sInstance == null) {
                synchronized(AIChatProtocolService::class.java) {
                    if (sInstance == null) {
                        sInstance = AIChatProtocolService()
                    }
                }
            }
            return sInstance!!
        }

        fun reset() {
            sInstance = null
        }

        private val voiceIdPublicMapping = mutableMapOf<String, String>(
            "common-agent-001" to "female-shaonv", // 声酱 少女音色
            "common-agent-002" to "male-qn-daxuesheng", // 程序员小声	青年大学生音色
            "common-agent-003" to "male-qn-jingying", // 声律师	精英青年音色
            "common-agent-004" to "audiobook_male_1", // 声医师	男性有声书1
        )
    }

    /**
     * 公共智能体是否加载完成
     */
    @Volatile
    var isPublicAgentLoaded = false

    /**
     * 创建的智能体是否加载完成
     */
    @Volatile
    var isUserAgentLoaded = false

    /**
     * 群聊智能体是否加载完成
     */
    @Volatile
    var isGroupAgentLoaded = false

    @Synchronized
    fun init(context: Context) {
        initIM(context.applicationContext)
    }

    /**
     * Initialize the SDK.
     */
    @Synchronized
    private fun initIM(context: Context) {
        if (EaseIM.isInited()) {
            return
        }
        val options = io.agora.chat.ChatOptions().apply {
            appKey = AIChatCenter.mChatAppKey
            autoLogin = false
        }
        EaseIM.init(context, options, object : EaseUserProfileProvider {


            override fun getUser(userId: String): EaseProfile? {
                return EaseIM.getCache().getUser(userId)
            }

            override fun fetchUsers(userIds: List<String>, onValueSuccess: OnValueSuccess<List<EaseProfile>>) {
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching {
                        if (userIds.isEmpty()) {
                            onValueSuccess(emptyList())
                            return@launch
                        }
                        val userInfoMap = ChatClient.getInstance().userInfoManager().fetchUserInfo(userIds)
                        userInfoMap.forEach { (t, u) ->
                            if (voiceIdPublicMapping.containsKey(t)) {
                                u.birth = voiceIdPublicMapping[t]
                            }
                        }
                        // 自己没有更新头像，这里需要过滤掉
                        val easeProfileList =
                            userInfoMap.values.map { it.parse() }.filter { it.id != AIChatCenter.mChatUserId }
                        if (easeProfileList.isNotEmpty()) {
                            EaseIM.updateUsersInfo(easeProfileList)
                        }
                        easeProfileList
                    }
                        .onSuccess {
                            onValueSuccess(it)
                        }
                        .onFailure { it ->
                            it.printStackTrace()
                            onValueSuccess(emptyList())
                        }

                }
            }
        })
        EaseIM.addContactListener(contactListener)
        EaseIM.addConversationListener(conversationListener)
    }

    private fun isPublicAgent(userId: String): Boolean {
        return userId.contains("common-agent")
    }

    private fun isUserAgent(userId: String): Boolean {
        return userId.contains("user-agent")
    }

    private fun isGroupAgent(userId: String): Boolean {
        return userId.contains("user-group")
    }

    private val contactListener = object : EaseContactListener() {

        override fun onContactAdded(username: String?) {
            username ?: return
        }

        override fun onContactDeleted(username: String?) {
            username ?: return
//            ChatClient.getInstance().chatManager()
//                .deleteConversationFromServer(username, ConversationType.Chat, true, CallbackImpl(
//                    onSuccess = {
//                    },
//                    onError = { code, message ->
//                    }
//                ))
        }
    }

    private val conversationListener = object : EaseConversationListener() {
        override fun onConversationRead(from: String?, to: String?) {

        }

        override fun onConversationUpdate() {

        }
    }

    /**
     * 获取公开智能体列表
     *
     * @return
     */
    suspend fun fetchPublicAgent(force: Boolean = false): List<EaseProfile> = withContext(Dispatchers.IO) {
        val agentResult = aiChatService.fetchPublicAgent()
        val agentList = agentResult.data?.sortedBy { it.index }
        if (agentResult.isSuccess && !agentList.isNullOrEmpty()) {

        } else {
            throw AIApiException(agentResult.code ?: -1, agentResult.message ?: "")
        }
        val publicAgentIds = agentList.map { it.username }

        // 获取本地缓存的智能体
        if (isPublicAgentLoaded && !force) {
            val publicAgentList =
                EaseIM.getCache().getAllUsers().filter { it.id.contains("common-agent-${EaseIM.getCurrentUser().id}") }
            val exitsList = publicAgentList.filter { agent -> publicAgentIds.contains(agent.id) }
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
            isPublicAgentLoaded = true
        }
        easeProfileList
    }


    /**
     * 获取用户创建的智能体列表
     *
     * @return
     */
    suspend fun fetchUserAgent(force: Boolean = false): List<EaseProfile> = withContext(Dispatchers.IO) {
        if (isUserAgentLoaded && !force) {
            return@withContext EaseIM.getCache().getAllUsers()
                .filter { it.id.contains("user-agent-${EaseIM.getCurrentUser().id}") }
        } else {
            val conServerList = ChatClient.getInstance()
                .contactManager().allContactsFromServer.filter { it.contains("user-agent-${EaseIM.getCurrentUser().id}") }
            val easeServerList = EaseIM.getUserProvider().fetchUsersBySuspend(conServerList).filter {
                it.id.contains("user-agent-${EaseIM.getCurrentUser().id}")
            }
            if (easeServerList.size == conServerList.size) {
                AIChatProtocolService.instance().isUserAgentLoaded = true
            }
            return@withContext easeServerList
        }
    }

    /**
     * 获取群聊创建的智能体列表
     *
     * @return
     */
    suspend fun fetchGroupAgent(force: Boolean = false): List<EaseProfile> = withContext(Dispatchers.IO) {
        if (isGroupAgentLoaded && !force) {
            return@withContext EaseIM.getCache().getAllUsers()
                .filter { it.id.contains("user-group-${EaseIM.getCurrentUser().id}") }
        } else {
            val conServerList = ChatClient.getInstance()
                .contactManager().allContactsFromServer.filter { it.contains("user-group-${EaseIM.getCurrentUser().id}") }
            val easeServerList = EaseIM.getUserProvider().fetchUsersBySuspend(conServerList).filter {
                it.id.contains("user-group-${EaseIM.getCurrentUser().id}")
            }
            if (easeServerList.size == conServerList.size) {
                isGroupAgentLoaded = true
            }
            return@withContext easeServerList
        }
    }

    suspend fun fetchForceAllAgent(): List<EaseProfile> = withContext(Dispatchers.IO) {
        val publicList = fetchPublicAgent(true)
        val conServerList = ChatClient.getInstance()
            .contactManager().allContactsFromServer.filter {
                it.contains("user-group-${EaseIM.getCurrentUser().id}")
                        || it.contains("user-agent-${EaseIM.getCurrentUser().id}")
            }
        val easeServerList = EaseIM.getUserProvider().fetchUsersBySuspend(conServerList).filter {
            it.id.contains("user-group-${EaseIM.getCurrentUser().id}") || it.id.contains(
                "user-agent-${
                    EaseIM
                        .getCurrentUser().id
                }"
            )
        }
        return@withContext publicList + easeServerList
    }

    /**
     * Create user agent
     *
     * @param previewAvatar
     * @param nickname
     * @param sign
     * @param prompt
     * @return
     */
    suspend fun createUserAgent(
        previewAvatar: PreviewAvatarItem,
        nickname: String,
        sign: String,
        prompt: String
    ): String = withContext(Dispatchers.IO) {

        val username = EaseIM.getCurrentUser().id
        val requestUser = AICreateUserReq(username, CreateUserType.Agent)

        // 创建智能体
        val createAgent = aiChatService.createChatUser(req = requestUser)
        val resultUsername = if (createAgent.isSuccess) {
            createAgent.data?.username ?: throw AIApiException(-1, "Username is null")
        } else {
            throw AIApiException(createAgent.code ?: -1, createAgent.message ?: "")
        }

        // 更新用户元数据
        val userEx = mutableMapOf<String, String>()
        userEx["nickname"] = nickname
        userEx["avatarurl"] = previewAvatar.avatar
        userEx["sign"] = sign
        userEx["birth"] = previewAvatar.voiceId // 用户属性中birth字段存的是voiceId
        userEx["ext"] = JSONObject().putOpt("prompt", prompt).toString()
        val updateUser = aiChatService.updateMetadata(username = resultUsername, fields = userEx)
        if (!updateUser.isSuccess) {
            throw AIApiException(updateUser.code ?: -1, updateUser.message ?: "")
        }
        val conversation =
            ChatClient.getInstance().chatManager().getConversation(resultUsername, ChatConversationType.Chat, true)

        ChatClient.getInstance().chatManager().saveMessage(
            conversation.createAgentOrGroupSuccessMessage(false)
        )
        val chatMessage = conversation.saveGreetingMessage(EaseProfile(resultUsername, name = nickname), true)
        chatMessage?.run {
            ChatClient.getInstance().chatManager().saveMessage(this)
        }
        resultUsername
    }

    /**
     * Create group agent
     *
     * @param groupName
     * @param list
     * @return
     */
    suspend fun createGroupAgent(groupName: String, list: List<EaseProfile>): String = withContext(Dispatchers.IO) {
        val createGroup =
            aiChatService.createChatUser(req = AICreateUserReq(EaseIM.getCurrentUser().id, CreateUserType.Group))
        val resultUsername: String = if (createGroup.isSuccess) {
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
        val botIds = list.map { it.id }.joinToString(",")
        extJSONObject.putOpt("botIds", botIds)
        extJSONObject.putOpt("groupName", groupName)
        extJSONObject.putOpt("groupIcon", groupAvatar)
        extJSONObject.putOpt("bot_group", true)

        userEx["ext"] = extJSONObject.toString()
        val updateUser = aiChatService.updateMetadata(username = resultUsername, fields = userEx)
        if (!updateUser.isSuccess) {
            throw AIApiException(updateUser.code ?: -1, updateUser.message ?: "")
        }

        val conversation =
            ChatClient.getInstance().chatManager().getConversation(resultUsername, ChatConversationType.Chat, true)

        conversation.extField = extJSONObject.toString()
        ChatClient.getInstance().chatManager().saveMessage(
            conversation.createAgentOrGroupSuccessMessage(true)
        )
        resultUsername
    }

    /**
     * 获取会话列表
     *
     * @param isForce
     * @return
     */
    suspend fun fetchConversation(isForce: Boolean = false): List<EaseConversation> =
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
                fetchForceAllAgent()
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
                EasePreferenceManager.getInstance().setLoadedConversationsFromServer(true)
            }
            conversationList
        }

    /**
     * 删除智能体
     *
     * @param id
     * @return
     */
    suspend fun deleteAgent(id: String): Boolean = withContext(Dispatchers.IO) {
        val response = aiChatService.deleteChatUser(username = EaseIM.getCurrentUser().id, toDeleteUsername = id)
        val isSuccess = response.isSuccess
        if (isSuccess) {
            EaseIM.getCache().removeUser(id)
            ChatClient.getInstance().chatManager().deleteConversation(id, true)
            ChatClient.getInstance().chatManager().deleteConversationFromServer(id, ConversationType.Chat, true)
        }
        isSuccess
    }

    /**
     * 删除会话，群聊需要删除群用户
     *
     * @param conversationId
     */
    suspend fun deleteConversation(conversationId: String): Boolean = withContext(Dispatchers.IO) {
        if (isGroupAgent(conversationId)) {
            return@withContext deleteAgent(conversationId)
        }
        ChatClient.getInstance().chatManager().deleteConversation(conversationId, true)
        val result = ChatClient.getInstance().chatManager().deleteConversationFromServer(
            conversationId,
            ConversationType.Chat, true
        )
        result == ChatError.EM_NO_ERROR
    }

    /**
     * Request tts
     *
     * @param text
     * @param voiceId
     * @return
     */
    suspend fun requestTts(message: ChatMessage): String = withContext(Dispatchers.IO) {
        val text = message.getMessageDigest()
        val voiceId = message.getUser()?.voiceId ?: "female-shaonv"
        val req = TTSReq(text, voiceId)
        val response = aiChatService.requestTts(req = req)
        if (response.isSuccess) {
            val audioPath = response.data?.audio ?: ""
            if (audioPath.isNotEmpty()) {
                EaseIM.getCache().insertMessageAudio(message.conversationId(), message.msgId, audioPath)
                return@withContext audioPath
            }
        }
        throw AIApiException(response.code ?: -1, response.message ?: "")
    }
}