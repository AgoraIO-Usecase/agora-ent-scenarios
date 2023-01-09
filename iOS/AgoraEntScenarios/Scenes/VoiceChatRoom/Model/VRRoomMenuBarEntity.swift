//
//  VRRoomMenuBarEntity.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation

@objcMembers open class VRRoomMenuBarEntity: Codable {
    var title: String = ""
    var detail: String = ""
    var selected: Bool = false
    var soundType: Int = 0
    var index: Int? = 0
}
