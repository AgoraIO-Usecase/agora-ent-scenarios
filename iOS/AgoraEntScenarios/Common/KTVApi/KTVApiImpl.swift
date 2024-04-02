//
//  KTVApiImpl.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraRtcKit
import SwiftProtobuf
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

@objc class KTVApiImpl: NSObject, KTVApiDelegate{
    
    private var apiConfig: KTVApiConfig?

    private var songConfig: KTVSongConfiguration?
    private var subChorusConnection: AgoraRtcConnection?

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
   
    private var audioPlayoutDelay: NSInteger = 0
    private var isNowMicMuted: Bool = false
    private var loadSongState: KTVLoadSongState = .idle
    private var lastNtpTime: Int = 0
    private var startHighTime: Int = 0
    private var isRelease: Bool = false
    private var songUrl2: String = ""
    private var enableMultipathing = true
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
    
    private let tag = "KTV_API_LOG"
    private let messageId = "agora:scenarioAPI"
    private let version = "1_iOS_4.3.0"
    private let lyricSyncVersion = 2

    private var singerRole: KTVSingRole = .audience {
        didSet {
            agoraPrint("singerRole changed: \(oldValue.rawValue)->\(singerRole.rawValue)")
        }
    }
    private var lrcControl: KTVLrcViewDelegate?
    
    private var timer: Timer?
    private var isPause: Bool = false
    private var recvFromDataStream = false
    public var remoteVolume: Int = 30
    private var joinChorusNewRole: KTVSingRole = .audience
    private var oldPitch: Double = 0
    private var isWearingHeadPhones: Bool = false
    private var enableProfessional: Bool = false
    private var isPublishAudio: Bool = false
    private var preludeDuration: Int64 = 0
    private lazy var apiDelegateHandler = KTVApiRTCDelegateHandler(with: self)
    
    private var totalSize: Int = 0
    
    deinit {
        mcc?.register(nil)
        agoraPrint("deinit KTVApiImpl")
    }
    
    @objc func createKtvApi(config: KTVApiConfig) {
        agoraPrint("init KTVApiImpl")
        self.apiConfig = config
        
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
        apiConfig?.engine?.addDelegate(apiDelegateHandler)
        mediaPlayer?.setPlayerOption("play_pos_change_callback", value: 100)
        initTimer()
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
        engine.setParameters("{\"che.audio.ans.noise_gate\": 20}")
        engine.setParameters("{\"rtc.use_audio4\": true}")
        if apiConfig?.type == .singRelay {
            engine.setParameters("{\"che.audio.aiaec.working_mode\": 1}")
        }
        
        //4.3.0 add
        enableMultipathing = true
//        engine.setParameters("{\"rtc.enable_tds_request_on_join\": true}")
//        engine.setParameters("{\"rtc.remote_path_scheduling_strategy\": 0}")
        engine.setParameters("{\"rtc.path_scheduling_strategy\": 0}")
       // engine.setParameters("{\"rtc.enableMultipath\": true}")
        engine.setParameters("{\"rtc.log_external_input\":true}")
        // 数据上报
        engine.setParameters("{\"rtc.direct_send_custom_event\": true}")
       // engine.setParameters("{\"rtc.qos_for_test_purpose\": true}")
    }
    
    func renewInnerDataStreamId() {
        let dataStreamConfig = AgoraDataStreamConfig()
        dataStreamConfig.ordered = false
        dataStreamConfig.syncWithAudio = true
        self.apiConfig?.engine?.createDataStream(&dataStreamId, config: dataStreamConfig)
        sendCustomMessage(with: "renewInnerDataStreamId", label: "")
        agoraPrint("renewInnerDataStreamId")
    }
}

//MARK: KTVApiDelegate
extension KTVApiImpl {
    
    func getMusicContentCenter() -> AgoraMusicContentCenter? {
        return mcc
    }
    
    func setLrcView(view: KTVLrcViewDelegate) {
        sendCustomMessage(with: "renewInnerDataStreamId", label: "view:\(view.description)")
        lrcControl = view
    }
    
    //主要针对本地歌曲播放的主唱伴奏切换的 loadmusic MCC直接忽视这个方法
    func load2Music(url1: String, url2: String, config: KTVSongConfiguration) {
        print("load2Music called: songUrl url1:(url1),url2:(url2)")
        self.songMode = .songUrl
        self.songConfig = config
        self.songIdentifier = config.songIdentifier
        self.songUrl = url1
        self.songUrl2 = url2
        
//        if config.autoPlay {
//            // 主唱自动播放歌曲
//            if self.singerRole != .leadSinger {
//                switchSingerRole(newRole: .soloSinger) { state, failRes in
//                }
//            }
//            startSing(url: url1, startPos: 0)
       // }
    }
    
    //主要针对本地歌曲播放的主唱伴奏切换的 MCC直接忽视这个方法
    func switchPlaySrc(url: String, syncPts: Bool) {
        print("switchPlaySrc called: (url)")
        
        if self.songUrl != url && self.songUrl2 != url {
            print("switchPlaySrc failed: canceled")
            return
        }
        
        let curPlayPosition: Int = syncPts ? mediaPlayer?.getPosition() ?? 0 : 0
        mediaPlayer?.stop()
        startSing(url: url, startPos: curPlayPosition)
    }
    
    func loadMusic(songCode: Int, config: KTVSongConfiguration, onMusicLoadStateListener: IMusicLoadStateListener) {
        sendCustomMessage(with: "loadMusic", label: "config:\(config.printObjectContent())")
        agoraPrint("loadMusic songCode:\(songCode) ")
        self.songMode = .songCode
        self.songCode = songCode
        self.songIdentifier = config.songIdentifier
        _loadMusic(config: config, mode: config.mode, onMusicLoadStateListener: onMusicLoadStateListener)
    }
    
