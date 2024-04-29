package io.agora.scene.joy.service

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

object JoyParameters {
    const val ROOM_USER_COUNT = "roomUserCount"
    const val THUMBNAIL_ID = "thumbnailId"
    const val CREATED_AT = "createdAt"
    const val BADGE_TITLE = "badgeTitle"
}

data class JoyStartGameInfo constructor(
    var gameId: String = "", //游戏 id
    var taskId: String = "", // 游戏 taskid
    var assistantUid: Int = 0, //游戏画面uid
    var gameName: String = "",
    @Expose
    @SerializedName("objectId")
    var objectId: String? = null,
) : Serializable

data class JoyMessage constructor(
    var userId: String = "",     //用户id (rtc uid)
    var userName: String?,        //用户名
    var message: String?,         //消息文本内容
    var createAt: Long = 0,      //创建时间，与19700101时间比较的毫秒数
)