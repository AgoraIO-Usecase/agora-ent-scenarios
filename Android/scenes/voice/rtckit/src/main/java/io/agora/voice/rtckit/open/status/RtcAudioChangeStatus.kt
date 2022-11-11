package io.agora.voice.rtckit.open.status

/**
 * @author create by zhangwei03
 *
 * 音频监听-音频状态改变
 */
sealed class RtcAudioChangeStatus constructor(
    var userId: String = "",
    var enabled: Boolean = false
) {

    /** 本地状态 */
    class LocalAudio(userId: String, enabled: Boolean) : RtcAudioChangeStatus(userId, enabled)

    /** mute状态  */
    class MutedAudio(userId: String, enabled: Boolean) : RtcAudioChangeStatus(userId, enabled)

    /** 远端状态 */
    class RemoteAudio(userId: String, enabled: Boolean) : RtcAudioChangeStatus(userId, enabled)
}
