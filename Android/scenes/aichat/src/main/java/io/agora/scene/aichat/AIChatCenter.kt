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

    // TODO: 正式环境采用声动互娱的uid，测试环境使用1000000+uid，来区分环境
    val isStaging = true

    val mAppId: String get() = BuildConfig.AGORA_APP_ID
    val mChatAppKey: String get() = io.agora.scene.aichat.BuildConfig.IM_APP_KEY

    val mXFAppKey: String get() = io.agora.scene.aichat.BuildConfig.XF_APP_KEY
    val mXFAppId: String get() = io.agora.scene.aichat.BuildConfig.XF_APP_ID
    val mXFAppSecret: String get() = io.agora.scene.aichat.BuildConfig.XF_APP_SECRET

    val mUser: User get() = UserManager.getInstance().user

    val mRtcUid: Long = if (isStaging) (mUser.id + 1000000) else mUser.id

    val mChatUserId: String get() = if (isStaging) (mUser.id + 1000000).toString() else mUser.id.toString()

    var mChatToken: String = ""

    var mRtcToken: String = ""

    fun onLogoutScene() {
        mChatToken = ""
        mRtcToken = ""
    }
}