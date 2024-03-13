//
//  AUIRoomContext.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation

public class AUIRoomContext: NSObject {
    public static let shared: AUIRoomContext = AUIRoomContext()
    public let currentUserInfo: AUIUserThumbnailInfo = AUIUserThumbnailInfo()
    public var commonConfig: AUICommonConfig? {
        didSet {
            guard let userInfo = commonConfig?.owner else {return}
            currentUserInfo.userName = userInfo.userName
            currentUserInfo.userId = userInfo.userId
            currentUserInfo.userAvatar = userInfo.userAvatar
        }
    }
    
//    public var rtmToken: String = ""
    public var roomOwnerMap: [String: String] = [:]
    public var roomArbiterMap: [String: AUIArbiter] = [:]
    
    public var seatCount: UInt = 8
    
    public func isRoomOwner(channelName: String) ->Bool {
        return isRoomOwner(channelName: channelName, userId: currentUserInfo.userId)
    }
    
    public func isRoomOwner(channelName: String, userId: String) ->Bool {
        return roomOwnerMap[channelName] == userId
    }
    
    public func getArbiter(channelName: String) -> AUIArbiter? {
//        guard let _ = roomInfoMap[channelName] else {return nil}
        if let handler = roomArbiterMap[channelName] {
            return handler
        }
        
//        assert(false, "arbiter == nil!")
        return nil
    }
    
    public func clean(channelName: String) {
        roomOwnerMap[channelName] = nil
        roomArbiterMap[channelName] = nil
    }
}
