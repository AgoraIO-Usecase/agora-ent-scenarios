//
//  ResourceBundleExten.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation

public let resourceBundle = Bundle(path: Bundle.main.path(forResource: "VoiceChatRoomResource", ofType: "bundle") ?? "") ?? Bundle.main

public extension Bundle {
    
    static var voiceRoomBundle: Bundle { resourceBundle }
}
