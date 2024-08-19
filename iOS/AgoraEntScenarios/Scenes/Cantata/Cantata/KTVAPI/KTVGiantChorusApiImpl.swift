//
//  KTVApiImpl.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraRtcKit

/// 加载歌曲状态
@objc fileprivate enum KTVLoadSongState: Int {
    case idle = -1      //空闲
    case ok = 0         //成功
    case failed         //失败
    case inProgress    //加载中
}

fileprivate enum KTVSongMode: Int {
    case songCode
    case songUrl
}

@objc class KTVGiantChorusApiImpl: NSObject, KTVApiDelegate{
    
    private var apiConfig: GiantChorusConfiguration?

    private var songConfig: KTVSongConfiguration?
    private var subChorusConnection: AgoraRtcConnection?
    private var singChannelConnection: AgoraRtcConnection?
    private var mpkConnection: AgoraRtcConnection?

    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var loadMusicListeners: NSMapTable<NSString, AnyObject> = NSMapTable<NSString, AnyObject>(keyOptions: .copyIn, valueOptions: .weakMemory)

   // private var musicPlayer: AgoraRtcMediaPlayerProtocol? //mcc
    private var mediaPlayer: AgoraRtcMediaPlayerProtocol? //local
    private var mcc: AgoraMusicContentCenter?

    private var loadSongMap = Dictionary<String, KTVLoadSongState>()
    private var lyricUrlMap = Dictionary<String, String>()
    private var loadDict = Dictionary<String, KTVLoadSongState>()
    private var lyricCallbacks = Dictionary<String, LyricCallback>()
    private var musicCallbacks = Dictionary<String, LoadMusicCallback>()
    
    private var hasSendPreludeEndPosition: Bool = false
    private var hasSendEndPosition: Bool = false
    
    //multipath
    private var enableMultipathing: Bool = true
   
    private var audioPlayoutDelay: NSInteger = 0
    private var isNowMicMuted: Bool = false
    private var loadSongState: KTVLoadSongState = .idle
    private var lastNtpTime: Int = 0
    private var startHighTime: Int = 0
    private var isRelease: Bool = false
    private var songUrl2: String = ""
    private var playerState: AgoraMediaPlayerState = .idle {
        didSet {
            agoraPrint("playerState did changed: \(oldValue.rawValue)->\(playerState.rawValue)")
            updateRemotePlayBackVolumeIfNeed()
            updateTimer(with: playerState)
        }
    }
    private var pitch: Double = 0
    private var localPlayerPosition: TimeInterval = 0
    private var remotePlayerPosition: TimeInterval = 0
    private var remotePlayerDuration: TimeInterval = 0
    private var localPlayerSystemTime: TimeInterval = 0
    private var lastMainSingerUpdateTime: TimeInterval = 0
    private var playerDuration: TimeInterval = 0
  //  private lazy var apiDelegateHandler = KTVApiRTCDelegateHandler(with: self)

    private var musicChartDict: [String: MusicChartCallBacks] = [:]
    private var musicSearchDict: Dictionary<String, MusicResultCallBacks> = Dictionary<String, MusicResultCallBacks>()
    private var onJoinExChannelCallBack : JoinExChannelCallBack?
    private var mainSingerHasJoinChannelEx: Bool = false
    private var dataStreamId: Int = 0
    private var lastReceivedPosition: TimeInterval = 0
    private var localPosition: Int = 0
    
    private var songMode: KTVSongMode = .songCode
    private var useCustomAudioSource:Bool = false
    private var songUrl: String = ""
    private var songCode: Int = 0
    private var songIdentifier: String = ""

    private var singerRole: KTVSingRole = .audience {
        didSet {
            agoraPrint("singerRole changed: \(oldValue.rawValue)->\(singerRole.rawValue)")
        }
    }
    private var lrcControl: KTVLrcViewDelegate?
    
    private var timer: Timer?
    private var isPause: Bool = false
    
    private var singingScore: Int = 0
    
    public var remoteVolume: Int = 30
    private var joinChorusNewRole: KTVSingRole = .audience
    private var oldPitch: Double = 0
    private var isWearingHeadPhones: Bool = false
    private var enableProfessional: Bool = false
    private var isPublishAudio: Bool = false
    private var audioRouting: Int = -1
    private var recvFromDataStream = false
    //大合唱独有
    private var mStopSyncPitch = true
    private var mSyncPitchTimer: DispatchSourceTimer?
    private var mStopSyncScore = true
    private var mSyncScoreTimer: DispatchSourceTimer?
    private var mStopSyncCloudConvergenceStatus = true
    private var mSyncCloudConvergenceStatusTimer: DispatchSourceTimer?
    private var mStopProcessDelay = true
    private var processDelayFuture: DispatchSourceTimer?
    private var processSubscribeFuture: DispatchSourceTimer?
    private var subScribeSingerMap = [Int: Int]() // <uid, ntpE2eDelay>
    private var singerList = [Int]() // <uid>
    private var mainSingerDelay = 0
    
    private let tag = "KTV_API_LOG"
    private let messageId = "agora:scenarioAPI"
    private let version = "5.0.0"
    private let lyricSyncVersion = 2
    
    private var apiRepoter: APIReporter?
    
    deinit {
        mcc?.register(nil)
        agoraPrint("deinit KTVApiImpl")
    }

    func createKTVGiantChorusApi(config: GiantChorusConfiguration) {
        self.apiConfig = config
        agoraPrint("createKTVGiantChorusApi")
        self.singChannelConnection = AgoraRtcConnection(channelId: config.chorusChannelName, localUid: config.localUid)
        
        setParams()
        
        if config.musicType == .mcc {
            // ------------------ 初始化内容中心 ------------------
            let contentCenterConfiguration = AgoraMusicContentCenterConfig()
            contentCenterConfiguration.appId = config.appId
            contentCenterConfiguration.mccUid = config.localUid
            contentCenterConfiguration.token = config.rtmToken
            contentCenterConfiguration.rtcEngine = config.engine
            contentCenterConfiguration.maxCacheSize = UInt(config.maxCacheSize)
            if let domain = config.mccDomain {
                contentCenterConfiguration.mccDomain = domain
            }
            mcc = AgoraMusicContentCenter.sharedContentCenter(config: contentCenterConfiguration)
            mcc?.register(self)
            // ------------------ 初始化音乐播放器实例 ------------------
            mediaPlayer = mcc?.createMusicPlayer(delegate: self)
            mediaPlayer?.adjustPlayoutVolume(50)
            mediaPlayer?.adjustPublishSignalVolume(50)
        } else {
            mediaPlayer = apiConfig?.engine?.createMediaPlayer(with: self)
            // 音量最佳实践调整
            mediaPlayer?.adjustPlayoutVolume(50)
            mediaPlayer?.adjustPublishSignalVolume(50)
        }
        
        apiRepoter = APIReporter(type: .ktv, version: version, engine: apiConfig?.engine ?? AgoraRtcEngineKit())
        
        initTimer()
        mediaPlayer?.setPlayerOption("play_pos_change_callback", value: 100)
        apiConfig?.engine?.setDelegateEx(self, connection: mpkConnection ?? AgoraRtcConnection())
        startSyncPitch()
        startSyncScore()
        startSyncCloudConvergenceStatus()
    }
    
    private func setParams() {
        guard let engine = self.apiConfig?.engine else {return}
        engine.setParameters("{\"rtc.enable_nasa2\": true}")
        engine.setParameters("{\"rtc.ntp_delay_drop_threshold\": 1000}")
        engine.setParameters("{\"rtc.video.enable_sync_render_ntp\": true}")
        engine.setParameters("{\"rtc.net.maxS2LDelay\": 800}")
        engine.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\": true}")
        engine.setParameters("{\"rtc.net.maxS2LDelayBroadcast\": 400}")
        engine.setParameters("{\"che.audio.neteq.prebuffer\": true}")
        engine.setParameters("{\"che.audio.neteq.prebuffer_max_delay\": 600}")
        engine.setParameters("{\"che.audio.max_mixed_participants\": 8}")
        engine.setParameters("{\"che.audio.custom_bitrate\": 48000}")
        engine.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
        engine.setParameters("{\"che.audio.neteq.targetlevel_offset\": 20}")
        engine.setParameters("{\"che.audio.uplink_apm_async_process\": true}")
                // 标准音质
        engine.setParameters("{\"che.audio.aec.split_srate_for_48k\": 16000}")
        engine.setParameters("{\"che.audio.ans.noise_gate\": 20}")//
        engine.setParameters("{\"rtc.use_audio4\": true}")
        
        //4.3.0 add
        // mutipath
        enableMultipathing = true
        engine.setParameters("{\"rtc.enable_tds_request_on_join\": true}")
        engine.setParameters("{\"rtc.remote_path_scheduling_strategy\": 0}")
        engine.setParameters("{\"rtc.path_scheduling_strategy\": 0}")
        engine.setParameters("{\"rtc.enableMultipath\": true}")
        
        // 数据上报
         engine.setParameters("{\"rtc.direct_send_custom_event\": true}")
        // engine.setParameters("{\"rtc.qos_for_test_purpose\": true}")
    }
    
    func renewInnerDataStreamId() {
        let dataStreamConfig = AgoraDataStreamConfig()
        dataStreamConfig.ordered = false
        dataStreamConfig.syncWithAudio = true
        self.apiConfig?.engine?.createDataStreamEx(&dataStreamId, config: dataStreamConfig, connection: singChannelConnection ?? AgoraRtcConnection())

        sendCustomMessage(with: "renewInnerDataStreamId", dict: [:])
        agoraPrint("renewInnerDataStreamId")
    }
}

//MARK: KTVApiDelegate
extension KTVGiantChorusApiImpl {
    
    func objectContent(of object: Any) -> [String: Any] {
        var content = [String: Any]()
        
        let mirror = Mirror(reflecting: object)
        for child in mirror.children {
            if let propertyName = child.label {
                if let convertibleValue = convertToJSONSerializable(child.value) {
                    content[propertyName] = convertibleValue
                }
            }
        }
        
        return content
    }

    func convertToJSONSerializable(_ value: Any) -> Any? {
        switch value {
        case let value as String:
            return value
        case let value as Int:
            return value
        case let value as Double:
            return value
        case let value as Bool:
            return value
        case let value as Int?:
            return value
        case let value as Double?:
            return value
        case let value as Bool?:
            return value
        case let value as String?:
            return value
        default:
            return nil
        }
    }
    
