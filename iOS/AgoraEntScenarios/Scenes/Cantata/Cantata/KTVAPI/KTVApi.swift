//
//  KTVApiDelegate.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraRtcKit

/// 用户角色
@objc public enum KTVSingRole: Int {
    case soloSinger = 0     //独唱者
    case coSinger           //伴唱
    case leadSinger         //主唱
    case audience           //观众
//    case followSinger       //跟唱
}

@objc public enum loadMusicType: Int {
    case mcc
    case local
}

/// 歌曲状态
@objc public enum KTVPlayerTrackMode: Int {
    case origin = 0    //原唱
    case lead          //导唱
    case acc           //伴奏
}

/// 加载歌曲失败原因
@objc public enum KTVLoadMusicMode: Int {
    case loadNone
    case loadMusicOnly
    case loadLrcOnly
    case loadMusicAndLrc
}

/// 加载歌曲失败原因
@objc public enum KTVLoadSongFailReason: Int {
    case noLyricUrl = 0         //无歌词
    case musicPreloadFail   //歌曲预加载失败
    case cancled // 本次加载取消/停止
}

@objc public enum KTVSwitchRoleState: Int {
    case success = 0
    case fail
}

@objc public enum KTVSwitchRoleFailReason: Int {
    case none = 0
    case joinChannelFail
    case noPermission
}


/// 加入合唱结果状态
@objc public enum KTVJoinChorusState: Int {
    case success = 0    //加入合唱成功
    case fail           //加入合唱失败
}


/// 加入合唱失败原因
@objc public enum KTVJoinChorusFailReason: Int {
    case musicOpenFail     //歌曲打开失败
    case joinChannelFail   //加入ex频道失败
}

@objc public enum KTVType: Int {
    case normal
    case singbattle
    case singRelay
}

@objc public protocol IMusicLoadStateListener: NSObjectProtocol {
    
    
    /// 歌曲进度
    /// - Parameters:
    ///   - songCode: <#songCode description#>
    ///   - percent: 歌曲加载进度 范围： 0-100
    ///   - status: <#status description#>
    ///   - msg: <#msg description#>
    ///   - lyricUrl: <#lyricUrl description#>
    func onMusicLoadProgress(songCode: Int, percent: Int, status: AgoraMusicContentCenterPreloadStatus, msg: String?, lyricUrl: String?)
    
    /// 歌曲加载成功
    /// - Parameters:
    ///   - songCode: 歌曲code
    ///   - lyricUrl: 歌词远端url
    func onMusicLoadSuccess(songCode: Int, lyricUrl: String)

    
    /// 歌曲加载失败
    /// - Parameters:
    ///   - songCode: 歌曲code
    ///   - lyricUrl: 歌曲远端url
    ///   - reason: 错误原因
    func onMusicLoadFail(songCode: Int, reason: KTVLoadSongFailReason)
}

@objc public protocol KTVLrcViewDelegate: NSObjectProtocol {
    func onUpdatePitch(pitch: Float)
    func onUpdateProgress(progress: Int)
    func onDownloadLrcData(url: String)
    func onHighPartTime(highStartTime: Int, highEndTime: Int)
}

@objc public protocol KTVApiEventHandlerDelegate: NSObjectProtocol {
    
    /// 歌曲播放状态变化
    /// - Parameters:
    ///   - state: <#state description#>
    ///   - error: <#error description#>
    ///   - isLocal: <#isLocal description#>
    func onMusicPlayerStateChanged(state: AgoraMediaPlayerState,
                                   error: AgoraMediaPlayerError,
                                   isLocal: Bool)
    
    
    /// 歌曲得分回调
    /// - Parameter score: <#score description#>
    func onSingingScoreResult(score: Float)
     
