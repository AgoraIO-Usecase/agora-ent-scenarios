//
//  AIChatAgentService.swift
//  AIChat
//
//  Created by qinhui on 2024/9/9.
//

import Foundation
import AgoraCommon

typealias agentRequestCompletion = (String, Error?) -> Void

class AIChatAgentService {
    private var channelName: String
    private var appId: String
    
    init(channelName: String, appId: String) {
        self.channelName = channelName
        self.appId = appId
    }
    
    private func handleResponse(error: Error?, data: Any, completion: agentRequestCompletion?) {
        guard let completion = completion else { return }

        if let response: VLResponseData = data as? VLResponseData {
            if response.code != 200 {
                completion(response.message ?? "", nil)
            } else {
                completion("", error)
            }
        } else {
            completion("", error)
        }
    }
    
    func startAgent(prompt: String,
                    voiceId: String,
                    completion: agentRequestCompletion?) {
        let uid = VLUserCenter.user.id
        let model = AIChatAgentStartModel(appId: appId, channelName: channelName)
        model.uid = UInt(uid) ?? 0
        model.prompt = prompt
        model.voiceId = "male-qn-qingse"
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
    
    func stopAgent(completion: agentRequestCompletion?) {
        let uid = VLUserCenter.user.id
        let model = AIChatAgentStopModel(appId: appId, channelName: channelName)
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
    
    func pingAgent(completion: agentRequestCompletion?) {
        let model = AIChatAgentPingModel(appId: appId, channelName: channelName)
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
    
    func updateAgent(completion: agentRequestCompletion?) {
        let model = AIChatAgentUpdateModel(appId: appId, channelName: channelName)
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
    
    func interruptAgent(completion: agentRequestCompletion?) {
        let model = AIChatAgentInterruptModel(appId: appId, channelName: channelName)
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
}
