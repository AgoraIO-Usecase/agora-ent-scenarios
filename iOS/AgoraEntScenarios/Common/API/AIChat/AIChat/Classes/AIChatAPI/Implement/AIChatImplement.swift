//
//  AIChatImplement.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/29.
//

import UIKit
import AgoraChat
import AgoraCommon
import SVProgressHUD

public class AIChatImplement: NSObject {
    
    private var listeners: NSHashTable<AIChatListenerProtocol> = NSHashTable<AIChatListenerProtocol>.weakObjects()
    
    public private(set) var conversationId = ""

    required public init(conversationId: String) {
        super.init()
        self.conversationId = conversationId
    }
    
    func initAIChatSceneRequired(completion: @escaping (Error?) -> Void) {
        let taskGroup = DispatchGroup()
        let queue = DispatchQueue(label: "com.imsdk.initialize")
        let queue1 = DispatchQueue(label: "com.token.get")
        let queue2 = DispatchQueue(label: "com.user.create")
        
        var initIMFinished = false
        var tokenFinished = false
        var userCreateFinished = false
        
        var errorResult: Error?
        
        var token = ""
        
        SVProgressHUD.show()
        taskGroup.enter()
        queue.async { [weak self] in
            let options = AgoraChatOptions(appkey: AppContext.shared.imAppKey)
            options.loadEmptyConversations = true
            options.enableConsoleLog = true
            if let error = AgoraChatClient.shared().initializeSDK(with: options) {
                ToastView.show(text: "initializeIMSDK error:\(error.errorDescription ?? "")", postion: .center)
                errorResult = NSError(domain: "AIChat Error", code: error.code.rawValue, userInfo: [ NSLocalizedDescriptionKey : error.errorDescription ?? ""])
                taskGroup.leave()
                return
            }
            initIMFinished = true
            taskGroup.leave()
        }

        taskGroup.enter()
        queue1.async {
            let generator = AIChatTokenGenerator()
            generator.request { (error, result) in
                errorResult = error
                if let response: VLResponseData = result as? VLResponseData {
                    if response.code != 200 {
                        if response.code != 1201 {
                            ToastView.show(text: response.message ?? "", postion: .center)
                        } else {
                            tokenFinished = true
                        }
                    } else {
                        if let tokenResult = response.data as? Dictionary<String,Any> {
                            token = (tokenResult["token"] as? String) ?? ""
                        }
                        
                        tokenFinished = true
                    }
                } else {
                    ToastView.show(text: error?.localizedDescription ?? "", postion: .center)
                }
                taskGroup.leave()
            }
        }
        
        taskGroup.enter()
        queue2.async {
            let model = AIChatUserCreateNetworkModel()
            model.request { error, data in
                errorResult = error
                if let response: VLResponseData = data as? VLResponseData {
                    if  response.code != 200 {
                        if response.code != 1201 {
                            ToastView.show(text: response.message ?? "", postion: .center)
                        } else {
                            userCreateFinished = true
                        }
                    } else {
                        userCreateFinished = true
                        
                    }
                } else {
                    ToastView.show(text: error?.localizedDescription ?? "", postion: .center)
                }
                taskGroup.leave()
            }
        }

        taskGroup.notify(queue: .main) { [weak self] in
            let result = initIMFinished && tokenFinished && userCreateFinished
            if result {
                self?.login(token: token, completion: completion)
            } else {
                SVProgressHUD.dismiss()
                completion(errorResult)
            }
        }
    }
    
    func login(token: String,completion: @escaping (Error?) -> Void) {
        AgoraChatClient.shared().login(withUsername: VLUserCenter.user.id, agoraToken: token) { [weak self] (userID, error) in
            SVProgressHUD.dismiss()
            if error != nil {
                if error?.code == .userAlreadyLoginSame {
                    completion(nil)
                } else {
                    completion(NSError(domain: "AIChat Error", code: error?.code.rawValue ?? 303, userInfo: [ NSLocalizedDescriptionKey : error?.errorDescription ?? ""]))
                }
            } else {
                completion(nil)
            }
        }
    }
    
    func addChatListener() {
        AIChatRTCService.shared.run(appId: AppContext.shared.appId, channelName: "ai_chat")
        AgoraChatClient.shared().chatManager?.remove(self)
        AgoraChatClient.shared().chatManager?.add(self, delegateQueue: nil)
    }
}

extension AIChatImplement: AIChatServiceProtocol {
    
    public func addListener(listener: any AIChatListenerProtocol) {
        if self.listeners.contains(listener) {
            return
        }
        self.listeners.add(listener)
        self.addChatListener()
    }
    
    public func removeListener(listener: any AIChatListenerProtocol) {
        self.listeners.remove(listener)
    }
    
    public func sendMessage(message: String,extensionInfo: [String:Any] = [:]) async -> (AgoraChatMessage?,AgoraChatError?)? {
        let message = AgoraChatMessage(conversationID: self.conversationId, body: AgoraChatTextMessageBody(text: message), ext: extensionInfo)
        return await AgoraChatClient.shared().chatManager?.send(message, progress: nil)
    }
    
    public func loadMessages(start messageId: String,completion: @escaping ([AgoraChatMessage]?,AgoraChatError?) -> Void) {
        AgoraChatClient.shared().chatManager?.getConversationWithConvId(self.conversationId)?.loadMessagesStart(fromId: messageId, count: Int32(messagesPageSize), searchDirection: .up,completion: { messages, error in
            completion(messages,error)
        })
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
    
    public func cmdMessagesDidReceive(_ aCmdMessages: [AgoraChatMessage]) {
        for message in aCmdMessages {
            if let body = message.body as? AgoraChatCmdMessageBody,body.action == "AIChatEditEnd" {
                if let info = message.ext?["ai_chat"] as? Dictionary<String,Any>,let messageId = info["edit_end_message_id"] as? String,let message = AgoraChatClient.shared().chatManager?.getMessageWithMessageId(messageId) {
                    for listener in self.listeners.allObjects {
                        listener.onMessageContentEditedFinished(message: message)
                    }
                }
                return
            }
        }
        
    }
}

class AIChatTokenGenerator: AUINetworkModel {
    
    var uid = VLUserCenter.user.id
    
    var expire = UInt32(60*60*24)
    
    var channelName = ""
    
    override init() {
        super.init()
        self.method = .post
        self.host = "https://ai-chat-service-staging.sh3t.agoralab.co"
        self.interfaceName = "/v1/projects/\(AppContext.shared.appId)/chat/token"
    }
    
    override func parse(data: Data?) throws -> Any? {
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
