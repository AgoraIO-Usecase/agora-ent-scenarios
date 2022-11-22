package io.agora.scene.voice.bean

/**
 * @author create by zhangwei03
 */
data class RoomUserInfoBean constructor(
    var userId: String = "",
    var chatUid: String = "",
    var rtcUid: Int = -1,
    var username: String = "",
    var userAvatar: String = "",
) : BaseRoomBean