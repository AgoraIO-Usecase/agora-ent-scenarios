package io.agora.scene.pure1v1.service

import android.os.Parcel
import android.os.Parcelable

class UserInfo(
    public var userId: String = "",
    public var userName: String = "",
    public var avatar: String = "",
    public var createdAt: Long = System.currentTimeMillis(),
    var objectId: String = "",
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: ""
    ) {
    }

    fun getRoomId(): String {
        return userId
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