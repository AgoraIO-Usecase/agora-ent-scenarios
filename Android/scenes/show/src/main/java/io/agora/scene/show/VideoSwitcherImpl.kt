package io.agora.scene.show

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.videoSwitcherAPI.VideoSwitcherAPI
import io.agora.scene.show.videoSwitcherAPI.VideoSwitcherAPIImpl
import java.util.*

class VideoSwitcherImpl constructor(private val rtcEngine: RtcEngineEx, private val api: VideoSwitcherAPI) : VideoSwitcher {
    private val tag = "VideoSwitcherImpl"
    private var preloadCount = 3

    private val connectionsForPreloading = Collections.synchronizedList(mutableListOf<RtcConnectionWrap>())
    private val connectionsPreloaded = Collections.synchronizedList(mutableListOf<RtcConnectionWrap>())
    private val connectionsJoined = Collections.synchronizedList(mutableListOf<RtcConnectionWrap>())
    private val remoteVideoCanvasList = Collections.synchronizedList(mutableListOf<RemoteVideoCanvasWrap>())
    private var localVideoCanvas: LocalVideoCanvasWrap? = null

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val preLoadRun = Runnable { preloadChannels() }
    private var quickStartTime = 0L

    override fun setPreloadCount(count: Int) {
        preloadCount = count
        ShowLogger.d(tag, "setPreloadCount count=$count")
    }

    override fun preloadConnections(connections: List<RtcConnection>) {
        connectionsForPreloading.clear()
        connectionsForPreloading.addAll(connections.map { RtcConnectionWrap(it) })
        val roomList = mutableListOf<VideoSwitcherAPI.RoomInfo>()
        connections.forEach {
            roomList.add(
                VideoSwitcherAPI.RoomInfo(
                it.channelId,
                it.localUid,
                RtcEngineInstance.generalToken(),
                null
            ))
        }
        api.preloadRoom(roomList)
        ShowLogger.d(tag, "preloadConnections connections=$connectionsForPreloading")
    }

    override fun unloadConnections() {
        mainHandler.removeCallbacksAndMessages(null)

        api.cleanCache()
    }

    override fun preJoinChannel(
        connection: RtcConnection,
        mediaOptions: ChannelMediaOptions,
        eventListener: VideoSwitcherAPI.IChannelEventListener?
    ) {
        ShowLogger.d("hugo", "111111")
        api.switchRoomState(VideoSwitcherAPI.RoomStatus.PREJOINED, VideoSwitcherAPI.RoomInfo(
            connection.channelId,
            connection.localUid,
            RtcEngineInstance.generalToken(),
            eventListener
        ), mediaOptions)
    }

    override fun joinChannel(
        connection: RtcConnection,
        mediaOptions: ChannelMediaOptions,
        eventListener: VideoSwitcherAPI.IChannelEventListener?
    ) {
        api.switchRoomState(VideoSwitcherAPI.RoomStatus.JOINED, VideoSwitcherAPI.RoomInfo(
            connection.channelId,
            connection.localUid,
            RtcEngineInstance.generalToken(),
            eventListener
        ), mediaOptions)
//        connectionsJoined.firstOrNull { it.isSameChannel(connection) }
//            ?.let {
//                ShowLogger.d(tag, "joinChannel joined connection=$it")
//                it.rtcEventHandler?.setEventListener(eventListener)
//                return
//            }
//
//        connectionsPreloaded.firstOrNull { it.isSameChannel(connection) }
//            ?.let {
//                ShowLogger.d(tag, "joinChannel preloaded connection=$it")
//                it.rtcEventHandler?.setEventListener(eventListener)
//                it.rtcEventHandler?.subscribeMediaTime = SystemClock.elapsedRealtime()
//                it.mediaOptions = mediaOptions
//                rtcEngine.updateChannelMediaOptionsEx(mediaOptions, it)
//                connectionsPreloaded.remove(it)
//                connectionsJoined.add(it)
//                return
//            }
//
        val connectionWrap = RtcConnectionWrap(connection)
        connectionsJoined.add(connectionWrap)
//
        mainHandler.removeCallbacks(preLoadRun)
        if (connectionsJoined.size == 1 || connectionsPreloaded.size <= 0) {
            mainHandler.postDelayed(preLoadRun, 500)
        }
    }

    override fun setChannelEvent(
        channelName: String,
        uid: Int,
        eventHandler: VideoSwitcherAPI.IChannelEventListener?
    ) {
        api.setRoomEvent(channelName, uid, eventHandler)
    }

