package io.agora.scene.playzone.service.api

import java.io.Serializable

/**
 * Game type
 *
 * @property value
 * @constructor Create empty Play game type
 */
enum class PlayGameType(val value: Int) {
    leisure_and_entertainment(0), // Leisure and entertainment
    voice_interaction(1), // Voice interaction
    realtime_competition(2), // Real-time competition
    classic_board_games(3), // Classic board games
    party_games(4), // Party Game
}

data class PlayGameListModel constructor(
    val gameType: PlayGameType, // Game type
    val gameList: List<PlayGameInfoModel>, // Game list
)

data class PlayGameInfoModel constructor(
    var gameName: String = "", // Game name
    var gameId: Long = 0, // Game ID
    var gamePic: Int = 0, // Game icon
    var gameUrl: String? = null, // Game URL
    var supportRobots: Boolean = true, // Whether robots are supported
) : Serializable


data class PlayZoneMessage constructor(
    var userId: String = "",     // User ID (rtc uid)
    var userName: String?,       // Username
    var message: String?,        // Message text content
    var createAt: Long = 0,      // Creation time, milliseconds compared to 19700101
)