package io.agora.scene.show.videoLoaderAPI

import android.util.Log
import android.view.TextureView
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import java.util.*

/**
 * 房间状态
 * @param IDLE 默认状态
 * @param PRE_JOINED 预加入房间状态
 * @param JOINED 已进入房间状态
 * @param JOINED_WITHOUT_AUDIO 不播放音频
 */
enum class AnchorState {
    IDLE,
    PRE_JOINED,
    JOINED,
    JOINED_WITHOUT_AUDIO,
}

class VideoLoaderImpl constructor(private val rtcEngine: RtcEngineEx) : VideoLoader {
    private val tag = "VideoLoaderImpl"
    private val anchorStateMap = Collections.synchronizedMap(mutableMapOf<RtcConnectionWrap, AnchorState>())
    private val remoteVideoCanvasList = Collections.synchronizedList(mutableListOf<RemoteVideoCanvasWrap>())

    override fun cleanCache() {
        anchorStateMap.forEach {
            innerSwitchAnchorState(AnchorState.IDLE, 0, it.key, null, null)
        }
        anchorStateMap.clear()
    }

    override fun preloadAnchor(anchorList: List<VideoLoader.AnchorInfo>, uid: Int) {
        anchorList.forEach {
            rtcEngine.preloadChannel(it.token, it.channelId, uid)
        }
    }

    override fun switchAnchorState(
        newState: AnchorState,
        anchorInfo: VideoLoader.AnchorInfo,
        uid: Int,
    ) {
        innerSwitchAnchorState(newState, anchorInfo.anchorUid, RtcConnection(anchorInfo.channelId, uid), anchorInfo.token, null)
    }

