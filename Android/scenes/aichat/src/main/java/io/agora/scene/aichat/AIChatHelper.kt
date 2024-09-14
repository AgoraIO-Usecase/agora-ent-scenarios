package io.agora.scene.aichat

import android.content.Context
import android.util.Log
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.extensions.parse
import io.agora.scene.aichat.imkit.impl.OnValueSuccess
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.EaseUserProfileProvider
import io.agora.scene.aichat.imkit.supends.fetchUserInfo
import io.agora.scene.aichat.list.roomdb.ChatDataModel
import io.agora.scene.aichat.service.api.AIAgentResult
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
                return getInstance().getDataModel().getAllContacts()[userId]
            }

            override fun fetchUsers(userIds: List<String>, onValueSuccess: OnValueSuccess<List<EaseProfile>>) {
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching {
                        if (userIds.isEmpty()) {
                            onValueSuccess(mutableListOf())
                            return@launch
                        }
                        val userInfoMap = ChatClient.getInstance().userInfoManager().fetchUserInfo(userIds)
                        // 自己没有更新头像，这里需要过滤掉
                        val easeProfileList =
                            userInfoMap.values.map { it.parse() }.filter { it.id != AIChatCenter.mUser.id.toString() }
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
    }
}