package io.agora.scene.voice.controller

import android.content.Context
import io.agora.scene.voice.bean.SoundAudioBean
import io.agora.scene.voice.BuildConfig
import io.agora.scene.voice.service.VoiceBuddyFactory
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.buddy.config.ConfigConstants
import io.agora.voice.rtckit.open.IRtcKitListener
import io.agora.voice.rtckit.open.RtcKitManager
import io.agora.voice.rtckit.open.config.RtcChannelConfig
import io.agora.voice.rtckit.open.config.RtcInitConfig
import io.agora.voice.rtckit.open.event.MediaPlayerEvent
import io.agora.voice.rtckit.open.event.RtcAudioEvent
import io.agora.voice.rtckit.open.event.RtcDeNoiseEvent
import io.agora.voice.rtckit.open.status.*
import io.agora.voice.network.tools.VRValueCallBack

/**
 * @author create by zhangwei03
 */
class RtcRoomController : IRtcKitListener {

    companion object {

        @JvmStatic
        fun get() = InstanceHelper.sSingle
    }

    object InstanceHelper {
        val sSingle = RtcRoomController()
    }

    private val rtcChannelConfig by lazy {
        RtcChannelConfig(VoiceBuddyFactory.get().getVoiceBuddy().rtcToken())
    }

    private var rtcManger: RtcKitManager? = null

    /**是否是主播*/
    var broadcaster = true

    /**本地audio 是否mute*/
    var isLocalAudioMute = false

    /**第一次启动机器，播放*/
    var firstActiveBot = true

    /**第一次切换ai 降噪*/
    var firstSwitchAnis = true

    /**降噪*/
    var anisMode = ConfigConstants.AINSMode.AINS_Medium

    /**是否开启机器人*/
    var isUseBot: Boolean = false

