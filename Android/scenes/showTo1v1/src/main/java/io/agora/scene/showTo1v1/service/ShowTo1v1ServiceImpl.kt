package io.agora.scene.showTo1v1.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.manager.UserManager
import io.agora.syncmanager.rtm.IObject
import io.agora.syncmanager.rtm.RethinkConfig
import io.agora.syncmanager.rtm.Scene
import io.agora.syncmanager.rtm.SceneReference
import io.agora.syncmanager.rtm.Sync
import io.agora.syncmanager.rtm.SyncManagerException
import kotlin.random.Random

class ShowTo1v1ServiceImpl constructor(
    private val context: Context, private val errorHandler: ((Exception) -> Unit)
) : ShowTo1v1ServiceProtocol {

    companion object {
        private const val TAG = "Show1v1_LOG"
        private const val kSceneId = "scene_Livetoprivate_3.7.0"
    }


    @Volatile
    private var syncUtilsInited = false

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var mSceneReferenceMap = mutableMapOf<String, SceneReference>()
    private var mShowTo1v1ServiceListener: ShowTo1v1ServiceListenerProtocol? = null

    @Volatile
    private var currRoomNo: String = ""

    private val roomMap = mutableMapOf<String, ShowTo1v1RoomInfo>() // key: roomNo

    private val objIdOfRoomNo = mutableMapOf<String, String>() // objectId of room no

    // time limit
    private val ROOM_AVAILABLE_DURATION: Long = 20 * 60 * 1000 // 20min
    private val timerRoomEndRun = Runnable {
        runOnMainThread {
            Log.d(TAG, "time up exit room!")
            mShowTo1v1ServiceListener?.onRoomTimeUp()
        }
    }

    private fun runOnMainThread(runnable: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    private fun initScene(complete: () -> Unit) {
        if (syncUtilsInited) {
            complete.invoke()
            return
        }
        Sync.Instance().init(
            RethinkConfig(BuildConfig.AGORA_APP_ID, kSceneId),
            object : Sync.Callback {
                override fun onSuccess() {
                    Log.d(TAG, "initScene onSuccess")
                    syncUtilsInited = true
                    runOnMainThread {
                        complete.invoke()
                    }
                }

                override fun onFail(exception: SyncManagerException) {
                    Log.e(TAG, "initScene:$exception")
                    runOnMainThread { errorHandler?.invoke(exception) }
                }
            }
        )
        Sync.Instance().subscribeConnectState { connectionState ->
            Log.d(TAG, "subscribeConnectState:$connectionState")
            when (connectionState) {
                Sync.ConnectionState.open -> {
                    runOnMainThread {
                        mShowTo1v1ServiceListener?.onNetworkStatusChanged(ShowTo1v1ServiceNetworkStatus.Open)
                    }
                }

                Sync.ConnectionState.connecting -> {
                    runOnMainThread {
                        mShowTo1v1ServiceListener?.onNetworkStatusChanged(ShowTo1v1ServiceNetworkStatus.Connecting)
                    }
                }

                Sync.ConnectionState.closed -> {
                    runOnMainThread {
                        mShowTo1v1ServiceListener?.onNetworkStatusChanged(ShowTo1v1ServiceNetworkStatus.Closed)
                    }
                }

                Sync.ConnectionState.fail -> {
                    runOnMainThread {
                        mShowTo1v1ServiceListener?.onNetworkStatusChanged(ShowTo1v1ServiceNetworkStatus.Fail)
                    }
                }
            }

        }
    }

    private fun subscribeRoomStatusChanged(channelId: String) {
        val sceneReference = mSceneReferenceMap[channelId] ?: return
        Log.d(TAG, "subscribeRoomStatusChanged $channelId")
        sceneReference.subscribe(object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                Log.d(TAG, "subscribe onUpdated:${item}")
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                val roomInfo = roomMap[item.id] ?: return
                runOnMainThread {
                    mShowTo1v1ServiceListener?.onRoomDidDestroy(roomInfo)
                }
                Log.d(TAG, "subscribeRoomStatusChanged subscribe onDeleted:${roomInfo}")
            }

            override fun onSubscribeError(ex: SyncManagerException) {
                errorHandler.invoke(ex)
            }

        })
    }

    override fun createRoom(roomName: String, completion: (error: Exception?, roomInfo: ShowTo1v1RoomInfo?) -> Unit) {
        initScene {
            val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
            val roomInfo = ShowTo1v1RoomInfo(
                roomId = roomId,
                roomName = roomName,
                createdAt = System.currentTimeMillis(),
                userId = UserManager.getInstance().user.id.toString(),
                userName = UserManager.getInstance().user.name,
                avatar = UserManager.getInstance().user.headUrl,
            )
            val scene = Scene()
            scene.id = roomInfo.roomId
            scene.userId = roomInfo.userId

            scene.property = GsonUtils.covertToMap(roomInfo)

            Sync.Instance().createScene(scene, object : Sync.Callback {
                override fun onSuccess() {
                    Log.d(TAG, "createRoom onSuccess")
                    roomMap[roomInfo.roomId] = roomInfo
                    runOnMainThread {
                        completion.invoke(null, roomInfo)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    Log.e(TAG, "createRoom onFail ${exception?.toString()}")
                    runOnMainThread { completion.invoke(exception, null) }
                }
            })
        }
    }

    override fun joinRoom(roomInfo: ShowTo1v1RoomInfo, completion: (error: Exception?) -> Unit) {
        currRoomNo = ""
        initScene {
            Sync.Instance().joinScene(true, true, roomInfo.roomId, object : Sync.JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference) {
                    Log.d(TAG, "joinRoom onSuccess $sceneReference")
                    currRoomNo = roomInfo.roomId
                    mSceneReferenceMap[roomInfo.roomId] = sceneReference
                    subscribeRoomStatusChanged(roomInfo.roomId)
                    // 重置体验时间事件
                    mainHandler.removeCallbacks(timerRoomEndRun)
                    // 定时删除房间
                    val expireLeftTime = ROOM_AVAILABLE_DURATION - (System.currentTimeMillis() - roomInfo.createdAt)
                    Log.d(TAG, "expireLeftTime: $expireLeftTime")
                    mainHandler.postDelayed(timerRoomEndRun, expireLeftTime)
                    runOnMainThread {
                        completion.invoke(null)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    Log.e(TAG, "joinRoom onFail ${exception?.toString()}")
                    runOnMainThread { completion.invoke(exception ?: java.lang.Exception("joinRoom onFail")) }
                }
            })
        }

    }

    override fun leaveRoom(roomInfo: ShowTo1v1RoomInfo, completion: (error: Exception?) -> Unit) {
        // 重置体验时间事件
        mainHandler.removeCallbacks(timerRoomEndRun)
        mSceneReferenceMap[roomInfo.roomId]?.delete(object : Sync.Callback {
            override fun onSuccess() {
                Log.d(TAG, "leaveRoom onSuccess")
                currRoomNo = ""
                runOnMainThread {
                    completion.invoke(null)
                }
            }

            override fun onFail(exception: SyncManagerException?) {
                Log.e(TAG, "leaveRoom onFail ${exception?.toString()}")
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    override fun getRoomList(completion: (error: Exception?, roomList: List<ShowTo1v1RoomInfo>) -> Unit) {
        initScene {
            Sync.Instance().getScenes(object : Sync.DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    Log.d(TAG, "getRoomList = $result")
                    roomMap.clear()
                    val ret = ArrayList<ShowTo1v1RoomInfo>()
                    result?.forEach {
                        val obj = it.toObject(ShowTo1v1RoomInfo::class.java)
                        objIdOfRoomNo[obj.roomId] = it.id
                        ret.add(obj)
                        roomMap[obj.roomId] = obj
                    }
                    //按照创建时间顺序排序
                    ret.sortBy { it.createdAt }
                    runOnMainThread { completion.invoke(null, ret) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception, emptyList()) }
                }
            })
        }

    }

    override fun subscribeListener(listener: ShowTo1v1ServiceListenerProtocol) {
        mShowTo1v1ServiceListener = listener
    }
}