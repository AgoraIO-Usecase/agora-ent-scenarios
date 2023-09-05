package io.agora.scene.show.videoSwitcherAPI

import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.scene.show.VideoSwitcher

interface VideoSwitcherAPI {

    data class IChannelEventListener(
        var onTokenGenerateFailedException: ((error: Throwable)->Unit)? = null,
        var onChannelJoined: ((connection: RtcConnection)->Unit)? = null,
        var onUserJoined: ((uid: Int) -> Unit)? = null,
        var onUserOffline: ((uid: Int) -> Unit)? = null,
        var onLocalVideoStateChanged: ((state: Int) -> Unit)? = null,
        var onRemoteVideoStateChanged: ((uid: Int, state: Int) -> Unit)? = null,
        var onRtcStats: ((stats: IRtcEngineEventHandler.RtcStats) -> Unit)? = null,
        var onLocalVideoStats: ((stats: IRtcEngineEventHandler.LocalVideoStats) -> Unit)? = null,
        var onRemoteVideoStats: ((stats: IRtcEngineEventHandler.RemoteVideoStats) -> Unit)? = null,
        var onLocalAudioStats: ((stats: IRtcEngineEventHandler.LocalAudioStats) -> Unit)? = null,
        var onRemoteAudioStats: ((stats: IRtcEngineEventHandler.RemoteAudioStats) -> Unit)? = null,
        var onUplinkNetworkInfoUpdated: ((info: IRtcEngineEventHandler.UplinkNetworkInfo) -> Unit)? = null,
        var onDownlinkNetworkInfoUpdated: ((info: IRtcEngineEventHandler.DownlinkNetworkInfo) -> Unit)? = null,
        var onContentInspectResult: ((result: Int) -> Unit)? = null,
        var onFirstRemoteVideoFrame: ((uid: Int, width: Int, height: Int, elapsed: Int)->Unit)? = null,
    )

    /**
     * 房间状态
     * @param IDLE 默认状态
     * @param PREJOINED 预加入房间状态
     * @param JOINED 已进入房间状态
     */
    enum class RoomStatus {
        IDLE,
        PREJOINED,
        JOINED,
    }

    /**
     * 房间信息
     * @param channelName 房间channelId
     * @param uid 加入者uid
     * @param token channelName/uid对应的token
     * @param eventHandler IChannelEventListener
     */
    data class RoomInfo(
        val channelName: String,
        val uid: Int,
        val token: String,
        val eventHandler: IChannelEventListener?
    )

    /**
     * 预加载房间
     * @param preloadRoomList 预加载房间列表
     */
    fun preloadRoom(preloadRoomList: List<RoomInfo>)

    /**
     * 切换房间状态
     * @param newState 目标状态
     * @param roomInfo 目标房间信息
     * @param mediaOptions 目标房间的option
     */
    fun switchRoomState(newState: RoomStatus, roomInfo: RoomInfo, mediaOptions: ChannelMediaOptions?)

    fun setRoomEvent(channelName: String, uid: Int, eventHandler: IChannelEventListener?)

    /**
     * 获取当前的房间状态
     * @param channelName 目标房间channelId
     * @param roomInfo 目标房间信息
     * @return 房间状态
     */
    fun getRoomState(channelName: String, uid: Int): RoomStatus?

    fun getQuickStartTime(): Long

    /**
     * 获取当前的房间状态
     * @param roomInfo 目标房间信息
     * @param container 视频view容器
     */
    fun renderVideo(roomInfo: RoomInfo, container: VideoSwitcher.VideoCanvasContainer)

    /**
     * 清楚缓存（离开所有缓存频道）
     */
    fun cleanCache()

    /**
     * 开启混音
     *
     * @param filePath 文件路径，assets下文件以/assets/开头
     * @param loopbackOnly 是否仅本地播放，true: 仅本地播放不推给远端，false: 本地播放并推给远端
     * @param cycle ≥ 0: 播放次数。-1: 无限循环播放。
     *
     */
    fun startAudioMixing(connection: RtcConnection, filePath: String, loopbackOnly: Boolean, cycle: Int)

    /**
     * 停止混音
     */
    fun stopAudioMixing(connection: RtcConnection)

    /**
     * 调整音乐音量
     * @param volume 0～100
     */
    fun adjustAudioMixingVolume(connection: RtcConnection, volume: Int)
}