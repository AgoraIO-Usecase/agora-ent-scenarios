//
//  VoiceChatServiceModel.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

@objcMembers
class VoiceChatBaseInfo: NSObject {
    var objectId: String?    //SyncManager获取到的对象带的唯一标识，用于差改删
}

/// 用户信息
@objcMembers
class VoiceChatUsers: VoiceChatBaseInfo {
    var userName: String = "User-\(UserInfo.userId)"
    var avatar: String = .init(format: "portrait%02d", Int.random(in: 1...14))
    var userId: String = UserInfo.userId
    //    var status: PKApplyInfoStatus? = .end
    var timestamp: String = "".timeStamp16
    var isEnableVideo: Bool? = false
    var isEnableAudio: Bool? = false
}

enum VoiceChatRoomType: Int {
    case normal = 0     //普通语聊房
    case spatial
}

/// 房间信息
@objcMembers
class VoiceChatRoom: VoiceChatBaseInfo {
    var roomName: String = ""
    var type: VoiceChatRoomType = .normal
    var roomId: String = "\(arc4random_uniform(899999) + 100000)"
    var userId: String = "\(UserInfo.userId)"
    var backgroundId: String = .init(format: "portrait%02d", Int.random(in: 1...2))
    
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}

/// 麦位信息
@objcMembers
class VoiceChatMicSeat: VoiceChatBaseInfo {
    var seatIndex: Int = 0     //麦位索引
    var userNo: String?        //上麦用户id，如果为空，则无用户
    var isLock: Bool = false   //是否锁麦
    var isMute: Bool = false   //是否静音
}

/// 申请上麦请求
@objcMembers
class VoiceChatMicSeatRequest: VoiceChatBaseInfo {
    var seatIndex: Int = 0        //座位信息
    var userNo: String?           //申请上麦用户id
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}


/// 邀请上麦请求
@objcMembers
class VoiceChatMicSeatInvitation: VoiceChatBaseInfo {
    var seatIndex: Int = 0        //座位信息
    var userNo: String?           //邀请上麦用户id
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}



/// 聊天消息
@objcMembers
class VoiceChatMessage: VoiceChatBaseInfo {
    var content: String?          //座位信息
    var userNo: String?           //邀请上麦用户id
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}


/// 聊天消息
@objcMembers
class VoiceChatGift: VoiceChatBaseInfo {
    var giftId: String?           //对应礼物id
    var userNo: String?           //邀请上麦用户id
    var createAt: Double = 0      //创建时间，与19700101时间比较的毫秒数
}
