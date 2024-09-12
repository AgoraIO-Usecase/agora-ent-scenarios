//
//  AIChatBotProfile.swift
//  AFNetworking
//
//  Created by 朱继超 on 2024/8/27.
//

import Foundation
import KakaJSON

class AIChatBotProfile: AIChatBotProfileProtocol,Convertible {
    var selected: Bool = false
    
    var botIcon: String = ""
    
    var botId: String = ""
    
    var botName: String = ""
    
    var prompt: String = ""
    
    var botDescription: String = ""
    
    var type: AIChatBotType = .common
    
    required init() {}
    
    func kj_modelKey(from property: Property) -> any ModelPropertyKey {
        property.name
    }
    
    func toDictionary() -> [String : Any] {
        ["AIChatBotProfile": self.kj.JSONObject()]
    }
}


