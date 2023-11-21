package io.agora.scene.show.beauty

import android.app.Application
import android.content.Context
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.faceunity.core.faceunity.FURenderKit
import io.agora.beautyapi.bytedance.ByteDanceBeautyAPI
import io.agora.beautyapi.bytedance.EventCallback
import io.agora.beautyapi.bytedance.createByteDanceBeautyAPI
import io.agora.beautyapi.faceunity.FaceUnityBeautyAPI
import io.agora.beautyapi.faceunity.createFaceUnityBeautyAPI
import io.agora.beautyapi.sensetime.Config
import io.agora.beautyapi.sensetime.STHandlers
import io.agora.beautyapi.sensetime.SenseTimeBeautyAPI
import io.agora.beautyapi.sensetime.createSenseTimeBeautyAPI
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.show.R
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

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

    // 美颜类型
    var beautyType = BeautyType.SenseTime
        set(value) {
            if (field == value) {
                when (value) {
                    BeautyType.SenseTime -> senseTimeBeautyAPI?.let { return }
                    BeautyType.FaceUnity -> faceUnityBeautyAPI?.let { return }
                    BeautyType.ByteDance -> byteDanceBeautyAPI?.let { return }
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
    }

    fun setupLocalVideo(view: View, renderMode: Int) {
        this.videoView = WeakReference(view)
        this.renderMode = renderMode
        when (beautyType) {
            BeautyType.SenseTime -> senseTimeBeautyAPI?.setupLocalVideo(view, renderMode)
            BeautyType.FaceUnity -> faceUnityBeautyAPI?.setupLocalVideo(view, renderMode)
            BeautyType.ByteDance -> byteDanceBeautyAPI?.setupLocalVideo(view, renderMode)
            BeautyType.Agora -> rtcEngine?.setupLocalVideo(VideoCanvas(view, renderMode))
        }
    }

    fun destroy() {
        videoView?.get()?.let {
            rtcEngine?.setupLocalVideo(VideoCanvas(null))
        }
        context = null
        rtcEngine = null
        videoView = null
        destroyBeauty(beautyType)
    }


    private fun switchBeauty(oldType: BeautyType, newType: BeautyType) {
        val rtc = rtcEngine ?: return

        videoView?.get()?.let {
            rtc.setupLocalVideo(VideoCanvas(null))
        }

        destroyBeauty(oldType)
        createBeauty(newType)
    }

    private fun createBeauty(type: BeautyType) {
        val ctx = context ?: return
        val rtc = rtcEngine ?: return
        workerExecutor.execute {
            when (type) {
                BeautyType.SenseTime -> {
                    if (SenseTimeBeautySDK.initBeautySDK(ctx)) {
                        senseTimeBeautyAPI = createSenseTimeBeautyAPI()
                        senseTimeBeautyAPI?.initialize(
                            Config(
                                ctx,
                                rtc,
                                STHandlers(
                                    SenseTimeBeautySDK.mobileEffectNative,
                                    SenseTimeBeautySDK.humanActionNative
                                )
                            )
                        )
                        senseTimeBeautyAPI?.enable(enable)
                        SenseTimeBeautySDK.setBeautyAPI(senseTimeBeautyAPI!!)
                        mainExecutor.post {
                            videoView?.get()?.let {
                                senseTimeBeautyAPI?.setupLocalVideo(it, renderMode)
                            }
                        }

                    } else {
                        mainExecutor.post {
                            Toast.makeText(
                                ctx,
                                R.string.show_beauty_license_senesetime,
                                Toast.LENGTH_LONG
                            ).show()
                            videoView?.get()?.let {
                                rtc.setupLocalVideo(VideoCanvas(it))
                            }
                        }
                    }
                }

                BeautyType.FaceUnity -> {
                    if (FaceUnityBeautySDK.initBeauty(ctx)) {
                        faceUnityBeautyAPI = createFaceUnityBeautyAPI()
                        faceUnityBeautyAPI?.initialize(
                            io.agora.beautyapi.faceunity.Config(
                                ctx,
                                rtc,
                                FURenderKit.getInstance()
                            )
                        )
                        faceUnityBeautyAPI?.enable(enable)
                        FaceUnityBeautySDK.setBeautyAPI(faceUnityBeautyAPI!!)
                        mainExecutor.post {
                            videoView?.get()?.let {
                                faceUnityBeautyAPI?.setupLocalVideo(it, renderMode)
                            }
                        }
                    } else {
                        mainExecutor.post {
                            Toast.makeText(
                                ctx,
                                R.string.show_beauty_license_faceunity,
                                Toast.LENGTH_LONG
                            ).show()
                            videoView?.get()?.let {
                                rtc.setupLocalVideo(VideoCanvas(it))
                            }
                        }
                    }
                }

                BeautyType.ByteDance -> {
                    if (ByteDanceBeautySDK.initBeautySDK(ctx)) {
                        byteDanceBeautyAPI = createByteDanceBeautyAPI()
                        byteDanceBeautyAPI?.initialize(
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
                                )
                            )
                        )
                        byteDanceBeautyAPI?.enable(enable)
                        ByteDanceBeautySDK.setBeautyAPI(byteDanceBeautyAPI!!)
                        mainExecutor.post {
                            videoView?.get()?.let {
                                byteDanceBeautyAPI?.setupLocalVideo(it, renderMode)
                            }
                        }

                    } else {
                        mainExecutor.post {
                            Toast.makeText(
                                ctx,
                                R.string.show_beauty_license_bytedance,
                                Toast.LENGTH_LONG
                            ).show()
                            videoView?.get()?.let {
                                rtc.setupLocalVideo(VideoCanvas(it, renderMode))
                            }
                        }
                    }
                }

                BeautyType.Agora -> {
                    AgoraBeautySDK.initBeautySDK(rtc)
                    AgoraBeautySDK.enable(enable)
                    mainExecutor.post {
                        videoView?.get()?.let {
                            rtc.setupLocalVideo(VideoCanvas(it, renderMode))
                        }
                    }

                }
            }
        }
    }

    private fun destroyBeauty(type: BeautyType) {
        when (type) {
            BeautyType.SenseTime ->
                senseTimeBeautyAPI?.let {
                    it.release()
                    senseTimeBeautyAPI = null
                    workerExecutor.execute {
                        SenseTimeBeautySDK.unInitBeautySDK()
                    }
                }

            BeautyType.FaceUnity ->
                faceUnityBeautyAPI?.let {
                    it.release()
                    faceUnityBeautyAPI = null
                    workerExecutor.execute {
                        FaceUnityBeautySDK.unInitBeauty()
                    }
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

}