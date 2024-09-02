package io.agora.rtmsyncmanager

import android.util.Log
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter
import io.agora.rtmsyncmanager.utils.AUILogger
import java.util.*

/**
 * Class representing the conditions for entering a room in the Agora RTM Sync Manager.
 *
 * This class checks whether the conditions for entering a room are met.
 */
class AUISceneEnterCondition(private val channelName: String, private val arbiter: AUIArbiter) {
    private val kConditionKey = "AUICondition"
    var enterCompletion: (() -> Unit)? = null

    var lockOwnerRetrieved: Boolean = false
        set(value) {
            field = value
            checkRoomValid()
        }

    var lockOwnerAcquireSuccess: Boolean = false
        set(value) {
            field = value
            checkRoomValid()
        }

    var subscribeSuccess: Boolean = false
        set(value) {
            field = value
            checkRoomValid()
        }

    var ownerId: String = ""
        set(value) {
            field = value
            AUIRoomContext.shared().roomOwnerMap[channelName] = ownerId
            checkRoomValid()
        }

    /**
     * Checks if the room is valid for entering.
     *
     * The conditions for entering a room are:
     * 1. Subscription is successful.
     * 2. The initialization information is obtained (the owner's uid is obtained).
     * 3. The lock is also successfully obtained (the callback of acquire is successfully received,
     *    and the metadata can be successfully written only after receiving the callback.
     *    Even if the lock is obtained but the callback is not received, the metadata writing will fail).
     * 4. The lock owner is obtained (modifying metadata requires sending a message to the lock owner).
     */
    private fun checkRoomValid() {
        AUILogger.logger().d(kConditionKey, "checkRoomValid:$channelName subscribeSuccess:$subscribeSuccess lockOwnerRetrieved:$lockOwnerRetrieved ownerId:$ownerId")
        if (subscribeSuccess && lockOwnerRetrieved && ownerId.isNotEmpty()) {
            if (arbiter.isArbiter() && !lockOwnerAcquireSuccess) return
            enterCompletion?.invoke()
        }
    }
}

/**
 * Class representing the conditions for a room to expire in the Agora RTM Sync Manager.
 *
 * This class checks whether the conditions for a room to expire are met.
 */
class AUISceneExpiredCondition(private val channelName: String, private val roomExpiration: RoomExpirationPolicy) {
    private val tag = "AUISceneExpiredCondition"
    private var lastUpdateDate: Date? = null
    var roomDidExpired: (() -> Unit)? = null

    var offlineTimestamp: Long = 0
        set(value) {
            field = value
            AUILogger.logger().d(tag, "[$channelName]did offline: $value")
        }

    var joinCompletion: Boolean = false
        set(value) {
            field = value
            checkRoomExpired()
        }

    var createTimestamp: Long? = null
        set(value) {
            field = value
            checkRoomExpired()
        }

    var userSnapshotList: List<AUIUserInfo>? = null
        set(value) {
            field = value
            checkRoomExpired()
        }

    /**
     * A flag indicating whether the room owner has left the room.
     */
    var ownerHasLeftRoom: Boolean = false
        set(value) {
            field = value
            checkRoomExpired()
        }

    var lastUpdateTimestamp: Long? = null
        set(value) {
            field = value
            lastUpdateDate = Date()
            checkRoomExpired()
        }

    fun reconnectNow(timestamp: Long) {
        AUILogger.logger().d(tag, "[$channelName]reconnectNow: currentTs:$timestamp, offlineTs:$offlineTimestamp")
        if (offlineTimestamp <= 0 || roomExpiration.ownerReconnectMaxTime <= 0) {
            return
        }
        if (timestamp - offlineTimestamp <= roomExpiration.ownerReconnectMaxTime) {
            return
        }
        offlineTimestamp = 0
        roomDidExpired?.invoke()
    }

    /**
     * Checks if the room has expired.
     *
     * The conditions for a room to expire are:
     * 1. The room owner needs to check if the room owner is in the user list (queried by who now).
     * 2. The audience joins to check if the room owner is not in the user list.
     * 3. The room time has expired (dynamically configured, not directly written to death for 20 minutes).
     */
    private fun checkRoomExpired() {
        AUILogger.logger().d(tag, "checkRoomExpired[$channelName] joinCompletion: $joinCompletion, userSnapshotList count: ${userSnapshotList?.size}, createTimestamp: $createTimestamp")
        val userList = userSnapshotList ?: return
        val cts = createTimestamp ?: return
        if (!joinCompletion) return

        if (roomExpiration.isAssociatedWithOwnerOffline) {
            val isRoomOwner = AUIRoomContext.shared().isRoomOwner(channelName)
            if (isRoomOwner) {
                //step 1
                if (ownerHasLeftRoom) {
                    AUILogger.logger().d(tag, "checkRoomExpired: room owner has left")
                    roomDidExpired?.invoke()
                }
            } else {
                //step 2
                if (userList.none { AUIRoomContext.shared().isRoomOwner(channelName, it.userId) }) {
                    //room owner not found, clean room
                    AUILogger.logger().d(tag, "checkRoomExpired: room owner leave")
                    roomDidExpired?.invoke()
                    return
                }
            }
        }

        //TODO: Currently, the check only has enter room. If the room owner is always in the room, it will not expire.
        // Whether to check internally or let the upper layer handle it.
        //step 3
        val lts = lastUpdateTimestamp ?: return
        if (roomExpiration.expirationTime > 0) {
            if (lts - cts  > roomExpiration.expirationTime) {
                AUILogger.logger().d(tag, "checkRoomExpired: room is expired: $lts - $cts > ${roomExpiration.expirationTime}")
                roomDidExpired?.invoke()
                return
            }
        }
    }

    /**
     * Returns the duration of room usage.
     *
     * @return The duration of room usage.
     */
    fun roomUsageDuration(): Long? {
        Log.d(tag, "lastUpdateTimestamp:$lastUpdateTimestamp this:$this")
        val currentTs = roomCurrentTs() ?: return null
        val createTs = createTimestamp ?: return null
        return currentTs - createTs
    }

    /**
     * Returns the current timestamp of the room.
     *
     * @return The current timestamp of the room.
     */
    fun roomCurrentTs(): Long? {
        val updateTs = lastUpdateTimestamp ?: return null
        val date = lastUpdateDate ?: return null
        val deltaDuration = (-date.time + Date().time)

        return updateTs + deltaDuration
    }
}