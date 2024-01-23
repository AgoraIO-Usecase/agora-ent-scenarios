package io.agora.scene.pure1v1.service

import android.os.Handler
import android.os.Looper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.pure1v1.Pure1v1Logger
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.Instance
import io.agora.syncmanager.rtm.Sync.JoinSceneCallback

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */
class Pure1v1ServiceImp(
    private var user: UserInfo? = null
) {

    private val tag = "1v1_Service_LOG"
    private val kSceneId = "scene_1v1PrivateVideo_4.2.0"
    @Volatile
    private var syncUtilsInited = false
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val sceneRefs = mutableMapOf<String, SceneReference>()
    private val errorHandler: ((Exception?) -> Unit)? = null

    private var userList: List<UserInfo> = emptyList()

    fun reset() {
        if (syncUtilsInited) {
            Instance().destroy()
            syncUtilsInited = false
            sceneRefs.clear()
        }
    }

    /*
     * 拉取房间列表
     */
    fun getUserList(completion: (String?, List<UserInfo>) -> Unit) {
        initScene {
            Instance().getScenes(object : Sync.DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    Pure1v1Logger.d(tag, "result = $result")
                    val ret = ArrayList<UserInfo>()
                    result?.forEach {
                        val obj = it.toObject(UserInfo::class.java)
                        ret.add(obj)
                    }
                    //按照创建时间顺序排序
                    ret.sortBy { it.createdAt }
                    userList = ret.toList()
                    runOnMainThread { completion.invoke(null, userList) }
                }
                override fun onFail(exception: SyncManagerException?) {
                    val msg = exception?.localizedMessage ?: "Refresh User List Failed"
                    runOnMainThread { completion.invoke(msg, userList) }
                }
            })
        }
    }

    /*
     * 创建并加入一个房间
     */
    fun enterRoom(completion: (Error?) -> Unit) {
        //比较通过roomid，一个人可能会有不同的roomid，但是create scene通过uid，保证不同roomId会被覆盖，保证一个用户不会展示多个
        val containsUser = userList.any { it.getRoomId() == user?.getRoomId() }
        val u = user
        if (u == null || containsUser) {
            completion(null)
            return
        }
        Pure1v1Logger.d(tag, "createUser start")
        initScene {
            val scene = Scene()
            scene.id = u.userId
            scene.userId = u.userId
            scene.property = GsonUtils.covertToMap(u)
            Instance().createScene(scene, object : Sync.Callback {
                override fun onSuccess() {
                    Instance().joinScene(true, true, u.userId, object : JoinSceneCallback {
                        override fun onSuccess(sceneReference: SceneReference?) {
                            if (sceneReference != null) {
                                sceneRefs[u.userId] = sceneReference
                                runOnMainThread { completion.invoke(null) }
                            } else {
                                runOnMainThread { completion.invoke(java.lang.Error("error")) }
                            }
                        }
                        override fun onFail(exception: SyncManagerException?) {
                            runOnMainThread { completion.invoke(java.lang.Error("error")) }
                        }
                    })
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread {
                        completion.invoke(java.lang.Error("error"))
                    }
                }
            })
        }
    }

    /*
     * 离开房间
     */
    fun leaveRoom(completion: (Error?) -> Unit) {
        sceneRefs[user?.userId ?: ""]?.delete(object : Sync.Callback {
            override fun onSuccess() {
                runOnMainThread {
                    completion.invoke(null)
                }
            }
            override fun onFail(exception: SyncManagerException?) {
                completion.invoke(java.lang.Error("error"))
            }
        })
    }

    // --------------------- inner ---------------------
    /*
     * 建立和业务服务器之间的连接
     */
    private fun initScene(complete: () -> Unit) {
        if (syncUtilsInited) {
            complete.invoke()
            return
        }
        Instance().init(
            RethinkConfig(BuildConfig.AGORA_APP_ID, kSceneId),
            object : Sync.Callback {
                override fun onSuccess() {
                    syncUtilsInited = true
                    runOnMainThread{
                        complete.invoke()
                    }
                }
                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { errorHandler?.invoke(exception) }
                }
            }
        )
        Instance().subscribeConnectState {
            Pure1v1Logger.d(tag, "subscribeConnectState: $it")
        }
    }

    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }
}