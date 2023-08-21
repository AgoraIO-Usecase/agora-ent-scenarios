//
//  CallMessageManager.swift
//  CallAPI
//
//  Created by wushengtao on 2023/6/9.
//

import AgoraRtmKit

let kReceiptsKey: String = "receipts"      //回执的消息id
let kReceiptsRoomIdKey: String = "receiptsRoomId"     //回执到哪个房间，因为没有点对点，所以单点消息通过不同房间发送消息
let kMessageId: String = "messageId"     //发送的消息id

/// 回执的消息队列对象
private class CallQueueInfo: NSObject {
    var messageId: Int = 0
    var messageInfo: [String: Any]?
    var callback: ((NSError?)->())?
    
    var checkReceiptsFail: ((CallQueueInfo)->())?
    
    private var createDate: Date = Date()
    private(set) var retryTimes: Int = 3
    private var timer: Timer? {
        didSet {
            oldValue?.invalidate()
        }
    }
    
    deinit {
        timer?.invalidate()
//        callMessagePrint("CallQueueInfo deinit \(messageId) cost: \(-Int(createDate.timeIntervalSinceNow * 1000)) ms")
    }
    
    func checkReceipt() {
        self.timer = Timer.scheduledTimer(withTimeInterval: 3, repeats: false) {[weak self] timer in
            guard let self = self else {return}
            self.retryTimes -= 1
//            callMessagePrint("receipt timeout retry \(self.retryTimes)")
            self.checkReceiptsFail?(self)
        }
    }
}

protocol CallMessageDelegate: NSObjectProtocol {
    
    /// 回执没有收到
    /// - Parameter message: <#message description#>
    func onMissReceipts(message: [String: Any])
    
    func debugInfo(message: String)
    func debugWarning(message: String)
}

class CallMessageManager: NSObject {
    public weak var delegate: CallMessageDelegate?
    private var config: CallConfig!
    private var rtmClient: AgoraRtmClientKit!
    private weak var rtmDelegate: AgoraRtmClientDelegate?
    
    private var snapshotDidRecv: (()->())?
    
    /// RTM是否已经登录
    private var isLoginedRTM: Bool = false
    /// RTM 是否已经订阅频道
    public private(set) var isSubscribedRTM: Bool = false
    
    // 消息id
    private var messageId: Int = 0
    //待接收回执队列，保存没有接收到回执或者等待未超时的消息
    private var receiptsQueue: [CallQueueInfo] = []
    
    deinit {
        #if DEBUG
        print("deinit-- CallMessageManager ")
        #endif
    }
    
    init(config: CallConfig, rtmDelegate: AgoraRtmClientDelegate?, delegate: CallMessageDelegate?) {
        super.init()
        self.config = config
        self.delegate = delegate
        self.rtmClient = _createRtmClient(delegate: rtmDelegate)
        callMessagePrint("init-- CallMessageManager ")
    }
    
    func logout() {
        self.rtmClient?.logout()
        self.rtmClient?.destroy()
    }
}

//MARK: private method
extension CallMessageManager {
    
    //创建RTM
    private func _createRtmClient(delegate: AgoraRtmClientDelegate?) -> AgoraRtmClientKit {
        let rtmConfig = AgoraRtmClientConfig()
        rtmConfig.userId = "\(config.userId)"
        rtmConfig.appId = config.appId
        if rtmConfig.userId.count == 0 {
            callWarningPrint("userId is empty")
        }
        if rtmConfig.appId.count == 0 {
            callWarningPrint("appId is empty")
        }
        
        self.rtmDelegate = delegate
        let rtmClient = AgoraRtmClientKit(config: rtmConfig, delegate: self)!
        return rtmClient
    }
    
