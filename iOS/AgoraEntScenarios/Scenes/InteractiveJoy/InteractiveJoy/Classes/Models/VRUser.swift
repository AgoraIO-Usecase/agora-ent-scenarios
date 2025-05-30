//
//  VRUser.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import Foundation
import KakaJSON

@objcMembers open class VRUser: NSObject, Convertible {
    public var uid: String?
    public var chat_uid: String?
    public var channel_id: String?
    public var name: String?
    public var portrait: String?
    public var invited = false
    public var amount: Int? = 0
    public var mic_index: Int?
    public var rtc_uid: String?
    public var volume: Int = 0 // 麦克风音量
    public var micStatus: Int = 1 //1开麦 0自主静麦

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc open class VoiceRoomUserInfo: NSObject {
    public static let shared = VoiceRoomUserInfo()

    public var user: VRUser?

    public var currentRoomOwner: VRUser?
}
