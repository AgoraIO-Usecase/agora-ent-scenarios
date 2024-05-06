//
//  AUIRtmMsgProxy.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation
//import AgoraRtcKit
import AgoraRtmKit

@objc public protocol AUIRtmErrorProxyDelegate: NSObjectProtocol {
    
    /// token过期
    /// - Parameter channelName: <#channelName description#>
    @objc optional func onTokenPrivilegeWillExpire(channelName: String?)
    
    /// 网络状态变化
    /// - Parameters:
    ///   - channelName: <#channelName description#>
    ///   - state: <#state description#>
    ///   - reason: <#reason description#>
    @objc optional func onConnectionStateChanged(channelName: String,
                                                 connectionStateChanged state: AgoraRtmClientConnectionState,
                                                 result reason: AgoraRtmClientConnectionChangeReason)
    
    /// 收到的KV为空
    /// - Parameter channelName: <#channelName description#>
    @objc optional func onMsgRecvEmpty(channelName: String)
}

@objc public protocol AUIRtmAttributesProxyDelegate: NSObjectProtocol {
    func onAttributesDidChanged(channelName: String, key: String, value: Any)
}

@objc public protocol AUIRtmMessageProxyDelegate: NSObjectProtocol {
    func onMessageReceive(publisher: String, message: String)
}

@objc public protocol AUIRtmUserProxyDelegate: NSObjectProtocol {
    func onCurrentUserJoined(channelName: String)
    func onUserSnapshotRecv(channelName: String, userId:String, userList: [[String: Any]])
    func onUserDidJoined(channelName: String, userId:String, userInfo: [String: Any])
    func onUserDidLeaved(channelName: String, userId:String, userInfo: [String: Any])
    func onUserDidUpdated(channelName: String, userId:String, userInfo: [String: Any])
//    func onUserBeKicked(channelName: String, userId:String, userInfo: [String: Any])
}

@objc public protocol AUIRtmLockProxyDelegate: NSObjectProtocol {
    func onReceiveLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail)
    func onReleaseLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail)
}


/// RTM消息转发器
open class AUIRtmMsgProxy: NSObject {
    private var rtmChannelType: AgoraRtmChannelType!
    private var attributesDelegates:[String: NSHashTable<AUIRtmAttributesProxyDelegate>] = [:]
    private var attributesCacheAttr: [String: [String: String]] = [:]
    private var lockDelegates: [String: NSHashTable<AUIRtmLockProxyDelegate>] = [:]
    private var lockDetailCaches: [String: [AgoraRtmLockDetail]] = [:]
    private var messageDelegates:NSHashTable<AUIRtmMessageProxyDelegate> = NSHashTable<AUIRtmMessageProxyDelegate>.weakObjects()
    private var userDelegates: NSHashTable<AUIRtmUserProxyDelegate> = NSHashTable<AUIRtmUserProxyDelegate>.weakObjects()
    private var errorDelegates: NSHashTable<AUIRtmErrorProxyDelegate> = NSHashTable<AUIRtmErrorProxyDelegate>.weakObjects()
    
    required public init(rtmChannelType: AgoraRtmChannelType) {
        super.init()
        self.rtmChannelType = rtmChannelType
    }
    
    func cleanCache(channelName: String) {
        attributesCacheAttr[channelName] = nil
    }
    
    func keys(channelName: String) -> [String]? {
        let cache = attributesCacheAttr[channelName]
        return cache?.map { $0.key }
    }
    
    func subscribeAttributes(channelName: String, itemKey: String, delegate: AUIRtmAttributesProxyDelegate) {
        let key = "\(channelName)__\(itemKey)"
        if let value = attributesDelegates[key] {
            if !value.contains(delegate) {
                value.add(delegate)
            }
        }else{
            let weakObjects = NSHashTable<AUIRtmAttributesProxyDelegate>.weakObjects()
            weakObjects.add(delegate)
            attributesDelegates[key] = weakObjects
        }
        let cache = attributesCacheAttr[channelName]
        let item = cache?[itemKey]
        guard let itemData = item?.data(using: .utf8), let itemValue = try? JSONSerialization.jsonObject(with: itemData) else {
            return
        }
        //To ensure that the callback can be correctly received by using async dispatch of changes (such as direct callback but external incomplete forwarding processing)
        DispatchQueue.main.async {
            delegate.onAttributesDidChanged(channelName: channelName, key: itemKey, value: itemValue)
        }
    }
    
    func unsubscribeAttributes(channelName: String, itemKey: String, delegate: AUIRtmAttributesProxyDelegate) {
        let key = "\(channelName)__\(itemKey)"
        guard let value = attributesDelegates[key] else {
            return
        }
        value.remove(delegate)
    }
    
    func subscribeMessage(channelName: String, delegate: AUIRtmMessageProxyDelegate) {
        if messageDelegates.contains(delegate) {
            return
        }
        messageDelegates.add(delegate)
    }
    
