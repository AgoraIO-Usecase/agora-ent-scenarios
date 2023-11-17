//
//  VideoLoaderApiImpl.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import AgoraRtcKit

public class VideoLoaderApiImpl: NSObject {
    public static let shared = VideoLoaderApiImpl()
    public var printClosure: ((String)->())?
    public var warningClosure: ((String)->())?
    public var errorClosure: ((String)->())?
    private var config: VideoLoaderConfig?
    
    private let apiProxy = VideoLoaderApiProxy()
    private var profilerMap: [String: VideoLoaderProfiler] = [:]
    private var rtcProxys = [String: VideoLoaderAgoraExProxy]()
    //[ex channelId: connection]
    private var exConnectionMap: [String: AgoraRtcConnection] = [:]
    //[ex channelId: [tagId: status]]
    private var exConnectionDeps: [String: [String: AnchorState]] = [:]
    private var renderViewMap: [String: AnchorInfo] = [:]
        
    deinit {
        apiPrint("deinit-- VideoLoaderApiImpl")
        cleanCache()
        rtcProxys.forEach { key, value in
            value.removeAllListener()
        }
    }
    
    private override init() {
        apiPrint("init-- VideoLoaderApiImpl")
        super.init()
    }
}

//MARK: private
extension VideoLoaderApiImpl {
    private func _getProfiler(anchorId: String) -> VideoLoaderProfiler {
        let profiler = profilerMap[anchorId] ?? VideoLoaderProfiler(anchorId: anchorId)
        profiler.firstFrameCompletion = {[weak self] cost, uid in
            guard let self = self else {return}
            let api = self.apiProxy as IVideoLoaderApiListener
            api.onFirstFrameRecv?(channelName: anchorId, uid: uid, elapsed: cost)
        }
        profilerMap[anchorId] = profiler
        return profiler
    }
    
    private func _getProxy(anchorId: String) -> VideoLoaderAgoraExProxy {
        var rtcProxy = rtcProxys[anchorId]
        if rtcProxy == nil {
            let proxy = VideoLoaderAgoraExProxy()
            rtcProxys[anchorId] = proxy
            rtcProxy = proxy
        }
        
        return rtcProxy!
    }
    
    private func _updateChannelEx(channelId: String, options: AgoraRtcChannelMediaOptions) {
        guard let engine = config?.rtcEngine,
              let connection = exConnectionMap[channelId] else {
            errorLoaderPrint("updateChannelEx fail[\(channelId)]: connection is empty")
            return
        }
        engine.updateChannelEx(with: options, connection: connection)
    }
    
    private func _joinChannelEx(channelId: String,
                                ownerId: UInt,
                                localUid: UInt,
                                token: String,
                                options:AgoraRtcChannelMediaOptions) {
        guard let engine = config?.rtcEngine else {
//            assert(true, "rtc engine not initlized")
            errorLoaderPrint("joinChannel[\(channelId)] fail! rtc engine not initlized")
            return
        }
//        assert(token.count > 0, "token is empty")
        
        if localUid == ownerId {
            errorLoaderPrint("joinChannel[\(channelId)] fail! ownerId == localUid")
            return
        }
        
        if let _ = exConnectionMap[channelId] {
            return
        }
        
        if token.count == 0 {
            errorLoaderPrint("joinChannel[\(channelId)] fail! token is empty")
        }
        
        let subscribeStatus = false
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = subscribeStatus
        mediaOptions.autoSubscribeVideo = subscribeStatus
        mediaOptions.clientRoleType = .audience
        // 极速直播?
        mediaOptions.audienceLatencyLevel = .lowLatency
    
        let connection = AgoraRtcConnection()
        connection.channelId = channelId
        connection.localUid = localUid
        
        //TODO: retain cycle in joinChannelEx to use rtcProxy
        let rtcProxy = _getProxy(anchorId: channelId)
        let profiler = _getProfiler(anchorId: channelId)
        addRTCListener(anchorId: channelId, listener: profiler)
        
        let date = Date()
        debugLoaderPrint("try to join room[\(connection.channelId)] ex uid: \(connection.localUid)")
//        let type = AgoraAudioSessionOperationRestriction(rawValue: 0)
//        engine.setAudioSessionOperationRestriction(type)
        let ret =
        engine.joinChannelEx(byToken: token,
                             connection: connection,
                             delegate: rtcProxy,
                             mediaOptions: mediaOptions) { channelName, uid, elapsed in
            let cost = Int(-date.timeIntervalSinceNow * 1000)
            debugLoaderPrint("join room[\(channelName)] ex success uid: \(uid) cost \(cost) ms")
        }
        
        engine.updateChannelEx(with: mediaOptions, connection: connection)
        exConnectionMap[channelId] = connection
            
        if ret == 0 {
            debugLoaderPrint("join room ex[\(channelId)]: ownerId: \(ownerId) connection count: \(exConnectionMap.count)")
        }else{
            errorLoaderPrint("join room ex fail[\(channelId)]: ownerId: \(ownerId) token = \(token), \(ret)")
        }
    }
    
