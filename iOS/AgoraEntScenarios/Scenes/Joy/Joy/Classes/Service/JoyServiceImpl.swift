//
//  JoyServiceImpl.swift
//  Joy
//
//  Created by CP on 2024/3/25.
//

import Foundation
import RTMSyncManager
import YYModel
import AgoraRtmKit

private let kSceneId = "scene_joy_4.10.1"
private let SYNC_SCENE_ROOM_STARTGAME_COLLECTION = "startGameCollection"
class JoyServiceImpl: NSObject {
    private var appId: String
    private var user: JoyUserInfo
    private var rtmClient: AgoraRtmClientKit?
    private var roomList: [JoyRoomInfo] = []
    private var sceneBinded: Bool = false
    private var collectionBinded: Bool = false
    private var host: String
    private var appCertificate: String
    private weak var listener: JoyServiceListenerProtocol?
    
    private var userList: [JoyUserInfo] = [] {
        didSet {
            self.listener?.onUserListDidChanged(userList: userList)
        }
    }
    
    private var isConnected: Bool = false
    
    private lazy var roomManager = AUIRoomManagerImpl(sceneId: kSceneId)
    private lazy var syncManager: AUISyncManager = {
        let config = AUICommonConfig()
        config.appId = appId
        let owner = AUIUserThumbnailInfo()
        owner.userId = String(user.userId) ?? ""
        owner.userName = user.userName
        owner.userAvatar = user.avatar
        config.owner = owner
        config.host = self.host
        let manager = AUISyncManager(rtmClient: rtmClient, commonConfig: config)
        
        return manager
    }()
    
    required init(appId: String, host: String, appCertificate: String, user: JoyUserInfo, rtmClient: AgoraRtmClientKit?) {
        self.appId = appId
        self.user = user
        self.rtmClient = rtmClient
        self.host = host
        self.appCertificate = appCertificate
        AUIRoomContext.shared.displayLogClosure = { msg in
            joyPrint(msg, context: "RTMSyncManager")
        }
        super.init()
        syncManager.rtmManager.subscribeError(channelName: "", delegate: self)
        login { err in
            self.isConnected = err == nil ? true : false
        }
    }
}

extension JoyServiceImpl: JoyServiceProtocol {
    func getRoomList(completion: @escaping ([JoyRoomInfo]) -> Void) {
        let fetchRoomList: () -> Void = {[weak self] in
            self?.roomManager.getRoomInfoList(lastCreateTime: 0, pageSize: 50) {[weak self] err, list in
                let joyRoomList = list?.compactMap{ self?.convertAUIRoomInfo2JoyRoomInfo(with:$0) } ?? []
                self?.roomList = joyRoomList
                completion(joyRoomList)
            }
        }
        
        if isConnected == false {
            login {[weak self] err in
                if err == nil {
                    fetchRoomList()
                } else {
                    completion([])
                }
            }
        } else {
            fetchRoomList()
        }
    }
    
    func createRoom(roomName: String, completion: @escaping (JoyRoomInfo?, Error?) -> Void) { joyPrint("createRoom start")

        let roomInfo = AUIRoomInfo()
        roomInfo.roomName = roomName
        roomInfo.roomId = "\(arc4random_uniform(899999) + 100000)"
        roomInfo.customPayload = ["roomUserCount": 1]

        let owner = AUIUserThumbnailInfo()
        owner.userId = String(self.user.userId ?? 0)
        owner.userName = self.user.userName
        owner.userAvatar = self.user.avatar
        roomInfo.owner = owner

        func create(roomInfo: AUIRoomInfo) {
            let scene = getCurrentScene(with: roomInfo.roomId)
            roomManager.createRoom(room: roomInfo) { [weak self] err, info in
                guard let self = self else { return }
                
                if let err = err {
                    joyPrint("create room fail: \(err.localizedDescription)")
                    completion(nil, err)
                    return
                }
                
                scene.create(payload: [:]) { [weak self] err in
                    if let err = err {
                        joyPrint("create scene fail: \(err.localizedDescription)")
                        completion(nil, err)
                        return
                    }
                    
                    scene.enter { [weak self] payload, err in
                        if let err = err {
                            joyPrint("enter scene fail: \(err.localizedDescription)")
                            completion(nil, err)
                            return
                        }
                        self?.syncManager.rtmManager.subscribeMessage(channelName: roomInfo.roomId, delegate: self!)
                        
                        completion(self?.convertAUIRoomInfo2JoyRoomInfo(with: info ?? roomInfo), nil)
                    }
                }
            }
        }

        if isConnected == false {
            login { [weak self] err in
                if err == nil {
                    create(roomInfo: roomInfo)
                } else {
                    completion(nil, err)
                }
            }
        } else {
            create(roomInfo: roomInfo)
        }
    }
    
