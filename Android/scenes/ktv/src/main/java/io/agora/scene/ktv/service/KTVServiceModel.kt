package io.agora.scene.ktv.service

import io.agora.scene.base.R

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
){
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
)


data class KTVCreateRoomInputModel(
    val belCanto: String,
    val icon: String,
    val isPrivate: Int,
    val name: String,
    val password: String,
    val soundEffect: String,
    val userNo: String,
)

data class KTVCreateRoomOutputModel(
    val name: String,
    val roomNo: String,
    val seatsArray: List<VLRoomSeatModel>?,
    val agoraRTMToken: String,
    val agoraRTCToken: String,
    val agoraPlayerRTCToken: String
)

data class KTVJoinRoomInputModel(
    val roomNo: String,
    val password: String?,
)

data class KTVJoinRoomOutputModel(
    val creator: String,
    val seatsArray: List<VLRoomSeatModel>?,
    val agoraRTMToken: String,
    val agoraRTCToken: String,
    val agoraPlayerRTCToken: String
)


data class KTVChangeMVCoverInputModel(
    val mvIndex: Int
)

data class KTVOnSeatInputModel(
    val seatIndex: Int
)

data class KTVRemoveSongInputModel(
    val songNo: String,
    val sort: String,
    var objectId: String?
)

data class VLRoomSelSongModel(
    // 合唱者userNo
    val chorusNo: String,
    val imageUrl: String,
    // 是否合唱
    val isChorus: Boolean,
    //是否原唱
    val isOriginal: String,
    val singer: String,
    val songName: String,
    val songNo: String,
    val songUrl: String,
    // 歌词
    val lyric: String,
    // 排序
    val sort: String,
    // 0 未开始 1.已唱 2.正在唱
    val status: Int,
    // 是谁点的歌
    val userNo: String,
    val userId: String,
    // 点歌人昵称
    val name : String,
    // 得分
    val score: Double,
    // 是否是自己点的歌曲
    val isOwnSong: Boolean,

    var objectId: String?
)

data class KTVJoinChorusInputModel(
    val isChorus: String,
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

data class KTVChooseSongInputModel(
    val isChorus: Boolean,
    val songName: String,
    val songNo: String,
    val songUrl: String,
    val singer: String,
    val imageUrl: String,
)

data class KTVMakeSongTopInputModel(
    val songNo: String,
    val sort: String,
    var objectId: String?
)