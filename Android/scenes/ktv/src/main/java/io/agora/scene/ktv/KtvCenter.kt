package io.agora.scene.ktv

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager

/**
 * Ktv center
 * ktv 场景全局
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
    fun generateRtmToken(callback: (rtmToken: String?, exception: Exception?) -> Unit) {
        TokenGenerator.generateToken(
            channelName = "", // 万能 token
            uid = UserManager.getInstance().user.id.toString(),
            genType = TokenGenerator.TokenGeneratorType.token007,
            tokenType = TokenGenerator.AgoraTokenType.rtm,
            success = { rtmToken ->
                mRtmToken = rtmToken
                KTVLogger.d(TAG, "generate RtmTokens success")
                callback.invoke(rtmToken, null)
            },
            failure = {
                KTVLogger.e(TAG, it, "generate RtmToken failed,$it")
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
                KTVLogger.d(TAG, "generate RtcToken success")
                mRtcToken = rtcToken
                callback.invoke(mRtcToken, null)
//                TokenGenerator.generateToken(channelName + "_rtc_ex", mCurrentUser.userId,
//                    TokenGenerator.TokenGeneratorType.Token006, TokenGenerator.AgoraTokenType.Rtc,
//                    success = { rtcChorusToken ->
//                        KTVLogger.d(TAG, "renewRtcChorusToken success")
//                        callback.invoke(rtcToken, rtcChorusToken, null)
//                    },
//                    failure = { exception ->
//                        KTVLogger.e(TAG, "renewRtcChorusToken failed, $exception")
//                        callback.invoke(rtcToken, null, exception)
//                    })
            },
            failure = { exception ->
                KTVLogger.e(TAG, "generate RtcToken failed, $exception")
                callback.invoke(null, exception)
            })
    }
}