package io.agora.scene.voice.service

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
    var uid: String? = null,
    var chatUid: String? = null,
    var nickName: String? = null,
    var portrait: String? = null,
    var rtcUid: Int = 0,
    var micIndex: Int = 0,
) : BaseRoomBean

data class VoiceRankUserModel constructor(
    val name: String? = null,
    val portrait: String = "",
    val amount: Int = 0
) : BaseRoomBean

data class VoiceRoomModel constructor(
    var owner: VoiceMemberModel? = null,
    var roomId: String = "",
    var isPrivate: Boolean = false,
    var memberCount: Int = 0,
    var roomType: Int = 0,
    var roomName: String = "",
    var soundEffect: Int = 0,
    var channelId: String = "",
    var chatroomId: String = "",
    var createdAt: Long = 0,
    var roomPassword: String = "",
) : BaseRoomBean

data class VoiceMicInfoModel constructor(
    var index: Int = 0,
    var userInfo: VoiceMemberModel? = null,
    var micStatus: Int = -1, // 座位状态
    var userStatus: Int = -1, // 用户状态，备用
) : BaseRoomBean
