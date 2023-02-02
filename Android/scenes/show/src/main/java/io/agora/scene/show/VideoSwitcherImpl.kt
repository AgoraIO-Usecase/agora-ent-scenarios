package io.agora.scene.show

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.utils.ToastUtils

class VideoSwitcherImpl(private val rtcEngine: RtcEngineEx) : VideoSwitcher {
    private val tag = "VideoSwitcherImpl"
    private var preloadCount = 3

    private val connectionsForPreloading = mutableListOf<RtcConnection>()
    private val connectionsPreloaded = mutableListOf<RtcConnection>()
    private val connectionsJoined = mutableListOf<RtcConnection>()
    private val rtcEventHandlers = mutableMapOf<String, RtcEngineEventHandlerImpl>()
    private val remoteVideoCanvasList = mutableListOf<RemoteVideoCanvasWrap>()
    private var localVideoCanvas: LocalVideoCanvasWrap? = null

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun setPreloadCount(count: Int) {
        preloadCount = count
    }

    override fun preloadConnections(connections: List<RtcConnection>) {
        connectionsForPreloading.clear()
        connectionsForPreloading.addAll(connections)
    }

    override fun unloadConnections() {
        connectionsJoined.forEach {
            leaveRtcChannel(it)
        }
        connectionsPreloaded.forEach {
            leaveRtcChannel(it)
        }

        localVideoCanvas?.release()
        rtcEventHandlers.clear()
        connectionsForPreloading.clear()
        connectionsPreloaded.clear()
        connectionsJoined.clear()
    }

    override fun joinChannel(
        connection: RtcConnection,
        mediaOptions: ChannelMediaOptions,
        eventListener: VideoSwitcher.IChannelEventListener
    ) {
        connectionsJoined.firstOrNull{ it.equal(connection)}
            ?.let {
                rtcEventHandlers[connection.channelId]?.setEventListener(eventListener)
                return
            }

        connectionsPreloaded.firstOrNull { it.equal(connection) }
            ?.let {
                rtcEventHandlers[it.channelId]?.setEventListener(eventListener)
                rtcEventHandlers[it.channelId]?.subscribeMediaTime = SystemClock.elapsedRealtime()
                rtcEngine.updateChannelMediaOptionsEx(mediaOptions, it)
                connectionsPreloaded.remove(it)
                connectionsJoined.add(it)
                return
            }

        joinRtcChannel(connection, mediaOptions, eventListener)
        connectionsJoined.add(connection)

        preloadChannels()
    }

