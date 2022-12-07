package io.agora.scene.show

import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.beauty.IBeautyProcessor
import io.agora.scene.show.beauty.bytedance.BeautyByteDanceImpl

object RtcEngineInstance {

    val videoEncoderConfiguration = VideoEncoderConfiguration()

    val beautyProcessor: IBeautyProcessor by lazy {
        BeautyByteDanceImpl(AgoraApplication.the())
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

    fun destroy(){
        innerRtcEngine?: return
        RtcEngine.destroy()
        innerRtcEngine = null
    }
}