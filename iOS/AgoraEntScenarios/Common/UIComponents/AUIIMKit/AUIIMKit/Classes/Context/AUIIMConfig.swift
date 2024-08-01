//
//  AUIRoomConfig.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation

@objcMembers open class AUIChatRoomInfo: NSObject {
    var ownerUserId: String = ""   //环信聊天室的管理员。
    var chatRoomId: String = ""    //环信聊天室ID
}

///用户简略信息，用于各个模型传递简单数据
@objcMembers open class AUIChatUserInfo: NSObject {
    public var userId: String = ""      //用户Id
    public var userName: String = ""    //用户名
    public var userAvatar: String = ""  //用户头像
    
    public func isEmpty() -> Bool {
        guard userId.count > 0, userName.count > 0 else {return true}
        
        return false
    }
}

@objcMembers
open class AUIChatCommonConfig: NSObject {
    /// 声网AppId
    public var appId: String = ""
    public var appCert: String = ""
    
    /// 环信AppKey(可选，如果没有用到后端IM服务可以不设置)
    public var imAppKey: String = ""
    /// 环信ClientId(可选，如果没有用到后端IM服务可以不设置)
    public var imClientId: String = ""
    /// 环信ClientSecret(可选，如果没有用到后端IM服务可以不设置)
    public var imClientSecret: String = ""
    
    /// 域名(可选，如果没有用到后端服务可以不设置)
    public var host: String = ""
    /// 用户信息
    public var owner: AUIChatUserInfo?
    
    public func isValidate() -> Bool {
        if appId.isEmpty || owner?.isEmpty() ?? true  {
            return false
        }
        
        return true
    }
}
