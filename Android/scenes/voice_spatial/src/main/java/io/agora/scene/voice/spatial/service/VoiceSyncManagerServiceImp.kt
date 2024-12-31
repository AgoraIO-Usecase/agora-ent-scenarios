package io.agora.scene.voice.spatial.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.utils.GsonTools
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.global.VSpatialCenter
import io.agora.scene.voice.spatial.model.*
import io.agora.scene.voice.spatial.model.annotation.MicStatus
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback
import io.agora.syncmanager.rtm.Sync.JoinSceneCallback
import java.util.concurrent.CountDownLatch

/**
 * Voice chat room service implementation using SyncManager
 */
class VoiceSyncManagerServiceImp(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : VoiceServiceProtocol {

    companion object {
        private val TAG = VoiceSyncManagerServiceImp::class.java.simpleName
    }

    private val voiceSceneId = "scene_spatialChatRoom_${BuildConfig.APP_VERSION_NAME}"
    private val kCollectionIdUser = "user_collection"
    private val kCollectionIdSeatInfo = "seat_info_collection"
    private val kCollectionIdSeatApply = "show_seat_apply_collection"
    private val kCollectionIdRobotInfo = "robot_info_collection"

    private val roomChecker = RoomChecker(context)

    @Volatile
    private var syncUtilsInit = false

    private var mSceneReference: SceneReference? = null

    @Volatile
    private var currRoomNo: String = ""

    private val roomMap = mutableMapOf<String, VoiceRoomModel>() // key: roomNo
    private val objIdOfRoomNo = mutableMapOf<String, String>() // objectId of room no

    private val roomSubscribeListener = mutableListOf<Sync.EventListener>()

    private val roomServiceSubscribeDelegates = mutableListOf<VoiceRoomSubscribeDelegate>()

    // user
    private val userMap = mutableMapOf<String, VoiceMemberModel>()
    private val objIdOfUserId = mutableMapOf<String, String>() // key: userId, value: objectId

    // mic apply
    private val micSeatApplyList = ArrayList<VoiceRoomApply>()
    private val objIdOfSeatApply = ArrayList<String>() // objectId of seat Apply

    // seat info
    private val micSeatMap = mutableMapOf<String, VoiceMicInfoModel>() // key: seatIndex
    private val objIdOfSeatInfo = HashMap<Int, String>() // objectId of seat index

    // robot info
    private var robotInfo = RobotSpatialAudioModel()
    private var objIdOfRobotInfo: String? = null

    // time limit
    private var roomTimeUpSubscriber: (() -> Unit)? = null
    private val ROOM_AVAILABLE_DURATION: Int = 20 * 60 * 1000 // 20min
    private val timerRoomEndRun = Runnable {
        ThreadManager.getInstance().runOnMainThread {
            roomTimeUpSubscriber?.invoke()
        }
    }

    /**
     * Register subscription
     * @param delegate Chat room IM callback handler
     */
    override fun subscribeEvent(delegate: VoiceRoomSubscribeDelegate) {
        roomServiceSubscribeDelegates.add(delegate)
    }

    /**
     * Unsubscribe
     */
    override fun unsubscribeEvent() {
        roomServiceSubscribeDelegates.clear()
    }

    override fun getSubscribeDelegates(): MutableList<VoiceRoomSubscribeDelegate> {
        return roomServiceSubscribeDelegates
    }

    override fun reset() {
        if (syncUtilsInit) {
            Sync.Instance().destroy()
            syncUtilsInit = false
        }
    }

    /**
     * Get room list
     * @param page Page index, starts from 0 (temporarily invalid due to SyncManager limitations)
     */
    override fun fetchRoomList(page: Int, completion: (error: Int, result: List<VoiceRoomModel>) -> Unit) {
        initScene {
            Sync.Instance().getScenes(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    val ret = mutableListOf<VoiceRoomModel>()
                    result?.forEach { iObj ->
                        try {
                            if (iObj.id != "") {
                                val voiceRoom = iObj.toObject(VoiceRoomModel::class.java)
                                ret.add(voiceRoom)
                                roomMap[voiceRoom.roomId] = voiceRoom
                                objIdOfRoomNo[voiceRoom.roomId] = iObj.id
                            }
                        } catch (e: Exception) {
                            VoiceSpatialLogger.e(TAG, "voice room list get scene error: ${e.message}")
                        }

                    }
                    // Sort by creation time
                    val comparator: Comparator<VoiceRoomModel> = Comparator { o1, o2 ->
                        o2.createdAt.compareTo(o1.createdAt)
                    }
                    ret.sortWith(comparator)
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(VoiceServiceProtocol.ERR_OK, ret)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    val e = exception ?: return
                    val ret = mutableListOf<VoiceRoomModel>()
                    if (e.code == -VoiceServiceProtocol.ERR_ROOM_LIST_EMPTY) {
                        ThreadManager.getInstance().runOnMainThread {
                            completion.invoke(VoiceServiceProtocol.ERR_OK, ret)
                        }
                        return
                    }
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, emptyList())
                    }
                }
            })
        }
    }

    /**
     * Create room
     * @param inputModel Room information input
     */
    override fun createRoom(
        inputModel: VoiceCreateRoomModel, completion: (error: Int, result: VoiceRoomModel) -> Unit
    ) {
        // 1、Create room information based on user input information
        val currentMilliseconds = System.currentTimeMillis()
        val voiceRoomModel = VoiceRoomModel().apply {
            roomId = currentMilliseconds.toString()
            channelId = currentMilliseconds.toString()
            soundEffect = inputModel.soundEffect
            isPrivate = inputModel.isPrivate
            roomName = inputModel.roomName
            createdAt = currentMilliseconds
            roomPassword = inputModel.password
            memberCount = 2 // Two robots
            clickCount = 2 // Two robots
        }
        val owner = VoiceMemberModel().apply {
            rtcUid = VSpatialCenter.rtcUid
            nickName = VSpatialCenter.nickname
            userId = VSpatialCenter.userId
            micIndex = 0
            portrait = VSpatialCenter.headUrl
        }
        voiceRoomModel.owner = owner

        //2、Token exchange, get IM configuration, room creation needs this data TODO: Not needed?

        //3、Create room
        initScene {
            val scene = Scene()
            scene.id = voiceRoomModel.roomId
            scene.userId = owner.userId
            scene.property = GsonTools.beanToMap(voiceRoomModel)
            Sync.Instance().createScene(scene, object : Sync.Callback {
                override fun onSuccess() {
                    roomMap[voiceRoomModel.roomId] = voiceRoomModel
                    completion.invoke(VoiceServiceProtocol.ERR_OK, voiceRoomModel)
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, voiceRoomModel)
                }
            })
        }
    }

    /**
     * Join room
     * @param roomId Room ID
     */
    override fun joinRoom(roomId: String, completion: (error: Int, result: VoiceRoomModel?) -> Unit) {
        initScene {
            val isRoomOwner = roomMap[roomId]?.owner?.userId == VSpatialCenter.userId
            Sync.Instance().joinScene(isRoomOwner, true, roomId, object : JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    VoiceSpatialLogger.d(TAG, "syncManager joinScene onSuccess ${sceneReference?.id}")
                    mSceneReference = sceneReference
                    if (roomMap[roomId] == null) {
                        completion.invoke(VoiceServiceProtocol.ERR_ROOM_UNAVAILABLE, null)
                        VoiceSpatialLogger.e(TAG, " room is not existent ")
                        return
                    }

                    // Update room information
                    val curRoomInfo = roomMap[roomId] ?: return
                    currRoomNo = curRoomInfo.roomId
                    if (roomChecker.joinRoom(roomId)) {
                        curRoomInfo.memberCount = curRoomInfo.memberCount + 1
                    }
                    curRoomInfo.clickCount = curRoomInfo.clickCount + 1
                    VoiceSpatialLogger.d(TAG, "joinRoom memberCount $curRoomInfo")
                    innerUpdateRoomInfo(curRoomInfo, {}, {})

                    // Subscribe to room events
                    innerSubscribeRoomChanged()
                    innerAddRobotInfo({}, {})
                    innerSubscribeSeatApply {}
                    innerMayAddLocalUser({
                        innerAutoOnSeatIfNeed { _, _ ->
                            ThreadManager.getInstance().runOnMainThread {
                                completion.invoke(VoiceServiceProtocol.ERR_OK, curRoomInfo)
                            }
                        }
                    }, {})

                    // Room countdown
                    if (TextUtils.equals(curRoomInfo.owner?.userId, VSpatialCenter.userId)) {
                        ThreadManager.getInstance().runOnMainThreadDelay(timerRoomEndRun, ROOM_AVAILABLE_DURATION)
                    } else {
                        ThreadManager.getInstance().runOnMainThreadDelay(
                            timerRoomEndRun,
                            ROOM_AVAILABLE_DURATION - (System.currentTimeMillis() - curRoomInfo.createdAt).toInt()
                        )
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                    VoiceSpatialLogger.e(TAG, "syncManager joinScene onFail ${exception.toString()}")
                }
            })
        }
    }

    /**
     * Leave room
     * @param roomId Room ID
     */
    override fun leaveRoom(
        roomId: String,
        isRoomOwnerLeave: Boolean,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        val cacheRoom = roomMap[roomId] ?: return
        roomChecker.leaveRoom(roomId)
        // Cancel all subscriptions
        roomSubscribeListener.forEach {
            mSceneReference?.unsubscribe(it)
        }
        ThreadManager.getInstance().removeCallbacks(timerRoomEndRun)
        roomTimeUpSubscriber = null
        roomSubscribeListener.clear()
        if (TextUtils.equals(cacheRoom.owner?.userId, VSpatialCenter.userId)) {
            // Remove room
            mSceneReference?.delete(object : Sync.Callback {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(roomId, true)
                        completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                    }
                    VoiceSpatialLogger.d(TAG, "syncManager delete onSuccess")
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnIOThread {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                    }
                    VoiceSpatialLogger.e(TAG, "syncManager delete onFail：${exception.toString()}")
                }
            })
        } else {
            if (isRoomOwnerLeave) {
                // Remove local user information
                innerRemoveUser(VSpatialCenter.userId, {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(currRoomNo, false)
                        completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                    }
                    VoiceSpatialLogger.d(TAG, "syncManager update onSuccess")
                }, {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(roomId, false)
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                    }
                    VoiceSpatialLogger.e(TAG, "syncManager update onFail：${it}")
                })
                return
            }

            val curRoomInfo = roomMap[roomId] ?: return
            curRoomInfo.memberCount -= 1
            innerUpdateRoomInfo(curRoomInfo, {
                // Remove local user information
                innerRemoveUser(VSpatialCenter.userId, {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(currRoomNo, false)
                        completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                    }
                    VoiceSpatialLogger.d(TAG, "syncManager update onSuccess")
                }, {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(roomId, false)
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                    }
                    VoiceSpatialLogger.e(TAG, "syncManager update onFail：${it}")
                })
            }, {
                ThreadManager.getInstance().runOnIOThread {
                    resetCacheInfo(roomId, false)
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                }
            })
        }
    }

    /**
     * Get room details
     * @param voiceRoomModel Room summary
     */
    override fun fetchRoomDetail(
        voiceRoomModel: VoiceRoomModel,
        completion: (error: Int, result: VoiceRoomInfo?) -> Unit
    ) {
        Sync.Instance().getScenes(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                result?.forEach { iObj ->
                    try {
                        val voiceRoom = iObj.toObject(VoiceRoomModel::class.java)
                        if (voiceRoom.roomId == currRoomNo) {
                            innerGetAllSeatInfo { list ->
                                innerGetRobotInfo {
                                    val info = VoiceRoomInfo().apply {
                                        roomInfo = voiceRoom
                                        micInfo = list
                                        robotInfo = it
                                    }
                                    ThreadManager.getInstance().runOnIOThread {
                                        completion.invoke(VoiceServiceProtocol.ERR_OK, info)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        VoiceSpatialLogger.e(TAG, "voice room list get scene error: ${e.message}")
                    }
                }
            }

            override fun onFail(exception: SyncManagerException?) {
                ThreadManager.getInstance().runOnMainThread {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }
        })
    }

    /**
     * Get user list
     */
    override fun fetchRoomMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
        innerGetUserList({
            // Need to exclude room owner
            val listExceptRoomOwner = mutableListOf<VoiceMemberModel>()
            it.forEach { user ->
                if (user.userId != VSpatialCenter.userId) {
                    listExceptRoomOwner.add(user)
                }
            }
            completion.invoke(VoiceServiceProtocol.ERR_OK, listExceptRoomOwner)
        }, {
            ThreadManager.getInstance().runOnMainThread {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, emptyList())
            }
        })
    }

    /**
     * Get applicant list
     */
    override fun fetchApplicantsList(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
        innerGetAllMicSeatApply { e, list ->
            if (e != 0) {
                // error
            } else {
                val memberList = arrayListOf<VoiceMemberModel>()
                list.forEach { apply ->
                    apply.member?.let { it -> memberList.add(it) }
                }
                ThreadManager.getInstance().runOnIOThread {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, memberList)
                }
            }
        }
    }

    /**
     * Apply for mic
     * @param micIndex Mic position index
     */
    override fun startMicSeatApply(micIndex: Int?, completion: (error: Int, result: Boolean) -> Unit) {
        val localUid = VSpatialCenter.userId
        if (userMap.containsKey(localUid)) {
            val apply = VoiceRoomApply().apply {
                index = micIndex
                member = userMap[localUid]
                created_at = System.currentTimeMillis()
            }
            innerCreateMicSeatApply(apply, completion)
        }
    }

    /**
     * Accept application
     * @param userId User ID
     */
    override fun acceptMicSeatApply(userId: String, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        innerGetAllMicSeatApply { e, list ->
            if (e == 0) {
                list.forEach { it ->
                    if (it.member?.userId == userId) {
                        // Update mic position status
                        val micIndex = selectEmptySeat(it.index!!)
                        if (micSeatMap.containsKey(micIndex.toString()) &&
                            micSeatMap[micIndex.toString()]?.member != null
                        ) {
                            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                            // Mic occupied
                            return@forEach
                        }
                        val member = userMap[userId]
                        if (member?.micIndex != -1) {
                            val toSeat = micSeatMap[member?.micIndex.toString()]
                            completion.invoke(VoiceServiceProtocol.ERR_OK, toSeat)
                            return@forEach
                        }
                        val toSeat = micSeatMap[micIndex.toString()]
                        if (member != null && toSeat != null) {
                            member.micIndex = toSeat.micIndex
                            seatDownMember(toSeat, member)
                            innerUpdateUserInfo(member, {}, {})
                            innerUpdateSeat(toSeat) { e ->
                                if (e == null) {
                                    // Remove application after success
                                    val index = micSeatApplyList.indexOf(it)
                                    micSeatApplyList.removeAt(index)
                                    val objId = objIdOfSeatApply.removeAt(index)
                                    innerRemoveMicSeatApply(objId, it) { _, _ -> }
                                    completion.invoke(VoiceServiceProtocol.ERR_OK, toSeat)
                                } else {
                                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                                }
                            }
                        } else {
                            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                        }
                    }
                }
            }
        }
    }

    /**
     * Cancel mic
     * @param userId im uid
     */
    override fun cancelMicSeatApply(userId: String, completion: (error: Int, result: Boolean) -> Unit) {
        innerGetAllMicSeatApply { e, list ->
            if (e == 0) {
                list.forEach {
                    if (it.member?.userId == userId) {
                        // 1、Remove this application
                        val index = micSeatApplyList.indexOf(it)
                        micSeatApplyList.removeAt(index)
                        val objId = objIdOfSeatApply.removeAt(index)
                        innerRemoveMicSeatApply(objId, it) { _, _ ->
                            completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                        }
                    }
                }
            } else {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        }
    }

    /**
     * Invite user to mic
     * @param userId im uid
     */
    override fun startMicSeatInvitation(
        userId: String,
        micIndex: Int?,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        val micIndex = selectEmptySeat(micIndex ?: -1)
        val toSeat = micSeatMap[micIndex.toString()]
        if (toSeat == null) {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            return
        }
        val userInfo = userMap[userId] ?: return
        val index = micIndex ?: return
        userInfo.micIndex = index
        userInfo.status = MicRequestStatus.waitting.value
        innerUpdateUserInfo(userInfo, {
            completion.invoke(VoiceServiceProtocol.ERR_OK, true)
        }, {})
    }

    /**
     * Accept invitation
     */
    override fun acceptMicSeatInvitation(completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        val member = userMap[VSpatialCenter.userId] ?: return
        member.status = MicRequestStatus.accepted.value
        val toIndex = selectEmptySeat(member.micIndex)
        val toSeat = micSeatMap[toIndex.toString()]
        if (toSeat == null) {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            return
        }
        if (micSeatMap.containsKey(toIndex.toString()) && toSeat.member != null) {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            return
        }
        member.micIndex = toIndex
        seatDownMember(toSeat, member)
        innerUpdateUserInfo(member, {
            innerUpdateSeat(toSeat) { e ->
                if (e == null) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, toSeat)
                } else {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }
        }, {})
    }

    /**
     * Refuse invitation
     */
    override fun refuseInvite(completion: (error: Int, result: Boolean) -> Unit) {
        val userInfo = userMap[VSpatialCenter.userId] ?: return
        userInfo.status = MicRequestStatus.idle.value
        innerUpdateUserInfo(userInfo, {
            completion.invoke(VoiceServiceProtocol.ERR_OK, true)
        }, {})
    }

    /**
     * mute
     * @param mute on / off
     */
    override fun muteLocal(mute: Boolean, completion: (error: Int, result: VoiceMemberModel?) -> Unit) {
        var user: VoiceMemberModel? = null
        userMap.forEach { (_, model) ->
            if (model.rtcUid == VSpatialCenter.rtcUid) {
                model.micStatus = if (mute) MicStatus.Mute else MicStatus.Normal
                user = model
            }
        }
        val userInfo = user
        if (userInfo != null) {
            innerUpdateUserInfo(userInfo, {
                completion.invoke(VoiceServiceProtocol.ERR_OK, userInfo)
            }) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        } else {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
        }
        if (user?.micIndex != -1) {
            micSeatMap[user?.micIndex.toString()]?.let {
                seatDownMember(it, user)
                innerUpdateSeat(it) {}
            }
        }
    }

    /**
     * Mute specific mic position
     * @param micIndex Mic position index
     */
    override fun forbidMic(
        micIndex: Int,
        completion: (error: Int, result: VoiceMicInfoModel?) -> Unit
    ) {
        val seatInfo = micSeatMap[micIndex.toString()]
        if (seatInfo != null) {
            when (seatInfo.micStatus) {
                MicStatus.Lock -> {
                    seatInfo.micStatus = MicStatus.LockForceMute
                }

                else -> {
                    seatInfo.micStatus = MicStatus.ForceMute
                }
            }
            innerUpdateSeat(seatInfo) {
                if (it == null) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, seatInfo)
                } else {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }
        } else {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
        }
    }

    /**
     * Unmute specific mic position
     * @param micIndex Mic position index
     */
    override fun unForbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        val seatInfo = micSeatMap[micIndex.toString()]
        if (seatInfo != null) {
            when (seatInfo.micStatus) {
                MicStatus.LockForceMute -> {
                    seatInfo.micStatus = MicStatus.Lock
                }

                else -> {
                    seatInfo.micStatus = if (seatInfo.member == null) MicStatus.Idle else MicStatus.Normal
                }
            }
            innerUpdateSeat(seatInfo) {
                if (it == null) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, seatInfo)
                } else {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }
        } else {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
        }
    }

    /**
     * Lock mic
     * @param micIndex Mic position index
     */
    override fun lockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        val seatInfo = micSeatMap[micIndex.toString()]
        if (seatInfo != null) {
            when (seatInfo.micStatus) {
                MicStatus.ForceMute -> {
                    seatInfo.micStatus = MicStatus.LockForceMute
                }

                else -> {
                    seatInfo.micStatus = MicStatus.Lock
                }
            }
            innerUpdateSeat(seatInfo) {
                if (it == null) {
                    kickOff(micIndex) { i, a ->

                    }
                    completion.invoke(VoiceServiceProtocol.ERR_OK, seatInfo)
                } else {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }

        } else {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
        }
    }

    /**
     * Unlock mic
     * @param micIndex Mic position index
     */
    override fun unLockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        val seatInfo = micSeatMap[micIndex.toString()]
        if (seatInfo != null) {
            when (seatInfo.micStatus) {
                MicStatus.LockForceMute -> {
                    seatInfo.micStatus = MicStatus.ForceMute
                }

                else -> {
                    seatInfo.micStatus = if (seatInfo.member == null) MicStatus.Idle else MicStatus.Normal
                }
            }
            innerUpdateSeat(seatInfo) {
                if (it == null) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, seatInfo)
                } else {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }
        } else {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
        }
    }

    /**
     * Kick user off mic
     * @param micIndex Mic position index
     */
    override fun kickOff(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        if (micSeatMap.containsKey(micIndex.toString())) {
            val seatInfo = micSeatMap[micIndex.toString()]
            val member = userMap[seatInfo?.member?.userId]
            if (seatInfo != null && member != null) {// Mic occupied
                seatDownMember(seatInfo, null)
                innerUpdateSeat(seatInfo) {
                    if (it == null) {
                        // Reset user information
                        member.micIndex = -1
                        member.status = MicRequestStatus.idle.value
                        innerUpdateUserInfo(member, {
                            completion.invoke(VoiceServiceProtocol.ERR_OK, seatInfo)
                        }, {
                            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                        })
                    } else {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                    }
                }
            }
        }
    }

    /**
     * Leave mic
     * @param micIndex Mic position index
     */
    override fun leaveMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        if (micSeatMap.containsKey(micIndex.toString())) {
            val seatInfo = micSeatMap[micIndex.toString()]
            val member = userMap[seatInfo?.member?.userId]
            if (seatInfo != null && member != null) {// Mic occupied
                seatDownMember(seatInfo, null)
                innerUpdateSeat(seatInfo) {
                    if (it == null) {
                        // Reset user information
                        member.micIndex = -1
                        member.status = MicRequestStatus.idle.value
                        innerUpdateUserInfo(member, {
                            completion.invoke(VoiceServiceProtocol.ERR_OK, seatInfo)
                        }, {
                            completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                        })
                    } else {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                    }
                }
            }
        }
    }

    /**
     * Change mic
     * @param oldIndex Old mic position index
     * @param newIndex New mic position index
     */
    override fun changeMic(
        oldIndex: Int,
        newIndex: Int,
        completion: (error: Int, result: Map<Int, VoiceMicInfoModel>?) -> Unit
    ) {
        if (micSeatMap.containsKey(oldIndex.toString())) {
            val fromSeat = micSeatMap[oldIndex.toString()]
            val toSeat = micSeatMap[newIndex.toString()]
            val member = userMap[fromSeat?.member?.userId]
            if (fromSeat != null && member != null) { // Mic occupied
                seatDownMember(fromSeat, null)
                innerUpdateSeat(fromSeat) {
                    if (it == null && toSeat != null) {
                        member.micIndex = newIndex
                        seatDownMember(toSeat, member)
                        innerUpdateSeat(toSeat) { e ->
                            if (e == null) {
                                val micMap = hashMapOf<Int, VoiceMicInfoModel>()
                                micMap[newIndex] = toSeat
                                completion.invoke(VoiceServiceProtocol.ERR_OK, micMap)
                            } else {
                                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                            }
                        }
                        innerUpdateUserInfo(member, {}, {})
                    } else {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                    }
                }
            }
        }
    }

    /**
     * Update announcement
     * @param content Announcement content
     */
    override fun updateAnnouncement(content: String, completion: (error: Int, result: Boolean) -> Unit) {
        val roomInfo = roomMap[currRoomNo] ?: return
        roomInfo.announcement = content
        innerUpdateRoomInfo(GsonTools.beanToMap(roomInfo)) {
            if (it == null) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            } else {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        }
    }

    /**
     * Update robot configuration
     * @param info Robot configuration
     */
    override fun updateRobotInfo(
        info: RobotSpatialAudioModel,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        val targetSeatInfo = if (info.useRobot) {
            VoiceMicInfoModel().apply {
                this.micIndex = 3
                micStatus = MicStatus.BotActivated
            }
        } else {
            VoiceMicInfoModel().apply {
                this.micIndex = 3
                micStatus = MicStatus.BotInactive
            }
        }

        innerUpdateSeat(targetSeatInfo) {
            if (it == null) {
                innerUpdateRobotInfo(info) { e ->
                    if (e == null) {
                        robotInfo = info
                        completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                    } else {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                    }
                }
            } else {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        }
    }

    override fun subscribeRoomTimeUp(onRoomTimeUp: () -> Unit) {
        roomTimeUpSubscriber = onRoomTimeUp
    }

    private fun initScene(complete: () -> Unit) {
        if (syncUtilsInit) {
            complete.invoke()
            return
        }
        val handler = Handler(Looper.getMainLooper())
        Sync.Instance().init(
            RethinkConfig(VSpatialCenter.rtcAppId, voiceSceneId),
            object : Sync.Callback {
                override fun onSuccess() {
                    handler.post {
                        Sync.Instance().joinScene(voiceSceneId, object : JoinSceneCallback {
                            override fun onSuccess(sceneReference: SceneReference?) {
                                syncUtilsInit = true
                                ThreadManager.getInstance().runOnMainThread {
                                    VoiceSpatialLogger.d(TAG, "SyncManager init success")
                                    complete.invoke()
                                }
                            }

                            override fun onFail(exception: SyncManagerException?) {
                                ThreadManager.getInstance().runOnMainThread {
                                    VoiceSpatialLogger.e(TAG, "SyncManager init error: ${exception?.message}")
                                    errorHandler?.invoke(exception)
                                }
                            }
                        })
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnMainThread {
                        VoiceSpatialLogger.e(TAG, "SyncManager init error: ${exception?.message}")
                        errorHandler?.invoke(exception)
                    }
                }
            }
        )

    }

    private fun resetCacheInfo(roomId: String, isRoomDestroyed: Boolean = false) {
        if (isRoomDestroyed) {
            roomMap.remove(roomId)
        }
        mSceneReference = null
    }

    // ============================= inner func =============================
    // ----------------------------- Room properties -----------------------------
    private fun innerUpdateRoomInfo(curRoomInfo: VoiceRoomModel, success: () -> Unit, error: (error: Int) -> Unit) {
        val updateMap: HashMap<String, Any> = HashMap<String, Any>().apply {
            putAll(GsonTools.beanToMap(curRoomInfo))
        }
        VoiceSpatialLogger.d(TAG, " leaveRoom memberCount $curRoomInfo")
        mSceneReference?.update(updateMap, object : Sync.DataItemCallback {
            override fun onSuccess(result: IObject?) {
                success.invoke()
            }

            override fun onFail(exception: SyncManagerException?) {
                val e = exception ?: return
                error.invoke(e.code)
            }
        })
    }

    // ----------------------------- User properties -----------------------------
    private fun innerMayAddLocalUser(success: () -> Unit, error: (e: Exception?) -> Unit) {
        innerSubscribeOnlineUsers {}
        val uid = VSpatialCenter.userId
        innerGetUserList({ list ->
            if (list.none { it.userId == it.toString() }) {
                val user = VoiceMemberModel().apply {
                    rtcUid = VSpatialCenter.rtcUid
                    nickName = VSpatialCenter.nickname
                    userId = VSpatialCenter.userId
                    micIndex = -1
                    portrait = VSpatialCenter.headUrl
                }
                innerAddUser(user, {
                    objIdOfUserId[uid] = it
                    userMap[uid] = user
                    success.invoke()
                },
                    { e -> error.invoke(e) }
                )
            } else {
                success.invoke()
            }
        }, { e -> error.invoke(e) })
    }

    private fun innerGetUserList(success: (List<VoiceMemberModel>) -> Unit, error: (e: Exception?) -> Unit) {
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdUser)
            .get(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    result ?: return
                    val map = result.map { it.toObject(VoiceMemberModel::class.java) }
                    userMap.clear()

                    val ret = ArrayList<VoiceMemberModel>()
                    result.forEach {
                        val obj = it.toObject(VoiceMemberModel::class.java)
                        val uid = obj.userId ?: return@forEach
                        objIdOfUserId[uid] = it.id
                        ret.add(obj)
                        userMap[obj.userId!!] = obj
                    }
                    ThreadManager.getInstance().runOnMainThread {
                        success.invoke(map)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnMainThread {
                        error.invoke(exception)
                    }
                }
            })
    }

    private fun innerAddUser(
        user: VoiceMemberModel,
        success: (String) -> Unit,
        error: (Exception) -> Unit
    ) {
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdUser)
            .add(user, object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject?) {
                    success.invoke(result?.id!!)
                }

                override fun onFail(exception: SyncManagerException?) {
                    error.invoke(exception!!)
                }
            })
    }

    private fun innerRemoveUser(
        userId: String,
        success: () -> Unit,
        error: (Exception) -> Unit
    ) {
        val sceneReference = mSceneReference ?: return
        val objectId = objIdOfUserId[userId] ?: return
        sceneReference.collection(kCollectionIdUser)
            .delete(objectId, object : Sync.Callback {
                override fun onSuccess() {
                    success.invoke()
                }

                override fun onFail(exception: SyncManagerException?) {
                    error.invoke(exception!!)
                }
            })
    }

    private fun innerUpdateUserInfo(
        user: VoiceMemberModel,
        success: () -> Unit,
        error: (Exception) -> Unit
    ) {
        val sceneReference = mSceneReference ?: return
        val objectId = objIdOfUserId[user.userId] ?: return
        sceneReference.collection(kCollectionIdUser)
            ?.update(objectId, user, object : Sync.Callback {
                override fun onSuccess() {
                    success.invoke()
                }

                override fun onFail(exception: SyncManagerException?) {
                    error.invoke(exception!!)
                }
            })
    }

    // Subscribe online users
    private fun innerSubscribeOnlineUsers(completion: () -> Unit) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                // Store user information in local list
                val userInfo = item.toObject(VoiceMemberModel::class.java)
                // User receives seat invitation
                if (userInfo.userId ==VSpatialCenter.userId &&
                    userMap[userInfo.userId.toString()]?.status != MicRequestStatus.waitting.value &&
                    userInfo.status == MicRequestStatus.waitting.value
                ) {
                    roomServiceSubscribeDelegates.forEach {
                        ThreadManager.getInstance().runOnMainThread {
                            it.onReceiveSeatInvitation()
                        }
                    }
                }

                // Room member update
                if (!userMap.containsKey(userInfo.userId.toString())) {
                    roomServiceSubscribeDelegates.forEach {
                        ThreadManager.getInstance().runOnMainThread {
                            it.onUserJoinedRoom(currRoomNo, userInfo)
                        }
                    }
                } else {
                    val map = userMap
                    val i = item
                }
                userMap[userInfo.userId.toString()] = userInfo
                objIdOfUserId[userInfo.userId.toString()] = item.id
                //innerUpdateUserCount(userMap.size)
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                // Remove user information from local list
                objIdOfUserId.forEach { entry ->
                    if (entry.value == item.id) {
                        val removeUserNo = entry.key
                        userMap.remove(removeUserNo)
                        objIdOfUserId.remove(entry.key)

                        // Room member update
                        roomServiceSubscribeDelegates.forEach {
                            ThreadManager.getInstance().runOnMainThread {
                                it.onUserLeftRoom(currRoomNo, removeUserNo)
                            }
                        }
                        return
                    }
                }

            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.collection(kCollectionIdUser)?.subscribe(listener)
    }

    /** Get empty mic position
     *
     * Pass in a specific index, use the mic position when it is available, and move down to the valid mic position when it is not available
     */
    private fun selectEmptySeat(index: Int): Int {
        // Check if the mic position is available
        val toSeat = micSeatMap[index.toString()]
        if (toSeat != null && toSeat.member == null && (toSeat.micStatus == MicStatus.Idle||
                    toSeat.micStatus == MicStatus.Mute || toSeat.micStatus == MicStatus.ForceMute)) {
            return index
        }
        var toIndex = -1
        for (i in 1 until 5) {
            val toSeat = micSeatMap[i.toString()]
            if (toSeat != null && toSeat.member == null && toSeat.micStatus == MicStatus.Idle) {
                toIndex = i
                break
            }
        }
        return toIndex
    }

    // ----------------------------- Seat application -----------------------------
    private fun innerGetAllMicSeatApply(completion: (error: Int, result: List<VoiceRoomApply>) -> Unit) {
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdSeatApply).get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VoiceRoomApply>()
                val retObjId = ArrayList<String>()
                result?.forEach {
                    val obj = it.toObject(VoiceRoomApply::class.java)
                    ret.add(obj)
                    retObjId.add(it.id)
                }

                micSeatApplyList.clear()
                micSeatApplyList.addAll(ret)
                objIdOfSeatApply.clear()
                objIdOfSeatApply.addAll(retObjId)

                completion.invoke(VoiceServiceProtocol.ERR_OK, ret)
            }

            override fun onFail(exception: SyncManagerException?) {

            }
        })
    }

    private fun innerCreateMicSeatApply(apply: VoiceRoomApply, completion: (error: Int, result: Boolean) -> Unit) {
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdSeatApply)
            .add(apply, object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject?) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, false)
                }
            })
    }

    private fun innerRemoveMicSeatApply(
        objId: String,
        apply: VoiceRoomApply,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdSeatApply)
            .delete(objId, object : Sync.Callback {
                override fun onSuccess() {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, false)
                }
            })
    }

    private fun innerSubscribeSeatApply(completion: () -> Unit) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
            }

            override fun onUpdated(item: IObject?) {
                item?.toObject(VoiceRoomApply::class.java) ?: return
                roomServiceSubscribeDelegates.forEach {
                    ThreadManager.getInstance().runOnMainThread {
                        it.onReceiveSeatRequest()
                    }
                }
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                roomServiceSubscribeDelegates.forEach {
                    ThreadManager.getInstance().runOnMainThread {
                        it.onReceiveSeatRequestRejected("")
                    }
                }
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.collection(kCollectionIdSeatApply)?.subscribe(listener)
    }

    // ----------------------------- Mic status -----------------------------
    private fun seatDownMember(seat: VoiceMicInfoModel, member: VoiceMemberModel?) {
        seat.member = member
        if (member != null) { // Seat
            seat.micStatus = if (seat.micStatus == MicStatus.Idle) MicStatus.Normal else seat.micStatus
        } else { // Leave
            seat.micStatus = if (seat.micStatus == MicStatus.Normal) MicStatus.Idle else seat.micStatus
        }
    }

    private fun innerGenerateDefaultSeatInfo(index: Int, uid: String): VoiceMicInfoModel {
        val seatInfo = micSeatMap[index.toString()]
        var mem: VoiceMemberModel? = null
        var micState = MicStatus.Idle
        if (userMap.containsKey(uid)) {
            mem = userMap[uid]
            mem?.micIndex = index
            micState = seatInfo?.micStatus ?: MicStatus.Normal
        }
        return VoiceMicInfoModel().apply {
            micIndex = index
            member = mem
            ownerTag = false
            micStatus = micState
        }
    }

    private fun innerGenerateAllDefaultSeatInfo(completion: (error: Exception?) -> Unit) {
        val countDownLatch = CountDownLatch(6)
        innerAddSeatInfo(innerGenerateDefaultSeatInfo(1, "")) {
            if (it == null) countDownLatch.countDown()
        }
        innerAddSeatInfo(innerGenerateDefaultSeatInfo(2, "")) {
            if (it == null) countDownLatch.countDown()
        }
        innerAddSeatInfo(VoiceMicInfoModel().apply {
            micIndex = 3
            member = null
            ownerTag = false
            micStatus = MicStatus.BotInactive
        }) {
            if (it == null) countDownLatch.countDown()
        }
        innerAddSeatInfo(innerGenerateDefaultSeatInfo(4, "")) {
            if (it == null) countDownLatch.countDown()
        }
        innerAddSeatInfo(innerGenerateDefaultSeatInfo(5, "")) {
            if (it == null) countDownLatch.countDown()
        }
        innerAddSeatInfo(VoiceMicInfoModel().apply {
            micIndex = 6
            member = null
            ownerTag = false
            micStatus = MicStatus.BotInactive
        }) {
            if (it == null) countDownLatch.countDown()
        }

        Thread {
            countDownLatch.await()
            completion.invoke(null)
        }.start()
    }

    private fun innerGetAllSeatInfo(success: (List<VoiceMicInfoModel>) -> Unit) {
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdSeatInfo).get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VoiceMicInfoModel>()
                result?.forEach {
                    val obj = it.toObject(VoiceMicInfoModel::class.java)
                    objIdOfSeatInfo[obj.micIndex] = it.id
                    ret.add(obj)

                    micSeatMap[obj.micIndex.toString()] = obj
                }

                success.invoke(ret)
            }

            override fun onFail(exception: SyncManagerException?) {

            }
        })
    }

    private fun innerAutoOnSeatIfNeed(completion: (error: Exception?, seat: List<VoiceMicInfoModel>) -> Unit) {
        val cacheRoom = roomMap[currRoomNo] ?: return
        innerSubscribeSeats {}
        innerGetAllSeatInfo { _ ->
            var hasMaster = false
            val outList = ArrayList<VoiceMicInfoModel>()
            micSeatMap.forEach {
                it.value.let { seat ->
                    outList.add(seat)
                    if (seat.ownerTag) {
                        hasMaster = true
                    }
                }
            }
            if (!hasMaster && cacheRoom.owner?.userId == VSpatialCenter.userId) {
                // Host joins the microphone
                innerGenerateAllDefaultSeatInfo {
                    val targetSeatInfo = VoiceMicInfoModel().apply {
                        micIndex = 0
                        member = cacheRoom.owner
                        ownerTag = true
                        micStatus = MicStatus.Normal
                    }
                    innerAddSeatInfo(targetSeatInfo) { error ->
                        if (error != null) {
                            completion.invoke(error, emptyList())
                            return@innerAddSeatInfo
                        }
                        outList.add(targetSeatInfo)
                        ThreadManager.getInstance().runOnMainThread {
                            completion.invoke(null, outList)
                        }
                    }
                }
            } else {
                completion.invoke(null, outList)
            }
        }
    }

    private fun innerUpdateSeat(
        seatInfo: VoiceMicInfoModel,
        completion: (error: Exception?) -> Unit
    ) {
        val objectId = objIdOfSeatInfo[seatInfo.micIndex]
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.update(objectId, seatInfo, object : Sync.Callback {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerAddSeatInfo(
        seatInfo: VoiceMicInfoModel,
        completion: (error: Exception?) -> Unit
    ) {
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.add(seatInfo, object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject) {
                    ThreadManager.getInstance().runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerSubscribeSeats(completion: () -> Unit) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
            }

            override fun onUpdated(item: IObject?) {
                val obj = item?.toObject(VoiceMicInfoModel::class.java) ?: return
                objIdOfSeatInfo[obj.micIndex] = item.id

                micSeatMap[obj.micIndex.toString()] = obj
                obj.member?.userId?.let {
                    userMap[it]?.micIndex = obj.micIndex
                }
                roomServiceSubscribeDelegates.forEach {
                    ThreadManager.getInstance().runOnMainThread {
                        val attributeMap = hashMapOf<String, String>()
                        val key = "mic_" + obj.micIndex
                        attributeMap[key] = item.toString()
                        it.onSeatUpdated(currRoomNo, attributeMap)
                    }
                }
            }

            override fun onDeleted(item: IObject?) {
                item ?: return

                micSeatMap.forEach { entry ->
                    entry.value.let { seat ->
                        if (objIdOfSeatInfo[seat.micIndex] == item.id) {
                            micSeatMap.remove(entry.key)
                            return
                        }
                    }
                }
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.collection(kCollectionIdSeatInfo)?.subscribe(listener)
    }

    // ----------------------------- Robot information -----------------------------
    private fun innerAddRobotInfo(success: () -> Unit, error: (e: Exception?) -> Unit) {
        innerSubscribeRobotInfo {}

        if (roomMap[currRoomNo]?.owner?.userId == VSpatialCenter.userId) {
            val robotSpatialAudioInfo = RobotSpatialAudioModel()
            val sceneReference = mSceneReference ?: return
            sceneReference.collection(kCollectionIdRobotInfo)
                .add(robotSpatialAudioInfo, object : Sync.DataItemCallback {
                    override fun onSuccess(result: IObject?) {
                        success.invoke()
                    }

                    override fun onFail(exception: SyncManagerException?) {
                        error.invoke(exception!!)
                    }
                })
        }
    }

    private fun innerGetRobotInfo(success: (RobotSpatialAudioModel) -> Unit) {
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdRobotInfo).get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                result?.forEach {
                    val obj = it.toObject(RobotSpatialAudioModel::class.java)
                    objIdOfRobotInfo = it.id
                    robotInfo = obj
                    success.invoke(obj)
                }
            }

            override fun onFail(exception: SyncManagerException?) {

            }
        })
    }

    private fun innerUpdateRobotInfo(
        robotSpatialAudioInfo: RobotSpatialAudioModel,
        completion: (error: Exception?) -> Unit
    ) {
        val objectId = objIdOfRobotInfo ?: return
        mSceneReference?.collection(kCollectionIdRobotInfo)
            ?.update(objectId, robotSpatialAudioInfo, object : Sync.Callback {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerSubscribeRobotInfo(completion: () -> Unit) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
            }

            override fun onUpdated(item: IObject?) {
                val obj = item?.toObject(RobotSpatialAudioModel::class.java) ?: return

                roomServiceSubscribeDelegates.forEach {
                    ThreadManager.getInstance().runOnMainThread {
                        it.onRobotUpdate(currRoomNo, obj)
                    }
                }
                robotInfo = obj
                objIdOfRobotInfo = item.id
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.collection(kCollectionIdRobotInfo)?.subscribe(listener)
    }

    // ----------------------------- Room information -----------------------------
    private fun innerUpdateRoomInfo(data: Map<String, Any>, completion: (error: Exception?) -> Unit) {
        val dataMap = hashMapOf<String, Any>()
        data.forEach {
            dataMap[it.key] = it.value
        }
        mSceneReference?.update(
            dataMap,
            object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject?) {
                    ThreadManager.getInstance().runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerSubscribeRoomChanged() {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                val originRoomInfo = roomMap[currRoomNo] ?: return
                val roomInfo = item.toObject(VoiceRoomModel::class.java)
                //if (item.id != currRoomNo) return
                if (originRoomInfo.announcement != roomInfo.announcement) {
                    roomServiceSubscribeDelegates.forEach {
                        ThreadManager.getInstance().runOnMainThread {
                            it.onAnnouncementChanged(roomInfo.roomId, roomInfo.announcement)
                        }
                    }
                }
                roomMap[roomInfo.roomId] = roomInfo
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                val roomInfo = roomMap[item.id] ?: return
                roomServiceSubscribeDelegates.forEach {
                    ThreadManager.getInstance().runOnMainThread {
                        it.onRoomDestroyed(roomInfo.roomId)
                    }
                }
            }

            override fun onSubscribeError(ex: SyncManagerException?) {

            }

        }
        roomSubscribeListener.add(listener)
        Sync.Instance().subscribeScene(mSceneReference, listener)
    }
}