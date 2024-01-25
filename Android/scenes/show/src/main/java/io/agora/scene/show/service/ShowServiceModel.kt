package io.agora.scene.show.service

import android.os.Parcel
import android.os.Parcelable
import io.agora.scene.show.R

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */

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
data class ShowRoomDetailModel constructor(
    val roomId: String,
    val roomName: String,
    val roomUserCount: Int,
    val thumbnailId: String, // 0, 1, 2, 3
    val ownerId: String,
    val ownerAvatar: String,// http url
    val ownerName: String,
    val roomStatus: Int = ShowRoomStatus.activity.value,
    val interactStatus: Int = ShowInteractionStatus.idle.value,
    val createdAt: Double,
    val updatedAt: Double
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!, // 房间号
        parcel.readString()?:"",
        parcel.readInt(),
        parcel.readString()?:"",
        parcel.readString()!!, // 房主id
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    fun toMap(): HashMap<String, Any>{
        return hashMapOf(
            Pair("roomId", roomId),
            Pair("roomName", roomName),
            Pair("roomUserCount", roomUserCount),
            Pair("thumbnailId", thumbnailId),
            Pair("ownerId", ownerId),
            Pair("ownerAvatar", ownerAvatar),
            Pair("ownerName", ownerName),
            Pair("roomStatus", roomStatus),
            Pair("interactStatus", interactStatus),
            Pair("createdAt", createdAt),
            Pair("updatedAt", updatedAt),
        )
    }

    fun isRobotRoom() = roomId.length > 6

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roomId)
        parcel.writeString(roomName)
        parcel.writeInt(roomUserCount)
        parcel.writeString(thumbnailId)
        parcel.writeString(ownerId)
        parcel.writeString(ownerAvatar)
        parcel.writeString(ownerName)
        parcel.writeInt(roomStatus)
        parcel.writeInt(interactStatus)
        parcel.writeDouble(createdAt)
        parcel.writeDouble(updatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowRoomDetailModel> {
        override fun createFromParcel(parcel: Parcel): ShowRoomDetailModel {
            return ShowRoomDetailModel(parcel)
        }

        override fun newArray(size: Int): Array<ShowRoomDetailModel?> {
            return arrayOfNulls(size)
        }
    }


}

//用户信息
data class ShowUser constructor(
    val userId: String,
    val avatar: String,
    val userName: String,
    val status: Int = ShowRoomRequestStatus.idle.value
)

// 聊天消息
data class ShowMessage constructor(
    val userId: String,
    val userName: String,
    val message: String,
    val createAt: Double
)

// 连麦申请
data class ShowMicSeatApply constructor(
    val userId: String,
    val avatar: String,
    val userName: String,
    val status: Int,
    val createAt: Double
)

// 连麦邀请
data class ShowMicSeatInvitation constructor(
    val userId: String,
    val avatar: String,
    val userName: String,
    val status: Int,
)

// PK邀请
data class ShowPKInvitation constructor(
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
data class ShowInteractionInfo constructor(
    val userId: String,
    val userName: String,
    val roomId: String,
    val interactStatus: Int,
    val muteAudio: Boolean = false,
    val ownerMuteAudio: Boolean = false,
    val createdAt: Double
)

