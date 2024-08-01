//
//  AUIIMServiceImplement.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/17.
//implement

import Foundation
import AgoraChat
import YYModel
import AgoraCommon

private let kChatAttrKey = "chatRoom"
private let kChatIdKey = "chatRoomId"
fileprivate let AUIChatRoomJoinedMember = "AUIChatRoomJoinedMember"

@objcMembers open class AUIIMManagerServiceImplement: NSObject {
    private var userId: String = ""
    private var chatToken: String = ""
    private var currentRoomId: String = ""
    
    private var responseDelegates: NSHashTable<AUIMManagerRespDelegate> = NSHashTable<AUIMManagerRespDelegate>.weakObjects()
    
    
    private var currentUser:AUIChatUserInfo {
        return AUIChatContext.shared.currentUserInfo
    }
    
    public override init() {
        super.init()
        
        aui_info("init AUIIMManagerServiceImplement", tag: "AUIIMManagerServiceImplement")
    }
    /// Description judge login state
    private var isLogin: Bool {
        AgoraChatClient.shared().isLoggedIn
    }
    
    private func loginAndJoinChatRoom() {
        guard !self.currentRoomId.isEmpty, !self.chatToken.isEmpty else { return }
        self.login {[weak self] error in
            guard let self = self else { return }
            if error == nil {
                self.joinedChatRoom(roomId: self.currentRoomId) { message, error in
                    aui_info("joinedChatRoom:\(error == nil ? "successful!" : "\(error!.localizedDescription)")")
                }
            }
            aui_info("IM.onAttributesDidChanged login:\(error == nil ? "successful!":"failed! error = \(error!.localizedDescription)")")
        }
    }
 
    /// Description 退出登录IMSDK
    private func logout() {
        AgoraChatClient.shared().logout(false)
    }
    
    private func mapError(error: AgoraChatError?) -> NSError? {
        return error != nil ? AUICommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError():nil
    }
    
    private func addChatRoomListener() {
        AgoraChatClient.shared().add(self, delegateQueue: .main)
        AgoraChatClient.shared().chatManager?.add(self, delegateQueue: .main)
        AgoraChatClient.shared().roomManager?.add(self, delegateQueue: .main)
    }

    private func removeListener() {
        AgoraChatClient.shared().removeDelegate(self)
        AgoraChatClient.shared().roomManager?.remove(self)
        AgoraChatClient.shared().chatManager?.remove(self)
    }
    
    private func joinChatRoom(completion: @escaping (NSError?) -> Void) {
        AgoraChatClient.shared().roomManager?.joinChatroom(self.currentRoomId, completion: { chatRoom, error in
            completion(self.mapError(error: error))
        })
    }
    
    deinit {
        removeListener()
        aui_info("deinit AUIIMManagerServiceImplement", tag: "AUIIMManagerServiceImplement")
    }
}


//MARK: - AUIMManagerServiceDelegate
extension AUIIMManagerServiceImplement: AUIMManagerServiceDelegate {
    public func bindRespDelegate(delegate: AUIMManagerRespDelegate) {
        self.responseDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIMManagerRespDelegate) {
        self.responseDelegates.remove(delegate)
    }
    
    public func loginChat(completion: @escaping (NSError?) -> Void) {
        getUserIdAndToken { [weak self] err in
            guard let self = self else {return}
            if let err = err {
                completion(err)
                return
            }
            self.login(completion: completion)
        }
        
    }
    
    public func logoutChat(completion: @escaping (NSError?) -> Void) {
        logout()
        completion(nil)
    }
    
    public func createChatRoom(roomId: String,
                               description: String,
                               completion: @escaping (String?, NSError?) -> Void) {
        loginChat { err in
            if let err = err {
                completion(nil, err)
                return
            }
            NetworkManager.shared.generateIMConfig(type: 2,
                                                   channelName: "",
                                                   nickName: VLUserCenter.user.name,
                                                   chatId: "",
                                                   imUid: VLUserCenter.user.id,
                                                   password: "12345678",
                                                   uid:  VLUserCenter.user.id) { [weak self] uid, room_id, token in
                guard let self = self else {return}
                guard let uid = uid, let token = token else {
                    completion(nil, NSError(domain: "room id generate fail", code: -1))
                    return
                }
                self.currentRoomId = room_id ?? ""
                completion(room_id, nil)
            }
        }
    }
    
    public func leaveChatRoom(completion: ((NSError?) -> Void)?) {
        if !self.isLogin {
            if completion != nil {
                completion!(AUICommonError.httpError(400, "please login first.").toNSError())
            } else {
                aui_error("quitChatroom failed! please login first.")
            }
            return
        }
        AgoraChatClient.shared().roomManager?.leaveChatroom(self.currentRoomId ?? "", completion:nil)
        self.currentRoomId = ""
        self.removeListener()
        self.logout()
     }

    public func destroyChatRoom() {
        if !self.isLogin {
            aui_error("destroyChatroom failed! please login first.")
            return
        }
        AgoraChatClient.shared().roomManager?.destroyChatroom(self.currentRoomId ?? "")
        self.removeListener()
        self.logout()
        self.currentRoomId = ""
    }

