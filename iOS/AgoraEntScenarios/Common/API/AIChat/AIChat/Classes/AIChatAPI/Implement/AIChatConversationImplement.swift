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
                let infoResult = await AgoraChatClient.shared().userInfoManager?.fetchUserInfo(byId: ids)
                if infoResult?.1 != nil {
                    return ([],infoResult?.1)
                } else {
                    return (self.mapperInfo(conversations: list),nil)
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
                    let infoResult = await AgoraChatClient.shared().userInfoManager?.fetchUserInfo(byId: ids)
                    if infoResult?.1 != nil {
                        return ([],infoResult?.1)
                    } else {
                        return (self.mapperInfo(conversations: list),nil)
                    }
                } else {
                    return ([],nil)
                }
            }
        }
    }
    
    private func mapperInfo(conversations: [AgoraChatConversation]) -> [AIChatConversationInfo] {
        var infos = [AIChatConversationInfo]()
        for conversation in conversations {
            let info = AIChatConversationInfo()
            info.id = conversation.conversationId
            info.unreadCount = Int(conversation.unreadMessagesCount)
            info.lastMessage = conversation.latestMessage
            if let botMap = conversation.ext?["AIChatBotProfile"] as? [String:Any] {
                let bot = model(from: botMap, AIChatBotProfile.self)
                bot.type = commonBotIds.contains(bot.botId) ? .common : .custom
                if bot != nil {
                    info.bot = bot
                }
                if let prompt = botMap["prompt"] as? String {
                    info.bot?.prompt = prompt
                }
                info.avatar = bot.botIcon ?? ""
                info.name = bot.botName ?? info.id
            }
            if let groupInfo = conversation.ext?[info.id] as? Dictionary<String,Any> {
                info.bot = AIChatBotProfile()
                info.bot?.botId = info.id
                info.avatar = groupInfo["groupIcon"] as? String ?? ""
                info.name = groupInfo["groupName"] as? String ?? info.id
                info.bot?.botName = info.name
                info.bot?.botIcon = info.avatar
            }
            infos.append(info)
        }
        return infos
    }
    
}

extension AIChatConversationImplement: AgoraChatManagerDelegate {
    
    public func conversationListDidUpdate(_ aConversationList: [AgoraChatConversation]) {
        if let conversations = AgoraChatClient.shared().chatManager?.getAllConversations(true) {
            for listener in self.listeners.allObjects {
                listener.onAIConversationListChanged(self.mapperInfo(conversations: conversations))
            }
        }
    }
    
    public func messagesDidReceive(_ aMessages: [AgoraChatMessage]) {
        
        if let conversations = AgoraChatClient.shared().chatManager?.getAllConversations(true) {
            for listener in self.listeners.allObjects {
                listener.onAIConversationLastMessageChanged(self.mapperInfo(conversations: conversations))
            }
        }
    }
    
}
