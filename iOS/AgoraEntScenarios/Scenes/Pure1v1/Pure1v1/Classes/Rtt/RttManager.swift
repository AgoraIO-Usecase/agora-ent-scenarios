//
//  RttManager.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/25.
//

import Foundation

@objc protocol RttEventListener: NSObjectProtocol {
    func onRttStart()
    func onRttStop()
}

class RttManager {
    
    static let shared = RttManager()

    private init() {}

    static func getInstance() -> RttManager {
        return shared
    }
    
    // 记录 STT 功能开启状态的属性
    private var isRttEnabled: Bool = false
    
    var languages: [String] = [
        "kn-IN",
        "gu-IN",
        "te-IN",
        "ta-IN",
        "bn-IN",
        "he-IL",
        "nl-NL",
        "fil-PH",
        "th-TH",
        "vi-VN",
        "tr-TR",
        "ru-RU",
        "ms-MY",
        "fa-IR",
        "zh-HK",
        "id-ID",
        "ar-JO",
        "ar-EG",
        "ar-SA",
        "ar-AE",
        "zh-TW",
        "en-US",
        "hi-IN",
        "ko-KR",
        "ja-JP",
        "de-DE",
        "es-ES",
        "fr-FR",
        "it-IT",
        "zh-CN",
        "pt-PT"
    ]
    
    var languageDisplayNames: [String: String] = [
        "kn-IN": "卡纳达语",
        "gu-IN": "古吉拉特语",
        "te-IN": "泰卢固语",
        "ta-IN": "泰米尔语",
        "bn-IN": "孟加拉语（印度)",
        "he-IL": "希伯来语",
        "nl-NL": "荷兰语",
        "fil-PH": "菲律宾语",
        "th-TH": "泰语",
        "vi-VN": "越南语",
        "tr-TR": "土耳其语",
        "ru-RU": "俄语",
        "ms-MY": "马来语",
        "fa-IR": "波斯语",
        "zh-HK": "中文（粤语）",
        "id-ID": "印度尼西亚语",
        "ar-JO": "阿拉伯语（约旦）",
        "ar-EG": "阿拉伯语（埃及)",
        "ar-SA": "阿拉伯语（沙特)",
        "ar-AE": "阿拉伯语（阿联酋)",
        "zh-TW": "中文（台湾)",
        "en-US": "英语（美国)",
        "hi-IN": "印地语",
        "ko-KR": "韩语",
        "ja-JP": "日语",
        "de-DE": "德语",
        "es-ES": "西班牙语",
        "fr-FR": "法语",
        "it-IT": "意大利语",
        "zh-CN": "中文",
        "pt-PT": "葡萄牙语"
    ]
    
    private let rttListenerList:NSHashTable<RttEventListener> = NSHashTable<RttEventListener>.weakObjects()
    
    // 当前选中的条目索引
    var selectedSourceLanguageIndex: Int = 29
    
    var selectedTargetLanguageIndex: Int = 21
    
    var subBotUid: String = "3000"
    
    var pubBotUid: String = "4000"
    
    var targetUid: String = ""
    
    var subBotToken: String = ""
    
    var pubBotToken: String = ""

    func enableRtt(channelName: String, completion: @escaping ((Bool)->Void)) {
        Pure1v1Logger.info("RttManager enableRtt[\(channelName)] subBotUid[\(subBotUid)] pubBotUid[\(pubBotUid)] targetUid[\(targetUid)] isRttEnabled[\(isRttEnabled)]")
        if (isRttEnabled) {
            completion(false)
            return
        }
        RttApiManager.shared.fetchStartRtt(
            languages: [languages[selectedSourceLanguageIndex]],
            sourceLanguage: languages[selectedSourceLanguageIndex],
            targetLanguages: [languages[selectedTargetLanguageIndex]],
            channelName: channelName,
            subBotUid: self.subBotUid,
            subBotToken: self.subBotToken,
            pubBotUid: self.pubBotUid,
            pubBotToken: self.pubBotToken
        ) { success in
            if (success) {
                self.isRttEnabled = true
                self.rttListenerList.allObjects.forEach { it in
                    it.onRttStart()
                }
            }
            completion(success)
        }
    }

    func disableRtt(force: Bool, completion: @escaping ((Bool)->Void)) {
        Pure1v1Logger.info("RttManager disableRtt force[\(force)] isRttEnabled[\(isRttEnabled)]")
        if (!isRttEnabled) {
            completion(false)
            return
        }
        if (force) {
            self.isRttEnabled = false
            self.rttListenerList.allObjects.forEach { it in
                it.onRttStop()
            }
        }
        RttApiManager.shared.fetchStopRtt { success in
            if (success) {
                self.isRttEnabled = false
                self.rttListenerList.allObjects.forEach { it in
                    it.onRttStop()
                }
            }
            completion(success)
        }
    }

    // 用于检查 STT 功能是否开启的方法
    func checkSttStatus() -> Bool {
        return isRttEnabled
    }
    
    // 设置当前选中的语言代码
    func selectSourceLanguage(at index: Int) {
        guard index >= 0 && index < languages.count else {
            print("Index out of range")
            return
        }
        selectedSourceLanguageIndex = index
    }
    
    // 获取当前选中的语言代码
    func currentSourceLanguageCode() -> String {
        return languages[selectedSourceLanguageIndex]
    }

    // 获取当前选中的语言的中文名字
    func currentSourceLanguageDisplayName() -> String {
        return displayName(for: currentSourceLanguageCode()) ?? ""
    }
    
    // 设置当前选中的语言代码
    func selectTargetLanguage(at index: Int) {
        guard index >= 0 && index < languages.count else {
            print("Index out of range")
            return
        }
        selectedTargetLanguageIndex = index
    }
    
    // 获取当前选中的语言代码
    func currentTargetLanguageCode() -> String {
        return languages[selectedTargetLanguageIndex]
    }

    // 获取当前选中的语言的中文名字
    func currentTargetLanguageDisplayName() -> String {
        return displayName(for: currentTargetLanguageCode()) ?? ""
    }
    
    // 获取语言中文名字的方法
    func displayName(for languageCode: String) -> String {
        return languageDisplayNames[languageCode] ?? ""
    }
    
    func addListener(listener: RttEventListener) {
        if (!rttListenerList.contains(listener)) {
            self.rttListenerList.add(listener)
        }
    }
    
    func removeListener(listener: RttEventListener) {
        if (rttListenerList.contains(listener)) {
            self.rttListenerList.remove(listener)
        }
    }
}