    func getMusicContentCenter() -> AgoraMusicContentCenter? {
        return mcc
    }
    
    func setLrcView(view: KTVLrcViewDelegate) {
        sendCustomMessage(with: "renewInnerDataStreamId", dict: [:])
        lrcControl = view
    }
    
    //主要针对本地歌曲播放的主唱伴奏切换的 loadmusic MCC直接忽视这个方法
    func load2Music(url1: String, url2: String, config: KTVSongConfiguration) {
        agoraPrint("load2Music called: songUrl url1:(url1),url2:(url2)")
        self.songMode = .songUrl
        self.songConfig = config
        self.songIdentifier = config.songIdentifier
        self.songUrl = url1
        self.songUrl2 = url2
        
//        if config.autoPlay {
//            // 主唱自动播放歌曲
//            if self.singerRole != .leadSinger {
//                switchSingerRole(newRole: .soloSinger) { state, failRes in
//
//                }
//            }
//            startSing(url: url1, startPos: 0)
//        }
    }
    
    //主要针对本地歌曲播放的主唱伴奏切换的 MCC直接忽视这个方法
    func switchPlaySrc(url: String, syncPts: Bool) {
        agoraPrint("switchPlaySrc called: \(url)")
        
        if self.songUrl != url && self.songUrl2 != url {
            print("switchPlaySrc failed: canceled")
            return
        }
        
        let curPlayPosition: Int = syncPts ? mediaPlayer?.getPosition() ?? 0 : 0
        mediaPlayer?.stop()
        startSing(url: url, startPos: curPlayPosition)
    }

    func loadMusic(songCode: Int, config: KTVSongConfiguration, onMusicLoadStateListener: IMusicLoadStateListener) {
        sendCustomMessage(with: "loadMusicWithSongCode:\(songCode)", dict: objectContent(of: config))
        agoraPrint("loadMusic songCode:\(songCode) ")
        self.songMode = .songCode
        self.songCode = songCode
        self.songIdentifier = config.songIdentifier
        _loadMusic(config: config, mode: config.mode, onMusicLoadStateListener: onMusicLoadStateListener)
    }
    
    func loadMusic(config: KTVSongConfiguration, url: String) {
        sendCustomMessage(with: "loadMusicWithUrl:\(url)", dict: objectContent(of: config))
        agoraPrint("loadMusic url:\(url)")
        self.songMode = .songUrl
        self.songUrl = url
        self.songIdentifier = config.songIdentifier
//        if config.autoPlay {
//            // 主唱自动播放歌曲
//            if singerRole != .leadSinger {
//                switchSingerRole(newRole: .soloSinger) { _, _ in
//
//                }
//            }
//            startSing(url: url, startPos: 0)
//        }
    }
    
    func getMusicPlayer() -> AgoraRtcMediaPlayerProtocol? {
        return mediaPlayer
    }

    func addEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        sendCustomMessage(with: "addEventHandler", dict: [:])
        agoraPrint("addEventHandler")
        if eventHandlers.contains(ktvApiEventHandler) {
            return
        }
        eventHandlers.add(ktvApiEventHandler)
    }

    func removeEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        sendCustomMessage(with: "removeEventHandler", dict: [:])
        agoraPrint("removeEventHandler")
        eventHandlers.remove(ktvApiEventHandler)
    }

    func cleanCache() {
        sendCustomMessage(with: "cleanCache", dict: [:])
        isRelease = true
        mediaPlayer?.stop()
        freeTimer()
        agoraPrint("cleanCache")
        singerRole = .audience
        
        stopSyncCloudConvergenceStatus()
        stopSyncScore()
        singingScore = 0
        lrcControl = nil
        lyricCallbacks.removeAll()
        musicCallbacks.removeAll()
        onJoinExChannelCallBack = nil
        loadMusicListeners.removeAllObjects()
        apiConfig?.engine?.destroyMediaPlayer(mediaPlayer)
        mediaPlayer = nil
        if apiConfig?.musicType == .mcc {
            mcc?.register(nil)
            mcc = nil
        }
        apiConfig = nil
        AgoraMusicContentCenter.destroy()
        self.eventHandlers.removeAllObjects()
    }
    
    @objc public func enableMutipath(enable: Bool) {
        sendCustomMessage(with: "enableMutipath", dict: ["enable":enable])
        agoraPrint("enableMutipath:\(enable)")
        enableMultipathing = enable
        if singerRole == .coSinger || singerRole == .leadSinger {
            if let subChorusConnection = subChorusConnection {
                apiConfig?.engine?.setParametersEx("{\"rtc.enableMultipath\": \(enable), \"rtc.path_scheduling_strategy\": 0, \"rtc.remote_path_scheduling_strategy\": 0}", connection: subChorusConnection)
            }
        }
    }
    
    func renewToken(rtmToken: String, chorusChannelRtcToken: String) {
        let dict: [String: Any] = [
                    "rtmToken":rtmToken,
                    "chorusChannelRtcToken":chorusChannelRtcToken
        ]
        sendCustomMessage(with: "renewToken", dict: dict)
        agoraPrint("renewToken rtmToken:\(rtmToken) chorusChannelRtcToken:\(chorusChannelRtcToken)")
            // 更新RtmToken
        mcc?.renewToken(rtmToken)
            // 更新合唱频道RtcToken
            if let subChorusConnection = subChorusConnection {
                let channelMediaOption = AgoraRtcChannelMediaOptions()
                channelMediaOption.token = chorusChannelRtcToken
                apiConfig?.engine?.updateChannelEx(with: channelMediaOption, connection: subChorusConnection)
            }
        }

    func fetchMusicCharts(completion: @escaping MusicChartCallBacks) {
        sendCustomMessage(with: "fetchMusicCharts", dict: [:])
        agoraPrint("fetchMusicCharts")
        let requestId = mcc!.getMusicCharts()
        musicChartDict[requestId] = completion
    }

    func searchMusic(musicChartId: Int,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion:@escaping (String, AgoraMusicContentCenterStateReason, AgoraMusicCollection) -> Void) {
        agoraPrint("searchMusic with musicChartId: \(musicChartId)")
        let dict: [String: Any] = [
                    "musicChartId":musicChartId,
                    "page": page,
                    "pageSize": pageSize,
                    "jsonOption": jsonOption
        ]
        sendCustomMessage(with: "searchMusic", dict: dict)
        let requestId = mcc!.getMusicCollection(musicChartId: musicChartId, page: page, pageSize: pageSize, jsonOption: jsonOption)
        musicSearchDict[requestId] = completion
    }

    func searchMusic(keyword: String,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion: @escaping (String, AgoraMusicContentCenterStateReason, AgoraMusicCollection) -> Void) {
        agoraPrint("searchMusic with keyword: \(keyword)")
        let dict: [String: Any] = [
                    "keyword": keyword,
                    "page": page,
                    "pageSize": pageSize,
                    "jsonOption": jsonOption
        ]
        sendCustomMessage(with: "searchMusic", dict: dict)
        let requestId = mcc!.searchMusic(keyWord: keyword, page: page, pageSize: pageSize, jsonOption: jsonOption)
        musicSearchDict[requestId] = completion
    }

//    func switchSingerRole(newRole: KTVSingRole, onSwitchRoleState: @escaping (KTVSwitchRoleState, KTVSwitchRoleFailReason) -> Void) {
//        let oldRole = singerRole
//        self.switchSingerRole(oldRole: oldRole, newRole: newRole, token: apiConfig?.chorusChannelToken ?? "", stateCallBack: onSwitchRoleState)
//    }

    /**
     * 恢复播放
     */
    @objc public func resumeSing() {
        sendCustomMessage(with: "resumeSing", dict: [:])
        agoraPrint("resumeSing")
        if mediaPlayer?.getPlayerState() == .paused {
            mediaPlayer?.resume()
        } else {
            let ret = mediaPlayer?.play()
            agoraPrint("resumeSing ret: \(ret ?? -1)")
        }
    }

    /**
     * 暂停播放
     */
    @objc public func pauseSing() {
        sendCustomMessage(with: "pauseSing", dict: [:])
        agoraPrint("pauseSing")
        mediaPlayer?.pause()
    }

    /**
     * 调整进度
     */
    @objc public func seekSing(time: NSInteger) {
        sendCustomMessage(with: "seekSing", dict: ["time":time])
        agoraPrint("seekSing")
        mediaPlayer?.seek(toPosition: time)
    }

    /**
     * 选择音轨，原唱、伴唱
     */
//    @objc public func selectPlayerTrackMode(mode: KTVPlayerTrackMode) {
//        apiConfig?.engine.selectAudioTrack(mode == .original ? 0 : 1)
//    }

    /**
     * 设置当前mic开关状态
     */
    @objc public func muteMic(muteStatus: Bool) {
        sendCustomMessage(with: "setMicStatus", dict: ["muteStatus":muteStatus])
        agoraPrint("setMicStatus status:\(muteStatus)")
        self.isNowMicMuted = muteStatus
        if self.singerRole == .leadSinger || self.singerRole == .soloSinger {
            apiConfig?.engine?.adjustRecordingSignalVolume(muteStatus ? 0 : 100)
        } else {
            apiConfig?.engine?.muteLocalAudioStream(muteStatus)
        }
    }
    
    @objc public func removeMusic(songCode: Int) {
        sendCustomMessage(with: "removeMusic", dict: ["songCode": songCode])
        agoraPrint("removeMusic:\(songCode)")
        let ret: Int = mcc?.removeCache(songCode: songCode) ?? 0
        if ret < 0 {
            agoraPrint("removeMusic failed: ret:\(ret)")
        }
    }

    private func agoraPrint(_ message: String) {
        apiRepoter?.writeLog(content: message, level: .info)
    }
    
    private func agoraPrintError(_ message: String) {
        apiRepoter?.writeLog(content: message, level: .error)
    }

}

