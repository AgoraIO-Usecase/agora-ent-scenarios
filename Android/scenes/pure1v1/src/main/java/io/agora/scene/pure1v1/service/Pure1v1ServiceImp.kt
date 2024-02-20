package io.agora.scene.pure1v1.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.auikit.service.http.CommonResp
import io.agora.rtm.*
import io.agora.rtmsyncmanager.ISceneResponse
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUICommonConfig
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.http.token.TokenGenerateReq
import io.agora.rtmsyncmanager.service.http.token.TokenGenerateResp
import io.agora.rtmsyncmanager.service.http.token.TokenInterface
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.pure1v1.Pure1v1Logger
import retrofit2.Response

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
    private var user: UserInfo? = null
) : ISceneResponse {

    private val tag = "1v1_Service_LOG"
    private val kSceneId = "scene_1v1PrivateVideo_4.2.1"
    @Volatile
    private var syncUtilsInited = false
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val syncManager: SyncManager

    private val roomManager = AUIRoomManager()

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
    }

    fun reset() {
        if (syncUtilsInited) {
            syncUtilsInited = false
        }
    }

    /*
     * 拉取房间列表
     */
    fun getUserList(completion: (String?, List<UserInfo>) -> Unit) {
        roomManager.getRoomInfoList(BuildConfig.AGORA_APP_ID, kSceneId, System.currentTimeMillis(), 20) { error, list ->
            if (error != null) {
                runOnMainThread { completion.invoke(null, ArrayList<UserInfo>().toList()) }
            }
            if (list != null) {
                val ret = ArrayList<UserInfo>()
                list.forEach {
                    ret.add(UserInfo(
                        userId = it.owner!!.userId,
                        userName = it.owner!!.userName,
                        avatar = it.owner!!.userAvatar,
                        createdAt = it.createTime
                    ))
                }
                //按照创建时间顺序排序
                ret.sortBy { it.createdAt }
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
        Pure1v1Logger.d(tag, "createUser start")
        val roomInfo = AUIRoomInfo()
        roomInfo.roomId = u.getRoomId()
        roomInfo.roomName = u.getCallChannelId()
        val owner = AUIUserThumbnailInfo()
        owner.userId = u.userId
        owner.userName = u.userName
        owner.userAvatar = u.avatar
        roomInfo.owner = owner
        roomInfo.thumbnail = u.avatar
        roomInfo.createTime = TimeUtils.currentTimeMillis()
        roomManager.createRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomInfo) { e, info ->
            if (info != null) {
                val temp = mutableListOf<AUIRoomInfo>()
                temp.add(roomInfo)
                temp.addAll(userList)
                userList = temp

                val scene = syncManager.getScene(roomInfo.roomId)
                scene.bindRespDelegate(this)
                scene.create(null) { er ->
                    if (er != null) {
                        Log.d(tag, "enter scene fail: ${er.message}")
                        completion.invoke(Error(er.message))
                        return@create
                    }
                    scene.enter { payload, e ->
                        if (e != null) {
                            Log.d(tag, "enter scene fail: ${e.message}")
                            completion.invoke(Error(e.message))
                        } else {
                            completion.invoke(null)
                        }
                    }
                }

                completion.invoke(null)
            }
            if (e != null) {
                completion.invoke(java.lang.Error(e.message))
            }
        }
    }

    /*
     * 离开房间
     */
    fun leaveRoom(completion: (Error?) -> Unit) {
        val u = user ?: return
        val scene = syncManager.getScene(u.getRoomId())
        scene.unbindRespDelegate(this)
        scene.delete()
        roomManager.destroyRoom(BuildConfig.AGORA_APP_ID, kSceneId, u.getRoomId()) { e ->

        }
    }

    // --------------------- inner ---------------------

    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }

    private fun createRtmClient(): RtmClient {
        val commonConfig = AUIRoomContext.shared().requireCommonConfig()
        val userInfo = AUIRoomContext.shared().currentUserInfo
        val rtmConfig = RtmConfig.Builder(commonConfig.appId, userInfo.userId).apply {
            presenceTimeout(60)
        }.build()
        if (rtmConfig.appId.isEmpty()) {
            assert(false) { "userId is empty" }
        }
        if (rtmConfig.userId.isEmpty()) {
            assert(false) { "appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'" }
        }
        return RtmClient.create(rtmConfig)
    }

    override fun onSceneDestroy(roomId: String) {
        leaveRoom {}
        roomManager.destroyRoom(BuildConfig.AGORA_APP_ID, kSceneId, roomId) {
        }
    }
}