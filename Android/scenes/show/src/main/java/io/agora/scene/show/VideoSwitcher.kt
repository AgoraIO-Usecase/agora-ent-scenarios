package io.agora.scene.show

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection

/**
 *
 */
interface VideoSwitcher {

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
    )

    data class VideoCanvasContainer(
        val lifecycleOwner: LifecycleOwner,
        val container: ViewGroup,
        val uid: Int,
        val viewIndex: Int = 0,
        val renderMode: Int = Constants.RENDER_MODE_HIDDEN,
    )

    /**
     * 设置最大预加载的连接数
     */
    fun setPreloadCount(count: Int)

    /**
     * 设置预加载的连接列表
     */
    fun preloadConnections(connections: List<RtcConnection>)

    /**
     * 离开所有已加入的频道连接
     */
    fun unloadConnections()

    /**
     * 加入频道并预先加入预加载连接列表里在该connection上下不超过最大预加载连接数的频道
     */
    fun joinChannel(
        connection: RtcConnection,
        mediaOptions: ChannelMediaOptions,
        eventListener: IChannelEventListener
    )

    /**
     * 离开频道，如果在已预加载的频道则只取消订阅音视频流
     * @param force 强制离开
     */
    fun leaveChannel(connection: RtcConnection,force: Boolean): Boolean

    /**
     * 渲染远端视频，相比于RtcEngineEx.setupRemoteVideoEx，这里会缓存渲染视图，减少渲染时不断重复创建渲染视图，提高渲染速度
     */
    fun setupRemoteVideo(connection: RtcConnection, container: VideoCanvasContainer)

    /**
     * 渲染本地视频，相比于RtcEngineEx.setupLocalVideo，这里会缓存渲染视图，减少渲染时不断重复创建渲染视图，提高渲染速度
     */
    fun setupLocalVideo(container: VideoCanvasContainer)

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