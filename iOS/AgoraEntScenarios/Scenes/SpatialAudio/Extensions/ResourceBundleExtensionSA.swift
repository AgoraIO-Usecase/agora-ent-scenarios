//
//  ResourceBundleExten.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation

fileprivate let spatial_bundle = Bundle(path: Bundle.main.path(forResource: "SpatialAudioResource", ofType: "bundle") ?? "") ?? Bundle.main

public extension Bundle {
    
    static var spatial: Bundle { spatial_bundle }
    
}
