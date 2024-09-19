package io.agora.scene.aichat

import android.content.Context
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.impl.OnValueSuccess
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.EaseUserProfileProvider
import io.agora.scene.aichat.imkit.supends.fetchUserInfo
import io.agora.scene.aichat.list.roomdb.ChatDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AIChatHelper private constructor() {

    private lateinit var dataModel: ChatDataModel

    @Synchronized
    fun init(context: Context) {
        dataModel = ChatDataModel(context)
        initIM(context.applicationContext)
    }

    fun getDataModel(): ChatDataModel {
        return dataModel
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
        EaseIM.init(context, options)
        EaseIM.setUserProfileProvider(object : EaseUserProfileProvider {

            override fun getUser(userId: String): EaseProfile? {
                return getDataModel().getAllContacts()[userId]
            }

            override fun fetchUsers(userIds: List<String>, onValueSuccess: OnValueSuccess<List<EaseProfile>>) {
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching {
                        if (userIds.isEmpty()) {
                            onValueSuccess(mutableListOf())
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
                            getInstance().getDataModel().insertUsers(easeProfileList)
                            getInstance().getDataModel().updateUsersTimes(easeProfileList)
                            EaseIM.updateUsersInfo(easeProfileList)
                        }
                        easeProfileList
                    }
                        .onSuccess {
                            onValueSuccess(it)
                        }
                        .onFailure {

                        }

                }
            }
        })
    }

    companion object {
        private const val TAG = "AIChatHelper"
        private var instance: AIChatHelper? = null
        fun getInstance(): AIChatHelper {
            if (instance == null) {
                synchronized(AIChatHelper::class.java) {
                    if (instance == null) {
                        instance = AIChatHelper()
                    }
                }
            }
            return instance!!
        }

        fun reset() {
            instance = null
        }


        private val voiceIdPublicMapping = mutableMapOf<String, String>(
            "staging-common-agent-001" to "female-shaonv", // 声酱 少女音色
            "staging-common-agent-002" to "male-qn-daxuesheng", // 程序员小声	青年大学生音色
            "staging-common-agent-003" to "male-qn-jingying", // 声律师	精英青年音色
            "staging-common-agent-004" to "audiobook_male_1", // 声医师	男性有声书1
        )
    }
}