//
//  AppContext+AIChat.swift
//  AIChat
//
//  Created by qinhui on 2024/9/10.
//

import Foundation
import AgoraCommon

public class AIChatLogger: NSObject {
    
    public static let kLogKey = "AIChat"
    
    public static func info(_ text: String, context: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: context)
        }
    }

    public static func warn(_ text: String, context: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).warning(text, context: context)
        }
    }

    public static func error(_ text: String, context: String? = nil) {
        agoraDoMainThreadTask {
            AgoraEntLog.getSceneLogger(with: kLogKey).error(text, context: context)
        }
    }
}

extension AppContext {
    static var agentServerUrl = "https://ai-chat-service-staging.sh3t.agoralab.co"

    static private var _rtcService: AIChatRTCService?
    static private var _audioTextConvertorService: AIChatAudioTextConvertorService?
    
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
    
    static func destoryRtcService() {
        _rtcService?.destory()
        _rtcService = nil
    }
    
    static func destoryConvertorService() {
        _audioTextConvertorService?.destory()
        _audioTextConvertorService = nil
    }
}
