package io.agora.scene.aichat

import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager

/**
 * Ai chat center
 *
 * @constructor Create empty Ai chat center
 */
object AIChatCenter {

    private const val TAG = "AIChatCenter"

    val mAppId: String get() = BuildConfig.AGORA_APP_ID
    val mChatAppKey: String get() = io.agora.scene.aichat.BuildConfig.IM_APP_KEY

    val mUser: User get() = UserManager.getInstance().user

    val mChatUsername: String get() = mUser.id.toString()

    var mChatToken: String = ""

    var mRtcToken: String = ""

    fun onLogoutScene() {
        mChatToken = ""
        mRtcToken = ""
    }
}