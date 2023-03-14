//
//  KTVApiImpl.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraRtcKit

class KTVApiImpl: NSObject, AgoraRtcMediaPlayerDelegate {
    private var apiConfig: KTVApiConfig?
    
    private var songConfig: KTVSongConfiguration?
    private var subChorusConnection: AgoraRtcConnection?
    private var downloadManager: AgoraDownLoadManager = AgoraDownLoadManager()
    
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    private var musicPlayer: AgoraMusicPlayerProtocol?
    private var mcc: AgoraMusicContentCenter?
    
    private var loadSongMap = Dictionary<String, KTVLoadSongState>()
    private var lyricUrlMap = Dictionary<String, String>()
    private var loadDict = Dictionary<String, KTVLoadSongState>()
    private var lyricCallbacks = Dictionary<String, LyricCallback>()
    private var musicCallbacks = Dictionary<String, LoadMusicCallback>()
    
    private var remoteVolume: Int = 15
    
    private var musicChartDict: [String: MusicChartCallBacks] = [:]
    private var musicSearchDict: Dictionary<String, MusicResultCallBacks> = Dictionary<String, MusicResultCallBacks>()
    private var onJoinExChannelCallBack : JoinExChannelCallBack?
    
    private var singerRole: KTVSingRole = .audience
}

//MARK: KTVApiDelegate
extension KTVApiImpl: KTVApiDelegate {
    func setup(config: KTVApiConfig) {
        self.apiConfig = config
        
        // ------------------ 初始化内容中心 ------------------
        let contentCenterConfiguration = AgoraMusicContentCenterConfig()
        contentCenterConfiguration.appId = config.appId
        contentCenterConfiguration.mccUid = config.localUid
        contentCenterConfiguration.token = config.rtmToken
        contentCenterConfiguration.rtcEngine = config.engine
 
        mcc = AgoraMusicContentCenter.sharedContentCenter(config: contentCenterConfiguration)

        // ------------------ 初始化音乐播放器实例 ------------------
        musicPlayer = mcc?.createMusicPlayer(delegate: self)
        
        // 音量最佳实践调整
        musicPlayer?.adjustPlayoutVolume(50)
        musicPlayer?.adjustPublishSignalVolume(50)
        
        downloadManager.delegate = self
        
//        initTimer()
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
//        freeTimer()
        downloadManager.delegate = nil
        apiConfig?.engine.setAudioFrameDelegate(nil)
        lyricCallbacks.removeAll()
        musicCallbacks.removeAll()
        self.eventHandlers.removeAllObjects()
    }
    
    func fetchMusicCharts(completion: @escaping MusicChartCallBacks) -> String {
        let requestId = mcc!.getMusicCharts()
        musicChartDict[requestId] = completion
        
        return requestId
    }
    
    func searchMusic(musicChartId: Int,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion:@escaping (String, AgoraMusicContentCenterStatusCode, AgoraMusicCollection) -> Void) -> String {
        let requestId = mcc!.getMusicCollection(musicChartId: musicChartId, page: page, pageSize: pageSize, jsonOption: jsonOption)
        musicSearchDict[requestId] = completion
        return requestId
    }
    
    func searchMusic(keyword: String,
                     page: Int,
                     pageSize: Int,
                     jsonOption: String,
                     completion: @escaping (String, AgoraMusicContentCenterStatusCode, AgoraMusicCollection) -> Void) -> String {
        let requestId = mcc!.searchMusic(keyWord: keyword, page: page, pageSize: pageSize, jsonOption: jsonOption)
        musicSearchDict[requestId] = completion
        return requestId
    }
    
    func loadMusic(config: KTVSongConfiguration, onMusicLoadStateListener: KTVMusicLoadStateListener) {
        self.songConfig = config
        guard let songCode = config.songCode, let mcc = self.mcc else {
            //TODO: error
            return
        }
        var err = mcc.isPreloaded(songCode: songCode)
        if err == 0 {
            musicCallbacks.removeValue(forKey: String(songCode))
//            callBaclk(.OK)
            return
        }
        
        err = mcc.preload(songCode: songCode, jsonOption: nil)
        if err != 0 {
            musicCallbacks.removeValue(forKey: String(songCode))
//            callBaclk(.error)
            return
        }
//        musicCallbacks.updateValue(callBaclk, forKey: String(songCode))
    }
    
