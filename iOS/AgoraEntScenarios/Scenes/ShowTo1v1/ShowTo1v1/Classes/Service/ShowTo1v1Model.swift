//
//  ShowTo1v1Model.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import VideoLoaderAPI
import RTMSyncManager

@objcMembers
public class ShowTo1v1UserInfo: NSObject {
    public var uid: String = ""
    public var userName: String = ""
    public var avatar: String = ""
    
    convenience init(userInfo: AUIUserInfo) {
        self.init()
        self.uid = userInfo.userId
        self.userName = userInfo.userName
        self.avatar = userInfo.userAvatar
    }
    
    static func modelCustomPropertyMapper() -> [String : Any]? {
        return ["uid": "userId"]
    }
    
    func getUIntUserId() -> UInt {
        return UInt(uid) ?? 0
    }
    
    func bgImage() ->UIImage? {
        let uid = getUIntUserId()
        let image = UIImage.sceneImage(name: "user_bg\(uid % 9 + 1)")
        return image
    }
}

private let kCreateAtKey = "createdAt"

@objcMembers
public class ShowTo1v1RoomInfo: ShowTo1v1UserInfo {
    public var roomId: String = ""
    public var roomName: String = ""
    public var token: String = ""
    public var createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    
    convenience init(roomInfo: AUIRoomInfo) {
        self.init()
        self.roomId = roomInfo.roomId
        self.roomName = roomInfo.roomName
        if let owner = roomInfo.owner {
            self.uid = owner.userId
            self.avatar = owner.userAvatar
            self.userName = owner.userName
        } else {
            assert(false)
        }
        if let createTime = roomInfo.customPayload[kCreateAtKey] as? Int64 {
            self.createdAt = createTime
        }
    }
    
    func convertAUIRoomInfo()  -> AUIRoomInfo {
        let roomInfo = AUIRoomInfo()
        roomInfo.roomId = self.roomId
        roomInfo.roomName = self.roomName
        let owner = AUIUserThumbnailInfo()
        owner.userId = uid
        owner.userName = userName
        owner.userAvatar = avatar
        roomInfo.owner = owner
        roomInfo.customPayload = [kCreateAtKey: createdAt]
        return roomInfo
    }
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
