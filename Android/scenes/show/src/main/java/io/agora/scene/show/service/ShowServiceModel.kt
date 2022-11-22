package io.agora.scene.show.service

enum class ShowRoomStatus(val value: Int) {
    activity(0),//直播中
    end(1)//直播结束
}

enum class ShowRoomRequestStatus(val value: Int){
    watitting(1),// 等待中
    accept(2),//  已接受
    refuse(3),// 已拒绝
    end(4)// 已结束
}

// 房间详情信息
data class ShowRoomDetailModel(
    val roomNo: String,
    val roomName: String,
    val roomUserCount: Int,
    val thumbnailId: String,
    val ownerId: String,
    val roomStatus: Int = ShowRoomStatus.activity.value,
    val crateAt: Double,
    val updateAt: Double,
): java.io.Serializable {
    fun toMap(): Map<String, Any>{
        return mapOf(
            Pair("roomNo", roomNo),
            Pair("roomName", roomName),
            Pair("roomUserCount", 0),
            Pair("thumbnailId", ""),
            Pair("ownerId", ownerId),
            Pair("roomStatus", roomStatus),
            Pair("crateAt", crateAt),
            Pair("updateAt", updateAt),
        )
    }
}

//用户信息
data class ShowUser(
    val userId: String,
    val avatar: String,
    val userName: String
)

// 聊天消息
data class ShowMessage(
    val userId: String,
    val userName: String,
    val message: String,
    val createAt: Double
)

// 连麦申请
data class ShowMicSeatApply(
    val userId: String,
    val userAvatar: String,
    val userName: String,
    val status: ShowRoomRequestStatus,
    val createAt: Double
)

// 连麦邀请
data class ShowMicSeatInvitation(
    val userId: String,
    val userAvatar: String,
    val userName: String,
    val fromUserId: String,
    val status: ShowRoomRequestStatus,
    val createAt: Double
)

// PK邀请
data class ShowPKInvitation(
    val userId: String,
    val roomId: String,
    val fromUserId: String,
    val fromName: String,
    val fromRoomId: String,
    val status: ShowRoomRequestStatus,
    val createAt: Double
)