    /**机器人音量*/
    var botVolume: Int = ConfigConstants.RotDefaultVolume

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
        rtcManger = RtcKitManager.initRTC(context, RtcInitConfig(VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId()), this)
        rtcChannelConfig.roomId = roomId
        rtcChannelConfig.userId = userId
        rtcChannelConfig.broadcaster = broadcaster
        this.joinCallback = joinCallback
        this.broadcaster = broadcaster
        rtcManger?.joinChannel(rtcChannelConfig)
    }

    fun switchRole(broadcaster: Boolean) {
        if (this.broadcaster == broadcaster) return
        rtcManger?.switchRole(broadcaster)
        this.broadcaster = broadcaster
    }

    /**
     * 降噪控制
     */
    fun deNoise(anisModeBean: io.agora.scene.voice.bean.AINSModeBean) {
        val event = when (anisModeBean.anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> RtcDeNoiseEvent.CloseEvent()
            ConfigConstants.AINSMode.AINS_High -> RtcDeNoiseEvent.HighEvent()
            else -> RtcDeNoiseEvent.MediumEvent()
        }
        rtcManger?.operateDeNoise(event)
    }

    /**
     * 音效队列
     */
    private val soundAudioQueue: ArrayDeque<SoundAudioBean> = ArrayDeque()

    /**
     * 播放音效
     */
    fun playMusic(soundAudioList: List<SoundAudioBean>) {
        // 复原其他
        rtcManger?.operateMediaPlayer(MediaPlayerEvent.ResetEvent())
        // 加入音效队列
        soundAudioQueue.clear()
        soundAudioQueue.addAll(soundAudioList)
        // 取队列第一个播放
        soundAudioQueue.removeFirstOrNull()?.let {
            rtcManger?.operateMediaPlayer(
                MediaPlayerEvent.OpenEvent(it.audioUrl, 0, it.speakerType)
            )
        }
    }

    /**
     * 播放音效
     */
    fun playMusic(soundId: Int, audioUrl: String, speakerType: Int) {
        resetMediaPlayer()
        rtcManger?.operateMediaPlayer(MediaPlayerEvent.OpenEvent(audioUrl, 0, speakerType))
    }

    fun resetMediaPlayer() {
        soundAudioQueue.clear()
        rtcManger?.operateMediaPlayer(MediaPlayerEvent.ResetEvent())
    }

    fun updateEffectVolume(volume: Int) {
        rtcManger?.operateMediaPlayer(MediaPlayerEvent.AdjustPlayoutVolumeEvent(volume))
    }

    /**
     * 本地mute/unmute
     */
    fun enableLocalAudio(mute: Boolean) {
        if (isLocalAudioMute == mute) return
        isLocalAudioMute = mute
        rtcManger?.operateAudio(RtcAudioEvent.AudioMuteLocal(mute))
    }

    fun destroy() {
        // 退出房间恢复默认值
        firstActiveBot = true
        firstSwitchAnis = true
        anisMode = ConfigConstants.AINSMode.AINS_Medium
        isUseBot = false
        botVolume = ConfigConstants.RotDefaultVolume
        rtcManger?.leaveChannel()
        rtcManger?.destroy()
    }

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        // 默认开启降噪
        val event = when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> RtcDeNoiseEvent.CloseEvent()
            ConfigConstants.AINSMode.AINS_High -> RtcDeNoiseEvent.HighEvent()
            else -> RtcDeNoiseEvent.MediumEvent()
        }
        rtcManger?.operateDeNoise(event)
        joinCallback?.onSuccess(true)
    }

    override fun onLeaveChannel() {

    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {

    }

    override fun onNetworkStatus(netWorkStatus: RtcNetWorkStatus) {

    }

    override fun onAudioStatus(audioChangeStatus: RtcAudioChangeStatus) {
    }

    override fun onUserJoin(userId: Int) {
    }


    override fun onLeaveChannel(userId: Int) {

    }

    override fun onAudioMixingFinished(soundId: Int, finished: Boolean, speakerType: Int) {
//        if (finished) {
//            // 结束播放回调--->>播放下一个，取队列第一个播放
//            ThreadManager.getInstance().runOnMainThread {
//                micVolumeListener?.onBotVolume(speakerType, true)
//            }
//            soundAudioQueue.removeFirstOrNull()?.let {
//                rtcManger?.operateSoundEffect(
//                    RtcSoundEffectEvent.PlayEffectEvent(it.soundId, it.audioUrl, false, 1, it.speakerType)
//                )
//            }
//        } else {
//            // 开始播放回调--->>
//            ThreadManager.getInstance().runOnMainThread {
//                micVolumeListener?.onBotVolume(speakerType, false)
//            }
//        }
    }

    override fun onMediaPlayerFinished(finished: Boolean, speakerType: Int) {
        if (finished) {
            // 结束播放回调--->> 播放下一个，取队列第一个播放
            ThreadManager.getInstance().runOnMainThread {
                micVolumeListener?.onBotVolume(speakerType, true)
                soundAudioQueue.removeFirstOrNull()?.let {
                    rtcManger?.operateMediaPlayer(
                        MediaPlayerEvent.OpenEvent(it.audioUrl, 0, it.speakerType)
                    )
                }
            }

        } else {
            // 开始播放回调--->>
            ThreadManager.getInstance().runOnMainThread {
                micVolumeListener?.onBotVolume(speakerType, false)
            }
        }
    }

    override fun onError(rtcErrorStatus: RtcErrorStatus) {
    }

    override fun onAudioVolumeIndication(volumeIndicationStatus: RtcAudioVolumeIndicationStatus) {
        ThreadManager.getInstance().runOnMainThread {
            volumeIndicationStatus.speakers?.forEach { audioVolumeInfo ->
                if (audioVolumeInfo.volume == 0) {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_None)
                } else if (audioVolumeInfo.volume <= 60) {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Low)
                } else if (audioVolumeInfo.volume <= 120) {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Medium)
                } else if (audioVolumeInfo.volume <= 180) {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_High)
                } else {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Max)
                }
            }
        }
    }
}