    func loadMusic(config: KTVSongConfiguration, url: String) {
        sendCustomMessage(with: "loadMusic", label: "config:\(config.printObjectContent()), url:\(url)")
        self.songMode = .songUrl
        self.songUrl = url
        self.songIdentifier = config.songIdentifier
//        if config.autoPlay {
//            // 主唱自动播放歌曲
//            if singerRole != .leadSinger {
//                switchSingerRole(newRole: .soloSinger) { _, _ in
//                }
//            }
//            startSing(url: url, startPos: 0)
//        }
    }
    
    func getMusicPlayer() -> AgoraRtcMediaPlayerProtocol? {
        sendCustomMessage(with: "getMusicPlayer", label: "")
        return mediaPlayer
    }
    
    func addEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        sendCustomMessage(with: "addEventHandler", label: "")
        if eventHandlers.contains(ktvApiEventHandler) {
            return
        }
        eventHandlers.add(ktvApiEventHandler)
    }
    
    func removeEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        sendCustomMessage(with: "removeEventHandler", label: "")
        eventHandlers.remove(ktvApiEventHandler)
    }
    
    func cleanCache() {
        sendCustomMessage(with: "cleanCache", label: "")
        isRelease = true
        freeTimer()
        agoraPrint("cleanCache")
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
    
    func renewToken(rtmToken: String, chorusChannelRtcToken: String) {
        sendCustomMessage(with: "renewToken", label: "rtmToken:\(rtmToken), chorusChannelRtcToken:\(chorusChannelRtcToken)")
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
        sendCustomMessage(with: "fetchMusicCharts", label: "")
        agoraPrint("fetchMusicCharts")
        let requestId = mcc!.getMusicCharts()
        musicChartDict[requestId] = completion
    }
    
    func searchMusic(musicChartId: Int,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion:@escaping (String, AgoraMusicContentCenterStatusCode, AgoraMusicCollection) -> Void) {
        agoraPrint("searchMusic with musicChartId: \(musicChartId)")
        sendCustomMessage(with: "searchMusic", label: "musicChartId:\(musicChartId), page:\(page), pageSize:\(pageSize), jsonOption:\(jsonOption)")
        let requestId = mcc!.getMusicCollection(musicChartId: musicChartId, page: page, pageSize: pageSize, jsonOption: jsonOption)
        musicSearchDict[requestId] = completion
    }
    
    func searchMusic(keyword: String,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion: @escaping (String, AgoraMusicContentCenterStatusCode, AgoraMusicCollection) -> Void) {
        agoraPrint("searchMusic with keyword: \(keyword)")
        sendCustomMessage(with: "searchMusic", label: "keyword:\(keyword), page:\(page), pageSize:\(pageSize), jsonOption:\(jsonOption)")
        let requestId = mcc!.searchMusic(keyWord: keyword, page: page, pageSize: pageSize, jsonOption: jsonOption)
        musicSearchDict[requestId] = completion
    }
    
    func switchSingerRole(newRole: KTVSingRole, onSwitchRoleState: @escaping (KTVSwitchRoleState, KTVSwitchRoleFailReason) -> Void) {
        let oldRole = singerRole
        sendCustomMessage(with: "switchSingerRole", label: "oldRole:\(oldRole.rawValue), newRole: \(newRole.rawValue)")
        agoraPrint("switchSingerRole oldRole:\(oldRole.rawValue), newRole: \(newRole.rawValue)")
        
        if (apiConfig?.type != .singRelay) {
            if ((oldRole == .leadSinger || oldRole == .soloSinger) && (newRole == .coSinger || newRole == .audience) && isNowMicMuted) {
                    apiConfig?.engine?.muteLocalAudioStream(true)
                    apiConfig?.engine?.adjustRecordingSignalVolume(100)
            } else if ((oldRole == .audience || oldRole == .coSinger) && (newRole == .leadSinger || newRole == .soloSinger) && isNowMicMuted) {
                        apiConfig?.engine?.adjustRecordingSignalVolume(0)
                        apiConfig?.engine?.muteLocalAudioStream(false)
            }
        }
        
        self.switchSingerRole(oldRole: oldRole, newRole: newRole, token: apiConfig?.chorusChannelToken ?? "", stateCallBack: onSwitchRoleState)
    }

    /**
     * 恢复播放
     */
    @objc public func resumeSing() {
        sendCustomMessage(with: "resumeSing", label: "")
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
        sendCustomMessage(with: "pauseSing", label: "")
        agoraPrint("pauseSing")
        mediaPlayer?.pause()
    }

    /**
     * 调整进度
     */
    @objc public func seekSing(time: NSInteger) {
        sendCustomMessage(with: "seekSing", label: "")
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
        sendCustomMessage(with: "setMicStatus", label: "\(muteStatus)")
        self.isNowMicMuted = muteStatus
        if (apiConfig?.type != .singRelay) {
            if self.singerRole == .leadSinger || self.singerRole == .soloSinger {
                apiConfig?.engine?.adjustRecordingSignalVolume(muteStatus ? 0 : 100)
            } else {
                let channelMediaOptions = AgoraRtcChannelMediaOptions()
                channelMediaOptions.publishMicrophoneTrack = !muteStatus
                channelMediaOptions.clientRoleType = .broadcaster
                apiConfig?.engine?.updateChannel(with: channelMediaOptions)
                apiConfig?.engine?.muteLocalAudioStream(muteStatus)
            }
        } else {
            apiConfig?.engine?.adjustRecordingSignalVolume(muteStatus ? 0 : 100)
        }
    }
    
    @objc public func removeMusic(songCode: Int) {
        sendCustomMessage(with: "removeMusic", label: "songCode:\(songCode)")
        let ret: Int = mcc?.removeCache(songCode: songCode) ?? 0
        if ret < 0 {
            agoraPrint("removeMusic failed: ret:\(ret)")
        }
    }
    
    @objc public func enableMutipath(enable: Bool) {
        sendCustomMessage(with: "enableMutipath", label: "enable:\(enable)")
        agoraPrint("enableMutipath:\(enable)")
        enableMultipathing = enable
        if singerRole == .coSinger || singerRole == .leadSinger {
            if let subChorusConnection = subChorusConnection {
                let mediaOption = AgoraRtcChannelMediaOptions()
                mediaOption.parameters = "{\"rtc.enableMultipath\": \(enable), \"rtc.path_scheduling_strategy\": 0, \"rtc.remote_path_scheduling_strategy\": 0}"
                apiConfig?.engine?.updateChannelEx(with: mediaOption, connection: subChorusConnection)
            }
        }
    }

    private func agoraPrint(_ message: String) {
        #if DEBUG
            print(message)
        #else
            apiConfig?.engine?.writeLog(.info, content: "ktv_info:\(message)")
        #endif
    }
    
    private func agoraPrintError(_ message: String) {
        #if DEBUG
            print(message)
        #else
            apiConfig?.engine?.writeLog(.error, content: "ktv_err:\(message)")
        #endif
    }
}