    func joinRoom(roomInfo: JoyRoomInfo, completion: @escaping (Error?) -> Void) {
        let enterScene: () -> Void = {[weak self] in
            let scene = self?.getCurrentScene(with: roomInfo.roomId)
            scene?.enter {[weak self] payload, err in
                if let err = err {
                    joyPrint("enter scene fail: \(err.localizedDescription)")
                    completion(err)
                    return
                }
                self?.syncManager.rtmManager.subscribeMessage(channelName: roomInfo.roomId, delegate: self!)
                completion(nil)
            }
        }
        
        if isConnected == false {
            login {[weak self] err in
                if err == nil {
                    enterScene()
                } else {
                    completion(err)
                }
            }
        } else {
            enterScene()
        }
    }
    
    func updateRoom(roomInfo: JoyRoomInfo, completion: @escaping (NSError?) -> Void) {
        let updateRoomInfo: () -> Void = {[weak self] in
            guard let self = self else {return}
            let channelName = roomInfo.roomId
            
            let aui_roominfo = convertJoyRoomInfo2AUIRoomInfo(with: roomInfo)
            roomManager.updateRoom(room: aui_roominfo) {[weak self] err, info in
                if let err = err {
                    joyPrint("enter scene fail: \(err.localizedDescription)")
                    completion(err)
                    return
                }
                completion(nil)
            }
        }
        
        if isConnected == false {
            login {[weak self] err in
                if err == nil {
                    updateRoomInfo()
                } else {
                    completion(err as NSError?)
                }
            }
        } else {
            updateRoomInfo()
        }
    }

    
    func getStartGame(roomId: String, completion: @escaping (NSError?, JoyStartGameInfo?) -> Void)  {
        joyPrint("imp start game get...")
        
        let fetchStartGameInfo: () -> Void = {[weak self] in
            guard let self = self else {return}
            let mapCollection: AUIMapCollection? = getCurrentCollection(with: roomId)
            mapCollection?.getMetaData(callback: { err, data in
                var info = JoyStartGameInfo()
                if let data = data as? [String : Any] {
                    if let objectId = data["objectId"] as? String {
                        info.objectId = objectId
                    }
                    
                    if let gameId = data["gameId"] as? String {
                        info.gameId = gameId
                    }
                    
                    if let taskId = data["taskId"] as? String {
                        info.taskId = taskId
                    }
                    
                    if let assistantUid = data["assistantUid"] as? String {
                        info.assistantUid = UInt(assistantUid) ?? 0
                    }
                    
                    if let gameName = data["gameName"] as? String {
                        info.gameName = gameName
                    }
                }
                completion(err, info)
            })
        }
        
        if isConnected == false {
            login { err in
                if err == nil {
                    fetchStartGameInfo()
                } else {
                    completion(err as? NSError, nil)
                }
            }
        } else {
            fetchStartGameInfo()
        }
    }
  
    func updateStartGame(roomId: String,
                         gameInfo: JoyStartGameInfo,
                         completion: @escaping (NSError?) -> Void) {
        let updateMetaData: () -> Void = {[weak self] in
            guard let self = self else {return}
            let mapCollection: AUIMapCollection? = getCurrentCollection(with: roomId)

            var dict: [String : Any] = [:]
            dict["gameId"] = gameInfo.gameId
            dict["taskId"] = gameInfo.taskId
            dict["assistantUid"] = gameInfo.assistantUid
            dict["gameName"] = gameInfo.gameName
            dict["objectId"] = gameInfo.objectId
            mapCollection?.updateMetaData(valueCmd: nil, value: dict, filter: nil, callback: { err in
                completion(err)
            })
        }
        
        if isConnected == false {
            login {[weak self] err in
                if err == nil {
                    updateMetaData()
                } else {
                    completion(err as? NSError)
                }
            }
        } else {
            updateMetaData()
        }
    }
    
