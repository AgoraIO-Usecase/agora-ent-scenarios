package io.agora.scene.ktv.live

import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.musiccontentcenter.IAgoraMusicContentCenter
import io.agora.musiccontentcenter.Music
import io.agora.musiccontentcenter.MusicChartInfo
import io.agora.rtc2.RtcEngine
import io.agora.scene.ktv.widget.LrcControlView

/**
 * KTVSingRoleMainSinger 主唱
 * KTVSingRoleCoSinger 合唱者
 * KTVSingRoleFollowSinger 跟唱者
 * KTVSingRoleAudience 观众
 */
enum class KTVSingRole(val value: Int) {
    KTVSingRoleMainSinger(0), KTVSingRoleCoSinger(1), KTVSingRoleFollowSinger(2), KTVSingRoleAudience(3)
}

/**
 * KTVPlayerTrackOrigin 原唱
 * KTVPlayerTrackAcc 伴唱
 */
enum class KTVPlayerTrackMode(val value: Int) {
    KTVPlayerTrackOrigin(0), KTVPlayerTrackAcc(1)
}

/**
 * KTVLoadSongStateOK 加载成功
 * KTVLoadSongStateInProgress 正在加载中
 * KTVLoadSongStateNoLyricUrl 没有歌词
 * KTVLoadSongStatePreloadFail Mcc 预加载歌曲失败
 *
 */
enum class KTVLoadSongState(val value: Int) {
    KTVLoadSongStateOK(0), KTVLoadSongStateInProgress(1), KTVLoadSongStateNoLyricUrl(2), KTVLoadSongStatePreloadFail(3), KTVLoadSongStateIdle(4)
}

/**
 * SUCCESS 加入合唱成功
 * FAIL 加入合唱失败
 */
enum class KTVJoinState(val value: Int) {
    SUCCESS(0), FAIL(1)
};

/**
 * MUSIC_PRELOAD_FAIL 歌曲预加载失败
 * MUSIC_OPEN_FAIL 歌曲 open 失败
 * JOIN_CHANNEL_FAIL 加入 RTC 频道失败
 */
enum class KTVJoinFailReasonType(val value: Int) {
    MUSIC_PRELOAD_FAIL(0), MUSIC_OPEN_FAIL(1), JOIN_CHANNEL_FAIL(2)
}

abstract class IKTVApiEventHandler {
    /**
     * @param state MediaPlayer 播放状态
     * @param error MediaPlayer Error 信息
     * @param isLocal 本地还是主唱端的 Player 信息
     */
    open fun onMusicPlayerStateChanged(
        state: Constants.MediaPlayerState, error: Constants.MediaPlayerError, isLocal: Boolean
    ) {
    }

    /**
     * Mcc 歌曲列表信息
     */
    open fun onMusicCollectionResult(
        requestId: String?,
        status: Int,
        page: Int,
        pageSize: Int,
        total: Int,
        list: Array<out Music>?
    ) {
    }

    /**
     * Mcc 榜单信息
     */
    open fun onMusicChartsResult(
        requestId: String?, status: Int, list: Array<out MusicChartInfo>?
    ) {
    }

    /**
     * 歌曲加载状态信息
     */
    open fun onMusicLoadStateChanged(
        songCode: Long,
        lyricUrl: String,
        role: KTVSingRole,
        state: KTVLoadSongState
    ) {
    }

    /**
     * 加入合唱状态信息
     */
    open fun onJoinChorusStateChanged(status: KTVJoinState, reason: KTVJoinFailReasonType) {}

    /**
     *
     */
    open fun onSingingScoreResult(score: Float) {}
}

/**
 * @param appId 用来初始化 Mcc Engine
 * @param rtmToken 创建 Mcc Engine 需要
 * @param engine RTC engine 对象
 * @param channelName 频道号，子频道名以基于主频道名 + "_ex" 固定规则生成频道号
 * @param dataStreamId 数据流传输通道，用来：
 * Player 状态同步
 * 打分结果同步
 * 歌词同步
 * @param localUid 创建 Mcc engine 和 加入子频道需要用到
 * @param defaultMediaPlayerVolume 调整本地 MPK Playout 以及 publish 的音量值
 */

data class KTVApiConfig(
    val appId: String,
    val rtmToken: String,
    val engine: RtcEngine,
    val channelName: String,
    val dataStreamId: Int,
    val localUid: Int
);

/**
 * @param role 唱歌的角色
 * @param songCode 歌曲 id
 * @param mainSingerUid 主唱的 Uid，如果是伴唱，伴唱需要根据这个信息 mute 主频道主唱的声音
 */
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
    fun addEventHandler(ktvApiEventHandler: IKTVApiEventHandler)

    /**
     * 取消订阅KTVApi事件
     */
    fun removeEventHandler(ktvApiEventHandler: IKTVApiEventHandler)

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
     * 停止播放歌曲
     */
    fun stopSing()

    /**
     * 加入合唱
     */
    fun joinChorus(token: String, role: KTVSingRole)

    /**
     * 离开合唱
     */
    fun leaveChorus()

    /**
     * 恢复播放
     */
    fun resumeMusicPlayer()

    /**
     * 暂停播放
     */
    fun pauseMusicPlayer()

    /**
     * 调整进度
     */
    fun seekMusicPlayer(time: Long)

    /**
     * 调整音乐本地播放的声音 （主唱&&伴唱都可以调节）
     */
    fun adjustMusicPlayerPlayoutVolume(volume: Int)

    /**
     * 调整音乐推送到远端的声音大小 （主唱调整）
     */
    fun adjustMusicPlayerPublishVolume(volume: Int)

    /**
     * 调整本地播放远端伴唱人声音量的大小（主唱 && 伴唱都可以调整）
     * 观众调整的是远端所有音乐 + 人声的音量大小
     */
    fun adjustPlaybackVolume(volume: Int)
    /**
     * 选择音轨，原唱、伴唱
     */
    fun selectPlayerTrackMode(mode: KTVPlayerTrackMode)

    /**
     * 设置歌词组件，在任意时机设置都可以生效
     */
    fun setLycView(view: LrcControlView)

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