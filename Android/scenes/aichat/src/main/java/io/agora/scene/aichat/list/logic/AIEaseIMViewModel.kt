package io.agora.scene.aichat.list.logic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.agora.chat.UserInfo
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.EaseIM
import io.agora.scene.aichat.imkit.EaseIMCache
import io.agora.scene.aichat.imkit.impl.OnValueSuccess
import io.agora.scene.aichat.imkit.impl.ValueCallbackImpl
import io.agora.scene.aichat.imkit.model.EaseGroupProfile
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.EaseGroupProfileProvider
import io.agora.scene.aichat.imkit.provider.EaseUserProfileProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Ai ease im viewmodel
 *
 * @constructor Create empty A i ease i m view model
 */
class AIEaseIMViewModel constructor(val app: Application) : AndroidViewModel(app) {

    fun initIM() {
        if (EaseIM.isInited()) {
            return
        }
        val options = io.agora.chat.ChatOptions().apply {
            appKey = AIChatCenter.mChatAppKey
            autoLogin = false
        }
        EaseIM.init(app, options)
        EaseIM.setUserProfileProvider(object : EaseUserProfileProvider {
            override fun getUser(userId: String?): EaseProfile? {
                return null
            }

            override fun fetchUsers(userIds: List<String>, onValueSuccess: OnValueSuccess<List<EaseProfile>>) {
                ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(
                    userIds.toTypedArray(), ValueCallbackImpl(
                        onSuccess = {
                            onValueSuccess.invoke(emptyList())
                        },
                        onError = { code, error ->
                            onValueSuccess.invoke(emptyList())
                        })
                )
            }
        })
        EaseIM.setGroupProfileProvider(object : EaseGroupProfileProvider {
            override fun getGroup(id: String?): EaseGroupProfile? {
                return null
            }

            override fun fetchGroups(groupIds: List<String>, onValueSuccess: OnValueSuccess<List<EaseGroupProfile>>) {
                // TODO:  
            }
        })
    }

    fun resetIM() {
        EaseIM.logout(true)
        EaseIM.releaseGlobalListener()
    }
}