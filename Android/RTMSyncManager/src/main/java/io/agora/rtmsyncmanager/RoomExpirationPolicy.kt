package io.agora.rtmsyncmanager

/**
 * Class representing the room expiration policy.
 */
class RoomExpirationPolicy {
    /**
     * The expiration time of the room in milliseconds.
     * A value of 0 indicates that the room does not expire.
     */
    var expirationTime: Long = 0

    /**
     * A flag indicating whether the room's existence is associated with the owner's online status.
     * If true, the room is destroyed when the owner goes offline.
     * If false, the room persists even when the owner goes offline.
     */
    var isAssociatedWithOwnerOffline: Boolean = true

    /**
     * The maximum time in milliseconds that the owner can be offline before the room is destroyed.
     */
    var ownerReconnectMaxTime: Long = 5 * 60 * 1000
}