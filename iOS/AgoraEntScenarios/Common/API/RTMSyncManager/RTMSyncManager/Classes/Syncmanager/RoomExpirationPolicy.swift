//
//  RoomExpirationPolicy.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/5/6.
//

import Foundation

// 房间过期策略模型
public class RoomExpirationPolicy: NSObject {
    // 房间过期时间，单位ms，0表示房间不过期
    public var expirationTime: UInt64 = 0
    
    /// 房主离线最长时间，单位ms，超过该时间认为房间被销毁，0表示不处理
    public var ownerReconnectMaxTime: UInt64 = 5 * 60 * 1000
    
    // 是否和房主离线关联，true: 房主不在线则销毁房间，false: 房主不在线不销毁房间
    public var isAssociatedWithOwnerOffline: Bool = true
    
    public static func defaultPolicy() -> RoomExpirationPolicy {
        let policy = RoomExpirationPolicy()
        policy.expirationTime = 20 * 60 * 1000
        
        return policy
    }
}
