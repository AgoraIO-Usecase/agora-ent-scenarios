package io.agora.scene.ktv.ktvapi

import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.musiccontentcenter.IAgoraMusicContentCenter
import io.agora.musiccontentcenter.Music
import io.agora.musiccontentcenter.MusicChartInfo
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

/**
 * KTV场景类型
 * @param Normal 普通独唱或多人合唱
 * @param SingBattle 嗨歌抢唱
 * @param SingRelay 抢麦接唱
 */
enum class KTVType(val value: Int)  {
    Normal(0),
    SingBattle(1),
    SingRelay(2)
}

/**
 * KTV歌曲类型
 * @param SONG_CODE mcc版权歌单songCode
 * @param SONG_URL 本地歌曲地址url
 */
enum class KTVMusicType(val value: Int) {
    SONG_CODE(0),
    SONG_URL(1)
}

/**
 * 在KTVApi中的身份
 * @param SoloSinger 独唱者: 当前只有自己在唱歌
 * @param CoSinger 伴唱: 加入合唱需要通过调用switchSingerRole将切换身份成合唱
 * @param LeadSinger 主唱: 有合唱者加入后，需要通过调用switchSingerRole切换身份成主唱
 * @param Audience 听众: 默认状态
 */
enum class KTVSingRole(val value: Int) {
    SoloSinger(0),
    CoSinger(1),
    LeadSinger(2),
    Audience(3)
}

/**
 * loadMusic失败的原因
 * @param NO_LYRIC_URL 没有歌词，不影响音乐正常播放
 * @param MUSIC_PRELOAD_FAIL 音乐加载失败
 * @param CANCELED 本次加载已终止
 */
enum class KTVLoadMusicFailReason(val value: Int) {
    NO_LYRIC_URL(0),
    MUSIC_PRELOAD_FAIL(1),
    CANCELED(2),
    GET_SIMPLE_INFO_FAIL(3)
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
 * 加入合唱错误原因
 * @param JOIN_CHANNEL_FAIL 加入合唱子频道失败
 * @param MUSIC_OPEN_FAIL 歌曲open失败
 */
enum class KTVJoinChorusFailReason(val value: Int) {
    JOIN_CHANNEL_FAIL(0),
    MUSIC_OPEN_FAIL(1)
}


/**
 * 加载音乐的模式
 * @param LOAD_MUSIC_ONLY 只加载音乐（通常加入合唱前使用此模式）
 * @param LOAD_LRC_ONLY 只加载歌词（通常歌曲开始播放时观众使用此模式）
 * @param LOAD_MUSIC_AND_LRC 默认模式，加载歌词和音乐（通常歌曲开始播放时主唱使用此模式）
 */
enum class KTVLoadMusicMode(val value: Int) {
    LOAD_NONE(-1),
    LOAD_MUSIC_ONLY(0),
    LOAD_LRC_ONLY(1),
    LOAD_MUSIC_AND_LRC(2)
}

/**
 * 加载音乐的状态
 * @param COMPLETED 加载完成, 进度为100
 * @param FAILED 加载失败
 * @param INPROGRESS 加载中
 */
enum class MusicLoadStatus(val value: Int) {
    COMPLETED(0),
    FAILED(1),
    INPROGRESS(2),
}

/**
 * 音乐音轨模式
 * @param YUAN_CHANG 原唱：主唱开启原唱后，自己听到原唱，听众听到原唱
 * @param BAN_ZOU 伴奏：主唱开启伴奏后，自己听到伴奏，听众听到伴奏
 * @param DAO_CHANG 导唱：主唱开启导唱后，自己听到原唱，听众听到伴奏
 */
enum class AudioTrackMode(val value: Int) {
    YUAN_CHANG(0),
    BAN_ZOU(1),
    DAO_CHANG(2),
}

/**
 * 大合唱中演唱者互相收听对方音频流的选路策略
 * @param RANDOM 随机选取几条流
 * @param BY_DELAY 根据延迟选择最低的几条流
 * @param TOP_N 根据音强选流
 * @param BY_DELAY_AND_TOP_N 同时开始延迟选路和音强选流
 */
enum class GiantChorusRouteSelectionType(val value: Int) {
    RANDOM(0),
    BY_DELAY(1),
    TOP_N(2),
    BY_DELAY_AND_TOP_N(3)
}

/**
 * 大合唱中演唱者互相收听对方音频流的选路配置
 * @param type 选路策略
 * @param streamNum 最大选取的流个数（推荐6）
 */
data class GiantChorusRouteSelectionConfig constructor(
    val type: GiantChorusRouteSelectionType,
    val streamNum: Int
)

/**
 * 歌词组件接口，您setLrcView传入的歌词组件需要继承此接口类，并实现以下几个方法
 */
interface ILrcView {
    /**
     * ktvApi内部更新音高pitch时会主动调用此方法将pitch值传给你的歌词组件
     * @param pitch 音高值
     */
    fun onUpdatePitch(pitch: Float?)

