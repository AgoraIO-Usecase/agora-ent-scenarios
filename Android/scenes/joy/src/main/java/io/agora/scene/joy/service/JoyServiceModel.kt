package io.agora.scene.joy.service

import io.agora.scene.base.manager.UserManager
import java.io.Serializable

data class JoyUserInfo constructor(
    var userId: Int = 0,
    var userName: String = "",
    var avatar: String = "",
    var createdAt: Long = System.currentTimeMillis(),

    var objectId: String = ""
)

data class JoyRoomInfo constructor(
    var roomId: String = "",//房间号
    var roomName: String = "", //房间名
    var roomUserCount: Int = 0,//房间内人数
    var thumbnailId: String?,  //缩略图id
    var ownerId: Int = 0, // 房主 userid(rtc uid)
    var ownerAvatar: String?, // 房主头像
    var ownerName: String?, // 房主名
    var badgeTitle: String = "",//胖可争霸/羊羊抗狼
    var createdAt: Long = System.currentTimeMillis(),

    var objectId: String = ""
) : Serializable {

    val mIsOwner: Boolean
        get() = ownerId.toLong() == UserManager.getInstance().user.id
}

data class JoyStartGameInfo constructor(
    var gameId: String = "", //游戏 id
    var taskId: String = "", // 游戏 taskid
    var assistantUid: Int = 0, //游戏画面uid
    var gameName: String = "",
    var objectId: String = ""
) : Serializable

data class JoyMessage constructor(
    var userId: String = "",     //用户id (rtc uid)
    var userName: String?,        //用户名
    var message: String?,         //消息文本内容
    var createAt: Long = 0,      //创建时间，与19700101时间比较的毫秒数

    var objectId: String = ""
)