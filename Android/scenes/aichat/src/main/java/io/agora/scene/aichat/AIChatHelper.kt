package io.agora.scene.aichat

import android.content.Context
import io.agora.chat.Conversation.ConversationType
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.impl.CallbackImpl
import io.agora.scene.aichat.imkit.impl.EaseContactListener
import io.agora.scene.aichat.imkit.impl.EaseConversationListener
import io.agora.scene.aichat.imkit.impl.OnValueSuccess
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.EaseUserProfileProvider
import io.agora.scene.aichat.imkit.supends.fetchUserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AIChatHelper private constructor() {

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

    // 公开智能体
    private val _publicAgentList = mutableSetOf<EaseProfile>()

    val publicAgentList: List<EaseProfile> get() = _publicAgentList.toList()

    // 创建的智能体
    private val _userAgentList = mutableSetOf<EaseProfile>()

    val userAgentList: List<EaseProfile> get() = _userAgentList.toList()

    // 群聊智能体
    private val _groupAgentList = mutableSetOf<EaseProfile>()

    val groupAgentList: List<EaseProfile> get() = _groupAgentList.toList()

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
                if (isPublicAgent(userId)) {
                    return _publicAgentList.firstOrNull { it.id == userId }
                } else if (isUserAgent(userId)) {
                    return _userAgentList.firstOrNull { it.id == userId }
                } else if (isGroupAgent(userId)) {
                    return _groupAgentList.firstOrNull { it.id == userId }
                }
                return null
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
                        easeProfileList.forEach {
                            if (isPublicAgent(it.id)) {
                                _publicAgentList.add(it)
                            } else if (isUserAgent(it.id)) {
                                _userAgentList.add(it)
                            } else if (isGroupAgent(it.id)) {
                                _groupAgentList.add(it)
                            }
                        }
                        easeProfileList
                    }
                        .onSuccess {
                            onValueSuccess(it)
                        }
                        .onFailure { it->
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
            if (isPublicAgent(username)) {
                _publicAgentList.removeIf { it.id == username }
            } else if (isUserAgent(username)) {
                _userAgentList.removeIf { it.id == username }
            } else if (isGroupAgent(username)) {
                _groupAgentList.removeIf { it.id == username }
            }
            ChatClient.getInstance().chatManager()
                .deleteConversationFromServer(username, ConversationType.Chat, true, CallbackImpl(
                    onSuccess = {
                    },
                    onError = { code, message ->
                    }
                ))
        }
    }

    private val conversationListener = object : EaseConversationListener() {
        override fun onConversationRead(from: String?, to: String?) {

        }

        override fun onConversationUpdate() {

        }
    }

    companion object {
        private const val TAG = "AIChatHelper"
        private var sInstance: AIChatHelper? = null
        fun instance(): AIChatHelper {
            if (sInstance == null) {
                synchronized(AIChatHelper::class.java) {
                    if (sInstance == null) {
                        sInstance = AIChatHelper()
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
}