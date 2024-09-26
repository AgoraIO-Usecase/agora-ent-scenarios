import Foundation
import AgoraChat
import AgoraCommon

public class AIChatBotImplement: NSObject {
    
    static var commonBot: [AIChatBotProfileProtocol] = []
    
    static var customBot: [AIChatBotProfileProtocol] = []
    
    static var voiceIds = ["avatar1":"female-shaonv","avatar2":"audiobook_female_1","avatar3":"audiobook_male_1","avatar4":"male-qn-badao","avatar5":"audiobook_male_2","avatar6":"clever_boy","avatar7":"male-qn-jingying-jingpin","avatar8":"audiobook_female_1","avatar9":"audiobook_female_1","avatar10":"cute_boy"]
    
    static var commonBotWelcomeMessage = ["agent-001":"今天过得怎么样？有什么有趣的事情想分享吗？","agent-002":"不会又来找我debug吧？","agent-003":"说说看，又遇到什么棘手问题需要我处理了？","agent-004":"工作再忙也要劳逸结合，找我什么事？"]
    
    override init() {
        super.init()
        Task {
            AIChatBotImplement.customBot = (await self.getCustomBotProfile()).0 ?? []
        }
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
                    info.birth = bot.voiceId
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
            bot.botDescription = user.sign ?? "我是您的智能助手，很高兴为您服务。"
            bot.voiceId = user.birth ?? ""
            if bot.botName.isEmpty || bot.botIcon.isEmpty {
                aichatPrint("AIChatBotImplement#mapperBotProfile: botId:\(bot.botId) icon or name is empty")
            }

            if bot.voiceId.isEmpty,let iconName = bot.botIcon.fileName.components(separatedBy: ".").first {
                bot.voiceId = AIChatBotImplement.voiceIds[iconName] ?? "female-chengshu"
            }
            if let prompt = (user.ext?.z.jsonToDictionary() as? [String:Any])?["prompt"] as? String {
                bot.prompt = prompt
            } else {
                bot.prompt = bot.botDescription
            }
            bot.type = type
            if user.userId == nil {
                user.userId = ""
            }
            if !(user.ext ?? "").contains("botIds") {//ext中包含botIds为群组
                print("user ext:\(user.ext ?? "") userId:\(user.userId ?? "")")
                if !(user.userId!).contains("group") {
                    bots.append(bot)
                }
            } else {
                if let id = user.userId {
                    if !id.contains("group") {
                        print("user ext:\(user.ext ?? "") userId:\(user.userId ?? "")")
                        bots.append(bot)
                    }
                }
                
            }
            
            
        }
        return bots
    }
    
    public func createGroupChatBot(groupName: String, bots: [any AIChatBotProfileProtocol], completion: @escaping ((any Error)?, String) -> Void) {
        let model = AIChatUserCreateNetworkModel()
        model.userType = 2
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
                    info.nickname = groupName
                    if let botIcon = bots.last?.botIcon {
                        info.avatarurl = VLUserCenter.user.headUrl+","+botIcon
                    }
                    
                    info.ext = [userId:["botIds":bots.map { $0.botId }]].z.jsonString
                    info.request { error, data in
                        if error == nil {
                            if let response: VLResponseData = data as? VLResponseData {
                                if  response.code != 200 {
                                    errorResult = NSError(domain: "AIChat Error", code: response.code?.intValue ?? 300, userInfo: [ NSLocalizedDescriptionKey : response.message ?? ""])
                                    completion(errorResult, userId)
                                } else {
                                    let conversation = AgoraChatClient.shared().chatManager?.getConversation(info.username, type: .chat, createIfNotExist: true)
                                    conversation?.ext = [info.username:["botIds":bots.map { $0.botId },"groupName":groupName,"groupIcon":info.avatarurl,"bot_group":true]]
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
    
    func groupInfo(groupId: String) -> (String,[String]) {
        let conversation = AgoraChatClient.shared().chatManager?.getConversation(groupId, type: .chat, createIfNotExist: false)
        if var ext = conversation?.ext?[groupId] as? [String:Any] {
            if let groupName = ext["groupName"] as? String, let botIds = ext["botIds"] as? [String] {
                return (groupName,botIds)
            }
        }
        return ("",[])
    }
    
    func updateGroupName(groupId: String, groupName: String, completion: @escaping ((any Error)?) -> Void) {
        let info = AIChatUpdateUserInfoNetworkModel()
        info.interfaceName! += groupId
        info.username = groupId
        info.nickname = groupName
        info.request { error, data in
            if error == nil {
                if let response: VLResponseData = data as? VLResponseData {
                    if  response.code != 200 {
                        completion(NSError(domain: "AIChat Error", code: response.code?.intValue ?? 300, userInfo: [ NSLocalizedDescriptionKey : response.message ?? ""]))
                    } else {
                        let conversation = AgoraChatClient.shared().chatManager?.getConversation(groupId, type: .chat, createIfNotExist: false)
                        if var ext = conversation?.ext?[groupId] as? [String:Any] {
                            ext["groupName"] = groupName
                            conversation?.ext = [groupId:ext]
                            
                        }
                        completion(nil)
                    }
                } else{
                    completion(NSError(domain: "AIChat Error", code: 303, userInfo: [ NSLocalizedDescriptionKey : "返回数据格式不合法"]))
                }
            } else {
                completion(error)
            }
        }
    }
    
    func updateGroupMembers(groupId: String, botIds: [String], completion: @escaping ((any Error)?) -> Void) {
        let info = AIChatUpdateUserInfoNetworkModel()
        info.interfaceName! += groupId
        info.username = groupId
        info.ext = [groupId:["botIds":botIds]].z.jsonString
        info.request { error, data in
            if error == nil {
                if let response: VLResponseData = data as? VLResponseData {
                    if  response.code != 200 {
                        completion(NSError(domain: "AIChat Error", code: response.code?.intValue ?? 300, userInfo: [ NSLocalizedDescriptionKey : response.message ?? ""]))
                    } else {
                        let conversation = AgoraChatClient.shared().chatManager?.getConversation(groupId, type: .chat, createIfNotExist: false)
                        if var ext = conversation?.ext?[groupId] as? [String:Any] {
                            ext["botIds"] = botIds
                            conversation?.ext = [groupId:ext]
                        }
                        completion(nil)
                    }
                } else{
                    completion(NSError(domain: "AIChat Error", code: 303, userInfo: [ NSLocalizedDescriptionKey : "返回数据格式不合法"]))
                }
            } else {
                completion(error)
            }
        }
    }
    
    public func deleteChatBot(botId: String, completion: @escaping ((any Error)?) -> Void) {
        let model = AIChatUserAgentDeleteNetworkModel()
        model.toDeleteUsername = botId
        model.request { error, data in
            if let response: VLResponseData = data as? VLResponseData {
                if  response.code != 200 {
                    completion(error)
                } else {
                    completion(nil)
                }
            } else{
                completion(error)
            }
        }
    }
}


public class AIChatUserAgentDeleteNetworkModel: AUINetworkModel {
    
    public var username = VLUserCenter.user.id
    
    public var toDeleteUsername = "" {
        didSet {
            self.interfaceName! += toDeleteUsername
        }
    }
    
    public override init() {
        super.init()
        self.host = "https://ai-chat-service-staging.sh3t.agoralab.co"
//        AppContext.shared.hostUrl
        self.method = .delete
        self.interfaceName = "/v1/projects/\(AppContext.shared.appId)/chat/users/\(VLUserCenter.user.id)/toDeleteAgent/"
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

public class AIChatUserCreateNetworkModel: AUINetworkModel {
    
    public var userType: UInt32 = 0 //0普通用户 1 agent机器人 2群组用户
    
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
    
    public var birth = "female-chengshu"
    
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
    
    public override func getHttpBody() -> Data? {
        let body = "nickname=\(nickname)&avatarurl=\(avatarurl)&sign=\(sign)&ext=\(ext)&username=\(username)&birth=\(birth)"
        return body.data(using: .utf8)
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


public class AIChatTTSNetworkModel: AUINetworkModel {
    public var text: String = ""
    public var voiceId: String = ""
    public override init() {
        super.init()
        self.host = "https://ai-chat-service-staging.sh3t.agoralab.co"
        self.interfaceName = "/v1/projects/\(AppContext.shared.appId)/voice/tts"
    }
    
    public override func parse(data: Data?) throws -> Any? {
        var dic: Any? = nil
        guard let response = data else { return nil }
        do {
            try dic = try JSONSerialization.jsonObject(with: response, options: .allowFragments) as? Dictionary<String,Any>
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let data = dic["data"] as? [String: Any],
              let url = data["audio"] as? String else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        return url
    }
}