    /// 角色切换回调
    /// - Parameters:
    ///   - oldRole: <#oldRole description#>
    ///   - newRole: <#newRole description#>
    func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole)
    
    func onTokenPrivilegeWillExpire()
    
    /**
         * 合唱频道人声音量提示
         * @param speakers 不同用户音量信息
         * @param totalVolume 总音量
         */
    func onChorusChannelAudioVolumeIndication(
        speakers: [AgoraRtcAudioVolumeInfo],
        totalVolume: Int)
    
    //MPK时间回调 只给房主用 仅适合接唱
    func onMusicPlayerProgressChanged(with progress: Int)
}

// 大合唱中演唱者互相收听对方音频流的选路策略
enum GiantChorusRouteSelectionType: Int {
    case random = 0 // 随机选取几条流
    case byDelay = 1 // 根据延迟选择最低的几条流
    case topN = 2 // 根据音强选流
    case byDelayAndTopN = 3 // 同时开始延迟选路和音强选流
}

// 大合唱中演唱者互相收听对方音频流的选路配置
@objc public class GiantChorusRouteSelectionConfig: NSObject {
    let type: GiantChorusRouteSelectionType // 选路策略
    let streamNum: Int // 最大选取的流个数（推荐6）

    init(type: GiantChorusRouteSelectionType, streamNum: Int) {
        self.type = type
        self.streamNum = streamNum
    }
}

@objc open class GiantChorusConfiguration: NSObject {
    var appId: String
    var rtmToken: String
    weak var engine: AgoraRtcEngineKit?
    var channelName: String
    var localUid: Int = 0
    var chorusChannelName: String
    var chorusChannelToken: String
    var maxCacheSize: Int = 10
    var musicType: loadMusicType = .mcc
    var audienceChannelToken: String = ""
    var musicStreamUid: Int = 0
    var musicChannelToken: String = ""
    var routeSelectionConfig: GiantChorusRouteSelectionConfig = GiantChorusRouteSelectionConfig(type: .byDelay, streamNum: 6)
    var mccDomain: String?
    @objc public
    init(appId: String,
         rtmToken: String,
         engine: AgoraRtcEngineKit,
         localUid: Int,
         audienceChannelName: String,
         audienceChannelToken: String,
         chorusChannelName: String,
         chorusChannelToken: String,
         musicStreamUid: Int,
         musicChannelToken: String,
         maxCacheSize: Int,
         musicType: loadMusicType,
         routeSelectionConfig: GiantChorusRouteSelectionConfig,
         mccDomain: String?
    ) {
        self.appId = appId
        self.rtmToken = rtmToken
        self.engine = engine
        self.channelName = audienceChannelName
        self.localUid = localUid
        self.chorusChannelName = chorusChannelName
        self.chorusChannelToken = chorusChannelToken
        self.maxCacheSize = maxCacheSize
        self.musicType = musicType
        self.audienceChannelToken = audienceChannelToken
        self.musicStreamUid = musicStreamUid
        self.musicChannelToken = musicChannelToken
        self.routeSelectionConfig = routeSelectionConfig
        self.mccDomain = mccDomain
    }
}

@objc open class KTVApiConfig: NSObject{
    var appId: String
    var rtmToken: String
    weak var engine: AgoraRtcEngineKit?
    var channelName: String
    var localUid: Int = 0
    var chorusChannelName: String
    var chorusChannelToken: String
    var type: KTVType = .normal
    var maxCacheSize: Int = 10
    var musicType: loadMusicType = .mcc
    var mccDomain: String?
    @objc public
    init(appId: String,
         rtmToken: String,
         engine: AgoraRtcEngineKit,
         channelName: String,
         localUid: Int,
         chorusChannelName: String,
         chorusChannelToken: String,
         type: KTVType,
         musicType: loadMusicType,
         maxCacheSize: Int,
         mccDomain: String?
    ) {
        self.appId = appId
        self.rtmToken = rtmToken
        self.engine = engine
        self.channelName = channelName
        self.localUid = localUid
        self.chorusChannelName = chorusChannelName
        self.chorusChannelToken = chorusChannelToken
        self.type = type
        self.maxCacheSize = maxCacheSize
        self.musicType = musicType
        self.mccDomain = mccDomain
    }
    
    
}

