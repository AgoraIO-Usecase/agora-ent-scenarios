package io.agora.voice.rtckit.middle

import android.content.Context
import io.agora.rtc2.RtcEngineEx
import io.agora.voice.rtckit.open.config.RtcInitConfig
import io.agora.voice.rtckit.internal.RtcBaseClientEx
import io.agora.voice.rtckit.open.event.*
import io.agora.voice.rtckit.internal.AgoraRtcClientEx
import io.agora.voice.rtckit.internal.IRtcClientListener
import io.agora.voice.rtckit.open.IRtcValueCallback
import io.agora.voice.rtckit.open.config.RtcChannelConfig

/**
 * @author create by zhangwei03
 *
 */
class RtcMiddleServiceImpl constructor(
    context: Context, config: RtcInitConfig, rtcClientListener: IRtcClientListener
) : IRtcMiddleService {

    private val rtcClient: RtcBaseClientEx<RtcEngineEx> by lazy {
        AgoraRtcClientEx()
    }

    init {
        rtcClient.createClient(context, config, rtcClientListener)
    }

    override fun joinChannel(config: RtcChannelConfig) {
        rtcClient.joinChannel(config)
    }

    override fun leaveChannel() {
        rtcClient.leaveChannel()
    }

    override fun switchRole(broadcaster: Boolean) {
        rtcClient.switchRole(broadcaster)
    }

    override fun onAudioEvent(audioEvent: RtcAudioEvent) {
        rtcClient.getAudioEngine()?.apply {
            when (audioEvent) {
                is RtcAudioEvent.AudioEnable -> enableLocalAudio(audioEvent.enabled)
                is RtcAudioEvent.AudioMuteLocal -> muteLocalAudio(audioEvent.mute)
                is RtcAudioEvent.AudioMuteRemote -> muteRemoteAudio(audioEvent.userId, audioEvent.mute)
                is RtcAudioEvent.AudioMuteAll -> muteRemoteAllAudio(audioEvent.mute)
            }
        }
    }

    override fun onSoundEffectEvent(soundEffect: RtcSoundEffectEvent) {
        rtcClient.getSoundEffectEngine()?.apply {
            when (soundEffect) {
                is RtcSoundEffectEvent.PlayEffectEvent -> playEffect(
                    soundEffect.soundId,
                    soundEffect.filePath,
                    soundEffect.loopback,
                    soundEffect.cycle,
                    soundEffect.soundSpeaker
                )
                is RtcSoundEffectEvent.StopEffectEvent -> stopEffect(soundEffect.soundId)
                is RtcSoundEffectEvent.PauseEffectEvent -> pauseEffect(soundEffect.soundId)
                is RtcSoundEffectEvent.ResumeEffectEvent -> resumeEffect(soundEffect.soundId)
                is RtcSoundEffectEvent.StopAllEffectEvent -> stopAllEffect()
                is RtcSoundEffectEvent.UpdateAudioEffectEvent -> updateEffectVolume(soundEffect.volume)
            }
        }
    }

    override fun onMediaPlayer(mediaPlayerEvent: MediaPlayerEvent) {
        rtcClient.getMediaPlayerEngine()?.apply {
            when (mediaPlayerEvent) {
                is MediaPlayerEvent.OpenEvent -> open(
                    mediaPlayerEvent.url,
                    mediaPlayerEvent.startPos,
                    mediaPlayerEvent.soundSpeaker
                )
                is MediaPlayerEvent.PlayEvent -> play()
                is MediaPlayerEvent.PauseEvent -> pause()
                is MediaPlayerEvent.StopEvent -> stop()
                is MediaPlayerEvent.ResumeEvent -> resume()
                is MediaPlayerEvent.ResetEvent -> reset()
                is MediaPlayerEvent.AdjustPlayoutVolumeEvent -> adjustPlayoutVolume(mediaPlayerEvent.volume)
            }
        }
    }

    override fun onDeNoiseEvent(deNoiseEvent: RtcDeNoiseEvent, callback: IRtcValueCallback<Boolean>?) {
        rtcClient.getDeNoiseEngine()?.apply {
            val result = when (deNoiseEvent) {
                is RtcDeNoiseEvent.CloseEvent -> closeDeNoise()
                is RtcDeNoiseEvent.MediumEvent -> openMediumDeNoise()
                is RtcDeNoiseEvent.HighEvent -> openHeightDeNoise()
            }
            callback?.onSuccess(result)
        }
    }

    override fun onSpatialAudioEvent(spatialAudioEvent: RtcSpatialAudioEvent) {
    }

    override fun destroy() {
        rtcClient.destroy()
    }
}