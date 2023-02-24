package io.agora.scene.voice.spatial.model

import android.graphics.PointF
import com.google.gson.annotations.SerializedName
import io.agora.voice.common.constant.ConfigConstants

enum class MicRequestStatus(val value: Int){
    idle(0),
    waitting(1),// 等待中
    accepted(2),//  已接受
    rejected(3),// 已拒绝
    ended(4)// 已结束
}

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
    @SerializedName("name") var nickName: String? = null,
    @SerializedName("portrait") var portrait: String? = null,
    // 这里用的是user.id
    @SerializedName("rtc_uid") var rtcUid: Int = 0,
    @SerializedName("mic_index") var micIndex: Int = -1,
    @SerializedName("status") var status: Int = MicRequestStatus.idle.value
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
    @SerializedName("type") var roomType: Int = 1,
    @SerializedName("name") var roomName: String = "",
    @SerializedName("sound_effect") var soundEffect: Int = 0,
    @SerializedName("channel_id") var channelId: String = "",
    @SerializedName("created_at") var createdAt: Long = 0,
    @SerializedName("roomPassword") var roomPassword: String = "",
    @Transient var rankingList: List<VoiceRankUserModel>? = null,
    @Transient var memberList: List<VoiceMemberModel>? = null,
    @Transient var giftAmount: Int = 0,
    @SerializedName("announcement") var announcement: String = "",
) : BaseRoomBean

data class RobotSpatialAudioModel constructor(
    @SerializedName("use_robot") var useRobot: Boolean = false,
    @SerializedName("robot_volume") var robotVolume: Int = 50,
    // [0, 1]
    @SerializedName("red_robot_attenuation") var redRobotAttenuation: Double = 0.5,
    @SerializedName("red_robot_absorb") var redRobotAbsorb: Boolean = true,
    @SerializedName("red_robot_blur") var redRobotBlur: Boolean = false,
    // [0, 1]
    @SerializedName("blue_robot_attenuation") var blueRobotAttenuation: Double = 0.5,
    @SerializedName("blue_robot_absorb") var blueRobotAbsorb: Boolean = true,
    @SerializedName("blue_robot_blur") var blueRobotBlur: Boolean = false,
) : BaseRoomBean

/**
 * 麦位数据
 */
data class VoiceMicInfoModel constructor(
    @SerializedName("mic_index") var micIndex: Int = -1,
    var member: VoiceMemberModel? = null,
    @SerializedName("status") var micStatus: Int = -1, // 座位状态
    @Transient var userStatus: Int = -1, // 用户状态，备用
    @Transient var ownerTag: Boolean = false,
    @Transient var audioVolumeType: Int = ConfigConstants.VolumeType.Volume_None,
    @Transient var position: PointF = PointF(0f, 0f),
    @Transient var forward: PointF = PointF(0f, 0f),
    @Transient var isSpatialSet: Boolean = false,
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
    var robotInfo: RobotSpatialAudioModel? = null
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
