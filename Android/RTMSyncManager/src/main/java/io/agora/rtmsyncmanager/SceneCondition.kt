package io.agora.rtmsyncmanager

import android.util.Log
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter
import io.agora.rtmsyncmanager.utils.AUILogger
import java.util.*

/*
 * 加入房间成功条件判断
 */
class AUISceneEnterCondition(private val channelName: String, private val arbiter: AUIArbiter) {
    private val tag = "AUISceneEnterCondition"
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

    /*
     检查加入房间成功，需要以下条件全部满足：
     1. subscribe成功
     2. 获取到初始化信息(拿到ownerId房主uid)
     3. 锁也获取成功(acquire的callback成功收到，收到callback写metadata才能成功，如果没有收到callback但是获取到锁主，哪怕锁住是自己，写metadata还是会失败)
     4. 获取到锁主(修改metadata需要向锁主发消息)
     */
    private fun checkRoomValid() {
        AUILogger.logger().d(tag, "checkRoomValid:$channelName subscribeSuccess:$subscribeSuccess lockOwnerRetrieved:$lockOwnerRetrieved ownerId:$ownerId")
        if (subscribeSuccess && lockOwnerRetrieved && ownerId.isNotEmpty()) {
            if (arbiter.isArbiter() && !lockOwnerAcquireSuccess) return
            enterCompletion?.invoke()
        }
    }
}

/*
 * 房间过期条件判断
 */
class AUISceneExpiredCondition(private val channelName: String, private val roomExpiration: RoomExpirationPolicy) {
    private val tag = "AUISceneExpiredCondition"
    private var lastUpdateDate: Date? = null
    var roomDidExpired: (() -> Unit)? = null

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

    /*
     检查房间过期，其中一个不满足表示过期,需要房间加入完成后检查（目前认为没有enter完成不做检查过期处理）：
     1.房主加入需要检查房主在用户列表里(通过who now查询)
     2.观众加入检查房主不在用户列表里
     3.房间时间过期（动态配置，不可直接写死20min）
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

        //TODO: 目前检查只有enter room时，如果房主一直在房间内不会过期，是否需要内部检查，还是让上层处理
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

    /*
      createTimestamp --[createDuration]--> lastUpdateTimestamp (lastUpdateTimestamp - createTimestamp = createDuration)
      (lastUpdateDate) --[deltaDuration]--> nowDate (nowDate - lastUpdateDate = deltaDuration)
     */
    fun roomUsageDuration(): Long? {
        Log.d(tag, "lastUpdateTimestamp:$lastUpdateTimestamp this:$this")
        val currentTs = roomCurrentTs() ?: return null
        val createTs = createTimestamp ?: return null
        return currentTs - createTs
    }

    fun roomCurrentTs(): Long? {
        val updateTs = lastUpdateTimestamp ?: return null
        val date = lastUpdateDate ?: return null
        val deltaDuration = (-date.time + Date().time)

        return updateTs + deltaDuration
    }
}