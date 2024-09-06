//
//  PKService.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/6/7.
//

import Foundation

@objc public enum PKType: Int, Codable {
    case inviting = 0
    case accept = 1
    case reject = 2
    case end = 3
}

@objcMembers public class PKInfo: NSObject, Codable {
    public var id: String = UUID().uuidString
    public var userId: String = ""
    public var userName: String = ""
    public var roomId: String = ""
    public var fromUserId: String = ""
    public var fromUserName: String = ""
    public var fromRoomId: String = ""
    public var type: PKType = .inviting
    
    enum CodingKeys: String, CodingKey {
        case id, userId, userName, roomId, fromUserId, fromUserName, fromRoomId, type
    }
}

@objc public protocol PKServiceProtocol: NSObjectProtocol {
    func onPKInfoDidReceive(channelName: String, info: PKInfo)
}

private let key = "pk"

public class PKService: NSObject {
    private var channelName: String = ""
    private var syncManager: AUISyncManager
    private var roomPresenceService: RoomPresenceService
    private var interactionService: InteractionService
    
    private var respDelegates = NSHashTable<PKServiceProtocol>.weakObjects()
    
    private lazy var messageManager: InviteMessageManager = {
        let manager = InviteMessageManager(channelName: channelName, key: key, rtmManager: syncManager.rtmManager)
        return manager
    }()
    
    deinit {
        aui_info("deinit PKService[\(channelName)]", tag: "PKService")
        innerUnsubscribe()
    }
    
    required init(channelName: String,
                  syncManager: AUISyncManager,
                  presenceService: RoomPresenceService,
                  interactionService: InteractionService) {
        aui_info("init PKService[\(channelName)]", tag: "PKService")
        self.channelName = channelName
        self.syncManager = syncManager
        self.roomPresenceService = presenceService
        self.interactionService = interactionService
        super.init()
        innerSubscribe()
    }
    
    private func innerSubscribe() {
        messageManager.subscribe(delegate: self)
        roomPresenceService.subscribe(delegate: self)
    }
    
    private func innerUnsubscribe() {
        messageManager.unsubscribe(delegate: self)
        roomPresenceService.unsubscribe(delegate: self)
    }
    
    private func createPKInfo(_ user: RoomPresenceInfo, type: PKType) -> PKInfo {
        let info = PKInfo()
        info.userId = user.ownerId
        info.userName = user.ownerName
        info.roomId = user.roomId
        info.fromUserId = user.interactorId
        info.fromUserName = user.interactorName
        info.fromRoomId = user.roomId
        info.type = type
        
        return info
    }
}

//MARK: public
extension PKService {
    public func subscribe(delegate: PKServiceProtocol) {
        if respDelegates.contains(delegate) {
            return
        }
        
        respDelegates.add(delegate)
    }
    
    public func unsubscribe(delegate: PKServiceProtocol) {
        respDelegates.remove(delegate)
    }
    
    public func invitePK(roomId: String, completion: ((NSError?) -> ())?) {
        guard let roomPresenceInfo = roomPresenceService.getRoomPresenceInfo(roomId: roomId),
              roomPresenceInfo.status == .idle else {
            aui_info("invitePK roomId: \(roomId) fail: room presence status is not idle", tag: "PKService")
            completion?(NSError(domain: "room presence status is not idle", code: 0, userInfo: nil))
            return
        }
        
        guard let currRoomPresenceInfo = roomPresenceService.getRoomPresenceInfo(roomId: channelName),
              currRoomPresenceInfo.status == .idle else {
            aui_info("invitePK roomId: \(roomId) fail: current room presence status is not idle", tag: "PKService")
            completion?(NSError(domain: "current room presence status is not idle", code: 0, userInfo: nil))
            return
        }
        
        aui_info("invitePK roomId: \(roomId)", tag: "PKService")
        let info = PKInfo()
        info.userId = roomPresenceInfo.ownerId
        info.userName = roomPresenceInfo.ownerName
        info.roomId = roomPresenceInfo.roomId
        info.fromUserId = AUIRoomContext.shared.currentUserInfo.userId
        info.fromUserName = AUIRoomContext.shared.currentUserInfo.userName
        info.fromRoomId = channelName
        info.type = .inviting
        
        let content = encodeModelToJsonStr(info) ?? ""
        messageManager.sendMessage(content: content,
                                   userId: roomPresenceInfo.ownerId,
                                   channelName: roomPresenceInfo.roomId) { err in
            aui_info("invitePK roomId: \(roomId) completion: \(err?.localizedDescription ?? "success")", tag: "PKService")
            completion?(err)
        }
    }
    
