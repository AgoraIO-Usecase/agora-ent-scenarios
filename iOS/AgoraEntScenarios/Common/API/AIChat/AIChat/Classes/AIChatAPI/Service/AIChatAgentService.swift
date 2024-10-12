//
//  AIChatAgentService.swift
//  AIChat
//
//  Created by qinhui on 2024/9/9.
//

import Foundation
import AgoraCommon

typealias AgentRequestCompletion = (String?, Error?) -> Void

class AIChatAgentService {
    private var channelName: String
    private var appId: String
        
    init(channelName: String, appId: String) {
        self.channelName = channelName
        self.appId = appId
    }
    
    func startAgent(prompt: String,
                    voiceId: String,
                    greeting: String?,
                    context: [[String:Any]]?,
                    completion: AgentRequestCompletion?) {
        let uid = VLUserCenter.user.id
        let model = AIChatAgentStartModel(appId: appId, channelName: channelName)
        model.uid = NSNumber(string: uid )
        model.prompt = prompt
        model.voiceId = voiceId
        model.greeting = greeting
        model.context = context
        model.request { error, data in
            if let error = error {
                completion?(nil, error)
                return
            }
            
            var agentId: String = ""
            if let dic = data as? [String: Any] {
                let taskId = dic["taskId"] as? String ?? ""
                agentId = "\(dic["agentId"] as? Int ?? 0)"
                let pasteboard = UIPasteboard.general
                pasteboard.string = "voice chat taskId: \(taskId)"
            }
            completion?(agentId, nil)
        }
    }
    
    func stopAgent(completion: AgentRequestCompletion?) {
        let uid = VLUserCenter.user.id
        let model = AIChatAgentStopModel(appId: appId, channelName: channelName)
        model.request { error, data in
            completion?(nil, error)
        }
    }
    
    func pingAgent(completion: AgentRequestCompletion?) {
        let model = AIChatAgentPingModel(appId: appId, channelName: channelName)
        model.request { error, data in
            completion?(nil, error)
        }
    }
    
    func voiceInterruptAgent(enable: Bool, completion: AgentRequestCompletion?) {
        let model = AIChatAgentUpdateModel(appId: appId, channelName: channelName)
        model.isFlushAllowed = enable
        model.request { error, data in
            completion?(nil, error)
        }
    }
    
    func interruptAgent(completion: AgentRequestCompletion?) {
        let model = AIChatAgentInterruptModel(appId: appId, channelName: channelName)
        model.request { error, data in
            completion?(nil, error)
        }
    }
}
