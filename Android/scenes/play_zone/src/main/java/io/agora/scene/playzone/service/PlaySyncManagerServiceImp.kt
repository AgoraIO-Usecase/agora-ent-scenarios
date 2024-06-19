package io.agora.scene.playzone.service

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import io.agora.scene.playzone.PlayZoneCenter
import io.agora.scene.playzone.PlayZoneLogger
import kotlin.random.Random

class PlaySyncManagerServiceImp constructor(private val cxt: Context) : PlayZoneServiceProtocol, ISceneResponse,
    IAUIUserService.AUIUserRespObserver, AUIRtmAttributeRespObserver {

    companion object {
        private const val TAG = "PZ_Service_LOG"
        private const val kSceneId = "scene_play_zone_4.10.2"
        private const val kCollectionStartGameInfo = "startGameCollection"
    }

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

    /**
     * current user
     */
    private val mCurrentUser: AUIUserThumbnailInfo get() = AUIRoomContext.shared().currentUserInfo

    /**
     * room user list
     */
    private val mUserList = mutableListOf<AUIUserInfo>()

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
                    PlayZoneLogger.d(rtmSyncTag, "$tag $message")
                }

                override fun onLogInfo(tag: String, message: String) {
                    PlayZoneLogger.d(rtmSyncTag, "$tag $message")
                }

                override fun onLogWarning(tag: String, message: String) {
                    PlayZoneLogger.w(rtmSyncTag, "$tag $message")
                }

                override fun onLogError(tag: String, message: String) {
                    PlayZoneLogger.e(rtmSyncTag, "$tag $message")
                }

            })
        )

        val commonConfig = AUICommonConfig().apply {
            context = cxt
            appId = PlayZoneCenter.mAppId
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


    private fun getStartGameCollection(roomId: String): AUIMapCollection? {
        if (roomId.isEmpty()) {
            return null
        }
        val scene = mSyncManager.createScene(roomId)
        return scene.getCollection(kCollectionStartGameInfo) { a, b, c -> AUIMapCollection(a, b, c) }
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
        if (PlayZoneCenter.mRtmToken.isEmpty()) {
            PlayZoneLogger.d(TAG, "initRtmSync, renewToken start")
            PlayZoneCenter.generateRtmToken { rtmToken, exception ->
                val token = rtmToken ?: run {
                    PlayZoneLogger.e(TAG, "initRtmSync, $exception")
                    completion.invoke(AUIRtmException(-1, exception?.message ?: "error", ""))
                    return@generateRtmToken
                }
                mSyncManager.login(token, completion = {
                    if (it == null) {
                        completion.invoke(null)
                        PlayZoneLogger.d(TAG, "initRtmSync, with renewToken loginRtm success")
                    } else {
                        completion.invoke(it)
                        PlayZoneLogger.e(TAG, "initRtmSync, with renewToken loginRtm failed: $it")
                    }
                })
            }
        } else {
            mSyncManager.login(PlayZoneCenter.mRtmToken, completion = {
                if (it == null) {
                    completion.invoke(null)
                    PlayZoneLogger.d(TAG, "initRtmSync, without loginRtm success")
                } else {
                    completion.invoke(it)
                    PlayZoneLogger.e(TAG, "initRtmSync, without renewToken loginRtm failed: $it")
                }
            })
        }
    }


    override fun onAttributeChanged(channelName: String, key: String, value: Any) {
        if (key == kCollectionStartGameInfo) {
            val newValue = when (value) {
                is ByteArray -> String(value)
                is String -> value
                else -> ""
            }
            PlayZoneLogger.d(TAG, "onAttributeChanged $kCollectionStartGameInfo:$channelName,key:$key, value:$value")
            GsonTools.toBean(newValue, PlayStartGameInfo::class.java)?.let { startGameInfo ->
                mObservableHelper.notifyEventHandlers { delegate ->
                    delegate.onStartGameInfoDidChanged(startGameInfo)
                }
            }
        }
    }

    override fun onWillInitSceneMetadata(channelName: String): Map<String, Any>? {
        return super.onWillInitSceneMetadata(channelName)
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        PlayZoneLogger.d(TAG, "onTokenPrivilegeWillExpire, $channelName")
        PlayZoneCenter.generateRtmToken { rtmToken, exception ->
            val token = rtmToken ?: run {
                PlayZoneLogger.e(TAG, "onTokenPrivilegeWillExpire, with renewRtmToken failed: $exception")
                return@generateRtmToken
            }
            mSyncManager.login(token, completion = { rtmException ->
                if (rtmException == null) {
                    PlayZoneLogger.d(TAG, "onTokenPrivilegeWillExpire, with loginRtm success")
                } else {
                    PlayZoneLogger.e(TAG, "onTokenPrivilegeWillExpire, with loginRtm failed: $rtmException")
                }
            })
        }
    }

    override fun onSceneExpire(channelName: String) {
        PlayZoneLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            leaveRoom { }
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onRoomExpire()
            }
        }
    }

    override fun onSceneDestroy(channelName: String) {
        PlayZoneLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            leaveRoom { }
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onRoomDestroy()
            }
        }
    }

    override fun onSceneUserBeKicked(channelName: String, userId: String) {
        PlayZoneLogger.d(TAG, "onSceneUserBeKicked, channelName:$channelName, userId:$userId")
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        PlayZoneLogger.d(TAG, "onRoomUserSnapshot, roomId:$roomId, userList:${userList?.count()}")
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
        PlayZoneLogger.d(TAG, "onRoomUserEnter, roomId:$roomId, userInfo:$userInfo")
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
        mRoomManager.updateRoomInfo(PlayZoneCenter.mAppId, kSceneId, cacheRoom, callback = { auiException, roomInfo ->
            if (auiException == null) {
                PlayZoneLogger.d(TAG, "onRoomUserEnter updateRoom success: $roomInfo")
            } else {
                PlayZoneLogger.e(TAG, "onRoomUserEnter updateRoom failed: $mCurRoomNo $auiException")
            }
        })
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        PlayZoneLogger.d(TAG, "onRoomUserUpdate, roomId:$roomId, userInfo:$userInfo")
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        PlayZoneLogger.d(TAG, "onRoomUserLeave, roomId:$roomId, userInfo:$userInfo")
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
        mRoomManager.updateRoomInfo(PlayZoneCenter.mAppId, kSceneId, cacheRoom, callback = { auiException, roomInfo ->
            if (auiException == null) {
                PlayZoneLogger.d(TAG, "onRoomUserLeave updateRoom success: $roomId, $roomInfo")
            } else {
                PlayZoneLogger.d(TAG, "onRoomUserLeave updateRoom failed: $roomId $auiException")
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
        PlayZoneLogger.d(TAG, "getRoomList start")
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }
            mRoomService.getRoomList(PlayZoneCenter.mAppId, kSceneId, 0, 50,
                cleanClosure = { auiRoomInfo ->
                    return@getRoomList auiRoomInfo.roomOwner?.userId == PlayZoneCenter.mUser.id.toString()

                },
                completion = { uiException, ts, roomList ->
                    if (uiException == null) {
                        ts?.let { serverTs ->
                            restfulDiffTs = System.currentTimeMillis() - serverTs
                        }
                        val newRoomList = roomList?.sortedBy { -it.createTime } ?: emptyList()
                        PlayZoneLogger.d(TAG, "getRoomList success,serverTs:$ts roomCount:${newRoomList.size}")
                        runOnMainThread { completion.invoke(null, newRoomList) }
                    } else {
                        PlayZoneLogger.e(TAG, "getRoomList error, $uiException")
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
        PlayZoneLogger.d(TAG, "createRoom start")
        val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }
            PlayZoneCenter.generateRtcToken { rtcToken, exception ->
                // 创建房间提前获取 rtcToken
                val token = rtcToken ?: run {
                    PlayZoneLogger.e(TAG, "createRoom, with renewRtcToken failed: $exception")
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
                    this.customPayload[PlayZoneParameters.ROOM_USER_COUNT] = 1
                    this.customPayload[PlayZoneParameters.THUMBNAIL_ID] = getRandomThumbnailId(createAt)
                    this.customPayload[PlayZoneParameters.PASSWORD] = inputModel.password ?: ""
                    this.customPayload[PlayZoneParameters.IS_PRIVATE] = !inputModel.password.isNullOrEmpty()
                }
                val scene = mSyncManager.createScene(roomInfo.roomId)
                scene.bindRespDelegate(this)
                scene.userService.registerRespObserver(this)
                mSyncManager.rtmManager.subscribeAttribute(roomInfo.roomId, kCollectionStartGameInfo, this)
                mCurRoomNo = roomInfo.roomId
                mRoomService.createRoom(PlayZoneCenter.mAppId, kSceneId, roomInfo, completion = { rtmException, _ ->
                    if (rtmException == null) {
                        PlayZoneLogger.d(TAG, "createRoom success: $roomInfo")
                        mCurRoomNo = roomInfo.roomId
                        startTimer()
                        runOnMainThread {
                            completion.invoke(null, roomInfo)
                        }
                    } else {
                        mCurRoomNo = ""
                        PlayZoneLogger.e(TAG, "createRoom failed: $rtmException")
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
            PlayZoneCenter.generateRtcToken(callback = { rtcToken, exception ->
                // 进入房间提前获取 rtcToken
                val token = rtcToken ?: run {
                    PlayZoneLogger.e(TAG, "joinRoom, with renewRtcToken failed: $exception")
                    completion.invoke(exception)
                    return@generateRtcToken
                }
                val scene = mSyncManager.createScene(roomId)
                scene.bindRespDelegate(this)
                scene.userService.registerRespObserver(this)
                mSyncManager.rtmManager.subscribeAttribute(roomId, kCollectionStartGameInfo, this)
                mCurRoomNo = roomId
                mRoomService.enterRoom(PlayZoneCenter.mAppId, kSceneId, roomId, completion = { rtmException ->
                    if (rtmException == null) {
                        PlayZoneLogger.d(TAG, "enterRoom success: ${cacheRoom.roomId}")
                        mCurRoomNo = cacheRoom.roomId
                        runOnMainThread {
                            completion.invoke(null)
                        }
                    } else {
                        mCurRoomNo = ""
                        PlayZoneLogger.e(TAG, "enterRoom failed: $rtmException")
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
                mRoomService.leaveRoom(PlayZoneCenter.mAppId, kSceneId, mCurRoomNo)
            }
        }
        mUserList.clear()
        mCurRoomNo = ""
    }

    override fun getStartGame(roomId: String, completion: (error: Exception?, out: PlayStartGameInfo?) -> Unit) {
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}(${it.code})"), null)
                return@initRtmSync
            }
            val startGameCollection = getStartGameCollection(mCurRoomNo) ?: return@initRtmSync
            startGameCollection.getMetaData { error, metadata ->
                if (error != null) {
                    PlayZoneLogger.d(TAG, "getStartGame failed roomId:$roomId $error")
                    runOnMainThread {
                        completion.invoke(Exception(error.message), null)
                    }
                    return@getMetaData
                }
                try {
                    val startGameInfo =
                        GsonTools.toBean(GsonTools.beanToString(metadata), PlayStartGameInfo::class.java)
                    PlayZoneLogger.d(TAG, "getStartGame onSuccess roomId:$roomId $startGameInfo")
                    runOnMainThread {
                        completion.invoke(null, startGameInfo)
                    }
                } catch (e: Exception) {
                    PlayZoneLogger.d(TAG, "getStartGame onFail roomId:$roomId $e")
                    runOnMainThread {
                        completion.invoke(e, null)
                    }
                }
            }
        }
    }

    override fun updateStartGame(roomId: String, gameInfo: PlayStartGameInfo, completion: (error: Exception?) -> Unit) {
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}(${it.code})"))
                return@initRtmSync
            }
            val startGameCollection = getStartGameCollection(mCurRoomNo) ?: return@initRtmSync
            val map = GsonTools.beanToMap(gameInfo)
            startGameCollection.updateMetaData(roomId, map) {
                if (it != null) {
                    PlayZoneLogger.d(TAG, "updateStartGame failed roomId:$roomId $it")
                    runOnMainThread {
                        completion.invoke(Exception(it.message))
                    }
                } else {
                    runOnMainThread {
                        completion.invoke(null)
                    }
                    val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId) ?: return@updateMetaData
                    cacheRoom.customPayload[PlayZoneParameters.BADGE_TITLE] = gameInfo.gameName
                    mRoomManager.updateRoomInfo(
                        BuildConfig.AGORA_APP_ID,
                        kSceneId,
                        cacheRoom,
                        callback = { auiException, roomInfo ->
                            if (auiException == null) {
                                PlayZoneLogger.d(TAG, "updateStartGame updateRoom success, $roomInfo")
                            } else {
                                PlayZoneLogger.e(TAG, "updateStartGame updateRoom failed, $auiException")
                            }
                        })
                }
            }
        }
    }

    override fun subscribeListener(listener: PlayZoneServiceListenerProtocol) {
        mObservableHelper.subscribeEvent(listener)
    }

    override fun unsubscribeListener(listener: PlayZoneServiceListenerProtocol) {
        mObservableHelper.unSubscribeEvent(listener)
    }
}