// 主要是角色切换，加入合唱，加入多频道，退出合唱，退出多频道
extension KTVGiantChorusApiImpl {
//    private func switchSingerRole(oldRole: KTVSingRole, newRole: KTVSingRole, token: String, stateCallBack:@escaping ISwitchRoleStateListener) {
//    //    agoraPrint("switchSingerRole oldRole: \(oldRole.rawValue), newRole: \(newRole.rawValue)")
//        if oldRole == .audience && newRole == .soloSinger {
//            // 1、KTVSingRoleAudience -》KTVSingRoleMainSinger
//            singerRole = newRole
//            becomeSoloSinger()
//            getEventHander { delegate in
//                delegate.onSingerRoleChanged(oldRole: .audience, newRole: .soloSinger)
//            }
//
//            stateCallBack(.success, .none)
//        } else if oldRole == .audience && newRole == .leadSinger {
//            becomeSoloSinger()
//            joinChorus(role: newRole, token: token, joinExChannelCallBack: {[weak self] flag, status in
//                guard let self = self else {return}
//                //还原临时变量为观众
//                self.joinChorusNewRole = .audience
//
//                if flag == true {
//                    self.singerRole = newRole
//                    self.getEventHander { delegate in
//                        delegate.onSingerRoleChanged(oldRole: .audience, newRole: .leadSinger)
//                    }
//                    stateCallBack(.success, .none)
//                } else {
//                    self.leaveChorus(role: .leadSinger)
//                    stateCallBack(.fail, .joinChannelFail)
//                }
//            })
//
//        } else if oldRole == .soloSinger && newRole == .audience {
//            stopSing()
//            singerRole = newRole
//            getEventHander { delegate in
//                delegate.onSingerRoleChanged(oldRole: .soloSinger, newRole: .audience)
//            }
//
//            stateCallBack(.success, .none)
//        } else if oldRole == .audience && newRole == .coSinger {
//            joinChorus(role: newRole, token: token, joinExChannelCallBack: {[weak self] flag, status in
//                guard let self = self else {return}
//                //还原临时变量为观众
//                self.joinChorusNewRole = .audience
//                if flag == true {
//                    self.singerRole = newRole
//                    //TODO(chenpan):如果观众变成伴唱，需要重置state，防止同步主唱state因为都是playing不会修改
//                    //后面建议改成remote state(通过data stream获取)和local state(通过player didChangedToState获取)
//                    self.playerState = self.mediaPlayer?.getPlayerState() ?? .idle
//                    self.getEventHander { delegate in
//                        delegate.onSingerRoleChanged(oldRole: .audience, newRole: .coSinger)
//                    }
//                    stateCallBack(.success, .none)
//                } else {
//                    self.leaveChorus(role: .coSinger)
//                    stateCallBack(.fail, .joinChannelFail)
//                }
//            })
//        } else if oldRole == .coSinger && newRole == .audience {
//            leaveChorus(role: .coSinger)
//            singerRole = newRole
//            getEventHander { delegate in
//                delegate.onSingerRoleChanged(oldRole: .coSinger, newRole: .audience)
//            }
//
//            stateCallBack(.success, .none)
//        } else if oldRole == .soloSinger && newRole == .leadSinger {
//            joinChorus(role: newRole, token: token, joinExChannelCallBack: {[weak self] flag, status in
//                guard let self = self else {return}
//                //还原临时变量为观众
//                self.joinChorusNewRole = .audience
//                if flag == true {
//                    self.singerRole = newRole
//                    self.getEventHander { delegate in
//                        delegate.onSingerRoleChanged(oldRole: .soloSinger, newRole: .leadSinger)
//                    }
//                    stateCallBack(.success, .none)
//                } else {
//                    self.leaveChorus(role: .leadSinger)
//                    stateCallBack(.fail, .joinChannelFail)
//                }
//            })
//        } else if oldRole == .leadSinger && newRole == .soloSinger {
//            leaveChorus(role: .leadSinger)
//            singerRole = newRole
//            getEventHander { delegate in
//                delegate.onSingerRoleChanged(oldRole: .leadSinger, newRole: .soloSinger)
//            }
//
//            stateCallBack(.success, .none)
//        } else if oldRole == .leadSinger && newRole == .audience {
//            leaveChorus(role: .leadSinger)
//            stopSing()
//            singerRole = newRole
//            getEventHander { delegate in
//                delegate.onSingerRoleChanged(oldRole: .leadSinger, newRole: .audience)
//            }
//
//            stateCallBack(.success, .none)
//        } else {
//            stateCallBack(.fail, .noPermission)
//            agoraPrint("Error！You can not switch role from \(oldRole.rawValue) to \(newRole.rawValue)!")
//        }
//
//    }
    
    func switchSingerRole(newRole: KTVSingRole, onSwitchRoleState: @escaping (KTVSwitchRoleState, KTVSwitchRoleFailReason) -> Void) {
        
        agoraPrint("switchSingerRole oldRole: \(singerRole), newRole: \(newRole)")
        let oldRole = singerRole
        
        if singerRole == .audience && newRole == .leadSinger {
            // 1、Audience -》LeadSinger
            // 离开观众频道
            apiConfig?.engine?.leaveChannelEx(AgoraRtcConnection(channelId: apiConfig?.channelName ?? "", localUid: apiConfig?.localUid ?? 0))
            joinChorus(newRole: newRole)
            self.singerRole = newRole
            self.getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: .audience, newRole: .leadSinger)
            }
            onSwitchRoleState(.success, .none)
        } else if singerRole == .audience && newRole == .coSinger {
            // 2、Audience -》CoSinger
            // 离开观众频道
            apiConfig?.engine?.leaveChannelEx(AgoraRtcConnection( channelId: apiConfig?.channelName ?? "", localUid: apiConfig?.localUid ?? 0))
            joinChorus(newRole: newRole)
            singerRole = newRole
            self.getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: .audience, newRole: .coSinger)
            }
            onSwitchRoleState(.success, .none)
        } else if singerRole == .coSinger && newRole == .audience {
            // 3、CoSinger -》Audience
            leaveChorus2(role: singerRole)
            // 加入观众频道
            apiConfig?.engine?.joinChannelEx(byToken: apiConfig?.audienceChannelToken,
                                             connection: AgoraRtcConnection(channelId: apiConfig?.channelName ?? "", localUid: apiConfig?.localUid ?? 0),
                                             delegate: self,
                                             mediaOptions: AgoraRtcChannelMediaOptions(),
                                             joinSuccess: {[weak self] _,_, _ in
            })
            apiConfig?.engine?.setParametersEx("{\"rtc.use_audio4\": true}", connection: AgoraRtcConnection(channelId: apiConfig?.channelName ?? "", localUid: apiConfig?.localUid ?? 0))
            self.singerRole = newRole
            self.getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: oldRole, newRole: newRole)
            }
            onSwitchRoleState(.success, .none)
        } else if singerRole == .leadSinger && newRole == .audience {
            // 4、LeadSinger -》Audience
            stopSing()
            leaveChorus2(role: singerRole)
            // 加入观众频道
            apiConfig?.engine?.joinChannelEx(byToken: apiConfig?.audienceChannelToken,
                                             connection: AgoraRtcConnection(channelId: apiConfig?.channelName ?? "", localUid: apiConfig?.localUid ?? 0),
                                             delegate: self,
                                             mediaOptions: AgoraRtcChannelMediaOptions(),
                                             joinSuccess: {[weak self] _,_, _ in
            })
            apiConfig?.engine?.setParametersEx("{\"rtc.use_audio4\": true}", connection: AgoraRtcConnection(channelId: apiConfig?.channelName ?? "", localUid: apiConfig?.localUid ?? 0))
            self.singerRole = newRole
            self.getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: oldRole, newRole: newRole)
            }
            onSwitchRoleState(.success, .none)
        } else {
            onSwitchRoleState(.fail, .noPermission)
            print("Error! You can not switch role from \(singerRole) to \(newRole)!")
        }
    }

    private func becomeSoloSinger() {
        apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
        apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
        apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 80000}")
        apiConfig?.engine?.setAudioScenario(.chorus)
        agoraPrint("becomeSoloSinger")
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.autoSubscribeAudio = true
        //mediaOption.autoSubscribeVideo = true
        if apiConfig?.musicType == .mcc {
            mediaOption.publishMediaPlayerId = Int(mediaPlayer?.getMediaPlayerId() ?? 0)
        } else {
            mediaOption.publishMediaPlayerId = Int(mediaPlayer?.getMediaPlayerId() ?? 0)
        }
        mediaOption.publishMediaPlayerAudioTrack = true
        apiConfig?.engine?.updateChannel(with: mediaOption)
    }

    /**
     * 加入合唱
     */
    private func joinChorus(role: KTVSingRole, token: String, joinExChannelCallBack: @escaping JoinExChannelCallBack) {
        self.onJoinExChannelCallBack = joinExChannelCallBack
        if role == .leadSinger {
            agoraPrint("joinChorus: KTVSingRoleMainSinger")
            joinChorus2ndChannel(newRole: role, token: token)
        } else if role == .coSinger {
            
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.autoSubscribeAudio = true
           // mediaOption.autoSubscribeVideo = true
            mediaOption.publishMediaPlayerAudioTrack = false
            apiConfig?.engine?.updateChannel(with: mediaOption)
            
            if apiConfig?.musicType == .mcc {
                (mediaPlayer as? AgoraMusicPlayerProtocol)?.openMedia(songCode: self.songCode , startPos: 0)
            } else {
                mediaPlayer?.open(self.songUrl, startPos: 0)
            }
            
            joinChorus2ndChannel(newRole: role, token: token)

        } else if role == .audience {
            agoraPrint("joinChorus fail!")
        }
    }

    private func joinChorus2ndChannel(newRole: KTVSingRole, token: String) {
        let role = newRole
        if role == .soloSinger || role == .audience {
            agoraPrint("joinChorus2ndChannel with wrong role")
            return
        }
        
        agoraPrint("joinChorus2ndChannel role: \(role.rawValue)")
        if newRole == .coSinger {
            apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
            apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
            apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 48000}")
            apiConfig?.engine?.setAudioScenario(.chorus)
        }

        let mediaOption = AgoraRtcChannelMediaOptions()
        // main singer do not subscribe 2nd channel
        // co singer auto sub
        mediaOption.autoSubscribeAudio = role != .leadSinger
      //  mediaOption.autoSubscribeVideo = false
        mediaOption.publishMicrophoneTrack = newRole == .leadSinger
        mediaOption.enableAudioRecordingOrPlayout = role != .leadSinger
        mediaOption.clientRoleType = .broadcaster

        let rtcConnection = AgoraRtcConnection()
        rtcConnection.channelId = apiConfig?.chorusChannelName ?? ""
        rtcConnection.localUid = UInt(apiConfig?.localUid ?? 0)
       subChorusConnection = rtcConnection

        joinChorusNewRole = role
       let ret = apiConfig?.engine?.joinChannelEx(byToken: token, connection: rtcConnection, delegate: self, mediaOptions: mediaOption, joinSuccess: nil)
        agoraPrint("joinChannelEx ret: \(ret ?? -999)")
        if newRole == .coSinger {
            let uid = UInt(songConfig?.mainSingerUid ?? 0)
            let ret =
            apiConfig?.engine?.muteRemoteAudioStreamEx(uid, mute: false, connection: singChannelConnection ?? AgoraRtcConnection())
            agoraPrint("muteRemoteAudioStream: \(uid), ret: \(ret ?? -1)")
        }
        apiConfig?.engine?.setParametersEx("{\"rtc.use_audio4\": true}", connection: rtcConnection)

    }

    private func leaveChorus2ndChannel(_ role: KTVSingRole) {
        guard let config = songConfig else {return}
        guard let subConn = subChorusConnection else {return}
        if (role == .leadSinger) {
            apiConfig?.engine?.leaveChannelEx(subConn)
        } else if (role == .coSinger) {
            apiConfig?.engine?.leaveChannelEx(subConn)
            apiConfig?.engine?.muteRemoteAudioStreamEx(UInt(config.mainSingerUid), mute: false, connection: singChannelConnection ?? AgoraRtcConnection())
        }
    }

    /**
     * 离开合唱
     */

    private func leaveChorus(role: KTVSingRole) {
        agoraPrint("leaveChorus role: \(singerRole.rawValue)")
        if role == .leadSinger {
            mainSingerHasJoinChannelEx = false
            leaveChorus2ndChannel(role)
        } else if role == .coSinger {
            mediaPlayer?.stop()
            let mediaOption = AgoraRtcChannelMediaOptions()
          //  mediaOption.autoSubscribeAudio = true
         //   mediaOption.autoSubscribeVideo = false
            mediaOption.publishMediaPlayerAudioTrack = false
            apiConfig?.engine?.updateChannel(with: mediaOption)
            leaveChorus2ndChannel(role)
            apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
            apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
            apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 48000}")
            apiConfig?.engine?.setAudioScenario(.gameStreaming)
        } else if role == .audience {
            agoraPrint("joinChorus: KTVSingRoleAudience does not need to leaveChorus!")
        }
    }

}

