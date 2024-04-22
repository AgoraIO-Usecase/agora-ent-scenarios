package io.agora.scene.show.beauty

import android.app.Application
import android.content.Context
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.faceunity.core.faceunity.FURenderKit
import io.agora.base.VideoFrame
import io.agora.beautyapi.bytedance.ByteDanceBeautyAPI
import io.agora.beautyapi.bytedance.EventCallback
import io.agora.beautyapi.bytedance.createByteDanceBeautyAPI
import io.agora.beautyapi.faceunity.FaceUnityBeautyAPI
import io.agora.beautyapi.faceunity.createFaceUnityBeautyAPI
import io.agora.beautyapi.sensetime.BeautyStats
import io.agora.beautyapi.sensetime.CaptureMode
import io.agora.beautyapi.sensetime.Config
import io.agora.beautyapi.sensetime.IEventCallback
import io.agora.beautyapi.sensetime.STHandlers
import io.agora.beautyapi.sensetime.SenseTimeBeautyAPI
import io.agora.beautyapi.sensetime.createSenseTimeBeautyAPI
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.IVideoFrameObserver
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.show.R
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future

object BeautyManager {

    private var context: Application? = null
    private var rtcEngine: RtcEngine? = null
    private var senseTimeBeautyAPI: SenseTimeBeautyAPI? = null
    private var faceUnityBeautyAPI: FaceUnityBeautyAPI? = null
    private var byteDanceBeautyAPI: ByteDanceBeautyAPI? = null

    private var videoView: WeakReference<View>? = null
    private var renderMode: Int = Constants.RENDER_MODE_HIDDEN

    private val workerExecutor = Executors.newSingleThreadExecutor()
    private val mainExecutor = android.os.Handler(Looper.getMainLooper())
    private var createBeautyFuture: Future<*>? = null
    private var destroyBeautyFuture: Future<*>? = null

    // 美颜类型
    var beautyType = BeautyType.SenseTime
        set(value) {
            if (field == value) {
                when (value) {
                    BeautyType.SenseTime -> senseTimeBeautyAPI?.let { return }
                    BeautyType.FaceUnity -> faceUnityBeautyAPI?.let { return }
                    BeautyType.ByteDance -> byteDanceBeautyAPI?.let { return }
                    BeautyType.Agora -> return
                }
            }
            val oldType = field
            field = value
            switchBeauty(oldType, value)
        }


    // 美颜开关
    var enable = false
        set(value) {
            field = value
            when (beautyType) {
                BeautyType.SenseTime -> senseTimeBeautyAPI?.enable(value)
                BeautyType.FaceUnity -> faceUnityBeautyAPI?.enable(value)
                BeautyType.ByteDance -> byteDanceBeautyAPI?.enable(value)
                BeautyType.Agora -> AgoraBeautySDK.enable(value)
            }
        }


    fun initialize(context: Context, rtcEngine: RtcEngine) {
        this.context = context.applicationContext as Application
        this.rtcEngine = rtcEngine
        this.beautyType = BeautyType.SenseTime
        this.enable = rtcEngine.queryDeviceScore() >= 75
        rtcEngine.registerVideoFrameObserver(MultiBeautyVideoObserver())
    }

    fun setupLocalVideo(view: View, renderMode: Int) {
        mainExecutor.post {
            this.videoView = WeakReference(view)
            this.renderMode = renderMode
            when (beautyType) {
                BeautyType.SenseTime -> senseTimeBeautyAPI?.setupLocalVideo(view, renderMode)
                BeautyType.FaceUnity -> faceUnityBeautyAPI?.setupLocalVideo(view, renderMode)
                BeautyType.ByteDance -> byteDanceBeautyAPI?.setupLocalVideo(view, renderMode)
                BeautyType.Agora -> rtcEngine?.setupLocalVideo(
                    VideoCanvas(
                        view,
                        renderMode,
                        0
                    ).apply {
                        mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
                    }
                )
            }
        }
    }

    fun destroy() {
        rtcEngine?.registerVideoFrameObserver(null)
        mainExecutor.post {
            videoView?.get()?.let {
                rtcEngine?.setupLocalVideo(VideoCanvas(null))
            }
            videoView = null
        }
        context = null
        rtcEngine = null
        destroyBeauty(beautyType)
    }


    private fun switchBeauty(oldType: BeautyType, newType: BeautyType) {
        createBeautyFuture?.cancel(true)
        destroyBeautyFuture?.cancel(true)

        destroyBeautyFuture = destroyBeauty(oldType)
        createBeautyFuture = createBeauty(newType)

    }

