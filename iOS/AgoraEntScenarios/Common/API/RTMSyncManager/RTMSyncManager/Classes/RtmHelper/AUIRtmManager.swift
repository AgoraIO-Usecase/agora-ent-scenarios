//
//  AUIRtmManager.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/1.
//

import Foundation
//import AgoraRtcKit
import AgoraRtmKit

private let kReceiptTimeout: TimeInterval = 10.0

private let unSubscribeInterval: Int64 = 2500

/// 对RTM相关操作的封装类
open class AUIRtmManager: NSObject {
    private var rtmChannelType: AgoraRtmChannelType!
    private var streamChannel: AgoraRtmStreamChannel?
    private lazy var proxy: AUIRtmMsgProxy = AUIRtmMsgProxy(rtmChannelType:rtmChannelType)
    
    private var rtmClient: AgoraRtmClientKit!
    
    public private(set) var isLogin: Bool = false
    private var isExternalLogin: Bool!
    private var throttlerUpdateModel = AUIThrottlerUpdateMetaDataModel()
    private var throttlerRemoveModel = AUIThrottlerRemoveMetaDataModel()
    
    private var receiptTimer: Timer?
    private(set) var receiptCallbackMap: [String: AUIReceipt] = [:] {
        didSet {
            if receiptCallbackMap.count == 0 {
                receiptTimer?.invalidate()
                receiptTimer = nil
                return
            }
            
            guard self.receiptTimer == nil else { return }
            
            self.receiptTimer =
            Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] timer in
                guard let self = self else {return}
                for receipt in self.receiptCallbackMap.values {
                    if abs(receipt.startDate.timeIntervalSinceNow) > kReceiptTimeout {
                        self.receiptCallbackMap.removeValue(forKey: receipt.uniqueId)
                        receipt.closure?(AUICommonError.noResponse.toNSError())
                    }
                }
            }
            
            self.receiptTimer?.fire()
        }
    }
    
    deinit {
        aui_info("deinit AUIRtmManager", tag: "AUIRtmManager")
        self.rtmClient.removeDelegate(proxy)
    }
    
    public required init(rtmClient: AgoraRtmClientKit, 
                         rtmChannelType: AgoraRtmChannelType,
                         isExternalLogin: Bool) {
        self.isExternalLogin = isExternalLogin
        self.isLogin = isExternalLogin
        self.rtmClient = rtmClient
        self.rtmChannelType = rtmChannelType
        super.init()
        self.rtmClient.addDelegate(proxy)
        aui_info("init AUIRtmManager", tag: "AUIRtmManager")
        
        //publish message/set metadata timeout seconds = 3s
        let ret1 = self.rtmClient.setParameters("{\"rtm.msg.tx_timeout\": 3000}")
        let ret2 = self.rtmClient.setParameters("{\"rtm.metadata.api_timeout\": 3000}")
        let ret3 = self.rtmClient.setParameters("{\"rtm.metadata.api_max_retries\": 1}")
        aui_info("setParameters: \(ret1.rawValue)/\(ret2.rawValue)/\(ret3.rawValue)", tag: "AUIRtmManager")
    }
    
    public func login(token: String, completion: @escaping (NSError?)->()) {
        if isLogin {
            aui_info("login already", tag: "AUIRtmManager")
            completion(nil)
            return
        }
        self.rtmClient.login(token) {[weak self] resp, error in
            aui_info("login: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
            if let error = error, error.errorCode != .ok, error.errorCode != .duplicateOperation {
                self?.isLogin = false
                completion(error.toNSError())
                return
            }
            self?.isLogin = true
            completion(nil)
        }
        aui_info("login ", tag: "AUIRtmManager")
    }
    
    public func logout() {
        aui_info("logout", tag: "AUIRtmManager")
        if isExternalLogin {return}
        rtmClient.logout()
        isLogin = false
    }
    
    public func renew(token: String, completion: ((NSError?)->())?) {
        aui_info("renew: \(token)", tag: "AUIRtmManager")
        rtmClient.renewToken(token) { _, err in
            completion?(err)
        }
    }
}