    func unsubscribeMessage(channelName: String, delegate: AUIRtmMessageProxyDelegate) {
        messageDelegates.remove(delegate)
    }
    
    func subscribeUser(channelName: String, delegate: AUIRtmUserProxyDelegate) {
        if userDelegates.contains(delegate) {
            return
        }
        userDelegates.add(delegate)
    }
    
    func unsubscribeUser(channelName: String, delegate: AUIRtmUserProxyDelegate) {
        userDelegates.remove(delegate)
    }
    
    func subscribeError(channelName: String, delegate: AUIRtmErrorProxyDelegate) {
        if errorDelegates.contains(delegate) {
            return
        }
        errorDelegates.add(delegate)
    }
    
    func unsubscribeError(channelName: String, delegate: AUIRtmErrorProxyDelegate) {
        errorDelegates.remove(delegate)
    }
    
    func subscribeLock(channelName: String, lockName: String, delegate: AUIRtmLockProxyDelegate) {
        let key = "\(channelName)__\(lockName)"
        if let value = lockDelegates[key] {
            if !value.contains(delegate) {
                value.add(delegate)
            }
        }else{
            let weakObjects = NSHashTable<AUIRtmLockProxyDelegate>.weakObjects()
            weakObjects.add(delegate)
            lockDelegates[key] = weakObjects
        }
        let lockDetails = lockDetailCaches[channelName]
        guard let lockDetail = lockDetails?.first(where: { $0.lockName == lockName }) else {
            return
        }
        
        //To ensure that the callback can be correctly received by using async dispatch of changes (such as direct callback but external incomplete forwarding processing)
        DispatchQueue.main.async {
            delegate.onReceiveLockDetail(channelName: channelName, lockDetail: lockDetail)
        }
    }
    
    func unsubscribeLock(channelName: String, lockName: String, delegate: AUIRtmLockProxyDelegate) {
        let key = "\(channelName)__\(lockName)"
        guard let value = lockDelegates[key] else {
            return
        }
        value.remove(delegate)
    }
    
    func processMetaData(channelName: String, data: AgoraRtmMetadata?) {
        guard let data = data else { return }
        
        var cache = self.attributesCacheAttr[channelName] ?? [:]
        data.getItems().forEach { item in
//            aui_info("\(item.key): \(item.value)", tag: "AUIRtmMsgProxy")
            //判断value和缓存里是否一致，这里用string可能会不准，例如不同终端序列化的时候json obj不同kv的位置不一样会造成生成的json string不同
            if cache[item.key] == item.value {
                aui_info("there are no changes of [\(item.key)] \(item.value)", tag: "AUIRtmMsgProxy")
                return
            }
            cache[item.key] = item.value
            guard let itemData = item.value.data(using: .utf8),
                  let itemValue = try? JSONSerialization.jsonObject(with: itemData) else {
                aui_info("parse itemData fail: \(item.key) \(item.value)", tag: "AUIRtmMsgProxy")
                return
            }
            let delegateKey = "\(channelName)__\(item.key)"
//            aui_info("itemValue: \(item.value)")
            guard let value = self.attributesDelegates[delegateKey] else { return }
            for element in value.allObjects {
                element.onAttributesDidChanged(channelName: channelName, key: item.key, value: itemValue)
            }
        }
        self.attributesCacheAttr[channelName] = cache
        if data.getItems().count > 0 {
            return
        }
        for element in errorDelegates.allObjects {
            element.onMsgRecvEmpty?(channelName: channelName)
        }
    }
}

