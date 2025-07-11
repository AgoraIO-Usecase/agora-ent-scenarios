package io.agora.scene.aichat

import io.agora.scene.base.BuildConfig
import io.agora.scene.base.ServerConfig
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

    val mXFAppKey: String get() = io.agora.scene.aichat.BuildConfig.XF_APP_KEY
    val mXFAppId: String get() = io.agora.scene.aichat.BuildConfig.XF_APP_ID
    val mXFAppSecret: String get() = io.agora.scene.aichat.BuildConfig.XF_APP_SECRET

    val mUser: User get() = UserManager.getInstance().user

    val mRtcUid: Int = if (ServerConfig.envRelease) mUser.id.toInt() else (mUser.id + 1000000).toInt()

    val mChatUserId: String get() = if (ServerConfig.envRelease) mUser.id.toString() else (mUser.id + 1000000).toString()

    var mChatToken: String = ""

    var mRtcToken: String = ""

    fun onLogoutScene() {
        mChatToken = ""
        mRtcToken = ""
    }
}