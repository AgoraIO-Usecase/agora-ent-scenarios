//
//  AUIKitModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation

/// 房间列表展示数据
@objc(SyncRoomInfo)
@objcMembers open class AUIRoomInfo: NSObject {
    public var roomName: String = ""    //房间名称
    public var roomId: String = ""            //房间id
    public var owner: AUIUserThumbnailInfo?   //房主信息
    public var customPayload: [String: Any] = [:]   //扩展信息
    public var createTime: Int64 = 0
}

///用户简略信息，用于各个模型传递简单数据
@objc(SyncUserThumbnailInfo)
@objcMembers open class AUIUserThumbnailInfo: NSObject {
    public var userId: String = ""      //用户Id
    public var userName: String = ""    //用户名
    public var userAvatar: String = ""  //用户头像
}

let kUserMuteAudioInitStatus = false
let kUserMuteVideoInitStatus = true

//用户信息
@objc(SyncUserInfo)
@objcMembers open class AUIUserInfo: AUIUserThumbnailInfo {
    public var muteAudio: Bool = kUserMuteAudioInitStatus  //是否静音状态
    public var muteVideo: Bool = kUserMuteVideoInitStatus   //是否关闭视频状态
    public var customPayload: String?   //扩展信息
    
    public convenience init(thumbUser: AUIUserThumbnailInfo) {
        self.init()
        self.userId = thumbUser.userId
        self.userName = thumbUser.userName
        self.userAvatar = thumbUser.userAvatar
    }
}