    override fun renderVideo(anchorInfo: VideoLoader.AnchorInfo, localUid: Int, container: VideoLoader.VideoCanvasContainer) {
        Log.d(tag, "renderVideo called: $anchorInfo")
        remoteVideoCanvasList.firstOrNull {
            it.connection.channelId == anchorInfo.channelId && it.uid == container.uid && it.renderMode == container.renderMode && it.lifecycleOwner == container.lifecycleOwner
        }?.let {
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
        anchorStateMap.forEach {
            if (it.key.isSameChannel(connection)) {
                val connectionWrap = it.key
                val remoteVideoCanvasWrap = RemoteVideoCanvasWrap(
                    connectionWrap,
                    container.lifecycleOwner,
                    videoView,
                    container.renderMode,
                    container.uid
                )
                rtcEngine.setupRemoteVideoEx(
                    remoteVideoCanvasWrap,
                    connectionWrap
                )
                return
            }
        }

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

    private fun innerSwitchAnchorState(
        newState: AnchorState,
        anchorUid: Int,
        connection: RtcConnection,
        token: String?,
        mediaOptions: ChannelMediaOptions?
    ) {
        Log.d(tag, "innerSwitchAnchorState, newState: $newState, connection: $connection, anchorStateMap: $anchorStateMap")
        // anchorStateMap 无当前主播记录
        if (anchorStateMap.none {it.key.isSameChannel(connection)}) {
            val rtcConnectionWrap = RtcConnectionWrap(connection)
            when (newState) {
                AnchorState.PRE_JOINED -> {
                    // 加入频道但不收流
                    val options = mediaOptions ?: ChannelMediaOptions().apply {
                        clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        autoSubscribeVideo = false
                        autoSubscribeAudio = false
                    }
                    val ret = rtcEngine.joinChannelEx(token, connection, options, object : IRtcEngineEventHandler() {})
                    Log.d(tag, "joinChannel PRE_JOINED, connection:$connection, ret:$ret")
                }
                AnchorState.JOINED -> {
                    // 加入频道且收流
                    val options = mediaOptions ?: ChannelMediaOptions().apply {
                        // 加入频道且收流
                        clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        autoSubscribeVideo = true
                        autoSubscribeAudio = true
                    }
                    val ret = rtcEngine.joinChannelEx(token, connection, options, object : IRtcEngineEventHandler() {})
                    Log.d(tag, "joinChannel JOINED, connection:$connection, ret:$ret")
                }
                AnchorState.JOINED_WITHOUT_AUDIO -> {
                    val options = mediaOptions ?: ChannelMediaOptions().apply {
                        clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        autoSubscribeVideo = true
                        autoSubscribeAudio = true
                    }
                    val ret = rtcEngine.joinChannelEx(token, connection, options, object : IRtcEngineEventHandler() {})
                    // 防止音画不同步， 我们采用先订阅再将播放调为0的方式
                    rtcEngine.adjustUserPlaybackSignalVolumeEx(anchorUid, 0, connection)
                    Log.d(tag, "joinChannel JOINED_WITHOUT_AUDIO, connection:$connection, ret:$ret")
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
                    Log.d(tag, "switchAnchorState is already this state")
                    return
                }
                anchorStateMap[it.key] = newState
                when {
                    oldState == AnchorState.IDLE && newState == AnchorState.PRE_JOINED -> {
                        // 加入频道但不收流
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = false
                            autoSubscribeAudio = false
                        }
                        val ret = rtcEngine.joinChannelEx(token, connection, options, object : IRtcEngineEventHandler() {})
                        Log.d(tag, "joinChannel PRE_JOINED, connection:$connection, ret:$ret")
                    }
                    (oldState == AnchorState.PRE_JOINED || oldState == AnchorState.JOINED_WITHOUT_AUDIO) && newState == AnchorState.JOINED -> {
                        // 保持在频道内, 收流
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = true
                        }
                        val ret = rtcEngine.updateChannelMediaOptionsEx(options, connection)
                        Log.d(tag, "updateChannelMediaOptionsEx, connection:$connection, ret:$ret")
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
                        Log.d(tag, "updateChannelMediaOptionsEx, connection:$connection, ret:$ret")
                    }
                    oldState == AnchorState.IDLE && newState == AnchorState.JOINED -> {
                        // 加入频道，且收流
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = true
                        }
                        val ret = rtcEngine.joinChannelEx(token, connection, options, object : IRtcEngineEventHandler() {})
                        Log.d(tag, "joinChannelEx1, connection:$connection, ret:$ret")
                    }
                    oldState == AnchorState.IDLE && newState == AnchorState.JOINED_WITHOUT_AUDIO -> {
                        // 加入频道，且收流
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = true
                        }
                        val ret = rtcEngine.joinChannelEx(token, connection, options, object : IRtcEngineEventHandler() {})
                        Log.d(tag, "joinChannelEx1, connection:$connection, ret:$ret")
                        // 防止音画不同步， 我们采用先订阅再将播放调为0的方式
                        rtcEngine.adjustUserPlaybackSignalVolumeEx(anchorUid, 0, connection)
                    }
                    oldState == AnchorState.PRE_JOINED && newState == AnchorState.JOINED_WITHOUT_AUDIO -> {
                        // 保持在频道内, 收流
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = true
                        }
                        val ret = rtcEngine.updateChannelMediaOptionsEx(options, connection)
                        Log.d(tag, "updateChannelMediaOptionsEx, connection:$connection, ret:$ret")
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

    override fun getRoomState(channelId: String, localUid: Int): AnchorState? {
        anchorStateMap.forEach {
            if (it.key.isSameChannel(RtcConnection(channelId, localUid))) {
                return it.value
            }
        }
        return null
    }

    private fun leaveRtcChannel(connection: RtcConnectionWrap) {
        val ret = rtcEngine.leaveChannelEx(connection)
        Log.d(
            tag,
            "leaveChannel ret : connection=$connection, code=$ret, message=${RtcEngine.getErrorDescription(ret)}"
        )
        remoteVideoCanvasList.filter { it.connection.channelId == connection.channelId }.forEach { it.release() }
    }

    inner class RtcConnectionWrap constructor(connection: RtcConnection) :
        RtcConnection(connection.channelId, connection.localUid) {

        fun isSameChannel(connection: RtcConnection?) =
            connection != null && channelId == connection.channelId && localUid == connection.localUid

        override fun toString(): String {
            return "{channelId=$channelId, localUid=$localUid"
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
            setupMode = VIEW_SETUP_MODE_ADD
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
            setupMode = VIEW_SETUP_MODE_REMOVE
            rtcEngine.setupRemoteVideoEx(this, connection)
            remoteVideoCanvasList.remove(this)
        }
    }
}