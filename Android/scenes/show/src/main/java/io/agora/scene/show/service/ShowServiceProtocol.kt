package io.agora.scene.show.service

import io.agora.scene.base.component.AgoraApplication

/*
 * Service module
 * Introduction: This module is responsible for interaction between frontend business modules and business server 
 * (including room list + room business data synchronization, etc.)
 * Implementation principle: The business server of this scene is wrapped with a rethinkDB backend service for data storage.
 * It can be considered as a DB that can be freely written by the app side. Room list data and room business data 
 * are constructed on the app and stored in this DB.
 * When data in DB is added/deleted/modified, all clients will be notified to achieve business data synchronization
 * TODO Note⚠️: The backend service of this scene is for demo only and cannot be used commercially. 
 * If you need to go live, you must deploy your own backend service or cloud storage server 
 * (such as leancloud, easemob, etc.) and reimplement this module!!!!!!!!!
 */
interface ShowServiceProtocol {

    companion object {
        // Room survival time in milliseconds
        var ROOM_AVAILABLE_DURATION: Long = 1200 * 1000
        // PK session duration in milliseconds
        var PK_AVAILABLE_DURATION: Long = 120 * 1000

        private var instance : ShowServiceProtocol? = null
            get() {
                if (field == null) {
                    field = ShowServiceImpl(AgoraApplication.the())
                }
                return field
            }

        @Synchronized
        fun get(): ShowServiceProtocol = instance!!

        @Synchronized
        fun destroy() {
            (instance as? ShowServiceImpl)?.destroy()
            instance = null
        }
    }

    // Get room list
    fun getRoomList(
        success: (List<ShowRoomDetailModel>) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // Create room
    fun createRoom(
        roomId: String,
        roomName: String,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // Join room
    fun joinRoom(
        roomId: String,
        success: () -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // Leave room
    fun leaveRoom(roomId: String)

    // Subscribe to update/delete events of current joined room
    fun subscribeCurrRoomEvent(roomId: String, onUpdate: (status: ShowSubscribeStatus, roomInfo: ShowRoomDetailModel?) -> Unit)

    // Get all users in current room
    fun getAllUserList(roomId: String, success: (List<ShowUser>) -> Unit, error: ((Exception) -> Unit)? = null)

    // Monitor user changes
    fun subscribeUser(roomId: String, onUserChange: (ShowSubscribeStatus, ShowUser?) -> Unit)

    // Send chat message
    fun sendChatMessage(
        roomId: String,
        message: String,
        success: ((ShowMessage) -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Subscribe to chat message changes
    fun subscribeMessage(
        roomId: String,
        onMessageChange: (ShowSubscribeStatus, ShowMessage) -> Unit
    )

    // Get linking application list
    fun getAllMicSeatApplyList(
        roomId: String,
        success: (List<ShowMicSeatApply>) -> Unit,
        error: ((Exception) -> Unit)? = null
    )

    // Host subscribes to linking application changes
    fun subscribeMicSeatApply(roomId: String, onMicSeatChange: (ShowSubscribeStatus, List<ShowMicSeatApply>) -> Unit)

    // Audience applies for linking
    fun createMicSeatApply(roomId: String, success: ((ShowMicSeatApply) -> Unit)? = null, error: ((Exception) -> Unit)? = null)

    // Audience cancels linking application
    fun cancelMicSeatApply(roomId: String, success: (() -> Unit)? = null, error: ((Exception) -> Unit)? = null)

    // Host accepts linking application
    fun acceptMicSeatApply(
        roomId: String,
        userId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Audience subscribes to linking invitation
    fun subscribeMicSeatInvitation(
        roomId: String,
        onMicSeatInvitationChange: (ShowSubscribeStatus, ShowMicSeatInvitation?) -> Unit
    )

    // Host creates linking invitation
    fun createMicSeatInvitation(
        roomId: String,
        userId: String,
        success: ((ShowMicSeatInvitation) -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Audience accepts linking
    fun acceptMicSeatInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Audience rejects linking
    fun rejectMicSeatInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Get available PK target list
    fun getAllPKUserList(
        roomId: String,
        success: ((List<ShowPKUser>) -> Unit),
        error: ((Exception) -> Unit)? = null
    )

    // Audience subscribes to PK invitation
    fun subscribePKInvitationChanged(roomId: String, onPKInvitationChanged: (ShowSubscribeStatus, ShowPKInvitation?) -> Unit)

    // Create PK invitation
    fun createPKInvitation(
        roomId: String,
        pkRoomId: String,
        success: ((ShowPKInvitation) -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Accept PK
    fun acceptPKInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Reject PK
    fun rejectPKInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Get current interaction information
    fun getInteractionInfo(
        roomId: String,
        success: ((ShowInteractionInfo?) -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Subscribe to interaction invitation
    fun subscribeInteractionChanged(roomId: String, onInteractionChanged: (ShowSubscribeStatus, ShowInteractionInfo?) -> Unit)

    // Stop interaction
    fun stopInteraction(
        roomId: String,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Mute settings
    fun muteAudio(
        roomId: String,
        mute: Boolean,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    )

    // Subscribe to reconnection events
    fun subscribeReConnectEvent(roomId: String, onReconnect: () -> Unit)

    // Start robot
    fun startCloudPlayer()
}