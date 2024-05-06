//
//  AUIRoomConfig.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation

@objcMembers
open class AUICommonConfig: NSObject {
    /// 声网AppId
    public var appId: String = ""
    /// 声网App证书(可选，如果没有用到后端token生成服务可以不设置)
    public var appCert: String = ""
    
    /// 环信AppKey(可选，如果没有用到后端IM服务可以不设置)
    public var imAppKey: String = ""
    /// 环信ClientId(可选，如果没有用到后端IM服务可以不设置)
    public var imClientId: String = ""
    /// 环信ClientSecret(可选，如果没有用到后端IM服务可以不设置)
    public var imClientSecret: String = ""
    
    /// 域名(可选，如果没有用到后端服务可以不设置)
    public var host: String = "" //(optional)
    /// 用户信息
    public var owner: AUIUserThumbnailInfo?
    
    public func isValidate() -> Bool {
        if appId.isEmpty || owner == nil  {
            return false
        }
        
        return true
    }
}


