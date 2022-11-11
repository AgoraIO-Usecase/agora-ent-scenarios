package io.agora.voice.rtckit.internal.impl

import io.agora.rtc2.Constants
import io.agora.voice.rtckit.internal.base.RtcBaseAudioEngine
import io.agora.rtc2.RtcEngineEx
import io.agora.voice.rtckit.open.status.RtcAudioChangeStatus

/**
 * @author create by zhangwei03
 *
 * agora 音频管理引擎
 */
internal class AgoraAudioEngine : RtcBaseAudioEngine<RtcEngineEx>() {

    override fun enableLocalAudio(enabled: Boolean): Boolean {
        val isSuccess = isSuccess(engine?.enableLocalAudio(enabled))
        listener?.onAudioStatus(RtcAudioChangeStatus.LocalAudio("", true))
        return isSuccess
    }

    override fun muteLocalAudio(mute: Boolean): Boolean {
        val isSuccess = isSuccess(engine?.muteLocalAudioStream(mute))
        listener?.onAudioStatus(RtcAudioChangeStatus.MutedAudio("", mute))
        return isSuccess
    }

    override fun muteRemoteAudio(uid: String, mute: Boolean): Boolean {
        val isSuccess = isSuccess(engine?.muteRemoteAudioStream(uid.toIntOrNull() ?: -1, mute))
        listener?.onAudioStatus(RtcAudioChangeStatus.RemoteAudio(uid, mute))
        return isSuccess
    }

    override fun muteRemoteAllAudio(mute: Boolean): Boolean {
        val isSuccess = isSuccess(engine?.muteAllRemoteAudioStreams(mute))
        listener?.onAudioStatus(RtcAudioChangeStatus.RemoteAudio("", mute))
        return isSuccess
    }

    private fun isSuccess(code: Int?): Boolean {
        return code == Constants.ERR_OK
    }
}