    /// 根据策略订阅频道消息
    /// - Parameters:
    ///   - prepareConfig: <#prepareConfig description#>
    ///   - completion: <#completion description#>
    private func _subscribeRTM(tokenConfig: CallTokenConfig?, completion: ((NSError?)->())?) {
        var roomId: String? = nil
        if config.mode == .showTo1v1 {
            roomId = config.role == .caller ? tokenConfig?.roomId : config.ownerRoomId
        } else {
            roomId = tokenConfig?.roomId
        }
        guard let roomId = roomId else {
            completion?(NSError(domain: "channelName is Empty", code: -1))
            return
        }
        
        /*
         纯1v1
         订阅自己频道的presence和消息
         
         秀场转1v1
         1.主叫
            a.订阅被叫频道的presence，用来写入presence
            b.订阅自己频道的message, 用来收消息
         2.被叫
            a.订阅自己频道的presence和消息
         */
        if config?.role == .caller, config.mode == .showTo1v1 {
            guard let ownerRoomId = config?.ownerRoomId else {
                completion?(NSError(domain: "ownerRoomId is nil, please invoke 'initialize' to setup config", code: -1))
                return
            }
            
            let group = DispatchGroup()
            
            var error1: NSError? = nil
            var error2: NSError? = nil
            let options1 = AgoraRtmSubscribeOptions()
            options1.withMessage = true
            options1.withMetadata = false
            options1.withPresence = false
            group.enter()
            callMessagePrint("1/3 will _subscribe[\(roomId)]")
            _subscribe(channelName: roomId, option: options1) {[weak self] error in
                error1 = error
                self?.callMessagePrint("1/3 _subscribe[\(roomId)]: \(error?.localizedDescription ?? "success")")
                group.leave()
            }
            
            let options2 = AgoraRtmSubscribeOptions()
            options2.withMessage = false
            options2.withMetadata = false
            options2.withPresence = true
            group.enter()
            callMessagePrint("2/3 will _subscribe[\(ownerRoomId)]")
            _subscribe(channelName: ownerRoomId, option: options2) {[weak self] error in
                error2 = error
                self?.callMessagePrint("2/3 _subscribe[\(ownerRoomId)]: \(error?.localizedDescription ?? "success")")
                group.leave()
            }
            
            group.enter()
            callMessagePrint("3/3 waiting for snapshot")
            //保证snapshot完成才认为subscribe完成，否则presence服务不一定成功导致后续写presence可能不成功
            snapshotDidRecv = {[weak self] in
                self?.callMessagePrint("3/3 recv snapshot")
                group.leave()
            }
            
            group.notify(queue: DispatchQueue.main) {
                completion?(error1 ?? error2)
            }
        } else {
            let group = DispatchGroup()
            
            let options = AgoraRtmSubscribeOptions()
            options.withMessage = true
            options.withMetadata = false
            options.withPresence = true
            group.enter()
            var err: NSError? = nil
            _subscribe(channelName: roomId, option: options) { error in
                err = error
                group.leave()
            }
            
            group.enter()
            snapshotDidRecv = {
                group.leave()
            }
            
            group.notify(queue: DispatchQueue.main) {
                completion?(err)
            }
        }
    }
    
    /// 发送回执消息
    /// - Parameters:
    ///   - roomId: 回执消息发往的频道
    ///   - messageId: 回执的消息id
    ///   - retryCount: 重试次数
    ///   - completion: <#completion description#>
    public func _sendReceipts(roomId: String,
                              messageId: Int,
                              retryCount: Int = 3,
                              completion: ((NSError?)-> Void)? = nil) {
        var message: [String: Any] = [:]
        message[kReceiptsKey] = messageId
        callMessagePrint("_sendReceipts to '\(roomId)', message: \(message), retryCount: \(retryCount)")
        let data = try? JSONSerialization.data(withJSONObject: message) as? NSData
        let options = AgoraRtmPublishOptions()
        let date = Date()
        rtmClient.publish(roomId, message: data!, withOption: options) { [weak self] resp, err in
            guard let self = self else {return}
            let error = err.errorCode == .ok ? nil : NSError(domain: err.reason, code: err.errorCode.rawValue)
//            self.callMessagePrint("_sendReceipts cost \(-date.timeIntervalSinceNow * 1000) ms")
            if error == nil {
                completion?(nil)
                return
            }
            if retryCount <= 1 {
                completion?(error)
            } else {
                self._sendReceipts(roomId: roomId, messageId: messageId, retryCount: retryCount - 1, completion: completion)
            }
        }
    }
    
