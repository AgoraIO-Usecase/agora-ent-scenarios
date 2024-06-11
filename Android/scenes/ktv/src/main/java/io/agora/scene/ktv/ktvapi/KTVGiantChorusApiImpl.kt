package io.agora.scene.ktv.ktvapi

import android.os.Handler
import android.os.Looper
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.CacheStatistics
import io.agora.mediaplayer.data.PlayerPlaybackStats
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.musiccontentcenter.*
import io.agora.rtc2.*
import io.agora.rtc2.Constants.*
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.*

class KTVGiantChorusApiImpl(
    val giantChorusApiConfig: KTVGiantChorusApiConfig
) : KTVApi, IMusicContentCenterEventHandler, IMediaPlayerObserver, IRtcEngineEventHandler() {

    companion object {
        private val scheduledThreadPool: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
        private const val tag = "KTV_API_LOG_GIANT"
        private const val version = "5.0.0"
        private const val lyricSyncVersion = 2
    }

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private var mRtcEngine: RtcEngineEx = giantChorusApiConfig.engine as RtcEngineEx
    private lateinit var mMusicCenter: IAgoraMusicContentCenter
    private var mPlayer: IMediaPlayer
    private val apiReporter: APIReporter = APIReporter(APIType.KTV, version, mRtcEngine)

    private var innerDataStreamId: Int = 0
    private var singChannelRtcConnection: RtcConnection? = null
    private var subChorusConnection: RtcConnection? = null
    private var mpkConnection: RtcConnection? = null

    private var mainSingerUid: Int = 0
    private var songCode: Long = 0
    private var songUrl: String = ""
    private var songUrl2: String = ""
    private var songIdentifier: String = ""

    private val lyricCallbackMap =
        mutableMapOf<String, (songNo: Long, lyricUrl: String?) -> Unit>() // (requestId, callback)
    private val lyricSongCodeMap = mutableMapOf<String, Long>() // (requestId, songCode)
    private val loadMusicCallbackMap =
        mutableMapOf<String, (songCode: Long,
                              percent: Int,
                              status: Int,
                              msg: String?,
                              lyricUrl: String?) -> Unit>() // (songNo, callback)
    private val musicChartsCallbackMap =
        mutableMapOf<String, (requestId: String?, errorCode: Int, list: Array<out MusicChartInfo>?) -> Unit>()
    private val musicCollectionCallbackMap =
        mutableMapOf<String, (requestId: String?, errorCode: Int, page: Int, pageSize: Int, total: Int, list: Array<out Music>?) -> Unit>()

    private var lrcView: ILrcView? = null

    private var localPlayerPosition: Long = 0
    private var localPlayerSystemTime: Long = 0

    //歌词实时刷新
    private var mReceivedPlayPosition: Long = 0 //播放器播放position，ms
    private var mLastReceivedPlayPosTime: Long? = null

    // event
    private var ktvApiEventHandlerList = mutableListOf<IKTVApiEventHandler>()
    private var mainSingerHasJoinChannelEx: Boolean = false

    // 合唱校准
    private var audioPlayoutDelay = 0

    // 音高
    private var pitch = 0.0

    // 是否在麦上
    private var isOnMicOpen = false
    private var isRelease = false

    // mpk状态
    private var mediaPlayerState: MediaPlayerState = MediaPlayerState.PLAYER_STATE_IDLE

    private var professionalModeOpen = false
    private var audioRouting = 0
    private var isPublishAudio = false // 通过是否发音频流判断

    // 演唱分数
    private var singingScore = 0

    // multipath
    private var enableMultipathing = true

    // 歌词信息是否来源于 dataStream
    private var recvFromDataStream = false

    // 开始播放歌词
    private var mStopDisplayLrc = true
    private var displayLrcFuture: ScheduledFuture<*>? = null
    private val displayLrcTask = object : Runnable {
        override fun run() {
            if (!mStopDisplayLrc){
                if (singerRole == KTVSingRole.Audience && !recvFromDataStream) return  // audioMetaData方案观众return
                val lastReceivedTime = mLastReceivedPlayPosTime ?: return
                val curTime = System.currentTimeMillis()
                val offset = curTime - lastReceivedTime
                if (offset <= 100) {
                    val curTs = mReceivedPlayPosition + offset
                    if (singerRole == KTVSingRole.LeadSinger || singerRole == KTVSingRole.SoloSinger) {
                        val lrcTime = LrcTimeOuterClass.LrcTime.newBuilder()
                            .setTypeValue(LrcTimeOuterClass.MsgType.LRC_TIME.number)
                            .setForward(true)
                            .setSongId(songIdentifier)
                            .setTs(curTs)
                            .setUid(giantChorusApiConfig.musicStreamUid)
                            .build()

                        mRtcEngine.sendAudioMetadataEx(lrcTime.toByteArray(), mpkConnection)
                    }
                    runOnMainThread {
                        lrcView?.onUpdatePitch(pitch.toFloat())
                        // (fix ENT-489)Make lyrics delay for 200ms
                        // Per suggestion from Bob, it has a intrinsic buffer/delay between sound and `onPositionChanged(Player)`,
                        // such as AEC/Player/Device buffer.
                        // We choose the estimated 200ms.
                        lrcView?.onUpdateProgress(if (curTs > 200) (curTs - 200) else curTs) // The delay here will impact both singer and audience side
                    }
                }
            }
        }
    }

    // 评分驱动混音
    private var mSyncScoreFuture :ScheduledFuture<*>? = null
    private var mStopSyncScore = true
    private val mSyncScoreTask = Runnable {
        if (!mStopSyncScore) {
            if (mediaPlayerState == MediaPlayerState.PLAYER_STATE_PLAYING &&
                (singerRole == KTVSingRole.LeadSinger || singerRole == KTVSingRole.CoSinger)) {
                sendSyncScore()
            }
        }
    }

    // 云端合流信息
    private var mSyncCloudConvergenceStatusFuture :ScheduledFuture<*>? = null
    private var mStopSyncCloudConvergenceStatus = true
    private val mSyncCloudConvergenceStatusTask = Runnable {
        if (!mStopSyncCloudConvergenceStatus && singerRole == KTVSingRole.LeadSinger) {
            sendSyncCloudConvergenceStatus()
        }
    }

    init {
        apiReporter.reportFuncEvent("initialize", mapOf("config" to giantChorusApiConfig), mapOf())
        this.singChannelRtcConnection = RtcConnection(giantChorusApiConfig.chorusChannelName, giantChorusApiConfig.localUid)

        // ------------------ 初始化内容中心 ------------------
        if (giantChorusApiConfig.musicType == KTVMusicType.SONG_CODE) {
            val contentCenterConfiguration = MusicContentCenterConfiguration()
            contentCenterConfiguration.appId = giantChorusApiConfig.appId
            contentCenterConfiguration.mccUid = giantChorusApiConfig.localUid.toLong()
            contentCenterConfiguration.token = giantChorusApiConfig.rtmToken
            contentCenterConfiguration.maxCacheSize = giantChorusApiConfig.maxCacheSize
            if (KTVApi.debugMode) {
                contentCenterConfiguration.mccDomain = KTVApi.mccDomain
            }
            mMusicCenter = IAgoraMusicContentCenter.create(mRtcEngine)
            mMusicCenter.initialize(contentCenterConfiguration)
            mMusicCenter.registerEventHandler(this)

            // ------------------ 初始化音乐播放器实例 ------------------
            mPlayer = mMusicCenter.createMusicPlayer()
        } else {
            mPlayer = mRtcEngine.createMediaPlayer()
        }
        mPlayer.adjustPublishSignalVolume(KTVApi.mpkPublishVolume)
        mPlayer.adjustPlayoutVolume(KTVApi.mpkPlayoutVolume)

        // 注册回调
        mPlayer.registerPlayerObserver(this)
        setKTVParameters()
        startDisplayLrc()
        startSyncScore()
        startSyncCloudConvergenceStatus()
        isRelease = false

        mPlayer.setPlayerOption("play_pos_change_callback", 100)
    }

    // 日志输出
    private fun ktvApiLog(msg: String) {
        if (isRelease) return
        apiReporter.writeLog("[${tag}] $msg", LOG_LEVEL_INFO)
    }

    // 日志输出
    private fun ktvApiLogError(msg: String) {
        if (isRelease) return
        apiReporter.writeLog("[${tag}] $msg", LOG_LEVEL_ERROR)
    }

    override fun renewInnerDataStreamId() {
        apiReporter.reportFuncEvent("renewInnerDataStreamId", mapOf(), mapOf())

        val innerCfg = DataStreamConfig()
        innerCfg.syncWithAudio = true
        innerCfg.ordered = false
        this.innerDataStreamId = mRtcEngine.createDataStreamEx(innerCfg, singChannelRtcConnection)
    }

    private fun setKTVParameters() {
        mRtcEngine.setParameters("{\"rtc.enable_nasa2\": true}")
        mRtcEngine.setParameters("{\"rtc.ntp_delay_drop_threshold\":1000}")
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp\": true}")
        mRtcEngine.setParameters("{\"rtc.net.maxS2LDelay\": 800}")
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")

        mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
        mRtcEngine.setParameters("{\"che.audio.neteq.targetlevel_offset\": 20}")

        mRtcEngine.setParameters("{\"rtc.net.maxS2LDelayBroadcast\":400}")
        mRtcEngine.setParameters("{\"che.audio.neteq.prebuffer\":true}")
        mRtcEngine.setParameters("{\"che.audio.neteq.prebuffer_max_delay\":600}")
        mRtcEngine.setParameters("{\"che.audio.max_mixed_participants\": 8}")
        mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
        mRtcEngine.setParameters("{\"che.audio.uplink_apm_async_process\": true}")

        // 标准音质
        mRtcEngine.setParameters("{\"che.audio.aec.split_srate_for_48k\": 16000}")

        // ENT-901
        mRtcEngine.setParameters("{\"che.audio.ans.noise_gate\": 20}")

        // Android Only
        mRtcEngine.setParameters("{\"che.audio.enable_estimated_device_delay\":false}")

        // TopN + SendAudioMetadata
        mRtcEngine.setParameters("{\"rtc.use_audio4\": true}")

        // mutipath
        enableMultipathing = false
        //mRtcEngine.setParameters("{\"rtc.enableMultipath\": true}")
        mRtcEngine.setParameters("{\"rtc.enable_tds_request_on_join\": true}")
        //mRtcEngine.setParameters("{\"rtc.remote_path_scheduling_strategy\": 0}")
        //mRtcEngine.setParameters("{\"rtc.path_scheduling_strategy\": 0}")

        // 数据上报
        mRtcEngine.setParameters("{\"rtc.direct_send_custom_event\": true}")
    }

    private fun resetParameters() {
        mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
        mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 80000}")     // 兼容之前的profile = 3设置
        mRtcEngine.setParameters("{\"che.audio.max_mixed_participants\": 3}") // 正常3路下行流混流
        mRtcEngine.setParameters("{\"che.audio.neteq.prebuffer\": false}")    // 关闭 接收端快速对齐模式
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp\": false}") // 观众关闭 多端同步
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\": false}") //主播关闭多端同步
    }

    override fun addEventHandler(ktvApiEventHandler: IKTVApiEventHandler) {
        apiReporter.reportFuncEvent("addEventHandler", mapOf("ktvApiEventHandler" to ktvApiEventHandler), mapOf())
        ktvApiEventHandlerList.add(ktvApiEventHandler)
    }

    override fun removeEventHandler(ktvApiEventHandler: IKTVApiEventHandler) {
        apiReporter.reportFuncEvent("removeEventHandler", mapOf("ktvApiEventHandler" to ktvApiEventHandler), mapOf())
        ktvApiEventHandlerList.remove(ktvApiEventHandler)
    }

    override fun release() {
        apiReporter.reportFuncEvent("release", mapOf(), mapOf())
        if (isRelease) return
        isRelease = true
        singerRole = KTVSingRole.Audience

        resetParameters()
        stopSyncCloudConvergenceStatus()
        stopSyncScore()
        stopDisplayLrc()
        this.mLastReceivedPlayPosTime = null
        this.mReceivedPlayPosition = 0
        this.innerDataStreamId = 0
        this.singingScore = 0

        lyricCallbackMap.clear()
        loadMusicCallbackMap.clear()
        musicChartsCallbackMap.clear()
        musicCollectionCallbackMap.clear()
        lrcView = null

        mPlayer.unRegisterPlayerObserver(this)

        if (giantChorusApiConfig.musicType == KTVMusicType.SONG_CODE) {
            mMusicCenter.unregisterEventHandler()
        }

        mPlayer.stop()
        mPlayer.destroy()
        IAgoraMusicContentCenter.destroy()

        mainSingerHasJoinChannelEx = false
        professionalModeOpen = false
        audioRouting = 0
        isPublishAudio = false
    }

    override fun enableProfessionalStreamerMode(enable: Boolean) {
        apiReporter.reportFuncEvent("enableProfessionalStreamerMode", mapOf("enable" to enable), mapOf())
        this.professionalModeOpen = enable
        processAudioProfessionalProfile()
    }

    private fun processAudioProfessionalProfile() {
        ktvApiLog("processAudioProfessionalProfile: audioRouting: $audioRouting, professionalModeOpen: $professionalModeOpen， isPublishAudio：$isPublishAudio")
        if (!isPublishAudio) return // 必须为麦上者
        if (professionalModeOpen) {
            // 专业
            if (audioRouting == 0 || audioRouting == 2 || audioRouting == 5 || audioRouting == 6) {
                // 耳机 关闭3A 关闭md
                mRtcEngine.setParameters("{\"che.audio.aec.enable\": false}")
                mRtcEngine.setParameters("{\"che.audio.agc.enable\": false}")
                mRtcEngine.setParameters("{\"che.audio.ans.enable\": false}")
                mRtcEngine.setParameters("{\"che.audio.md.enable\": false}")
                mRtcEngine.setAudioProfile(AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO) // AgoraAudioProfileMusicHighQualityStereo
            } else {
                // 非耳机 开启3A 关闭md
                mRtcEngine.setParameters("{\"che.audio.aec.enable\": true}")
                mRtcEngine.setParameters("{\"che.audio.agc.enable\": true}")
                mRtcEngine.setParameters("{\"che.audio.ans.enable\": true}")
                mRtcEngine.setParameters("{\"che.audio.md.enable\": false}")
                mRtcEngine.setAudioProfile(AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO) // AgoraAudioProfileMusicHighQualityStereo
            }
        } else {
            // 非专业 开启3A 关闭md
            mRtcEngine.setParameters("{\"che.audio.aec.enable\": true}")
            mRtcEngine.setParameters("{\"che.audio.agc.enable\": true}")
            mRtcEngine.setParameters("{\"che.audio.ans.enable\": true}")
            mRtcEngine.setParameters("{\"che.audio.md.enable\": false}")
            mRtcEngine.setAudioProfile(AUDIO_PROFILE_MUSIC_STANDARD_STEREO) // AgoraAudioProfileMusicStandardStereo
        }
    }

    override fun enableMulitpathing(enable: Boolean) {
        apiReporter.reportFuncEvent("enableMulitpathing", mapOf("enable" to enable), mapOf())
        this.enableMultipathing = enable

        // TODO 4.3.1 not ready
//        if (singerRole == KTVSingRole.LeadSinger || singerRole == KTVSingRole.CoSinger) {
//            subChorusConnection?.let {
//                mRtcEngine.updateChannelMediaOptionsEx(ChannelMediaOptions().apply {
//                    parameters = "{\"rtc.enableMultipath\": $enable, \"rtc.path_scheduling_strategy\": 0, \"rtc.remote_path_scheduling_strategy\": 0}"
//                }, subChorusConnection)
//            }
//        }
    }

    override fun renewToken(rtmToken: String, chorusChannelRtcToken: String) {
        apiReporter.reportFuncEvent("renewToken", mapOf(), mapOf())
        // 更新RtmToken
        mMusicCenter.renewToken(rtmToken)
        // 更新合唱频道RtcToken
        if (subChorusConnection != null) {
            val channelMediaOption = ChannelMediaOptions()
            channelMediaOption.token = chorusChannelRtcToken
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption, subChorusConnection)
        }
    }

    // 1、Audience -》SoloSinger
    // 2、Audience -》LeadSinger
    // 3、SoloSinger -》Audience
    // 4、Audience -》CoSinger
    // 5、CoSinger -》Audience
    // 6、SoloSinger -》LeadSinger
    // 7、LeadSinger -》SoloSinger
    // 8、LeadSinger -》Audience
    var singerRole: KTVSingRole = KTVSingRole.Audience

    override fun switchSingerRole(
        newRole: KTVSingRole,
        switchRoleStateListener: ISwitchRoleStateListener?
    ) {
        apiReporter.reportFuncEvent("switchSingerRole", mapOf("newRole" to newRole), mapOf())
        ktvApiLog("switchSingerRole oldRole: $singerRole, newRole: $newRole")
        val oldRole = singerRole
        if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.LeadSinger) {
            // 1、Audience -》LeadSinger
            // 离开观众频道
            mRtcEngine.leaveChannelEx(RtcConnection(giantChorusApiConfig.audienceChannelName, giantChorusApiConfig.localUid))
            joinChorus(newRole)
            singerRole = newRole
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()
        } else if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.CoSinger) {
            // 2、Audience -》CoSinger
            // 离开观众频道
            mRtcEngine.leaveChannelEx(RtcConnection(giantChorusApiConfig.audienceChannelName, giantChorusApiConfig.localUid))
            joinChorus(newRole)
            singerRole = newRole
            switchRoleStateListener?.onSwitchRoleSuccess()
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
        } else if (this.singerRole == KTVSingRole.CoSinger && newRole == KTVSingRole.Audience) {
            // 3、CoSinger -》Audience
            leaveChorus2(singerRole)
            // 加入观众频道
            mRtcEngine.joinChannelEx(giantChorusApiConfig.audienceChannelToken, RtcConnection(giantChorusApiConfig.audienceChannelName, giantChorusApiConfig.localUid), ChannelMediaOptions(), object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                }

                override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                    super.onStreamMessage(uid, streamId, data)
                    dealWithStreamMessage(uid, streamId, data)
                }

                override fun onAudioMetadataReceived(uid: Int, data: ByteArray?) {
                    super.onAudioMetadataReceived(uid, data)
                    dealWithAudioMetadata(uid, data)
                }
            })

            singerRole = newRole
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()
        } else if (this.singerRole == KTVSingRole.LeadSinger && newRole == KTVSingRole.Audience) {
            // 4、LeadSinger -》Audience
            stopSing()
            leaveChorus2(singerRole)

            // 加入观众频道
            mRtcEngine.joinChannelEx(giantChorusApiConfig.audienceChannelToken, RtcConnection(giantChorusApiConfig.audienceChannelName, giantChorusApiConfig.localUid), ChannelMediaOptions(), object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                }

                override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                    super.onStreamMessage(uid, streamId, data)
                    dealWithStreamMessage(uid, streamId, data)
                }

                override fun onAudioMetadataReceived(uid: Int, data: ByteArray?) {
                    super.onAudioMetadataReceived(uid, data)
                    dealWithAudioMetadata(uid, data)
                }
            })

            singerRole = newRole
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()
        } else {
            switchRoleStateListener?.onSwitchRoleFail(SwitchRoleFailReason.NO_PERMISSION)
            ktvApiLogError("Error！You can not switch role from $singerRole to $newRole!")
        }
    }

    override fun fetchMusicCharts(onMusicChartResultListener: (requestId: String?, status: Int, list: Array<out MusicChartInfo>?) -> Unit) {
        apiReporter.reportFuncEvent("fetchMusicCharts", mapOf(), mapOf())
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
        apiReporter.reportFuncEvent("searchMusicByMusicChartId", mapOf(), mapOf())
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
        apiReporter.reportFuncEvent("searchMusicByKeyword", mapOf(), mapOf())
        val requestId = mMusicCenter.searchMusic(keyword, page, pageSize, jsonOption)
        musicCollectionCallbackMap[requestId] = onMusicCollectionResultListener
    }

    override fun loadMusic(
        songCode: Long,
        config: KTVLoadMusicConfiguration,
        musicLoadStateListener: IMusicLoadStateListener
    ) {
        apiReporter.reportFuncEvent("loadMusic", mapOf("songCode" to songCode, "config" to config), mapOf())
        ktvApiLog("loadMusic called: songCode $songCode")
        // 设置到全局， 连续调用以最新的为准
        this.songCode = songCode
        this.songIdentifier = config.songIdentifier
        this.mainSingerUid = config.mainSingerUid
        mLastReceivedPlayPosTime = null
        mReceivedPlayPosition = 0

        if (config.mode == KTVLoadMusicMode.LOAD_NONE) {
            return
        }

        if (config.mode == KTVLoadMusicMode.LOAD_LRC_ONLY) {
            // 只加载歌词
            loadLyric(songCode) { song, lyricUrl ->
                if (this.songCode != song) {
                    // 当前歌曲已发生变化，以最新load歌曲为准
                    ktvApiLogError("loadMusic failed: CANCELED")
                    musicLoadStateListener.onMusicLoadFail(song, KTVLoadMusicFailReason.CANCELED)
                    return@loadLyric
                }

                if (lyricUrl == null) {
                    // 加载歌词失败
                    ktvApiLogError("loadMusic failed: NO_LYRIC_URL")
                    musicLoadStateListener.onMusicLoadFail(song, KTVLoadMusicFailReason.NO_LYRIC_URL)
                } else {
                    // 加载歌词成功
                    ktvApiLog("loadMusic success")
                    lrcView?.onDownloadLrcData(lyricUrl)
                    musicLoadStateListener.onMusicLoadSuccess(song, lyricUrl)
                }
            }
            return
        }

        // 预加载歌曲
        preLoadMusic(songCode) { song, percent, status, msg, lrcUrl ->
            if (status == 0) {
                // 预加载歌曲成功
                if (this.songCode != song) {
                    // 当前歌曲已发生变化，以最新load歌曲为准
                    ktvApiLogError("loadMusic failed: CANCELED")
                    musicLoadStateListener.onMusicLoadFail(song, KTVLoadMusicFailReason.CANCELED)
                    return@preLoadMusic
                }
                if (config.mode == KTVLoadMusicMode.LOAD_MUSIC_AND_LRC) {
                    // 需要加载歌词
                    loadLyric(song) { _, lyricUrl ->
                        if (this.songCode != song) {
                            // 当前歌曲已发生变化，以最新load歌曲为准
                            ktvApiLogError("loadMusic failed: CANCELED")
                            musicLoadStateListener.onMusicLoadFail(song, KTVLoadMusicFailReason.CANCELED)
                            return@loadLyric
                        }

                        if (lyricUrl == null) {
                            // 加载歌词失败
                            ktvApiLogError("loadMusic failed: NO_LYRIC_URL")
                            musicLoadStateListener.onMusicLoadFail(song, KTVLoadMusicFailReason.NO_LYRIC_URL)
                        } else {
                            // 加载歌词成功
                            ktvApiLog("loadMusic success")
                            lrcView?.onDownloadLrcData(lyricUrl)
                            musicLoadStateListener.onMusicLoadProgress(song, 100, MusicLoadStatus.COMPLETED, msg, lrcUrl)
                            musicLoadStateListener.onMusicLoadSuccess(song, lyricUrl)
                        }
                    }
                } else if (config.mode == KTVLoadMusicMode.LOAD_MUSIC_ONLY) {
                    // 不需要加载歌词
                    ktvApiLog("loadMusic success")
                    musicLoadStateListener.onMusicLoadProgress(song, 100, MusicLoadStatus.COMPLETED, msg, lrcUrl)
                    musicLoadStateListener.onMusicLoadSuccess(song, "")
                }
            } else if (status == 2) {
                // 预加载歌曲加载中
                musicLoadStateListener.onMusicLoadProgress(song, percent, MusicLoadStatus.values().firstOrNull { it.value == status } ?: MusicLoadStatus.FAILED, msg, lrcUrl)
            } else {
                // 预加载歌曲失败
                ktvApiLogError("loadMusic failed: MUSIC_PRELOAD_FAIL")
                musicLoadStateListener.onMusicLoadFail(song, KTVLoadMusicFailReason.MUSIC_PRELOAD_FAIL)
            }
        }
    }

    override fun loadMusic(
        url: String,
        config: KTVLoadMusicConfiguration
    ) {
        apiReporter.reportFuncEvent("loadMusic", mapOf("url" to url, "config" to config), mapOf())
        ktvApiLog("loadMusic called: songCode $songCode")
        this.songIdentifier = config.songIdentifier
        this.songUrl = url
        this.mainSingerUid = config.mainSingerUid
    }

    override fun removeMusic(songCode: Long) {
        apiReporter.reportFuncEvent("removeMusic", mapOf("songCode" to songCode), mapOf())
        val ret = mMusicCenter.removeCache(songCode)
        if (ret < 0) {
            ktvApiLogError("removeMusic failed, ret: $ret")
        }
    }

    override fun load2Music(url1: String, url2: String, config: KTVLoadMusicConfiguration) {
        apiReporter.reportFuncEvent("load2Music", mapOf("url1" to url1, "url2" to url2, "config" to config), mapOf())
        this.songIdentifier = config.songIdentifier
        this.songUrl = url1
        this.songUrl2 = url2
        this.mainSingerUid = config.mainSingerUid
    }

    override fun switchPlaySrc(url: String, syncPts: Boolean) {
        apiReporter.reportFuncEvent("switchPlaySrc", mapOf("url" to url, "syncPts" to syncPts), mapOf())
        if (this.songUrl != url && this.songUrl2 != url) {
            ktvApiLogError("switchPlaySrc failed: canceled")
            return
        }
        val curPlayPosition = if (syncPts) mPlayer.playPosition else 0
        mPlayer.stop()
        startSing(url, curPlayPosition)
    }

    override fun startSing(songCode: Long, startPos: Long) {
        apiReporter.reportFuncEvent("startSing", mapOf("songCode" to songCode, "startPos" to startPos), mapOf())
        ktvApiLog("playSong called: $singerRole")
        if (singerRole != KTVSingRole.SoloSinger && singerRole != KTVSingRole.LeadSinger) {
            ktvApiLogError("startSing failed: error singerRole")
            return
        }
        if (this.songCode != songCode) {
            ktvApiLogError("startSing failed: canceled")
            return
        }
        mRtcEngine.adjustPlaybackSignalVolume(KTVApi.remoteVolume)

        // 导唱
        mPlayer.setPlayerOption("enable_multi_audio_track", 1)
        val ret = (mPlayer as IAgoraMusicPlayer).open(songCode, startPos)
        if (ret != 0) {
            ktvApiLogError("mpk open failed: $ret")
        }
    }

    override fun startSing(url: String, startPos: Long) {
        apiReporter.reportFuncEvent("startSing", mapOf("url" to url, "startPos" to startPos), mapOf())
        ktvApiLog("playSong called: $singerRole")
        if (singerRole != KTVSingRole.SoloSinger && singerRole != KTVSingRole.LeadSinger) {
            ktvApiLogError("startSing failed: error singerRole")
            return
        }
        if (this.songUrl != url && this.songUrl2 != url) {
            ktvApiLogError("startSing failed: canceled")
            return
        }
        mRtcEngine.adjustPlaybackSignalVolume(KTVApi.remoteVolume)

        // 导唱
        mPlayer.setPlayerOption("enable_multi_audio_track", 1)
        val ret = mPlayer.open(url, startPos)
        if (ret != 0) {
            ktvApiLogError("mpk open failed: $ret")
        }
    }

    override fun resumeSing() {
        apiReporter.reportFuncEvent("resumeSing", mapOf(), mapOf())
        ktvApiLog("resumePlay called")
        mPlayer.resume()
    }

    override fun pauseSing() {
        apiReporter.reportFuncEvent("pauseSing", mapOf(), mapOf())
        ktvApiLog("pausePlay called")
        mPlayer.pause()
    }

    override fun seekSing(time: Long) {
        apiReporter.reportFuncEvent("seekSing", mapOf("time" to time), mapOf())
        ktvApiLog("seek called")
        mPlayer.seek(time)
        syncPlayProgress(time)
    }

    override fun setLrcView(view: ILrcView) {
        apiReporter.reportFuncEvent("setLrcView", mapOf(), mapOf())
        ktvApiLog("setLrcView called")
        this.lrcView = view
    }

    override fun muteMic(mute: Boolean) {
        apiReporter.reportFuncEvent("muteMic", mapOf("mute" to mute), mapOf())
        this.isOnMicOpen = !mute
        if (singerRole == KTVSingRole.Audience) return
        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.publishMicrophoneTrack = isOnMicOpen
        channelMediaOption.clientRoleType = CLIENT_ROLE_BROADCASTER
        mRtcEngine.updateChannelMediaOptions(channelMediaOption)
        mRtcEngine.muteLocalAudioStreamEx(!isOnMicOpen, singChannelRtcConnection)
    }

    override fun setAudioPlayoutDelay(audioPlayoutDelay: Int) {
        apiReporter.reportFuncEvent("setAudioPlayoutDelay", mapOf("audioPlayoutDelay" to audioPlayoutDelay), mapOf())
        this.audioPlayoutDelay = audioPlayoutDelay
    }

    fun setSingingScore(score: Int) {
        this.singingScore = score
    }

    fun setAudienceStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
        dealWithStreamMessage(uid, streamId, data)
    }

    fun setAudienceAudioMetadataReceived(uid: Int, data: ByteArray?) {
        dealWithAudioMetadata(uid, data)
    }

    override fun getMediaPlayer(): IMediaPlayer {
        return mPlayer
    }

    override fun getMusicContentCenter(): IAgoraMusicContentCenter {
        return mMusicCenter
    }

    override fun switchAudioTrack(mode: AudioTrackMode) {
        apiReporter.reportFuncEvent("switchAudioTrack", mapOf("mode" to mode), mapOf())
        when (singerRole) {
            KTVSingRole.LeadSinger, KTVSingRole.SoloSinger -> {
                when (mode) {
                    AudioTrackMode.YUAN_CHANG -> mPlayer.selectMultiAudioTrack(0, 0)
                    AudioTrackMode.BAN_ZOU -> mPlayer.selectMultiAudioTrack(1, 1)
                    AudioTrackMode.DAO_CHANG -> mPlayer.selectMultiAudioTrack(0, 1)
                }
            }
            KTVSingRole.CoSinger -> {
                when (mode) {
                    AudioTrackMode.YUAN_CHANG -> mPlayer.selectAudioTrack(0)
                    AudioTrackMode.BAN_ZOU -> mPlayer.selectAudioTrack(1)
                    AudioTrackMode.DAO_CHANG -> ktvApiLogError("CoSinger can not switch to DAO_CHANG")
                }
            }
            KTVSingRole.Audience -> ktvApiLogError("CoSinger can not switch audio track")
        }
    }

    // ------------------ inner KTVApi --------------------
    private fun stopSing() {
        ktvApiLog("stopSong called")

        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.autoSubscribeAudio = true
        channelMediaOption.publishMediaPlayerAudioTrack = false
        mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption, singChannelRtcConnection)

        mPlayer.stop()

        // 更新音频配置
        mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
        mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
        mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
    }

    private val subScribeSingerMap = mutableMapOf<Int, Int>() // <uid, ntpE2eDelay>
    private val singerList = mutableListOf<Int>() // <uid>
    private var mainSingerDelay = 0
    private fun joinChorus(newRole: KTVSingRole) {
        ktvApiLog("joinChorus: $newRole")
        val singChannelMediaOptions = ChannelMediaOptions()
        singChannelMediaOptions.autoSubscribeAudio = true
        singChannelMediaOptions.publishMicrophoneTrack = true
        singChannelMediaOptions.clientRoleType = CLIENT_ROLE_BROADCASTER
        //singChannelMediaOptions.parameters = "{\"che.audio.max_mixed_participants\": 8}" // TODO 4.3.1 not ready
        if (newRole == KTVSingRole.LeadSinger) {
            // 主唱不参加TopN
            singChannelMediaOptions.isAudioFilterable = false
            mRtcEngine.setParameters("{\"che.audio.filter_streams\":${KTVApi.routeSelectionConfig.streamNum}}")
        } else {
            mRtcEngine.setParameters("{\"che.audio.filter_streams\":${KTVApi.routeSelectionConfig.streamNum - 1}}")
        }

        // 加入演唱频道
        mRtcEngine.joinChannelEx(giantChorusApiConfig.chorusChannelToken, singChannelRtcConnection, singChannelMediaOptions, object :
            IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)
                ktvApiLog("singChannel onJoinChannelSuccess: $newRole")
            }

            override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                super.onStreamMessage(uid, streamId, data)
                dealWithStreamMessage(uid, streamId, data)
            }

            override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
                val allSpeakers = speakers ?: return
                // VideoPitch 回调, 用于同步各端音准
                if (singerRole != KTVSingRole.Audience) {
                    for (info in allSpeakers) {
                        if (info.uid == 0) {
                            pitch =
                                if (mediaPlayerState == MediaPlayerState.PLAYER_STATE_PLAYING && isOnMicOpen) {
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
                if (KTVApi.useCustomAudioSource) return
                val audioState = stats ?: return
                audioPlayoutDelay = audioState.audioPlayoutDelay
            }

            // 用于检测耳机状态
            override fun onAudioRouteChanged(routing: Int) { // 0\2\5 earPhone
                audioRouting = routing
                processAudioProfessionalProfile()
            }

            // 用于检测收发流状态
            override fun onAudioPublishStateChanged(
                channel: String?,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
            ) {
                ktvApiLog("onAudioPublishStateChanged: oldState: $oldState, newState: $newState")
                if (newState == 3) {
                    isPublishAudio = true
                    processAudioProfessionalProfile()
                } else if (newState == 1) {
                    isPublishAudio = false
                }
            }

            // 延迟选路策略
            override fun onUserJoined(uid: Int, elapsed: Int) {
                super.onUserJoined(uid, elapsed)
                if (uid != giantChorusApiConfig.musicStreamUid && subScribeSingerMap.size < 8) {
                    mRtcEngine.muteRemoteAudioStreamEx(uid, false, singChannelRtcConnection)
                    if (uid != mainSingerUid) {
                        subScribeSingerMap[uid] = 0
                    }
                } else if (uid != giantChorusApiConfig.musicStreamUid && subScribeSingerMap.size == 8) {
                    mRtcEngine.muteRemoteAudioStreamEx(uid, true, singChannelRtcConnection)
                }
                if (uid != giantChorusApiConfig.musicStreamUid && uid != mainSingerUid) {
                    singerList.add(uid)
                }
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
                subScribeSingerMap.remove(uid)
                singerList.remove(uid)
            }

            override fun onLeaveChannel(stats: RtcStats?) {
                super.onLeaveChannel(stats)
                subScribeSingerMap.clear()
                singerList.clear()
            }

            override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
                super.onRemoteAudioStats(stats)
                stats ?: return
                if (KTVApi.routeSelectionConfig.type == GiantChorusRouteSelectionType.RANDOM || KTVApi.routeSelectionConfig.type == GiantChorusRouteSelectionType.TOP_N) return
                val uid = stats.uid
                if (uid == mainSingerUid) {
                    mainSingerDelay = stats.e2eDelay
                }
//                if (uid == mainSingerUid && stats.e2eDelay > 300) {
//                    //ToastUtils.showToast("主唱 $mainSingerUid 延迟超过300ms，目前延迟：${stats.ntpE2eDelay}")
//                }
//                if (subScribeSingerMap.any { it.key == uid } && stats.e2eDelay > 300) {
//                    //ToastUtils.showToast("当前订阅用户 $uid 延迟超过300ms，目前延迟：${stats.ntpE2eDelay}")
//                }
                if (uid != mainSingerUid && uid != giantChorusApiConfig.musicStreamUid && subScribeSingerMap.containsKey(uid)) {
                    subScribeSingerMap[uid] = stats.e2eDelay
                }
            }
        })
        mRtcEngine.setParameters("{\"rtc.use_audio4\": true}")
        // 选路策略处理
        if (KTVApi.routeSelectionConfig.type == GiantChorusRouteSelectionType.TOP_N || KTVApi.routeSelectionConfig.type == GiantChorusRouteSelectionType.BY_DELAY_AND_TOP_N) {
            if (newRole == KTVSingRole.LeadSinger) {
                mRtcEngine.setParameters("{\"che.audio.filter_streams\":${KTVApi.routeSelectionConfig.streamNum}}")
            } else {
                mRtcEngine.setParameters("{\"che.audio.filter_streams\":${KTVApi.routeSelectionConfig.streamNum - 1}}")
            }
        } else {
            mRtcEngine.setParameters("{\"che.audio.filter_streams\": 0}")
        }
        mRtcEngine.enableAudioVolumeIndicationEx(50, 10, true, singChannelRtcConnection)

        when (newRole) {
            KTVSingRole.LeadSinger -> {
                // 更新音频配置
                mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)
                mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
                mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
                mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 80000}")

                // mpk流加入频道
                val options = ChannelMediaOptions()
                options.autoSubscribeAudio = false
                options.autoSubscribeVideo = false
                options.publishMicrophoneTrack = false
                options.publishMediaPlayerAudioTrack = true
                options.publishMediaPlayerId = mPlayer.mediaPlayerId
                options.clientRoleType = CLIENT_ROLE_BROADCASTER
                // 防止主唱和合唱听见mpk流的声音
                options.enableAudioRecordingOrPlayout = false

                val rtcConnection = RtcConnection()
                rtcConnection.channelId = giantChorusApiConfig.chorusChannelName
                rtcConnection.localUid = giantChorusApiConfig.musicStreamUid
                mpkConnection = rtcConnection

                mRtcEngine.joinChannelEx(
                    giantChorusApiConfig.musicStreamToken,
                    mpkConnection,
                    options,
                    object : IRtcEngineEventHandler() {
                        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                            ktvApiLog("onMPKJoinChannelSuccess, channel: $channel, uid: $uid")
                        }

                        override fun onLeaveChannel(stats: RtcStats) {
                            ktvApiLog("onMPKLeaveChannel")
                        }
                    })
            }
            KTVSingRole.CoSinger -> {
                // 防止主唱和合唱听见mpk流的声音
                mRtcEngine.muteRemoteAudioStreamEx(
                    giantChorusApiConfig.musicStreamUid,
                    true,
                    singChannelRtcConnection
                )

                // 更新音频配置
                mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)
                mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
                mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
                mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")

                // 预加载歌曲成功
                // 导唱
                mPlayer.setPlayerOption("enable_multi_audio_track", 1)
                if (giantChorusApiConfig.musicType == KTVMusicType.SONG_CODE) {
                    val ret = (mPlayer as IAgoraMusicPlayer).open(songCode, 0) // TODO open failed
                    if (ret != 0) {
                        ktvApiLogError("mpk open failed: $ret")
                    }
                } else {
                    val ret = mPlayer.open(songUrl, 0) // TODO open failed
                    if (ret != 0) {
                        ktvApiLogError("mpk open failed: $ret")
                    }
                }
            }
            else -> {
                ktvApiLogError("JoinChorus with Wrong role: $singerRole")
            }
        }

        mRtcEngine.muteRemoteAudioStreamEx(giantChorusApiConfig.musicStreamUid, true, singChannelRtcConnection)
        // 加入演唱频道后，创建data stream
        renewInnerDataStreamId()
    }

    private fun leaveChorus2(role: KTVSingRole) {
        ktvApiLog("leaveChorus: $singerRole")
        when (role) {
            KTVSingRole.LeadSinger -> {
                mRtcEngine.leaveChannelEx(mpkConnection)
            }
            KTVSingRole.CoSinger -> {
                mPlayer.stop()

                // 更新音频配置
                mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
                mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
                mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
                mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
            }
            else -> {
                ktvApiLogError("JoinChorus with wrong role: $singerRole")
            }
        }
        mRtcEngine.leaveChannelEx(singChannelRtcConnection)
    }

    // ------------------ inner --------------------

    private fun isChorusCoSinger(): Boolean {
        return singerRole == KTVSingRole.CoSinger
    }

    private fun sendStreamMessageWithJsonObject(
        obj: JSONObject,
        success: (isSendSuccess: Boolean) -> Unit
    ) {
        val ret = mRtcEngine.sendStreamMessageEx(innerDataStreamId, obj.toString().toByteArray(), singChannelRtcConnection)
        if (ret == 0) {
            success.invoke(true)
        } else {
            ktvApiLogError("sendStreamMessageWithJsonObject failed: $ret, innerDataStreamId:$innerDataStreamId")
        }
    }

    private fun syncPlayState(
        state: Constants.MediaPlayerState,
        error: Constants.MediaPlayerReason
    ) {
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "PlayerState"
        msg["state"] = Constants.MediaPlayerState.getValue(state)
        msg["error"] = Constants.MediaPlayerReason.getValue(error)
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

    // ------------------ 歌词播放、同步 ------------------
    private fun startDisplayLrc() {
        ktvApiLog("startDisplayLrc called")
        mStopDisplayLrc = false
        displayLrcFuture = scheduledThreadPool.scheduleAtFixedRate(displayLrcTask, 0,20, TimeUnit.MILLISECONDS)
    }

    // 停止播放歌词
    private fun stopDisplayLrc() {
        ktvApiLog("stopDisplayLrc called")
        mStopDisplayLrc = true
        displayLrcFuture?.cancel(true)
        displayLrcFuture = null
        if (scheduledThreadPool is ScheduledThreadPoolExecutor) {
            scheduledThreadPool.remove(displayLrcTask)
        }
    }

    // ------------------ 评分驱动混音同步 ------------------
    private fun sendSyncScore() {
        val jsonObject = JSONObject()
        jsonObject.put("service", "audio_smart_mixer") // data message的目标消费者（服务）名
        jsonObject.put("version", "V1") //协议版本号（而非服务版本号）
        val payloadJson = JSONObject()
        payloadJson.put("cname", giantChorusApiConfig.chorusChannelName) // 频道名，演唱频道
        payloadJson.put("uid", giantChorusApiConfig.localUid.toString()) // 自己的uid
        payloadJson.put("uLv", -1) //user-leve1（用户级别，若无则为 -1，Level 越高，越重要）
        payloadJson.put("specialLabel", 0) //0: default-mode ，1：这个用户需要被排除出智能混音
        payloadJson.put("audioRoute", audioRouting) //音频路由：监听 onAudioRouteChanged
        payloadJson.put("vocalScore", singingScore) //单句打分
        jsonObject.put("payload", payloadJson)
        ktvApiLog("sendSyncScore: $jsonObject")
        sendStreamMessageWithJsonObject(jsonObject) {}
    }

    // 开始发送分数 3s/次
    private fun startSyncScore() {
        mStopSyncScore = false
        mSyncScoreFuture = scheduledThreadPool.scheduleAtFixedRate(mSyncScoreTask, 0, 3000, TimeUnit.MILLISECONDS)
    }

    // 停止发送分数
    private fun stopSyncScore() {
        mStopSyncScore = true
        singingScore = 0

        mSyncScoreFuture?.cancel(true)
        mSyncScoreFuture = null
        if (scheduledThreadPool is ScheduledThreadPoolExecutor) {
            scheduledThreadPool.remove(mSyncScoreTask)
        }
    }

    // ------------------ 云端合流信息同步 ------------------
    private fun sendSyncCloudConvergenceStatus() {
        val jsonObject = JSONObject()
        jsonObject.put("service", "audio_smart_mixer_status") // data message的目标消费者（服务）名
        jsonObject.put("version", "V1") //协议版本号（而非服务版本号）
        val payloadJson = JSONObject()
        payloadJson.put("Ts", getNtpTimeInMs()) // NTP 时间
        payloadJson.put("cname", giantChorusApiConfig.chorusChannelName) // 频道名
        payloadJson.put("status", getCloudConvergenceStatus()) //（-1： unknown，0：非K歌状态，1：K歌播放状态，2：K歌暂停状态）
        payloadJson.put("bgmUID", mpkConnection?.localUid.toString()) // mpk流的uid
        payloadJson.put("leadsingerUID", mainSingerUid.toString()) //（"-1" = unknown） //主唱Uid
        jsonObject.put("payload", payloadJson)
        ktvApiLog("sendSyncCloudConvergenceStatus: $jsonObject")
        sendStreamMessageWithJsonObject(jsonObject) {}
    }

    // -1： unknown，0：非K歌状态，1：K歌播放状态，2：K歌暂停状态）
    private fun getCloudConvergenceStatus(): Int {
        var status = -1
        when (this.mediaPlayerState) {
            MediaPlayerState.PLAYER_STATE_PLAYING -> status = 1
            MediaPlayerState.PLAYER_STATE_PAUSED -> status = 2
            else -> {}
        }
        return status
    }

    // 开始发送分数 200ms/次
    private fun startSyncCloudConvergenceStatus() {
        mStopSyncCloudConvergenceStatus = false
        mSyncCloudConvergenceStatusFuture = scheduledThreadPool.scheduleAtFixedRate(mSyncCloudConvergenceStatusTask, 0, 200,TimeUnit.MILLISECONDS)
    }

    // 停止发送分数
    private fun stopSyncCloudConvergenceStatus() {
        mStopSyncCloudConvergenceStatus = true

        mSyncCloudConvergenceStatusFuture?.cancel(true)
        mSyncCloudConvergenceStatusFuture = null
        if (scheduledThreadPool is ScheduledThreadPoolExecutor) {
            scheduledThreadPool.remove(mSyncCloudConvergenceStatusTask)
        }
    }

    // ------------------ 延迟选路 ------------------
    private var mStopProcessDelay = true

    private val mProcessDelayTask = Runnable {
        if (!mStopProcessDelay && singerRole != KTVSingRole.Audience) {
            val n = if (singerRole == KTVSingRole.LeadSinger) KTVApi.routeSelectionConfig.streamNum else KTVApi.routeSelectionConfig.streamNum -1
            val sortedEntries = subScribeSingerMap.entries.sortedBy { it.value }
            val other = sortedEntries.drop(3)
            val drop = mutableListOf<Int>()
            if (n > 3) {
                other.drop(n - 3).forEach { (uid, _) ->
                    drop.add(uid)
                    mRtcEngine.muteRemoteAudioStreamEx(uid, true, singChannelRtcConnection)
                    subScribeSingerMap.remove(uid)
                }
            }
            ktvApiLog("选路重新订阅, drop:$drop")

            val filteredList = singerList.filter { !subScribeSingerMap.containsKey(it) }
            val filteredList2 = filteredList.filter { !drop.contains(it) }
            val shuffledList = filteredList2.shuffled()
            if (subScribeSingerMap.size < 8) {
                val randomSingers = shuffledList.take(8 - subScribeSingerMap.size)
                ktvApiLog("选路重新订阅, newSingers:$randomSingers")
                for (singer in randomSingers) {
                    subScribeSingerMap[singer] = 0
                    mRtcEngine.muteRemoteAudioStreamEx(singer, false, singChannelRtcConnection)
                }
            }
            ktvApiLog("选路重新订阅, newSubScribeSingerMap:$subScribeSingerMap")
        }
    }

    private val mProcessSubscribeTask = Runnable {
        if (!mStopProcessDelay && singerRole != KTVSingRole.Audience) {
            val n = if (singerRole == KTVSingRole.LeadSinger) KTVApi.routeSelectionConfig.streamNum else KTVApi.routeSelectionConfig.streamNum -1
            val sortedEntries = subScribeSingerMap.entries.sortedBy { it.value }
            val mustToHave = sortedEntries.take(3)
            mustToHave.forEach { (uid, _) ->
                mRtcEngine.adjustUserPlaybackSignalVolumeEx(uid, 100, singChannelRtcConnection)
            }
            val other = sortedEntries.drop(3)
            if (n > 3) {
                other.take(n - 3).forEach { (uid, delay) ->
                    if (delay > 300) {
                        mRtcEngine.adjustUserPlaybackSignalVolumeEx(uid, 0, singChannelRtcConnection)
                    } else {
                        mRtcEngine.adjustUserPlaybackSignalVolumeEx(uid, 100, singChannelRtcConnection)
                    }
                }
                other.drop(n - 3).forEach { (uid, _) ->
                    mRtcEngine.adjustUserPlaybackSignalVolumeEx(uid, 0, singChannelRtcConnection)
                }
            }

            ktvApiLog("选路排序+调整播放音量, mustToHave:$mustToHave, other:$other")
        }
    }

    private var mProcessDelayFuture :ScheduledFuture<*>? = null
    private var mProcessSubscribeFuture :ScheduledFuture<*>? = null
    private fun startProcessDelay() {
        if (KTVApi.routeSelectionConfig.type == GiantChorusRouteSelectionType.TOP_N || KTVApi.routeSelectionConfig.type == GiantChorusRouteSelectionType.RANDOM) return
        mStopProcessDelay = false
        mProcessDelayFuture = scheduledThreadPool.scheduleAtFixedRate(mProcessDelayTask, 10000, 20000, TimeUnit.MILLISECONDS)
        mProcessSubscribeFuture = scheduledThreadPool.scheduleAtFixedRate(mProcessSubscribeTask,15000,20000, TimeUnit.MILLISECONDS)
    }

    private fun stopProcessDelay() {
        mStopProcessDelay = true

        mProcessDelayFuture?.cancel(true)
        mProcessSubscribeFuture?.cancel(true)
        mProcessDelayFuture = null
        if (scheduledThreadPool is ScheduledThreadPoolExecutor) {
            scheduledThreadPool.remove(mProcessDelayTask)
            scheduledThreadPool.remove(mProcessSubscribeTask)
        }
    }

    private fun loadLyric(songNo: Long, onLoadLyricCallback: (songNo: Long, lyricUrl: String?) -> Unit) {
        ktvApiLog("loadLyric: $songNo")
        val requestId = mMusicCenter.getLyric(songNo, 0)
        if (requestId.isEmpty()) {
            onLoadLyricCallback.invoke(songNo, null)
            return
        }
        lyricSongCodeMap[requestId] = songNo
        lyricCallbackMap[requestId] = onLoadLyricCallback
    }

    private fun preLoadMusic(songNo: Long, onLoadMusicCallback: (songCode: Long,
                                                                 percent: Int,
                                                                 status: Int,
                                                                 msg: String?,
                                                                 lyricUrl: String?) -> Unit) {
        ktvApiLog("loadMusic: $songNo")
        val ret = mMusicCenter.isPreloaded(songNo)
        if (ret == 0) {
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(songNo, 100, 0, null, null)
            return
        }

        val retPreload = mMusicCenter.preload(songNo, null)
        if (retPreload != 0) {
            ktvApiLogError("preLoadMusic failed: $retPreload")
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(songNo, 100, 1, null, null)
            return
        }
        loadMusicCallbackMap[songNo.toString()] = onLoadMusicCallback
    }

    private fun getNtpTimeInMs(): Long {
        val currentNtpTime = mRtcEngine.ntpWallTimeInMs
        return if (currentNtpTime != 0L) {
            currentNtpTime + 2208988800L * 1000
        } else {
            ktvApiLogError("getNtpTimeInMs DeviceDelay is zero!!!")
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
    private fun dealWithStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
        val jsonMsg: JSONObject
        val messageData = data ?: return
        try {
            val strMsg = String(messageData)
            jsonMsg = JSONObject(strMsg)
            if (!jsonMsg.has("cmd")) return
            if (jsonMsg.getString("cmd") == "setLrcTime") { //同步歌词
                val position = jsonMsg.getLong("time")
                val realPosition = jsonMsg.getLong("realTime")
                val duration = jsonMsg.getLong("duration")
                val remoteNtp = jsonMsg.getLong("ntp")
                val songId = jsonMsg.getString("songIdentifier")
                val mpkState = jsonMsg.getInt("playerState")

                if (isChorusCoSinger()) {
                    // 本地BGM校准逻辑
                    if (this.mediaPlayerState == MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                        // 合唱者开始播放音乐前调小远端人声
                        mRtcEngine.adjustPlaybackSignalVolume(KTVApi.remoteVolume)
                        // 收到leadSinger第一次播放位置消息时开启本地播放（先通过seek校准）
                        val delta = getNtpTimeInMs() - remoteNtp
                        val expectPosition = position + delta + audioPlayoutDelay
                        if (expectPosition in 1 until duration) {
                            mPlayer.seek(expectPosition)
                        }
                        mPlayer.play()
                    } else if (this.mediaPlayerState == MediaPlayerState.PLAYER_STATE_PLAYING) {
                        val localNtpTime = getNtpTimeInMs()
                        val localPosition =
                            localNtpTime - this.localPlayerSystemTime + this.localPlayerPosition // 当前副唱的播放时间
                        val expectPosition =
                            localNtpTime - remoteNtp + position + audioPlayoutDelay // 实际主唱的播放时间
                        val diff = expectPosition - localPosition
                        if (KTVApi.debugMode) {
                            ktvApiLog("play_status_seek: " + diff + " audioPlayoutDelay：" + audioPlayoutDelay +  "  localNtpTime: " + localNtpTime + "  expectPosition: " + expectPosition +
                                    "  localPosition: " + localPosition + "  ntp diff: " + (localNtpTime - remoteNtp))
                        }
                        if ((diff > 50 || diff < -50) && expectPosition < duration) { //设置阈值为50ms，避免频繁seek
                            mPlayer.seek(expectPosition)
                        }
                    } else {
                        mLastReceivedPlayPosTime = System.currentTimeMillis()
                        mReceivedPlayPosition = realPosition
                    }

                    if (MediaPlayerState.getStateByValue(mpkState) != this.mediaPlayerState) {
                        when (MediaPlayerState.getStateByValue(mpkState)) {
                            MediaPlayerState.PLAYER_STATE_PAUSED -> {
                                mPlayer.pause()
                            }
                            MediaPlayerState.PLAYER_STATE_PLAYING -> {
                                mPlayer.resume()
                            }
                            else -> {}
                        }
                    }
                } else {
                    // 独唱观众
                    if (jsonMsg.has("ver")) {
                        recvFromDataStream = false
                    } else {
                        recvFromDataStream = true
                        if (this.songIdentifier == songId) {
                            mLastReceivedPlayPosTime = System.currentTimeMillis()
                            mReceivedPlayPosition = realPosition
                        } else {
                            mLastReceivedPlayPosTime = null
                            mReceivedPlayPosition = 0
                        }
                    }
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
                ktvApiLog("onStreamMessage PlayerState: $state")
                if (isChorusCoSinger()) {
                    when (MediaPlayerState.getStateByValue(state)) {
                        MediaPlayerState.PLAYER_STATE_PAUSED -> {
                            mPlayer.pause()
                        }
                        MediaPlayerState.PLAYER_STATE_PLAYING -> {
                            mPlayer.resume()
                        }
                        else -> {}
                    }
                } else if (this.singerRole == KTVSingRole.Audience) {
                    this.mediaPlayerState = MediaPlayerState.getStateByValue(state)
                }
                ktvApiEventHandlerList.forEach { it.onMusicPlayerStateChanged(
                    MediaPlayerState.getStateByValue(state),
                    Constants.MediaPlayerReason.getErrorByValue(error),
                    false
                ) }
            } else if (jsonMsg.getString("cmd") == "setVoicePitch") {
                val pitch = jsonMsg.getDouble("pitch")
                if (this.singerRole == KTVSingRole.Audience) {
                    this.pitch = pitch
                }
            }
        } catch (exp: JSONException) {
            ktvApiLogError("onStreamMessage:$exp")
        }
    }

    private fun dealWithAudioMetadata(uid: Int, data: ByteArray?) {
        val messageData = data ?: return
        val lrcTime = LrcTimeOuterClass.LrcTime.parseFrom(messageData)
        if (lrcTime.type == LrcTimeOuterClass.MsgType.LRC_TIME) { //同步歌词
            val realPosition = lrcTime.ts
            val songId = lrcTime.songId
            val curTs = if (this.songIdentifier == songId) realPosition else 0
            runOnMainThread {
                lrcView?.onUpdatePitch(pitch.toFloat())
                // (fix ENT-489)Make lyrics delay for 200ms
                // Per suggestion from Bob, it has a intrinsic buffer/delay between sound and `onPositionChanged(Player)`,
                // such as AEC/Player/Device buffer.
                // We choose the estimated 200ms.
                lrcView?.onUpdateProgress(if (curTs > 200) (curTs - 200) else curTs) // The delay here will impact both singer and audience side
            }
        }
    }

    // ------------------------ AgoraMusicContentCenterEventDelegate  ------------------------
    override fun onPreLoadEvent(
        requestId: String?,
        songCode: Long,
        percent: Int,
        lyricUrl: String?,
        status: Int,
        errorCode: Int
    ) {
        val callback = loadMusicCallbackMap[songCode.toString()] ?: return
        if (status == 0 || status == 1) {
            loadMusicCallbackMap.remove(songCode.toString())
        }
        if (errorCode == 2) {
            // Token过期
            ktvApiEventHandlerList.forEach { it.onTokenPrivilegeWillExpire() }
        }
        callback.invoke(songCode, percent, status, RtcEngine.getErrorDescription(errorCode), lyricUrl)
    }

    override fun onMusicCollectionResult(
        requestId: String?,
        page: Int,
        pageSize: Int,
        total: Int,
        list: Array<out Music>?,
        errorCode: Int
    ) {
        ktvApiLog("onMusicCollectionResult, requestId: $requestId, list: $list, errorCode: $errorCode")
        val id = requestId ?: return
        val callback = musicCollectionCallbackMap[id] ?: return
        musicCollectionCallbackMap.remove(id)
        if (errorCode == 2) {
            // Token过期
            ktvApiEventHandlerList.forEach { it.onTokenPrivilegeWillExpire() }
        }
        callback.invoke(requestId, errorCode, page, pageSize, total, list)
    }

    override fun onMusicChartsResult(requestId: String?, list: Array<out MusicChartInfo>?, errorCode: Int) {
        val id = requestId ?: return
        val callback = musicChartsCallbackMap[id] ?: return
        musicChartsCallbackMap.remove(id)
        if (errorCode == 2) {
            // Token过期
            ktvApiEventHandlerList.forEach { it.onTokenPrivilegeWillExpire() }
        }
        callback.invoke(requestId, errorCode, list)
    }

    override fun onLyricResult(
        requestId: String?,
        songCode: Long,
        lyricUrl: String?,
        errorCode: Int
    ) {
        val callback = lyricCallbackMap[requestId] ?: return
        val songCode = lyricSongCodeMap[requestId] ?: return
        lyricCallbackMap.remove(lyricUrl)
        if (errorCode == 2) {
            // Token过期
            ktvApiEventHandlerList.forEach { it.onTokenPrivilegeWillExpire() }
        }
        if (lyricUrl == null || lyricUrl.isEmpty()) {
            callback(songCode, null)
            return
        }
        callback(songCode, lyricUrl)
    }


    override fun onSongSimpleInfoResult(
        requestId: String?,
        songCode: Long,
        simpleInfo: String,
        errorCode: Int
    ) {}

    // ------------------------ AgoraRtcMediaPlayerDelegate ------------------------
    private var duration: Long = 0
    override fun onPlayerStateChanged(
        state: Constants.MediaPlayerState?,
        reason: Constants.MediaPlayerReason?
    ) {
        val mediaPlayerState = state ?: return
        val mediaPlayerError = reason ?: return
        ktvApiLog("onPlayerStateChanged called, state: $mediaPlayerState, error: $mediaPlayerError")
        this.mediaPlayerState = mediaPlayerState
        when (mediaPlayerState) {
            MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                duration = mPlayer.duration
                this.localPlayerPosition = 0
                // 伴奏
                mPlayer.selectMultiAudioTrack(1, 1)
                if (this.singerRole == KTVSingRole.SoloSinger ||
                    this.singerRole == KTVSingRole.LeadSinger
                ) {
                    mPlayer.play()
                }
                startProcessDelay()
            }
            MediaPlayerState.PLAYER_STATE_PLAYING -> {
                mRtcEngine.adjustPlaybackSignalVolume(KTVApi.remoteVolume)
            }
            MediaPlayerState.PLAYER_STATE_PAUSED -> {
                mRtcEngine.adjustPlaybackSignalVolume(100)
            }
            MediaPlayerState.PLAYER_STATE_STOPPED -> {
                mRtcEngine.adjustPlaybackSignalVolume(100)
                duration = 0
                stopProcessDelay()
            }
            else -> {}
        }

        if (this.singerRole == KTVSingRole.SoloSinger || this.singerRole == KTVSingRole.LeadSinger) {
            syncPlayState(mediaPlayerState, mediaPlayerError)
        }
        ktvApiEventHandlerList.forEach { it.onMusicPlayerStateChanged(mediaPlayerState, mediaPlayerError, true) }
    }

    // 同步播放进度
    override fun onPositionChanged(position_ms: Long, timestamp_ms: Long) {
        localPlayerPosition = position_ms
        localPlayerSystemTime = timestamp_ms

        if ((this.singerRole == KTVSingRole.SoloSinger || this.singerRole == KTVSingRole.LeadSinger) && position_ms > audioPlayoutDelay) {
            val msg: MutableMap<String?, Any?> = HashMap()
            msg["cmd"] = "setLrcTime"
            msg["ntp"] = timestamp_ms
            msg["duration"] = duration
            msg["time"] =
                position_ms - audioPlayoutDelay // "position-audioDeviceDelay" 是计算出当前播放的真实进度
            msg["realTime"] = position_ms
            msg["playerState"] = MediaPlayerState.getValue(this.mediaPlayerState)
            msg["pitch"] = pitch
            msg["songIdentifier"] = songIdentifier
            msg["forward"] = true
            msg["ver"] = lyricSyncVersion
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

    override fun onAgoraCDNTokenWillExpire() {}

    override fun onPlayerSrcInfoChanged(from: SrcInfo?, to: SrcInfo?) {}

    override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo?) {}

    override fun onPlayerCacheStats(stats: CacheStatistics?) {}

    override fun onPlayerPlaybackStats(stats: PlayerPlaybackStats?) {}

    override fun onAudioVolumeIndication(volume: Int) {}
}