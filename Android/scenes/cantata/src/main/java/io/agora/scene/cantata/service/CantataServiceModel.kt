package io.agora.scene.cantata.service

import java.io.Serializable

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */

data class RoomListModel constructor(
    val roomNo: String = "",
    val name: String = "",
    val icon: String = "",
    val isPrivate: Boolean = false,
    val password: String = "",
    val creatorNo: String = "",
    val creatorName: String = "",
    val creatorAvatar: String = "",
    val createdAt: String = System.currentTimeMillis().toString(),

    /**
     * 背景图
     */
    val bgOption: String = "",
    /**
     * 房间内人数
     */
    val roomPeopleNum: Int = 0,
)

data class RoomSeatModel constructor(
    val isMaster: Boolean,// 是否是房主
    val headUrl: String,// 头像
    val userNo: String,// 座位上用户no
    val rtcUid: String,// 座位上用户id，与rtc的userId一致
    val name: String,// 座位上用户昵称
    val seatIndex: Int,// 座位编号
    var chorusSongCode: String = "",// 是否合唱
    var isAudioMuted: Int,// 是否静音
    var isVideoMuted: Int,// 是否开启视频
    var score:Int,
    val isOwner:Boolean = false

) : Serializable {

    companion object {
        val MUTED_VALUE_TRUE = 1

        val MUTED_VALUE_FALSE = 0
    }
}

data class UserModel constructor(
    val name: String, // 座位上用户昵称
    val headUrl: String, // 头像
    var score:Int // 得分
)


data class CreateRoomInputModel constructor(
    val icon: String,
    val isPrivate: Int,
    val name: String,
    val password: String,
    val userNo: String,
)

data class CreateRoomOutputModel constructor(
    val roomNo: String,
    val password: String?,
)


data class JoinRoomInputModel constructor(
    val roomNo: String,
    val password: String?,
)

data class JoinRoomOutputModel constructor(
    val roomName: String,
    val roomNo: String,
    val creatorNo: String,
    val creatorAvatar: String, // 创建人头像
    val bgOption: String,
    val seatsArray: List<RoomSeatModel>?,
    /**
     * 房间内人数
     */
    val roomPeopleNum: Int,

    val agoraRTMToken: String,
    val agoraRTCToken: String,
    val agoraChorusToken: String,
    val agoraMusicToken: String,
    val createdAt: String
) : Serializable


data class ChangeMVCoverInputModel constructor(
    val mvIndex: Int
)

data class OnSeatInputModel constructor(
    val score: Int
)

data class OutSeatInputModel constructor(
    val userNo: String,
    val userId: String,
    val userName: String,
    val userHeadUrl: String,
    val userOnSeat: Int,
)

data class RemoveSongInputModel constructor(
    val songNo: String,
)

data class RoomSelSongModel constructor(
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
    val status: Int, // 0 未开始 1.已唱 2.正在唱
    val createAt: Long,
    val pinAt: Double,
    val musicEnded: Boolean = false
) {
    companion object {
        val STATUS_IDLE = 0
        val STATUS_PLAYED = 1
        val STATUS_PLAYING = 2
    }
}

data class JoinChorusInputModel constructor(
    val songNo: String
)

data class ChooseSongInputModel constructor(
    val songName: String,
    val songNo: String,
    val singer: String,
    val imageUrl: String,
)

data class MakeSongTopInputModel constructor(
    val songNo: String
)

data class UpdateSingingScoreInputModel constructor(
    val score: Double
)

data class ScoringAlgoControlModel constructor(
    val level: Int,
    val offset: Int
)

data class ScoringAverageModel constructor(
    val isLocal: Boolean,
    val score: Int
)