    func _leaveChannelEx(channelId: String) {
        guard let rtcEngine = config?.rtcEngine,
              let connection = exConnectionMap[channelId] else { return }
        let depMap = exConnectionDeps[channelId]
        if depMap?.count ?? 0 > 0 {
            debugLoaderPrint("leaveChannelEx break, depcount: \(depMap?.count ?? 0), channelId: \(channelId)")
            return
        }
        
        let profiler = _getProfiler(anchorId: channelId)
        removeRTCListener(anchorId: channelId, listener: profiler)
//        rtcEngine.setAudioSessionOperationRestriction(.all)
        rtcEngine.leaveChannelEx(connection)
        exConnectionMap[channelId] = nil
        debugLoaderPrint("leaveChannelEx channelId: \(channelId)  connection count: \(exConnectionMap.count)")
    }
}

//MARK: VideoLoaderApiProtocol
extension VideoLoaderApiImpl: IVideoLoaderApi {
    public func setup(config: VideoLoaderConfig) {
        cleanCache()
//        config.rtcEngine?.setParameters("{\"rtc.log_filter\":65535}")
        self.config = config
    }
    
    public func preloadAnchor(preloadAnchorList: [AnchorInfo], uid: UInt) {
        guard let rtcEngine = self.config?.rtcEngine else {return}
        debugLoaderPrint("preloadAnchor: \(preloadAnchorList.map({$0.channelName}))")
        preloadAnchorList.forEach { anchorInfo in
            if anchorInfo.token.count == 0 {
                warningLoaderPrint("preloadChannel[\(anchorInfo.channelName)] fail! token is empty ")
                return
            }
            rtcEngine.preloadChannel(byToken: anchorInfo.token, channelId: anchorInfo.channelName, uid: uid)
        }
    }
    
    public func switchAnchorState(newState: AnchorState,
                                  localUid: UInt,
                                  anchorInfo: AnchorInfo,
                                  tagId: String?) {
        if localUid == 0 {
            warningLoaderPrint("\(anchorInfo.channelName) localUid invalidate")
            return
        }
        
        if localUid == anchorInfo.uid {
            warningLoaderPrint("\(anchorInfo.channelName) localUid == anchorInfo.uid")
            return
        }
        
        var map: [String: AnchorState]? = exConnectionDeps[anchorInfo.channelName]
        if map == nil {
            map = [:]
        }
        let tagId = tagId ?? ""
        if newState == .idle {
            map?[tagId] = nil
        } else {
            map?[tagId] = newState
        }
        
        guard let map = map else {
            errorLoaderPrint("switchAnchorState fatal, map init fail")
            return
        }
        let oldState = getAnchorState(anchorInfo: anchorInfo)
        
        exConnectionDeps[anchorInfo.channelName] = map
        
        let realState = getAnchorState(anchorInfo: anchorInfo)
        
        if realState == .idle {
            _leaveChannelEx(channelId: anchorInfo.channelName)
        } else {
            _joinChannelEx(channelId: anchorInfo.channelName,
                           ownerId: anchorInfo.uid,
                           localUid: localUid,
                           token: anchorInfo.token,
                           options: AgoraRtcChannelMediaOptions())
        }
        
        var isMuteAllRemoteAudioStreamsEx: Bool? = nil
        let mediaOptions = AgoraRtcChannelMediaOptions()
        if realState == .joinedWithAudioVideo {
            mediaOptions.autoSubscribeAudio = true
            mediaOptions.autoSubscribeVideo = true
            
            isMuteAllRemoteAudioStreamsEx = false
            
        } else if realState == .joinedWithVideo {
            mediaOptions.autoSubscribeAudio = true
            mediaOptions.autoSubscribeVideo = true
            
            isMuteAllRemoteAudioStreamsEx = true
            if let engine = config?.rtcEngine,
               let connection = exConnectionMap[anchorInfo.channelName]  {
                DispatchQueue.main.async {
                    engine.muteAllRemoteAudioStreamsEx(true, connection: connection)
                }
                
            } else {
                warningLoaderPrint("[\(anchorInfo.channelName)] muteAllRemoteAudioStreamsEx(true) fail")
            }
        } else {
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.autoSubscribeVideo = false
        }

        debugLoaderPrint("tagId[\(tagId)] switchAnchorState[\(anchorInfo.channelName)] want:\(newState.rawValue) real: \(realState.rawValue)")
//        debugLoaderPrint("tagId[\(tagId)] switchAnchorState[\(anchorInfo.channelName)] autoSubscribeAudio:\(mediaOptions.autoSubscribeAudio) autoSubscribeVideo: \(mediaOptions.autoSubscribeVideo)")
        _updateChannelEx(channelId:anchorInfo.channelName, options: mediaOptions)
        
        if let isMuteAllRemoteAudioStreamsEx = isMuteAllRemoteAudioStreamsEx {
            if let engine = config?.rtcEngine,
               let connection = exConnectionMap[anchorInfo.channelName]  {
                engine.muteAllRemoteAudioStreamsEx(isMuteAllRemoteAudioStreamsEx, connection: connection)
            } else {
                warningLoaderPrint("[\(anchorInfo.channelName)] muteAllRemoteAudioStreamsEx(\(isMuteAllRemoteAudioStreamsEx)) fail")
            }
        }
        
        if (realState == .joinedWithAudioVideo || realState == .joinedWithVideo), (oldState == .idle || oldState == .prejoined) {
            let profiler = _getProfiler(anchorId: anchorInfo.channelName)
            profiler.startTime = Int64(NSDate().timeIntervalSince1970 * 1000)
        }
        if realState != oldState {
            let api = apiProxy as IVideoLoaderApiListener
            api.onStateDidChange?(newState: realState, oldState: oldState, channelName: anchorInfo.channelName)
        }
    }
    
