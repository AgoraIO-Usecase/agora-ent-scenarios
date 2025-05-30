package io.agora.scene.voice.spatial.model

import android.graphics.PointF
import com.google.gson.annotations.SerializedName
import io.agora.scene.voice.spatial.global.ConfigConstants

enum class MicRequestStatus(val value: Int){
    idle(0),
    waitting(1),// Waiting
    accepted(2),// Accepted
    rejected(3),// Rejected
    ended(4)// Ended
}

/**
 * Create room data
 */
data class VoiceCreateRoomModel constructor(
    val roomName: String,
    val isPrivate: Boolean,
    val password: String,
    val soundEffect: Int = 0,
    val roomType: Int = 0,
) : BaseRoomBean

/**
 * User data
 * @see io.agora.scene.base.api.model.User
 */
data class VoiceMemberModel constructor(

    // Use user.userNo here
    @SerializedName("uid") var userId: String? = null,
    @SerializedName("name") var nickName: String? = null,
    @SerializedName("portrait") var portrait: String? = null,
    // Use user.id here
    @SerializedName("rtc_uid") var rtcUid: Int = 0,
    @SerializedName("mic_index") var micIndex: Int = -1,
    // -1: none, 0: un mute, 1: mute
    @SerializedName("mic_status") var micStatus: Int = 0,
    @SerializedName("status") var status: Int = MicRequestStatus.idle.value
) : BaseRoomBean

/**
 * Contribution list
 */
data class VoiceRankUserModel constructor(
    @SerializedName("chat_uid") var chatUid: String? = null,
    var name: String? = null,
    var portrait: String? = "",
    var amount: Int = 0
) : BaseRoomBean

/**
 * Room data
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
 * Seat data
 */
data class VoiceMicInfoModel constructor(
    @SerializedName("mic_index") var micIndex: Int = -1,
    var member: VoiceMemberModel? = null,
    @SerializedName("status") var micStatus: Int = -1, // Seat status
    @Transient var ownerTag: Boolean = false,
    @Transient var audioVolumeType: Int = ConfigConstants.VolumeType.Volume_None,
    @Transient var position: PointF = PointF(0f, 0f),
    @Transient var forward: PointF = PointF(0f, 0f),
    @Transient var isSpatialSet: Boolean = false,
) : BaseRoomBean

/**
 * Application data
 */
data class VoiceRoomApply constructor(
    var index: Int? = -1,
    var member: VoiceMemberModel? = null,
    var created_at:Long? = 0

) :BaseRoomBean

/**
 * Room details
 */
data class VoiceRoomInfo constructor(
    var roomInfo: VoiceRoomModel? = null,
    var micInfo: List<VoiceMicInfoModel>? = null,
    var robotInfo: RobotSpatialAudioModel? = null
) : BaseRoomBean

/**
 * Gift
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
