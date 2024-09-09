import Foundation
import AgoraChat
import KakaJSON

public enum AIChatBotType: UInt8,ConvertibleEnum {
    case common
    case custom
}

public protocol AIChatBotProfileProtocol {
    var botName: String {set get}//智能体名称
    var botIcon: String {set get}//智能体头像
    var botId: String {set get}//智能体id
    var prompt: String {set get}//智能体提示
    var botDescription: String {set get}//智能体描述
    var type: AIChatBotType {set get}//智能体类型
    func toDictionary() -> [String: Any]
}

public protocol AIChatBotServiceProtocol {
    
    /// 获取公共智能体
    /// - Parameter botIds: 智能体id字符串数组 
    /// - Returns: 遵守智能体信息协议的对象数组以及错误信息
    func getCommonBots(botIds: [String]) async -> ([AIChatBotProfileProtocol]?,AgoraChatError?)
    
    /// 获取自定义智能体
    /// - Returns: 遵守智能体信息协议的对象数组以及错误信息
    func getCustomBotProfile() async -> ([AIChatBotProfileProtocol]?,AgoraChatError?)
    
    /// 创建智能体
    /// - Parameters:
    ///   - bot:遵守智能体信息协议的对象
    ///   - completion: 创建结果回调，错误为空则成功
    func createChatBot(bot: AIChatBotProfileProtocol, completion: @escaping (Error?,String) -> Void)
}
