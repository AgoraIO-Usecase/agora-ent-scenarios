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

protocol CallMessageDelegate: NSObjectProtocol, AgoraRtmClientDelegate {
    /// 回执没有收到
    /// - Parameter message: <#message description#>
    func onMissReceipts(message: [String: Any])
    
    func onConnectionFail()
    
    func debugInfo(message: String)
    func debugWarning(message: String)
}

class CallMessageManager: NSObject {
    public weak var delegate: CallMessageDelegate?
    private var config: CallConfig!
    private var rtmClient: AgoraRtmClientKit!

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
    
    required init(config: CallConfig, rtmClient: AgoraRtmClientKit, delegate: CallMessageDelegate?) {
        super.init()
        self.config = config
        self.delegate = delegate
        self.rtmClient = rtmClient
        if rtmClient == config.rtmClient {
            //如果外部传入rtmclient，默认登陆成功
            self.isLoginedRTM = true
        }
        callMessagePrint("init-- CallMessageManager ")
    }
}

//MARK: private method
extension CallMessageManager {
    /// 根据策略订阅频道消息
    /// - Parameters:
    ///   - roomId: <#prepareConfig description#>
    ///   - completion: <#completion description#>
    private func _subscribeRTM(userId: String, completion: ((NSError?)->())?) {
        guard let rtmClient = self.rtmClient else {
            completion?(NSError(domain: "rtmClient is nil, please invoke 'initialize' to setup config", code: -1))
            return
        }
        let options = AgoraRtmSubscribeOptions()
        options.features = .message
        callMessagePrint("will subscribe[\(userId)] features: \(options.features)")
        rtmClient.unsubscribe(userId)
        rtmClient.subscribe(channelName: userId, option: options) {[weak self] resp, err in
            guard let self = self else {return}
            self.callMessagePrint("subscribe[\(userId)] finished = \(err?.errorCode.rawValue ?? 0)")
            if let err = err {
                completion?(NSError(domain: err.reason, code: err.errorCode.rawValue))
                return
            }
            completion?(nil)
        }
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
        let data = try? JSONSerialization.data(withJSONObject: message)
        let options = AgoraRtmPublishOptions()
        rtmClient.publish(channelName: roomId, data: data!, option: options) { resp, err in
            if let err = err {
                completion?(NSError(domain: err.reason, code: err.errorCode.rawValue))
                return
            }
            completion?(nil)
        }
    }
    
    private func _sendMessage(userId: String, message: [String: Any], completion: ((NSError?)-> Void)?) {
        if userId.count == 0 {
            completion?(NSError(domain: "send message fail! roomId is empty", code: -1))
            return
        }
        callMessagePrint("_sendMessage to '\(userId)', message: \(message)")
        let msgId = message[kMessageId] as? Int ?? 0
        let data = try? JSONSerialization.data(withJSONObject: message)
        let options = AgoraRtmPublishOptions()
        let date = Date()
        rtmClient.publish(channelName: userId, data: data!, option: options) { [weak self] resp, err in
            if let err = err {
                let error = NSError(domain: err.reason, code: err.errorCode.rawValue)
                self?.callWarningPrint("_sendMessage: fail: \(error)")
                completion?(error)
                return
            }
            self?.callMessagePrint("publish cost \(-date.timeIntervalSinceNow * 1000) ms")
            completion?(nil)
            //发送成功检查回执，没收到则重试
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
                    self.receiptsQueue = self.receiptsQueue.filter({$0.messageId != msgId})
                    self.delegate?.onMissReceipts(message: message)
                    return
                }
                self._sendMessage(userId: userId, message: message) { _ in
                }
            }
            self?.receiptsQueue.append(receiptInfo)
            receiptInfo.checkReceipt()
        }
    }
    
    private func loginRTM(rtmClient: AgoraRtmClientKit, token: String, completion: @escaping (AgoraRtmErrorInfo?)->()) {
        if isLoginedRTM {
            completion(nil)
            return
        }
        callMessagePrint("will login")
        rtmClient.logout()
        rtmClient.login(token) {[weak self] resp, error in
            guard let self = self else {return}
            self.callMessagePrint("login completion: \(error?.errorCode.rawValue ?? 0)")
            self.isLoginedRTM = error == nil ? true : false
            completion(error)
        }
    }
}

