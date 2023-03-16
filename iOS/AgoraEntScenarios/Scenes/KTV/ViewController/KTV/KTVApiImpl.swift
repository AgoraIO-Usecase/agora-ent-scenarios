//
//  KTVApiImpl.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraRtcKit
import AgoraLyricsScore

/// 加载歌曲状态
@objc public enum KTVLoadSongState: Int {
    case idle = -1      //空闲
    case ok = 0         //成功
    case failed         //失败
    case inProgress    //加载中
}

private func agoraPrint(_ message: String) {
    KTVLog.info(text: message, tag: "KTVApi")
}

class KTVApiImpl: NSObject{
    
    private var apiConfig: KTVApiConfig?

    private var songConfig: KTVSongConfiguration?
    private var subChorusConnection: AgoraRtcConnection?
    private var downloadManager: AgoraDownLoadManager = AgoraDownLoadManager()

    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()

    private var musicPlayer: AgoraMusicPlayerProtocol!
    private var mcc: AgoraMusicContentCenter!

    private var loadSongMap = Dictionary<String, KTVLoadSongState>()
    private var lyricUrlMap = Dictionary<String, String>()
    private var loadDict = Dictionary<String, KTVLoadSongState>()
    private var lyricCallbacks = Dictionary<String, LyricCallback>()
    private var musicCallbacks = Dictionary<String, LoadMusicCallback>()
    
    private var hasSendPreludeEndPosition: Bool = false
    private var hasSendEndPosition: Bool = false
    private var totalLines: NSInteger = 0
    private var totalScore: NSInteger = 0
    private var audioPlayoutDelay: NSInteger = 0
    private var isNowMicMuted: Bool = false
    
    private var playerState: AgoraMediaPlayerState = .idle {
        didSet {
            updateRemotePlayBackVolumeIfNeed()
            updateTimer(with: playerState)
        }
    }
    private var pitch: Double = 0
    private var localPlayerPosition: TimeInterval = 0
    private var remotePlayerPosition: TimeInterval = 0
    private var remotePlayerDuration: TimeInterval = 0
    private var localPlayerSystemTime: TimeInterval = 0
    private var lastAudienceUpTime: TimeInterval = 0
    private var playerDuration: TimeInterval = 0

    private var musicChartDict: [String: MusicChartCallBacks] = [:]
    private var musicSearchDict: Dictionary<String, MusicResultCallBacks> = Dictionary<String, MusicResultCallBacks>()
    private var onJoinExChannelCallBack : JoinExChannelCallBack?
    private var mainSingerHasJoinChannelEx: Bool = false

    private var singerRole: KTVSingRole = .audience
    private var lrcControl: KTVLrcViewDelegate?
    
    private var timer: Timer?
    private var isPause: Bool = false
    
    public var remoteVolume: Int = 15
    private var joinChorusNewRole: KTVSingRole = .audience
    private var loadMusicState: KTVLoadSongFailReason = .none
    
    deinit {
        mcc.register(nil)
        agoraPrint("deinit KTVApiImpl")
    }

    @objc required init(config: KTVApiConfig) {
        super.init()
        agoraPrint("init KTVApiImpl")
        self.apiConfig = config

        // ------------------ 初始化内容中心 ------------------
        let contentCenterConfiguration = AgoraMusicContentCenterConfig()
        contentCenterConfiguration.appId = config.appId
        contentCenterConfiguration.mccUid = config.localUid
        contentCenterConfiguration.token = config.rtmToken
        contentCenterConfiguration.rtcEngine = config.engine

        mcc = AgoraMusicContentCenter.sharedContentCenter(config: contentCenterConfiguration)
        mcc.register(self)
        // ------------------ 初始化音乐播放器实例 ------------------
        musicPlayer = mcc.createMusicPlayer(delegate: self)

        // 音量最佳实践调整
        musicPlayer.adjustPlayoutVolume(50)
        musicPlayer.adjustPublishSignalVolume(50)

        downloadManager.delegate = self

        initTimer()
    }
}

//MARK: KTVApiDelegate
extension KTVApiImpl: KTVApiDelegate {
    func setLrcView(view: KTVLrcViewDelegate) {
        lrcControl = view
//        lrcControl?.skipCallBack =  {[weak self] time in
//            self?.musicPlayer.seek(toPosition: time)
//        }
    }
    

    func loadMusic(config: KTVSongConfiguration, mode: KTVLoadMusicMode, onMusicLoadStateListener: KTVMusicLoadStateListener) {
        agoraPrint("loadMusic songCode:\(config.songCode) ")
        _loadMusic(config: config, mode: mode, onMusicLoadStateListener: onMusicLoadStateListener)
    }

    func getMediaPlayer() -> AgoraMusicPlayerProtocol? {
        return musicPlayer
    }

