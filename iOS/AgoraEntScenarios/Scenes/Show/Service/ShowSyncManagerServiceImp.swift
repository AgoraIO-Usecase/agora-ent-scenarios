//
//  ShowSyncManagerServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/3.
//

import Foundation
import AgoraCommon
import RTMSyncManager
import AgoraRtmKit

private let kSceneId = "scene_show_5.0.0"
private let kRoomPresenceChannelName = "scene_show_5_0_0_9999999"

private func agoraPrint(_ message: String) {
    ShowLogger.info(message, context: "Service")
}

public class ShowSyncManagerServiceImp: NSObject {
    private let user: AUIUserThumbnailInfo
    private var isLogined: Bool = false
    private var expireTimerMap: [String: Timer] = [:]
    private var delegates: [String: NSHashTable<ShowSubscribeServiceProtocol>] = [:]
    private lazy var roomManager = AUIRoomManagerImpl(sceneId: kSceneId)
    private lazy var syncManager: AUISyncManager = {
        let config = AUICommonConfig()
        config.appId = AppContext.shared.appId
        config.owner = user
        config.host = AppContext.shared.roomManagerUrl
        let logConfig = AgoraRtmLogConfig()
        logConfig.filePath = AgoraEntLog.rtmSdkLogPath()
        logConfig.fileSizeInKB = 1024
        logConfig.level = .info
        let manager = AUISyncManager(rtmClient: nil, commonConfig: config, logConfig: logConfig)
        
        return manager
    }()
    
    private lazy var interactionManager: InteractionManager = {
        let manager = InteractionManager(roomPresenceChannelName: kRoomPresenceChannelName, syncmanager: syncManager)
        
        return manager
    }()
    
    private lazy var roomService: AUIRoomService = {
        let service = AUIRoomService(expirationPolicy: RoomExpirationPolicy.defaultPolicy(),
                                     roomManager: roomManager,
                                     syncmanager: syncManager)
        
        return service
    }()
    
    override init() {
        let owner = AUIUserThumbnailInfo()
        owner.userId = VLUserCenter.user.id
        owner.userName = VLUserCenter.user.name
        owner.userAvatar = VLUserCenter.user.headUrl
        self.user = owner
        AUIRoomContext.shared.displayLogClosure = { msg in
            ShowLogger.info(msg, context: "RTMSyncManager")
        }
        super.init()
        syncManager.rtmManager.subscribeError(channelName: "", delegate: self)
        
        AppContext.shared.agoraRTCToken = ""
        AppContext.shared.agoraRTMToken = ""
    }
}

//MARK: private
extension ShowSyncManagerServiceImp {
    private func interactionEnter(roomId: String, roomName: String) {
        interactionManager.subscribe(channelName: roomId, delegate: self)
        interactionManager.enterScene(channelName: roomId, roomName: roomName)
        let msgManager = interactionManager.getMessageManager(channelName: roomId, key: "chatMessage")
        msgManager.subscribe(delegate: self)
        startCheckExpireTimer(roomId: roomId)
    }
    
    private func interactionLeave(roomId: String) {
        interactionManager.unsubscribe(channelName: roomId, delegate: self)
        let msgManager = interactionManager.getMessageManager(channelName: roomId, key: "chatMessage")
        msgManager.unsubscribe(delegate: self)
        interactionManager.leaveScene(channelName: roomId)
        stopCheckExpireTimer(roomId: roomId)
    }
    
    private func preGenerateToken(completion:@escaping (NSError?)->()) {
        agoraPrint("preGenerateToken start")
        AppContext.shared.agoraRTCToken = ""
        AppContext.shared.agoraRTMToken = ""
        
        let date = Date()
        NetworkManager.shared.generateToken(channelName: "",
                                            uid: "\(user.userId)",
                                            tokenTypes: [.rtc, .rtm]) {  token in
            guard let rtcToken = token, let rtmToken = token else {
                completion(NSError(domain: "generate token fail", code: -1))
                return
            }
            
            agoraPrint("[Timing]preGenerateToken rtc & rtm cost: \(Int64(-date.timeIntervalSinceNow * 1000)) ms")
            AppContext.shared.agoraRTCToken = rtcToken
            AppContext.shared.agoraRTMToken = rtmToken
            completion(nil)
        }
    }
    
