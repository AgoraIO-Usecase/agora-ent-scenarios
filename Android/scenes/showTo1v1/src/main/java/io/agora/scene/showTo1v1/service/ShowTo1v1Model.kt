package io.agora.scene.showTo1v1.service

import android.os.Parcel
import android.os.Parcelable

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */

/*
 * 用户数据结构
 */
open class ShowTo1v1UserInfo constructor(
    val userId: String,
    val userName: String,
    var avatar: String,
    var objectId: String = "",
    var createdAt: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong()
    ) {
    }

    constructor(map: Map<String, Any>) : this(
        map["userId"] as? String ?: "",
        map["userName"] as? String ?: "",
        map["avatar"] as? String ?: "",
        map["objectId"] as? String ?: "",
        map["createdAt"] as? Long ?: 0L
    )

    fun toMap(): Map<String, Any> {
        return mapOf(
            Pair("userId", this.userId),
            Pair("userName", this.userName),
            Pair("avatar", this.avatar),
            Pair("objectId", this.objectId),
            Pair("createdAt", this.createdAt)
        )
    }

    fun getIntUserId(): Int {
        return userId.toIntOrNull() ?: 0
    }

    fun get1v1ChannelId(): String {
        return "1v1_${userId}_${createdAt}"
    }

    fun bgImage(): String {
        val uid = getIntUserId()
        return "show_to1v1_user_bg${uid % 9 + 1}"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(avatar)
        parcel.writeString(objectId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowTo1v1UserInfo> {
        override fun createFromParcel(parcel: Parcel): ShowTo1v1UserInfo {
            return ShowTo1v1UserInfo(parcel)
        }

        override fun newArray(size: Int): Array<ShowTo1v1UserInfo?> {
            return arrayOfNulls(size)
        }
    }
}

/*
 * 房间信息数据结构
 */
class ShowTo1v1RoomInfo constructor(
    val roomId: String,
    val roomName: String,
    userId: String, userName: String, avatar: String, objectId: String = "", createdAt: Long
) : ShowTo1v1UserInfo(userId, userName, avatar, objectId, createdAt), Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roomId)
        parcel.writeString(roomName)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(avatar)
        parcel.writeString(objectId)
        parcel.writeLong(createdAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowTo1v1RoomInfo> {
        override fun createFromParcel(parcel: Parcel): ShowTo1v1RoomInfo {
            return ShowTo1v1RoomInfo(parcel)
        }

        override fun newArray(size: Int): Array<ShowTo1v1RoomInfo?> {
            return arrayOfNulls(size)
        }
    }
}