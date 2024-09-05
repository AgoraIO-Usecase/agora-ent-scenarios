package io.agora.scene.aichat.imkit.impl

import android.content.Context
import io.agora.scene.aichat.ext.isMainProcess
import io.agora.scene.aichat.imkit.ChatClient
import io.agora.scene.aichat.imkit.ChatConnectionListener
import io.agora.scene.aichat.imkit.ChatContactListener
import io.agora.scene.aichat.imkit.ChatConversationListener
import io.agora.scene.aichat.imkit.ChatEventResultListener
import io.agora.scene.aichat.imkit.ChatGroupChangeListener
import io.agora.scene.aichat.imkit.ChatLog
import io.agora.scene.aichat.imkit.ChatMessageListener
import io.agora.scene.aichat.imkit.ChatMultiDeviceListener
import io.agora.scene.aichat.imkit.ChatOptions
import io.agora.scene.aichat.imkit.ChatPresenceListener
import io.agora.scene.aichat.imkit.ChatRoomChangeListener
import io.agora.scene.aichat.imkit.ChatThreadChangeListener
import io.agora.scene.aichat.imkit.EaseIMCache
import io.agora.scene.aichat.imkit.EaseIMClient
import io.agora.scene.aichat.imkit.model.EaseGroupProfile
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.EaseGroupProfileProvider
import io.agora.scene.aichat.imkit.provider.EaseUserProfileProvider
import java.util.concurrent.atomic.AtomicBoolean

internal class EaseIMClientImpl : EaseIMClient {
    private var isInit: AtomicBoolean = AtomicBoolean(false)
    private var groupProfileProvider: EaseGroupProfileProvider? = null
    private var userProvider: EaseUserProfileProvider? = null
    private var config: ChatOptions = ChatOptions()
    private lateinit var context: Context
    private val cache: EaseIMCache by lazy { EaseIMCache() }
    private var user: EaseProfile? = null

    companion object {
        private const val TAG = "EaseIMClient"
    }

    override fun init(context: Context, options: ChatOptions?) {
        ChatLog.e(TAG, "UIKIt init")
        if (isInit.get()) {
            return
        }
        if (!context.isMainProcess()) {
            ChatLog.e(TAG, "Please init EaseIM in main process")
            return
        }
        this.context = context.applicationContext
        var chatOptions: ChatOptions? = null
        if (options == null) {
            chatOptions = ChatOptions().apply {
                // change to need confirm contact invitation
                acceptInvitationAlways = false
                // set if need read ack
                requireAck = true
                // set if need delivery ack
                requireDeliveryAck = false
            }
        } else {
            chatOptions = options
        }
        this.config = chatOptions
        ChatClient.getInstance().init(context, chatOptions)
        addChatListenersWrapper()
        isInit.set(true)
        // If auto login, should init the cache.
        if (chatOptions.autoLogin && ChatClient.getInstance().isLoggedInBefore) {
            cache.init()
        }
        ChatLog.e(TAG, "UIKIt init end")
    }

    override fun loginWithAgoraToken(username: String, token: String, onSuccess: OnSuccess, onError: OnError) {
        this.user = EaseProfile(username)
        ChatClient.getInstance().loginWithAgoraToken(username, token, CallbackImpl(onSuccess = {
            cache.init()
            cache.insertUser(user!!)
            onSuccess.invoke()
        }, onError))
    }

    override fun loginWithToken(username: String, token: String, onSuccess: OnSuccess, onError: OnError) {
        this.user = EaseProfile(username)
        ChatClient.getInstance().loginWithToken(username, token, CallbackImpl(onSuccess = {
            cache.init()
            cache.insertUser(user!!)
            onSuccess.invoke()
        }, onError))
    }

    override fun login(userId: String, password: String, onSuccess: OnSuccess, onError: OnError) {
        this.user = EaseProfile(userId)
        ChatClient.getInstance().login(userId, password, CallbackImpl(onSuccess = {
            cache.init()
            cache.insertUser(user!!)
            onSuccess.invoke()
        }, onError))
    }

    override fun logout(unbindDeviceToken: Boolean, onSuccess: OnSuccess, onError: OnError) {
        val oldId = getCurrentUser()?.id
        ChatClient.getInstance().logout(unbindDeviceToken, CallbackImpl(onSuccess = {
            cache.clear()
            onSuccess.invoke()
        }, onError))
    }

    override fun isInited(): Boolean {
        return isInit.get()
    }

    override fun isLoggedIn(): Boolean {
        return ChatClient.getInstance().isLoggedIn
    }

    override fun updateCurrentUser(user: EaseProfile) {
        this.user = user
        cache.insertUser(user)
    }

