package io.agora.scene.voice.service

import com.google.gson.annotations.SerializedName
import io.agora.scene.voice.bean.BaseRoomBean

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
 */
data class VoiceMemberModel constructor(
    @SerializedName("uid") var userId: String? = null,
    @SerializedName("chat_uid") var chatUid: String? = null,
    @SerializedName("name") var nickName: String? = null,
    @SerializedName("portrait") var portrait: String? = null,
    @SerializedName("rtc_uid") var rtcUid: Int = 0,
    @SerializedName("mic_index") var micIndex: Int = 0,
) : BaseRoomBean

/**
 * 贡献榜
 */
data class VoiceRankUserModel constructor(
    val name: String? = null,
    val portrait: String = "",
    val amount: Int = 0
) : BaseRoomBean

/**
 * 房间数据
 */
data class VoiceRoomModel constructor(
    var owner: VoiceMemberModel? = null,
    @SerializedName("room_id") var roomId: String = "",
    @SerializedName("is_private") var isPrivate: Boolean = false,
    @SerializedName("member_count") var memberCount: Int = 0,
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
    @Transient var clickCount: Int = 0,
    @Transient var userRobot: Boolean = false,
    @Transient var robotVolume: Int = 50,
) : BaseRoomBean

/**
 * 麦位数据
 */
data class VoiceMicInfoModel constructor(
    @SerializedName("mic_index") var micIndex: Int = 0,
    var member: VoiceMemberModel? = null,
    @SerializedName("status") var micStatus: Int = -1, // 座位状态
    var userStatus: Int = -1, // 用户状态，备用
) : BaseRoomBean

/**
 * 房间详情
 */
data class VoiceRoomInfo constructor(
    var roomInfo: VoiceRoomModel? = null,
    var micInfo: List<VoiceMicInfoModel>? = null
) : BaseRoomBean

/**
 * 礼物
 */
data class VoiceGiftModel constructor(
    var id: String = "",
)

/**
 * 上麦申请消息
 */
data class VoiceRoomApplyModel constructor(
    var id: String = "",
)
