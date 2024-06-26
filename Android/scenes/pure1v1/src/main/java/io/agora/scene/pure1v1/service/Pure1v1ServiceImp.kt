package io.agora.scene.pure1v1.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.Instance
import io.agora.syncmanager.rtm.Sync.JoinSceneCallback


class Pure1v1ServiceImp(
    private var user: UserInfo? = null
) {

    private val TAG = "1v1_Service_LOG"
    private val kSceneId = "scene_1v1PrivateVideo_4.0.0"
    @Volatile
    private var syncUtilsInited = false
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val sceneRefs = mutableMapOf<String, SceneReference>()
    private val errorHandler: ((Exception?) -> Unit)? = null

    private var userList: List<UserInfo> = emptyList()

    // MARK: - Public
    fun getUserList(completion: (String?, List<UserInfo>) -> Unit) {
        initScene {
            Instance().getScenes(object : Sync.DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    Log.d(TAG, "result = $result")
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

    fun enterRoom(completion: (Error?) -> Unit) {
        //比较通过roomid，一个人可能会有不同的roomid，但是create scene通过uid，保证不同roomId会被覆盖，保证一个用户不会展示多个
        val containsUser = userList.any { it.getRoomId() == user?.getRoomId() }
        val u = user
        if (u == null || containsUser) {
            completion(null)
            return
        }
        Log.d(TAG, "createUser start")
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

    fun subscribeNetworkStatusChanged(changedBlock: () -> Void) {
//        self.networkDidChanged = changedBlock
    }

    fun unsubscribeAll() {
//        networkDidChanged = nil
//        roomExpiredDidChanged = nil
    }

    // MARK: - Inner
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
            if (it == Sync.ConnectionState.open) {
                runOnMainThread {

                }
            }
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