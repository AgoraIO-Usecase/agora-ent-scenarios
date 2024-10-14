//
//  AIChatConversationImplement.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/29.
//

import UIKit
import AgoraChat
import ZSwiftBaseLib
import KakaJSON

let commonBotIds = ["staging-common-agent-001","staging-common-agent-002","staging-common-agent-003","staging-common-agent-004"]

public class AIChatConversationImplement: NSObject {
    
    @UserDefault("AIChatLoadServerConversationListFinished", defaultValue: false) var localHas
    
    private var listeners: NSHashTable<AIChatConversationListener> = NSHashTable.weakObjects()
    
    public override init() {
        super.init()
        AgoraChatClient.shared().chatManager?.add(self, delegateQueue: nil)
    }

}

extension AIChatConversationImplement: AIChatConversationServiceProtocol {
    public func delete(conversationId: String) async -> AgoraChatError? {
        if let conversation = AgoraChatClient.shared().chatManager?.getConversationWithConvId(conversationId) {
            let serverResult = await AgoraChatClient.shared().chatManager?.deleteServerConversation(conversationId, conversationType: .chat, isDeleteServerMessages: true)
            if serverResult?.1 != nil {
                return serverResult?.1
            }
            let result = await AgoraChatClient.shared().chatManager?.delete([conversation], isDeleteMessages: true)
            return result
        }
        return nil
    }
    
    
    public func addListener(listener: any AIChatConversationListener) {
        if self.listeners.contains(listener) {
            return
        }
        self.listeners.add(listener)
    }
    
    public func removeListener(listener: any AIChatConversationListener) {
        self.listeners.remove(listener)
    }
    
    public func fetchAIConversationList() async -> ([AIChatConversationInfo],AgoraChatError?) {
        if self.localHas {
            let conversations = AgoraChatClient.shared().chatManager?.getAllConversations(true)
            if let list = conversations {
                let ids = list.compactMap { $0.conversationId ?? "" }
                if ids.isEmpty {
                    return ([],nil)
                }
                let infoResult = await AgoraChatClient.shared().userInfoManager?.fetchUserInfo(byId: ids)
                if infoResult?.1 != nil {
                    return ([],infoResult?.1)
                } else {
                    if let infoMap = infoResult?.0 as? [String:AgoraChatUserInfo] {
                        return (self.mapperInfo(conversations: list, infoMap: infoMap),nil)
                    }
                    return (self.mapperInfo(conversations: list, infoMap: [:]),nil)
                }
            } else {
                return ([],nil)
            }
        } else {
            let result = await AgoraChatClient.shared().chatManager?.conversationsFromServer(withCursor: "", pageSize: 20)
            if result?.1 != nil {
                return ([],result?.1)
            } else {
                self.localHas = true
                if let list = result?.0?.list {
                    let ids = list.compactMap { $0.conversationId ?? "" }
                    if ids.isEmpty {
                        return ([],nil)
                    }
                    let infoResult = await AgoraChatClient.shared().userInfoManager?.fetchUserInfo(byId: ids)
                    if infoResult?.1 != nil {
                        return ([],infoResult?.1)
                    } else {
                        if let infoMap = infoResult?.0 as? [String:AgoraChatUserInfo] {
                            return (self.mapperInfo(conversations: list, infoMap: infoMap),nil)
                        }
                        return (self.mapperInfo(conversations: list, infoMap: [:]),nil)
                    }
                } else {
                    return ([],nil)
                }
            }
        }
    }
    
