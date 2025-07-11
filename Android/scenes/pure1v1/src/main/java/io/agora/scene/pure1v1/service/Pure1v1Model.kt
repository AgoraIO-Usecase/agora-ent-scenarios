package io.agora.scene.pure1v1.service

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/*
 * User information data structure
 */
class UserInfo(
    var userId: String = "",
    var userName: String = "",
    var avatar: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var objectId: String = "",
) : Parcelable {
    constructor(map: Map<String, Any>): this(
        map["userId"] as? String ?: "",
        map["userName"] as? String ?: "",
        map["avatar"] as? String ?: "",
        map["createdAt"] as? Long ?: System.currentTimeMillis(),
        map["objectId"] as? String ?: ""
    )

    fun toMap(): Map<String, Any> {
        return mapOf(
            Pair("userId", this.userId),
            Pair("userName", this.userName),
            Pair("avatar", this.avatar),
            Pair("createdAt", this.createdAt),
            Pair("objectId", this.objectId)
        )
    }

    fun getRoomId(): String {
        return "${userId}_${createdAt}"
    }

    // Only used in prepare phase, because a universal token is used, so a different channelId is required each time to ensure security
    fun getCallChannelId(): String {
        return UUID.randomUUID().toString()
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: ""
    ) {
    }

    override fun toString(): String {
        return "userId:$userId, userName:$userName, avatar:$avatar, createdAt:$createdAt"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(avatar)
        parcel.writeLong(createdAt)
        parcel.writeString(objectId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInfo> {
        override fun createFromParcel(parcel: Parcel): UserInfo {
            return UserInfo(parcel)
        }

        override fun newArray(size: Int): Array<UserInfo?> {
            return arrayOfNulls(size)
        }
    }
}