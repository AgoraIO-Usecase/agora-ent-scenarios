package io.agora.scene.voice.rtckit.listener

/**
 * @author create by zhangwei03
 *
 * 麦位音量监听
 */
abstract class RtcMicVolumeListener {

    /**
     * 模拟机器人音量显示
     */
    abstract fun onBotVolume(speaker: Int, finished: Boolean)

    /**
     * 用户语聊音量
     */
    abstract fun onUserVolume(rtcUid: Int, volume: Int)
}