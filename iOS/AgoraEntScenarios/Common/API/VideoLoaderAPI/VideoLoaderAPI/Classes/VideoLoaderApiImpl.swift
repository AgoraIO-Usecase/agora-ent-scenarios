//
//  VideoLoaderApiImpl.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import AgoraRtcKit

private func apiPrint(_ message: String) {
    print("[VideoLoaderApi]\(message)")
}

private func apiWarningPrint(_ message: String) {
    print("[VideoLoaderApi][Warning]\(message)")
}

private func apiErrorPrint(_ message: String) {
    print("[VideoLoaderApi][Error]\(message)")
}

class VideoLoaderApiImpl: NSObject {
    private var config: VideoLoaderConfig?
    
    private let apiProxy = VideoLoaderApiProxy()
    private let rtcProxy = VideoLoaderAgoraExProxy()
    //[ex channelId: connection]
    private var exConnectionMap: [String: AgoraRtcConnection] = [:]
    //[ex channelId: [room id: status]]
    private var exConnectionDeps: [String: [String: RoomStatus]] = [:]
    
    deinit {
        rtcProxy.removeListener(self)
    }
    
    override init() {
        super.init()
//        rtcProxy.addListener(self)
    }
}

//MARK: private
extension VideoLoaderApiImpl {
    func _updateChannelEx(channelId: String, options: AgoraRtcChannelMediaOptions) {
        guard let engine = config?.rtcEngine,
              let connection = exConnectionMap[channelId] else {
            apiErrorPrint("updateChannelEx fail: connection is empty")
            return
        }
        engine.updateChannelEx(with: options, connection: connection)
    }
    
    private func _joinChannelEx(channelId: String,
                                ownerId: UInt,
                                token: String,
                                options:AgoraRtcChannelMediaOptions) {
        guard let engine = config?.rtcEngine, let uid = config?.userId else {
            assert(true, "rtc engine not initlized")
            return
        }
        
        if let connection = exConnectionMap[channelId] {
            return
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
        connection.localUid = uid
        
        //TODO: retain cycle in joinChannelEx to use rtcProxy
        let date = Date()
//            showLogger.info("try to join room[\(connection.channelId)] ex uid: \(connection.localUid)", context: kShowLogBaseContext)
        let ret =
        engine.joinChannelEx(byToken: token,
                               connection: connection,
                               delegate: rtcProxy,
                               mediaOptions: mediaOptions) {[weak self] channelName, uid, elapsed in
            let cost = Int(-date.timeIntervalSinceNow * 1000)
//                showLogger.info("join room[\(channelName)] ex success uid: \(uid) cost \(cost) ms", context: kShowLogBaseContext)
        }
        engine.updateChannelEx(with: mediaOptions, connection: connection)
        exConnectionMap[channelId] = connection
            
            if ret == 0 {
                apiPrint("join room ex: channelId: \(channelId) ownerId: \(ownerId)")
            }else{
                apiErrorPrint("join room ex fail: channelId: \(channelId) ownerId: \(ownerId) token = \(token), \(ret)")
            }
    }
    
    func _leaveChannelEx(channelId: String) {
        guard let engine = config?.rtcEngine,
              let connection = exConnectionMap[channelId] else { return }
        let depMap = exConnectionDeps[channelId]
        if depMap?.count ?? 0 > 0 {
            apiPrint("leaveChannelEx break, depcount: \(depMap?.count ?? 0), channelId: \(channelId)")
            return
        }
        apiPrint("leaveChannelEx channelId: \(channelId)")
        engine.leaveChannelEx(connection)
        exConnectionMap[channelId] = nil
    }
}

//MARK: VideoLoaderApiProtocol
extension VideoLoaderApiImpl: IVideoLoaderApi {
    func setup(config: VideoLoaderConfig) {
        self.config = config
    }
    
    func preloadRoom(preloadRoomList: [RoomInfo]) {
        guard let rtcEngine = self.config?.rtcEngine else {return}
        preloadRoomList.forEach { roomInfo in
            rtcEngine.preloadChannel(byToken: roomInfo.token, channelId: roomInfo.channelName, uid: roomInfo.uid)
        }
    }
    
    func switchRoomState(newState: RoomStatus, roomInfo: RoomInfo, tagId: String?) {
        var map: [String: RoomStatus]? = exConnectionDeps[roomInfo.channelName]
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
            apiErrorPrint("updateLoadingType fatal, map init fail")
            return
        }
        let oldState = getRoomState(roomInfo: roomInfo)
        
        exConnectionDeps[roomInfo.channelName] = map
        
        let realState = getRoomState(roomInfo: roomInfo)
        
        if realState == .idle {
            _leaveChannelEx(channelId: roomInfo.channelName)
        } else {
            _joinChannelEx(channelId: roomInfo.channelName, ownerId: roomInfo.uid, token: roomInfo.token, options: AgoraRtcChannelMediaOptions())
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        if realState == .joined {
            mediaOptions.autoSubscribeAudio = true
            mediaOptions.autoSubscribeVideo = true
        } else {
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.autoSubscribeVideo = false
        }

        apiPrint("tagId[tagId] updateLoadingType \(roomInfo.channelName) want:\(newState.rawValue) real: \(realState.rawValue)")
        _updateChannelEx(channelId:roomInfo.channelName, options: mediaOptions)
        if realState != oldState {
            let api = apiProxy as IVideoLoaderApiListener
            api.onStateDidChange?(newState: realState, oldState: oldState, channelName: roomInfo.channelName)
        }
    }
    
    func getRoomState(roomInfo: RoomInfo) -> RoomStatus {
        var realState: RoomStatus = .idle
        
        guard let map: [String: RoomStatus] = exConnectionDeps[roomInfo.channelName] else {
            return realState
        }
        //calc real type
        map.forEach { (key: String, value: RoomStatus) in
            if realState.rawValue < value.rawValue {
                realState = value
            }
        }
        
        return realState
    }
    
    func renderVideo(roomInfo: RoomInfo, container: VideoCanvasContainer) {
        guard let engine = config?.rtcEngine,
              let localUid = config?.userId,
              let connection = exConnectionMap[roomInfo.channelName] else {
            apiErrorPrint("_joinChannelEx fail: connection is empty")
            return
        }
                
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = roomInfo.uid
        videoCanvas.view = container.container
        videoCanvas.renderMode = .hidden
        let ret = engine.setupRemoteVideoEx(videoCanvas, connection: connection)
                
        apiPrint("setupRemoteVideoEx ret = \(ret), uid:\(roomInfo.uid) localuid: \(localUid) channelId: \(roomInfo.channelName)")
    }
    
    func cleanCache() {
        guard let rtcEngine = self.config?.rtcEngine else {return}
        exConnectionMap.forEach { roomId, connection in
            rtcEngine.leaveChannelEx(connection)
        }
        exConnectionMap.removeAll()
        exConnectionDeps.removeAll()
    }
    
    func addListener(listener: IVideoLoaderApiListener) {
        apiProxy.addListener(listener)
    }
    
    func removeListener(listener: IVideoLoaderApiListener) {
        apiProxy.addListener(listener)
    }
    
    func addRTCListener(listener: AgoraRtcEngineDelegate) {
        rtcProxy.addListener(listener)
    }
    
    func removeRTCListener(listener: AgoraRtcEngineDelegate) {
        rtcProxy.removeListener(listener)
    }
}
