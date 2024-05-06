package io.agora.rtmsyncmanager.service.http.room

import io.agora.rtmsyncmanager.service.http.PayloadResp
import io.agora.rtmsyncmanager.model.AUIRoomInfo

data class CreateRoomReq constructor(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo
)
data class CreateRoomResp constructor(
    val roomId: String,
    val payload: AUIRoomInfo,
    val createTime: Long,
    val updateTime: Long
)
data class RoomUserReq constructor(
    val appId: String,
    val sceneId: String,
    val roomId: String
)
data class DestroyRoomResp constructor(
    val roomId: String
)
data class RoomListReq constructor(
    val appId: String,
    val sceneId: String,
    val pageSize: Int,
    val lastCreateTime: Long?
)
data class QueryRoomResp constructor(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo,
    val createTime: Long,
    val updateTime: Long
)
data class UpdateRoomReq constructor(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo
)
data class QueryRoomReq constructor(
    val appId: String,
    val sceneId: String,
    val roomId: String
)
data class RoomListResp constructor(
    val pageSize: Int,
    val count: Int,
    val list: List<PayloadResp<AUIRoomInfo>>
){
    fun getRoomList(): List<AUIRoomInfo>{
        val list = mutableListOf<AUIRoomInfo>()
        this.list.forEach {
            list.add(AUIRoomInfo().apply {
                roomId = it.roomId
                roomName = it.payload?.roomName ?: ""
                roomOwner = it.payload?.roomOwner
                customPayload = it.payload?.customPayload
            })
        }
        return list
    }
}