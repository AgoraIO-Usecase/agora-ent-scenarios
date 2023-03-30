package io.agora.scene.ktv.live

import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.musiccontentcenter.IAgoraMusicContentCenter
import io.agora.musiccontentcenter.Music
import io.agora.musiccontentcenter.MusicChartInfo
import io.agora.rtc2.RtcEngine

/**
 * 在KTVApi中的身份
 * @param SoloSinger 独唱者: 当前只有自己在唱歌
 * @param CoSinger 合唱者: 加入合唱需要通过调用switchSingerRole将切换身份成合唱
 * @param LeadSinger 主唱: 有合唱者加入后，需要通过调用switchSingerRole切换身份成主唱
 * @param Audience 观众: 默认状态
 */
enum class KTVSingRole(val value: Int) {
    SoloSinger(0),
    CoSinger(1),
    LeadSinger(2),
    Audience(3)
}

/**
 * loadSong失败的原因
 * @param NO_LYRIC_URL 没有歌词，不影响音乐正常播放
 * @param MUSIC_PRELOAD_FAIL 音乐加载失败
 * @param CANCELED 本次加载已终止
 */
enum class KTVLoadSongFailReason(val value: Int) {
    NO_LYRIC_URL(0),
    MUSIC_PRELOAD_FAIL(1),
    CANCELED(2)
}

/**
 * switchSingerRole的失败的原因
 * @param JOIN_CHANNEL_FAIL 加入channel2失败
 * @param NO_PERMISSION switchSingerRole传入了错误的目标角色（不能从当前角色切换到目标角色）
 */
enum class SwitchRoleFailReason(val value: Int) {
    JOIN_CHANNEL_FAIL(0),
    NO_PERMISSION(1)
}

/**
 * 加载音乐的模式
 * @param LOAD_MUSIC_ONLY 只加载音乐（通常加入合唱前使用此模式）
 * @param LOAD_LRC_ONLY 只加载歌词（通常歌曲开始播放时观众使用此模式）
 * @param LOAD_MUSIC_AND_LRC 默认模式，加载歌词和音乐（通常歌曲开始播放时主唱使用此模式）
 */
enum class KTVLoadMusicMode(val value: Int) {
    LOAD_MUSIC_ONLY(0),
    LOAD_LRC_ONLY(1),
    LOAD_MUSIC_AND_LRC(2)
}

/**
 * 歌词组件接口，您setLrcView传入的歌词组件需要继承此接口类，并实现以下三个方法
 */
interface ILrcView {
    /**
     * ktvApi内部更新音高pitch时会主动调用此方法将pitch值传给你的歌词组件
     * @param pitch 音高值
     */
    fun onUpdatePitch(pitch: Float?)

    /**
     * ktvApi内部更新音乐播放进度progress时会主动调用此方法将进度值progress传给你的歌词组件，50ms回调一次
     * @param progress 歌曲播放的真实进度 50ms回调一次
     */
    fun onUpdateProgress(progress: Long?)

    /**
     * ktvApi获取到歌词地址时会主动调用此方法将歌词地址url传给你的歌词组件，您需要在这个回调内完成歌词的下载
     */
    fun onDownloadLrcData(url: String?)
}

enum class MusicLoadStatus(val value: Int) {
    COMPLETED(0),
    FAILED(1),
    INPROGRESS(2),
}

/**
 * 音乐加载状态接口
 */
interface OnMusicLoadStateListener {
    /**
     * 音乐加载成功
     * @param songCode 歌曲编码， 和你loadMusic传入的songCode一致
     * @param lyricUrl 歌词地址
     */
    fun onMusicLoadSuccess(songCode: Long, lyricUrl: String)

    /**
     * 音乐加载失败
     * @param reason 歌曲加载失败的原因
     */
    fun onMusicLoadFail(songCode: Long, reason: KTVLoadSongFailReason)

    /**
     * 音乐加载进度
     * @param songCode 歌曲编码
     * @param percent 歌曲加载进度
     * @param status 歌曲加载的状态
     * @param msg
     * @param lyricUrl
     */
    fun onMusicLoadProgress(songCode: Long, percent: Int, status: MusicLoadStatus, msg: String?, lyricUrl: String?)
}

/**
 * 切换演唱角色状态接口
 */
interface OnSwitchRoleStateListener {
    /**
     * 切换演唱角色成功
     */
    fun onSwitchRoleSuccess()

