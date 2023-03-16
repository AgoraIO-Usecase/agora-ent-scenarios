package io.agora.scene.ktv.service

import io.agora.scene.base.R

data class RoomListModel(
    val roomNo: String = "",
    val name: String = "",
    val icon: String = "",
    val isPrivate: Boolean = false,
    val password: String = "",
    val creatorNo: String = "",
    val createdAt: String = System.currentTimeMillis().toString(),

    /**
     * 背景图
     */
    val bgOption: String = "",
    /**
     * 房间内人数
     */
    val roomPeopleNum: Int = 0,
) {
    fun getCoverRes(): Int {
        return when (icon) {
            "1" -> R.mipmap.icon_room_cover1
            "2" -> R.mipmap.icon_room_cover2
            "3" -> R.mipmap.icon_room_cover3
            "4" -> R.mipmap.icon_room_cover4
            "5" -> R.mipmap.icon_room_cover5
            "6" -> R.mipmap.icon_room_cover6
            "7" -> R.mipmap.icon_room_cover7
            "8" -> R.mipmap.icon_room_cover8
            "9" -> R.mipmap.icon_room_cover9
            else -> R.mipmap.icon_room_cover1
        }
    }
}

data class RoomSeatModel(
    val isMaster: Boolean,// 是否是房主
    val headUrl: String,// 头像
    val userNo: String,// 座位上用户no
    val rtcUid: String,// 座位上用户id，与rtc的userId一致
    val name: String,// 座位上用户昵称
    val seatIndex: Int,// 座位编号
    val chorusSongCode: String = "",// 是否合唱
    val isAudioMuted: Int,// 是否静音
    val isVideoMuted: Int,// 是否开启视频
) : java.io.Serializable {

    companion object{
        val MUTED_VALUE_TRUE = 1

        val MUTED_VALUE_FALSE = 0
    }
}


data class CreateRoomInputModel(
    val icon: String,
    val isPrivate: Int,
    val name: String,
    val password: String,
    val userNo: String,
)

data class CreateRoomOutputModel(
    val roomNo: String?,
    val password: String?,
)


data class JoinRoomInputModel(
    val roomNo: String,
    val password: String?,
)

data class JoinRoomOutputModel(
    val roomName: String,
    val roomNo: String,
    val creatorNo: String,
    val bgOption: String,
    val seatsArray: List<RoomSeatModel>?,
    /**
     * 房间内人数
     */
    val roomPeopleNum: Int,

    val agoraRTMToken: String,
    val agoraRTCToken: String,
    val agoraPlayerRTCToken: String,
    val createdAt: String
) : java.io.Serializable


data class ChangeMVCoverInputModel(
    val mvIndex: Int
)

data class OnSeatInputModel(
    val seatIndex: Int
)

data class OutSeatInputModel(
    val userNo: String,
    val userId: String,
    val userName: String,
    val userHeadUrl: String,
    val userOnSeat: Int,
)

data class RemoveSongInputModel(
    val songNo: String,
)

data class RoomSelSongModel(
    // 获取歌词列表返回的歌词信息
    val songName: String,// 歌曲名
    val songNo: String, // 歌词唯一标识
    val singer: String, // 演唱者
    val imageUrl: String,// 歌曲封面

    // 获取已点歌记返回的歌词信息，同时也包含上面信息
    val userNo: String? = null,// 点歌人No
    val name: String? = null,// 点歌人昵称
    val isOriginal: Int = 0, //是否原唱

    // 排序字段
    val status : Int, // 0 未开始 1.已唱 2.正在唱
    val createAt: Double,
    val pinAt: Double
){
    companion object {
        val STATUS_IDLE = 0
        val STATUS_PLAYED = 1
        val STATUS_PLAYING = 2
    }
}

data class JoinChorusInputModel(
    val songNo: String
)

data class ChooseSongInputModel(
    val songName: String,
    val songNo: String,
    val singer: String,
    val imageUrl: String,
)

data class MakeSongTopInputModel(
    val songNo: String
)

data class UpdateSingingScoreInputModel(
    val score: Double
)

data class ScoringAlgoControlModel(
    val level: Int,
    val offset: Int
)
