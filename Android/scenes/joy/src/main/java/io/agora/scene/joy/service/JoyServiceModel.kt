package io.agora.scene.joy.service

data class RoomListModel constructor(
    val roomNo: String = "",
    val name: String = "",
    val icon: String = "",
    val isPrivate: Boolean = false,
    val password: String = "",
    val creatorNo: String = "",
    val creatorName: String = "",
    val creatorAvatar: String = "",
    val createdAt: String = System.currentTimeMillis().toString(),

    /**
     * 背景图
     */
    val bgOption: String = "",
    /**
     * 房间内人数
     */
    val roomPeopleNum: Int = 0,
)

data class CreateRoomInputModel constructor(
    val icon: String,
    val isPrivate: Int,
    val name: String,
    val password: String,
    val userNo: String,
)

data class CreateRoomOutputModel constructor(
    val roomNo: String?,
    val password: String?,
)


data class JoinRoomInputModel constructor(
    val roomNo: String,
    val password: String?,
)

data class JoinRoomOutputModel constructor(
    val roomName: String,
    val roomNo: String,
    val creatorNo: String,
    val creatorAvatar: String,
    val bgOption: String,
    /**
     * 房间内人数
     */
    val roomPeopleNum: Int,
    val agoraRTMToken: String,
    val agoraRTCToken: String,
    val agoraChorusToken: String,
    val createdAt: String
) : java.io.Serializable