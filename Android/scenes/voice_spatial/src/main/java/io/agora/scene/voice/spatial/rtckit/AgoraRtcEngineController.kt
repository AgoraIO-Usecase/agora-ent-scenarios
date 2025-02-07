package io.agora.scene.voice.spatial.rtckit

import android.content.Context
import io.agora.mediaplayer.Constants.MediaPlayerReason
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.*
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.utils.GsonTools
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.global.VSpatialCenter
import io.agora.scene.voice.spatial.model.DataStreamInfo
import io.agora.scene.voice.spatial.model.SeatPositionInfo
import io.agora.scene.voice.spatial.model.SoundAudioBean
import io.agora.scene.voice.spatial.net.callback.VRValueCallBack
import io.agora.scene.voice.spatial.rtckit.listener.MediaPlayerObserver
import io.agora.scene.voice.spatial.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.spatial.rtckit.listener.RtcSpatialPositionListener
import io.agora.spatialaudio.ILocalSpatialAudioEngine
import io.agora.spatialaudio.LocalSpatialAudioConfig
import io.agora.spatialaudio.RemoteVoicePositionInfo

/**
 * @author create by zhangwei03
 */
class AgoraRtcEngineController {

    companion object {

        @JvmStatic
        fun get() = InstanceHelper.sSingle

        private const val TAG = "AgoraRtcEngineController"
    }

    object InstanceHelper {
        val sSingle = AgoraRtcEngineController()
    }

    private var rtcEngine: RtcEngineEx? = null

    private var micVolumeListener: RtcMicVolumeListener? = null

    private var spatialListener: RtcSpatialPositionListener? = null

    private var spatial: ILocalSpatialAudioEngine? = null

    private val playerVoicePositionInfo = hashMapOf<Int, RemoteVoicePositionInfo>()
    private var localVoicePositionInfoRun: Runnable? = null

    private var dataStreamId: Int = 0

    fun setMicVolumeListener(micVolumeListener: RtcMicVolumeListener) {
        this.micVolumeListener = micVolumeListener
    }

    fun setSpatialListener(spatialListener: RtcSpatialPositionListener) {
        this.spatialListener = spatialListener
    }

    private var joinCallback: VRValueCallBack<Boolean>? = null

    /**Join RTC channel*/
    fun joinChannel(
        context: Context, channelId: String, rtcUid: Int, soundEffect: Int, broadcaster: Boolean = false,
        joinCallback: VRValueCallBack<Boolean>
    ) {
        initRtcEngine(context)
        setupSpatialAudio()
        this.joinCallback = joinCallback
        VSpatialCenter.rtcChannelTemp.broadcaster = broadcaster
        checkJoinChannel(channelId, rtcUid, soundEffect, broadcaster)

        // Audio moderation
        AudioModeration.moderationAudio(channelId, rtcUid.toLong(),
            AudioModeration.AgoraChannelType.Broadcast, "voice", {})
    }

    private fun initRtcEngine(context: Context): Boolean {
        if (rtcEngine != null) {
            return false
        }
        synchronized(AgoraRtcEngineController::class.java) {
            if (rtcEngine != null) return false
            // Initialize RTC
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = VSpatialCenter.rtcAppId
            config.mEventHandler = object : IRtcEngineEventHandler() {

                override fun onError(err: Int) {
                    super.onError(err)
                    VoiceSpatialLogger.e(TAG, "voice rtc onError code:$err")
                }

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    VoiceSpatialLogger.d(TAG, "voice rtc onJoinChannelSuccess channel:$channel,uid:$uid")
                    // Default noise reduction enabled
                    deNoise(VSpatialCenter.rtcChannelTemp.AINSMode)
                    dataStreamId = rtcEngine?.createDataStream(DataStreamConfig()) ?: 0
                    joinCallback?.onSuccess(true)
                }

                override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                    super.onStreamMessage(uid, streamId, data)
                    data?.let {
                        GsonTools.toBean(String(it), DataStreamInfo::class.java)?.apply {
                            if (code == 101) {
                                onRemoteSpatialStreamMessage(uid, streamId, this)
                            }
                        }
                    }
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
            try {
                rtcEngine = RtcEngineEx.create(config) as RtcEngineEx?
            } catch (e: Exception) {
                e.printStackTrace()
                VoiceSpatialLogger.e(TAG, "voice rtc engine init error:${e.message}")
                return false
            }
            return true
        }
    }