//MARK: user
extension AUIRtmManager {
    public func getUserCount(channelName: String, completion:@escaping (NSError?, Int)->()) {
        guard let presence = rtmClient.getPresence() else {
            completion(AUICommonError.rtmError(-1).toNSError(), 0)
            return
        }
        
        let options = AgoraRtmPresenceOptions()
        options.includeUserId = false
        options.includeState = false
        presence.whoNow(channelName: channelName, 
                        channelType: rtmChannelType,
                        options: options,
                        completion: { resp, error in
//            aui_info("presence whoNow '\(channelName)' finished: \(error.errorCode.rawValue) list count: \(resp.userStateList.count) userId: \(AUIRoomContext.shared.commonConfig?.userId ?? "")", tag: "AUIRtmManager")
            aui_info("getUserCount: \(resp?.totalOccupancy ?? 0)", tag: "AUIRtmManager")
            let userList = resp?.userList()
            completion(error?.toNSError(), userList!.count)
        })
        aui_info("presence whoNow '\(channelName)'", tag: "AUIRtmManager")
    }
    
    public func whoNow(channelName: String, completion:@escaping (NSError?, [[String: String]]?)->()) {
        guard let presence = rtmClient.getPresence() else {
            completion(AUICommonError.rtmError(-1).toNSError(), nil)
            return
        }
        
        let options = AgoraRtmPresenceOptions()
        options.includeUserId = true
        options.includeState = true
        presence.whoNow(channelName: channelName, channelType: rtmChannelType, options: options, completion: { resp, error in
//        aui_info("presence whoNow '\(channelName)' finished: \(error.errorCode.rawValue) list count: \(resp?.userStateList.count ?? 0) userId: \(AUIRoomContext.shared.commonConfig?.userId ?? "")", tag: "AUIRtmManager")
            
            let userList = resp?.userList()
            completion(error?.toNSError(), userList)
        })
        aui_info("presence whoNow '\(channelName)'", tag: "AUIRtmManager")
    }
    
    public func setPresenceState(channelName: String, 
                                 attr:[String: Any],
                                 completion: @escaping (NSError?)->()) {
        guard let presence = rtmClient.getPresence() else {
            completion(AUICommonError.rtmError(-1).toNSError())
            return
        }
        
        var items: [String: String] = [:]
        attr.forEach { (key: String, value: Any) in
            if let val = value as? String {
                items[key] = val
            } else if let val = value as? UInt {
                items[key] = "\(val)"
            } else if let val = value as? Double {
                items[key] = "\(val)"
            } else {
                aui_error("setPresenceState missmatch item: \(key): \(value)", tag: "AUIRtmManager")
                return
            }
        }
        presence.setState(channelName: channelName, 
                          channelType: rtmChannelType,
                          items: items,
                          completion: { resp, error in
            aui_info("presence setState '\(channelName)' finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
            completion(error?.toNSError())
        })
        aui_info("presence setState'\(channelName)' attr: \(attr)", tag: "AUIRtmManager")
    }
}

//MARK: subscribe
extension AUIRtmManager {
    public func subscribeAttributes(channelName: String, 
                                    itemKey: String,
                                    delegate: AUIRtmAttributesProxyDelegate) {
        proxy.subscribeAttributes(channelName: channelName, 
                                  itemKey: itemKey,
                                  delegate: delegate)
    }
    
    public func unsubscribeAttributes(channelName: String, 
                                      itemKey: String,
                                      delegate: AUIRtmAttributesProxyDelegate) {
        proxy.unsubscribeAttributes(channelName: channelName, 
                                    itemKey: itemKey,
                                    delegate: delegate)
    }
    
    public func subscribeMessage(channelName: String, 
                                 delegate: AUIRtmMessageProxyDelegate) {
        proxy.subscribeMessage(channelName: channelName, delegate: delegate)
    }
    
    public func unsubscribeMessage(channelName: String, 
                                   delegate: AUIRtmMessageProxyDelegate) {
        proxy.unsubscribeMessage(channelName: channelName, delegate: delegate)
    }
    
    public func subscribeUser(channelName: String, 
                              delegate: AUIRtmUserProxyDelegate) {
        proxy.subscribeUser(channelName: channelName, delegate: delegate)
    }
    
    public func unsubscribeUser(channelName: String, 
                                delegate: AUIRtmUserProxyDelegate) {
        proxy.unsubscribeUser(channelName: channelName, delegate: delegate)
    }
    
