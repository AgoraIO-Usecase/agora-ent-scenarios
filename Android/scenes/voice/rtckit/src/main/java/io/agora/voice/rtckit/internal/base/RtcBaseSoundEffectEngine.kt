package io.agora.voice.rtckit.internal.base

import io.agora.voice.buddy.config.ConfigConstants

/**
 * @author create by zhangwei03
 *
 * 音效管理引擎
 */
internal abstract class RtcBaseSoundEffectEngine<T> : RtcBaseEngine<T>() {

    /**
     * @param soundSpeakerType  机器人播放类型
     * @param soundId 唯一标识
     * @param filePath String 需要进行混音的音乐文件路径。
     * @param loopBack 是否只在本地客户端播放音乐文件
     * @param cycle 音乐文件的播放次数。设为 -1 表示循环播放。
     */
    abstract fun playEffect(
        soundId: Int,
        filePath: String,
        loopBack: Boolean,
        cycle: Int,
        soundSpeakerType: Int = ConfigConstants.BotSpeaker.BotBlue,
    ): Boolean

    abstract fun stopEffect(soundId: Int): Boolean

    abstract fun pauseEffect(soundId: Int): Boolean

    abstract fun resumeEffect(soundId: Int): Boolean

    abstract fun stopAllEffect(): Boolean

    abstract fun updateEffectVolume(volume: Int): Boolean
}