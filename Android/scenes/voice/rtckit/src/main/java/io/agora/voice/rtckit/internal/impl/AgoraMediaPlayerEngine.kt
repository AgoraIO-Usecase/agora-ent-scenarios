package io.agora.voice.rtckit.internal.impl

import io.agora.voice.buddy.config.ConfigConstants
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.mediaplayer.Constants.MediaPlayerError
import io.agora.mediaplayer.IMediaPlayer
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngineEx
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.rtckit.internal.base.BaseMediaPlayerEngine
import io.agora.voice.rtckit.middle.MediaPlayerObserver

/**
 * @author create by zhangwei03
 *
 * agora 播放器管理引擎,维护两个mediaPlayer，无缝播放
 */
internal class AgoraMediaPlayerEngine : BaseMediaPlayerEngine<RtcEngineEx>() {

    private var soundSpeakerType = ConfigConstants.BotSpeaker.BotBlue

    private var isFirst = true

    private val mediaPlayer: IMediaPlayer? by lazy {
        engine?.createMediaPlayer()?.apply {
            registerPlayerObserver(firstMediaPlayerObserver)
        }?.also {
            val options = ChannelMediaOptions()
            options.publishMediaPlayerAudioTrack = true
            options.publishMediaPlayerId = it.mediaPlayerId
            engine?.updateChannelMediaOptions(options)
        }
    }

    private val firstMediaPlayerObserver = object : MediaPlayerObserver() {
        override fun onPlayerStateChanged(state: MediaPlayerState?, error: MediaPlayerError?) {
            "firstMediaPlayerObserver onPlayerStateChanged state:$state error:$error".logE(TAG)
            if (state == MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                play()
            }
            if (state == MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED) {
                listener?.onMediaPlayerFinished()
            } else if (state == MediaPlayerState.PLAYER_STATE_PLAYING) {
                listener?.onMediaPlayerFinished(false, soundSpeakerType)
            }
        }
    }

    private fun isSuccess(code: Int?): Boolean {
        return code == Constants.ERR_OK
    }

    override fun adjustPlayoutVolume(volume: Int): Boolean {
        mediaPlayer?.adjustPlayoutVolume(volume)
        mediaPlayer?.adjustPublishSignalVolume(volume)
        return isSuccess(Constants.ERR_OK)
    }

    override fun open(url: String, startPos: Long, soundSpeakerType: Int): Boolean {
        val result = mediaPlayer?.open(url, startPos)
        this.soundSpeakerType = soundSpeakerType
        return isSuccess(result)
    }

    override fun play(): Boolean {
        val result = mediaPlayer?.play()
        return isSuccess(result)
    }

    override fun pause(): Boolean {
        val result = mediaPlayer?.pause()
        return isSuccess(result)
    }

    override fun stop(): Boolean {
        val result = mediaPlayer?.stop()
        return isSuccess(result)
    }

    override fun resume(): Boolean {
        val result = mediaPlayer?.resume()
        return isSuccess(result)
    }

    override fun reset(): Boolean {
        mediaPlayer?.stop()
        return isSuccess(Constants.ERR_OK)
    }

    override fun destroy(): Boolean {
        mediaPlayer?.apply {
            unRegisterPlayerObserver(firstMediaPlayerObserver)
            destroy()
        }
        return isSuccess(Constants.ERR_OK)
    }

    override fun detach() {
        isFirst = true
        destroy()
        super.detach()
    }
}