//
//  VRRoomEntity.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation
import KakaJSON
import RTMSyncManager

enum VoiceMusicPlayStatus {
    case pause
    case download
    case playing
    case none
}

class VoiceMusicModel: NSObject {
    var name: String?
    var singer: String?
    var songCode: Int = 0
    var status: VoiceMusicPlayStatus = .none
}

@objc public class VRRoomsEntity: NSObject, Convertible {
    public var total: Int? // 总房间数量
    public var cursor: String? // 下一次请求房间列表的cursor
    public var rooms: [VRRoomEntity]? // 房间信息数组

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc open class VRRoomEntity: NSObject, Convertible {
    public var room_id: String? // 房间id
    public var channel_id: String? // agora rtc channel id
    public var chatroom_id: String? // agora chat chatroom id
    @objc public var name: String? // 房间名称
    public var member_count: Int? = 0 // 房间人数
    public var gift_amount: Int? = 0 // 礼物数
    public var owner: VRUser?
    @objc public var is_private: Bool = false // 是否为私密房间
    public var type: Int? = 0 // 房间类型， 0 ：普通房间，1:3D房间
    public var created_at: UInt? // 创建房间时间戳，单位毫秒
    @objc public var roomPassword = ""
    public var click_count: Int? = 0 // 观看人数
    public var announcement: String? // Notice
    public var ranking_list: [VRUser]? = [VRUser]() // 土豪榜
    public var member_list: [VRUser]? = [VRUser]() // 用户榜
    public var rtc_uid: Int? = 0
    public var use_robot: Bool? = false
    public var turn_AIAEC: Bool? = false
    public var turn_AGC: Bool? = false
    public var turn_InEar: Bool? = false
    public var inEar_volume: Int = 100
    public var inEarMode: String?
    public var robot_volume: UInt?
    public var sound_effect: Int = 1
    public var musicIsOrigin: Bool = false 
    var backgroundMusic: VoiceMusicModel?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

enum VRRoomMicStatus: Int {
    case idle = -1              //空闲
    case normal = 0             //正常
    case close = 1              //闭麦
    case forbidden = 2          //禁言
    case lock = 3               //锁麦
    case forbiddenAndLock = 4   //锁麦和禁言
}

@objc open class VRRoomMic: NSObject, Convertible {
    var mic_index: Int = 0

    var status: Int = 0 // 0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 -1:空闲

    var member: VRUser?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
    
    open override var description: String {
        return "seat: \(mic_index), status: \(status), memeber: \(member?.rtc_uid ?? "empty")"
    }
}

@objc open class VRUsers: NSObject, Convertible {
    var ranking_list: [VRUser]?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}

@objc open class VRRoomInfo: NSObject, Convertible {
    var room: VRRoomEntity?
    var mic_info: [VRRoomMic]?

    override public required init() {}

    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}