    override fun getCurrentUser(): EaseProfile? {
        return if (!ChatClient.getInstance().currentUser.isNullOrEmpty()) {
            cache.getUser(ChatClient.getInstance().currentUser)
                ?: EaseProfile(ChatClient.getInstance().currentUser)
        } else null
    }

    override fun setGroupProfileProvider(provider: EaseGroupProfileProvider) {
        groupProfileProvider = provider
    }

    override fun setUserProfileProvider(provider: EaseUserProfileProvider) {
        userProvider = provider
    }

    override fun updateGroupProfiles(profiles: List<EaseGroupProfile>) {
        cache.updateProfiles(profiles)
    }

    override fun updateUsersInfo(users: List<EaseProfile>) {
        cache.updateUsers(users)
    }

    override fun getContext(): Context? {
        if (!isInit.get()) {
            ChatLog.e(TAG, "please init UIKit SDK first!")
            return null
        }
        return context
    }

    override fun getGroupProfileProvider(): EaseGroupProfileProvider? {
        return groupProfileProvider
    }

    override fun getUserProvider(): EaseUserProfileProvider? {
        return userProvider
    }

    override fun clearKitCache() {
        cache.clear()
    }

    override fun getKitCache(): EaseIMCache {
        return cache
    }

    private fun addChatListenersWrapper() {
        ChatListenersWrapper.getInstance().addListeners()
    }

    fun removeChatListener() {
        ChatListenersWrapper.getInstance().removeListeners()
    }

    override fun addConnectionListener(listener: ChatConnectionListener) {
        ChatListenersWrapper.getInstance().addConnectionListener(listener)
    }

    override fun removeConnectionListener(listener: ChatConnectionListener) {
        ChatListenersWrapper.getInstance().removeConnectionListener(listener)
    }

    override fun addChatMessageListener(listener: ChatMessageListener) {
        ChatListenersWrapper.getInstance().addChatMessageListener(listener)
    }

    override fun removeChatMessageListener(listener: ChatMessageListener) {
        ChatListenersWrapper.getInstance().removeChatMessageListener(listener)
    }

    override fun addGroupChangeListener(listener: ChatGroupChangeListener) {
        ChatListenersWrapper.getInstance().addGroupChangeListener(listener)
    }

    override fun removeGroupChangeListener(listener: ChatGroupChangeListener) {
        ChatListenersWrapper.getInstance().removeGroupChangeListener(listener)
    }

    override fun addContactListener(listener: ChatContactListener) {
        ChatListenersWrapper.getInstance().addContactListener(listener)
    }

    override fun removeContactListener(listener: ChatContactListener) {
        ChatListenersWrapper.getInstance().removeContactListener(listener)
    }

    override fun addConversationListener(listener: ChatConversationListener) {
        ChatListenersWrapper.getInstance().addConversationListener(listener)
    }

    override fun removeConversationListener(listener: ChatConversationListener) {
        ChatListenersWrapper.getInstance().removeConversationListener(listener)
    }

    override fun addPresenceListener(listener: ChatPresenceListener) {
        ChatListenersWrapper.getInstance().addPresenceListener(listener)
    }

    override fun removePresenceListener(listener: ChatPresenceListener) {
        ChatListenersWrapper.getInstance().removePresenceListener(listener)
    }

    override fun addChatRoomChangeListener(listener: ChatRoomChangeListener) {
        ChatListenersWrapper.getInstance().addChatRoomChangeListener(listener)
    }

    override fun removeChatRoomChangeListener(listener: ChatRoomChangeListener) {
        ChatListenersWrapper.getInstance().removeChatRoomChangeListener(listener)
    }

    override fun addMultiDeviceListener(listener: ChatMultiDeviceListener) {
        ChatListenersWrapper.getInstance().addMultiDeviceListener(listener)
    }

    override fun removeMultiDeviceListener(listener: ChatMultiDeviceListener) {
        ChatListenersWrapper.getInstance().removeMultiDeviceListener(listener)
    }

    override fun addEventResultListener(listener: ChatEventResultListener) {
        ChatListenersWrapper.getInstance().addEventResultListener(listener)
    }

    override fun removeEventResultListener(listener: ChatEventResultListener) {
        ChatListenersWrapper.getInstance().removeEventResultListener(listener)
    }

    override fun callbackEvent(function: String, errorCode: Int, errorMessage: String?) {
        ChatListenersWrapper.getInstance().callbackEvent(function, errorCode, errorMessage)
    }

    override fun addThreadChangeListener(listener: ChatThreadChangeListener) {
        ChatListenersWrapper.getInstance().addThreadChangeListener(listener)
    }

    override fun removeThreadChangeListener(listener: ChatThreadChangeListener) {
        ChatListenersWrapper.getInstance().removeThreadChangeListener(listener)
    }

    override fun releaseGlobalListener() {
        removeChatListener()
    }
}