    private fun preloadChannels() {
        val size = connectionsForPreloading.size
        val index =
            connectionsForPreloading.indexOfFirst { it.equal(connectionsJoined.firstOrNull()) }
        val connPreLoaded = mutableListOf<RtcConnection>()
        for (i in (index - (preloadCount - 1) / 2)..(index + preloadCount / 2)) {
            if (i == index) {
                continue
            }
            val realIndex = (if (i < 0) size + i else i) % size
            if (realIndex < 0 || realIndex >= size) {
                continue
            }
            val conn = connectionsForPreloading[realIndex]
            if (connectionsJoined.any { it.equal(conn) }) {
                continue
            }
            if (connectionsPreloaded.none { it.equal(conn) }) {
                val options = ChannelMediaOptions()
                options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                options.autoSubscribeVideo = false
                options.autoSubscribeAudio = false
                joinRtcChannel(conn, options)
                connectionsPreloaded.add(conn)
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
        connectionsJoined.firstOrNull { it.equal(connection) }
            ?.let { conn ->
                val options = ChannelMediaOptions()
                options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                options.autoSubscribeVideo = false
                options.autoSubscribeAudio = false
                rtcEngine.updateChannelMediaOptionsEx(options, conn)
                rtcEventHandlers[conn.channelId]?.setEventListener(null)
                connectionsJoined.remove(conn)
                connectionsPreloaded.add(conn)
                //remoteVideoCanvasList.filter { canvas -> canvas.connection.equal(conn) }.forEach { it.release() }
                return true
            }

        connectionsPreloaded.firstOrNull { it.equal(connection) }
            ?.let {
                leaveRtcChannel(it)
                connectionsPreloaded.remove(it)
                return true
            }

        return false
    }

    override fun setupRemoteVideo(
        connection: RtcConnection,
        container: VideoSwitcher.VideoCanvasContainer
    ) {
        remoteVideoCanvasList.firstOrNull {
            it.connection.equal(connection) && it.uid == container.uid && it.renderMode == container.renderMode && it.lifecycleOwner == container.lifecycleOwner
        }?.let {
            val videoView = it.view
            val viewIndex = container.container.indexOfChild(videoView)
            if (viewIndex == container.viewIndex) {
                return
            }
            (videoView.parent as? ViewGroup)?.removeView(videoView)
            container.container.addView(videoView, container.viewIndex)
            return
        }

        var videoView = container.container.getChildAt(container.viewIndex)
        if (!(videoView is TextureView)) {
            videoView = TextureView(container.container.context)
            container.container.addView(videoView, container.viewIndex)
        }

        val remoteVideoCanvasWrap = RemoteVideoCanvasWrap(
            connection,
            container.lifecycleOwner,
            videoView,
            container.renderMode,
            container.uid
        )
        rtcEventHandlers[connection.channelId]?.let {
            if (it.isJoinChannelSuccess) {
                rtcEngine.setupRemoteVideoEx(
                    remoteVideoCanvasWrap,
                    connection
                )
            }
        }
    }

    override fun setupLocalVideo(container: VideoSwitcher.VideoCanvasContainer) {
        localVideoCanvas?.let {
            if (it.lifecycleOwner == container.lifecycleOwner && it.renderMode == container.renderMode && it.uid == container.uid) {
                val videoView = it.view
                val viewIndex = container.container.indexOfChild(videoView)
                if (viewIndex == container.viewIndex) {
                    return
                }
                (videoView.parent as? ViewGroup)?.removeView(videoView)
                container.container.addView(videoView, container.viewIndex)
                return
            }
        }
        var videoView = container.container.getChildAt(container.viewIndex)
        if (!(videoView is TextureView)) {
            videoView = TextureView(container.container.context)
            container.container.addView(videoView, container.viewIndex)
        }

        rtcEngine.setupLocalVideo(LocalVideoCanvasWrap(container.lifecycleOwner,
            videoView, container.renderMode, container.uid))
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
        remoteVideoCanvasList.filter { it.connection.equal(connection) }.forEach { it.release() }
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
                    RtcEngineEventHandlerImpl(joinChannelTime, connection)
                eventHandler.setEventListener(eventListener)
                val ret = rtcEngine.joinChannelEx(it, connection, mediaOptions, eventHandler)
                ShowLogger.d(
                    tag,
                    "join channel ret : channel=${connection.channelId} code=$ret, message=${
                        RtcEngine.getErrorDescription(
                            ret
                        )
                    }"
                )
                rtcEventHandlers[connection.channelId] = eventHandler
            },
            failure = {
                ShowLogger.e(tag, it, "generate token failed")
                ToastUtils.showToast(it!!.message)
                eventListener?.onTokenGenerateFailedException?.invoke(it)
            })
    }

    inner class LocalVideoCanvasWrap(
        val lifecycleOwner: LifecycleOwner,
        view: View,
        renderMode: Int,
        uid: Int
    ) : DefaultLifecycleObserver, VideoCanvas(view, renderMode, uid) {

        init {
            lifecycleOwner.lifecycle.addObserver(this)
            if(localVideoCanvas != this){
                localVideoCanvas?.release()
                localVideoCanvas = this
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            if (lifecycleOwner == owner) {
                release()
            }
        }

        fun release(){
            lifecycleOwner.lifecycle.removeObserver(this)
            view = null
            rtcEngine.setupLocalVideo(this)
            localVideoCanvas = null
        }

    }

    inner class RemoteVideoCanvasWrap(
        val connection: RtcConnection,
        val lifecycleOwner: LifecycleOwner,
        view: View,
        renderMode: Int,
        uid: Int
    ) : DefaultLifecycleObserver, VideoCanvas(view, renderMode, uid) {

        init {
            lifecycleOwner.lifecycle.addObserver(this)
            remoteVideoCanvasList.add(this)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            if (lifecycleOwner == owner) {
                release()
            }
        }

        fun release(){
            lifecycleOwner.lifecycle.removeObserver(this)
            view = null
            rtcEngine.setupRemoteVideoEx(this, connection)
            remoteVideoCanvasList.remove(this)
        }

    }


    inner class RtcEngineEventHandlerImpl(
        private val joinChannelTime: Long,
        private val connection: RtcConnection,
    ) : IRtcEngineEventHandler() {

        private var firstRemoteUid: Int = 0
        var isJoinChannelSuccess = false
        private var eventListener: VideoSwitcher.IChannelEventListener? = null
        var subscribeMediaTime: Long = joinChannelTime

        fun setEventListener(listener: VideoSwitcher.IChannelEventListener?) {
            eventListener = listener
            if (isJoinChannelSuccess) {
                eventListener?.onChannelJoined?.invoke(connection)
            }
            if (firstRemoteUid != 0) {
                eventListener?.onUserJoined?.invoke(firstRemoteUid)
            }
        }

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
            isJoinChannelSuccess = true
            eventListener?.onChannelJoined?.invoke(connection)
            remoteVideoCanvasList.filter { canvas -> canvas.connection.equal(connection) }.forEach {
                runOnUiThread {
                    rtcEngine.setupRemoteVideoEx(it, connection)
                }
            }
            ShowLogger.d(
                tag,
                "join channel $channel success cost time : ${SystemClock.elapsedRealtime() - joinChannelTime} ms"
            )
        }

        override fun onLeaveChannel(stats: RtcStats?) {
            super.onLeaveChannel(stats)
            isJoinChannelSuccess = false
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
            if (firstRemoteUid == 0) {
                firstRemoteUid = uid
            }
            eventListener?.onUserJoined?.invoke(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            if (uid == firstRemoteUid) {
                firstRemoteUid = 0
            }
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
            if (state == Constants.REMOTE_VIDEO_STATE_PLAYING
                && (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED || reason == Constants.REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED)
            ) {
                val durationFromSubscribe = SystemClock.elapsedRealtime() - subscribeMediaTime
                val durationFromJoiningRoom = SystemClock.elapsedRealtime() - joinChannelTime
                ShowLogger.d(
                    tag,
                    "video cost time : channel=${connection.channelId}, uid=$uid, durationFromJoiningRoom=$durationFromJoiningRoom, durationFromSubscribe=$durationFromSubscribe "
                )
            }
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

    private fun runOnUiThread(run: () -> Unit) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            run.invoke()
        } else {
            mainHandler.post(run)
        }
    }

}