package io.agora.scene.aichat.imkit

import android.content.Context
import io.agora.scene.aichat.imkit.impl.OnError
import io.agora.scene.aichat.imkit.impl.OnSuccess
import io.agora.scene.aichat.imkit.model.EaseGroupProfile
import io.agora.scene.aichat.imkit.model.EaseProfile
import io.agora.scene.aichat.imkit.provider.EaseGroupProfileProvider
import io.agora.scene.aichat.imkit.provider.EaseUserProfileProvider

interface EaseIMClient {

    /**
     * Initialize the Chat UIKit.
     * @param context The application context.
     * @param options The options of the Chat SDK.
     */
    fun init(context: Context, options: ChatOptions?)

    /**
     * Login with user object by token.
     * @param username The username.
     * @param token The token.
     * @param onSuccess The callback of success.
     * @param onError The callback of error.
     */
    fun loginWithAgoraToken(username: String,
                            token: String,
                            onSuccess: OnSuccess,
                            onError: OnError
    )

    /**
     * Temp for test.
     */
    fun login(
        userId: String,
        password: String,
        onSuccess: OnSuccess,
        onError: OnError
    )

    /**
     * Temp for test.
     */
    fun loginWithToken(
        username: String,
        token: String,
        onSuccess: OnSuccess,
        onError: OnError
    )

    /**
     * Log out from the Chat SDK.
     * @param unbindDeviceToken Whether unbind the device token.
     * @param onSuccess The callback of success.
     * @param onError The callback of error.
     */
    fun logout(
        unbindDeviceToken: Boolean,
        onSuccess: OnSuccess,
        onError: OnError
    )

    /**
     * Whether the uikit is be initialized.
     */
    fun isInited(): Boolean

    /**
     * Whether the user is logged in.
     */
    fun isLoggedIn(): Boolean

    /**
     * Update the current user info.
     * @param user
     */
    fun updateCurrentUser(user: EaseProfile)

    /**
     * Get the current user.
     */
    fun getCurrentUser(): EaseProfile

    /**
     * Set the conversation information provider.
     * @param provider The provider of the conversation information.
     */
    fun setGroupProfileProvider(provider: EaseGroupProfileProvider)

    /**
     * Set the userinfo provider.
     * @param provider The provider of the userinfo.
     */
    fun setUserProfileProvider(provider: EaseUserProfileProvider)

    /**
     * Update the UIKit profiles in cache.
     */
    fun updateGroupProfiles(profiles: List<EaseGroupProfile>)

    /**
     * Update the UIKit userinfo in cache.
     */
    fun updateUsersInfo(users: List<EaseProfile>)

    /**
     * Get the application context.
     */
    fun getContext(): Context?

    /**
     * Get the conversation information provider.
     */
    fun getGroupProfileProvider(): EaseGroupProfileProvider?

    /**
     * Get the userinfo provider.
     */
    fun getUserProvider(): EaseUserProfileProvider?

    /**
     * Clear the cache.
     */
    fun clearKitCache()

    /**
     * Get the cache.
     */
    fun getKitCache(): EaseIMCache

    /**
     * Add Connection Listener
     */
    fun addConnectionListener(listener: ChatConnectionListener) {}

    /**
     * Remove Connection Listener
     */
    fun removeConnectionListener(listener: ChatConnectionListener) {}

    /**
     * Add ChatMessage Listener
     */
    fun addChatMessageListener(listener: ChatMessageListener) {}

    /**
     * Remove ChatMessage Listener
     */
    fun removeChatMessageListener(listener: ChatMessageListener) {}

    /**
     * Add GroupChange Listener
     */
    fun addGroupChangeListener(listener: ChatGroupChangeListener) {}

    /**
     * Remove GroupChange Listener
     */
    fun removeGroupChangeListener(listener: ChatGroupChangeListener) {}

    /**
     * Add Contact Listener
     */
    fun addContactListener(listener: ChatContactListener) {}

    /**
     * Remove Contact Listener
     */
    fun removeContactListener(listener: ChatContactListener) {}

    /**
     * Add Conversation Listener
     */
    fun addConversationListener(listener: ChatConversationListener) {}

    /**
     * Remove Conversation Listener
     */
    fun removeConversationListener(listener: ChatConversationListener) {}

    /**
     * Add Presence Listener
     */
    fun addPresenceListener(listener: ChatPresenceListener) {}

    /**
     * Remove Presence Listener
     */
    fun removePresenceListener(listener: ChatPresenceListener) {}

    /**
     * Add ChatRoomChange Listener
     */
    fun addChatRoomChangeListener(listener: ChatRoomChangeListener) {}

    /**
     * Remove ChatRoomChange Listener
     */
    fun removeChatRoomChangeListener(listener: ChatRoomChangeListener) {}

    /**
     * Add MultiDevice Listener
     */
    fun addMultiDeviceListener(listener: ChatMultiDeviceListener) {}

    /**
     * Remove MultiDevice Listener
     */
    fun removeMultiDeviceListener(listener: ChatMultiDeviceListener) {}

    /**
     * Add Event Result Listener
     */
    fun addEventResultListener(listener: ChatEventResultListener) {}

    /**
     * Remove Event Result Listener
     */
    fun removeEventResultListener(listener: ChatEventResultListener) {}

    /**
     * Callback Event
     */
    fun callbackEvent(function: String, errorCode: Int, errorMessage: String?) {}

    /**
     * Add Thread Change Listener
     */
    fun addThreadChangeListener(listener: ChatThreadChangeListener) {}

    /**
     * Remove Thread Change Listener
     */
    fun removeThreadChangeListener(listener: ChatThreadChangeListener) {}

    /**
     * release global listener
     */
    fun releaseGlobalListener() {}
}