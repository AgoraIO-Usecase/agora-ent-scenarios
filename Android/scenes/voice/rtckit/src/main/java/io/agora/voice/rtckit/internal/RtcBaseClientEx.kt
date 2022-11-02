package io.agora.voice.rtckit.internal

import android.content.Context
import io.agora.voice.rtckit.open.config.RtcInitConfig
import io.agora.voice.rtckit.constants.RtcKitConstant
import io.agora.voice.rtckit.internal.base.*
import io.agora.voice.rtckit.open.config.RtcChannelConfig

internal abstract class RtcBaseClientEx<T> {

    companion object {
        const val TAG = RtcKitConstant.RTC_TAG
    }

    protected var rtcEngine: T? = null
    protected var rtcListener: IRtcClientListener? = null

    private val engineMap = HashMap<Class<*>, RtcBaseEngine<*>?>()

    /**创建rtc*/
    abstract fun createClient(context: Context, config: RtcInitConfig, rtcClientListener: IRtcClientListener): T?

    abstract fun joinChannel(config: RtcChannelConfig)

    abstract fun leaveChannel()

    abstract fun switchRole(broadcaster: Boolean)

    /**创建音频管理引擎*/
    abstract fun createAudioEngine(): RtcBaseAudioEngine<T>?

    /**创建AI降噪管理引擎*/
    abstract fun createDeNoiseEngine(): RtcBaseDeNoiseEngine<T>?

    /**创建音效管理引擎*/
    abstract fun createSoundEffectEngine(): RtcBaseSoundEffectEngine<T>?

    /**创建空间音频管理引擎*/
    abstract fun createSpatialAudioEngine(): RtcBaseSpatialAudioEngine<T>?

    /**创建播放器管理引擎*/
    abstract fun createMediaPlayerEngine(): BaseMediaPlayerEngine<T>?

    fun getRtcEngineEx(): T? {
        return rtcEngine
    }

    fun getAudioEngine(): RtcBaseAudioEngine<T>? {
        var engine = engineMap[RtcBaseAudioEngine::class.java]
        if (engine == null) {
            engine = createAudioEngine()
            engine?.attach(rtcEngine, rtcListener)
            engineMap[RtcBaseAudioEngine::class.java] = engine
        }
        return engine as RtcBaseAudioEngine<T>?
    }

    fun getDeNoiseEngine(): RtcBaseDeNoiseEngine<T>? {
        var engine = engineMap[RtcBaseDeNoiseEngine::class.java]
        if (engine == null) {
            engine = createDeNoiseEngine()
            engine?.attach(rtcEngine, rtcListener)
            engineMap[RtcBaseDeNoiseEngine::class.java] = engine
        }
        return engine as RtcBaseDeNoiseEngine<T>?
    }

    fun getSoundEffectEngine(): RtcBaseSoundEffectEngine<T>? {
        var engine = engineMap[RtcBaseSoundEffectEngine::class.java]
        if (engine == null) {
            engine = createSoundEffectEngine()
            engine?.attach(rtcEngine, rtcListener)
            engineMap[RtcBaseSoundEffectEngine::class.java] = engine
        }
        return engine as RtcBaseSoundEffectEngine<T>?
    }

    fun getSpatialAudioEngine(): RtcBaseSpatialAudioEngine<T>? {
        var engine = engineMap[RtcBaseSpatialAudioEngine::class.java]
        if (engine == null) {
            engine = createSpatialAudioEngine()
            engine?.attach(rtcEngine, rtcListener)
            engineMap[RtcBaseSpatialAudioEngine::class.java] = engine
        }
        return engine as RtcBaseSpatialAudioEngine<T>?
    }

    fun getMediaPlayerEngine(): BaseMediaPlayerEngine<T>? {
        var engine = engineMap[BaseMediaPlayerEngine::class.java]
        if (engine == null) {
            engine = createMediaPlayerEngine()
            engine?.attach(rtcEngine, rtcListener)
            engineMap[BaseMediaPlayerEngine::class.java] = engine
        }
        return engine as BaseMediaPlayerEngine<T>?
    }

    open fun destroy() {
        this.rtcEngine = null
        engineMap.forEach {
            it.value?.detach()
        }
        engineMap.clear()
        this.rtcListener = null
    }
}