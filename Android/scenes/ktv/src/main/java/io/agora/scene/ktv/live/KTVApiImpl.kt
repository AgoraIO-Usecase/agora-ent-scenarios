package io.agora.scene.ktv.live

import io.agora.mediaplayer.Constants
import io.agora.musiccontentcenter.IAgoraMusicContentCenter
import io.agora.musiccontentcenter.IAgoraMusicPlayer
import io.agora.rtc2.RtcEngine
import io.agora.scene.ktv.widget.LrcControlView

class KTVApiImpl : KTVApi{


    override fun initWithRtcEngine(
        engine: RtcEngine,
        channelName: String,
        musicCenter: IAgoraMusicContentCenter,
        player: IAgoraMusicPlayer,
        streamId: Int,
        onPlayerStateChanged: (controller: KTVApi, songCode: Int, state: Constants.MediaPlayerState, isLocal: Boolean) -> Unit,
        onPlayerPositionChanged: (controller: KTVApi, songCode: Int, configuration: KTVSongConfiguration, position: Int, isLocal: Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun loadSong(
        songCode: Int,
        config: KTVSongConfiguration,
        onLoaded: (songCode: Int, lyricUrl: String, role: KTVSingRole, state: KTVLoadSongState) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun playSong(songCode: Int) {
        TODO("Not yet implemented")
    }

    override fun stopSong() {
        TODO("Not yet implemented")
    }

    override fun resumePlay() {
        TODO("Not yet implemented")
    }

    override fun pausePlay() {
        TODO("Not yet implemented")
    }

    override fun selectTrackMode(mode: KTVPlayerTrackMode) {
        TODO("Not yet implemented")
    }

    override fun setLycView(view: LrcControlView) {
        TODO("Not yet implemented")
    }
}