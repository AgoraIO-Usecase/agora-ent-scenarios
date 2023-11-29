package io.agora.scene.joy.service

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.io.Serializable

data class JoyUserInfo constructor(
    var userId: Int = 0,
    var userName: String = "",
    var avatar: String = "",
    var createdAt: Long = System.currentTimeMillis(),

    var objectId: String = ""
)

data class JoyRoomInfo constructor(
    var roomNo: String = "",//房间号
    var roomName: String = "", //房间名
    var roomUserCount: Int = 0,//房间内人数
    var thumbnailId: String?,  //缩略图id
    var ownerId: Int = 0, // 房主 userid(rtc uid)
    var ownerAvatar: String?, // 房主头像
    var ownerName: String?, // 房主名
    var gameId: String="", // 游戏 id
    var badgeTitle: String="",//胖可争霸/羊羊抗狼
    var assistantUid: Int = 0, //游戏画面uid
    var createdAt: Long = System.currentTimeMillis(),

    var objectId: String = ""
):Serializable

data class JoyGameInfo constructor(
    @DrawableRes val drawableId: Int
)