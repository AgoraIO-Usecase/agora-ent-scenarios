//
//  CloudBarrageModel.swift
//  Joy
//
//  Created by wushengtao on 2023/11/29.
//

import Foundation

//public struct CloudGameUser: Codable {
//    public var openId:String?
//    public var avatar: String?
//    public var nickname: String?
//    
//    enum CodingKeys: String, CodingKey {
//        case openId = "open_id"
//        case avatar, nickname
//    }
//}

//游戏信息
public struct CloudGameInfo: Codable {
    public var gameId: String?
    public var name: String?
    public var vendor: String?
    public var thumbnail: String?
    public var introduce: String?
    public var vendorGameId: String?

    enum CodingKeys: String, CodingKey {
        case gameId = "game_id"
        case vendorGameId = "vendor_game_id"
        case name, vendor, thumbnail, introduce
    }
}

public struct CloudGameFeatureInfo: Codable {
    public var comment: Int = 0
    public var like: Int = 0
    
    enum CodingKeys: String, CodingKey {
        case comment, like
    }
}

/// 游戏礼物
public struct CloudGameGiftInfo: Codable {
    public var giftId: String?
    public var name: String?
    public var price: Int = 0
    public var thumbnail: String?
    public var vendorGiftId: String?
    public var gameId: String?
    
    enum CodingKeys: String, CodingKey {
        case giftId = "id"
        case price = "value"
        case vendorGiftId = "vendor_gift_id"
        case gameId = "game_id"
        case name, thumbnail
    }
}

/// 游戏详情信息
public struct CloudGameDetailInfo: Codable {
    public var gameId: String?
    public var name: String?
    public var vendor: String?
    public var thumbnail: String?
    public var introduce: String?
    public var vendorGameId: String?
    
    public var gifts: [CloudGameGiftInfo]?
    public var instrunct: [String]?
    public var feature: CloudGameFeatureInfo?
    
    enum CodingKeys: String, CodingKey {
        case gameId = "game_id"
        case vendorGameId = "vendor_game_id"
        case name, vendor, thumbnail, introduce, gifts, instrunct, feature
    }
}

/// 评论内容
public struct CloudGameCommentInfo: Codable {
    public var msgId: String?
    public var userId:String?
    public var userAvatar: String?
    public var userName: String?
    public var content: String?
    public var timestamp: Int?
    
    enum CodingKeys: String, CodingKey {
        case msgId = "msg_id"
        case userId = "open_id"
        case userAvatar = "avatar"
        case userName = "nickname"
        case content, timestamp
    }
}

public struct CloudGameSendCommentConfig: Codable {
    public var vid: String?
    public var roomId: String?
    public var commentList: [CloudGameCommentInfo]?
    
    enum CodingKeys: String, CodingKey {
        case vid
        case roomId = "room_id"
        case commentList = "payload"
    }
}

/// 点赞内容
public struct CloudGameSendLikeInfo: Codable {
    public var msgId: String?
    public var userId:String?
    public var userAvatar: String?
    public var userName: String?
    public var likeNum = 1
    public var timestamp: Int?
    
    enum CodingKeys: String, CodingKey {
        case msgId = "msg_id"
        case userId = "open_id"
        case userAvatar = "avatar"
        case userName = "nickname"
        case likeNum, timestamp
    }
}

public struct CloudGameSendLikeConfig: Codable {
    public var vid: String?
    public var roomId: String?
    public var likeList: [CloudGameCommentInfo]?
    
    enum CodingKeys: String, CodingKey {
        case vid
        case roomId = "room_id"
        case likeList = "payload"
    }
}

// 开始游戏
public struct CloudGameEncryption: Codable {
    public var mode:Int = 0             // agora加密mode
    public var secret:String?           // agora加密secret
    public var salt: String?            // agora加密salt
    
    enum CodingKeys: String, CodingKey {
        case mode, secret, salt
    }
}

public struct CloudGameRtcConfig: Codable {
    public var broadcastUid: UInt = 0              // 主播uid
    public var assistantUid: UInt = 0              // 主播助手rtc uid
    public var assistantToken: String?             // 主播助手rtc token
    public var channelName: String?                // agora rtc cname
    public var encryption: CloudGameEncryption?    // agora encryption，无加密不填
    
    enum CodingKeys: String, CodingKey {
        case assistantUid = "uid"
        case assistantToken = "token"
        case broadcastUid, channelName, encryption
    }
}

public struct CloudGameStartConfig: Codable {
    public var roomId: String?                     // 主播room_id
    public var gameId: String?                     // 游戏id
    public var userId: String?
    public var userAvatar: String?
    public var userName: String?
    public var rtcConfig: CloudGameRtcConfig?
    
    enum CodingKeys: String, CodingKey {
        case roomId, gameId, rtcConfig
        case userId = "openId"
        case userAvatar = "avatar"
        case userName = "nickname"
    }
}


/// 更新token
public struct CloudGameTokenConfig: Codable {
    public var vid: String?
    public var roomId: String?
    public var userId: String?
    public var taskId: String?
    public var rtcUid: String?       // 主播助手rtc uid
    public var rtcToken: String?     // 主播助手rtc uid
    
    enum CodingKeys: String, CodingKey {
        case vid
        case roomId = "room_id"
        case userId = "open_id"
        case taskId = "task_id"
        case rtcUid = "rtc_uid"
        case rtcToken = "rtc_token"
    }
}
