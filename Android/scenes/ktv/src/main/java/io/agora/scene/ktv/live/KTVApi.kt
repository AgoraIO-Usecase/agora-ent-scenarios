package io.agora.scene.ktv.live

import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.musiccontentcenter.Music
import io.agora.musiccontentcenter.MusicChartInfo
import io.agora.rtc2.RtcEngine
import io.agora.scene.ktv.widget.LrcControlView

/**
 * SoloSinger 独唱
 * CoSinger 合唱者
 * Audience 观众
 * LeadSinger 主唱
 */
enum class KTVSingRole(val value: Int) {
    SoloSinger(0),
    CoSinger(1),
    LeadSinger(2),
    Audience(3)
}

/**
 * Origin 原唱
 * Acc 伴唱
 */
enum class KTVPlayerTrackMode(val value: Int) {
    Origin(0), Acc(1)
}

/**
 * KTVLoadSongStateOK 加载成功
 * KTVLoadSongStateInProgress 正在加载中
 * KTVLoadSongStateNoLyricUrl 没有歌词
 * KTVLoadSongStatePreloadFail Mcc 预加载歌曲失败
 *
 */
enum class KTVLoadSongState(val value: Int) {
    OK(0),
    FAILED(1),
    IN_PROGRESS(2),
    IDLE(3)
}

enum class KTVLoadSongFailReason(val value: Int) {
    NONE(0),
    NO_LYRIC_URL(1),
    MUSIC_PRELOAD_FAIL(2),
    MUSIC_PRELOAD_FAIL_AND_JOIN_CHANNEL_FAIL(3)
}


enum class SwitchRoleState(val value: Int) {
    SUCCESS(0),
    FAIL(1)
}


enum class SwitchRoleFailReason(val value: Int) {
    NONE(0),
    JOIN_CHANNEL_FAIL(1),
    MUSIC_PRELOAD_FAIL(2),
    MUSIC_PRELOAD_FAIL_AND_JOIN_CHANNEL_FAIL(3),
    NO_PERMISSION(4)
}

/**
 * SUCCESS 加入合唱成功
 * FAIL 加入合唱失败
 */
enum class KTVJoinChorusState(val value: Int) {
    SUCCESS(0),
    FAIL(1)
}

/**
 * MUSIC_PRELOAD_FAIL 歌曲预加载失败
 * MUSIC_OPEN_FAIL 歌曲 open 失败
 * JOIN_CHANNEL_FAIL 加入 RTC 频道失败
 */
enum class KTVJoinChorusFailReason(val value: Int) {
    NONE(0),
    MUSIC_PRELOAD_FAIL(1),
    MUSIC_OPEN_FAIL(2),
    JOIN_CHANNEL_FAIL(3),
}

interface OnMusicLoadStateListener {
    fun onMusicLoadSuccess(
        songCode: Long, lyricUrl: String
    )

    fun onMusicLoadFail(
        songCode: Long, lyricUrl: String, reason: KTVLoadSongFailReason
    )
}

interface OnJoinChorusStateListener {
    fun onJoinChorusSuccess()
    fun onJoinChorusFail(reason: KTVJoinChorusFailReason)
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
     *
     */
    open fun onSingingScoreResult(score: Float) {}

    open fun onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {}
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
)

/**
 * @param role 唱歌的角色
 * @param songCode 歌曲 id
 * @param mainSingerUid 主唱的 Uid，如果是伴唱，伴唱需要根据这个信息 mute 主频道主唱的声音
 */
data class KTVSongConfiguration(
    val autoPlay: Boolean,
    val songCode: Long,
    val mainSingerUid: Int
)


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
     * 获取歌曲榜单
     */
    fun fetchMusicCharts(onMusicChartResultListener: (requestId: String?, status: Int, list: Array<out MusicChartInfo>?) -> Unit)

    /**
     * 根据歌曲榜单类型搜索歌单
     */
    fun searchMusicByMusicChartId(
        musicChartId: Int,
        page: Int,
        pageSize: Int,
        jsonOption: String,
        onMusicCollectionResultListener: (requestId: String?,
                                          status: Int,
                                          page: Int,
                                          pageSize: Int,
                                          total: Int,
                                          list: Array<out Music>?) -> Unit
    )

    /**
     * 根据关键字搜索歌曲
     */
    fun searchMusicByKeyword(
        keyword: String,
        page: Int, pageSize: Int,
        jsonOption: String,
        onMusicCollectionResultListener: (requestId: String?,
                                          status: Int,
                                          page: Int,
                                          pageSize: Int,
                                          total: Int,
                                          list: Array<out Music>?) -> Unit
    )


    /**
     * 加载歌曲
     */
    fun loadMusic(config: KTVSongConfiguration, onMusicLoadStateListener: OnMusicLoadStateListener)

    /**
     * 切换演唱身份
     */
    fun switchSingerRole(newRole: KTVSingRole, token: String, onSwitchRoleState: (state: SwitchRoleState, reason: SwitchRoleFailReason) -> Unit)

    /**
     * 播放歌曲
     */
    fun startSing(startPos: Long)

    /**
     * 恢复播放
     */
    fun resumeSing()

    /**
     * 暂停播放
     */
    fun pauseSing()

    /**
     * 调整进度
     */
    fun seekSing(time: Long)

    /**
     * 设置歌词组件，在任意时机设置都可以生效
     */
    fun setLrcView(view: LrcControlView)

    /**
     * 设置当前mic开关状态
     * 目前关麦调用 adjustRecordSignalVolume(0) 后 onAudioVolumeIndication 仍然会执行， ktvApi需要增加一个变量判断当前是否关麦， 如果关麦把设置给歌词组件的pitch改为0
     */
    fun setMicStatus(isOnMicOpen: Boolean)

    /**
     * 获取mpk实例
     */
    fun getMediaPlayer(): IMediaPlayer
}