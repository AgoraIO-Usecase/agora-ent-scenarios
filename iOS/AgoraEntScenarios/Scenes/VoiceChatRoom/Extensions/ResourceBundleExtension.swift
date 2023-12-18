//
//  ResourceBundleExten.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation

private let voice_bundle = Bundle(path: Bundle.main.path(forResource: "VoiceChatRoomResource", ofType: "bundle") ?? "") ?? Bundle.main
public let spatialResourceBundle = Bundle(path: Bundle.main.path(forResource: "SpatialAudioResource", ofType: "bundle") ?? "") ?? Bundle.main

public extension Bundle {
    static var voiceRoomBundle: Bundle { voice_bundle }
    static var spatialRoomBundle: Bundle { spatialResourceBundle }
}
