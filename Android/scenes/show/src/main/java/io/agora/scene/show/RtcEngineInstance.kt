package io.agora.scene.show

import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.beauty.IBeautyProcessor
import io.agora.scene.show.beauty.sensetime.BeautySenseTimeImpl

object RtcEngineInstance {

    val videoEncoderConfiguration = VideoEncoderConfiguration()
    val virtualBackgroundSource = VirtualBackgroundSource().apply {
        backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR
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
                        ToastUtils.showToast(
                            "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err)
                        )
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    registerVideoFrameObserver(beautyProcessor)
                    enableVideo()
                }
            }
            return innerRtcEngine!!
        }

    fun destroy() {
        innerRtcEngine?.let {
            RtcEngine.destroy()
            innerRtcEngine = null
        }
        innerBeautyProcessor?.let { processor->
            processor.release()
            innerBeautyProcessor = null
        }
    }
}