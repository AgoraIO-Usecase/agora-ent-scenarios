package io.agora.videoloaderapi

import android.os.Handler
import android.util.Log
import android.view.TextureView
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.agora.rtc2.*
import io.agora.rtc2.IRtcEngineEventHandler.VideoRenderingTracingInfo
import io.agora.rtc2.video.VideoCanvas
import io.agora.videoloaderapi.report.ApiCostEvent
import org.json.JSONObject
import java.util.*

class VideoLoaderImpl constructor(private val rtcEngine: RtcEngineEx) : VideoLoader {
    private val tag = "VideoLoader"
    private val anchorStateMap = Collections.synchronizedMap(mutableMapOf<RtcConnectionWrap, AnchorState>())
    private val remoteVideoCanvasList = Collections.synchronizedList(mutableListOf<RemoteVideoCanvasWrap>())
    private val videoProfileMap = mutableMapOf<String, VideoLoaderProfiler>() // anchorId, VideoLoaderProfiler

    override fun cleanCache() {
        VideoLoader.reporter?.reportFuncEvent("cleanCache", mapOf(), mapOf())
        VideoLoader.videoLoaderApiLog(tag, "cleanCache")
        anchorStateMap.forEach {
            innerSwitchAnchorState(AnchorState.IDLE, 0, it.key, null, null)
        }
        anchorStateMap.clear()
    }

    override fun preloadAnchor(anchorList: List<VideoLoader.AnchorInfo>, uid: Int) {
        VideoLoader.reporter?.reportFuncEvent("preloadAnchor", mapOf("anchorList" to anchorList, "uid" to uid), mapOf())
        VideoLoader.videoLoaderApiLog(tag, "preloadAnchor, anchorList:$anchorList, uid:$uid")
        anchorList.forEach {
            rtcEngine.preloadChannel(it.token, it.channelId, uid)
        }
    }

    override fun switchAnchorState(
        newState: AnchorState,
        anchorInfo: VideoLoader.AnchorInfo,
        localUid: Int,
        mediaOptions: ChannelMediaOptions?
    ) {
        VideoLoader.reporter?.reportFuncEvent("switchAnchorState", mapOf("newState" to newState, "anchorInfo" to anchorInfo, "localUid" to localUid), mapOf())
        VideoLoader.videoLoaderApiLog(tag, "switchAnchorState, newState:$newState, anchorInfo:$anchorInfo, localUid:$localUid, mediaOptions:$mediaOptions")
        innerSwitchAnchorState(newState, anchorInfo.anchorUid, RtcConnection(anchorInfo.channelId, localUid), anchorInfo.token, mediaOptions)
    }

    override fun getAnchorState(channelId: String, localUid: Int): AnchorState? {
        VideoLoader.reporter?.reportFuncEvent("getAnchorState", mapOf("channelId" to channelId, "localUid" to localUid), mapOf())
        VideoLoader.videoLoaderApiLog(tag, "getAnchorState, channelId:$channelId, localUid:$localUid")
        anchorStateMap.forEach {
            if (it.key.isSameChannel(RtcConnection(channelId, localUid))) {
                return it.value
            }
        }
        return null
    }

