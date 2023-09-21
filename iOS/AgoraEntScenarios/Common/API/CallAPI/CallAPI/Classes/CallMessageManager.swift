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
    
    private var loginSuccess: ((AgoraRtmErrorInfo?)->())?
    
    /// RTM是否已经登录
    private var isLoginedRTM: Bool = false
    /// RTM 是否已经订阅频道
    public private(set) var isSubscribedRTM: Bool = false
    
    private var prepareConfig: PrepareConfig?
    private var tokenConfig: CallTokenConfig?
    
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
        callMessagePrint("logout")
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
    ///   - roomId: <#prepareConfig description#>
    ///   - completion: <#completion description#>
    private func _subscribeRTM(userId: String, completion: ((NSError?)->())?) {
        /*
         移除所有的presence，所有缓存由调用的业务服务器去控制
         订阅自己频道的message，用来收消息
         */
        let options = AgoraRtmSubscribeOptions()
        options.withMessage = true
        options.withMetadata = false
        options.withPresence = false
        _subscribe(channelName: userId, option: options, completion: completion)
    }
    
    /// 发送回执消息
    /// - Parameters:
    ///   - roomId: 回执消息发往的频道
    ///   - messageId: 回执的消息id
    ///   - completion: <#completion description#>
    public func _sendReceipts(roomId: String, messageId: Int, completion: ((NSError?)-> Void)? = nil) {
        var message: [String: Any] = [:]
        message[kReceiptsKey] = messageId
        callMessagePrint("_sendReceipts to '\(roomId)', message: \(message)")
        let data = try? JSONSerialization.data(withJSONObject: message) as NSData
        let options = AgoraRtmPublishOptions()
//        let date = Date()
        rtmClient.publish(roomId, message: data!, withOption: options) { resp, err in
//            guard let self = self else {return}
            let error = err.errorCode == .ok ? nil : NSError(domain: err.reason, code: err.errorCode.rawValue)
//            self.callMessagePrint("_sendReceipts cost \(-date.timeIntervalSinceNow * 1000) ms")
            if error == nil {
                completion?(nil)
                return
            }
            completion?(error)
        }
    }
    
    private func _sendMessage(userId: String, message: [String: Any], completion: ((NSError?)-> Void)?) {
        if userId.count == 0 {
            completion?(NSError(domain: "send message fail! roomId is empty", code: -1))
            return
        }
        callMessagePrint("_sendMessage to '\(userId)', message: \(message)")
        let msgId = message[kMessageId] as? Int ?? 0
        let data = try? JSONSerialization.data(withJSONObject: message) as NSData
        let options = AgoraRtmPublishOptions()
        let date = Date()
        rtmClient.publish(userId, message: data!, withOption: options) { [weak self] resp, err in
            
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
                        self._sendMessage(userId: userId, message: message, completion: completion)
                    }
                    self?.receiptsQueue.append(receiptInfo)
                    receiptInfo.checkReceipt()
                }
                return
            }
            if let error = error {
                self?.callWarningPrint("_sendMessage: fail: \(error)")
            }
            completion?(error)
        }
    }
    
    private func _subscribe(channelName: String, option: AgoraRtmSubscribeOptions, completion: ((NSError?) -> ())?) {
        guard let rtmClient = self.rtmClient else {
            completion?(NSError(domain: "rtmClient is nil, please invoke 'initialize' to setup config", code: -1))
            return
        }
        
        callMessagePrint("will subscribe[\(channelName)] message: \(option.withMessage) presence: \(option.withPresence)")
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
        
        callMessagePrint("will login")
        self.loginSuccess = completion
        rtmClient.logout()
        rtmClient.login(byToken: token) {[weak self] resp, error in
            guard let self = self else {return}
            //TODO(RTM Team): timeout to reconnect bug (callback multi times)
            self.callMessagePrint("login completion: \(error.errorCode.rawValue)")
            self.isLoginedRTM = error.errorCode == .ok ? true : false
            self.loginSuccess?(error)
            self.loginSuccess = nil
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
    public func rtmInitialize(prepareConfig: PrepareConfig, tokenConfig: CallTokenConfig?, completion: ((NSError?) -> ())?) {
        callMessagePrint("_rtmInitialize")
        self.prepareConfig = prepareConfig
        self.tokenConfig = tokenConfig
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
                
                self?.rtmInitialize(prepareConfig: prepareConfig,
                                    tokenConfig: tokenConfig,
                                    completion: completion)
            }
        } else if isLoginedRTM, prepareConfig.autoSubscribeRTM {
            _subscribeRTM(userId: "\(config.userId)") {[weak self] error in
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
            if let prepareConfig = prepareConfig, prepareConfig.autoLoginRTM {
                callMessagePrint("renewToken need to reinit")
                self.rtmClient.logout()
                rtmInitialize(prepareConfig: prepareConfig, tokenConfig: tokenConfig) { err in
                }
            }
            return
        }
        rtmClient?.renewToken(rtmToken,completion: {[weak self] resp, err in
            self?.callMessagePrint("rtm renewToken: \(err.errorCode.rawValue)")
        })
    }
    
    /// 发送频道消息
    /// - Parameters:
    ///   - userId: 往哪个用户发送消息
    ///   - fromUserId: 哪个用户发送的，用来给对端发送回执
    ///   - message: 发送的消息字典
    ///   - completion: <#completion description#>
    public func sendMessage(userId: String,
                            fromUserId: String,
                            message: [String: Any],
                            completion: ((NSError?)-> Void)?) {
        messageId += 1
        messageId %= Int.max
        var message = message
        let msgId = messageId
        message[kMessageId] = msgId
        message[kReceiptsRoomIdKey] = fromUserId
        assert(fromUserId.count > 0, "kReceiptsRoomIdKey is empty")
        _sendMessage(userId: userId, message: message, completion: completion)
    }
}

//MARK: AgoraRtmClientDelegate
extension CallMessageManager: AgoraRtmClientDelegate {
    func rtmKit(_ kit: AgoraRtmClientKit, channel channelName: String, connectionStateChanged state: AgoraRtmClientConnectionState, reason: AgoraRtmClientConnectionChangeReason) {
        callMessagePrint("rtm connectionStateChanged: \(state.rawValue) reason: \(reason.rawValue)")
        if reason == .changedTokenExpired {
            self.rtmDelegate?.rtmKit?(kit, onTokenPrivilegeWillExpire: nil)
        }
    }
    
    func rtmKit(_ rtmKit: AgoraRtmClientKit, onTokenPrivilegeWillExpire channel: String?) {
        callMessagePrint("rtm onTokenPrivilegeWillExpire[\(channel ?? "nil")]")
        self.rtmDelegate?.rtmKit?(rtmKit, onTokenPrivilegeWillExpire: channel)
    }
    
    //收到RTM消息
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, on event: AgoraRtmMessageEvent) {
        let message = event.message
        if let data = message.getData() as? Data,
           let dic = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            if let messageId = dic[kMessageId] as? Int,
               let receiptsRoomId = dic[kReceiptsRoomIdKey] as? String {
                _sendReceipts(roomId: receiptsRoomId, messageId: messageId)
            } else if let receiptsId = dic[kReceiptsKey] as? Int {
                callMessagePrint("recv receipts \(receiptsId)")
                receiptsQueue = receiptsQueue.filter({$0.messageId != receiptsId})
            }
            
            callMessagePrint("on event message: \(String(data: data, encoding: .utf8) ?? "")")
        } else {
            callWarningPrint("on event message parse fail, \(message.getType().rawValue) \(message.getData())")
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
