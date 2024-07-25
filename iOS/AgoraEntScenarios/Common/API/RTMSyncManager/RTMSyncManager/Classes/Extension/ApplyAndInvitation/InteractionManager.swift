//
//  InteractionManager.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/6/13.
//

import Foundation

private let inviteKey = ""

@objc public protocol InteractionManagerProtocol: RoomPresenceProtocol,
                                                  PKServiceProtocol,
                                                  ApplyServiceProtocol,
                                                  InvitationServiceProtocol,
                                                  InteractionServiceProtocol {
}

@objc public class InteractionManager: NSObject {
    private let roomPresenceChannelName: String
    private let syncmanager: AUISyncManager
    private lazy var interactionMap: [String: InteractionService] = [:]
    private lazy var applyMap: [String: ApplyService] = [:]
    private lazy var invitationMap: [String: InvitationService] = [:]
    private lazy var pkMap: [String: PKService] = [:]
    private lazy var messageManagerMap: [String: InviteMessageManager] = [:]
    private lazy var roomPresenceService: RoomPresenceService = {
        let service = RoomPresenceService(channelName: roomPresenceChannelName, rtmManager: syncmanager.rtmManager)
        
        return service
    }()
    
    public required init(roomPresenceChannelName: String, syncmanager: AUISyncManager) {
        self.roomPresenceChannelName = roomPresenceChannelName
        self.syncmanager = syncmanager
        super.init()
    }
    
    private func getInteractionService(channelName: String) -> InteractionService {
        if let service = interactionMap[channelName] {
            return service
        }
        
        let service = InteractionService(channelName: channelName, 
                                         syncManager: syncmanager,
                                         presenceService: roomPresenceService)
        interactionMap[channelName] = service
        subscribeInteraction(channelName: channelName)
        return service
    }
    
    private func getApplyService(channelName: String) -> ApplyService {
        if let service = applyMap[channelName] {
            return service
        }
        
        let service = ApplyService(channelName: channelName, syncManager: syncmanager)
        applyMap[channelName] = service
        subscribeApply(channelName: channelName)
        return service
    }
    
    private func getInvitationService(channelName: String) -> InvitationService {
        if let service = invitationMap[channelName] {
            return service
        }
        
        let service = InvitationService(channelName: channelName, 
                                        syncManager: syncmanager,
                                        interactionService: getInteractionService(channelName: channelName))
        invitationMap[channelName] = service
        subscribeInteraction(channelName: channelName)
        return service
    }
    
    private func getPkService(channelName: String) -> PKService {
        if let service = pkMap[channelName] {
            return service
        }
        
        let service = PKService(channelName: channelName, 
                                syncManager: syncmanager,
                                presenceService: roomPresenceService, 
                                interactionService: getInteractionService(channelName: channelName))
        pkMap[channelName] = service
        return service
    }
    
    
    private func subscribeInteraction(channelName: String) {
        let collection = getInteractionService(channelName: channelName).interactionCollection
        collection.subscribeWillAdd {[weak collection] publisherUid, valueCmd, newItem in
            guard let valueCmd = valueCmd, let cmd = InteractionCmd(rawValue: valueCmd) else {
                return AUICommonError.unknown.toNSError()
            }
            let currentItem = collection?.getLocalMetaData()?.getMap()
            let currentType = InteractionType(rawValue: currentItem?["type"] as? Int ?? 0) ?? .idle
            switch cmd {
            case .startLink, .startPk:
                //开始互动需要保证连麦和pk请求时空闲
                guard currentType == .idle else {
                    return AUICommonError.unknown.toNSError()
                }
                return nil
            case .stop:
                //停止互动需要保证互动用户是互动表里的双方
                guard let interactionUserId = currentItem?["userId"] as? String else {
                    return AUICommonError.unknown.toNSError()
                }
                let stopUserId = publisherUid
                guard stopUserId == interactionUserId || AUIRoomContext.shared.isRoomOwner(channelName: channelName, userId: stopUserId) else {
                    return AUICommonError.noPermission.toNSError()
                }
                return nil
            }
        }
    }
    
