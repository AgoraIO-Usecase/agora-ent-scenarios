import Foundation
import AgoraChat
import AgoraCommon

public class AIChatBotImplement: NSObject {
    
    override init() {
        super.init()
    }
    
}

//MARK: - AIChatBotServiceProtocol Implementation
extension AIChatBotImplement: AIChatBotServiceProtocol {
    
    public func getCommonBots(botIds: [String]) async -> ([any AIChatBotProfileProtocol]?, AgoraChatError?) {
        let result = await AgoraChatClient.shared().userInfoManager?.fetchUserInfo(byId: botIds)
        if result?.1 != nil {
            return (nil,result?.1)
        } else {
            if let usersMap = result?.0 as? Dictionary<String,AgoraChatUserInfo> {
                return (self.mapperBotProfile(userMaps: usersMap, bot: .common),nil)
            } else {
                return (nil,AgoraChatError(description: "parser  error", code: .invalidParam))
            }
        }
    }
    
    public func getCustomBotProfile() async -> ([any AIChatBotProfileProtocol]?, AgoraChatError?) {
        let result = await AgoraChatClient.shared().contactManager?.contactsFromServer()
        if result?.1 != nil {
            return (nil,result?.1)
        } else {
            let contacts = result?.0 ?? []
            let userResult = await AgoraChatClient.shared().userInfoManager?.fetchUserInfo(byId: contacts)
            if userResult?.1 != nil {
                return (nil,userResult?.1)
            } else {
                if let usersMap = userResult?.0 as? Dictionary<String,AgoraChatUserInfo> {
                    return (self.mapperBotProfile(userMaps: usersMap,bot: .custom),nil)
                } else {
                    return (nil,AgoraChatError(description: "parser  error", code: .invalidParam))
                }
            }
        }
    }
    
    public func createChatBot(bot: any AIChatBotProfileProtocol, completion: @escaping ((any Error)?,String) -> Void) {
        let model = AIChatUserCreateNetworkModel()
        model.userType = 1
        model.request { error, data in
            if let response: VLResponseData = data as? VLResponseData {
                if  response.code != 200 {
                    completion(error,"")
                } else {
                    var userId = ""
                    if let data = response.data as? Dictionary<String,Any> {
                        if let botId = data["username"] as? String {
                            userId = botId
                        }
                    }
                    var errorResult: Error?
                    let info = AIChatUpdateUserInfoNetworkModel()
                    info.interfaceName! += userId
                    info.username = userId
                    info.nickname = bot.botName
                    info.avatarurl = bot.botIcon
                    info.sign = bot.prompt
                    info.ext = ["prompt":bot.botDescription].z.jsonString
                    info.request { error, data in
                        if error == nil {
                            if let response: VLResponseData = data as? VLResponseData {
                                if  response.code != 200 {
                                    errorResult = NSError(domain: "AIChat Error", code: response.code?.intValue ?? 300, userInfo: [ NSLocalizedDescriptionKey : response.message ?? ""])
                                    completion(errorResult, userId)
                                } else {
                                    completion(nil, userId)
                                }
                            } else{
                                completion(NSError(domain: "AIChat Error", code: 303, userInfo: [ NSLocalizedDescriptionKey : "返回数据格式不合法"]), userId)
                            }
                        } else {
                            completion(error, userId)
                        }
                    }
                }
            } else{
                completion(error, "")
            }
        }
    }
    
    private func mapperBotProfile(userMaps: [String:AgoraChatUserInfo],bot type: AIChatBotType) -> [AIChatBotProfileProtocol] {
        var bots = [AIChatBotProfile]()
        for user in userMaps.values {
            let bot = AIChatBotProfile()
            bot.botId = user.userId ?? ""
            bot.botName = user.nickname ?? ""
            bot.botIcon = user.avatarUrl ?? ""
            bot.prompt = user.sign ?? ""
            if let prompt = (user.ext?.z.jsonToDictionary() as? [String:Any])?["prompt"] as? String {
                bot.botDescription = prompt
            }
            bot.type = type
            bots.append(bot)
        }
        return bots
    }
}


public class AIChatUserCreateNetworkModel: AUINetworkModel {
    
    public var userType: UInt32 = 0
    
    public var username = VLUserCenter.user.id
    
    public override init() {
        super.init()
        self.host = "https://ai-chat-service-staging.sh3t.agoralab.co"
//        AppContext.shared.hostUrl
        self.method = .post
        self.interfaceName = "/v1/projects/\(AppContext.shared.appId)/chat/users"
    }
    
    public override func parse(data: Data?) throws -> Any? {
        var dic: Any? = nil
        guard let response = data else { return nil }
        do {
            try dic = try JSONSerialization.jsonObject(with: response, options: .allowFragments) as? Dictionary<String,Any>
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        let rooms = dic.kj.model(VLResponseData.self)
        return rooms
    }
}


public class AIChatUpdateUserInfoNetworkModel: AUINetworkModel {
    
    public var nickname = ""
    
    public var avatarurl = ""
    
    public var sign = ""
    
    public var ext = ""
        
    public var username = VLUserCenter.user.id
    
    public override init() {
        super.init()
        self.host = "https://ai-chat-service-staging.sh3t.agoralab.co"
//        AppContext.shared.hostUrl
        self.method = .put
        self.interfaceName = "/v1/projects/\(AppContext.shared.appId)/chat/metadata/user/"
    }
    
    public override func getHeaders() -> [String : String] {
        return ["Content-Type": "application/x-www-form-urlencoded"]
    }
    
    
    public override func parse(data: Data?) throws -> Any? {
        var dic: Any? = nil
        guard let response = data else { return nil }
        do {
            try dic = try JSONSerialization.jsonObject(with: response, options: .allowFragments) as? Dictionary<String,Any>
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        let rooms = dic.kj.model(VLResponseData.self)
        return rooms
    }
}


public class AIChatAddFriendNetworkModel: AUINetworkModel {
    
    public var friendId = "" {
        didSet {
            self.interfaceName = "/v1/projects/\(AppContext.shared.appId)/chat/users/\(VLUserCenter.user.id)/contacts/users/\(self.friendId)"
        }
    }
        
    public override init() {
        super.init()
        self.host = "https://ai-chat-service-staging.sh3t.agoralab.co"
    }
    
    
}
