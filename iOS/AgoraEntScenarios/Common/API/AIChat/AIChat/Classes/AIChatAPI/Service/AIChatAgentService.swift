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
    
    func startAgent(channelName: String,
                    prompt: String,
                    voiceId: String,
                    completion: agentRequestCompletion?) {
        let uid = VLUserCenter.user.id
        let model = AIChatAgentStartModel(appId: AppContext.shared.appId, channelName: channelName)
        model.uid = UInt(uid) ?? 0
        model.prompt = prompt
        model.voiceId = "male-qn-qingse"
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
    
    func stopAgent(channelName: String, 
                   completion: agentRequestCompletion?) {
        let uid = VLUserCenter.user.id
        let model = AIChatAgentStopModel(appId: AppContext.shared.appId, channelName: channelName)
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
    
    func pingAgent(channelName: String,
                   completion: agentRequestCompletion?) {
        let model = AIChatAgentPingModel(appId: AppContext.shared.appId, channelName: channelName)
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
    
    func updateAgent(channelName: String,
                     completion: agentRequestCompletion?) {
        let model = AIChatAgentUpdateModel(appId: AppContext.shared.appId, channelName: channelName)
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
    
    func interruptAgent(channelName: String,
                        completion: agentRequestCompletion?) {
        let model = AIChatAgentInterruptModel(appId: AppContext.shared.appId, channelName: channelName)
        model.request { error, data in
            self.handleResponse(error: error, data: data, completion: completion)
        }
    }
}
