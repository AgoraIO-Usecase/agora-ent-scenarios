//
//  AIChatAgentService.swift
//  AIChat
//
//  Created by qinhui on 2024/9/9.
//

import Foundation
import AgoraCommon
import AgoraRtcKit

typealias AgentRequestCompletion = (String?, Error?) -> Void

class AIChatAgentService: NSObject {
    private var channelName: String
    private var appId: String
    private var robotRtcUid: Int = 0
    private var dataStreamCallbackMap: [String: AgentRequestCompletion] = [:]
    private var timer: Timer?
        
    required init(channelName: String, appId: String) {
        self.channelName = channelName
        self.appId = appId
        super.init()
    }
    
    func startAgent(prompt: String,
                    voiceId: String,
                    greeting: String?,
                    context: [[String:Any]]?,
                    completion: AgentRequestCompletion?) {
        let uid = AppContext.shared.getAIChatUid()
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
            self.robotRtcUid = Int(agentId) ?? 0
            completion?(agentId, nil)
        }
        
        AppContext.rtcService()?.addDelegate(channelName: channelName, delegate: self)
    }
    
    func stopAgent(completion: AgentRequestCompletion?) {
        let uid = AppContext.shared.getAIChatUid()
        let model = AIChatAgentStopModel(appId: appId, channelName: channelName)
        model.request { error, data in
            completion?(nil, error)
        }
        AppContext.rtcService()?.removeDelegate(channelName: channelName, delegate: self)
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
//        let model = AIChatAgentInterruptModel(appId: appId, channelName: channelName)
//        model.request { error, data in
//            completion?(nil, error)
//        }
        guard robotRtcUid > 0 else {
            aichatPrint("voiceInterruptAgent fail! robotRtcUid == 0")
            completion?(nil, NSError(domain: "robot rtc uid is empty", code: -1))
            return
        }
        
        let messageId = AppContext.rtcService()?.sendDataStream(channelName: channelName, to: robotRtcUid, cmd: "flush")
        if let messageId = messageId, let completion = completion {
            dataStreamCallbackMap[messageId] = completion
            
            timer = Timer.scheduledTimer(withTimeInterval: 5, repeats: false, block: {[weak self] _ in
                completion(nil, NSError(domain: "voiceInterruptAgent request time out", code: -1))
                self?.dataStreamCallbackMap[messageId] = nil
            })
        }
    }
}

extension AIChatAgentService: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        let message = String.init(data: data, encoding: .utf8) ?? ""
        aichatPrint("receiveDataStreamMessageFromUid: \(uid) \(message)")
        let map = message.z.jsonToDictionary()
        guard let type = map["type"] as? Int,
              type == 1,
              let messageId = map["messageId"] as? String,
              let callback = dataStreamCallbackMap[messageId] else {
            return
        }
        
        dataStreamCallbackMap[messageId] = nil
        callback(nil, nil)
    }
}
