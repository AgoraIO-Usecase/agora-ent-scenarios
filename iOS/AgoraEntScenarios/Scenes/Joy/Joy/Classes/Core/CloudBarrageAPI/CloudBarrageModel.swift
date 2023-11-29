//
//  CloudBarrageModel.swift
//  Joy
//
//  Created by wushengtao on 2023/11/29.
//

import Foundation

public class CloudGameUser: NSObject {
    public var openId:String?
    public var avatar: String?
    public var nickname: String?
}

//游戏信息
public class CloudGameInfo: NSObject, Codable {
    public var gameId: String?
    public var name: String?
    public var vendor: String?
    public var thumbnail: String?
    public var introduce: String?
    public var vendorGameId: String?
}

public class CloudGameFeatureInfo: NSObject {
    public var comment: Int = 0
    public var like: Int = 0
}

/// 游戏礼物
public class CloudGameGiftInfo: NSObject, Codable {
    public var giftId: String?
    public var name: String?
    public var price: Int = 0
    public var thumbnail: String?
    public var vendorGiftId: String?
    public var gameId: String?
}

/// 游戏详情信息
public class CloudGameDetailInfo: CloudGameInfo {
    public var gifts: [CloudGameGiftInfo]?
    public var instrunct: [Any]?
    public var feature: CloudGameFeatureInfo?
}

/// 评论内容
public class CloudGameSendCommentConfig: NSObject {
    public var vid: String?
    public var roomId: String?
    public var msgId: String?
    public var user: CloudGameUser?
    public var content: String?
    public var timestamp: Int?
}


/// 点赞内容
public class CloudGameSendLikeConfig: NSObject {
    public var vid: String?
    public var roomId: String?
    public var msgId: String?
    public var user: CloudGameUser?
    public var likeNum = 1
    public var timestamp: Int?
}

// 开播内容
public class CloudGameEncryption: NSObject {
    public var mode:Int = 0             // agora加密mode
    public var secret:String?           // agora加密secret
    public var salt: String?            // agora加密salt
}

public class CloudGameStartConfig: NSObject {
    public var vid: String?                        // agora vid
    public var roomId: String?                     // 主播room_id
    public var user: CloudGameUser?                // 主播信息
    public var broadcastUid: UInt = 0              // 主播uid
    public var assistantUid: UInt = 0              // 主播助手rtc uid
    public var token: String?                      // 主播助手rtc token
    public var channelName: String?                // agora rtc cname
    public var encryption: CloudGameEncryption?    // agora encryption，无加密不填
}


/// 更新token
public class CloudGameTokenConfig: NSObject {
    public var vid: String?
    public var roomId: String?
    public var openId: String?
    public var taskId: String?
    public var rtcUid: String?       // 主播助手rtc uid
    public var rtcToken: String?     // 主播助手rtc uid
}
