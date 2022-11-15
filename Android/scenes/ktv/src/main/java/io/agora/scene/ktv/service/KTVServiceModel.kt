package io.agora.scene.ktv.service

import io.agora.scene.base.R

data class VLRoomListModel(
    val roomNo: String = "",
    val name: String = "",
    val icon: String = "",
    val isPrivate: Boolean = false,
    var password: String = "",
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

data class VLRoomSeatModel(
    val isMaster: Boolean,// 是否是房主
    val headUrl: String,// 头像
    val userNo: String,// 座位上用户no
    val rtcUid: String,// 座位上用户id，与rtc的userId一致
    val name: String,// 座位上用户昵称
    val seatIndex: Int,// 座位编号
    val joinSing: Boolean,// 是否合唱
    val isSelfMuted: Int,// 是否静音
    val isVideoMuted: Int,// 是否开启视频

    // TODO 下面两个字段似乎是多余的？
    val ifSelTheSingSong: Boolean,// 判断当前歌曲是否是自己点的
    val ifJoinedChorus: Boolean,
) : java.io.Serializable


data class KTVCreateRoomInputModel(
    val icon: String,
    val isPrivate: Int,
    val name: String,
    val password: String,
    val userNo: String,
)

data class KTVCreateRoomOutputModel(
    val roomNo: String?,
    val password: String?,
)


data class KTVJoinRoomInputModel(
    val roomNo: String,
    val password: String?,
)

data class KTVJoinRoomOutputModel(
    val roomName: String,
    val roomNo: String,
    val creatorNo: String,
    val bgOption: String,
    val seatsArray: List<VLRoomSeatModel>?,
    /**
     * 房间内人数
     */
    val roomPeopleNum: Int,

    val agoraRTMToken: String,
    val agoraRTCToken: String,
    val agoraPlayerRTCToken: String,
) : java.io.Serializable


data class KTVChangeMVCoverInputModel(
    val mvIndex: Int
)

data class KTVOnSeatInputModel(
    val seatIndex: Int
)

data class KTVOutSeatInputModel(
    val userNo: String,
    val userId: String,
    val userName: String,
    val userHeadUrl: String,
    val userOnSeat: Int,
)

data class KTVRemoveSongInputModel(
    val songNo: String,
)

data class VLRoomSelSongModel(
    // 获取歌词列表返回的歌词信息
    val songName: String,// 歌曲名
    val songNo: String, // 歌词唯一标识
    val singer: String, // 演唱者
    val imageUrl: String,// 歌曲封面

    // 获取已点歌记返回的歌词信息，同时也包含上面信息
    val userNo: String? = null,// 点歌人No
    val userId: String? = null,// 点歌人id
    val name: String? = null,// 点歌人昵称
    val chorusNo: String? = null, // 合唱者userNo
    val isChorus: Boolean = false, // 是否合唱
    val isOriginal: Int = 0, //是否原唱
    val sort: Int = 0,// 已点歌曲的播放顺序排序
)

data class KTVJoinChorusInputModel(
    val songNo: String
)

data class KTVSongDetailInputModel(
    val songNo: String
)

data class KTVSongDetailOutputModel(
    val songNo: String,
    val lyric: String,
    val songUrl: String
)

data class KTVChooseSongInputModel(
    val isChorus: Int,
    val songName: String,
    val songNo: String,
    val singer: String,
    val imageUrl: String,
)

data class KTVMakeSongTopInputModel(
    val songNo: String,
    val sort: Int,
)