package io.agora.scene.show.service

import androidx.annotation.DrawableRes
import io.agora.scene.show.R
import kotlin.random.Random

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
    val roomId: String,
    val roomName: String,
    val roomUserCount: Int,
    val thumbnailId: String, // 0, 1, 2, 3
    val ownerId: String,
    val ownerAvater: String,// http url
    val roomStatus: Int = ShowRoomStatus.activity.value,
    val createAt: Double,
    val updateAt: Double,
): java.io.Serializable {
    fun toMap(): HashMap<String, Any>{
        return hashMapOf(
            Pair("roomId", roomId),
            Pair("roomName", roomName),
            Pair("roomUserCount", roomUserCount),
            Pair("thumbnailId", ""),
            Pair("ownerId", ownerId),
            Pair("ownerAvater", ownerAvater),
            Pair("roomStatus", roomStatus),
            Pair("crateAt", createAt),
            Pair("updateAt", updateAt),
        )
    }

    fun getThumbnailIcon() = when (thumbnailId) {
        "0" -> R.mipmap.show_room_cover_0
        "1" -> R.mipmap.show_room_cover_1
        "2" -> R.mipmap.show_room_cover_2
        "3" -> R.mipmap.show_room_cover_3
        else -> R.mipmap.show_room_cover_0
    }

    companion object{

        fun getRandomRoomId() = (Random(System.currentTimeMillis()).nextInt(10000) + 100000).toString()

        fun getRandomThumbnailId() = Random(System.currentTimeMillis()).nextInt(0, 3).toString()

        @DrawableRes
        fun getThumbnailIcon(thumbnailId: String) = when (thumbnailId) {
            "0" -> R.mipmap.show_room_cover_0
            "1" -> R.mipmap.show_room_cover_1
            "2" -> R.mipmap.show_room_cover_2
            "3" -> R.mipmap.show_room_cover_3
            else -> R.mipmap.show_room_cover_0
        }

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

