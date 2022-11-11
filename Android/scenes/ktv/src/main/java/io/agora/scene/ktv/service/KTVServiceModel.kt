package io.agora.scene.ktv.service

import io.agora.scene.base.R
import io.agora.scene.base.bean.MemberMusicModel

data class VLRoomListModel(
    val name: String,
    val isPrivate: Boolean,
    var password: String?,
    val creator: String,
    val roomNo: String,
    val isChorus: Int,
    val bgOption: String,
    val soundEffect: String,
    val belCanto: String,
    val createdAt: String?,
    val updatedAt: String?,
    val status: Int,
    val deletedAt: String?,
    val roomPeopleNum: Int,
    val icon: String,
    // 新加字段 当前房间的创建者
    val creatorNo: String,

    // SyncManager独有，用来更新和删除数据
    var objectId: String? = null
) {
    fun getCoverRes(): Int {
        if ("1" == icon) {
            return R.mipmap.icon_room_cover1
        } else if ("2" == icon) {
            return R.mipmap.icon_room_cover2
        } else if ("3" == icon) {
            return R.mipmap.icon_room_cover3
        } else if ("4" == icon) {
            return R.mipmap.icon_room_cover4
        } else if ("5" == icon) {
            return R.mipmap.icon_room_cover5
        } else if ("6" == icon) {
            return R.mipmap.icon_room_cover6
        } else if ("7" == icon) {
            return R.mipmap.icon_room_cover7
        } else if ("8" == icon) {
            return R.mipmap.icon_room_cover8
        } else if ("9" == icon) {
            return R.mipmap.icon_room_cover9
        }
        return R.mipmap.icon_room_cover1
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

    // SyncManager独有，用来更新和删除数据
    var objectId: String? = null
) : java.io.Serializable


data class KTVCreateRoomInputModel(
    val icon: String,
    val isPrivate: Int,
    val name: String,
    val password: String,
    val userNo: String,

    // the params below may can be deleted?
    val belCanto: String = "",
    val soundEffect: String = "",
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
    var objectId: String? = null
)

data class VLRoomSelSongModel(
    // 获取歌词列表返回的歌词信息
    val songName: String,// 歌曲名
    val songNo: String, // 歌词唯一标识
    val songUrl: String, // mp3路径
    val singer: String, // 演唱者
    val lyric: String, // 歌词
    val status: Int,// 0 未开始 1.已唱 2.正在唱
    val imageUrl: String,// 歌曲封面

    // 获取已点歌记返回的歌词信息，同时也包含上面信息
    val userNo: String? = null,// 点歌人No
    val userId: String? = null,// 点歌人id
    val name: String? = null,// 点歌人昵称
    val chorusNo: String? = null, // 合唱者userNo
    val isChorus: Boolean = false, // 是否合唱
    val isOriginal: Int = 0, //是否原唱
    val sort: Int = 0,// 已点歌曲的播放顺序排序

    // 自定义数据
    val score: Double = 0.0,// 唱歌得分
    val isOwnSong: Boolean = false,// 是否是自己点的歌曲
    var objectId: String? = null // SyncManager数据唯一标识
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
            status = this@VLRoomSelSongModel.status
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
    val lyricType: Int,
    val songNo: String
)

data class KTVSongDetailOutputModel(
    val songNo: String,
    val lyric: String,
    val songUrl: String
)

data class KTVSwitchSongInputModel(
    val userNo: String,
    val songNo: String,
    val roomNo: String
)

data class KTVChooseSongInputModel(
    val isChorus: Int,
    val songName: String,
    val songNo: String,
    val songUrl: String,
    val singer: String,
    val imageUrl: String,
)

data class KTVMakeSongTopInputModel(
    val songNo: String,
    val sort: Int,
    var objectId: String?
)