    private func login(completion:(@escaping (NSError?)-> Void)) {
        let token = AppContext.shared.agoraRTMToken
        if !token.isEmpty {
            let date = Date()
            self.syncManager.rtmManager.login(token: token) { err in
                agoraPrint("[Timing]login cost: \(Int64(-date.timeIntervalSinceNow * 1000))ms")
                self.isLogined = err == nil ? true : false
                completion(err)
            }
            return
        }
        preGenerateToken { [weak self] err in
            if let err = err {
                completion(err)
                return
            }
            self?.login(completion: completion)
        }
    }
    
    private func subscribeScene(chanelName:String) {
        let scene = syncManager.getScene(channelName: chanelName)
        scene?.bindRespDelegate(delegate: self)
        scene?.userService.bindRespDelegate(delegate: self)
    }
    
    private func unSubscribeScene(chanelName:String) {
        let scene = syncManager.getScene(channelName: chanelName)
        scene?.unbindRespDelegate(delegate: self)
        scene?.userService.unbindRespDelegate(delegate: self)
    }
    
    private func _sendJoinOrLeaveText(channelName: String,
                                      user: AUIUserInfo,
                                      isJoin: Bool) {
        if let values = delegates[channelName] {
            let message = ShowMessage()
            message.userId = user.userId
            message.userName = user.userName
            message.message = (isJoin ? "join_live_room" : "leave_live_room").show_localized
            message.createAt = Date().millionsecondSince1970()
            
            for element in values.allObjects {
                element.onMessageDidAdded(channelName: channelName, message: message)
            }
        }
    }
    
    
    private func updateRoom(channelName:String, 
                            userCount: Int,
                            completion: @escaping (NSError?) -> Void) {
        guard let roomInfo = roomService.getRoomInfo(roomId: channelName) else {
            completion(NSError(domain: "not found roominfo", code: -1))
            return
        }
        let updateRoomInfo: () -> Void = {[weak self] in
            guard let self = self else {return}
            roomInfo.roomUserCount = userCount
            roomManager.updateRoom(room: roomInfo) { err, info in
                if let err = err {
                    agoraPrint("enter scene fail: \(err.localizedDescription)")
                    completion(err)
                    return
                }
                completion(nil)
            }
        }
        
        if isLogined == false {
            login { err in
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
    
    public func startCheckExpireTimer(roomId: String) {
        guard let ownerId = roomService.getRoomInfo(roomId: roomId)?.owner?.userId,
           ShowRobotService.shared.isRobotOwner(ownerId: ownerId) == false else {
            return
        }
        let expireTimer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] timer in
            guard let self = self,
                  let scene = self.syncManager.getScene(channelName: roomId) else { return }
            
            let duration = scene.getRoomDuration()
            let expiredDuration = 20 * 60 * 1000
            if duration >= expiredDuration {
                self.stopCheckExpireTimer(roomId: roomId)
                
                self.onSceneExpire(channelName: roomId)
                return
            }
        }
        expireTimerMap[roomId] = expireTimer
    }
    
    public func stopCheckExpireTimer(roomId: String) {
        let expireTimer = expireTimerMap[roomId]
        expireTimer?.invalidate()
        expireTimerMap[roomId] = nil
    }
}

//MARK: ShowServiceProtocol
extension ShowSyncManagerServiceImp: ShowServiceProtocol {
    func getRoomList(page: Int, 
                     completion: @escaping (NSError?, [ShowRoomListModel]?) -> Void) {
        if isLogined == false {
            login {[weak self] err in
                if let err = err {
                    completion(err, nil)
                    return
                }
                self?.getRoomList(page: page, completion: completion)
            }
            return
        }
        
        let currentUserId = user.userId
        roomService.getRoomList(lastCreateTime: 0,
                                pageSize: 50) { info in
            return info.owner?.userId == currentUserId
        } completion: { err, ts, list in
            let roomList = list?.map({ $0.createShowServiceModel()}) ?? []
            
            let dataArray = ShowRobotService.shared.generateRobotRoomsAppend(rooms: roomList)
            completion(err, dataArray)
        }
    }
    