    func addEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        if eventHandlers.contains(ktvApiEventHandler) {
            return
        }
        eventHandlers.add(ktvApiEventHandler)
    }

    func removeEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        eventHandlers.remove(ktvApiEventHandler)
    }

    func cleanCache() {
        freeTimer()
        agoraPrint("cleanCache")
        downloadManager.delegate = nil
        apiConfig?.engine.setAudioFrameDelegate(nil)
        lyricCallbacks.removeAll()
        musicCallbacks.removeAll()
        apiConfig?.engine .destroyMediaPlayer(musicPlayer)
        AgoraMusicContentCenter.destroy()
        self.eventHandlers.removeAllObjects()
    }

    func fetchMusicCharts(completion: @escaping MusicChartCallBacks) {
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
        let requestId = mcc!.getMusicCollection(musicChartId: musicChartId, page: page, pageSize: pageSize, jsonOption: jsonOption)
        musicSearchDict[requestId] = completion
    }

    func searchMusic(keyword: String,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion: @escaping (String, AgoraMusicContentCenterStatusCode, AgoraMusicCollection) -> Void) {
        agoraPrint("searchMusic with keyword: \(keyword)")
        let requestId = mcc!.searchMusic(keyWord: keyword, page: page, pageSize: pageSize, jsonOption: jsonOption)
        musicSearchDict[requestId] = completion
    }

    func switchSingerRole(newRole: KTVSingRole, token: String, onSwitchRoleState: @escaping (KTVSwitchRoleState, KTVSwitchRoleFailReason) -> Void) {
        agoraPrint("switchSingerRole oldRole: \(singerRole.rawValue), newRole: \(newRole.rawValue) token: \(token)")
        let oldRole = singerRole
        self.switchSingerRole(oldRole: oldRole, newRole: newRole, token: token, stateCallBack: onSwitchRoleState)
    }

    /**
     * 恢复播放
     */
    @objc public func resumeSing() {
        agoraPrint("resumeSing")
        if musicPlayer?.getPlayerState() == .paused {
            musicPlayer?.resume()
        } else {
            musicPlayer?.play()
        }
    }

    /**
     * 暂停播放
     */
    @objc public func pauseSing() {
        agoraPrint("pauseSing")
        musicPlayer?.pause()
    }

    /**
     * 调整进度
     */
    @objc public func seekSing(time: NSInteger) {
        agoraPrint("seekSing")
        musicPlayer?.seek(toPosition: time)
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
    @objc public func setMicStatus(isOnMicOpen: Bool) {
        self.isNowMicMuted = !isOnMicOpen
    }

    /**
     * 获取mpk实例
     */
    @objc public func getMusicPlayer() -> AgoraMusicPlayerProtocol? {
        return musicPlayer
    }
}

