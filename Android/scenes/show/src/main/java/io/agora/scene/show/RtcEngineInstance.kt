package io.agora.scene.show

import io.agora.rtc2.Constants
import io.agora.rtc2.IMediaExtensionObserver
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.SegmentationProperty
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.show.debugSettings.DebugSettingModel
import io.agora.scene.show.videoSwitcherAPI.VideoSwitcher
import java.util.concurrent.Executors

object RtcEngineInstance {

    val videoEncoderConfiguration = VideoEncoderConfiguration().apply {
        orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
    }
    val virtualBackgroundSource = VirtualBackgroundSource().apply {
        backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR
    }
    val virtualBackgroundSegmentation = SegmentationProperty()
    val videoCaptureConfiguration = CameraCapturerConfiguration(CameraCapturerConfiguration.CaptureFormat()).apply {
        followEncodeDimensionRatio = false
    }
    val debugSettingModel = DebugSettingModel().apply { }

    private val workingExecutor = Executors.newSingleThreadExecutor()

    // 万能通用 token ,进入房间列表默认获取万能 token
    private var generalToken: String = ""

    fun setupGeneralToken(generalToken: String) {
        this.generalToken = generalToken
    }

    fun generalToken(): String = generalToken

    private var innerRtcEngine: RtcEngineEx? = null
    val rtcEngine: RtcEngineEx
        get() {
            if (innerRtcEngine == null) {
                val config = RtcEngineConfig()
                config.mContext = AgoraApplication.the()
                config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
                config.mExtensionObserver = object: IMediaExtensionObserver{
                    override fun onEvent(
                        provider: String?,
                        extension: String?,
                        key: String?,
                        value: String?
                    ) {
                        ShowLogger.d(
                            "RtcEngineInstance",
                            "Rtc Extension onEvent >> provider=$provider, extension=$extension, key=$key, value=$value"
                        )
                    }

                    override fun onStarted(provider: String?, extension: String?) {
                        ShowLogger.d(
                            "RtcEngineInstance",
                            "Rtc Extension onStarted >> provider=$provider, extension=$extension"
                        )
                    }

                    override fun onStopped(provider: String?, extension: String?) {
                        ShowLogger.d(
                            "RtcEngineInstance",
                            "Rtc Extension onStopped >> provider=$provider, extension=$extension"
                        )
                    }

                    override fun onError(
                        provider: String?,
                        extension: String?,
                        error: Int,
                        message: String?
                    ) {
                        ShowLogger.d(
                            "RtcEngineInstance",
                            "Rtc Extension onError >> provider=$provider, extension=$extension, error=$error, message=$message"
                        )
                    }

                }
                config.mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onError(err: Int) {
                        super.onError(err)
                        ShowLogger.d(
                            "RtcEngineInstance",
                            "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err)
                        )
                    }

                    override fun onLocalVideoStateChanged(
                        source: Constants.VideoSourceType?,
                        state: Int,
                        error: Int
                    ) {
                        super.onLocalVideoStateChanged(source, state, error)
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    enableVideo()
                }
            }
            return innerRtcEngine!!
        }

    fun cleanCache() {
        VideoSwitcher.getImplInstance(rtcEngine).unloadConnections()
    }

    fun resetVirtualBackground() {
        virtualBackgroundSegmentation.modelType = SegmentationProperty.SEG_MODEL_AI
        virtualBackgroundSegmentation.greenCapacity = 0.5f
        virtualBackgroundSource.backgroundSourceType =
            VirtualBackgroundSource.BACKGROUND_COLOR
        innerRtcEngine?.enableVirtualBackground(
            false,
            virtualBackgroundSource,
            virtualBackgroundSegmentation
        )
    }


    fun destroy() {
        innerRtcEngine?.let {
            workingExecutor.execute { RtcEngineEx.destroy() }
            innerRtcEngine = null
        }
        debugSettingModel.apply {
            pvcEnabled = true
            autoFocusFaceModeEnabled = true
            exposurePositionX = null
            exposurePositionY = null
            cameraSelect = null
            videoFullrangeExt = null
            matrixCoefficientsExt = null
            enableHWEncoder = true
            codecType = 3     // 2 -> h264, 3 -> h265
            mirrorMode = false
            renderMode = 0       // 0 -> hidden, 1 -> fix
            colorEnhance = false
            dark = false
            noise = false
            srEnabled = false
            srType = 1.0
        }
    }
}