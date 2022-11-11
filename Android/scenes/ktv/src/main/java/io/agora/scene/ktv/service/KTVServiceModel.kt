package io.agora.scene.ktv.service

import io.agora.scene.base.R
import io.agora.scene.base.bean.MemberMusicModel

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
    val isMaster: Boolean,
    val headUrl: String,
    val userNo: String,
    val id: String,
    val name: String,
    val onSeat: Int,
    val joinSing: Boolean,
    val isSelfMuted: Int,
    val isVideoMuted: Int,
    val ifSelTheSingSong: Boolean,
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
    val sort: Int,
)

data class VLRoomSelSongModel(
    // 获取歌词列表返回的歌词信息
    val songName: String,// 歌曲名
    val songNo: String, // 歌词唯一标识
    val songUrl: String, // mp3路径
    val singer: String, // 演唱者
    val lyric: String, // 歌词
    val imageUrl: String,// 歌曲封面

    // 获取已点歌记返回的歌词信息，同时也包含上面信息
    val userNo: String? = null,// 点歌人No
    val userId: String? = null,// 点歌人id
    val name: String? = null,// 点歌人昵称
    val chorusNo: String? = null, // 合唱者userNo
    val isChorus: Boolean = false, // 是否合唱
    val isOriginal: Int = 0, //是否原唱
    val sort: Int = 0,// 已点歌曲的播放顺序排序
) {

    // TODO remove it
    fun toMemberMusicModel(): MemberMusicModel {
        return MemberMusicModel().apply {
            chorusNo = this@VLRoomSelSongModel.chorusNo
            imageUrl = this@VLRoomSelSongModel.imageUrl
            isChorus = this@VLRoomSelSongModel.isChorus
            isOriginal = this@VLRoomSelSongModel.isOriginal
            singer = this@VLRoomSelSongModel.singer
            songName = this@VLRoomSelSongModel.songName
            songNo = this@VLRoomSelSongModel.songNo
            songUrl = this@VLRoomSelSongModel.songUrl
            lyric = this@VLRoomSelSongModel.lyric
            sort = this@VLRoomSelSongModel.sort
            userNo = this@VLRoomSelSongModel.userNo
            userId = this@VLRoomSelSongModel.userId
            name = this@VLRoomSelSongModel.name
        }
    }
}

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

data class KTVSwitchSongInputModel(
    val songNo: String
)

data class KTVChooseSongInputModel(
    val isChorus: Int,
    val songName: String,
    val songNo: String,
    val songUrl: String,
    val singer: String,
    val imageUrl: String,
    val lyric: String,
)

data class KTVMakeSongTopInputModel(
    val songNo: String,
    val sort: Int,
)