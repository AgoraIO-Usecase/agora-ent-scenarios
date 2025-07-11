import Foundation
import AgoraChat
import KakaJSON

@objc public enum AIChatBotType: UInt8,ConvertibleEnum {
    case common
    case custom
}

@objc public protocol AIChatBotProfileProtocol {
    var botName: String {set get}//智能体名称
    var botIcon: String {set get}//智能体头像
    var botId: String {set get}//智能体id
    var prompt: String {set get}//智能体提示
    var voiceId: String {set get}//智能体语音银色id
    var botDescription: String {set get}//智能体描述
    var type: AIChatBotType {set get}//智能体类型
    var selected: Bool {set get}//是否选中
    func toDictionary() -> [String: Any]
}

extension AIChatBotProfileProtocol {
    func backgroundIcon() -> String {
        return botIcon.replacingOccurrences(of: "avatar", with: "bg").replacingOccurrences(of: "png", with: "jpg")
    }
}

public protocol AIChatBotServiceProtocol {
    
    func commonBotIds(completion: @escaping ([String],Error?) -> Void)
    
    /// 获取公开智能体
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
    
    /// 创建智能体群聊
    /// - Parameters:
    ///   - groupName: 群聊名称
    ///   - bots: 智能体信息协议对象数组
    ///   - completion: 创建结果回调，错误为空则成功
    func createGroupChatBot(groupName: String, bots: [AIChatBotProfileProtocol] ,completion: @escaping (Error?,String) -> Void)
    
    /// 删除智能体
    /// - Parameters:
    ///   - botId: 智能体id
    ///   - completion: 删除结果回调，错误为空为成功
    func deleteChatBot(botId: String, completion: @escaping (Error?) -> Void)
}