    /**
     * ktvApi内部更新音乐播放进度progress时会主动调用此方法将进度值progress传给你的歌词组件，50ms回调一次
     * @param progress 歌曲播放的真实进度 20ms回调一次
     */
    fun onUpdateProgress(progress: Long?)

    /**
     * ktvApi获取到歌词地址时会主动调用此方法将歌词地址url传给你的歌词组件，您需要在这个回调内完成歌词的下载
     * @param url 歌词地址
     */
    fun onDownloadLrcData(url: String?)

    /**
     * ktvApi获取到抢唱切片歌曲副歌片段时间时，会调用此方法回调给歌词组件
     * @param highStartTime 副歌片段起始时间
     * @param highEndTime 副歌片段终止时间
     */
    fun onHighPartTime(highStartTime: Long, highEndTime: Long)
}

/**
 * 音乐加载状态接口
 */
interface IMusicLoadStateListener {
    /**
     * 音乐加载成功
     * @param songCode 歌曲编码，和loadMusic传入的songCode一致
     * @param lyricUrl 歌词地址
     */
    fun onMusicLoadSuccess(songCode: Long, lyricUrl: String)

    /**
     * 音乐加载失败
     * @param songCode 加载失败的歌曲编码
     * @param reason 歌曲加载失败的原因
     */
    fun onMusicLoadFail(songCode: Long, reason: KTVLoadMusicFailReason)

    /**
     * 音乐加载进度
     * @param songCode 歌曲编码
     * @param percent 歌曲加载进度
     * @param status 歌曲加载的状态
     * @param msg 相关信息
     * @param lyricUrl 歌词地址
     */
    fun onMusicLoadProgress(songCode: Long, percent: Int, status: MusicLoadStatus, msg: String?, lyricUrl: String?)
}

/**
 * 切换演唱角色状态接口
 */
interface ISwitchRoleStateListener {
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

interface OnJoinChorusStateListener {
    fun onJoinChorusSuccess()
    fun onJoinChorusFail(reason: KTVJoinChorusFailReason)
}

/**
 * KTVApi事件回调
 */
abstract class IKTVApiEventHandler {
    /**
     * 播放器状态变化
     * @param state MediaPlayer 播放状态
     * @param reason MediaPlayer Error 信息
     * @param isLocal 本地还是主唱端的 Player 信息
     */
    open fun onMusicPlayerStateChanged(
        state: Constants.MediaPlayerState, reason: Constants.MediaPlayerReason, isLocal: Boolean
    ) {
    }

    /**
     * ktvApi内部角色切换
     * @param oldRole 老角色
     * @param newRole 新角色
     */
    open fun onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {}

    /**
     * rtm或合唱频道token将要过期回调，需要renew这个token
     */
    open fun onTokenPrivilegeWillExpire() {}

    /**
     * 合唱频道人声音量提示
     * @param speakers 不同用户音量信息
     * @param totalVolume 总音量
     */
    open fun onChorusChannelAudioVolumeIndication(
        speakers: Array<out IRtcEngineEventHandler.AudioVolumeInfo>?,
        totalVolume: Int) {}

