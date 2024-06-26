//
//  InvitationService.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/6/7.
//

import Foundation

private let key = "invitation"

@objc public enum InvitationType: Int, Codable {
    case inviting = 0
    case accept = 1
    case reject = 2
}

@objc public class InvitationInfo: NSObject, Codable {
    public var id: String = UUID().uuidString
    public var userId: String = ""
    public var userName: String = ""
    public var type: InvitationType = .inviting
    
    enum CodingKeys: String, CodingKey {
        case id, userId, userName, type
    }
}

@objc public protocol InvitationServiceProtocol: NSObjectProtocol {
    func onInvitationDidReceive(channelName: String, info: InvitationInfo)
}

public class InvitationService: NSObject {
    
    private var channelName: String
    private var syncManager: AUISyncManager
    private var interactionService: InteractionService
    
    private var respDelegates = NSHashTable<InvitationServiceProtocol>.weakObjects()
    
    private lazy var messageManager: InviteMessageManager = {
        let manager = InviteMessageManager(channelName: channelName, key: key, rtmManager: syncManager.rtmManager)
        return manager
    }()
    
    deinit {
        aui_info("deinit InvitationService[\(channelName)]", tag: "InvitationService")
        innerUnsubscribe()
    }
    
    required init(channelName: String, 
                  syncManager: AUISyncManager,
                  interactionService: InteractionService) {
        aui_info("init InvitationService[\(channelName)]", tag: "InvitationService")
        self.channelName = channelName
        self.syncManager = syncManager
        self.interactionService = interactionService
        super.init()
        innerSubscribe()
    }
    
    private func innerSubscribe() {
        messageManager.subscribe(delegate: self)
    }
    
    private func innerUnsubscribe() {
        messageManager.unsubscribe(delegate: self)
    }
    
    private func createInviteInfo(userId: String, type: InvitationType) -> InvitationInfo? {
        guard let userList = syncManager.getScene(channelName: channelName)?.userService.userList,
              let user = userList.first(where: { $0.userId == userId}) else {
            return nil
        }
        
        let invitInfo = InvitationInfo()
        invitInfo.type = type
        invitInfo.userId = userId
        invitInfo.userName = user.userName
        
        return invitInfo
    }
    
    private func getInviteMessage(invitationId: String) -> InviteMessageInfo? {
        let message = messageManager.getMessage({ $0.content.contains(invitationId)})
        
        return message
    }
    
    private func createInviteInfo(messageInfo: InviteMessageInfo, type: InvitationType) -> InvitationInfo?  {
        let invitInfo: InvitationInfo? = decodeModel(jsonStr: messageInfo.content)
        invitInfo?.type = type
        
        return invitInfo
    }
}

//MARK: public
extension InvitationService {
    public func subscribe(delegate: InvitationServiceProtocol) {
        if respDelegates.contains(delegate) {
            return
        }
        
        respDelegates.add(delegate)
    }
    
    public func unsubscribe(delegate: InvitationServiceProtocol) {
        respDelegates.remove(delegate)
    }
    
    public func sendInvitation(userId: String, completion: ((NSError?)->())?) {
        guard let invitInfo = createInviteInfo(userId: userId, type: .inviting),
              let content = encodeModelToJsonStr(invitInfo) else {
            aui_info("sendInvitation userId: \(userId)", tag: "InvitationService")
            completion?(NSError(domain: "sendInvitation fail", code: -1, userInfo: nil))
            return
        }
        
        aui_info("sendInvitation userId: \(userId)", tag: "InvitationService")
        messageManager.sendMessage(content: content, userId: userId) { err in
            aui_info("sendInvitation userId: \(userId) completion: \(err?.localizedDescription ?? "success")", tag: "InvitationService")
            completion?(err)
        }
    }
    
    public func acceptInvitation(invitationId: String, completion: ((NSError?)->())?) {
        guard let invitMessage = getInviteMessage(invitationId: invitationId),
              let invitInfo = createInviteInfo(messageInfo: invitMessage, type: .accept),
              let content = encodeModelToJsonStr(invitInfo) else {
            aui_info("acceptInvitation invitationId: \(invitationId) fail", tag: "InvitationService")
            completion?(NSError(domain: "acceptInvitation fail", code: -1, userInfo: nil))
            return
        }
        
        aui_info("acceptInvitation invitationId: \(invitationId)", tag: "InvitationService")
        messageManager.sendMessage(content: content, userId: invitMessage.publisherId) {[weak self] err in
            aui_info("acceptInvitation invitationId: \(invitationId) completion: \(err?.localizedDescription ?? "success")", tag: "InvitationService")
            if err == nil {
                self?.messageManager.removeMessages(filter: { $0.publisherId == invitMessage.publisherId})
            }
            completion?(err)
        }
        
        interactionService.startLinkingInteraction(userId: invitInfo.userId) { err in
            aui_info("startLinkingInteraction: \(invitationId) completion: \(err?.localizedDescription ?? "success")", tag: "InvitationService")
        }
    }
    
    public func rejectInvitation(invitationId: String, completion: ((NSError?)->())?) {
        guard let invitMessage = getInviteMessage(invitationId: invitationId),
              let invitInfo = createInviteInfo(messageInfo: invitMessage, type: .accept),
              let content = encodeModelToJsonStr(invitInfo)  else {
            aui_info("rejectInvitation invitationId: \(invitationId) fail", tag: "InvitationService")
            completion?(NSError(domain: "rejectInvitation fail", code: -1, userInfo: nil))
            return
        }

        aui_info("rejectInvitation invitationId: \(invitationId)", tag: "InvitationService")
        messageManager.sendMessage(content: content, userId:  invitMessage.publisherId) {[weak self] err in
            aui_info("rejectInvitation invitationId: \(invitationId) completion: \(err?.localizedDescription ?? "success")", tag: "InvitationService")
            if err == nil {
                self?.messageManager.removeMessages(filter: { $0.publisherId == invitMessage.publisherId})
            }
            completion?(err)
        }
    }
}

//MARK: InviteMessageProtocol
extension InvitationService: InviteMessageProtocol {
    public func onNewInviteDidReceived(channelName: String, message: InviteMessageInfo) {
        guard let info: InvitationInfo = decodeModel(jsonStr: message.content) else { return }
        aui_info("onNewInviteDidReceived: \(message.content)", tag: "InvitationService")
        if info.type != .inviting {
            messageManager.removeMessages { $0.publisherId == message.publisherId}
        }
        respDelegates.allObjects.forEach { delegate in
            delegate.onInvitationDidReceive(channelName: channelName, info: info)
        }
    }
}
