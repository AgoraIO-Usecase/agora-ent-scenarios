package io.agora.scene.voice.spatial.rtckit

import android.content.Context
import io.agora.mediaplayer.Constants.MediaPlayerReason
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.*
import io.agora.scene.base.AudioModeration
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
import io.agora.scene.voice.spatial.utils.GsonTools
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

    /**加入rtc频道*/
    fun joinChannel(
        context: Context, channelId: String, rtcUid: Int, soundEffect: Int, broadcaster: Boolean = false,
        joinCallback: VRValueCallBack<Boolean>
    ) {
        initRtcEngine(context)
        setupSpatialAudio()
        this.joinCallback = joinCallback
        VSpatialCenter.rtcChannelTemp.broadcaster = broadcaster
        checkJoinChannel(channelId, rtcUid, soundEffect, broadcaster)

        // 语音鉴定
        AudioModeration.moderationAudio(channelId, rtcUid.toLong(),
            AudioModeration.AgoraChannelType.broadcast, "voice", {})
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
            config.mAppId = VSpatialCenter.rtcAppId
            config.mEventHandler = object : IRtcEngineEventHandler() {

                override fun onError(err: Int) {
                    super.onError(err)
                    VoiceSpatialLogger.e(TAG, "voice rtc onError code:$err")
                }

                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    super.onJoinChannelSuccess(channel, uid, elapsed)
                    VoiceSpatialLogger.d(TAG, "voice rtc onJoinChannelSuccess channel:$channel,uid:$uid")
                    // 默认开启降噪
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
     * 初始化空间音频
     * 设置声音最大距离为 10
     * 最大接收人数为 6
     * 距离单位1值为 1f
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
     * 更新自己空间音频位置
     * @param pos 位置[x, y, z]
     * @param forward 朝向[x, y, z]
     * @param right 朝向[x, y, z]
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
     * 发送本地位置到远端
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
     * 更新远端音源的配置
     * 人声模糊关闭，空气衰减开启，衰减系数为0.5
     * @param uid 远端音源的uid
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
     * 更新远端音源的位置
     * @param pos 位置[x, y, z]
     * @param forward 朝向[x, y, z]
     */
    public fun updateRemotePosition(uid: Int, pos: FloatArray, forward: FloatArray) {
        val position = RemoteVoicePositionInfo()
        position.position = pos
        position.forward = forward
        spatial?.updateRemotePosition(uid, position)
    }

    /**
     * 更新播放器音源位置
     * @param pos 位置[x, y, z]
     * @param forward 朝向[x, y, z]
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
                playerVoicePositionInfo[botBluePlayer!!.mediaPlayerId] = position
            }

            ConfigConstants.BotSpeaker.BotRed -> {
                val position = RemoteVoicePositionInfo()
                position.position = pos
                position.forward = forward
                playerVoicePositionInfo[botRedPlayer!!.mediaPlayerId] = position
            }
        }
    }

    /**
     * 处理远端空间位置变化产生的回调
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
        val status = rtcEngine?.joinChannel(VSpatialCenter.rtcToken, channelId, "", rtcUid)
        // 启用用户音量提示。
        rtcEngine?.enableAudioVolumeIndication(1000, 3, false)
        if (status != IRtcEngineEventHandler.ErrorCode.ERR_OK) {
            joinCallback?.onError(status ?: IRtcEngineEventHandler.ErrorCode.ERR_FAILED, "")
            return false
        }
        mediaPlayer = rtcEngine?.createMediaPlayer()?.apply {
            registerPlayerObserver(firstMediaPlayerObserver)
        }
        botBluePlayer = rtcEngine?.createMediaPlayer()?.apply {
            registerPlayerObserver(firstMediaPlayerObserver)
        }
        botRedPlayer = rtcEngine?.createMediaPlayer()?.apply {
            registerPlayerObserver(firstMediaPlayerObserver)
        }
        return true
    }

    /**
     * 切换角色
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
        VoiceSpatialLogger.d(TAG, "playMusic soundId:$soundId")
        resetMediaPlayer()
        openMediaPlayer(audioUrl, speakerType)
    }

    // -------------- EQ相关 ----------------
    // 打开/关闭空气衰减
    fun enableBlueAbsorb(isChecked: Boolean) {
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_air_absorb = isChecked
        botBluePlayer?.setSpatialAudioParams(spatialAudioParams)
    }

    // 打开/关闭模糊
    fun enableBlueBlur(isChecked: Boolean) {
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_blur = isChecked
        botBluePlayer?.setSpatialAudioParams(spatialAudioParams)
    }

    // 打开/关闭空气衰减
    fun enableRedAbsorb(isChecked: Boolean) {
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_air_absorb = isChecked
        botRedPlayer?.setSpatialAudioParams(spatialAudioParams)
    }

    // 打开/关闭模糊
    fun enableRedBlur(isChecked: Boolean) {
        val spatialAudioParams = SpatialAudioParams()
        spatialAudioParams.enable_blur = isChecked
        botRedPlayer?.setSpatialAudioParams(spatialAudioParams)
    }

    // 设置衰减系数
    fun adjustBlueAttenuation(progress: Double) {
        botBluePlayer?.mediaPlayerId?.let {
            spatial?.setPlayerAttenuation(it, progress, false);
        }
    }

    // 设置衰减系数
    fun adjustRedAttenuation(progress: Double) {
        botRedPlayer?.mediaPlayerId?.let {
            spatial?.setPlayerAttenuation(it, progress.toDouble(), false);
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
     * 本地mute/unmute
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
                            botBluePlayer?.play()
                            playerVoicePositionInfo[botBluePlayer!!.mediaPlayerId]?.let {
                                spatial?.updatePlayerPositionInfo(botBluePlayer!!.mediaPlayerId, it)
                                localVoicePositionInfoRun?.run()
                            }
                        }

                        ConfigConstants.BotSpeaker.BotRed -> {
                            botRedPlayer?.play()
                            playerVoicePositionInfo[botRedPlayer!!.mediaPlayerId]?.let {
                                spatial?.updatePlayerPositionInfo(botRedPlayer!!.mediaPlayerId, it)
                                localVoicePositionInfoRun?.run()
                            }
                        }

                        ConfigConstants.BotSpeaker.BotBoth -> {
                            botBluePlayer?.play()
                            botRedPlayer?.play()
                            enableRedAbsorb(true)
                            enableBlueAbsorb(true)
                            playerVoicePositionInfo[botBluePlayer!!.mediaPlayerId]?.let {
                                spatial?.updatePlayerPositionInfo(botBluePlayer!!.mediaPlayerId, it)
                                localVoicePositionInfoRun?.run()
                            }
                            playerVoicePositionInfo[botRedPlayer!!.mediaPlayerId]?.let {
                                spatial?.updatePlayerPositionInfo(botRedPlayer!!.mediaPlayerId, it)
                                localVoicePositionInfoRun?.run()
                            }
                        }

                        else -> {
                            mediaPlayer?.play()
                            playerVoicePositionInfo[mediaPlayer!!.mediaPlayerId]?.let {
                                spatial?.updatePlayerPositionInfo(mediaPlayer!!.mediaPlayerId, it)
                                localVoicePositionInfoRun?.run()
                            }
                        }
                    }
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