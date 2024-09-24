package io.agora.scene.aichat.list.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.agora.scene.aichat.imkit.model.EaseProfile

@Entity
data class ChatUserEntity constructor(
    @PrimaryKey val userId: String,
    val name: String?,
    val avatar: String?,
    val sign: String? = null,
    val voiceId: String? = null,
    val ext: String? = null,
    @ColumnInfo(name = "update_times")
    var updateTimes: Int = 0
)

/**
 * Convert the user data to the profile data.
 */
internal fun ChatUserEntity.parse() = EaseProfile(userId, name, avatar, sign, voiceId, ext)

internal fun EaseProfile.parseToDbBean() = ChatUserEntity(id, name, avatar, sign, voiceId, ext)