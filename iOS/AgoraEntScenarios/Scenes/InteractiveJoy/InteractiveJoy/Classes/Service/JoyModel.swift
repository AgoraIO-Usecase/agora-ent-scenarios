//
//  JoyModel.swift
//  Joy
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation

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
    var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    
    var objectId: String = ""
}