    private fun preloadChannels() {
        val size = connectionsForPreloading.size
        val index =
            connectionsForPreloading.indexOfFirst { it.isSameChannel(connectionsJoined.firstOrNull()) }
        val connPreLoaded = mutableListOf<RtcConnection>()
        for (i in (index - (preloadCount - 1) / 2)..(index + preloadCount / 2)) {
            if (i == index) {
                continue
            }
            // workaround
            if (size == 0) {
                return
            }
            val realIndex = (if (i < 0) size + i else i) % size
            if (realIndex < 0 || realIndex >= size) {
                continue
            }
            val conn = connectionsForPreloading[realIndex]
            if (connectionsJoined.any { it.isSameChannel(conn) }) {
                continue
            }
            if (api.getRoomState(conn.channelId, conn.localUid) != VideoSwitcherAPI.RoomStatus.PREJOINED) {
                ShowLogger.d("hugo", "22222")
                api.switchRoomState(VideoSwitcherAPI.RoomStatus.PREJOINED, VideoSwitcherAPI.RoomInfo(
                    conn.channelId,
                    conn.localUid,
                    RtcEngineInstance.generalToken(),
                    null
                ), null)
            }
            connPreLoaded.add(conn)
        }

        if (connectionsPreloaded.size > preloadCount) {
            connectionsPreloaded.iterator().let { connIterator ->
                while (connIterator.hasNext()) {
                    val next = connIterator.next()
                    if (connPreLoaded.none { next.isSameChannel(it) }) {
                        leaveRtcChannel(next)
                        connIterator.remove()
                    }
                }
            }
        }
    }


    override fun leaveChannel(connection: RtcConnection, force: Boolean): Boolean {
        if (force) {
            ShowLogger.d("hugo", "4444")
            api.switchRoomState(VideoSwitcherAPI.RoomStatus.IDLE, VideoSwitcherAPI.RoomInfo(
                connection.channelId,
                connection.localUid,
                RtcEngineInstance.generalToken(),
                null
            ), null)
            return true
        } else {
            ShowLogger.d("hugo", "3333")
            api.switchRoomState(VideoSwitcherAPI.RoomStatus.PREJOINED, VideoSwitcherAPI.RoomInfo(
                connection.channelId,
                connection.localUid,
                RtcEngineInstance.generalToken(),
                null
            ), null)
            return true
        }
    }

    override fun setupRemoteVideo(
        connection: RtcConnection,
        container: VideoSwitcher.VideoCanvasContainer
    ) {
        api.renderVideo(
            VideoSwitcherAPI.RoomInfo(
                connection.channelId,
                connection.localUid,
                RtcEngineInstance.generalToken(),
                null
        ), container)
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

        val local = LocalVideoCanvasWrap(
            container.lifecycleOwner,
            videoView, container.renderMode, container.uid
        )
        local.mirrorMode = Constants.VIDEO_MIRROR_MODE_DISABLED
        rtcEngine.setupLocalVideo(local)
    }

    override fun getFirstVideoFrameTime(): Long {
        return api.getQuickStartTime()
    }

    private fun leaveRtcChannel(connection: RtcConnectionWrap) {
        val options = LeaveChannelOptions()
        options.stopAllEffect = false
        options.stopAudioMixing = false
        options.stopMicrophoneRecording = false
        val ret = rtcEngine.leaveChannelEx(connection)
        ShowLogger.d(
            tag,
            "leaveChannel ret : connection=$connection, code=$ret, message=${RtcEngine.getErrorDescription(ret)}"
        )
        connection.audioMixingPlayer?.stop()
        connection.audioMixingPlayer?.destroy()
        connection.audioMixingPlayer = null
        remoteVideoCanvasList.filter { it.connection.isSameChannel(connection) }.forEach { it.release() }
    }

    override fun startAudioMixing(
        connection: RtcConnection,
        filePath: String,
        loopbackOnly: Boolean,
        cycle: Int
    ) {
        api.startAudioMixing(connection, filePath, loopbackOnly, cycle)
    }

    override fun stopAudioMixing(connection: RtcConnection) {
        api.stopAudioMixing(connection)
    }

    override fun adjustAudioMixingVolume(connection: RtcConnection, volume: Int) {
        api.adjustAudioMixingVolume(connection, volume)
    }

