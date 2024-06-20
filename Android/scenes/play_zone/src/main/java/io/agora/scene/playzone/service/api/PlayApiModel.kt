package io.agora.scene.playzone.service.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PlayZoneCommonResp<Data> constructor(
    val code: Int = 0,
    val data: Data?,
    val msg: String?,
    val tips: String?
)

data class PlayZoneGameListModel constructor(
    val carousel: List<PlayZoneGameBanner>?,
)

data class PlayZoneGameBanner constructor(
    @Expose
    @SerializedName("index")
    var index: Int? = null,
    @Expose
    @SerializedName("url")
    var url: String? = null,
)

data class PlayZoneGameModel constructor(
    val vendorId:String, // 厂商id
    val gameType: String, // 游戏类型
    val gameId: Int,
    val name: String?,
    val icon:String?
)

/**
 * 游戏类型
 *
 * @property value
 * @constructor Create empty Play game type
 */
enum class PlayGameType(val value: Int) {
    leisure_and_entertainment(0), // 休闲娱乐
    voice_interaction(1), // 语音互动
    realtime_competition(2), // 实时竞技
    classic_board_games(3), // 经典棋牌
    party_games(4), // Party Game
}

data class PlayGameListModel constructor(
    val gameType: PlayGameType, // 游戏类型
    val gameList: List<PlayGameInfoModel>, // 游戏列表
)

data class PlayGameInfoModel constructor(
    var gameName: String = "", // 游戏名称
    var gameId: Long = 0, // 游戏id
    var gamePic: Int = 0, // 游戏图标
    var gameUrl: String? = null, // 游戏地址
) : Serializable


data class PlayZoneMessage constructor(
    var userId: String = "",     //用户id (rtc uid)
    var userName: String?,        //用户名
    var message: String?,         //消息文本内容
    var createAt: Long = 0,      //创建时间，与19700101时间比较的毫秒数
)