    public func acceptPK(pkId: String, completion: ((NSError?) -> ())?) {
        guard let message = messageManager.getMessage({ $0.content.contains(pkId) }),
              let pkInfo: PKInfo = decodeModel(jsonStr: message.content) else {
            self.messageManager.removeMessage(id: pkId)
            aui_info("acceptPK pkId: \(pkId) fail: pk info not found", tag: "PKService")
            completion?(NSError(domain: "pk info not found", code: 0, userInfo: nil))
            return
        }
        
        guard let roomPresenceInfo = roomPresenceService.getRoomPresenceInfo(roomId: pkInfo.fromRoomId),
              roomPresenceInfo.status == .idle else {
            self.messageManager.removeMessage(id: pkId)
            aui_info("acceptPK pkId: \(pkId) fail: room presence status is not idle", tag: "PKService")
            completion?(NSError(domain: "room presence status is not idle", code: 0, userInfo: nil))
            return
        }
        
        aui_info("acceptPK pkId: \(pkId)", tag: "PKService")
        pkInfo.type = .accept
        let content = encodeModelToJsonStr(pkInfo) ?? ""
        // 因为不管对方在不在线，更新presence总是会成功，这里被一个点对点消息用于判断对方是否在线
        messageManager.sendMessage(content: content, userId: pkInfo.fromUserId) {[weak self] err in
            guard let self = self else {return}
            aui_info("send accept message completion: \(err?.localizedDescription ?? "")", tag: "PKService")
            if let err = err {
                completion?(err)
                return
            }
            
            self.roomPresenceService.updateRoomPresenceInfo(roomId: self.channelName,
                                                            status: .pk,
                                                            interactorId: pkInfo.fromUserId,
                                                            interactorName: pkInfo.fromUserName) {[weak self] err in
                guard let self = self else { return }
                aui_info("acceptPK pkId: \(pkId) completion: \(err?.localizedDescription ?? "success")", tag: "PKService")
                if err == nil {
                    self.messageManager.removeMessage(id: message.id)
                }
                
                completion?(err)
            }
        }
        
    }
    
    public func rejectPK(pkId: String, completion: ((NSError?) -> ())?) {
        guard let message = messageManager.getMessage({ $0.content.contains(pkId) }),
              let pkInfo: PKInfo = decodeModel(jsonStr: message.content) else {
            aui_info("rejectPK pkId: \(pkId) fail: pk info not found", tag: "PKService")
            completion?(NSError(domain: "pk info not found", code: 0, userInfo: nil))
            return
        }
        aui_info("rejectPK pkId: \(pkId)", tag: "PKService")
        
        pkInfo.type = .reject
        let content = encodeModelToJsonStr(pkInfo) ?? ""
        messageManager.sendMessage(content: content,
                                   userId: pkInfo.userId) {[weak self] err in
            guard let self = self else { return }
            aui_info("rejectPK pkId: \(pkId) completion: \(err?.localizedDescription ?? "success")", tag: "PKService")
            if err == nil {
                self.messageManager.removeMessage(id: message.id)
            }
            
            completion?(err)
        }
    }
}


//MARK: InviteMessageProtocol
extension PKService: InviteMessageProtocol {
    public func onNewInviteDidReceived(channelName: String, message: InviteMessageInfo) {
        guard let info: PKInfo = decodeModel(jsonStr: message.content) else { return }
        aui_info("onNewInviteDidReceived message: \(message.content)", tag: "PKService")
        respDelegates.allObjects.forEach { delegate in
            delegate.onPKInfoDidReceive(channelName: channelName, info: info)
        }
    }
}

//MARK: RoomPresenceProtocol
extension PKService: RoomPresenceProtocol {
    public func onUserSnapshot(channelName: String, userList: [RoomPresenceInfo]) {
        
    }
    
