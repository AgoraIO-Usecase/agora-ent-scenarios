//
//  SBGApiDelegate.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraRtcKit

/// 用户角色
@objc public enum SBGSingRole: Int {
    case soloSinger = 0     //独唱者
    case coSinger           //伴唱
    case leadSinger         //主唱
    case audience           //观众
//    case followSinger       //跟唱
}


/// 歌曲状态
@objc public enum SBGPlayerTrackMode: Int {
    case origin = 0    //原唱
    case lead           //导唱
    case acc            //伴奏
}

/// 加载歌曲失败原因
@objc public enum SBGLoadMusicMode: Int {
    case loadNone
    case loadMusicOnly
    case loadLrcOnly
    case loadMusicAndLrc
}

/// 加载歌曲失败原因
@objc public enum SBGLoadSongFailReason: Int {
    case noLyricUrl = 0         //无歌词
    case musicPreloadFail   //歌曲预加载失败
    case cancled // 本次加载取消/停止
}

@objc public enum SBGSwitchRoleState: Int {
    case success = 0
    case fail
}

@objc public enum SBGSwitchRoleFailReason: Int {
    case none = 0
    case joinChannelFail
    case noPermission
}


/// 加入合唱结果状态
@objc public enum SBGJoinChorusState: Int {
    case success = 0    //加入合唱成功
    case fail           //加入合唱失败
}


/// 加入合唱失败原因
@objc public enum SBGJoinChorusFailReason: Int {
    case musicPreloadFail  //歌曲预加载失败
    case musicOpenFail     //歌曲打开失败
    case joinChannelFail   //加入ex频道失败
    case musicPreloadFailAndJoinChannelFail
}

@objc public enum KTVType: Int {
    case normal
    case singbattle
}

@objc public protocol ISBGMusicLoadStateListener: NSObjectProtocol {
    
    
    /// 歌曲进度
    /// - ParameteSBG:
    ///   - songCode: <#songCode description#>
    ///   - percent: 歌曲加载进度 范围： 0-100
    ///   - status: <#status description#>
    ///   - msg: <#msg description#>
    ///   - lyricUrl: <#lyricUrl description#>
    func onMusicLoadProgress(songCode: Int, percent: Int, status: AgoraMusicContentCenterPreloadStatus, msg: String?, lyricUrl: String?)
    
    /// 歌曲加载成功
    /// - ParameteSBG:
    ///   - songCode: 歌曲code
    ///   - lyricUrl: 歌词远端url
    func onMusicLoadSuccess(songCode: Int, lyricUrl: String)

    
    /// 歌曲加载失败
    /// - ParameteSBG:
    ///   - songCode: 歌曲code
    ///   - lyricUrl: 歌曲远端url
    ///   - reason: 错误原因
    func onMusicLoadFail(songCode: Int, reason: SBGLoadSongFailReason)
}


//public protocol SBGJoinChorusStateListener: NSObjectProtocol {
//
//    /// 加入合唱成功
//    func onJoinChorusSuccess()
//
//    /// 加入合唱失败
//    /// - Parameter reason: 失败原因
//    func onJoinChorusFail(reason: SBGJoinChorusFailReason)
//}

@objc public protocol SBGApiEventHandlerDelegate: NSObjectProtocol {
    
    /// 歌曲播放状态变化
    /// - ParameteSBG:
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
    /// - ParameteSBG:
    ///   - oldRole: <#oldRole description#>
    ///   - newRole: <#newRole description#>
    func onSingerRoleChanged(oldRole: SBGSingRole, newRole: SBGSingRole)
    
    
    func onTokenPrivilegeWillExpire()
            
    /**
         * 合唱频道人声音量提示
         * @param speakers 不同用户音量信息
         * @param totalVolume 总音量
         */
    func onChorusChannelAudioVolumeIndication(
        speakers: [AgoraRtcAudioVolumeInfo],
        totalVolume: Int)
}

@objc open class SBGApiConfig: NSObject{
    var appId: String
    var rtmToken: String
    weak var engine: AgoraRtcEngineKit?
    var channelName: String
    var localUid: Int = 0
    var chorusChannelName: String
    var chorusChannelToken: String
    var type: KTVType = .normal
    var maxCacheSize: Int = 10
    @objc public
    init(appId: String,
         rtmToken: String,
         engine: AgoraRtcEngineKit,
         channelName: String,
         localUid: Int,
         chorusChannelName: String,
         chorusChannelToken: String,
         type: KTVType,
         maxCacheSize: Int
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
    }
}