// 主要是角色切换，加入合唱，加入多频道，退出合唱，退出多频道
extension KTVApiImpl {
    private func switchSingerRole(oldRole: KTVSingRole, newRole: KTVSingRole, token: String, stateCallBack:@escaping ISwitchRoleStateListener) {
    //    agoraPrint("switchSingerRole oldRole: \(oldRole.rawValue), newRole: \(newRole.rawValue)")
        if oldRole == .audience && newRole == .soloSinger {
            // 1、KTVSingRoleAudience -》KTVSingRoleMainSinger
            singerRole = newRole
            becomeSoloSinger()
            getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: .audience, newRole: .soloSinger)
            }
            
            stateCallBack(.success, .none)
        } else if oldRole == .audience && newRole == .leadSinger {
            becomeSoloSinger()
            joinChorus(role: newRole, token: token, joinExChannelCallBack: {[weak self] flag, status in
                guard let self = self else {return}
                //还原临时变量为观众
                self.joinChorusNewRole = .audience

                if flag == true {
                    self.singerRole = newRole
                    self.getEventHander { delegate in
                        delegate.onSingerRoleChanged(oldRole: .audience, newRole: .leadSinger)
                    }
                    stateCallBack(.success, .none)
                } else {
                    self.leaveChorus(role: .leadSinger)
                    stateCallBack(.fail, .joinChannelFail)
                }
            })

        } else if oldRole == .soloSinger && newRole == .audience {
            stopSing()
            singerRole = newRole
            getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: .soloSinger, newRole: .audience)
            }
            
            stateCallBack(.success, .none)
        } else if oldRole == .audience && newRole == .coSinger {
            joinChorus(role: newRole, token: token, joinExChannelCallBack: {[weak self] flag, status in
                guard let self = self else {return}
                //还原临时变量为观众
                self.joinChorusNewRole = .audience
                if flag == true {
                    self.singerRole = newRole
                    //TODO(chenpan):如果观众变成伴唱，需要重置state，防止同步主唱state因为都是playing不会修改
                    //后面建议改成remote state(通过data stream获取)和local state(通过player didChangedToState获取)
                    self.playerState = self.mediaPlayer?.getPlayerState() ?? .idle
                    self.getEventHander { delegate in
                        delegate.onSingerRoleChanged(oldRole: .audience, newRole: .coSinger)
                    }
                    stateCallBack(.success, .none)
                } else {
                    self.leaveChorus(role: .coSinger)
                    stateCallBack(.fail, .joinChannelFail)
                }
            })
        } else if oldRole == .coSinger && newRole == .audience {
            leaveChorus(role: .coSinger)
            singerRole = newRole
            getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: .coSinger, newRole: .audience)
            }
            
            stateCallBack(.success, .none)
        } else if oldRole == .soloSinger && newRole == .leadSinger {
            joinChorus(role: newRole, token: token, joinExChannelCallBack: {[weak self] flag, status in
                guard let self = self else {return}
                //还原临时变量为观众
                self.joinChorusNewRole = .audience
                if flag == true {
                    self.singerRole = newRole
                    self.getEventHander { delegate in
                        delegate.onSingerRoleChanged(oldRole: .soloSinger, newRole: .leadSinger)
                    }
                    stateCallBack(.success, .none)
                } else {
                    self.leaveChorus(role: .leadSinger)
                    stateCallBack(.fail, .joinChannelFail)
                }
            })
        } else if oldRole == .leadSinger && newRole == .soloSinger {
            leaveChorus(role: .leadSinger)
            singerRole = newRole
            getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: .leadSinger, newRole: .soloSinger)
            }
            
            stateCallBack(.success, .none)
        } else if oldRole == .leadSinger && newRole == .audience {
            leaveChorus(role: .leadSinger)
            stopSing()
            singerRole = newRole
            getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: .leadSinger, newRole: .audience)
            }
            
            stateCallBack(.success, .none)
        } else if oldRole == .coSinger && newRole == .leadSinger {
            self.singerRole = .leadSinger
            self.syncNewLeadSinger(with: apiConfig?.localUid ?? 0)
            apiConfig?.engine?.muteRemoteAudioStream(UInt(songConfig?.mainSingerUid ?? 0), mute: false)
            songConfig?.mainSingerUid = apiConfig?.localUid ?? 0
            
            apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
            apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
            apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 80000}")
            
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.publishMediaPlayerId = Int(mediaPlayer?.getMediaPlayerId() ?? 0)
            mediaOption.publishMediaPlayerAudioTrack = true
            apiConfig?.engine?.updateChannel(with: mediaOption)
            
            let mediaOption2 = AgoraRtcChannelMediaOptions()
            mediaOption2.autoSubscribeAudio = false
            mediaOption2.autoSubscribeVideo = false
            mediaOption2.publishMicrophoneTrack = true
            mediaOption2.enableAudioRecordingOrPlayout = false
            mediaOption2.clientRoleType = .broadcaster
            apiConfig?.engine?.updateChannelEx(with: mediaOption2, connection: subChorusConnection ?? AgoraRtcConnection())
            getEventHander { delegate in
                delegate.onSingerRoleChanged(oldRole: .coSinger, newRole: .leadSinger)
            }
            
            stateCallBack(.success, .none)
        } else {
            stateCallBack(.fail, .noPermission)
            agoraPrint("Error！You can not switch role from \(oldRole.rawValue) to \(newRole.rawValue)!")
        }

    }

    private func becomeSoloSinger() {
        apiConfig?.engine?.setAudioScenario(.chorus)
        apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
        apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
        apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 80000}")
        agoraPrint("becomeSoloSinger")
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.autoSubscribeAudio = true
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
            mediaOption.publishMediaPlayerAudioTrack = false
            apiConfig?.engine?.updateChannel(with: mediaOption)

            mediaPlayer?.setPlayerOption("enable_multi_audio_track", value: 0)
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
            apiConfig?.engine?.setAudioScenario(.chorus)
            apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":false}")
            apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":false}")
            apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 48000}")
        }

        let mediaOption = AgoraRtcChannelMediaOptions()
        // main singer do not subscribe 2nd channel
        // co singer auto sub
        mediaOption.autoSubscribeAudio = role != .leadSinger
        mediaOption.publishMicrophoneTrack = newRole == .leadSinger
        mediaOption.enableAudioRecordingOrPlayout = role != .leadSinger
        mediaOption.clientRoleType = .broadcaster
        mediaOption.parameters = "{\"rtc.use_audio4\": true}"
        if enableMultipathing {
            mediaOption.parameters = "{\"rtc.enableMultipath\": true, \"rtc.path_scheduling_strategy\": 0, \"rtc.remote_path_scheduling_strategy\": 0}"
        }

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
            apiConfig?.engine?.muteRemoteAudioStream(uid, mute: true)
            agoraPrint("muteRemoteAudioStream: \(uid), ret: \(ret ?? -1)")
        }
        apiConfig?.engine?.setParameters("{\"rtc.use_audio4\": true}")
    }

    private func leaveChorus2ndChannel(_ role: KTVSingRole) {
        guard let config = songConfig else {return}
        guard let subConn = subChorusConnection else {return}
        if (role == .leadSinger) {
            apiConfig?.engine?.leaveChannelEx(subConn)
        } else if (role == .coSinger) {
            apiConfig?.engine?.leaveChannelEx(subConn)
            apiConfig?.engine?.muteRemoteAudioStream(UInt(config.mainSingerUid), mute: false)
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
            mediaOption.publishMediaPlayerAudioTrack = false
            apiConfig?.engine?.updateChannel(with: mediaOption)
            leaveChorus2ndChannel(role)
            apiConfig?.engine?.setAudioScenario(.gameStreaming)
            apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
            apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
            apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 48000}")
        } else if role == .audience {
            agoraPrint("joinChorus: KTVSingRoleAudience does not need to leaveChorus!")
        }
    }
}

