package io.agora.scene.showTo1v1.service

import android.os.Parcel
import android.os.Parcelable
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcherAPI

open class ShowTo1v1UserInfo constructor(
    val userId: String,
    val userName: String,
    var avatar: String,
    var objectId: String = ""
) {

    fun getIntUserId(): Int {
        return userId.toIntOrNull() ?: 0
    }

    fun get1v1ChannelId(): String {
        return "1v1_$userId"
    }

    fun bgImage(): String {
        val uid = getIntUserId()
        return "user_bg${uid % 9 + 1}"
    }
}

class ShowTo1v1RoomInfo constructor(
    val roomId: String,
    val roomName: String,
    var createdAt: Long, userId: String, userName: String, avatar: String, objectId: String=""
) : ShowTo1v1UserInfo(userId, userName, avatar, objectId), Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roomId)
        parcel.writeString(roomName)
        parcel.writeLong(createdAt)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(avatar)
        parcel.writeString(objectId)
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

    fun createRoomInfo(token: String): VideoSwitcherAPI.RoomInfo {
        return VideoSwitcherAPI.RoomInfo(
            channelName = roomId,
            uid = getIntUserId(),
            token = token,
            eventHandler = null
        )
    }
}