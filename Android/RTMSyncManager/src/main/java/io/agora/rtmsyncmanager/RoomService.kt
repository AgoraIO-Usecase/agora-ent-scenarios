package io.agora.rtmsyncmanager

import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.rtmsyncmanager.service.callback.AUIException
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.utils.AUILogger

class RoomService(
    private val expirationPolicy: RoomExpirationPolicy,
    private val roomManager: AUIRoomManager,
    private val syncManager: SyncManager
) {
    private val tag = "RoomService"
    private val kPayloadOwnerId = "room_payload_owner_id"
    private val roomInfoMap: MutableMap<String, AUIRoomInfo> = mutableMapOf()

    fun getRoomList(
        appId: String,
        sceneId: String,
        lastCreateTime: Long,
        pageSize: Int,
        cleanClosure: ((AUIRoomInfo) -> Boolean)? = null,
        completion: (AUIException?, Long?, List<AUIRoomInfo>?) -> Unit
    ) {
        // The room list will return the latest server time ts.
        roomManager.getRoomInfoList(appId, sceneId, lastCreateTime, pageSize) { err, roomList, ts ->
            if (err != null || ts == null) {
                completion(err, ts, null)
                return@getRoomInfoList
            }

            val list: MutableList<AUIRoomInfo> = mutableListOf()
            roomList?.forEach { roomInfo ->
                // Traverse each room information to check if it has expired.
                var needCleanRoom: Boolean = false
                if (expirationPolicy.expirationTime > 0 && ts - roomInfo.createTime >= expirationPolicy.expirationTime + 60 * 1000) {
                    needCleanRoom = true
                } else if (cleanClosure?.invoke(roomInfo) == true) {
                    needCleanRoom = true
                }

                if (needCleanRoom) {
                    val scene = syncManager.createScene(roomInfo.roomId, expirationPolicy)
                    scene.delete()
                    roomManager.destroyRoom(appId, sceneId, roomInfo.roomId) {}
                    roomInfoMap.remove(roomInfo.roomId)
                    return@forEach
                }

                list.add(roomInfo)
            }
            completion(null, ts, list)
        }
    }

    // TODO: Replace AUIRoomInfo with the IAUIRoomInfo interface. The server will create a room id, should roomManager throw it out here?.
    fun createRoom(appId: String, sceneId: String, room: AUIRoomInfo, completion: (AUIRtmException?, AUIRoomInfo?) -> Unit) {
        val scene = syncManager.createScene(channelName = room.roomId, roomExpiration = expirationPolicy)
        roomManager.createRoom(appId, sceneId, room) { err, roomInfo ->
            if (err != null) {
                completion(AUIRtmException(err.code, err.message.toString(), ""), null)
                return@createRoom
            }

            if (roomInfo == null) {
                return@createRoom
            }

            roomInfoMap[roomInfo.roomId] = roomInfo

            scene.create(createTime = roomInfo.createTime, mapOf(kPayloadOwnerId to (room.roomOwner?.userId ?: ""))) { error ->
                if (error != null) {
                    // Need to clean up dirty room information on failure.
                    createRoomRevert(appId, sceneId, room.roomId)
                    completion(error, null)
                    return@create
                }

                scene.enter { _, err ->
                    if (err != null) {
                        // Need to clean up dirty room information on failure.
                        createRoomRevert(appId, sceneId, room.roomId)
                        completion(err, null)
                        return@enter
                    }

                    completion(null, roomInfo)
                }
            }
        }
    }

    fun enterRoom(appId: String, sceneId: String, roomId: String, completion: (AUIRtmException?) -> Unit) {
        val scene = syncManager.createScene(channelName = roomId, roomExpiration = expirationPolicy)
        scene.enter { payload, err ->
            val ownerId = payload?.get(kPayloadOwnerId) as? String ?: ""
            val room = AUIRoomInfo()
            room.roomId = roomId
            val owner = AUIUserThumbnailInfo()
            owner.userId = ownerId
            room.roomOwner = owner
            roomInfoMap[room.roomId] = room
            if (err != null) {
                enterRoomRevert(appId, sceneId, roomId)
                completion(err)
                return@enter
            }
            completion(null)
        }
    }

    fun leaveRoom(appId: String, sceneId: String, roomId: String) {
        val scene = syncManager.getScene(roomId)
        val isOwner = roomInfoMap[roomId]?.roomOwner?.userId == AUIRoomContext.shared().currentUserInfo.userId

        if (isOwner) {
            roomManager.destroyRoom(appId, sceneId, roomId) {}
            scene?.delete()
        } else {
            scene?.leave()
        }

        roomInfoMap.remove(roomId)
    }

    fun isRoomOwner(roomId: String): Boolean {
        return AUIRoomContext.shared().isRoomOwner(roomId)
    }

    private fun createRoomRevert(appId: String, sceneId: String, roomId: String) {
        AUILogger.logger().d(tag, "createRoomRevert: $roomId")
        leaveRoom(appId, sceneId, roomId)
    }

    private fun enterRoomRevert(appId: String, sceneId: String, roomId: String) {
        AUILogger.logger().d(tag, "enterRoomRevert: $roomId")
        leaveRoom(appId, sceneId, roomId)
    }
}