    /**
     * Initialize spatial audio
     * Set the maximum distance to 10
     * Maximum number of receivers is 6
     * Distance unit 1 value is 1f
     */
    private fun setupSpatialAudio() {
        VoiceSpatialLogger.d(TAG, "spatial setup spatial audio")
        val localSpatial = ILocalSpatialAudioEngine.create()
        val localSpatialAudioConfig = LocalSpatialAudioConfig()
        localSpatialAudioConfig.mRtcEngine = rtcEngine
        localSpatial.initialize(localSpatialAudioConfig)
        localSpatial.setMaxAudioRecvCount(6)
        localSpatial.setAudioRecvRange(20f)
        localSpatial.setDistanceUnit(1f)
        spatial = localSpatial
    }

    /**
     * Update self spatial audio position
     * @param pos Position [x, y, z]
     * @param forward Forward direction [x, y, z]
     * @param right Right direction [x, y, z]
     */
    public fun updateSelfPosition(pos: FloatArray, forward: FloatArray, right: FloatArray) {
        localVoicePositionInfoRun = Runnable {
            spatial?.updateSelfPosition(
                pos,
                forward,
                right,
                floatArrayOf(0.0f, 0.0f, 1.0f)
            )
        }
        localVoicePositionInfoRun?.run()
    }

    /**
     * Send local position to remote
     */
    public fun sendSelfPosition(position: SeatPositionInfo) {
        GsonTools.beanToString(position)?.also {
            val steamInfo = DataStreamInfo(101, it)
            val ret = rtcEngine?.sendStreamMessage(
                dataStreamId,
                GsonTools.beanToString(steamInfo)?.toByteArray()
            )
        }
    }

    /**
     * Update remote audio source configuration
     * Voice blur off, air attenuation on, attenuation coefficient 0.5
     * @param uid Remote audio source uid
     */
    public fun setupRemoteSpatialAudio(uid: Int) {
        VoiceSpatialLogger.d(TAG, "spatial setup remote: u: $uid")
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_blur = false
        spatialAudioParams.enable_air_absorb = true
        rtcEngine?.setRemoteUserSpatialAudioParams(uid, spatialAudioParams)
        spatial?.setRemoteAudioAttenuation(uid, 0.5, false)
    }

    /**
     * Update remote audio source position
     * @param pos Position [x, y, z]
     * @param forward Forward direction [x, y, z]
     */
    public fun updateRemotePosition(uid: Int, pos: FloatArray, forward: FloatArray) {
        val position = RemoteVoicePositionInfo()
        position.position = pos
        position.forward = forward
        spatial?.updateRemotePosition(uid, position)
    }

    /**
     * Update player audio source position
     * @param pos Position [x, y, z]
     * @param forward Forward direction [x, y, z]
     */
    public fun updatePlayerPosition(
        pos: FloatArray,
        forward: FloatArray,
        soundSpeaker: Int = ConfigConstants.BotSpeaker.BotBlue
    ) {
        when (soundSpeaker) {
            ConfigConstants.BotSpeaker.BotBlue -> {
                val position = RemoteVoicePositionInfo()
                position.position = pos
                position.forward = forward
                botBluePlayer?.mediaPlayerId?.let {
                    playerVoicePositionInfo[it] = position
                }
            }

            ConfigConstants.BotSpeaker.BotRed -> {
                val position = RemoteVoicePositionInfo()
                position.position = pos
                position.forward = forward
                botRedPlayer?.mediaPlayerId?.let {
                    playerVoicePositionInfo[it] = position
                }
            }
        }
    }

