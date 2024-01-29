package io.agora.scene.pure1v1.service

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
 * 用户信息数据结构
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

    // 只在prepare阶段使用，因为是用了万能token，每次需要不同的channelId以保证安全性
    fun getCallChannelId(): String {
        return "${userId}_${System.currentTimeMillis()}"
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: ""
    ) {
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