    private fun createBeauty(type: BeautyType) =
        workerExecutor.submit {
            val ctx = context ?: return@submit
            val rtc = rtcEngine ?: return@submit

            val setupLocalVideoCountDownLatch = CountDownLatch(1)
            when (type) {
                BeautyType.SenseTime -> {
                    if (SenseTimeBeautySDK.initBeautySDK(ctx)) {
                        val senseTimeBeautyAPI = createSenseTimeBeautyAPI()
                        senseTimeBeautyAPI.initialize(
                            Config(
                                ctx,
                                rtc,
                                captureMode = CaptureMode.Custom,
                                eventCallback = object :IEventCallback{
                                    override fun onBeautyStats(stats: BeautyStats) {
                                    }

                                    override fun onEffectInitialized(): STHandlers {
                                        SenseTimeBeautySDK.initMobileEffect(ctx)
                                        SenseTimeBeautySDK.beautyConfig.resume()
                                        return STHandlers(
                                            SenseTimeBeautySDK.mobileEffectNative,
                                            SenseTimeBeautySDK.humanActionNative
                                        )
                                    }

                                    override fun onEffectDestroyed() {
                                        SenseTimeBeautySDK.unInitMobileEffect()
                                    }

                                }
                            )
                        )
                        senseTimeBeautyAPI.enable(enable)
                        SenseTimeBeautySDK.setBeautyAPI(senseTimeBeautyAPI)
                        this.senseTimeBeautyAPI = senseTimeBeautyAPI
                        mainExecutor.post {
                            videoView?.get()?.let {
                                senseTimeBeautyAPI.setupLocalVideo(it, renderMode)
                            }
                            setupLocalVideoCountDownLatch.countDown()
                        }

                    } else {
                        mainExecutor.post {
                            Toast.makeText(
                                ctx,
                                R.string.show_beauty_license_senesetime,
                                Toast.LENGTH_LONG
                            ).show()
                            videoView?.get()?.let {
                                rtc.setupLocalVideo(
                                    VideoCanvas(
                                        it,
                                        renderMode,
                                        0
                                    ).apply {
                                        mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
                                    }
                                )
                            }
                            setupLocalVideoCountDownLatch.countDown()
                        }
                    }
                }

                BeautyType.FaceUnity -> {
                    if (FaceUnityBeautySDK.initBeauty(ctx)) {
                        val faceUnityBeautyAPI = createFaceUnityBeautyAPI()
                        faceUnityBeautyAPI.initialize(
                            io.agora.beautyapi.faceunity.Config(
                                ctx,
                                rtc,
                                FURenderKit.getInstance(),
                                captureMode = io.agora.beautyapi.faceunity.CaptureMode.Custom
                            )
                        )
                        faceUnityBeautyAPI.enable(enable)
                        FaceUnityBeautySDK.setBeautyAPI(faceUnityBeautyAPI)
                        this.faceUnityBeautyAPI = faceUnityBeautyAPI
                        mainExecutor.post {
                            videoView?.get()?.let {
                                faceUnityBeautyAPI.setupLocalVideo(it, renderMode)
                            }
                            setupLocalVideoCountDownLatch.countDown()
                        }
                    } else {
                        mainExecutor.post {
                            Toast.makeText(
                                ctx,
                                R.string.show_beauty_license_faceunity,
                                Toast.LENGTH_LONG
                            ).show()
                            videoView?.get()?.let {
                                rtc.setupLocalVideo(
                                    VideoCanvas(
                                        it,
                                        renderMode,
                                        0
                                    ).apply {
                                        mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
                                    }
                                )
                            }
                            setupLocalVideoCountDownLatch.countDown()
                        }
                    }
                }

                BeautyType.ByteDance -> {
                    if (ByteDanceBeautySDK.initBeautySDK(ctx)) {
                        val byteDanceBeautyAPI = createByteDanceBeautyAPI()
                        byteDanceBeautyAPI.initialize(
                            io.agora.beautyapi.bytedance.Config(
                                ctx,
                                rtc,
                                ByteDanceBeautySDK.renderManager,
                                EventCallback(
                                    onEffectInitialized = {
                                        ByteDanceBeautySDK.initEffect(ctx)
                                    },
                                    onEffectDestroyed = {
                                        ByteDanceBeautySDK.unInitEffect()
                                    }
                                ),
                                captureMode = io.agora.beautyapi.bytedance.CaptureMode.Custom
                            )
                        )
                        byteDanceBeautyAPI.enable(enable)
                        ByteDanceBeautySDK.setBeautyAPI(byteDanceBeautyAPI)
                        this.byteDanceBeautyAPI = byteDanceBeautyAPI
                        mainExecutor.post {
                            videoView?.get()?.let {
                                byteDanceBeautyAPI.setupLocalVideo(it, renderMode)
                            }
                            setupLocalVideoCountDownLatch.countDown()
                        }

                    } else {
                        mainExecutor.post {
                            Toast.makeText(
                                ctx,
                                R.string.show_beauty_license_bytedance,
                                Toast.LENGTH_LONG
                            ).show()
                            videoView?.get()?.let {
                                rtc.setupLocalVideo(
                                    VideoCanvas(
                                        it,
                                        renderMode,
                                        0
                                    ).apply {
                                        mirrorMode = Constants.VIDEO_MIRROR_MODE_AUTO
                                    }
                                )
                            }
                            setupLocalVideoCountDownLatch.countDown()
                        }
                    }
                }

                BeautyType.Agora -> {
                    AgoraBeautySDK.initBeautySDK(rtc)
                    AgoraBeautySDK.enable(enable)
                    mainExecutor.postDelayed({
                        videoView?.get()?.let {
                            rtc.setupLocalVideo(
                                VideoCanvas(
                                    it,
                                    renderMode,
                                    0
                                ).apply {
                                    mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
                                }
                            )
                        }
                        setupLocalVideoCountDownLatch.countDown()
                    }, 140)
                }
            }
            setupLocalVideoCountDownLatch.await()
        }


