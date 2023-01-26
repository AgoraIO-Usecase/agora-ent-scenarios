package io.agora.scene.show

import android.os.SystemClock
import io.agora.rtc2.*
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.utils.ToastUtils

class VideoSwitcherImpl(private val rtcEngine: RtcEngineEx) : VideoSwitcher {
    private val tag = "VideoSwitcherImpl"
    private var preloadCount = 3

    private val connectionsForPreloading = mutableListOf<RtcConnection>()
    private val connectionsPreloaded = mutableListOf<RtcConnection>()
    private var connectionJoined: RtcConnection? = null

    private val rtcEventHandlers = mutableMapOf<String, RtcEngineEventHandlerImpl>()

    override fun setPreloadCount(count: Int) {
        preloadCount = count
    }

    override fun preloadConnections(connections: List<RtcConnection>) {
        connectionsForPreloading.clear()
        connectionsForPreloading.addAll(connections)
    }

    override fun unloadConnections() {
        val connJoined = connectionJoined
        if (connJoined != null) {
            leaveRtcChannel(connJoined)
        }
        connectionsPreloaded.forEach {
            leaveRtcChannel(it)
        }

        rtcEventHandlers.clear()
        connectionsForPreloading.clear()
        connectionsPreloaded.clear()
        connectionJoined = null
    }

    override fun joinChannel(
        connection: RtcConnection,
        mediaOptions: ChannelMediaOptions,
        eventListener: VideoSwitcher.IChannelEventListener
    ) {
        connectionJoined?.let {
            if (it.equal(connection)) {
                rtcEventHandlers[connection.channelId]?.eventListener = eventListener
                eventListener.onChannelJoined?.invoke()
                return
            }
            leaveChannel(it)
            connectionJoined = null
        }

        connectionsPreloaded.firstOrNull { it.equal(connection) }
            ?.let {
                eventListener.onChannelJoined?.invoke()
                rtcEventHandlers[it.channelId]?.eventListener = eventListener
                rtcEngine.updateChannelMediaOptionsEx(mediaOptions, it)
                connectionsPreloaded.remove(it)
                connectionJoined = it
            }

        if (connectionJoined == null) {
            joinRtcChannel(connection, mediaOptions, eventListener)
            connectionJoined = connection
        }


        if (connectionsPreloaded.size >= preloadCount) {
            return
        }

        val size = connectionsForPreloading.size
        val index =
            connectionsForPreloading.indexOfFirst { it.equal(connection) }
        val connPreLoaded = mutableListOf<RtcConnection>()
        for (i in (index - (preloadCount - 1) / 2) until (index + preloadCount / 2)) {
            if (i == index) {
                continue
            }
            val realIndex = if (i < 0) size + i else i
            if (realIndex < 0 || realIndex >= size) {
                continue
            }
            val conn = connectionsForPreloading[realIndex]
            if (conn.equal(connectionJoined)) {
                continue
            }
            if (connectionsPreloaded.none { it.equal(conn) }) {
                val options = ChannelMediaOptions()
                options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                options.autoSubscribeVideo = false
                options.autoSubscribeAudio = false
                joinRtcChannel(conn, options)
            }
            connPreLoaded.add(conn)
        }

        if (connectionsPreloaded.size > preloadCount) {
            connectionsPreloaded.iterator().let {
                while (it.hasNext()) {
                    val next = it.next()
                    if (connPreLoaded.none { it.equal(next) }) {
                        leaveRtcChannel(next)
                        it.remove()
                    }
                }
            }
        }
    }


    override fun leaveChannel(connection: RtcConnection): Boolean {
        if (connection.equal(connectionJoined)) {
            val options = ChannelMediaOptions()
            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            options.autoSubscribeVideo = false
            options.autoSubscribeAudio = false
            rtcEngine.updateChannelMediaOptionsEx(options, connection)
            rtcEventHandlers[connection.channelId]?.eventListener = null
            connectionJoined = null
            connectionsPreloaded.add(connection)
            return true
        } else if (connectionsPreloaded.any { it.equal(connection) }){
            leaveRtcChannel(connection)
            connectionsPreloaded.removeIf { it.equal(connection) }
            return true
        }
        return false
    }

    private fun leaveRtcChannel(connection: RtcConnection) {
        val options = LeaveChannelOptions()
        options.stopAllEffect = false
        options.stopAudioMixing = false
        options.stopMicrophoneRecording = false
        val ret = rtcEngine.leaveChannelEx(connection)
        ShowLogger.d(
            tag,
            "leave channel ret : code=$ret, message=${RtcEngine.getErrorDescription(ret)}"
        )
        rtcEventHandlers.remove(connection.channelId)
    }

