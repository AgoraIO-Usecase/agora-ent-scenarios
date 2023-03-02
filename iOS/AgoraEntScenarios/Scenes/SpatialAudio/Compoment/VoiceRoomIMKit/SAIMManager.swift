//
//  SAIMManager.swift
//  Pods-VoiceRoomIMKit_Tests
//
//  Created by 朱继超 on 2022/9/1.
//

import AgoraChat
import Foundation
import KakaJSON

public let SARoomGift = "chatroom_gift"
public let SAPraise = "chatroom_praise" // like 点赞
public let SAInviteSite = "chatroom_inviteSiteNotify"
public let SACancelInviteSite = "chatroom_inviteRefusedNotify"
public let SAApplySite = "chatroom_applySiteNotify"
public let SASubmitApplySite = "chatroom_submitApplySiteNotify"
public let SACancelApplySite = "chatroom_submitApplySiteNotifyCancel"
public let SADeclineApply = "chatroom_applyRefusedNotify"
public let SAUpdateRobotVolume = "chatroom_updateRobotVolume"
public let SAJoinedMember = "chatroom_join"

@objc public protocol SAIMDelegate: NSObjectProtocol {
    /// Description you'll call login api,when you receive this message
    /// - Parameter code: AgoraChatErrorCode
    func chatTokenWillExpire(code: AgoraChatErrorCode)
    /// Description receive text message
    /// - Parameters:
    ///   - roomId: AgoraChat's uid
    ///   - message: SAChatEntity
    func receiveTextMessage(roomId: String, message: SAChatEntity)

    func receiveGift(roomId: String, meta: [String: String]?)

    func receiveApplySite(roomId: String, meta: [String: String]?)

    func receiveCancelApplySite(roomId: String, chat_uid: String)

    func receiveInviteSite(roomId: String, meta: [String: String]?)

    func refuseInvite(roomId: String,chat_uid: String, meta: [String: String]?)

    func userJoinedRoom(roomId: String, username: String, ext: [String: Any]?)

    func announcementChanged(roomId: String, content: String)

    func userBeKicked(roomId: String, reason: AgoraChatroomBeKickedReason)

    func roomAttributesDidUpdated(roomId: String, attributeMap: [String: String]?, from fromId: String)

    func memberLeave(roomId: String, userName: String)
}

fileprivate let once = SAIMManager()

@objc public class SAIMManager: NSObject, AgoraChatManagerDelegate, AgoraChatroomManagerDelegate, AgoraChatClientDelegate {
    
    public var currentRoomId = ""
    
    @objc public static var shared: SAIMManager? = once

    @objc public weak var delegate: SAIMDelegate?

    @objc public func configIM(appkey: String) -> AgoraChatError? {
        let options = AgoraChatOptions(appkey: appkey.isEmpty ? "easemob-demo#easeim" : appkey)
        options.enableConsoleLog = true
        options.isAutoLogin = false
//        options.setValue(false, forKeyPath: "enableDnsConfig")
//        options.setValue(6717, forKeyPath: "chatPort")
//        options.setValue("https://a1.chat.agora.io", forKeyPath: "chatServer")
        options.setValue("https://a1.chat.agora.io", forKeyPath: "restServer")
        return AgoraChatClient.shared().initializeSDK(with: options)
    }

    @objc public func loginIM(userName: String, token: String, completion: @escaping (String, AgoraChatError?) -> Void) {
        if AgoraChatClient.shared().isLoggedIn {
            completion(AgoraChatClient.shared().currentUsername ?? "", nil)
        } else {
            AgoraChatClient.shared().login(withUsername: userName, token: token, completion: completion)
        }
    }
    
    @objc func logoutIM() {
        AgoraChatClient.shared().logout(false)
    }

    @objc public func addChatRoomListener() {
        AgoraChatClient.shared().add(self, delegateQueue: .main)
        AgoraChatClient.shared().chatManager?.add(self, delegateQueue: .main)
        AgoraChatClient.shared().roomManager?.add(self, delegateQueue: .main)
    }
    

    @objc public func removeListener() {
        AgoraChatClient.shared().roomManager?.remove(self)
        AgoraChatClient.shared().chatManager?.remove(self)
    }
}

public extension SAIMManager {
    // MARK: - AgoraChatClientDelegate

