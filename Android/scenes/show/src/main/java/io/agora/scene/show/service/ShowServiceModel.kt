package io.agora.scene.show.service

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import java.util.UUID

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */

@IntDef(ShowInteractionStatus.idle, ShowInteractionStatus.linking, ShowInteractionStatus.pking)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class ShowInteractionStatus {
    companion object {
        const val idle = 0 // 空闲
        const val linking = 1 // 连麦中
        const val pking = 2 // pk中
    }
}

// 房间详情信息
data class ShowRoomDetailModel constructor(
    val roomId: String,
    val roomName: String,
    val roomUserCount: Int,
    val ownerId: String,
    val ownerAvatar: String,// http url
    val ownerName: String,
    val createdAt: Double = 0.0,
    val updatedAt: Double = 0.0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roomId)
        parcel.writeString(roomName)
        parcel.writeInt(roomUserCount)
        parcel.writeString(ownerId)
        parcel.writeString(ownerAvatar)
        parcel.writeString(ownerName)
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

fun String.isRobotRoom() = length > 6
fun ShowRoomDetailModel.isRobotRoom() = roomId.isRobotRoom()

//用户信息
data class ShowUser constructor(
    val userId: String,
    val avatar: String,
    val userName: String,
    val muteAudio: Boolean,
    @ShowInteractionStatus val status: Int = ShowInteractionStatus.idle,
    val isWaiting: Boolean = false
)

data class ShowPKUser constructor(
    val userId: String,
    val userName: String,
    val roomId: String,
    val avatar: String,
    @ShowInteractionStatus val status: Int = ShowInteractionStatus.idle,
    val isWaiting: Boolean = false
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
    val createAt: Double
)

@IntDef(ShowInvitationType.invitation, ShowInvitationType.accept, ShowInvitationType.reject, ShowInvitationType.end)
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ShowInvitationType {
    companion object {
        const val invitation = 0
        const val accept = 1
        const val reject = 2
        const val end = 3
    }
}

// 连麦邀请
data class ShowMicSeatInvitation constructor(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    @ShowInvitationType val type: Int = ShowInvitationType.invitation,
)

// PK邀请
data class ShowPKInvitation constructor(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    var userName: String,
    val roomId: String,
    val fromUserId: String,
    val fromUserName: String,
    val fromRoomId: String,
    val createAt: Double,
    @ShowInvitationType val type: Int = ShowInvitationType.invitation,
)

//连麦/Pk模型
data class ShowInteractionInfo constructor(
    val userId: String, // 互动者ID
    val userName: String, // 互动者用户名
    val roomId: String, // 互动房间ID
    @ShowInteractionStatus val interactStatus: Int, // 互动状态
    val createdAt: Double // 开始时间
)


enum class ShowSubscribeStatus {
    added,
    deleted,
    updated
}