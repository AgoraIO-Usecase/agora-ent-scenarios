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
)

data class PlayRobotInfo constructor(
    var gender: String? = null, // 性别 male：男，female：女
    var level: Int = 0, // 机器人等级 1:简单 2:适中 3:困难
    var owner: AUIUserThumbnailInfo? = null,
) : Serializable
