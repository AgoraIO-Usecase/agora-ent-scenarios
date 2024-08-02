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
import AgoraCommon
import AUIIMKit

private let kSceneId = "scene_play_zone_4.10.2"
private let KCollectionRobotInfo = "robot_info"
private let SYNC_SCENE_ROOM_STARTGAME_COLLECTION = "startGameCollection"
private let robotUid = 3000000001
private let headUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/{head}.png"

class JoyServiceImpl: NSObject {
    private var mRobotMap: [String:PlayRobotInfo] = [String:PlayRobotInfo]()
    private var appId: String
    private var user: InteractiveJoyUserInfo
    private var rtmClient: AgoraRtmClientKit?
    private var roomList: [InteractiveJoyRoomInfo] = []
    private var sceneBinded: Bool = false
    private var collectionBinded: Bool = false
    private var host: String
    private weak var listener: JoyServiceListenerProtocol?
    
    private(set) lazy var imService: AUIIMManagerServiceImplement = {
        let user = AUIChatUserInfo()
        user.userId = "\(self.user.userId)"
        user.userAvatar = self.user.avatar
        user.userName = self.user.userName
        let commonConfig = AUIChatCommonConfig()
        commonConfig.imAppKey = AppContext.shared.imAppKey
        commonConfig.owner = user
        AUIChatContext.shared.commonConfig = commonConfig
        
        let service = AUIIMManagerServiceImplement()
        return service
    }()
    
    private var userList: [InteractiveJoyUserInfo] = [] {
        didSet {
            self.listener?.onUserListDidChanged(userList: userList)
        }
    }
    
    private var rtmIsConnected: Bool = false
    private var imIsConnected: Bool = false
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
        
        let logConfig = AgoraRtmLogConfig()
        logConfig.filePath = AgoraEntLog.rtmSdkLogPath()
        logConfig.fileSizeInKB = 1024
        logConfig.level = .info
        let manager = AUISyncManager(rtmClient: rtmClient, commonConfig: config, logConfig: logConfig)
        return manager
    }()
    
    private lazy var roomService: AUIRoomService = {
        let poliocy = RoomExpirationPolicy()
        poliocy.expirationTime = 20 * 60 * 1000
        let service = AUIRoomService(expirationPolicy: poliocy, roomManager: roomManager, syncmanager: syncManager)
        
        return service
    }()
    
    required init(appId: String, host: String, user: InteractiveJoyUserInfo, rtmClient: AgoraRtmClientKit?) {
        self.appId = appId
        self.user = user
        self.rtmClient = rtmClient
        self.host = host
        AUIRoomContext.shared.displayLogClosure = { msg in
            JoyLogger.info(msg, context: "RTMSyncManager")
        }
        super.init()
        syncManager.rtmManager.subscribeError(channelName: "", delegate: self)
        login { err in
            self.rtmIsConnected = err == nil ? true : false
        }
    
        self.imServicelogin(retryCount: 2) { [weak self] err in
            self?.imIsConnected = err == nil ? true : false
        }
    }
    
    private func getRobotCollection(roomId: String) -> AUIMapCollection? {
        if roomId.isEmpty {
            return nil
        }
        
        let scene = syncManager.getScene(channelName: roomId)
        return scene?.getCollection(key: KCollectionRobotInfo)
    }
}

extension JoyServiceImpl {
    private func imServicelogin(retryCount: Int, completion: @escaping (NSError?) -> Void) {
        if retryCount <= 0 {
            let error = NSError(domain: "im service login failed", code: -1)
            JoyLogger.info("im login failed : \(error)")
            completion(error)
            return
        }
        
        self.imService.loginChat { [weak self] error in
            guard let self = self else { return }
            if let error = error {
                self.imServicelogin(retryCount: retryCount - 1, completion: completion)
            } else {
                completion(nil)
            }
        }
    }
    
