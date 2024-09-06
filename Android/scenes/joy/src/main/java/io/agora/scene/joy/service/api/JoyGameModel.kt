package io.agora.scene.joy.service.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.agora.scene.joy.service.base.JoyJsonModel

data class JoyRtcConfig constructor(
    @Expose
    @SerializedName("broadcastUid")
    var broadcastUid: Int = 0,
    @Expose
    @SerializedName("uid")
    var uid: Int = 0,
    @Expose
    @SerializedName("token")
    var token: String? = null,
    @Expose
    @SerializedName("channelName")
    var channelName: String? = null
) : JoyJsonModel

data class JoyGameEntity constructor(
    @Expose
    @SerializedName("appId")
    var appId: String? = null,
    @Expose
    @SerializedName("basicAuth")
    var basicAuth: String? = null, //basicAuth 认证凭据，白名单AppID 无须上传
    @Expose
    @SerializedName("src")
    var src: String? = "Android", // 来源/请求方
    @Expose
    @SerializedName("traceId")
    var traceId: String? = null, // 请求ID
    @Expose
    @SerializedName("id")
    var id: Long? = null,
    @Expose
    @SerializedName("uid")
    var uid: Int? = null,
    @Expose
    @SerializedName("roomId")
    var roomId: String? = null,
    @Expose
    @SerializedName("openId")
    var openId: String? = null,
    @Expose
    @SerializedName("taskId")
    var taskId: String? = null,
    @Expose
    @SerializedName("nickname")
    var nickname: String? = null,
    @Expose
    @SerializedName("avatar")
    var avatar: String? = null,
    @Expose
    @SerializedName("rtcConfig")
    var rtcConfig: JoyRtcConfig? = null,
    @Expose
    @SerializedName("gameId")
    var gameId: String? = null,
    @Expose
    @SerializedName("introduce")
    var introduce: String? = null,
    @Expose
    @SerializedName("name")
    var name: String? = null,
    @Expose
    @SerializedName("thumbnail")
    var thumbnail: String? = null,
    @Expose
    @SerializedName("vendor")
    var vendor: String? = null,
    @Expose
    @SerializedName("vendor_game_id")
    var vendorGameId: String? = null,
    @Expose
    @SerializedName("channel_name")
    var channelName: String? = null,
    @Expose
    @SerializedName("rtc_uid")
    var rtcUid: String? = null,
    @Expose
    @SerializedName("rtc_token")
    var rtcToken: String? = null,
) : JoyJsonModel

data class JoyFeatureEntity constructor(
    @Expose
    @SerializedName("like")
    var like: Int = 0,
    @Expose
    @SerializedName("comment")
    var comment: Int = 0
) : JoyJsonModel

data class JoyGiftEntity constructor(
    @Expose
    @SerializedName("game_id")
    var gameId: String? = null,
    @Expose
    @SerializedName("id")
    var id: String? = null,
    @Expose
    @SerializedName("name")
    var name: String? = null,
    @Expose
    @SerializedName("price")
    var price: Int = 0,
    @Expose
    @SerializedName("thumbnail")
    var thumbnail: String? = null,
    @Expose
    @SerializedName("value")
    var value: Int = 0,
    @Expose
    @SerializedName("vendor_gift_id")
    var vendorGiftId: String? = null,
    var isSelect: Boolean = false
) : JoyJsonModel

/**
 * 游戏列表
 */
data class JoyGameListResult constructor(
    @Expose
    @SerializedName("game_id")
    var gameId: String? = null,
    @Expose
    @SerializedName("introduce")
    var introduce: String? = null,
    @Expose
    @SerializedName("name")
    var name: String? = null,
    @Expose
    @SerializedName("thumbnail")
    var thumbnail: String? = null,
    @Expose
    @SerializedName("vendor")
    var vendor: String? = null,
    @Expose
    @SerializedName("vendor_game_id")
    var vendorGameId: String? = null,
) : JoyJsonModel

data class JoyAction constructor(
    @Expose
    @SerializedName("command")
    var command: List<String>? = null,
    var icon: String? = null,
    var index: Int = 0,
)

data class JoyGameDetailResult constructor(
    @Expose
    @SerializedName("actions")
    var actions: List<JoyAction>? = null,
    @Expose
    @SerializedName("feature")
    var feature: JoyFeatureEntity? = null,
    @Expose
    @SerializedName("game_id")
    var gameId: String? = null,
    @Expose
    @SerializedName("gifts")
    var gifts: List<JoyGiftEntity>? = null,
    @Expose
    @SerializedName("instruct")
    var instruct: List<Any>? = null,
    @Expose
    @SerializedName("introduce")
    var introduce: String? = null,
    @Expose
    @SerializedName("name")
    var name: String? = null,
    @Expose
    @SerializedName("thumbnail")
    var thumbnail: String? = null,
    @Expose
    @SerializedName("vendor")
    var vendor: String? = null,
) : JoyJsonModel

data class JoyGameResult constructor(
    @Expose
    @SerializedName("list")
    var list: List<JoyGameListResult>? = null,
    @Expose
    @SerializedName("status")
    var status: String? = null,
    @Expose
    @SerializedName("page_num")
    var pageNum: Int? = null,
    @Expose
    @SerializedName("page_size")
    var pageSize: Int? = null,
    @Expose
    @SerializedName("total")
    var total: Int? = null,
    @Expose
    @SerializedName("task_id")
    var taskId: String? = null,
    @Expose
    @SerializedName("carousel")
    var bannerList: List<JoyGameBanner>? = null
) : JoyJsonModel

data class JoyMessageEntity constructor(
    @Expose
    @SerializedName("msgId")
    var msgId: String? = null,
    @Expose
    @SerializedName("openId")
    var openId: String? = null,
    @Expose
    @SerializedName("avatar")
    var avatar: String? = null,
    @Expose
    @SerializedName("nickname")
    var nickname: String? = null,
    @Expose
    @SerializedName("content")
    var content: String? = null,
    @Expose
    @SerializedName("giftId")
    var giftId: String? = null,
    @Expose
    @SerializedName("giftNum")
    var giftNum: Int? = null,
    @Expose
    @SerializedName("likeNum")
    var likeNum: Int? = null,
    @Expose
    @SerializedName("giftValue")
    var giftValue: Int? = null,
    @Expose
    @SerializedName("timestamp")
    var timestamp: Long? = null,
) : JoyJsonModel

data class JoySendMessage constructor(
    @Expose
    @SerializedName("appId")
    var appId: String? = null,
    @Expose
    @SerializedName("basicAuth")
    var basicAuth: String? = null, //basicAuth 认证凭据，白名单AppID 无须上传
    @Expose
    @SerializedName("src")
    var src: String? = "Android", // 来源/请求方
    @Expose
    @SerializedName("traceId")
    var traceId: String? = null, // 请求ID
    @Expose
    @SerializedName("gameId")
    var gameId: String? = null,
    @Expose
    @SerializedName("roomId")
    var roomId: String? = null,
    @Expose
    @SerializedName("payload")
    var payload: List<JoyMessageEntity>? = null,
) : JoyJsonModel

data class JoyGameBanner constructor(
    @Expose
    @SerializedName("index")
    var roomId: Int? = null,
    @Expose
    @SerializedName("url")
    var url: String? = null,
)

enum class JoyGameStatus {
    schedule,
    scheduled,
    starting,
    start_failed,
    started,
    stopping,
    stopped
}
