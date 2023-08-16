package io.agora.scene.showTo1v1.service

import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcherAPI

open class ShowTo1v1UserInfo constructor(
    val userId: String,
    val userName: String,
    var avatar: String,
) {

    fun getIntUserId(): Int {
        return userId.toIntOrNull() ?: 0
    }

    fun get1v1ChannelId(): String {
        return "1v1_$userId"
    }

    fun bgImage(): String {
        val uid = getIntUserId()
        return "user_bg${uid % 9 + 1}"
    }
}

class ShowTo1v1RoomInfo constructor(
    val roomId: String,
    val roomName: String,
    var createdAt: Long, userId: String, userName: String, avatar: String,
) : ShowTo1v1UserInfo(userId, userName, avatar) {

    fun createRoomInfo(token: String): VideoSwitcherAPI.RoomInfo {
        return VideoSwitcherAPI.RoomInfo(
            channelName = roomId,
            uid = getIntUserId(),
            token = token,
            eventHandler = null
        )
    }
}