    func tokenWillExpire(_ aErrorCode: AgoraChatErrorCode) {
        if delegate != nil, delegate!.responds(to: #selector(SAIMDelegate.chatTokenWillExpire(code:))) {
            self.delegate?.chatTokenWillExpire(code: aErrorCode)
        }
    }
    
    func getItem(dic: [String: String], join: Bool) -> SAChatEntity {
        let item = SAChatEntity()
        item.userName = dic["userName"]
        item.content = dic["content"]
        item.joined = join
        item.attributeContent = item.attributeContent
        item.width = item.width
        item.height = item.height
        return item
    }

    // MARK: - AgoraChatManagerDelegate
    func messagesDidReceive(_ aMessages: [AgoraChatMessage]) {
        for message in aMessages {
            if message.body is AgoraChatTextMessageBody {
                if delegate != nil, delegate!.responds(to: #selector(SAIMDelegate.receiveTextMessage(roomId:message:))) {
                    if let body = message.body as? AgoraChatTextMessageBody, let userName = message.ext?["userName"] as? String {
                        let dic = ["userName": userName, "content": body.text]
                        let entity = self.getItem(dic: dic, join: false)
                        self.delegate?.receiveTextMessage(roomId: self.currentRoomId, message: entity)
                    }
                }
                continue
            }
            if let body = message.body as? AgoraChatCustomMessageBody {
                if delegate != nil {
                    switch body.event {
                    case SARoomGift:
                        if delegate!.responds(to: #selector(SAIMDelegate.receiveGift(roomId:meta:))) {
                            self.delegate?.receiveGift(roomId: self.currentRoomId, meta: body.customExt)
                        }
                    case SAInviteSite:
                        if delegate!.responds(to: #selector(SAIMDelegate.receiveInviteSite(roomId:meta:))) {
                            self.delegate?.receiveInviteSite(roomId: self.currentRoomId, meta: body.customExt)
                        }
                    case SACancelApplySite:
                        if delegate!.responds(to: #selector(SAIMDelegate.receiveCancelApplySite(roomId:chat_uid:))) {
                            guard let chatRoomId = body.customExt?["chatroomId"] else {return}
                            if chatRoomId != self.currentRoomId {return}
                            self.delegate?.receiveCancelApplySite(roomId: self.currentRoomId,chat_uid: message.from)
                        }
                    case SASubmitApplySite:
                        if delegate!.responds(to: #selector(SAIMDelegate.receiveApplySite(roomId:meta:))) {
                            self.delegate?.receiveApplySite(roomId: self.currentRoomId, meta: body.customExt)
                        }
                    case SACancelInviteSite:
                        if delegate!.responds(to: #selector(SAIMDelegate.refuseInvite(roomId:chat_uid:meta:))) {
                            self.delegate?.refuseInvite(roomId: self.currentRoomId, chat_uid: message.from, meta: body.customExt)
                        }
                    case SAJoinedMember:
                        if delegate!.responds(to: #selector(SAIMDelegate.userJoinedRoom(roomId:username:ext:))) {
                            if let ext = body.customExt["user"], let user = model(from: ext, SAUser.self) {
                                self.delegate?.userJoinedRoom(roomId: message.to, username: user.name ?? "", ext: body.customExt)
                            }
                        }
                    default:
                        break
                    }
                }
            }
        }
    }

    // MARK: - AgoraChatroomManagerDelegate
    func chatroomAnnouncementDidUpdate(_ aChatroom: AgoraChatroom, announcement aAnnouncement: String?) {
        if delegate != nil, delegate!.responds(to: #selector(SAIMDelegate.announcementChanged(roomId:content:))) {
            if let roomId = aChatroom.chatroomId, let announcement = aAnnouncement, roomId == self.currentRoomId {
                self.delegate?.announcementChanged(roomId: roomId, content: announcement)
            }
        }
    }

    func didDismiss(from aChatroom: AgoraChatroom, reason aReason: AgoraChatroomBeKickedReason) {
        if delegate != nil, delegate!.responds(to: #selector(SAIMDelegate.userBeKicked(roomId:reason:))) {
            if let roomId = aChatroom.chatroomId, roomId == self.currentRoomId {
                self.delegate?.userBeKicked(roomId: roomId, reason: aReason)
            }
        }
        switch aReason {
        case .beRemoved, .destroyed:
            if let roomId = aChatroom.chatroomId, roomId == currentRoomId {
                currentRoomId = ""
            }
        default:
            break
        }
        removeListener()
        AgoraChatClient.shared().logout(false)
    }

