package io.agora.scene.aichat.imkit

import android.content.Context
import io.agora.chat.adapter.EMAError
import io.agora.scene.aichat.imkit.impl.EaseIMClientImpl
import io.agora.scene.aichat.imkit.impl.OnError
import io.agora.scene.aichat.imkit.impl.OnSuccess
import io.agora.scene.aichat.imkit.model.EaseGroupProfile
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.EaseGroupProfileProvider
import io.agora.scene.aichat.imkit.provider.EaseUserProfileProvider

/**
 * It is the main class of the Chat UIKit.
 */
object EaseIM {
    // Whether the debug mode is open in EaseIM.
    const val DEBUG: Boolean = true

    /**
     * Open Application first load block list from server.
     */
    var isLoadBlockListFromServer: Boolean? = false

    private val client: EaseIMClient by lazy {
        EaseIMClientImpl()
    }

    /**
     * Initialize the Chat UIKit.
     * @param context The application context.
     * @param options The options of the Chat SDK.
     */
    @Synchronized
    fun init(context: Context, options: ChatOptions): EaseIM {
        client.init(context, options)
        return this
    }

    /**
     * Judge whether the uikit is be initialized.
     */
    fun isInited(): Boolean {
        return client.isInited()
    }

    fun releaseGlobalListener() {
        client.releaseGlobalListener()
    }

    /**
     * login with token
     */
    fun loginWithToken(
        username: String,
        token: String,
        onSuccess: OnSuccess = {},
        onError: OnError = { _, _ -> }
    ) {
        client.loginWithAgoraToken(username, token, onSuccess, onError = { code, error ->
            if (code == EMAError.USER_ALREADY_LOGIN) {
                onSuccess.invoke()
            } else {
                onError.invoke(code, error)
            }
        })
    }

    /**
     * Temp for test.
     */
    fun login(
        userId: String,
        password: String,
        onSuccess: OnSuccess = {},
        onError: OnError = { _, _ -> }
    ) {
        client.login(userId, password, onSuccess, onError)
    }

    /**
     * Log out from the Chat SDK.
     * @param unbindDeviceToken Whether unbind the device token.
     * @param onSuccess The callback of success.
     * @param onError The callback of error.
     */
    fun logout(
        unbindDeviceToken: Boolean,
        onSuccess: OnSuccess = {},
        onError: OnError = { _, _ -> }
    ) {
        client.logout(unbindDeviceToken, onSuccess, onError)
    }

    /**
     * Whether the user is logged in.
     */
    fun isLoggedIn(): Boolean {
        return client.isLoggedIn()
    }

    /**
     * Update the current user.
     */
    fun updateCurrentUser(user: EaseProfile) {
        client.updateCurrentUser(user)
    }

    /**
     * Get the current user.
     */
    fun getCurrentUser(): EaseProfile? {
        return client.getCurrentUser()
    }

    /**
     * Set the conversation information provider.
     * @param provider The provider of the conversation information.
     */
    fun setGroupProfileProvider(provider: EaseGroupProfileProvider): EaseIM {
        client.setGroupProfileProvider(provider)
        return this
    }

    /**
     * Set the userinfo provider.
     * @param provider The provider of the userinfo.
     */
    fun setUserProfileProvider(provider: EaseUserProfileProvider): EaseIM {
        client.setUserProfileProvider(provider)
        return this
    }

    /**
     * Update the UIKit group information in cache.
     * @param profiles The profiles to update.
     */
    fun updateGroupInfo(profiles: List<EaseGroupProfile>) {
        client.updateGroupProfiles(profiles)
    }

    /**
     * Update the UIKit user information in cache.
     * @param users The profiles to update.
     */
    fun updateUsersInfo(users: List<EaseProfile>) {
        client.updateUsersInfo(users)
    }

    /**
     * Get the application context.
     */
    fun getContext(): Context? {
        return client.getContext()
    }

    /**
     * Clear the cache.
     */
    fun clearCache() {
        client.clearKitCache()
    }

