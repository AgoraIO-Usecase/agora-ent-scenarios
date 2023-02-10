//
//  VRUser.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import Foundation
import KakaJSON


public enum MicRequestStatus: Int, ConvertibleEnum {
    case idle = 0   // 空闲
    case waitting   // 等待中
    case accepted   // 已接受
    case rejected   // 已拒绝
    case ended      // 已结束
}

@objcMembers open class SAUser: NSObject, Convertible {
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
    
    public var objectId: String?
    public var status: MicRequestStatus = .idle

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc open class SAUserInfo: NSObject {
    public static let shared = SAUserInfo()

    public var user: SAUser?

    public var currentRoomOwner: SAUser?
}