extension KTVApiImpl {
    
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
        
        if apiConfig?.type == .singbattle {
           mcc?.getSongSimpleInfo(songCode: songCode)
        }
        
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
//                        }
//                    }
//                    self.startSing(songCode: self.songCode, startPos: 0)
//                }
            }
        } else {
            loadMusicListeners.setObject(onMusicLoadStateListener, forKey: "\(self.songCode)" as NSString)
         //   onMusicLoadStateListener.onMusicLoadProgress(songCode: self.songCode, percent: 0, status: .preloading, msg: "", lyricUrl: "")
            // TODO: 只有未缓存时才显示进度条
            if mcc?.isPreloaded(songCode: songCode) != 0 {
                onMusicLoadStateListener.onMusicLoadProgress(songCode: self.songCode, percent: 0, status: .preloading, msg: "", lyricUrl: "")
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
                            if self.songCode != songCode {
                                onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .cancled)
                                return
                            }
                            if let urlPath = url, !urlPath.isEmpty {
                                self.lyricUrlMap[String(songCode)] = urlPath
                                self.setLyric(with: urlPath) { lyricUrl in
                                    onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: urlPath)
                                    self.agoraPrint("loadMusicAndLrc: songCode:\(songCode) status:\(status.rawValue) ulr:\(String(describing: url))")
                                }
                            } else {
                                onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .noLyricUrl)
                                self.agoraPrint("loadMusicAndLrc: songCode:\(songCode) status:\(status.rawValue) ulr:\(String(describing: url))")
                            }