    func switchSingerRole(newRole: KTVSingRole, token: String, onSwitchRoleState: (KTVSwitchRoleState, KTVSwitchRoleFailReason) -> Void) {
        print("switchSingerRole oldRole: (singerRole), newRole: (newRole)")
        let oldRole = singerRole
        //TODO(chenpan): fix it
//        if oldRole == .audience && newRole == .soloSinger {
//            // 1、KTVSingRoleAudience -》KTVSingRoleMainSinger
//            songConfig?.role = newRole
//            becomeSoloSinger()
//            delegate.onSingerRoleChanged(oldRole: .audience, newRole: .soloSinger)
//            stateCallBack(.success, .none)
//        } else if oldRole == .audience && newRole == .leadSinger {
//            becomeSoloSinger()
//            joinChorus(token: apiConfig?.rtmToken ?? "", joinExChannelCallBack: { flag, status in
//                if flag == true {
//                    self.songConfig?.role = newRole
//                    stateCallBack(.success, .none)
//                } else {
//                    self.leaveChorus()
//                    if status == .musicPreloadFail {
//                        stateCallBack(.fail, .musicPreloadFail)
//                    } else if status == .joinChannelFail {
//                        stateCallBack(.fail, .joinChannelFail)
//                    } else if status == .musicPreloadFailAndJoinchannelFail {
//                        stateCallBack(.fail, .musicPreloadFailAndJoinchannelFail)
//                    }
//                }
//            })
//
//        } else if oldRole == .soloSinger && newRole == .audience {
//            stopSing()
//            singerRole = newRole
//            delegate.onSingerRoleChanged(oldRole: .soloSinger, newRole: .audience)
//            stateCallBack(.success, .none)
//        } else if oldRole == .audience && newRole == .coSinger {
//            joinChorus(token: apiConfig?.rtmToken ?? "", joinExChannelCallBack: { flag, status in
//                if flag == true {
//                    self.songConfig?.role = newRole
//                    stateCallBack(.success, .none)
//                } else {
//                    self.leaveChorus()
//                    if status == .musicPreloadFail {
//                        stateCallBack(.fail, .musicPreloadFail)
//                    } else if status == .joinChannelFail {
//                        stateCallBack(.fail, .joinChannelFail)
//                    } else if status == .musicPreloadFailAndJoinchannelFail {
//                        stateCallBack(.fail, .musicPreloadFailAndJoinchannelFail)
//                    }
//                }
//            })
//        } else if oldRole == .coSinger && newRole == .audience {
//            leaveChorus()
//            songConfig?.role = newRole
//            delegate.onSingerRoleChanged(oldRole: .coSinger, newRole: .audience)
//            stateCallBack(.success, .none)
//        } else if oldRole == .soloSinger && newRole == .leadSinger {
//            joinChorus(token: apiConfig?.rtmToken ?? "", joinExChannelCallBack: { flag, status in
//                if flag == true {
//                    self.songConfig?.role = newRole
//                    stateCallBack(.success, .none)
//                } else {
//                    self.leaveChorus()
//                    if status == .musicPreloadFail {
//                        stateCallBack(.fail, .musicPreloadFail)
//                    } else if status == .joinChannelFail {
//                        stateCallBack(.fail, .joinChannelFail)
//                    } else if status == .musicPreloadFailAndJoinchannelFail {
//                        stateCallBack(.fail, .musicPreloadFailAndJoinchannelFail)
//                    }
//                }
//            })
//        } else if oldRole == .leadSinger && newRole == .soloSinger {
//            leaveChorus()
//            songConfig?.role = newRole
//            delegate.onSingerRoleChanged(oldRole: .leadSinger, newRole: .soloSinger)
//            stateCallBack(.success, .none)
//        } else if oldRole == .leadSinger && newRole == .audience {
//            stopSing()
//            songConfig?.role = newRole
//            delegate.onSingerRoleChanged(oldRole: .leadSinger, newRole: .audience)
//            stateCallBack(.success, .none)
//        } else {
//            stateCallBack(.fail, .noPermission)
//            print("Error！You can not switch role from $singerRole to $newRole!")
//        }
        
    }
    
    func startSing(startPos: Int) {
        let role = singerRole
        print("playSong called: $singerRole")
        if role == .soloSinger {
            apiConfig?.engine.adjustPlaybackSignalVolume(Int(remoteVolume))
            musicPlayer?.openMedia(songCode: songConfig?.songCode ?? 0, startPos: 0)
        } else {
            print("Wrong role playSong, you are not mainSinger right now!")
        }
    }
    
    func resumeSing() {
        
    }
    
    func pauseSing() {
        
    }
    
    func seekSing(time: Int) {
        
    }
    
    func setLrcView(view: KTVLrcViewDelegate) {
        
    }
    
    func setMicStatus(isOnMicOpen: Bool) {
        
    }
    
    func getMediaPlayer() -> AgoraMusicPlayerProtocol? {
        return musicPlayer
    }
}

//MARK: AgoraLrcDownloadDelegate
extension KTVApiImpl: AgoraLrcDownloadDelegate {
    
}

//MARK: AgoraMusicContentCenterEventDelegate
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
