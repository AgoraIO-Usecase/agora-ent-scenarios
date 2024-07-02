package io.agora.scene.joy

import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import java.util.concurrent.Executors

object JoyServiceManager {

     const val TAG = "JoyServiceManager"

    private val mWorkingExecutor = Executors.newSingleThreadExecutor()

    var mRtmToken: String = ""
        private set(value) {
            field = value
        }

    var mRtcToken: String = ""
        private set(value) {
            field = value
        }

    private var innerRtcEngine: RtcEngineEx? = null
    val rtcEngine: RtcEngineEx
        get() {
            if (innerRtcEngine == null) {
                val config = RtcEngineConfig()
                config.mContext = AgoraApplication.the()
                config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
                config.mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onError(err: Int) {
                        super.onError(err)
                        JoyLogger.d(TAG, "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    enableVideo()
                }
            }
            return innerRtcEngine!!
        }

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
                JoyLogger.d(TAG, "generate RtmTokens success")
                callback.invoke(rtmToken, null)
            },
            failure = {
                JoyLogger.e(TAG, "generate RtmToken failed,$it")
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
                JoyLogger.d(TAG, "generate RtcToken success")
                mRtcToken = rtcToken
                callback.invoke(mRtcToken, null)
            },
            failure = { exception ->
                JoyLogger.e(TAG, "generate RtcToken failed, $exception")
                callback.invoke(null, exception)
            })
    }

    fun destroy() {
        innerRtcEngine?.let {
            mWorkingExecutor.execute { RtcEngineEx.destroy() }
            innerRtcEngine = null
        }
        mRtmToken = ""
        mRtcToken = ""
    }
}