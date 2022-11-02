package io.agora.voice.rtckit.internal.base

/**
 * @author create by zhangwei03
 *
 * 音频管理引擎
 */
internal abstract class BaseMediaPlayerEngine<T> : RtcBaseEngine<T>() {

    abstract fun adjustPlayoutVolume(volume: Int): Boolean

    abstract fun open(url: String, startPos: Long, soundSpeakerType: Int): Boolean

    abstract fun play(): Boolean

    abstract fun pause(): Boolean

    abstract fun stop(): Boolean

    abstract fun resume(): Boolean

    abstract fun reset(): Boolean

    abstract fun destroy(): Boolean
}