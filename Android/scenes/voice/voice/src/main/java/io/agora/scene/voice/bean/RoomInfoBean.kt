package io.agora.scene.voice.bean

import io.agora.voice.buddy.config.ConfigConstants

data class RoomInfoBean constructor(
    var roomId: String = "",
    var channelId: String = "",
    var chatroomId: String = "",
    var chatroomName: String = "",
    var owner: io.agora.scene.voice.bean.RoomUserInfoBean? = null,
    var memberCount: Int = 0,
    var giftCount: Int = 0,
    var watchCount: Int = 0,
    var soundSelection: Int = ConfigConstants.SoundSelection.Social_Chat,
    var topRankUsers: List<io.agora.scene.voice.bean.RoomRankUserBean> = emptyList(), // 前三名
    var roomType: Int = ConfigConstants.RoomType.Common_Chatroom // 房间类型
) : io.agora.scene.voice.bean.BaseRoomBean