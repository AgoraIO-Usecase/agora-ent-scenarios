package io.agora.scene.playzone

import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.base.component.AgoraApplication
import java.util.concurrent.Executors

object PlayRtcEngineInstance {

    private val mWorkingExecutor = Executors.newSingleThreadExecutor()

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
                        PlayZoneLogger.d("PlayRtcEngineInstance", "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    enableAudio()
                }
            }
            return innerRtcEngine!!
        }

    fun destroy() {
        innerRtcEngine?.let {
            mWorkingExecutor.execute { RtcEngineEx.destroy() }
            innerRtcEngine = null
        }
    }
}