package io.agora.scene.ktv.singbattle.service

/*
 * Service Module
 * Introduction: This module is responsible for the interaction between the frontend business module and the business server 
 * (including room list + room business data synchronization, etc.)
 * Implementation principle: The business server of this scenario is a backend service wrapped with rethinkDB for data storage. 
 * It can be considered as a DB that can be freely written by the app side. Room list data and room business data are constructed 
 * on the app and stored in this DB.
 * When data in the DB is added, deleted, or modified, each end will be notified to achieve business data synchronization.
 * TODO Note⚠️: The backend service of this scenario is only for demonstration purposes and cannot be used commercially. 
 * If you need to go online, you must deploy your own backend service or cloud storage server (such as leancloud, easemob, etc.) 
 * and re-implement this module!!!!!!!!!
 */

data class RoomListModel(
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
     * Background image
     */
    val bgOption: String = "",
    /**
     * Number of people in the room
     */
    val roomPeopleNum: Int = 0,
) {}

data class RoomSeatModel(
    val isMaster: Boolean,// Whether is room owner
    val headUrl: String,// Avatar
    val userNo: String,// User number on the seat
    val rtcUid: String,// User ID on the seat, consistent with rtc userId
    val name: String,// Nickname of user on the seat
    val seatIndex: Int,// Seat number
    val chorusSongCode: String = "",// Whether in chorus
    val isAudioMuted: Int,// Whether audio is muted
    val isVideoMuted: Int,// Whether video is enabled
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
    val creatorAvatar: String,
    val bgOption: String,
    val seatsArray: List<RoomSeatModel>?,
    /**
     * Number of people in the room
     */
    val roomPeopleNum: Int,

    val agoraRTMToken: String,
    val agoraRTCToken: String,
    val agoraChorusToken: String,
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
    // Lyrics information returned from getting lyrics list
    val songName: String,// Song name
    val songNo: String, // Lyrics unique identifier
    val singer: String, // Singer
    val imageUrl: String,// Song cover

    // Song information returned from getting selected song list, also includes above information
    val userNo: String? = null,// Song selector's No
    val name: String? = null,// Song selector's nickname
    val isOriginal: Int = 0, // Whether original singer version
    val winnerNo: String = "",// Grab-to-sing winner's No

    // Sorting fields
    val status : Int, // 0 Not started 1.Finished 2.Playing
    val createAt: Long,
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

data class ScoringAverageModel(
    val isLocal: Boolean,
    val score: Int
)

data class RankModel(
    val userName: String,
    val songNum: Int = 0,
    val score: Int = 0,
    val poster: String
)

enum class SingBattleGameStatus(val value: Int) {
    idle(0),
    waitting(1),// Waiting
    started(2),// Started
    ended(3)// Ended
}

data class SingBattleGameModel(
    val status: Int,
    val rank: Map<String, RankModel>?
)
