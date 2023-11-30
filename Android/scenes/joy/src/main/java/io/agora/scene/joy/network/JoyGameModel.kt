package io.agora.scene.joy.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

interface JoyJsonModel {

    class JoyEmpty : JoyJsonModel
}

data class JoyApiResult<T> constructor(
    @Expose
    @SerializedName("message")
    var msg: String? = null,
    @Expose
    @SerializedName("err_msg")
    var errorMsg: String? = null,
    @Expose
    @SerializedName("code")
    var code: Int = 0,
    @Expose
    @SerializedName("result")
    var data: T? = null,
    @SerializedName("trace")
    var trace: String? = null,
    @SerializedName("uri")
    var uri: String? = null
) {
    val isSucceed: Boolean
        get() = code == 200 || code == 0
}

data class JoyRtcConfig constructor(
    @Expose
    @SerializedName("broadcast_uid")
    var broadcastUid: Int = 0,
    @Expose
    @SerializedName("uid")
    var uid: Int = 0,
    @Expose
    @SerializedName("token")
    var token: String? = null,
    @Expose
    @SerializedName("channel_name")
    var channelName: String? = null
) : JoyJsonModel

data class JoyGameEntity constructor(
    @Expose
    @SerializedName("id")
    var id: Long? = null,
    @Expose
    @SerializedName("vid")
    var vid: String? = null,
    @Expose
    @SerializedName("uid")
    var uid: Int = 0,
    @Expose
    @SerializedName("room_id")
    var roomId: String? = null,
    @Expose
    @SerializedName("open_id")
    var openId: String? = null,
    @Expose
    @SerializedName("task_id")
    var taskId: String? = null,
    @Expose
    @SerializedName("nickname")
    var nickname: String? = null,
    @Expose
    @SerializedName("avatar")
    var avatar: String? = null,
    @Expose
    @SerializedName("rtc_config")
    var rtcConfig: JoyRtcConfig? = null,
    @Expose
    @SerializedName("game_id")
    var gameId: String? = null,
    @Expose
    @SerializedName("introduce")
    var brief: String? = null,
    @Expose
    @SerializedName("name")
    var name: String? = null,
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
    @SerializedName("name")
    var name: String? = null,
    @Expose
    @SerializedName("id")
    var id: String? = null,
    @Expose
    @SerializedName("msg_id")
    var msgId: String? = null,
    @Expose
    @SerializedName("open_id")
    var openId: String? = null,
    @Expose
    @SerializedName("avatar")
    var avatar: String? = null,
    @Expose
    @SerializedName("nickname")
    var nickname: String? = null,
    @Expose
    @SerializedName("gift_id")
    var giftId: String? = null,
    @Expose
    @SerializedName("gift_num")
    var giftNum: Int = 0,
    @Expose
    @SerializedName("value")
    var value: Int = 0,
    @Expose
    @SerializedName("gift_value")
    var giftValue: Int = 0,
    @Expose
    @SerializedName("vendor_gift_id")
    var vendorGiftId: String? = null,
    @Expose
    @SerializedName("timestamp")
    var timestamp: Long? = null,
    @Expose
    @SerializedName("game_id")
    var gameId: String? = null,
    @Expose
    @SerializedName("thumbnail")
    var thumbnail: String? = null,
    @Expose
    @SerializedName("smallPath")
    var smallPath: String? = null,
    @Expose
    @SerializedName("price")
    var price: Int = 0,
    var isSelect: Boolean = false
) : JoyJsonModel

data class JoyGameResult constructor(
    @Expose
    @SerializedName("list")
    var list: List<JoyGameEntity>? = null,
    @Expose
    @SerializedName("task_id")
    var taskId: String? = null,
    @Expose
    @SerializedName("status")
    var status: String? = null,
    @Expose
    @SerializedName("feature")
    var feature: JoyFeatureEntity? = null,
    @Expose
    @SerializedName("gifts")
    var gifts: List<JoyGiftEntity>? = null
) : JoyJsonModel

data class JoyMessageEntity constructor(
    @Expose
    @SerializedName("msg_id")
    var msgId: String? = null,
    @Expose
    @SerializedName("open_id")
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
    @SerializedName("gift_id")
    var giftId: String? = null,
    @Expose
    @SerializedName("gift_num")
    var giftNum: Int = 0,
    @Expose
    @SerializedName("like_num")
    var likeNum: Int = 0,
    @Expose
    @SerializedName("gift_value")
    var giftValue: Int = 0,
    @Expose
    @SerializedName("timestamp")
    var timestamp: Long? = null,
) : JoyJsonModel

data class JoySendMessage constructor(
    @Expose
    @SerializedName("vid")
    var vid: String? = null,
    @Expose
    @SerializedName("room_id")
    var roomId: String? = null,
    @Expose
    @SerializedName("payload")
    var payload: List<JoyMessageEntity>? = null
) : JoyJsonModel
