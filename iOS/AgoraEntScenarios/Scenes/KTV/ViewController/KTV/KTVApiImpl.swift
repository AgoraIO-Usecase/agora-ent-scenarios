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
    private var loadMusicListeners: NSMapTable<NSString, AnyObject> = NSMapTable<NSString, AnyObject>(keyOptions: .copyIn, valueOptions: .weakMemory)

    private var musicPlayer: AgoraMusicPlayerProtocol!
    private var mcc: AgoraMusicContentCenter!

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

    private var singerRole: KTVSingRole = .audience {
        didSet {
            agoraPrint("singerRole changed: \(oldValue.rawValue)->\(singerRole.rawValue)")
        }
    }
    private var lrcControl: KTVLrcViewDelegate?
    
    private var timer: Timer?
    private var isPause: Bool = false
    
    public var remoteVolume: Int = 15
    private var joinChorusNewRole: KTVSingRole = .audience
    private var oldPitch: Double = 0
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
    func getMusicCenter() -> AgoraMusicContentCenter? {
        return mcc
    }
    
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
        mcc.register(nil)
        AgoraMusicContentCenter.destroy()
        musicPlayer.stop()
        apiConfig?.engine.destroyMediaPlayer(musicPlayer)
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

    func switchSingerRole(newRole: KTVSingRole, onSwitchRoleState: @escaping (KTVSwitchRoleState, KTVSwitchRoleFailReason) -> Void) {
        let oldRole = singerRole
        self.switchSingerRole(oldRole: oldRole, newRole: newRole, token: apiConfig?.chorusChannelToken ?? "", stateCallBack: onSwitchRoleState)
    }

    /**
     * 恢复播放
     */
    @objc public func resumeSing() {
        agoraPrint("resumeSing")
        if musicPlayer?.getPlayerState() == .paused {
            musicPlayer?.resume()
        } else {
            let ret = musicPlayer?.play()
            agoraPrint("resumeSing ret: \(ret ?? -1)")
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
                    self.playerState = self.musicPlayer?.getPlayerState() ?? .idle
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
        } else {
            stateCallBack(.fail, .noPermission)
            agoraPrint("Error！You can not switch role from \(oldRole.rawValue) to \(newRole.rawValue)!")
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
            
            musicPlayer.openMedia(songCode: songConfig?.songCode ?? 0, startPos: 0)
            
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
        rtcConnection.channelId = apiConfig?.chorusChannelName ?? ""
        rtcConnection.localUid = UInt(apiConfig?.localUid ?? 0)
       subChorusConnection = rtcConnection

        joinChorusNewRole = role
       let ret = apiConfig?.engine.joinChannelEx(byToken: token, connection: rtcConnection, delegate: self, mediaOptions: mediaOption, joinSuccess: nil)
        agoraPrint("joinChannelEx ret: \(ret ?? -999)")
        if newRole == .coSinger {
            let uid = UInt(songConfig?.mainSingerUid ?? 0)
            let ret =
            apiConfig?.engine.muteRemoteAudioStream(uid, mute: true)
            agoraPrint("muteRemoteAudioStream: \(uid), ret: \(ret ?? -1)")
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
    
    private func _loadMusic(config: KTVSongConfiguration, mode: KTVLoadMusicMode, onMusicLoadStateListener: KTVMusicLoadStateListener){
        
        songConfig = config
        let role = singerRole
        let songCode = config.songCode
        
        if mode == .loadLrcOnly {
            loadLyric(with: songCode, callBack: {[weak self] url in
                agoraPrint("loadLrcOnly: songCode:\(songCode) ulr:\(String(describing: url))")
                
                if self?.songConfig?.songCode != songCode {
                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .cancled)
                    return
                }
                
                if let urlPath = url, urlPath.count > 0 {
                        self?.lyricUrlMap.updateValue(urlPath, forKey: String(songCode))
                        self?.setLyric(with: urlPath, callBack: { lyricUrl in
                          //  self?.loadSongState = .ok
                            onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: urlPath)
                    })
                } else {
                  //  self?.loadSongState = .failed
                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .noLyricUrl)
                }
                return
            })
        } else {
            loadMusicListeners.setObject(onMusicLoadStateListener, forKey: "\(songCode)" as NSString)
            onMusicLoadStateListener.onMusicLoadProgress(songCode: songCode, percent: 0, status: .preloading, msg: "", lyricUrl: "")
            //TODO(chenpan):如果没有缓存，才加，否则有缓存不会有进度给出100%
            if mcc.isPreloaded(songCode: songCode) != 0 {
               onMusicLoadStateListener.onMusicLoadProgress(songCode: songCode, percent: 0, status: .preloading, msg: "", lyricUrl: "")
            }
            preloadMusic(with: songCode) {[weak self] status, songCode in
                
                if self?.songConfig?.songCode != songCode {
                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .cancled)
                    return
                }

                if status == .OK {
                    
                    if mode == .loadMusicAndLrc {
                        //需要加载歌词
                        self?.loadLyric(with: songCode, callBack: { url in
                            agoraPrint("loadMusicAndLrc: songCode:\(songCode) status:\(status.rawValue) ulr:\(String(describing: url))")
                            
                            if self?.songConfig?.songCode != songCode {
                                onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .cancled)
                                return
                            }
                            
                            if let urlPath = url, urlPath.count > 0 {
                                    self?.lyricUrlMap.updateValue(urlPath, forKey: String(songCode))
                                    self?.setLyric(with: urlPath, callBack: { lyricUrl in
                                      //  self?.loadSongState = .ok
                                        onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: urlPath)
                                })
                            } else {
                               // self?.loadSongState = .failed
                                onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .noLyricUrl)
                            }
                            
                            if config.autoPlay == true {
                                self?.startSing(startPos: 0)
                            }
                            
                        })
                    } else if mode == .loadMusicOnly {
                        agoraPrint("loadMusicOnly: songCode:\(songCode) load success")
                        if config.autoPlay == true {
                            self?.startSing(startPos: 0)
                        }
                        onMusicLoadStateListener.onMusicLoadSuccess(songCode: songCode, lyricUrl: "")
                    }
                    
                } else {
                    agoraPrint("load music failed songCode:\(songCode)")
                   // self?.loadSongState = .failed
                    onMusicLoadStateListener.onMusicLoadFail(songCode: songCode, reason: .musicPreloadFail)
                }
            }
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
            callBaclk(.OK, songCode)
            return
        }

        err = self.mcc.preload(songCode: songCode, jsonOption: nil)
        if err != 0 {
            musicCallbacks.removeValue(forKey: String(songCode))
            callBaclk(.error, songCode)
            return
        }
        musicCallbacks.updateValue(callBaclk, forKey: String(songCode))
    }

    private func setLyric(with url: String, callBack:@escaping LyricCallback) {
        agoraPrint("setLyric url: \(url)")
        if self.lyricCallbacks.keys.contains(url) {
            self.lyricCallbacks.updateValue(callBack, forKey: url)
        }

        downloadManager.downloadLrcFile(urlString: url) {[weak self] lrcurl in
            var path: String? = nil
            defer{
                callBack(path)
            }

            guard let lrcurl = lrcurl else {
                agoraPrint("downloadLrcFile fail, lrcurl = nil")
                return
            }
            let curStr: String = url.components(separatedBy: "/").last ?? ""
            let loadStr: String = lrcurl.components(separatedBy: "/").last ?? ""
            let curSongStr: String = curStr.components(separatedBy: ".").first ?? ""
            let loadSongStr: String = loadStr.components(separatedBy: ".").first ?? ""
            if curSongStr != loadSongStr {
                agoraPrint("downloadLrcFile fail, missmatch cur:\(curSongStr) load:\(loadSongStr)")
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
        apiConfig?.engine.adjustPlaybackSignalVolume(Int(remoteVolume))
        let ret = musicPlayer?.openMedia(songCode: songConfig?.songCode ?? 0, startPos: startPos)
        agoraPrint("startSing->openMedia(\(songConfig?.songCode ?? -1) fail: \(ret ?? -1)")
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
           self.onJoinExChannelCallBack?(true, nil)
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
            self.onJoinExChannelCallBack?(false, .joinChannelFail)
        }
    }

    func onRecordAudioFrame(_ frame: AgoraAudioFrame, channelId: String) -> Bool {
        
        if mainSingerHasJoinChannelEx == true {
            guard let buffer = frame.buffer else {return false}
            apiConfig?.engine.pushDirectAudioFrameRawData(buffer, samples: frame.channels*frame.samplesPerChannel, sampleRate: frame.samplesPerSec, channels: frame.channels)
        }
        return true
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        getEventHander { delegate in
            delegate.onChorusChannelTokenPrivilegeWillExpire(token: token)
        }
    }
}

