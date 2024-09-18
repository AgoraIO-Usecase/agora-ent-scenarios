//
//  AIChatAgentNetworkModel.swift
//  AIChat
//
//  Created by qinhui on 2024/9/10.
//

import Foundation
import AgoraCommon

class AIChatAgentNetworkModel: AUINetworkModel {
    override init() {
        super.init()
        self.host = AppContext.agentServerUrl
    }
}

class AIChatAgentActionModel: AIChatAgentNetworkModel {
    init(appId: String, channelName: String, action: String, method: AUINetworkMethod) {
        super.init()
        self.interfaceName = "/v1/projects/\(appId)/chat/agent/channelName/\(channelName)\(action)"
        self.method = method
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
              let code = dic["code"] as? Int else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        return dic
    }
}

class AIChatAgentStartModel: AIChatAgentActionModel {
    var uid: UInt?
    var voiceId: String?
    var prompt: String?
    var greeting: String?
    
    init(appId: String, channelName: String) {
        super.init(appId: appId, channelName: channelName, action: "", method: .post)
    }
    
    public override func parse(data: Data?) throws -> Any? {
        let dic = try super.parse(data: data) as? [String: Any]
        
        let requestId = dic?["requestId"] as? String
        
        return requestId
    }
}

class AIChatAgentStopModel: AIChatAgentActionModel {
    init(appId: String, channelName: String) {
        super.init(appId: appId, channelName: channelName, action: "", method: .delete)
    }
}

class AIChatAgentPingModel: AIChatAgentActionModel {
    init(appId: String, channelName: String) {
        super.init(appId: appId, channelName: channelName, action: "/ping", method: .post)
    }
}

class AIChatAgentUpdateModel: AIChatAgentActionModel {
    init(appId: String, channelName: String) {
        super.init(appId: appId, channelName: channelName, action: "", method: .delete)
    }
    
    public override func getHeaders() -> [String : String] {
        return ["Content-Type": "application/x-www-form-urlencoded"]
    }
}

class AIChatAgentInterruptModel: AIChatAgentActionModel {
    init(appId: String, channelName: String) {
        super.init(appId: appId, channelName: channelName, action: "/interrupt", method: .delete)
    }
}
