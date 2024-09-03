package io.agora.scene.aichat

import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
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

    /**
     * Renew rtc/chat token
     *
     * @param callback
     * @receiver
     */
    fun generateToken(callback: (token: String?, exception: Exception?) -> Unit) {
        TokenGenerator.generateTokens(
            channelName = "", // 万能 token
            uid = UserManager.getInstance().user.id.toString(),
            genType = TokenGenerator.TokenGeneratorType.token007,
            tokenTypes = arrayOf(
                TokenGenerator.AgoraTokenType.rtc,
                TokenGenerator.AgoraTokenType.chat
            ),
            success = { token ->
                mChatToken = token
                mRtcToken = token
                AILogger.d(TAG, "generate tokens success")
                callback.invoke(token, null)
            },
            failure = {
                AILogger.e(TAG, it, "generate tokens failed,$it")
                callback.invoke(null, it)
            })
    }

    fun onLogoutScene() {
        mChatToken = ""
        mRtcToken = ""
    }
}