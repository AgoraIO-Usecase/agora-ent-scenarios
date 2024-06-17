package io.agora.rtmsyncmanager.service.http.room

import io.agora.rtmsyncmanager.service.http.PayloadResp
import io.agora.rtmsyncmanager.model.AUIRoomInfo

/**
 * Data class for creating a room request.
 * @property appId The application ID.
 * @property sceneId The scene ID.
 * @property roomId The room ID.
 * @property payload The payload containing room information.
 */
data class CreateRoomReq(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo
)

/**
 * Data class for creating a room response.
 * @property roomId The room ID.
 * @property payload The payload containing room information.
 * @property createTime The time the room was created.
 * @property updateTime The time the room was last updated.
 */
data class CreateRoomResp(
    val roomId: String,
    val payload: AUIRoomInfo,
    val createTime: Long,
    val updateTime: Long
)

/**
 * Data class for room user request.
 * @property appId The application ID.
 * @property sceneId The scene ID.
 * @property roomId The room ID.
 */
data class RoomUserReq(
    val appId: String,
    val sceneId: String,
    val roomId: String
)

/**
 * Data class for destroying a room response.
 * @property roomId The room ID.
 */
data class DestroyRoomResp(
    val roomId: String
)

/**
 * Data class for room list request.
 * @property appId The application ID.
 * @property sceneId The scene ID.
 * @property pageSize The number of rooms to return per page.
 * @property lastCreateTime The creation time of the last room in the previous page.
 */
data class RoomListReq(
    val appId: String,
    val sceneId: String,
    val pageSize: Int,
    val lastCreateTime: Long?
)

/**
 * Data class for querying a room response.
 * @property appId The application ID.
 * @property sceneId The scene ID.
 * @property roomId The room ID.
 * @property payload The payload containing room information.
 * @property createTime The time the room was created.
 * @property updateTime The time the room was last updated.
 */
data class QueryRoomResp(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo,
    val createTime: Long,
    val updateTime: Long
)

/**
 * Data class for updating a room request.
 * @property appId The application ID.
 * @property sceneId The scene ID.
 * @property roomId The room ID.
 * @property payload The payload containing room information.
 */
data class UpdateRoomReq(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo
)

/**
 * Data class for querying a room request.
 * @property appId The application ID.
 * @property sceneId The scene ID.
 * @property roomId The room ID.
 */
data class QueryRoomReq(
    val appId: String,
    val sceneId: String,
    val roomId: String
)

/**
 * Data class for room list response.
 * @property pageSize The number of rooms returned per page.
 * @property count The total number of rooms.
 * @property list The list of rooms.
 */
data class RoomListResp(
    val pageSize: Int,
    val count: Int,
    val list: List<PayloadResp<AUIRoomInfo>>
){
    /**
     * Method to get a list of room information.
     * @return A list of room information.
     */
    fun getRoomList(): List<AUIRoomInfo>{
        val list = mutableListOf<AUIRoomInfo>()
        this.list.forEach {
            list.add(AUIRoomInfo().apply {
                roomId = it.roomId
                roomName = it.payload?.roomName ?: ""
                roomOwner = it.payload?.roomOwner
                customPayload = it.payload?.customPayload
                createTime = it.createTime
            })
        }
        return list
    }
}