    private func imServiceCreatChatRoom(roomInfo: InteractiveJoyRoomInfo, retryCount: Int, completion: @escaping (NSError?, String?) -> Void) {
        if retryCount <= 0 {
            let error = NSError(domain: "im service creat chat room failed", code: -1)
            JoyLogger.info("create chat room failed : \(error)")
            completion(error, nil)
            return
        }
        
        self.imService.createChatRoom(roomId: roomInfo.roomId, description: "") { [weak self] chatId, error in
            guard let self = self else { return }
            if let error = error {
                self.imServiceCreatChatRoom(roomInfo: roomInfo, retryCount: retryCount - 1, completion: completion)
            } else {
                completion(nil, chatId)
            }
        }
    }
    
    private func imServiceJoinChatRoom(roomInfo: InteractiveJoyRoomInfo, retryCount: Int, completion: @escaping (NSError?, String?) -> Void) {
        if retryCount <= 0 {
            let error = NSError(domain: "im service join chat room failed", code: -1)
            JoyLogger.info("join chat room failed : \(error)")
            completion(error, nil)
            return
        }
        
        self.imService.joinChatRoom(roomId: roomInfo.chatId ?? "") { [weak self] msg, error in
            guard let self = self else { return }
            if let error = error {
                self.imServiceJoinChatRoom(roomInfo: roomInfo, retryCount: retryCount - 1, completion: completion)
            } else {
                completion(nil, msg?.content)
            }
        }
    }
}

extension JoyServiceImpl: JoyServiceProtocol {
    func sendMessage(roomId: String, text: String, completion: @escaping (NSError?) -> Void) {
        self.imService.sendMessage(roomId: roomId, text: text) { _, error in
            completion(error)
        }
    }
    
    func createImRoom(roomInfo: InteractiveJoyRoomInfo, completion: @escaping (NSError?, String?) -> Void) {
        if imIsConnected {
            self.imServiceCreatChatRoom(roomInfo: roomInfo, retryCount: 2, completion: completion)
        } else {
            self.imServicelogin(retryCount: 2) { error in
                if error == nil {
                    self.imServiceCreatChatRoom(roomInfo: roomInfo, retryCount: 2, completion: completion)
                }
            }
        }
    }
    
    func joinImRoom(roomInfo: InteractiveJoyRoomInfo, completion: @escaping (NSError?, String?) -> Void) {
        if imIsConnected {
            self.imServiceJoinChatRoom(roomInfo: roomInfo, retryCount: 2, completion: completion)
        } else {
            self.imServicelogin(retryCount: 2) { error in
                if error == nil {
                    self.imServiceJoinChatRoom(roomInfo: roomInfo, retryCount: 2, completion: completion)
                }
            }
        }
    }
    
    func getRobotList() -> [PlayRobotInfo] {
        var robots = [PlayRobotInfo]()
        mRobotMap.values.forEach { robot in
            robots.append(robot)
        }
        
        return robots
    }
    
