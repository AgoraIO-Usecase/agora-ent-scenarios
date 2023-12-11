package io.agora.scene.voice.rtckit

import android.content.Context
import android.util.Log
import io.agora.mediaplayer.Constants.MediaPlayerError
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.*
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.TokenGenerator
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.model.SoundAudioBean
import io.agora.scene.voice.rtckit.listener.MediaPlayerObserver
import io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.net.callback.VRValueCallBack
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.ThreadManager

/**
 * @author create by zhangwei03
 */
class AgoraRtcEngineController {

    companion object {

        @JvmStatic
        fun get() = InstanceHelper.sSingle

        private const val TAG = "ENGINE_CONTROLLER_LOG"
    }

    object InstanceHelper {
        val sSingle = AgoraRtcEngineController()
    }

    private var rtcEngine: RtcEngineEx? = null

    private var mLocalUid = 0

    private var mBgmManager: AgoraBGMManager? = null

    private var mEarBackManager: AgoraEarBackManager? = null

    private var mSoundCardManager: AgoraSoundCardManager? = null

    private var mRtmToken = ""

    private var micVolumeListener: RtcMicVolumeListener? = null

    fun setMicVolumeListener(micVolumeListener: RtcMicVolumeListener) {
        this.micVolumeListener = micVolumeListener
    }

    private var joinCallback: VRValueCallBack<Boolean>? = null

    /**加入rtc频道*/
    fun joinChannel(
        context: Context, channelId: String, rtcUid: Int, soundEffect: Int, broadcaster: Boolean = false,
        joinCallback: VRValueCallBack<Boolean>
    ) {
        TokenGenerator.generateTokens(
            channelId,
            rtcUid.toString(),
            TokenGenerator.TokenGeneratorType.token006,
            arrayOf(
                TokenGenerator.AgoraTokenType.rtm
            ),
            { ret ->
                mRtmToken = ret[TokenGenerator.AgoraTokenType.rtm] ?: ""

                initRtcEngine(context)
                this.mLocalUid = rtcUid
                this.joinCallback = joinCallback
                VoiceBuddyFactory.get().rtcChannelTemp.broadcaster = broadcaster
                checkJoinChannel(channelId, rtcUid, soundEffect, broadcaster)
                // 语音鉴定
                AudioModeration.moderationAudio(channelId, rtcUid.toLong(),
                    AudioModeration.AgoraChannelType.broadcast, "voice", {})
            },{
                joinCallback?.onError(Constants.ERR_FAILED, "get token error")
            }
        )
    }

    fun bgmManager(): AgoraBGMManager {
        if (mBgmManager == null) {
            mBgmManager = AgoraBGMManager(
                rtcEngine!!,
                VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId(),
                mLocalUid,
                mRtmToken
            )
        }
        return mBgmManager!!
    }

    fun earBackManager(): AgoraEarBackManager? {
        return mEarBackManager
    }

    fun soundCardManager(): AgoraSoundCardManager? {
        return mSoundCardManager
    }

