//
//  AUIRtmMsgProxy.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation
//import AgoraRtcKit
import AgoraRtmKit

private let kAUIRtmMsgProxyKey = "AUIRtmMsgProxy"
//TODO: change protocol name
@objc public protocol AUIRtmErrorProxyDelegate: NSObjectProtocol {
    
    /// 系统时间戳更新
    /// - Parameters:
    ///   - timeStamp: <#timeStamp description#>
    @objc optional func onTimeStampsDidUpdate(timeStamp: UInt64)
    
    /// token过期
    /// - Parameter channelName: <#channelName description#>
    @objc optional func onTokenPrivilegeWillExpire(channelName: String?)
    
    /// 网络状态变化
    /// - Parameters:
    ///   - channelName: <#channelName description#>
    ///   - state: <#state description#>
    ///   - reason: <#reason description#>
    @objc optional func didReceiveLinkStateEvent(event: AgoraRtmLinkStateEvent)
    
    /// 收到的KV为空
    /// - Parameter channelName: <#channelName description#>
    @objc optional func onMsgRecvEmpty(channelName: String)
}

@objc public protocol AUIRtmAttributesProxyDelegate: NSObjectProtocol {
    func onAttributesDidChanged(channelName: String, key: String, value: Any)
}

@objc public protocol AUIRtmMessageProxyDelegate: NSObjectProtocol {
    func onMessageReceive(publisher: String, channelName: String, message: String)
}

@objc public enum AUIRtmUserLeaveReason: Int {
    case normal = 0
    case timeout = 1
}

@objc public protocol AUIRtmUserProxyDelegate: NSObjectProtocol {
    func onCurrentUserJoined(channelName: String)
    func onUserSnapshotRecv(channelName: String, userId:String, userList: [[String: Any]])
    func onUserDidJoined(channelName: String, userId:String, userInfo: [String: Any])
    func onUserDidLeaved(channelName: String, userId:String, userInfo: [String: Any], reason: AUIRtmUserLeaveReason)
    func onUserDidUpdated(channelName: String, userId:String, userInfo: [String: Any])
}

@objc public protocol AUIRtmLockProxyDelegate: NSObjectProtocol {
    func onReceiveLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail, eventType: AgoraRtmLockEventType)
    func onReleaseLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail, eventType: AgoraRtmLockEventType)
}

/// RTM消息转发器
open class AUIRtmMsgProxy: NSObject {
    private var rtmChannelType: AgoraRtmChannelType!
    private var attributesDelegates:[String: NSHashTable<AUIRtmAttributesProxyDelegate>] = [:]
    private var attributesCacheAttr: [String: [String: String]] = [:]
    private var lockDelegates: [String: NSHashTable<AUIRtmLockProxyDelegate>] = [:]
    private var lockDetailCaches: [String: [AgoraRtmLockDetail]] = [:]
    private var messageDelegates = NSHashTable<AUIRtmMessageProxyDelegate>.weakObjects()
    private var userDelegates: [String: NSHashTable<AUIRtmUserProxyDelegate>] = [:]
    private var errorDelegates: [String: NSHashTable<AUIRtmErrorProxyDelegate>] = [:]
    
    required public init(rtmChannelType: AgoraRtmChannelType) {
        super.init()
        self.rtmChannelType = rtmChannelType
    }
    
    func cleanCache(channelName: String) {
        attributesCacheAttr[channelName] = nil
    }
    
    func subscribeAttributes(channelName: String, itemKey: String, delegate: AUIRtmAttributesProxyDelegate) {
        aui_info("subscribeAttributes[\(channelName)]: \(itemKey) delegate: \(delegate)", tag: kAUIRtmMsgProxyKey)
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
        aui_info("unsubscribeAttributes[\(channelName)]: \(itemKey) delegate: \(delegate)", tag: kAUIRtmMsgProxyKey)
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
        if let value = userDelegates[channelName] {
            if !value.contains(delegate) {
                value.add(delegate)
            }
        }else{
            let weakObjects = NSHashTable<AUIRtmUserProxyDelegate>.weakObjects()
            weakObjects.add(delegate)
            userDelegates[channelName] = weakObjects
        }
    }
    
    func unsubscribeUser(channelName: String, delegate: AUIRtmUserProxyDelegate) {
        guard let value = userDelegates[channelName] else {
            return
        }
        value.remove(delegate)
    }
    
    func subscribeError(channelName: String, delegate: AUIRtmErrorProxyDelegate) {
        if let value = errorDelegates[channelName] {
            if !value.contains(delegate) {
                value.add(delegate)
            }
        }else{
            let weakObjects = NSHashTable<AUIRtmErrorProxyDelegate>.weakObjects()
            weakObjects.add(delegate)
            errorDelegates[channelName] = weakObjects
        }
    }
    