// 主要是角色切换，加入合唱，加入多频道，退出合唱，退出多频道
extension KTVApiImpl {
    private func switchSingerRole(oldRole: KTVSingRole, newRole: KTVSingRole, token: String, stateCallBack:@escaping SwitchRoleStateCallBack) {
        agoraPrint("switchSingerRole oldRole: \(singerRole.rawValue), newRole: \(newRole.rawValue)")
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
            joinChorus(role: newRole, token: token, joinExChannelCallBack: { flag, status in
                //还原临时变量为观众
                self.joinChorusNewRole = .audience

                if flag == true {
                    self.singerRole = newRole
                    stateCallBack(.success, .none)
                } else {
                    self.leaveChorus(role: .leadSinger)
                    if status == .musicPreloadFail {
                        stateCallBack(.fail, .musicPreloadFail)
                    } else if status == .joinChannelFail {
                        stateCallBack(.fail, .joinChannelFail)
                    } else if status == .musicPreloadFailAndJoinChannelFail {
                        stateCallBack(.fail, .musicPreloadFailAndJoinChannelFail)
                    }
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
            joinChorus(role: newRole, token: token, joinExChannelCallBack: { flag, status in
                //还原临时变量为观众
                self.joinChorusNewRole = .audience
                if flag == true {
                    self.singerRole = newRole
                    stateCallBack(.success, .none)
                } else {
                    self.leaveChorus(role: .coSinger)
                    if status == .musicPreloadFail {
                        stateCallBack(.fail, .musicPreloadFail)
                    } else if status == .joinChannelFail {
                        stateCallBack(.fail, .joinChannelFail)
                    } else if status == .musicPreloadFailAndJoinChannelFail {
                        stateCallBack(.fail, .musicPreloadFailAndJoinChannelFail)
                    }
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
            joinChorus(role: newRole, token: token, joinExChannelCallBack: { flag, status in
                //还原临时变量为观众
                self.joinChorusNewRole = .audience
                if flag == true {
                    self.singerRole = newRole
                    stateCallBack(.success, .none)
                } else {
                    self.leaveChorus(role: .leadSinger)
                    if status == .musicPreloadFail {
                        stateCallBack(.fail, .musicPreloadFail)
                    } else if status == .joinChannelFail {
                        stateCallBack(.fail, .joinChannelFail)
                    } else if status == .musicPreloadFailAndJoinChannelFail {
                        stateCallBack(.fail, .musicPreloadFailAndJoinChannelFail)
                    }
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
        } else {
            stateCallBack(.fail, .noPermission)
            agoraPrint("Error！You can not switch role from \(singerRole.rawValue) to \(newRole.rawValue)!")
        }

    }

    private func becomeSoloSinger() {
        apiConfig?.engine.setAudioScenario(.chorus)
        agoraPrint("becomeSoloSinger")
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.autoSubscribeAudio = true
        //mediaOption.autoSubscribeVideo = true
        mediaOption.publishMediaPlayerId = Int(musicPlayer?.getMediaPlayerId() ?? 0)
        mediaOption.publishMediaPlayerAudioTrack = true
        apiConfig?.engine.updateChannel(with: mediaOption)
        apiConfig?.engine.setDirectExternalAudioSource(true)
        apiConfig?.engine.setRecordingAudioFrameParametersWithSampleRate(48000, channel: 2, mode: .readOnly, samplesPerCall: 960)
        apiConfig?.engine.setAudioFrameDelegate(self)
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
            apiConfig?.engine.updateChannel(with: mediaOption)
            //合唱者先加载歌曲再加入多频道
            didCoSingerLoadMusic(with: songConfig?.songCode ?? 0) { status in
                if status != .OK {//如果歌曲加载失败直接返回
                    self.onJoinExChannelCallBack?(false, .musicPreloadFail)
                } else {
                    self.joinChorus2ndChannel(newRole: role, token: token)
                }
            }
            
        } else if role == .audience {
            agoraPrint("joinChorus fail!")
        }
    }

    private func didCoSingerLoadMusic(with songCode: NSInteger, callBaclk:@escaping LoadMusicCallback) {
        preloadMusic(with: songCode) { status in
        agoraPrint("didCoSingerLoadMusic songCode: \(songCode)")
            callBaclk(status)
            if status == .OK {
                self.apiConfig?.engine.adjustPlaybackSignalVolume(Int(self.remoteVolume))
                self.musicPlayer?.openMedia(songCode: songCode, startPos: 0)
            }
        }

        // 音量最佳实践调整
        apiConfig?.engine.adjustPlaybackSignalVolume(Int(remoteVolume))

        musicPlayer?.openMedia(songCode: songCode, startPos: 0)
    }


    private func joinChorus2ndChannel(newRole: KTVSingRole, token: String) {
        let role = newRole
        if role == .soloSinger || role == .audience {
            agoraPrint("joinChorus2ndChannel with wrong role")
            return
        }
        
        agoraPrint("joinChorus2ndChannel role: \(role.rawValue)")
        apiConfig?.engine.setAudioScenario(.chorus)


        let mediaOption = AgoraRtcChannelMediaOptions()
        // main singer do not subscribe 2nd channel
        // co singer auto sub
        mediaOption.autoSubscribeAudio = role != .leadSinger
      //  mediaOption.autoSubscribeVideo = false
        mediaOption.publishMicrophoneTrack = false
        mediaOption.enableAudioRecordingOrPlayout = role != .leadSinger
        mediaOption.clientRoleType = .broadcaster
        mediaOption.publishDirectCustomAudioTrack = role == .leadSinger

        let rtcConnection = AgoraRtcConnection()
        rtcConnection.channelId = (apiConfig?.channelName ?? "") + "_ex"
        rtcConnection.localUid = UInt(apiConfig?.localUid ?? 0)
       subChorusConnection = rtcConnection

        joinChorusNewRole = role
       let ret = apiConfig?.engine.joinChannelEx(byToken: token, connection: rtcConnection, delegate: self, mediaOptions: mediaOption, joinSuccess: nil)

//        if role == .coSinger {
//        subChorusConnection = rtcConnection
//
//        let ret =
//        apiConfig!.engine.joinChannelEx(byToken: token, connection: rtcConnection, delegate: self, mediaOptions: mediaOption) {[weak self] channelId, uid, elapsed in
//            guard let self = self else {
//                return
//            }
//            agoraPrint("didJoinChannel: \(channelId) uid: \(uid)")
//            if newRole == .leadSinger {
//                self.mainSingerHasJoinChannelEx = true
//                self.onJoinExChannelCallBack?(true, nil)
//            }
//            if newRole == .coSinger {
//                self.didCoSingerLoadMusic(with: self.songConfig?.songCode ?? 0) { status in
//                    if status == .OK {
//                        self.onJoinExChannelCallBack?(true, nil)
//                    } else {
//                        self.onJoinExChannelCallBack?(false, .musicPreloadFail)
//                    }
//                }
//            }
//        }
        agoraPrint("joinChannelEx ret: \(ret ?? -999)")
        if singerRole == .coSinger {
            apiConfig?.engine.muteRemoteAudioStream(UInt(songConfig?.mainSingerUid ?? 0), mute: true)
       }
    }

    private func leaveChorus2ndChannel(_ role: KTVSingRole) {
        guard let config = songConfig else {return}
        guard let subConn = subChorusConnection else {return}
        if (role == .leadSinger) {
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.publishDirectCustomAudioTrack = false
            apiConfig?.engine.updateChannelEx(with: mediaOption, connection: subConn)
            apiConfig?.engine.leaveChannelEx(subConn)
        } else if (role == .coSinger) {
            apiConfig?.engine.leaveChannelEx(subConn)
            apiConfig?.engine.muteRemoteAudioStream(UInt(config.mainSingerUid), mute: false)
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
            musicPlayer?.stop()
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.autoSubscribeAudio = true
         //   mediaOption.autoSubscribeVideo = false
            mediaOption.publishMediaPlayerAudioTrack = false
            apiConfig?.engine.updateChannel(with: mediaOption)
            leaveChorus2ndChannel(role)

            apiConfig?.engine.setAudioScenario(.gameStreaming)
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
    
    /**
     * 加载歌曲
     */
    private func _loadMusic(config: KTVSongConfiguration, mode: KTVLoadMusicMode, onMusicLoadStateListener: KTVMusicLoadStateListener) {
        agoraPrint("_loadMusic songCode: \(config.songCode) role: \(singerRole.rawValue)")
        songConfig = config
        let role = singerRole
        let songCode = config.songCode

//        if (loadDict.keys.contains(String(songCode))) {
//            let loadState = loadDict[String(songCode)]
//            if loadState == .ok {
//                agoraPrint("_loadMusic songCode1: \(config.songCode) role: \(singerRole.rawValue) url: \(lyricUrlMap[String(songCode)] ?? "")")
//                if let url = lyricUrlMap[String(songCode)] {
//                    if (config.autoPlay && (self.singerRole == .soloSinger || self.singerRole == .leadSinger)) {
//                        // 主唱自动播放歌曲
//                        self.startSing(startPos: 0)
//                    }
//                    onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: url)
//                    return
//                }
//            }
//        }

       // loadDict.updateValue(.inProgress, forKey: String(songCode))
        var isLoadUrlSuccess: Bool = false
        var isLoadMusicSuccess: Bool = false

        let KTVGroup = DispatchGroup()
        let KTVQueue = DispatchQueue(label: "com.agora.ktv.www")

        if role != .audience  {
            KTVGroup.enter()
            KTVQueue.async { [weak self] in
                if mode == .loadMusicOnly {
                    isLoadUrlSuccess = true
                    KTVGroup.leave()
                }
                self?.loadLyric(with: songCode, callBack: { url in
                    if let urlPath = url, urlPath.count > 0 {
                        self?.lyricUrlMap.updateValue(urlPath, forKey: String(songCode))
                        self?.setLyric(with: urlPath, callBack: { lyricUrl in
                            isLoadUrlSuccess = true
                            KTVGroup.leave()
                        })
                    } else {
                        self?.loadDict.removeValue(forKey: String(songCode))
                        isLoadUrlSuccess = false
                        KTVGroup.leave()
                    }
                })
            }

            KTVGroup.enter()
            KTVQueue.async {[weak self] in
                if mode == .loadLrcOnly {
                    isLoadUrlSuccess = true
                    KTVGroup.leave()
                }
                self?.preloadMusic(with: songCode, callBaclk: { status in
                    if status != .OK {
                        self?.loadDict.removeValue(forKey: String(songCode))
                        isLoadMusicSuccess = false
                    } else {
                        isLoadMusicSuccess = true
                    }
                    KTVGroup.leave()
                })

            }
        } else {
            KTVGroup.enter()
            KTVQueue.async { [weak self] in
                
                if mode == .loadMusicOnly {
                    isLoadMusicSuccess = true
                    isLoadUrlSuccess = true
                    KTVGroup.leave()
                }
                
                self?.loadLyric(with: songCode, callBack: { url in
                    if let urlPath = url, urlPath.count > 0 {
                        self?.lyricUrlMap.updateValue(urlPath, forKey: String(songCode))
                        self?.setLyric(with: urlPath, callBack: { lyricUrl in
                            isLoadUrlSuccess = true
                            KTVGroup.leave()
                        })
                    } else {
                        self?.loadDict.removeValue(forKey: String(songCode))
                        isLoadUrlSuccess = false
                        KTVGroup.leave()
                    }
                    isLoadMusicSuccess = true
                })
            }
        }

        KTVGroup.notify(queue: .main) { [weak self] in
            if (isLoadUrlSuccess && isLoadMusicSuccess) {
              //  self?.loadDict.updateValue(.ok, forKey: String(songCode))
                if mode == .loadMusicOnly {
                    agoraPrint("only load music callBack ")
                    onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: "")
                    return
                }

                if (config.autoPlay && (self?.singerRole == .soloSinger || self?.singerRole == .leadSinger)) {
                    // 主唱自动播放歌曲
                    if mode != .loadLrcOnly {
                        self?.startSing(startPos: 0)
                    }
                }
                if let url = self?.lyricUrlMap[String(songCode)] {
                    onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: url)
                }
            } else if (!isLoadUrlSuccess && isLoadMusicSuccess) {
                onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, lyricUrl: "", reason: .noLyricUrl)
            } else if (isLoadUrlSuccess && !isLoadMusicSuccess) {
                if let url = self?.lyricUrlMap[String(songCode)] {
                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, lyricUrl: url, reason: .musicPreloadFail)
                }
            } else {
                if let url = self?.lyricUrlMap[String(songCode)] {
                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, lyricUrl: url, reason: .musicPreloadFailedAndNoLyricUrl)
                } else {
                    return
                }
            }
            agoraPrint("_loadMusic finished: \(songCode) url: \(self?.lyricUrlMap[String(songCode)] ?? "")")
        }

    }

    private func loadLyric(with songCode: NSInteger, callBack:@escaping LyricCallback) {
        agoraPrint("loadLyric songCode: \(songCode)")
        let requestId: String = self.mcc.getLyric(songCode: songCode, lyricType: 0)
        self.lyricCallbacks.updateValue(callBack, forKey: requestId)
    }

    private func preloadMusic(with songCode: NSInteger, callBaclk:@escaping LoadMusicCallback) {
        agoraPrint("preloadMusic songCode: \(songCode)")
        var err = self.mcc.isPreloaded(songCode: songCode)
        if err == 0 {
            musicCallbacks.removeValue(forKey: String(songCode))
            callBaclk(.OK)
            return
        }

        err = self.mcc.preload(songCode: songCode, jsonOption: nil)
        if err != 0 {
            musicCallbacks.removeValue(forKey: String(songCode))
            callBaclk(.error)
            return
        }
        musicCallbacks.updateValue(callBaclk, forKey: String(songCode))
    }

    private func setLyric(with url: String, callBack:@escaping LyricCallback) {
        agoraPrint("setLyric url: \(url)")
        if self.lyricCallbacks.keys.contains(url) {
            self.lyricCallbacks.updateValue(callBack, forKey: url)
        }

        hasSendPreludeEndPosition = false
        hasSendEndPosition = false
        totalLines = 0
        totalScore = 0

        downloadManager.downloadLrcFile(urlString: url) {[weak self] lrcurl in
            var path: String? = nil
            defer{
                callBack(path)
            }

            guard let lrcurl = lrcurl else {return}
            let curStr: String = url.components(separatedBy: "/").last ?? ""
            let loadStr: String = lrcurl.components(separatedBy: "/").last ?? ""
            let curSongStr: String = curStr.components(separatedBy: ".").first ?? ""
            let loadSongStr: String = loadStr.components(separatedBy: ".").first ?? ""
            if curSongStr != loadSongStr {
                return
            }

//            var musicUrl: URL
//            if isLocal {
//                let loadUrl: URL = URL(fileURLWithPath: lrcurl)
//                musicUrl = loadUrl
//            } else {
//                guard let loadUrl: URL = URL(string: lrcurl) else {return}
//                musicUrl = loadUrl
//            }
//
//            guard let data = try? Data(contentsOf: musicUrl) else {return}
//            guard let model: LyricModel = KaraokeView.parseLyricData(data: data) else {return}
//            self?.lyricModel = model
//
//            self?.lrcView?.setLyricData(data: model)
            self?.lrcControl?.onDownloadLrcData(url: lrcurl)
//            self?.totalCount = model.lines.count
            path = lrcurl

        } failure: {
            callBack(nil)
            agoraPrint("歌词解析失败")
        }

    }

    func startSing(startPos: Int) {
        let role = singerRole
        agoraPrint("startSing role: \(role.rawValue)")
        if role == .soloSinger {
            apiConfig?.engine.adjustPlaybackSignalVolume(Int(remoteVolume))
            musicPlayer?.openMedia(songCode: songConfig?.songCode ?? 0, startPos: startPos)
        } else {
            agoraPrint("Wrong role playSong, you are not mainSinger right now!")
        }
    }

    /**
     * 停止播放歌曲
     */
    @objc public func stopSing() {
        agoraPrint("stopSing")

        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.autoSubscribeAudio = true
      //  mediaOption.autoSubscribeVideo = true
        mediaOption.publishMediaPlayerAudioTrack = false
        apiConfig?.engine.updateChannel(with: mediaOption)

        if musicPlayer?.getPlayerState() != .stopped {
            musicPlayer?.stop()
        }
  
        apiConfig?.engine.setAudioScenario(.gameStreaming)
    }

}

// rtc的代理回调
extension KTVApiImpl: AgoraRtcEngineDelegate, AgoraAudioFrameDelegate {

    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        agoraPrint("didJoinChannel channel:\(channel) uid: \(uid)")
        if joinChorusNewRole == .leadSinger {
            mainSingerHasJoinChannelEx = true
            onJoinExChannelCallBack?(true, nil)
        }
        if joinChorusNewRole == .coSinger {
            if loadMusicState == .none {
                self.onJoinExChannelCallBack?(true, nil)
            } else {
                self.onJoinExChannelCallBack?(false, .musicPreloadFail)
            }
        }

    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        agoraPrint("didOccurError: \(errorCode.rawValue)")
        if errorCode != .joinChannelRejected {return}
        agoraPrint("join ex channel failed")
        engine.setAudioScenario(.gameStreaming)
        if joinChorusNewRole == .leadSinger {
            mainSingerHasJoinChannelEx = false
            onJoinExChannelCallBack?(false, .joinChannelFail)
        }

        if joinChorusNewRole == .coSinger {
            if loadMusicState == .none {
                self.onJoinExChannelCallBack?(false, .joinChannelFail)
            }
        }
    }

    func onRecordAudioFrame(_ frame: AgoraAudioFrame, channelId: String) -> Bool {
        
        if mainSingerHasJoinChannelEx == true {
            guard let buffer = frame.buffer else {return false}
            apiConfig?.engine.pushDirectAudioFrameRawData(buffer, samples: frame.channels*frame.samplesPerChannel, sampleRate: frame.samplesPerSec, channels: frame.channels)
        }
        return true
    }
}

//需要外部转发的方法 主要是dataStream相关的
extension KTVApiImpl {
    @objc public func didKTVAPIReceiveStreamMessageFrom( uid: NSInteger, streamId: NSInteger, data: Data){
        let songCode: Int = songConfig?.songCode ?? 0
        let role = singerRole
//        guard let state: KTVLoadSongState = loadDict[String(songCode)] else {return}
//        if state != .ok {return}

        guard let dict = dataToDictionary(data: data) else {return}
        if isMainSinger() {return}

        if dict.keys.contains("cmd") {
            if dict["cmd"] as! String == "setLrcTime" {
                guard let position: Int64 = dict["time"] as? Int64 else {return}
                guard let duration: Int64 = dict["duration"] as? Int64 else {return}
                guard let remoteNtp: Int64 = dict["ntp"] as? Int64 else {return}
                guard let voicePitch: Int64 = dict["pitch"] as? Int64 else {return}

                guard let mainSingerState: Int = dict["playerState"] as? Int else {return}
                let state = AgoraMediaPlayerState(rawValue: mainSingerState) ?? .stopped
                if (self.playerState != state) {
                    agoraPrint("recv state with setLrcTime : \(state.rawValue)")
                    self.playerState = state
                    updateCosingerPlayerStatusIfNeed()
                    getEventHander { delegate in
                        delegate.onMusicPlayerStateChanged(state: state, error: .none, isLocal: false)
                    }
                    
                }
                self.remotePlayerPosition = Date().milListamp - TimeInterval(position)
                self.remotePlayerDuration = TimeInterval(duration)
                if (role == .audience) {
                    self.lastAudienceUpTime = Date().milListamp
                }

                if role == .coSinger {
                    if musicPlayer?.getPlayerState() == .playing {
                        let localNtpTime = getNtpTimeInMs()
                        let localPosition = Date().milListamp - self.localPlayerPosition
                        let expectPosition = Int(position) + localNtpTime - Int(remoteNtp) + self.audioPlayoutDelay
                        let threshold = expectPosition - Int(localPosition)
                        if(abs(threshold) > 40) {
                            musicPlayer?.seek(toPosition: expectPosition)
                            agoraPrint("progress: setthreshold: \(threshold) expectPosition: \(expectPosition) position: \(position), localNtpTime: \(localNtpTime), remoteNtp: \(remoteNtp), audioPlayoutDelay: \(self.audioPlayoutDelay), localPosition: \(localPosition)")
                        }
                    }
                } else if role == .audience {
                    self.pitch = Double(voicePitch)
                }

            } else if dict["cmd"] as? String == "PlayerState" {
                let mainSingerState: Int = dict["state"] as? Int ?? 0
                self.playerState = AgoraMediaPlayerState(rawValue: mainSingerState) ?? .idle

                updateCosingerPlayerStatusIfNeed()
                getEventHander { delegate in
                    delegate.onMusicPlayerStateChanged(state: self.playerState, error: .none, isLocal: false)
                }
                
            } else if dict["cmd"] as? String == "setVoicePitch" {
                if role == .coSinger {return}
                //同步新的pitch策略 todo
            }
        }
    }

    @objc public func didKTVAPIReceiveAudioVolumeIndication(with speakers: [AgoraRtcAudioVolumeInfo], totalVolume: NSInteger) {
        if playerState != .playing {return}
        if singerRole == .audience {return}

        let pitch = isNowMicMuted ? 0 : speakers.first?.voicePitch
        self.pitch = pitch ?? 0
        lrcControl?.onUpdatePitch(pitch: Float(self.pitch))
        lrcControl?.onUpdateProgress(progress: Int(getPlayerCurrentTime()))

        if isMainSinger()  {return}
    }

    @objc public func didKTVAPILocalAudioStats(stats: AgoraRtcLocalAudioStats) {
        audioPlayoutDelay = Int(stats.audioDeviceDelay)
    }

}

//private method
extension KTVApiImpl {

    private func initTimer() {
        if timer != nil {
            return
        }

        timer = Timer.scheduledTimer(withTimeInterval: 0.02, block: {[weak self] timer in
            let current = self?.getPlayerCurrentTime()
            if self?.singerRole == .audience && (Date().milListamp - (self?.lastAudienceUpTime ?? 0)) > 1000 {
                return
            }
            self?.setProgress(with: Int(current ?? 0))
            //这里面的逻辑放到control来做
//            guard let lyricModel = self?.lyricModel else {
//                assert(false)
//                return
//            }
//            guard let delegate = self?.delegate else {return}
//            if role == .soloSinger || role == .leadSinger {
//                if (Int(current ?? 0) + 500) >= lyricModel.preludeEndPosition && self?.hasSendPreludeEndPosition == false {
//                    self?.hasSendPreludeEndPosition = true
//                    delegate.didSkipViewShowPreludeEndPosition()
//                } else if Int(current ?? 0) >= lyricModel.duration && self?.hasSendEndPosition == false {
//                    self?.hasSendEndPosition = true
//                    delegate.didSkipViewShowEndDuration()
//                }
//            }
        }, repeats: true)
    }

    private func setPlayerState(with state: AgoraMediaPlayerState) {
        playerState = state
        updateRemotePlayBackVolumeIfNeed()
        updateTimer(with: state)
    }

    private func updateRemotePlayBackVolumeIfNeed() {
        let role = singerRole
        if role == .audience {
            apiConfig?.engine.adjustPlaybackSignalVolume(100)
            return
        }

        let vol = self.playerState == .playing ? remoteVolume : 100
        apiConfig?.engine.adjustPlaybackSignalVolume(Int(vol))
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
        if role == .soloSinger || role == .coSinger || role == .leadSinger{
            let time = Date().milListamp - localPlayerPosition
            return time
        }
        return Date().milListamp - remotePlayerPosition
    }

    private func updateCosingerPlayerStatusIfNeed() {
        let role = singerRole
        if role == .coSinger {
            if playerState == .stopped {
                stopSing()
            } else if playerState == .paused {
                pausePlay()
            } else if playerState == .playing {
                resumeSing()
            }
        }
    }

    private func pausePlay() {
        musicPlayer?.pause()
    }

    private func dataToDictionary(data: Data) -> Dictionary<String, Any>? {
        guard let json = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
            let dictionary = json as? [String: Any] else {
            return nil
        }
        return dictionary
    }

    private func compactDictionaryToData(_ dict: NSDictionary) -> Data? {
        guard JSONSerialization.isValidJSONObject(dict) else { return nil }
        guard let jsonData = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
        return jsonData
    }

    private func getNtpTimeInMs() -> Int {
        var localNtpTime: Int = apiConfig?.engine.getNtpTimeInMs() ?? 0

        if localNtpTime != 0 {
            localNtpTime -= 2208988800 * 1000
        } else {
            localNtpTime = Int(round(Date().timeIntervalSince1970 * 1000.0))
        }

        return localNtpTime
    }

    private func syncPlayState(state: AgoraMediaPlayerState) {
        let dict: [String: Any] = ["cmd": "PlayerState", "userId": apiConfig?.localUid as Any, "state": "\(state.rawValue)"]
        sendStreamMessageWithDict(dict, success: nil)
    }

    private func sendStreamMessageWithDict(_ dict: [String: Any], success: ((_ success: Bool) -> Void)?) {
        let messageData = compactDictionaryToData(dict as NSDictionary)
        let code = apiConfig?.engine.sendStreamMessage(apiConfig?.dataStreamId ?? 0, data: messageData ?? Data())
        if code == 0 && success != nil { success!(true) }
        if code != 0 {
            agoraPrint("sendStreamMessage fail: \(String(describing: code))")
        }
    }

    private func syncPlayState(_ state: AgoraMediaPlayerState) {
        let dict: [String: Any] = [ "cmd": "PlayerState", "userId": apiConfig?.localUid as Any, "state": "\(state.rawValue)" ]
        sendStreamMessageWithDict(dict, success: nil)
    }
    
    private func setProgress(with pos: NSInteger) {
        lrcControl?.onUpdatePitch(pitch: Float(self.pitch))
        lrcControl?.onUpdateProgress(progress: pos)
    }
}

//主要是MPK的回调
extension KTVApiImpl: AgoraRtcMediaPlayerDelegate {
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedToPosition position: Int) {
        self.localPlayerPosition = Date().milListamp - Double(position)
        if isMainSinger() && position > self.audioPlayoutDelay {
            if isMainSinger()  { //if i am main singer
                let dict: [String: Any] = [ "cmd": "setLrcTime",
                                            "duration": self.playerDuration,
                                            "time": position - self.audioPlayoutDelay,
                                            //不同机型delay不同，需要发送同步的时候减去发送机型的delay，在接收同步加上接收机型的delay
                                            "ntp": self.getNtpTimeInMs(),
                                            "pitch": self.pitch,
                                            "playerState": self.playerState.rawValue
                ]
                sendStreamMessageWithDict(dict, success: nil)
            }
//            guard let delegate = self.delegate else {return}
//            delegate.onSyncMusicPosition(position: position, pitch: Float(pitch))
        }
    }
    
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        agoraPrint("loadSong play status: \(state.rawValue) \(songConfig?.songCode ?? 0)")

        if state == .openCompleted {
            self.localPlayerPosition = Date().milListamp
            self.playerDuration = 0
            if isMainSinger() { //主唱播放，通过同步消息“setLrcTime”通知伴唱play
                playerKit.play()
            }
        } else if state == .stopped {
            self.localPlayerPosition = Date().milListamp
            self.playerDuration = 0
            self.remotePlayerPosition = Date().milListamp - 0
        } else if state == .playing {
            self.localPlayerPosition = Date().milListamp - Double(musicPlayer?.getPosition() ?? 0)
            self.playerDuration = 0
        } else if state == .paused {
            self.remotePlayerPosition = Date().milListamp
        } else if state == .playing {
            self.localPlayerPosition = Date().milListamp - Double(musicPlayer?.getPosition() ?? 0)
            self.playerDuration = 0
            self.remotePlayerPosition = Date().milListamp
        }

        if isMainSinger() {
            syncPlayState(state: state)
        }
        self.playerState = state
        agoraPrint("recv state with player callback : \(state.rawValue)")
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
    func onMusicChartsResult(_ requestId: String, status: AgoraMusicContentCenterStatusCode, result: [AgoraMusicChartInfo]) {
        guard let callback = musicChartDict[requestId] else {return}
        callback(requestId, status, result)
        musicChartDict.removeValue(forKey: requestId)

    }

    func onMusicCollectionResult(_ requestId: String, status: AgoraMusicContentCenterStatusCode, result: AgoraMusicCollection) {
        guard let callback = musicSearchDict[requestId] else {return}
        callback(requestId, status, result)
        musicSearchDict.removeValue(forKey: requestId)

    }

    func onLyricResult(_ requestId: String, lyricUrl: String) {
        let callback = self.lyricCallbacks[requestId]
        guard let lyricCallback = callback else { return }
        self.lyricCallbacks.removeValue(forKey: requestId)
        if lyricUrl.isEmpty {
            lyricCallback(nil)
            return
        }
        lyricCallback(lyricUrl)
    }

    func onPreLoadEvent(_ songCode: Int, percent: Int, status: AgoraMusicContentCenterPreloadStatus, msg: String, lyricUrl: String) {
        if (status == .preloading) { return }
        let SongCode = "\(songCode)"
        guard let block = self.musicCallbacks[SongCode] else { return }
        self.musicCallbacks.removeValue(forKey: SongCode)
        block(status)
    }
}

//主要是歌词组件的回调
extension KTVApiImpl: KaraokeDelegate {
    func onKaraokeView(view: KaraokeView, didDragTo position: Int) {
        musicPlayer?.seek(toPosition: position)
        totalScore = view.scoringView.getCumulativeScore()
        //将分数传到vc
//        guard let delegate = self.delegate else {return}
//        delegate.didlrcViewDidScrolled(with: self.totalScore, totalScore: self.totalCount * 100)
    }

    func onKaraokeView(view: KaraokeView, didFinishLineWith model: LyricLineModel, score: Int, cumulativeScore: Int, lineIndex: Int, lineCount: Int) {
        self.totalLines = lineCount
        self.totalScore = cumulativeScore
        //将分数传到vc
//        guard let delegate = self.delegate else {return}
//        delegate.didlrcViewDidScrollFinished(with: self.totalScore, totalScore: lineCount * 100, lineScore: score)
    }
}

//主要是歌曲下载的回调
extension KTVApiImpl: AgoraLrcDownloadDelegate {

    func downloadLrcFinished(url: String) {
        agoraPrint("download lrc finished \(url)")
        guard let callback = self.lyricCallbacks[url] else { return }
        self.lyricCallbacks.removeValue(forKey: url)
        callback(url)
    }

    func downloadLrcError(url: String, error: Error?) {
        agoraPrint("download lrc fail \(url): \(String(describing: error))")
        guard let callback = self.lyricCallbacks[url] else { return }
        self.lyricCallbacks.removeValue(forKey: url)
        callback(nil)
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
