package io.agora.scene.cantata.ktvapi

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.musiccontentcenter.*
import io.agora.rtc2.*
import io.agora.rtc2.Constants.*
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.*

/**
 * 加入合唱错误原因
 */
enum class KTVJoinChorusFailReason(val value: Int) {
    JOIN_CHANNEL_FAIL(0),  // 加入channel2失败
    MUSIC_OPEN_FAIL(1)     // 歌曲open失败
}

interface OnJoinChorusStateListener {
    fun onJoinChorusSuccess()
    fun onJoinChorusFail(reason: KTVJoinChorusFailReason)
}

class KTVApiImpl : KTVApi, IMusicContentCenterEventHandler, IMediaPlayerObserver, IRtcEngineEventHandler() {
    private val TAG: String = "KTV_API_LOG"
    var debugMode = false

    // 外部可修改
    var useCustomAudioSource:Boolean = false

    // 音频最佳实践
    var remoteVolume: Int = 30 // 远端音频
    var mpkPlayoutVolume: Int = 50
    var mpkPublishVolume: Int = 50

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private lateinit var mRtcEngine: RtcEngineEx
    private lateinit var mMusicCenter: IAgoraMusicContentCenter
    private lateinit var mPlayer: IMediaPlayer

    private lateinit var ktvApiConfig: KTVApiConfig
    private lateinit var giantChorusConfig: GiantChorusConfiguration
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
    private var mStopDisplayLrc = true
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

    companion object{
        private val scheduledThreadPool: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
    }

    private var professionalModeOpen = false
    private var audioRouting = 0
    private var isPublishAudio = false // 通过是否发音频流判断
    // 演唱分数
    private var singingScore = 0

    // 数据上报
    private fun reportCallScenarioApi(event: String, params: JSONObject) {
        mRtcEngine.sendCustomReportMessage(
                "scenarioAPI",
                "1_android_3.8.0",
                event,
                params.toString(),
                0)
    }

    override fun initialize(config: KTVApiConfig, giantChorusConfig: GiantChorusConfiguration?) {
        this.mRtcEngine = config.engine as RtcEngineEx
        reportCallScenarioApi("initialize", JSONObject().put("config", config))

        this.ktvApiConfig = config
        this.singChannelRtcConnection = RtcConnection(ktvApiConfig.chorusChannelName, ktvApiConfig.localUid)

        if (ktvApiConfig.type == KTVType.Cantata) {
            this.giantChorusConfig = giantChorusConfig ?: return
        }

        // ------------------ 初始化内容中心 ------------------
        if (config.musicType == KTVMusicType.SONG_CODE) {
            val contentCenterConfiguration = MusicContentCenterConfiguration()
            contentCenterConfiguration.appId = config.appId
            contentCenterConfiguration.mccUid = ktvApiConfig.localUid.toLong()
            contentCenterConfiguration.token = config.rtmToken
            contentCenterConfiguration.maxCacheSize = config.maxCacheSize
            if (debugMode) {
                contentCenterConfiguration.mccDomain = "api-test.agora.io"
            }
            mMusicCenter = IAgoraMusicContentCenter.create(mRtcEngine)
            mMusicCenter.initialize(contentCenterConfiguration)
            mMusicCenter.registerEventHandler(this)

            // ------------------ 初始化音乐播放器实例 ------------------
            mPlayer = mMusicCenter.createMusicPlayer()
        } else {
            mPlayer = mRtcEngine.createMediaPlayer()
        }
        mPlayer.adjustPublishSignalVolume(mpkPublishVolume)
        mPlayer.adjustPlayoutVolume(mpkPlayoutVolume)

        // 注册回调
        mPlayer.registerPlayerObserver(this)
        setKTVParameters()
        startDisplayLrc()
        startSyncPitch()
        startSyncScore()
        startSyncCloudConvergenceStatus()
        isRelease = false
    }

    override fun renewInnerDataStreamId() {
        reportCallScenarioApi("renewInnerDataStreamId", JSONObject())

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
    }

    override fun addEventHandler(ktvApiEventHandler: IKTVApiEventHandler) {
        reportCallScenarioApi("addEventHandler", JSONObject())
        ktvApiEventHandlerList.add(ktvApiEventHandler)
    }

    override fun removeEventHandler(ktvApiEventHandler: IKTVApiEventHandler) {
        reportCallScenarioApi("removeEventHandler", JSONObject())
        ktvApiEventHandlerList.remove(ktvApiEventHandler)
    }

