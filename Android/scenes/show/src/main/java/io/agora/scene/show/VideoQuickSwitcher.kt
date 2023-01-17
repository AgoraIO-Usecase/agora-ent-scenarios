package io.agora.scene.show

import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngineEx

class VideoQuickSwitcher(private val rtcEngineEx: RtcEngineEx = RtcEngineInstance.rtcEngine) {



    /**
     * 设置最大预加载的连接数
     */
    fun setMaxPreJoinedConnectionCount(count: Int) {

    }

    /**
     * 设置预加载的连接列表
     */
    fun preloadConnections(connections: List<RtcConnection>) {

    }

    /**
     * 离开所有已加入的频道连接
     */
    fun unloadAllConnections() {

    }

    /**
     * 加入频道并预先加入预加载连接列表里在该connection上下不超过最大预加载连接数的频道
     */
    fun joinChannel(
        connection: RtcConnection,
        mediaOptions: ChannelMediaOptions,
        eventHandler: IRtcEngineEventHandler
    ) {

    }

    /**
     * 离开频道，如果在已预加载的频道则只取消订阅音视频流
     */
    fun leaveChannel(connection: RtcConnection) {

    }


}