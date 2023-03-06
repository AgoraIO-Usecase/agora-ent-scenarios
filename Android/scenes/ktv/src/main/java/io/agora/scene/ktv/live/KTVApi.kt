package io.agora.scene.ktv.live

import io.agora.mediaplayer.Constants
import io.agora.musiccontentcenter.IAgoraMusicContentCenter
import io.agora.musiccontentcenter.IAgoraMusicPlayer
import io.agora.musiccontentcenter.Music
import io.agora.musiccontentcenter.MusicChartInfo
import io.agora.rtc2.RtcEngine
import io.agora.scene.ktv.widget.LrcControlView

enum class KTVSingRole {
    KTVSingRoleMainSinger, KTVSingRoleCoSinger, KTVSingRoleFollowSinger, KTVSingRoleAudience
}

enum class KTVPlayerTrackMode {
    KTVPlayerTrackOrigin, KTVPlayerTrackAcc
}

enum class KTVLoadSongState {
    KTVLoadSongStateOK, KTVLoadSongStateInProgress, KTVLoadSongStateNoLyricUrl, KTVLoadSongStatePreloadFail, KTVLoadSongStateIdle
}

enum class JOIN_STATES(val value: Int) {
    SUCCESS(0), FAIL(1)
};

enum class REASON_TYPE(val value: Int) {
    MUSIC_PRELOAD_FAIL(0), MUSIC_OPEN_FAIL(1), JOIN_CHANNEL_FAIL(2)
}

abstract class KTVApiEventHandler {
    open fun onPlayerStateChanged(
        songCode: Long, state: Constants.MediaPlayerState, isLocal: Boolean
    ) {
    }

    open fun onMusicCollectionResult(
        requestId: String?,
        status: Int,
        page: Int,
        pageSize: Int,
        total: Int,
        list: Array<out Music>?
    ) {
    }

    open fun onMusicChartsResult(
        requestId: String?, status: Int, list: Array<out MusicChartInfo>?
    ) {
    }

    /**
     * 用来同步歌词进度的回调
     * mainSinger 和 coSinger:
     *  通过本地播放的 MPK position 获取到进度
     *
     *
     * 观众：
     * 1：
     *
     */
    open fun onSyncMusicPosition(position: Float) {}

    open fun onMusicLoaded(songCode: Long, lyricUrl: String, role: KTVSingRole, state: KTVLoadSongState) {}

    open fun onJoinChorusStates(status: JOIN_STATES, reason: REASON_TYPE) {}

    open fun onSingingScoreResult(score: Float) {}
}

/**
 * @param engine, RtcEngine instance
 * @param channelName, Your rtc channel
 * @param streamId, Datastream id,create by createDataStream
 * @param localUid
 */

data class KTVApiConfig(
    val engine: RtcEngine,
    val channelName: String,
    val streamId: Int,
    val localUid: Int,
    val musicCenter: IAgoraMusicContentCenter,
    val player: IAgoraMusicPlayer,
    val ktvApiEventHandler: KTVApiEventHandler,
);


data class KTVSongConfiguration(
    val role: KTVSingRole,
    val songCode: Long,
    val mainSingerUid: Int,
);


interface KTVApi {
    /**
     * 初始化内部变量/缓存数据，并注册相应的监听
     * @param config
     */
    fun initialize(config: KTVApiConfig)

    /**
     * 清空内部变量/缓存，取消在initWithRtcEngine时的监听，以及取消网络请求等
     */
    fun release()

    /**
     * 加载歌曲
     */
    fun loadSong(
        config: KTVSongConfiguration
    )

    /**
     * 播放歌曲
     */
    fun startSing(startPos: Long)

    /**
     * 加入合唱
     */
    fun joinChorus(token: String, config: KTVSongConfiguration)

    /**
     * 离开合唱
     */
    fun leaveChorus()

    /**
     * 停止播放歌曲
     */
    fun stopSing()

    /**
     * 恢复播放
     */
    fun resumePlayer()

    /**
     * 暂停播放
     */
    fun pausePlayer()

    /**
     * 调整进度
     */
    fun seekPlayer(time: Long)

    /**
     * 选择音轨，原唱、伴唱
     */
    fun selectPlayerTrackMode(mode: KTVPlayerTrackMode)

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