    func createRoom(roomId: String,
                    roomName: String,
                    completion: @escaping (NSError?, ShowRoomDetailModel?) -> Void) {
        if isLogined == false {
            login {[weak self] err in
                if let err = err {
                    completion(err, nil)
                    return
                }
                self?.createRoom(roomId: roomId, 
                                 roomName: roomName,
                                 completion: completion)
            }
            return
        }
        agoraPrint("createRoom roomId: \(roomId) roomName: \(roomName)")
        let roomInfo = AUIRoomInfo()
        roomInfo.roomId = roomId
        roomInfo.roomName = roomName
        roomInfo.owner = user
        roomService.createRoom(room: roomInfo, enterEnable: false) {[weak self] err, info in
            if err == nil {
//                self?.interactionEnter(roomId: roomId, roomName: roomName)
            } else {
                self?.unSubscribeScene(chanelName: roomId)
            }
            completion(err, info?.createShowServiceModel())
        }
        subscribeScene(chanelName: roomId)
    }
    
    func joinRoom(room: ShowRoomListModel, completion: @escaping (NSError?, ShowRoomDetailModel?) -> Void) {
        if isLogined == false {
            login {[weak self] err in
                if let err = err {
                    completion(err, nil)
                    return
                }
                self?.joinRoom(room: room, completion: completion)
            }
            return
        }
        agoraPrint("joinRoom roomId: \(room.roomId) roomName: \(room.roomName ?? "")")
        let roomInfo = AUIRoomInfo.convertFromShowRoomListModel(room)
        if ShowRobotService.shared.isRobotOwner(ownerId: room.ownerId) {
            let poliocy = RoomExpirationPolicy()
            poliocy.isAssociatedWithOwnerOffline = false
            let scene = syncManager.createScene(channelName: room.roomId, roomExpiration: poliocy)
            scene.create(createTime: 0,
                         ownerId: room.ownerId,
                         payload: [kRoomServicePayloadOwnerId: room.ownerId]) {[weak self] err in
                guard let self = self else { return }
                if let err = err {
                    completion(err, nil)
                    return
                }
                self.roomService.enterRoom(roomInfo: roomInfo, expirationPolicy: poliocy) {[weak self] err in
                    if err == nil {
                        self?.interactionEnter(roomId: room.roomId, roomName: room.roomName ?? "")
                    } else {
                        self?.unSubscribeScene(chanelName: room.roomId)
                    }
                    completion(err, room)
                }
            }
        } else {
            roomService.enterRoom(roomInfo: roomInfo) {[weak self] err in
                if err == nil {
                    self?.interactionEnter(roomId: room.roomId, roomName: room.roomName ?? "")
                } else {
                    self?.unSubscribeScene(chanelName: room.roomId)
                }
                completion(err, room)
            }
        }
        subscribeScene(chanelName: room.roomId)
    }
    
    func leaveRoom(roomId: String, completion: @escaping (NSError?) -> Void) {
        if isLogined == false {
            login {[weak self] err in
                if let err = err {
                    completion(err)
                    return
                }
                self?.leaveRoom(roomId: roomId, completion: completion)
            }
            return
        }
        
        interactionLeave(roomId: roomId)
        roomService.leaveRoom(roomId: roomId)
        completion(nil)
    }
    
    func getAllUserList(roomId: String, completion: @escaping (NSError?, [ShowUser]?) -> Void) {
        if isLogined == false {
            login {[weak self] err in
                if let err = err {
                    completion(err, nil)
                    return
                }
                self?.getAllUserList(roomId: roomId, completion: completion)
            }
            return
        }
        
        let scene = syncManager.getScene(channelName: roomId)
        scene?.userService.getUserInfoList(roomId: roomId, callback: completion)
    }
    
    func sendChatMessage(roomId: String, message: ShowMessage, completion: ((NSError?) -> Void)?) {
        let msgManager = interactionManager.getMessageManager(channelName: roomId, key: "chatMessage")
        let content = encodeToJsonStr(message) ?? ""
        msgManager.sendMessage(content: content, userId: "",
                               completion: completion)
    }
    
    func getAllMicSeatApplyList(roomId: String, completion: @escaping (NSError?, [ShowMicSeatApply]?) -> Void) {
        interactionManager.getApplyList(channelName: roomId, 
                                        completion: completion)
    }
    
    func createMicSeatApply(roomId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.startApply(channelName: roomId, 
                                      userId: user.userId,
                                      completion: completion)
    }
    
    func cancelMicSeatApply(roomId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.cancelApply(channelName: roomId, 
                                       userId: user.userId,
                                       completion: completion)
    }
    
    func acceptMicSeatApply(roomId: String, userId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.acceptApply(channelName: roomId, 
                                       userId: userId,
                                       completion: completion)
    }
    