/// 歌曲加载配置信息
@objcMembers open class SBGSongConfiguration: NSObject {
    public var songIdentifier: String = ""
    public var autoPlay: Bool = false   //是否加载完成自动播放
    public var mainSingerUid: Int = 0     //主唱uid
    public var mode: SBGLoadMusicMode = .loadMusicAndLrc
}


public typealias SBGLyricCallback = ((String?) -> Void)
public typealias SBGLoadMusicCallback = ((AgoraMusicContentCenterPreloadStatus, NSInteger) -> Void)
public typealias ISBGSwitchRoleStateListener = (SBGSwitchRoleState, SBGSwitchRoleFailReason) -> Void
public typealias SBGMusicChartCallBacks = (String, AgoraMusicContentCenterStatusCode, [AgoraMusicChartInfo]?) -> Void
public typealias SBGMusicResultCallBacks = (String, AgoraMusicContentCenterStatusCode, AgoraMusicCollection) -> Void
public typealias SBGJoinExChannelCallBack = ((Bool, SBGJoinChorusFailReason?)-> Void)

@objc public protocol SBGApiDelegate: NSObjectProtocol {
    
    /// 初始化
    /// - Parameter config: <#config description#>
    init(config: SBGApiConfig)
    
    
    /// 订阅SBGApi事件
    /// - Parameter SBGApiEventHandler: <#SBGApiEventHandler description#>
    func addEventHandler(SBGApiEventHandler: SBGApiEventHandlerDelegate)
    
    
    /// 取消订阅SBGApi事件
    /// - Parameter SBGApiEventHandler: <#SBGApiEventHandler description#>
    func removeEventHandler(SBGApiEventHandler: SBGApiEventHandlerDelegate)
    
    
    /// 清空内部变量/缓存，取消在initWithRtcEngine时的监听，以及取消网络请求等
    func cleanCache()
    
    
    /**
     * 获取歌曲榜单
     * Parameter completion: 榜单列表回调
     */
    func fetchMusicCharts(completion:@escaping SBGMusicChartCallBacks)
    
    /**
    * 根据歌曲榜单类型搜索歌单
    *  ParameteSBG:
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
                     completion:@escaping SBGMusicResultCallBacks)
    
    /**
    * 根据关键字搜索歌曲
    *  ParameteSBG:
    *  keyword: 搜索关键字
    *  page: 榜单的查询页数
    *  pageSize: 查询每页的数据长度
    *  jsonOption: 自定义过滤模式
    *  completion: 歌曲列表回调
    */
    func searchMusic(keyword: String,
                     page: Int, pageSize: Int,
                     jsonOption: String,
                     completion: @escaping SBGMusicResultCallBacks)
            
    
    
    /// 加载歌曲
    /// - ParameteSBG:
    ///   - config: <#config description#>
    ///   - onMusicLoadStateListener: <#onMusicLoadStateListener description#>
    func loadMusic(songCode: Int, config: SBGSongConfiguration, onMusicLoadStateListener: ISBGMusicLoadStateListener)
    
    /// 通过url加载歌曲
    /// - ParameteSBG:
    ///   - config: <#config description#>
    ///   - onMusicLoadStateListener: <#onMusicLoadStateListener description#>
    func loadMusic(config: SBGSongConfiguration, url: String)
    
    
    /// 切换角色
    /// - ParameteSBG:
    ///   - newRole: <#newRole description#>
    ///   - token: <#token description#>
    ///   - onSwitchRoleState: <#onSwitchRoleState description#>
    func switchSingerRole(newRole: SBGSingRole, onSwitchRoleState:@escaping ISBGSwitchRoleStateListener)
    
    
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
    func setLrcView(view: SBGLrcViewDelegate)
    
    
    /// 设置当前mic开关状态目前关麦调用
    /// 目前关麦调用 adjustRecordSignalVolume(0) 后 onAudioVolumeIndication 仍然会执行， SBGApi需要增加一个变量判断当前是否关麦， 如果关麦把设置给歌词组件的pitch改为0
    /// - Parameter isOnMicOpen: <#isOnMicOpen description#>
    func setMicStatus(isOnMicOpen: Bool)
    
    /// 获取mpk实例
    /// - Returns: <#description#>
    func getMediaPlayer() -> AgoraMusicPlayerProtocol?
    
    /// 获取MCC实例
    /// - Returns: <#description#>
    func getMusicContentCenter() -> AgoraMusicContentCenter?
    
    /**
     创建dataStreamID
     */
    func renewInnerDataStreamId()
}
