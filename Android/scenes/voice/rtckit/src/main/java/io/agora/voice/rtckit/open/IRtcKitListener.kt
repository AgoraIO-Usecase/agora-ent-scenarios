package io.agora.voice.rtckit.open

import io.agora.voice.rtckit.open.status.*

/**
 * @author create by zhangwei03
 *
 * 管道监听
 */
interface IRtcKitListener {

    /**加入房间*/
    fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int)

    /**离开房间*/
    fun onLeaveChannel()

    /**网络情况*/
    fun onConnectionStateChanged(state: Int, reason: Int)

    /**网络等状态*/
    fun onNetworkStatus(netWorkStatus: RtcNetWorkStatus)

    /**音频状态：本地静音，远程静音等*/
    fun onAudioStatus(audioChangeStatus: RtcAudioChangeStatus)

    /**当音效文件播放开始/结束 后触发该回调*/
    fun onAudioMixingFinished(soundId: Int, finished: Boolean, speakerType: Int)

    /**用户进入rtc 房间*/
    fun onUserJoin(userId: Int)

    fun onLeaveChannel(userId: Int)

    /**错误回调*/
    fun onError(rtcErrorStatus: RtcErrorStatus)

    /**当MPK播放开始/结束 后触发该回调*/
    fun onMediaPlayerFinished( finished: Boolean, speakerType: Int)

    /**用户音量提示回调。*/
    fun onAudioVolumeIndication(volumeIndicationStatus: RtcAudioVolumeIndicationStatus)
}