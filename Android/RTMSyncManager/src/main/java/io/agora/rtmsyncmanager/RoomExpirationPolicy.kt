package io.agora.rtmsyncmanager

/*
 * 房间过期策略模型
 */
class RoomExpirationPolicy {
    // 房间过期时间，单位ms，0表示房间不过期
    var expirationTime: Long = 0
    // 是否和房主离线关联，true: 房主不在线则销毁房间，false: 房主不在线不销毁房间
    var isAssociatedWithOwnerOffline: Boolean = true
}