    private func _sendMessage(roomId: String,
                             message: [String: Any],
                             retryCount: Int = 3,
                             completion: ((NSError?)-> Void)?) {
        callMessagePrint("_sendMessage to '\(roomId)', message: \(message), retryCount: \(retryCount)")
        let msgId = message[kMessageId] as? Int ?? 0
        let data = try? JSONSerialization.data(withJSONObject: message) as? NSData
        let options = AgoraRtmPublishOptions()
        let date = Date()
        rtmClient.publish(roomId, message: data!, withOption: options) { [weak self] resp, err in
            let error: NSError? = err.errorCode == .ok ? nil : NSError(domain: err.reason, code: err.errorCode.rawValue)
            self?.callMessagePrint("publish cost \(-date.timeIntervalSinceNow * 1000) ms")
            if error == nil {
                completion?(nil)
                if error == nil {
                    if let receiptInfo = self?.receiptsQueue.first(where: {$0.messageId == msgId}) {
                        receiptInfo.checkReceipt()
                        return
                    }
                    let receiptInfo = CallQueueInfo()
                    receiptInfo.messageId = msgId
                    receiptInfo.messageInfo = message
                    receiptInfo.callback = completion
                    receiptInfo.checkReceiptsFail = { info in
                        guard let self = self else {return}
                        guard info.retryTimes > 0 else {
                            let message = info.messageInfo ?? [:]
//                            self.callMessagePrint("get receipts fail, msg: \(message)")
                            self.receiptsQueue = self.receiptsQueue.filter({$0.messageId != msgId})
                            self.delegate?.onMissReceipts(message: message)
                            return
                        }
                        self._sendMessage(roomId: roomId, message: message, completion: completion)
                    }
                    self?.receiptsQueue.append(receiptInfo)
                    receiptInfo.checkReceipt()
                }
                return
            }
            if retryCount <= 1 {
                if let error = error {
                    self?.callWarningPrint("_sendMessage: fail: \(error)")
                }
                completion?(error)
            } else {
                self?._sendMessage(roomId: roomId, message: message, retryCount: retryCount - 1, completion: completion)
            }
        }
    }
    
    private func _subscribe(channelName: String, option: AgoraRtmSubscribeOptions, completion: ((NSError?) -> ())?) {
        guard let rtmClient = self.rtmClient else {
            completion?(NSError(domain: "rtmClient is nil, please invoke 'initialize' to setup config", code: -1))
            return
        }
        
        callMessagePrint("will subscribe[\(channelName)]")
        rtmClient.unsubscribe(withChannel: channelName)
        rtmClient.subscribe(withChannel: channelName, option: option) {[weak self] resp, err in
            guard let self = self else {return}
            self.callMessagePrint("subscribe[\(channelName)] finished = \(err.errorCode.rawValue)")
            guard err.errorCode == .ok else {
                completion?(NSError(domain: err.reason, code: err.errorCode.rawValue))
                return
            }
            
            completion?(nil)
        }
    }
    
    private func loginRTM(rtmClient: AgoraRtmClientKit, token: String, completion: @escaping (AgoraRtmErrorInfo?)->()) {
        if isLoginedRTM {
            completion(nil)
            return
        }
        
        self.callMessagePrint("will login")
        rtmClient.login(byToken: token) {[weak self] resp, error in
            guard let self = self else {return}
            self.callMessagePrint("login: \(error.errorCode.rawValue)")
            self.isLoginedRTM = error.errorCode == .ok ? true : false
            completion(error)
        }
    }
}

