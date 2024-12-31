package io.agora.scene.cantata.service

import java.io.Serializable

/*
 * Service Module
 * Introduction: This module is responsible for interaction between frontend business modules and business servers 
 * (including room list + room business data synchronization, etc.)
 * Implementation principle: The business server of this scene is wrapped with a rethinkDB backend service for data storage,
 * which can be considered as a DB that can be freely written by the app side. Room list data and room business data 
 * are constructed on the app and stored in this DB.
 * When data in DB is added/deleted/modified, each end will be notified to achieve business data synchronization
 * TODO Warning: The backend service of this scene is only for demo purposes and cannot be commercialized. 
 * If you need to go online, you must deploy your own backend service or cloud storage server 
 * (such as leancloud, easemob, etc.) and re-implement this module!!!!!!!!!
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
     * Background Image
     */
    val bgOption: String = "",
    /**
     * Number of People in the Room
     */
    val roomPeopleNum: Int = 0,

    val streamMode: Int = 0
)

data class RoomSeatModel constructor(
    val isMaster: Boolean,// Whether the user is the room owner
    val headUrl: String,// Avatar
    val userNo: String,// User no on the seat
    val rtcUid: String,// User id on the seat, consistent with rtc's userId
    val name: String,// User nickname on the seat
    val seatIndex: Int,// Seat number
    var chorusSongCode: String = "",// Whether to sing together
    var isAudioMuted: Int,// Whether to mute audio
    var isVideoMuted: Int,// Whether to enable video
    var score:Int,
    val isOwner:Boolean = false

) : Serializable {

    companion object {
        val MUTED_VALUE_TRUE = 1

        val MUTED_VALUE_FALSE = 0
    }
}

data class UserModel constructor(
    val name: String, // User nickname on the seat
    val headUrl: String, // Avatar
    var score:Int // Score
)


data class CreateRoomInputModel constructor(
    val icon: String,
    val isPrivate: Int,
    val name: String,
    val password: String,
    val userNo: String,
    val delayType: Int
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
    val creatorAvatar: String, // Creator avatar
    val bgOption: String,
    val seatsArray: List<RoomSeatModel>?,
    /**
     * Number of People in the Room
     */
    val roomPeopleNum: Int,

    val agoraRTMToken: String,
    val agoraRTCToken: String,
    val agoraChorusToken: String,
    val agoraMusicToken: String,
    val createdAt: String,
    val steamMode: Int
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
    // Get lyrics information returned by lyrics list
    val songName: String,// Song name
    val songNo: String, // Lyrics unique identifier
    val singer: String, // Singer
    val imageUrl: String,// Song cover

    // Get lyrics information returned by song list, which also contains the above information
    val userNo: String? = null,// Song requestor No
    val name: String? = null,// Song requestor nickname
    val isOriginal: Int = 0, //Whether it's original

    // Sorting field
    val status: Int, // 0 Not started 1.Sung 2.Singing
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
