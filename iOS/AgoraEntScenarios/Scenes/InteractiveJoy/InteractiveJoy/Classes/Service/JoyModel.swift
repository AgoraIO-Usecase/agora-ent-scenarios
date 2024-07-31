//
//  JoyModel.swift
//  Joy
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import RTMSyncManager

@objcMembers
public class InteractiveJoyUserInfo: NSObject {
    public var userId: UInt = 0
    public var userName: String = ""
    public var avatar: String = ""
    public var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    
    var objectId: String = ""
}

@objcMembers
public class InteractiveJoyRoomInfo: NSObject {
    var roomId: String = ""        //房间号
    var roomName: String?          //房间名
    var roomUserCount: Int = 1     //房间人数
    var thumbnailId: String?       //缩略图id
    var ownerId: UInt = 0           //房主user id (rtc uid)
    var ownerAvatar: String?       //房主头像
    var ownerName: String?         //房主名
    var badgeTitle: String = ""    //胖可争霸/羊羊抗狼
    var gameId: Int64 = 0          //游戏Id
    var password: String?          //房间密码
    var isPrivate: Bool = false    //是否为私密房间
    var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    
    var objectId: String = ""
}

class PlayRobotInfo: NSObject, Codable {
    var gender: String?            //性别 male：男 female：女
    var level: Int = 0             //机器人等级 1:简单 2：适中 3：困难
    var owner: AUIUserThumbnailInfo?
}