/// 歌曲加载配置信息
@objcMembers open class KTVSongConfiguration: NSObject {
    public var songIdentifier: String = ""
    public var mainSingerUid: Int = 0     //主唱uid
    public var mode: KTVLoadMusicMode = .loadMusicAndLrc
    public var songCutter: Bool = false
}


public typealias LyricCallback = ((String?) -> Void)
public typealias LoadMusicCallback = ((AgoraMusicContentCenterPreloadStatus, NSInteger) -> Void)
public typealias ISwitchRoleStateListener = (KTVSwitchRoleState, KTVSwitchRoleFailReason) -> Void
public typealias MusicChartCallBacks = (String, AgoraMusicContentCenterStatusCode, [AgoraMusicChartInfo]?) -> Void
public typealias MusicResultCallBacks = (String, AgoraMusicContentCenterStatusCode, AgoraMusicCollection) -> Void
public typealias JoinExChannelCallBack = ((Bool, KTVJoinChorusFailReason?)-> Void)

@objc public protocol KTVApiDelegate: NSObjectProtocol {
    
    @objc optional func createKtvApi(config: KTVApiConfig) //小合唱必选
    
    @objc optional func createKTVGiantChorusApi(config: GiantChorusConfiguration) //大合唱必选
    
    /// 订阅KTVApi事件
    /// - Parameter ktvApiEventHandler: <#ktvApiEventHandler description#>
    func addEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate)
    
    
    /// 取消订阅KTVApi事件
    /// - Parameter ktvApiEventHandler: <#ktvApiEventHandler description#>
    func removeEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate)
    
    
    /// 清空内部变量/缓存，取消在initWithRtcEngine时的监听，以及取消网络请求等
    func cleanCache()
    
    /**
     * 收到 IKTVApiEventHandler.onTokenPrivilegeWillExpire 回调时需要主动调用方法更新Token
     * @param rtmToken musicContentCenter模块需要的rtm token
     * @param chorusChannelRtcToken 合唱需要的频道rtc token
     */
    func renewToken(
        rtmToken: String,
        chorusChannelRtcToken: String)
    
    /**
     * 获取歌曲榜单
     * Parameter completion: 榜单列表回调
     */
    func fetchMusicCharts(completion:@escaping MusicChartCallBacks)
    
    /**
    * 根据歌曲榜单类型搜索歌单
    *  Parameters:
    *  musicChartId: 榜单id
    *  page: 榜单的查询页数
    *  pageSize: 查询每页的数据长度
    *  jsonOption: 自定义过滤模式
    *  completion: 歌曲列表回调
    */
    func searchMusic(musicChartId: Int,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion:@escaping MusicResultCallBacks)
    
    /**
    * 根据关键字搜索歌曲
    *  Parameters:
    *  keyword: 搜索关键字
    *  page: 榜单的查询页数
    *  pageSize: 查询每页的数据长度
    *  jsonOption: 自定义过滤模式
    *  completion: 歌曲列表回调
    */
    func searchMusic(keyword: String,
                     page: Int, pageSize: Int,
                     jsonOption: String,
                     completion: @escaping MusicResultCallBacks)
            
    
    
    /// 加载歌曲
    /// - Parameters:
    ///   - config: <#config description#>
    ///   - onMusicLoadStateListener: <#onMusicLoadStateListener description#>
    func loadMusic(songCode: Int, config: KTVSongConfiguration, onMusicLoadStateListener: IMusicLoadStateListener)
    
    /// 通过url加载歌曲
    /// - Parameters:
    ///   - config: <#config description#>
    ///   - onMusicLoadStateListener: <#onMusicLoadStateListener description#>
    func loadMusic(config: KTVSongConfiguration, url: String)
    
    
    /// 切换角色
    /// - Parameters:
    ///   - newRole: <#newRole description#>
    ///   - token: <#token description#>
    ///   - onSwitchRoleState: <#onSwitchRoleState description#>
    func switchSingerRole(newRole: KTVSingRole, onSwitchRoleState:@escaping ISwitchRoleStateListener)
    
    
    /// 播放
    /// - Parameter startPos: <#startPos description#>
    func startSing(songCode: Int, startPos: Int)
    
    /**
     * 播放歌曲
     * @param url 歌曲地址
     * @param startPos 开始播放的位置
     * 对于主唱：
     * 如果loadMusic时你选择了autoPlay = true 则不需要主动调用startSing
     * 如果loadMusic时你选择了autoPlay = false 则需要在loadMusic成功后调用startSing
     */
    func startSing(url: String, startPos: Int)
    
    /// 恢复播放
    func resumeSing()
    
    
    /// 暂停播放
    func pauseSing()
    
    
    /// 调整进度
    /// - Parameter time: 进度，单位ms
    func seekSing(time: Int)
    
    /**
     * 设置当前音频播放delay， 适用于音频自采集的情况
     * @param audioPlayoutDelay 音频帧处理和播放的时间差
     */
    func setAudioPlayoutDelay(audioPlayoutDelay: Int)
    
    /// 设置歌词组件，在任意时机设置都可以生效
    /// - Parameter view: <#view description#>
    func setLrcView(view: KTVLrcViewDelegate)
    
    
    /// 设置当前mic开关状态目前关麦调用
    /// 目前关麦调用 adjustRecordSignalVolume(0) 后 onAudioVolumeIndication 仍然会执行， ktvApi需要增加一个变量判断当前是否关麦， 如果关麦把设置给歌词组件的pitch改为0
    /// - Parameter muteStatus: mute mic status
    func muteMic(muteStatus: Bool)
    
    func getMusicPlayer() -> AgoraRtcMediaPlayerProtocol?
    
    /// 获取MCC实例
    /// - Returns: <#description#>
    func getMusicContentCenter() -> AgoraMusicContentCenter?
    
    // 开启专业主播模式
    func enableProfessionalStreamerMode(_ enable: Bool)
    
    /**
     创建dataStreamID
     */
    func renewInnerDataStreamId()
    
    
  /**
   * 加载歌曲，同时只能为一首歌loadSong，同步调用， 一般使用此loadSong是歌曲已经preload成功（url为本地文件地址）
   * @param config 加载歌曲配置，config.autoPlay = true，默认播放url1
   * @param url1 歌曲地址1
   * @param url2 歌曲地址2
   *
   *
   * 推荐调用：
   * 歌曲开始时：
   * 主唱 loadMusic(KTVSongConfiguration(autoPlay=true, mode=LOAD_MUSIC_AND_LRC, url, mainSingerUid)) switchSingerRole(SoloSinger)
   * 观众 loadMusic(KTVSongConfiguration(autoPlay=false, mode=LOAD_LRC_ONLY, url, mainSingerUid))
   * 加入合唱时：
   * 准备加入合唱者：loadMusic(KTVSongConfiguration(autoPlay=false, mode=LOAD_MUSIC_ONLY, url, mainSingerUid))
   * loadMusic成功后switchSingerRole(CoSinger)
   */
  func load2Music(
      url1: String,
      url2: String,
      config: KTVSongConfiguration
  )
  
  /**
   * 多文件切换播放资源
   * @param url 需要切换的播放资源，需要为 load2Music 中 参数 url1，url2 中的一个
   * @param syncPts 是否同步切换前后的起始播放位置: true 同步，false 不同步，从 0 开始
   */
  func switchPlaySrc(url: String, syncPts: Bool)
    
  /**
   * 取消歌曲下载，会打断加载歌曲的进程并移除歌曲缓存
   * @param songCode 歌曲唯一编码
   */
      
   func removeMusic(songCode: Int)
    
   @objc func didAudioMetadataReceived( uid: UInt, metadata: Data)
}
