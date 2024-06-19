package io.agora.scene.playzone

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager

/**
 * Ktv center
 * ktv 场景全局
 */
object PlayZoneCenter {

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

    /**
     * Renew rtm token
     *
     * @param callback
     * @receiver
     */
    fun generateRtmToken(callback: (rtmToken: String?, exception: Exception?) -> Unit) {
        TokenGenerator.generateToken(
            channelName = "", // 万能 token
            uid = UserManager.getInstance().user.id.toString(),
            genType = TokenGenerator.TokenGeneratorType.token007,
            tokenType = TokenGenerator.AgoraTokenType.rtm,
            success = { rtmToken ->
                mRtmToken = rtmToken
                PlayZoneLogger.d(TAG, "generate RtmTokens success")
                callback.invoke(rtmToken, null)
            },
            failure = {
                PlayZoneLogger.e(TAG, it, "generate RtmToken failed,$it")
                callback.invoke(null, it)
            })
    }

    /**
     * Renew rtc token
     *
     * @param channelName
     * @param callback
     * @receiver
     */
    fun generateRtcToken(callback: (rtcToken: String?, exception: Exception?) -> Unit) {
        if (mRtcToken.isNotEmpty()) {
            callback.invoke(mRtcToken, null)
            return
        }
        TokenGenerator.generateToken(
            channelName = "",
            uid = UserManager.getInstance().user.id.toString(),
            genType = TokenGenerator.TokenGeneratorType.token007,
            tokenType = TokenGenerator.AgoraTokenType.rtc,
            success = { rtcToken ->
                PlayZoneLogger.d(TAG, "generate RtcToken success")
                mRtcToken = rtcToken
                callback.invoke(mRtcToken, null)
            },
            failure = { exception ->
                PlayZoneLogger.e(TAG, "generate RtcToken failed, $exception")
                callback.invoke(null, exception)
            })
    }
}