    public func subscribeError(channelName: String, 
                               delegate: AUIRtmErrorProxyDelegate) {
        proxy.subscribeError(channelName: channelName, delegate: delegate)
    }
    
    public func unsubscribeError(channelName: String, 
                                 delegate: AUIRtmErrorProxyDelegate) {
        proxy.unsubscribeError(channelName: channelName, delegate: delegate)
    }
    
    public func subscribeLock(channelName: String, 
                              lockName: String,
                              delegate: AUIRtmLockProxyDelegate) {
        proxy.subscribeLock(channelName: channelName, 
                            lockName: lockName, delegate: delegate)
    }
    
    public func unsubscribeLock(channelName: String, 
                                lockName: String,
                                delegate: AUIRtmLockProxyDelegate) {
        proxy.unsubscribeLock(channelName: channelName, 
                              lockName: lockName,
                              delegate: delegate)
    }
    
    public func subscribe(channelName: String, completion:@escaping (NSError?)->()) {
        aui_info("subscribe '\(channelName)'", tag: "AUIRtmManager")
        let options = AgoraRtmSubscribeOptions()
        options.features = [.metadata, .presence, .lock, .message]
        let date = Date()
        rtmClient.subscribe(channelName: channelName, option: options) { resp, error in
            aui_benchmark("rtm subscribe '\(channelName)' with message type", cost: -date.timeIntervalSinceNow)
            aui_info("subscribe '\(channelName)' finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
            completion(error?.toNSError())
        }
    }
    
    public func unSubscribe(channelName: String) {
        aui_info("unSubscribe '\(channelName)'", tag: "AUIRtmManager")
        proxy.cleanCache(channelName: channelName)
        rtmClient.unsubscribe(channelName)
    }
}

//MARK: Channel Metadata
extension AUIRtmManager {
    public func cleanBatchMetadata(channelName: String,
                                   lockName: String,
                                   removeKeys: [String],
                                   fetchImmediately: Bool = false,
                                   completion: @escaping (NSError?)->()) {
        aui_info("cleanBatchMetadata[\(channelName)] removeKeys:\(removeKeys)")
        throttlerRemoveModel.appendMetaDataInfo(keys: removeKeys, completion: completion)
        //TODO: throttler by channel & lockName
        throttlerRemoveModel.throttler.triggerLastEvent(after: 0.01, execute: { [weak self] in
            guard let self = self else {return}
            guard self.throttlerRemoveModel.keys.count > 0 else {return}
            let callbacks = self.throttlerRemoveModel.callbacks
            aui_info("cleanBatchMetadata[\(channelName)] keys count: \(self.throttlerRemoveModel.keys.count)")
            self.cleanMetadata(channelName: channelName,
                               removeKeys: self.throttlerRemoveModel.keys.sorted(),
                               lockName: lockName) { err in
                callbacks.forEach { callback in
                    callback(err)
                }
            }
            self.throttlerRemoveModel.reset()
        })
        if fetchImmediately {
            throttlerRemoveModel.throttler.triggerNow()
        }
    }
    
    public func cleanAllMedadata(channelName: String,
                                 lockName: String,
                                 completion: @escaping (NSError?)->()) {
        cleanMetadata(channelName: channelName,
                      removeKeys: [],
                      lockName: lockName,
                      completion: completion)
    }
    
