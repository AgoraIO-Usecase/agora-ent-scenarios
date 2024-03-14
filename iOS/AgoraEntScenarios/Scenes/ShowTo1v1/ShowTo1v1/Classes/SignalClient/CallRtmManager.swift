//
//  CallRtmManager.swift
//  CallAPI
//
//  Created by wushengtao on 2024/2/27.
//

import Foundation
import AgoraRtmKit

public func createRtmClient(appId: String, userId: String) -> AgoraRtmClientKit {
    let rtmConfig = AgoraRtmClientConfig(appId: appId, userId: userId)
    var rtmClient: AgoraRtmClientKit? = nil
    do {
        rtmClient = try AgoraRtmClientKit(rtmConfig, delegate: nil)
    } catch {
        callMessagePrint("create rtm client fail: \(error.localizedDescription)")
    }
    return rtmClient!
}

func callMessagePrint(_ message: String) {
    NSLog(message)
}

/// CallRtmManager回调协议
public protocol ICallRtmManagerListener: NSObjectProtocol {
    
    /// rtm连接成功
    func onConnected()
    
    /// rtm连接断开
    func onDisconnected()
    
    /// rtm中断，需要重新login
    func onConnectionLost()
    
    ///token即将过期，需要renew token
    func onTokenPrivilegeWillExpire(channelName: String)
}

public class CallRtmManager: NSObject {
    public var isConnected: Bool = false
    public weak var delegate: ICallRtmManagerListener?
    
    private var rtmClient: AgoraRtmClientKit

    /// RTM是否已经登录
    private var isLoginedRtm: Bool = false
    
    /// 是否外部传入的rtm，如果是则不需要手动logout
    private var isExternalRtmClient: Bool = false
    
    deinit {
        self.rtmClient.removeDelegate(self)
    }
    
    public required init(appId: String, userId: String, rtmClient: AgoraRtmClientKit? = nil) {
        if let rtmClient = rtmClient {
            //如果外部传入rtmclient，默认登陆成功
            self.isLoginedRtm = true
            self.isExternalRtmClient = true
            self.rtmClient = rtmClient
            self.isConnected = true
        } else {
            self.rtmClient = createRtmClient(appId: appId, userId: userId)
        }
        super.init()
        
        self.rtmClient.addDelegate(self)
        
        // disable retry message
        let _ = self.rtmClient.setParameters("{\"rtm.msg.tx_timeout\": 3000}")
        callMessagePrint("init-- CallMessageManager ")
    }
    
    public func getRtmClient() -> AgoraRtmClientKit {
        return rtmClient
    }
    
    public func login(rtmToken: String, completion: @escaping ((NSError?) -> ())) {
        callMessagePrint("initialize")
        if rtmToken.isEmpty, isExternalRtmClient == false {
            let reason = "RTM Token is Empty"
            completion(NSError(domain: reason, code: -1))
            return
        }
        
        if !isLoginedRtm {
            loginRtm(rtmClient: rtmClient, token: rtmToken) {/*[weak self]*/ err in
                if let err = err, err.errorCode != .ok, err.errorCode != .duplicateOperation {
                    completion(NSError(domain: err.reason, code: err.errorCode.rawValue))
                    return
                }
                completion(nil)
            }
        } else {
            completion(nil)
        }
    }
    
    public func logout() {
        if isExternalRtmClient == false {
            rtmClient.logout()
            rtmClient.destroy()
            self.isConnected = false
        }
    }
    
    /// 更新RTM token
    /// - Parameter tokenConfig: CallTokenConfig
    public func renewToken(rtmToken: String) {
        guard isLoginedRtm else {
            //没有登陆成功，但是需要自动登陆，可能是初始token问题，这里重新initialize
            callMessagePrint("renewToken need to reinit")
            self.rtmClient.logout()
            login(rtmToken: rtmToken) { err in
            }
            return
        }
        rtmClient.renewToken(rtmToken, completion: { resp, err in
            callMessagePrint("rtm renewToken: \(err?.errorCode.rawValue ?? 0)")
        })
    }
    
    private func loginRtm(rtmClient: AgoraRtmClientKit,
                          token: String,
                          completion: @escaping (AgoraRtmErrorInfo?)->()) {
        if isLoginedRtm {
            completion(nil)
            return
        }
        rtmClient.logout()
        callMessagePrint("will login")
        rtmClient.login(token) {[weak self] resp, error in
            guard let self = self else {return}
            callMessagePrint("login completion: \(error?.errorCode.rawValue ?? 0)")
            self.isLoginedRtm = error == nil ? true : false
            completion(error)
        }
    }
}

//MARK: AgoraRtmClientDelegate
//TODO: error handler
extension CallRtmManager: AgoraRtmClientDelegate {
    public func rtmKit(_ kit: AgoraRtmClientKit,
                channel channelName: String,
                connectionChangedToState state: AgoraRtmClientConnectionState,
                reason: AgoraRtmClientConnectionChangeReason) {
        callMessagePrint("rtm connectionChangedToState: \(state.rawValue) reason: \(reason.rawValue)")
        if reason == .changedTokenExpired {
            self.delegate?.onTokenPrivilegeWillExpire(channelName: channelName)
        } else if reason == .changedChangedLost {
            //TODO: 内部重试，rtm 2.2.0支持
            self.isConnected = false
            self.delegate?.onConnectionLost()
        } else if state == .connected {
            if self.isConnected == true { return }
            self.isConnected = true
            self.delegate?.onConnected()
        } else {
            if self.isConnected == false { return }
            self.isConnected = false
            self.delegate?.onDisconnected()
        }
    }

    public func rtmKit(_ rtmKit: AgoraRtmClientKit, tokenPrivilegeWillExpire channel: String?) {
        callMessagePrint("rtm onTokenPrivilegeWillExpire[\(channel ?? "nil")]")
        self.delegate?.onTokenPrivilegeWillExpire(channelName: channel ?? "")
    }
}
