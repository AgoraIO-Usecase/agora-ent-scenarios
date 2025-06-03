package io.agora.scene.voice.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.CallBack
import io.agora.ValueCallBack
import io.agora.chat.ChatRoom
import io.agora.rtmsyncmanager.RoomExpirationPolicy
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.ServerConfig
import io.agora.scene.voice.R
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.*
import io.agora.scene.voice.netkit.CHATROOM_CREATE_TYPE_ROOM
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.global.VoiceCenter
import io.agora.scene.voice.service.VoiceServiceProtocol.Companion.ROOM_AVAILABLE_DURATION
import kotlin.random.Random

/**
 * @author create by zhangwei03
 */
class VoiceSyncManagerServiceImp(
    private val mContext: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : VoiceServiceProtocol {

    private val TAG = "VOICE_SYNC_LOG"

    private val voiceSceneId = "scene_chatRoom_${BuildConfig.APP_VERSION_NAME}"

    private val mObservableHelper = ObservableHelper<VoiceServiceListenerProtocol>()

    private val mMainHandler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * Run on main thread
     *
     * @param r
     */
    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mMainHandler.looper.thread) {
            r.run()
        } else {
            mMainHandler.post(r)
        }
    }

    /**
     * room manager
     */
    private val mRoomManager = AUIRoomManager()

    /**
     * current room no
     */
    @Volatile
    private var mCurRoomNo: String = ""

    /**
     * room user list
     */
    private val mUserList = mutableListOf<VoiceMemberModel>()

    private val roomExpirationPolicy = RoomExpirationPolicy()

    private val roomInfoMap: MutableMap<String, AUIRoomInfo> = mutableMapOf()
    private val creatingRoomIds: MutableSet<String> = mutableSetOf()

    init {
        HttpManager.setBaseURL(ServerConfig.roomManagerUrl)
        val roomManagerTag = "ROOM_MANGER_LOG"
        AUILogger.initLogger(
            AUILogger.Config(mContext, "Voice", logCallback = object : AUILogger.AUILogCallback {
                override fun onLogDebug(tag: String, message: String) {
                    VoiceLogger.d(roomManagerTag, "$tag $message")
                }

                override fun onLogInfo(tag: String, message: String) {
                    VoiceLogger.d(roomManagerTag, "$tag $message")
                }

                override fun onLogWarning(tag: String, message: String) {
                    VoiceLogger.w(roomManagerTag, "$tag $message")
                }

                override fun onLogError(tag: String, message: String) {
                    VoiceLogger.e(roomManagerTag, "$tag $message")
                }
            })
        )

        roomExpirationPolicy.expirationTime =  ROOM_AVAILABLE_DURATION
        roomExpirationPolicy.isAssociatedWithOwnerOffline = true

        subscribeListener(object : VoiceServiceListenerProtocol {
            override fun onUserJoinedRoom(roomId: String, voiceMember: VoiceMemberModel) {
                super.onUserJoinedRoom(roomId, voiceMember)
                VoiceLogger.d(TAG, "onUserJoinedRoom, roomId:$roomId, userInfo:$voiceMember")
                if (mCurRoomNo != roomId) {
                    return
                }
                mUserList.removeIf { it.userId == voiceMember.userId }
                mUserList.add(voiceMember)
                mObservableHelper.notifyEventHandlers { delegate ->
                    delegate.onSyncUserCountUpdate(mUserList.size + ConfigConstants.ROBOT_COUNT)
                }
                val cacheRoom = roomInfoMap[roomId] ?: return
                // Everyone can modify the number of users // Owner + robots
                cacheRoom.customPayload[VoiceParameters.ROOM_USER_COUNT] = mUserList.size + ConfigConstants.ROBOT_COUNT
                mRoomManager.updateRoomInfo(BuildConfig.AGORA_APP_ID,
                    voiceSceneId, cacheRoom, callback = { auiException, roomInfo ->
                        if (auiException == null) {
                            VoiceLogger.d(TAG, "onUserJoinedRoom updateRoom success: $roomInfo")
                        } else {
                            VoiceLogger.e(TAG, "onUserJoinedRoom updateRoom failed: $roomId $auiException")
                        }
                    })
            }

            override fun onUserLeftRoom(roomId: String, chatUid: String) {
                super.onUserLeftRoom(roomId, chatUid)
                VoiceLogger.d(TAG, "onUserLeftRoom, roomId:$roomId, userInfo:$chatUid")
                if (mCurRoomNo != roomId) {
                    return
                }
                mUserList.removeIf { it.chatUid == chatUid }
                mObservableHelper.notifyEventHandlers { delegate ->
                    delegate.onSyncUserCountUpdate(mUserList.size + ConfigConstants.ROBOT_COUNT)
                }
                val cacheRoom = roomInfoMap[roomId] ?: return
                // Everyone can modify the number of users
                cacheRoom.customPayload[VoiceParameters.ROOM_USER_COUNT] = mUserList.count() + 2
                mRoomManager.updateRoomInfo(VoiceCenter.rtcAppId,
                    voiceSceneId, cacheRoom, callback = { auiException, roomInfo ->
                        if (auiException == null) {
                            VoiceLogger.d(TAG, "onUserLeftRoom updateRoom success: $roomId, $roomInfo")
                        } else {
                            VoiceLogger.d(TAG, "onUserLeftRoom updateRoom failed: $roomId $auiException")
                        }
                    })
            }
        })
    }

    private fun startTimer() {
        mMainHandler.postDelayed(timerRoomCountDownTask, 1000)
    }

    private val timerRoomCountDownTask = object : Runnable {
        override fun run() {
            if (mCurRoomNo.isEmpty()) return
            val roomDuration = getCurrentDuration(mCurRoomNo)
            if (roomDuration == 0L) return
            if (roomDuration >= VoiceServiceProtocol.ROOM_AVAILABLE_DURATION) {
                mMainHandler.removeCallbacks(this)
                onInnerRoomExpire(mCurRoomNo)
                VoiceLogger.d(TAG, "timerRoomCountDownTask, run")
            } else {
                mMainHandler.postDelayed(this, 1000)
            }
        }
    }

    private fun onInnerRoomExpire(channelName: String) {
        VoiceLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onRoomRoomExpire(mCurRoomNo)
            }
            leaveRoom { }
        }
    }

    /**
     * Get current duration
     *
     * @param channelName
     * @return
     */
    override fun getCurrentDuration(channelName: String): Long {
        if (channelName.isEmpty()) return 0
        val roomInfo = roomInfoMap[mCurRoomNo] ?: return 0
        val roomDuration = (System.currentTimeMillis() - restfulDiffTs) - roomInfo.createTime
        return roomDuration
    }

    /**
     * Get current ts
     *
     * @param channelName
     * @return
     */
    override fun getCurrentTs(channelName: String): Long {
        if (channelName.isEmpty()) return 0
        return System.currentTimeMillis() - restfulDiffTs
    }

    /**
     * Register subscription
     * @param delegate Chatroom IM callback handler
     */
    override fun subscribeListener(listener: VoiceServiceListenerProtocol) {
        mObservableHelper.subscribeEvent(listener)
        if (mUserList.isNotEmpty()) {
            listener.onSyncUserCountUpdate(mUserList.size + ConfigConstants.ROBOT_COUNT)
        }
    }

    /**
     * Unsubscribe
     */
    override fun unsubscribeListener() {
        mObservableHelper.unSubscribeAll()
    }

    override fun getSubscribeListeners(): ObservableHelper<VoiceServiceListenerProtocol> {
        return mObservableHelper
    }

    // Local time difference with restful server
    private var restfulDiffTs: Long = 0

    /**
     * Get room list
     * @param completion
     */
    override fun getRoomList(completion: (error: Exception?, roomInfoList: List<AUIRoomInfo>?) -> Unit) {
        mRoomManager.getRoomInfoList(BuildConfig.AGORA_APP_ID, voiceSceneId, 0, 50) { err, roomList, serverTs ->
            if (err != null || serverTs == null) {
                completion(err, null)
                VoiceLogger.e(TAG, "getRoomList error, $err")
                return@getRoomInfoList
            }
            restfulDiffTs = System.currentTimeMillis() - serverTs

            val list: MutableList<AUIRoomInfo> = mutableListOf()
            roomList?.forEach { roomInfo ->
                // Traverse each room information to check if it has expired.
                var needCleanRoom = false
                if (creatingRoomIds.contains(roomInfo.roomId)) {
                    // Do nothing
                } else if (roomExpirationPolicy.expirationTime > 0 && serverTs - roomInfo.createTime >= roomExpirationPolicy.expirationTime + 60 * 1000) {
                    needCleanRoom = true
                } else if (roomInfo.roomOwner?.userId == VoiceCenter.userId) {
                    needCleanRoom = true
                }

                if (needCleanRoom) {
                    mRoomManager.destroyRoom(
                        VoiceCenter.rtcAppId,
                        voiceSceneId,
                        roomInfo.roomId
                    ) {}
                    roomInfoMap.remove(roomInfo.roomId)
                    return@forEach
                }
                list.add(roomInfo)
                roomInfoMap[roomInfo.roomId] = roomInfo
            }
            VoiceLogger.d(TAG, "getRoomList success,serverTs:$serverTs roomCount:${list.size}")
            val newRoomList = list.sortedBy { -it.createTime }
            completion(null, newRoomList)
        }
    }

    /**
     * Create room
     * @param inputModel Input room information
     */
    override fun createRoom(
        inputModel: VoiceCreateRoomModel, completion: (error: Exception?, out: AUIRoomInfo?) -> Unit
    ) {
        val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
        // Create an environment room
        innerCreateChatRoom(inputModel.roomName, completion = { chatId, error ->
            if (chatId == null) {
                completion.invoke(error, null)
                return@innerCreateChatRoom
            }

            val createAt = System.currentTimeMillis() - restfulDiffTs
            val roomInfo = AUIRoomInfo().apply {
                this.roomId = roomId
                this.roomName = inputModel.roomName
                this.roomOwner = AUIUserThumbnailInfo().apply {
                    userId = VoiceCenter.userId
                    userName = VoiceCenter.nickname
                    userAvatar = VoiceCenter.headUrl
                }
                this.createTime = createAt
                this.customPayload[VoiceParameters.ROOM_USER_COUNT] = 2L //two robots
                this.customPayload[VoiceParameters.ROOM_SOUND_EFFECT] = inputModel.soundEffect
                this.customPayload[VoiceParameters.PASSWORD] = inputModel.password
                this.customPayload[VoiceParameters.IS_PRIVATE] = inputModel.password.isNotEmpty()
                this.customPayload[VoiceParameters.CHATROOM_ID] = chatId
            }
            mRoomManager.createRoom(BuildConfig.AGORA_APP_ID, voiceSceneId, roomInfo) { err, info ->
                if (err != null || info == null) {
                    VoiceLogger.e(TAG, "createRoom failed: $err")
                    runOnMainThread {
                        completion.invoke(err, null)
                    }
                    return@createRoom
                }
                VoiceLogger.d(TAG, "createRoom success: $roomInfo")
                mCurRoomNo = roomInfo.roomId
                startTimer()
                runOnMainThread {
                    completion.invoke(null, roomInfo)
                }
                mCurRoomNo = roomInfo.roomId
                creatingRoomIds.add(roomInfo.roomId)
                roomInfoMap[roomInfo.roomId] = roomInfo
            }
        })
    }

    private fun innerCreateChatRoom(roomName: String, completion: (chatId: String?, error: Exception?) -> Unit) {
        VoiceToolboxServerHttpManager.createImRoom(
            roomName = roomName,
            roomOwner = VoiceCenter.userId,
            chatroomId = "",
            type = CHATROOM_CREATE_TYPE_ROOM
        ) { resp, error ->
            if (error == null && resp != null) {
                completion.invoke(resp.chatId, null)
            } else {
                completion.invoke(null, error)
            }
        }
    }

    override fun joinRoom(
        roomId: String, password: String?, completion: (error: Exception?, roomInfo: AUIRoomInfo?) -> Unit
    ) {
        if (mCurRoomNo.isNotEmpty()) {
            completion.invoke(Exception("already join room $mCurRoomNo!"), null)
            return
        }
        val cacheRoom = roomInfoMap[roomId]
        if (cacheRoom == null) {
            completion.invoke(Exception("room $roomId null!"), null)
            return
        }
        val roomPassword = cacheRoom.roomPassword()
        if (roomPassword.isNotEmpty() && roomPassword != password) {
            completion.invoke(Exception(mContext.getString(R.string.voice_room_check_password)), null)
            return
        }
        mCurRoomNo = roomId
        startTimer()
        completion.invoke(null, cacheRoom)
    }

    /**
     * leave the room
     */
    override fun leaveRoom(completion: (error: Exception?) -> Unit) {
        val isOwner = roomInfoMap[mCurRoomNo]?.roomOwner?.userId == VoiceCenter.userId
        mMainHandler.removeCallbacks(timerRoomCountDownTask)
        if (isOwner) {
            mRoomManager.destroyRoom(BuildConfig.AGORA_APP_ID, voiceSceneId, mCurRoomNo) {}
        } else {
            // nothing
        }

        roomInfoMap.remove(mCurRoomNo)
        mUserList.clear()
        mCurRoomNo = ""
        completion.invoke(null)
    }

    /**
     *Get room details
     * @param voiceRoomModel Room overview
     */
    override fun fetchRoomDetail(
        voiceRoomModel: VoiceRoomModel,
        completion: (error: Int, result: VoiceRoomInfo?) -> Unit
    ) {
        ChatroomIMManager.getInstance().fetchRoomDetail(voiceRoomModel, object : ValueCallBack<VoiceRoomInfo> {
            override fun onSuccess(value: VoiceRoomInfo?) {
                if (value != null) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, value)
                } else {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Get the leaderboard list
     */
    override fun fetchGiftContribute(completion: (error: Int, result: List<VoiceRankUserModel>?) -> Unit) {
        ChatroomIMManager.getInstance().fetchGiftContribute(object :
            ValueCallBack<MutableList<VoiceRankUserModel>> {
            override fun onSuccess(value: MutableList<VoiceRankUserModel>) {
                runOnMainThread {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, value)
                }
            }

            override fun onError(error: Int, errorMsg: String?) {
                runOnMainThread {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }
        })
    }

    /**
     * Get the invitation list (filter members who are already in Maiwei)
     */
    override fun fetchRoomInvitedMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
        val memberList = ChatroomIMManager.getInstance().fetchRoomInviteMembers()
        if (memberList != null) {
            completion.invoke(VoiceServiceProtocol.ERR_OK, memberList)
        } else {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, mutableListOf())
        }
    }

    /**
     * Get the list of room members
     */
    override fun fetchRoomMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
        val memberList = ChatroomIMManager.getInstance().fetchRoomMembers()
        mUserList.clear()
        mUserList.addAll(memberList)
        if (memberList != null) {
            completion.invoke(VoiceServiceProtocol.ERR_OK, memberList)
        } else {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, mutableListOf())
        }
    }

    override fun kickMemberOutOfRoom(
        chatUidList: MutableList<String>,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        // The homeowner kicks out the user (kicks out of the room)
        ChatroomIMManager.getInstance().removeMemberToRoom(chatUidList, object :
            ValueCallBack<ChatRoom> {
            override fun onSuccess(value: ChatRoom?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * Update user list
     */
    override fun updateRoomMembers(completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().updateRoomMembers(object : CallBack {
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * Apply list
     */
    override fun fetchApplicantsList(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
        val raisedList = ChatroomIMManager.getInstance().fetchRaisedList()
        if (raisedList != null) {
            completion.invoke(VoiceServiceProtocol.ERR_OK, raisedList)
        } else {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, mutableListOf())
        }
    }

    /**
     * Apply for a microphone
     * @param micIndex Mic position index
     */
    override fun startMicSeatApply(micIndex: Int?, completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().startMicSeatApply(micIndex ?: -1, object : CallBack {
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * Agree to apply
     * @param chatUid Ease user id
     */
    override fun acceptMicSeatApply(
        micIndex: Int?,
        chatUid: String,
        completion: (error: Int, result: VoiceMicInfoModel?) -> Unit
    ) {
        ChatroomIMManager.getInstance().acceptMicSeatApply(micIndex ?: -1, chatUid, object :
            ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Cancel mic application
     * @param chatUid IM uid
     */
    override fun cancelMicSeatApply(
        chatroomId: String,
        chatUid: String,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        ChatroomIMManager.getInstance().cancelMicSeatApply(chatroomId, chatUid, object : CallBack {
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * Invite user to mic
     * @param chatUid IM uid
     */
    override fun startMicSeatInvitation(
        chatUid: String,
        micIndex: Int?,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        ChatroomIMManager.getInstance().invitationMic(chatUid, micIndex ?: -1, object : CallBack {
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * Accept invitation
     */
    override fun acceptMicSeatInvitation(
        micIndex: Int,
        completion: (error: Int, result: VoiceMicInfoModel?) -> Unit
    ) {
        ChatroomIMManager.getInstance().acceptMicSeatInvitation(micIndex, object :
            ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Reject invitation
     */
    override fun refuseInvite(completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance()
            .refuseInvite(VoiceCenter.chatUid, object : CallBack {
                override fun onSuccess() {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                }

                override fun onError(code: Int, error: String?) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, false)
                }
            })
    }

    /**
     * Mute
     * @param micIndex Mic position index
     */
    override fun muteLocal(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().muteLocal(micIndex, object :
            ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Unmute
     * @param micIndex Mic position index
     */
    override fun unMuteLocal(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().unMuteLocal(micIndex, object :
            ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Forbid specified mic position
     * @param micIndex Mic position index
     */
    override fun forbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().forbidMic(micIndex, object :
            ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Cancel forbidding specified mic position
     * @param micIndex Mic position index
     */
    override fun unForbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().unForbidMic(micIndex, object :
            ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Lock mic
     * @param micIndex Mic position index
     */
    override fun lockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().lockMic(micIndex, object : ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel) {
                runOnMainThread {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, value)
                }
            }

            override fun onError(error: Int, errorMsg: String?) {
                runOnMainThread {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }

        })
    }

    /**
     * Unlock mic
     * @param micIndex Mic position index
     */
    override fun unLockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().unLockMic(micIndex, object :
            ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Kick user off mic
     * @param micIndex Mic position index
     */
    override fun kickOff(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().kickOff(micIndex, object : ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Leave mic
     * @param micIndex Mic position index
     */
    override fun leaveMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().leaveMic(micIndex, object : ValueCallBack<VoiceMicInfoModel> {
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Change mic position
     * @param oldIndex Old mic position index
     * @param newIndex New mic position index
     */
    override fun changeMic(
        oldIndex: Int,
        newIndex: Int,
        completion: (error: Int, result: Map<Int, VoiceMicInfoModel>?) -> Unit
    ) {
        ChatroomIMManager.getInstance().changeMic(oldIndex, newIndex, object :
            ValueCallBack<MutableMap<Int, VoiceMicInfoModel>> {
            override fun onSuccess(value: MutableMap<Int, VoiceMicInfoModel>?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * Update announcement
     * @param content Announcement content
     */
    override fun updateAnnouncement(content: String, completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().updateAnnouncement(content, object : CallBack {
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * Enable/disable robot
     * @param enable true to enable robot, false to disable robot
     */
    override fun enableRobot(enable: Boolean, completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().enableRobot(enable, object :
            ValueCallBack<Boolean> {
            override fun onSuccess(value: Boolean) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * Update robot volume
     * @param value Volume
     */
    override fun updateRobotVolume(value: Int, completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().updateRobotVolume(value, object : CallBack {
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    fun destroy() {
        VoiceLogger.d(TAG, message = "destroy")
    }
}