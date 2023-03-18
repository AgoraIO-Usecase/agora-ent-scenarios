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


/// 歌曲状态
@objc public enum KTVPlayerTrackMode: Int {
    case origin = 0    //原唱
    case acc           //伴奏
}

/// 加载歌曲失败原因
@objc public enum KTVLoadMusicMode: Int {
    case loadMusicOnly
    case loadLrcOnly
    case loadMusicAndLrc
}

/// 加载歌曲失败原因
@objc public enum KTVLoadSongFailReason: Int {
    case noLyricUrl = 0         //无歌词
    case musicPreloadFail   //歌曲预加载失败
    case inProgress
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
    case musicPreloadFail  //歌曲预加载失败
    case musicOpenFail     //歌曲打开失败
    case joinChannelFail   //加入ex频道失败
    case musicPreloadFailAndJoinChannelFail
}

@objc public protocol KTVMusicLoadStateListener: NSObjectProtocol {
    
    
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
    func onMusicLoadFail(reason: KTVLoadSongFailReason)
}


//public protocol KTVJoinChorusStateListener: NSObjectProtocol {
//
//    /// 加入合唱成功
//    func onJoinChorusSuccess()
//
//    /// 加入合唱失败
//    /// - Parameter reason: 失败原因
//    func onJoinChorusFail(reason: KTVJoinChorusFailReason)
//}

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
}

@objc open class KTVApiConfig: NSObject{
    var appId: String
    var rtmToken: String
    var engine: AgoraRtcEngineKit
    var channelName: String
    var dataStreamId: Int = -1
    var localUid: Int = 0
    
    @objc public
    init(appId: String,
         rtmToken: String,
         engine: AgoraRtcEngineKit,
         channelName: String,
         streamId: Int,
         localUid: Int) {
        self.appId = appId
        self.rtmToken = rtmToken
        self.engine = engine
        self.channelName = channelName
        self.dataStreamId = streamId
        self.localUid = localUid
    }
}

/// 歌曲加载配置信息
@objcMembers open class KTVSongConfiguration: NSObject {
    public var autoPlay: Bool = true   //是否加载完成自动播放
    public var songCode: Int = 0          //歌曲id
    public var mainSingerUid: Int = 0     //主唱uid
}


public typealias LyricCallback = ((String?) -> Void)
public typealias LoadMusicCallback = ((AgoraMusicContentCenterPreloadStatus) -> Void)
public typealias SwitchRoleStateCallBack = (KTVSwitchRoleState, KTVSwitchRoleFailReason) -> Void
public typealias MusicChartCallBacks = (String, AgoraMusicContentCenterStatusCode, [AgoraMusicChartInfo]?) -> Void
public typealias MusicResultCallBacks = (String, AgoraMusicContentCenterStatusCode, AgoraMusicCollection) -> Void
public typealias JoinExChannelCallBack = ((Bool, KTVJoinChorusFailReason?)-> Void)

@objc public protocol KTVApiDelegate: NSObjectProtocol {
    
    /// 初始化
    /// - Parameter config: <#config description#>
    init(config: KTVApiConfig)
    
    
    /// 订阅KTVApi事件
    /// - Parameter ktvApiEventHandler: <#ktvApiEventHandler description#>
    func addEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate)
    
    
    /// 取消订阅KTVApi事件
    /// - Parameter ktvApiEventHandler: <#ktvApiEventHandler description#>
    func removeEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate)
    
    
    /// 清空内部变量/缓存，取消在initWithRtcEngine时的监听，以及取消网络请求等
    func cleanCache()
    
    
    
    /// 获取歌曲榜单
    /// - Parameter musicChartResult: request, status,  chat info list
    /// - Returns: <#description#>
    func fetchMusicCharts(completion:@escaping MusicChartCallBacks)
    
    
    /// 根据歌曲榜单类型搜索歌单
    /// - Parameters:
    ///   - musicChartId: <#musicChartId description#>
    ///   - page: <#page description#>
    ///   - pageSize: <#pageSize description#>
    ///   - jsonOption: <#jsonOption description#>
    ///   - musicCollectionResult: request, status, music list
    /// - Returns: <#description#>
    func searchMusic(musicChartId: Int,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion:@escaping MusicResultCallBacks)
    
    
    /// 根据关键字搜索歌曲
    /// - Parameters:
    ///   - keyword: <#keyword description#>
    ///   - page: <#page description#>
    ///   - pageSize: <#pageSize description#>
    ///   - jsonOption: <#jsonOption description#>
    ///   - completion: <#completion description#>
    /// - Returns: <#description#>
    func searchMusic(keyword: String,
                     page: Int, pageSize: Int,
                     jsonOption: String,
                     completion: @escaping MusicResultCallBacks)
            
    
    
    /// 加载歌曲
    /// - Parameters:
    ///   - config: <#config description#>
    ///   - onMusicLoadStateListener: <#onMusicLoadStateListener description#>
    func loadMusic(config: KTVSongConfiguration, mode: KTVLoadMusicMode, onMusicLoadStateListener: KTVMusicLoadStateListener)
    
    
    /// 切换角色
    /// - Parameters:
    ///   - newRole: <#newRole description#>
    ///   - token: <#token description#>
    ///   - onSwitchRoleState: <#onSwitchRoleState description#>
    func switchSingerRole(newRole: KTVSingRole, token: String, onSwitchRoleState:@escaping SwitchRoleStateCallBack)
    
    
    /// 播放
    /// - Parameter startPos: <#startPos description#>
    func startSing(startPos: Int)
    
    
    /// 恢复播放
    func resumeSing()
    
    
    /// 暂停播放
    func pauseSing()
    
    
    /// 调整进度
    /// - Parameter time: 进度，单位ms
    func seekSing(time: Int)
    
    
    /// 设置歌词组件，在任意时机设置都可以生效
    /// - Parameter view: <#view description#>
    func setLrcView(view: KTVLrcViewDelegate)
    
    
    /// 设置当前mic开关状态目前关麦调用
    /// 目前关麦调用 adjustRecordSignalVolume(0) 后 onAudioVolumeIndication 仍然会执行， ktvApi需要增加一个变量判断当前是否关麦， 如果关麦把设置给歌词组件的pitch改为0
    /// - Parameter isOnMicOpen: <#isOnMicOpen description#>
    func setMicStatus(isOnMicOpen: Bool)
    
    /// 获取mpk实例
    /// - Returns: <#description#>
    func getMediaPlayer() -> AgoraMusicPlayerProtocol?
}
