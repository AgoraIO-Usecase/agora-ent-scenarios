//
//  AIChatImplement.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/29.
//

import UIKit
import AgoraChat

public class AIChatImplement: NSObject {
    
    private var listeners: NSHashTable<AIChatListenerProtocol> = NSHashTable<AIChatListenerProtocol>.weakObjects()
    
    public private(set) var conversationId = ""

    required public init(conversationId: String) {
        super.init()
        self.conversationId = conversationId
        AgoraChatClient.shared().chatManager?.add(self, delegateQueue: nil)
    }
}

extension AIChatImplement: AIChatServiceProtocol {
    
    public func addListener(listener: any AIChatListenerProtocol) {
        if !self.listeners.contains(listener) {
            return
        }
        self.listeners.add(listener)
    }
    
    public func removeListener(listener: any AIChatListenerProtocol) {
        self.listeners.remove(listener)
    }
    
    public func sendMessage(message: String) async throws -> (AgoraChatMessage?,AgoraChatError?)? {
        let message = AgoraChatMessage(conversationID: self.conversationId, body: AgoraChatTextMessageBody(text: message), ext: ["context":""])
        return await AgoraChatClient.shared().chatManager?.send(message, progress: nil)
    }
    
}

extension AIChatImplement: AgoraChatManagerDelegate {
    
    public func onMessageContentChanged(_ message: AgoraChatMessage, operatorId: String, operationTime: UInt) {
        for listener in self.listeners.allObjects {
            listener.onMessageContentEdited(message: message)
        }
    }
    
    public func messagesDidReceive(_ aMessages: [AgoraChatMessage]) {
        for listener in self.listeners.allObjects {
            listener.onMessageReceived(messages: aMessages)
        }
    }
    
    
}
