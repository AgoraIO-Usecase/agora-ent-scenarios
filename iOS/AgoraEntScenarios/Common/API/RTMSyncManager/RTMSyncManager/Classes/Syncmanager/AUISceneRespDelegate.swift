//
//  AUISceneRespDelegate.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/1/25.
//

import Foundation

/// Scene操作对应的响应
@objc public protocol AUISceneRespDelegate: NSObjectProtocol {
    
    @objc optional func onTokenPrivilegeWillExpire(channelName: String?)

    /// 房间被销毁的回调
    /// - Parameter roomId: 房间id
    @objc optional func onSceneDestroy(roomId: String)
    
    /// Description 房间用户被踢出房间
    ///
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userId: 用户id
    @objc optional func onSceneUserBeKicked(roomId: String,userId: String)
}
