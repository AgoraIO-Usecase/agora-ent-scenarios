package io.agora.scene.show.widget.pk

import io.agora.scene.show.service.ShowRoomDetailModel

// 房间详情信息
class LiveRoomConfig constructor(room: ShowRoomDetailModel, waitingForPK: Boolean) {
    private val roomId: String
    private val roomName: String
    private val roomUserCount: Int
    private val thumbnailId: String // 0, 1, 2, 3
    private val ownerId: String
    private val ownerAvatar: String// http url
    private val ownerName: String
    private val roomStatus: Int
    private val interactStatus: Int
    private val createdAt: Double
    private val updatedAt: Double
    private val waitingForPK: Boolean

    init {
        roomId = room.roomId
        roomName = room.roomName
        roomUserCount = room.roomUserCount
        thumbnailId = room.thumbnailId
        ownerId = room.ownerId
        ownerAvatar = room.ownerAvatar
        ownerName = room.ownerName
        roomStatus = room.roomStatus
        interactStatus = room.interactStatus
        createdAt = room.createdAt
        updatedAt = room.updatedAt
        this.waitingForPK = waitingForPK
    }

    fun convertToShowRoomDetailModel() : ShowRoomDetailModel {
        return ShowRoomDetailModel(
            roomId,
            roomName,
            roomUserCount,
            thumbnailId,
            ownerId,
            ownerAvatar,
            ownerName,
            roomStatus,
            interactStatus,
            createdAt,
            updatedAt
        )
    }

    fun getOwnerAvatar() : String {
        return ownerAvatar
    }

    fun getOwnerName() : String {
        return ownerName
    }

    fun getInteractStatus() : Int {
        return interactStatus
    }

    fun isWaitingForPK() : Boolean {
        return waitingForPK
    }

    fun isRobotRoom() = roomId.length > 6
}
