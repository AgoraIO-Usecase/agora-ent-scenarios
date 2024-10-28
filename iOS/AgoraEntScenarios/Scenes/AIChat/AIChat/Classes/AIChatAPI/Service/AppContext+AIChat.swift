//
//  AppContext+AIChat.swift
//  AIChat
//
//  Created by qinhui on 2024/9/10.
//

import Foundation
import AgoraCommon


func aichatPrint(_ message: String, context: String? = "UI") {
    AIChatLogger.info(message, context: context)
}

func aichatWarn(_ message: String, context: String? = "UI") {
    AIChatLogger.warn(message, context: context)
}

func aichatError(_ message: String, context: String? = "UI") {
    AIChatLogger.error(message, context: context)
}

class AIChatLogger: NSObject {
    static let kLogKey = "AIChat"
    
    static func info(_ text: String, context: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: context)
        }
    }

    static func warn(_ text: String, context: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).warning(text, context: context)
        }
    }

    static func error(_ text: String, context: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).error(text, context: context)
        }
    }
}

extension AppContext {
    static private var _agentServerUrl: String?
    static private var _rtcService: AIChatRTCService?
    static private var _audioTextConvertorService: AIChatAudioTextConvertorService?
    static private var _speechManager:SpeechManager?
    
    static func agentServerUrl() -> String {
        if let url = _agentServerUrl {
            return url
        }
        
        let url = AppContext.shared.aichatAgentHost
        _agentServerUrl = url
        return url
    }
    
    static func rtcService() -> AIChatRTCService? {
        if let service = _rtcService {
            return  service
        }
        
        _rtcService = AIChatRTCService(appId: AppContext.shared.appId, convertService: AppContext.audioTextConvertorService())
        return _rtcService
    }
    
    static func audioTextConvertorService() -> AIChatAudioTextConvertorService? {
        if let service = _audioTextConvertorService {
            return service
        }
        
        _audioTextConvertorService = AIChatAudioTextConvertorService()
        return _audioTextConvertorService
    }
    
    static func speechManager() -> SpeechManager? {
        if let manager = _speechManager {
            return manager
        }
        
        _speechManager = SpeechManager()
        return _speechManager
    }
    
    static func destory() {
        _agentServerUrl = nil
        _rtcService?.destory()
        _rtcService = nil
        
        _audioTextConvertorService?.destory()
        _audioTextConvertorService = nil
        
        _speechManager = nil
    }
    
    func getAIChatUid() -> String {
        let uid = Int(VLUserCenter.user.id) ?? 0
        return "\(isDebugMode ? uid + 1000000 : uid)"
    }
}