    override fun renderVideo(anchorInfo: VideoLoader.AnchorInfo, localUid: Int, container: VideoLoader.VideoCanvasContainer) {
        VideoLoader.reporter?.reportFuncEvent("renderVideo", mapOf("anchorInfo" to anchorInfo, "localUid" to localUid, "container" to container), mapOf())
        VideoLoader.videoLoaderApiLog(tag, "renderVideo, anchorInfo:$anchorInfo, localUid:$localUid, container:$container")
        remoteVideoCanvasList.firstOrNull {
            it.connection.channelId == anchorInfo.channelId && it.uid == container.uid && it.renderMode == container.renderMode && it.lifecycleOwner == container.lifecycleOwner
        }?.let {
            // remoteVideoCanvasList 内已经存在这个view
            VideoLoader.videoLoaderApiLog(tag, "remoteVideoCanvasList contains this view")

            val videoView = it.view
            val viewIndex = container.container.indexOfChild(videoView)

            if (viewIndex == container.viewIndex) {
                rtcEngine.setupRemoteVideoEx(
                    it,
                    it.connection
                )
                return
            }
        }

        VideoLoader.videoLoaderApiLog(tag, "new view")

        var videoView = container.container.getChildAt(container.viewIndex)
        if (videoView !is TextureView) {
            videoView = TextureView(container.container.context)
            container.container.addView(videoView, container.viewIndex)
        } else {
            container.container.removeViewInLayout(videoView)
            videoView = TextureView(container.container.context)
            container.container.addView(videoView, container.viewIndex)
        }

        val connection = RtcConnection(anchorInfo.channelId, localUid)
//        anchorStateMap.forEach {
//            if (it.key.isSameChannel(connection)) {
//                val connectionWrap = it.key
//                val remoteVideoCanvasWrap = RemoteVideoCanvasWrap(
//                    connectionWrap,
//                    container.lifecycleOwner,
//                    videoView,
//                    container.renderMode,
//                    container.uid
//                )
//                rtcEngine.setupRemoteVideoEx(
//                    remoteVideoCanvasWrap,
//                    connectionWrap
//                )
//                return
//            }
//        }

        val remoteVideoCanvasWrap = RemoteVideoCanvasWrap(
            connection,
            container.lifecycleOwner,
            videoView,
            container.renderMode,
            container.uid
        )
        rtcEngine.setupRemoteVideoEx(
            remoteVideoCanvasWrap,
            connection
        )
    }

    fun getProfiler(roomId: String): VideoLoaderProfiler {
        val profiler = videoProfileMap[roomId] ?: VideoLoaderProfiler(roomId)
        videoProfileMap[roomId] = profiler
        return profiler
    }

    // ------------------------------- inner private -------------------------------