    /**
     * 切换演唱角色失败
     * @param reason 切换演唱角色失败的原因
     */
    fun onSwitchRoleFail(reason: SwitchRoleFailReason)
}

/**
 * KTVApi事件回调
 */
abstract class IKTVApiEventHandler {
    /**
     * 播放器状态变化
     * @param state MediaPlayer 播放状态
     * @param error MediaPlayer Error 信息
     * @param isLocal 本地还是主唱端的 Player 信息
     */
    open fun onMusicPlayerStateChanged(
        state: Constants.MediaPlayerState, error: Constants.MediaPlayerError, isLocal: Boolean
    ) {
    }

    /**
     * 歌曲播放结束分数
     * @param score 歌曲结束的总分
     */
    open fun onSingingScoreResult(score: Float) {}

    /**
     * ktvApi内部角色切换
     * @param oldRole 老角色
     * @param newRole 新角色
     */
    open fun onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {}

    open fun onChorusChannelTokenPrivilegeWillExpire(token: String?) {}
}

/**
 * 初始化KTVApi的配置
 * @param appId 用来初始化 Mcc Engine
 * @param rtmToken 创建 Mcc Engine 需要
 * @param engine RTC engine 对象
 * @param channelName 频道号，子频道名以基于主频道名 + "_ex" 固定规则生成频道号
 * Player 状态同步
 * 打分结果同步
 * 歌词同步
 * pitch同步
 * 建议你为KTVApi单独创建一个新的dataStreamId
 * @param localUid 创建 Mcc engine 和 加入子频道需要用到
 * @param chorusChannelName 子频道名 加入子频道需要用到
 * @param chorusChannelToken 子频道token 加入子频道需要用到
 */
data class KTVApiConfig(
    val appId: String,
    val rtmToken: String,
    val engine: RtcEngine,
    val channelName: String,
    val localUid: Int,
    val chorusChannelName: String,
    val chorusChannelToken: String
)

/**
 * 加载歌曲的配置，不允许在一首歌没有load完成前（成功/失败均算完成）进行下一首歌的加载
 * @param autoPlay 是否自动播放歌曲（通常主唱选择true）默认为false
 * @param mode 歌曲加载的模式， 默认为音乐和歌词均加载
 * @param songCode 歌曲 id
 * @param mainSingerUid 主唱的 Uid，如果是伴唱，伴唱需要根据这个信息 mute 主频道主唱的声音
 */
data class KTVLoadMusicConfiguration(
    val autoPlay: Boolean = false,
    val songCode: Long,
    val mainSingerUid: Int,
    val mode: KTVLoadMusicMode = KTVLoadMusicMode.LOAD_MUSIC_AND_LRC
)

interface KTVApi {
    /**
     * 初始化内部变量/缓存数据，并注册相应的监听，必须在其他KTVApi调用前调用initialize初始化KTVApi
     * @param config
     */
    fun initialize(config: KTVApiConfig)

    /**
     * 订阅KTVApi事件, 支持多注册
     * @param ktvApiEventHandler KTVApi事件接口实例
     */
    fun addEventHandler(ktvApiEventHandler: IKTVApiEventHandler)

    /**
     * 取消订阅KTVApi事件
     * @param ktvApiEventHandler KTVApi事件接口实例
     */
    fun removeEventHandler(ktvApiEventHandler: IKTVApiEventHandler)

    /**
     * 清空内部变量/缓存，取消在initWithRtcEngine时的监听，以及取消网络请求等
     */
    fun release()

    /**
     * 获取歌曲榜单
     * @param onMusicChartResultListener 榜单列表回调
     */
    fun fetchMusicCharts(
        onMusicChartResultListener: (
            requestId: String?, // TODO 不需要？
            status: Int,        // status=2 时token过期
            list: Array<out MusicChartInfo>?
        ) -> Unit
    )

    /**
     * 根据歌曲榜单类型获取歌单
     * @param musicChartId 榜单id
     * @param page 歌曲列表回调
     * @param pageSize 歌曲列表回调
     * @param jsonOption 自定义过滤模式
     * @param onMusicCollectionResultListener 歌曲列表回调
     */
    fun searchMusicByMusicChartId(
        musicChartId: Int,
        page: Int,
        pageSize: Int,
        jsonOption: String,
        onMusicCollectionResultListener: (
            requestId: String?,  // TODO 不需要？
            status: Int,         // status=2 时token过期
            page: Int,
            pageSize: Int,
            total: Int,
            list: Array<out Music>?
        ) -> Unit
    )

