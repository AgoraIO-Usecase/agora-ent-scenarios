package io.agora.scene.aichat.chat.logic

import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.CacheStatistics
import io.agora.mediaplayer.data.PlayerPlaybackStats
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo

open class AIMediaPlayerObserver:IMediaPlayerObserver {
    override fun onPlayerStateChanged(state: Constants.MediaPlayerState?, reason: Constants.MediaPlayerReason?) {

    }

    override fun onPositionChanged(positionMs: Long, timestampMs: Long) {
    }

    override fun onPlayerEvent(eventCode: Constants.MediaPlayerEvent?, elapsedTime: Long, message: String?) {
    }

    override fun onMetaData(type: Constants.MediaPlayerMetadataType?, data: ByteArray?) {
    }

    override fun onPlayBufferUpdated(playCachedBuffer: Long) {
    }

    override fun onPreloadEvent(src: String?, event: Constants.MediaPlayerPreloadEvent?) {
    }

    override fun onAgoraCDNTokenWillExpire() {
    }

    override fun onPlayerSrcInfoChanged(from: SrcInfo?, to: SrcInfo?) {
    }

    override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo?) {
    }

    override fun onPlayerCacheStats(stats: CacheStatistics?) {
    }

    override fun onPlayerPlaybackStats(stats: PlayerPlaybackStats?) {
    }

    override fun onAudioVolumeIndication(volume: Int) {
    }
}