    public func sendMessage(roomId: String, text: String, completion: @escaping (AgoraChatTextMessage?, NSError?) -> Void) {
        if !self.isLogin {
            completion(nil, AUICommonError.httpError(400, "please login first.").toNSError())
            return
        }
        let message = AgoraChatMessage(conversationID: self.currentRoomId, body: AgoraChatTextMessageBody(text: text), ext: ["user": currentUser.yy_modelToJSONString() ?? ""])
        message.chatType = .chatRoom
        AgoraChatClient.shared().chatManager?.send(message, progress: nil) { message, error in
            guard let responseMessage = message else { return }
            completion(self.convertTextMessage(message: responseMessage,receive: false), self.mapError(error: error))
        }
    }
 
    public func joinChatRoom(roomId: String, completion: @escaping ((AgoraChatTextMessage?, NSError?) -> Void)) {
        self.addChatRoomListener()
        self.joinChatRoom { error in
            if error == nil {
                if !self.isLogin {
                    completion(nil, AUICommonError.httpError(400, "please login first.").toNSError())
                    return
                }
                let message = AgoraChatMessage(conversationID: self.currentRoomId,
                                               body: AgoraChatCustomMessageBody(event: AUIChatRoomJoinedMember,
                                                                                customExt: ["user" : self.currentUser.yy_modelToJSONString() ?? ""]),
                                               ext: nil)
                message.chatType = .chatRoom
                AgoraChatClient.shared().chatManager?.send(message, progress: nil, completion: { message, error in
                    var textMessage: AgoraChatTextMessage?
                    if error == nil {
                        guard let responseMessage = message else { return }
                        textMessage = AgoraChatTextMessage()
                        textMessage?.messageId = responseMessage.messageId
                        textMessage?.content = aui_localized("Joined")
                        textMessage?.user = self.currentUser
                        for del in self.responseDelegates.allObjects {
                            del.onUserDidJoinRoom(roomId: self.currentRoomId, message:  textMessage ?? AgoraChatTextMessage())
                        }
                        self.currentRoomId = roomId
                    }
                    completion(textMessage, self.mapError(error: error))
                })
            } else {
                completion(nil,error)
            }
        }
    }
 
    private func convertTextMessage(message: AgoraChatMessage,receive: Bool) -> AgoraChatTextMessage {
        let body = message.body as! AgoraChatTextMessageBody
        let textMessage = AgoraChatTextMessage()
        textMessage.messageId = message.messageId
        textMessage.content = body.text
        if receive {
            if let jsonString = message.ext?["user"] as? String {
                textMessage.user = AUIChatUserInfo.yy_model(with: jsonString.a.jsonToDictionary() )
            }
        } else {
            textMessage.user = self.currentUser
        }
        return textMessage
    }
}

extension AUIIMManagerServiceImplement {
    
    /// 创建用户并获取token
    /// - Parameter completion: <#completion description#>
    private func getUserIdAndToken(completion:@escaping ((NSError?)->())) {
        guard chatToken.isEmpty || userId.isEmpty else {
            completion(nil)
            return
        }
        NetworkManager.shared.generateIMConfig(type: 1,
                                               channelName: "",
                                               nickName: VLUserCenter.user.name,
                                               chatId: "",
                                               imUid: userId,
                                               password: "12345678",
                                               uid:  VLUserCenter.user.id){ [weak self] uid, room_id, token in
            guard let self = self else {return}
            guard let uid = uid, let token = token else {
                completion(NSError(domain: "token generate fail", code: -1))
                return
            }
            self.chatToken = token
            self.userId = uid
//            self.login(completion: completion)
            completion(nil)
        }
    }
    
    
    /// Description  登录IMSDK
    /// - Parameters:
    ///   - completion: 回调
    private func login(completion: @escaping (NSError?) -> Void) {
        if isLogin {
            completion(nil)
            return
        }
        
        AgoraChatClient.shared().login(withUsername: userId, token: chatToken) {[weak self] _, error in
            guard let self = self else {return}
            if let err = error {
                aui_warn("login fail: userId: \(self.userId) error: \(err.errorDescription ?? "")")
            }
            completion(error == nil ? nil : AUICommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError())
        }
    }
}

//MARK: - AgoraChat Delegate
extension AUIIMManagerServiceImplement: AgoraChatManagerDelegate, AgoraChatroomManagerDelegate, AgoraChatClientDelegate {
    public func messagesDidReceive(_ aMessages: [AgoraChatMessage]) {
        for message in aMessages {
            if message.body is AgoraChatTextMessageBody {
                for response in self.responseDelegates.allObjects {
                    response.messageDidReceive(roomId: self.currentRoomId, message: self.convertTextMessage(message: message,receive: true))
                }
                continue
            }
            if let body = message.body as? AgoraChatCustomMessageBody {
                switch body.event {
                case AUIChatRoomJoinedMember:
                    for response in self.responseDelegates.allObjects {
                        if let ext = body.customExt["user"], !ext.isEmpty{
                            let user = AUIChatUserInfo.yy_model(with: ext.a.jsonToDictionary())
                            let textMessage = AgoraChatTextMessage()
                            textMessage.messageId = message.messageId
                            textMessage.content = aui_localized("Joined")
                            textMessage.user = user
                            response.onUserDidJoinRoom(roomId: message.to, message: textMessage)
                        }
                    }
                default:
                    break
                }
            }
        }
    }
    
    public func didDismiss(from aChatroom: AgoraChatroom, reason aReason: AgoraChatroomBeKickedReason) {
//        AUIToast.show(text: "You were kicked out of the chatroom".a.localize(type: .chat))
    }
}


