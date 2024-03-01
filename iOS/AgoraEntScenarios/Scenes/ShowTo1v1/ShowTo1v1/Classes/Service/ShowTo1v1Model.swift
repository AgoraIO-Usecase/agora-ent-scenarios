//
//  ShowTo1v1Model.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import VideoLoaderAPI

@objcMembers
public class ShowTo1v1UserInfo: NSObject {
    public var uid: String = ""
    public var userName: String = ""
    public var avatar: String = ""
    
    public var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    
    var objectId: String = ""
    
    static func modelCustomPropertyMapper() -> [String : Any]? {
        return ["uid": "userId"]
    }
    
    func getUIntUserId() -> UInt {
        return UInt(uid) ?? 0
    }
    
    func get1V1ChannelId() ->String {
        return "1v1_\(uid)_\(Int64(Date().timeIntervalSince1970 * 1000))"
    }
    
    func bgImage() ->UIImage? {
        let uid = getUIntUserId()
        let image = UIImage.sceneImage(name: "user_bg\(uid % 9 + 1)")
        return image
    }
}

@objcMembers
public class ShowTo1v1RoomInfo: ShowTo1v1UserInfo {
    public var roomId: String = ""
    public var roomName: String = ""
    public var token: String = ""
}

extension ShowTo1v1RoomInfo: IVideoLoaderRoomInfo {
    public var anchorInfoList: [VideoLoaderAPI.AnchorInfo] {
        guard token.count > 0 && getUIntUserId() > 0 else {return []}
        let room = AnchorInfo()
        room.uid = getUIntUserId()
        room.channelName = roomId
        room.token = token
        return [room]
    }
    
    public func channelName() -> String {
        return roomId
    }
    
    public func userId() -> String {
        return self.uid
    }
    
    
}