//                            if config.autoPlay {
//                                // 主唱自动播放歌曲
//                                if self.singerRole != .leadSinger {
//                                    self.switchSingerRole(newRole: .soloSinger) { _, _ in
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
//                                }
//                            }
//                            self.startSing(songCode: self.songCode, startPos: 0)
//                        }
                        onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: "")
                    }
                } else {
                    agoraPrintError("load music failed songCode:\(songCode)")
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
        let err = self.mcc?.preload(songCode: songCode, jsonOption: nil)
        if err != 0 {
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
        sendCustomMessage(with: "startSing", label: "songCode:\(songCode), startPos: \(startPos)")
        let role = singerRole
        agoraPrint("startSing role: \(role.rawValue)")
        if self.songCode != songCode {
            agoraPrintError("startSing failed: canceled")
            return
        }
        
        if self.singerRole == .leadSinger || self.singerRole == .soloSinger {
            mediaPlayer?.setPlayerOption("enable_multi_audio_track", value: 1)
        }
        apiConfig?.engine?.adjustPlaybackSignalVolume(Int(remoteVolume))
        let ret = (mediaPlayer as? AgoraMusicPlayerProtocol)?.openMedia(songCode: songCode, startPos: startPos)
        agoraPrintError("startSing->openMedia(\(songCode) fail: \(ret ?? -1)")
    }
    
    func startSing(url: String, startPos: Int) {
        sendCustomMessage(with: "startSing", label: "url:\(url), startPos: \(startPos)")
        let role = singerRole
        agoraPrint("startSing role: \(role.rawValue)")
        if self.songUrl != songUrl {
            agoraPrintError("startSing failed: canceled")
            return
        }
        apiConfig?.engine?.adjustPlaybackSignalVolume(Int(remoteVolume))
        let ret = mediaPlayer?.open(url, startPos: 0)
        agoraPrintError("startSing->openMedia(\(url) fail: \(ret ?? -1)")
    }

    /**
     * 停止播放歌曲
     */
    @objc public func stopSing() {
        agoraPrint("stopSing")
        sendCustomMessage(with: "stopSing", label: "")
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.publishMediaPlayerAudioTrack = false
        apiConfig?.engine?.updateChannel(with: mediaOption)

        if mediaPlayer?.getPlayerState() != .stopped {
            mediaPlayer?.stop()
        }
        apiConfig?.engine?.setAudioScenario(.gameStreaming)
        apiConfig?.engine?.setParameters("{\"rtc.video.enable_sync_render_ntp_broadcast\":true}")
        apiConfig?.engine?.setParameters("{\"che.audio.neteq.enable_stable_playout\":true}")
        apiConfig?.engine?.setParameters("{\"che.audio.custom_bitrate\": 48000}")
    }
    
    @objc public func setAudioPlayoutDelay(audioPlayoutDelay: Int) {
        self.audioPlayoutDelay = audioPlayoutDelay
    }
    
    @objc func enableProfessionalStreamerMode(_ enable: Bool)   {
        if self.isPublishAudio == false {return}
        agoraPrint("enableProfessionalStreamerMode enable:\(enable)")
        self.enableProfessional = enable
        //专业非专业还需要根据是否佩戴耳机来判断是否开启3A
        apiConfig?.engine?.setAudioProfile(enable ? .musicHighQualityStereo : .musicStandardStereo)
        apiConfig?.engine?.setParameters("{\"che.audio.aec.enable\":\((enable && isWearingHeadPhones) ? false : true)}")
        apiConfig?.engine?.setParameters("{\"che.audio.agc.enable\":\((enable && isWearingHeadPhones) ? false : true)}")
        apiConfig?.engine?.setParameters("{\"che.audio.ans.enable\":\((enable && isWearingHeadPhones) ? false : true)}")
        apiConfig?.engine?.setParameters("{\"che.audio.md.enable\": false}")
    }

    private func syncNewLeadSinger(with uid: Int) {
        let dict = [
            "cmd": "syncNewLeadSinger",
            "uid":uid
        ] as [String : Any]
        sendStreamMessageWithDict(dict) { _ in
            
        }
    }
    
}

// rtc的子频道代理回调
extension KTVApiImpl: AgoraRtcEngineDelegate {

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
        agoraPrintError("didOccurError: \(errorCode.rawValue)")
        if errorCode != .joinChannelRejected {return}
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
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        getEventHander { delegate in
            delegate.onTokenPrivilegeWillExpire()
        }
    }
}

//需要外部转发的方法 主要是dataStream相关的
extension KTVApiImpl {
    