    private func subscribeApply(channelName: String) {
        
        let collection = self.getApplyService(channelName: channelName).applyCollection
//        collection.subscribeWillAdd {[weak self] uid, valueCmd, newItem in
//            //添加申请需要判断当前是否在互动
//            guard let valueCmd = valueCmd,
//                  let cmd = ApplyCmd(rawValue: valueCmd),
//                  cmd == .create else {
//                return AUICommonError.unknown.toNSError()
//            }
//            
//            guard let collection = self?.getInteractionService(channelName: channelName).interactionCollection,
//                  let interationMap = collection.getLocalMetaData()?.getMap(),
//                  interationMap["type"] as? Int ?? 0 == InteractionType.idle.rawValue else {
//                return AUICommonError.unknown.toNSError()
//            }
//            
//            return nil
//        }
        collection.subscribeWillRemove {[weak self] publisherId, valueCmd, oldValue in
            guard let valueCmd = valueCmd, let cmd = ApplyCmd(rawValue: valueCmd) else {
                return AUICommonError.unknown.toNSError()
            }
            
            switch cmd {
            case .accept:
                guard let interactionService = self?.getInteractionService(channelName: channelName),
                      let interationMap = interactionService.interactionCollection.getLocalMetaData()?.getMap(),
                      interationMap["type"] as? Int ?? 0 == InteractionType.idle.rawValue else {
                    return AUICommonError.unknown.toNSError()
                }
                //accept必须保证当前不在互动
                guard interationMap["type"] as? Int ?? 0 == InteractionType.idle.rawValue else {
                    return AUICommonError.unknown.toNSError()
                }
                let userId = oldValue["userId"] as? String ?? ""
                interactionService.startLinkingInteraction(userId: userId) { _ in
                }
                return nil
            case .reject:
                break
            default:
                break
            }
            return nil
        }
    }
    
    private func subscribeApplyEvent(channelName: String, delegate: ApplyServiceProtocol) {
        let applyService = getApplyService(channelName: channelName)
        applyService.subscribe(delegate: delegate)
    }
    
    private func unsubscribeApplyEvent(channelName: String, delegate: ApplyServiceProtocol) {
        let service = getApplyService(channelName: channelName)
        service.unsubscribe(delegate: delegate)
    }
    
    private func subscribeInviteEvent(channelName: String, delegate: InvitationServiceProtocol) {
        let service = getInvitationService(channelName: channelName)
        service.subscribe(delegate: delegate)
    }
    
    private func unsubscribeInviteEvent(channelName: String, delegate: InvitationServiceProtocol) {
        let service = getInvitationService(channelName: channelName)
        service.unsubscribe(delegate: delegate)
    }
    
    private func subscribePKEvent(channelName: String, delegate: PKServiceProtocol) {
        let service = getPkService(channelName: channelName)
        service.subscribe(delegate: delegate)
    }
    
    private func unsubscribePKEvent(channelName: String, delegate: PKServiceProtocol) {
        let service = getPkService(channelName: channelName)
        service.unsubscribe(delegate: delegate)
    }
    
    private func subscribeInteractionEvent(channelName: String, delegate: InteractionServiceProtocol) {
        let service = getInteractionService(channelName: channelName)
        service.subscribe(delegate: delegate)
    }
    
    private func unsubscribeInteractionEvent(channelName: String, delegate: InteractionServiceProtocol) {
        let service = getInteractionService(channelName: channelName)
        service.unsubscribe(delegate: delegate)
    }
    
    private func subscribePKUserList(delegate: RoomPresenceProtocol) {
        roomPresenceService.subscribe(delegate: delegate)
    }
    
    private func unsubscribePKUserList(delegate: RoomPresenceProtocol) {
        roomPresenceService.unsubscribe(delegate: delegate)
    }
}


//MARK: public
extension InteractionManager {
    public func getMessageManager(channelName: String, key: String) -> InviteMessageManager {
        if let manager = messageManagerMap[channelName] {
            return manager
        }
        
        let manager = InviteMessageManager(channelName: channelName,
                                           key: key,
                                           rtmManager: syncmanager.rtmManager)
        messageManagerMap[channelName] = manager
        return manager
    }
    
    public func enterScene(channelName: String, roomName: String) {
        let scene = syncmanager.getScene(channelName: channelName)
        scene?.bindRespDelegate(delegate: self)
        
        let isRoomOwner = AUIRoomContext.shared.isRoomOwner(channelName: channelName)
        if isRoomOwner {
            //set pk user list
            let user = RoomPresenceInfo()
            user.roomId = channelName
            user.roomName = roomName
            user.ownerId = AUIRoomContext.shared.currentUserInfo.userId
            user.ownerName = AUIRoomContext.shared.currentUserInfo.userName
            user.ownerAvatar = AUIRoomContext.shared.currentUserInfo.userAvatar
            user.status = .idle
            roomPresenceService.setRoomPresenceInfo(user: user) { err in
            }
            
            roomPresenceService.subscribeChannel()
        }
        
        //lazy load service
//        let messageManager = getMessageManager(channelName: channelName)
        let interactionService = getInteractionService(channelName: channelName)
        let applyService = getApplyService(channelName: channelName)
        let invitationService = getInvitationService(channelName: channelName)
        let pkService = getPkService(channelName: channelName)
    }
    
