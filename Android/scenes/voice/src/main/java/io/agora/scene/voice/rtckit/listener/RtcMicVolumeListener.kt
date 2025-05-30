package io.agora.scene.voice.rtckit.listener

/**
 * @author create by zhangwei03
 *
 * Mic position volume listener
 */
abstract class RtcMicVolumeListener {

    /**
     * Simulate robot volume display
     */
    abstract fun onBotVolume(speaker: Int, finished: Boolean)

    /**
     * User voice chat volume
     */
    abstract fun onUserVolume(rtcUid: Int, volume: Int)
}