extension KTVGiantChorusApiImpl {
    
    private func getEventHander(callBack:((KTVApiEventHandlerDelegate)-> Void)) {
        for obj in eventHandlers.allObjects {
            if obj is KTVApiEventHandlerDelegate {
                callBack(obj as! KTVApiEventHandlerDelegate)
            }
        }
    }
    
    private func _loadMusic(config: KTVSongConfiguration, mode: KTVLoadMusicMode, onMusicLoadStateListener: IMusicLoadStateListener){
        
        songConfig = config
        lastReceivedPosition = 0
        localPosition = 0

        if (config.mode == .loadNone) {
            return
        }
        
        if mode == .loadLrcOnly {
            loadLyric(with: songCode) { [weak self] url in
                guard let self = self else { return }
                agoraPrint("loadLrcOnly: songCode:\(self.songCode) ulr:\(String(describing: url))")
//                if self.songCode != songCode {
//                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .cancled)
//                    return
//                }
                if let urlPath = url, !urlPath.isEmpty {
                    self.lyricUrlMap[String(self.songCode)] = urlPath
                    self.setLyric(with: urlPath) { lyricUrl in
                        onMusicLoadStateListener.onMusicLoadSuccess(songCode: self.songCode, lyricUrl: urlPath)
                    }
                } else {
                    onMusicLoadStateListener.onMusicLoadFail(songCode: self.songCode, reason: .noLyricUrl)
                }
                
//                if (config.autoPlay) {
//                    // 主唱自动播放歌曲
//                    if self.singerRole != .leadSinger {
//                        self.switchSingerRole(newRole: .soloSinger) { _, _ in
//
//                        }
//                    }
//                    self.startSing(songCode: self.songCode, startPos: 0)
//                }
            }
        } else {
            loadMusicListeners.setObject(onMusicLoadStateListener, forKey: "\(self.songCode)" as NSString)
            onMusicLoadStateListener.onMusicLoadProgress(songCode: self.songCode, percent: 0, state: .preloading, msg: "", lyricUrl: "")
            // TODO: 只有未缓存时才显示进度条
            if mcc?.isPreloaded(songCode: songCode) != 0 {
                onMusicLoadStateListener.onMusicLoadProgress(songCode: self.songCode, percent: 0, state: .preloading, msg: "", lyricUrl: "")
            }
 
            preloadMusic(with: songCode) { [weak self] status, songCode in
                guard let self = self else { return }
                if self.songCode != songCode {
                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .cancled)
                    return
                }
                if status == .OK {
                    if mode == .loadMusicAndLrc {
                        // 需要加载歌词
                        self.loadLyric(with: songCode) { url in
                            self.agoraPrint("loadMusicAndLrc: songCode:\(songCode) status:\(status.rawValue) ulr:\(String(describing: url))")
                            if self.songCode != songCode {
                                onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .cancled)
                                return
                            }
                            if let urlPath = url, !urlPath.isEmpty {
                                self.lyricUrlMap[String(songCode)] = urlPath
                                self.setLyric(with: urlPath) { lyricUrl in
                                    onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: urlPath)
                                }
                            } else {
                                onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .noLyricUrl)
                            }
//                            if config.autoPlay {
//                                // 主唱自动播放歌曲
//                                if self.singerRole != .leadSinger {
//                                    self.switchSingerRole(newRole: .soloSinger) { _, _ in
//
//                                    }
//                                }
//                                self.startSing(songCode: self.songCode, startPos: 0)
//                            }
                        }
                    } else if mode == .loadMusicOnly {
                        agoraPrint("loadMusicOnly: songCode:\(songCode) load success")
//                        if config.autoPlay {
//                            // 主唱自动播放歌曲
//                            if self.singerRole != .leadSinger {
//                                self.switchSingerRole(newRole: .soloSinger) { _, _ in
//
//                                }
//                            }
//                            self.startSing(songCode: self.songCode, startPos: 0)
//                        }
                        onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: "")
                    }
                } else {
                    agoraPrint("load music failed songCode:\(songCode)")
                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .musicPreloadFail)
                }
            }
        }
    }
    
    private func loadLyric(with songCode: NSInteger, callBack:@escaping LyricCallback) {
        agoraPrint("loadLyric songCode: \(songCode)")
        let requestId: String = self.mcc?.getLyric(songCode: songCode, lyricType: 0) ?? ""
        self.lyricCallbacks.updateValue(callBack, forKey: requestId)
    }
    
    private func preloadMusic(with songCode: Int, callback: @escaping LoadMusicCallback) {
        agoraPrint("preloadMusic songCode: \(songCode)")
        if self.mcc?.isPreloaded(songCode: songCode) == 0 {
            musicCallbacks.removeValue(forKey: String(songCode))
            callback(.OK, songCode)
            return
        }
        let err = self.mcc?.preload(songCode: songCode)
        if err == nil {
            musicCallbacks.removeValue(forKey: String(songCode))
            callback(.error, songCode)
            return
        }
        musicCallbacks.updateValue(callback, forKey: String(songCode))
    }
    
    private func setLyric(with url: String, callBack: @escaping LyricCallback) {
        agoraPrint("setLyric url: (url)")
        self.lrcControl?.onDownloadLrcData(url: url)
        callBack(url)
    }

    func startSing(songCode: Int, startPos: Int) {
        let dict: [String: Any] = [
            "songCode": songCode,
            "startPos": startPos
        ]
        sendCustomMessage(with: "startSing", dict: dict)
        let role = singerRole
        agoraPrint("startSing role: \(role.rawValue)")
        if self.songCode != songCode {
            agoraPrint("startSing failed: canceled")
            return
        }
        mediaPlayer?.setPlayerOption("enable_multi_audio_track", value: 1)
        apiConfig?.engine?.adjustPlaybackSignalVolume(Int(remoteVolume))
        let ret = (mediaPlayer as? AgoraMusicPlayerProtocol)?.openMedia(songCode: songCode, startPos: startPos)
        mediaPlayer?.setLoopCount(-1)
        agoraPrint("startSing->openMedia(\(songCode) fail: \(ret ?? -1)")
    }
    
    func startSing(url: String, startPos: Int) {
        let dict: [String: Any] = [
            "url": url,
            "startPos": startPos
        ]
        sendCustomMessage(with: "startSing", dict: dict)
        let role = singerRole
        agoraPrint("startSing role: \(role.rawValue)")
        if self.songUrl != songUrl {
            agoraPrint("startSing failed: canceled")
            return
        }
        apiConfig?.engine?.adjustPlaybackSignalVolume(Int(remoteVolume))
        let ret = mediaPlayer?.open(url, startPos: startPos)
        agoraPrint("startSing->openMedia(\(url) fail: \(ret ?? -1)")
    }

    /**
     * 停止播放歌曲
     */
    @objc public func stopSing() {
        agoraPrint("stopSing")
        sendCustomMessage(with: "stopSing", dict: [:])
        let mediaOption = AgoraRtcChannelMediaOptions()
      //  mediaOption.autoSubscribeAudio = true
      //  mediaOption.autoSubscribeVideo = true
        mediaOption.publishMediaPlayerAudioTrack = false
        apiConfig?.engine?.updateChannelEx(with: mediaOption, connection: singChannelConnection ?? AgoraRtcConnection())

        if mediaPlayer?.getPlayerState() != .stopped {
            mediaPlayer?.stop()
        }

        apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
        apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
        apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 48000}")
        apiConfig?.engine?.setAudioScenario(.gameStreaming)
    }
    
    @objc public func setSingingScore(score: Int) {
        self.singingScore = score
    }
    
    @objc func setAudienceStreamMessage(dict: [String: Any]) {
        sendStreamMessageWithDict(dict) { _ in
            
        }
    }
    
    @objc public func setAudioPlayoutDelay(audioPlayoutDelay: Int) {
        self.audioPlayoutDelay = audioPlayoutDelay
    }
    
    @objc func enableProfessionalStreamerMode(_ enable: Bool)   {
        if self.isPublishAudio == false {return}
        self.enableProfessional = enable
        //专业非专业还需要根据是否佩戴耳机来判断是否开启3A
        apiConfig?.engine?.setAudioProfile(enable ? .musicHighQualityStereo : .musicStandardStereo)
        apiConfig?.engine?.setParameters("{\"che.audio.aec.enable\":\((enable && isWearingHeadPhones) ? false : true)}")
        apiConfig?.engine?.setParameters("{\"che.audio.agc.enable\":\((enable && isWearingHeadPhones) ? false : true)}")
        apiConfig?.engine?.setParameters("{\"che.audio.ans.enable\":\((enable && isWearingHeadPhones) ? false : true)}")
        apiConfig?.engine?.setParameters("{\"che.audio.md.enable\": false}")
    }
    
    func joinChorus(newRole: KTVSingRole) {
        agoraPrint("joinChorus: \(newRole)")
        let singChannelMediaOptions = AgoraRtcChannelMediaOptions()
        singChannelMediaOptions.autoSubscribeAudio = true
        singChannelMediaOptions.publishMicrophoneTrack = true
        singChannelMediaOptions.clientRoleType = .broadcaster
//        singChannelMediaOptions.parameters = "{\"che.audio.max_mixed_participants\": 8}"
        if newRole == .leadSinger {
            // 主唱不参加TopN
            singChannelMediaOptions.isAudioFilterable = false
            apiConfig?.engine?.setParameters("{\"che.audio.filter_streams\":\(apiConfig?.routeSelectionConfig.streamNum ?? 0)}")
        } else {
            apiConfig?.engine?.setParameters("{\"che.audio.filter_streams\":\((apiConfig?.routeSelectionConfig.streamNum ?? 0) - 1)}")
        }
        
        guard let token = apiConfig?.chorusChannelToken, let singConnection = singChannelConnection else {return}
        
        
        // 加入演唱频道
       let ret = apiConfig?.engine?.joinChannelEx(byToken: token, connection: singConnection, delegate: self, mediaOptions: singChannelMediaOptions)
        apiConfig?.engine?.setParametersEx("{\"rtc.use_audio4\": true}", connection: singConnection)
        if apiConfig?.routeSelectionConfig.type == .topN || apiConfig?.routeSelectionConfig.type == .byDelayAndTopN {
            if newRole == .leadSinger {
                apiConfig?.engine?.setParameters("{\"che.audio.filter_streams\":\(apiConfig?.routeSelectionConfig.streamNum)}")
            } else {
                apiConfig?.engine?.setParameters("{\"che.audio.filter_streams\":\((apiConfig?.routeSelectionConfig.streamNum ?? 0) - 1)}")
            }
        } else {
            apiConfig?.engine?.setParameters("{\"che.audio.filter_streams\": 0}")
        }
        
       let res = apiConfig?.engine?.enableAudioVolumeIndicationEx(50, smooth: 10, reportVad: true, connection: singConnection)
        switch newRole {
            case .leadSinger:
                // 更新音频配置
                apiConfig?.engine?.setAudioScenario(.chorus)
                apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
                apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
                apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 80000}")

                // mpk流加入频道
                let options = AgoraRtcChannelMediaOptions()
                options.autoSubscribeAudio = false
                options.autoSubscribeVideo = false
                options.publishMicrophoneTrack = false
                options.publishMediaPlayerAudioTrack = true
                options.publishMediaPlayerId = Int(mediaPlayer?.getMediaPlayerId() ?? 0)
                options.clientRoleType = .broadcaster
                // 防止主唱和合唱听见mpk流的声音
                options.enableAudioRecordingOrPlayout = false

                let rtcConnection = AgoraRtcConnection()
                rtcConnection.channelId = apiConfig?.chorusChannelName ?? ""
                rtcConnection.localUid = UInt(apiConfig?.musicStreamUid ?? 0)
                mpkConnection = rtcConnection
                
                // 加入演唱频道
                let delegate = NSObject()
                let ret = apiConfig?.engine?.joinChannelEx(byToken: apiConfig?.musicChannelToken, connection: mpkConnection ?? AgoraRtcConnection(), delegate: nil, mediaOptions: options)
                apiConfig?.engine?.setParametersEx("{\"rtc.use_audio4\": true}", connection: mpkConnection ?? AgoraRtcConnection())


        case .coSinger:
            // 防止主唱和合唱听见mpk流的声音
            apiConfig?.engine?.muteRemoteAudioStreamEx(UInt(apiConfig?.musicStreamUid ?? 0), mute: true, connection: singChannelConnection ?? AgoraRtcConnection())

            // 更新音频配置
            apiConfig?.engine?.setAudioScenario(.chorus)
            apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
            apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
            apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 48000}")

            // 预加载歌曲成功
            // 导唱
            mediaPlayer?.setPlayerOption("enable_multi_audio_track", value: 1)
            if apiConfig?.musicType == .mcc {
                (mediaPlayer as? AgoraMusicPlayerProtocol)?.openMedia(songCode: self.songCode , startPos: 0) // TODO open failed
            } else {
                mediaPlayer?.open(songUrl, startPos: 0) // TODO open failed
            }
        default:
            agoraPrintError("JoinChorus with Wrong role: \(singerRole)")
        }

        
        apiConfig?.engine?.muteRemoteAudioStreamEx(UInt(apiConfig?.musicStreamUid ?? 0), mute: true, connection: singChannelConnection ?? AgoraRtcConnection())
        // 加入演唱频道后，创建data stream
        renewInnerDataStreamId()
    }

    func leaveChorus2(role: KTVSingRole) {
        agoraPrint("leaveChorus: \(role)")
        switch role {
        case .leadSinger:
            apiConfig?.engine?.leaveChannelEx(mpkConnection ?? AgoraRtcConnection())
        case .coSinger:
            mediaPlayer?.stop()

            // 更新音频配置
            apiConfig?.engine?.setAudioScenario(.gameStreaming)
            apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
            apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
            apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 48000}")
        default:
            agoraPrint("JoinChorus with wrong role: \(singerRole)")
        }
        apiConfig?.engine?.leaveChannelEx(singChannelConnection ?? AgoraRtcConnection())
    }

}