    public func leaveScene(channelName: String) {
        roomPresenceService.unsubscribeChannel()
//        let messageManager = getMessageManager(channelName: channelName)
//        let interactionService = getInteractionService(channelName: channelName)
//        let applyService = getApplyService(channelName: channelName)
//        let invitationService = getInvitationService(channelName: channelName)
//        let pkService = getPkService(channelName: channelName)
    }
    
    public func subscribe(channelName: String, delegate: InteractionManagerProtocol) {
        subscribePKUserList(delegate: delegate)
        subscribeApplyEvent(channelName: channelName, delegate: delegate)
        subscribeInviteEvent(channelName: channelName, delegate: delegate)
        subscribeInteractionEvent(channelName: channelName, delegate: delegate)
        subscribePKEvent(channelName: channelName, delegate: delegate)
    }
    
    public func unsubscribe(channelName: String, delegate: InteractionManagerProtocol) {
        unsubscribePKUserList(delegate: delegate)
        unsubscribeApplyEvent(channelName: channelName, delegate: delegate)
        unsubscribeInviteEvent(channelName: channelName, delegate: delegate)
        unsubscribeInteractionEvent(channelName: channelName, delegate: delegate)
        unsubscribePKEvent(channelName: channelName, delegate: delegate)
    }

    //apply
    public func startApply(channelName: String, userId: String, completion: ((NSError?)->())?) {
        let service = getApplyService(channelName: channelName)
        service.addApply(userId: userId, completion: completion)
    }
    
    public func acceptApply(channelName: String, userId: String, completion: ((NSError?)->())?) {
        let service = getApplyService(channelName: channelName)
        service.acceptApply(userId: userId, completion: completion)
    }
    
    public func cancelApply(channelName: String, userId: String, completion: ((NSError?)->())?) {
        let service = getApplyService(channelName: channelName)
        service.cancelApply(userId: userId, completion: completion)
    }
    
    public func getApplyList(channelName: String, completion:@escaping ((NSError?, [ApplyInfo]?)-> Void)) {
        let service = getApplyService(channelName: channelName)
        service.getApplyList(completion: completion)
    }
    
    //invitation
    public func startInvite(channelName: String, userId: String, completion: ((NSError?)->())?) {
        let service = getInvitationService(channelName: channelName)
        service.sendInvitation(userId: userId, completion: completion)
    }
    
    public func acceptInvite(channelName: String, invitationId: String, completion: ((NSError?)->())?) {
        let service = getInvitationService(channelName: channelName)
        service.acceptInvitation(invitationId: invitationId, completion: completion)
    }
    
    public func rejectInvite(channelName: String, invitationId: String, completion: ((NSError?)->())?) {
        let service = getInvitationService(channelName: channelName)
        service.rejectInvitation(invitationId: invitationId, completion: completion)
    }
    
    //pk
    public func invitePK(currentChannelName: String,
                         targetChannelName: String,
                         completion: ((NSError?)->())?) {
        let service = getPkService(channelName: currentChannelName)
        service.invitePK(roomId: targetChannelName, completion: completion)
    }
    
    public func acceptPK(channelName: String,
                         pkId: String,
                         completion: ((NSError?)->())?) {
        let service = getPkService(channelName: channelName)
        service.acceptPK(pkId: pkId, completion: completion)
    }
    
    public func rejectPK(channelName: String,
                         pkId: String,
                         completion: ((NSError?)->())?) {
        let service = getPkService(channelName: channelName)
        service.rejectPK(pkId: pkId, completion: completion)
    }
    
    public func getPKUserList() -> [RoomPresenceInfo] {
        return roomPresenceService.userList
    }
    
    public func getPKUserList(completion: @escaping (NSError?, [RoomPresenceInfo]?)->()) {
        roomPresenceService.getAllRoomPresenceInfo(completion: completion)
    }
    
    public func getLatestInteractionInfo(channelName: String,
                                         completion: ((NSError?, InteractionInfo?)->())?) {
        let service = getInteractionService(channelName: channelName)
        service.getLatestInteractionInfo(completion: completion)
    }
    
    public func stopInteraction(channelName: String,
                                completion: ((NSError?)->())?) {
        let service = getInteractionService(channelName: channelName)
        service.stopInteraction(completion: completion)
    }
}

//MARK: AUISceneRespDelegate
extension InteractionManager: AUISceneRespDelegate {
    public func onSceneExpire(channelName: String) {
        
    }
    
    public func onSceneDestroy(channelName: String) {
        
    }
}