//需要外部转发的方法 主要是dataStream相关的
extension KTVApiImpl {
    @objc public func didKTVAPIReceiveStreamMessageFrom( uid: NSInteger, streamId: NSInteger, data: Data){
//        let songCode: Int = songConfig?.songCode ?? 0
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
               // guard let voicePitch: Double = dict["pitch"] as? Double else {return}
                guard let realPosition: Int64 = dict["realTime"] as? Int64 else {return}
                guard let songCode: Int64 = dict["songCode"] as? Int64 else {return}
                guard let mainSingerState: Int = dict["playerState"] as? Int else {return}
//                if songConfig?.songCode == 0 {
//                    songConfig?.songCode = Int(songCode)
//                }
                //如果接收到的歌曲和自己本地的歌曲不一致就不更新进度
                if songCode != songConfig?.songCode ?? 0 {
                    agoraPrint("local songCode[\(songCode)] is not equal to recv songCode[\(songConfig?.songCode ?? 0)] role: \(singerRole.rawValue)")
                    return
                }
          
                self.remotePlayerDuration = TimeInterval(duration)
                self.lastMainSingerUpdateTime = Date().milListamp
                self.remotePlayerPosition = TimeInterval(realPosition)
                print("remote position: \(self.remotePlayerPosition)")
                let state = AgoraMediaPlayerState(rawValue: mainSingerState) ?? .stopped
                if (self.playerState != state) {
                    agoraPrint("[setLrcTime] recv state: \(self.playerState.rawValue)->\(state.rawValue) role: \(singerRole.rawValue) role: \(singerRole.rawValue)")
                    if state == .playing, singerRole == .coSinger, playerState == .openCompleted {
                        //如果是伴唱等待主唱开始播放，seek 到指定位置开始播放保证歌词显示位置准确
                        self.localPlayerPosition = self.lastMainSingerUpdateTime - Double(position)
                        agoraPrint("seek toPosition: \(position)")
                        musicPlayer.seek(toPosition: Int(position))
                    }
                    print("recv state with MainSinger0: \(state.rawValue)")
                    syncPlayStateFromRemote(state: state, needDisplay: false)
                }
                
                if role == .coSinger {
                    if musicPlayer?.getPlayerState() == .playing {
                        let localNtpTime = getNtpTimeInMs()
                        let localPosition = self.lastMainSingerUpdateTime - self.localPlayerPosition
                        let expectPosition = Int(position) + localNtpTime - Int(remoteNtp) + self.audioPlayoutDelay
                        let threshold = expectPosition - Int(localPosition)
                        if(abs(threshold) > 40) {
                            musicPlayer?.seek(toPosition: expectPosition)
                            agoraPrint("progress: setthreshold: \(threshold) expectPosition: \(expectPosition) position: \(position), localNtpTime: \(localNtpTime), remoteNtp: \(remoteNtp), audioPlayoutDelay: \(self.audioPlayoutDelay), localPosition: \(localPosition)")
                        }
                    } else {
                       // self.pitch = Double(voicePitch)
                    }
                } else if role == .audience {
                  //  self.pitch = Double(voicePitch)
                }

            } else if dict["cmd"] as? String == "PlayerState" {
                let mainSingerState: Int = dict["state"] as? Int ?? 0
                let state = AgoraMediaPlayerState(rawValue: mainSingerState) ?? .idle
                if state == .playing, singerRole == .coSinger, playerState == .openCompleted {
                    //如果是伴唱等待主唱开始播放，seek 到指定位置开始播放保证歌词显示位置准确
                    self.localPlayerPosition = getPlayerCurrentTime()
                    agoraPrint("seek toPosition: \(self.localPlayerPosition)")
                    musicPlayer.seek(toPosition: Int(self.localPlayerPosition))
                }
                print("recv state with MainSinger: \(state.rawValue)")
                syncPlayStateFromRemote(state: state, needDisplay: true)
                
            } else if dict["cmd"] as? String == "setVoicePitch" {
                if role == .audience {
                    guard let voicePitch: Double = dict["pitch"] as? Double else {return}
                    print("test pitch:\(voicePitch)")
                    self.pitch = voicePitch
                }
            }
        }
    }

    @objc public func didKTVAPIReceiveAudioVolumeIndication(with speakers: [AgoraRtcAudioVolumeInfo], totalVolume: NSInteger) {
        if playerState != .playing {return}
        if singerRole == .audience {return}

        guard var pitch: Double = speakers.first?.voicePitch else {return}
        pitch = isNowMicMuted ? 0 : pitch
        //如果mpk不是playing状态 pitch = 0
        if musicPlayer.getPlayerState() != .playing {pitch = 0}
        self.pitch = pitch
        lrcControl?.onUpdatePitch(pitch: Float(self.pitch))
        lrcControl?.onUpdateProgress(progress: Int(getPlayerCurrentTime()))
        
        //将主唱的pitch同步到观众
        if isMainSinger() {
            let dict: [String: Any] = [ "cmd": "setVoicePitch",
                                        "pitch": pitch,
            ]
            print("sendPitch: \(pitch)")
            sendStreamMessageWithDict(dict, success: nil)
        }
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
            if self?.singerRole == .audience && (Date().milListamp - (self?.lastMainSingerUpdateTime ?? 0)) > 1000 {
                return
            }
            
            if self?.singerRole == .audience {
                if self?.oldPitch == self?.pitch && (self?.oldPitch != 0 && self?.pitch != 0) {
                    self?.pitch = -1
                }
            }
          
            self?.setProgress(with: Int(current ?? 0))
            self?.oldPitch = self?.pitch ?? -1
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
        if role == .soloSinger || role == .leadSinger{
            let time = Date().milListamp - localPlayerPosition
//                agoraPrint("getPlayerCurrentTime MainSinger \(time) \(playerState.rawValue)")
            return time
        } else if role == .coSinger {
            if playerState == .playing || playerState == .paused {
                let time = Date().milListamp - localPlayerPosition
//                agoraPrint("getPlayerCurrentTime CoSinger \(time) \(playerState.rawValue)")
                return time
            }
        }
        
        var position = Date().milListamp - self.lastMainSingerUpdateTime + remotePlayerPosition
        if playerState != .playing {
            position = remotePlayerPosition
        }
//        agoraPrint("getPlayerCurrentTime Audience \(position)")
        return position
    }

    private func syncPlayStateFromRemote(state: AgoraMediaPlayerState, needDisplay: Bool) {
        let role = singerRole
        if role == .coSinger {
            if state == .stopped {
                stopSing()
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
        musicPlayer?.pause()
    }

    private func dataToDictionary(data: Data) -> Dictionary<String, Any>? {
        guard let json = try? JSONSerialization.jsonObject(with: data, options: []),
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

    private func syncPlayState(state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        let dict: [String: Any] = ["cmd": "PlayerState", "userId": apiConfig?.localUid as Any, "state": state.rawValue, "error": "\(error.rawValue)"]
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
    
    private func setProgress(with pos: Int) {
        lrcControl?.onUpdatePitch(pitch: Float(self.pitch))
        lrcControl?.onUpdateProgress(progress: pos)
        print("test: pitch:\(self.pitch) pos: \(pos)")
    }
}

//主要是MPK的回调
extension KTVApiImpl: AgoraRtcMediaPlayerDelegate {
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedToPosition position: Int) {
        self.localPlayerPosition = Date().milListamp - Double(position)
        if isMainSinger() && getPlayerCurrentTime() > TimeInterval(self.audioPlayoutDelay) {
            let dict: [String: Any] = [ "cmd": "setLrcTime",
                                        "duration": self.playerDuration,
                                        "time": getPlayerCurrentTime(),
                                        //不同机型delay不同，需要发送同步的时候减去发送机型的delay，在接收同步加上接收机型的delay
                                        "realTime":position,
                                        "ntp": self.getNtpTimeInMs(),
                                        "playerState": self.playerState.rawValue,
                                        "songCode": songConfig?.songCode ?? 0
            ]
            sendStreamMessageWithDict(dict, success: nil)
            print("lrc: \(getPlayerCurrentTime())")
        }
    }
    
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol, didChangedTo state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        agoraPrint("agoraRtcMediaPlayer didChangedToState: \(state.rawValue) \(songConfig?.songCode ?? 0)")

        if state == .openCompleted {
            self.localPlayerPosition = Date().milListamp
            self.playerDuration = TimeInterval(musicPlayer.getDuration())
            playerKit.selectAudioTrack(1)
            if isMainSinger() { //主唱播放，通过同步消息“setLrcTime”通知伴唱play
                playerKit.play()
            }
        } else if state == .stopped {
            self.localPlayerPosition = Date().milListamp
            self.playerDuration = 0
//            self.remotePlayerPosition = Date().milListamp - 0
        }
        else if state == .paused {
//            self.remotePlayerPosition = Date().milListamp
        } else if state == .playing {
            self.localPlayerPosition = Date().milListamp - Double(musicPlayer?.getPosition() ?? 0)
//            self.remotePlayerPosition = Date().milListamp
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
        agoraPrint("加载的歌词地址:\(lyricUrl)")
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
        if let listener = self.loadMusicListeners.object(forKey: "\(songCode)" as NSString) as? KTVMusicLoadStateListener {
            listener.onMusicLoadProgress(songCode: songCode, percent: percent, status: status, msg: msg, lyricUrl: lyricUrl)
        }
        if (status == .preloading) { return }
        let SongCode = "\(songCode)"
        guard let block = self.musicCallbacks[SongCode] else { return }
        self.musicCallbacks.removeValue(forKey: SongCode)
        block(status, songCode)
    }
}

//主要是歌词组件的回调
//extension KTVApiImpl: KaraokeDelegate {
//    func onKaraokeView(view: KaraokeView, didDragTo position: Int) {
//        musicPlayer?.seek(toPosition: position)
//        totalScore = view.scoringView.getCumulativeScore()
//        //将分数传到vc
////        guard let delegate = self.delegate else {return}
////        delegate.didlrcViewDidScrolled(with: self.totalScore, totalScore: self.totalCount * 100)
//    }
//
//    func onKaraokeView(view: KaraokeView, didFinishLineWith model: LyricLineModel, score: Int, cumulativeScore: Int, lineIndex: Int, lineCount: Int) {
//        self.totalLines = lineCount
//        self.totalScore = cumulativeScore
//        //将分数传到vc
////        guard let delegate = self.delegate else {return}
////        delegate.didlrcViewDidScrollFinished(with: self.totalScore, totalScore: lineCount * 100, lineScore: score)
//    }
//}

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
