package io.agora.voice.rtckit.open.status

/**
 * @author create by zhangwei03
 *
 * 用户音量提示回调。
 */
data class RtcAudioVolumeIndicationStatus constructor(
    val speakers: List<RtcAudioVolumeInfo>? = null,
)

data class RtcAudioVolumeInfo constructor(
    var uid: Int = 0,
    var volume: Int = 0
)