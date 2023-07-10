package io.agora.scene.show

import android.util.Log
import com.sensetime.effects.STRenderKit
import io.agora.base.VideoFrame
import io.agora.beauty.sensetime.SenseTimeBeautyAPI
import io.agora.beauty.sensetime.createSenseTimeBeautyAPI
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.IVideoFrameObserver
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.beauty.IBeautyProcessor
import io.agora.scene.show.beauty.sensetime.BeautySenseTimeImpl
import io.agora.scene.show.debugSettings.DebugSettingModel
import java.util.concurrent.Executors

object RtcEngineInstance {

    val videoEncoderConfiguration = VideoEncoderConfiguration().apply {
        orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
    }
    val virtualBackgroundSource = VirtualBackgroundSource().apply {
        backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR
    }
    val videoCaptureConfiguration = CameraCapturerConfiguration(CameraCapturerConfiguration.CaptureFormat()).apply {
        followEncodeDimensionRatio = false
    }
    val debugSettingModel = DebugSettingModel().apply {  }

    private val workingExecutor = Executors.newSingleThreadExecutor()

    private var innerSenseTimeApi: SenseTimeBeautyAPI? = null
    val mSenseTimeApi: SenseTimeBeautyAPI
        get() {
            if (innerSenseTimeApi == null) {
                innerSenseTimeApi = createSenseTimeBeautyAPI()
            }
            return innerSenseTimeApi!!
        }


    private var innerBeautyProcessor: IBeautyProcessor? = null
    val beautyProcessor: IBeautyProcessor
        get() {
            if (innerBeautyProcessor == null) {
                innerBeautyProcessor = BeautySenseTimeImpl(AgoraApplication.the())
            }
            return innerBeautyProcessor!!
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
                        ShowLogger.d(
                            "RtcEngineInstance",
                            "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err)
                        )
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    //registerVideoFrameObserver(beautyProcessor)
                    registerVideoFrameObserver(object : IVideoFrameObserver {
                        private var shouldMirror = true
                        override fun onCaptureVideoFrame(
                            type: Int,
                            videoFrame: VideoFrame?
                        ): Boolean {
                            shouldMirror = false
                            val ret = mSenseTimeApi.onFrame(videoFrame!!)
                            Log.d("hugo", "mSenseTimeApi.onFrame: " + ret)
                            return when(ret) {
                                io.agora.beauty.sensetime.ErrorCode.ERROR_OK.value -> true
                                io.agora.beauty.sensetime.ErrorCode.ERROR_FRAME_SKIPPED.value -> false
                                else -> {
                                    shouldMirror = videoFrame.sourceType == VideoFrame.SourceType.kFrontCamera
                                    true
                                }
                            }
                        }

                        override fun onPreEncodeVideoFrame(
                            type: Int,
                            videoFrame: VideoFrame?
                        ): Boolean = false

                        override fun onMediaPlayerVideoFrame(
                            videoFrame: VideoFrame?,
                            mediaPlayerId: Int
                        ): Boolean = false

                        override fun onRenderVideoFrame(
                            channelId: String?,
                            uid: Int,
                            videoFrame: VideoFrame?
                        ): Boolean = false

                        override fun getVideoFrameProcessMode(): Int = IVideoFrameObserver.PROCESS_MODE_READ_WRITE

                        override fun getVideoFormatPreference(): Int = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

                        override fun getRotationApplied(): Boolean = false

                        override fun getMirrorApplied(): Boolean = shouldMirror

                        override fun getObservedFramePosition(): Int = IVideoFrameObserver.POSITION_POST_CAPTURER

                    })
                    enableVideo()
                }
            }
            return innerRtcEngine!!
        }

    private var innerVideoSwitcher: VideoSwitcher? = null
    val videoSwitcher: VideoSwitcher
        get() {
            if (innerVideoSwitcher == null) {
                innerVideoSwitcher = VideoSwitcherImpl(rtcEngine)
            }
            return innerVideoSwitcher!!
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
        innerBeautyProcessor?.let { processor ->
            processor.release()
            innerBeautyProcessor = null
        }
        innerSenseTimeApi?.release()
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