    /**
     * 根据关键字搜索歌曲
     * @param keyword 关键字
     * @param page 歌曲列表回调
     * @param jsonOption 自定义过滤模式
     * @param onMusicCollectionResultListener 歌曲列表回调
     */
    fun searchMusicByKeyword(
        keyword: String,
        page: Int, pageSize: Int,
        jsonOption: String,
        onMusicCollectionResultListener: (
            requestId: String?,  // TODO 不需要？
            status: Int,         // status=2 时token过期
            page: Int,
            pageSize: Int,
            total: Int,
            list: Array<out Music>?
        ) -> Unit
    )

    /**
     * 异步加载歌曲，同时只能为一首歌loadSong，loadSong结果会通过回调通知业务层
     * @param config 加载歌曲配置
     * @param onMusicLoadStateListener 加载歌曲结果回调
     *
     * 推荐调用：
     * 歌曲开始时：
     * 主唱 loadMusic(KTVLoadMusicConfiguration(autoPlay=true, mode=LOAD_MUSIC_AND_LRC, songCode, mainSingerUid)) switchSingerRole(SoloSinger)
     * 观众 loadMusic(KTVLoadMusicConfiguration(autoPlay=false, mode=LOAD_LRC_ONLY, songCode, mainSingerUid))
     * 加入合唱时：
     * 准备加入合唱者：loadMusic(KTVLoadMusicConfiguration(autoPlay=false, mode=LOAD_MUSIC_ONLY, songCode, mainSingerUid))
     * loadMusic成功后switchSingerRole(CoSinger)
     */
    fun loadMusic(
        config: KTVLoadMusicConfiguration,
        onMusicLoadStateListener: OnMusicLoadStateListener
    )

    /**
     * 异步切换演唱身份，结果会通过回调通知业务层
     * @param newRole 新演唱身份
     * @param onSwitchRoleState 切换演唱身份结果
     *
     * 允许的调用路径：
     * 1、Audience -》SoloSinger 自己点的歌播放时
     * 2、Audience -》LeadSinger 自己点的歌播放时， 且歌曲开始时就有合唱者加入
     * 3、SoloSinger -》Audience 独唱结束时
     * 4、Audience -》CoSinger   加入合唱时
     * 5、CoSinger -》Audience   退出合唱时
     * 6、SoloSinger -》LeadSinger 当前第一个合唱者加入合唱时，主唱由独唱切换成领唱
     * 7、LeadSinger -》SoloSinger 最后一个合唱者退出合唱时，主唱由领唱切换成独唱
     * 8、LeadSinger -》Audience 以领唱的身份结束歌曲时
     */
    fun switchSingerRole(
        newRole: KTVSingRole,
        onSwitchRoleStateListener: OnSwitchRoleStateListener?
    )

    /**
     * 播放歌曲
     * @param startPos 开始播放的位置
     * 对于主唱：
     * 如果loadMusic时你选择了autoPlay = true 则不需要主动调用startSing
     * 如果loadMusic时你选择了autoPlay = false 则需要在loadMusic成功后调用startSing
     * TODO 如何支持URL
     */
    fun startSing(songCode: Long, startPos: Long)

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
     * @param view 传入的歌词组件view， 需要继承ILrcView并实现ILrcView的三个接口
     */
    fun setLrcView(view: ILrcView)

    /**
     * 设置当前mic开关状态
     * 目前关麦调用 adjustRecordSignalVolume(0) 后 onAudioVolumeIndication 仍然会执行， ktvApi需要增加一个变量判断当前是否关麦， 如果关麦把设置给歌词组件的pitch改为0
     */
    fun setMicStatus(isOnMicOpen: Boolean)

    /**
     * 获取mpk实例
     */
    fun getMediaPlayer() : IMediaPlayer

    /**
     * 获取mcc实例
     */
    fun getMusicCenter() : IAgoraMusicContentCenter
}

// 0、注释换成Print 不要有AgoraPrint
// 1、token 和 2ndChannelName 传递方式和业务逻辑调用
// 2、主动暴露 getMusicCenter() 为了renewToken
// 3、onChorusChannelTokenPrivilegeWillExpire