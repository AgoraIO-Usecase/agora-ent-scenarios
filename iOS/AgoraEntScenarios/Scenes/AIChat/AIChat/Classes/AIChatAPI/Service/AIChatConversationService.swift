//
//  AIChatConversationService.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/28.
//

import Foundation
import AgoraChat

public protocol AIChatConversationServiceProtocol {
    
    /// 添加监听器
    /// - Parameter listener: 遵守``AIChatConversationListener``协议的监听器对象
    func addListener(listener: AIChatConversationListener)
    
    /// 移除监听器`
    /// - Parameter listener: 遵守``AIChatConversationListener``协议的监听器对象
    func removeListener(listener: AIChatConversationListener)
    
    /// 获取与AI聊天的会话列表
    /// - Returns: 会话对象数组
    func fetchAIConversationList() async -> ([AIChatConversationInfo],AgoraChatError?)
    
    /// 删除会话
    /// - Parameter conversationId: 会话id
    /// - Returns: 错误信息
    func delete(conversationId: String) async -> AgoraChatError?
}

@objc public protocol AIChatConversationListener: NSObjectProtocol {
    
    /// 会话最后一条消息发生变化
    /// - Parameter conversation: 会话对象数组
    func onAIConversationLastMessageChanged(_ conversations: [AIChatConversationInfo])
    
    /// 会话列表发生变化
    /// - Parameter conversations: 会话对象数组
    func onAIConversationListChanged(_ conversations: [AIChatConversationInfo])
}
