package io.agora.voice.rtckit.internal

import io.agora.voice.buddy.config.ConfigConstants
import io.agora.voice.rtckit.open.status.RtcAudioChangeStatus
import io.agora.voice.rtckit.open.status.RtcAudioVolumeIndicationStatus
import io.agora.voice.rtckit.open.status.RtcErrorStatus
import io.agora.voice.rtckit.open.status.RtcNetWorkStatus

/**
 * @author create by zhangwei03
 *
 * rtc 监听
 */
interface IRtcClientListener {

    /**加入房间*/
    fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int)

    /**离开房间*/
    fun onLeaveChannel()

    /**网络情况*/
    fun onConnectionStateChanged(state: Int, reason: Int)

    /**用户进入/离开 rtc 房间*/
    fun onUserJoined(userId: Int, joined: Boolean)

    /**网络等状态，自己*/
    fun onNetworkStatus(netWorkStatus: RtcNetWorkStatus)

    /**音频状态：本地静音，远程静音等*/
    fun onAudioStatus(audioChangeStatus: RtcAudioChangeStatus)

    /**当音效文件开始播放触发该回调*/
    fun onAudioMixingFinished(
        soundId: Int = -1,
        finished: Boolean = true,
        speakerType: Int = ConfigConstants.BotSpeaker.BotBoth
    )

    /**当播放器开始播放触发该回调*/
    fun onMediaPlayerFinished(finished: Boolean = true, speakerType: Int = ConfigConstants.BotSpeaker.BotBoth)

    /**错误回调*/
    fun onError(rtcErrorStatus: RtcErrorStatus)

    /**用户音量提示回调*/
    fun onAudioVolumeIndication(volumeIndicationStatus: RtcAudioVolumeIndicationStatus)
}