    public func getAnchorState(anchorInfo: AnchorInfo) -> AnchorState {
        var realState: AnchorState = .idle
        
        guard let map: [String: AnchorState] = exConnectionDeps[anchorInfo.channelName] else {
            return realState
        }
        //calc real type
        map.forEach { (key: String, value: AnchorState) in
            if realState.rawValue < value.rawValue {
                realState = value
            }
        }
        
        return realState
    }
    
    public func getConnectionMap() -> [String: AgoraRtcConnection] {
        return exConnectionMap
    }
    
    public func renderVideo(anchorInfo: AnchorInfo, container: VideoCanvasContainer) {
        guard let engine = config?.rtcEngine,
              let connection = exConnectionMap[anchorInfo.channelName] else {
            errorLoaderPrint("renderVideo fail: connection is empty")
            return
        }
        
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = container.uid
        videoCanvas.view = container.container
        videoCanvas.renderMode = .hidden
        videoCanvas.setupMode = container.setupMode
        let ret = engine.setupRemoteVideoEx(videoCanvas, connection: connection)
        debugLoaderPrint("renderVideo[\(connection.channelId)] ret = \(ret), uid:\(anchorInfo.uid)")
        //查找缓存这个view是不是被其他connection用了，如果有则remove
        let ptrString = String(format: "%p", videoCanvas.view ?? 0)
        if container.setupMode == .add {
            if anchorInfo.channelName != renderViewMap[ptrString]?.channelName {
                defer {
                    renderViewMap[ptrString] = anchorInfo
                }
                guard let info = renderViewMap[ptrString], let connection = exConnectionMap[info.channelName] else {return}
                let videoCanvas = AgoraRtcVideoCanvas()
                videoCanvas.uid = info.uid
                videoCanvas.view = container.container
                videoCanvas.renderMode = .hidden
                videoCanvas.setupMode = .remove
                let _ = engine.setupRemoteVideoEx(videoCanvas, connection: connection)
            }
        } else if container.setupMode == .remove {
            renderViewMap[ptrString] = nil
        }
    }
    
    public func cleanCache() {
        guard let rtcEngine = self.config?.rtcEngine else {return}
//        rtcEngine.setAudioSessionOperationRestriction(.all)
        exConnectionMap.forEach { anchorId, connection in
            rtcEngine.leaveChannelEx(connection)
        }
        exConnectionMap.removeAll()
        exConnectionDeps.removeAll()
        profilerMap.removeAll()
        renderViewMap.removeAll()
        
        debugLoaderPrint("cleanCache")
    }
    
    public func addListener(listener: IVideoLoaderApiListener) {
        apiProxy.addListener(listener)
    }
    
    public func removeListener(listener: IVideoLoaderApiListener) {
        apiProxy.removeListener(listener)
    }
    
    public func addRTCListener(anchorId: String, listener: AgoraRtcEngineDelegate) {
        let rtcProxy = _getProxy(anchorId: anchorId)
        debugLoaderPrint("[VideoLoaderProfiler] addRTCListener: \(anchorId)")
        rtcProxy.addListener(listener)
    }
    
    public func removeRTCListener(anchorId: String, listener: AgoraRtcEngineDelegate) {
        let rtcProxy = _getProxy(anchorId: anchorId)
        debugLoaderPrint("[VideoLoaderProfiler] removeRTCListener: \(anchorId)")
        rtcProxy.removeListener(listener)
    }
    
    public func getRTCListener(anchorId: String) -> AgoraRtcEngineDelegate? {
        return rtcProxys[anchorId]
    }
}

extension VideoLoaderApiImpl {

    func startMediaRenderingTracing(anchorId: String) {
        guard let engine = config?.rtcEngine, let connection = exConnectionMap[anchorId] else {return}
        engine.startMediaRenderingTracingEx(connection)
    }
    
    func getUsedAnchorIds(tagId: String) -> [String] {
        var anchorIds: [String] = []
        //find anchor id list
        exConnectionDeps.forEach { key, value in
            if value[tagId] == nil { return }
            anchorIds.append(key)
        }
        return anchorIds
    }
}