// rtc的代理回调
extension KTVGiantChorusApiImpl: AgoraRtcEngineDelegate {

     public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        agoraPrint("didJoinChannel channel:\(channel) uid: \(uid)")
        if joinChorusNewRole == .leadSinger {
            mainSingerHasJoinChannelEx = true
            onJoinExChannelCallBack?(true, nil)
        }
        if joinChorusNewRole == .coSinger {
          self.onJoinExChannelCallBack?(true, nil)
        }
        if let subChorusConnection = subChorusConnection {
            apiConfig?.engine?.enableAudioVolumeIndicationEx(50, smooth: 10, reportVad: true, connection: subChorusConnection)
        }
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        if errorCode != .joinChannelRejected {return}
        agoraPrintError("join ex channel failed")
        engine.setAudioScenario(.gameStreaming)
        if joinChorusNewRole == .leadSinger {
            mainSingerHasJoinChannelEx = false
            onJoinExChannelCallBack?(false, .joinChannelFail)
        }

        if joinChorusNewRole == .coSinger {
            self.onJoinExChannelCallBack?(false, .joinChannelFail)
        }
    }
    
    //合唱频道的声音回调
    public func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        getEventHander { delegate in
            delegate.onChorusChannelAudioVolumeIndication(speakers: speakers, totalVolume: totalVolume)
        }
        didKTVAPIReceiveAudioVolumeIndication(with: speakers, totalVolume: totalVolume)
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        getEventHander { delegate in
            delegate.onTokenPrivilegeWillExpire()
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        didKTVAPIReceiveStreamMessageFrom(uid: NSInteger(uid), streamId: streamId, data: data)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, audioMetadataReceived uid: UInt, metadata: Data) {
        guard let time: LrcTime = try? LrcTime(serializedData: metadata) else {return}
        if time.type == .lrcTime && self.singerRole == .audience {
            self.setProgress(with: Int(time.ts))
       }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        guard let musicId = apiConfig?.musicStreamUid,let mainSingerId = songConfig?.mainSingerUid else {return}
        if uid != musicId && subScribeSingerMap.count < 8 {
            apiConfig?.engine?.muteRemoteAudioStreamEx(uid, mute: false, connection: singChannelConnection ?? AgoraRtcConnection())
            if uid != mainSingerId {
                subScribeSingerMap[Int(uid)] = 0
            }
        } else if uid != musicId && subScribeSingerMap.count == 8 {
            apiConfig?.engine?.muteRemoteAudioStreamEx(uid, mute: false, connection: singChannelConnection ?? AgoraRtcConnection())
        }
        if uid != musicId && uid != mainSingerId {
            singerList.append(Int(uid))
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didLeaveChannelWith stats: AgoraChannelStats) {
        subScribeSingerMap.removeAll()
        singerList.removeAll()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        subScribeSingerMap.removeValue(forKey: Int(uid))
        if let index = singerList.firstIndex(of: Int(uid)) {
            singerList.remove(at: index)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
        guard let musicId = apiConfig?.musicStreamUid,let mainSingerId = songConfig?.mainSingerUid else {return}
        if apiConfig?.routeSelectionConfig.type == .random || apiConfig?.routeSelectionConfig.type == .topN { return }
        let uid = stats.uid
        if uid == mainSingerId {
            mainSingerDelay = stats.e2eDelay
        }
        if uid != mainSingerId && uid != musicId && subScribeSingerMap[Int(uid)] != nil {
            subScribeSingerMap[Int(uid)] = stats.e2eDelay
        }
    }
}

//需要外部转发的方法 主要是dataStream相关的
extension KTVGiantChorusApiImpl {
    
    @objc func didAudioPublishStateChange(newState: AgoraStreamPublishState) {
        self.isPublishAudio = newState == .published
        enableProfessionalStreamerMode(self.enableProfessional)
        agoraPrint("PublishStateChange:\(newState)")
    }
    
    @objc func didAudioRouteChanged( routing: AgoraAudioOutputRouting) {
        agoraPrint("Route changed:\(routing)")
        self.audioRouting = routing.rawValue
        let headPhones: [AgoraAudioOutputRouting] = [.headset, .bluetoothDeviceHfp, .bluetoothDeviceA2dp, .headsetNoMic]
        let wearHeadPhone: Bool = headPhones.contains(routing)
        if wearHeadPhone == self.isWearingHeadPhones {
            return
        }
        self.isWearingHeadPhones = wearHeadPhone
        enableProfessionalStreamerMode(self.enableProfessional)
    }
    
    @objc public func didKTVAPIReceiveStreamMessageFrom(uid: NSInteger, streamId: NSInteger, data: Data) {
        let role = singerRole
        if isRelease {return}
        guard let dict = dataToDictionary(data: data), let cmd = dict["cmd"] as? String else { return }
        agoraPrint("recv dict:\(dict)")
        switch cmd {
        case "setLrcTime":
            handleSetLrcTimeCommand(dict: dict, role: role)
        case "PlayerState":
            handlePlayerStateCommand(dict: dict, role: role)
        case "setVoicePitch":
            handleSetVoicePitchCommand(dict: dict, role: role)
        default:
            break
        }
    }
    
    private func handleSetLrcTimeCommand(dict: [String: Any], role: KTVSingRole) {
        guard let position = dict["time"] as? Int64,
                let duration = dict["duration"] as? Int64,
                let realPosition = dict["realTime"] as? Int64,
               // let songCode = dict["songCode"] as? Int64,
                let mainSingerState = dict["playerState"] as? Int,
                let ntpTime = dict["ntp"] as? Int,
                let songId = dict["songIdentifier"] as? String
        else { return }
        #if DUBUG
            print("realTime:\(realPosition) position:\(position) lastNtpTime:\(lastNtpTime) ntpTime:\(ntpTime) ntpGap:\(ntpTime - self.lastNtpTime) ")
        #endif
        //如果接收到的歌曲和自己本地的歌曲不一致就不更新进度
//        guard songCode == self.songCode else {
//            agoraPrint("local songCode[\(songCode)] is not equal to recv songCode[\(self.songCode)] role: \(singerRole.rawValue)")
//            return
//        }

        self.lastNtpTime = ntpTime
        self.remotePlayerDuration = TimeInterval(duration)
        
        let state = AgoraMediaPlayerState(rawValue: mainSingerState) ?? .stopped
//        self.lastMainSingerUpdateTime = Date().milListamp
//        self.remotePlayerPosition = TimeInterval(realPosition)
        if self.playerState != state {
            #if DUBUG
            print("[setLrcTime] recv state: \(self.playerState.rawValue)->\(state.rawValue) role: \(singerRole.rawValue) role: \(singerRole.rawValue)")
            #endif
            if state == .playing, singerRole == .coSinger, playerState == .openCompleted {
                //如果是伴唱等待主唱开始播放，seek 到指定位置开始播放保证歌词显示位置准确
                self.localPlayerPosition = self.lastMainSingerUpdateTime - Double(position)
                print("localPlayerPosition:playerKit:handleSetLrcTimeCommand \(localPlayerPosition)")
                agoraPrint("seek toPosition: \(position)")
                mediaPlayer?.seek(toPosition: Int(position))
            }
            
            syncPlayStateFromRemote(state: state, needDisplay: false)
        }

        if role == .coSinger {
            self.lastMainSingerUpdateTime = Date().milListamp
            self.remotePlayerPosition = TimeInterval(realPosition)
            handleCoSingerRole(dict: dict)
        } else if role == .audience {
            if dict.keys.contains("ver") {
                recvFromDataStream = false
            } else {
                recvFromDataStream = true
                if self.songIdentifier == songId  {
                    self.lastMainSingerUpdateTime = Date().milListamp
                    self.remotePlayerPosition = TimeInterval(realPosition)
                } else {
                    self.lastMainSingerUpdateTime = 0
                    self.remotePlayerPosition = 0
                }
                handleAudienceRole(dict: dict)
            }
        }
    }
    
    private func handlePlayerStateCommand(dict: [String: Any], role: KTVSingRole) {
        let mainSingerState: Int = dict["state"] as? Int ?? 0
        let state = AgoraMediaPlayerState(rawValue: mainSingerState) ?? .idle

//        if state == .playing, singerRole == .coSinger, playerState == .openCompleted {
//            //如果是伴唱等待主唱开始播放，seek 到指定位置开始播放保证歌词显示位置准确
//            self.localPlayerPosition = getPlayerCurrentTime()
//            print("localPlayerPosition:playerKit:handlePlayerStateCommand \(localPlayerPosition)")
//            agoraPrint("seek toPosition: \(self.localPlayerPosition)")
//            mediaPlayer?.seek(toPosition: Int(self.localPlayerPosition))
//        }

        agoraPrint("recv state with MainSinger: \(state.rawValue)")
        syncPlayStateFromRemote(state: state, needDisplay: true)
    }

    private func handleSetVoicePitchCommand(dict: [String: Any], role: KTVSingRole) {
        if role == .audience, let voicePitch = dict["pitch"] as? Double {
            self.pitch = voicePitch
        }
    }

    private func handleCoSingerRole(dict: [String: Any]) {

        if mediaPlayer?.getPlayerState() == .playing {
            let localNtpTime = getNtpTimeInMs()
            let localPosition = localNtpTime - Int(localPlayerSystemTime) + localPosition
            let expectPosition = Int(dict["time"] as? Int64 ?? 0) + localNtpTime - Int(dict["ntp"] as? Int64 ?? 0) + self.audioPlayoutDelay
            let threshold = expectPosition - Int(localPosition)
            let ntpTime = dict["ntp"] as? Int ?? 0
            let time = dict["time"] as? Int64 ?? 0
            #if DUBUG
            agoraPrint("checkNtp, diff:\(threshold), localNtp:\(getNtpTimeInMs()), localPosition:\(localPosition), audioPlayoutDelay:\(audioPlayoutDelay), remoteDiff:\(String(describing: ntpTime - Int(time)))")
            #endif
            if abs(threshold) > 50 {
                agoraPrint("need seek, time:\(threshold)")
                 mediaPlayer?.seek(toPosition: expectPosition)
            }
        }
        
    }

    private func handleAudienceRole(dict: [String: Any]) {
        // do something for audience role
        guard let position = dict["time"] as? Int64,
                let duration = dict["duration"] as? Int64,
                let realPosition = dict["realTime"] as? Int64,
                let songCode = dict["songCode"] as? Int64,
                let mainSingerState = dict["playerState"] as? Int
        else { return }
    }
    
    @objc public func didKTVAPIReceiveAudioVolumeIndication(with speakers: [AgoraRtcAudioVolumeInfo], totalVolume: NSInteger) {
        if playerState != .playing {return}
        if singerRole == .audience {return}

        guard var pitch: Double = speakers.first?.voicePitch else {return}
        pitch = isNowMicMuted ? 0 : pitch
        //如果mpk不是playing状态 pitch = 0
        if mediaPlayer?.getPlayerState() != .playing {pitch = 0}
        self.pitch = pitch
        //将主唱的pitch同步到观众
//        if isMainSinger() {
//            let dict: [String: Any] = [ "cmd": "setVoicePitch",
//                                        "pitch": pitch,
//            ]
//            sendStreamMessageWithDict(dict, success: nil)
//        }
    }

    @objc public func didKTVAPILocalAudioStats(stats: AgoraRtcLocalAudioStats) {
        if useCustomAudioSource == true {return}
        audioPlayoutDelay = Int(stats.audioPlayoutDelay)
    }
    
    @objc func didAudioMetadataReceived( uid: UInt, metadata: Data) {
        guard let time: LrcTime = try? LrcTime(serializedData: metadata) else {return}
        if time.type == .lrcTime && self.singerRole == .audience {
            self.setProgress(with: Int(time.ts))
       }
    }

}

//private method
extension KTVGiantChorusApiImpl {

    private func initTimer() {
        
        guard timer == nil else { return }

        timer = Timer.scheduledTimer(withTimeInterval: 0.05, repeats: true, block: {[weak self] timer in
            guard let self = self else {
                timer.invalidate()
                return
            }
            
            var current = self.getPlayerCurrentTime()
            if self.singerRole == .audience && (Date().milListamp - (self.lastMainSingerUpdateTime )) > 1000 {
                return
            }
            
            if self.singerRole != .audience && (Date().milListamp - (self.lastReceivedPosition )) > 1000 {
                return
            }

            if self.oldPitch == self.pitch && (self.oldPitch != 0 && self.pitch != 0) {
                self.pitch = -1
            }
            
            if self.singerRole != .audience {
                current = Date().milListamp - self.lastReceivedPosition + Double(self.localPosition)
            }
            if self.singerRole == .audience && !recvFromDataStream {
                
            } else {
                if self.singerRole != .audience {
                    current = Date().milListamp - self.lastReceivedPosition + Double(self.localPosition)
                    if self.singerRole == .leadSinger || self.singerRole == .soloSinger {
                        var time: LrcTime = LrcTime()
                        time.forward = true
                        time.ts = Int64(current) + Int64(self.startHighTime)
                        time.songID = songIdentifier
                        time.type = .lrcTime
                        //大合唱的uid是musicuid
                        time.uid = Int32(Int(apiConfig?.musicStreamUid ?? 0))
                        sendMetaMsg(with: time)
                    }
                }
                self.setProgress(with: Int(current) + Int(self.startHighTime))
            }
            self.oldPitch = self.pitch
       })
    }

    private func setPlayerState(with state: AgoraMediaPlayerState) {
        playerState = state
        updateRemotePlayBackVolumeIfNeed()
        updateTimer(with: state)
    }

    private func updateRemotePlayBackVolumeIfNeed() {
        let role = singerRole
        if role == .audience {
            apiConfig?.engine?.adjustPlaybackSignalVolume(100)
            return
        }

        let vol = self.playerState == .playing ? remoteVolume : 100
        apiConfig?.engine?.adjustPlaybackSignalVolume(Int(vol))
    }

    private func updateTimer(with state: AgoraMediaPlayerState) {
        DispatchQueue.main.async {
            if state == .paused || state == .stopped {
                self.pauseTimer()
            } else if state == .playing {
                self.startTimer()
            }
        }
    }

    //timer method
    private func startTimer() {
        guard let timer = self.timer else {return}
        if isPause == false {
            RunLoop.current.add(timer, forMode: .common)
            self.timer?.fire()
        } else {
            resumeTimer()
        }
    }

    private func resumeTimer() {
        if isPause == false {return}
        isPause = false
        timer?.fireDate = Date()
    }

    private func pauseTimer() {
        if isPause == true {return}
        isPause = true
        timer?.fireDate = Date.distantFuture
    }

    private func freeTimer() {
        guard let _ = self.timer else {return}
        self.timer?.invalidate()
        self.timer = nil
    }

    private func getPlayerCurrentTime() -> TimeInterval {
        let role = singerRole
        if role == .soloSinger || role == .leadSinger{
            let time = Date().milListamp - localPlayerPosition
            return time
        } else if role == .coSinger {
            if playerState == .playing || playerState == .paused {
                let time = Date().milListamp - localPlayerPosition
                return time
            }
        }
        
        var position = Date().milListamp - self.lastMainSingerUpdateTime + remotePlayerPosition
        if playerState != .playing {
            position = remotePlayerPosition
        }
        return position
    }

    private func syncPlayStateFromRemote(state: AgoraMediaPlayerState, needDisplay: Bool) {
        let role = singerRole
        if role == .coSinger {
            if state == .stopped {
               // stopSing()
            } else if state == .paused {
                pausePlay()
            } else if state == .playing {
                resumeSing()
            } else if (state == .playBackAllLoopsCompleted && needDisplay == true) {
                getEventHander { delegate in
                    delegate.onMusicPlayerStateChanged(state: state, reason: .none, isLocal: true)
                }
            }
        } else {
            self.playerState = state
            getEventHander { delegate in
                delegate.onMusicPlayerStateChanged(state: self.playerState, reason: .none, isLocal: false)
            }
        }
    }

    private func pausePlay() {
        mediaPlayer?.pause()
    }
    
    private func dataToDictionary(data: Data) -> [String: Any]? {
        do {
            let json = try JSONSerialization.jsonObject(with: data, options: [])
            return json as? [String: Any]
        } catch {
            print("Error decoding data: (error.localizedDescription)")
            return nil
        }
    }

    private func compactDictionaryToData(_ dict: [String: Any]) -> Data? {
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: dict, options: [])
            return jsonData
        } catch {
            print("Error encoding data: (error.localizedDescription)")
            return nil
        }
    }

    private func getNtpTimeInMs() -> Int {
        var localNtpTime: Int = Int(apiConfig?.engine?.getNtpWallTimeInMs() ?? 0)

        if localNtpTime != 0 {
            localNtpTime = localNtpTime + 2208988800 * 1000
        }

        return localNtpTime
    }

    private func syncPlayState(state: AgoraMediaPlayerState, reason: AgoraMediaPlayerReason) {
        let dict: [String: Any] = ["cmd": "PlayerState", "userId": apiConfig?.localUid as Any, "state": state.rawValue, "reason": "\(reason.rawValue)"]
        sendStreamMessageWithDict(dict, success: nil)
    }
    
