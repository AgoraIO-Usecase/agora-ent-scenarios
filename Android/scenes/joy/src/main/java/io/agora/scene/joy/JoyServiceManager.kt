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
    fun generateToken(callback: (rtmToken: String?, exception: Exception?) -> Unit) {
        TokenGenerator.generateTokens(
            channelName = "", // 万能 token
            uid = UserManager.getInstance().user.id.toString(),
            genType = TokenGenerator.TokenGeneratorType.token007,
            tokenTypes = arrayOf(
                TokenGenerator.AgoraTokenType.rtc,
                TokenGenerator.AgoraTokenType.rtm),
            success = { token ->
                mRtmToken = token
                mRtcToken = token
                JoyLogger.d(TAG, "generate token success")
                callback.invoke(token, null)
            },
            failure = {
                JoyLogger.e(TAG, "generate token failed,$it")
                callback.invoke(null, it)
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