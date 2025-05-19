//
//  AUIKitModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation

public typealias AUICallback = (NSError?) -> ()

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

@objc public enum AUILockSeatStatus: Int {
    case idle = 0
    case user = 1
    case locked = 2
}

