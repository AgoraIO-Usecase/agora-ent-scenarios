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
    /// - Parameter message: 收到的消息内容
    func onMessageReceive(message: String)
    
    /// 信令日志回调
    /// - Parameters:
    ///   - message: 日志消息内容
    ///   - logLevel: 日志优先级
    @objc optional func debugInfo(message: String, logLevel: Int)
}

/// 信令抽象协议
@objc public protocol ISignalClient: NSObjectProtocol {
    
    /// CallApi 通过该方法使用信令系统向指定用户发送消息
    /// - Parameters:
    ///   - userId: 目标用户id
    ///   - message: 消息对象
    ///   - completion: 发送完成的回调
    func sendMessage(userId: String,
                     message: String,
                     completion: ((NSError?)-> Void)?)
    
    /// 向信令系统添加一个信令监听器
    /// - Parameter listener: 信令监听器，用于处理消息接收和日志记录
    func addListener(listener: ISignalClientListener)
    
    /// 从信令系统移除指定的信令监听器
    /// - Parameter listener: 待移除的信令监听器
    func removeListener(listener: ISignalClientListener)
}
