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
    
    // 是否和房主离线关联，true: 房主不在线则销毁房间，false: 房主不在线不销毁房间
    public var isAssociatedWithOwnerOffline: Bool = true
}
