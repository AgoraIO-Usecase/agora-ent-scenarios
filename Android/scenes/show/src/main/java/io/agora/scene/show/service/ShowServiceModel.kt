package io.agora.scene.show.service

import androidx.annotation.DrawableRes
import io.agora.scene.show.R
import kotlin.random.Random

enum class ShowRoomStatus(val value: Int) {
    activity(0),//直播中
    end(1)//直播结束
}

enum class ShowRoomRequestStatus(val value: Int){
    idle(0),
    waitting(1),// 等待中
    accepted(2),//  已接受
    rejected(3),// 已拒绝
    ended(4)// 已结束
}

enum class ShowInteractionStatus(val value: Int) {
    idle(0), /// 空闲
    onSeat(1), /// 连麦中
    pking(2) /// pk中
}

// 房间详情信息
data class ShowRoomDetailModel(
    val roomId: String,
    val roomName: String,
    val roomUserCount: Int,
    val thumbnailId: String, // 0, 1, 2, 3
    val ownerId: String,
    val ownerAvater: String,// http url
    val ownerName: String,
    val roomStatus: Int = ShowRoomStatus.activity.value,
    val interactStatus: Int = ShowInteractionStatus.idle.value,
    val createdAt: Double,
    val updatedAt: Double,
): java.io.Serializable {
    fun toMap(): HashMap<String, Any>{
        return hashMapOf(
            Pair("roomId", roomId),
            Pair("roomName", roomName),
            Pair("roomUserCount", roomUserCount),
            Pair("thumbnailId", thumbnailId),
            Pair("ownerId", ownerId),
            Pair("ownerAvater", ownerAvater),
            Pair("ownerName", ownerName),
            Pair("roomStatus", roomStatus),
            Pair("interactStatus", interactStatus),
            Pair("createdAt", createdAt),
            Pair("updatedAt", updatedAt),
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
    val userName: String,
    val status: Int = ShowRoomRequestStatus.idle.value
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
    val avatar: String,
    val userName: String,
    val status: Int,
    val createAt: Double
)

// 连麦邀请
data class ShowMicSeatInvitation(
    val userId: String,
    val avatar: String,
    val userName: String,
    val status: Int,
)

// PK邀请
data class ShowPKInvitation(
    val userId: String,
    var userName: String,
    val roomId: String,
    val fromUserId: String,
    val fromName: String,
    val fromRoomId: String,
    val status: Int,
    var userMuteAudio: Boolean = false,
    var fromUserMuteAudio: Boolean = false,
    val createAt: Double
)

//连麦/Pk模型
data class ShowInteractionInfo(
    val userId: String,
    val userName: String,
    val roomId: String,
    val interactStatus: Int,
    val muteAudio: Boolean = false,
    val ownerMuteAudio: Boolean = false,
    val createdAt: Double
)