    private func handleSetLrcTimeCommand(dict: [String: Any], role: KTVSingRole) {
        guard let position = dict["time"] as? Int64,
                let duration = dict["duration"] as? Int64,
                let realPosition = dict["realTime"] as? Int64,
               // let songCode = dict["songCode"] as? Int64,
                let mainSingerState = dict["playerState"] as? Int,
                let ntpTime = dict["ntp"] as? Int,
                let songId = dict["songIdentifier"] as? String
        else { return }

        self.lastNtpTime = ntpTime
        self.remotePlayerDuration = TimeInterval(duration)
        
        let state = AgoraMediaPlayerState(rawValue: mainSingerState) ?? .stopped
        if self.playerState != state {
            agoraPrint("[setLrcTime] recv state: \(self.playerState.rawValue)->\(state.rawValue) role: \(singerRole.rawValue) role: \(singerRole.rawValue)")
            
            if state == .playing, singerRole == .coSinger, playerState == .openCompleted {
                //如果是伴唱等待主唱开始播放，seek 到指定位置开始播放保证歌词显示位置准确
                self.localPlayerPosition = self.lastMainSingerUpdateTime - Double(position)
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

        agoraPrint("recv state with MainSinger: \(state.rawValue)")
        syncPlayStateFromRemote(state: state, needDisplay: true)
    }

    private func handleSetVoicePitchCommand(dict: [String: Any], role: KTVSingRole) {
        if apiConfig?.type == .singRelay {
            if isNowMicMuted || singerRole == .audience {
                if let voicePitch = dict["pitch"] as? Double {
                    self.pitch = voicePitch
                }
            }
        } else {
            if role == .audience, let voicePitch = dict["pitch"] as? Double {
                self.pitch = voicePitch
            }
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
           // agoraPrint("checkNtp, diff:\(threshold), localNtp:\(getNtpTimeInMs()), localPosition:\(localPosition), audioPlayoutDelay:\(audioPlayoutDelay), remoteDiff:\(String(describing: ntpTime - Int(time)))")
            if abs(threshold) > 50 {
                 agoraPrint("expectPosition:\(expectPosition)")
                 mediaPlayer?.seek(toPosition: expectPosition)
            }
        }
        
    }
    
    private func handleCosingerToLeadSinger(with dict: [String: Any]) {
        if dict["cmd"] as! String == "syncNewLeadSinger" {
            if self.singerRole == .coSinger {
                apiConfig?.engine?.muteRemoteAudioStream(UInt(songConfig?.mainSingerUid ?? 0), mute: false)
                    let mainSingerUid = dict["uid"] as? Int ?? 0
                    songConfig?.mainSingerUid = mainSingerUid
                    let ret = apiConfig?.engine?.muteRemoteAudioStream(UInt(mainSingerUid), mute: true)
                agoraPrint("handleCosingerToLeadSinger:ret:\(String(describing: ret))")
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
        agoraPrint("audience: position: \(position) realPosition:\(realPosition)")
    }

}

//private method
extension KTVApiImpl {

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
                var curTime:Int64 = Int64(current) + Int64(self.startHighTime)
                if songConfig?.songCutter == true {
                    curTime = curTime - preludeDuration > 0 ? curTime - preludeDuration : curTime
                }
                if self.singerRole != .audience {
                    current = Date().milListamp - self.lastReceivedPosition + Double(self.localPosition)
                    
                    if self.singerRole == .leadSinger || self.singerRole == .soloSinger {
                        var time: LrcTime = LrcTime()
                        time.forward = true
                        time.ts = curTime
                        time.songID = songIdentifier
                        time.type = .lrcTime
                        //大合唱的uid是musicuid
                        time.uid = Int32(apiConfig?.localUid ?? 0)
                        sendMetaMsg(with: time)
                    }
                }
                self.setProgress(with: Int(curTime))
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
            } else if state == .paused {
                pausePlay()
            } else if state == .playing {
                resumeSing()
            } else if (state == .playBackAllLoopsCompleted && needDisplay == true) {
                getEventHander { delegate in
                    delegate.onMusicPlayerStateChanged(state: state, error: .none, isLocal: true)
                }
            }
        } else {
            self.playerState = state
            getEventHander { delegate in
                delegate.onMusicPlayerStateChanged(state: self.playerState, error: .none, isLocal: false)
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

    private func syncPlayState(state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        let dict: [String: Any] = ["cmd": "PlayerState", "userId": apiConfig?.localUid as Any, "state": state.rawValue, "error": "\(error.rawValue)"]
        sendStreamMessageWithDict(dict, success: nil)
    }
    
    private func sendCustomMessage(with event: String, label: String) {
        apiConfig?.engine?.sendCustomReportMessage(messageId, category: version, event: event, label: label, value: 0)
    }

    private func sendStreamMessageWithDict(_ dict: [String: Any], success: ((_ success: Bool) -> Void)?) {
        let messageData = compactDictionaryToData(dict as [String: Any])
        let sizeInBits = (messageData ?? Data()).count * 8
        totalSize += sizeInBits
        let code = apiConfig?.engine?.sendStreamMessage(dataStreamId, data: messageData ?? Data())
        if code == 0 && success != nil { success!(true) }
        if code != 0 {
            agoraPrint("sendStreamMessage fail: \(String(describing: code))")
        }
        print("totalSize:\(totalSize)")
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
        let code = apiConfig?.engine?.sendAudioMetadata(data ?? Data())
        if code != 0 {
            agoraPrintError("sendStreamMessage fail: \(String(describing: code))")
        }
    }
}

//主要是MPK的回调
extension KTVApiImpl: AgoraRtcMediaPlayerDelegate {

    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo position_ms: Int, atTimestamp timestamp_ms: TimeInterval) {
       self.lastReceivedPosition = Date().milListamp
       self.localPosition = Int(position_ms)
       self.localPlayerSystemTime = timestamp_ms
       self.localPlayerPosition = Date().milListamp - Double(position_ms)
       if isMainSinger() && getPlayerCurrentTime() > TimeInterval(self.audioPlayoutDelay) {
           let dict: [String: Any] = [ "cmd": "setLrcTime",
                                       "duration": self.playerDuration,
                                       "time": position_ms - audioPlayoutDelay,
                                       //不同机型delay不同，需要发送同步的时候减去发送机型的delay，在接收同步加上接收机型的delay
                                       "realTime":position_ms,
                                       "ntp": timestamp_ms,
                                       "playerState": self.playerState.rawValue,
                                       "songIdentifier": songIdentifier,
                                       "ver":2,
           ]
       //    agoraPrint("position_ms:\(position_ms), ntp:\(getNtpTimeInMs()), delta:\(self.getNtpTimeInMs() - position_ms), autoPlayoutDelay:\(self.audioPlayoutDelay)")
           
           sendStreamMessageWithDict(dict) { _ in
               
           }
       }
        
        if apiConfig?.type == .singRelay {
            getEventHander { delegate in
                 delegate.onMusicPlayerProgressChanged(with: position_ms)
            }
        }
   }

   func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo position: Int) {
       
   }
    
    func AgoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        agoraPrint("agoraRtcMediaPlayer didChangedToState: \(state.rawValue) \(self.songCode)")
        if isRelease {return}
        if state == .openCompleted {
            self.localPlayerPosition = Date().milListamp
            print("localPlayerPosition:playerKit:openCompleted \(localPlayerPosition)")
            self.playerDuration = TimeInterval(mediaPlayer?.getDuration() ?? 0)
            if isMainSinger() { //主唱播放，通过同步消息“setLrcTime”通知伴唱play
                playerKit.play()
                playerKit.selectMultiAudioTrack(1, publishTrackIndex: 1)
            } else {
                playerKit.selectAudioTrack(1)
            }
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
            print("localPlayerPosition:playerKit:playing \(localPlayerPosition)")
        }

        if isMainSinger() {
            syncPlayState(state: state, error: error)
        }
        self.playerState = state
        agoraPrint("recv state with player callback : \(state.rawValue)")
        if state == .playBackAllLoopsCompleted && singerRole == .coSinger {//可能存在伴唱不返回allloopbackComplete状态 这个状态通过主唱的playerState来同步
            return
        }
        getEventHander { delegate in
            delegate.onMusicPlayerStateChanged(state: state, error: .none, isLocal: true)
        }
    }

    private func isMainSinger() -> Bool {
        return singerRole == .soloSinger || singerRole == .leadSinger
    }
}

//主要是MCC的回调
extension KTVApiImpl: AgoraMusicContentCenterEventDelegate {
    
