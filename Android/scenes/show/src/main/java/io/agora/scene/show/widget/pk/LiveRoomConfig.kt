package io.agora.scene.show.widget.pk

import io.agora.scene.show.service.ShowRoomDetailModel

// 房间详情信息
class LiveRoomConfig constructor(room: ShowRoomDetailModel, interactStatus: Int, waitingForPK: Boolean) {
    private val roomId: String
    private val roomName: String
    private val roomUserCount: Int
    private val ownerId: String
    private val ownerAvatar: String // http url
    private val ownerName: String
    private val createdAt: Double
    private val updatedAt: Double
    private val waitingForPK: Boolean
    private var interactStatus: Int

    init {
        roomId = room.roomId
        roomName = room.roomName
        roomUserCount = room.roomUserCount
        ownerId = room.ownerId
        ownerAvatar = room.ownerAvatar
        ownerName = room.ownerName
        createdAt = room.createdAt
        updatedAt = room.updatedAt
        this.interactStatus = interactStatus
        this.waitingForPK = waitingForPK
    }

    fun convertToShowRoomDetailModel() : ShowRoomDetailModel {
        return ShowRoomDetailModel(
            roomId,
            roomName,
            roomUserCount,
            ownerId,
            ownerAvatar,
            ownerName,
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

    fun setInteractStatus(interactStatus: Int) {
        this.interactStatus = interactStatus
    }

    fun isWaitingForPK() : Boolean {
        return waitingForPK
    }

    fun isRobotRoom() = roomId.length > 6

    fun getRoomId() = roomId
}