    func createMicSeatInvitation(roomId: String, userId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.startInvite(channelName: roomId, 
                                       userId: userId,
                                       completion: completion)
    }
    
    func acceptMicSeatInvitation(roomId: String, invitationId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.acceptInvite(channelName: roomId, 
                                        invitationId: invitationId,
                                        completion: completion)
    }
    
    func rejectMicSeatInvitation(roomId: String, invitationId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.rejectInvite(channelName: roomId, 
                                        invitationId: invitationId,
                                        completion: completion)
    }
    
    func getAllPKUserList(completion: @escaping (NSError?, [ShowPKUserInfo]?) -> Void) {
        interactionManager.getPKUserList { err, list in
            let list = list?.filter({ $0.ownerId != self.user.userId})
            completion(err, list)
        }
    }
    
    func createPKInvitation(roomId: String, pkRoomId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.invitePK(currentChannelName: roomId, 
                                    targetChannelName: pkRoomId,
                                    completion: completion)
    }
    
    func acceptPKInvitation(roomId: String, invitationId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.acceptPK(channelName: roomId,
                                    pkId: invitationId,
                                    completion: completion)
    }
    
    func rejectPKInvitation(roomId: String, invitationId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.rejectPK(channelName: roomId, 
                                    pkId: invitationId,
                                    completion: completion)
    }
    
    func getInterationInfo(roomId: String, completion: @escaping (NSError?, ShowInteractionInfo?) -> Void) {
        interactionManager.getLatestInteractionInfo(channelName: roomId) { err, info in
            if let err = err {
                completion(err, nil)
                return
            }
            
            completion(nil, info)
        }
    }
    
    func stopInteraction(roomId: String, completion: @escaping (NSError?) -> Void) {
        interactionManager.stopInteraction(channelName: roomId,
                                           completion: completion)
    }
    
    func muteAudio(roomId: String, mute: Bool, completion: @escaping (NSError?) -> Void) {
        let scene = syncManager.getScene(channelName: roomId)
        scene?.userService.muteUserAudio(isMute: mute, callback: completion)
    }
    
    func getCurrentNtpTs(roomId: String) -> UInt64 {
        let scene = syncManager.getScene(channelName: roomId)
        return scene?.getCurrentTs() ?? 0
    }
    
    func subscribeEvent(roomId: String, delegate: ShowSubscribeServiceProtocol) {
        if let value = delegates[roomId] {
            if !value.contains(delegate) {
                value.add(delegate)
            }
        }else{
            let weakObjects = NSHashTable<ShowSubscribeServiceProtocol>.weakObjects()
            weakObjects.add(delegate)
            delegates[roomId] = weakObjects
        }
    }
    
    func unsubscribeEvent(roomId: String, delegate: ShowSubscribeServiceProtocol) {
        guard let value = delegates[roomId] else {
            return
        }
        value.remove(delegate)
    }
}

//MARK: AUISceneRespDelegate
extension ShowSyncManagerServiceImp: AUISceneRespDelegate {
    public func onSceneExpire(channelName: String) {
        agoraPrint("onSceneExpire: \(channelName)")
        roomService.leaveRoom(roomId: channelName)
        if let values = delegates[channelName] {
            for element in values.allObjects {
                element.onRoomExpired(channelName: channelName)
            }
        }
    }
    
    public func onSceneDestroy(channelName: String) {
        agoraPrint("onSceneDestroy: \(channelName)")
        roomService.leaveRoom(roomId: channelName)
        if let values = delegates[channelName] {
            for element in values.allObjects {
                element.onRoomDestroy(channelName: channelName)
            }
        }
    }
    
    public func onTokenPrivilegeWillExpire(channelName: String?) {
//        NetworkManager.shared.generateToken(channelName: "", uid: String(user.id), tokenType: .token007, type: .rtm) { token in
//            if let token = token {
//                self.syncManager.renew(token: token) { err in
//                    guard let err = err else { return }
//                    KTVLog.error(text: "renew token：err \(err.localizedDescription)")
//                }
//            }
//        }
    }
}

extension ShowSyncManagerServiceImp: InviteMessageProtocol {
    public func onNewInviteDidReceived(channelName: String, message: InviteMessageInfo) {
        if let values = delegates[channelName] {
            guard let model: ShowMessage = decodeModel(jsonStr: message.content) else { return }
            for element in values.allObjects {
                element.onMessageDidAdded(channelName: channelName, message: model)
            }
        }
    }
}

extension ShowSyncManagerServiceImp: InteractionManagerProtocol {
    public func onUserSnapshot(channelName: String, userList: [RoomPresenceInfo]) {
        
    }
    