    private func mapperInfo(conversations: [AgoraChatConversation],infoMap:[String:AgoraChatUserInfo]) -> [AIChatConversationInfo] {
        var infos = [AIChatConversationInfo]()
        let botsMap = self.mapperBotProfile(userMaps: infoMap)
        for conversation in conversations {
            if conversation.latestMessage == nil {
                continue
            }
            let info = AIChatConversationInfo()
            info.id = conversation.conversationId
            info.unreadCount = Int(conversation.unreadMessagesCount)
            info.lastMessage = conversation.latestMessage
            if !infoMap.isEmpty {
                info.bot = botsMap[info.id]
                info.avatar = botsMap[info.id]?.botIcon ?? ""
                info.name = botsMap[info.id]?.botName ?? ""
            } else {
                if let botMap = conversation.ext?["AIChatBotProfile"] as? [String:Any] {
                    let bot = model(from: botMap, AIChatBotProfile.self)
                    bot.type = commonBotIds.contains(bot.botId) ? .common : .custom
                    if bot != nil {
                        info.bot = bot
                    }
                    if let iconName = bot.botIcon.fileName.components(separatedBy: ".").first {
                        bot.voiceId = AIChatBotImplement.voiceIds[iconName] ?? "female-chengshu"
                    }
                    if let prompt = botMap["prompt"] as? String {
                        info.bot?.prompt = prompt
                    }
                    info.avatar = bot.botIcon ?? ""
                    info.name = bot.botName ?? info.id
                }
                if commonBotIds.contains(info.id) {
                    for bot in AIChatBotImplement.commonBot {
                        if bot.botId == info.id {
                            bot.type = .common
                            if let iconName = bot.botIcon.fileName.components(separatedBy: ".").first {
                                bot.voiceId = AIChatBotImplement.voiceIds[iconName] ?? "female-chengshu"
                            }
                            info.bot = bot
                            break
                        }
                    }
                } else {
                    info.bot?.type = .custom
                }
                
            }
            if let groupInfo = conversation.ext?[info.id] as? Dictionary<String,Any> {
                info.bot = AIChatBotProfile()
                info.isGroup = true
                info.bot?.botId = info.id
                info.avatar = groupInfo["groupIcon"] as? String ?? ""
                info.name = groupInfo["groupName"] as? String ?? info.id
                info.bot?.botName = info.name
                info.bot?.botIcon = info.avatar
            }
            if let bot = info.bot,( bot.botName.isEmpty || bot.botIcon.isEmpty) {
                aichatPrint("AIChatConversationImplement#mapperBotProfile: conversationId:\(info.id) botId:\(bot.botId) icon or name is empty")
            } else {
                infos.append(info)
            }
        }
        return infos
    }
    
    private func mapperBotProfile(userMaps: [String:AgoraChatUserInfo]) -> [String:AIChatBotProfile] {
        var bots = [String:AIChatBotProfile]()
        for user in userMaps.values {
            let bot = AIChatBotProfile()
            bot.botId = user.userId ?? ""
            bot.botName = user.nickname ?? ""
            bot.botIcon = user.avatarUrl ?? ""
            if bot.botName.isEmpty || bot.botIcon.isEmpty {
                aichatPrint("AIChatConversationImplement#mapperBotProfile: botId:\(bot.botId) icon or name is empty")
            }

            bot.botDescription = user.sign ?? "我是您的智能助手，很高兴为您服务。"
            bot.voiceId = user.birth ?? ""
            if let iconName = bot.botIcon.fileName.components(separatedBy: ".").first {
                bot.voiceId = AIChatBotImplement.voiceIds[iconName] ?? "female-chengshu"
            }
            if let prompt = (user.ext?.z.jsonToDictionary() as? [String:Any])?["prompt"] as? String {
                bot.prompt = prompt
            } else {
                bot.prompt = bot.botDescription
            }
            if commonBotIds.contains(bot.botId) {
                bot.type = .common
            } else {
                bot.type = .custom
            }
            
            bots[user.userId ?? ""] = bot
            
            
        }
        return bots
    }
    
}

extension AIChatConversationImplement: AgoraChatManagerDelegate {
    
    public func conversationListDidUpdate(_ aConversationList: [AgoraChatConversation]) {
        if let conversations = AgoraChatClient.shared().chatManager?.getAllConversations(true) {
//            for listener in self.listeners.allObjects {
//                listener.onAIConversationListChanged(self.mapperInfo(conversations: conversations, infoMap: [:]))
//            }
        }
    }
    
    public func messagesDidReceive(_ aMessages: [AgoraChatMessage]) {
        
        if let conversations = AgoraChatClient.shared().chatManager?.getAllConversations(true) {
            for listener in self.listeners.allObjects {
                listener.onAIConversationLastMessageChanged(self.mapperInfo(conversations: conversations, infoMap: [:]))
            }
        }
    }
    
}
