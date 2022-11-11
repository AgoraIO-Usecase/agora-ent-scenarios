package io.agora.voice.rtckit.internal.base

/**
 * @author create by zhangwei03
 *
 * 音频管理引擎
 */
internal abstract class RtcBaseAudioEngine<T> : RtcBaseEngine<T>() {

    /**启动/停止audio*/
    abstract fun enableLocalAudio(enabled: Boolean): Boolean

    /**禁本地audio*/
    abstract fun muteLocalAudio(mute: Boolean): Boolean

    /**禁远程audio*/
    abstract fun muteRemoteAudio(uid: String, mute: Boolean): Boolean

    abstract fun muteRemoteAllAudio(mute: Boolean): Boolean
}