package io.agora.voice.rtckit.open.event

/**
 * @author create by zhangwei03
 *
 * 音频操作事件
 */
sealed class RtcAudioEvent {

    /**音频启动/停止*/
    class AudioEnable constructor(val enabled: Boolean) : RtcAudioEvent()

    /**本地音频静音/开启*/
    class AudioMuteLocal constructor(val mute: Boolean) : RtcAudioEvent()

    /**远程音频静音/开启*/
    class AudioMuteRemote constructor(val userId: String, val mute: Boolean) : RtcAudioEvent()

    /**所有音频静音/开启*/
    class AudioMuteAll constructor(val userId: String, val mute: Boolean) : RtcAudioEvent()
}