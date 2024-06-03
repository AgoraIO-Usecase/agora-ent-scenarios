package io.agora.scene.ktv.service

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.ktv.KTVLogger

/**
 * Ktv service listener protocol
 *
 * @constructor Create empty Ktv service listener protocol
 */
interface KtvServiceListenerProtocol {

    /**
     * On room expire
     *
     */
    fun onRoomExpire() {}

    /**
     * On room destroy
     *
     */
    fun onRoomDestroy() {}

    /**
     * On user count update
     *
     * @param userCount
     */
    fun onUserCountUpdate(userCount: Int) {}

    /**
     * On mic seat snapshot
     *
     * @param seatMap
     */
    fun onMicSeatSnapshot(seatMap: Map<Int, RoomMicSeatInfo>) {}

    /**
     * On user seat update
     *
     * @param seatInfo
     */
    fun onUserSeatUpdate(seatInfo: RoomMicSeatInfo) {}

    /**
     * On user enter seat
     *
     * @param seatIndex
     * @param user
     */
    fun onUserEnterSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {}

    /**
     * On user leave seat
     *
     * @param seatIndex
     * @param seatInfo
     */
    fun onUserLeaveSeat(seatIndex: Int, user: AUIUserThumbnailInfo) {}

    /**
     * On seat audio mute
     *
     * @param seatIndex
     * @param isMute
     */
    fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {}

    /**
     * On seat video mute
     *
     * @param seatIndex
     * @param isMute
     */
    fun onSeatVideoMute(seatIndex: Int, isMute: Boolean) {}

    /**
     * On update all choose songs
     *
     * @param chosenSongList
     */
    fun onChosenSongListDidChanged(chosenSongList: List<ChosenSongInfo>) {}

    /**
     * On chorister did enter
     *
     * @param chorister
     */
    fun onChoristerDidEnter(chorister: RoomChoristerInfo) {}

    /**
     * On chorister did leave
     *
     * @param chorister
     */
    fun onChoristerDidLeave(chorister: RoomChoristerInfo) {}
}

/**
 * Ktv service protocol
 *
 * @constructor Create empty Ktv service protocol
 */
interface KTVServiceProtocol {

    companion object {
        private val instance by lazy {
            // KTVServiceImp()
            KTVSyncManagerServiceImp(AgoraApplication.the()) { error ->
                error?.message?.let { KTVLogger.e("SyncManager", it) }
            }
        }

        fun getImplInstance(): KTVServiceProtocol = instance
    }

    /**
     * Get room list
     *
     * @param completion
     * @receiver
     */
    fun getRoomList(completion: (error: Exception?, roomList: List<AUIRoomInfo>?) -> Unit)

    /**
     * Create room
     *
     * @param createRoomInfo
     * @param completion
     * @receiver
     */
    fun createRoom(createRoomInfo: CreateRoomInfo, completion: (error: Exception?, roomInfo: AUIRoomInfo?) -> Unit)

    /**
     * Join room
     *
     * @param inputModel
     * @param completion
     * @receiver
     */
    fun joinRoom(roomId: String, password: String?, completion: (error: Exception?) -> Unit)

    /**
     * Leave room
     *
     * @param completion
     * @receiver
     */
    fun leaveRoom(completion: (error: Exception?) -> Unit)

    /**
     * On seat
     *
     * @param seatIndex null autoOnseat
     * @param completion
     * @receiver
     */
    fun enterSeat(seatIndex: Int?, completion: (error: Exception?) -> Unit)

    /**
     * Out seat
     *
     * @param completion
     * @receiver
     */
    fun leaveSeat(completion: (error: Exception?) -> Unit)

    /**
     * Kick seat
     *
     * @param seatIndex
     * @param completion
     * @receiver
     */
    fun kickSeat(seatIndex: Int, completion: (error: Exception?) -> Unit)

    /**
     * Mute user audio
     *
     * @param mute
     * @param completion
     * @receiver
     */
    fun updateSeatAudioMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit)

    /**
     * Mute user video
     *
     * @param mute
     * @param completion
     * @receiver
     */
    fun updateSeatVideoMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit)

    /**
     * Get chosen songs list
     *
     * @param completion`
     * @receiver
     */
    fun getChosenSongList(completion: (error: Exception?, list: List<ChosenSongInfo>?) -> Unit)

    /**
     * Remove song
     *
     * @param songInfo
     * @param completion
     * @receiver
     */
    fun removeSong(songCode: String, completion: (error: Exception?) -> Unit)

    /**
     * Choose song
     *
     * @param inputModel
     * @param completion
     * @receiver
     */
    fun chooseSong(inputModel: ChooseSongInputModel, completion: (error: Exception?) -> Unit)

    /**
     * Make song top
     *
     * @param songCode
     * @param completion
     * @receiver
     */
    fun pinSong(songCode: String, completion: (error: Exception?) -> Unit)

    /**
     * Make song did play
     *
     * @param songCode
     * @param completion
     * @receiver
     */
    fun makeSongDidPlay(songCode: String, completion: (error: Exception?) -> Unit)

    /**
     * Join chorus
     *
     * @param songCode
     * @param completion
     * @receiver
     */
    fun joinChorus(songCode: String, completion: (error: Exception?) -> Unit)

    /**
     * Leave chorus
     *
     * @param songCode
     * @param completion
     * @receiver
     */
    fun leaveChorus(songCode: String, completion: (error: Exception?) -> Unit)

    /**
     * Get current duration
     *
     * @param channelName
     * @return
     */
    fun getCurrentDuration(channelName: String): Long

    /**
     * Get current ts
     *
     * @param channelName
     * @return
     */
    fun getCurrentTs(channelName: String): Long

    /**
     * Subscribe listener
     *
     * @param listener
     */
    fun subscribeListener(listener: KtvServiceListenerProtocol)

    /**
     * Unsubscribe listener
     *
     * @param listener
     */
    fun unsubscribeListener(listener: KtvServiceListenerProtocol)
}