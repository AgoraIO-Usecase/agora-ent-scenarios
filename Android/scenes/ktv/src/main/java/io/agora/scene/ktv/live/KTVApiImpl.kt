package io.agora.scene.ktv.live

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.musiccontentcenter.*
import io.agora.rtc2.*
import io.agora.rtc2.Constants.*
import io.agora.rtc2.audio.AudioParams
import io.agora.scene.ktv.KTVLogger
import io.agora.scene.ktv.widget.LrcControlView
import org.json.JSONException
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

class KTVApiImpl : KTVApi, IMusicContentCenterEventHandler, IMediaPlayerObserver,
    IRtcEngineEventHandler(), IAudioFrameObserver {
    private val TAG: String = "KTV_API_LOG"
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private lateinit var mRtcEngine: RtcEngineEx
    private lateinit var mMusicCenter: IAgoraMusicContentCenter
    private lateinit var mPlayer: IAgoraMusicPlayer

    private var localUid: Int = 0
    private var channelName: String = ""
    private var dataStreamId: Int = 0
    private var songConfig: KTVSongConfiguration? = null
    private var subChorusConnection: RtcConnection? = null

    private val loadSongMap = mutableMapOf<String, KTVLoadSongState>()
    private val lyricUrlMap = mutableMapOf<String, String>() // (songCode, lyricUrl)
    private val lyricCallbackMap =
        mutableMapOf<String, (lyricUrl: String?) -> Unit>() // (requestId, callback)
    private val loadMusicCallbackMap =
        mutableMapOf<String, (isPreload: Int?) -> Unit>() // (songNo, callback)

    private var lrcView: LrcControlView? = null

    private var localPlayerPosition: Long = 0
    private var localPlayerSystemTime: Long = 0

    //歌词实时刷新
    private var mStopDisplayLrc = true
    private var mDisplayThread: Thread? = null
    private var mReceivedPlayPosition: Long = 0 //播放器播放position，ms
    private var mLastReceivedPlayPosTime: Long? = null

    // event
    private var ktvApiEventHandler: IKTVApiEventHandler? = null
    private var mainSingerHasJoinChannelEx: Boolean = false

    // 合唱校准
    private var audioPlayoutDelay = 0
    private var remoteVolume: Int = 15 // 远端音频
    private var mpkPlayoutVolume: Int = 50
    private var mpkPublishVolume: Int = 50

    // 音高
    private var pitch = 0.0

    // 是否在麦上
    private var isOnMicOpen = false
    private var isRelease = false

    override fun initialize(
        config: KTVApiConfig
    ) {
        this.mRtcEngine = config.engine as RtcEngineEx
        this.channelName = config.channelName
        this.dataStreamId = config.dataStreamId
        this.localUid = config.localUid

        // ------------------ 初始化内容中心 ------------------
        val contentCenterConfiguration = MusicContentCenterConfiguration()
        contentCenterConfiguration.appId = config.appId
        contentCenterConfiguration.mccUid = localUid.toLong()
        contentCenterConfiguration.token = config.rtmToken
        mMusicCenter = IAgoraMusicContentCenter.create(mRtcEngine)
        mMusicCenter.initialize(contentCenterConfiguration)

        // ------------------ 初始化音乐播放器实例 ------------------
        mPlayer = mMusicCenter.createMusicPlayer()

        // 注册回调
        mRtcEngine.addHandler(this)
        mPlayer.registerPlayerObserver(this)
        mMusicCenter.registerEventHandler(this)

        // 音量最佳实践调整
//        mPlayer.adjustPlayoutVolume(config.defaultMediaPlayerVolume)
//        mPlayer.adjustPublishSignalVolume(config.defaultMediaPlayerVolume)
//        remoteVolume = config.defaultChorusRemoteUserVolume
    }

    override fun addEventHandler(ktvApiEventHandler: IKTVApiEventHandler) {
        this.ktvApiEventHandler = ktvApiEventHandler
    }

    override fun removeEventHandler(ktvApiEventHandler: IKTVApiEventHandler) {
        //TODO("Not yet implemented")
    }

    override fun release() {
        isRelease = true

        loadSongMap.clear()
        lyricUrlMap.clear()
        lyricCallbackMap.clear()
        loadMusicCallbackMap.clear()
        lrcView = null

        mRtcEngine.removeHandler(this)
        mPlayer.unRegisterPlayerObserver(this)
        mMusicCenter.unregisterEventHandler()

        mPlayer.stop()
        mPlayer.destroy()
        IAgoraMusicContentCenter.destroy()

        mainSingerHasJoinChannelEx = false
        dataStreamId = 0
    }

    override fun getMusicCharts(): String {
        return mMusicCenter.musicCharts
    }

    override fun getMusicCollectionByMusicChartId(
        musicChartId: Int,
        page: Int,
        pageSize: Int,
        jsonOption: String
    ): String {
        return mMusicCenter.getMusicCollectionByMusicChartId(musicChartId, page, pageSize, jsonOption)
    }

    override fun searchMusic(
        keyword: String,
        page: Int,
        pageSize: Int,
        jsonOption: String
    ): String {
        return mMusicCenter.searchMusic(keyword, page, pageSize, jsonOption)
    }

    override fun loadSong(
        config: KTVSongConfiguration,
    ) {
        KTVLogger.d(TAG, "loadSong called")
        this.songConfig = config
        val role = config.role
        val songCode = config.songCode

        // TODO 验证下
        // 已经loadSong过的歌曲
        if (loadSongMap.containsKey(songCode.toString())) {
            when (val state = loadSongMap[songCode.toString()]) {
                KTVLoadSongState.KTVLoadSongStateOK -> {
                    val url = lyricUrlMap[songCode.toString()] ?: return
                    ktvApiEventHandler?.onMusicLoadStateChanged(songCode, url, role, state)
                    return
                }
                KTVLoadSongState.KTVLoadSongStateInProgress -> {
                    ktvApiEventHandler?.onMusicLoadStateChanged(songCode, "", role, state)
                    return
                }
                else -> {}
            }
        }

        // 没有loadSong的歌曲
        loadSongMap[songCode.toString()] = KTVLoadSongState.KTVLoadSongStateInProgress
        ktvApiEventHandler?.onMusicLoadStateChanged(songCode, "", role, KTVLoadSongState.KTVLoadSongStateInProgress)

        var state = KTVLoadSongState.KTVLoadSongStateInProgress
        val countDownLatch = CountDownLatch(2)

        when (role) {
            KTVSingRole.KTVSingRoleMainSinger -> {
                KTVLogger.d(TAG, "loadSong called KTVSingRoleMainSinger")
                // 获取歌词url
                loadLyric(songCode) { lyricUrl ->
                    if (lyricUrl == null) {
                        loadSongMap.remove(songCode.toString())
                        state = KTVLoadSongState.KTVLoadSongStateNoLyricUrl
                    } else {
                        lyricUrlMap[songCode.toString()] = lyricUrl
                        setLrcLyric(lyricUrl) { }
                    }
                    countDownLatch.countDown()
                }

                // 预加载歌曲
                loadMusic(songCode) { status ->
                    if (status != 0) {
                        loadSongMap.remove(songCode.toString())
                        state = KTVLoadSongState.KTVLoadSongStatePreloadFail
                    }
                    countDownLatch.countDown()
                }
            }
            KTVSingRole.KTVSingRoleCoSinger -> {
                // 合唱需要在joinChorus前loadSong
                KTVLogger.d(TAG, "loadSong called KTVSingRoleCoSinger")
                // 获取歌词url
                loadLyric(songCode) { lyricUrl ->
                    if (lyricUrl == null) {
                        loadSongMap.remove(songCode.toString())
                        state = KTVLoadSongState.KTVLoadSongStateNoLyricUrl
                    } else {
                        lyricUrlMap[songCode.toString()] = lyricUrl
                        setLrcLyric(lyricUrl) { }
                    }
                    countDownLatch.countDown()
                }

                // 预加载歌曲
                loadMusic(songCode) { status ->
                    if (status != 0) {
                        loadSongMap.remove(songCode.toString())
                        state = KTVLoadSongState.KTVLoadSongStatePreloadFail
                    }
                    countDownLatch.countDown()
                }
            }
            KTVSingRole.KTVSingRoleAudience -> {
                KTVLogger.d(TAG, "loadSong called KTVSingRoleAudience")
                // 获取歌词url
                loadLyric(songCode) { lyricUrl ->
                    if (lyricUrl == null) {
                        loadSongMap.remove(songCode.toString())
                        state = KTVLoadSongState.KTVLoadSongStateNoLyricUrl
                    } else {
                        lyricUrlMap[songCode.toString()] = lyricUrl
                        setLrcLyric(lyricUrl) { }
                    }
                    countDownLatch.countDown()
                    countDownLatch.countDown()
                }
            }
            else -> {
                KTVLogger.d(TAG, "loadSong called wrong role")
            }
        }

        Thread {
            countDownLatch.await()
            val url = lyricUrlMap[songCode.toString()] ?: return@Thread
            if (state == KTVLoadSongState.KTVLoadSongStateInProgress) {
                loadSongMap[songCode.toString()] = KTVLoadSongState.KTVLoadSongStateOK
                state = KTVLoadSongState.KTVLoadSongStateOK
                ktvApiEventHandler?.onMusicLoadStateChanged(songCode, url, role, state)
            } else {
                ktvApiEventHandler?.onMusicLoadStateChanged(songCode, url, role, state)
            }
        }.start()
    }

    override fun startSing(startPos: Long) {
        KTVLogger.d(TAG, "playSong called")
        val config = songConfig ?: return

        // reset status
        stopDisplayLrc()
        this.mLastReceivedPlayPosTime = null
        this.mReceivedPlayPosition = 0
        startDisplayLrc()

        val role = config.role
        val songCode = config.songCode

        when (role) {
            KTVSingRole.KTVSingRoleMainSinger -> {
                KTVLogger.d(TAG, "KTVSongTypeChorus,KTVSingRoleMainSinger playSong")
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = true
                channelMediaOption.publishMediaPlayerId = mPlayer.mediaPlayerId
                channelMediaOption.publishMediaPlayerAudioTrack = true
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)

                mRtcEngine.setDirectExternalAudioSource(true)
                mRtcEngine.setRecordingAudioFrameParameters(48000, 2, 0, 960)
                mRtcEngine.registerAudioFrameObserver(this)

                mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
                mPlayer.open(songCode, startPos)
            }
            KTVSingRole.KTVSingRoleAudience -> {
                KTVLogger.d(TAG, "KTVSongTypeChorus,KTVSingRoleAudience playSong")
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = true
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
            }
            else -> {}
        }
    }

    override fun joinChorus(token: String, role: KTVSingRole) {
        val oldConfig = songConfig ?: return
        Log.d(TAG, "joinChorus: $role")

        // 主唱已经加入合唱模式（2ndChannel）
        if (oldConfig.role == KTVSingRole.KTVSingRoleMainSinger && mainSingerHasJoinChannelEx) {
            Log.e(TAG, "joinChorus failed: mainSingerHasJoinChannelEx")
            return
        }

        // config 更新
        this.songConfig = KTVSongConfiguration(
            role,
            oldConfig.songCode,
            oldConfig.mainSingerUid,
        )

        val songCode = oldConfig.songCode
        when (role) {
            KTVSingRole.KTVSingRoleMainSinger -> {
                Log.d(TAG, "joinChorus: KTVSingRoleMainSinger")
                joinChorus2ndChannel(token)
            }
            KTVSingRole.KTVSingRoleCoSinger -> {
                Log.d(TAG, "joinChorus: KTVSingRoleCoSinger")
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = true
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
                joinChorus2ndChannel(token)

                // 音量最佳实践调整
                mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)

                mPlayer.open(songCode, 0)
            }
            KTVSingRole.KTVSingRoleFollowSinger -> {
                Log.d(TAG, "joinChorus: KTVSingRoleCoSinger")
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = true
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
                joinChorus2ndChannel(token)

                // 音量最佳实践调整

                mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
                mRtcEngine.enableInEarMonitoring(true, EAR_MONITORING_FILTER_NONE)
                mRtcEngine.muteLocalAudioStream(true)

                mPlayer.open(songCode, 0)
            }
            KTVSingRole.KTVSingRoleAudience -> {
                Log.d(TAG, "joinChorus: Role is error!")
            }
        }
    }

    override fun leaveChorus() {
        Log.d(TAG, "leaveChorus")
        val config = songConfig ?: return

        when (config.role) {
            KTVSingRole.KTVSingRoleMainSinger -> {
                Log.d(TAG, "leaveChorus: KTVSingRoleMainSinger")
                leaveChorus2ndChannel()
            }
            KTVSingRole.KTVSingRoleCoSinger -> {
                Log.d(TAG, "leaveChorus: KTVSingRoleCoSinger")
                mPlayer.stop()
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = false
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
                leaveChorus2ndChannel()

                mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)

                // config 切成观众
                songConfig = KTVSongConfiguration(
                    KTVSingRole.KTVSingRoleAudience,
                    0,
                    config.mainSingerUid,
                )
            }
            KTVSingRole.KTVSingRoleFollowSinger -> {
                Log.d(TAG, "joinChorus: KTVSingRoleCoSinger")
                mRtcEngine.enableInEarMonitoring(false, EAR_MONITORING_FILTER_NONE)
                mRtcEngine.muteLocalAudioStream(false)

                mPlayer.stop()
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = false
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
                leaveChorus2ndChannel()

                mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)

                // config 切成观众
                songConfig = KTVSongConfiguration(
                    KTVSingRole.KTVSingRoleAudience,
                    0,
                    config.mainSingerUid,
                )
            }
            KTVSingRole.KTVSingRoleAudience -> {
                Log.d(TAG, "joinChorus: KTVSingRoleAudience does not need to leaveChorus!")
            }
        }
    }

    override fun stopSing() {
        KTVLogger.d(TAG, "stopSong called")
        mainSingerHasJoinChannelEx = false
        stopDisplayLrc()
        this.mLastReceivedPlayPosTime = null
        this.mReceivedPlayPosition = 0

        Thread {
            if (mPlayer.state != Constants.MediaPlayerState.PLAYER_STATE_STOPPED) {
                mPlayer.stop()
            }
            leaveChorus2ndChannel()
            mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
        }.start()
    }

    override fun resumeMusicPlayer() {
        KTVLogger.d(TAG, "resumePlay called")
        mPlayer.resume()
    }

    override fun pauseMusicPlayer() {
        KTVLogger.d(TAG, "pausePlay called")
        mPlayer.pause()
    }

    override fun seekMusicPlayer(time: Long) {
        KTVLogger.d(TAG, "seek called")
        mPlayer.seek(time)
        syncPlayProgress(time)
    }

    override fun adjustMusicPlayerPlayoutVolume(volume: Int) {
        this.mpkPlayoutVolume = volume
        mPlayer.adjustPlayoutVolume(volume)
    }

    override fun adjustMusicPlayerPublishVolume(volume: Int) {
        this.mpkPublishVolume = volume
        mPlayer.adjustPublishSignalVolume(volume)
    }

    override fun adjustPlaybackVolume(volume: Int) {
        remoteVolume = volume
        mRtcEngine.adjustPlaybackSignalVolume(volume)
    }

    override fun selectPlayerTrackMode(mode: KTVPlayerTrackMode) {
        KTVLogger.d(TAG, "selectTrackMode called")
        val trackMode = if (mode == KTVPlayerTrackMode.KTVPlayerTrackOrigin) 0 else 1
        mPlayer.selectAudioTrack(trackMode)
    }

    override fun setLycView(view: LrcControlView) {
        KTVLogger.d(TAG, "setLycView called")
        this.lrcView = view
    }

    override fun setIsMicOpen(isOnMicOpen: Boolean) {
        this.isOnMicOpen = isOnMicOpen
    }

    override fun getMediaPlayer(): IMediaPlayer {
        return mPlayer
    }

    override fun getMusicCenter(): IAgoraMusicContentCenter {
        return mMusicCenter
    }

    // ------------------ inner --------------------

    private fun isChorusCoSinger() : Boolean? {
        val config = songConfig ?: return null
        return config.role == KTVSingRole.KTVSingRoleCoSinger || config.role == KTVSingRole.KTVSingRoleFollowSinger
    }

    private fun sendStreamMessageWithJsonObject(
        obj: JSONObject,
        success: (isSendSuccess: Boolean) -> Unit
    ) {
        val ret = mRtcEngine.sendStreamMessage(dataStreamId, obj.toString().toByteArray())
        if (ret == 0) {
            success.invoke(true)
        } else {
            KTVLogger.e(TAG, "sendStreamMessageWithJsonObject failed: $ret")
        }
    }

    private fun syncPlayState(state: Constants.MediaPlayerState, error: Constants.MediaPlayerError) {
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "PlayerState"
        msg["state"] = Constants.MediaPlayerState.getValue(state)
        msg["error"] = Constants.MediaPlayerError.getValue(error)
        val jsonMsg = JSONObject(msg)
        sendStreamMessageWithJsonObject(jsonMsg) {}
    }

    private fun syncPlayProgress(time: Long) {
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "Seek"
        msg["position"] = time
        val jsonMsg = JSONObject(msg)
        sendStreamMessageWithJsonObject(jsonMsg) {}
    }

    private fun syncSingingScore(score: Float) {
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "SingingScore"
        msg["score"] = score.toDouble()
        val jsonMsg = JSONObject(msg)
        sendStreamMessageWithJsonObject(jsonMsg) {}
    }

    // 合唱
    private fun joinChorus2ndChannel(token: String) {
        Log.d(TAG, "joinChorus2ndChannel: token:$token")

        mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)

        val config = songConfig ?: return
        val role = config.role
        val channelMediaOption = ChannelMediaOptions()
        // main singer do not subscribe 2nd channel
        // co singer auto sub
        channelMediaOption.autoSubscribeAudio = role != KTVSingRole.KTVSingRoleMainSinger
        channelMediaOption.autoSubscribeVideo = false
        channelMediaOption.publishMicrophoneTrack = false
        channelMediaOption.enableAudioRecordingOrPlayout = role != KTVSingRole.KTVSingRoleMainSinger
        channelMediaOption.clientRoleType = CLIENT_ROLE_BROADCASTER
        channelMediaOption.publishDirectCustomAudioTrack = role == KTVSingRole.KTVSingRoleMainSinger

        val rtcConnection = RtcConnection()
        rtcConnection.channelId = channelName + "_ex"
        rtcConnection.localUid = localUid
        subChorusConnection = rtcConnection

        mRtcEngine.joinChannelEx(
            token,
            rtcConnection,
            channelMediaOption,
            object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    Log.d(TAG, "onJoinChannel2Success: channel:$channel, uid:$uid")
                    if (isRelease) return
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    if (role == KTVSingRole.KTVSingRoleMainSinger) {
                        mainSingerHasJoinChannelEx = true
                    }
                }

                override fun onLeaveChannel(stats: RtcStats?) {
                    Log.d(TAG, "onLeaveChannel2")
                    if (isRelease) return
                    super.onLeaveChannel(stats)
                    mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
                    if (role == KTVSingRole.KTVSingRoleMainSinger) {
                        mainSingerHasJoinChannelEx = false
                    }
                }
            }
        )

        if (config.role == KTVSingRole.KTVSingRoleCoSinger) {
            mRtcEngine.muteRemoteAudioStream(config.mainSingerUid, true)
        }
    }

    private fun leaveChorus2ndChannel() {
        val config = songConfig ?: return
        val role = config.role
        if (role == KTVSingRole.KTVSingRoleMainSinger) {
            val channelMediaOption = ChannelMediaOptions()
            channelMediaOption.publishDirectCustomAudioTrack = false
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption, subChorusConnection)
            mRtcEngine.leaveChannelEx(subChorusConnection)
        } else if (role == KTVSingRole.KTVSingRoleCoSinger) {
            mRtcEngine.leaveChannelEx(subChorusConnection)
            mRtcEngine.muteRemoteAudioStream(config.mainSingerUid, false)
        }
    }

    private fun setLrcLyric(lyricUrl: String, onSetLrcLyricCallback: (lyricUrl: String?) -> Unit) {
        if (lyricCallbackMap[lyricUrl] != null) {
            lyricCallbackMap[lyricUrl] = onSetLrcLyricCallback
        }
        lrcView?.downloadLrcData(lyricUrl)
    }

    // ------------------ 歌词播放、同步 ------------------
    // 开始播放歌词
    private fun startDisplayLrc() {
        KTVLogger.d(TAG, "startDisplayLrc called")
        mStopDisplayLrc = false
        mDisplayThread = Thread {
            while (!mStopDisplayLrc) {
                val lastReceivedTime = mLastReceivedPlayPosTime ?: continue
                val curTime = System.currentTimeMillis()
                val offset = curTime - lastReceivedTime
                if (offset <= 1000) {
                    val curTs = mReceivedPlayPosition + offset
                    runOnMainThread {
                        lrcView?.karaokeView?.setPitch(pitch.toFloat())
                        lrcView?.setProgress(curTs)
                    }
                }

                try {
                    Thread.sleep(50)
                } catch (exp: InterruptedException) {
                    break
                }
            }
        }
        mDisplayThread?.name = "Thread-Display"
        mDisplayThread?.start()
    }

    // 停止播放歌词
    private fun stopDisplayLrc() {
        KTVLogger.d(TAG, "stopDisplayLrc called")
        mStopDisplayLrc = true
        if (mDisplayThread != null) {
            try {
                mDisplayThread?.join()
                mDisplayThread = null
            } catch (exp: InterruptedException) {
                KTVLogger.d(TAG, "stopDisplayLrc: $exp")
            }
        }
    }

    private fun loadLyric(songNo: Long, onLoadLyricCallback: (lyricUrl: String?) -> Unit) {
        KTVLogger.d(TAG, "loadLyric: $songNo")
        val requestId = mMusicCenter.getLyric(songNo, 0)
        if (requestId.isEmpty()) {
            onLoadLyricCallback.invoke(null)
            return
        }
        lyricCallbackMap[requestId] = onLoadLyricCallback
    }

    private fun loadMusic(songNo: Long, onLoadMusicCallback: (status: Int?) -> Unit) {
        KTVLogger.d(TAG, "loadMusic: $songNo")
        val ret = mMusicCenter.isPreloaded(songNo)
        if (ret == 0) {
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(0)
            return
        }

        val retPreload = mMusicCenter.preload(songNo, null)
        if (retPreload != 0) {
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(0)
            return
        }
        loadMusicCallbackMap[songNo.toString()] = onLoadMusicCallback
    }

    private fun getNtpTimeInMs(): Long {
        val currentNtpTime = mRtcEngine.ntpTimeInMs
        return if (currentNtpTime != 0L) {
            currentNtpTime - 2208988800L * 1000
        } else {
            KTVLogger.e(TAG, "getNtpTimeInMs DeviceDelay is zero!!!")
            System.currentTimeMillis()
        }
    }

    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }

    // ------------------------ AgoraRtcEvent ------------------------
    override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
        super.onStreamMessage(uid, streamId, data)
        val jsonMsg: JSONObject
        val messageData = data ?: return
        try {
            val strMsg = String(messageData)
            jsonMsg = JSONObject(strMsg)
            KTVLogger.d(TAG, "onStreamMessage: $strMsg")
            if (jsonMsg.getString("cmd") == "setLrcTime") { //同步歌词
                val position = jsonMsg.getLong("time")
                val duration = jsonMsg.getLong("duration")
                val remoteNtp = jsonMsg.getLong("ntp")
                val pitch = jsonMsg.getDouble("pitch")

                val isChorusCoSinger = isChorusCoSinger() ?: return
                if (isChorusCoSinger) {
                    // 本地BGM校准逻辑
                    if (mPlayer.state == Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                        val delta = getNtpTimeInMs() - remoteNtp
                        mPlayer.play()
                        val expectPosition = position + delta + audioPlayoutDelay
                        if (expectPosition in 1 until duration) {
                            mPlayer.seek(expectPosition)
                        }
                    } else if (mPlayer.state == Constants.MediaPlayerState.PLAYER_STATE_PLAYING) {
                        val localNtpTime = getNtpTimeInMs()
                        val currentSystemTime = System.currentTimeMillis()
                        val localPosition =
                            currentSystemTime - this.localPlayerSystemTime + this.localPlayerPosition // 当前副唱的播放时间
                        val expectPosition =
                            localNtpTime - remoteNtp + position + audioPlayoutDelay // 期望主唱的播放时间
                        val diff = expectPosition - localPosition
                        if ((diff > 40 || diff < -40) && expectPosition < duration) { //设置阈值为40ms，避免频繁seek
                            mPlayer.seek(expectPosition)
                        }
                    }
                } else {
                    // 独唱观众
                    mLastReceivedPlayPosTime = System.currentTimeMillis()
                    mReceivedPlayPosition = position
                    this.pitch = pitch
                }
            } else if (jsonMsg.getString("cmd") == "Seek") {
                // 伴唱收到原唱seek指令
                val isChorusCoSinger = isChorusCoSinger() ?: return
                if (isChorusCoSinger) {
                    val position = jsonMsg.getLong("position")
                    mPlayer.seek(position)
                }
            } else if (jsonMsg.getString("cmd") == "PlayerState") {
                // 其他端收到原唱seek指令
                val state = jsonMsg.getInt("state")
                val error = jsonMsg.getInt("error")
                val config = songConfig ?: return
                val isChorusCoSinger = isChorusCoSinger() ?: return
                if (isChorusCoSinger) {
                    when (Constants.MediaPlayerState.getStateByValue(state)) {
                        Constants.MediaPlayerState.PLAYER_STATE_PAUSED -> {
                            mPlayer.pause()
                        }
                        Constants.MediaPlayerState.PLAYER_STATE_PLAYING -> {
                            mPlayer.resume()
                        }
                        else -> {}
                    }
                }
                ktvApiEventHandler?.onMusicPlayerStateChanged(
                    Constants.MediaPlayerState.getStateByValue(state),
                    Constants.MediaPlayerError.getErrorByValue(error),
                    false
                )
            } else if (jsonMsg.getString("cmd") == "SingingScore") {
                // 其他端收到原唱seek指令
                val isChorusCoSinger = isChorusCoSinger() ?: return
                val score = jsonMsg.getDouble("score").toFloat()
                if (!isChorusCoSinger) {
                    ktvApiEventHandler?.onSingingScoreResult(score)
                }
            }
        } catch (exp: JSONException) {
            KTVLogger.e(TAG, "onStreamMessage:$exp")
        }
    }

    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
        super.onAudioVolumeIndication(speakers, totalVolume)
        val allSpeakers = speakers ?: return
        for (info in allSpeakers) {
            KTVLogger.d(TAG, "onAudioVolumeIndication info: uid:${info.uid}, volume:${info.volume}")
        }
        // VideoPitch 回调, 用于同步各端音准
        val config = songConfig ?: return
        if (config.role != KTVSingRole.KTVSingRoleAudience) {
            for (info in allSpeakers) {
                if (info.uid == 0) {
                    pitch =
                        if (mPlayer.state == Constants.MediaPlayerState.PLAYER_STATE_PLAYING && isOnMicOpen) {
                            info.voicePitch
                        } else {
                            0.0
                        }
                }
            }
        }
    }

    // 用于合唱校准
    override fun onLocalAudioStats(stats: LocalAudioStats?) {
        super.onLocalAudioStats(stats)
        val audioState = stats ?: return
        audioPlayoutDelay = audioState.audioPlayoutDelay
        KTVLogger.d(TAG, "onLocalAudioStats, audioPlay-outDelay=$audioPlayoutDelay")
    }

    // ------------------------ AgoraMusicContentCenterEventDelegate  ------------------------
    override fun onPreLoadEvent(
        songCode: Long,
        percent: Int,
        status: Int,
        msg: String?,
        lyricUrl: String?
    ) {
        if (status == 2) return
        val callback = loadMusicCallbackMap[songCode.toString()] ?: return
        loadMusicCallbackMap.remove(songCode.toString())
        callback.invoke(status)
    }

    override fun onMusicCollectionResult(
        requestId: String?,
        status: Int,
        page: Int,
        pageSize: Int,
        total: Int,
        list: Array<out Music>?
    ) {
        ktvApiEventHandler?.onMusicCollectionResult(requestId, status, page, pageSize, total, list)
    }

    override fun onMusicChartsResult(
        requestId: String?,
        status: Int,
        list: Array<out MusicChartInfo>?
    ) {
        ktvApiEventHandler?.onMusicChartsResult(requestId, status, list)
    }

    override fun onLyricResult(requestId: String?, lyricUrl: String?) {
        val callback = lyricCallbackMap[requestId] ?: return
        lyricCallbackMap.remove(lyricUrl)
        if (lyricUrl == null || lyricUrl.isEmpty()) {
            callback(null)
            return
        }
        callback(lyricUrl)
    }

    // ------------------------ AgoraRtcMediaPlayerDelegate ------------------------
    private var duration: Long = 0
    override fun onPlayerStateChanged(
        state: Constants.MediaPlayerState?,
        error: Constants.MediaPlayerError?
    ) {
        val config = songConfig ?: return
        val mediaPlayerState = state ?: return
        val mediaPlayerError = error ?: return
        KTVLogger.d(TAG, "onPlayerStateChanged called, state: $mediaPlayerState, error: $error")
        when (mediaPlayerState) {
            Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                duration = mPlayer.duration
                this.localPlayerPosition = 0
                mPlayer.selectAudioTrack(1)
                if (config.role == KTVSingRole.KTVSingRoleMainSinger) {
                    mPlayer.play()
                }
            }
            Constants.MediaPlayerState.PLAYER_STATE_PLAYING -> {
                mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
            }
            Constants.MediaPlayerState.PLAYER_STATE_STOPPED -> {
                mRtcEngine.adjustPlaybackSignalVolume(100)
                duration = 0
            }
            Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                // 打分 + 同步分数
                val isChorusCoSinger = isChorusCoSinger() ?: return
                val view = lrcView ?: return
                val score = view.cumulativeScoreInPercentage.toFloat()
                ktvApiEventHandler?.onSingingScoreResult(score)

                // 只有主唱同步分数给观众端
                if (isChorusCoSinger) return
                syncSingingScore(score)
            }
            else -> {}
        }
        syncPlayState(mediaPlayerState, mediaPlayerError)
        ktvApiEventHandler?.onMusicPlayerStateChanged(mediaPlayerState, mediaPlayerError, true)
    }

    // 同步播放进度
    override fun onPositionChanged(position_ms: Long) {
        val config = songConfig ?: return
        localPlayerPosition = position_ms
        localPlayerSystemTime = System.currentTimeMillis()

        if (config.role == KTVSingRole.KTVSingRoleMainSinger && position_ms > audioPlayoutDelay) {
            val msg: MutableMap<String?, Any?> = HashMap()
            msg["cmd"] = "setLrcTime"
            msg["ntp"] = getNtpTimeInMs()
            msg["duration"] = duration
            msg["time"] =
                position_ms - audioPlayoutDelay // "position-audioDeviceDelay" 是计算出当前播放的真实进度
            msg["playerState"] = Constants.MediaPlayerState.getValue(mPlayer.state)
            msg["pitch"] = pitch
            val jsonMsg = JSONObject(msg)
            sendStreamMessageWithJsonObject(jsonMsg) {}
        }
        mLastReceivedPlayPosTime = System.currentTimeMillis()
        mReceivedPlayPosition = position_ms
    }

    override fun onPlayerEvent(
        eventCode: Constants.MediaPlayerEvent?,
        elapsedTime: Long,
        message: String?
    ) {
    }

    override fun onMetaData(type: Constants.MediaPlayerMetadataType?, data: ByteArray?) {}

    override fun onPlayBufferUpdated(playCachedBuffer: Long) {}

    override fun onPreloadEvent(src: String?, event: Constants.MediaPlayerPreloadEvent?) {}

    override fun onCompleted() {}

    override fun onAgoraCDNTokenWillExpire() {}

    override fun onPlayerSrcInfoChanged(from: SrcInfo?, to: SrcInfo?) {}

    override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo?) {}

    override fun onAudioVolumeIndication(volume: Int) {}

    override fun onRecordAudioFrame(
        channelId: String?,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        if (mainSingerHasJoinChannelEx) {
            mRtcEngine.pushDirectAudioFrame(buffer, renderTimeMs, 48000, 2)
        }
        return true
    }

    override fun onPlaybackAudioFrame(
        channelId: String?,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        return false
    }

    override fun onMixedAudioFrame(
        channelId: String?,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        return false
    }

    override fun onPlaybackAudioFrameBeforeMixing(
        channelId: String?,
        userId: Int,
        type: Int,
        samplesPerChannel: Int,
        bytesPerSample: Int,
        channels: Int,
        samplesPerSec: Int,
        buffer: ByteBuffer?,
        renderTimeMs: Long,
        avsync_type: Int
    ): Boolean {
        return false
    }

    override fun getObservedAudioFramePosition(): Int {
        return 0
    }

    override fun getRecordAudioParams(): AudioParams? {
        return null
    }

    override fun getPlaybackAudioParams(): AudioParams? {
        return null
    }

    override fun getMixedAudioParams(): AudioParams? {
        return null
    }
}