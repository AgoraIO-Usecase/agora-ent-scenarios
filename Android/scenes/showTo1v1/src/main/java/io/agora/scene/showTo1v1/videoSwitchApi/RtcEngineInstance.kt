package io.agora.scene.showTo1v1.videoSwitchApi

import android.util.Log
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.component.AgoraApplication
import java.util.concurrent.Executors

object RtcEngineInstance {

    val videoEncoderConfiguration = VideoEncoderConfiguration().apply {
        orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
    }
    val virtualBackgroundSource = VirtualBackgroundSource().apply {
        backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR
    }
    val videoCaptureConfiguration = CameraCapturerConfiguration(CameraCapturerConfiguration.CaptureFormat()).apply {
        followEncodeDimensionRatio = false
    }

    private val workingExecutor = Executors.newSingleThreadExecutor()

    // 万能通用 token ,进入房间列表默认获取万能 token
    private var generalToken: String = ""

    fun setupGeneralToken(generalToken: String) {
        RtcEngineInstance.generalToken = generalToken
    }

    fun generalToken(): String = generalToken

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
                        Log.d("RtcEngine", "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    enableVideo()
                }
            }
            return innerRtcEngine!!
        }

    private var innerVideoSwitcher: VideoSwitcher? = null
    val videoSwitcher: VideoSwitcher
        get() {
            if (innerVideoSwitcher == null) {
                innerVideoSwitcher = VideoSwitcherImpl(rtcEngine, VideoSwitcherAPIImpl(rtcEngine))
            }
            return innerVideoSwitcher!!
        }

    fun cleanCache() {
        innerVideoSwitcher?.let {
            it.unloadConnections()
        }
    }

    fun destroy() {
        innerVideoSwitcher?.let {
            it.unloadConnections()
            innerVideoSwitcher = null
        }
        innerRtcEngine?.let {
            workingExecutor.execute { RtcEngine.destroy() }
            innerRtcEngine = null
        }
    }
}