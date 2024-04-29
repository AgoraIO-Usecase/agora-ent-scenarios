package io.agora.rtmsyncmanager.service.rtm

import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import java.util.UUID

const val kAUISeatEnterInterface = "/v1/seat/enter"
const val kAUISeatLeaveInterface = "/v1/seat/leave"
const val kAUISeatKickInterface = "/v1/seat/kick"
const val kAUISeatMuteAudioInterface = "/v1/seat/audio/mute"
const val kAUISeatUnmuteAudioInterface = "/v1/seat/audio/unmute"
const val kAUISeatLockInterface = "/v1/seat/lock"
const val kAUISeatUnlockInterface = "/v1/seat/unlock"

data class AUIRtmMicSeatInfo(
    val roomId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val micSeatNo: Int
)

const val kAUISongAddNetworkInterface = "/v1/song/add"
const val kAUISongPinNetworkInterface = "/v1/song/pin"
const val kAUISongRemoveNetworkInterface = "/v1/song/remove"
const val kAUISongPlayNetworkInterface = "/v1/song/play"
const val kAUISongStopNetworkInterface = "/v1/song/stop"

data class AUIRtmSongInfo(
    val roomId: String,
    val userId: String,

    val songCode: String = "",
    val singer: String = "",
    val name: String = "",
    val poster: String = "",
    val duration: Int = 0,
    val musicUrl: String? = "",
    val lrcUrl: String? = "",
    val owner: AUIUserThumbnailInfo? = null
)

const val kAUIPlayerJoinInterface = "/v1/chorus/join"
const val kAUIPlayerLeaveInterface = "/v1/chorus/leave"

data class AUIRtmPlayerInfo(
    val songCode: String,
    val userId: String,
    val roomId: String
)

data class AUIRtmPublishModel<Model>(
    val uniqueId: String? = UUID.randomUUID().toString(),
    val interfaceName: String?,
    val data: Model?,
    val channelName: String?
)

data class AUIRtmReceiptModel(
    val uniqueId: String,
    val code: Int,
    val channelName: String,
    val reason: String
)

data class AUIRtmPayload<Payload>(
    val roomId: String,
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis(),
    val payload: Payload?
)