    /**
     * Get the cache.
     */
    internal fun getCache() = client.getKitCache()

    /**
     * Get the conversation information provider.
     */
    fun getGroupProfileProvider(): EaseGroupProfileProvider? {
        return client.getGroupProfileProvider()
    }

    /**
     * Get the userinfo provider.
     */
    fun getUserProvider(): EaseUserProfileProvider? {
        return client.getUserProvider()
    }


    /**
     * Add Connection Listener
     */
    fun addConnectionListener(connectListener: ChatConnectionListener) {
        client.addConnectionListener(connectListener)
    }

    /**
     * Remove Connection Listener
     */
    fun removeConnectionListener(connectListener: ChatConnectionListener) {
        client.removeConnectionListener(connectListener)
    }

    /**
     * Add ChatMessage Listener
     */
    fun addChatMessageListener(listener: ChatMessageListener) {
        client.addChatMessageListener(listener)
    }

    /**
     * Remove ChatMessage Listener
     */
    fun removeChatMessageListener(listener: ChatMessageListener) {
        client.removeChatMessageListener(listener)
    }

    /**
     * Add GroupChange Listener
     */
    fun addGroupChangeListener(listener: ChatGroupChangeListener) {
        client.addGroupChangeListener(listener)
    }

    /**
     * Remove GroupChange Listener
     */
    fun removeGroupChangeListener(listener: ChatGroupChangeListener) {
        client.removeGroupChangeListener(listener)
    }

    /**
     * Add Contact Listener
     */
    fun addContactListener(listener: ChatContactListener) {
        client.addContactListener(listener)
    }

    /**
     * Remove Contact Listener
     */
    fun removeContactListener(listener: ChatContactListener) {
        client.removeContactListener(listener)
    }

    /**
     * Add Conversation Listener
     */
    fun addConversationListener(listener: ChatConversationListener) {
        client.addConversationListener(listener)
    }

    /**
     * Remove Conversation Listener
     */
    fun removeConversationListener(listener: ChatConversationListener) {
        client.removeConversationListener(listener)
    }

    /**
     * Add Presence Listener
     */
    fun addPresenceListener(listener: ChatPresenceListener) {
        client.addPresenceListener(listener)
    }

    /**
     * Remove Presence Listener
     */
    fun removePresenceListener(listener: ChatPresenceListener) {
        client.removePresenceListener(listener)
    }

    /**
     * Add ChatRoomChange Listener
     */
    fun addChatRoomChangeListener(listener: ChatRoomChangeListener) {
        client.addChatRoomChangeListener(listener)
    }

    /**
     * Remove ChatRoomChange Listener
     */
    fun removeChatRoomChangeListener(listener: ChatRoomChangeListener) {
        client.removeChatRoomChangeListener(listener)
    }

    /**
     * Add MultiDevice Listener
     */
    fun addMultiDeviceListener(listener: ChatMultiDeviceListener) {
        client.addMultiDeviceListener(listener)
    }

    /**
     * Remove MultiDevice Listener
     */
    fun removeMultiDeviceListener(listener: ChatMultiDeviceListener) {
        client.removeMultiDeviceListener(listener)
    }

    /**
     * Add Event Result Listener
     */
    fun addEventResultListener(listener: ChatEventResultListener) {
        client.addEventResultListener(listener)
    }

    /**
     * Remove Event Result Listener
     */
    fun removeEventResultListener(listener: ChatEventResultListener) {
        client.removeEventResultListener(listener)
    }

    /**
     * Set Event Result Callback
     */
    fun setEventResultCallback(function: String, errorCode: Int, errorMessage: String?) {
        client.callbackEvent(function, errorCode, errorMessage)
    }

    /**
     * Add Thread Change Listener
     */
    fun addThreadChangeListener(listener: ChatThreadChangeListener) {
        client.addThreadChangeListener(listener)
    }

    /**
     * Remove Thread Change Listener
     */
    fun removeThreadChangeListener(listener: ChatThreadChangeListener) {
        client.removeThreadChangeListener(listener)
    }

}