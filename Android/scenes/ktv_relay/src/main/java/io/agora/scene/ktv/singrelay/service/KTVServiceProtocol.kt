package io.agora.scene.ktv.singrelay.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.ktv.singrelay.KTVLogger

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
interface KTVServiceProtocol {

    enum class KTVSubscribe {
        KTVSubscribeCreated,      // Created
        KTVSubscribeDeleted,      // Deleted
        KTVSubscribeUpdated,      // Updated
    }

    companion object {

        private var instance : KTVServiceProtocol? = null
            get() {
                if (field == null) {
                    field = KTVSyncManagerServiceImp(AgoraApplication.the()){ error ->
                        error?.message?.let { KTVLogger.e("SyncManager", it) }
                    }
                }
                return field
            }

        @Synchronized
        fun getImplInstance(): KTVServiceProtocol = instance!!

        @Synchronized
        fun destroy() {
            (instance as? KTVSyncManagerServiceImp)?.reset()
            instance = null
        }
    }

    fun reset()

    // ============== Room Related ==============

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
    fun leaveRoom(
        completion: (error: Exception?) -> Unit
    )

    /**
     * Change MV cover
     */
    fun changeMVCover(
        inputModel: ChangeMVCoverInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * room status did changed
     */
    fun subscribeRoomStatus(
        changedBlock: (KTVSubscribe, RoomListModel?) -> Unit
    )

    /**
     * user count did changed
     */
    fun subscribeUserListCount(
        changedBlock: (count: Int) -> Unit
    )

    fun subscribeRoomTimeUp(
        onRoomTimeUp: () -> Unit
    )


    // ===================== Mic Position Related =================================

    /**
     * Get mic position list
     */
    fun getSeatStatusList(
        completion: (error: Exception?, list: List<RoomSeatModel>?) -> Unit
    )

    /**
     * Take the mic
     */
    fun onSeat(
        inputModel: OnSeatInputModel,
        completion: (error: Exception?) -> Unit
    )

    fun autoOnSeat(
        completion: (error: Exception?) -> Unit
    )

    /**
     * Leave the mic
     */
    fun outSeat(
        inputModel: OutSeatInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * Mute mic position
     */
    fun updateSeatAudioMuteStatus(
        mute: Boolean, completion: (error: Exception?) -> Unit
    )

    /**
     * Turn on mic position camera
     */
    fun updateSeatVideoMuteStatus(
        mute: Boolean, completion: (error: Exception?) -> Unit
    )

    /**
     * seat list did changed
     */
    fun subscribeSeatList(
        changedBlock: (KTVServiceProtocol.KTVSubscribe, RoomSeatModel?) -> Unit
    )

    // =================== Song Related =========================

    /**
     * Get selected song list
     */
    fun getChoosedSongsList(
        completion: (error: Exception?, list: List<RoomSelSongModel>?) -> Unit
    )

    /**
     * Delete song
     */
    fun removeSong(
        isSingingSong: Boolean, inputModel: RemoveSongInputModel, completion: (error: Exception?) -> Unit
    )

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
    fun joinChorus(
        inputModel: RoomSelSongModel,
        completion: (error: Exception?) -> Unit
    )

    /**
     * Chorus member leaves chorus
     */
    fun leaveChorus(
        completion: (error: Exception?) -> Unit
    )

    /**
     * song did changed
     */
    fun subscribeChooseSong(
        changedBlock: (KTVSubscribe, RoomSelSongModel?) -> Unit
    )

    // =================== Sing Relay Game Related =========================
    fun prepareSingRelayGame(completion: (error: Exception?) -> Unit)

    fun startSingRelayGame(
        completion: (error: Exception?) -> Unit
    )

    fun finishSingRelayGame(
        rank: Map<String, RankModel>,
        completion: (error: Exception?) -> Unit
    )

    fun getSingRelayGameInfo(
        completion: (error: Exception?, info: SingRelayGameModel?) -> Unit
    )

    fun updateSongModel(
        songCode: String,
        winner: String,
        winnerName: String,
        headUrl: String,
        completion: (error: Exception?) -> Unit
    )

    fun subscribeSingRelayGame(
        changedBlock: (KTVSubscribe, SingRelayGameModel?) -> Unit
    )

    // =================== Network Reconnection Related =========================

    /**
     * Subscribe to reconnection events
     */
    fun subscribeReConnectEvent(onReconnect: () -> Unit)

    /**
     * Get user list in room
     */
    fun getAllUserList(success: (userNum : Int) -> Unit, error: ((Exception) -> Unit)? = null)
}