    public func cleanMetadata(channelName: String, 
                              removeKeys: [String],
                              lockName: String,
                              completion: @escaping (NSError?)->()) {
        guard let storage = rtmClient.getStorage(),
              let data = AgoraRtmMetadata.createMetadata(keys: removeKeys) else {
            assert(false, "cleanMetadata fail")
            return
        }
        
        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        storage.removeChannelMetadata(channelName: channelName,
                                      channelType: rtmChannelType,
                                      data: data,
                                      options: options,
                                      lock: lockName) { resp, error in
            aui_info("cleanMetadata[\(channelName)][\(lockName)] finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
            completion(error?.toNSError())
        }
        aui_info("cleanMetadata[\(channelName)][\(lockName)] \(removeKeys)", tag: "AUIRtmManager")
    }
    
    public func setBatchMetadata(channelName: String,
                                 lockName: String,
                                 metadata: [String: String],
                                 fetchImmediately: Bool = false,
                                 completion: @escaping (NSError?)->()) {
        aui_info("setBatchMetadata1[\(channelName)] metadata keys: \(metadata.keys)")
        throttlerUpdateModel.appendMetaDataInfo(metaData: metadata, completion: completion)
        //TODO: throttler by channel & lockName
        throttlerUpdateModel.throttler.triggerLastEvent(after: 0.01, execute: { [weak self] in
            guard let self = self else {return}
            guard self.throttlerUpdateModel.metaData.count > 0 else {return}
            let callbacks = self.throttlerUpdateModel.callbacks
            aui_info("setBatchMetadata2[\(channelName)] metadata keys: \(self.throttlerUpdateModel.metaData.keys)")
            self.setMetadata(channelName: channelName,
                             lockName: lockName,
                             metadata: self.throttlerUpdateModel.metaData) { err in
                callbacks.forEach { callback in
                    callback(err)
                }
            }
            self.throttlerUpdateModel.reset()
        })
        if fetchImmediately {
            throttlerUpdateModel.throttler.triggerNow()
        }
    }

    public func setMetadata(channelName: String,
                            lockName: String,
                            metadata: [String: String],
                            completion: @escaping (NSError?)->()) {
        guard let storage = rtmClient.getStorage(),
              let data = AgoraRtmMetadata.createMetadata(metadata: metadata) else {
            assert(false, "setMetadata fail")
            return
        }

        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        storage.setChannelMetadata(channelName: channelName,
                                   channelType: rtmChannelType,
                                   data: data,
                                   options: options,
                                   lock: lockName) { resp, error in
            aui_info("setMetadata[\(channelName)][\(lockName)] finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
            completion(error?.toNSError())
        }
        aui_info("setMetadata[\(channelName)][\(lockName)] keys:\(metadata.keys)", tag: "AUIRtmManager")
    }

    public func updateMetadata(channelName: String,
                               lockName: String,
                               metadata: [String: String],
                               completion: @escaping (NSError?)->()) {
        guard let storage = rtmClient.getStorage(),
              let data = AgoraRtmMetadata.createMetadata(metadata: metadata) else {
            assert(false, "updateMetadata fail")
            return
        }

        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        storage.updateChannelMetadata(channelName: channelName,
                                      channelType: rtmChannelType,
                                      data: data,
                                      options: options,
                                      lock: lockName) { resp, error in
            aui_info("updateMetadata[\(channelName)][\(lockName)] finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
            completion(error?.toNSError())
        }
        aui_info("updateMetadata", tag: "AUIRtmManager")
    }
    
    public func getMetadata(channelName: String, completion: @escaping (NSError?, [String: String]?)->()) {
        getMetadata(channelName: channelName) { error, data in
            var map: [String: String] = [:]
            data?.items?.forEach({ item in
                map[item.key] = item.value
            })
            completion(error, map)
        }
    }
    
    public func getMetadata(channelName: String, completion: @escaping (NSError?, AgoraRtmMetadata?)->()) {
        guard let storage = rtmClient.getStorage() else {
            assert(false, "getMetadata fail")
            return
        }
        let date = Date()
        storage.getChannelMetadata(channelName: channelName, channelType: rtmChannelType) { resp, error in
            aui_benchmark("getChannelMetadata[\(channelName)]", cost: -date.timeIntervalSinceNow)
            aui_info("getMetadata[\(channelName)] finished: \(error?.errorCode.rawValue ?? 0) item count: \(resp?.data?.items?.count ?? 0)", tag: "AUIRtmManager")
            completion(error?.toNSError(), resp?.data)
        }
        aui_info("getMetadata", tag: "AUIRtmManager")
    }
    
    public func fetchMetaDataSnapshot(channelName: String, completion: @escaping (NSError?) -> ()) {
        getMetadata(channelName: channelName) {[weak self] error, data in
            self?.proxy.processMetaData(channelName: channelName, data: data)
            completion(error)
        }
    }
}

//MARK: user metadata
extension AUIRtmManager {
    public func subscribeUser(userId: String) {
        guard let storage = rtmClient.getStorage() else {
            assert(false, "subscribeUserMetadata fail")
            return
        }
        storage.subscribeUserMetadata(userId: userId, completion: { resp, error in
            aui_info("subscribeUser finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
        })
        aui_info("subscribeUserMetadata", tag: "AUIRtmManager")
    }
    
    public func unSubscribeUser(userId: String) {
        guard let storage = rtmClient.getStorage() else {
            aui_error("subscribeUserMetadata fail", tag: "AUIRtmManager")
            assert(false, "subscribeUserMetadata fail")
            return
        }
        storage.unsubscribeUserMetadata(userId: userId)
        aui_info("subscribeUserMetadata", tag: "AUIRtmManager")
    }
    
    public func removeUserMetadata(userId: String) {
        guard let storage = rtmClient.getStorage(),
              let data = AgoraRtmMetadata() else {
            aui_info("removeUserMetadata fail", tag: "AUIRtmManager")
            assert(false, "removeUserMetadata fail")
            return
        }
        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        
        storage.removeUserMetadata(userId: userId, 
                                   data: data,
                                   options: options,
                                   completion: { resp, error in
            aui_info("removeUserMetadata finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
        })
        aui_info("removeUserMetadata", tag: "AUIRtmManager")
    }
    
    public func setUserMetadata(userId: String, metadata: [String: String]) {
        guard let storage = rtmClient.getStorage(),
              let data = AgoraRtmMetadata.createMetadata(metadata: metadata) else {
            assert(false, "setUserMetadata fail")
            return
        }
        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        
        storage.setUserMetadata(userId: userId, 
                                data: data,
                                options: options,
                                completion: { resp, error in
            aui_info("setUserMetadata finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
        })
        aui_info("setUserMetadata", tag: "AUIRtmManager")
    }
    
    public func updateUserMetadata(userId: String, metadata: [String: String]) {
        guard let storage = rtmClient.getStorage(),
              let data = AgoraRtmMetadata.createMetadata(metadata: metadata) else {
            aui_error("updateUserlMetadata fail", tag: "AUIRtmManager")
            assert(false, "updateUserlMetadata fail")
            return
        }
        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        
        storage.updateUserMetadata(userId: userId, 
                                   data: data,
                                   options: options,
                                   completion: { resp, error in
            aui_info("updateUserlMetadata finished: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
        })
        aui_info("updateUserlMetadata ", tag: "AUIRtmManager")
    }
    
    public func getUserMetadata(userId: String) {
        guard let storage = rtmClient.getStorage() else {
            aui_error("getUserMetadata fail", tag: "AUIRtmManager")
            return
        }
        
        storage.getUserMetadata(userId: userId) { resp, error in
            aui_info("getUserMetadata: \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
        }
        aui_info("getUserMetadata ", tag: "AUIRtmManager")
    }
}

//MARK: message
extension AUIRtmManager {
    public func markReceiptFinished(uniqueId: String) {
        self.receiptCallbackMap[uniqueId] = nil
    }
    
    public func publishAndWaitReceipt(userId: String,
                                      channelName: String,
                                      message: String,
                                      uniqueId: String,
                                      completion: ( (NSError?)->())?) {
        let date = Date()
        publish(userId: userId, 
                channelName: channelName,
                message: message) {[weak self] err in
            guard let self = self else {return}
            if let err = err {
                completion?(err)
                return
            }
            self.receiptCallbackMap[uniqueId] = AUIReceipt(closure: { error in
                aui_benchmark("publishAndWaitReceipt completion", cost: -date.timeIntervalSinceNow)
                completion?(error)
            }, uniqueId: uniqueId)
        }
    }
    
    public func publish(userId: String, 
                        channelName: String,
                        message: String,
                        completion: @escaping (NSError?)->()) {
        //uid和
        let options = AgoraRtmPublishOptions()
        options.channelType = .user
        rtmClient.publish(channelName: userId, 
                          message: message,
                          option: options) { resp, error in
            var callbackError: NSError?
            if let error = error {
                callbackError = AUICommonError.httpError(error.errorCode.rawValue, error.reason).toNSError()
            }
            completion(callbackError)
            aui_info("publish '\(message)' to user '\(userId)': \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
        }
        aui_info("publish '\(message)' to user '\(userId)'", tag: "AUIRtmManager")
    }
    
    public func publish(channelName: String, 
                        message: String,
                        completion: @escaping (NSError?)->()) {
        //uid和
        let options = AgoraRtmPublishOptions()
        options.channelType = .message
        rtmClient.publish(channelName: channelName, 
                          message: message,
                          option: options) { resp, error in
            var callbackError: NSError?
            if let error = error {
                callbackError = AUICommonError.httpError(error.errorCode.rawValue, error.reason).toNSError()
            }
            completion(callbackError)
            aui_info("publish '\(message)' to channelName '\(channelName)': \(error?.errorCode.rawValue ?? 0)", tag: "AUIRtmManager")
        }
        aui_info("publish '\(message)' to channelName '\(channelName)'", tag: "AUIRtmManager")
    }
    
    public func sendReceipt(userId: String, 
                            channelName: String,
                            uniqueId: String,
                            error: NSError?) {
        let receiptMap: [String: Any] = [
            "uniqueId": uniqueId,
            "code": error?.code ?? 0,
            "channelName": channelName,
            "reason": error?.localizedDescription ?? ""
        ]
        let data = try! JSONSerialization.data(withJSONObject: receiptMap, options: .prettyPrinted)
        let message = String(data: data, encoding: .utf8)!
        publish(userId: userId, channelName: channelName, message: message) { err in
        }
    }
}

//MARK: lock
extension AUIRtmManager {
    public func setLock(channelName: String, 
                        lockName: String,
                        completion:@escaping((NSError?)->())) {
        guard let lock = rtmClient.getLock() else {
            DispatchQueue.main.async {
                completion(AUICommonError.rtmError(-1).toNSError())
            }
            return
        }
        aui_info("setLock[\(channelName)][\(lockName)] start")
        lock.setLock(channelName: channelName,
                     channelType: rtmChannelType,
                     lockName: lockName,
                     ttl: 10) { resp, errorInfo in
            aui_info("setLock[\(channelName)][\(lockName)]: \(errorInfo?.errorCode.rawValue ?? 0)")
            completion(errorInfo?.toNSError())
        }
    }
    public func acquireLock(channelName: String, 
                            lockName: String,
                            completion:@escaping((NSError?)->())) {
        guard let lock = rtmClient.getLock() else {
            DispatchQueue.main.async {
                completion(AUICommonError.rtmError(-1).toNSError())
            }
            return
        }
        aui_info("acquireLock[\(channelName)][\(lockName)] start")
        lock.acquireLock(channelName: channelName,
                         channelType: rtmChannelType,
                         lockName: lockName,
                         retry: true) { resp, errorInfo in
            aui_info("acquireLock[\(channelName)][\(lockName)]: \(errorInfo?.errorCode.rawValue ?? 0)")
            completion(errorInfo?.toNSError())
        }
    }
    
    public func releaseLock(channelName: String, 
                            lockName: String,
                            completion:@escaping((NSError?)->())) {
        guard let lock = rtmClient.getLock() else {
            DispatchQueue.main.async {
                completion(AUICommonError.rtmError(-1).toNSError())
            }
            return
        }
        aui_info("releaseLock[\(channelName)][\(lockName)] start")
        lock.releaseLock(channelName: channelName,
                         channelType: rtmChannelType,
                         lockName: lockName,
                         completion: { resp, errorInfo in
            aui_info("releaseLock[\(channelName)][\(lockName)]: \(errorInfo?.reason ?? "")")
            completion(errorInfo?.toNSError())
        })
    }
    
    public func removeLock(channelName: String, 
                           lockName: String,
                           completion:@escaping((NSError?)->())) {
        guard let lock = rtmClient.getLock() else {
            DispatchQueue.main.async {
                completion(AUICommonError.rtmError(-1).toNSError())
            }
            return
        }
        aui_info("removeLock[\(channelName)][\(lockName)] start")
        lock.removeLock(channelName: channelName,
                        channelType: rtmChannelType,
                        lockName: lockName,
                        completion: { resp, errorInfo in
            aui_info("removeLock[\(channelName)][\(lockName)]: \(errorInfo?.reason ?? "")")
            completion(errorInfo?.toNSError())
        })
    }
}

extension AgoraRtmErrorInfo {
    func toNSError()-> NSError? {
        return self.errorCode == .ok ? nil : NSError(domain: self.reason, code: self.errorCode.rawValue)
    }
}

