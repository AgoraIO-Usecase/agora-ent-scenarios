package io.agora.scene.showTo1v1.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.rtm.*
import io.agora.rtmsyncmanager.ISceneResponse
import io.agora.rtmsyncmanager.RoomExpirationPolicy
import io.agora.rtmsyncmanager.RoomService
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.*
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.showTo1v1.ShowTo1v1Logger
import kotlin.random.Random

class ShowTo1v1ServiceImpl constructor(
    context: Context,
    private val rtmClient: RtmClient,
    private val user: ShowTo1v1UserInfo,
) : ShowTo1v1ServiceProtocol, ISceneResponse, IAUIUserService.AUIUserRespObserver {

    companion object {
        private const val TAG = "Show1v1_LOG"
    }

    private val kSceneId = "scene_Livetoprivate_500"
    @Volatile
    private var syncUtilsInited = false

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val syncManager: SyncManager

    private val roomManager = AUIRoomManager()

    private lateinit var roomService: RoomService

    private var roomList = emptyList<AUIRoomInfo>()

    private var userList: List<AUIUserInfo> = emptyList()

    private var listener: ShowTo1v1ServiceListenerProtocol? = null

    private var isConnected = false

    private var needDestroy = ""

    init {
        HttpManager.setBaseURL(BuildConfig.ROOM_MANAGER_SERVER_HOST)
        AUILogger.initLogger(AUILogger.Config(context, "showTo1v1"))

        val commonConfig = AUICommonConfig()
        commonConfig.context = context
        commonConfig.appId = BuildConfig.AGORA_APP_ID
        val owner = AUIUserThumbnailInfo()
        owner.userId = UserManager.getInstance().user.id.toString()
        owner.userName = UserManager.getInstance().user.name
        owner.userAvatar = UserManager.getInstance().user.headUrl
        commonConfig.owner = owner
        commonConfig.host = BuildConfig.TOOLBOX_SERVER_HOST
        AUIRoomContext.shared().setCommonConfig(commonConfig)
        syncManager = SyncManager(context, rtmClient, commonConfig)

        val roomExpirationPolicy = RoomExpirationPolicy()
        roomExpirationPolicy.expirationTime = ROOM_AVAILABLE_DURATION
        roomService = RoomService(roomExpirationPolicy, roomManager, syncManager)
        rtmClient.addEventListener(object :RtmEventListener {
            override fun onConnectionStateChanged(
                channelName: String?,
                state: RtmConstants.RtmConnectionState?,
                reason: RtmConstants.RtmConnectionChangeReason?
            ) {
                super.onConnectionStateChanged(channelName, state, reason)
                if (state == RtmConstants.RtmConnectionState.CONNECTED) {
                    isConnected = true
                    if (needDestroy != "") {
                        val scene = syncManager.createScene(needDestroy)
                        scene.delete()
                        needDestroy = ""
                    }
                } else if (state == RtmConstants.RtmConnectionState.DISCONNECTED) {
                    isConnected = false
                }
            }
        })
    }

    override fun reset() {
        if (syncUtilsInited) {
            syncUtilsInited = false
        }
    }

    override fun createRoom(
        roomName: String,
        completion: (error: Exception?, roomInfo: ShowTo1v1RoomInfo?) -> Unit
    ) {
        ShowTo1v1Logger.d(TAG, "createRoom start, roomName:$roomName")
        val roomInfo = AUIRoomInfo()
        roomInfo.roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
        roomInfo.roomName = roomName
        val owner = AUIUserThumbnailInfo()
        owner.userId = UserManager.getInstance().user.id.toString()
        owner.userName = UserManager.getInstance().user.name
        owner.userAvatar = UserManager.getInstance().user.headUrl
        roomInfo.roomOwner = owner
        roomInfo.createTime = TimeUtils.currentTimeMillis()

        roomService.createRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomInfo) { e, info ->
            if (info != null) {
                completion.invoke(null, ShowTo1v1RoomInfo(
                    roomId = roomInfo.roomId,
                    roomName = roomInfo.roomName,
                    userId = owner.userId,
                    userName = owner.userName,
                    avatar = owner.userAvatar,
                    createdAt = roomInfo.createTime
                ))
            }
            if (e != null) {
                ShowTo1v1Logger.e(TAG, e,"createRoom failed，roomId:${roomInfo.roomId}")
                completion.invoke(Exception(e.message), null)
            }
        }
    }

    override fun joinRoom(roomInfo: ShowTo1v1RoomInfo, completion: (error: Exception?) -> Unit) {
        ShowTo1v1Logger.d(TAG, "joinRoom start，roomId:${roomInfo.roomId}")

        if (roomInfo.userId != UserManager.getInstance().user.id.toString()) {
            roomService.enterRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomInfo.roomId) { e ->
                if (e != null) {
                    ShowTo1v1Logger.e(TAG, e,"joinRoom failed，roomId:${roomInfo.roomId}")
                    completion.invoke(Exception(e.message))
                } else {
                    ShowTo1v1Logger.d(TAG, "joinRoom end，roomId:${roomInfo.roomId}")
                    completion.invoke(null)
                }
            }
        } else {
            completion.invoke(null)
        }

        val scene = syncManager.createScene(roomInfo.roomId)
        scene.userService.registerRespObserver(this)
    }

    override fun leaveRoom(roomInfo: ShowTo1v1RoomInfo, completion: (error: Exception?) -> Unit) {
        ShowTo1v1Logger.d(TAG, "leaveRoom start ${roomInfo.roomId}")
        val scene = syncManager.createScene(roomInfo.roomId)
        scene.userService.unRegisterRespObserver(this)
        roomService.leaveRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomInfo.roomId)
    }

    /*
     * 拉取房间列表
     */
    override fun getRoomList(completion: (error: Exception?, roomList: List<ShowTo1v1RoomInfo>) -> Unit) {
        ShowTo1v1Logger.d(TAG, "getRoomList start")
        roomService.getRoomList(BuildConfig.AGORA_APP_ID, kSceneId, System.currentTimeMillis(), 20, cleanClosure = {
            if (!isConnected) needDestroy = it.roomId
            isConnected && it.roomOwner?.userId == UserManager.getInstance().user.id.toString()
        }) { error, ts, list ->
            if (error != null) {
                ShowTo1v1Logger.e(TAG, error, "getRoomList failed")
                runOnMainThread { completion.invoke(error, ArrayList<ShowTo1v1RoomInfo>().toList()) }
            }
            if (list != null && ts != null) {
                val ret = ArrayList<ShowTo1v1RoomInfo>()
                list.forEach {
                    if (it.roomOwner?.userId != UserManager.getInstance().user.id.toString()) {
                        ret.add(ShowTo1v1RoomInfo(
                            roomId = it.roomId,
                            roomName = it.roomName,
                            userId = it.roomOwner!!.userId,
                            userName = it.roomOwner!!.userName,
                            avatar = it.roomOwner!!.userAvatar,
                            createdAt = it.createTime
                        ))
                    }
                }
                //按照创建时间顺序排序
                ret.sortBy { it.createdAt }
                ShowTo1v1Logger.d(TAG, "getRoomList end, roomCount:${ret.size}")
                runOnMainThread { completion.invoke(null, ret.toList()) }
            }
        }
    }

    override fun subscribeListener(listener: ShowTo1v1ServiceListenerProtocol) {
        this.listener = listener
    }

    // --------------------- inner ---------------------

    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }

    override fun onSceneDestroy(roomId: String) {
        listener?.onRoomDidDestroy(roomId)
        roomManager.destroyRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomId) {
            if (it!=null){
                ShowTo1v1Logger.e(TAG, it,"destroyRoom failed，roomId:$roomId")
            }else{
                ShowTo1v1Logger.d(TAG, "destroyRoom, roomId:$roomId")
            }
        }
    }

    // -------- IAUIUserService.AUIUserRespObserver ----------
    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userList?.let {
            this.userList = it
        }
    }

    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        ShowTo1v1Logger.d(TAG, "onRoomUserEnter, roomId:$roomId, userInfo:$userInfo")
        listener?.onUserListDidChanged(userList.size)
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        ShowTo1v1Logger.d(TAG, "onRoomUserLeave, roomId:$roomId, userInfo:$userInfo")
        listener?.onUserListDidChanged(userList.size)
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        super.onRoomUserUpdate(roomId, userInfo)
        listener?.onUserListDidChanged(userList.size)
    }
}