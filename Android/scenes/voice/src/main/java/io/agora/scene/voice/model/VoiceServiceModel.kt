package io.agora.scene.voice.model

import com.google.gson.annotations.SerializedName
import io.agora.voice.common.constant.ConfigConstants

/**
 *创建房间数据
 */
data class VoiceCreateRoomModel constructor(
    val roomName: String,
    val isPrivate: Boolean,
    val password: String,
    val soundEffect: Int = 0,
    val roomType: Int = 0,
) : BaseRoomBean

/**
 * 用户数据
 * @see io.agora.scene.base.api.model.User
 */
data class VoiceMemberModel constructor(

    // 这里用的是user.userNo
    @SerializedName("uid") var userId: String? = null,
    // 这里用的是user.id
    @SerializedName("chat_uid") var chatUid: String? = null,
    @SerializedName("name") var nickName: String? = null,
    @SerializedName("portrait") var portrait: String? = null,
    // 这里用的是user.id
    @SerializedName("rtc_uid") var rtcUid: Int = 0,
    @SerializedName("mic_index") var micIndex: Int = -1,
    @SerializedName("micStatus") var micStatus: Int = 1, // 角色麦位状态(0 关 1 开)
) : BaseRoomBean

/**
 * 贡献榜
 */
data class VoiceRankUserModel constructor(
    @SerializedName("chat_uid") var chatUid: String? = null,
    var name: String? = null,
    var portrait: String? = "",
    var amount: Int = 0
) : BaseRoomBean

/**
 * 房间数据
 */
data class VoiceRoomModel constructor(
    var owner: VoiceMemberModel? = null,
    @SerializedName("room_id") var roomId: String = "",
    @SerializedName("is_private") var isPrivate: Boolean = false,
    @SerializedName("member_count") var memberCount: Int = 0,
    @SerializedName("click_count") var clickCount: Int = 0,
    @SerializedName("type") var roomType: Int = 0,
    @SerializedName("name") var roomName: String = "",
    @SerializedName("sound_effect") var soundEffect: Int = 0,
    @SerializedName("channel_id") var channelId: String = "",
    @SerializedName("chatroom_id") var chatroomId: String = "",
    @SerializedName("created_at") var createdAt: Long = 0,
    @SerializedName("roomPassword") var roomPassword: String = "",
    @Transient var rankingList: List<VoiceRankUserModel>? = null,
    @Transient var memberList: List<VoiceMemberModel>? = null,
    @Transient var giftAmount: Int = 0,
    @Transient var useRobot: Boolean = false,
    @Transient var robotVolume: Int = 50,
    @Transient var announcement: String = "",
) : BaseRoomBean

data class VoiceBgmModel constructor(
    var songName: String = "",
    var singerName: String = "",
    var isOrigin: Boolean = false,
) : BaseRoomBean

/**
 * 麦位数据
 */
data class VoiceMicInfoModel constructor(
    @SerializedName("mic_index") var micIndex: Int = -1,
    var member: VoiceMemberModel? = null,
    @SerializedName("status") var micStatus: Int = -1, // 座位状态
    @Transient var ownerTag: Boolean = false,
    @Transient var audioVolumeType: Int = ConfigConstants.VolumeType.Volume_None,
) : BaseRoomBean

/**
 * 申请数据
 */
data class VoiceRoomApply constructor(
    var index: Int? = -1,
    var member: VoiceMemberModel? = null,
    var created_at:Long? = 0

) :BaseRoomBean

/**
 * 房间详情
 */
data class VoiceRoomInfo constructor(
    var roomInfo: VoiceRoomModel? = null,
    var micInfo: List<VoiceMicInfoModel>? = null,
    var bgmInfo: VoiceBgmModel? = null
) : BaseRoomBean

/**
 * 礼物
 */
data class VoiceGiftModel constructor(
    var gift_id: String? = "",
    var gift_count:String? = "",
    var gift_name: String? = "",
    var gift_price: String? = "",
    var userName: String? = "",
    var portrait: String? = "",
    var isChecked: Boolean? = false
)