    public func onUserUpdate(channelName: String, user: RoomPresenceInfo) {
        aui_info("onUserUpdate[\(user.roomId)] userId: \(user.ownerId), name: \(user.ownerName), status: \(user.status.rawValue)", tag: "PKService")
        let channelName = self.channelName
        // 被PK方 -> 发起PK方
        if user.roomId != channelName {
            if let currInfo = roomPresenceService.getRoomPresenceInfo(roomId: channelName),
               user.status == .pk,
               currInfo.status == .idle,
               user.interactorId == AUIRoomContext.shared.currentUserInfo.userId {
                aui_info("after acceptPK >> updateRoomPresenceInfo : status=\(InteractionType.pk.rawValue), interactorId=\(user.ownerId), interactorName=\(user.ownerName)", tag: "PKService")
                roomPresenceService.updateRoomPresenceInfo(roomId: channelName,
                                                           status: .pk,
                                                           interactorId: user.ownerId,
                                                           interactorName: user.ownerName,
                                                           completion: nil)
            }
        }
        
        // 发起PK方 -> 被PK方
        if user.roomId != channelName {
            if let currInfo = roomPresenceService.getRoomPresenceInfo(roomId: channelName),
               user.status == .pk,
               currInfo.status == .pk,
               currInfo.interactorId == user.ownerId,
               user.interactorId == currInfo.ownerId {
                aui_info("after acceptPK >> startPKInteraction roomId=\(user.roomId), ownerId=\(user.ownerId), ownerName=\(user.ownerName)", tag: "PKService")
                interactionService.startPKInteraction(roomId: user.roomId,
                                                      userId: user.ownerId,
                                                      userName: user.ownerName,
                                                      completion: nil)
                let info = self.createPKInfo(user, type: .accept)
                respDelegates.allObjects.forEach { delegate in
                    delegate.onPKInfoDidReceive(channelName: channelName, info: info)
                }
            }
        }
        
        // 发起PK方
        if user.roomId == channelName {
            if let pkRoomInfo = roomPresenceService.getRoomPresenceInfoByOwnerId(ownerId:  user.interactorId),
               user.status == .pk,
               pkRoomInfo.status == .pk,
               pkRoomInfo.interactorId == user.ownerId,
               user.interactorId == pkRoomInfo.ownerId {
                aui_info("after acceptPK >> startPKInteraction roomId=\(pkRoomInfo.roomId), ownerId=\(pkRoomInfo.ownerId), ownerName=\(pkRoomInfo.ownerName)", tag: "PKService")
                
                interactionService.startPKInteraction(roomId: pkRoomInfo.roomId,
                                                      userId: pkRoomInfo.ownerId,
                                                      userName: pkRoomInfo.ownerName,
                                                      completion: nil)
                let info = self.createPKInfo(pkRoomInfo, type: .accept)
                respDelegates.allObjects.forEach { delegate in
                    delegate.onPKInfoDidReceive(channelName: channelName, info: info)
                }
            }
        }
        
        
        // 被PK方/发起PK方 有一方关闭即停止PK
        if user.roomId != channelName {
            if let currInfo = roomPresenceService.getRoomPresenceInfo(roomId: channelName),
               user.status == .idle,
               currInfo.status == .pk,
               currInfo.interactorId == user.ownerId {
                aui_info("after stopPK >> stopInteraction", tag: "PKService")
                
                interactionService.stopInteraction { err in
                    aui_info("stopInteraction completion \(err?.localizedDescription ?? "success")")
                }
                
                let info = self.createPKInfo(currInfo, type: .end)
                info.fromUserId = user.ownerId
                info.fromUserName = user.ownerId
                info.fromRoomId = user.ownerName
                respDelegates.allObjects.forEach { delegate in
                    delegate.onPKInfoDidReceive(channelName: channelName, info: info)
                }
            }
        }
        
        // 主播已经和其他人PK
        if user.roomId != channelName {
            if let currInfo = roomPresenceService.getRoomPresenceInfo(roomId: channelName),
               user.status == .pk,
               currInfo.status == .pk,
               currInfo.interactorId == user.ownerId,
               user.interactorId != AUIRoomContext.shared.currentUserInfo.userId {
                aui_info("after startPK >> updateRoomPresenceInfo for pk owner has started interaction with others: status=\(InteractionType.idle.rawValue)", tag: "PKService")
                
                roomPresenceService.updateRoomPresenceInfo(roomId: channelName,
                                                           status: .idle,
                                                           interactorId: "",
                                                           interactorName: "",
                                                           completion: nil)
                
                let info = self.createPKInfo(currInfo, type: .end)
                respDelegates.allObjects.forEach { delegate in
                    delegate.onPKInfoDidReceive(channelName: channelName,info: info)
                }
                
            }
        }
        
    }
    
    public func onUserDelete(channelName: String, user: RoomPresenceInfo) {
        guard let info = interactionService.interactionInfo,
              info.type == .pk,
              info.userId == user.ownerId else { return }
        aui_info("onUserDelete ownerId: \(user.ownerId) ownerName: \(user.ownerName) roomId: \(user.roomId)", tag: "PKService")
        interactionService.stopInteraction(completion: nil)
        roomPresenceService.updateRoomPresenceInfo(roomId: channelName,
                                                   status: .idle,
                                                   interactorId: "", 
                                                   interactorName: "",
                                                   completion: nil)
    }
    
    public func onUserError(channelName: String, error: NSError) {
        
    }
}
