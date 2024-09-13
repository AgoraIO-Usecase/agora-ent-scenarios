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
}

class AIChatAgentStartModel: AIChatAgentActionModel {
    var uid: UInt?
    var voiceId: String?
    var prompt: String?
    
    init(appId: String, channelName: String) {
        super.init(appId: appId, channelName: channelName, action: "", method: .post)
    }
}

class AIChatAgentStopModel: AIChatAgentActionModel {
    init(appId: String, channelName: String) {
        super.init(appId: appId, channelName: channelName, action: "", method: .delete)
    }
}

class AIChatAgentPingModel: AIChatAgentActionModel {
    init(appId: String, channelName: String) {
        super.init(appId: appId, channelName: channelName, action: "/ping", method: .delete)
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
