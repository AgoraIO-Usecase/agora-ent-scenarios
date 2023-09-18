package io.agora.scene.showTo1v1.service

import android.os.Parcel
import android.os.Parcelable

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