package io.agora.scene.voice.bean

import io.agora.voice.buddy.config.ConfigConstants
import java.io.Serializable

/**
 * @author create by zhangwei03
 *
 * 房间初始化属性，不会更改
 */
data class RoomKitBean constructor(
    var roomId: String = "",
    var channelId: String = "",
    var chatroomId: String = "",
    var ownerId: String = "",
    var roomType: Int = ConfigConstants.RoomType.Common_Chatroom,
    var isOwner: Boolean = false,
    var soundEffect: Int = ConfigConstants.SoundSelection.Social_Chat
) : Serializable