    private fun destroyBeauty(type: BeautyType) =
        workerExecutor.submit {
            val setupLocalVideoCountDownLatch = CountDownLatch(1)

            mainExecutor.post {
                videoView?.get()?.let {
                    rtcEngine?.setupLocalVideo(VideoCanvas(null))
                }
                setupLocalVideoCountDownLatch.countDown()
            }

            setupLocalVideoCountDownLatch.await()

            when (type) {
                BeautyType.SenseTime ->
                    senseTimeBeautyAPI?.let {
                        it.release()
                        senseTimeBeautyAPI = null
                        SenseTimeBeautySDK.unInitBeautySDK()
                    }

                BeautyType.FaceUnity ->
                    faceUnityBeautyAPI?.let {
                        it.release()
                        faceUnityBeautyAPI = null
                        FaceUnityBeautySDK.unInitBeauty()
                    }

                BeautyType.ByteDance ->
                    byteDanceBeautyAPI?.let {
                        it.release()
                        byteDanceBeautyAPI = null
                    }

                BeautyType.Agora ->
                    AgoraBeautySDK.unInitBeautySDK()
            }
        }


    enum class BeautyType {
        SenseTime,
        FaceUnity,
        ByteDance,
        Agora
    }


    class MultiBeautyVideoObserver : IVideoFrameObserver {
        private var isFront = true
        override fun onCaptureVideoFrame(type: Int, videoFrame: VideoFrame?): Boolean {
            if (createBeautyFuture?.isDone != true) {
                return false
            }
            val frame = videoFrame ?: return false
            val oIsFront = isFront
            isFront = frame.sourceType == VideoFrame.SourceType.kFrontCamera

            when (beautyType) {
                BeautyType.SenseTime -> {
                    return when (senseTimeBeautyAPI?.onFrame(frame)) {
                        io.agora.beautyapi.sensetime.ErrorCode.ERROR_FRAME_SKIPPED.value -> false
                        else -> true
                    }
                }

                BeautyType.FaceUnity -> {
                    return when (faceUnityBeautyAPI?.onFrame(frame)) {
                        io.agora.beautyapi.faceunity.ErrorCode.ERROR_FRAME_SKIPPED.value -> false
                        else -> true
                    }
                }

                BeautyType.ByteDance -> {
                    return when (byteDanceBeautyAPI?.onFrame(frame)) {
                        io.agora.beautyapi.bytedance.ErrorCode.ERROR_FRAME_SKIPPED.value -> false
                        else -> true
                    }
                }

                BeautyType.Agora -> return oIsFront == isFront
            }
        }

        override fun onPreEncodeVideoFrame(type: Int, videoFrame: VideoFrame?) = true

        override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame?, mediaPlayerId: Int) = true

        override fun onRenderVideoFrame(
            channelId: String?,
            uid: Int,
            videoFrame: VideoFrame?
        ) = true

        override fun getVideoFrameProcessMode() = IVideoFrameObserver.PROCESS_MODE_READ_WRITE

        override fun getVideoFormatPreference() = IVideoFrameObserver.VIDEO_PIXEL_DEFAULT

        override fun getRotationApplied() = false

        override fun getMirrorApplied(): Boolean {
            return when (beautyType) {
                BeautyType.SenseTime -> senseTimeBeautyAPI?.getMirrorApplied() ?: false
                BeautyType.FaceUnity -> faceUnityBeautyAPI?.getMirrorApplied() ?: false
                BeautyType.ByteDance -> byteDanceBeautyAPI?.getMirrorApplied() ?: false
                BeautyType.Agora -> isFront
            }
        }

        override fun getObservedFramePosition() = IVideoFrameObserver.POSITION_POST_CAPTURER

    }
}