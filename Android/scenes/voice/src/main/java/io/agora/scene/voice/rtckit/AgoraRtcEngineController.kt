package io.agora.scene.voice.rtckit

import android.content.Context
import android.util.Log
import io.agora.mediaplayer.Constants.MediaPlayerReason
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.*
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.model.SoundAudioBean
import io.agora.scene.voice.rtckit.listener.MediaPlayerObserver
import io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.ui.debugSettings.VoiceDebugSettingModel
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.global.VoiceCenter
import io.agora.scene.voice.netkit.callback.VRValueCallBack

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

    private var mEarBackManager: AgoraEarBackManager? = null

    private var mSoundCardManager: AgoraSoundCardManager? = null

    private var micVolumeListener: RtcMicVolumeListener? = null

    fun setMicVolumeListener(micVolumeListener: RtcMicVolumeListener) {
        this.micVolumeListener = micVolumeListener
    }

    private var joinCallback: VRValueCallBack<Boolean>? = null

    /** Join RTC channel */
    fun joinChannel(
        context: Context, channelId: String, rtcUid: Int, soundEffect: Int, broadcaster: Boolean = false,
        joinCallback: VRValueCallBack<Boolean>
    ) {
        initRtcEngine(context)
        this.mLocalUid = rtcUid
        this.joinCallback = joinCallback
        VoiceCenter.rtcChannelTemp.broadcaster = broadcaster
        checkJoinChannel(channelId, rtcUid, soundEffect, broadcaster)
        AudioModeration.moderationAudio(channelId, rtcUid.toLong(),
            AudioModeration.AgoraChannelType.broadcast, "voice", {})
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
            // Initialize RTC
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = VoiceCenter.rtcAppId
            config.mEventHandler = object : IRtcEngineEventHandler() {

                override fun onError(err: Int) {
                    super.onError(err)
                    VoiceLogger.e(TAG, "voice rtc onError code:$err")
                }

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    VoiceLogger.d(TAG, "voice rtc onJoinChannelSuccess channel:$channel,uid:$uid")
                    rtcEngine?.setEnableSpeakerphone(true)
                    // Noise reduction is enabled by default
                    ThreadManager.getInstance().runOnMainThread {
                        deDefaultNoise(VoiceCenter.rtcChannelTemp.AINSMode)
                        deMusicNoise(VoiceCenter.rtcChannelTemp.AINSMusicMode)
                        deMicNoise(VoiceCenter.rtcChannelTemp.AINSMicMode)
                    }
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
            // Load ai noise reduction so
            config.addExtension("agora_ai_noise_suppression_extension")
            config.addExtension("agora_ai_echo_cancellation_extension")
            try {
                rtcEngine = RtcEngineEx.create(config) as RtcEngineEx?
                rtcEngine?.setParameters("{\"che.audio.input_sample_rate\" : 48000}")
            } catch (e: Exception) {
                e.printStackTrace()
                VoiceLogger.d(TAG, "voice rtc engine init error:${e.message}")
                return false
            }
            setInitNoiseParameters()
            mEarBackManager = AgoraEarBackManager(rtcEngine!!)
            mSoundCardManager = AgoraSoundCardManager(rtcEngine!!)
            return true
        }
    }

    private fun setInitNoiseParameters() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.enabled\":true}")
            setParameters("{\"che.audio.sf.nsEnable\":1}")
            setParameters("{\"che.audio.sf.nlpEnable\":1}")
            setParameters("{\"che.audio.ans.enable\":false}")
            setParameters("{\"che.audio.aec.nlpEnable\":false}")
        }
    }

    private fun checkJoinChannel(channelId: String, rtcUid: Int, soundEffect: Int, isBroadcaster: Boolean): Boolean {
        VoiceLogger.d(TAG, "joinChannel $channelId,$rtcUid")
        if (channelId.isEmpty() || rtcUid < 0) {
            joinCallback?.onError(Constants.ERR_FAILED, "roomId or rtcUid illegal!")
            return false
        }

        rtcEngine?.apply {
            when (soundEffect) {
                ConfigConstants.SoundSelection.Social_Chat,
                ConfigConstants.SoundSelection.Karaoke -> { // Social chat, KTV
                    setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                    setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
                    setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                }

                ConfigConstants.SoundSelection.Gaming_Buddy -> { // Game companion
                    setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
                }

                else -> { // Professional broadcaster
                    setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY)
                    setAudioScenario(Constants.AUDIO_SCENARIO_GAME_STREAMING)
                    setParameters("{\"che.audio.custom_payload_type\":73}")
                    setParameters("{\"che.audio.custom_bitrate\":128000}")
                    // setRecordingDeviceVolume(128) Only supported in 4.0.1
                    setParameters("{\"che.audio.input_channels\":2}")
                }
            }
        }
        if (isBroadcaster) {
            // Default sound effect volume 50
            rtcEngine?.adjustAudioMixingVolume(ConfigConstants.RotDefaultVolume)
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        val status = rtcEngine?.joinChannel(VoiceCenter.rtcToken, channelId, "", rtcUid)
        // Enable user volume indication
        rtcEngine?.enableAudioVolumeIndication(1000, 3, false)
        if (status != IRtcEngineEventHandler.ErrorCode.ERR_OK) {
            joinCallback?.onError(status ?: IRtcEngineEventHandler.ErrorCode.ERR_FAILED, "")
            return false
        }
        if (isBroadcaster) {
            mediaPlayer = rtcEngine?.createMediaPlayer()?.apply {
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
        if (VoiceCenter.rtcChannelTemp.broadcaster == broadcaster) return
        if (broadcaster) {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        VoiceCenter.rtcChannelTemp.broadcaster = broadcaster
    }

    /**
     * AI noise reduction
     * @param anisMode Noise reduction mode
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
     * NS
     */
    fun deDefaultNoise(anisMode: Int) {
        when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> { // NS off
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.nsEnable\":0}")
                }
            }

            ConfigConstants.AINSMode.AINS_Tradition_Weakness -> { // 传统NS soft模式
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.nsEnable\":1}")
                    setParameters("{\"che.audio.sf.nsngAlgRoute\":10}")
                    setParameters("{\"che.audio.sf.nsngPredefAgg\":10}")
                    setParameters("{\"che.audio.sf.statNsFastNsSpeechTrigThreshold\":0}")
                }
            }

            ConfigConstants.AINSMode.AINS_Tradition_Strong -> { // 传统NS aggressive模式
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.nsEnable\":1}")
                    setParameters("{\"che.audio.sf.nsngAlgRoute\":10}")
                    setParameters("{\"che.audio.sf.nsngPredefAgg\":11}")
                    setParameters("{\"che.audio.sf.statNsFastNsSpeechTrigThreshold\":50}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Weakness -> { // AINS soft模式
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.nsEnable\":1}")
                    setParameters("{\"che.audio.sf.ainsToLoadFlag\":1}")
                    setParameters("{\"che.audio.sf.nsngAlgRoute\":12}")
                    setParameters("{\"che.audio.sf.nsngPredefAgg\":10}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Strong -> { // AINS aggressive模式
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.nsEnable\":1}")
                    setParameters("{\"che.audio.sf.ainsToLoadFlag\":1}")
                    setParameters("{\"che.audio.sf.nsngAlgRoute\":12}")
                    setParameters("{\"che.audio.sf.nsngPredefAgg\":11}")
                }
            }

            ConfigConstants.AINSMode.AINS_Custom -> { // Custom
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.nsEnable\":${VoiceDebugSettingModel.nsEnable}}")
                    setParameters("{\"che.audio.sf.ainsToLoadFlag\":${VoiceDebugSettingModel.ainsToLoadFlag}}")
                    setParameters("{\"che.audio.sf.nsngAlgRoute\":${VoiceDebugSettingModel.nsngAlgRoute}}")
                    setParameters("{\"che.audio.sf.nsngPredefAgg\":${VoiceDebugSettingModel.nsngPredefAgg}}")
                    setParameters("{\"che.audio.sf.nsngMapInMaskMin\":${VoiceDebugSettingModel.nsngMapInMaskMin}}")
                    setParameters("{\"che.audio.sf.nsngMapOutMaskMin\":${VoiceDebugSettingModel.nsngMapOutMaskMin}}")
                    setParameters("{\"che.audio.sf.statNsLowerBound\":${VoiceDebugSettingModel.statNsLowerBound}}")
                    setParameters("{\"che.audio.sf.nsngFinalMaskLowerBound\":${VoiceDebugSettingModel.nsngFinalMaskLowerBound}}")
                    setParameters("{\"che.audio.sf.statNsEnhFactor\":${VoiceDebugSettingModel.statNsEnhFactor}}")
                    setParameters("{\"che.audio.sf.statNsFastNsSpeechTrigThreshold\":${VoiceDebugSettingModel.statNsFastNsSpeechTrigThreshold}}")
                }
            }
        }
    }

    /**
     * Music noise reduction
     */
    fun deMusicNoise(anisMode: Int) {
        when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> { //  off
                rtcEngine?.apply {
                    setParameters("{\"che.audio.aed.enable\":0}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Weakness -> { // Weakness
                rtcEngine?.apply {
                    setParameters("{\"che.audio.aed.enable\":1}")
                    setParameters("{\"che.audio.sf.nsngMusicProbThr\":85}")
                    setParameters("{\"che.audio.sf.ainsMusicModeBackoffDB\":270}")
                    setParameters("{\"che.audio.sf.statNsMusicModeBackoffDB\":200}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Strong -> { // Strong
                rtcEngine?.apply {
                    setParameters("{\"che.audio.aed.enable\":1}")
                    setParameters("{\"che.audio.sf.nsngMusicProbThr\":60}")
                    setParameters("{\"che.audio.sf.ainsMusicModeBackoffDB\":270}")
                    setParameters("{\"che.audio.sf.statNsMusicModeBackoffDB\":200}")
                }
            }

            ConfigConstants.AINSMode.AINS_Custom -> { // Custom
                rtcEngine?.apply {
                    setParameters("{\"che.audio.aed.enable\":${VoiceDebugSettingModel.aedEnable}}")
                    setParameters("{\"che.audio.sf.nsngMusicProbThr\":${VoiceDebugSettingModel.nsngMusicProbThr}}")
                    setParameters("{\"che.audio.sf.ainsMusicModeBackoffDB\":${VoiceDebugSettingModel.ainsMusicModeBackoffDB}}")
                    setParameters("{\"che.audio.sf.statNsMusicModeBackoffDB\":${VoiceDebugSettingModel.statNsMusicModeBackoffDB}}")
                }
            }
        }
    }

    /**
     * Mic noise reduction
     */
    fun deMicNoise(anisMode: Int) {
        when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> { //  off
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.ainsSpeechProtectThreshold\":100}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Weakness -> { // Weakness
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.ainsSpeechProtectThreshold\":85}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Strong -> { // Strong
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.ainsSpeechProtectThreshold\":50}")
                }
            }

            ConfigConstants.AINSMode.AINS_Custom -> { // Custom
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.ainsSpeechProtectThreshold\":${VoiceDebugSettingModel.ainsSpeechProtectThreshold}}")
                }
            }
        }
    }

    fun updateNsEnable() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.nsEnable\":${VoiceDebugSettingModel.nsEnable}}")
        }
    }

    fun updateAinsToLoadFlag() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.ainsToLoadFlag\":${VoiceDebugSettingModel.ainsToLoadFlag}}")
        }
    }

    fun updateNsngAlgRoute() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.nsngAlgRoute\":${VoiceDebugSettingModel.nsngAlgRoute}}")
        }
    }

    fun updateNsngPredefAgg() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.nsngPredefAgg\":${VoiceDebugSettingModel.nsngPredefAgg}}")
        }
    }

    fun updateNsngMapInMaskMin() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.nsngMapInMaskMin\":${VoiceDebugSettingModel.nsngMapInMaskMin}}")
        }
    }

    fun updateNsngMapOutMaskMin() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.nsngMapOutMaskMin\":${VoiceDebugSettingModel.nsngMapOutMaskMin}}")
        }
    }

    fun updateStatNsLowerBound() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.statNsLowerBound\":${VoiceDebugSettingModel.statNsLowerBound}}")
        }
    }

    fun updateNsngFinalMaskLowerBound() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.nsngFinalMaskLowerBound\":${VoiceDebugSettingModel.nsngFinalMaskLowerBound}}")
        }
    }

    fun updateStatNsEnhFactor() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.statNsEnhFactor\":${VoiceDebugSettingModel.statNsEnhFactor}}")
        }
    }

    fun updateStatNsFastNsSpeechTrigThreshold() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.statNsFastNsSpeechTrigThreshold\":${VoiceDebugSettingModel.statNsFastNsSpeechTrigThreshold}}")
        }
    }

    fun updateAedEnable() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.aed.enable\":${VoiceDebugSettingModel.aedEnable}}")
        }
    }

    fun updateNsngMusicProbThr() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.nsngMusicProbThr\":${VoiceDebugSettingModel.nsngMusicProbThr}}")
        }
    }

    fun updateAinsMusicModeBackoffDB() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.ainsMusicModeBackoffDB\":${VoiceDebugSettingModel.ainsMusicModeBackoffDB}}")
        }
    }

    fun updateStatNsMusicModeBackoffDB() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.statNsMusicModeBackoffDB\":${VoiceDebugSettingModel.statNsMusicModeBackoffDB}}")
        }
    }

    fun updateAinsSpeechProtectThreshold() {
        rtcEngine?.apply {
            setParameters("{\"che.audio.sf.ainsSpeechProtectThreshold\":${VoiceDebugSettingModel.ainsSpeechProtectThreshold}}")
        }
    }

    /**
     * AI echo cancellation (AIAEC)
     */
    fun setAIAECOn(isOn: Boolean) {
        rtcEngine?.apply {
            if (isOn) {
//                setParameters("{\"che.audio.aiaec.working_mode\":1}")
                setParameters("{\"che.audio.sf.ainlpToLoadFlag\":1}")
                setParameters("{\"che.audio.sf.nlpAlgRoute\":11}")
            } else {
//                setParameters("{\"che.audio.aiaec.working_mode\":0}")
                setParameters("{\"che.audio.sf.nlpAlgRoute\":10}")
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

    fun createLocalMediaPlayer(): IMediaPlayer? {
        return rtcEngine?.createMediaPlayer()
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
        // Reset others
        resetMediaPlayer()
        // Add to sound effect queue
        soundAudioQueue.clear()
        soundAudioQueue.addAll(soundAudioList)
        // Get first from queue to play
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
        VoiceLogger.d(TAG, "playMusic soundId:$soundId")
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
     * Local mute/unmute
     */
    fun enableLocalAudio(enable: Boolean) {
        Log.d(TAG, "set local audio enable: $enable")
        rtcEngine?.enableLocalAudio(enable)
        mEarBackManager?.updateEnableInEarMonitoring()
    }

    fun destroy() {
        VoiceCenter.rtcChannelTemp.reset()

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

    private var mediaPlayer: IMediaPlayer? = null

    private val firstMediaPlayerObserver = object : MediaPlayerObserver() {
        override fun onPlayerStateChanged(state: MediaPlayerState?, error: MediaPlayerReason?) {
            VoiceLogger.d(TAG, "firstMediaPlayerObserver onPlayerStateChanged state:$state error:$error")

            when (state) {
                MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                    mediaPlayer?.play()
                }

                MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                    // End playback callback -> Play next, get first from queue
                    ThreadManager.getInstance().runOnMainThread {
                        micVolumeListener?.onBotVolume(soundSpeakerType, true)
                        soundAudioQueue.removeFirstOrNull()?.let {
                            openMediaPlayer(it.audioUrl, it.speakerType)
                        }
                    }
                }

                MediaPlayerState.PLAYER_STATE_PLAYING -> {
                    // Start playback callback
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

    fun renewRtcToken(rtcToken: String){
        rtcEngine?.renewToken(rtcToken)
    }
}