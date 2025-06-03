package io.agora.scene.playzone.service

import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import java.io.Serializable

object PlayZoneParameters {
    const val ROOM_USER_COUNT = "roomPeopleNum"
    const val THUMBNAIL_ID = "icon"
    const val PASSWORD = "password"
    const val IS_PRIVATE = "isPrivate"
    const val BADGE_TITLE = "badgeTitle"
    const val GAME_ID = "gameId"
    const val CHAT_ID = "chatId"
}

data class PlayCreateRoomModel constructor(
    val roomName: String,
    val password: String? = null,
    val gameId:Long = 0L,
    val gameName: String = "",
    val chatRoomId:String = ""
)

data class PlayRobotInfo constructor(
    var gender: String? = null, // Gender male: male, female: female
    var level: Int = 0, // Robot level 1: simple 2: moderate 3: difficult
    var owner: AUIUserThumbnailInfo? = null,
) : Serializable
