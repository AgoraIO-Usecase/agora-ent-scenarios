package io.agora.scene.voice.spatial.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.model.*

/**
 * @author create by zhangwei03
 *
 * Voice chat room protocol definition
 */
interface VoiceServiceProtocol {

    companion object {

        const val ERR_OK = 0
        const val ERR_FAILED = 1
        const val ERR_LOGIN_ERROR = 2
        const val ERR_LOGIN_SUCCESS = 3
        const val ERR_ROOM_UNAVAILABLE = 4
        const val ERR_ROOM_NAME_INCORRECT = 5
        const val ERR_ROOM_LIST_EMPTY = 1003
        private val instance by lazy {
            // VoiceChatServiceImp()
            VoiceSyncManagerServiceImp(AgoraApplication.the()) { error ->
                VoiceSpatialLogger.e("VoiceServiceProtocol ", "voice chat protocol error：${error?.message}")
            }
        }

        @JvmStatic
        fun getImplInstance(): VoiceServiceProtocol = instance
    }

    /**
     * Register subscription
     * @param delegate Chat room IM callback handler
     */
    fun subscribeEvent(delegate: VoiceRoomSubscribeDelegate)

    /**
     * Unsubscribe
     */
    fun unsubscribeEvent()

    fun reset()

    fun getSubscribeDelegates(): MutableList<VoiceRoomSubscribeDelegate>

    /**
     * Get room list
     * @param page Page index, starts from 0 (temporarily invalid due to SyncManager limitations)
     */
    fun fetchRoomList(
        page: Int = 0,
        completion: (error: Int, result: List<VoiceRoomModel>) -> Unit
    )

    /**
     * Create room
     * @param inputModel Room information input
     */
    fun createRoom(inputModel: VoiceCreateRoomModel, completion: (error: Int, result: VoiceRoomModel) -> Unit)

    /**
     * Join room
     * @param roomId Room ID
     */
    fun joinRoom(roomId: String, completion: (error: Int, result: VoiceRoomModel?) -> Unit)

    /**
     * Leave room
     * @param roomId Room ID
     */
    fun leaveRoom(roomId: String, isRoomOwnerLeave: Boolean, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Get room details
     * @param voiceRoomModel Room summary
     */
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel, completion: (error: Int, result: VoiceRoomInfo?) -> Unit)

    /**
     * Get user list
     */
    fun fetchRoomMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit)

    /**
     * Get applicant list
     */
    fun fetchApplicantsList(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit)

    /**
     * Apply for mic
     * @param micIndex Mic position index
     */
    fun startMicSeatApply(micIndex: Int? = null, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Accept application
     * @param userId User ID
     */
    fun acceptMicSeatApply(userId: String, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Cancel mic application
     * @param userId IM user ID
     */
    fun cancelMicSeatApply(userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Invite user to mic
     * @param userId IM user ID
     */
    fun startMicSeatInvitation(userId: String, micIndex: Int?, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Accept invitation
     */
    fun acceptMicSeatInvitation(completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Reject invitation
     */
    fun refuseInvite(completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Mute
     * @param mute Mute state
     */
    fun muteLocal(mute: Boolean, completion: (error: Int, result: VoiceMemberModel?) -> Unit)

    /**
     * Forbid specific mic position
     * @param micIndex Mic position index
     */
    fun forbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Unforbid specific mic position
     * @param micIndex Mic position index
     */
    fun unForbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Lock mic
     * @param micIndex Mic position index
     */
    fun lockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Unlock mic
     * @param micIndex Mic position index
     */
    fun unLockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Kick user off mic
     * @param micIndex Mic position index
     */
    fun kickOff(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Leave mic
     * @param micIndex Mic position index
     */
    fun leaveMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Change mic position
     * @param oldIndex Old mic position index
     * @param newIndex New mic position index
     */
    fun changeMic(
        oldIndex: Int,
        newIndex: Int,
        completion: (error: Int, result: Map<Int, VoiceMicInfoModel>?) -> Unit
    )

    /**
     * Update announcement
     * @param content Announcement content
     */
    fun updateAnnouncement(content: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Update robot configuration
     * @param info Robot spatial audio configuration
     */
    fun updateRobotInfo(info: RobotSpatialAudioModel, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Subscribe to room time up event
     */
    fun subscribeRoomTimeUp(
        onRoomTimeUp: () -> Unit
    )
}