    func unsubscribeError(channelName: String, delegate: AUIRtmErrorProxyDelegate) {
        guard let value = errorDelegates[channelName] else {
            return
        }
        value.remove(delegate)
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
            delegate.onReceiveLockDetail(channelName: channelName, lockDetail: lockDetail, eventType: .lockAcquired)
        }
    }
    
    func unsubscribeLock(channelName: String, lockName: String, delegate: AUIRtmLockProxyDelegate) {
        let key = "\(channelName)__\(lockName)"
        guard let value = lockDelegates[key] else { return }
        value.remove(delegate)
    }
    
    func processMetaData(channelName: String, data: AgoraRtmMetadata?) {
        guard let data = data else {return}
        let items = data.items ?? []
        
        var cache = self.attributesCacheAttr[channelName] ?? [:]
        var existKeys: [String] = []
        items.forEach { item in
//            aui_info("\(item.key): \(item.value)", tag: kAUIRtmMsgProxyKey)
            //判断value和缓存里是否一致，这里用string可能会不准，例如不同终端序列化的时候json obj不同kv的位置不一样会造成生成的json string不同
            existKeys.append(item.key)
            if cache[item.key] == item.value {
                aui_info("processMetaData[\(channelName)] there are no changes of [\(item.key)]", tag: kAUIRtmMsgProxyKey)
                return
            }
            cache[item.key] = item.value
            guard let itemData = item.value.data(using: .utf8),
                  let itemValue = try? JSONSerialization.jsonObject(with: itemData) else {
                aui_info("[\(channelName)]parse itemData fail: \(item.key) \(item.value)", tag: kAUIRtmMsgProxyKey)
                return
            }
            let delegateKey = "\(channelName)__\(item.key)"
            aui_info("processMetaData[\(channelName)] did change of [\(item.key)]: \(item.value)")
            guard let value = self.attributesDelegates[delegateKey] else { return }
            for element in value.allObjects {
                element.onAttributesDidChanged(channelName: channelName, key: item.key, value: itemValue)
            }
        }
        
        let cacheKeys = cache.keys
        cacheKeys.forEach { key in
            if existKeys.contains(key) {return}
            //远端已经不存在对应的key了，需要通知所有的delegate
            cache.removeValue(forKey: key)
            let delegateKey = "\(channelName)__\(key)"
            guard let value = self.attributesDelegates[delegateKey] else { return }
            for element in value.allObjects {
                element.onAttributesDidChanged(channelName: channelName, key: key, value: [:])
            }
        }
        
        self.attributesCacheAttr[channelName] = cache
        if items.count > 0 {
            return
        }
        guard let elements = self.errorDelegates[channelName] else { return }
        for element in elements.allObjects {
            element.onMsgRecvEmpty?(channelName: channelName)
        }
    }
    
    func processTimeStampsDidUpdate(channelName: String, timeStamp: UInt64) {
        guard let elements = self.errorDelegates[channelName] else { return }
        for element in elements.allObjects {
            element.onTimeStampsDidUpdate?(timeStamp: timeStamp)
        }
    }
}

