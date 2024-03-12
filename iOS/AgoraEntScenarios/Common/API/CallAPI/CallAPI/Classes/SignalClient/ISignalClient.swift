//
//  ISignalClient.swift
//  CallAPI
//
//  Created by wushengtao on 2024/2/27.
//

import Foundation

/// 信令回调协议
@objc public protocol ISignalClientListener: NSObjectProtocol {

    /// 收到消息的回调
    /// - Parameter message: 收到的消息
    func onMessageReceive(message: String)
    
    /// 信令日志回调
    /// - Parameters:
    ///   - message: 日志消息内容
    ///   - logLevel: 日志优先级
    @objc optional func debugInfo(message: String, logLevel: Int)
}

/// 信令抽象协议
@objc public protocol ISignalClient: NSObjectProtocol {
    
    /// CallApi往信令系统发消息
    /// - Parameters:
    ///   - userId: 目标用户id
    ///   - message: 消息对象
    ///   - completion: 完成回调
    func sendMessage(userId: String,
                     message: String,
                     completion: ((NSError?)-> Void)?)
    
    /// 监听信令系统回调
    /// - Parameter listener: <#listener description#>
    func addListener(listener: ISignalClientListener)
    
    /// 移除信令系统回调
    /// - Parameter listener: <#listener description#>
    func removeListener(listener: ISignalClientListener)
}