    func chatroomAttributesDidUpdated(_ roomId: String, attributeMap: [String: String]?, from fromId: String) {
        if delegate != nil, delegate!.responds(to: #selector(SAIMDelegate.roomAttributesDidUpdated(roomId:attributeMap:from:))), roomId == currentRoomId {
            self.delegate?.roomAttributesDidUpdated(roomId: roomId, attributeMap: attributeMap, from: fromId)
        }
    }

    func userDidLeave(_ aChatroom: AgoraChatroom, user aUsername: String) {
        if self.delegate != nil, self.delegate!.responds(to: #selector(SAIMDelegate.memberLeave(roomId:userName:))), aChatroom.chatroomId == currentRoomId {
            self.delegate?.memberLeave(roomId: self.currentRoomId, userName: aUsername)
        }
    }

    // MARK: - Send
    @objc func sendMessage(roomId: String, text: String, ext: [AnyHashable: Any]?, completion: @escaping (AgoraChatMessage?, AgoraChatError?) -> Void) {
        let message = AgoraChatMessage(conversationID: roomId, body: AgoraChatTextMessageBody(text: text), ext: ext)
        message.chatType = .chatRoom
        AgoraChatClient.shared().chatManager?.send(message, progress: nil, completion: completion)
    }

    @objc func sendCustomMessage(roomId: String, event: String, customExt: [String: String], completion: @escaping (AgoraChatMessage?, AgoraChatError?) -> Void) {
        let message = AgoraChatMessage(conversationID: roomId, body: AgoraChatCustomMessageBody(event: event, customExt: customExt), ext: nil)
        message.chatType = .chatRoom
        AgoraChatClient.shared().chatManager?.send(message, progress: nil, completion: completion)
    }
    
    @objc func sendChatCustomMessage(to_uid: String, event: String, customExt: [String: String], completion: @escaping (AgoraChatMessage?, AgoraChatError?) -> Void) {
        let message = AgoraChatMessage(conversationID: to_uid, body: AgoraChatCustomMessageBody(event: event, customExt: customExt), ext: nil)
        message.chatType = .chat
        AgoraChatClient.shared().chatManager?.send(message, progress: nil, completion: completion)
    }

    @objc func joinedChatRoom(roomId: String, completion: @escaping ((AgoraChatroom?, AgoraChatError?) -> Void)) {
        AgoraChatClient.shared().roomManager?.joinChatroom(roomId, completion: { room, error in
            if error == nil, let id = room?.chatroomId {
                self.currentRoomId = id
            }
            completion(room, error)
        })
    }

    @objc func userQuitRoom(completion: ((AgoraChatError?) -> Void)?) {
        AgoraChatClient.shared().roomManager?.leaveChatroom(currentRoomId, completion: { error in
            if error == nil {
                AgoraChatClient.shared().roomManager?.remove(self)
                AgoraChatClient.shared().chatManager?.remove(self)
                self.currentRoomId = ""
            }
            if completion != nil {
                completion!(error)
            }
        })
        self.removeListener()
        AgoraChatClient.shared().logout(false)
    }
    
    func userDestroyedChatroom() {
        AgoraChatClient.shared().roomManager?.destroyChatroom(self.currentRoomId)
    }

    func fetchMembersCount(roomId: String, completion: @escaping (Int) -> Void) {
        AgoraChatClient.shared().roomManager?.getChatroomSpecificationFromServer(withId: roomId, fetchMembers: true, completion: { room, error in
            completion(room?.occupantsCount ?? 0)
        })
    }
    
    func setChatroomAttributes(attributes: Dictionary<String,String>, completion: @escaping (AgoraChatError?) -> ()) {
        AgoraChatClient.shared().roomManager?.setChatroomAttributesForced(self.currentRoomId, attributes: attributes, autoDelete: false, completionBlock: { error, errorAttributes in
            completion(error)
        })
    }
    
    func fetchChatroomAttributes(keys:[String],completion: ((AgoraChatError?,[String:String]?) -> ())?) {
        AgoraChatClient.shared().roomManager?.fetchChatroomAttributes(self.currentRoomId, keys: keys,completion: completion)
    }
    
    func updateAnnouncement(content: String,completion: @escaping (Bool) -> Void) {
        AgoraChatClient.shared().roomManager?.updateChatroomAnnouncement(withId: self.currentRoomId, announcement: content,completion: { room, error in
            completion(error == nil)
        })
    }
}
