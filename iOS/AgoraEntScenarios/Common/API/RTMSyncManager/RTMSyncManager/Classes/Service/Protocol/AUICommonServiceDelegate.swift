//
//  AUICommonServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/8.
//

import Foundation

@objc public protocol AUICommonServiceDelegate: NSObjectProtocol {
    
    func getChannelName() -> String
    
    /// init param if scene init
    /// - Parameter completion: <#completion description#>
    @objc optional func initService(completion:  @escaping  ((NSError?) -> ()))
    
    /// room setup succss
    @objc optional func sereviceDidLoad()
    
    /// clean param if scene deinit
    /// - Parameter completion: <#completion description#>
    @objc optional func deinitService(completion:  @escaping  ((NSError?) -> ()))
    
    
    /// clean user info
    /// - Parameters:
    ///   - userId: user id
    ///   - completion: completion
    @objc optional func cleanUserInfo(userId: String, completion:  @escaping  ((NSError?) -> ()))
    
    /// 获取当前房间上下文
    /// - Returns: <#description#>
    func getRoomContext() -> AUIRoomContext
}

extension AUICommonServiceDelegate {
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func currentUserIsRoomOwner() -> Bool {
        return getRoomContext().isRoomOwner(channelName: getChannelName())
    }
    
    public func getLockOwnerId() -> String? {
        return getRoomContext().getArbiter(channelName: getChannelName())?.lockOwnerId
    }
}
