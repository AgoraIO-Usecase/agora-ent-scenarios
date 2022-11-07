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
): java.io.Serializable


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
): java.io.Serializable


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
    val sort: String,
    var objectId: String?
)

data class VLRoomSelSongModel(
    // 合唱者userNo
    var chorusNo: String? = null,
    var imageUrl: String? = null,
    // 是否合唱
    var isChorus: Boolean? = null,
    //是否原唱
    var isOriginal: Int? = null,
    var singer: String? = null,
    var songName: String? = null,
    var songNo: String? = null,
    var songUrl: String? = null,
    // 歌词
    var lyric: String? = null,
    // 排序
    var sort: Int? = null,
    // 0 未开始 1.已唱 2.正在唱
    var status: Int? = null,
    // 是谁点的歌
    var userNo: String? = null,
    var userId: String? = null,
    // 点歌人昵称
    var name : String? = null,
    // 得分
    var score: Double? = null,
    // 是否是自己点的歌曲
    var isOwnSong: Boolean? = null,

    var objectId: String? = null
) {
    // TODO remove it
    fun toMemberMusicModel(): MemberMusicModel{
        return MemberMusicModel().apply {
            imageUrl = this@VLRoomSelSongModel.imageUrl
            //isChorus = this@VLRoomSelSongModel.isChorus
            //isOriginal = this@VLRoomSelSongModel.isOriginal
            singer = this@VLRoomSelSongModel.singer
            songName = this@VLRoomSelSongModel.songName
            songNo = this@VLRoomSelSongModel.songNo
            songUrl = this@VLRoomSelSongModel.songUrl
            //lrc = this@VLRoomSelSongModel.lyric
            //sort = this@VLRoomSelSongModel.sort
            //status = this@VLRoomSelSongModel.status
            userNo = this@VLRoomSelSongModel.userNo
            user1Id = this@VLRoomSelSongModel.userId
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
    val userNo:String,
    val songNo: String,
    val roomNo: String
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