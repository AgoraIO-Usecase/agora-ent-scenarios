//
//  VoiceChatServiceModel.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import KakaJSON

@objcMembers
class SABaseInfo: NSObject {
    var objectId: String?    //SyncManager获取到的对象带的唯一标识，用于差改删
}

/// 用户信息
@objcMembers
class SAUserModel: SABaseInfo {
    var userName: String = "User-\(UserInfo.userId)"                                //用户名称
    var avatar: String = .init(format: "portrait%02d", Int.random(in: 1...14))      //用户头像
    var userId: String = UserInfo.userId                                            //用户uid
}

enum SARoomType: Int {
    case normal = 0     //普通语聊房
    case spatial = 1    //空间音频
}

/// 房间信息
@objcMembers
class SARoom: SABaseInfo {

    var roomName: String = ""                                            //房间名
    var type: SARoomType = .normal                                //房间类型
    var roomId: String = "\(arc4random_uniform(899999) + 100000)"        //房间Id
    var userId: String = "\(UserInfo.userId)"                            //用户id
    var createAt: Double = 0                                             //创建时间，与19700101时间比较的毫秒数
}

/// 麦位信息
@objcMembers
class SAMicSeat: SABaseInfo {
    var seatIndex: Int = 0     //麦位索引
    var userNo: String?        //上麦用户id，如果为空，则无用户
    var isLock: Bool = false   //是否锁麦
    var isMute: Bool = false   //是否静音
}

/// 申请上麦请求
@objcMembers
class SAMicSeatRequest: SABaseInfo {
    var seatIndex: Int = 0        //座位信息
    var userNo: String?           //申请上麦用户id
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}


/// 邀请上麦请求
@objcMembers
class SAMicSeatInvitation: SABaseInfo {
    var seatIndex: Int = 0        //座位信息
    var userNo: String?           //邀请上麦用户id
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}



/// 聊天消息
@objcMembers
class SAMessage: SABaseInfo {
    var content: String?          //座位信息
    var userNo: String?           //邀请上麦用户id
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}


/// 聊天消息
@objcMembers
class SAGift: SABaseInfo {
    var giftId: String?           //对应礼物id
    var userNo: String?           //邀请上麦用户id
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}


@objcMembers
public class SARobotAudioInfo: NSObject, Convertible {
    var use_robot: Bool = false
    var robot_volume: UInt = 45
    var red_robot_absorb: Bool = false
    var red_robot_blur: Bool = false
    var blue_robot_absorb: Bool = false
    var blue_robot_blur: Bool = false
    var blue_robot_attenuation: Double = 0.2
    var red_robot_attenuation: Double = 0.2
    
    var objectId: String = ""
    
    override public required init() {}
    public func kj_modelKey(from property: Property) -> ModelPropertyKey {
        property.name
    }
}
