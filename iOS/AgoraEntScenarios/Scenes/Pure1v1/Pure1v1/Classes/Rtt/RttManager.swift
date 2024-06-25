//
//  RttManager.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/25.
//

import Foundation

class RttManager {
    
    static let shared = RttManager()

    private init() {}

    static func getInstance() -> RttManager {
        return shared
    }
    
    // 记录 STT 功能开启状态的属性
    private var isRttEnabled: Bool = false
        
    var languages: [String] = ["zh-CN", "en-US"]
    
    var sourceLanguage: String = "zh-CN"
    
    var targetLanguages: [String] = ["en-US"]
    
    var subBotUid: String = "1000"
    
    var pubBotUid: String = "2000"

    func enableRtt(channelName: String, subBotToken: String, pubBotToken: String, completion: @escaping ((Bool)->Void)) {
        RttApiManager.shared.fetchStartRtt(
            languages: self.languages,
            sourceLanguage: self.sourceLanguage,
            targetLanguages: self.targetLanguages,
            channelName: channelName,
            subBotUid: self.subBotUid,
            subBotToken: subBotToken,
            pubBotUid: self.pubBotUid,
            pubBotToken: pubBotToken
        ) { success in
            if (success) {
                self.isRttEnabled = true
            }
            completion(success)
        }
    }

    func disableRtt(completion: @escaping ((Bool)->Void)) {
        RttApiManager.shared.fetchStopRtt { success in
            if (success) {
                self.isRttEnabled = false
            }
            completion(success)
        }
    }

    // 用于检查 STT 功能是否开启的方法
    func checkSttStatus() -> Bool {
        return isRttEnabled
    }
}
