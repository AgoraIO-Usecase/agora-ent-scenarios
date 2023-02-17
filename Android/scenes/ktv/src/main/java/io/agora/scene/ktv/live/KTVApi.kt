package io.agora.scene.ktv.live

import io.agora.mediaplayer.Constants
import io.agora.musiccontentcenter.IAgoraMusicContentCenter
import io.agora.musiccontentcenter.IAgoraMusicPlayer
import io.agora.musiccontentcenter.Music
import io.agora.musiccontentcenter.MusicChartInfo
import io.agora.rtc2.RtcEngine
import io.agora.scene.ktv.widget.LrcControlView

enum class KTVSongType {
    KTVSongTypeSolo, KTVSongTypeChorus
}

enum class KTVSingRole {
    KTVSingRoleMainSinger, KTVSingRoleCoSinger, KTVSingRoleAudience
}

enum class KTVPlayerTrackMode {
    KTVPlayerTrackOrigin, KTVPlayerTrackAcc
}

enum class KTVLoadSongState {
    KTVLoadSongStateOK, KTVLoadSongStateInProgress, KTVLoadSongStateNoLyricUrl, KTVLoadSongStatePreloadFail, KTVLoadSongStateIdle
}


data class KTVSongConfiguration(
    val type: KTVSongType,
    val role: KTVSingRole,
    val songCode: Long,
    val mainSingerUid: Int,
    val coSingerUid: Int
)


interface KTVApi {

    interface KTVApiEventHandler {
        fun onPlayerStateChanged(controller: KTVApi, songCode: Long, state: Constants.MediaPlayerState, isLocal: Boolean)
        fun onSingingScoreResult(score: Float)
        fun onMusicCollectionResult(
            requestId: String?,
            status: Int,
            page: Int,
            pageSize: Int,
            total: Int,
            list: Array<out Music>?
        )
        fun onMusicChartsResult(
            requestId: String?,
            status: Int,
            list: Array<out MusicChartInfo>?
        )
    }

    /**
     * 初始化内部变量/缓存数据，并注册相应的监听
     */
    fun initWithRtcEngine(
        engine: RtcEngine,
        channelName: String,
        musicCenter: IAgoraMusicContentCenter,
        player: IAgoraMusicPlayer,
        streamId: Int,
        ktvApiEventHandler: KTVApiEventHandler
    )

    /**
     * 清空内部变量/缓存，取消在initWithRtcEngine时的监听，以及取消网络请求等
     */
    fun release()

    /**
     * 加载歌曲
     */
    fun loadSong(
        songCode: Long,
        config: KTVSongConfiguration,
        onLoaded: (songCode: Long, lyricUrl: String, role: KTVSingRole, state: KTVLoadSongState) -> Unit
    )

    /**
     * 播放歌曲
     */
    fun playSong(songCode: Long)

    /**
     * 停止播放歌曲
     */
    fun stopSong()

    /**
     * 恢复播放
     */
    fun resumePlay()

    /**
     * 暂停播放
     */
    fun pausePlay()

    /**
     * 调整进度
     */
    fun seek(time: Long)

    /**
     * 选择音轨，原唱、伴唱
     */
    fun selectTrackMode(mode: KTVPlayerTrackMode)

    /**
     * 设置歌词组件，在任意时机设置都可以生效
     */
    fun setLycView(view: LrcControlView)

    /**
     * 设置听到播放的所有音频的音量
     */
    fun adjustRemoteVolume(volume: Int)

    /**
     * 设置听到播放的所有音频的音量
     */
    fun setIsMicOpen(isOnMicOpen: Boolean)
}