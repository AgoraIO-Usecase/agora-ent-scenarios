//
//  SARoomEntity.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation
import KakaJSON

@objc public class SARoomsEntity: NSObject, Convertible {
    public var total: Int? // 总房间数量
    public var cursor: String? // 下一次请求房间列表的cursor
    public var rooms: [SARoomEntity]? // 房间信息数组

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc open class SARoomEntity: NSObject, Convertible {
    public var room_id: String? // 房间id
    public var channel_id: String? // agora rtc channel id
    public var chatroom_id: String? // agora chat chatroom id
    public var name: String? // 房间名称
    public var member_count: Int? = 0 // 房间人数
    public var gift_amount: Int? = 0 // 礼物数
    public var owner: SAUser?
    public var is_private: Bool? // 是否为私密房间
    public var type: Int = 1 // 房间类型， 0 ：普通房间，1:3D房间
    public var created_at: UInt? // 创建房间时间戳，单位毫秒
    public var roomPassword = ""
    public var click_count: Int? = 0 // 观看人数
    public var announcement: String? // Notice
    public var ranking_list: [SAUser]? = [SAUser]() // 土豪榜
    public var member_list: [SAUser]? = [SAUser]() // 用户榜
    public var rtc_uid: Int? = 0
//    public var use_robot: Bool? = false
    public var turn_AIAEC: Bool? = false
    public var turn_AGC: Bool? = false
//    public var robot_volume: UInt?
    public var sound_effect: Int = 1
    
    public var objectId: String?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc open class SARoomMic: NSObject, Convertible {
    var mic_index: Int = 0

    var status: Int = 0 // 0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 5: -1:空闲

    var member: SAUser?
    
    var objectId: String?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
    
    // TODO: shengtao
    var attenuation: Double = 0.2
    var airAbsorb: Bool = false
    var voiceBlur: Bool = false
    
    var pos: [NSNumber]?
    var forward: [NSNumber]?
    var right: [NSNumber]?
    var up: [NSNumber] = [0, 0, 1]
}

@objc open class SAUsers: NSObject, Convertible {
    var ranking_list: [SAUser]?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc open class SARoomInfo: NSObject, Convertible {
    var room: SARoomEntity?
    var mic_info: [SARoomMic]?
    var robotInfo: SARobotAudioInfo = SARobotAudioInfo()

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}
