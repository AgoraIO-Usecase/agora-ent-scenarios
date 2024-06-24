package io.agora.scene.playzone.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.reflect.TypeToken
import io.agora.rtmsyncmanager.ISceneResponse
import io.agora.rtmsyncmanager.RoomExpirationPolicy
import io.agora.rtmsyncmanager.RoomService
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUICommonConfig
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmAttributeRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.manager.UserManager
import io.agora.scene.playzone.PlayCenter
import io.agora.scene.playzone.PlayLogger
import tech.sud.mgp.SudMGPWrapper.state.SudMGPAPPState
import kotlin.random.Random

class PlaySyncManagerServiceImp constructor(private val cxt: Context) : PlayZoneServiceProtocol, ISceneResponse,
    IAUIUserService.AUIUserRespObserver {

    companion object {
        private const val TAG = "PZ_Service_LOG"
        private const val kSceneId = "scene_play_zone_4.10.2"
        private const val kCollectionRobotInfo = "robot_info" // map collection
    }

    // 机器人 mapCollection
    private fun getRobotCollection(roomId: String): AUIMapCollection? {
        if (roomId.isEmpty()) {
            return null
        }
        val scene = mSyncManager.getScene(roomId)
        return scene?.getCollection(kCollectionRobotInfo) { a, b, c -> AUIMapCollection(a, b, c) }
    }

    private val mMainHandler by lazy { Handler(Looper.getMainLooper()) }

    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mMainHandler.looper.thread) {
            r.run()
        } else {
            mMainHandler.post(r)
        }
    }

    // sync manager
    private val mSyncManager: SyncManager

    // room manager
    private val mRoomManager = AUIRoomManager()

    // room service
    private val mRoomService: RoomService

    // 房间号
    @Volatile
    private var mCurRoomNo: String = ""

    // 当前用户信息
    private val mCurrentUser: AUIUserThumbnailInfo get() = AUIRoomContext.shared().currentUserInfo

    // 用户信息
    private val mUserList = mutableListOf<AUIUserInfo>()

    // 机器人信息
    private val mRobotMap = mutableMapOf<String, PlayRobotInfo>()

    /**
     * Observable helper
     */
    private val mObservableHelper = ObservableHelper<PlayZoneServiceListenerProtocol>()

    // time limit
    private val ROOM_AVAILABLE_DURATION: Long = 10 * 60 * 1000 // 10min

    init {
        HttpManager.setBaseURL(ServerConfig.roomManagerUrl)
        val rtmSyncTag = "PlayZone_RTM_LOG"
        AUILogger.initLogger(
            AUILogger.Config(cxt, "KTV", logCallback = object : AUILogger.AUILogCallback {
                override fun onLogDebug(tag: String, message: String) {
                    PlayLogger.d(rtmSyncTag, "$tag $message")
                }

                override fun onLogInfo(tag: String, message: String) {
                    PlayLogger.d(rtmSyncTag, "$tag $message")
                }

                override fun onLogWarning(tag: String, message: String) {
                    PlayLogger.w(rtmSyncTag, "$tag $message")
                }

                override fun onLogError(tag: String, message: String) {
                    PlayLogger.e(rtmSyncTag, "$tag $message")
                }

            })
        )

        val commonConfig = AUICommonConfig().apply {
            context = cxt
            appId = PlayCenter.mAppId
            owner = AUIUserThumbnailInfo().apply {
                userId = UserManager.getInstance().user.id.toString()
                userName = UserManager.getInstance().user.name
                userAvatar = UserManager.getInstance().user.headUrl
            }
            host = ServerConfig.toolBoxUrl
        }
        mSyncManager = SyncManager(cxt, null, commonConfig)

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

    /**
     * Init rtm sync
     *
     * @param completion
     * @receiver
     */
    private fun initRtmSync(completion: (exception: AUIRtmException?) -> Unit) {
        if (mSyncManager.rtmManager.isLogin) {
            completion.invoke(null)
            return
        }
        if (PlayCenter.mRtmToken.isEmpty()) {
            PlayLogger.d(TAG, "initRtmSync, renewToken start")
            PlayCenter.generateRtmToken { rtmToken, exception ->
                val token = rtmToken ?: run {
                    PlayLogger.e(TAG, "initRtmSync, $exception")
                    completion.invoke(AUIRtmException(-1, exception?.message ?: "error", ""))
                    return@generateRtmToken
                }
                mSyncManager.login(token, completion = {
                    if (it == null) {
                        completion.invoke(null)
                        PlayLogger.d(TAG, "initRtmSync, with renewToken loginRtm success")
                    } else {
                        completion.invoke(it)
                        PlayLogger.e(TAG, "initRtmSync, with renewToken loginRtm failed: $it")
                    }
                })
            }
        } else {
            mSyncManager.login(PlayCenter.mRtmToken, completion = {
                if (it == null) {
                    completion.invoke(null)
                    PlayLogger.d(TAG, "initRtmSync, without loginRtm success")
                } else {
                    completion.invoke(it)
                    PlayLogger.e(TAG, "initRtmSync, without renewToken loginRtm failed: $it")
                }
            })
        }
    }

    private val robotUid = 3000000001
    private val headUrl = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/{head}.png"
    override fun onWillInitSceneMetadata(channelName: String): Map<String, Any> {
        // 初始化机器人信息
        val rotBotMap = mutableMapOf<String, Any>()
        for (i in 0 until 9) {
            val robotId1 = (robotUid + i * 2).toString()
            val modelMale = PlayRobotInfo(
                gender = "male",
                level = 1,
                owner = AUIUserThumbnailInfo().apply {
                    userId = robotId1
                    userName = "机器人${1 + i * 2}"
                    userAvatar = headUrl.replace("{head}", "man" + (i + 1))
                }
            )
            rotBotMap[robotId1] = modelMale
            val robotId2 = (robotUid + i * 2 + 1).toString()
            val modelFemale = PlayRobotInfo(
                gender = "female",
                level = 1,
                owner = AUIUserThumbnailInfo().apply {
                    userId = robotId2
                    userName = "机器人${2 + i * 2}"
                    userAvatar = headUrl.replace("{head}", "woman" + (i + 1))
                }
            )
            rotBotMap[robotId2] = modelFemale
        }

        return mapOf(kCollectionRobotInfo to rotBotMap)
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        PlayLogger.d(TAG, "onTokenPrivilegeWillExpire, $channelName")
        PlayCenter.generateRtmToken { rtmToken, exception ->
            val token = rtmToken ?: run {
                PlayLogger.e(TAG, "onTokenPrivilegeWillExpire, with renewRtmToken failed: $exception")
                return@generateRtmToken
            }
            mSyncManager.login(token, completion = { rtmException ->
                if (rtmException == null) {
                    PlayLogger.d(TAG, "onTokenPrivilegeWillExpire, with loginRtm success")
                } else {
                    PlayLogger.e(TAG, "onTokenPrivilegeWillExpire, with loginRtm failed: $rtmException")
                }
            })
        }
    }

    override fun onSceneExpire(channelName: String) {
        PlayLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            leaveRoom { }
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onRoomExpire()
            }
        }
    }

    override fun onSceneDestroy(channelName: String) {
        PlayLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            leaveRoom { }
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onRoomDestroy()
            }
        }
    }

    override fun onSceneUserBeKicked(channelName: String, userId: String) {
        PlayLogger.d(TAG, "onSceneUserBeKicked, channelName:$channelName, userId:$userId")
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        PlayLogger.d(TAG, "onRoomUserSnapshot, roomId:$roomId, userList:${userList?.count()}")
        if (mCurRoomNo != roomId) {
            return
        }
        userList?.let {
            this.mUserList.clear()
            this.mUserList.addAll(it)
        }
        mObservableHelper.notifyEventHandlers { delegate ->
            delegate.onUserCountUpdate(mUserList.size)
        }
    }

    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        PlayLogger.d(TAG, "onRoomUserEnter, roomId:$roomId, userInfo:$userInfo")
        if (mCurRoomNo != roomId) {
            return
        }
        mUserList.removeIf { it.userId == userInfo.userId }
        mUserList.add(userInfo)
        mObservableHelper.notifyEventHandlers { delegate ->
            delegate.onUserCountUpdate(mUserList.size)
        }
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId) ?: return
        // 所有人都可修改用户数
        cacheRoom.customPayload[PlayZoneParameters.ROOM_USER_COUNT] = mUserList.count()
        mRoomManager.updateRoomInfo(PlayCenter.mAppId, kSceneId, cacheRoom, callback = { auiException, roomInfo ->
            if (auiException == null) {
                PlayLogger.d(TAG, "onRoomUserEnter updateRoom success: $roomInfo")
            } else {
                PlayLogger.e(TAG, "onRoomUserEnter updateRoom failed: $mCurRoomNo $auiException")
            }
        })
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        PlayLogger.d(TAG, "onRoomUserUpdate, roomId:$roomId, userInfo:$userInfo")
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        PlayLogger.d(TAG, "onRoomUserLeave, roomId:$roomId, userInfo:$userInfo")
        if (mCurRoomNo != roomId) {
            return
        }
        mUserList.removeIf { it.userId == userInfo.userId }
        mObservableHelper.notifyEventHandlers { delegate ->
            delegate.onUserCountUpdate(mUserList.size)
        }
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId) ?: return
        // 所有人都可修改用户数
        cacheRoom.customPayload[PlayZoneParameters.ROOM_USER_COUNT] = mUserList.count()
        mRoomManager.updateRoomInfo(PlayCenter.mAppId, kSceneId, cacheRoom, callback = { auiException, roomInfo ->
            if (auiException == null) {
                PlayLogger.d(TAG, "onRoomUserLeave updateRoom success: $roomId, $roomInfo")
            } else {
                PlayLogger.d(TAG, "onRoomUserLeave updateRoom failed: $roomId $auiException")
            }
        })
    }


    // 本地时间与restful 服务端差值
    private var restfulDiffTs: Long = 0

    override fun getCurrentDuration(roomId: String): Long {
        if (roomId.isEmpty()) return 0
        val scene = mSyncManager.getScene(roomId)
        return scene?.getRoomDuration() ?: 0L
    }

    override fun getCurrentTs(roomId: String): Long {
        if (roomId.isEmpty()) return 0
        val scene = mSyncManager.getScene(roomId)
        return scene?.getCurrentTs() ?: 0L
    }

    override fun getRoomList(completion: (error: Exception?, roomList: List<AUIRoomInfo>?) -> Unit) {
        PlayLogger.d(TAG, "getRoomList start")
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }
            mRoomService.getRoomList(PlayCenter.mAppId, kSceneId, 0, 50,
                cleanClosure = { auiRoomInfo ->
                    return@getRoomList auiRoomInfo.roomOwner?.userId == PlayCenter.mUser.id.toString()

                },
                completion = { uiException, ts, roomList ->
                    if (uiException == null) {
                        ts?.let { serverTs ->
                            restfulDiffTs = System.currentTimeMillis() - serverTs
                        }
                        val newRoomList = roomList?.sortedBy { -it.createTime } ?: emptyList()
                        PlayLogger.d(TAG, "getRoomList success,serverTs:$ts roomCount:${newRoomList.size}")
                        runOnMainThread { completion.invoke(null, newRoomList) }
                    } else {
                        PlayLogger.e(TAG, "getRoomList error, $uiException")
                        runOnMainThread {
                            completion.invoke(uiException, null)
                        }
                    }
                })
        }
    }

    private fun getRandomThumbnailId(crateAt: Long) = Random(crateAt).nextInt(0, 5).toString()

    override fun createRoom(
        inputModel: PlayCreateRoomModel, completion: (error: Exception?, result: AUIRoomInfo?) -> Unit
    ) {
        PlayLogger.d(TAG, "createRoom start")
        val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }
            PlayCenter.generateRtcToken { rtcToken, exception ->
                // 创建房间提前获取 rtcToken
                val token = rtcToken ?: run {
                    PlayLogger.e(TAG, "createRoom, with renewRtcToken failed: $exception")
                    completion.invoke(exception, null)
                    return@generateRtcToken
                }
                if (exception != null) {
                    completion.invoke(exception, null)
                    return@generateRtcToken
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
                    this.customPayload[PlayZoneParameters.ROOM_USER_COUNT] = 1L
                    this.customPayload[PlayZoneParameters.THUMBNAIL_ID] = getRandomThumbnailId(createAt)
                    this.customPayload[PlayZoneParameters.PASSWORD] = inputModel.password ?: ""
                    this.customPayload[PlayZoneParameters.IS_PRIVATE] = !inputModel.password.isNullOrEmpty()
                    this.customPayload[PlayZoneParameters.GAME_ID] = inputModel.gameId
                    this.customPayload[PlayZoneParameters.BADGE_TITLE] = inputModel.gameName
                }
                val scene = mSyncManager.createScene(roomInfo.roomId)
                scene.bindRespDelegate(this)
                scene.userService.registerRespObserver(this)
                innerSubscribeAll(roomId)
                mCurRoomNo = roomInfo.roomId
                mRoomService.createRoom(PlayCenter.mAppId, kSceneId, roomInfo, completion = { rtmException, _ ->
                    if (rtmException == null) {
                        PlayLogger.d(TAG, "createRoom success: $roomInfo")
                        mCurRoomNo = roomInfo.roomId
                        startTimer()
                        runOnMainThread {
                            completion.invoke(null, roomInfo)
                        }
                    } else {
                        mCurRoomNo = ""
                        PlayLogger.e(TAG, "createRoom failed: $rtmException")
                        runOnMainThread {
                            completion.invoke(Exception("${rtmException.message}(${rtmException.code})"), null)
                        }
                    }
                })
            }
        }
    }

    override fun joinRoom(roomId: String, password: String?, completion: (error: Exception?) -> Unit) {
        if (mCurRoomNo.isNotEmpty()) {
            completion.invoke(Exception("already join room $mCurRoomNo!"))
            return
        }
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId)
        if (cacheRoom == null) {
            completion.invoke(Exception("room $mCurRoomNo null!"))
            return
        }
        val roomPassword = cacheRoom.customPayload[PlayZoneParameters.PASSWORD] as? String
        if (!roomPassword.isNullOrEmpty() && roomPassword != password) {
            completion.invoke(Exception("password is wrong!"))
            return
        }
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"))
                return@initRtmSync
            }
            PlayCenter.generateRtcToken(callback = { rtcToken, exception ->
                // 进入房间提前获取 rtcToken
                val token = rtcToken ?: run {
                    PlayLogger.e(TAG, "joinRoom, with renewRtcToken failed: $exception")
                    completion.invoke(exception)
                    return@generateRtcToken
                }
                val scene = mSyncManager.createScene(roomId)
                scene.bindRespDelegate(this)
                scene.userService.registerRespObserver(this)
                innerSubscribeAll(roomId)
                mCurRoomNo = roomId
                mRoomService.enterRoom(PlayCenter.mAppId, kSceneId, roomId, completion = { rtmException ->
                    if (rtmException == null) {
                        PlayLogger.d(TAG, "enterRoom success: ${cacheRoom.roomId}")
                        mCurRoomNo = cacheRoom.roomId
                        runOnMainThread {
                            completion.invoke(null)
                        }
                    } else {
                        mCurRoomNo = ""
                        PlayLogger.e(TAG, "enterRoom failed: $rtmException")
                        runOnMainThread {
                            completion.invoke(Exception("${rtmException.message}(${rtmException.code})"))
                        }
                    }
                })
            })
        }
    }

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
                mRoomService.leaveRoom(PlayCenter.mAppId, kSceneId, mCurRoomNo)
            }
        }
        mUserList.clear()
        mCurRoomNo = ""
    }

    private fun innerSubscribeAll(roomId: String) {
        val robotCollection = getRobotCollection(roomId)
        robotCollection?.subscribeAttributesDidChanged { channelName, observeKey, value ->
            if (observeKey != kCollectionRobotInfo) return@subscribeAttributesDidChanged
            PlayLogger.d(TAG, "attributesDidChanged roomId: $channelName key: $observeKey")
            val robots = value.getMap() ?: GsonTools.toBean(
                GsonTools.beanToString(value),
                object : TypeToken<Map<String, Any>>() {}.type
            )
            val robotMap = mutableMapOf<String, PlayRobotInfo>()
            robots?.values?.forEach {
                GsonTools.toBean(GsonTools.beanToString(it), PlayRobotInfo::class.java)?.let { robotInfo ->
                    robotInfo.owner?.userId?.let { userId ->
                        robotMap[userId] = robotInfo
                    }
                }
            }
            if (mRobotMap.isEmpty()) {
                mRobotMap.putAll(robotMap)
                mObservableHelper.notifyEventHandlers { delegate ->
                    delegate.onRobotMapSnapshot(mRobotMap)
                }
            }
        }
    }

    override fun subscribeListener(listener: PlayZoneServiceListenerProtocol) {
        mObservableHelper.subscribeEvent(listener)
        if (mUserList.isNotEmpty()) {
            listener.onUserCountUpdate(mUserList.size)
        }
        if (mRobotMap.isNotEmpty()) {
            listener.onRobotMapSnapshot(mRobotMap)
        }
    }

    override fun unsubscribeListener(listener: PlayZoneServiceListenerProtocol) {
        mObservableHelper.unSubscribeEvent(listener)
    }
}