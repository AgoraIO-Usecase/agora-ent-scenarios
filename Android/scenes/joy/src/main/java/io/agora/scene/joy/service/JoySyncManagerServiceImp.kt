package io.agora.scene.joy.service

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
import io.agora.rtmsyncmanager.service.rtm.AUIRtmMessageRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.joy.JoyLogger
import io.agora.scene.joy.JoyServiceManager
import kotlin.random.Random

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */
class JoySyncManagerServiceImp constructor(
    private val cxt: Context,
) : JoyServiceProtocol, ISceneResponse, IAUIUserService.AUIUserRespObserver,
    AUIRtmMessageRespObserver, AUIRtmAttributeRespObserver {

    companion object {
        private const val TAG = "Joy_Service_LOG"
        private const val kSceneId = "scene_joy_5.0.0"

        private const val kCollectionStartGameInfo = "startGameCollection"
    }

    private val mMainHandler by lazy { Handler(Looper.getMainLooper()) }

    private fun runOnMainThread(runnable: Runnable) {
        if (Thread.currentThread() == mMainHandler.looper.thread) {
            runnable.run()
        } else {
            mMainHandler.post(runnable)
        }
    }

    private val mSyncManager: SyncManager

    private val mRoomManager = AUIRoomManager()

    private lateinit var roomService: RoomService

    private var mUserList: MutableList<AUIUserInfo> = mutableListOf()

    private var mJoyServiceListener: JoyServiceListenerProtocol? = null

    @Volatile
    private var mCurrRoomNo: String = ""

    private val mUser: User get() = UserManager.getInstance().user


    private fun getRandomThumbnailId(crateAt: Long) =
        Random(crateAt).nextInt(0, 5).toString()

    private fun getStartGameCollection(roomId: String): AUIMapCollection? {
        if (roomId.isEmpty()) {
            return null
        }
        val scene = mSyncManager.getScene(roomId)
        return scene?.getCollection(kCollectionStartGameInfo) { a, b, c -> AUIMapCollection(a, b, c) }
    }

    init {
        HttpManager.setBaseURL(ServerConfig.roomManagerUrl)
        val rtmSyncTag = "JOY_RTM_LOG"
        AUILogger.initLogger(AUILogger.Config(cxt, "Joy", logCallback = object :AUILogger.AUILogCallback{
            override fun onLogDebug(tag: String, message: String) {
                JoyLogger.d(rtmSyncTag, "$tag $message")
            }

            override fun onLogInfo(tag: String, message: String) {
                JoyLogger.d(rtmSyncTag, "$tag $message")
            }

            override fun onLogWarning(tag: String, message: String) {
                JoyLogger.w(rtmSyncTag, "$tag $message")
            }

            override fun onLogError(tag: String, message: String) {
                JoyLogger.e(rtmSyncTag, "$tag $message")
            }
        }))

        val commonConfig = AUICommonConfig().apply {
            context = cxt
            appId = BuildConfig.AGORA_APP_ID
            owner = AUIUserThumbnailInfo().apply {
                userId = mUser.id.toString()
                userName = mUser.name
                userAvatar = mUser.headUrl
            }
            host = ServerConfig.toolBoxUrl
        }
        AUIRoomContext.shared().setCommonConfig(commonConfig)
        mSyncManager = SyncManager(cxt, null, commonConfig)
        val roomExpirationPolicy = RoomExpirationPolicy()
        roomExpirationPolicy.expirationTime = JoyServiceProtocol.ROOM_AVAILABLE_DURATION
        roomService = RoomService(roomExpirationPolicy, mRoomManager, mSyncManager)
    }

    private fun startTimer() {
        mMainHandler.removeCallbacks(timerRoomCountDownTask)
        mMainHandler.postDelayed(timerRoomCountDownTask, 1000)
    }

    private val timerRoomCountDownTask = object : Runnable {
        override fun run() {
            if (mCurrRoomNo.isEmpty()) return
            val roomDuration = getCurrentRoomDuration(mCurrRoomNo)
            if (roomDuration == -1L || roomDuration >= JoyServiceProtocol.ROOM_AVAILABLE_DURATION) {
                mMainHandler.removeCallbacks(this)
                onSceneExpire(mCurrRoomNo)
            } else {
                mMainHandler.postDelayed(this, 1000)
            }
        }
    }

    // 本地时间与restful 服务端差值
    private var rsetfulDiffTs: Long = 0

    override fun getRoomList(completion: (error: Exception?, roomList: List<AUIRoomInfo>?) -> Unit) {
        JoyLogger.d(TAG, "getRoomList start")
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}(${it.code})"), null)
                return@initRtmSync
            }
            roomService.getRoomList(
                BuildConfig.AGORA_APP_ID, kSceneId, 0, 20,
                cleanClosure = { auiRoomInfo ->
                    return@getRoomList auiRoomInfo.roomOwner?.userId == mUser.id.toString()

                },
                completion = { uiException, ts, roomList ->
                    if (uiException == null) {
                        ts?.let { serverTs ->
                            rsetfulDiffTs = System.currentTimeMillis() - serverTs
                        }
                        val newRoomList = roomList?.sortedBy { it.createTime } ?: emptyList()
                        JoyLogger.d(TAG, "getRoomList success, roomCount:${newRoomList.size}")
                        runOnMainThread { completion.invoke(null, newRoomList) }
                    } else {
                        JoyLogger.e(TAG, "getRoomList error, $uiException")
                        runOnMainThread {
                            completion.invoke(uiException, null)
                        }
                    }
                })
        }
    }

    override fun createRoom(roomName: String, completion: (error: Exception?, out: AUIRoomInfo?) -> Unit) {
        JoyLogger.d(TAG, "createRoom start,roomName:$roomName")
        val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}(${it.code})"), null)
                return@initRtmSync
            }
            val createdAt = System.currentTimeMillis() - rsetfulDiffTs
            val roomInfo = AUIRoomInfo().apply {
                this.roomId = roomId
                this.roomName = roomName
                this.roomOwner = AUIUserThumbnailInfo().apply {
                    userId = mUser.id.toString()
                    userName = mUser.name
                    userAvatar = mUser.headUrl
                }
                this.createTime = createdAt
                this.customPayload[JoyParameters.ROOM_USER_COUNT] = 1
                this.customPayload[JoyParameters.THUMBNAIL_ID] = getRandomThumbnailId(createdAt)
            }
            val scene = mSyncManager.createScene(roomInfo.roomId)
            scene.bindRespDelegate(this)
            scene.userService.registerRespObserver(this)
            mSyncManager.rtmManager.subscribeMessage(this)
            mSyncManager.rtmManager.subscribeAttribute(roomInfo.roomId, kCollectionStartGameInfo, this)
            mCurrRoomNo = roomInfo.roomId
            roomService.createRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomInfo) { rtmException, info ->
                if (rtmException == null) {
                    JoyLogger.d(TAG, "createRoom success: $roomInfo")
                    mCurrRoomNo = roomInfo.roomId
                    startTimer()
                    runOnMainThread {
                        completion.invoke(null, roomInfo)
                    }
                } else {
                    mCurrRoomNo = ""
                    JoyLogger.e(TAG, "createRoom failed: $rtmException")
                    runOnMainThread {
                        completion.invoke(Exception("${rtmException.message}(${rtmException.code})"), null)
                    }
                }
            }
        }
    }

    override fun joinRoom(roomId: String, completion: (error: Exception?) -> Unit) {
        JoyLogger.d(TAG, "joinRoom start $roomId")
        if (mCurrRoomNo.isNotEmpty()) {
            completion.invoke(Exception("already join room $mCurrRoomNo!"))
            return
        }
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId)
        if (cacheRoom == null) {
            completion.invoke(Exception("room $mCurrRoomNo null!"))
            return
        }
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}(${it.code})"))
                return@initRtmSync
            }
            val scene = mSyncManager.createScene(roomId)
            scene.bindRespDelegate(this)
            scene.userService.registerRespObserver(this)
            mSyncManager.rtmManager.subscribeMessage(this)
            mSyncManager.rtmManager.subscribeAttribute(roomId, kCollectionStartGameInfo, this)
            mCurrRoomNo = roomId
            roomService.enterRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomId, completion = { rtmException ->
                if (rtmException == null) {
                    JoyLogger.d(TAG, "enterRoom success: ${cacheRoom.roomId}")
                    mCurrRoomNo = cacheRoom.roomId
                    runOnMainThread {
                        completion.invoke(null)
                    }
                } else {
                    mCurrRoomNo = ""
                    JoyLogger.e(TAG, "enterRoom failed: $rtmException")
                    runOnMainThread {
                        completion.invoke(Exception("${rtmException.message}(${rtmException.code})"))
                    }
                }
            })
        }

    }

    override fun leaveRoom(completion: (error: Exception?) -> Unit) {
        JoyLogger.d(TAG, "leaveRoom start $mCurrRoomNo")
        val scene = mSyncManager.getScene(mCurrRoomNo)
        scene?.userService?.unRegisterRespObserver(this)
        scene?.unbindRespDelegate(this)

        if (AUIRoomContext.shared().isRoomOwner(mCurrRoomNo)) {
            mMainHandler.removeCallbacks(timerRoomCountDownTask)
        }
        mSyncManager.rtmManager.unsubscribeMessage(this)
        mSyncManager.rtmManager.unsubscribeAttribute(mCurrRoomNo, kCollectionStartGameInfo, this)

        roomService.leaveRoom(BuildConfig.AGORA_APP_ID, kSceneId, mCurrRoomNo)

        mUserList.clear()
        mCurrRoomNo = ""
        completion.invoke(null)
    }

    override fun getCurrentRoomDuration(roomId: String): Long {
        if (roomId.isEmpty()) {
            return 0
        }
        val scene = mSyncManager.getScene(roomId)
        return scene?.getRoomDuration() ?: 0L
    }

    override fun getStartGame(
        roomId: String, completion: (error: Exception?, startGameInfo: JoyStartGameInfo?) -> Unit
    ) {
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}(${it.code})"), null)
                return@initRtmSync
            }
            val startGameCollection = getStartGameCollection(mCurrRoomNo) ?: return@initRtmSync
            startGameCollection.getMetaData { error, metadata ->
                if (error != null) {
                    JoyLogger.d(TAG, "getStartGame failed roomId:$roomId $error")
                    runOnMainThread {
                        completion.invoke(Exception(error.message), null)
                    }
                    return@getMetaData
                }
                try {
                    val startGameInfo =
                        GsonTools.toBean(GsonTools.beanToString(metadata), JoyStartGameInfo::class.java)
                    JoyLogger.d(TAG, "getStartGame onSuccess roomId:$roomId $startGameInfo")
                    runOnMainThread {
                        completion.invoke(null, startGameInfo)
                    }
                } catch (e: Exception) {
                    JoyLogger.d(TAG, "getStartGame onFail roomId:$roomId $e")
                    runOnMainThread {
                        completion.invoke(e, null)
                    }
                }
            }
        }
    }

    override fun updateStartGame(roomId: String, gameInfo: JoyStartGameInfo, completion: (error: Exception?) -> Unit) {
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}(${it.code})"))
                return@initRtmSync
            }
            val startGameCollection = getStartGameCollection(mCurrRoomNo) ?: return@initRtmSync
            val map = GsonTools.beanToMap(gameInfo)
            startGameCollection.updateMetaData(roomId, map) {
                if (it != null) {
                    JoyLogger.d(TAG, "updateStartGame failed roomId:$roomId $it")
                    runOnMainThread {
                        completion.invoke(Exception(it.message))
                    }
                } else {
                    runOnMainThread {
                        completion.invoke(null)
                    }
                    val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId) ?: return@updateMetaData
                    cacheRoom.customPayload[JoyParameters.BADGE_TITLE] = gameInfo.gameName
                    mRoomManager.updateRoomInfo(
                        BuildConfig.AGORA_APP_ID,
                        kSceneId,
                        cacheRoom,
                        callback = { auiException, roomInfo ->
                            if (auiException == null) {
                                JoyLogger.d(TAG, "updateStartGame updateRoom success, $roomInfo")
                            } else {
                                JoyLogger.e(TAG, "updateStartGame updateRoom failed, $auiException")
                            }
                        })
                }
            }
        }
    }

    override fun sendChatMessage(roomId: String, message: String, completion: (error: Exception?) -> Unit) {
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}(${it.code})"))
                return@initRtmSync
            }
            val joyMessage = JoyMessage(
                userId = mUser.id.toString(),
                userName = mUser.name,
                message = message,
                createAt = TimeUtils.currentTimeMillis()
            )
            mSyncManager.rtmManager.publish(roomId, "", GsonTools.beanToString(joyMessage) ?: "", completion = {
                if (it == null) {
                    JoyLogger.d(TAG, "sendChatMessage onSuccess roomId:$roomId")
                    runOnMainThread {
                        mJoyServiceListener?.onMessageDidAdded(joyMessage)
                        completion.invoke(null)
                    }
                } else {
                    JoyLogger.e(TAG, "sendChatMessage onFail roomId:$roomId $it")
                    runOnMainThread {
                        completion.invoke(Exception(it.message))
                    }
                }
            })
        }
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

    override fun subscribeListener(listener: JoyServiceListenerProtocol) {
        mJoyServiceListener = listener
    }

    override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
        JoyLogger.d(
            TAG,
            "onMessageReceive channelName:$channelName,publisherId:$publisherId,message:$message"
        )
        GsonTools.toBean(message, JoyMessage::class.java)?.let { joyMessage ->
            runOnMainThread {
                mJoyServiceListener?.onMessageDidAdded(joyMessage)
            }
        }
    }

    override fun onAttributeChanged(channelName: String, key: String, value: Any) {
        if (key == kCollectionStartGameInfo) {
            val newValue = when (value) {
                is ByteArray -> String(value)
                is String -> value
                else -> ""
            }
            JoyLogger.d(
                TAG,
                "onAttributeChanged $kCollectionStartGameInfo:$channelName,key:$key, value:$value"
            )
            GsonTools.toBean(newValue, JoyStartGameInfo::class.java)?.let { startGameInfo ->
                runOnMainThread {
                    mJoyServiceListener?.onStartGameInfoDidChanged(startGameInfo)
                }
            }
        }

    }

    // ----------  ISceneResponse -----------------------
    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        JoyLogger.d(TAG, "onTokenPrivilegeWillExpire, channelName:$channelName")
        JoyServiceManager.generateToken { rtmToken, exception ->
            val token = rtmToken ?: run {
                JoyLogger.e(TAG, "onTokenPrivilegeWillExpire, with renewRtmToken failed: $exception")
                return@generateToken
            }
            mSyncManager.login(token, completion = { rtmException ->
                if (rtmException == null) {
                    JoyLogger.d(TAG, "onTokenPrivilegeWillExpire, with loginRtm success")
                } else {
                    JoyLogger.e(TAG, "onTokenPrivilegeWillExpire, with loginRtm failed: $rtmException")
                }
            })
        }
    }

    override fun onSceneDestroy(roomId: String) {
        JoyLogger.d(TAG, "onSceneDestroy, roomId:$roomId")
        if (mCurrRoomNo == roomId) {
            leaveRoom { }
            mJoyServiceListener?.onRoomDestroy()
        }
    }

    override fun onSceneExpire(channelName: String) {
        if (mCurrRoomNo == channelName) {
            leaveRoom { }
            mJoyServiceListener?.onRoomExpire()
        }
    }

    override fun onSceneUserBeKicked(roomId: String, userId: String) {
        JoyLogger.d(TAG, "onSceneUserBeKicked, roomId:$roomId,userId:$userId")
    }


    // -------- IAUIUserService.AUIUserRespObserver ----------
    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        JoyLogger.d(TAG, "onRoomUserSnapshot, roomId:$roomId, userList:${userList?.count()}")
        userList?.let {
            this.mUserList.clear()
            this.mUserList.addAll(it)
        }
    }

    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        JoyLogger.d(TAG, "onRoomUserEnter, roomId:$roomId, userInfo:$userInfo")
        if (mCurrRoomNo != roomId) {
            return
        }
        mUserList.removeIf { it.userId == userInfo.userId }
        mUserList.add(userInfo)
        mJoyServiceListener?.onUserListDidChanged(mUserList)
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId) ?: return
        // 所有人都可修改用户数
        cacheRoom.customPayload[JoyParameters.ROOM_USER_COUNT] = mUserList.count()
        mRoomManager.updateRoomInfo(
            BuildConfig.AGORA_APP_ID,
            kSceneId,
            cacheRoom,
            callback = { auiException, roomInfo ->
                if (auiException == null) {
                    JoyLogger.d(TAG, "onRoomUserEnter updateRoom success: $mCurrRoomNo, $roomInfo")
                } else {
                    JoyLogger.e(TAG, "onRoomUserEnter updateRoom failed: $mCurrRoomNo $auiException")
                }
            })
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        JoyLogger.d(TAG, "onRoomUserLeave, roomId:$roomId, userInfo:$userInfo")
        if (mCurrRoomNo != roomId) {
            return
        }
        mUserList.removeIf { it.userId == userInfo.userId }
        mJoyServiceListener?.onUserListDidChanged(mUserList)
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId) ?: return
        // 所有人都可修改用户数
        cacheRoom.customPayload[JoyParameters.ROOM_USER_COUNT] = mUserList.count()
        mRoomManager.updateRoomInfo(
            BuildConfig.AGORA_APP_ID,
            kSceneId,
            cacheRoom,
            callback = { auiException, roomInfo ->
                if (auiException == null) {
                    JoyLogger.d(TAG, "onRoomUserLeave updateRoom success: $mCurrRoomNo, $roomInfo")
                } else {
                    JoyLogger.e(TAG, "onRoomUserLeave updateRoom failed: $mCurrRoomNo $auiException")
                }
            })
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        JoyLogger.d(TAG, "onRoomUserUpdate, roomId:$roomId, userInfo:$userInfo")
    }

    private fun initRtmSync(completion: (exception: AUIRtmException?) -> Unit) {
        if (mSyncManager.rtmManager.isLogin) {
            completion.invoke(null)
            return
        }
        if (JoyServiceManager.mRtmToken.isEmpty()) {
            JoyLogger.d(TAG, "initRtmSync, renewTokens start")
            JoyServiceManager.generateToken { rtmToken, exception ->
                val token = rtmToken ?: return@generateToken
                mSyncManager.login(token, completion = {
                    if (it == null) {
                        completion.invoke(null)
                    } else {
                        completion.invoke(it)
                        JoyLogger.d(TAG, "initRtmSync, with renewToken loginRtm failed: $it")
                    }
                })
            }
        } else {
            mSyncManager.login(JoyServiceManager.mRtmToken, completion = {
                if (it == null) {
                    completion.invoke(it)
                } else {
                    completion.invoke(it)
                    JoyLogger.d(TAG, "initRtmSync, without renewToken loginRtm failed: $it")
                }
            })
        }
    }

    fun destroy() {
        JoyLogger.d(TAG, message = "destroy")
        mSyncManager.logout()
        mSyncManager.release()
    }
}