package io.agora.scene.ktv

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager

/**
 * Ktv center
 * Global KTV scene management
 */
object KtvCenter {

    val mAppId: String get() = BuildConfig.AGORA_APP_ID

    val mUser: User get() = UserManager.getInstance().user

    val AUIRoomInfo.rtcChorusChannelName
        get() = this.roomId + "_rtc_ex"

    var mRtmToken: String = ""
        private set(value) {
            field = value
        }

    var mRtcToken: String = ""
        private set(value) {
            field = value
        }

    private const val TAG = "KtvCenter"

    // 显示在线用户需要多加
    const val userAddMore: Int = 1

    /**
     * Renew rtm token
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
                TokenGenerator.AgoraTokenType.rtm
            ),
            success = { token ->
                mRtmToken = token
                mRtcToken = token
                KTVLogger.d(TAG, "generate tokens success")
                callback.invoke(token, null)
            },
            failure = {
                KTVLogger.e(TAG, it, "generate tokens failed,$it")
                callback.invoke(null, it)
            })
    }

    fun reset(){
        mRtmToken = ""
        mRtcToken = ""
    }
}