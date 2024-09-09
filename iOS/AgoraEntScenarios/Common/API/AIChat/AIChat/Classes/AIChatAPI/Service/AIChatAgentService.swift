//
//  AIChatAgentService.swift
//  AIChat
//
//  Created by qinhui on 2024/9/9.
//

import Foundation

// `AIChatAgentProtocol` 协议定义了调度Agent的基本操作。
/// 实现此协议的类可以启动Agent服务并管理Agent的状态。
protocol AIChatAgentProtocol: AnyObject {
    
    /// 启动语音通话Agent。
    ///
    /// 调用此方法后，会在频道内启动。
    /// 适用于取消音频捕获的场景，例如用户在录音过程中取消操作。
    func startAgent()
    
    /// 停止音频获取。
    ///
    /// 调用此方法后，会丢弃当前捕获的音频数据和转换结果，然后进入空闲状态。
    /// 适用于取消音频捕获的场景，例如用户在录音过程中取消操作。
    func stopAgent()
    
    /// 停止音频获取。
    ///
    /// 调用此方法后，会丢弃当前捕获的音频数据和转换结果，然后进入空闲状态。
    /// 适用于取消音频捕获的场景，例如用户在录音过程中取消操作。
    func pingAgent()
    
    /// 停止音频获取。
    ///
    /// 调用此方法后，会丢弃当前捕获的音频数据和转换结果，然后进入空闲状态。
    /// 适用于取消音频捕获的场景，例如用户在录音过程中取消操作。
    func updateAgent()
    
    /// 停止音频获取。
    ///
    /// 调用此方法后，会丢弃当前捕获的音频数据和转换结果，然后进入空闲状态。
    /// 适用于取消音频捕获的场景，例如用户在录音过程中取消操作。
    func interruptAgent()
}

class AIChatAgentService {
    
}

extension AIChatAgentService: AIChatAgentProtocol {
    func startAgent() {
        
    }
    
    func stopAgent() {
        
    }
    
    func pingAgent() {
        
    }
    
    func updateAgent() {
        
    }
    
    func interruptAgent() {
        
    }
    
    
}
