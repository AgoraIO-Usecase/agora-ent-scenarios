//
//  ShowTo1v1ServiceImp.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import RTMSyncManager
import YYModel
import AgoraRtmKit
import AgoraCommon

/// 房间内用户列表
private let kSceneId = "scene_Livetoprivate_6.0.0"
class ShowTo1v1ServiceImp: NSObject {
    private var user: ShowTo1v1UserInfo
    private var rtmClient: AgoraRtmClientKit
    private var roomList: [ShowTo1v1RoomInfo] = []
    private weak var listener: ShowTo1v1ServiceListenerProtocol?
    
    private var userList: [ShowTo1v1UserInfo] = [] {
        didSet {
            self.listener?.onUserListDidChanged(userList: userList)
        }
    }
    
    private lazy var roomManager = AUIRoomManagerImpl(sceneId: kSceneId)
    private lazy var syncManager: AUISyncManager = {
        let config = AUICommonConfig()
        config.appId = AppContext.shared.appId
        let owner = AUIUserThumbnailInfo()
        owner.userId = user.uid
        owner.userName = user.userName
        owner.userAvatar = user.avatar
        config.owner = owner
        config.host = AppContext.shared.roomManagerUrl
        let manager = AUISyncManager(rtmClient: rtmClient, commonConfig: config, logConfig: nil)
        
        return manager
    }()
    
    private lazy var roomService: AUIRoomService = {
        let poliocy = RoomExpirationPolicy()
        poliocy.expirationTime = 20 * 60 * 1000
        let service = AUIRoomService(expirationPolicy: poliocy, roomManager: roomManager, syncmanager: syncManager)
        
        return service
    }()
    
    required init(user: ShowTo1v1UserInfo, rtmClient: AgoraRtmClientKit) {
        self.user = user
        self.rtmClient = rtmClient
        AUIRoomContext.shared.displayLogClosure = { msg in
            ShowTo1v1Logger.info(msg, context: "RTMSyncManager")
        }
        super.init()
        let _ = self.syncManager
    }
}

extension ShowTo1v1ServiceImp: ShowTo1v1ServiceProtocol {
    func getRoomList(completion: @escaping (NSError?, [ShowTo1v1RoomInfo]?) -> Void) {
        roomService.getRoomList(lastCreateTime: 0, pageSize: 50) {[weak self] info in
            return info.owner?.userId == self?.user.uid
        } completion: {[weak self] err, ts, list in
            guard let self = self else {return}
            var showRoomList: [ShowTo1v1RoomInfo] = []
            list?.forEach({ info in
                showRoomList.append(ShowTo1v1RoomInfo(roomInfo: info))
            })
            self.roomList = showRoomList
            completion(err, showRoomList)
        }
    }
    
    func createRoom(roomName: String, completion: @escaping (ShowTo1v1RoomInfo?, Error?) -> Void) {
        ShowTo1v1Logger.info("createRoom start")
        let roomInfo = ShowTo1v1RoomInfo()
        roomInfo.uid = user.uid
        roomInfo.userName = user.userName
        roomInfo.avatar = user.avatar
        roomInfo.roomName = roomName
        roomInfo.roomId = "\(arc4random_uniform(899999) + 100000)"
        
        let aui_roomInfo = roomInfo.convertAUIRoomInfo()
        
        let scene = self.syncManager.createScene(channelName: roomInfo.roomId)
        scene.userService.bindRespDelegate(delegate: self)
        scene.bindRespDelegate(delegate: self)
        
        roomService.createRoom(room: aui_roomInfo) { err, info in
            if let err = err {
                ShowTo1v1Logger.error("enter scene fail: \(err.localizedDescription)")
                completion(nil, err)
                return
            }
            completion(ShowTo1v1RoomInfo(roomInfo: info ?? aui_roomInfo), nil)
        }
    }
    
    func joinRoom(roomInfo:ShowTo1v1RoomInfo, completion: @escaping (Error?) -> Void) {
        let scene = self.syncManager.createScene(channelName: roomInfo.roomId)
        scene.bindRespDelegate(delegate: self)
        scene.userService.bindRespDelegate(delegate: self)
        
        let aui_roomInfo = roomInfo.convertAUIRoomInfo()
        roomService.enterRoom(roomInfo: aui_roomInfo) { err in
            if let err = err {
                ShowTo1v1Logger.error("enter scene fail: \(err.localizedDescription)")
                scene.leave()
                completion(err)
                return
            }
            completion(nil)
        }
    }
    
    func leaveRoom(roomInfo: ShowTo1v1RoomInfo, completion: @escaping (Error?) -> Void) {
        _leaveRoom(roomId: roomInfo.roomId, isRoomOwner: roomInfo.uid == user.uid)
        completion(nil)
    }
    
    func subscribeListener(listener: ShowTo1v1ServiceListenerProtocol?) {
        self.listener = listener
    }
}

//MARK: room
extension ShowTo1v1ServiceImp: AUISceneRespDelegate {
    private func _leaveRoom(roomId: String, isRoomOwner: Bool) {
        guard let scene = self.syncManager.getScene(channelName: roomId) else {return}
        scene.unbindRespDelegate(delegate: self)
        scene.userService.unbindRespDelegate(delegate: self)
        roomService.leaveRoom(roomId: roomId)
    }
    
    func onSceneExpire(channelName: String) {
        onSceneDestroy(channelName: channelName)
    }
    
    func onSceneDestroy(channelName: String) {
        ShowTo1v1Logger.info("onSceneDestroy: \(channelName)")
        guard let model = self.roomList.filter({ $0.roomId == channelName }).first else {
            return
        }
        
        _leaveRoom(roomId: channelName, isRoomOwner: true)
        self.listener?.onRoomDidDestroy(roomInfo: model)
    }
}

//MARK: user
extension ShowTo1v1ServiceImp: AUIUserRespDelegate {
    func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        let list = userList.flatMap({ ShowTo1v1UserInfo(userInfo: $0) }) ?? []
        self.userList = list
    }
    
    func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        self.userList.append(ShowTo1v1UserInfo(userInfo: userInfo))
    }
    
    func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        let userList = self.userList
        self.userList = userList.filter({ $0.uid != userInfo.userId})
    }
    
    func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        if let idx = self.userList.firstIndex(where: { $0.uid == userInfo.userId}) {
            self.userList[idx] = ShowTo1v1UserInfo(userInfo: userInfo)
            return
        }
        
        self.userList.append(ShowTo1v1UserInfo(userInfo: userInfo))
    }
    
    func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    func onUserBeKicked(roomId: String, userId: String) {
        
    }
    
}