    func getRoomList(completion: @escaping ([InteractiveJoyRoomInfo]) -> Void) {
        let fetchRoomList: () -> Void = {[weak self] in
            self?.roomService.getRoomList(lastCreateTime: 0, pageSize: 50)  {[weak self] info in
                return info.owner?.userId == "\(self?.user.userId ?? 0)"
            } completion: {[weak self] err, ts, list in
                let joyRoomList = list?.compactMap{ self?.convertAUIRoomInfo2JoyRoomInfo(with:$0) } ?? []
                self?.roomList = joyRoomList
                completion(joyRoomList)
            }
        }
        
        if rtmIsConnected == false {
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
    
    func createRoom(gameRoomInfo: InteractiveJoyRoomInfo, completion: @escaping (InteractiveJoyRoomInfo?, Error?) -> Void) {
        JoyLogger.info("createRoom start")
        func create() {
            self.imServiceCreatChatRoom(roomInfo: gameRoomInfo, retryCount: 2) { [weak self] error, chatId in
                guard let self = self else {return}
                gameRoomInfo.chatId = chatId
                let aui_roominfo = convertJoyRoomInfo2AUIRoomInfo(with: gameRoomInfo)

                self.roomService.createRoom(room: aui_roominfo) { [weak self] err, info in
                    guard let self = self else { return }
                    if let err = err {
                        JoyLogger.info("enter scene fail: \(err.localizedDescription)")
                        completion(nil, err)
                        return
                    }
                    
                    completion(self.convertAUIRoomInfo2JoyRoomInfo(with: info ?? aui_roominfo), nil)
                    self.imServiceJoinChatRoom(roomInfo: gameRoomInfo, retryCount: 2) { _, _ in }
                }
                self.subscribeAll(channelName: gameRoomInfo.roomId)
            }
        }
        
        if imIsConnected == false {
            self.imServicelogin(retryCount: 2) { [weak self] error in
                self?.imIsConnected = error == nil ? true : false
            }
        }

        if rtmIsConnected == false {
            login { [weak self] err in
                if err == nil {
                    create()
                } else {
                    completion(nil, err)
                }
            }
        } else {
            create()
        }
    }
    
    func joinRoom(roomInfo: InteractiveJoyRoomInfo, completion: @escaping (Error?) -> Void) {
        let enterScene: () -> Void = {[weak self] in
            guard let self = self else {return}
            self.imServiceJoinChatRoom(roomInfo: roomInfo, retryCount: 2) { _, _ in }
            
            let aui_roominfo = convertJoyRoomInfo2AUIRoomInfo(with: roomInfo)
            self.roomService.enterRoom(roomInfo: aui_roominfo) { err in
                if let err = err {
                    JoyLogger.info("enter scene fail: \(err.localizedDescription)")
                    completion(err)
                    return
                }
                
                completion(nil)
            }
            subscribeAll(channelName: roomInfo.roomId)
        }
        
        if rtmIsConnected == false {
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
    
    func updateRoom(roomInfo: InteractiveJoyRoomInfo, completion: @escaping (NSError?) -> Void) {
        let updateRoomInfo: () -> Void = {[weak self] in
            guard let self = self else {return}
            let channelName = roomInfo.roomId
            
            let aui_roominfo = convertJoyRoomInfo2AUIRoomInfo(with: roomInfo)
            roomManager.updateRoom(room: aui_roominfo) {[weak self] err, info in
                if let err = err {
                    JoyLogger.info("enter scene fail: \(err.localizedDescription)")
                    completion(err)
                    return
                }
                completion(nil)
            }
        }
        
        if rtmIsConnected == false {
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
    
    func leaveRoom(roomInfo: InteractiveJoyRoomInfo, completion: @escaping (Error?) -> Void) {
        mRobotMap.removeAll()
        
        let performLeaveRoom: () -> Void = {[weak self] in
            guard let self = self else {return}
            _leaveRoom(roomId: roomInfo.roomId, isRoomOwner: roomInfo.ownerId == user.userId)
//            syncManager.rtmManager.unsubscribeMessage(channelName: roomInfo.roomId, delegate: self)
            completion(nil)
        }
        
        if rtmIsConnected == false {
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
        
        if imIsConnected == false {
            imService.leaveChatRoom { _ in }
        } else {
            imService.loginChat { [weak self] error in
                if error == nil {
                    self?.imService.leaveChatRoom { _ in }
                }
            }
        }
    }
    
    func subscribeListener(listener: JoyServiceListenerProtocol?) {
        self.listener = listener
    }
}

extension JoyServiceImpl: AUISceneRespDelegate {
    private func _leaveRoom(roomId: String, isRoomOwner: Bool) {
        JoyLogger.info("_leaveRoom: \(roomId) isRoomOwner:\(isRoomOwner)")
        roomService.leaveRoom(roomId: roomId)
        unsubscribeAll(channelName: roomId)
    }
    
    func onSceneExpire(channelName: String) {
        onSceneDestroy(channelName: channelName)
    }
    
    func onSceneDestroy(channelName: String) {
        JoyLogger.info("onSceneDestroy: \(channelName)")
        guard let model = self.roomList.filter({ $0.roomId == channelName }).first else {
            return
        }
        
        _leaveRoom(roomId: channelName, isRoomOwner: true)
        self.listener?.onRoomDidDestroy(roomInfo: model)
    }
    
    func onTokenPrivilegeWillExpire(channelName: String?) {
        NetworkManager.shared.generateToken(channelName: "",
                                            uid: String(user.userId),
                                            tokenTypes: [.rtm]) { token in
            if let token = token {
                self.syncManager.rtmManager.renew(token: token) { err in
                    JoyLogger.info("renew token：err \(err)")
                    if err == nil {
                        
                    }
                }
            }
        }
    }
    
    func onWillInitSceneMetadata(channelName: String) -> [String : Any]? {
        var robotMap = [String : Any]()
        for i in (0..<10) {
            let robotId1 = "\(robotUid + i * 2)"
            let userInfo1 =  AUIUserThumbnailInfo()
            userInfo1.userId = robotId1
            userInfo1.userName = "机器人\(1 + i * 2)"
            userInfo1.userAvatar = headUrl.replacingOccurrences(of: "{head}", with: "man\(i + 1)")

            let modelMale = PlayRobotInfo()
            modelMale.gender = "male"
            modelMale.level = 1
            modelMale.owner = userInfo1
            
            let modelMaleJson = JSONObject.toJson(modelMale)
            robotMap[robotId1] = modelMaleJson
            
            let robotId2 = "\(robotUid + i * 2 + 1)"
            let userInfo2 = AUIUserThumbnailInfo()
            userInfo2.userId = robotId2
            userInfo2.userName = "机器人\(2 + i * 2)"
            userInfo2.userAvatar = headUrl.replacingOccurrences(of: "{head}", with: "woman\(i + 1)")
            
            let modelFemale = PlayRobotInfo()
            modelFemale.gender = "female"
            modelFemale.level = 1
            modelFemale.owner = userInfo2
            
            let modelFemaleJson = JSONObject.toJson(modelFemale)
            robotMap[robotId2] = modelFemaleJson
            
        }
        return [
            KCollectionRobotInfo: robotMap
        ]
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
    
    func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
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
    private func convertAUIRoomInfo2JoyRoomInfo(with model: AUIRoomInfo) -> InteractiveJoyRoomInfo {
        let roomDict: [String: Any] = model.customPayload
        let joyRoom = InteractiveJoyRoomInfo()
        
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
        
        if let gameId = roomDict["gameId"] as? Int64 {
            joyRoom.gameId = gameId
        }
        
        if let pwd = roomDict["password"] as? String {
            joyRoom.password = pwd
        }
        
        if let isPrivate = roomDict["isPrivate"] as? Bool {
            joyRoom.isPrivate = isPrivate
        }
        
        if let chatId = roomDict["chatId"] as? String {
            joyRoom.chatId = chatId
        }
                
        return joyRoom
    }
    
    private func convertJoyRoomInfo2AUIRoomInfo(with model: InteractiveJoyRoomInfo) -> AUIRoomInfo {
        
        let roomInfo = AUIRoomInfo()
        var customPayload: [String: Any] = [:]
        customPayload["roomUserCount"] = model.roomUserCount
        customPayload["thumbnailId"] = model.thumbnailId
        customPayload["badgeTitle"] = model.badgeTitle
        customPayload["createdAt"] = model.createdAt
        customPayload["objectId"] = model.objectId
        customPayload["gameId"] = model.gameId
        customPayload["password"] = model.password
        customPayload["isPrivate"] = model.isPrivate
        customPayload["chatId"] = model.chatId
        
        roomInfo.customPayload = customPayload
        roomInfo.roomId = model.roomId
        
        let owner = AUIUserThumbnailInfo()
        owner.userId = "\(model.ownerId)"
        owner.userName = model.ownerName ?? ""
        owner.userAvatar = model.ownerAvatar ?? ""
        roomInfo.roomName = model.roomName ?? ""
        roomInfo.owner = owner
        
        return roomInfo
    }
    
    private func convertJoyUserInfo2AUIUserInfo(with user: InteractiveJoyUserInfo) -> AUIUserInfo {
        var userInfo = AUIUserInfo()
        userInfo.userId = String(user.userId)
        userInfo.userName = user.userName
        userInfo.userAvatar = user.avatar
        return userInfo
    }
    
    private func convertAUIUserInfo2JoyUserInfo(with user: AUIUserInfo) -> InteractiveJoyUserInfo {
        var userInfo = InteractiveJoyUserInfo()
        userInfo.userId = UInt(user.userId) ?? 0
        userInfo.userName = user.userName
        userInfo.avatar = user.userAvatar
        return userInfo
    }
    
    private func subscribeAll(channelName: String) {
        let scene = getCurrentScene(with: channelName)
        scene?.userService.bindRespDelegate(delegate: self)
        scene?.bindRespDelegate(delegate: self)
        innerSubscribeAll(roomId: channelName)
//        syncManager.rtmManager.subscribeMessage(channelName: channelName, delegate: self)
    }
    
    private func unsubscribeAll(channelName: String) {
        let scene = getCurrentScene(with: channelName)
        scene?.userService.unbindRespDelegate(delegate: self)
        scene?.unbindRespDelegate(delegate: self)
//        syncManager.rtmManager.unsubscribeMessage(channelName: channelName, delegate: self)
    }
    
    private func getCurrentScene(with channelName: String) -> AUIScene? {
        let scene = self.syncManager.getScene(channelName: channelName)
        return scene
    }
    
    private func innerSubscribeAll(roomId: String) {
        let robotCollection = getRobotCollection(roomId: roomId)
        robotCollection?.subscribeAttributesDidChanged(callback: {channelName, observeKey, value in
            if observeKey != KCollectionRobotInfo { return }
            var robots = value.getMap() ?? [String : Any]()
            var robotMap = [String: PlayRobotInfo]()
            
            for v in robots.values {
                guard let robot = JSONObject.toModel(PlayRobotInfo.self, value: v), let userId = robot.owner?.userId else { continue }
                robotMap[userId] = robot
            }
                        
            if self.mRobotMap.values.isEmpty {
                self.mRobotMap.merge(robotMap) { (current, _) in current }
                self.listener?.onRoomRobotDidLoad(robots: self.getRobotList())
            }
        })
    }
}

extension JoyServiceImpl: AUIRtmErrorProxyDelegate {
    private func login(completion:(@escaping (Error?)-> Void)) {
        NetworkManager.shared.generateToken(channelName: "",
                                            uid: String(user.userId),
                                            tokenTypes: [.rtm]) { token in
            if let token = token {
                self.syncManager.rtmManager.login(token: token) { err in
                    self.rtmIsConnected = err == nil ? true : false
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
            self.rtmIsConnected = false
            login { err in
                self.rtmIsConnected = err == nil ? true : false
            }
        }
    }
}

extension JSONObject {
    static public func toModel<T: Codable>(_ type: T.Type, value: Any) -> T? {
        guard let data = try? JSONSerialization.data(withJSONObject: value) else { return nil }
        let decoder = JSONDecoder()
        decoder.nonConformingFloatDecodingStrategy = .convertFromString(positiveInfinity: "+Infinity", negativeInfinity: "-Infinity", nan: "NaN")
        return try? decoder.decode(type, from: data)
    }
}