    func leaveRoom(roomInfo: JoyRoomInfo, completion: @escaping (Error?) -> Void) {
        let performLeaveRoom: () -> Void = {[weak self] in
            guard let self = self else {return}
            _leaveRoom(roomId: roomInfo.roomId, isRoomOwner: roomInfo.ownerId == user.userId)
            syncManager.rtmManager.unsubscribeMessage(channelName: roomInfo.roomId, delegate: self)
            completion(nil)
        }
        
        if isConnected == false {
            login {[weak self] err in
                if err == nil {
                    performLeaveRoom()
                } else {
                   completion(err)
                }
            }
        } else {
            performLeaveRoom()
        }
    }

    
    func sendChatMessage(roomId: String,
                         message: String,
                         completion: @escaping (NSError?) -> Void) {
        let publishMessage: () -> Void = {[weak self] in
            let model = JoyMessage()
            model.userId = String(self?.user.userId ?? 0)
            model.message = message
            model.userName = self?.user.userName
            model.createAt = Int64(Date().timeIntervalSince1970 * 1000)
            let encoder = JSONEncoder()
            encoder.outputFormatting = .prettyPrinted // 可选，格式化输出
            do {
                let jsonData = try encoder.encode(model)
                if let jsonString = String(data: jsonData, encoding: .utf8) {
                    self?.syncManager.rtmManager.publish(channelName: roomId, message: jsonString) { err in
                        if err == nil {
                            self?.listener?.onMessageDidAdded(message: model)
                        }
                        completion(err)
                    }
                }
            } catch {
                print("转换为 JSON 字符串时出错：\(error)")
            }
            
        }
        
        if isConnected == false {
            login {[weak self] err in
                if err == nil {
                    publishMessage()
                } else {
                    completion(err as? NSError)
                }
            }
        } else {
            publishMessage()
        }
    }
    
    func subscribeListener(listener: JoyServiceListenerProtocol?) {
        self.listener = listener
    }
}

extension JoyServiceImpl:AUIRtmMessageProxyDelegate {
    func onMessageReceive(publisher: String, message: String) {
        do {
            if let jsonData = message.data(using: .utf8) {
                let model = try JSONDecoder().decode(JoyMessage.self, from: jsonData)
                self.listener?.onMessageDidAdded(message: model)
            }
        } catch {
            print("Error decoding JSON: \(error)")
        }
    }
}

extension JoyServiceImpl: AUISceneRespDelegate {
    private func _leaveRoom(roomId: String, isRoomOwner: Bool) {
        joyPrint("_leaveRoom: \(roomId) isRoomOwner:\(isRoomOwner)")
        let scene = self.syncManager.getScene(channelName: roomId)
        scene.unbindRespDelegate(delegate: self)
        scene.userService.unbindRespDelegate(delegate: self)
        if isRoomOwner {
            scene.delete()
            roomManager.destroyRoom(roomId: roomId) { _ in
            }
        } else {
            scene.leave()
        }
    }
    
    func onSceneDestroy(roomId: String) {
        joyPrint("onSceneDestroy: \(roomId)")
        guard let model = self.roomList.filter({ $0.roomId == roomId }).first else {
            return
        }
        
        _leaveRoom(roomId: roomId, isRoomOwner: true)
        self.listener?.onRoomDidDestroy(roomInfo: model)
    }
    
    func onTokenPrivilegeWillExpire(channelName: String?) {
        NetworkManager.shared.generateToken(appId: appId, appCertificate: appCertificate, channelName: "", uid: String(user.userId), tokenType: .token007, type: .rtm) { token in
            if let token = token {
                self.syncManager.rtmManager.renew(token: token) { err in
                    joyPrint("renew token：err \(err)")
                    if err == nil {
                        
                    }
                }
            }
        }
    }
}

extension JoyServiceImpl: AUIUserRespDelegate {
    func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        let list = userList.flatMap({ convertAUIUserInfo2JoyUserInfo(with: $0)}) ?? []
        self.userList = list
    }
    
    func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        print("user: enter\(userInfo.userName)")
        self.userList.append(convertAUIUserInfo2JoyUserInfo(with: userInfo))
        self.listener?.onUserListDidChanged(userList: self.userList)
    }
    
    func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        print("user: leave\(userInfo.userName)")
        let userList = self.userList
        self.userList = userList.filter({ $0.userId != UInt(userInfo.userId)})
        self.listener?.onUserListDidChanged(userList: self.userList)
    }
    
    func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        print("user: update\(userInfo.userName)")
        if let idx = self.userList.firstIndex(where: { $0.userId == UInt(userInfo.userId)}) {
            self.userList[idx] = convertAUIUserInfo2JoyUserInfo(with: userInfo)
            return
        }
        
        self.userList.append(convertAUIUserInfo2JoyUserInfo(with: userInfo))
        self.listener?.onUserListDidChanged(userList: self.userList)
    }
  
    func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    func onUserBeKicked(roomId: String, userId: String) {
        
    }
}

