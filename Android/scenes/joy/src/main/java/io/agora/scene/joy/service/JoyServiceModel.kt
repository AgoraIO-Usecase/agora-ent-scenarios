package io.agora.scene.joy.service

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

object JoyParameters {
    const val ROOM_USER_COUNT = "roomUserCount"
    const val THUMBNAIL_ID = "thumbnailId"
    const val BADGE_TITLE = "badgeTitle"
}

data class JoyStartGameInfo constructor(
    var gameId: String = "", // Game id
    var taskId: String = "", // Game taskid
    var assistantUid: Int = 0, // Game screen uid
    var gameName: String = "",
) : Serializable

data class JoyMessage constructor(
    var userId: String = "", // User id (rtc uid)
    var userName: String?, // User name
    var message: String?, // Message text content
    var createAt: Long = 0, // Creation time, milliseconds since 19700101
)