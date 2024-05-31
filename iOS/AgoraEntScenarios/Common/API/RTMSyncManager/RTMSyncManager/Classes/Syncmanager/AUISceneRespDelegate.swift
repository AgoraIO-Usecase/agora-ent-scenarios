//
//  AUISceneRespDelegate.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/1/25.
//

import Foundation

/// Scene操作对应的响应
@objc public protocol AUISceneRespDelegate: NSObjectProtocol {
    
    /// 即将更新metadata
    /// - Parameter channelName: <#channelName description#>
    /// - Returns: 返回的map对象[key: value]，key: collection id，value：初始化结构，为map则是map collection，为array则是list collection
    @objc optional func onWillInitSceneMetadata(channelName: String) -> [String: Any]?
    
    
    /// token即将过期
    /// - Parameter channelName: <#channelName description#>
    @objc optional func onTokenPrivilegeWillExpire(channelName: String?)

    /// 房间过期的回调
    /// - Parameter channelName: <#channelName description#>
    @objc optional func onSceneExpire(channelName: String)
    
    /// 房间被销毁的回调
    /// - Parameter channelName: 房间id
    @objc optional func onSceneDestroy(channelName: String)
    
    /// Description 房间用户被踢出房间
    ///
    /// - Parameters:
    ///   - channelName: 房间id
    ///   - userId: 用户id
    @objc optional func onSceneUserBeKicked(channelName: String, userId: String)
}