//    private func sendCustomMessage(with event: String, label: String) {
//        apiConfig?.engine?.sendCustomReportMessage(messageId, category: version, event: event, label: label, value: 0)
//        apiRepoter?.reportFuncEvent(name: event, value: <#T##[String : Any]#>, ext: <#T##[String : Any]#>)
//    }
    
    private func sendCustomMessage(with event: String, dict: [String: Any]) {
        apiRepoter?.reportFuncEvent(name: event, value: dict, ext: [:])
    }

    private func sendStreamMessageWithDict(_ dict: [String: Any], success: ((_ success: Bool) -> Void)?) {
        let messageData = compactDictionaryToData(dict as [String: Any])
        let code = apiConfig?.engine?.sendStreamMessageEx(dataStreamId, data: messageData ?? Data(), connection: singChannelConnection ?? AgoraRtcConnection())
        if code == 0 && success != nil { success!(true) }
        if code != 0 {
            print("sendStreamMessage fail: \(String(describing: code))")
        }
    }

    private func syncPlayState(_ state: AgoraMediaPlayerState) {
        let dict: [String: Any] = [ "cmd": "PlayerState", "userId": apiConfig?.localUid as Any, "state": "\(state.rawValue)" ]
        sendStreamMessageWithDict(dict, success: nil)
    }
    
    private func setProgress(with pos: Int) {
        lrcControl?.onUpdatePitch(pitch: Float(self.pitch))
        lrcControl?.onUpdateProgress(progress: pos > 200 ? pos - 200 : pos)
    }
    
    private func sendMetaMsg(with time: LrcTime) {
        let data: Data? = try? time.serializedData()
        let code = apiConfig?.engine?.sendAudioMetadataEx(mpkConnection ?? AgoraRtcConnection(), metadata: data ?? Data())
        if code != 0 {
          //  agoraPrint("sendStreamMessage fail: \(String(describing: code))")
        }
    }
}