    func onSongSimpleInfoResult(_ requestId: String, songCode: Int, simpleInfo: String?, errorCode: AgoraMusicContentCenterStatusCode) {
        if let jsonData = simpleInfo?.data(using: .utf8) {
            do {
                let jsonMsg = try JSONSerialization.jsonObject(with: jsonData, options: []) as! [String: Any]
                let format = jsonMsg["format"] as! [String: Any]
                let highPart = format["highPart"] as! [[String: Any]]
                let highStartTime = highPart[0]["highStartTime"] as! Int
                let highEndTime = highPart[0]["highEndTime"] as! Int
                if highPart[0].keys.contains("preludeDuration") {
                    self.preludeDuration = highPart[0]["preludeDuration"] as! Int64
                }
                let time = highStartTime
                startHighTime = time
                self.lrcControl?.onHighPartTime(highStartTime: highStartTime, highEndTime: highEndTime)
            } catch {
                agoraPrintError("Error while parsing JSON: \(error.localizedDescription)")
            }
        }
        if (errorCode == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
    }

    func onMusicChartsResult(_ requestId: String, result: [AgoraMusicChartInfo], errorCode: AgoraMusicContentCenterStatusCode) {
        guard let callback = musicChartDict[requestId] else {return}
        callback(requestId, errorCode, result)
        musicChartDict.removeValue(forKey: requestId)
        if (errorCode == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
    }
    
    func onMusicCollectionResult(_ requestId: String, result: AgoraMusicCollection, errorCode: AgoraMusicContentCenterStatusCode) {
        guard let callback = musicSearchDict[requestId] else {return}
        callback(requestId, errorCode, result)
        musicSearchDict.removeValue(forKey: requestId)
        if (errorCode == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
    }
    
    func onLyricResult(_ requestId: String, songCode: Int, lyricUrl: String?, errorCode: AgoraMusicContentCenterStatusCode) {
        guard let lrcUrl = lyricUrl else {return}
        let callback = self.lyricCallbacks[requestId]
        guard let lyricCallback = callback else { return }
        self.lyricCallbacks.removeValue(forKey: requestId)
        if (errorCode == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
        if lrcUrl.isEmpty {
            lyricCallback(nil)
            agoraPrintError("onLyricResult: lrcUrl.isEmpty")
            return
        }
        lyricCallback(lrcUrl)
        agoraPrint("onLyricResult: lrcUrl is \(lrcUrl)")
    }
    
    func onPreLoadEvent(_ requestId: String, songCode: Int, percent: Int, lyricUrl: String?, status: AgoraMusicContentCenterPreloadStatus, errorCode: AgoraMusicContentCenterStatusCode) {
        if let listener = self.loadMusicListeners.object(forKey: "\(songCode)" as NSString) as? IMusicLoadStateListener {
            listener.onMusicLoadProgress(songCode: songCode, percent: percent, status: status, msg: String(errorCode.rawValue), lyricUrl: lyricUrl)
        }
        if (status == .preloading) { return }
        agoraPrint("songCode:\(songCode), status:\(status.rawValue), code:\(errorCode.rawValue)")
        let SongCode = "\(songCode)"
        guard let block = self.musicCallbacks[SongCode] else { return }
        self.musicCallbacks.removeValue(forKey: SongCode)
        if (errorCode == .errorGateway) {
            getEventHander { delegate in
                delegate.onTokenPrivilegeWillExpire()
            }
        }
        block(status, songCode)
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

extension KTVApiImpl: KTVApiRTCDelegate {
    func didJoinChannel(channel: String, withUid uid: UInt, elapsed: Int) {
        agoraPrint("ktvapi加入主频道成功")
    }
    
    func didJoinedOfUid(uid: UInt, elapsed: Int) {
        
    }
    
    func reportAudioVolumeIndicationOfSpeakers(speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        if playerState != .playing {return}
        if singerRole == .audience {return}

        guard var pitch: Double = speakers.first?.voicePitch else {return}
        pitch = isNowMicMuted ? 0 : pitch
        //如果mpk不是playing状态 pitch = 0
        if mediaPlayer?.getPlayerState() != .playing {pitch = 0}
        self.pitch = pitch
        //将主唱的pitch同步到观众
        if (apiConfig?.type == .singRelay ) {
            if ((singerRole == .coSinger || singerRole == .leadSinger || singerRole == .soloSinger) && !isNowMicMuted) {
                let dict: [String: Any] = [ "cmd": "setVoicePitch",
                                            "pitch": pitch,
                ]
                sendStreamMessageWithDict(dict, success: nil)
            }
        } else {
            let dict: [String: Any] = [ "cmd": "setVoicePitch",
                                        "pitch": pitch,
            ]
            sendStreamMessageWithDict(dict, success: nil)
        }
    }
    
    func didAudioPublishStateChange(channelId: String, oldState: AgoraStreamPublishState, newState: AgoraStreamPublishState, elapseSinceLastState: Int32) {
        self.isPublishAudio = newState == .published
        enableProfessionalStreamerMode(self.enableProfessional)
        agoraPrint("PublishStateChange:\(newState)")
    }
    
    func receiveStreamMessageFromUid(uid: UInt, streamId: Int, data: Data) {
        let role = singerRole
        if isRelease {return}
        guard let dict = dataToDictionary(data: data), let cmd = dict["cmd"] as? String else { return }
        
        switch cmd {
        case "setLrcTime":
            handleSetLrcTimeCommand(dict: dict, role: role)
        case "PlayerState":
            handlePlayerStateCommand(dict: dict, role: role)
        case "setVoicePitch":
            handleSetVoicePitchCommand(dict: dict, role: role)
        case "syncNewLeadSinger":
            handleCosingerToLeadSinger(with: dict)
        default:
            break
        }
    }
    
    func localAudioStats(stats: AgoraRtcLocalAudioStats) {
        if useCustomAudioSource == true {return}
        audioPlayoutDelay = Int(stats.audioPlayoutDelay)
    }
    
    func didRTCAudioRouteChanged(routing: AgoraAudioOutputRouting) {
        agoraPrint("Route changed:\(routing)")
        let headPhones: [AgoraAudioOutputRouting] = [.headset, .headsetBluetooth, .headsetNoMic]
        let wearHeadPhone: Bool = headPhones.contains(routing)
        if wearHeadPhone == self.isWearingHeadPhones {
            return
        }
        self.isWearingHeadPhones = wearHeadPhone
        enableProfessionalStreamerMode(self.enableProfessional)
    }
    
    func audioMetadataReceived(uid: UInt, metadata: Data) {
       guard let time: LrcTime = try? LrcTime(serializedData: metadata) else {return}
        if time.type == .lrcTime && self.singerRole == .audience {
            self.setProgress(with: Int(time.ts))
       }
    }

    @objc func didAudioMetadataReceived( uid: UInt, metadata: Data) {
        
    }
}

/*----这一块的代码主要是用来处理主频道的RTC代理事件，外部不再需要手动转代理，😁---*/
protocol KTVApiRTCDelegate: NSObjectProtocol  {
    func didJoinChannel(channel: String, withUid uid: UInt, elapsed: Int)
    func didJoinedOfUid(uid: UInt, elapsed: Int)
    func reportAudioVolumeIndicationOfSpeakers(speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int)
    func didRTCAudioRouteChanged(routing: AgoraAudioOutputRouting)
    func didAudioPublishStateChange(channelId: String, oldState: AgoraStreamPublishState, newState: AgoraStreamPublishState, elapseSinceLastState: Int32)
    func receiveStreamMessageFromUid(uid: UInt, streamId: Int, data: Data)
    func localAudioStats(stats: AgoraRtcLocalAudioStats)
    func audioMetadataReceived( uid: UInt, metadata: Data)
}

class KTVApiRTCDelegateHandler: NSObject, AgoraRtcEngineDelegate {
    
    var delegate: KTVApiRTCDelegate
    init(with delegate: KTVApiRTCDelegate) {
        self.delegate = delegate
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        delegate.didJoinChannel(channel: channel, withUid: uid, elapsed: elapsed)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        delegate.didJoinedOfUid(uid: uid, elapsed: elapsed)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        delegate.reportAudioVolumeIndicationOfSpeakers(speakers: speakers, totalVolume: totalVolume)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didAudioRouteChanged routing: AgoraAudioOutputRouting) {
        delegate.didRTCAudioRouteChanged(routing: routing)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didAudioPublishStateChange channelId: String, oldState: AgoraStreamPublishState, newState: AgoraStreamPublishState, elapseSinceLastState: Int32) {
        delegate.didAudioPublishStateChange(channelId: channelId, oldState: oldState, newState: newState, elapseSinceLastState: elapseSinceLastState)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        delegate.receiveStreamMessageFromUid(uid: uid, streamId: streamId, data: data)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        delegate.localAudioStats(stats: stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, audioMetadataReceived uid: UInt, metadata: Data) {
        delegate.audioMetadataReceived(uid: uid, metadata: metadata)
    }
}