//MARK: public method
extension CallMessageManager {
    /// 根据配置初始化RTM
    /// - Parameters:
    ///   - prepareConfig: <#prepareConfig description#>
    ///   - tokenConfig: <#tokenConfig description#>
    ///   - completion: <#completion description#>
    public func rtmInitialize(prepareConfig: PrepareConfig,
                              tokenConfig: CallTokenConfig?,
                              completion: ((NSError?) -> ())?) {
        callMessagePrint("_rtmInitialize")
        guard let rtmToken = tokenConfig?.rtmToken else {
            let reason = "RTM Token is Empty"
            completion?(NSError(domain: reason, code: -1))
            return
        }
        
        guard let rtmClient = self.rtmClient else {
            let reason = "rtmClient is nil, please invoke 'initialize' to setup config"
            completion?(NSError(domain: reason, code: -1))
            return
        }
        
        if prepareConfig.autoLoginRTM, !isLoginedRTM {
            loginRTM(rtmClient: rtmClient, token: rtmToken) {[weak self] err in
                if let err = err, err.errorCode != .ok {
                    completion?(NSError(domain: err.reason, code: err.errorCode.rawValue))
                    return
                }
                //TODO: workaround to fixed on presence err(AgoraRtmPresenceEventTypeErrorOutOfService)
                DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.2) {
                    self?.rtmInitialize(prepareConfig: prepareConfig,
                                        tokenConfig: tokenConfig,
                                        completion: completion)
                }
            }
        } else if isLoginedRTM, prepareConfig.autoSubscribeRTM {
            _subscribeRTM(tokenConfig: tokenConfig) {[weak self] error in
                guard let self = self else {return}
                if let error = error {
                    completion?(error)
                    return
                }
                
                self.isSubscribedRTM = true
                completion?(nil)
            }
        } else {
            completion?(nil)
        }
    }
    
    /// 更新RTM token
    /// - Parameter rtmToken: <#rtmToken description#>
    public func renewToken(rtmToken: String) {
        guard isLoginedRTM else {
            return
        }
        rtmClient?.renewToken(rtmToken,completion: {[weak self] resp, err in
            self?.callMessagePrint("rtm renewToken: \(err.errorCode.rawValue)")
        })
    }
    
    /// 发送频道消息
    /// - Parameters:
    ///   - roomId: 往哪个频道发送消息
    ///   - fromRoomId: 哪个频道发送的，用来给对端发送回执
    ///   - message: 发送的消息字典
    ///   - retryCount: 重试次数
    ///   - completion: <#completion description#>
    public func sendMessage(roomId: String,
                            fromRoomId: String,
                            message: [String: Any],
                            retryCount: Int = 3,
                            completion: ((NSError?)-> Void)?) {
        messageId += 1
        messageId %= Int.max
        var message = message
        let msgId = messageId
        message[kMessageId] = msgId
        message[kReceiptsRoomIdKey] = fromRoomId
        assert(fromRoomId.count > 0, "kReceiptsRoomIdKey is empty")
        _sendMessage(roomId: roomId, message: message, completion: completion)
    }
    
    /// 设置presence的属性信息
    /// - Parameters:
    ///   - attr: <#attr description#>
    ///   - retryCount: <#retryCount description#>
    ///   - completion: <#completion description#>
    public func setPresenceState(attr:[String: Any], retryCount: Int = 3, completion: @escaping (Error?)->()) {
        guard config.mode == .showTo1v1 else {
            completion(NSError(domain: "can not be set presence in 'pure 1v1' mode", code: -1))
            return
        }
        func _retry() -> Bool {
            if retryCount <= 1 {
                return false
            }
            DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 1) {
                self.setPresenceState(attr: attr, retryCount: retryCount - 1, completion: completion)
            }
            
            return true
        }
        
        guard let presence = rtmClient?.getPresence() else {
            completion(NSError(domain: "not presence", code: -1))
            return
        }
        guard let roomId = config?.ownerRoomId else {
            completion(NSError(domain: "ownweRoomId isEmpty", code: -1))
            return
        }
        callMessagePrint("_setPresenceState to '\(roomId)', attr: \(attr)")
        
        var items: [AgoraRtmStateItem] = []
        attr.forEach { (key: String, value: Any) in
            let item = AgoraRtmStateItem()
            item.key = key
            if let val = value as? String {
                item.value = val
            } else if let val = value as? UInt {
                item.value = "\(val)"
            } else if let val = value as? Double {
                item.value = "\(val)"
            } else {
                callWarningPrint("setPresenceState missmatch item: \(key): \(value)")
                return
            }
            
            items.append(item)
        }
        
        presence.setState(roomId, channelType: .message, items: items, completion: {[weak self] resp, error in
            guard let self = self else {return}
            self.callWarningPrint("presence setState '\(roomId)' finished: \(error.errorCode.rawValue)")
            if error.errorCode == .ok {
                completion(nil)
                return
            }
            if _retry() {
                return
            }
            completion(NSError(domain: error.reason, code: error.errorCode.rawValue))
        })
    }
    
    
    /// 清理presence信息
    /// - Parameters:
    ///   - keys: 需要清理的presence的key 数组
    ///   - completion: <#completion description#>
    public func removePresenceState(keys: [String], completion: @escaping (Error?)->()) {
        guard let presence = rtmClient?.getPresence() else {
            completion(NSError(domain: "not presence", code: -1))
            return
        }
        guard let roomId = config?.ownerRoomId else {
            completion(NSError(domain: "ownweRoomId isEmpty", code: -1))
            return
        }
        callMessagePrint("_removePresenceState to '\(roomId)', keys: \(keys)")
        
        presence.removeState(roomId, channelType: .message, items: keys, completion: {[weak self] resp, error in
            guard let self = self else {return}
            if error.errorCode == .ok {
                completion(nil)
                return
            }
            self.callWarningPrint("presence removeState '\(roomId)' finished: \(error.errorCode.rawValue)")
            completion(NSError(domain: error.reason, code: error.errorCode.rawValue))
        })
    }
}