//主要是MPK的回调
extension KTVGiantChorusApiImpl: AgoraRtcMediaPlayerDelegate {

    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo position_ms: Int, atTimestamp timestamp_ms: TimeInterval) {
       self.lastReceivedPosition = Date().milListamp
       self.localPosition = Int(position_ms)
       self.localPlayerSystemTime = timestamp_ms
       self.localPlayerPosition = Date().milListamp - Double(position_ms)
       if isMainSinger() && getPlayerCurrentTime() > TimeInterval(self.audioPlayoutDelay) {
           let dict: [String: Any] = [ "cmd": "setLrcTime",
                                       "duration": self.playerDuration,
                                       "time": position_ms - audioPlayoutDelay,
                                       "realTime":position_ms,
                                       "ntp": timestamp_ms,
                                       "playerState": self.playerState.rawValue,
                                       "songIdentifier": songIdentifier,
                                       "forward": true,
                                       "ver":2,
           ]
           #if DEBUG
            print("position_ms:\(position_ms), ntp:\(getNtpTimeInMs()), delta:\(self.getNtpTimeInMs() - position_ms), autoPlayoutDelay:\(self.audioPlayoutDelay), state:\(self.playerState.rawValue)")
           #endif
           sendStreamMessageWithDict(dict, success: nil)
       }
   }
    
    func AgoraRtcMediaPlayer(_ playerKit: any AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, reason: AgoraMediaPlayerReason) {
        agoraPrint("agoraRtcMediaPlayer didChangedToState: \(state.rawValue) \(self.songCode)")
        if isRelease {return}
        self.playerState = state
        if state == .openCompleted {
            self.localPlayerPosition = Date().milListamp
            self.playerDuration = TimeInterval(mediaPlayer?.getDuration() ?? 0)
            playerKit.selectMultiAudioTrack(1, publishTrackIndex: 1)
            if isMainSinger() { //主唱播放，通过同步消息“setLrcTime”通知伴唱play
                playerKit.play()
            }
            self.startProcessDelay()
        } else if state == .stopped {
            apiConfig?.engine?.adjustPlaybackSignalVolume(100)
            self.localPlayerPosition = Date().milListamp
            self.playerDuration = 0
        }
        else if state == .paused {
            apiConfig?.engine?.adjustPlaybackSignalVolume(100)
        } else if state == .playing {
            apiConfig?.engine?.adjustPlaybackSignalVolume(Int(remoteVolume))
            self.localPlayerPosition = Date().milListamp - Double(mediaPlayer?.getPosition() ?? 0)
        } else if state == .stopped {
            self.stopProcessDelay()
        }

        if isMainSinger() {
            syncPlayState(state: state, reason: reason)
        }
        agoraPrint("recv state with player callback : \(state.rawValue)")
        if state == .playBackAllLoopsCompleted && singerRole == .coSinger {//可能存在伴唱不返回allloopbackComplete状态 这个状态通过主唱的playerState来同步
            return
        }
        getEventHander { delegate in
            delegate.onMusicPlayerStateChanged(state: state, reason: .none, isLocal: true)
        }
    }

    private func isMainSinger() -> Bool {
        return singerRole == .soloSinger || singerRole == .leadSinger
    }
}

//主要是MCC的回调
extension KTVGiantChorusApiImpl: AgoraMusicContentCenterEventDelegate {
    
