//
//  AppContext+AIChat.swift
//  AIChat
//
//  Created by qinhui on 2024/9/10.
//

import Foundation
import AgoraCommon

extension AppContext {
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