    public func onUserUpdate(channelName: String, user: RoomPresenceInfo) {
        
    }
    
    public func onUserDelete(channelName: String, user: RoomPresenceInfo) {
        
    }
    
    public func onUserError(channelName: String, error: NSError) {
        
    }
    
    public func onPKInfoDidReceive(channelName: String, info: PKInfo) {
        if let values = delegates[channelName] {
            for element in values.allObjects {
                switch info.type {
                case .inviting:
                    element.onPKInvitationUpdated(channelName: channelName, invitation: info)
                case .accept:
                    element.onPKInvitationAccepted(channelName: channelName, invitation: info)
                case .reject:
                    element.onPKInvitationRejected(channelName: channelName, invitation: info)
                default:
                    break
                }
                
            }
        }
    }
    
    public func onApplyListDidUpdate(channelName: String, list: [ApplyInfo]) {
        if let values = delegates[channelName] {
            for element in values.allObjects {
                element.onMicSeatApplyUpdated(channelName: channelName, applies: list)
            }
        }
    }
    
    public func onInvitationDidReceive(channelName: String, info: InvitationInfo) {
        if let values = delegates[channelName] {
            for element in values.allObjects {
                switch info.type {
                case .inviting:
                    element.onMicSeatInvitationUpdated(channelName: channelName, invitation: info)
                case .accept:
                    element.onMicSeatInvitationAccepted(channelName: channelName, invitation: info)
                case .reject:
                    element.onMicSeatInvitationRejected(channelName: channelName, invitation: info)
                default:
                    break
                }
            }
        }
    }
    
    public func onInteractionListDidUpdate(channelName: String, list: [InteractionInfo]) {
        if let values = delegates[channelName] {
            for element in values.allObjects {
                element.onInteractionUpdated(channelName: channelName, interactions: list)
            }
        }
    }
}

//MARK: AUIUserRespDelegate
extension ShowSyncManagerServiceImp: AUIUserRespDelegate {
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        let scene = syncManager.getScene(channelName: roomId)
        let userCount = scene?.userService.userList.count ?? 0
        if let values = delegates[roomId] {
            for element in values.allObjects {
//                let scene = syncManager.getScene(channelName: roomId)
                element.onUserCountChanged(channelName: roomId,
                                           userCount: userCount)
            }
        }
        
        updateRoom(channelName: roomId, userCount: userCount) { _ in
        }
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        let scene = syncManager.getScene(channelName: roomId)
        let userCount = scene?.userService.userList.count ?? 0
        if let values = delegates[roomId] {
            for element in values.allObjects {
                element.onUserJoinedRoom(channelName: roomId, user: userInfo)
//                let scene = syncManager.getScene(channelName: roomId)
                element.onUserCountChanged(channelName: roomId,
                                           userCount: userCount)
            }
        }
        
        updateRoom(channelName: roomId, userCount: userCount) { _ in
        }
        
        _sendJoinOrLeaveText(channelName: roomId, user: userInfo, isJoin: true)
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        let scene = syncManager.getScene(channelName: roomId)
        let userCount = scene?.userService.userList.count ?? 0
        if let values = delegates[roomId] {
            for element in values.allObjects {
                element.onUserLeftRoom(channelName: roomId, user: userInfo)
//                let scene = syncManager.getScene(channelName: roomId)
                element.onUserCountChanged(channelName: roomId,
                                           userCount: userCount)
            }
        }
        
        updateRoom(channelName: roomId, userCount: userCount) { _ in
        }
        
        _sendJoinOrLeaveText(channelName: roomId, user: userInfo, isJoin: false)
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        let scene = syncManager.getScene(channelName: roomId)
        let userCount = scene?.userService.userList.count ?? 0
        if let values = delegates[roomId] {
            for element in values.allObjects {
                element.onUserCountChanged(channelName: roomId,
                                           userCount: userCount)
            }
        }
        
        updateRoom(channelName: roomId, userCount: userCount) { _ in
        }
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
  
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserBeKicked(roomId: String, userId: String) {
        
    }
    
    
}

//MARK: AUIRtmErrorProxyDelegate
extension ShowSyncManagerServiceImp: AUIRtmErrorProxyDelegate {
    
}
