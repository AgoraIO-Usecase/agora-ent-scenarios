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
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.manager.UserManager
import io.agora.scene.pure1v1.Pure1v1Logger
import okhttp3.internal.wait

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */
class Pure1v1ServiceImp(
    private val context: Context,
    private val rtmClient: RtmClient,
    private var user: UserInfo? = null,
    private var onUserChanged: () -> Unit
): IAUIUserService.AUIUserRespObserver {

    private val tag = "1v1_Service_LOG"
//    private val kSceneId = "scene_1v1PrivateVideo_4.2.1"
    private val kRoomId = "pure421"
    @Volatile
    private var syncUtilsInited = false
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val syncManager: SyncManager

    private val scene: Scene

    private var userSnapshotList: List<AUIUserInfo?>? = null

    private var userList = emptyList<AUIRoomInfo>()

    init {
        HttpManager.setBaseURL(BuildConfig.ROOM_MANAGER_SERVER_HOST)
        AUILogger.initLogger(AUILogger.Config(context, "eCommerce"))

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
        scene = syncManager.getScene(kRoomId)

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
     * 拉取房间列表
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
     * 创建并加入一个房间
     */
    fun enterRoom(completion: (Error?) -> Unit) {
        //比较通过roomid，一个人可能会有不同的roomid，但是create scene通过uid，保证不同roomId会被覆盖，保证一个用户不会展示多个
        val containsUser = userList.any { it.roomId == user?.getRoomId() }
        val u = user
        if (u == null || containsUser) {
            completion(null)
            return
        }
        Handler(Looper.getMainLooper()).postDelayed({
            syncManager.rtmManager.subscribe(kRoomId) { error ->
                error?.let { e ->
                    Log.d(tag, "enter scene fail: ${e.message}")
                    runOnMainThread { completion.invoke(Error(e.message)) }
                    return@subscribe
                }
                runOnMainThread { completion.invoke(null) }
            }
        }, 100)
    }

    /*
     * 离开房间
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

    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        Log.d(tag, "onRoomUserLeave, roomId:$roomId, userInfo:$userInfo")
        onUserChanged.invoke()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        super.onRoomUserUpdate(roomId, userInfo)
        onUserChanged.invoke()
    }
}