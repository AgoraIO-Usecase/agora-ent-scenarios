package io.agora.scene.show.service

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import io.agora.scene.base.utils.TimeUtils
import java.util.UUID

/*
 * Service module
 * Introduction: This module is responsible for interaction between frontend business modules and business server 
 * (including room list + room business data synchronization, etc.)
 * Implementation principle: The business server of this scene is wrapped with a rethinkDB backend service for data storage.
 * It can be considered as a DB that can be freely written by the app side. Room list data and room business data 
 * are constructed on the app and stored in this DB.
 * When data in DB is added/deleted/modified, all clients will be notified to achieve business data synchronization
 * TODO Note⚠️: The backend service of this scene is for demo only and cannot be used commercially. 
 * If you need to go live, you must deploy your own backend service or cloud storage server 
 * (such as leancloud, easemob, etc.) and reimplement this module!!!!!!!!!
 */

@IntDef(ShowInteractionStatus.idle, ShowInteractionStatus.linking, ShowInteractionStatus.pking)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class ShowInteractionStatus {
    companion object {
        const val idle = 0 // Idle
        const val linking = 1 // In linking
        const val pking = 2 // In PK
    }
}

// Room details information
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

// User information
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

// Chat message
data class ShowMessage constructor(
    val userId: String,
    val userName: String,
    val message: String,
)

// Linking application
data class ShowMicSeatApply constructor(
    val userId: String,
    val avatar: String,
    val userName: String
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

// Linking invitation
data class ShowMicSeatInvitation constructor(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userName: String,
    @ShowInvitationType val type: Int = ShowInvitationType.invitation,
)

// PK invitation
data class ShowPKInvitation constructor(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    var userName: String,
    val roomId: String,
    val fromUserId: String,
    val fromUserName: String,
    val fromRoomId: String,
    @ShowInvitationType val type: Int = ShowInvitationType.invitation,
)

// Linking/PK model
data class ShowInteractionInfo constructor(
    val userId: String, // Interactor ID
    val userName: String, // Interactor username
    val roomId: String, // Interaction room ID
    @ShowInteractionStatus val interactStatus: Int, // Interaction status
    val createdAt: Double = TimeUtils.currentTimeMillis().toDouble()// Start time
)


enum class ShowSubscribeStatus {
    added,
    deleted,
    updated
}