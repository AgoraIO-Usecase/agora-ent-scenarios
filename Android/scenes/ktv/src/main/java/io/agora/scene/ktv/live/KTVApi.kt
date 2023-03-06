package io.agora.scene.ktv.live

import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.musiccontentcenter.IAgoraMusicContentCenter
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
        state: Constants.MediaPlayerState, error: Constants.MediaPlayerError, isLocal: Boolean
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

    open fun onSyncMusicPosition(position: Float) {}

    open fun onMusicLoaded(
        songCode: Long,
        lyricUrl: String,
        role: KTVSingRole,
        state: KTVLoadSongState
    ) {
    }

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
    val appId: String,
    val rtmToken: String,
    val engine: RtcEngine,
    val channelName: String,
    val dataStreamId: Int,
    val localUid: Int,
    val ktvApiEventHandler: KTVApiEventHandler,
    val defaultMediaPlayerVolume: Int = 50,
    val defaultChorusRemoteUserVolume: Int = 15
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
     * 订阅KTVApi事件
     */
    fun addEventHandler(ktvApiEventHandler: KTVApiEventHandler)

    /**
     * 取消订阅KTVApi事件
     */
    fun removeEventHandler(ktvApiEventHandler: KTVApiEventHandler)

    /**
     * 清空内部变量/缓存，取消在initWithRtcEngine时的监听，以及取消网络请求等
     */
    fun release()

    /**
     * 获取歌曲类型
     */
    fun getMusicCharts(): String

    /**
     * 根据歌曲类型拉取歌单
     */
    fun getMusicCollectionByMusicChartId(
        musicChartId: Int,
        page: Int,
        pageSize: Int,
        jsonOption: String
    ): String

    /**
     * 搜索歌曲
     */
    fun searchMusic(keyword: String, page: Int, pageSize: Int, jsonOption: String): String

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
    fun joinChorus(token: String, role: KTVSingRole)

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
     * 设置当前mic开关状态
     */
    fun setIsMicOpen(isOnMicOpen: Boolean)

    /**
     * 获取mpk实例
     */
    fun getMediaPlayer(): IMediaPlayer

    /**
     * 获取mcc实例
     */
    fun getMusicCenter(): IAgoraMusicContentCenter
}