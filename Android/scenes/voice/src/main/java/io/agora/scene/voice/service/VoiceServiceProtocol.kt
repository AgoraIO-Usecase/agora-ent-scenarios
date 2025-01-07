package io.agora.scene.voice.service

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.model.*

/**
 * @author create by zhangwei03
 *
 * voice chat room protocol define
 */
interface VoiceServiceProtocol {

    companion object {

        // Room lifetime in milliseconds
        var ROOM_AVAILABLE_DURATION: Long = 1200 * 1000

        const val ERR_OK = 0
        const val ERR_FAILED = 1

        private var innerProtocol: VoiceServiceProtocol? = null

        @JvmStatic
        val serviceProtocol: VoiceServiceProtocol
            get() {
                if (innerProtocol == null) {
                    innerProtocol =   VoiceSyncManagerServiceImp(AgoraApplication.the()) { error ->
                        VoiceLogger.e("VoiceServiceProtocol", "voice chat protocol error：${error?.message}")
                    }
                }
                return innerProtocol!!
            }

        @Synchronized
        fun destroy() {
            (innerProtocol as? VoiceSyncManagerServiceImp)?.destroy()
            innerProtocol = null
        }
    }

    /**
     * Register subscription
     * @param listener Chatroom IM callback handler
     */
    fun subscribeListener(listener: VoiceServiceListenerProtocol)

    /**
     * Unsubscribe
     */
    fun unsubscribeListener()

    fun getSubscribeListeners(): ObservableHelper<VoiceServiceListenerProtocol>

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
     * Get room list
     * @param page Page index, starts from 0 (this property is temporarily invalid since SyncManager cannot paginate)
     */
    fun getRoomList(completion: (error: Exception?, roomList: List<AUIRoomInfo>?) -> Unit)

    /**
     * Create room
     * @param inputModel Input room information
     */
    fun createRoom(inputModel: VoiceCreateRoomModel, completion: (error: Exception?, out: AUIRoomInfo?) -> Unit)

    /**
     * Join room
     * @param roomId Room id
     */
    fun joinRoom(roomId: String, password: String?, completion: (error: Exception?, roomInfo: AUIRoomInfo?) -> Unit)

    /**
     * Leave room
     * @param roomId Room id
     */
    fun leaveRoom(completion: (error: Exception?) -> Unit)

    /**
     * Get room details
     * @param voiceRoomModel Room summary
     */
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel, completion: (error: Int, result: VoiceRoomInfo?) -> Unit)

    /**
     * Get ranking list
     */
    fun fetchGiftContribute(completion: (error: Int, result: List<VoiceRankUserModel>?) -> Unit)

    /**
     * Get invited users list
     */
    fun fetchRoomInvitedMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit)

    /**
     * Get user list
     */
    fun fetchRoomMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit)

    fun kickMemberOutOfRoom(chatUidList: MutableList<String>, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Update user list
     */
    fun updateRoomMembers(completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Application list
     */
    fun fetchApplicantsList(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit)

    /**
     * Apply for mic position
     * @param micIndex Mic position index
     */
    fun startMicSeatApply(micIndex: Int? = null, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Accept application
     * @param chatUid HuanXin user id
     */
    fun acceptMicSeatApply(
        micIndex: Int?,
        chatUid: String,
        completion: (error: Int, result: VoiceMicInfoModel?) -> Unit
    )

    /**
     * Cancel mic application
     * @param chatUid IM uid
     */
    fun cancelMicSeatApply(chatroomId: String, chatUid: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Invite user to mic
     * @param chatUid IM uid
     */
    fun startMicSeatInvitation(chatUid: String, micIndex: Int?, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Accept invitation
     */
    fun acceptMicSeatInvitation(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Reject invitation
     */
    fun refuseInvite(completion: (error: Int, result: Boolean) -> Unit)

    /**
     * Mute
     * @param micIndex Mic position index
     */
    fun muteLocal(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Unmute
     * @param micIndex Mic position index
     */
    fun unMuteLocal(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Forbid specified mic position
     * @param micIndex Mic position index
     */
    fun forbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * Cancel forbidding specified mic position
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
     * Enable/disable robot
     * @param enable true to enable robot, false to disable robot
     */
    fun enableRobot(enable: Boolean, completion: (error: Int, enable: Boolean) -> Unit)

    /**
     * Update robot volume
     * @param value Volume
     */
    fun updateRobotVolume(value: Int, completion: (error: Int, result: Boolean) -> Unit)
}