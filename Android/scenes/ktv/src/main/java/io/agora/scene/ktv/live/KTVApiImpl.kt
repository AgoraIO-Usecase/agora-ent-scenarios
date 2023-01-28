package io.agora.scene.ktv.live

import android.os.Handler
import android.os.Looper
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.musiccontentcenter.*
import io.agora.rtc2.*
import io.agora.rtc2.Constants.AUDIO_SCENARIO_CHORUS
import io.agora.rtc2.Constants.AUDIO_SCENARIO_GAME_STREAMING
import io.agora.rtc2.audio.AudioParams
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.ktv.KTVLogger
import io.agora.scene.ktv.widget.LrcControlView
import org.json.JSONException
import org.json.JSONObject
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

class KTVApiImpl : KTVApi, IMusicContentCenterEventHandler, IMediaPlayerObserver,
    IRtcEngineEventHandler(), IAudioFrameObserver {
    private val TAG: String = "KTV API LOG"
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var mRtcEngine: RtcEngineEx? = null
    private var mMusicCenter: IAgoraMusicContentCenter? = null
    private var mPlayer: IAgoraMusicPlayer? = null
    private var channelName: String = ""
    private var streamId: Int = 0
    private var songConfig: KTVSongConfiguration? = null
    private var subChorusConnection: RtcConnection? = null

    private val loadSongMap = mutableMapOf<String, KTVLoadSongState>()
    private val lyricUrlMap = mutableMapOf<String, String>() // (songCode, lyricUrl)
    private val lyricCallbackMap = mutableMapOf<String, (lyricUrl: String?) -> Unit>() // (requestId, callback)
    private val loadMusicCallbackMap = mutableMapOf<String, (isPreload: Int?) -> Unit>() // (songNo, callback)

    private var lrcView: LrcControlView? = null

    private var localPlayerPosition: Long = 0
    private var localPlayerSystemTime: Long = 0
    private var remotePlayerPosition: Long = 0
    private var remotePlayerDuration: Long = 0

    //歌词实时刷新
    private var mStopDisplayLrc = true
    private var mDisplayThread: Thread? = null
    private var mReceivedPlayPosition: Long = 0 //播放器播放position，ms
    private var mLastReceivedPlayPosTime: Long? = null

    // event
    private var ktvApiEventHandler: KTVApi.KTVApiEventHandler? = null
    private var hasJoinChannelEx: Boolean = false

    override fun initWithRtcEngine(
        engine: RtcEngine,
        channelName: String,
        musicCenter: IAgoraMusicContentCenter,
        player: IAgoraMusicPlayer,
        streamId: Int,
        ktvApiEventHandler: KTVApi.KTVApiEventHandler
    ) {
        this.mRtcEngine = engine as RtcEngineEx
        this.channelName = channelName
        this.streamId = streamId
        this.mPlayer = player
        this.mMusicCenter = musicCenter
        this.ktvApiEventHandler = ktvApiEventHandler

        engine.addHandler(this)
        player.registerPlayerObserver(this)
        musicCenter.registerEventHandler(this)
    }

    override fun release() {
        loadSongMap.clear()
        lyricUrlMap.clear()
        lyricCallbackMap.clear()
        loadMusicCallbackMap.clear()
        lrcView = null
        mRtcEngine?.removeHandler(this)
        mPlayer?.unRegisterPlayerObserver(this)
        mMusicCenter?.unregisterEventHandler()

        streamId = 0
        mPlayer = null
        mMusicCenter = null
        mRtcEngine = null
    }

    override fun loadSong(
        songCode: Long,
        config: KTVSongConfiguration,
        onLoaded: (songCode: Long, lyricUrl: String, role: KTVSingRole, state: KTVLoadSongState) -> Unit
    ) {
        this.songConfig = config
        val role = config.role
        if (loadSongMap.containsKey(songCode.toString())) {
            when (val state = loadSongMap[songCode.toString()]) {
                KTVLoadSongState.KTVLoadSongStateOK -> {
                    setLrcLyric(lyricUrlMap[songCode.toString()]!!) {
                        onLoaded.invoke(songCode, lyricUrlMap[songCode.toString()]!!, role, state)
                    }
                }
                KTVLoadSongState.KTVLoadSongStateInProgress -> {
                    return
                }
                else -> {}
            }
        }

        loadSongMap[songCode.toString()] = KTVLoadSongState.KTVLoadSongStateInProgress

        var state = KTVLoadSongState.KTVLoadSongStateInProgress
        val countDownLatch = CountDownLatch(2)
        when (role) {
            KTVSingRole.KTVSingRoleMainSinger -> {
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

                loadMusic(songCode) { status ->
                    if (status != 0) {
                        loadSongMap.remove(songCode.toString())
                        state = KTVLoadSongState.KTVLoadSongStatePreloadFail
                    }
                    countDownLatch.countDown()
                }
            }
            KTVSingRole.KTVSingRoleCoSinger -> {
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
                loadMusic(songCode) { status ->
                    if (status != 0) {
                        loadSongMap.remove(songCode.toString())
                        state = KTVLoadSongState.KTVLoadSongStatePreloadFail
                    }
                    countDownLatch.countDown()
                }
            }
            KTVSingRole.KTVSingRoleAudience -> {
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
        }

        Thread {
            countDownLatch.await()
            if (state == KTVLoadSongState.KTVLoadSongStateInProgress) {
                loadSongMap[songCode.toString()] = KTVLoadSongState.KTVLoadSongStateOK
                state = KTVLoadSongState.KTVLoadSongStateOK
                onLoaded.invoke(songCode, lyricUrlMap[songCode.toString()]!!, role, state)
            } else {
                onLoaded.invoke(songCode, lyricUrlMap[songCode.toString()]!!, role, state)
            }
        }.start()
    }

    override fun playSong(songCode: Long) {
        if (songConfig == null) return
        // reset status
        stopDisplayLrc()
        this.mLastReceivedPlayPosTime = null
        this.mReceivedPlayPosition = 0
        startDisplayLrc()

        val role = songConfig!!.role
        val type = songConfig!!.type
        if (type == KTVSongType.KTVSongTypeSolo) {
            // solo
            if (role == KTVSingRole.KTVSingRoleMainSinger) {
                mPlayer!!.open(songCode, 0)
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = true
                channelMediaOption.publishMediaPlayerId = mPlayer!!.mediaPlayerId
                channelMediaOption.publishMediaPlayerAudioTrack = true
                mRtcEngine!!.updateChannelMediaOptions(channelMediaOption)
            } else {
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = true
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine!!.updateChannelMediaOptions(channelMediaOption)
            }
        } else {
            // chorus
            when (role) {
                KTVSingRole.KTVSingRoleMainSinger -> {
                    mPlayer!!.open(songCode, 0)
                    val channelMediaOption = ChannelMediaOptions()
                    channelMediaOption.autoSubscribeAudio = true
                    channelMediaOption.autoSubscribeVideo = true
                    channelMediaOption.publishMediaPlayerId = mPlayer!!.mediaPlayerId
                    channelMediaOption.publishMediaPlayerAudioTrack = true
                    mRtcEngine!!.updateChannelMediaOptions(channelMediaOption)

                    mRtcEngine!!.setDirectExternalAudioSource(true)
                    mRtcEngine!!.setRecordingAudioFrameParameters(48000, 2, 0, 960)
                    mRtcEngine!!.registerAudioFrameObserver(this)

                    joinChorus2ndChannel()
                }
                KTVSingRole.KTVSingRoleCoSinger -> {
                    mPlayer!!.open(songCode, 0)
                    val channelMediaOption = ChannelMediaOptions()
                    channelMediaOption.autoSubscribeAudio = true
                    channelMediaOption.autoSubscribeVideo = true
                    channelMediaOption.publishMediaPlayerAudioTrack = false
                    mRtcEngine!!.updateChannelMediaOptions(channelMediaOption)
                    joinChorus2ndChannel()
                }
                KTVSingRole.KTVSingRoleAudience -> {
                    val channelMediaOption = ChannelMediaOptions()
                    channelMediaOption.autoSubscribeAudio = true
                    channelMediaOption.autoSubscribeVideo = true
                    channelMediaOption.publishMediaPlayerAudioTrack = false
                    mRtcEngine!!.updateChannelMediaOptions(channelMediaOption)
                }
            }
        }
    }

    override fun stopSong() {
        stopSyncPitch()
        stopDisplayLrc()
        this.mLastReceivedPlayPosTime = null
        this.mReceivedPlayPosition = 0
        mPlayer?.stop()
        if(songConfig?.type == KTVSongType.KTVSongTypeChorus) {
            leaveChorus2ndChannel()
        }
    }

    override fun resumePlay() {
        mPlayer?.resume()
    }

    override fun pausePlay() {
        mPlayer?.pause()
    }

    override fun seek(time: Long) {
        mPlayer?.seek(time)
        syncPlayProgress(time)
    }

    override fun selectTrackMode(mode: KTVPlayerTrackMode) {
        val trackMode = if (mode == KTVPlayerTrackMode.KTVPlayerTrackOrigin) 0 else 1
        mPlayer?.selectAudioTrack(trackMode)
        syncTrackMode(trackMode)
    }

    override fun setLycView(view: LrcControlView) {
        this.lrcView = view
    }

    // ------------------ inner --------------------

    private fun isChorusCoSinger() : Boolean? {
        if (songConfig == null) return null
        return songConfig!!.role == KTVSingRole.KTVSingRoleCoSinger &&
                songConfig!!.type == KTVSongType.KTVSongTypeChorus
    }

    private fun sendStreamMessageWithJsonObject(obj: JSONObject, success: (isSendSuccess: Boolean) -> Unit) {
        val ret = mRtcEngine!!.sendStreamMessage(streamId, obj.toString().toByteArray())
        if (ret == 0) {
            success.invoke(true)
        } else {
            KTVLogger.e(TAG, "sendStreamMessageWithJsonObject failed: $ret")
        }
    }

    private fun syncPlayState(state: Constants.MediaPlayerState) {
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "PlayerState"
        msg["state"] = Constants.MediaPlayerState.getValue(state)
        val jsonMsg = JSONObject(msg)
        sendStreamMessageWithJsonObject(jsonMsg) {}
    }

    private fun syncTrackMode(mode: Int) {
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "TrackMode"
        msg["mode"] = mode
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

    // ------------------ 音高pitch同步 ------------------
    private var mSyncPitchThread: Thread? = null
    private var mStopSyncPitch = true
    private var pitch = 0.0

    // 开始同步音高
    private fun startSyncPitch() {
        mSyncPitchThread = Thread(object : Runnable {
            override fun run() {
                mStopSyncPitch = false
                while (!mStopSyncPitch) {
                    if (mPlayer == null) {
                        break
                    }
                    if (mPlayer!!.state == Constants.MediaPlayerState.PLAYER_STATE_PLAYING ||
                        mPlayer!!.state == Constants.MediaPlayerState.PLAYER_STATE_PAUSED) {
                        sendSyncPitch(pitch)
                    }
                    try {
                        Thread.sleep(999L)
                    } catch (exp: InterruptedException) {
                        break
                    }
                }
            }

            private fun sendSyncPitch(pitch: Double) {
                val msg: MutableMap<String?, Any?> = java.util.HashMap()
                msg["cmd"] = "setVoicePitch"
                msg["pitch"] = pitch
                msg["time"] = mPlayer!!.playPosition
                val jsonMsg = JSONObject(msg)
                val ret = mRtcEngine!!.sendStreamMessage(streamId, jsonMsg.toString().toByteArray())
                if (ret < 0) {
                    KTVLogger.e(TAG, "sendPitch() sendStreamMessage called returned: $ret")
                }
            }
        })
        mSyncPitchThread!!.name = "Thread-SyncPitch"
        mSyncPitchThread!!.start()
    }

    // 停止同步音高
    private fun stopSyncPitch() {
        mStopSyncPitch = true
        pitch = 0.0
        if (mSyncPitchThread != null) {
            try {
                mSyncPitchThread!!.join()
            } catch (exp: InterruptedException) {
                KTVLogger.e(TAG, "stopSyncPitch: $exp")
            }
        }
    }

    // 合唱
    private fun joinChorus2ndChannel() {
        if (songConfig == null || mRtcEngine == null) return

        val role = songConfig!!.role
        val channelMediaOption = ChannelMediaOptions()
        // main singer do not subscribe 2nd channel
        // co singer auto sub
        channelMediaOption.autoSubscribeAudio = role != KTVSingRole.KTVSingRoleMainSinger
        channelMediaOption.autoSubscribeVideo = false
        channelMediaOption.publishMicrophoneTrack = false
        channelMediaOption.enableAudioRecordingOrPlayout = role != KTVSingRole.KTVSingRoleMainSinger
        channelMediaOption.clientRoleType = io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER
        channelMediaOption.publishDirectCustomAudioTrack = role == KTVSingRole.KTVSingRoleMainSinger

        val rtcConnection = RtcConnection()
        rtcConnection.channelId = channelName + "_ex"
        rtcConnection.localUid = UserManager.getInstance().user.id.toInt()
        subChorusConnection = rtcConnection

        TokenGenerator.generateTokens(
            rtcConnection.channelId,
            UserManager.getInstance().user.id.toString(),
            TokenGenerator.TokenGeneratorType.token006,
            arrayOf(
                TokenGenerator.AgoraTokenType.rtc),
            { ret ->
                val rtcToken = ret[TokenGenerator.AgoraTokenType.rtc] ?: ""
                mRtcEngine!!.joinChannelEx(
                    rtcToken,
                    rtcConnection,
                    channelMediaOption,
                    object: IRtcEngineEventHandler() {
                        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                            super.onJoinChannelSuccess(channel, uid, elapsed)
                            if (role == KTVSingRole.KTVSingRoleMainSinger) hasJoinChannelEx = true
                            mRtcEngine?.setAudioScenario(AUDIO_SCENARIO_CHORUS)
                        }

                        override fun onLeaveChannel(stats: RtcStats?) {
                            super.onLeaveChannel(stats)
                            if (role == KTVSingRole.KTVSingRoleMainSinger) hasJoinChannelEx = false
                            mRtcEngine?.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
                        }
                    }
                )
            }, {}
        )

        if (songConfig!!.type == KTVSongType.KTVSongTypeChorus &&
            songConfig!!.role == KTVSingRole.KTVSingRoleCoSinger) {
            mRtcEngine?.muteRemoteAudioStream(songConfig!!.mainSingerUid, true)
        }
    }

    private fun leaveChorus2ndChannel() {
        if (songConfig == null) return

        val role = songConfig!!.role
        if (role == KTVSingRole.KTVSingRoleMainSinger) {
            val channelMediaOption = ChannelMediaOptions()
            channelMediaOption.publishDirectCustomAudioTrack = false
            mRtcEngine!!.updateChannelMediaOptionsEx(channelMediaOption, subChorusConnection)
            mRtcEngine!!.leaveChannelEx(subChorusConnection)
        } else if (role == KTVSingRole.KTVSingRoleCoSinger) {
            mRtcEngine!!.leaveChannelEx(subChorusConnection)
            mRtcEngine!!.muteRemoteAudioStream(songConfig!!.mainSingerUid, false)
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
        KTVLogger.d("KTVLiveRoomLog:", "startDisplayLrc")
        mStopDisplayLrc = false
        mDisplayThread = Thread {
            var curTs: Long
            var curTime: Long
            var offset: Long
            while (!mStopDisplayLrc) {
                if (mLastReceivedPlayPosTime != null) {
                    curTime = System.currentTimeMillis()
                    offset = curTime - mLastReceivedPlayPosTime!!
                    if (offset <= 1000) {
                        curTs = mReceivedPlayPosition + offset
                        runOnMainThread {
                            lrcView?.lrcView?.updateTime(curTs)
                            lrcView?.pitchView?.updateTime(curTs)
                        }
                    }
                }
                try {
                    Thread.sleep(50)
                } catch (exp: InterruptedException) {
                    break
                }
            }
        }
        mDisplayThread!!.name = "Thread-Display"
        mDisplayThread!!.start()
    }

    // 停止播放歌词
    private fun stopDisplayLrc() {
        mStopDisplayLrc = true
        if (mDisplayThread != null) {
            try {
                mDisplayThread!!.join()
            } catch (exp: InterruptedException) {
                KTVLogger.d(TAG, "stopDisplayLrc: $exp")
            }
        }
    }

    private fun loadLyric(songNo: Long, onLoadLyricCallback: (lyricUrl: String?) -> Unit) {
        KTVLogger.d(TAG, "loadLyric: $songNo")
        if (mMusicCenter == null) return
        val requestId = mMusicCenter!!.getLyric(songNo, 0)
        if (requestId.isEmpty()) {
            onLoadLyricCallback.invoke(null)
            return
        }
        lyricCallbackMap[requestId] = onLoadLyricCallback
    }

    private fun loadMusic(songNo: Long, onLoadMusicCallback: (status: Int?) -> Unit) {
        KTVLogger.d(TAG, "loadMusic: $songNo")
        if (mMusicCenter == null) return
        val ret = mMusicCenter!!.isPreloaded(songNo)
        if (ret == 0) {
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(0)
            return
        }

        val retPreload = mMusicCenter!!.preload(songNo, null)
        if (retPreload != 0) {
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(0)
            return
        }
        loadMusicCallbackMap[songNo.toString()] = onLoadMusicCallback
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
        try {
            val strMsg = String(data!!)
            jsonMsg = JSONObject(strMsg)
            if (jsonMsg.getString("cmd") == "setLrcTime") { //同步歌词
                if (mRtcEngine == null || mPlayer == null || songConfig == null) return
                val position = jsonMsg.getLong("time")
                val duration = jsonMsg.getLong("duration")
                val remoteNtp = jsonMsg.getLong("ntp")
                this.remotePlayerDuration = duration
                this.remotePlayerPosition = position

                if (isChorusCoSinger()!!) {
                    if (mPlayer!!.state == Constants.MediaPlayerState.PLAYER_STATE_PLAYING) {
                        val localNtpTime = mRtcEngine!!.ntpTimeInMs
                        val currentSystemTime = System.currentTimeMillis()
                        val localPosition = currentSystemTime - this.localPlayerSystemTime + this.localPlayerPosition
                        val expectPosition = localNtpTime - remoteNtp + position
                        val diff = expectPosition - localPosition
                        if (diff > 40 || diff < -40) { // TODO labs()
                            this.localPlayerPosition = expectPosition
                            mPlayer!!.seek(expectPosition)
                        }
                    }
                } else {
                    // 独唱观众
                    mLastReceivedPlayPosTime = System.currentTimeMillis()
                    mReceivedPlayPosition = position
                }
            } else if (jsonMsg.getString("cmd") == "TrackMode") {
                // 伴唱收到原唱伴唱调整指令
                if (mPlayer == null || songConfig == null) return
                if (isChorusCoSinger()!!) {
                    val trackMode = jsonMsg.getInt("mode")
                    mPlayer!!.selectAudioTrack(trackMode)
                }
            } else if (jsonMsg.getString("cmd") == "Seek") {
                // 伴唱收到原唱seek指令
                if (mPlayer == null || songConfig == null) return
                if (isChorusCoSinger()!!) {
                    val position = jsonMsg.getLong("position")
                    mPlayer!!.seek(position)
                }
            } else if (jsonMsg.getString("cmd") == "setVoicePitch") {
                // 观众同步pitch
                if (mPlayer == null || songConfig == null) return
                if (!isChorusCoSinger()!!) {
                    val pitch = jsonMsg.getDouble("pitch")
                    runOnMainThread { lrcView!!.pitchView.updateLocalPitch(pitch.toFloat()) }
                }
            } else if (jsonMsg.getString("cmd") == "PlayerState") {
                // 其他端收到原唱seek指令
                if (mPlayer == null || songConfig == null) return
                val state = jsonMsg.getInt("state")
                if (isChorusCoSinger()!!) {
                    when (Constants.MediaPlayerState.getStateByValue(state)) {
                        Constants.MediaPlayerState.PLAYER_STATE_PAUSED -> {
                            mPlayer?.pause()
                        }
                        Constants.MediaPlayerState.PLAYER_STATE_PLAYING -> {
                            mPlayer?.resume()
                        }
                        else -> {}
                    }
                } else {
                    // 独唱观众
                    when (Constants.MediaPlayerState.getStateByValue(state)) {
                        Constants.MediaPlayerState.PLAYER_STATE_STOPPED -> {
                            stopDisplayLrc()
                            this.mLastReceivedPlayPosTime = null
                            this.mReceivedPlayPosition = 0
                        }
                        else -> {}
                    }
                }
                ktvApiEventHandler?.onPlayerStateChanged(this, songConfig!!.songCode, Constants.MediaPlayerState.getStateByValue(state), false)
            } else if (jsonMsg.getString("cmd") == "SingingScore") {
                // 其他端收到原唱seek指令
                if (mPlayer == null || songConfig == null) return
                val score = jsonMsg.getDouble("score").toFloat()
                if (!isChorusCoSinger()!!) {
                    ktvApiEventHandler?.onSingingScoreResult(score)
                }
            }
        } catch (exp: JSONException) {
            KTVLogger.e(TAG, "onStreamMessage:$exp")
        }
    }

    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
        super.onAudioVolumeIndication(speakers, totalVolume)
        // VideoPitch回调, 用于同步各端音准
        if (songConfig == null || mPlayer == null) return
        if (songConfig!!.mainSingerUid.toLong() == UserManager.getInstance().user.id
            || songConfig!!.coSingerUid.toLong() == UserManager.getInstance().user.id
        ) {
            for (info in speakers!!) {
                if (info.uid == 0) {
                    if (mPlayer != null && mPlayer!!.state == Constants.MediaPlayerState.PLAYER_STATE_PLAYING) {
                        runOnMainThread { lrcView?.pitchView?.updateLocalPitch(info.voicePitch.toFloat()) }
                        pitch = info.voicePitch
                    } else {
                        runOnMainThread { lrcView?.pitchView?.updateLocalPitch(0.0F) }
                        pitch = 0.0
                    }
                }
            }
        }
    }

    // ------------------------ AgoraRtcMediaPlayerDelegate ------------------------
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

    // ------------------------ AgoraMusicContentCenterEventDelegate ------------------------
    override fun onPlayerStateChanged(
        state: Constants.MediaPlayerState?,
        error: Constants.MediaPlayerError?
    ) {
        if (songConfig == null) return
        when (state) {
            Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                mPlayer?.play()
            }
            Constants.MediaPlayerState.PLAYER_STATE_PLAYING -> {
                startSyncPitch()
            }
            Constants.MediaPlayerState.PLAYER_STATE_STOPPED -> {
                this.localPlayerPosition = 0
                stopSyncPitch()
                stopDisplayLrc()
                this.mLastReceivedPlayPosTime = null
                this.mReceivedPlayPosition = 0
            }
            Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                // 打分 + 同步分数
                if (lrcView == null || isChorusCoSinger()!!) return
                val score = lrcView!!.pitchView.cumulatedScore
                ktvApiEventHandler?.onSingingScoreResult(score)
                syncSingingScore(score)
            }
            else -> {}
        }
        syncPlayState(state!!)
        ktvApiEventHandler?.onPlayerStateChanged(this, songConfig!!.songCode, state, true)
    }

    override fun onPositionChanged(position_ms: Long) {
        localPlayerPosition = position_ms
        localPlayerSystemTime = System.currentTimeMillis()

        if (mRtcEngine == null || songConfig == null || mPlayer == null) return
        if (songConfig!!.role == KTVSingRole.KTVSingRoleMainSinger) {
            val msg: MutableMap<String?, Any?> = HashMap()
            msg["cmd"] = "setLrcTime"
            msg["ntp"] = mRtcEngine!!.ntpTimeInMs
            msg["duration"] = mPlayer!!.duration
            msg["time"] = position_ms //ms
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
    ) {}

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
        if (hasJoinChannelEx && mRtcEngine != null) {
            mRtcEngine!!.pushDirectAudioFrame(buffer, renderTimeMs, 48000, 2)
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

    override fun getObservedAudioFramePosition(): Int { return 0 }

    override fun getRecordAudioParams(): AudioParams? { return null }

    override fun getPlaybackAudioParams(): AudioParams? { return null }

    override fun getMixedAudioParams(): AudioParams? { return null }
}