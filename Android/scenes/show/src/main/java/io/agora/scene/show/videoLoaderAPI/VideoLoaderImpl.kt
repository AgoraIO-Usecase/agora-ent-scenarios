package io.agora.scene.show.videoLoaderAPI

import android.content.Context
import android.util.Log
import android.view.TextureView
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import java.util.*

/**
 * 房间状态
 * @param IDLE 默认状态
 * @param PREJOINED 预加入房间状态
 * @param JOINED 已进入房间状态
 * @param JOINED_WITHOUT_AUDIO 不订阅音频
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
    private var needSubscribe = false
    private var needSubscribeConnection: RtcConnection? = null

    override fun cleanCache() {
        anchorStateMap.forEach {
            innerSwitchAnchorState(AnchorState.IDLE, it.key, null, null, null, null)
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
        context: Context?
    ) {
        innerSwitchAnchorState(newState, RtcConnection(anchorInfo.channelId, uid), anchorInfo.token, anchorInfo.anchorUid, null, context)
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
            it.release()
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
        connection: RtcConnection,
        token: String?,
        ownerUid: Int?,
        mediaOptions: ChannelMediaOptions?,
        context: Context?) {
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

                    val owner = ownerUid ?: return
                    context?.let {
                        val videoView = TextureView(it)
                        val remoteVideoCanvasWrap = RemoteVideoCanvasWrap(
                            connection,
                            context as LifecycleOwner,
                            videoView,
                            Constants.RENDER_MODE_HIDDEN,
                            owner
                        )
                        Log.d("hugo", "setupRemoteVideoEx777")
                        rtcEngine.setupRemoteVideoEx(
                            remoteVideoCanvasWrap,
                            connection
                        )
                    }
                }
                AnchorState.JOINED_WITHOUT_AUDIO -> {
                    // 加入频道只收音频流
                    val options = mediaOptions ?: ChannelMediaOptions().apply {
                        // 加入频道只收音频流
                        clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                        audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                        autoSubscribeVideo = true
                        autoSubscribeAudio = false
                    }
                    val ret = rtcEngine.joinChannelEx(token, connection, options, object : IRtcEngineEventHandler() {})
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
                        if (ret == -8) {
                            needSubscribe = true
                            needSubscribeConnection = connection
                        } else {
                            val owner = ownerUid ?: return
                            context?.let {
                                if (remoteVideoCanvasList.none {it.connection.channelId == connection.channelId && it.connection.localUid == connection.localUid}) {
                                    val videoView = TextureView(it)
                                    val remoteVideoCanvasWrap = RemoteVideoCanvasWrap(
                                        connection,
                                        context as LifecycleOwner,
                                        videoView,
                                        Constants.RENDER_MODE_HIDDEN,
                                        owner
                                    )
                                    Log.d("hugo", "setupRemoteVideoEx888")
                                    rtcEngine.setupRemoteVideoEx(
                                        remoteVideoCanvasWrap,
                                        connection
                                    )
                                }
                            }
                        }
                        Log.d(tag, "updateChannelMediaOptionsEx, connection:$connection, ret:$ret")
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
                        val owner = ownerUid ?: return
                        context?.let {
                            if (remoteVideoCanvasList.none {it.connection.channelId == connection.channelId && it.connection.localUid == connection.localUid}) {
                                val videoView = TextureView(it)
                                val remoteVideoCanvasWrap = RemoteVideoCanvasWrap(
                                    connection,
                                    context as LifecycleOwner,
                                    videoView,
                                    Constants.RENDER_MODE_HIDDEN,
                                    owner
                                )
                                Log.d("hugo", "setupRemoteVideoEx888")
                                rtcEngine.setupRemoteVideoEx(
                                    remoteVideoCanvasWrap,
                                    connection
                                )
                            }
                        }
                        Log.d(tag, "joinChannelEx1, connection:$connection, ret:$ret")
                    }
                    oldState == AnchorState.IDLE && newState == AnchorState.JOINED_WITHOUT_AUDIO -> {
                        // 加入频道，且收流
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = false
                        }
                        val ret = rtcEngine.joinChannelEx(token, connection, options, object : IRtcEngineEventHandler() {})
                        Log.d(tag, "joinChannelEx1, connection:$connection, ret:$ret")
                    }
                    oldState == AnchorState.PRE_JOINED && newState == AnchorState.JOINED_WITHOUT_AUDIO -> {
                        // 保持在频道内, 收流
                        val options = mediaOptions ?: ChannelMediaOptions().apply {
                            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                            audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                            autoSubscribeVideo = true
                            autoSubscribeAudio = false
                        }
                        val ret = rtcEngine.updateChannelMediaOptionsEx(options, connection)
                        if (ret == -8) {
                            needSubscribe = true
                            needSubscribeConnection = connection
                        }
                        Log.d(tag, "updateChannelMediaOptionsEx, connection:$connection, ret:$ret")
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

        var audioMixingPlayer : IMediaPlayer? = null

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
            Log.d(tag, "RemoteVideoCanvasWrap release: $connection")
            lifecycleOwner.lifecycle.removeObserver(this)
            view = null
            remoteVideoCanvasList.remove(this)
        }
    }

    // TODO
    override fun startAudioMixing(
        connection: RtcConnection,
        filePath: String,
        loopbackOnly: Boolean,
        cycle: Int
    ) {
        // 判断connetion是否加入了频道，即connectionsJoined是否包含，不包含则直接返回
        anchorStateMap.forEach {
            if (it.key.isSameChannel(connection) && it.value == AnchorState.JOINED) {
                val connectionWrap = it.key
                // 播放使用MPK，rtcEngine.createMediaPlayer
                // 使用一个Map缓存起来key:RtcConnection, value:MediaPlayer
                // 从缓存里取MediaPlayer，如不存在则重新创建
                // val mediaPlayer = rtcEngine.createMediaPlayer()
                val mediaPlayer = connectionWrap.audioMixingPlayer ?: rtcEngine.createMediaPlayer().apply {
                    registerPlayerObserver(object : IMediaPlayerObserver {
                        override fun onPlayerStateChanged(
                            state: io.agora.mediaplayer.Constants.MediaPlayerState?,
                            error: io.agora.mediaplayer.Constants.MediaPlayerError?
                        ) {
                            if(error == io.agora.mediaplayer.Constants.MediaPlayerError.PLAYER_ERROR_NONE){
                                if(state == io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED){
                                    play()
                                }
                            }
                        }

                        override fun onPositionChanged(position_ms: Long, timestamp_ms: Long) {

                        }

                        override fun onPlayerEvent(
                            eventCode: io.agora.mediaplayer.Constants.MediaPlayerEvent?,
                            elapsedTime: Long,
                            message: String?
                        ) {

                        }

                        override fun onMetaData(
                            type: io.agora.mediaplayer.Constants.MediaPlayerMetadataType?,
                            data: ByteArray?
                        ) {

                        }

                        override fun onPlayBufferUpdated(playCachedBuffer: Long) {

                        }

                        override fun onPreloadEvent(
                            src: String?,
                            event: io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent?
                        ) {

                        }

                        override fun onAgoraCDNTokenWillExpire() {

                        }

                        override fun onPlayerSrcInfoChanged(from: SrcInfo?, to: SrcInfo?) {

                        }

                        override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo?) {

                        }

                        override fun onAudioVolumeIndication(volume: Int) {

                        }
                    })
                }
                connectionWrap.audioMixingPlayer = mediaPlayer
                mediaPlayer.stop()
                mediaPlayer.open(filePath, 0)
                mediaPlayer.setLoopCount(if (cycle >= 0) 0 else Int.MAX_VALUE)

                // 开始推流，使用updateChannelMediaOptionEx
                // 使用一个Map缓存ChannelMediaOptions--key:RtcConnection, value:ChannelMediaOptions
                // val channelMediaOptions = ChannelMediaOptions()
                // channelMediaOptions.publishMediaPlayerId = mediaPlayer.getId()
                // channelMediaOptions.publishMediaPlayerAudioTrack = true
                // rtcEngine.updateChannelMediaOptionsEx(channelMediaOptions, connection)
                if(!loopbackOnly){
                    val mediaOptions = ChannelMediaOptions()
                    mediaOptions.publishMediaPlayerId = mediaPlayer.mediaPlayerId
                    // TODO: 没开启麦克风权限情况下，publishMediaPlayerAudioTrack = true 会自动停止音频播放
                    mediaOptions.publishMediaPlayerAudioTrack = true
                    rtcEngine.updateChannelMediaOptionsEx(mediaOptions, connectionWrap)
                }
            }
        }
    }

    override fun stopAudioMixing(connection: RtcConnection) {
        // 判断connetion是否加入了频道，即connectionsJoined是否包含，不包含则直接返回
        anchorStateMap.forEach {
            if (it.key.isSameChannel(connection) && it.value == AnchorState.JOINED) {
                val connectionWrap =
                    it.key

                // 停止播放，拿到connection对应的MediaPlayer并停止释放
                connectionWrap.audioMixingPlayer?.stop()

                // 停止推流，使用updateChannelMediaOptionEx
                val mediaOptions = ChannelMediaOptions()
                if (mediaOptions.isPublishMediaPlayerAudioTrack) {
                    mediaOptions.publishMediaPlayerAudioTrack = false
                    rtcEngine.updateChannelMediaOptionsEx(mediaOptions, connectionWrap)
                }
            }
        }
    }

    override fun adjustAudioMixingVolume(connection: RtcConnection, volume: Int) {
        anchorStateMap.forEach {
            if (it.key.isSameChannel(connection) && it.value == AnchorState.JOINED) {
                val connectionWrap =
                    it.key
                connectionWrap.audioMixingPlayer?.adjustPlayoutVolume(volume)
                connectionWrap.audioMixingPlayer?.adjustPublishSignalVolume(volume)
            }
        }
    }
}