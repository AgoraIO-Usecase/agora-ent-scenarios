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
    private var subChorusConnection: RtcConnection? = null

    private var mainSingerUid: Int = 0
    private var loadSongState: KTVLoadSongState = KTVLoadSongState.IDLE
    private var songCode: Long = 0

    private val lyricUrlMap = mutableMapOf<String, String>() // (songCode, lyricUrl)
    private val lyricCallbackMap =
        mutableMapOf<String, (lyricUrl: String?) -> Unit>() // (requestId, callback)
    private val loadMusicCallbackMap =
        mutableMapOf<String, (isPreload: Int?) -> Unit>() // (songNo, callback)
    private val musicChartsCallbackMap =
        mutableMapOf<String, (requestId: String?, status: Int, list: Array<out MusicChartInfo>?) -> Unit>()
    private val musicCollectionCallbackMap =
        mutableMapOf<String, (requestId: String?, status: Int, page: Int, pageSize: Int, total: Int, list: Array<out Music>?) -> Unit>()

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
        mPlayer.adjustPublishSignalVolume(50)
        mPlayer.adjustPlayoutVolume(50)

        // 注册回调
        mRtcEngine.addHandler(this)
        mPlayer.registerPlayerObserver(this)
        mMusicCenter.registerEventHandler(this)

        startDisplayLrc()
    }

    override fun addEventHandler(ktvApiEventHandler: IKTVApiEventHandler) {
        this.ktvApiEventHandler = ktvApiEventHandler
    }

    override fun removeEventHandler(ktvApiEventHandler: IKTVApiEventHandler) {
        this.ktvApiEventHandler = null
    }

    override fun release() {
        isRelease = true
        singerRole = KTVSingRole.Audience
        loadSongState = KTVLoadSongState.IDLE

        stopDisplayLrc()
        this.mLastReceivedPlayPosTime = null
        this.mReceivedPlayPosition = 0

        lyricUrlMap.clear()
        lyricCallbackMap.clear()
        loadMusicCallbackMap.clear()
        musicChartsCallbackMap.clear()
        musicCollectionCallbackMap.clear()
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

    // 1、KTVSingRoleAudience -》KTVSingRoleMainSinger
    // 2、KTVSingRoleMainSinger -》KTVSingRoleAudience
    // 3、KTVSingRoleAudience -》KTVSingRoleCoSinger
    // 4、KTVSingRoleCoSinger -》KTVSingRoleAudience
    // 5、KTVSingRoleMainSinger -》KTVSingRoleChorusMainSinger
    // 6、KTVSingRoleChorusMainSinger -》KTVSingRoleMainSinger
    // 7、KTVSingRoleChorusMainSinger -》KTVSingRoleAudience
    var singerRole: KTVSingRole = KTVSingRole.Audience
    override fun switchSingerRole(
        newRole: KTVSingRole,
        token: String,
        onSwitchRoleState: (state: SwitchRoleState, reason: SwitchRoleFailReason) -> Unit
    ) {
        Log.d(TAG, "switchSingerRole oldRole: $singerRole, newRole: $newRole")
        val oldRole = singerRole
        if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.SoloSinger) {
            // 1、KTVSingRoleAudience -》KTVSingRoleMainSinger
            this.singerRole = newRole

            // 主唱进入合唱模式
            mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)

            val channelMediaOption = ChannelMediaOptions()
            channelMediaOption.autoSubscribeAudio = true
            channelMediaOption.autoSubscribeVideo = true
            channelMediaOption.publishMediaPlayerId = mPlayer.mediaPlayerId
            channelMediaOption.publishMediaPlayerAudioTrack = true
            mRtcEngine.updateChannelMediaOptions(channelMediaOption)

            mRtcEngine.setDirectExternalAudioSource(true)
            mRtcEngine.setRecordingAudioFrameParameters(48000, 2, 0, 960)
            mRtcEngine.registerAudioFrameObserver(this)

            ktvApiEventHandler?.onSingerRoleChanged(oldRole, newRole)
            onSwitchRoleState(SwitchRoleState.SUCCESS, SwitchRoleFailReason.NONE)
        } else if (this.singerRole == KTVSingRole.SoloSinger && newRole == KTVSingRole.Audience) {
            // 2、KTVSingRoleMainSinger -》KTVSingRoleAudience

            stopSing()
            this.singerRole = newRole
            ktvApiEventHandler?.onSingerRoleChanged(oldRole, newRole)
            onSwitchRoleState(SwitchRoleState.SUCCESS, SwitchRoleFailReason.NONE)

        } else if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.CoSinger) {
            // 3、KTVSingRoleAudience -》KTVSingRoleCoSinger
            this.singerRole = newRole
            joinChorus(token, object : OnJoinChorusStateListener {
                override fun onJoinChorusSuccess() {
                    onSwitchRoleState(SwitchRoleState.SUCCESS, SwitchRoleFailReason.NONE)
                    ktvApiEventHandler?.onSingerRoleChanged(oldRole, newRole)
                }

                override fun onJoinChorusFail(reason: KTVJoinChorusFailReason) {
                    // TODO 错误处理
                    leaveChorus()
                    singerRole = oldRole
                    onSwitchRoleState(SwitchRoleState.FAIL, SwitchRoleFailReason.MUSIC_PRELOAD_FAIL_AND_JOIN_CHANNEL_FAIL)
                }
            })

        } else if (this.singerRole == KTVSingRole.CoSinger && newRole == KTVSingRole.Audience) {
            // 4、KTVSingRoleCoSinger -》KTVSingRoleAudience
            leaveChorus()

            this.singerRole = newRole
            ktvApiEventHandler?.onSingerRoleChanged(oldRole, newRole)
            onSwitchRoleState(SwitchRoleState.SUCCESS, SwitchRoleFailReason.NONE)

        } else if (this.singerRole == KTVSingRole.SoloSinger && newRole == KTVSingRole.LeadSinger) {
            // 5、KTVSingRoleMainSinger -》KTVSingRoleChorusMainSinger
            this.singerRole = newRole

            joinChorus(token, object : OnJoinChorusStateListener {
                override fun onJoinChorusSuccess() {
                    onSwitchRoleState(SwitchRoleState.SUCCESS, SwitchRoleFailReason.NONE)
                    ktvApiEventHandler?.onSingerRoleChanged(oldRole, newRole)
                }

                override fun onJoinChorusFail(reason: KTVJoinChorusFailReason) {
                    // TODO 错误处理
                    leaveChorus()
                    singerRole = oldRole
                    onSwitchRoleState(SwitchRoleState.FAIL, SwitchRoleFailReason.MUSIC_PRELOAD_FAIL_AND_JOIN_CHANNEL_FAIL)
                }
            })
        } else if (this.singerRole == KTVSingRole.LeadSinger && newRole == KTVSingRole.SoloSinger) {
            // 6、KTVSingRoleChorusMainSinger -》KTVSingRoleMainSinger
            leaveChorus()

            this.singerRole = newRole
            ktvApiEventHandler?.onSingerRoleChanged(oldRole, newRole)
            onSwitchRoleState(SwitchRoleState.SUCCESS, SwitchRoleFailReason.NONE)
        } else if (this.singerRole == KTVSingRole.LeadSinger && newRole == KTVSingRole.Audience) {
            // 7、KTVSingRoleChorusMainSinger -》KTVSingRoleMainSinger
            stopSing()

            this.singerRole = newRole
            ktvApiEventHandler?.onSingerRoleChanged(oldRole, newRole)
            onSwitchRoleState(SwitchRoleState.SUCCESS, SwitchRoleFailReason.NONE)
        } else {
            onSwitchRoleState(SwitchRoleState.FAIL, SwitchRoleFailReason.NO_PERMISSION)
            Log.e(TAG, "Error！You can not switch role from $singerRole to $newRole!")
        }
    }

    override fun fetchMusicCharts(onMusicChartResultListener: (requestId: String?, status: Int, list: Array<out MusicChartInfo>?) -> Unit) {
        val requestId = mMusicCenter.musicCharts
        musicChartsCallbackMap[requestId] = onMusicChartResultListener
    }

    override fun searchMusicByMusicChartId(
        musicChartId: Int,
        page: Int,
        pageSize: Int,
        jsonOption: String,
        onMusicCollectionResultListener: (requestId: String?, status: Int, page: Int, pageSize: Int, total: Int, list: Array<out Music>?) -> Unit
    ) {
        val requestId =
            mMusicCenter.getMusicCollectionByMusicChartId(musicChartId, page, pageSize, jsonOption)
        musicCollectionCallbackMap[requestId] = onMusicCollectionResultListener
    }

    override fun searchMusicByKeyword(
        keyword: String,
        page: Int,
        pageSize: Int,
        jsonOption: String,
        onMusicCollectionResultListener: (requestId: String?, status: Int, page: Int, pageSize: Int, total: Int, list: Array<out Music>?) -> Unit
    ) {
        val requestId = mMusicCenter.searchMusic(keyword, page, pageSize, jsonOption)
        musicCollectionCallbackMap[requestId] = onMusicCollectionResultListener
    }

    override fun loadMusic(
        config: KTVSongConfiguration,
        onMusicLoadStateListener: OnMusicLoadStateListener
    ) {
        Log.d(TAG, "loadSong called: $singerRole")
        if (loadSongState == KTVLoadSongState.IN_PROGRESS) {
            Log.e(TAG, "loadSong failed: KTVLoadSongState is in progress")
            return
        }

        loadSongState = KTVLoadSongState.IN_PROGRESS
        this.songCode = config.songCode
        this.mainSingerUid = config.mainSingerUid
        val countDownLatch = CountDownLatch(2)

        var isLrcUrlSuccess = false
        var isSongLoadedSuccess = false
        when (singerRole) {
            KTVSingRole.SoloSinger -> {
                // 获取歌词url
                loadLyric(songCode) { lyricUrl ->
                    if (lyricUrl == null) {
                        lyricUrlMap[songCode.toString()] = ""
                        isLrcUrlSuccess = false
                    } else {
                        lyricUrlMap[songCode.toString()] = lyricUrl
                        lrcView?.downloadLrcData(lyricUrl)
                        isLrcUrlSuccess = true
                    }
                    countDownLatch.countDown()
                }

                // 预加载歌曲
                loadMusic(songCode) { status ->
                    isSongLoadedSuccess = status == 0
                    countDownLatch.countDown()
                }
            }
            KTVSingRole.Audience -> {
                // 获取歌词url
                loadLyric(songCode) { lyricUrl ->
                    if (lyricUrl == null) {
                        lyricUrlMap[songCode.toString()] = ""
                        isLrcUrlSuccess = false
                    } else {
                        lyricUrlMap[songCode.toString()] = lyricUrl
                        lrcView?.downloadLrcData(lyricUrl)
                        isLrcUrlSuccess = true
                    }
                    isSongLoadedSuccess = true
                    countDownLatch.countDown()
                    countDownLatch.countDown()
                }
            }
            else -> {
                Log.d(TAG, "loadSong called wrong role")
            }
        }

        Thread {
            countDownLatch.await()
            val url = lyricUrlMap[songCode.toString()] ?: return@Thread
            if (isLrcUrlSuccess && isSongLoadedSuccess) {
                loadSongState = KTVLoadSongState.OK

                if (config.autoPlay && singerRole == KTVSingRole.SoloSinger) {
                    // 主唱自动播放歌曲
                    startSing(0)
                }
                onMusicLoadStateListener.onMusicLoadSuccess(songCode, url)

            } else if (!isLrcUrlSuccess && isSongLoadedSuccess) {
                onMusicLoadStateListener.onMusicLoadFail(
                    songCode,
                    url,
                    KTVLoadSongFailReason.NO_LYRIC_URL
                )
            } else if (isLrcUrlSuccess && !isSongLoadedSuccess) {
                onMusicLoadStateListener.onMusicLoadFail(
                    songCode,
                    url,
                    KTVLoadSongFailReason.MUSIC_PRELOAD_FAIL
                )
            } else {
                onMusicLoadStateListener.onMusicLoadFail(
                    songCode,
                    url,
                    KTVLoadSongFailReason.MUSIC_PRELOAD_FAIL_AND_JOIN_CHANNEL_FAIL
                )
            }
        }.start()
    }

    override fun startSing(startPos: Long) {
        Log.d(TAG, "playSong called: $singerRole")
        if (this.singerRole == KTVSingRole.SoloSinger) {
            mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
            mPlayer.open(songCode, startPos)
        } else {
            Log.e(TAG, "Wrong role playSong, you are not mainSinger right now!")
        }
    }

    private fun joinChorus(token: String, onJoinChorusStateListener: OnJoinChorusStateListener) {
        Log.d(TAG, "joinChorus: $singerRole")
        when (this.singerRole) {
            KTVSingRole.LeadSinger -> {
                joinChorus2ndChannel(token, mainSingerUid) { joinStatus ->
                    if (joinStatus == 0) {
                        onJoinChorusStateListener.onJoinChorusSuccess()
                    } else {
                        onJoinChorusStateListener.onJoinChorusFail(KTVJoinChorusFailReason.JOIN_CHANNEL_FAIL)
                    }
                }
            }
            KTVSingRole.CoSinger -> {
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = true
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)

                val countDownLatch = CountDownLatch(2)
                var isJoinSuccess = false
                var isLoadMusicSuccess = false
                joinChorus2ndChannel(token, mainSingerUid) { joinStatus ->
                    isJoinSuccess = joinStatus == 0
                    countDownLatch.countDown()
                }
                loadMusic(songCode) {
                    if (it == 0) {
                        mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
                        mPlayer.open(songCode, 0) // TODO open failed
                        isLoadMusicSuccess = true
                    }
                    countDownLatch.countDown()
                }

                Thread {
                    countDownLatch.await()
                    if (isLoadMusicSuccess && isJoinSuccess) {
                        onJoinChorusStateListener.onJoinChorusSuccess()
                    } else {
                        onJoinChorusStateListener.onJoinChorusFail(KTVJoinChorusFailReason.MUSIC_PRELOAD_FAIL)
                    }
                }.start()
            }
            else -> {
                Log.e(TAG, "JoinChorus with Wrong role: $singerRole")
            }
        }
    }

    private fun leaveChorus() {
        Log.d(TAG, "leaveChorus: $singerRole")
        when (this.singerRole) {
            KTVSingRole.LeadSinger -> {
                leaveChorus2ndChannel()
            }
            KTVSingRole.CoSinger -> {
                if (mPlayer.state != Constants.MediaPlayerState.PLAYER_STATE_STOPPED) {
                    mPlayer.stop()
                }
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.autoSubscribeVideo = true
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
                leaveChorus2ndChannel()
                mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
            }
            else -> {
                Log.e(TAG, "JoinChorus with Wrong role: $singerRole")
            }
        }
    }

    private fun stopSing() {
        Log.d(TAG, "stopSong called")
        mainSingerHasJoinChannelEx = false

        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.autoSubscribeAudio = true
        channelMediaOption.autoSubscribeVideo = true
        channelMediaOption.publishMediaPlayerAudioTrack = false
        mRtcEngine.updateChannelMediaOptions(channelMediaOption)

        if (mPlayer.state != Constants.MediaPlayerState.PLAYER_STATE_STOPPED) {
            mPlayer.stop()
        }
        leaveChorus2ndChannel()
        mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
    }

    override fun resumeSing() {
        Log.d(TAG, "resumePlay called")
        mPlayer.resume()
    }

    override fun pauseSing() {
        Log.d(TAG, "pausePlay called")
        mPlayer.pause()
    }

    override fun seekSing(time: Long) {
        Log.d(TAG, "seek called")
        mPlayer.seek(time)
        syncPlayProgress(time)
    }

    override fun setLrcView(view: LrcControlView) {
        Log.d(TAG, "setLycView called")
        this.lrcView = view
    }

    override fun setMicStatus(isOnMicOpen: Boolean) {
        this.isOnMicOpen = isOnMicOpen
    }

    override fun getMediaPlayer(): IMediaPlayer {
        return mPlayer
    }

    // ------------------ inner --------------------

    private fun isChorusCoSinger(): Boolean {
        return singerRole == KTVSingRole.CoSinger || singerRole == KTVSingRole.FollowSinger
    }

    private fun sendStreamMessageWithJsonObject(
        obj: JSONObject,
        success: (isSendSuccess: Boolean) -> Unit
    ) {
        val ret = mRtcEngine.sendStreamMessage(dataStreamId, obj.toString().toByteArray())
        if (ret == 0) {
            success.invoke(true)
        } else {
            Log.e(TAG, "sendStreamMessageWithJsonObject failed: $ret")
        }
    }

    private fun syncPlayState(
        state: Constants.MediaPlayerState,
        error: Constants.MediaPlayerError
    ) {
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
    private fun joinChorus2ndChannel(
        token: String,
        mainSingerUid: Int,
        onJoinChorus2ndChannelCallback: (status: Int?) -> Unit
    ) {
        Log.d(TAG, "joinChorus2ndChannel: token:$token")
        if (this.singerRole != KTVSingRole.LeadSinger && this.singerRole != KTVSingRole.CoSinger) {
            Log.e(TAG, "joinChorus2ndChannel with wrong role: $singerRole")
            return
        }

        mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)

        // main singer do not subscribe 2nd channel
        // co singer auto sub
        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.autoSubscribeAudio =
            singerRole != KTVSingRole.LeadSinger
        channelMediaOption.autoSubscribeVideo = false
        channelMediaOption.publishMicrophoneTrack = false
        channelMediaOption.enableAudioRecordingOrPlayout =
            singerRole != KTVSingRole.LeadSinger
        channelMediaOption.clientRoleType = CLIENT_ROLE_BROADCASTER
        channelMediaOption.publishDirectCustomAudioTrack =
            singerRole == KTVSingRole.LeadSinger

        val rtcConnection = RtcConnection()
        rtcConnection.channelId = channelName + "_ex"
        rtcConnection.localUid = localUid
        subChorusConnection = rtcConnection

        val ret = mRtcEngine.joinChannelEx(
            token,
            rtcConnection,
            channelMediaOption,
            object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    Log.d(TAG, "onJoinChannel2Success: channel:$channel, uid:$uid")
                    if (isRelease) return
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    if (singerRole == KTVSingRole.LeadSinger) {
                        mainSingerHasJoinChannelEx = true
                    }
                    onJoinChorus2ndChannelCallback(0)
                }

                override fun onLeaveChannel(stats: RtcStats?) {
                    Log.d(TAG, "onLeaveChannel2")
                    if (isRelease) return
                    super.onLeaveChannel(stats)
                    //mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
                    if (singerRole == KTVSingRole.LeadSinger) {
                        mainSingerHasJoinChannelEx = false
                    }
                }
            }
        )

        if (ret != 0) {
            Log.e(TAG, "joinChorus2ndChannel failed: $ret")
            onJoinChorus2ndChannelCallback(ret)
        }

        this.mainSingerUid = mainSingerUid
        if (singerRole == KTVSingRole.CoSinger ||
            singerRole == KTVSingRole.FollowSinger
        ) {
            mRtcEngine.muteRemoteAudioStream(mainSingerUid, true)
        }
    }

    private fun leaveChorus2ndChannel() {
        if (this.singerRole == KTVSingRole.LeadSinger) {
            val channelMediaOption = ChannelMediaOptions()
            channelMediaOption.publishDirectCustomAudioTrack = false
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption, subChorusConnection)
            mRtcEngine.leaveChannelEx(subChorusConnection)
        } else if (singerRole == KTVSingRole.CoSinger) {
            mRtcEngine.leaveChannelEx(subChorusConnection)
            mRtcEngine.muteRemoteAudioStream(mainSingerUid, false)
        }
    }

    // ------------------ 歌词播放、同步 ------------------
    // 开始播放歌词
    private fun startDisplayLrc() {
        Log.d(TAG, "startDisplayLrc called")
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
        Log.d(TAG, "stopDisplayLrc called")
        mStopDisplayLrc = true
        if (mDisplayThread != null) {
            try {
                mDisplayThread?.join()
                mDisplayThread = null
            } catch (exp: InterruptedException) {
                Log.d(TAG, "stopDisplayLrc: $exp")
            }
        }
    }

    private fun loadLyric(songNo: Long, onLoadLyricCallback: (lyricUrl: String?) -> Unit) {
        Log.d(TAG, "loadLyric: $songNo")
        val requestId = mMusicCenter.getLyric(songNo, 0)
        if (requestId.isEmpty()) {
            onLoadLyricCallback.invoke(null)
            return
        }
        lyricCallbackMap[requestId] = onLoadLyricCallback
    }

    private fun loadMusic(songNo: Long, onLoadMusicCallback: (status: Int?) -> Unit) {
        Log.d(TAG, "loadMusic: $songNo")
        val ret = mMusicCenter.isPreloaded(songNo)
        if (ret == 0) {
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(0)
            return
        }

        val retPreload = mMusicCenter.preload(songNo, null)
        if (retPreload != 0) {
            Log.e(TAG, "loadMusic failed: $retPreload")
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(retPreload)
            return
        }
        loadMusicCallbackMap[songNo.toString()] = onLoadMusicCallback
    }

    private fun getNtpTimeInMs(): Long {
        val currentNtpTime = mRtcEngine.ntpTimeInMs
        return if (currentNtpTime != 0L) {
            currentNtpTime - 2208988800L * 1000
        } else {
            Log.e(TAG, "getNtpTimeInMs DeviceDelay is zero!!!")
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
            Log.d(TAG, "onStreamMessage: $strMsg")
            if (jsonMsg.getString("cmd") == "setLrcTime") { //同步歌词
                val position = jsonMsg.getLong("time")
                val duration = jsonMsg.getLong("duration")
                val remoteNtp = jsonMsg.getLong("ntp")
                val pitch = jsonMsg.getDouble("pitch")

                if (isChorusCoSinger()) {
                    // 本地BGM校准逻辑
                    if (mPlayer.state == Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                        val delta = getNtpTimeInMs() - remoteNtp
                        val expectPosition = position + delta + audioPlayoutDelay
                        if (expectPosition in 1 until duration) {
                            mPlayer.seek(expectPosition)
                        }
                        mPlayer.play()
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
                    } else {
                        mLastReceivedPlayPosTime = System.currentTimeMillis()
                        mReceivedPlayPosition = position
                        this.pitch = pitch
                    }
                } else {
                    // 独唱观众
                    mLastReceivedPlayPosTime = System.currentTimeMillis()
                    mReceivedPlayPosition = position
                    this.pitch = pitch
                }
            } else if (jsonMsg.getString("cmd") == "Seek") {
                // 伴唱收到原唱seek指令
                if (isChorusCoSinger()) {
                    val position = jsonMsg.getLong("position")
                    mPlayer.seek(position)
                }
            } else if (jsonMsg.getString("cmd") == "PlayerState") {
                // 其他端收到原唱seek指令
                val state = jsonMsg.getInt("state")
                val error = jsonMsg.getInt("error")
                if (isChorusCoSinger()) {
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
                val score = jsonMsg.getDouble("score").toFloat()
                if (!isChorusCoSinger()) {
                    ktvApiEventHandler?.onSingingScoreResult(score)
                }
            }
        } catch (exp: JSONException) {
            Log.e(TAG, "onStreamMessage:$exp")
        }
    }

    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
        super.onAudioVolumeIndication(speakers, totalVolume)
        val allSpeakers = speakers ?: return
        // VideoPitch 回调, 用于同步各端音准
        if (this.singerRole != KTVSingRole.Audience) {
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
        Log.d(TAG, "onLocalAudioStats, audioPlay-outDelay=$audioPlayoutDelay")
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
        val id = requestId ?: return
        val callback = musicCollectionCallbackMap[id] ?: return
        musicCollectionCallbackMap.remove(id)
        callback.invoke(requestId, status, page, pageSize, total, list)
    }

    override fun onMusicChartsResult(
        requestId: String?,
        status: Int,
        list: Array<out MusicChartInfo>?
    ) {
        val id = requestId ?: return
        val callback = musicChartsCallbackMap[id] ?: return
        musicChartsCallbackMap.remove(id)
        callback.invoke(requestId, status, list)
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
        val mediaPlayerState = state ?: return
        val mediaPlayerError = error ?: return
        Log.d(TAG, "onPlayerStateChanged called, state: $mediaPlayerState, error: $error")
        when (mediaPlayerState) {
            Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                duration = mPlayer.duration
                this.localPlayerPosition = 0
                mPlayer.selectAudioTrack(1)
                if (this.singerRole == KTVSingRole.SoloSinger ||
                    this.singerRole == KTVSingRole.LeadSinger
                ) {
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
                val view = lrcView ?: return
                val score = view.cumulativeScoreInPercentage.toFloat()
                ktvApiEventHandler?.onSingingScoreResult(score)

                // 只有主唱同步分数给观众端
                if (isChorusCoSinger()) return
                syncSingingScore(score)
            }
            else -> {}
        }
        syncPlayState(mediaPlayerState, mediaPlayerError)
        ktvApiEventHandler?.onMusicPlayerStateChanged(mediaPlayerState, mediaPlayerError, true)
    }

    // 同步播放进度
    override fun onPositionChanged(position_ms: Long) {
        localPlayerPosition = position_ms
        localPlayerSystemTime = System.currentTimeMillis()

        if ((this.singerRole == KTVSingRole.SoloSinger || this.singerRole == KTVSingRole.LeadSinger) && position_ms > audioPlayoutDelay) {
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

        if (this.singerRole != KTVSingRole.Audience) {
            mLastReceivedPlayPosTime = System.currentTimeMillis()
            mReceivedPlayPosition = position_ms
        } else {
            mLastReceivedPlayPosTime = null
            mReceivedPlayPosition = 0
        }

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