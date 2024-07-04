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
import io.agora.scene.show.photographer.AiPhotographerType
import io.agora.scene.show.photographer.MetaEngineHandler
import io.agora.videoloaderapi.VideoLoader
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

    // 是否选择了虚拟背景
    val isVirtualBackgroundEnable: Boolean
        get() {
            return virtualBackgroundSource.backgroundSourceType == VirtualBackgroundSource.BACKGROUND_BLUR ||
                    virtualBackgroundSource.backgroundSourceType == VirtualBackgroundSource.BACKGROUND_IMG
        }
    val debugSettingModel = DebugSettingModel().apply { }

    private val workingExecutor = Executors.newSingleThreadExecutor()

    // 万能通用 token ,进入房间列表默认获取万能 token
    private var generalToken: String = ""

    fun setupGeneralToken(generalToken: String) {
        this.generalToken = generalToken
    }

    fun generalToken(): String = generalToken

    val mMetaEngineHandler = MetaEngineHandler()

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
                        ShowLogger.d(
                            "RtcEngineInstance",
                            "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err)
                        )
                    }
                }
                // 添加 MetaKit 插件
                config.addExtension("agora_metakit_extension")
                // 创建插件的事件回调接口类，注册 onEvent 等插件事件回调
                config.mExtensionObserver = object : IMediaExtensionObserver {

                    override fun onEvent(provider: String, ext: String, key: String, msg: String) {
                        mMetaEngineHandler.onEvent(provider, ext, key, msg)
                    }

                    override fun onStarted(provider: String, extension: String) {
                        mMetaEngineHandler.onStart(provider, extension)
                    }

                    override fun onStopped(provider: String, extension: String) {
                        mMetaEngineHandler.onStop(provider, extension)
                    }

                    override fun onError(provider: String, ext: String, key: Int, msg: String) {
                        mMetaEngineHandler.onError(provider, ext, key, msg)
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    mMetaEngineHandler.initializeRtc(this)
                    setParameters("{\"rtc.enable_camera_capture_yuv\":true}")
                    setParameters("{\"rtc.video.seg_before_exts\":true}")
                    // 背景分割需要在 startPreview 之后调用
//                    mMetaEngineHandler.enableSegmentation()
                    enableExtension("agora_video_filters_metakit", "metakit", true, Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE)
                    enableVideo()
                }
            }
            return innerRtcEngine!!
        }

    fun cleanCache() {
        VideoLoader.getImplInstance(rtcEngine).cleanCache()
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
        VideoLoader.release()
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