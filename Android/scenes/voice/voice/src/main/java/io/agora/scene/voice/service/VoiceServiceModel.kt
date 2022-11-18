package io.agora.scene.voice.service

import com.google.gson.annotations.SerializedName
import io.agora.scene.voice.bean.BaseRoomBean

/**
 * @author create by zhangwei03
 */
open class VoiceChatBaseModel(
    var objectId: String? = null //  // SyncManager独有，用来更新和删除数据
)

data class VoiceCreateRoomModel constructor(
    val roomName: String,
    val isPrivate: Boolean,
    val password: String,
    val soundEffect: Int = 0,
    val roomType: Int = 0,
) : BaseRoomBean

data class VoiceMemberModel constructor(
    @SerializedName("uid") var uid: String? = null,
    @SerializedName("chat_uid") var chatUid: String? = null,
    @SerializedName("name") var nickName: String? = null,
    @SerializedName("portrait") var portrait: String? = null,
    @SerializedName("rtc_uid") var rtcUid: Int = 0,
    @SerializedName("mic_index") var micIndex: Int = 0,
) : BaseRoomBean

data class VoiceRankUserModel constructor(
    val name: String? = null,
    val portrait: String = "",
    val amount: Int = 0
) : BaseRoomBean

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
) : BaseRoomBean

data class VoiceMicInfoModel constructor(
    var index: Int = 0,
    var userInfo: VoiceMemberModel? = null,
    var micStatus: Int = -1, // 座位状态
    var userStatus: Int = -1, // 用户状态，备用
) : BaseRoomBean