//MARK: public method
extension CallMessageManager {
    func rtmDeinitialize() {
        rtmClient.removeDelegate(self)
        callMessagePrint("unsubscribe[\(config.userId)]")
        rtmClient.unsubscribe("\(config.userId)")
        receiptsQueue.removeAll()
    }
    
    /// 根据配置初始化RTM
    /// - Parameters:
    ///   - prepareConfig: <#prepareConfig description#>
    ///   - tokenConfig: <#tokenConfig description#>
    ///   - completion: <#completion description#>
    func rtmInitialize(prepareConfig: PrepareConfig, tokenConfig: CallTokenConfig?, completion: ((NSError?) -> ())?) {
        callMessagePrint("_rtmInitialize")
        self.prepareConfig = prepareConfig
        self.tokenConfig = tokenConfig
        rtmClient.addDelegate(self)
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
    /// - Parameter tokenConfig: CallTokenConfig
    public func renewToken(tokenConfig: CallTokenConfig) {
        guard isLoginedRTM else {
            if let prepareConfig = prepareConfig, prepareConfig.autoLoginRTM {
                //没有登陆成功，但是需要自动登陆，可能是初始token问题，这里重新initialize
                callMessagePrint("renewToken need to reinit")
                self.rtmClient.logout()
                rtmInitialize(prepareConfig: prepareConfig, tokenConfig: tokenConfig) { err in
                }
            }
            return
        }
        self.tokenConfig = tokenConfig
        rtmClient?.renewToken(tokenConfig.rtmToken, completion: {[weak self] resp, err in
            self?.callMessagePrint("rtm renewToken: \(err?.errorCode.rawValue ?? 0)")
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
    func rtmKit(_ kit: AgoraRtmClientKit, channel channelName: String, connectionChangedToState state: AgoraRtmClientConnectionState, reason: AgoraRtmClientConnectionChangeReason) {
        callMessagePrint("rtm connectionChangedToState: \(state.rawValue) reason: \(reason.rawValue)")
        if reason == .changedTokenExpired {
            self.delegate?.rtmKit?(kit, tokenPrivilegeWillExpire: nil)
        } else if reason == .changedChangedLost {
            self.delegate?.onConnectionFail()
        }
    }
    
    func rtmKit(_ rtmKit: AgoraRtmClientKit, tokenPrivilegeWillExpire channel: String?) {
        callMessagePrint("rtm onTokenPrivilegeWillExpire[\(channel ?? "nil")]")
        self.delegate?.rtmKit?(rtmKit, tokenPrivilegeWillExpire: channel)
    }
    
    //收到RTM消息
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, didReceiveMessageEvent event: AgoraRtmMessageEvent) {
        let message = event.message
        if let data = message.rawData,
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
            callWarningPrint("on event message parse fail, \(message.rawData?.count ?? 0) )")
        }
        
        self.delegate?.rtmKit?(rtmKit, didReceiveMessageEvent: event)
    }
}


extension CallMessageManager {
    private func callMessagePrint(_ message: String) {
        let tag = "[MessageManager][\(String.init(format: "%p", self))][\(String.init(format: "%p", rtmClient))]"
        delegate?.debugInfo(message: "\(tag)\(message)")
        #if DEBUG
        if let _ = delegate {return}
        print("\(formatter.string(from: Date()))[CallApi]\(tag)\(message)")
        #endif
    }
    
    private func callWarningPrint(_ message: String) {
        let tag = "[MessageManager][\(String.init(format: "%p", self))][\(String.init(format: "%p", rtmClient))]"
        delegate?.debugWarning(message: "\(tag)\(message)")
        #if DEBUG
        if let _ = delegate {return}
        print("\(formatter.string(from: Date()))[CallApi][Warning]\(tag)\(message)")
        #endif
    }
}