    override fun release() {
        reportCallScenarioApi("release", JSONObject())
        if (isRelease) return
        isRelease = true
        singerRole = KTVSingRole.Audience

        stopSyncCloudConvergenceStatus()
        stopSyncScore()
        stopSyncPitch()
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

        if (ktvApiConfig.musicType == KTVMusicType.SONG_CODE) {
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
        reportCallScenarioApi("enableProfessionalStreamerMode", JSONObject())
        this.professionalModeOpen = enable
        processAudioProfessionalProfile()
    }

    private fun processAudioProfessionalProfile() {
        Log.d(TAG, "processAudioProfessionalProfile: audioRouting: $audioRouting, professionalModeOpen: $professionalModeOpen， isPublishAudio：$isPublishAudio")
        if (!isPublishAudio) return // 必须为麦上者
        if (professionalModeOpen) {
            // 专业
            if (audioRouting == 0 || audioRouting == 2 || audioRouting == 5 || audioRouting == 6) {
                // 耳机 关闭3A 关闭md
                mRtcEngine.setParameters("{\"che.audio.aec.enable\": false}")
                mRtcEngine.setParameters("{\"che.audio.agc.enable\": false}")
                mRtcEngine.setParameters("{\"che.audio.ans.enable\": false}")
                mRtcEngine.setParameters("{\"che.audio.md.enable\": false}")
                mRtcEngine.setAudioProfile(5) // AgoraAudioProfileMusicHighQualityStereo
            } else {
                // 非耳机 开启3A 关闭md
                mRtcEngine.setParameters("{\"che.audio.aec.enable\": true}")
                mRtcEngine.setParameters("{\"che.audio.agc.enable\": true}")
                mRtcEngine.setParameters("{\"che.audio.ans.enable\": true}")
                mRtcEngine.setParameters("{\"che.audio.md.enable\": false}")
                mRtcEngine.setAudioProfile(5) // AgoraAudioProfileMusicHighQualityStereo
            }
        } else {
            // 非专业 开启3A 关闭md
            mRtcEngine.setParameters("{\"che.audio.aec.enable\": true}")
            mRtcEngine.setParameters("{\"che.audio.agc.enable\": true}")
            mRtcEngine.setParameters("{\"che.audio.ans.enable\": true}")
            mRtcEngine.setParameters("{\"che.audio.md.enable\": false}")
            mRtcEngine.setAudioProfile(3) // AgoraAudioProfileMusicStandardStereo
        }
    }

    override fun renewToken(rtmToken: String, chorusChannelRtcToken: String) {
        reportCallScenarioApi("renewToken", JSONObject().put("rtmToken", rtmToken).put("chorusChannelRtcToken", chorusChannelRtcToken))
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
        reportCallScenarioApi("switchSingerRole", JSONObject().put("newRole", newRole))

        Log.d(TAG, "switchSingerRole oldRole: $singerRole, newRole: $newRole")
        val oldRole = singerRole
        if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.SoloSinger) {
            // 1、Audience -》SoloSinger
            this.singerRole = newRole
            becomeSoloSinger()
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()
        } else if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.LeadSinger) {
            // 2、Audience -》LeadSinger
            becomeSoloSinger()
            joinChorus(newRole, ktvApiConfig.chorusChannelToken, object : OnJoinChorusStateListener {
                override fun onJoinChorusSuccess() {
                    Log.d(TAG, "onJoinChorusSuccess")
                    singerRole = newRole
                    ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
                    switchRoleStateListener?.onSwitchRoleSuccess()
                }

                override fun onJoinChorusFail(reason: KTVJoinChorusFailReason) {
                    Log.d(TAG, "onJoinChorusFail reason：$reason")
                    leaveChorus(newRole)
                    switchRoleStateListener?.onSwitchRoleFail(SwitchRoleFailReason.JOIN_CHANNEL_FAIL)
                }
            })
        } else if (this.singerRole == KTVSingRole.SoloSinger && newRole == KTVSingRole.Audience) {
            // 3、SoloSinger -》Audience

            stopSing()
            this.singerRole = newRole
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()

        } else if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.CoSinger) {
            // 4、Audience -》CoSinger
            joinChorus(newRole, ktvApiConfig.chorusChannelToken, object : OnJoinChorusStateListener {
                override fun onJoinChorusSuccess() {
                    Log.d(TAG, "onJoinChorusSuccess")
                    singerRole = newRole
                    switchRoleStateListener?.onSwitchRoleSuccess()
                    ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
                }

                override fun onJoinChorusFail(reason: KTVJoinChorusFailReason) {
                    Log.d(TAG, "onJoinChorusFail reason：$reason")
                    leaveChorus(newRole)
                    switchRoleStateListener?.onSwitchRoleFail(SwitchRoleFailReason.JOIN_CHANNEL_FAIL)
                }
            })

        } else if (this.singerRole == KTVSingRole.CoSinger && newRole == KTVSingRole.Audience) {
            // 5、CoSinger -》Audience
            leaveChorus(singerRole)

            this.singerRole = newRole
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()

        } else if (this.singerRole == KTVSingRole.SoloSinger && newRole == KTVSingRole.LeadSinger) {
            // 6、SoloSinger -》LeadSinger

            joinChorus(newRole, ktvApiConfig.chorusChannelToken, object : OnJoinChorusStateListener {
                override fun onJoinChorusSuccess() {
                    Log.d(TAG, "onJoinChorusSuccess")
                    singerRole = newRole
                    switchRoleStateListener?.onSwitchRoleSuccess()
                    ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
                }

                override fun onJoinChorusFail(reason: KTVJoinChorusFailReason) {
                    Log.d(TAG, "onJoinChorusFail reason：$reason")
                    leaveChorus(newRole)
                    switchRoleStateListener?.onSwitchRoleFail(SwitchRoleFailReason.JOIN_CHANNEL_FAIL)
                }
            })
        } else if (this.singerRole == KTVSingRole.LeadSinger && newRole == KTVSingRole.SoloSinger) {
            // 7、LeadSinger -》SoloSinger
            leaveChorus(singerRole)

            this.singerRole = newRole
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()
        } else if (this.singerRole == KTVSingRole.LeadSinger && newRole == KTVSingRole.Audience) {
            // 8、LeadSinger -》Audience
            leaveChorus(singerRole)
            stopSing()

            this.singerRole = newRole
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()
        } else {
            switchRoleStateListener?.onSwitchRoleFail(SwitchRoleFailReason.NO_PERMISSION)
            Log.e(TAG, "Error！You can not switch role from $singerRole to $newRole!")
        }
    }

    override fun switchSingerRole2(
            newRole: KTVSingRole,
            switchRoleStateListener: ISwitchRoleStateListener?
    ) {
        reportCallScenarioApi("switchSingerRole", JSONObject().put("newRole", newRole))
        if (this.ktvApiConfig.type != KTVType.Cantata) {
            Log.e(TAG, "You can't call this api while not in cantata mode")
            return
        }
        Log.d(TAG, "switchSingerRole oldRole: $singerRole, newRole: $newRole")
        val oldRole = singerRole
        if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.LeadSinger) {
            // 1、Audience -》LeadSinger
            // 离开观众频道
            mRtcEngine.leaveChannelEx(RtcConnection(ktvApiConfig.channelName, ktvApiConfig.localUid))
            joinChorus(newRole)
            singerRole = newRole
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
            switchRoleStateListener?.onSwitchRoleSuccess()
        } else if (this.singerRole == KTVSingRole.Audience && newRole == KTVSingRole.CoSinger) {
            // 2、Audience -》CoSinger
            // 离开观众频道
            mRtcEngine.leaveChannelEx(RtcConnection(ktvApiConfig.channelName, ktvApiConfig.localUid))
            joinChorus(newRole)
            singerRole = newRole
            switchRoleStateListener?.onSwitchRoleSuccess()
            ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
        } else if (this.singerRole == KTVSingRole.CoSinger && newRole == KTVSingRole.Audience) {
            // 3、CoSinger -》Audience
            leaveChorus2(singerRole)
            // 加入观众频道
            mRtcEngine.joinChannelEx(giantChorusConfig.audienceChannelToken, RtcConnection(ktvApiConfig.channelName, ktvApiConfig.localUid), ChannelMediaOptions(), object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    singerRole = newRole
                    ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
                    switchRoleStateListener?.onSwitchRoleSuccess()
                }

                override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                    super.onStreamMessage(uid, streamId, data)
                    dealWithStreamMessage(uid, streamId, data)
                }
            })
        } else if (this.singerRole == KTVSingRole.LeadSinger && newRole == KTVSingRole.Audience) {
            // 4、LeadSinger -》Audience
            stopSing()
            leaveChorus2(singerRole)

            // 加入观众频道
            mRtcEngine.joinChannelEx(giantChorusConfig.audienceChannelToken, RtcConnection(ktvApiConfig.channelName, ktvApiConfig.localUid), ChannelMediaOptions(), object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    singerRole = newRole
                    ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
                    switchRoleStateListener?.onSwitchRoleSuccess()
                }

                override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                    super.onStreamMessage(uid, streamId, data)
                    dealWithStreamMessage(uid, streamId, data)
                }
            })
        } else {
            switchRoleStateListener?.onSwitchRoleFail(SwitchRoleFailReason.NO_PERMISSION)
            Log.e(TAG, "Error！You can not switch role from $singerRole to $newRole!")
        }
    }

    override fun fetchMusicCharts(onMusicChartResultListener: (requestId: String?, status: Int, list: Array<out MusicChartInfo>?) -> Unit) {
        reportCallScenarioApi("fetchMusicCharts", JSONObject())
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
        reportCallScenarioApi("searchMusicByMusicChartId", JSONObject())
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
        reportCallScenarioApi("searchMusicByKeyword", JSONObject())
        val requestId = mMusicCenter.searchMusic(keyword, page, pageSize, jsonOption)
        musicCollectionCallbackMap[requestId] = onMusicCollectionResultListener
    }

    override fun loadMusic(
            songCode: Long,
            config: KTVLoadMusicConfiguration,
            musicLoadStateListener: IMusicLoadStateListener
    ) {
        reportCallScenarioApi("loadMusic", JSONObject().put("songCode", songCode).put("config", config))
        Log.d(TAG, "loadMusic called: songCode $songCode")
        if (this.ktvApiConfig.type == KTVType.SingBattle) {
            mMusicCenter.getSongSimpleInfo(songCode);
        }
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
                    Log.e(TAG, "loadMusic failed: CANCELED")
                    musicLoadStateListener.onMusicLoadFail(song, KTVLoadSongFailReason.CANCELED)
                    return@loadLyric
                }

                if (lyricUrl == null) {
                    // 加载歌词失败
                    Log.e(TAG, "loadMusic failed: NO_LYRIC_URL")
                    musicLoadStateListener.onMusicLoadFail(song, KTVLoadSongFailReason.NO_LYRIC_URL)
                } else {
                    // 加载歌词成功
                    Log.d(TAG, "loadMusic success")
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
                    Log.e(TAG, "loadMusic failed: CANCELED")
                    musicLoadStateListener.onMusicLoadFail(song, KTVLoadSongFailReason.CANCELED)
                    return@preLoadMusic
                }
                if (config.mode == KTVLoadMusicMode.LOAD_MUSIC_AND_LRC) {
                    // 需要加载歌词
                    loadLyric(song) { _, lyricUrl ->
                        if (this.songCode != song) {
                            // 当前歌曲已发生变化，以最新load歌曲为准
                            Log.e(TAG, "loadMusic failed: CANCELED")
                            musicLoadStateListener.onMusicLoadFail(song, KTVLoadSongFailReason.CANCELED)
                            return@loadLyric
                        }

                        if (lyricUrl == null) {
                            // 加载歌词失败
                            Log.e(TAG, "loadMusic failed: NO_LYRIC_URL")
                            musicLoadStateListener.onMusicLoadFail(song, KTVLoadSongFailReason.NO_LYRIC_URL)
                        } else {
                            // 加载歌词成功
                            Log.d(TAG, "loadMusic success")
                            lrcView?.onDownloadLrcData(lyricUrl)
                            musicLoadStateListener.onMusicLoadProgress(song, 100, MusicLoadStatus.COMPLETED, msg, lrcUrl)
                            musicLoadStateListener.onMusicLoadSuccess(song, lyricUrl)
                        }

                        if (config.autoPlay) {
                            // 主唱自动播放歌曲
                            if (this.singerRole != KTVSingRole.LeadSinger) {
                                switchSingerRole(KTVSingRole.SoloSinger, null)
                            }
                            startSing(song, 0)
                        }
                    }
                } else if (config.mode == KTVLoadMusicMode.LOAD_MUSIC_ONLY) {
                    // 不需要加载歌词
                    Log.d(TAG, "loadMusic success")
                    if (config.autoPlay) {
                        // 主唱自动播放歌曲
                        if (this.singerRole != KTVSingRole.LeadSinger) {
                            switchSingerRole(KTVSingRole.SoloSinger, null)
                        }
                        startSing(song, 0)
                    }
                    musicLoadStateListener.onMusicLoadProgress(song, 100, MusicLoadStatus.COMPLETED, msg, lrcUrl)
                    musicLoadStateListener.onMusicLoadSuccess(song, "")
                }
            } else if (status == 2) {
                // 预加载歌曲加载中
                musicLoadStateListener.onMusicLoadProgress(song, percent, MusicLoadStatus.values().firstOrNull { it.value == status } ?: MusicLoadStatus.FAILED, msg, lrcUrl)
            } else {
                // 预加载歌曲失败
                Log.e(TAG, "loadMusic failed: MUSIC_PRELOAD_FAIL")
                musicLoadStateListener.onMusicLoadFail(song, KTVLoadSongFailReason.MUSIC_PRELOAD_FAIL)
            }
        }
    }

    override fun loadMusic(
            url: String,
            config: KTVLoadMusicConfiguration
    ) {
        reportCallScenarioApi("loadMusic", JSONObject().put("url", url).put("config", config))
        Log.d(TAG, "loadMusic called: songCode $songCode")
        this.songIdentifier = config.songIdentifier
        this.songUrl = url
        this.mainSingerUid = config.mainSingerUid

        if (config.autoPlay) {
            // 主唱自动播放歌曲
            if (this.singerRole != KTVSingRole.LeadSinger) {
                switchSingerRole(KTVSingRole.SoloSinger, null)
            }
            startSing(url, 0)
        }
    }

    override fun load2Music(url1: String, url2: String, config: KTVLoadMusicConfiguration) {
        Log.d(TAG, "load2Music called: songUrl url1:$url1,url2:$url2")
        this.songIdentifier = config.songIdentifier
        this.songUrl = url1
        this.songUrl2 = url2
        this.mainSingerUid = config.mainSingerUid

        if (config.autoPlay) {
            // 主唱自动播放歌曲
            if (this.singerRole != KTVSingRole.LeadSinger) {
                switchSingerRole(KTVSingRole.SoloSinger, null)
            }
            startSing(url1, 0)
        }
    }

    override fun switchPlaySrc(url: String, syncPts: Boolean) {
        Log.d(TAG, "switchPlaySrc called: $url")
        if (this.songUrl != url && this.songUrl2 != url) {
            Log.e(TAG, "switchPlaySrc failed: canceled")
            return
        }
        val curPlayPosition = if (syncPts) mPlayer.playPosition else 0
        mPlayer.stop()
        startSing(url, curPlayPosition)
    }

    override fun startSing(songCode: Long, startPos: Long) {
        reportCallScenarioApi("startSing", JSONObject().put("songCode", songCode).put("startPos", startPos))
        Log.d(TAG, "playSong called: $singerRole")
        if (this.songCode != songCode) {
            Log.e(TAG, "startSing failed: canceled")
            return
        }
        mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)

        // 导唱
        mPlayer.setPlayerOption("enable_multi_audio_track", 1)
        (mPlayer as IAgoraMusicPlayer).open(songCode, startPos)
    }

    override fun startSing(url: String, startPos: Long) {
        reportCallScenarioApi("startSing", JSONObject().put("url", url).put("startPos", startPos))
        Log.d(TAG, "playSong called: $singerRole")
        if (this.songUrl != url && this.songUrl2 != url) {
            Log.e(TAG, "startSing failed: canceled")
            return
        }
        mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)

        // 导唱
        mPlayer.setPlayerOption("enable_multi_audio_track", 1)
        mPlayer.open(url, startPos)
    }

    override fun resumeSing() {
        reportCallScenarioApi("resumeSing", JSONObject())
        Log.d(TAG, "resumePlay called")
        mPlayer.resume()
    }

    override fun pauseSing() {
        reportCallScenarioApi("pauseSing", JSONObject())
        Log.d(TAG, "pausePlay called")
        mPlayer.pause()
    }

    override fun seekSing(time: Long) {
        reportCallScenarioApi("seekSing", JSONObject().put("time", time))
        Log.d(TAG, "seek called")
        mPlayer.seek(time)
        syncPlayProgress(time)
    }

    override fun setLrcView(view: ILrcView) {
        //reportCallScenarioApi("setLrcView", JSONObject())
        Log.d(TAG, "setLrcView called")
        this.lrcView = view
    }

    override fun setMicStatus(isOnMicOpen: Boolean) {
        //reportCallScenarioApi("setMicStatus", JSONObject().put("isOnMicOpen", isOnMicOpen))
        this.isOnMicOpen = isOnMicOpen
    }

    override fun setAudioPlayoutDelay(audioPlayoutDelay: Int) {
        reportCallScenarioApi("setAudioPlayoutDelay", JSONObject().put("audioPlayoutDelay", audioPlayoutDelay))
        this.audioPlayoutDelay = audioPlayoutDelay
    }

    override fun setSingingScore(score: Int) {
        this.singingScore = score
    }

    override fun setAudienceStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
        dealWithStreamMessage(uid, streamId, data)
    }

    override fun getMediaPlayer(): IMediaPlayer {
        return mPlayer
    }

    override fun getMusicContentCenter(): IAgoraMusicContentCenter {
        return mMusicCenter
    }

    // ------------------ inner KTVApi --------------------
    private fun becomeSoloSinger() {
        Log.d(TAG, "becomeSoloSinger called")
        // 主唱进入合唱模式
        mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
        mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
        mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 80000}")

        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.autoSubscribeAudio = true
        channelMediaOption.publishMediaPlayerId = mPlayer.mediaPlayerId
        channelMediaOption.publishMediaPlayerAudioTrack = true
        mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption, singChannelRtcConnection)
    }

    private fun joinChorus(newRole: KTVSingRole, token: String, onJoinChorusStateListener: OnJoinChorusStateListener) {
        Log.d(TAG, "joinChorus: $newRole")
        when (newRole) {
            KTVSingRole.LeadSinger -> {
                joinChorus2ndChannel(newRole, token, mainSingerUid) { joinStatus ->
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
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption, singChannelRtcConnection)

                // 预加载歌曲成功
                // 导唱
                mPlayer.setPlayerOption("enable_multi_audio_track", 1)
                if (ktvApiConfig.musicType == KTVMusicType.SONG_CODE) {
                    (mPlayer as IAgoraMusicPlayer).open(songCode, 0) // TODO open failed
                } else {
                    mPlayer.open(songUrl, 0) // TODO open failed
                }

                // 预加载成功后加入第二频道：预加载时间>>joinChannel时间
                joinChorus2ndChannel(newRole, token, mainSingerUid) { joinStatus ->
                    if (joinStatus == 0) {
                        // 加入第二频道成功
                        onJoinChorusStateListener.onJoinChorusSuccess()
                    } else {
                        // 加入第二频道失败
                        onJoinChorusStateListener.onJoinChorusFail(KTVJoinChorusFailReason.JOIN_CHANNEL_FAIL)
                    }
                }
            }
            else -> {
                Log.e(TAG, "JoinChorus with Wrong role: $singerRole")
            }
        }
    }

    private fun leaveChorus(role: KTVSingRole) {
        Log.d(TAG, "leaveChorus: $singerRole")
        when (role) {
            KTVSingRole.LeadSinger -> {
                mainSingerHasJoinChannelEx = false
                leaveChorus2ndChannel(role)
            }
            KTVSingRole.CoSinger -> {
                mPlayer.stop()
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.autoSubscribeAudio = true
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption, singChannelRtcConnection)
                leaveChorus2ndChannel(role)

                // 更新音频配置
                mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
                mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
                mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
                mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
            }
            else -> {
                Log.e(TAG, "JoinChorus with wrong role: $singerRole")
            }
        }
    }

    private fun stopSing() {
        Log.d(TAG, "stopSong called")

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

    private fun joinChorus(newRole: KTVSingRole) {
        Log.d(TAG, "joinChorus: $newRole")
        val singChannelMediaOptions = ChannelMediaOptions()
        singChannelMediaOptions.autoSubscribeAudio = true
        singChannelMediaOptions.publishMicrophoneTrack = true
        singChannelMediaOptions.clientRoleType = CLIENT_ROLE_BROADCASTER
        if (newRole == KTVSingRole.LeadSinger) {
            // 主唱不参加TopN
            singChannelMediaOptions.isAudioFilterable = false
            mRtcEngine.setParameters("{\"che.audio.filter_streams\":${giantChorusConfig.topN}}")
        } else {
            mRtcEngine.setParameters("{\"che.audio.filter_streams\":${giantChorusConfig.topN - 1}}")
        }
        // 加入演唱频道
        mRtcEngine.joinChannelEx(ktvApiConfig.chorusChannelToken, singChannelRtcConnection, singChannelMediaOptions, object :
                IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)
                Log.d(TAG, "singChannel onJoinChannelSuccess: $newRole")
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
                if (useCustomAudioSource) return
                val audioState = stats ?: return
                audioPlayoutDelay = audioState.audioPlayoutDelay
            }

            // 用于检测耳机状态
            override fun onAudioRouteChanged(routing: Int) { // 0\2\5 earPhone
                audioRouting = routing
                processAudioProfessionalProfile()
                ktvApiEventHandlerList.forEach {
                    it.onAudioRouteChanged(routing)
                }
            }

            // 用于检测收发流状态
            override fun onAudioPublishStateChanged(
                channel: String?,
                oldState: Int,
                newState: Int,
                elapseSinceLastState: Int
            ) {
                Log.d(TAG, "onAudioPublishStateChanged: oldState: $oldState, newState: $newState")
                if (newState == 3) {
                    isPublishAudio = true
                    processAudioProfessionalProfile()
                } else if (newState == 1) {
                    isPublishAudio = false
                }
            }
        })
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
                rtcConnection.channelId = ktvApiConfig.chorusChannelName
                rtcConnection.localUid = giantChorusConfig.musicStreamUid
                mpkConnection = rtcConnection

                mRtcEngine.joinChannelEx(
                        giantChorusConfig.musicChannelToken,
                        mpkConnection,
                        options,
                        object : IRtcEngineEventHandler() {
                            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                                Log.d(TAG, "onMPKJoinChannelSuccess, channel: $channel, uid: $uid")
                            }

                            override fun onLeaveChannel(stats: RtcStats) {
                                Log.d(TAG, "onMPKLeaveChannel")
                            }
                        })
            }
            KTVSingRole.CoSinger -> {
                // 防止主唱和合唱听见mpk流的声音
                mRtcEngine.muteRemoteAudioStreamEx(
                        giantChorusConfig.musicStreamUid,
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
                if (ktvApiConfig.musicType == KTVMusicType.SONG_CODE) {
                    (mPlayer as IAgoraMusicPlayer).open(songCode, 0) // TODO open failed
                } else {
                    mPlayer.open(songUrl, 0) // TODO open failed
                }
            }
            else -> {
                Log.e(TAG, "JoinChorus with Wrong role: $singerRole")
            }
        }

        mRtcEngine.muteRemoteAudioStreamEx(giantChorusConfig.musicStreamUid, true, singChannelRtcConnection)
        // 加入演唱频道后，创建data stream
        renewInnerDataStreamId()
    }

    private fun leaveChorus2(role: KTVSingRole) {
        Log.d(TAG, "leaveChorus: $singerRole")
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
                Log.e(TAG, "JoinChorus with wrong role: $singerRole")
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
            Log.e(TAG, "sendStreamMessageWithJsonObject failed: $ret, innerDataStreamId:$innerDataStreamId")
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

    // 合唱
    private fun joinChorus2ndChannel(
            newRole: KTVSingRole,
            token: String,
            mainSingerUid: Int,
            onJoinChorus2ndChannelCallback: (status: Int?) -> Unit
    ) {
        Log.d(TAG, "joinChorus2ndChannel: token:$token")
        if (newRole == KTVSingRole.SoloSinger || newRole == KTVSingRole.Audience) {
            Log.e(TAG, "joinChorus2ndChannel with wrong role: $newRole")
            return
        }

        if (newRole == KTVSingRole.CoSinger) {
            mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
            mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
            mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
            mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)
        }

        // main singer do not subscribe 2nd channel
        // co singer auto sub
        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.autoSubscribeAudio =
                newRole != KTVSingRole.LeadSinger
        channelMediaOption.autoSubscribeVideo = false
        channelMediaOption.publishMicrophoneTrack = newRole == KTVSingRole.LeadSinger
        channelMediaOption.enableAudioRecordingOrPlayout =
                newRole != KTVSingRole.LeadSinger
        channelMediaOption.clientRoleType = CLIENT_ROLE_BROADCASTER

        val rtcConnection = RtcConnection()
        rtcConnection.channelId = ktvApiConfig.chorusChannelName
        rtcConnection.localUid = ktvApiConfig.localUid
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
                        if (newRole == KTVSingRole.LeadSinger) {
                            mainSingerHasJoinChannelEx = true
                        }
                        onJoinChorus2ndChannelCallback(0)
                        mRtcEngine.enableAudioVolumeIndicationEx(50, 10, true, rtcConnection)
                    }

                    override fun onLeaveChannel(stats: RtcStats?) {
                        Log.d(TAG, "onLeaveChannel2")
                        if (isRelease) return
                        super.onLeaveChannel(stats)
                        if (newRole == KTVSingRole.LeadSinger) {
                            mainSingerHasJoinChannelEx = false
                        }
                    }

                    override fun onError(err: Int) {
                        super.onError(err)
                        if (isRelease) return
                        if (err == ERR_JOIN_CHANNEL_REJECTED) {
                            Log.e(TAG, "joinChorus2ndChannel failed: ERR_JOIN_CHANNEL_REJECTED")
                            onJoinChorus2ndChannelCallback(ERR_JOIN_CHANNEL_REJECTED)
                        } else if (err == ERR_LEAVE_CHANNEL_REJECTED) {
                            Log.e(TAG, "leaveChorus2ndChannel failed: ERR_LEAVE_CHANNEL_REJECTED")
                        }
                    }

                    override fun onTokenPrivilegeWillExpire(token: String?) {
                        super.onTokenPrivilegeWillExpire(token)
                        ktvApiEventHandlerList.forEach { it.onTokenPrivilegeWillExpire() }
                    }

                    override fun onAudioVolumeIndication(
                            speakers: Array<out AudioVolumeInfo>?,
                            totalVolume: Int
                    ) {
                        super.onAudioVolumeIndication(speakers, totalVolume)
                        ktvApiEventHandlerList.forEach { it.onChorusChannelAudioVolumeIndication(speakers, totalVolume) }
                    }
                }
        )

        if (ret != 0) {
            Log.e(TAG, "joinChorus2ndChannel failed: $ret")
        }

        if (newRole == KTVSingRole.CoSinger) {
            mRtcEngine.muteRemoteAudioStreamEx(mainSingerUid, true, singChannelRtcConnection)
            Log.d(TAG, "muteRemoteAudioStream$mainSingerUid")
        }
    }

    private fun leaveChorus2ndChannel(role: KTVSingRole) {
        if (role == KTVSingRole.LeadSinger) {
            mRtcEngine.leaveChannelEx(subChorusConnection)
        } else if (role == KTVSingRole.CoSinger) {
            mRtcEngine.leaveChannelEx(subChorusConnection)
            mRtcEngine.muteRemoteAudioStreamEx(mainSingerUid, false, singChannelRtcConnection)
        }
    }

    // ------------------ 歌词播放、同步 ------------------
    // 开始播放歌词
    private val displayLrcTask = object : Runnable {
        override fun run() {
            if (!mStopDisplayLrc){
                val lastReceivedTime = mLastReceivedPlayPosTime ?: return
                val curTime = System.currentTimeMillis()
                val offset = curTime - lastReceivedTime
                if (offset <= 1000) {
                    val curTs = mReceivedPlayPosition + offset + highStartTime
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

    private var displayLrcFuture: ScheduledFuture<*>? = null
    private fun startDisplayLrc() {
        Log.d(TAG, "startDisplayLrc called")
        mStopDisplayLrc = false
        displayLrcFuture = scheduledThreadPool.scheduleAtFixedRate(displayLrcTask, 0,20, TimeUnit.MILLISECONDS)
    }

    // 停止播放歌词
    private fun stopDisplayLrc() {
        Log.d(TAG, "stopDisplayLrc called")
        mStopDisplayLrc = true
        displayLrcFuture?.cancel(true)
        displayLrcFuture = null
        if (scheduledThreadPool is ScheduledThreadPoolExecutor) {
            scheduledThreadPool.remove(displayLrcTask)
        }
    }

    // ------------------ 音高pitch同步 ------------------
//    private var mSyncPitchThread: Thread? = null
    private var mStopSyncPitch = true

    private val mSyncPitchTask = Runnable {
        if (!mStopSyncPitch) {
            if (mediaPlayerState == MediaPlayerState.PLAYER_STATE_PLAYING &&
                    (singerRole == KTVSingRole.LeadSinger || singerRole == KTVSingRole.SoloSinger)) {
                sendSyncPitch(pitch)
            }
        }
    }

    private fun sendSyncPitch(pitch: Double) {
        val msg: MutableMap<String?, Any?> = java.util.HashMap()
        msg["cmd"] = "setVoicePitch"
        msg["pitch"] = pitch
        val jsonMsg = JSONObject(msg)
        sendStreamMessageWithJsonObject(jsonMsg) {}
    }

    // 开始同步音高
    private var mSyncPitchFuture :ScheduledFuture<*>? = null
    private fun startSyncPitch() {
        mStopSyncPitch = false
        mSyncPitchFuture = scheduledThreadPool.scheduleAtFixedRate(mSyncPitchTask, 0, 50, TimeUnit.MILLISECONDS)
    }

    // 停止同步音高
    private fun stopSyncPitch() {
        mStopSyncPitch = true
        pitch = 0.0

        mSyncPitchFuture?.cancel(true)
        mSyncPitchFuture = null
        if (scheduledThreadPool is ScheduledThreadPoolExecutor) {
            scheduledThreadPool.remove(mSyncPitchTask)
        }
    }

    // ------------------ 评分驱动混音同步 ------------------
    private var mStopSyncScore = true

    private val mSyncScoreTask = Runnable {
        if (!mStopSyncScore) {
            if (ktvApiConfig.type == KTVType.Cantata &&
                    mediaPlayerState == MediaPlayerState.PLAYER_STATE_PLAYING &&
                    (singerRole == KTVSingRole.LeadSinger || singerRole == KTVSingRole.CoSinger)) {
                sendSyncScore()
            }
        }
    }

    private fun sendSyncScore() {
        val jsonObject = JSONObject()
        jsonObject.put("service", "audio_smart_mixer") // data message的目标消费者（服务）名
        jsonObject.put("version", "V1") //协议版本号（而非服务版本号）
        val payloadJson = JSONObject()
        payloadJson.put("cname", ktvApiConfig.chorusChannelName) // 频道名，演唱频道
        payloadJson.put("uid", ktvApiConfig.localUid.toString()) // 自己的uid
        payloadJson.put("uLv", -1) //user-leve1（用户级别，若无则为 -1，Level 越高，越重要）
        payloadJson.put("specialLabel", 0) //0: default-mode ，1：这个用户需要被排除出智能混音
        payloadJson.put("audioRoute", audioRouting) //音频路由：监听 onAudioRouteChanged
        payloadJson.put("vocalScore", singingScore) //单句打分
        jsonObject.put("payload", payloadJson)
        Log.d(TAG, "sendSyncScore: $jsonObject")
        sendStreamMessageWithJsonObject(jsonObject) {}
    }

    // 开始发送分数 3s/次
    private var mSyncScoreFuture :ScheduledFuture<*>? = null
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
    private var mStopSyncCloudConvergenceStatus = true

    private val mSyncCloudConvergenceStatusTask = Runnable {
        if (!mStopSyncCloudConvergenceStatus && ktvApiConfig.type == KTVType.Cantata && singerRole == KTVSingRole.LeadSinger) {
            sendSyncCloudConvergenceStatus()
        }
    }

    private fun sendSyncCloudConvergenceStatus() {
        val jsonObject = JSONObject()
        jsonObject.put("service", "audio_smart_mixer_status") // data message的目标消费者（服务）名
        jsonObject.put("version", "V1") //协议版本号（而非服务版本号）
        val payloadJson = JSONObject()
        payloadJson.put("Ts", getNtpTimeInMs()) // NTP 时间
        payloadJson.put("cname", ktvApiConfig.chorusChannelName) // 频道名
        payloadJson.put("status", getCloudConvergenceStatus()) //（-1： unknown，0：非K歌状态，1：K歌播放状态，2：K歌暂停状态）
        payloadJson.put("bgmUID", mpkConnection?.localUid.toString()) // mpk流的uid
        payloadJson.put("leadsingerUID", mainSingerUid.toString()) //（"-1" = unknown） //主唱Uid
        jsonObject.put("payload", payloadJson)
        Log.d(TAG, "sendSyncCloudConvergenceStatus: $jsonObject")
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
    private var mSyncCloudConvergenceStatusFuture :ScheduledFuture<*>? = null
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

    private fun loadLyric(songNo: Long, onLoadLyricCallback: (songNo: Long, lyricUrl: String?) -> Unit) {
        Log.d(TAG, "loadLyric: $songNo")
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
        Log.d(TAG, "loadMusic: $songNo")
        val ret = mMusicCenter.isPreloaded(songNo)
        if (ret == 0) {
            loadMusicCallbackMap.remove(songNo.toString())
            onLoadMusicCallback(songNo, 100, 0, null, null)
            return
        }

        val retPreload = mMusicCenter.preload(songNo, null)
        if (retPreload != 0) {
            Log.e(TAG, "preLoadMusic failed: $retPreload")
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
                        mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
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
                        Log.d(TAG,"play_status_seek: " + diff + " audioPlayoutDelay：" + audioPlayoutDelay +  "  localNtpTime: " + localNtpTime + "  expectPosition: " + expectPosition +
                                "  localPosition: " + localPosition + "  ntp diff: " + (localNtpTime - remoteNtp))
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
                    if (this.songIdentifier == songId) {
                        mLastReceivedPlayPosTime = System.currentTimeMillis()
                        mReceivedPlayPosition = realPosition
                    } else {
                        mLastReceivedPlayPosTime = null
                        mReceivedPlayPosition = 0
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
                Log.d(TAG, "onStreamMessage PlayerState: $state")
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
                        Constants.MediaPlayerError.getErrorByValue(error),
                        false
                ) }
            } else if (jsonMsg.getString("cmd") == "setVoicePitch") {
                val pitch = jsonMsg.getDouble("pitch")
                if (this.singerRole == KTVSingRole.Audience) {
                    this.pitch = pitch
                }
            }
        } catch (exp: JSONException) {
            Log.e(TAG, "onStreamMessage:$exp")
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
        Log.d(TAG, "onMusicCollectionResult, requestId: $requestId, list: $list, errorCode: $errorCode")
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

    private var highStartTime = 0L;
    override fun onSongSimpleInfoResult(
            requestId: String?,
            songCode: Long,
            simpleInfo: String,
            errorCode: Int
    ) {
        if (this.ktvApiConfig.type == KTVType.Normal) return
        val jsonMsg = JSONObject(simpleInfo)
        val format = jsonMsg.getJSONObject("format")
        val highPart = format.getJSONArray("highPart")
        val highStartTime = JSONObject(highPart[0].toString())
        val time = highStartTime.getLong("highStartTime")
        val endTime = highStartTime.getLong("highEndTime")
        this.highStartTime = time
        lrcView?.onHighPartTime(time, endTime)
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
            }
            MediaPlayerState.PLAYER_STATE_PLAYING -> {
                mRtcEngine.adjustPlaybackSignalVolume(remoteVolume)
            }
            MediaPlayerState.PLAYER_STATE_PAUSED -> {
                mRtcEngine.adjustPlaybackSignalVolume(100)
            }
            MediaPlayerState.PLAYER_STATE_STOPPED -> {
                mRtcEngine.adjustPlaybackSignalVolume(100)
                duration = 0
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

    override fun onAudioVolumeIndication(volume: Int) {}
}