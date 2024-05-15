package io.agora.scene.joy

import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.joy.service.TokenConfig
import java.util.concurrent.Executors

object JoyServiceManager {

     const val TAG = "JoyServiceManager"

    private val mWorkingExecutor = Executors.newSingleThreadExecutor()

    val mTokenConfig: TokenConfig = TokenConfig()

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

    fun renewTokens(callback: (tokenConfig: TokenConfig?, exception: Exception?) -> Unit) {
        TokenGenerator.generateTokens(
            "", // 万能 token
            UserManager.getInstance().user.id.toString(),
            TokenGenerator.TokenGeneratorType.token007,
            arrayOf(
                TokenGenerator.AgoraTokenType.rtc,
                TokenGenerator.AgoraTokenType.rtm
            ),
            success = { ret ->
                val rtcToken = ret[TokenGenerator.AgoraTokenType.rtc]
                val rtmToken = ret[TokenGenerator.AgoraTokenType.rtm]
                if (rtcToken == null || rtmToken == null) {
                    callback.invoke(null, Exception("rtcToken or rtmToken is null"))
                    return@generateTokens
                }
                mTokenConfig.rtcToken = rtcToken
                mTokenConfig.rtmToken = rtmToken
                callback.invoke(mTokenConfig, null)
            },
            failure = {
                JoyLogger.e(TAG, it, "generateTokens failed")
                callback.invoke(null, it)
            })
    }

    fun destroy() {
        innerRtcEngine?.let {
            mWorkingExecutor.execute { RtcEngineEx.destroy() }
            innerRtcEngine = null
        }
        mTokenConfig.rtcToken = ""
        mTokenConfig.rtmToken = ""
    }
}