    func onSongSimpleInfoResult(_ requestId: String, songCode: Int, simpleInfo: String?, reason: AgoraMusicContentCenterStateReason) {
        if let jsonData = simpleInfo?.data(using: .utf8) {
            do {
                let jsonMsg = try JSONSerialization.jsonObject(with: jsonData, options: []) as! [String: Any]
                let format = jsonMsg["format"] as! [String: Any]
                let highPart = format["highPart"] as! [[String: Any]]
                let highStartTime = highPart[0]["highStartTime"] as! Int
                let highEndTime = highPart[0]["highEndTime"] as! Int
                let time = highStartTime
                startHighTime = time
                self.lrcControl?.onHighPartTime(highStartTime: highStartTime, highEndTime: highEndTime)
            } catch {
                agoraPrintError("Error while parsing JSON: \(error.localizedDescription)")
            }
        }
        if (reason == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
    }

    func onMusicChartsResult(_ requestId: String, result: [AgoraMusicChartInfo], reason: AgoraMusicContentCenterStateReason) {
        guard let callback = musicChartDict[requestId] else {return}
        callback(requestId, reason, result)
        musicChartDict.removeValue(forKey: requestId)
        if (reason == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
    }
    
    func onMusicCollectionResult(_ requestId: String, result: AgoraMusicCollection, reason: AgoraMusicContentCenterStateReason) {
        guard let callback = musicSearchDict[requestId] else {return}
        callback(requestId, reason, result)
        musicSearchDict.removeValue(forKey: requestId)
        if (reason == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
    }
    
    func onLyricResult(_ requestId: String, songCode: Int, lyricUrl: String?, reason: AgoraMusicContentCenterStateReason) {
        guard let lrcUrl = lyricUrl else {return}
        let callback = self.lyricCallbacks[requestId]
        guard let lyricCallback = callback else { return }
        self.lyricCallbacks.removeValue(forKey: requestId)
        if (reason == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
        if lrcUrl.isEmpty {
            lyricCallback(nil)
            return
        }
        lyricCallback(lrcUrl)
    }
    
    func onPreLoadEvent(_ requestId: String, songCode: Int, percent: Int, lyricUrl: String?, state: AgoraMusicContentCenterPreloadState, reason: AgoraMusicContentCenterStateReason) {
        if let listener = self.loadMusicListeners.object(forKey: "\(songCode)" as NSString) as? IMusicLoadStateListener {
            listener.onMusicLoadProgress(songCode: songCode, percent: percent, state: state, msg: String(reason.rawValue), lyricUrl: lyricUrl)
        }
        if (state == .preloading) { return }
        let SongCode = "\(songCode)"
        guard let block = self.musicCallbacks[SongCode] else { return }
        self.musicCallbacks.removeValue(forKey: SongCode)
        if (reason == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
        block(state, songCode)
    }

}

extension KTVGiantChorusApiImpl {
    
    private func sendSyncPitch(_ pitch: Double) {
        var msg: [String:Any] = [:]
        msg["cmd"] = "setVoicePitch"
        msg["pitch"] = pitch
        sendStreamMessageWithDict(msg) { _ in
            
        }
    }
    
    private func startSyncPitch() {
        print("startSyncPitch")
        mStopSyncPitch = false
        let queue = DispatchQueue(label: "com.example.syncpitch")
        mSyncPitchTimer = DispatchSource.makeTimerSource(queue: queue)
        mSyncPitchTimer?.schedule(deadline: .now(), repeating: .milliseconds(50))
        mSyncPitchTimer?.setEventHandler { [weak self] in
            guard let self = self else { return }
            if !self.mStopSyncPitch &&
                playerState == .playing &&
                (singerRole == .leadSinger || singerRole == .soloSinger) {
                self.sendSyncPitch(pitch)
            }
        }
        mSyncPitchTimer?.resume()
    }

    private func stopSyncPitch() {
        print("stopSyncPitch")
        mStopSyncPitch = true
        pitch = 0.0

        mSyncPitchTimer?.cancel()
        mSyncPitchTimer = nil
    }
    
    private func sendSyncScore() {
        print("sendSyncScore")
        var dictionary: [String: Any] = [:]
        dictionary["service"] = "audio_smart_mixer"
        dictionary["version"] = "V1"
        var payload: [String: Any] = [:]
        payload["cname"] = apiConfig?.chorusChannelName
        payload["uid"] = String(apiConfig?.localUid ?? 0)
        payload["uLv"] = -1
        payload["specialLabel"] = 0
        payload["audioRoute"] = audioRouting
        payload["vocalScore"] = singingScore
        dictionary["payload"] = payload
        sendStreamMessageWithDict(dictionary) { _ in
            
        }
    }
    
    private func startSyncScore() {
        print("startSyncScore")
        mStopSyncScore = false
        let queue = DispatchQueue(label: "com.example.syncscore")
        mSyncScoreTimer = DispatchSource.makeTimerSource(queue: queue)
        mSyncScoreTimer?.schedule(deadline: .now(), repeating: .milliseconds(3000))
        mSyncScoreTimer?.setEventHandler { [weak self] in
            guard let self = self else { return }
            if !self.mStopSyncScore &&
                playerState == .playing &&
                (singerRole == .leadSinger || singerRole == .coSinger) {
                self.sendSyncScore()
            }
        }
        mSyncScoreTimer?.resume()
    }

    private func stopSyncScore() {
        print("stopSyncScore")
        mStopSyncScore = true
        singingScore = 0

        mSyncScoreTimer?.cancel()
        mSyncScoreTimer = nil
    }
    
    // -1： unknown，0：非K歌状态，1：K歌播放状态，2：K歌暂停状态）
    private func getCloudConvergenceStatus() -> Int {
        var status = -1
        switch playerState {
        case .playing:
            status = 1
        case .paused:
            status = 2
        default:
            break
        }
        return status
    }
    
    private func sendSyncCloudConvergenceStatus() {
        print("sendSyncCloudConvergenceStatus")
        var dictionary: [String: Any] = [:]
        dictionary["service"] = "audio_smart_mixer_status"
        dictionary["version"] = "V1"
        var payload: [String: Any] = [:]
        payload["Ts"] = getNtpTimeInMs()
        payload["cname"] = apiConfig?.chorusChannelName
        payload["status"] = getCloudConvergenceStatus()
        payload["bgmUID"] = mpkConnection?.localUid
        payload["leadsingerUID"] = String(songConfig?.mainSingerUid ?? 0)
        dictionary["payload"] = payload
        sendStreamMessageWithDict(dictionary) { _ in
            
        }
    }
    
    private func startSyncCloudConvergenceStatus() {
        print("startSyncCloudConvergenceStatus")
        mStopSyncCloudConvergenceStatus = false
        let queue = DispatchQueue(label: "com.example.synccloudconvergencestatus")
        mSyncCloudConvergenceStatusTimer = DispatchSource.makeTimerSource(queue: queue)
        mSyncCloudConvergenceStatusTimer?.schedule(deadline: .now(), repeating: .milliseconds(200))
        mSyncCloudConvergenceStatusTimer?.setEventHandler { [weak self] in
            guard let self = self else { return }
            if !self.mStopSyncCloudConvergenceStatus &&
                singerRole == .leadSinger {
                self.sendSyncCloudConvergenceStatus()
            }
        }
        mSyncCloudConvergenceStatusTimer?.resume()
    }

    private func stopSyncCloudConvergenceStatus() {
        print("stopSyncCloudConvergenceStatus")
        mStopSyncCloudConvergenceStatus = true

        mSyncCloudConvergenceStatusTimer?.cancel()
        mSyncCloudConvergenceStatusTimer = nil
    }
    
}

extension KTVGiantChorusApiImpl {
    
    private func processDelayTask() {
            if !mStopProcessDelay && singerRole != .audience {
                let n = singerRole == .leadSinger ? apiConfig?.routeSelectionConfig.streamNum : (apiConfig?.routeSelectionConfig.streamNum ?? 1) - 1
                let sortedEntries = subScribeSingerMap.sorted(by: { $0.value < $1.value })
                let other = Array(sortedEntries.dropFirst(3))
                var drop = [Int]()
                
                if n ?? 3 > 3 {
                    for (uid, _) in other.dropLast(n! - 3) {
                        drop.append(uid)
                        apiConfig?.engine?.muteRemoteAudioStreamEx(UInt(uid), mute: true, connection: singChannelConnection ?? AgoraRtcConnection())
                        subScribeSingerMap.removeValue(forKey: uid)
                    }
                }
                
                agoraPrint("选路重新订阅, drop:\(drop)")
                
                let filteredList = singerList.filter { !subScribeSingerMap.keys.contains($0) }
                let filteredList2 = filteredList.filter { !drop.contains($0) }
                let shuffledList = filteredList2.shuffled()
                
                if subScribeSingerMap.count < 8 {
                    let randomSingers = Array(shuffledList.prefix(8 - subScribeSingerMap.count))
                    agoraPrintError("选路重新订阅, newSingers:\(randomSingers)")
                    
                    for singer in randomSingers {
                        subScribeSingerMap[singer] = 0
                        apiConfig?.engine?.muteRemoteAudioStreamEx(UInt(singer), mute: false, connection: singChannelConnection ?? AgoraRtcConnection())
                    }
                }
                
                agoraPrint("选路重新订阅, newSubScribeSingerMap:\(subScribeSingerMap)")
            }
        }

        private func processSubscribeTask() {
            if !mStopProcessDelay && singerRole != .audience {
                let n = singerRole == .leadSinger ? apiConfig?.routeSelectionConfig.streamNum : (apiConfig?.routeSelectionConfig.streamNum ?? 0) - 1
                let sortedEntries = subScribeSingerMap.sorted(by: { $0.value < $1.value })
                let mustToHave = Array(sortedEntries.prefix(3))
                
                for (uid, _) in mustToHave {
                    apiConfig?.engine?.adjustUserPlaybackSignalVolumeEx(UInt(uid), volume: 100, connection: singChannelConnection ?? AgoraRtcConnection())
                }
                
                let other = Array(sortedEntries.dropFirst(3))
                
                if n ?? 3 > 3 {
                    for (uid, delay) in Array(other.prefix(n! - 3)) {
                        if delay > 300 {
                            apiConfig?.engine?.adjustUserPlaybackSignalVolumeEx(UInt(uid), volume: 0, connection: singChannelConnection ?? AgoraRtcConnection())
                        } else {
                            apiConfig?.engine?.adjustUserPlaybackSignalVolumeEx(UInt(uid), volume: 100, connection: singChannelConnection ?? AgoraRtcConnection())
                        }
                    }
                    
                    for (uid, _) in Array(other.dropFirst(n! - 3)) {
                        apiConfig?.engine?.adjustUserPlaybackSignalVolumeEx(UInt(uid), volume: 0, connection: singChannelConnection ?? AgoraRtcConnection())
                    }
                }
                
                agoraPrint("选路排序+调整播放音量, mustToHave:\(mustToHave), other:\(other)")
            }
        }
    
    private func startProcessDelay() {
        guard apiConfig?.routeSelectionConfig.type != .topN && apiConfig?.routeSelectionConfig.type != .random else { return }
        
        mStopProcessDelay = false
        
        // 创建并配置 processDelayTimer
        processDelayFuture = DispatchSource.makeTimerSource()
        processDelayFuture?.schedule(deadline: .now() + .seconds(10), repeating: .seconds(20))
        processDelayFuture?.setEventHandler { [weak self] in
            // 执行 mProcessDelayTask
            self?.processDelayTask()
        }
        processDelayFuture?.resume()

        // 创建并配置 processSubscribeTimer
        processSubscribeFuture = DispatchSource.makeTimerSource()
        processSubscribeFuture?.schedule(deadline: .now() + .seconds(15), repeating: .seconds(20))
        processSubscribeFuture?.setEventHandler { [weak self] in
            // 执行 mProcessSubscribeTask
            self?.processSubscribeTask()
        }
        processSubscribeFuture?.resume()
    }

    private func stopProcessDelay() {
        mStopProcessDelay = true

        processDelayFuture?.cancel()
        processDelayFuture = nil
        processSubscribeFuture?.cancel()
        processSubscribeFuture = nil
    }
}


extension Date {
    /// 获取当前 秒级 时间戳 - 10位
    ///
    var timeStamp : TimeInterval {
        let timeInterval: TimeInterval = self.timeIntervalSince1970
        return timeInterval
    }
    /// 获取当前 毫秒级 时间戳 - 13位
    var milListamp : TimeInterval {
        let timeInterval: TimeInterval = self.timeIntervalSince1970
        let millisecond = CLongLong(round(timeInterval*1000))
        return TimeInterval(millisecond)
    }
}

