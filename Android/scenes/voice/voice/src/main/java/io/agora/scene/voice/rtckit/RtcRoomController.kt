package io.agora.scene.voice.rtckit

import android.content.Context
import io.agora.mediaplayer.Constants.MediaPlayerError
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.*
import io.agora.scene.voice.bean.SoundAudioBean
import io.agora.scene.voice.rtckit.listener.MediaPlayerObserver
import io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.service.VoiceBuddyFactory
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.buddy.config.ConfigConstants
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.network.tools.VRValueCallBack

/**
 * @author create by zhangwei03
 */
class RtcRoomController {

    companion object {

        @JvmStatic
        fun get() = InstanceHelper.sSingle
    }

    object InstanceHelper {
        val sSingle = RtcRoomController()
    }

    private var rtcEngine: RtcEngineEx? = null

    /**房间临时数据*/
    val rtcChannelTemp = RtcChannelTemp()

    private var micVolumeListener: RtcMicVolumeListener? = null

    fun setMicVolumeListener(micVolumeListener: RtcMicVolumeListener) {
        this.micVolumeListener = micVolumeListener
    }

    private var joinCallback: VRValueCallBack<Boolean>? = null

    /**加入rtc频道*/
    fun joinChannel(
        context: Context,
        roomId: String,
        userId: Int,
        broadcaster: Boolean = false,
        joinCallback: VRValueCallBack<Boolean>
    ) {
        initRtcEngine(context, VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId())
        val rtcChannelConfig = RtcChannelConfig().apply {
            channelId = roomId
            rtcUid = userId
            isBroadcaster = broadcaster
        }
        this.joinCallback = joinCallback
        rtcChannelTemp.broadcaster = broadcaster
        checkJoinChannel(rtcChannelConfig)
    }

    private fun initRtcEngine(context: Context, appId: String): Boolean {
        if (rtcEngine != null) {
            return false
        }
        synchronized(RtcRoomController::class.java) {
            if (rtcEngine != null) return false
            //初始化RTC
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = appId
            config.mEventHandler = object : IRtcEngineEventHandler() {

                override fun onError(err: Int) {
                    super.onError(err)
                    "voice rtc onError code:$err".logE()
                }

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    // 默认开启降噪
                    deNoise(rtcChannelTemp.anisMode)
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
            }
            // 加载ai 降噪so
            config.addExtension("agora_ai_noise_suppression_extension")
            try {
                rtcEngine = RtcEngineEx.create(config) as RtcEngineEx?
            } catch (e: Exception) {
                e.printStackTrace()
                "rtc engine init error:${e.message}".logE()
                return false
            }
            return true
        }
    }

    private fun checkJoinChannel(config: RtcChannelConfig): Boolean {
        if (config.channelId.isEmpty() || config.rtcUid < 0) {
            "join channel error roomId or rtcUid illegal!(roomId:${config.channelId},rtcUid:${config.rtcUid})".logE()
            return false
        }

        rtcEngine?.apply {
            when (config.soundType) {
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
        if (config.isBroadcaster) {
            // 音效默认50
            rtcEngine?.adjustAudioMixingVolume(ConfigConstants.RotDefaultVolume)
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        val status = rtcEngine?.joinChannel(config.rtcToken, config.channelId, "", config.rtcUid)
        // 启用用户音量提示。
        rtcEngine?.enableAudioVolumeIndication(1000, 3, false)
        if (status != IRtcEngineEventHandler.ErrorCode.ERR_OK) {
            "join channel error status not ERR_OK!".logE()
            return false
        }
        return true
    }

    /**
     * 切换角色
     * @param broadcaster
     */
    fun switchRole(broadcaster: Boolean) {
        if (this.rtcChannelTemp.broadcaster == broadcaster) return
        if (broadcaster) {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        this.rtcChannelTemp.broadcaster = broadcaster
    }

    /**
     * Ai 降噪
     * @param anisMode 降噪模式
     */
    fun deNoise(anisMode: Int) {
        when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> {
                rtcEngine?.apply {
                    setParameters("{\"che.audio.ains_mode\":0}")
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
            openMediaPlayer(it.audioUrl,  it.speakerType)
        }
    }

    /**
     * 播放单个音效
     * @param soundId sound id
     * @param audioUrl cdn url
     * @param speakerType 模拟哪个机器人
     */
    fun playMusic(soundId: Int, audioUrl: String, speakerType: Int) {
        resetMediaPlayer()
        openMediaPlayer(audioUrl,  speakerType)
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
    fun enableLocalAudio(mute: Boolean) {
        if (rtcChannelTemp.isLocalAudioMute == mute) return
        rtcChannelTemp.isLocalAudioMute = mute
        rtcEngine?.enableLocalAudio(mute)
    }

    fun destroy() {
        rtcChannelTemp.reset()
        rtcEngine?.leaveChannel()
        mediaPlayer?.apply {
            unRegisterPlayerObserver(firstMediaPlayerObserver)
            destroy()
        }
        RtcEngineEx.destroy()
        rtcEngine = null
    }

    private var soundSpeakerType = ConfigConstants.BotSpeaker.BotBlue

    private val mediaPlayer: IMediaPlayer? by lazy {
        rtcEngine?.createMediaPlayer()?.apply {
            registerPlayerObserver(firstMediaPlayerObserver)
        }?.also {
            val options = ChannelMediaOptions()
            options.publishMediaPlayerAudioTrack = true
            options.publishMediaPlayerId = it.mediaPlayerId
            rtcEngine?.updateChannelMediaOptions(options)
        }
    }

    private val firstMediaPlayerObserver = object : MediaPlayerObserver() {
        override fun onPlayerStateChanged(state: MediaPlayerState?, error: MediaPlayerError?) {
            "firstMediaPlayerObserver onPlayerStateChanged state:$state error:$error".logE()

            when (state) {
                MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                    mediaPlayer?.play()
                }
                MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                    // 结束播放回调--->> 播放下一个，取队列第一个播放
                    ThreadManager.getInstance().runOnMainThread {
                        micVolumeListener?.onBotVolume(soundSpeakerType, true)
                        soundAudioQueue.removeFirstOrNull()?.let {
                            openMediaPlayer(it.audioUrl,  it.speakerType)
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
    }

    private fun openMediaPlayer(url: String, soundSpeaker: Int = ConfigConstants.BotSpeaker.BotBlue) {
        mediaPlayer?.open(url, 0)
        this.soundSpeakerType = soundSpeaker
    }
}