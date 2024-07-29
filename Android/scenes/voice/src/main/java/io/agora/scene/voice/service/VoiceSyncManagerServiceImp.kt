package io.agora.scene.voice.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.CallBack
import io.agora.ValueCallBack
import io.agora.chat.ChatRoom
import io.agora.rtmsyncmanager.ISceneResponse
import io.agora.rtmsyncmanager.RoomExpirationPolicy
import io.agora.rtmsyncmanager.RoomService
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUICommonConfig
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.voice.R
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.*
import io.agora.scene.voice.netkit.VRCreateRoomResponse
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.net.callback.VRValueCallBack
import kotlin.random.Random

/**
 * @author create by zhangwei03
 */
class VoiceSyncManagerServiceImp(
    private val mContext: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : VoiceServiceProtocol, ISceneResponse, IAUIUserService.AUIUserRespObserver {

    private val TAG = "VC_SYNC_LOG"

    private val voiceSceneId = "scene_chatRoom_5.0.0"
    private val kRoomBGMCollection = "room_bgm"

    private val roomChecker = RoomChecker(mContext)

    // 当前用户信息
    private val mCurrentUser: AUIUserThumbnailInfo get() = AUIRoomContext.shared().currentUserInfo

    private val mObservableHelper = ObservableHelper<VoiceServiceListenerProtocol>()

    private val ROOM_AVAILABLE_DURATION: Long = 20 * 60 * 1000 // 20min

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
     * sync manager
     */
    private val mSyncManager: SyncManager

    /**
     * room manager
     */
    private val mRoomManager = AUIRoomManager()

    /**
     * room service
     */
    private val mRoomService: RoomService

    /**
     * current room no
     */
    @Volatile
    private var mCurRoomNo: String = ""

    init {
        HttpManager.setBaseURL(ServerConfig.roomManagerUrl)
        val rtmSyncTag = "VOICE_RTM_LOG"
        AUILogger.initLogger(
            AUILogger.Config(mContext, "VOICE", logCallback = object : AUILogger.AUILogCallback {
                override fun onLogDebug(tag: String, message: String) {
                    VoiceLogger.d(rtmSyncTag, "$tag $message")
                }

                override fun onLogInfo(tag: String, message: String) {
                    VoiceLogger.d(rtmSyncTag, "$tag $message")
                }

                override fun onLogWarning(tag: String, message: String) {
                    VoiceLogger.w(rtmSyncTag, "$tag $message")
                }

                override fun onLogError(tag: String, message: String) {
                    VoiceLogger.e(rtmSyncTag, "$tag $message")
                }

            })
        )

        val commonConfig = AUICommonConfig().apply {
            context = mContext
            appId = VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId()
            owner = AUIUserThumbnailInfo().apply {
                userId = UserManager.getInstance().user.id.toString()
                userName = UserManager.getInstance().user.name
                userAvatar = UserManager.getInstance().user.headUrl
            }
            host = ServerConfig.toolBoxUrl
        }
        mSyncManager = SyncManager(mContext, null, commonConfig)

        val roomExpirationPolicy = RoomExpirationPolicy()
        roomExpirationPolicy.expirationTime = ROOM_AVAILABLE_DURATION
        roomExpirationPolicy.isAssociatedWithOwnerOffline = true
        mRoomService = RoomService(roomExpirationPolicy, mRoomManager, mSyncManager)
    }

    private fun startTimer() {
        mMainHandler.postDelayed(timerRoomCountDownTask, 1000)
    }

    private val timerRoomCountDownTask = object : Runnable {
        override fun run() {
            if (mCurRoomNo.isEmpty()) return
            val roomDuration = getCurrentDuration(mCurRoomNo)
            if (roomDuration >= ROOM_AVAILABLE_DURATION) {
                mMainHandler.removeCallbacks(this)
                onSceneExpire(mCurRoomNo)
            } else {
                mMainHandler.postDelayed(this, 1000)
            }
        }
    }

    // 背景音乐 mapCollection
    private fun getBgmCollection(roomId: String): AUIMapCollection? {
        if (roomId.isEmpty()) {
            return null
        }
        val scene = mSyncManager.getScene(roomId)
        return scene?.getCollection(kRoomBGMCollection) { a, b, c -> AUIMapCollection(a, b, c) }
    }

    private fun innerSubscribeAll(roomId: String) {
        val robotCollection = getBgmCollection(roomId)
        robotCollection?.subscribeAttributesDidChanged { channelName, observeKey, value ->
            if (observeKey != kRoomBGMCollection) return@subscribeAttributesDidChanged
            VoiceLogger.d(TAG, "attributesDidChanged roomId: $channelName key: $observeKey")
        }
    }

    override fun onWillInitSceneMetadata(channelName: String): Map<String, Any>? {
        return super.onWillInitSceneMetadata(channelName)
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        super.onTokenPrivilegeWillExpire(channelName)
    }

    override fun onSceneExpire(channelName: String) {
        VoiceLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            leaveRoom { }
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onSyncRoomExpire()
            }
        }
    }

    override fun onSceneDestroy(channelName: String) {
        VoiceLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            leaveRoom { }
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onSyncRoomDestroy()
            }
        }
    }

    override fun onSceneUserBeKicked(channelName: String, userId: String) {
        super.onSceneUserBeKicked(channelName, userId)
    }

    /**
     * Get current duration
     *
     * @param channelName
     * @return
     */
    override fun getCurrentDuration(channelName: String): Long {
        if (channelName.isEmpty()) return 0
        val scene = mSyncManager.getScene(channelName)
        return scene?.getRoomDuration() ?: 0L
    }

    /**
     * Get current ts
     *
     * @param channelName
     * @return
     */
    override fun getCurrentTs(channelName: String): Long {
        if (channelName.isEmpty()) return 0
        val scene = mSyncManager.getScene(channelName)
        return scene?.getCurrentTs() ?: 0L
    }

    /**
     * 注册订阅
     * @param delegate 聊天室内IM回调处理
     */
    override fun subscribeListener(listener: VoiceServiceListenerProtocol) {
        mObservableHelper.subscribeEvent(listener)
    }

    /**
     *  取消订阅
     */
    override fun unsubscribeListener() {
        mObservableHelper.unSubscribeAll()
    }

    override fun getSubscribeListeners(): ObservableHelper<VoiceServiceListenerProtocol> {
        return mObservableHelper
    }

    // 本地时间与restful 服务端差值
    private var restfulDiffTs: Long = 0

    /**
     * 获取房间列表
     * @param completion
     */
    override fun getRoomList(completion: (error: Exception?, roomInfoList: List<AUIRoomInfo>?) -> Unit) {
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }
            mRoomService.getRoomList(VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId(), voiceSceneId, 0, 50,
                cleanClosure = { auiRoomInfo ->
                    return@getRoomList auiRoomInfo.roomOwner?.userId == VoiceBuddyFactory.get().getVoiceBuddy().userId()
                },
                completion = { uiException, ts, roomList ->
                    if (uiException == null) {
                        ts?.let { serverTs ->
                            restfulDiffTs = System.currentTimeMillis() - serverTs
                        }
                        val newRoomList = roomList?.sortedBy { -it.createTime } ?: emptyList()
                        VoiceLogger.d(TAG, "getRoomList success,serverTs:$ts roomCount:${newRoomList.size}")
                        runOnMainThread {
                            completion.invoke(null, newRoomList)
                        }
                    } else {
                        VoiceLogger.e(TAG, "getRoomList error, $uiException")
                        runOnMainThread {
                            completion.invoke(uiException, null)
                        }
                    }
                }
            )
        }
    }

    /**
     * 创建房间
     * @param inputModel 输入的房间信息
     */
    override fun createRoom(
        inputModel: VoiceCreateRoomModel, completion: (error: Exception?, out: AUIRoomInfo?) -> Unit
    ) {
        val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()

        // rtm 初始化
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }

            // 创建环信房间
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
                        userId = mCurrentUser.userId
                        userName = mCurrentUser.userName
                        userAvatar = mCurrentUser.userAvatar
                    }
                    this.createTime = createAt
                    this.customPayload[VoiceParameters.ROOM_USER_COUNT] = 3L // 两个机器人
                    this.customPayload[VoiceParameters.PASSWORD] = inputModel.password
                    this.customPayload[VoiceParameters.IS_PRIVATE] = inputModel.password.isNotEmpty()
                    this.customPayload[VoiceParameters.CHANNEL_ID] = roomId
                    this.customPayload[VoiceParameters.CHATROOM_ID] = chatId
                    this.customPayload[VoiceParameters.RTC_UID] = mCurrentUser.userId.toInt()
                    this.customPayload[VoiceParameters.TYPE] = inputModel.soundEffect
                }

                val scene = mSyncManager.createScene(roomInfo.roomId)
                scene.bindRespDelegate(this)
                scene.userService.registerRespObserver(this)
                innerSubscribeAll(roomId)
                mCurRoomNo = roomInfo.roomId
                mRoomService.createRoom(VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId(), voiceSceneId, roomInfo,
                    completion = { rtmException, _ ->
                        if (rtmException == null) {
                            VoiceLogger.d(TAG, "createRoom success: $roomInfo")
                            mCurRoomNo = roomInfo.roomId
                            startTimer()
                            runOnMainThread {
                                completion.invoke(null, roomInfo)
                            }
                        } else {
                            mCurRoomNo = ""
                            VoiceLogger.e(TAG, "createRoom failed: $rtmException")
                            runOnMainThread {
                                completion.invoke(Exception("${rtmException.message}(${rtmException.code})"), null)
                            }
                        }
                    })
            })
        }
    }

    private fun innerCreateChatRoom(roomName: String, completion: (chatId: String?, error: Exception?) -> Unit) {
        VoiceToolboxServerHttpManager.createImRoom(
            roomName = roomName,
            roomOwner = mCurrentUser.userId,
            chatroomId = "",
            type = 2,
            callBack = object : VRValueCallBack<VRCreateRoomResponse> {
                override fun onSuccess(response: VRCreateRoomResponse?) {
                    completion.invoke(response?.chatId, null)
                }

                override fun onError(code: Int, message: String?) {
                    completion.invoke(null, Exception("$message $code"))
                }

            })
    }

    override fun joinRoom(
        roomId: String, password: String?, completion: (error: Exception?, roomInfo: AUIRoomInfo?) -> Unit
    ) {
        if (mCurRoomNo.isNotEmpty()) {
            completion.invoke(Exception("already join room $mCurRoomNo!"), null)
            return
        }
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId)
        if (cacheRoom == null) {
            completion.invoke(Exception("room $mCurRoomNo null!"), null)
            return
        }
        val roomPassword = cacheRoom.customPayload[VoiceParameters.PASSWORD] as? String
        if (!roomPassword.isNullOrEmpty() && roomPassword != password) {
            completion.invoke(Exception(mContext.getString(R.string.voice_room_check_password)), null)
            return
        }
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }
            val scene = mSyncManager.createScene(roomId)
            scene.bindRespDelegate(this)
            scene.userService.registerRespObserver(this)
            innerSubscribeAll(roomId)
            mCurRoomNo = roomId
        }
    }

    private fun remoteUpdateBGMInfo(info: VoiceBgmModel) {
        val song = info.songName
        val singer = info.singerName
        val isOrigin = info.isOrigin
        AgoraRtcEngineController.get().bgmManager().remoteUpdateBGMInfo(song, singer, isOrigin)
    }

    /**
     * 离开房间
     * @param roomId 房间id
     */
    override fun leaveRoom(completion: (error: Exception?) -> Unit) {
        mSyncManager.getScene(mCurRoomNo)?.let { scene ->
            scene.unbindRespDelegate(this)
            scene.userService.unRegisterRespObserver(this)
        }
        if (AUIRoomContext.shared().isRoomOwner(mCurRoomNo)) {
            mMainHandler.removeCallbacks(timerRoomCountDownTask)
        }
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"))
                return@initRtmSync
            } else {
                completion.invoke(null)
                mRoomService.leaveRoom(VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId(), voiceSceneId, mCurRoomNo)
            }
        }
        mCurRoomNo = ""
    }

    /**
     * 获取房间详情
     * @param voiceRoomModel 房间概要
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
     * 获取排行榜列表
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
     * 获取邀请列表（过滤已在麦位成员）
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
     * 获取房间成员列表
     */
    override fun fetchRoomMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
        val memberList = ChatroomIMManager.getInstance().fetchRoomMembers()
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
        //房主踢用户(踢出房间)
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
     * 更新用户列表
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
     * 申请列表
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
     * 申请上麦
     * @param micIndex 麦位index
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
     * 同意申请
     * @param chatUid 环信用户id
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
     * 取消上麦
     * @param chatUid im uid
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
     * 邀请用户上麦
     * @param chatUid im uid
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
     * 接受邀请
     */
    override fun acceptMicSeatInvitation(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
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
     * 拒绝邀请
     */
    override fun refuseInvite(completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance()
            .refuseInvite(VoiceBuddyFactory.get().getVoiceBuddy().chatUserName(), object : CallBack {
                override fun onSuccess() {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                }

                override fun onError(code: Int, error: String?) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, false)
                }
            })
    }

    /**
     * mute
     * @param micIndex 麦位index
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
     * unMute
     * @param micIndex 麦位index
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
     * 禁言指定麦位置
     * @param micIndex 麦位index
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
     * 取消禁言指定麦位置
     * @param micIndex 麦位index
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
     * 锁麦
     * @param micIndex 麦位index
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
     * 取消锁麦
     * @param micIndex 麦位index
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
     * 踢用户下麦
     * @param micIndex 麦位index
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
     * 下麦
     * @param micIndex 麦位index
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
     * 换麦
     * @param oldIndex 老麦位index
     * @param newIndex 新麦位index
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
     * 更新公告
     * @param content 公告内容
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

    override fun updateBGMInfo(info: VoiceBgmModel, completion: (error: Int) -> Unit) {

    }

    /**
     * 是否启用机器人
     * @param enable true 启动机器人，false 关闭机器人
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
     * 更新机器人音量
     * @param value 音量
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

    private fun initRtmSync(completion: (exception: AUIRtmException?) -> Unit) {
        if (mSyncManager.rtmManager.isLogin) {
            completion.invoke(null)
            return
        }
        if (VoiceBuddyFactory.get().getVoiceBuddy().rtmToken().isEmpty()) {
            VoiceToolboxServerHttpManager.generateAllToken{ rtmToken, exception ->
                val token = rtmToken ?: run {
                    VoiceLogger.e(TAG, "initRtmSync, $exception")
                    completion.invoke(AUIRtmException(-1, exception?.message ?: "error", ""))
                    return@generateAllToken
                }
                mSyncManager.login(token, completion = {
                    if (it == null) {
                        completion.invoke(null)
                        VoiceLogger.d(TAG, "initRtmSync, with renewToken loginRtm success")
                    } else {
                        completion.invoke(it)
                        VoiceLogger.e(TAG, "initRtmSync, with renewToken loginRtm failed: $it")
                    }
                })
            }
        } else {
            mSyncManager.login(VoiceBuddyFactory.get().getVoiceBuddy().rtmToken(), completion = {
                if (it == null) {
                    completion.invoke(null)
                    VoiceLogger.d(TAG, "initRtmSync, without loginRtm success")
                } else {
                    completion.invoke(it)
                    VoiceLogger.e(TAG, "initRtmSync, without renewToken loginRtm failed: $it")
                }
            })
        }
    }
}