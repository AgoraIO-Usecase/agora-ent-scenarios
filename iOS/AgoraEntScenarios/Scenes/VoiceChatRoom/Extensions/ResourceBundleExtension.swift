//
//  ResourceBundleExten.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation

fileprivate let voice_bundle = Bundle(path: Bundle.main.path(forResource: "VoiceChatRoomResource", ofType: "bundle") ?? "") ?? Bundle.main


public extension Bundle {
    
    static var voiceChat: Bundle { voice_bundle }
    
}
