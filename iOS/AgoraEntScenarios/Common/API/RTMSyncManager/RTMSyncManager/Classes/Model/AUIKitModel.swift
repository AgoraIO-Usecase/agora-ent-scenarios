//
//  AUIKitModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation

/// 房间列表展示数据
@objcMembers open class AUIRoomInfo: NSObject {
    public var roomName: String = ""    //房间名称
    public var roomId: String = ""            //房间id
    public var owner: AUIUserThumbnailInfo?   //房主信息
    public var customPayload: [String: Any] = [:]   //扩展信息
}

///用户简略信息，用于各个模型传递简单数据
@objcMembers open class AUIUserThumbnailInfo: NSObject {
    public var userId: String = ""      //用户Id
    public var userName: String = ""    //用户名
    public var userAvatar: String = ""  //用户头像
}

let kUserMuteAudioInitStatus = false
let kUserMuteVideoInitStatus = true

//用户信息
@objcMembers open class AUIUserInfo: AUIUserThumbnailInfo {
    public var muteAudio: Bool = kUserMuteAudioInitStatus  //是否静音状态
    public var muteVideo: Bool = kUserMuteVideoInitStatus   //是否关闭视频状态
}
