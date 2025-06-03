package io.agora.scene.voice.spatial.rtckit.listener

/**
 * @author create by zhangwei03
 *
 * Mic volume listener
 */
abstract class RtcMicVolumeListener {

    /**
     * Simulate robot volume display
     */
    abstract fun onBotVolume(speaker: Int, finished: Boolean)

    /**
     * User voice volume
     */
    abstract fun onUserVolume(rtcUid: Int, volume: Int)
}