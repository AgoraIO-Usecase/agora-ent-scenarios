package io.agora.scene.joy.service

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import com.google.gson.reflect.TypeToken
import io.agora.rtmsyncmanager.ISceneResponse
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
import io.agora.rtmsyncmanager.service.rtm.AUIRtmMessageRespObserver
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.GsonUtil
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
    private val errorHandler: ((Exception?) -> Unit)
) : JoyServiceProtocol, ISceneResponse, IAUIUserService.AUIUserRespObserver, AUIRtmMessageRespObserver {

    companion object {
        private const val TAG = "Joy_Service_LOG"
        private const val kSceneId = "scene_joy_4.10.1"

        private const val SYNC_SCENE_ROOM_START_GAME_COLLECTION = "startGameCollection"
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

    private val mRoomMap = mutableMapOf<String, AUIRoomInfo>()

    private var mUserList: MutableList<AUIUserInfo> = mutableListOf()

    private var mJoyServiceListener: JoyServiceListenerProtocol? = null

    @Volatile
    private var mCurrRoomNo: String = ""

    private val mUser: User
        get() = UserManager.getInstance().user

    private val mTimerRoomEndRun = Runnable {
        runOnMainThread {
            JoyLogger.d(TAG, "time up exit room!")
            val roomInfo = mRoomMap[mCurrRoomNo] ?: return@runOnMainThread
            mJoyServiceListener?.onRoomDidDestroy(roomInfo, false)
        }
    }

    private fun getRandomThumbnailId(crateAt: Long) =
        Random(crateAt).nextInt(0, 5).toString()

    init {
        HttpManager.setBaseURL(BuildConfig.ROOM_MANAGER_SERVER_HOST)
        AUILogger.initLogger(AUILogger.Config(cxt, "Joy"))

        val commonConfig = AUICommonConfig().apply {
            context = cxt
            appId = BuildConfig.AGORA_APP_ID
            owner = AUIUserThumbnailInfo().apply {
                userId = mUser.id.toString()
                userName = mUser.name
                userAvatar = mUser.headUrl
            }
            host = BuildConfig.TOOLBOX_SERVER_HOST
        }
        AUIRoomContext.shared().setCommonConfig(commonConfig)
        mSyncManager = SyncManager(cxt, JoyServiceManager.rtmClient, commonConfig)
    }

    override fun getRoomList(completion: (list: List<AUIRoomInfo>) -> Unit) {
        mRoomManager.getRoomInfoList(BuildConfig.AGORA_APP_ID, kSceneId, System.currentTimeMillis(), 20) { err, list ->
            if (err != null) {
                runOnMainThread {
                    completion.invoke(emptyList())
                }
            }
            if (list != null) {
                //按照创建时间顺序排序
                list.sortBy { it.customPayload[JoyParameters.CREATED_AT] as? Long }
                runOnMainThread { completion.invoke(list) }
            } else {
                runOnMainThread { completion.invoke(emptyList()) }
            }
        }
    }

    override fun createRoom(roomName: String, completion: (error: Exception?, out: AUIRoomInfo?) -> Unit) {
        JoyLogger.d(TAG, "createRoom start")
        val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
        val createdAt = TimeUtils.currentTimeMillis()
        val roomInfo = AUIRoomInfo().apply {
            this.roomId = roomId
            this.roomName = roomName
            this.roomOwner = AUIUserThumbnailInfo().apply {
                userId = mUser.id.toString()
                userName = mUser.name
                userAvatar = mUser.headUrl
            }
            this.customPayload[JoyParameters.ROOM_USER_COUNT] = 1
            this.customPayload[JoyParameters.THUMBNAIL_ID] = getRandomThumbnailId(createdAt)
            this.customPayload[JoyParameters.CREATED_AT] = createdAt
        }
        mRoomManager.createRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomInfo) { e, info ->
            if (info != null) {
                val scene = mSyncManager.getScene(roomInfo.roomId)
                scene.create(null) { er ->
                    if (er != null) {
                        Log.d(TAG, "enter scene fail: ${er.message}")
                        runOnMainThread {
                            completion.invoke(Exception(er.message), null)
                        }
                        return@create
                    }
                }
                mRoomMap[roomInfo.roomId] = roomInfo
                runOnMainThread {
                    completion.invoke(null, roomInfo)
                }
            }
            if (e != null) {
                runOnMainThread {
                    completion.invoke(Exception(e.message), null)
                }
            }
        }
    }

    override fun updateRoom(roomInfo: AUIRoomInfo, completion: (error: Exception?) -> Unit) {
        mRoomManager.updateRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomInfo) { error, roomInfo ->
            if (error==null){
                roomInfo?.let {
                    mRoomMap[it.roomId] = roomInfo
                }
            }
            runOnMainThread {
                completion.invoke(error)
            }
        }
    }

    override fun joinRoom(roomInfo: AUIRoomInfo, completion: (error: Exception?) -> Unit) {
        mCurrRoomNo = ""
        val scene = mSyncManager.getScene(roomInfo.roomId)
        scene.bindRespDelegate(this)
        scene.enter { _, e ->
            if (e != null) {
                JoyLogger.d(TAG, "enter scene fail: ${e.message}")
                runOnMainThread {
                    completion.invoke(Exception(e.message))
                }
            } else {
                JoyLogger.d(TAG, "enter scene success")
                mCurrRoomNo = roomInfo.roomId
                // 重置体验时间事件
                mMainHandler.removeCallbacks(mTimerRoomEndRun)
                val createTime = (roomInfo.customPayload[JoyParameters.CREATED_AT] as? Long) ?: 0
                // 定时删除房间
                val expireLeftTime =
                    JoyServiceProtocol.ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - createTime)
                JoyLogger.d(TAG, "expireLeftTime: $expireLeftTime")
                mMainHandler.postDelayed(mTimerRoomEndRun, expireLeftTime)
                runOnMainThread {
                    completion.invoke(null)
                }
            }
        }
        scene.userService.registerRespObserver(this)
        mSyncManager.rtmManager.subscribeMessage(this)
    }

    override fun leaveRoom(roomInfo: AUIRoomInfo, completion: (error: Exception?) -> Unit) {
        // 重置体验时间事件
        mMainHandler.removeCallbacks(mTimerRoomEndRun)
        val scene = mSyncManager.getScene(roomInfo.roomId)
        scene.unbindRespDelegate(this)
        mSyncManager.rtmManager.unsubscribeMessage(this)

        val createTime = (roomInfo.customPayload[JoyParameters.CREATED_AT] as? Long) ?: 0
        if (roomInfo.roomOwner?.userId == mUser.id.toString() ||
            TimeUtils.currentTimeMillis() - createTime >= JoyServiceProtocol.ROOM_AVAILABLE_DURATION
        ) {
            // 房主离开
            scene.delete()
            mRoomManager.destroyRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomInfo.roomId) { e ->
                if (e != null) {
                    JoyLogger.d(TAG, "enter destroyRoom fail: ${e.message}")
                    runOnMainThread {
                        completion.invoke(Exception(e.message))
                    }
                } else {
                    JoyLogger.d(TAG, "enter destroyRoom success")
                    innerReset(true)
                    runOnMainThread {
                        completion.invoke(null)
                    }
                }
            }
        } else {
            // 观众离开
            scene.leave()
            innerReset(false)
            runOnMainThread {
                completion.invoke(null)
            }
        }
    }

    override fun getStartGame(roomId: String, completion: (error: Exception?, out: JoyStartGameInfo?) -> Unit) {
        val scene = mSyncManager.getScene(roomId)
        val startGameCollection = scene.getCollection(SYNC_SCENE_ROOM_START_GAME_COLLECTION)
        { channelName, sceneKey, rtmManager ->
            AUIMapCollection(channelName, sceneKey, rtmManager)
        }
        startGameCollection.getMetaData { error, metadata ->
            if (error != null) {
                JoyLogger.d(TAG, "getStartGame onFail roomId:$roomId $error")
                runOnMainThread {
                    completion.invoke(Exception(error.message), null)
                }
                return@getMetaData
            }
            try {
                val out = GsonUtil.instance.fromJson(metadata.toString(), JoyStartGameInfo::class.java)
                JoyLogger.d(TAG, "getStartGame onSuccess roomId:$roomId $out")
                runOnMainThread {
                    completion.invoke(null, out)
                }
            } catch (e: Exception) {
                JoyLogger.d(TAG, "getStartGame onFail roomId:$roomId $error")
                runOnMainThread {
                    completion.invoke(e, null)
                }
            }
        }
    }

    override fun updateStartGame(roomId: String, gameInfo: JoyStartGameInfo, completion: (error: Exception?) -> Unit) {
        val scene = mSyncManager.getScene(roomId)
        val startGameCollection = scene.getCollection(SYNC_SCENE_ROOM_START_GAME_COLLECTION)
        { channelName, sceneKey, rtmManager ->
            AUIMapCollection(channelName, sceneKey, rtmManager)
        }
        val gson = GsonUtil.instance
        val map = gson.fromJson<Map<String, String>>(
            gson.toJson(gameInfo),
            object : TypeToken<HashMap<String, String>>() {}.type
        )
        startGameCollection.updateMetaData(roomId, map, null, null)
    }

    override fun sendChatMessage(roomId: String, message: String, completion: (error: Exception?) -> Unit) {
        val joyMessage = JoyMessage(
            userId = mUser.id.toString(),
            userName = mUser.name,
            message = message,
            createAt = TimeUtils.currentTimeMillis()
        )
        mSyncManager.rtmManager.publish(roomId, mUser.id.toString(), joyMessage.toString(), completion = {
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

    override fun subscribeListener(listener: JoyServiceListenerProtocol) {
        mJoyServiceListener = listener
    }

    override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
        runOnMainThread {
            GsonTools.toBean(message, JoyMessage::class.java)?.let {
                mJoyServiceListener?.onMessageDidAdded(it)
            }
        }
    }

    // ----------  ISceneResponse -----------------------
    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        JoyServiceManager.renewTokens { }
    }

    override fun onSceneDestroy(roomId: String) {
        mRoomManager.destroyRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomId) {
            innerReset(true)
        }
    }

    override fun onSceneUserBeKicked(roomId: String, userId: String) {
        // nothing
    }


    // -------- IAUIUserService.AUIUserRespObserver ----------
    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userList?.let {
            this.mUserList.clear()
            this.mUserList.addAll(it)
        }
    }

    private fun plusRoomUserCount(userCount: Any, roomInfo: AUIRoomInfo) {
        if (userCount is Int) {
            roomInfo.customPayload[JoyParameters.ROOM_USER_COUNT] = userCount + 1
        } else if (userCount is Long) {
            roomInfo.customPayload[JoyParameters.ROOM_USER_COUNT] = userCount.toInt() + 1
        }
    }

    private fun minusRoomUserCount(userCount: Any, roomInfo: AUIRoomInfo) {
        if (userCount is Int) {
            roomInfo.customPayload[JoyParameters.ROOM_USER_COUNT] = userCount - 1
        } else if (userCount is Long) {
            roomInfo.customPayload[JoyParameters.ROOM_USER_COUNT] = userCount.toInt() - 1
        }
    }

    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        JoyLogger.d(TAG, "onRoomUserEnter, roomId:$roomId, userInfo:$userInfo")
        mUserList.removeIf { it.userId == userInfo.userId }
        mUserList.add(userInfo)
        mJoyServiceListener?.onUserListDidChanged(mUserList)
        val roomInfo = mRoomMap[roomId] ?: return
        if (mUser.id.toString() == roomInfo.roomOwner?.userId) {
            roomInfo.customPayload[JoyParameters.ROOM_USER_COUNT]?.let { userCount->
                plusRoomUserCount(userCount,roomInfo)
                updateRoom(roomInfo, completion = {

                })
            }
        }
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        JoyLogger.d(TAG, "onRoomUserLeave, roomId:$roomId, userInfo:$userInfo")
        mUserList.removeIf { it.userId == userInfo.userId }
        mJoyServiceListener?.onUserListDidChanged(mUserList)
        val roomInfo = mRoomMap[roomId] ?: return
        if (mUser.id.toString() == roomInfo.roomOwner?.userId) {
            roomInfo.customPayload[JoyParameters.ROOM_USER_COUNT]?.let { userCount->
                minusRoomUserCount(userCount,roomInfo)
                updateRoom(roomInfo, completion = {

                })
            }
        }
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        JoyLogger.d(TAG, "onRoomUserUpdate, roomId:$roomId, userInfo:$userInfo")
        mUserList.removeIf { it.userId == userInfo.userId }
        mUserList.add(userInfo)
        mJoyServiceListener?.onUserListDidChanged(mUserList)
    }

    private fun innerReset(isRoomDestroy: Boolean) {
        mUserList.clear()
        if (isRoomDestroy) {
            mRoomMap.clear()
        }
        mCurrRoomNo = ""
    }
}