//MARK: AgoraRtmClientDelegate
extension AUIRtmMsgProxy: AgoraRtmClientDelegate {
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, tokenPrivilegeWillExpire channel: String?) {
        aui_info("onTokenPrivilegeWillExpire: \(channel ?? "")", tag: "AUIRtmMsgProxy")
        
        for element in errorDelegates.allObjects {
            element.onTokenPrivilegeWillExpire?(channelName: channel)
        }
    }
    
    public func rtmKit(_ kit: AgoraRtmClientKit,
                       channel channelName: String,
                       connectionChangedToState state: AgoraRtmClientConnectionState,
                       reason: AgoraRtmClientConnectionChangeReason) {
        aui_info("connectionStateChanged state: \(state.rawValue) reason: \(reason.rawValue)", tag: "AUIRtmMsgProxy")
        if errorDelegates.count <= 0 { return }
        for element in errorDelegates.allObjects {
            element.onConnectionStateChanged?(channelName: channelName,
                                              connectionStateChanged: state,
                                              result: reason)
        }
        
        if reason == .changedRejoinSuccess {
            for element in userDelegates.allObjects {
                element.onCurrentUserJoined(channelName: channelName)
            }
        }
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveStorageEvent event: AgoraRtmStorageEvent) {
        guard event.channelType == rtmChannelType else {
            return
        }
        
        aui_info("didReceiveStorageEvent event: [\(event.target)] channelType: [\(event.channelType.rawValue)] eventType: [\(event.eventType.rawValue)] =======", tag: "AUIRtmMsgProxy")
        //key使用channelType__eventType，保证message channel/stream channel, user storage event/channel storage event共存
        let channelName = event.target
        processMetaData(channelName: channelName, data: event.data)
        aui_info("storage event[\(channelName)] ========", tag: "AUIRtmMsgProxy")
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceivePresenceEvent event: AgoraRtmPresenceEvent) {
        aui_info("[\(event.channelName)] didReceivePresenceEvent event: [\(event.type.rawValue)] channel type: [\(event.channelType.rawValue)]] states: \(event.states.count) =======", tag: "AUIRtmMsgProxy")
        
        guard event.channelType == rtmChannelType else {
            return
        }
        
//        var map: [[]]
        var map: [String: String] = [:]
        event.states.forEach { item in
            map[item.key] = item.value
        }
        let userId = event.publisher ?? ""
        aui_info("presence userId: \(userId) event_type: \(event.type.rawValue) userInfo: \(map)", tag: "AUIRtmMsgProxy")
        if event.type == .remoteJoinChannel {
            if map.count == 0 {
                aui_warn("join user fail, empty: userId: \(userId) \(map)", tag: "AUIRtmMsgProxy")
                return
            }
            
            for element in userDelegates.allObjects {
                element.onUserDidJoined(channelName: event.channelName, userId: userId, userInfo: map)
            }
        } else if event.type == .remoteLeaveChannel || event.type == .remoteConnectionTimeout {
            for element in userDelegates.allObjects {
                element.onUserDidLeaved(channelName: event.channelName, userId: userId, userInfo: map)
            }
        } else if event.type == .remoteStateChanged {
            if map.count == 0 {
                aui_warn("update user fail, empty: userId: \(userId) \(map)", tag: "AUIRtmMsgProxy")
                return
            }
            for element in userDelegates.allObjects {
                element.onUserDidUpdated(channelName: event.channelName, userId: userId, userInfo: map)
            }
        } else if event.type == .snapshot {
            let userList = event.snapshotList()
            for element in userDelegates.allObjects {
                element.onUserSnapshotRecv(channelName: event.channelName, userId: userId, userList: userList)
                element.onCurrentUserJoined(channelName: event.channelName)
            }
        }
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveMessageEvent event: AgoraRtmMessageEvent) {
        aui_info("[\(event.channelName)] didReceiveMessageEvent  =======", tag: "AUIRtmMsgProxy")
        
        if let message = event.message.stringData {
            for element in messageDelegates.allObjects {
                element.onMessageReceive(publisher: event.publisher, message: message)
            }
        } else {
            aui_warn("recv unknown type message", tag: "AUIRtmMsgProxy")
        }
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveLockEvent event: AgoraRtmLockEvent) {
        aui_info("didReceiveLockEvent[\(event.channelName)]: type: \(event.eventType.rawValue) count: \(event.lockDetailList.count)")
        
        var addLockDetails: [AgoraRtmLockDetail] = []
        var removeLockDetails: [AgoraRtmLockDetail] = []
        switch event.eventType {
        case .snapshot:
            self.lockDetailCaches[event.channelName] = event.lockDetailList
            
            addLockDetails = event.lockDetailList
        case .lockAcquired:
            var snapshotList = self.lockDetailCaches[event.channelName] ?? []
            snapshotList += event.lockDetailList
            self.lockDetailCaches[event.channelName] = snapshotList
            
            addLockDetails = event.lockDetailList
            break
        case .lockExpired, .lockRemoved, .lockReleased:
            var snapshotList = self.lockDetailCaches[event.channelName] ?? []
            let removeIds = event.lockDetailList.map {$0.owner}
            snapshotList = snapshotList.filter({removeIds.contains($0.owner)})
            self.lockDetailCaches[event.channelName] = snapshotList
            
            removeLockDetails = event.lockDetailList
            break
        default:
            break
        }
        
        addLockDetails.forEach { lockDetail in
            let delegateKey = "\(event.channelName)__\(lockDetail.lockName)"
            guard let value = self.lockDelegates[delegateKey] else { return }
            for element in value.allObjects {
                element.onReceiveLockDetail(channelName: event.channelName, lockDetail: lockDetail)
            }
        }
        
        removeLockDetails.forEach { lockDetail in
            let delegateKey = "\(event.channelName)__\(lockDetail.lockName)"
            guard let value = self.lockDelegates[delegateKey] else { return }
            for element in value.allObjects {
                element.onReleaseLockDetail(channelName: event.channelName, lockDetail: lockDetail)
            }
        }
    }
}