    private fun initRtcEngine(context: Context): Boolean {
        if (rtcEngine != null) {
            return false
        }
        synchronized(AgoraRtcEngineController::class.java) {
            if (rtcEngine != null) return false
            //初始化RTC
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId()
            config.mEventHandler = object : IRtcEngineEventHandler() {

                override fun onError(err: Int) {
                    super.onError(err)
                    "voice rtc onError code:$err".logE(TAG)
                }

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    "voice rtc onJoinChannelSuccess channel:$channel,uid:$uid".logD(TAG)
                    rtcEngine?.setEnableSpeakerphone(true)
                    // 默认开启降噪
                    deNoise(VoiceBuddyFactory.get().rtcChannelTemp.AINSMode)
                    joinCallback?.onSuccess(true)
                }

                override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
                    super.onAudioVolumeIndication(speakers, totalVolume)
                    if (speakers.isNullOrEmpty()) return
                    ThreadManager.getInstance().runOnMainThread {
                        speakers.forEach { audioVolumeInfo ->
                            if (audioVolumeInfo.volume == 0) {
                                micVolumeListener?.onUserVolume(
                                    audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_None
                                )
                            } else if (audioVolumeInfo.volume <= 60) {
                                micVolumeListener?.onUserVolume(
                                    audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Low
                                )
                            } else if (audioVolumeInfo.volume <= 120) {
                                micVolumeListener?.onUserVolume(
                                    audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Medium
                                )
                            } else if (audioVolumeInfo.volume <= 180) {
                                micVolumeListener?.onUserVolume(
                                    audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_High
                                )
                            } else {
                                micVolumeListener?.onUserVolume(
                                    audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Max
                                )
                            }
                        }
                    }
                }

                override fun onLocalAudioStats(stats: LocalAudioStats?) {
                    mEarBackManager?.updateDelay(stats?.earMonitorDelay ?: 0)
                }
            }
            // 加载ai 降噪so
            config.addExtension("agora_ai_noise_suppression_extension")
            config.addExtension("agora_ai_echo_cancellation_extension")
            try {
                rtcEngine = RtcEngineEx.create(config) as RtcEngineEx?
                rtcEngine?.setParameters("{\"che.audio.input_sample_rate\" : 48000}")
            } catch (e: Exception) {
                e.printStackTrace()
                "voice rtc engine init error:${e.message}".logE(TAG)
                return false
            }
            mEarBackManager = AgoraEarBackManager(rtcEngine!!)
            mSoundCardManager = AgoraSoundCardManager(rtcEngine!!)
            return true
        }
    }

    private fun checkJoinChannel(channelId: String, rtcUid: Int, soundEffect: Int, isBroadcaster: Boolean): Boolean {
        "joinChannel $channelId,${VoiceBuddyFactory.get().getVoiceBuddy().rtcToken()}:$rtcUid".logD(TAG)
        if (channelId.isEmpty() || rtcUid < 0) {
            joinCallback?.onError(Constants.ERR_FAILED, "roomId or rtcUid illegal!")
            return false
        }

        rtcEngine?.apply {
            when (soundEffect) {
                ConfigConstants.SoundSelection.Social_Chat,
                ConfigConstants.SoundSelection.Karaoke -> { // 社交语聊，ktv
                    setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                    setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
                    setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                }
                ConfigConstants.SoundSelection.Gaming_Buddy -> { // 游戏陪玩
                    setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
                }
                else -> { //专业主播
                    setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
                    setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                    setParameters("{\"che.audio.custom_payload_type\":73}")
                    setParameters("{\"che.audio.custom_bitrate\":128000}")
                    // setRecordingDeviceVolume(128) 4.0.1上才支持
                    setParameters("{\"che.audio.input_channels\":2}")
                }
            }
        }
        if (isBroadcaster) {
            // 音效默认50
            rtcEngine?.adjustAudioMixingVolume(ConfigConstants.RotDefaultVolume)
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        val status = rtcEngine?.joinChannel(VoiceBuddyFactory.get().getVoiceBuddy().rtcToken(), channelId, "", rtcUid)
        // 启用用户音量提示。
        rtcEngine?.enableAudioVolumeIndication(1000, 3, false)
        if (status != IRtcEngineEventHandler.ErrorCode.ERR_OK) {
            joinCallback?.onError(status ?: IRtcEngineEventHandler.ErrorCode.ERR_FAILED, "")
            return false
        }
        if (isBroadcaster){
            mediaPlayer =  rtcEngine?.createMediaPlayer()?.apply {
                registerPlayerObserver(firstMediaPlayerObserver)
            }?.also {
                val options = ChannelMediaOptions()
                options.publishMediaPlayerAudioTrack = true
                options.publishMediaPlayerId = it.mediaPlayerId
                rtcEngine?.updateChannelMediaOptions(options)
            }
        }
        return true
    }

    /**
     * 切换角色
     * @param broadcaster
     */
    fun switchRole(broadcaster: Boolean) {
        if (VoiceBuddyFactory.get().rtcChannelTemp.broadcaster == broadcaster) return
        if (broadcaster) {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        VoiceBuddyFactory.get().rtcChannelTemp.broadcaster = broadcaster
    }

    /**
     * Ai 降噪
     * @param anisMode 降噪模式
     */
    fun deNoise(anisMode: Int) {
        when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> {
                rtcEngine?.apply {
                    setParameters("{\"che.audio.ains_mode\":-1}")
                    setParameters("{\"che.audio.nsng.lowerBound\":80}")
                    setParameters("{\"che.audio.nsng.lowerMask\":50}")
                    setParameters("{\"che.audio.nsng.statisticalbound\":5}")
                    setParameters("{\"che.audio.nsng.finallowermask\":30}")
                    setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
                }
            }
            ConfigConstants.AINSMode.AINS_High -> {
                rtcEngine?.apply {
                    setParameters("{\"che.audio.ains_mode\":2}")
                    setParameters("{\"che.audio.nsng.lowerBound\":10}")
                    setParameters("{\"che.audio.nsng.lowerMask\":10}")
                    setParameters("{\"che.audio.nsng.statisticalbound\":0}")
                    setParameters("{\"che.audio.nsng.finallowermask\":8}")
                    setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
                }
            }
            else -> {
                rtcEngine?.apply {
                    setParameters("{\"che.audio.ains_mode\":2}")
                    setParameters("{\"che.audio.nsng.lowerBound\":80}")
                    setParameters("{\"che.audio.nsng.lowerMask\":50}")
                    setParameters("{\"che.audio.nsng.statisticalbound\":5}")
                    setParameters("{\"che.audio.nsng.finallowermask\":30}")
                    setParameters("{\"che.audio.nsng.enhfactorstastical\":200}")
                }
            }
        }
    }

    /**
     * AI 回声消除（AIAEC）
     */
    fun setAIAECOn(isOn: Boolean) {
        rtcEngine?.apply {
            if (isOn) {
                setParameters("{\"che.audio.aiaec.working_mode\":1}")
            } else {
                setParameters("{\"che.audio.aiaec.working_mode\":0}")
            }
        }
    }

    /**
     * AI 人声增强（AIAGC）
     */
    fun setAIAGCOn(isOn: Boolean) {
        rtcEngine?.apply {
            if (isOn) {
                setParameters("{\"che.audio.agc.enable\":true}")
            } else {
                setParameters("{\"che.audio.agc.enable\":false}")
            }
            setParameters("{\"che.audio.agc.targetlevelBov\":3}")
            setParameters("{\"che.audio.agc.compressionGain\":18}")
        }
    }

    /**
     * APM全链路音频开关
     */
    fun setApmOn(isOn: Boolean) {
        if (isOn) {
            rtcEngine?.setParameters("{\"rtc.debug.enable\": true}");
            rtcEngine?.setParameters(
                "{\"che.audio.frame_dump\":{" +
                        "\"location\":\"all\"," +
                        "\"action\":\"start\"," +
                        "\"max_size_bytes\":\"120000000\"," +
                        "\"uuid\":\"123456789\"," +
                        "\"duration\":\"1200000\"}" +
                        "}")
        } else {
            rtcEngine?.setParameters("{\"rtc.debug.enable\": false}")
        }
    }

    fun createLocalMediaPlayer(): IMediaPlayer? {
        return rtcEngine?.createMediaPlayer()
    }

    /**
     * 音效队列
     */
    private val soundAudioQueue: ArrayDeque<SoundAudioBean> = ArrayDeque()

    /**
     * 播放音效列表
     * @param soundAudioList 音效列表
     */
    fun playMusic(soundAudioList: List<SoundAudioBean>) {
        // 复原其他
        resetMediaPlayer()
        // 加入音效队列
        soundAudioQueue.clear()
        soundAudioQueue.addAll(soundAudioList)
        // 取队列第一个播放
        soundAudioQueue.removeFirstOrNull()?.let {
            openMediaPlayer(it.audioUrl, it.speakerType)
        }
    }

    /**
     * 播放单个音效
     * @param soundId sound id
     * @param audioUrl cdn url
     * @param speakerType 模拟哪个机器人
     */
    fun playMusic(soundId: Int, audioUrl: String, speakerType: Int) {
        "playMusic soundId:$soundId".logD(TAG)
        resetMediaPlayer()
        openMediaPlayer(audioUrl, speakerType)
    }

    /**
     * reset mpk
     */
    fun resetMediaPlayer() {
        soundAudioQueue.clear()
        mediaPlayer?.stop()
    }

    fun updateEffectVolume(volume: Int) {
        mediaPlayer?.adjustPlayoutVolume(volume)
        mediaPlayer?.adjustPublishSignalVolume(volume)
    }

    /**
     * 本地mute/unmute
     */
    fun enableLocalAudio(enable: Boolean) {
        Log.d(TAG, "set local audio enable: $enable")
        rtcEngine?.enableLocalAudio(enable)
        mEarBackManager?.updateEnableInEarMonitoring()
    }

    fun destroy() {
        VoiceBuddyFactory.get().rtcChannelTemp.reset()

        mBgmManager?.release()
        mBgmManager = null

        mEarBackManager = null
        mSoundCardManager = null

        if (mediaPlayer != null) {
            mediaPlayer?.unRegisterPlayerObserver(firstMediaPlayerObserver)
            mediaPlayer?.destroy()
            mediaPlayer = null
        }
        if (rtcEngine != null) {
            rtcEngine?.leaveChannel()
            RtcEngineEx.destroy()
            rtcEngine = null
        }
    }

    private var soundSpeakerType = ConfigConstants.BotSpeaker.BotBlue

    private var mediaPlayer:IMediaPlayer?=null

    private val firstMediaPlayerObserver = object : MediaPlayerObserver() {
        override fun onPlayerStateChanged(state: MediaPlayerState?, error: MediaPlayerError?) {
            "firstMediaPlayerObserver onPlayerStateChanged state:$state error:$error".logD(TAG)

            when (state) {
                MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                    mediaPlayer?.play()
                }
                MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                    // 结束播放回调--->> 播放下一个，取队列第一个播放
                    ThreadManager.getInstance().runOnMainThread {
                        micVolumeListener?.onBotVolume(soundSpeakerType, true)
                        soundAudioQueue.removeFirstOrNull()?.let {
                            openMediaPlayer(it.audioUrl, it.speakerType)
                        }
                    }
                }
                MediaPlayerState.PLAYER_STATE_PLAYING -> {
                    // 开始播放回调--->>
                    ThreadManager.getInstance().runOnMainThread {
                        micVolumeListener?.onBotVolume(soundSpeakerType, false)
                    }
                }
                else -> {}
            }
        }

        override fun onPositionChanged(position_ms: Long, timestamp_ms: Long) {
        }
    }

    private fun openMediaPlayer(url: String, soundSpeaker: Int = ConfigConstants.BotSpeaker.BotBlue) {
        mediaPlayer?.open(url, 0)
        this.soundSpeakerType = soundSpeaker
    }
}