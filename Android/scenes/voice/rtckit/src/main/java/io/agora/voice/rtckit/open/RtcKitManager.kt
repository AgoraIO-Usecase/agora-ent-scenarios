package io.agora.voice.rtckit.open

import android.content.Context
import io.agora.voice.rtckit.internal.IRtcClientListener
import io.agora.voice.rtckit.open.config.RtcInitConfig
import io.agora.voice.rtckit.open.event.*
import io.agora.voice.rtckit.open.status.*
import io.agora.voice.rtckit.middle.RtcMiddleServiceImpl
import io.agora.voice.rtckit.middle.IRtcMiddleService
import io.agora.voice.rtckit.open.config.RtcChannelConfig

/**
 * @author create by zhangwei03
 *
 */
class RtcKitManager {

    private lateinit var initConfig: RtcInitConfig
    private var middleService: IRtcMiddleService? = null

    companion object {

        fun initRTC(context: Context, initConfig: RtcInitConfig, rtcKitListener: IRtcKitListener): RtcKitManager {
            val rtcKitManager = RtcKitManager().apply {
                this.initConfig = initConfig
                this.middleService = RtcMiddleServiceImpl(context, initConfig, object : IRtcClientListener {
                    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                        rtcKitListener.onJoinChannelSuccess(channel, uid, elapsed)
                    }

                    override fun onLeaveChannel() {
                        rtcKitListener.onLeaveChannel()
                    }

                    override fun onConnectionStateChanged(state: Int, reason: Int) {
                    }

                    override fun onUserJoined(userId: Int, joined: Boolean) {
                        if (joined) {
                            rtcKitListener.onUserJoin(userId)
                        } else {
                            rtcKitListener.onLeaveChannel(userId)
                        }
                    }

                    override fun onNetworkStatus(netWorkStatus: RtcNetWorkStatus) {
                        rtcKitListener.onNetworkStatus(netWorkStatus)
                    }

                    override fun onAudioStatus(audioChangeStatus: RtcAudioChangeStatus) {
                        rtcKitListener.onAudioStatus(audioChangeStatus)
                    }

                    override fun onAudioMixingFinished(soundId: Int, finished: Boolean, speakerType: Int) {
                        rtcKitListener.onAudioMixingFinished(soundId, finished, speakerType)
                    }

                    override fun onMediaPlayerFinished(finished: Boolean, speakerType: Int) {
                        rtcKitListener.onMediaPlayerFinished(finished, speakerType)
                    }

                    override fun onError(rtcErrorStatus: RtcErrorStatus) {
                        rtcKitListener.onError(rtcErrorStatus)
                    }

                    override fun onAudioVolumeIndication(volumeIndicationStatus: RtcAudioVolumeIndicationStatus) {
                        rtcKitListener.onAudioVolumeIndication(volumeIndicationStatus)
                    }
                })
            }
            return rtcKitManager
        }
    }

    fun joinChannel(channelConfig: RtcChannelConfig) {
        middleService?.joinChannel(channelConfig)
    }

    fun leaveChannel() {
        middleService?.leaveChannel()
    }

    fun switchRole(broadcaster: Boolean) {
        middleService?.switchRole(broadcaster)
    }

    fun operateAudio(audioEvent: RtcAudioEvent) {
        middleService?.onAudioEvent(audioEvent)
    }

    fun operateSoundEffect(soundEffect: RtcSoundEffectEvent) {
        middleService?.onSoundEffectEvent(soundEffect)
    }

    fun operateDeNoise(deNoiseEvent: RtcDeNoiseEvent, callback: IRtcValueCallback<Boolean>? = null) {
        middleService?.onDeNoiseEvent(deNoiseEvent, callback)
    }

    fun operateSpatialAudio(spatialAudioEvent: RtcSpatialAudioEvent) {
        middleService?.onSpatialAudioEvent(spatialAudioEvent)
    }

    fun operateMediaPlayer(mediaPlayerEvent: MediaPlayerEvent){
        middleService?.onMediaPlayer(mediaPlayerEvent)
    }

    fun destroy() {
        middleService?.destroy()
        middleService = null
    }
}