package io.agora.scene.voice.rtckit

import android.content.Context
import android.util.Log
import io.agora.mediaplayer.Constants.MediaPlayerReason
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.*
import io.agora.scene.base.AudioModeration
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.model.SoundAudioBean
import io.agora.scene.voice.rtckit.listener.MediaPlayerObserver
import io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.ui.debugSettings.VoiceDebugSettingModel
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.net.callback.VRValueCallBack
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
        initRtcEngine(context)
        this.mLocalUid = rtcUid
        this.joinCallback = joinCallback
        VoiceBuddyFactory.get().rtcChannelTemp.broadcaster = broadcaster
        checkJoinChannel(channelId, rtcUid, soundEffect, broadcaster)
        // 语音鉴定
        AudioModeration.moderationAudio(channelId, rtcUid.toLong(),
            AudioModeration.AgoraChannelType.broadcast, "voice", {})
    }

    fun bgmManager(): AgoraBGMManager {
        if (mBgmManager == null) {
            mBgmManager = AgoraBGMManager(
                rtcEngine!!,
                VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId(),
                mLocalUid,
                VoiceBuddyFactory.get().getVoiceBuddy().rtmToken()
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
                    VoiceLogger.e(TAG, "voice rtc onError code:$err")
                }

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    VoiceLogger.d(TAG, "voice rtc onJoinChannelSuccess channel:$channel,uid:$uid")
                    rtcEngine?.setEnableSpeakerphone(true)
                    // 默认开启降噪
                    ThreadManager.getInstance().runOnMainThread {
                        deDefaultNoise(VoiceBuddyFactory.get().rtcChannelTemp.AINSMode)
                        deMusicNoise(VoiceBuddyFactory.get().rtcChannelTemp.AINSMusicMode)
                        deMicNoise(VoiceBuddyFactory.get().rtcChannelTemp.AINSMicMode)
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
            // 加载ai 降噪so
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
        VoiceLogger.d(TAG, "joinChannel $channelId,${VoiceBuddyFactory.get().getVoiceBuddy().rtcToken()}:$rtcUid")
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

            ConfigConstants.AINSMode.AINS_Custom -> { // 自定义
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
     * 音乐保护
     */
    fun deMusicNoise(anisMode: Int) {
        when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> { // 音乐保护 off
                rtcEngine?.apply {
                    setParameters("{\"che.audio.aed.enable\":0}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Weakness -> { // 音乐保护弱
                rtcEngine?.apply {
                    setParameters("{\"che.audio.aed.enable\":1}")
                    setParameters("{\"che.audio.sf.nsngMusicProbThr\":85}")
                    setParameters("{\"che.audio.sf.ainsMusicModeBackoffDB\":270}")
                    setParameters("{\"che.audio.sf.statNsMusicModeBackoffDB\":200}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Strong -> { // 音乐保护强
                rtcEngine?.apply {
                    setParameters("{\"che.audio.aed.enable\":1}")
                    setParameters("{\"che.audio.sf.nsngMusicProbThr\":60}")
                    setParameters("{\"che.audio.sf.ainsMusicModeBackoffDB\":270}")
                    setParameters("{\"che.audio.sf.statNsMusicModeBackoffDB\":200}")
                }
            }

            ConfigConstants.AINSMode.AINS_Custom -> { // 自定义
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
     * 人声保护
     */
    fun deMicNoise(anisMode: Int) {
        when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> { // 人声保护 off
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.ainsSpeechProtectThreshold\":100}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Weakness -> { // 人声保护弱
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.ainsSpeechProtectThreshold\":85}")
                }
            }

            ConfigConstants.AINSMode.AINS_AI_Strong -> { // 人声保护强
                rtcEngine?.apply {
                    setParameters("{\"che.audio.sf.ainsSpeechProtectThreshold\":50}")
                }
            }

            ConfigConstants.AINSMode.AINS_Custom -> { // 自定义
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
     * AI 回声消除（AIAEC）
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

    private var mediaPlayer: IMediaPlayer? = null

    private val firstMediaPlayerObserver = object : MediaPlayerObserver() {
        override fun onPlayerStateChanged(state: MediaPlayerState?, error: MediaPlayerReason?) {
            VoiceLogger.d(TAG, "firstMediaPlayerObserver onPlayerStateChanged state:$state error:$error")

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

    fun renewRtcToken(rtcToken: String){
        rtcEngine?.renewToken(rtcToken)
    }
}