extension JoyServiceImpl{
    private func convertAUIRoomInfo2JoyRoomInfo(with model: AUIRoomInfo) -> JoyRoomInfo {
        let roomDict: [String: Any] = model.customPayload
        let joyRoom = JoyRoomInfo()
        
        joyRoom.roomId = model.roomId
        joyRoom.roomName = model.roomName
        joyRoom.ownerId = UInt(model.owner?.userId ?? "") ?? 0
        joyRoom.ownerAvatar = model.owner?.userAvatar
        joyRoom.ownerName = model.owner?.userName
        
        if let roomUserCount = roomDict["roomUserCount"] as? Int {
            joyRoom.roomUserCount = roomUserCount
        }
        if let thumbnailId = roomDict["thumbnailId"] as? String {
            joyRoom.thumbnailId = thumbnailId
        }
        if let badgeTitle = roomDict["badgeTitle"] as? String {
            joyRoom.badgeTitle = badgeTitle
        }
        if let createdAt = roomDict["createdAt"] as? Int64 {
            joyRoom.createdAt = createdAt
        }
        if let objectId = roomDict["objectId"] as? String {
            joyRoom.objectId = objectId
        }
        
        return joyRoom
    }
    
    private func convertJoyRoomInfo2AUIRoomInfo(with model: JoyRoomInfo) -> AUIRoomInfo {
        
        let roomInfo = AUIRoomInfo()
        var customPayload: [String: Any] = [:]
        customPayload["roomUserCount"] = model.roomUserCount
        customPayload["thumbnailId"] = model.thumbnailId
        customPayload["badgeTitle"] = model.badgeTitle
        customPayload["createdAt"] = model.createdAt
        customPayload["objectId"] = model.objectId
        
        roomInfo.customPayload = customPayload
        roomInfo.roomId = model.roomId
        
        let owner = AUIUserThumbnailInfo()
        owner.userId = String(user.userId)
        owner.userName = user.userName
        owner.userAvatar = user.avatar
        roomInfo.roomName = model.roomName ?? ""
        roomInfo.owner = owner
        
        return roomInfo
    }
    
    private func convertJoyUserInfo2AUIUserInfo(with user: JoyUserInfo) -> AUIUserInfo {
        var userInfo = AUIUserInfo()
        userInfo.userId = String(user.userId)
        userInfo.userName = user.userName
        userInfo.userAvatar = user.avatar
        return userInfo
    }
    
    private func convertAUIUserInfo2JoyUserInfo(with user: AUIUserInfo) -> JoyUserInfo {
        var userInfo = JoyUserInfo()
        userInfo.userId = UInt(user.userId) ?? 0
        userInfo.userName = user.userName
        userInfo.avatar = user.userAvatar
        return userInfo
    }
    
    private func getCurrentScene(with channelName: String) -> AUIScene {
        let scene = self.syncManager.getScene(channelName: channelName)
        if !sceneBinded {
            scene.userService.bindRespDelegate(delegate: self)
            scene.bindRespDelegate(delegate: self)
            sceneBinded = true
        }
        return scene
    }

    private func convertDict2JoyStartGameInfo(with dict: [String : Any]) -> JoyStartGameInfo {
        let gameInfo = JoyStartGameInfo()
        if let gameId = dict["gameId"] as? String {
            gameInfo.gameId = gameId
        }
        if let taskId = dict["taskId"] as? String {
            gameInfo.taskId = taskId
        }
        if let assistantUid = dict["assistantUid"] as? UInt {
            gameInfo.assistantUid = assistantUid
        }
        if let gameName = dict["gameName"] as? String {
            gameInfo.gameName = gameName
        }
        if let objectId = dict["objectId"] as? String {
            gameInfo.objectId = objectId
        }
        return gameInfo
    }
    
    private func getCurrentCollection(with roomId: String) -> AUIMapCollection? {
        let collection: AUIMapCollection? = getCurrentScene(with: roomId).getCollection(key: SYNC_SCENE_ROOM_STARTGAME_COLLECTION)
     //   if !collectionBinded {
            collection?.subscribeAttributesDidChanged(callback: {[weak self] str1, str2, model in
                if let dict = model.getMap(), let self = self {
                    self.listener?.onStartGameInfoDidChanged(startGameInfo: self.convertDict2JoyStartGameInfo(with: dict))
                }
            })
//            collectionBinded = true
//        }
        return collection
    }
}

extension JoyServiceImpl: AUIRtmErrorProxyDelegate {
    
    private func login(completion:(@escaping (Error?)-> Void)) {
        NetworkManager.shared.generateToken(appId: appId, appCertificate: appCertificate, channelName: "", uid: String(user.userId), tokenType: .token007, type: .rtm) { token in
            if let token = token {
                self.syncManager.rtmManager.login(token: token) { err in
                    self.isConnected = err == nil ? true : false
                    completion(err)
                }
            }
        }
    }
    
    public func onConnectionStateChanged(channelName: String,
                                         connectionStateChanged state: AgoraRtmClientConnectionState,
                                         result reason: AgoraRtmClientConnectionChangeReason) {
        if reason == .changedChangedLost {
            //这里断连了，需要重新login
            self.isConnected = false
            login { err in
                self.isConnected = err == nil ? true : false
            }
        }
    }
}