//MARK: AgoraRtmClientDelegate
extension AUIRtmMsgProxy: AgoraRtmClientDelegate {
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, tokenPrivilegeWillExpire channel: String?) {
        aui_info("onTokenPrivilegeWillExpire: \(channel ?? "")", tag: kAUIRtmMsgProxyKey)
        
        guard let elements = self.errorDelegates[channel ?? ""] else { return }
        for element in elements.allObjects {
            element.onTokenPrivilegeWillExpire?(channelName: channel)
        }
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveLinkStateEvent event: AgoraRtmLinkStateEvent) {
        aui_info("didReceiveLinkStateEvent currentState: \(event.currentState.rawValue) serviceType: \(event.serviceType.rawValue) operation: \(event.operation.rawValue) reason: \(event.reason ?? "") isResumed: \(event.isResumed)", tag: kAUIRtmMsgProxyKey)
        
        for channelName in event.affectedChannels {
            if let elements = self.errorDelegates[channelName] {
                for element in elements.allObjects {
                    element.didReceiveLinkStateEvent?(event: event)
                }
            }
            
//            if event.currentState == .connected, event.operation == .reconnected {
//                if let elements = self.userDelegates[channelName] {
//                    for element in elements.allObjects {
//                        element.onCurrentUserJoined(channelName: channelName)
//                    }
//                }
//            }
        }
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveStorageEvent event: AgoraRtmStorageEvent) {
        guard event.channelType == rtmChannelType else {
            return
        }
        
        aui_info("didReceiveStorageEvent event: [\(event.target)] channelType: [\(event.channelType.rawValue)] eventType: [\(event.eventType.rawValue)] =======", tag: kAUIRtmMsgProxyKey)
        
        //key使用channelType__eventType，保证message channel/stream channel, user storage event/channel storage event共存
        let channelName = event.target
        
        processTimeStampsDidUpdate(channelName: channelName, timeStamp: event.timestamp)
        
        processMetaData(channelName: channelName, data: event.data)
        aui_info("storage event[\(channelName)] ========", tag: kAUIRtmMsgProxyKey)
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceivePresenceEvent event: AgoraRtmPresenceEvent) {
        let channelName = event.channelName
        aui_info("[\(channelName)] didReceivePresenceEvent event: [\(event.type.rawValue)] channel type: [\(event.channelType.rawValue)]] states: \(event.states.count) =======", tag: kAUIRtmMsgProxyKey)
        
        processTimeStampsDidUpdate(channelName:channelName, timeStamp: event.timestamp)
        
        guard event.channelType == rtmChannelType else {
            return
        }
        
//        var map: [[]]
        var map: [String: String] = [:]
        event.states.keys.forEach { key in
            guard let key = key as? String, let value = event.states[key] as? String else {return}
            map[key] = value
        }
        let userId = event.publisher ?? ""
        aui_info("presence userId: \(userId) event_type: \(event.type.rawValue) userInfo: \(map)", tag: kAUIRtmMsgProxyKey)
        if event.type == .remoteJoinChannel {
//            if map.count == 0 {
//                aui_warn("join user fail, empty: userId: \(userId) \(map)", tag: kAUIRtmMsgProxyKey)
//                return
//            }
            
            if let elements = self.userDelegates[event.channelName] {
                for element in elements.allObjects {
                    element.onUserDidJoined(channelName: event.channelName, userId: userId, userInfo: map)
                }
            }
        } else if event.type == .remoteLeaveChannel || event.type == .remoteConnectionTimeout {
            let reason: AUIRtmUserLeaveReason = event.type == .remoteLeaveChannel ? .normal : .timeout
            if let elements = self.userDelegates[event.channelName] {
                for element in elements.allObjects {
                    element.onUserDidLeaved(channelName: event.channelName, userId: userId, userInfo: map, reason: reason)
                }
            }
        } else if event.type == .remoteStateChanged {
            if map.count == 0 {
                aui_warn("update user fail, empty: userId: \(userId) \(map)", tag: kAUIRtmMsgProxyKey)
                return
            }
            if let elements = self.userDelegates[event.channelName] {
                for element in elements.allObjects {
                    element.onUserDidUpdated(channelName: event.channelName, userId: userId, userInfo: map)
                }
            }
        } else if event.type == .snapshot {
            let userList = event.snapshotList()
            if let elements = self.userDelegates[event.channelName] {
                for element in elements.allObjects {
                    element.onUserSnapshotRecv(channelName: event.channelName, userId: userId, userList: userList)
                    element.onCurrentUserJoined(channelName: event.channelName)
                }
            }
        }
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveMessageEvent event: AgoraRtmMessageEvent) {
        aui_info("[\(event.channelName)]didReceiveMessageEvent  =======", tag: kAUIRtmMsgProxyKey)
        
        processTimeStampsDidUpdate(channelName: event.channelName, timeStamp: event.timestamp)
        
        var message: String = ""
        if let msg = event.message.stringData {
            message = msg
        } else if let data = event.message.rawData, let msg = String(data: data, encoding: .utf8) {
            message = msg
        } else {
            aui_warn("recv unknown type message", tag: kAUIRtmMsgProxyKey)
        }
        
        for element in messageDelegates.allObjects {
            element.onMessageReceive(publisher: event.publisher, channelName: event.channelName, message: message)
        }
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveLockEvent event: AgoraRtmLockEvent) {
        aui_info("didReceiveLockEvent[\(event.channelName)]: type: \(event.eventType.rawValue) count: \(event.lockDetailList.count)")
        
        processTimeStampsDidUpdate(channelName: event.channelName, timeStamp: event.timestamp)
        
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
                element.onReceiveLockDetail(channelName: event.channelName, lockDetail: lockDetail, eventType: event.eventType)
            }
        }
        
        removeLockDetails.forEach { lockDetail in
            let delegateKey = "\(event.channelName)__\(lockDetail.lockName)"
            guard let value = self.lockDelegates[delegateKey] else { return }
            for element in value.allObjects {
                element.onReleaseLockDetail(channelName: event.channelName, lockDetail: lockDetail, eventType: event.eventType)
            }
        }
    }
}
