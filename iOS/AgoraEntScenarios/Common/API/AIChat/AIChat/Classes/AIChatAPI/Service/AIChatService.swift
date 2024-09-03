import Foundation
import AgoraChat

public protocol AIChatServiceProtocol {
    
    /// 添加监听器
    /// - Parameter listener: 遵守``AIChatListenerProtocol``协议的监听器对象
    func addListener(listener: AIChatListenerProtocol)
    
    /// 移除监听器`
    /// - Parameter listener: 遵守``AIChatListenerProtocol``协议的监听器对象
    func removeListener(listener: AIChatListenerProtocol)
    
    /// 获取聊天记录
    /// - Parameter message: 消息文本内容
    /// - Returns: 消息对象数组以及错误信息
    func sendMessage(message: String) async throws -> (AgoraChatMessage?,AgoraChatError?)?
    
}


@objc public protocol AIChatListenerProtocol: NSObjectProtocol {
    
    /// 收到消息回调
    /// - Parameter messages: 消息对象数组
    func onMessageReceived(messages: [AgoraChatMessage])
    
    /// 消息内容被编辑回调
    /// - Parameter message: 消息对象
    func onMessageContentEdited(message: AgoraChatMessage)
    
}
