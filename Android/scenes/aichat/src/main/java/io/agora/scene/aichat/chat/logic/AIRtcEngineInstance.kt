package io.agora.scene.aichat.chat.logic

import io.agora.hy.extension.ExtensionManager
import io.agora.hyextension.AIChatAudioTextConvertorDelegate
import io.agora.hyextension.AIChatAudioTextConvertorService
import io.agora.hyextension.LanguageConvertType
import io.agora.rtc2.Constants
import io.agora.rtc2.IMediaExtensionObserver
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.aichat.AIChatCenter
import io.agora.scene.aichat.AILogger
import io.agora.scene.base.component.AgoraApplication
import java.util.concurrent.Executors

object AIRtcEngineInstance {

    private const val TAG = "AIRtcEngineInstance"

    private val mWorkingExecutor = Executors.newSingleThreadExecutor()

    private var innerRtcEngine: RtcEngineEx? = null

    var mAudioTextConvertorService: AIChatAudioTextConvertorService? = null

    val rtcEngine: RtcEngineEx
        get() {
            if (innerRtcEngine == null) {
                initRtcEngine()
            }
            return innerRtcEngine!!
        }

    private fun initRtcEngine() {
        if (innerRtcEngine != null) return
        val config = RtcEngineConfig()
        config.mContext = AgoraApplication.the()
        config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
        config.addExtension(ExtensionManager.EXTENSION_NAME)
        config.addExtension("agora_ai_echo_cancellation_extension")
        config.addExtension("agora_ai_noise_suppression_extension")
        config.mExtensionObserver = mMediaExtensionObserver
        //Name of dynamic link library is provided by plug-in vendor,
        //e.g. libagora-bytedance.so whose EXTENSION_NAME should be "agora-bytedance"
        //and one or more plug-ins can be added
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onError(err: Int) {
                super.onError(err)
                AILogger.d(TAG, "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)
                AILogger.d(TAG, "onJoinChannelSuccess: channel:$channel, uid:$uid")
                mAudioTextConvertorService?.startService(
                    AIChatCenter.mXFAppId,
                    AIChatCenter.mXFAppKey,
                    AIChatCenter.mXFAppSecret,
                    LanguageConvertType.NORMAL
                )
            }
        }
        innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
            enableExtension(
                ExtensionManager.EXTENSION_VENDOR_NAME,
                ExtensionManager.EXTENSION_AUDIO_FILTER_NAME, true
            )
            setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
            setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT)
            enableAudio()
            addParameters()
        }
    }

    fun addParameters(){
        innerRtcEngine?.apply {
            // 降噪
            setParameters("{\"che.audio.sf.enabled\":true}")
            setParameters("{\"che.audio.sf.ainlpToLoadFlag\":1}")
            setParameters("{\"che.audio.sf.nlpAlgRoute\":1}")
            setParameters("{\"che.audio.sf.ainsToLoadFlag\":1}")
            setParameters("{\"che.audio.sf.nsngAlgRoute\":12}")
            setParameters("{\"che.audio.sf.ainsModelPref\":11}")
            setParameters("{\"che.audio.sf.ainlpModelPref\":11}")
            setParameters("{\"che.audio.sf.nsngPredefAgg\":11}")

            // 标准音质
            setParameters("{\"che.audio.aec.split_srate_for_48k\": 16000}")
        }
    }

    fun reset(){
        innerRtcEngine?.let {
            it.leaveChannel()
            mWorkingExecutor.execute { RtcEngineEx.destroy() }
            innerRtcEngine = null
        }
    }

    private val mMediaExtensionObserver: IMediaExtensionObserver = object : IMediaExtensionObserver {
        override fun onEvent(provider: String, extension: String, key: String, value: String) {
            if (ExtensionManager.EXTENSION_VENDOR_NAME != provider || ExtensionManager.EXTENSION_AUDIO_FILTER_NAME != extension) {
                return
            }
            mAudioTextConvertorService?.onEvent(key, value)
        }

        override fun onStarted(provider: String, extension: String) {
            if (ExtensionManager.EXTENSION_VENDOR_NAME != provider || ExtensionManager.EXTENSION_AUDIO_FILTER_NAME != extension) {
                return
            }
            AILogger.d(TAG, "onStarted | provider: $provider, extension: $extension")
        }

        override fun onStopped(provider: String, extension: String) {
            if (ExtensionManager.EXTENSION_VENDOR_NAME != provider || ExtensionManager.EXTENSION_AUDIO_FILTER_NAME != extension) {
                return
            }
            AILogger.d(TAG, "onStopped | provider: $provider, extension: $extension")
        }

        override fun onError(provider: String, extension: String, errCode: Int, errMsg: String) {
            if (ExtensionManager.EXTENSION_VENDOR_NAME != provider || ExtensionManager.EXTENSION_AUDIO_FILTER_NAME != extension) {
                return
            }
            AILogger.e(TAG, "onError | provider: $provider, extension: $extension, errCode: $errCode, errMsg:$errMsg")
        }
    }

}