    /**
     * 播放进度回调
     * @param position_ms 音乐播放的进度
     */
    open fun onMusicPlayerPositionChanged(position_ms: Long, timestamp_ms: Long) {}
}

/**
 * 初始化KTVApi的配置
 * @param appId 用来初始化 Mcc Engine
 * @param rtmToken 创建 Mcc Engine 需要
 * @param engine RTC engine 对象
 * @param channelName 频道号，子频道名以基于主频道名 + "_ex" 固定规则生成频道号
 * @param localUid 创建 Mcc engine 和 加入子频道需要用到
 * @param chorusChannelName 子频道名 加入子频道需要用到
 * @param chorusChannelToken 子频道token 加入子频道需要用到
 * @param maxCacheSize 最大缓存歌曲数
 * @param type KTV场景
 * @param musicType 音乐类型
 */
data class KTVApiConfig constructor(
    val appId: String,
    val rtmToken: String,
    val engine: RtcEngine,
    val channelName: String,
    val localUid: Int,
    val chorusChannelName: String,
    var chorusChannelToken: String,
    val maxCacheSize: Int = 10,
    val type: KTVType = KTVType.Normal,
    val musicType: KTVMusicType = KTVMusicType.SONG_CODE
)

/**
 * 初始化KTVGiantChorusApi的配置
 * @param appId 用来初始化 Mcc Engine
 * @param rtmToken 创建 Mcc Engine 需要
 * @param engine RTC engine 对象
 * @param localUid 创建 Mcc engine 和 加入子频道需要用到
 * @param audienceChannelName 观众频道名 加入听众频道需要用到
 * @param chorusChannelToken 观众频道token 加入听众频道需要用到
 * @param chorusChannelName 演唱频道名 加入演唱频道需要用到
 * @param chorusChannelToken 演唱频道token 加入演唱频道需要用到
 * @param musicStreamUid 音乐Uid 主唱推入频道
 * @param musicStreamToken 音乐流token
 * @param maxCacheSize 最大缓存歌曲数
 * @param musicType 音乐类型
 * @param routeSelectionConfig 选路配置
 */
data class KTVGiantChorusApiConfig constructor(
    val appId: String,
    val rtmToken: String,
    val engine: RtcEngine,
    val localUid: Int,
    val audienceChannelName: String,
    val audienceChannelToken: String,
    val chorusChannelName: String,
    val chorusChannelToken: String,
    val musicStreamUid: Int,
    val musicStreamToken: String,
    val maxCacheSize: Int = 10,
    val musicType: KTVMusicType = KTVMusicType.SONG_CODE
)

/**
 * 加载歌曲的配置，不允许在一首歌没有load完成前（成功/失败均算完成）进行下一首歌的加载
 * @param songIdentifier 歌曲 id，通常由业务方给每首歌设置一个不同的SongId用于区分
 * @param mainSingerUid 主唱的 Uid，如果是伴唱，伴唱需要根据这个信息 mute 主频道主唱的声音
 * @param mode 歌曲加载的模式，默认为音乐和歌词均加载
 * @param needPrelude 播放切片歌曲情况下，是否播放
 */
data class KTVLoadMusicConfiguration(
    val songIdentifier: String,
    val mainSingerUid: Int,
    val mode: KTVLoadMusicMode = KTVLoadMusicMode.LOAD_MUSIC_AND_LRC,
    val needPrelude: Boolean = false
)

/**
 * 创建普通合唱KTVApi实例
 */
fun createKTVApi(config: KTVApiConfig): KTVApi = KTVApiImpl(config)

/**
 * 创建大合唱KTVApi实例
 */
fun createKTVGiantChorusApi(config: KTVGiantChorusApiConfig): KTVApi = KTVGiantChorusApiImpl(config)

/**
 * KTVApi 接口
 */
interface KTVApi {

    companion object {
        // 听到远端的音量
        var remoteVolume: Int = 30
        // 本地mpk播放音量
        var mpkPlayoutVolume: Int = 50
        // mpk发布音量
        var mpkPublishVolume: Int = 50

        // 是否使用音频自采集
        var useCustomAudioSource = false
        // 调试使用，会输出更多的日志
        var debugMode = false
        // 内部测试使用，无需关注
        var mccDomain = ""
        // 大合唱的选路策略
        var routeSelectionConfig = GiantChorusRouteSelectionConfig(GiantChorusRouteSelectionType.BY_DELAY, 6)
    }

    /**
     * 更新ktvapi内部使用的streamId，每次加入频道需要更新内部streamId
     */
    fun renewInnerDataStreamId()

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
     * 收到 IKTVApiEventHandler.onTokenPrivilegeWillExpire 回调时需要主动调用方法更新Token
     * @param rtmToken musicContentCenter模块需要的rtm token
     * @param chorusChannelRtcToken 合唱需要的频道rtc token
     */
    fun renewToken(
        rtmToken: String,
        chorusChannelRtcToken: String
    )

    /**
     * 获取歌曲榜单
     * @param onMusicChartResultListener 榜单列表回调
     */
    fun fetchMusicCharts(
        onMusicChartResultListener: (
            requestId: String?,
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
            requestId: String?,
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
            requestId: String?,
            status: Int,         // status=2 时token过期
            page: Int,
            pageSize: Int,
            total: Int,
            list: Array<out Music>?
        ) -> Unit
    )