//MARK: AgoraRtmClientDelegate
extension CallMessageManager: AgoraRtmClientDelegate {
    func rtmKit(_ rtmKit: AgoraRtmClientKit, onTokenPrivilegeWillExpire channel: String?) {
        callMessagePrint("rtm onTokenPrivilegeWillExpire[\(channel ?? "nil")]")
        self.rtmDelegate?.rtmKit?(rtmKit, onTokenPrivilegeWillExpire: channel)
    }
    //收到RTM消息
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, on event: AgoraRtmMessageEvent) {
        let message = event.message
        callMessagePrint("on event message: \(message.getType().rawValue)")
        if let data = message.getData() as? Data,
           let dic = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            if let messageId = dic[kMessageId] as? Int,
               let receiptsRoomId = dic[kReceiptsRoomIdKey] as? String {
                _sendReceipts(roomId: receiptsRoomId, messageId: messageId)
            } else if let receiptsId = dic[kReceiptsKey] as? Int {
                callMessagePrint("recv receipts \(receiptsId)")
                receiptsQueue = receiptsQueue.filter({$0.messageId != receiptsId})
            }
        }
        
        self.rtmDelegate?.rtmKit?(rtmKit, on: event)
    }
    
    //收到RTM的presence
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, on event: AgoraRtmPresenceEvent) {
        if event.type == .snapshot {
            snapshotDidRecv?()
            snapshotDidRecv = nil
        }
        self.rtmDelegate?.rtmKit?(rtmKit, on: event)
    }
}

extension CallMessageManager {
    private func callMessagePrint(_ message: String) {
        let tag = "[MessageManager][\(String.init(format: "%p", self))][\(String.init(format: "%p", rtmClient))]"
        delegate?.debugInfo(message: "\(tag)\(message)")
        #if DEBUG
        if let _ = delegate {return}
        print("[CallApi]\(tag)\(message)")
        #endif
    }
    
    private func callWarningPrint(_ message: String) {
        let tag = "[MessageManager][\(String.init(format: "%p", self))][\(String.init(format: "%p", rtmClient))]"
        delegate?.debugWarning(message: "\(tag)\(message)")
        #if DEBUG
        if let _ = delegate {return}
        print("[CallApi][Warning]\(tag)\(message)")
        #endif
    }
}