    /**
     * Handle remote spatial position change callback
     */
    private fun onRemoteSpatialStreamMessage(uid: Int, streamId: Int, info: DataStreamInfo) {
        GsonTools.toBean(info.message, SeatPositionInfo::class.java)?.apply {
            spatialListener?.onRemoteSpatialChanged(this)
        }
    }

    private fun checkJoinChannel(channelId: String, rtcUid: Int, soundEffect: Int, isBroadcaster: Boolean): Boolean {
        VoiceSpatialLogger.d(
            TAG,
            "joinChannel $channelId,$rtcUid"
        )
        if (channelId.isEmpty() || rtcUid < 0) {
            joinCallback?.onError(Constants.ERR_FAILED, "roomId or rtcUid illegal!")
            return false
        }

        rtcEngine?.apply {
            when (soundEffect) {
                ConfigConstants.SoundSelection.Social_Chat,
                ConfigConstants.SoundSelection.Karaoke -> { // Social chat, ktv
                    setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                    setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
                    setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                }

                ConfigConstants.SoundSelection.Gaming_Buddy -> { // Gaming buddy
                    setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
                }

                else -> { // Professional broadcaster
                    setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
                    setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                    setParameters("{\"che.audio.custom_payload_type\":73}")
                    setParameters("{\"che.audio.custom_bitrate\":128000}")
                    // setRecordingDeviceVolume(128) is only supported in version 4.0.1 and above
                    setParameters("{\"che.audio.input_channels\":2}")
                }
            }
        }
        if (isBroadcaster) {
            // Default volume 50
            rtcEngine?.adjustAudioMixingVolume(ConfigConstants.RotDefaultVolume)
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        val status = rtcEngine?.joinChannel(VSpatialCenter.rtcToken, channelId, "", rtcUid)
        // Enable user volume prompt.
        rtcEngine?.enableAudioVolumeIndication(1000, 3, false)
        if (status != IRtcEngineEventHandler.ErrorCode.ERR_OK) {
            joinCallback?.onError(status ?: IRtcEngineEventHandler.ErrorCode.ERR_FAILED, "")
            return false
        }
        if (isBroadcaster){
            mediaPlayer = rtcEngine?.createMediaPlayer()?.apply {
                registerPlayerObserver(firstMediaPlayerObserver)
            }?.also {
                val options = ChannelMediaOptions()
                options.publishMediaPlayerAudioTrack = true
                options.publishMediaPlayerId = it.mediaPlayerId
                rtcEngine?.updateChannelMediaOptions(options)
            }
            botBluePlayer = rtcEngine?.createMediaPlayer()?.apply {
                registerPlayerObserver(firstMediaPlayerObserver)
            }?.also {
                val options = ChannelMediaOptions()
                options.publishMediaPlayerAudioTrack = true
                options.publishMediaPlayerId = it.mediaPlayerId
                rtcEngine?.updateChannelMediaOptions(options)
            }
            botRedPlayer = rtcEngine?.createMediaPlayer()?.apply {
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
     * Switch role
     * @param broadcaster
     */
    fun switchRole(broadcaster: Boolean) {
        if (VSpatialCenter.rtcChannelTemp.broadcaster == broadcaster) return
        if (broadcaster) {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        VSpatialCenter.rtcChannelTemp.broadcaster = broadcaster
    }

    /**
     * Ai noise reduction
     * @param anisMode Noise reduction mode
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
     * AI echo cancellation (AIAEC)
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
     * AI voice enhancement (AIAGC)
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
     * Sound effect queue
     */
    private val soundAudioQueue: ArrayDeque<SoundAudioBean> = ArrayDeque()

    /**
     * Play sound effect list
     * @param soundAudioList Sound effect list
     */
    fun playMusic(soundAudioList: List<SoundAudioBean>) {
        // Restore other
        resetMediaPlayer()
        // Add to sound effect queue
        soundAudioQueue.clear()
        soundAudioQueue.addAll(soundAudioList)
        // Play the first one in the queue
        soundAudioQueue.removeFirstOrNull()?.let {
            openMediaPlayer(it.audioUrl, it.speakerType)
        }
    }

    /**
     * Play single sound effect
     * @param soundId sound id
     * @param audioUrl cdn url
     * @param speakerType Simulate which robot
     */
    fun playMusic(soundId: Int, audioUrl: String, speakerType: Int) {
        VoiceSpatialLogger.d(TAG, "playMusic soundId:$soundId")
        resetMediaPlayer()
        openMediaPlayer(audioUrl, speakerType)
    }

    // -------------- EQ related ----------------
    // Open/close air attenuation
    fun enableBlueAbsorb(isChecked: Boolean) {
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_air_absorb = isChecked
        botBluePlayer?.setSpatialAudioParams(spatialAudioParams)
    }

    // Open/close blur
    fun enableBlueBlur(isChecked: Boolean) {
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_blur = isChecked
        botBluePlayer?.setSpatialAudioParams(spatialAudioParams)
    }

    // Open/close air attenuation
    fun enableRedAbsorb(isChecked: Boolean) {
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_air_absorb = isChecked
        botRedPlayer?.setSpatialAudioParams(spatialAudioParams)
    }

    // Open/close blur
    fun enableRedBlur(isChecked: Boolean) {
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_blur = isChecked
        botRedPlayer?.setSpatialAudioParams(spatialAudioParams)
    }

    // Set attenuation coefficient
    fun adjustBlueAttenuation(progress: Double) {
        botBluePlayer?.mediaPlayerId?.let {
            spatial?.setPlayerAttenuation(it, progress, false);
        }
    }

    // Set attenuation coefficient
    fun adjustRedAttenuation(progress: Double) {
        botRedPlayer?.mediaPlayerId?.let {
            spatial?.setPlayerAttenuation(it, progress.toDouble(), false);
        }
    }

    /**
     * APM full-link audio switch
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
                        "}"
            )
        } else {
            rtcEngine?.setParameters("{\"rtc.debug.enable\": false}")
        }
    }

    /**
     * reset mpk
     */
    fun resetMediaPlayer() {
        soundAudioQueue.clear()
        mediaPlayer?.stop()
        botBluePlayer?.stop()
        botRedPlayer?.stop()
    }

    fun updateEffectVolume(volume: Int) {
        mediaPlayer?.adjustPlayoutVolume(volume)
        mediaPlayer?.adjustPublishSignalVolume(volume)

        botBluePlayer?.adjustPlayoutVolume(volume)
        botBluePlayer?.adjustPublishSignalVolume(volume)

        botRedPlayer?.adjustPlayoutVolume(volume)
        botRedPlayer?.adjustPublishSignalVolume(volume)
    }

    /**
     * Local mute/unmute
     */
    fun enableLocalAudio(enable: Boolean) {
        rtcEngine?.enableLocalAudio(enable)
    }

    fun destroy() {
        spatial = null
        VSpatialCenter.rtcChannelTemp.reset()
        if (mediaPlayer != null) {
            mediaPlayer?.unRegisterPlayerObserver(firstMediaPlayerObserver)
            mediaPlayer?.destroy()
            mediaPlayer = null
        }
        if (botBluePlayer != null) {
            botBluePlayer?.unRegisterPlayerObserver(firstMediaPlayerObserver)
            botBluePlayer?.destroy()
            botBluePlayer = null
        }
        if (botRedPlayer != null) {
            botRedPlayer?.unRegisterPlayerObserver(firstMediaPlayerObserver)
            botRedPlayer?.destroy()
            botRedPlayer = null
        }
        if (rtcEngine != null) {
            rtcEngine?.leaveChannel()
            RtcEngineEx.destroy()
            rtcEngine = null
        }
        localVoicePositionInfoRun = null
        playerVoicePositionInfo.clear()
    }

    private var soundSpeakerType = ConfigConstants.BotSpeaker.BotBlue

    private var mediaPlayer: IMediaPlayer? = null

    private var botBluePlayer: IMediaPlayer? = null

    private var botRedPlayer: IMediaPlayer? = null

    private val firstMediaPlayerObserver = object : MediaPlayerObserver() {
        override fun onPlayerStateChanged(state: MediaPlayerState?, error: MediaPlayerReason?) {
            VoiceSpatialLogger.d(TAG, "firstMediaPlayerObserver onPlayerStateChanged state:$state error:$error")
            when (state) {
                MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                    when (soundSpeakerType) {
                        ConfigConstants.BotSpeaker.BotBlue -> {
                            botBluePlayer?.let { mediaPlayer->
                                mediaPlayer.play()
                                playerVoicePositionInfo[mediaPlayer.mediaPlayerId]?.let {
                                    spatial?.updatePlayerPositionInfo(mediaPlayer.mediaPlayerId, it)
                                    localVoicePositionInfoRun?.run()
                                }
                            }

                        }

                        ConfigConstants.BotSpeaker.BotRed -> {
                            botRedPlayer?.let { mediaPlayer->
                                mediaPlayer.play()
                                playerVoicePositionInfo[mediaPlayer.mediaPlayerId]?.let {
                                    spatial?.updatePlayerPositionInfo(mediaPlayer.mediaPlayerId, it)
                                    localVoicePositionInfoRun?.run()
                                }
                            }
                        }

                        ConfigConstants.BotSpeaker.BotBoth -> {
                            botBluePlayer?.play()
                            botRedPlayer?.play()
                            enableRedAbsorb(true)
                            enableBlueAbsorb(true)
                            botBluePlayer?.let { mediaPlayer->
                                mediaPlayer.play()
                                playerVoicePositionInfo[mediaPlayer.mediaPlayerId]?.let {
                                    spatial?.updatePlayerPositionInfo(mediaPlayer.mediaPlayerId, it)
                                    localVoicePositionInfoRun?.run()
                                }
                            }
                            botRedPlayer?.let { mediaPlayer->
                                mediaPlayer.play()
                                playerVoicePositionInfo[mediaPlayer.mediaPlayerId]?.let {
                                    spatial?.updatePlayerPositionInfo(mediaPlayer.mediaPlayerId, it)
                                    localVoicePositionInfoRun?.run()
                                }
                            }
                        }

                        else -> {
                            mediaPlayer?.let { mediaPlayer->
                                mediaPlayer.play()
                                playerVoicePositionInfo[mediaPlayer.mediaPlayerId]?.let {
                                    spatial?.updatePlayerPositionInfo(mediaPlayer.mediaPlayerId, it)
                                    localVoicePositionInfoRun?.run()
                                }
                            }
                        }
                    }
                }

                MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                    // End playback callback--->> Play next, play the first one in the queue
                    ThreadManager.getInstance().runOnMainThread {
                        micVolumeListener?.onBotVolume(soundSpeakerType, true)
                        soundAudioQueue.removeFirstOrNull()?.let {
                            openMediaPlayer(it.audioUrl, it.speakerType)
                        }
                    }
                }

                MediaPlayerState.PLAYER_STATE_PLAYING -> {
                    // Start playback callback--->>
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
        this.soundSpeakerType = soundSpeaker
        when (soundSpeaker) {
            ConfigConstants.BotSpeaker.BotBlue -> {
                botBluePlayer?.stop()
                botBluePlayer?.open(url, 0)
            }

            ConfigConstants.BotSpeaker.BotRed -> {
                botRedPlayer?.stop()
                botRedPlayer?.open(url, 0)
            }

            ConfigConstants.BotSpeaker.BotBoth -> {
                botBluePlayer?.stop()
                botRedPlayer?.stop()
                botBluePlayer?.open(url, 0)
                botRedPlayer?.open(url, 0)
            }

            else -> {
                mediaPlayer?.stop()
                mediaPlayer?.open(url, 0)
            }
        }
    }
}