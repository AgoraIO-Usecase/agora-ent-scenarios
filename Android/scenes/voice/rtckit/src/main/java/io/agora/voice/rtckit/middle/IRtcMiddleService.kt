package io.agora.voice.rtckit.middle

import io.agora.voice.rtckit.open.IRtcValueCallback
import io.agora.voice.rtckit.open.config.RtcChannelConfig
import io.agora.voice.rtckit.open.event.*

/**
 * @author create by zhangwei03
 *
 * rtc 中间层服务监听
 */
interface IRtcMiddleService {

    fun joinChannel(config: RtcChannelConfig)

    fun leaveChannel()

    fun switchRole(broadcaster:Boolean)

    /**处理音频事件*/
    fun onAudioEvent(audioEvent: RtcAudioEvent)

    /**处理最佳音效事件*/
    fun onSoundEffectEvent(soundEffect: RtcSoundEffectEvent)

    /**处理AI 降噪事件*/
    fun onDeNoiseEvent(deNoiseEvent: RtcDeNoiseEvent,callback: IRtcValueCallback<Boolean>?)

    /**处理空间音频事件*/
    fun onSpatialAudioEvent(spatialAudioEvent: RtcSpatialAudioEvent)

    /**处理Media 事件*/
    fun onMediaPlayer(mediaPlayerEvent: MediaPlayerEvent)

    fun destroy()
}