    /**
     * 切换指定主播的状态
     * @param newState 目标状态
     * @param anchorUid 主播 uid
     * @param connection 对应频道的 RtcConnection
     * @param token 对应频道的 token
     * @param mediaOptions 自定义的 ChannelMediaOptions
     */
    private fun innerSwitchAnchorState(
        newState: AnchorState,
        anchorUid: Int,
        connection: RtcConnection,
        token: String?,
        mediaOptions: ChannelMediaOptions?
    ) {
        VideoLoader.videoLoaderApiLog(tag, "innerSwitchAnchorState, newState: $newState, connection: $connection, anchorStateMap: $anchorStateMap")
        val roomId = connection.channelId
        // anchorStateMap 无当前主播记录
        if (anchorStateMap.none {it.key.isSameChannel(connection)}) {
            val rtcConnectionWrap = RtcConnectionWrap(connection)
            VideoLoader.videoLoaderApiLog(tag, "null ==> $newState, connection:$connection")
            when (newState) {
                AnchorState.PRE_JOINED -> {
                    // 加入频道但不收流
                    joinRtcChannel(token, connection, mediaOptions ?: ChannelMediaOptions().apply {
                        clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        autoSubscribeVideo = false
                        autoSubscribeAudio = false
                    })
                }
                AnchorState.JOINED -> {
                    // 加入频道且收流
                    getProfiler(roomId).actualStartTime = System.currentTimeMillis()
                    joinRtcChannel(token, connection, mediaOptions ?: ChannelMediaOptions().apply {
                        clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        autoSubscribeVideo = true
                        autoSubscribeAudio = true
                    })
                }
                AnchorState.JOINED_WITHOUT_AUDIO -> {
                    getProfiler(roomId).actualStartTime = System.currentTimeMillis()
                    joinRtcChannel(token, connection, mediaOptions ?: ChannelMediaOptions().apply {
                        clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        autoSubscribeVideo = true
                        autoSubscribeAudio = true
                    })
                    // 防止音画不同步， 我们采用先订阅再将播放调为0的方式
                    rtcEngine.adjustUserPlaybackSignalVolumeEx(anchorUid, 0, connection)
                }

                else -> {}
            }
            anchorStateMap[rtcConnectionWrap] = newState
            return
        }

        anchorStateMap.forEach {
            if (it.key.isSameChannel(connection)) {
                val oldState = it.value
                if (oldState == newState) {
                    VideoLoader.videoLoaderApiLogWarning(tag, "switchAnchorState is already this state")
                    return
                }
                anchorStateMap[it.key] = newState
                VideoLoader.videoLoaderApiLog(tag, "$oldState ==> $newState, connection:$connection")
                when {
                    oldState == AnchorState.IDLE && newState == AnchorState.PRE_JOINED -> {
                        // 加入频道但不收流
                        joinRtcChannel(token, connection, mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = false
                            autoSubscribeAudio = false
                        })
                    }
                    (oldState == AnchorState.PRE_JOINED || oldState == AnchorState.JOINED_WITHOUT_AUDIO) && newState == AnchorState.JOINED -> {
                        // 保持在频道内, 收流
                        if (oldState == AnchorState.PRE_JOINED) {
                            getProfiler(roomId).actualStartTime = System.currentTimeMillis()
                        }
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = true
                        }
                        val ret = rtcEngine.updateChannelMediaOptionsEx(options, connection)
                        VideoLoader.videoLoaderApiLog(tag, "updateChannelMediaOptionsEx, connection:$connection, ret:$ret")
                        rtcEngine.adjustUserPlaybackSignalVolumeEx(anchorUid, 100, connection)
                    }
                    (oldState == AnchorState.JOINED || oldState == AnchorState.JOINED_WITHOUT_AUDIO)  && newState == AnchorState.PRE_JOINED -> {
                        // 保持在频道内，不收流
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = false
                            autoSubscribeAudio = false
                        }
                        val ret = rtcEngine.updateChannelMediaOptionsEx(options, connection)
                        remoteVideoCanvasList.filter { it.connection.channelId == connection.channelId }.forEach { it.release() }
                        VideoLoader.videoLoaderApiLog(tag, "updateChannelMediaOptionsEx, connection:$connection, ret:$ret")
                    }
                    oldState == AnchorState.IDLE && newState == AnchorState.JOINED -> {
                        // 加入频道，且收流
                        getProfiler(roomId).actualStartTime = System.currentTimeMillis()
                        joinRtcChannel(token, connection, mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = true
                        })
                    }
                    oldState == AnchorState.IDLE && newState == AnchorState.JOINED_WITHOUT_AUDIO -> {
                        // 加入频道，且收流
                        getProfiler(roomId).actualStartTime = System.currentTimeMillis()
                        joinRtcChannel(token, connection, mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = true
                        })
                        // 防止音画不同步， 我们采用先订阅再将播放调为0的方式
                        rtcEngine.adjustUserPlaybackSignalVolumeEx(anchorUid, 0, connection)
                    }
                    oldState == AnchorState.PRE_JOINED && newState == AnchorState.JOINED_WITHOUT_AUDIO -> {
                        // 保持在频道内, 收流
                        getProfiler(roomId).actualStartTime = System.currentTimeMillis()
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = true
                        }
                        val ret = rtcEngine.updateChannelMediaOptionsEx(options, connection)
                        VideoLoader.videoLoaderApiLog(tag, "updateChannelMediaOptionsEx, connection:$connection, ret:$ret")
                        // 防止音画不同步， 我们采用先订阅再将播放调为0的方式
                        rtcEngine.adjustUserPlaybackSignalVolumeEx(anchorUid, 0, connection)
                    }
                    newState == AnchorState.IDLE -> {
                        // 退出频道
                        leaveRtcChannel(it.key)
                    }
                }
                return
            }
        }
    }

    private fun joinRtcChannel(token: String?, connection: RtcConnection, options: ChannelMediaOptions) {
        val ret = rtcEngine.joinChannelEx(token, connection, options, getProfiler(connection.channelId))
        VideoLoader.videoLoaderApiLog(tag, "joinRtcChannel, connection:$connection, ret:$ret")
    }

    private fun leaveRtcChannel(connection: RtcConnectionWrap) {
        val ret = rtcEngine.leaveChannelEx(connection)
        VideoLoader.videoLoaderApiLog(tag, "leaveChannel ret: connection=$connection, code=$ret, message=${RtcEngine.getErrorDescription(ret)}")
        remoteVideoCanvasList.filter { it.connection.channelId == connection.channelId }.forEach { it.release() }
        videoProfileMap.remove(connection.channelId)
    }

    private fun printTracingInfo(tracingInfo: VideoRenderingTracingInfo?): String {
        val info = tracingInfo ?: return ""
        return "elapsedTime:${info.elapsedTime} start2JoinChannel:${info.start2JoinChannel} join2JoinSuccess:${info.join2JoinSuccess} joinSuccess2RemoteJoined:${info.joinSuccess2RemoteJoined} remoteJoined2SetView:${info.remoteJoined2SetView} remoteJoined2UnmuteVideo:${info.remoteJoined2UnmuteVideo} remoteJoined2PacketReceived:${info.remoteJoined2PacketReceived}"
    }

    inner class RtcConnectionWrap constructor(connection: RtcConnection) :
        RtcConnection(connection.channelId, connection.localUid) {

        fun isSameChannel(connection: RtcConnection?) =
            connection != null && channelId == connection.channelId && localUid == connection.localUid

        override fun toString(): String {
            return "{channelId=$channelId, localUid=$localUid}"
        }
    }

    inner class RemoteVideoCanvasWrap constructor(
        val connection: RtcConnection,
        val lifecycleOwner: LifecycleOwner,
        view: View,
        renderMode: Int,
        uid: Int
    ) : DefaultLifecycleObserver, VideoCanvas(view, renderMode, uid) {

        init {
            VideoLoader.videoLoaderApiLog(tag, "new video canvas $this")
            setupMode = VIEW_SETUP_MODE_ADD
            lifecycleOwner.lifecycle.addObserver(this)
            remoteVideoCanvasList.add(this)
        }

        override fun toString(): String {
            return "connection:$connection, lifecycleOwner:$lifecycleOwner, view:$view, renderMode:$renderMode, remoteUid:$uid"
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            if (lifecycleOwner == owner) {
                release()
            }
        }

        fun release() {
            lifecycleOwner.lifecycle.removeObserver(this)
            setupMode = VIEW_SETUP_MODE_REMOVE
            VideoLoader.videoLoaderApiLog(tag, "release video canvas $this")
            rtcEngine.setupRemoteVideoEx(this, connection)
            remoteVideoCanvasList.remove(this)
        }
    }

    inner class VideoLoaderProfiler(
        private val channelId: String
    ) : IRtcEngineEventHandler() {
        private val tag = "VideoLoaderProfiler"
        var actualStartTime: Long = 0
        var perceivedStartTime: Long = 0
        var reportExt: MutableMap<String, Any> = HashMap()
        var firstFrameCompletion: ((Long, Int) -> Unit)? = null

        init {
            this.actualStartTime = 0
            this.perceivedStartTime = 0
        }

        override fun onRemoteVideoStateChanged(
            uid: Int,
            state: Int,
            reason: Int,
            elapsed: Int
        ) {
            Log.d(tag, "remoteVideoStateChangedOfUid[$channelId]: $uid state: $state reason: $reason")
            if (state == 2 && (reason == 6 || reason == 4 || reason == 3)) {
                val currentTs = System.currentTimeMillis()
                val actualCost = currentTs - actualStartTime
                val perceivedCost = currentTs - perceivedStartTime

                Log.d(tag, "channelId[$channelId] uid[$uid] show first frame! actualCost: $actualCost ms perceivedCost: $perceivedCost ms")
                val ext = reportExt.toMutableMap()
                ext["channelName"] = channelId
                VideoLoader.reporter?.reportCostEvent(
                    ApiCostEvent.FIRST_FRAME_ACTUAL,
                    actualCost.toInt(),
                    ext
                )
                if (perceivedStartTime > 0) {
                    VideoLoader.reporter?.reportCostEvent(
                        ApiCostEvent.FIRST_FRAME_PERCEIVED,
                        perceivedCost.toInt(),
                        ext
                    )
                }
                firstFrameCompletion?.invoke(actualCost, uid)
            }
        }

        override fun onVideoRenderingTracingResult(
            uid: Int,
            currentEvent: Constants.MEDIA_TRACE_EVENT?,
            tracingInfo: VideoRenderingTracingInfo?
        ) {
            super.onVideoRenderingTracingResult(uid, currentEvent, tracingInfo)
            VideoLoader.videoLoaderApiLog(tag, "onVideoRenderingTracingResult channel: $channelId, uid: $uid, currentEvent: $currentEvent, tracingInfo: ${printTracingInfo(tracingInfo)}")
        }
    }
}