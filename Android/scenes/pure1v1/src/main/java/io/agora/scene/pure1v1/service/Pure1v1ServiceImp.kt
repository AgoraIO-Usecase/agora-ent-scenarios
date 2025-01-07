package io.agora.scene.pure1v1.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.rtm.*
import io.agora.rtmsyncmanager.Scene
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.*
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.manager.UserManager

class Pure1v1ServiceImp(
    private val context: Context,
    private val rtmClient: RtmClient,
    private var user: UserInfo? = null,
    private var onUserChanged: () -> Unit
): IAUIUserService.AUIUserRespObserver {

    private val tag = "1v1_Service_LOG"
    private val kRoomId = "pure600"
    @Volatile
    private var syncUtilsInited = false
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val syncManager: SyncManager

    private val scene: Scene

    private var userSnapshotList: List<AUIUserInfo?>? = null

    private var userList = emptyList<AUIRoomInfo>()

    init {
        HttpManager.setBaseURL(ServerConfig.roomManagerUrl)
        AUILogger.initLogger(AUILogger.Config(context, "Pure"))

        val commonConfig = AUICommonConfig()
        commonConfig.context = context
        commonConfig.appId = BuildConfig.AGORA_APP_ID
        val owner = AUIUserThumbnailInfo()
        owner.userId = UserManager.getInstance().user.id.toString()
        owner.userName = UserManager.getInstance().user.name
        owner.userAvatar = UserManager.getInstance().user.headUrl
        commonConfig.owner = owner
        commonConfig.host = ServerConfig.roomManagerUrl
        AUIRoomContext.shared().setCommonConfig(commonConfig)
        syncManager = SyncManager(context, rtmClient, commonConfig)

        scene = syncManager.createScene(kRoomId)
        scene.userService.registerRespObserver(this)
        syncUtilsInited = true
    }

    fun reset() {
        if (syncUtilsInited) {
            syncUtilsInited = false
            syncManager.release()
        }
    }

    /*
     * Pull room list
     */
    fun getUserList(completion: (String?, List<UserInfo>) -> Unit) {
        syncManager.rtmManager.whoNow(kRoomId) { e, list ->
            if (e != null) {
                runOnMainThread { completion(e.message, listOf()) }
            } else {
                val ret = ArrayList<UserInfo>()
                list?.forEach { userMap ->
                    GsonTools.toBean(GsonTools.beanToString(userMap), AUIUserInfo::class.java)?.let {
                        ret.add(UserInfo(
                            userId = it.userId,
                            userName = it.userName,
                            avatar = it.userAvatar,
                            createdAt = 0
                        ))
                    }
                }
                runOnMainThread { completion.invoke(null, ret.toList()) }
            }
        }
    }

    /*
     * Create and join a room
     */
    fun enterRoom(completion: (Error?) -> Unit) {
        // Compare through roomId, a user may have different roomIds, but create scene through uid, ensuring that different roomIds will be overwritten, ensuring that a user will not be displayed multiple times
        val containsUser = userList.any { it.roomId == user?.getRoomId() }
        val u = user
        if (u == null || containsUser) {
            completion(null)
            return
        }
        syncManager.rtmManager.subscribe(kRoomId) { error ->
            error?.let { e ->
                Log.d(tag, "enter scene fail: ${e.message}")
                runOnMainThread { completion.invoke(Error(e.message)) }
                return@subscribe
            }
            scene.userService.setUserAttr {}
            runOnMainThread { completion.invoke(null) }
        }
    }

    /*
     * Leave room
     */
    fun leaveRoom(completion: (Error?) -> Unit) {
        scene.leave()
        scene.delete()
    }

    // --------------------- inner ---------------------

    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }

    // -------- IAUIUserService.AUIUserRespObserver ----------
    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        userSnapshotList = userList
    }

    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        Log.d(tag, "onRoomUserEnter, roomId:$roomId, userInfo:$userInfo")
        onUserChanged.invoke()
    }

    override fun onRoomUserLeave(
        roomId: String,
        userInfo: AUIUserInfo,
        reason: AUIRtmUserLeaveReason
    ) {
        Log.d(tag, "onRoomUserLeave, roomId:$roomId, userInfo:$userInfo")
        onUserChanged.invoke()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        super.onRoomUserUpdate(roomId, userInfo)
        onUserChanged.invoke()
    }
}