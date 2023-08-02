//
//  Pure1v1Model.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import VideoLoaderAPI

@objcMembers
public class ShowTo1v1UserInfo: NSObject {
    public var userId: String = ""
    public var userName: String = ""
    public var avatar: String = ""
    
    func getUIntUserId() -> UInt {
        return UInt(userId) ?? 0
    }
}

@objcMembers
public class ShowTo1v1RoomInfo: ShowTo1v1UserInfo {
    public var roomId: String = ""
    public var roomName: String = ""
    
    public var createdAt: Int64 = 0
    
    var objectId: String = ""
    
    func bgImage() ->UIImage? {
        let uid = getUIntUserId()
        let image = UIImage.sceneImage(name: "user_bg\(uid % 9 + 1)")
        return image
    }
    
    func createRoomInfo(token: String) -> RoomInfo {
        let room = RoomInfo()
        room.uid = getUIntUserId()
        room.channelName = roomId
        room.token = token
        return room
    }
}
