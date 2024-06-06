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

class KTVApiImpl(
    val ktvApiConfig: KTVApiConfig
) : KTVApi, IMusicContentCenterEventHandler, IMediaPlayerObserver, IRtcEngineEventHandler() {

    companion object {
        private val scheduledThreadPool: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
        const val tag = "KTV_API_LOG"
        const val version = "5.0.0"
        const val lyricSyncVersion = 2
    }

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private var mRtcEngine: RtcEngineEx = ktvApiConfig.engine as RtcEngineEx
    private lateinit var mMusicCenter: IAgoraMusicContentCenter
    private var mPlayer: IMediaPlayer
    private val apiReporter: APIReporter = APIReporter(APIType.KTV, version, mRtcEngine)

    private var innerDataStreamId: Int = 0
    private var subChorusConnection: RtcConnection? = null

    private var mainSingerUid: Int = 0
    private var songCode: Long = 0
    private var songUrl: String = ""
    private var songUrl2: String = ""
    private var songIdentifier: String = ""

    private val lyricCallbackMap =
        mutableMapOf<String, (songNo: Long, lyricUrl: String?) -> Unit>() // (requestId, callback)
    private val lyricSongCodeMap = mutableMapOf<String, Long>() // (requestId, songCode)
    private val simpleInfoCallbackMap = mutableMapOf<String, (songNo: Long, success: Boolean) -> Unit>() // (requestId, callback)
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

    // multipath
    private var enableMultipathing = true

    private var professionalModeOpen = false
    private var audioRouting = 0
    private var isPublishAudio = false // 通过是否发音频流判断

    // 抢唱模式下是否需要prelude
    private var needPrelude = false

    // 歌词信息是否来源于 dataStream
    private var recvFromDataStream = false

    // 开始播放歌词
    private var mStopDisplayLrc = true
    private var displayLrcFuture: ScheduledFuture<*>? = null
    private val displayLrcTask = object : Runnable {
        override fun run() {
            if (!mStopDisplayLrc) {
                if (singerRole == KTVSingRole.Audience && !recvFromDataStream) return  // audioMetaData方案观众return
                val lastReceivedTime = mLastReceivedPlayPosTime ?: return
                val curTime = System.currentTimeMillis()
                val offset = curTime - lastReceivedTime
                if (offset <= 1000) {
                    val curTs = mReceivedPlayPosition + offset + highStartTime
                    if (singerRole == KTVSingRole.LeadSinger || singerRole == KTVSingRole.SoloSinger) {
                        val lrcTime = LrcTimeOuterClass.LrcTime.newBuilder()
                            .setTypeValue(LrcTimeOuterClass.MsgType.LRC_TIME.number)
                            .setForward(true)
                            .setSongId(songIdentifier)
                            .setTs(curTs)
                            .setUid(ktvApiConfig.localUid)
                            .build()

                        mRtcEngine.sendAudioMetadata(lrcTime.toByteArray())
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

    // 音高同步
    private var mStopSyncPitch = true
    private var mSyncPitchFuture :ScheduledFuture<*>? = null
    private val mSyncPitchTask = Runnable {
        if (!mStopSyncPitch) {
            if (ktvApiConfig.type == KTVType.SingRelay &&
                (singerRole == KTVSingRole.LeadSinger || singerRole == KTVSingRole.SoloSinger || singerRole == KTVSingRole.CoSinger) &&
                isOnMicOpen) {
                sendSyncPitch(pitch)
            } else if (mediaPlayerState == MediaPlayerState.PLAYER_STATE_PLAYING &&
                (singerRole == KTVSingRole.LeadSinger || singerRole == KTVSingRole.SoloSinger)) {
                sendSyncPitch(pitch)
            }
        }
    }

    init {
        apiReporter.reportFuncEvent("initialize", mapOf("config" to ktvApiConfig), mapOf())
        if (ktvApiConfig.musicType == KTVMusicType.SONG_CODE) {
            val contentCenterConfiguration = MusicContentCenterConfiguration()
            contentCenterConfiguration.appId = ktvApiConfig.appId
            contentCenterConfiguration.mccUid = ktvApiConfig.localUid.toLong()
            contentCenterConfiguration.token = ktvApiConfig.rtmToken
            contentCenterConfiguration.maxCacheSize = ktvApiConfig.maxCacheSize
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
        mRtcEngine.addHandler(this)
        mPlayer.registerPlayerObserver(this)

        renewInnerDataStreamId()
        setKTVParameters()
        startDisplayLrc()
        startSyncPitch()
        isRelease = false

        if (ktvApiConfig.type == KTVType.SingRelay) {
            KTVApi.remoteVolume = 100
        }
        mPlayer.setPlayerOption("play_pos_change_callback", 100)
    }

    // 日志输出
    private fun ktvApiLog(msg: String) {
        if (isRelease) return
        apiReporter.writeLog("[$tag][${ktvApiConfig.type}] $msg", LOG_LEVEL_INFO)
    }

    // 日志输出
    private fun ktvApiLogError(msg: String) {
        if (isRelease) return
        apiReporter.writeLog("[$tag][${ktvApiConfig.type}] $msg", LOG_LEVEL_ERROR)
    }

    override fun renewInnerDataStreamId() {
        apiReporter.reportFuncEvent("renewInnerDataStreamId", mapOf(), mapOf())

        val innerCfg = DataStreamConfig()
        innerCfg.syncWithAudio = true
        innerCfg.ordered = false
        this.innerDataStreamId = mRtcEngine.createDataStream(innerCfg)
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

        // ENT-1036
        if (ktvApiConfig.type == KTVType.SingRelay) {
            mRtcEngine.setParameters("{\"che.audio.aiaec.working_mode\":1}")
        }

        // 歌词强同步需要在audio4环境
        mRtcEngine.setParameters("{\"rtc.use_audio4\": true}")

        // mutipath
        enableMultipathing = true
        //mRtcEngine.setParameters("{\"rtc.enableMultipath\": true}")
        mRtcEngine.setParameters("{\"rtc.enable_tds_request_on_join\": true}")
        //mRtcEngine.setParameters("{\"rtc.remote_path_scheduling_strategy\": 0}")
        //mRtcEngine.setParameters("{\"rtc.path_scheduling_strategy\": 0}")
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
        stopSyncPitch()
        stopDisplayLrc()
        this.mLastReceivedPlayPosTime = null
        this.mReceivedPlayPosition = 0
        this.innerDataStreamId = 0

        lyricCallbackMap.clear()
        loadMusicCallbackMap.clear()
        musicChartsCallbackMap.clear()
        musicCollectionCallbackMap.clear()
        simpleInfoCallbackMap.clear()
        lrcView = null

        mRtcEngine.removeHandler(this)
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
        apiReporter.reportFuncEvent("enableProfessionalStreamerMode", mapOf("enable" to enable), mapOf())
        this.professionalModeOpen = enable
        processAudioProfessionalProfile()
    }

    // 专业模式
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
//                    parameters =
//                        "{\"rtc.enableMultipath\": $enable, \"rtc.path_scheduling_strategy\": 0, \"rtc.remote_path_scheduling_strategy\": 0}"
//                }, subChorusConnection)
//            }
//        }
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

    override fun renewToken(rtmToken: String, chorusChannelRtcToken: String) {
        apiReporter.reportFuncEvent("renewToken", mapOf(), mapOf())
        // 更新RtmToken
        mMusicCenter.renewToken(rtmToken)
        // 更新合唱频道RtcToken
        if (subChorusConnection != null) {
            val channelMediaOption = ChannelMediaOptions()
            channelMediaOption.token = chorusChannelRtcToken
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption, subChorusConnection)
            ktvApiConfig.chorusChannelToken = chorusChannelRtcToken
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
    // 9、CoSinger -》LeadSinger
    var singerRole: KTVSingRole = KTVSingRole.Audience
    override fun switchSingerRole(
        newRole: KTVSingRole,
        switchRoleStateListener: ISwitchRoleStateListener?
    ) {
        apiReporter.reportFuncEvent("switchSingerRole", mapOf("newRole" to newRole), mapOf())
        val oldRole = singerRole

        // 调整开关麦状态
        if (ktvApiConfig.type != KTVType.SingRelay) {
            if ((oldRole == KTVSingRole.LeadSinger || oldRole == KTVSingRole.SoloSinger) && (newRole == KTVSingRole.CoSinger || newRole == KTVSingRole.Audience) && !isOnMicOpen) {
                mRtcEngine.muteLocalAudioStream(true)
                mRtcEngine.adjustRecordingSignalVolume(100)
            } else if ((oldRole == KTVSingRole.Audience || oldRole == KTVSingRole.CoSinger) && (newRole == KTVSingRole.LeadSinger || newRole == KTVSingRole.SoloSinger) && !isOnMicOpen) {
                mRtcEngine.adjustRecordingSignalVolume(0)
                mRtcEngine.muteLocalAudioStream(false)
            }
        }

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
                    ktvApiLog("onJoinChorusSuccess")
                    singerRole = newRole
                    ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
                    switchRoleStateListener?.onSwitchRoleSuccess()
                }

                override fun onJoinChorusFail(reason: KTVJoinChorusFailReason) {
                    ktvApiLog("onJoinChorusFail reason：$reason")
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
                    ktvApiLog("onJoinChorusSuccess")
                    singerRole = newRole
                    switchRoleStateListener?.onSwitchRoleSuccess()
                    ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
                }

                override fun onJoinChorusFail(reason: KTVJoinChorusFailReason) {
                    ktvApiLog("onJoinChorusFail reason：$reason")
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
                    ktvApiLog("onJoinChorusSuccess")
                    singerRole = newRole
                    switchRoleStateListener?.onSwitchRoleSuccess()
                    ktvApiEventHandlerList.forEach { it.onSingerRoleChanged(oldRole, newRole) }
                }

                override fun onJoinChorusFail(reason: KTVJoinChorusFailReason) {
                    ktvApiLog("onJoinChorusFail reason：$reason")
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
        } else if (this.singerRole == KTVSingRole.CoSinger && newRole == KTVSingRole.LeadSinger) {
            // 9、CoSinger -》LeadSinger
            this.singerRole = newRole
            syncNewLeadSinger(ktvApiConfig.localUid)
            mRtcEngine.muteRemoteAudioStream(mainSingerUid, false)
            mainSingerUid = ktvApiConfig.localUid

            mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
            mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
            mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 80000}")

            val channelMediaOption = ChannelMediaOptions()
            channelMediaOption.autoSubscribeAudio = true
            channelMediaOption.publishMediaPlayerId = mPlayer.mediaPlayerId
            channelMediaOption.publishMediaPlayerAudioTrack = true
            mRtcEngine.updateChannelMediaOptions(channelMediaOption)

            val channelMediaOption1 = ChannelMediaOptions()
            channelMediaOption.autoSubscribeAudio = false
            channelMediaOption.autoSubscribeVideo = false
            channelMediaOption.publishMicrophoneTrack = true
            channelMediaOption.enableAudioRecordingOrPlayout = false
            channelMediaOption.clientRoleType = CLIENT_ROLE_BROADCASTER
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOption1, subChorusConnection)
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
        apiReporter.reportFuncEvent("searchMusicByMusicChartId", mapOf("musicChartId" to musicChartId, "page" to page, "pageSize" to pageSize, "jsonOption" to jsonOption), mapOf())
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
        this.needPrelude = config.needPrelude
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
                    if (this.ktvApiConfig.type != KTVType.SingBattle) {
                        musicLoadStateListener.onMusicLoadSuccess(song, lyricUrl)
                    } else {
                        getSongSimpleInfo(songCode) { code, success ->
                            if (success) {
                                musicLoadStateListener.onMusicLoadSuccess(song, lyricUrl)
                            } else {
                                musicLoadStateListener.onMusicLoadFail(code,
                                    KTVLoadMusicFailReason.GET_SIMPLE_INFO_FAIL
                                )
                            }
                        }
                    }
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
                            if (this.ktvApiConfig.type != KTVType.SingBattle) {
                                musicLoadStateListener.onMusicLoadSuccess(song, lyricUrl)
                            } else {
                                getSongSimpleInfo(songCode) { code, success ->
                                    if (success) {
                                        musicLoadStateListener.onMusicLoadSuccess(song, lyricUrl)
                                    } else {
                                        musicLoadStateListener.onMusicLoadFail(code,
                                            KTVLoadMusicFailReason.GET_SIMPLE_INFO_FAIL
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (config.mode == KTVLoadMusicMode.LOAD_MUSIC_ONLY) {
                    // 不需要加载歌词
                    ktvApiLog("loadMusic success")
                    musicLoadStateListener.onMusicLoadProgress(song, 100, MusicLoadStatus.COMPLETED, msg, lrcUrl)
                    if (this.ktvApiConfig.type != KTVType.SingBattle) {
                        musicLoadStateListener.onMusicLoadSuccess(song, "")
                    } else {
                        getSongSimpleInfo(songCode) { code, success ->
                            if (success) {
                                musicLoadStateListener.onMusicLoadSuccess(song, "")
                            } else {
                                musicLoadStateListener.onMusicLoadFail(code,
                                    KTVLoadMusicFailReason.GET_SIMPLE_INFO_FAIL
                                )
                            }
                        }
                    }
                }
            } else if (status == 2) {
                // 预加载歌曲加载中
                musicLoadStateListener.onMusicLoadProgress(song, percent, MusicLoadStatus.values().firstOrNull { it.value == status } ?: MusicLoadStatus.FAILED, msg, lrcUrl)
            } else if (status == 3) {
                // 主动停止下载
                musicLoadStateListener.onMusicLoadFail(song, KTVLoadMusicFailReason.CANCELED)
            } else {
                // 预加载歌曲失败
                ktvApiLogError("loadMusic failed: MUSIC_PRELOAD_FAIL")
                musicLoadStateListener.onMusicLoadFail(song, KTVLoadMusicFailReason.MUSIC_PRELOAD_FAIL)
            }
        }
    }

    override fun removeMusic(songCode: Long) {
        apiReporter.reportFuncEvent("removeMusic", mapOf("songCode" to songCode), mapOf())
        val ret = mMusicCenter.removeCache(songCode)
        if (ret < 0) {
            ktvApiLogError("removeMusic failed, ret: $ret")
        }
    }

    override fun loadMusic(
        url: String,
        config: KTVLoadMusicConfiguration
    ) {
        apiReporter.reportFuncEvent("loadMusic", mapOf("url" to url, "config" to config), mapOf())
        this.songIdentifier = config.songIdentifier
        this.songUrl = url
        this.mainSingerUid = config.mainSingerUid
        this.needPrelude = config.needPrelude
    }

    override fun load2Music(url1: String, url2: String, config: KTVLoadMusicConfiguration) {
        apiReporter.reportFuncEvent("load2Music", mapOf("url1" to url1, "url2" to url2, "config" to config), mapOf())
        this.songIdentifier = config.songIdentifier
        this.songUrl = url1
        this.songUrl2 = url2
        this.mainSingerUid = config.mainSingerUid
        this.needPrelude = config.needPrelude
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
        mPlayer.resume()
    }

    override fun pauseSing() {
        apiReporter.reportFuncEvent("pauseSing", mapOf(), mapOf())
        mPlayer.pause()
    }

    override fun seekSing(time: Long) {
        apiReporter.reportFuncEvent("seekSing", mapOf("time" to time), mapOf())
        mPlayer.seek(time)
        syncPlayProgress(time)
    }

    override fun setLrcView(view: ILrcView) {
        apiReporter.reportFuncEvent("setLrcView", mapOf("view" to view), mapOf())
        this.lrcView = view
    }

    override fun muteMic(mute: Boolean) {
        apiReporter.reportFuncEvent("muteMic", mapOf("mute" to mute), mapOf())
        this.isOnMicOpen = !mute
        if (ktvApiConfig.type != KTVType.SingRelay) {
            if (this.singerRole == KTVSingRole.SoloSinger || this.singerRole == KTVSingRole.LeadSinger) {
                mRtcEngine.adjustRecordingSignalVolume(if (isOnMicOpen) 100 else 0)

                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.publishMicrophoneTrack = isOnMicOpen
                channelMediaOption.clientRoleType = CLIENT_ROLE_BROADCASTER
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
                mRtcEngine.muteLocalAudioStream(!isOnMicOpen)
            } else {
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.publishMicrophoneTrack = isOnMicOpen
                channelMediaOption.clientRoleType = CLIENT_ROLE_BROADCASTER
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
                mRtcEngine.muteLocalAudioStream(!isOnMicOpen)
            }
        } else {
            mRtcEngine.adjustRecordingSignalVolume(if (isOnMicOpen) 100 else 0)
        }
    }

    override fun setAudioPlayoutDelay(audioPlayoutDelay: Int) {
        apiReporter.reportFuncEvent("setAudioPlayoutDelay", mapOf("audioPlayoutDelay" to audioPlayoutDelay), mapOf())
        this.audioPlayoutDelay = audioPlayoutDelay
    }

    override fun getMediaPlayer(): IMediaPlayer {
        return mPlayer
    }

    override fun getMusicContentCenter(): IAgoraMusicContentCenter {
        return mMusicCenter
    }

    // ------------------ inner KTVApi --------------------
    private fun becomeSoloSinger() {
        ktvApiLog("becomeSoloSinger called")
        // 主唱进入合唱模式
        mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
        mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
        mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 80000}")

        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.autoSubscribeAudio = true
        channelMediaOption.publishMediaPlayerId = mPlayer.mediaPlayerId
        channelMediaOption.publishMediaPlayerAudioTrack = true
        mRtcEngine.updateChannelMediaOptions(channelMediaOption)
    }

    private fun joinChorus(newRole: KTVSingRole, token: String, onJoinChorusStateListener: OnJoinChorusStateListener) {
        ktvApiLog("joinChorus: $newRole")
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
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)

                // 预加载歌曲成功
                if (ktvApiConfig.musicType == KTVMusicType.SONG_CODE) {
                    mPlayer.setPlayerOption("enable_multi_audio_track", 0)
                    val ret = (mPlayer as IAgoraMusicPlayer).open(songCode, 0) // TODO open failed
                    if (ret != 0) {
                        ktvApiLogError("mpk open failed: $ret")
                    }
                } else {
                    mPlayer.setPlayerOption("enable_multi_audio_track", 0)
                    val ret = mPlayer.open(songUrl, 0) // TODO open failed
                    if (ret != 0) {
                        ktvApiLogError("mpk open failed: $ret")
                    }
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
                ktvApiLogError("JoinChorus with Wrong role: $singerRole")
            }
        }
    }

    private fun leaveChorus(role: KTVSingRole) {
        ktvApiLog("leaveChorus: $singerRole")
        when (role) {
            KTVSingRole.LeadSinger -> {
                mainSingerHasJoinChannelEx = false
                leaveChorus2ndChannel(role)
            }
            KTVSingRole.CoSinger -> {
                mPlayer.stop()
                val channelMediaOption = ChannelMediaOptions()
                channelMediaOption.publishMediaPlayerAudioTrack = false
                mRtcEngine.updateChannelMediaOptions(channelMediaOption)
                leaveChorus2ndChannel(role)

                mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
                mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
                mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
                mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
            }
            else -> {
                ktvApiLogError("JoinChorus with wrong role: $singerRole")
            }
        }
    }

    private fun stopSing() {
        ktvApiLog("stopSong called")

        val channelMediaOption = ChannelMediaOptions()
        channelMediaOption.publishMediaPlayerAudioTrack = false
        mRtcEngine.updateChannelMediaOptions(channelMediaOption)

        mPlayer.stop()

        mRtcEngine.setAudioScenario(AUDIO_SCENARIO_GAME_STREAMING)
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
        mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
        mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
    }

    // ------------------ inner --------------------

    private fun isChorusCoSinger(): Boolean {
        return singerRole == KTVSingRole.CoSinger
    }

    private fun sendStreamMessageWithJsonObject(
        obj: JSONObject,
        success: (isSendSuccess: Boolean) -> Unit
    ) {
        val ret = mRtcEngine.sendStreamMessage(innerDataStreamId, obj.toString().toByteArray())
        if (ret == 0) {
            success.invoke(true)
        } else {
            ktvApiLogError("sendStreamMessageWithJsonObject failed: $ret")
        }
    }

    private fun syncPlayState(
        state: MediaPlayerState,
        reason: Constants.MediaPlayerReason
    ) {
        val msg: MutableMap<String?, Any?> = HashMap()
        msg["cmd"] = "PlayerState"
        msg["state"] = MediaPlayerState.getValue(state)
        msg["error"] = Constants.MediaPlayerReason.getValue(reason)
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
    private var handlerEx :IRtcEngineEventHandler? = null
    private fun joinChorus2ndChannel(
        newRole: KTVSingRole,
        token: String,
        mainSingerUid: Int,
        onJoinChorus2ndChannelCallback: (status: Int?) -> Unit
    ) {
        ktvApiLog("joinChorus2ndChannel: token:$token")
        if (newRole == KTVSingRole.SoloSinger || newRole == KTVSingRole.Audience) {
            ktvApiLogError("joinChorus2ndChannel with wrong role: $newRole")
            return
        }

        if (newRole == KTVSingRole.CoSinger) {
            mRtcEngine.setAudioScenario(AUDIO_SCENARIO_CHORUS)
            mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
            mRtcEngine.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
            mRtcEngine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
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
        // TODO 4.3.1 not ready
//        if (enableMultipathing) {
//            channelMediaOption.parameters =
//                "{\"rtc.path_scheduling_strategy\":0, \"rtc.enableMultipath\": true, \"rtc.remote_path_scheduling_strategy\": 0}"
//        }

        val rtcConnection = RtcConnection()
        rtcConnection.channelId = ktvApiConfig.chorusChannelName
        rtcConnection.localUid = ktvApiConfig.localUid
        subChorusConnection = rtcConnection

        val ret = mRtcEngine.joinChannelEx(
            token,
            rtcConnection,
            channelMediaOption,
            null
        )
        mRtcEngine.setParameters("{\"rtc.use_audio4\": true}")
        val handler = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                ktvApiLog("onJoinChannel2Success: channel:$channel, uid:$uid")
                if (isRelease) return
                super.onJoinChannelSuccess(channel, uid, elapsed)
                if (newRole == KTVSingRole.LeadSinger) {
                    mainSingerHasJoinChannelEx = true
                }
                onJoinChorus2ndChannelCallback(0)
                mRtcEngine.enableAudioVolumeIndicationEx(50, 10, true, rtcConnection)
            }

            override fun onLeaveChannel(stats: RtcStats?) {
                ktvApiLog("onLeaveChannel2")
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
                    ktvApiLogError("joinChorus2ndChannel failed: ERR_JOIN_CHANNEL_REJECTED")
                    onJoinChorus2ndChannelCallback(ERR_JOIN_CHANNEL_REJECTED)
                } else if (err == ERR_LEAVE_CHANNEL_REJECTED) {
                    ktvApiLogError("leaveChorus2ndChannel failed: ERR_LEAVE_CHANNEL_REJECTED")
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
        handlerEx = handler
        mRtcEngine.addHandlerEx(handler, rtcConnection)

        if (ret != 0) {
            ktvApiLogError("joinChorus2ndChannel failed: $ret")
        }

        if (newRole == KTVSingRole.CoSinger) {
            mRtcEngine.muteRemoteAudioStream(mainSingerUid, true)
            ktvApiLog("muteRemoteAudioStream$mainSingerUid")
        }
    }

    private fun leaveChorus2ndChannel(role: KTVSingRole) {
        mRtcEngine.removeHandlerEx(handlerEx, subChorusConnection)
        if (role == KTVSingRole.LeadSinger) {
            mRtcEngine.leaveChannelEx(subChorusConnection)
        } else if (role == KTVSingRole.CoSinger) {
            mRtcEngine.leaveChannelEx(subChorusConnection)
            mRtcEngine.muteRemoteAudioStream(mainSingerUid, false)
        }
    }

    // ------------------ 同步新主唱 --------------------
    private fun syncNewLeadSinger(uid: Int) {
        val msg: MutableMap<String?, Any?> = java.util.HashMap()
        msg["cmd"] = "syncNewLeadSinger"
        msg["uid"] = uid
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

    // ------------------ 音高pitch同步 ------------------
    private fun sendSyncPitch(pitch: Double) {
        val msg: MutableMap<String?, Any?> = java.util.HashMap()
        msg["cmd"] = "setVoicePitch"
        msg["pitch"] = pitch
        val jsonMsg = JSONObject(msg)
        sendStreamMessageWithJsonObject(jsonMsg) {}
    }

    // 开始同步音高
    private fun startSyncPitch() {
        mStopSyncPitch = false
        mSyncPitchFuture = scheduledThreadPool.scheduleAtFixedRate(mSyncPitchTask,0,50,TimeUnit.MILLISECONDS)
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

    private fun loadLyric(songNo: Long, onLoadLyricCallback: (songNo: Long, lyricUrl: String?) -> Unit) {
        ktvApiLog("loadLyric: $songNo")
        val requestId = mMusicCenter.getLyric(songNo, 0)
        if (requestId == null || requestId.isEmpty()) {
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

    private fun getSongSimpleInfo(songNo: Long, onSongSimpleInfoResult: (songCode: Long, success: Boolean) -> Unit) {
        ktvApiLog("getSongSimpleInfo: $songNo")
        val requestId = mMusicCenter.getSongSimpleInfo(songNo)
        if (requestId == null || requestId.isEmpty()) {
            onSongSimpleInfoResult.invoke(songNo, false)
            return
        }
        simpleInfoCallbackMap[requestId] = onSongSimpleInfoResult
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
    override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
        super.onStreamMessage(uid, streamId, data)
        if (uid != mainSingerUid) return
        dealWithStreamMessage(data)
    }

    override fun onAudioMetadataReceived(uid: Int, data: ByteArray?) {
        super.onAudioMetadataReceived(uid, data)
        val messageData = data ?: return
        try {

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
        } catch (exp: JSONException) {
            ktvApiLog("onStreamMessage:$exp")
        }
    }

    private fun dealWithStreamMessage(data: ByteArray?) {
        val jsonMsg: JSONObject
        val messageData = data ?: return
        try {
            val strMsg = String(messageData)
            jsonMsg = JSONObject(strMsg)
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
                            ktvApiLog(
                                "play_status_seek: " + diff + " audioPlayoutDelay：" + audioPlayoutDelay + "  localNtpTime: " + localNtpTime + "  expectPosition: " + expectPosition +
                                        "  localPosition: " + localPosition + "  ntp diff: " + (localNtpTime - remoteNtp)
                            )
                        }
                        if ((diff > 50 || diff < -50) && expectPosition < duration) { //设置阈值为50ms，避免频繁seek
                            ktvApiLog("player seek: $diff")
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
                        // 发送端是新发送端, 歌词信息需要从 audioMetadata 里取
                        recvFromDataStream = false
                    } else {
                        // 发送端是老发送端, 歌词信息需要从 dataStreamMessage 里取
                        recvFromDataStream = true
                        if (this.songIdentifier == songId) {
                            mLastReceivedPlayPosTime = System.currentTimeMillis()
                            mReceivedPlayPosition = realPosition
                            ktvApiEventHandlerList.forEach { it.onMusicPlayerPositionChanged(realPosition, 0) }
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
                ktvApiEventHandlerList.forEach {
                    it.onMusicPlayerStateChanged(
                        MediaPlayerState.getStateByValue(state),
                        Constants.MediaPlayerReason.getErrorByValue(error),
                        false
                    )
                }
            } else if (jsonMsg.getString("cmd") == "setVoicePitch") {
                val pitch = jsonMsg.getDouble("pitch")
                if (ktvApiConfig.type == KTVType.SingRelay && !isOnMicOpen && this.singerRole != KTVSingRole.Audience) {
                    this.pitch = pitch
                }
                if (this.singerRole == KTVSingRole.Audience) {
                    this.pitch = pitch
                }
            } else if (jsonMsg.getString("cmd") == "syncNewLeadSinger") {
                if (singerRole == KTVSingRole.CoSinger) {
                    mRtcEngine.muteRemoteAudioStream(mainSingerUid, false)
                    mainSingerUid = jsonMsg.getInt("uid")
                    mRtcEngine.muteRemoteAudioStream(mainSingerUid, true)
                }
            }
        } catch (_: JSONException) {
        }
    }

    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
        super.onAudioVolumeIndication(speakers, totalVolume)
        val allSpeakers = speakers ?: return
        // VideoPitch 回调, 用于同步各端音准
        if (this.ktvApiConfig.type == KTVType.SingRelay && !isOnMicOpen) {
            return
        }
        if (this.singerRole != KTVSingRole.Audience) {
            for (info in allSpeakers) {
                if (info.uid == 0) {
                    pitch =
                        if (this.mediaPlayerState == MediaPlayerState.PLAYER_STATE_PLAYING && isOnMicOpen) {
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
        if (KTVApi.useCustomAudioSource) return
        val audioState = stats ?: return
        audioPlayoutDelay = audioState.audioPlayoutDelay
    }

    // 用于检测耳机状态
    override fun onAudioRouteChanged(routing: Int) { // 0\2\5 earPhone
        super.onAudioRouteChanged(routing)
        this.audioRouting = routing
        processAudioProfessionalProfile()
    }

    // 用于检测收发流状态
    override fun onAudioPublishStateChanged(
        channel: String?,
        oldState: Int,
        newState: Int,
        elapseSinceLastState: Int
    ) {
        super.onAudioPublishStateChanged(channel, oldState, newState, elapseSinceLastState)
        if (newState == 3) {
            this.isPublishAudio = true
            processAudioProfessionalProfile()
        } else if (newState == 1) {
            this.isPublishAudio = false
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

    private var highStartTime = 0L
    override fun onSongSimpleInfoResult(
        requestId: String?,
        songCode: Long,
        simpleInfo: String,
        errorCode: Int
    ) {
        if (this.ktvApiConfig.type == KTVType.Normal) return
        val callback = simpleInfoCallbackMap[requestId] ?: return
        if (errorCode != 0) {
            ktvApiLogError("onSongSimpleInfoResult failed, requestId: $requestId, songCode: $songCode, errorCode: $errorCode")
            callback.invoke(songCode, false)
            return
        }
        try {
            val jsonMsg = JSONObject(simpleInfo)
            val format = jsonMsg.getJSONObject("format")
            val highPart = format.getJSONArray("highPart")
            val highStartTime = JSONObject(highPart[0].toString())
            val time = highStartTime.getLong("highStartTime")
            val endTime = highStartTime.getLong("highEndTime")
            val preludeDuration = highStartTime.getLong("preludeDuration")
            this.highStartTime = time
            if (needPrelude) {
                this.highStartTime -= preludeDuration
            }
            lrcView?.onHighPartTime(time, endTime)
            callback.invoke(songCode, true)
        } catch (e: JSONException) {
            ktvApiLogError("onSongSimpleInfoResult: ${e.message}")
            callback.invoke(songCode, false)
        }
    }

    // ------------------------ AgoraRtcMediaPlayerDelegate ------------------------
    private var duration: Long = 0
    override fun onPlayerStateChanged(
        state: MediaPlayerState?,
        reason: Constants.MediaPlayerReason?
    ) {
        val mediaPlayerState = state ?: return
        val mediaPlayerError = reason ?: return
        ktvApiLog("onPlayerStateChanged: $state")
        this.mediaPlayerState = mediaPlayerState
        when (mediaPlayerState) {
            MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                duration = mPlayer.duration
                this.localPlayerPosition = 0
                // 伴奏
                if (this.singerRole == KTVSingRole.SoloSinger ||
                    this.singerRole == KTVSingRole.LeadSinger
                ) {
                    mPlayer.selectMultiAudioTrack(1, 1)
                    mPlayer.play()
                } else {
                    mPlayer.selectAudioTrack(1)
                }
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

        ktvApiEventHandlerList.forEach { it.onMusicPlayerPositionChanged(position_ms, timestamp_ms) }
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