    private fun joinRtcChannel(
        connection: RtcConnection,
        mediaOptions: ChannelMediaOptions,
        eventListener: VideoSwitcher.IChannelEventListener? = null
    ) {
        val joinChannelTime = SystemClock.elapsedRealtime()
        ShowLogger.d(
            tag,
            "join channel : channelId=${connection.channelId}, uid=${connection.localUid}"
        )
        TokenGenerator.generateToken(
            connection.channelId, connection.localUid.toString(),
            TokenGenerator.TokenGeneratorType.token006,
            TokenGenerator.AgoraTokenType.rtc,
            success = {
                ShowLogger.d(
                    tag,
                    "generate channel ${connection.channelId} token success cost time : ${SystemClock.elapsedRealtime() - joinChannelTime} ms"
                )
                val eventHandler =
                    RtcEngineEventHandlerImpl(joinChannelTime, connection, eventListener)
                val ret = rtcEngine.joinChannelEx(it, connection, mediaOptions, eventHandler)
                ShowLogger.d(
                    tag,
                    "join channel ret : code=$ret, message=${RtcEngine.getErrorDescription(ret)}"
                )
                rtcEventHandlers[connection.channelId] = eventHandler
            },
            failure = {
                ShowLogger.e(tag, it, "generate token failed")
                ToastUtils.showToast(it!!.message)
            })
    }


    inner class RtcEngineEventHandlerImpl(
        private val joinChannelTime: Long,
        private val connection: RtcConnection,
        var eventListener: VideoSwitcher.IChannelEventListener? = null
    ) : IRtcEngineEventHandler() {

        override fun onError(err: Int) {
            super.onError(err)
            ShowLogger.e(
                tag,
                message = "channel ${connection.channelId} error : code=$err, message=${
                    RtcEngine.getErrorDescription(err)
                }"
            )
        }

        override fun onJoinChannelSuccess(
            channel: String?,
            uid: Int,
            elapsed: Int
        ) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            eventListener?.onChannelJoined?.invoke()
            ShowLogger.d(
                tag,
                "join channel $channel success cost time : ${SystemClock.elapsedRealtime() - joinChannelTime} ms"
            )
        }

        override fun onFirstRemoteVideoFrame(
            uid: Int,
            width: Int,
            height: Int,
            elapsed: Int
        ) {
            super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
            ShowLogger.d(
                tag,
                "$uid first remote video frame cost time : ${SystemClock.elapsedRealtime() - joinChannelTime} ms"
            )
        }

        override fun onFirstLocalVideoFrame(
            source: Constants.VideoSourceType?,
            width: Int,
            height: Int,
            elapsed: Int
        ) {
            super.onFirstLocalVideoFrame(source, width, height, elapsed)
            ShowLogger.d(
                tag,
                "$source first local video frame cost time : ${SystemClock.elapsedRealtime() - joinChannelTime} ms"
            )
        }


        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            eventListener?.onUserJoined?.invoke(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            eventListener?.onUserOffline?.invoke(uid)
        }

        override fun onLocalVideoStateChanged(
            source: Constants.VideoSourceType?,
            state: Int,
            error: Int
        ) {
            super.onLocalVideoStateChanged(source, state, error)
            eventListener?.onLocalVideoStateChanged?.invoke(state)
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            eventListener?.onRemoteVideoStateChanged?.invoke(uid, state)
        }

        override fun onRtcStats(stats: RtcStats?) {
            super.onRtcStats(stats)
            stats ?: return
            eventListener?.onRtcStats?.invoke(stats)
        }

        override fun onLocalVideoStats(
            source: Constants.VideoSourceType?,
            stats: LocalVideoStats?
        ) {
            super.onLocalVideoStats(source, stats)
            stats ?: return
            eventListener?.onLocalVideoStats?.invoke(stats)
        }

        override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
            super.onRemoteVideoStats(stats)
            stats ?: return
            eventListener?.onRemoteVideoStats?.invoke(stats)
        }

        override fun onLocalAudioStats(stats: LocalAudioStats?) {
            super.onLocalAudioStats(stats)
            stats ?: return
            eventListener?.onLocalAudioStats?.invoke(stats)
        }

        override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
            super.onRemoteAudioStats(stats)
            stats ?: return
            eventListener?.onRemoteAudioStats?.invoke(stats)
        }

        override fun onUplinkNetworkInfoUpdated(info: UplinkNetworkInfo?) {
            super.onUplinkNetworkInfoUpdated(info)
            info ?: return
            eventListener?.onUplinkNetworkInfoUpdated?.invoke(info)
        }

        override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo?) {
            super.onDownlinkNetworkInfoUpdated(info)
            info ?: return
            eventListener?.onDownlinkNetworkInfoUpdated?.invoke(info)
        }

        override fun onContentInspectResult(result: Int) {
            super.onContentInspectResult(result)
            eventListener?.onContentInspectResult?.invoke(result)
        }

    }

    private fun RtcConnection.equal(conn: Any?) =
        conn is RtcConnection && this.channelId == conn.channelId && this.localUid == conn.localUid

}