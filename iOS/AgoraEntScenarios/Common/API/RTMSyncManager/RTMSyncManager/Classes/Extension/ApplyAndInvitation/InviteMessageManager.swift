//
//  InviteMessageManager.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/6/7.
//

import Foundation

/**
 * 消息持有者。用于管理消息的发送和接收，以及消息的缓存。消息结构如下：
 * {"id":"","content":"{\"id\":\"123\",\"content\":\"hello\"}","key":"xxx","publisherId":"xxx"}
 *
 * @param T 消息类型
 * @property rtmManager RTM管理器
 * @property channelName 频道名称
 * @property type 消息类型
 */

@objc public class InviteMessageInfo: NSObject, Codable {
    public var id: String = UUID().uuidString
    public var key: String = ""
    public var channelName: String = ""
    public var publisherId: String = ""
    public var content: String = ""
    
    enum CodingKeys: String, CodingKey {
        case id, key, channelName, publisherId, content
    }
}

@objc public protocol InviteMessageProtocol: NSObjectProtocol {
    func onNewInviteDidReceived(channelName: String, message: InviteMessageInfo)
}

public class InviteMessageManager: NSObject {
    private var rtmManager: AUIRtmManager
    private var channelName: String
    private var key: String
    private var messageList: [InviteMessageInfo] = []
    private var respDelegates = NSHashTable<InviteMessageProtocol>.weakObjects()
    
    deinit {
        aui_info("deinit InviteMessageManager[\(channelName)]", tag: "InviteMessageManager")
        innerUnsubscribe()
    }
    
    required init(channelName: String,
                  key: String,
                  rtmManager: AUIRtmManager) {
        aui_info("init InviteMessageManager[\(channelName)]", tag: "InviteMessageManager")
        self.channelName = channelName
        self.key = key
        self.rtmManager = rtmManager
        super.init()
        innerSubscribe()
    }
    
    private func innerSubscribe() {
        rtmManager.subscribeUser(channelName: channelName, delegate: self)
        rtmManager.subscribeMessage(channelName: channelName, delegate: self)
    }
    
    private func innerUnsubscribe() {
        rtmManager.unsubscribeUser(channelName: channelName, delegate: self)
        rtmManager.unsubscribeMessage(channelName: channelName, delegate: self)
    }
    
    private func insertMessage(msg: InviteMessageInfo, notify: Bool = false) {
        if let _ = messageList.first(where: { $0.id == msg.id }) {return}
        
        messageList.append(msg)
        guard notify else {return}
        self.respDelegates.allObjects.forEach { delegate in
            delegate.onNewInviteDidReceived(channelName: channelName, message: msg)
        }
    }
}

//MARK: public method
extension InviteMessageManager {
    public func subscribe(delegate: InviteMessageProtocol) {
        if respDelegates.contains(delegate) {
            return
        }
        
        respDelegates.add(delegate)
    }
    
    public func unsubscribe(delegate: InviteMessageProtocol) {
        respDelegates.remove(delegate)
    }
    
    public func sendMessage(content: String,
                            userId: String,
                            channelName: String? = nil,
                            completion: ((NSError?)-> ())?) {
        let model = InviteMessageInfo()
        model.key = key
        model.channelName = channelName ?? self.channelName
        model.publisherId = AUIRoomContext.shared.currentUserInfo.userId
        model.content = content
        let messageStr = encodeModelToJsonStr(model) ?? ""
        aui_info("sendMessage userId: \(userId) content: \(content)", tag: "InviteMessageManager")
        rtmManager.publish(userId: userId,
                           channelName: model.channelName,
                           message: messageStr) { err in
            aui_info("sendMessage userId: \(userId) completion: \(err?.localizedDescription ?? "success")", tag: "InviteMessageManager")
            if err == nil {
                self.insertMessage(msg: model)
            }
            completion?(err)
        }
    }
    
    public func getMessage(_ filter: (InviteMessageInfo)->Bool) -> InviteMessageInfo? {
        aui_info("getMessage with filter", tag: "InviteMessageManager")
        return messageList.first(where: filter)
    }
    
    public func removeMessage(id: String) {
        aui_info("removeMessage id: \(id)", tag: "InviteMessageManager")
        messageList.removeAll { $0.id == id}
    }
    
    public func removeMessages(filter: ((InviteMessageInfo)->Bool)?) {
        aui_info("onUserDidLeaved with filter", tag: "InviteMessageManager")
        if let filter = filter {
            messageList.removeAll(where: filter)
        } else {
            messageList.removeAll()
        }
    }
}

extension InviteMessageManager: AUIRtmUserProxyDelegate {
    public func onCurrentUserJoined(channelName: String) {
        
    }
    
    public func onUserSnapshotRecv(channelName: String, userId: String, userList: [[String : Any]]) {
        
    }
    
    public func onUserDidJoined(channelName: String, userId: String, userInfo: [String : Any]) {
        
    }
    
    public func onUserDidLeaved(channelName: String, userId: String, userInfo: [String : Any], reason: AUIRtmUserLeaveReason) {
        aui_info("onUserDidLeaved userId: \(userId)", tag: "InviteMessageManager")
        removeMessages { $0.publisherId == userId }
    }
    
    public func onUserDidUpdated(channelName: String, userId: String, userInfo: [String : Any]) {
        
    }
}

extension InviteMessageManager: AUIRtmMessageProxyDelegate {
    public func onMessageReceive(publisher: String, channelName: String, message: String) {
        guard let messageInfo: InviteMessageInfo = decodeModel(jsonStr: message),
              key == messageInfo.key,
              self.channelName == messageInfo.channelName else {
            return
        }
        aui_info("onMessageReceive message: \(message)", tag: "InviteMessageManager")
        insertMessage(msg: messageInfo, notify: true)
    }
}
