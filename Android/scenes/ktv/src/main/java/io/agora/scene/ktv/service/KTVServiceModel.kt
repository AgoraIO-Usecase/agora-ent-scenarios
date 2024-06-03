package io.agora.scene.ktv.service

import androidx.annotation.IntDef
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import java.io.Serializable

/**
 * Ktv parameters
 * [AUIRoomInfo.customPayload] key
 *
 * @constructor Create empty Ktv parameters
 */
object KTVParameters {
    const val ROOM_USER_COUNT = "roomPeopleNum"
    const val THUMBNAIL_ID = "icon"
    const val PASSWORD = "password"
    const val IS_PRIVATE = "isPrivate"
}

/**
 * Create room input model
 *
 * @property icon
 * @property name
 * @property password
 * @constructor Create empty Create room input model
 */
data class CreateRoomInfo constructor(
    val icon: String,
    val name: String,
    val password: String,
)

/**
 * Room mic seat status
 *
 * @constructor Create empty Room mic seat status
 */
@IntDef(RoomMicSeatStatus.idle, RoomMicSeatStatus.used, RoomMicSeatStatus.locked)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RoomMicSeatStatus {
    companion object {
        const val idle = 0 // 空闲
        const val used = 1 // 使用中
        const val locked = 2 // 锁定
    }
}

enum class RoomSeatCmd {
    enterSeatCmd,
    leaveSeatCmd,
    muteAudioCmd,
    muteVideoCmd,
    kickSeatCmd,
}

/**
 * Room mic seat info
 *
 * @constructor Create empty Room mic seat info
 */
data class RoomMicSeatInfo constructor(
    var owner: AUIUserThumbnailInfo? = null,
    var seatIndex: Int = 0,
    var isAudioMuted: Boolean = false, // 麦位禁用声音
    var isVideoMuted: Boolean = true, // 麦位禁用视频
) : Serializable

enum class RoomChorusCmd {
    joinChorusCmd,
    leaveChorusCmd,
    kickAllOutOfChorusCmd, //移除所有合唱
    KickUserOutOfChorusCmd, //踢出指定用户出合唱列表
}

/**
 * Room chorister info
 *
 * @property userId
 * @property chorusSongNo
 * @constructor Create empty Room chorister info
 */
data class RoomChoristerInfo constructor(
    var userId: String = "",
    var chorusSongNo: String = ""  //合唱演唱歌曲
) : Serializable

@IntDef(PlayStatus.idle, PlayStatus.playing)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
annotation class PlayStatus {
    companion object {
        const val idle = 0 // 未播放
        const val playing = 1 // 播放中
    }
}

enum class RoomSongCmd {
    chooseSongCmd, //添加一首歌
    removeSongCmd, //移除一首歌
    pingSongCmd,  //置顶一首歌
    updatePlayStatusCmd, //更新歌曲播放状态
    removedUserSongsCmd,  //移除指定用户所有歌曲
}

data class ChooseSongInputModel constructor(
    val songName: String,
    val songNo: String,
    val singer: String,
    val imageUrl: String,
)

/**
 * Room song info
 *
 * @property songName
 * @property songNo
 * @property singer
 * @property imageUrl
 * @property owner
 * @property status
 * @property createAt
 * @property pinAt
 * @constructor Create empty Room sel song model
 */
data class ChosenSongInfo constructor(
    // 获取歌词列表返回的歌词信息
    val songName: String,// 歌曲名
    val songNo: String, // 歌词唯一标识
    val singer: String, // 演唱者
    val imageUrl: String,// 歌曲封面

    var owner: AUIUserThumbnailInfo? = null, // 点歌人

    // 排序字段
    @PlayStatus
    val status: Int = PlayStatus.idle, // 0 未开始 1.播放中
    val createAt: Long = 0,
    val pinAt: Long = 0, // 置顶时间
)

/**
 * Enum value or null
 *
 * @param T
 * @param name
 * @return
 */
inline fun <reified T : Enum<T>> enumValueOrNull(name: String?): T? {
    return try {
        enumValueOf<T>(name ?: "")
    } catch (e: IllegalArgumentException) {
        null
    }
}

val AUIUserThumbnailInfo.fullHeadUrl
    get() = if (this.userAvatar.startsWith("http")) {
        this.userAvatar
    } else {
        "file:///android_asset/" + this.userAvatar + ".png"
    }