    private fun joinRtcChannel(
        connection: RtcConnectionWrap,
        eventListener: VideoSwitcherAPI.IChannelEventListener? = null
    ) {
        val joinChannelTime = SystemClock.elapsedRealtime()
        ShowLogger.d(tag, "joinChannelEx start : channelId=${connection.channelId}, uid=${connection.localUid}")
        val generalToken = RtcEngineInstance.generalToken()
        if (generalToken.isNotEmpty()) {
            val eventHandler = RtcEngineEventHandlerImpl(joinChannelTime, connection)
            eventHandler.setEventListener(eventListener)
            connection.rtcEventHandler = eventHandler
            val ret = rtcEngine.joinChannelEx(generalToken, connection, connection.mediaOptions, eventHandler)
            ShowLogger.d(tag, "joinChannelEx ret:channel=${connection.channelId} uid=${connection.localUid} code=$ret")
        } else {
            TokenGenerator.generateToken("", connection.localUid.toString(),
                TokenGenerator.TokenGeneratorType.token007,
                TokenGenerator.AgoraTokenType.rtc,
                success = {
                    ShowLogger.d(
                        tag,
                        "joinChannelEx generate token : channel ${connection.channelId} uid=${connection.localUid} token success cost " +
                                "time : ${SystemClock.elapsedRealtime() - joinChannelTime} ms"
                    )
                    RtcEngineInstance.setupGeneralToken(it)
                    val eventHandler = RtcEngineEventHandlerImpl(joinChannelTime, connection)
                    eventHandler.setEventListener(eventListener)
                    connection.rtcEventHandler = eventHandler
                    val ret = rtcEngine.joinChannelEx(it, connection, connection.mediaOptions, eventHandler)
                    ShowLogger.d(tag, "joinChannelEx ret : channel=${connection.channelId} code=$ret")
                },
                failure = {
                    ShowLogger.e(tag, it, "joinChannelEx generate token failed")
                    ToastUtils.showToast(it!!.message)
                    eventListener?.onTokenGenerateFailedException?.invoke(it)
                })
        }
    }

    inner class LocalVideoCanvasWrap constructor(
        val lifecycleOwner: LifecycleOwner,
        view: View,
        renderMode: Int,
        uid: Int
    ) : DefaultLifecycleObserver, VideoCanvas(view, renderMode, uid) {

        init {
            lifecycleOwner.lifecycle.addObserver(this)
            if (localVideoCanvas != this) {
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

        fun release() {
            lifecycleOwner.lifecycle.removeObserver(this)
            view = null
            rtcEngine.setupLocalVideo(this)
            localVideoCanvas = null
        }

    }

    inner class RtcConnectionWrap constructor(connection: RtcConnection) :
        RtcConnection(connection.channelId, connection.localUid) {

        var mediaOptions = ChannelMediaOptions()
        var rtcEventHandler: RtcEngineEventHandlerImpl? = null
        var audioMixingPlayer: IMediaPlayer? = null

        fun isSameChannel(connection: RtcConnection?) =
            connection != null && channelId == connection.channelId && localUid == connection.localUid


        override fun toString(): String {
            return "{channelId=$channelId, localUid=$localUid, mediaOptions=$mediaOptions, rtcEnventHandler=$rtcEventHandler, audioMixingPlayer=$audioMixingPlayer}"
        }

    }

    inner class RemoteVideoCanvasWrap constructor(
        val connection: RtcConnectionWrap,
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

        fun release() {
            lifecycleOwner.lifecycle.removeObserver(this)
            view = null
            rtcEngine.setupRemoteVideoEx(this, connection)
            remoteVideoCanvasList.remove(this)
        }

    }


    inner class RtcEngineEventHandlerImpl constructor(
        private val joinChannelTime: Long,
        private val connection: RtcConnection,
    ) : IRtcEngineEventHandler() {

        private var firstRemoteUid: Int = 0
        var isJoinChannelSuccess = false
        private var eventListener: VideoSwitcherAPI.IChannelEventListener? = null
        var subscribeMediaTime: Long = joinChannelTime

        fun setEventListener(listener: VideoSwitcherAPI.IChannelEventListener?) {
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
            remoteVideoCanvasList.filter { canvas -> canvas.connection.isSameChannel(connection) }.forEach {
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
            ShowLogger.d(
                tag,
                "leave channel ${connection.channelId} success"
            )
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
                quickStartTime = durationFromSubscribe
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

    private fun runOnUiThread(run: () -> Unit) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            run.invoke()
        } else {
            mainHandler.post(run)
        }
    }

}