package io.agora.scene.cantata.service

import io.agora.scene.cantata.CantataLogger
import io.agora.scene.base.component.AgoraApplication

/*
 * Service Module
 * Introduction: This module is responsible for the interaction between the frontend business module and the business server 
 * (including room list + room business data synchronization, etc.)
 * Implementation principle: The business server of this scene is a backend service wrapped with rethinkDB for data storage. 
 * It can be considered as a DB that can be freely written on the app side. Room list data and room business data are constructed 
 * on the app and stored in this DB.
 * When data in DB is added/deleted/modified, all clients will be notified to achieve business data synchronization
 * TODO Warning: The backend service of this scene is for demonstration only and cannot be used commercially. 
 * If you need to go live, you must deploy your own backend service or cloud storage server 
 * (such as leancloud, easemob, etc.) and re-implement this module!!!!!!!!!
 */
interface CantataServiceProtocol {

    enum class KTVSubscribe {
        KTVSubscribeCreated,      //Created
        KTVSubscribeDeleted,      //Deleted
        KTVSubscribeUpdated,      //Updated
    }

    companion object {
        private val instance by lazy {
            // KTVServiceImp()
            CantataSyncManagerServiceImp(AgoraApplication.the()) { error ->
                error?.message?.let { CantataLogger.e("SyncManager", it) }
            }
        }

        fun getImplInstance(): CantataServiceProtocol = instance
    }

    fun reset()

    // ============== Room related ==============

    /**
     * Get room list
     */
    fun getRoomList(completion: (error: Exception?, list: List<RoomListModel>?) -> Unit)

    /**
     * Create room
     */
    fun createRoom(
        inputModel: CreateRoomInputModel,
        completion: (error: Exception?, out: CreateRoomOutputModel?) -> Unit
    )

    /**
     * Join room
     */
    fun joinRoom(
        inputModel: JoinRoomInputModel,
        completion: (error: Exception?, out: JoinRoomOutputModel?) -> Unit
    )

    /**
     * Leave room
     */
    fun leaveRoom(completion: (error: Exception?) -> Unit)

    /**
     * Change MV cover
     */
    fun changeMVCover(inputModel: ChangeMVCoverInputModel, completion: (error: Exception?) -> Unit)

    /**
     * Subscribe to room status changes
     */
    fun subscribeRoomStatusChanged(changedBlock: (KTVSubscribe, RoomListModel?) -> Unit)

    /**
     * user count did changed
     */
    fun subscribeUserListCount(changedBlock: (count: Int) -> Unit)

    fun subscribeRoomTimeUp(onRoomTimeUp: () -> Unit)


    // ===================== Seat related =========================

    /**
     * Get seat status list
     */
    fun getSeatStatusList(completion: (error: Exception?, list: List<RoomSeatModel>?) -> Unit)

    /**
     * Take seat
     */
    fun onSeat(inputModel: OnSeatInputModel, completion: (error: Exception?) -> Unit)

    /**
     * Leave seat
     */
    fun leaveSeat(inputModel: OutSeatInputModel, completion: (error: Exception?) -> Unit)

    /**
     * Leave seat but only delete current song
     */
    fun leaveSeatWithoutRemoveSong(inputModel: OutSeatInputModel,completion: (error: Exception?) -> Unit)

    /**
     * Set seat audio mute status
     */
    fun updateSeatAudioMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit)

    /**
     * Turn on seat camera
     */
    fun updateSeatVideoMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit)

    /**
     * Update seat score
     */
    fun updateSeatScoreStatus(score:Int,completion: (error: Exception?) -> Unit)

    /**
     * Subscribe to seat changes
     */
    fun subscribeSeatListChanged(changedBlock: (KTVSubscribe, RoomSeatModel?) -> Unit)

    // =================== Song related =========================

    /**
     * Get selected song list
     */
    fun getChoosedSongsList(
        completion: (error: Exception?, list: List<RoomSelSongModel>?) -> Unit
    )

    /**
     * Remove song
     */
    fun removeSong(
        isSingingSong: Boolean, inputModel: RemoveSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * Mark song as finished -> Show settlement page
     */
    fun markSongEnded(inputModel: RoomSelSongModel, completion: (error: Exception?) -> Unit)

    /**
     * Choose song
     */
    fun chooseSong(
        inputModel: ChooseSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * Move song to top
     */
    fun makeSongTop(
        inputModel: MakeSongTopInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * Mark song as playing
     */
    fun makeSongDidPlay(inputModel: RoomSelSongModel, completion: (error: Exception?) -> Unit)

    /**
     * Join chorus
     */
    fun joinChorus(inputModel: RoomSelSongModel, completion: (error: Exception?) -> Unit)

    /**
     * Chorus member leaves chorus
     */
    fun leaveChorus(completion: (error: Exception?) -> Unit)

    /**
     * Subscribe to song changes
     */
    fun subscribeChooseSongChanged(changedBlock: (KTVSubscribe, RoomSelSongModel?) -> Unit)

    // =================== Reconnection related =========================

    /**
     * Subscribe to reconnection events
     */
    fun subscribeReConnectEvent(onReconnect: () -> Unit)

    /**
     * Get room user list
     */
    fun getAllUserList(success: (userNum : Int) -> Unit, error: ((Exception) -> Unit)? = null)
}