    /**
     * 异步加载歌曲，同时只能为一首歌loadSong，loadSong结果会通过回调通知业务层
     * @param songCode 歌曲唯一编码
     * @param config 加载歌曲配置
     * @param musicLoadStateListener 加载歌曲结果回调
     *
     * 推荐调用：
     * 歌曲开始时：
     * 主唱 loadMusic(KTVLoadMusicConfiguration(mode=LOAD_MUSIC_AND_LRC, songCode, mainSingerUid)) switchSingerRole(SoloSinger)
     * 观众 loadMusic(KTVLoadMusicConfiguration(mode=LOAD_LRC_ONLY, songCode, mainSingerUid))
     * 加入合唱时：
     * 准备加入合唱者：loadMusic(KTVLoadMusicConfiguration(mode=LOAD_MUSIC_ONLY, songCode, mainSingerUid))
     * loadMusic成功后switchSingerRole(CoSinger)
     */
    fun loadMusic(
        songCode: Long,
        config: KTVLoadMusicConfiguration,
        musicLoadStateListener: IMusicLoadStateListener
    )

    /**
     * 取消加载歌曲，会打断加载歌曲的进程并移除歌曲缓存
     * @param songCode 歌曲唯一编码
     */
    fun removeMusic(songCode: Long)

    /**
     * 加载歌曲，同时只能为一首歌loadSong，同步调用， 一般使用此loadSong是歌曲已经preload成功（url为本地文件地址）
     * @param url 歌曲地址
     * @param config 加载歌曲配置
     *
     * 推荐调用：
     * 歌曲开始时：
     * 主唱 loadMusic(KTVLoadMusicConfiguration(mode=LOAD_MUSIC_AND_LRC, url, mainSingerUid)) switchSingerRole(SoloSinger)
     * 观众 loadMusic(KTVLoadMusicConfiguration(mode=LOAD_LRC_ONLY, url, mainSingerUid))
     * 加入合唱时：
     * 准备加入合唱者：loadMusic(KTVLoadMusicConfiguration(mode=LOAD_MUSIC_ONLY, url, mainSingerUid))
     * loadMusic成功后switchSingerRole(CoSinger)
     */
    fun loadMusic(
        url: String,
        config: KTVLoadMusicConfiguration
    )

    /**
     * 加载歌曲，同时只能为一首歌loadSong，同步调用， 一般使用此loadSong是歌曲已经preload成功（url为本地文件地址）
     * @param config 加载歌曲配置，默认播放url1
     * @param url1 歌曲地址1
     * @param url2 歌曲地址2
     *
     *
     * 推荐调用：
     * 歌曲开始时：
     * 主唱 loadMusic(KTVLoadMusicConfiguration(mode=LOAD_MUSIC_AND_LRC, url, mainSingerUid)) switchSingerRole(SoloSinger)
     * 观众 loadMusic(KTVLoadMusicConfiguration(mode=LOAD_LRC_ONLY, url, mainSingerUid))
     * 加入合唱时：
     * 准备加入合唱者：loadMusic(KTVLoadMusicConfiguration(mode=LOAD_MUSIC_ONLY, url, mainSingerUid))
     * loadMusic成功后switchSingerRole(CoSinger)
     */
    fun load2Music(
        url1: String,
        url2: String,
        config: KTVLoadMusicConfiguration
    )

    /**
     * 多文件切换播放资源
     * @param url 需要切换的播放资源，需要为 load2Music 中 参数 url1，url2 中的一个
     * @param syncPts 是否同步切换前后的起始播放位置: true 同步，false 不同步，从 0 开始
     */
    fun switchPlaySrc(url: String, syncPts: Boolean)

    /**
     * 异步切换演唱身份，结果会通过回调通知业务层
     * @param newRole 新演唱身份
     * @param switchRoleStateListener 切换演唱身份结果
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
        switchRoleStateListener: ISwitchRoleStateListener?
    )

    /**
     * 播放歌曲
     * @param songCode 歌曲唯一编码
     * @param startPos 开始播放的位置
     */
    fun startSing(songCode: Long, startPos: Long)

    /**
     * 播放歌曲
     * @param url 歌曲地址
     * @param startPos 开始播放的位置
     */
    fun startSing(url: String, startPos: Long)

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
     * 开关麦
     * @param mute true 关麦 false 开麦
     */
    fun muteMic(mute: Boolean)

    /**
     * 设置当前音频播放delay， 适用于音频自采集的情况
     * @param audioPlayoutDelay 音频帧处理和播放的时间差
     */
    fun setAudioPlayoutDelay(audioPlayoutDelay: Int)

    /**
     * 获取mpk实例
     */
    fun getMediaPlayer() : IMediaPlayer

    /**
     * 获取mcc实例
     */
    fun getMusicContentCenter() : IAgoraMusicContentCenter

    /**
     * 切换音轨, 原唱/伴奏/导唱
     */
    fun switchAudioTrack(mode: AudioTrackMode)

    /**
     * 开启关闭专业模式，默认关
     */
    fun enableProfessionalStreamerMode(enable: Boolean)

    /**
     * 开启 Multipathing, 默认开
     */
    fun enableMulitpathing(enable: Boolean)
}