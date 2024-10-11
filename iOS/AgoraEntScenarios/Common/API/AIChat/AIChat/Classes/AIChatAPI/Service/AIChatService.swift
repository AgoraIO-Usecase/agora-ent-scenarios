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
    /// - Parameter extensionInfo: 消息扩展信息
    /// - Returns: 消息对象数组以及错误信息
    /// - Note: 该方法为异步方法，调用者需要使用`await`关键字调
    func sendMessage(message: String,extensionInfo: [String:Any]) async -> (AgoraChatMessage?,AgoraChatError?)?
    
    /// 重发消息
    /// - Parameter messageId: 消息id
    /// - Returns: 消息对象以及错误信息
    func resendMessage(messageId: String) async -> (AgoraChatMessage?,AgoraChatError?)?
    
    /// 加载历史消息
    /// - Parameters:
    ///   - messageId: 从哪条消息开始加载历史消息
    ///   - completion: 消息对象数组以及错误信息
    func loadMessages(start messageId: String,completion: @escaping ([AgoraChatMessage]?,AgoraChatError?) -> Void)
    
}


@objc public protocol AIChatListenerProtocol: NSObjectProtocol {
    
    /// 收到消息回调
    /// - Parameter messages: 消息对象数组
    func onMessageReceived(messages: [AgoraChatMessage])
    
    /// 消息内容被编辑回调
    /// - Parameter message: 消息对象
    func onMessageContentEdited(message: AgoraChatMessage)
    
    /// 消息内容编辑完成回调
    /// - Parameter message: 消息对象
    func onMessageContentEditedFinished(message: AgoraChatMessage)
    
    /// 消息状态变更回调
    /// - Parameters:
    ///   - message: 消息对象
    ///   - status: 消息状态
    func onMessageStatusDidChange(message: AgoraChatMessage, status: AgoraChatMessageStatus)
    
}
