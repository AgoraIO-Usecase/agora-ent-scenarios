package io.agora.scene.playzone.service

import java.io.Serializable

object PlayZoneParameters {
    const val ROOM_USER_COUNT = "roomPeopleNum"
    const val THUMBNAIL_ID = "icon"
    const val PASSWORD = "password"
    const val IS_PRIVATE = "isPrivate"
    const val BADGE_TITLE = "badgeTitle"
    const val GAME_ID = "gameId"
}

data class PlayCreateRoomModel constructor(
    val roomName: String,
    val password: String? = null,
    val gameId:Long = 0L,
    val gameName: String = "",
)

data class PlayStartGameInfo constructor(
    var gameId: String = "", //游戏 id
    var gameName: String = "",
) : Serializable