package io.agora.scene.joy.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.joy.utils.JoyLogger
import io.agora.syncmanager.rtm.IObject
import io.agora.syncmanager.rtm.RethinkConfig
import io.agora.syncmanager.rtm.Scene
import io.agora.syncmanager.rtm.SceneReference
import io.agora.syncmanager.rtm.Sync
import io.agora.syncmanager.rtm.SyncManagerException
import kotlin.random.Random

class JoySyncManagerServiceImp constructor(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)
) : JoyServiceProtocol {

    companion object {
        private const val TAG = "Joy_Service_LOG"
        private const val kSceneId = "scene_joy_4.10.0"
        private const val SYNC_SCENE_ROOM_USER_COLLECTION = "userCollection"
    }

    @Volatile
    private var syncInitialized = false
    private val mMainHandler by lazy { Handler(Looper.getMainLooper()) }

    private fun runOnMainThread(runnable: Runnable) {
        if (Thread.currentThread() == mMainHandler.looper.thread) {
            runnable.run()
        } else {
            mMainHandler.post(runnable)
        }
    }

    private var mSceneReference: SceneReference? = null
    private var mSceneReferenceMap = mutableMapOf<String, SceneReference>()
    private var mJoyServiceListener: JoyServiceListenerProtocol? = null

    @Volatile
    private var mCurrRoomNo: String = ""


    // cache objectId
    private val objIdOfRoomNo = HashMap<String, String>() // objectId of room no
    private val objIdOfUserNo = HashMap<String, String>() // objectId of user no

    // cache data
    private val mRoomSubscribeListener = mutableListOf<Sync.EventListener>()
    private val mRoomMap = mutableMapOf<String, JoyRoomInfo>() // key: roomNo
    private val mCurrentRoomUserList = mutableListOf<JoyUserInfo>()

    private val mTimerRoomEndRun = Runnable {
        runOnMainThread {
            JoyLogger.d(TAG, "time up exit room!")
            mJoyServiceListener?.onRoomTimeUp()
        }
    }

    private fun initSync(complete: () -> Unit) {
        if (syncInitialized) {
            complete.invoke()
            return
        }
        Sync.Instance().init(RethinkConfig(BuildConfig.AGORA_APP_ID, kSceneId),
            object : Sync.Callback {
                override fun onSuccess() {
                    syncInitialized = true
                    runOnMainThread { complete.invoke() }
                }

                override fun onFail(exception: SyncManagerException?) {
                    syncInitialized = false
                    errorHandler.invoke(exception)
                }
            }
        )
        Sync.Instance().subscribeConnectState {
            JoyLogger.d(TAG, "subscribeConnectState state=$it")
            mJoyServiceListener?.onNetworkStatusChanged(it)
            if (it == Sync.ConnectionState.open) {
                runOnMainThread {
                    // 判断当前房间是否还存在
                    mRoomMap[mCurrRoomNo]?.let { oldRoomInfo ->
                        getRoomList {
                            val roomInfo = mRoomMap[mCurrRoomNo]
                            if (roomInfo == null) {
                                runOnMainThread {
                                    mJoyServiceListener?.onRoomDidDestroy(oldRoomInfo)
                                }
                            }
                        }
                    }

                }
            }
        }
    }


    override fun reset() {
        if (syncInitialized){
            mRoomSubscribeListener.forEach {
                mSceneReference?.unsubscribe(it)
            }
            mRoomMap.clear()
            mCurrentRoomUserList.clear()
            Sync.Instance().destroy()
            syncInitialized = false
        }
    }

    override fun getRoomList(completion: (list: List<JoyRoomInfo>) -> Unit) {
        initSync {
            Sync.Instance().getScenes(object : Sync.DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    JoyLogger.d(TAG, "getRoomList success")
                    mRoomMap.clear()
                    val ret = ArrayList<JoyRoomInfo>()
                    result?.forEach {
                        val obj = it.toObject(JoyRoomInfo::class.java)
                        objIdOfRoomNo[obj.roomId] = it.id
                        ret.add(obj)
                        mRoomMap[obj.roomId] = obj
                    }
                    //按照创建时间顺序排序
                    ret.sortBy { it.createdAt }
                    runOnMainThread { completion.invoke(ret) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(emptyList()) }
                }
            })
        }
    }


    override fun createRoom(roomName: String, completion: (error: Exception?, out: JoyRoomInfo?) -> Unit) {
        initSync {
            val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
            val user = UserManager.getInstance().user
            val createdAt = TimeUtils.currentTimeMillis()
            val roomInfo = JoyRoomInfo(
                roomId = roomId,
                roomName = roomName,
                ownerId = user.id.toInt(),
                ownerAvatar = user.headUrl,
                ownerName = user.name,
                createdAt = createdAt,
                thumbnailId = getRandomThumbnailId(createdAt),
                objectId = roomId,
            )
            val scene = Scene()
            scene.id = roomInfo.roomId
            scene.userId = roomInfo.ownerId.toString()

            scene.property = GsonUtils.covertToMap(roomInfo)

            Sync.Instance().createScene(scene, object : Sync.Callback {
                override fun onSuccess() {
                    JoyLogger.d(TAG, "createRoom onSuccess")
                    mRoomMap[roomInfo.roomId] = roomInfo
                    runOnMainThread {
                        completion.invoke(null, roomInfo)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    JoyLogger.e(TAG, "createRoom onFail ${exception?.toString()}")
                    runOnMainThread { completion.invoke(exception, null) }
                }
            })
        }
    }

    private fun getRandomThumbnailId(crateAt: Long) =
        Random(crateAt).nextInt(0, 4).toString()

    override fun joinRoom(roomInfo: JoyRoomInfo, completion: (error: Exception?) -> Unit) {
        mCurrRoomNo = ""
        initSync {
            Sync.Instance().joinScene(true, true, roomInfo.roomId, object : Sync.JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference) {
                    JoyLogger.d(TAG, "joinRoom onSuccess $sceneReference")
                    mSceneReference = sceneReference
                    mCurrRoomNo = roomInfo.roomId
                    mSceneReferenceMap[roomInfo.roomId] = sceneReference
                    innerAddUserIfNeed(roomInfo.roomId)
                    innerSubscribeRoomChanged(roomInfo.roomId)
                    innerSubscribeUserChanged(roomInfo.roomId)
                    // 重置体验时间事件
                    mMainHandler.removeCallbacks(mTimerRoomEndRun)
                    // 定时删除房间
                    val expireLeftTime = JoyServiceProtocol.ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - roomInfo.createdAt)
                    JoyLogger.d(TAG, "expireLeftTime: $expireLeftTime")
                    mMainHandler.postDelayed(mTimerRoomEndRun, expireLeftTime)
                    runOnMainThread {
                        completion.invoke(null)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    JoyLogger.e(TAG, "joinRoom onFail ${exception?.message}")
                    runOnMainThread { completion.invoke(exception ?: java.lang.Exception("joinRoom onFail")) }
                }
            })
        }
    }

    private fun innerAddUserIfNeed(roomNo: String) {
        mSceneReference?.collection(SYNC_SCENE_ROOM_USER_COLLECTION)?.get(object : Sync.DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                JoyLogger.d(TAG, "getUserList onSuccess roomId:$roomNo userCount:${result?.size}")
                val ret = mutableListOf<JoyUserInfo>()
                result?.forEach {
                    val obj = it.toObject(JoyUserInfo::class.java)
                    obj.objectId = it.id
                    ret.add(obj)
                }
                mCurrentRoomUserList.clear()
                mCurrentRoomUserList.addAll(ret)
                runOnMainThread {
                    mJoyServiceListener?.onUserListDidChanged(mCurrentRoomUserList)
                }
                innerAddUserInfo(roomNo, completion = { objectId, error ->

                })
            }

            override fun onFail(exception: SyncManagerException?) {
                JoyLogger.d(TAG, "getUserList onFail roomId:$roomNo ${exception?.message}")
            }
        })
    }

    private fun innerAddUserInfo(roomId: String, completion: (objectId: String?, error: Exception?) -> Unit) {
        val userInfo = JoyUserInfo(
            userId = UserManager.getInstance().user.id.toInt(),
            userName = UserManager.getInstance().user.name,
            avatar = UserManager.getInstance().user.headUrl,
            createdAt = TimeUtils.currentTimeMillis()
        )
        mSceneReference?.collection(SYNC_SCENE_ROOM_USER_COLLECTION)?.add(userInfo, object : Sync.DataItemCallback {
            override fun onSuccess(result: IObject?) {
                JoyLogger.d(TAG, "innerAddUserInfo onSuccess roomId:$roomId objectId:${result?.id}")
                result?.let { res ->
                    val addUser = res.toObject(JoyUserInfo::class.java)
                    objIdOfUserNo[roomId] = result.id
                    if (mCurrentRoomUserList.find { it.userId == addUser.userId } != null) return@let
                    mCurrentRoomUserList.add(addUser)
                    runOnMainThread {
                        mJoyServiceListener?.onUserListDidChanged(mCurrentRoomUserList)
                    }
                }
                completion.invoke(result?.id, null)
            }

            override fun onFail(exception: SyncManagerException?) {
                JoyLogger.e(TAG, "innerAddUserInfo onFail roomId:$roomId ${exception?.toString()}")
                completion.invoke(null, exception)
            }

        })
    }

    private fun innerRemoveUserInfo(roomId: String, completion: (error: Exception?) -> Unit) {
        val objectId = objIdOfUserNo[roomId] ?: return
        mSceneReference?.collection(SYNC_SCENE_ROOM_USER_COLLECTION)?.delete(objectId, object : Sync.Callback {
            override fun onSuccess() {
                JoyLogger.d(TAG, "innerRemoveUserInfo onSuccess roomId:$roomId objectId:$objectId")
                completion.invoke(null)
                objIdOfUserNo.remove(roomId)
            }

            override fun onFail(exception: SyncManagerException?) {
                JoyLogger.e(TAG, "innerRemoveUserInfo onFail roomId:$roomId ${exception?.toString()}")
                completion.invoke(exception)
            }
        })
    }

    private fun innerSubscribeRoomChanged(roomNo: String) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                JoyLogger.d(TAG, "innerSubscribeRoomChanged onUpdated:${item}")
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                val roomInfo = mRoomMap[item.id] ?: return
                runOnMainThread {
                    mJoyServiceListener?.onRoomDidDestroy(roomInfo)
                }
                JoyLogger.d(TAG, "innerSubscribeRoomChanged subscribe onDeleted:${roomInfo}")
            }

            override fun onSubscribeError(ex: SyncManagerException) {
                errorHandler.invoke(ex)
            }

        }
        mSceneReference?.subscribe(listener)
        mRoomSubscribeListener.add(listener)
    }

    private fun innerSubscribeUserChanged(roomNo: String) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                JoyLogger.d(TAG, "innerSubscribeUserChanged onUpdated:${item}")
                val updateUser = item.toObject(JoyUserInfo::class.java)
                updateUser.objectId = item.id
                if (mCurrentRoomUserList.find { it.userId == updateUser.userId } != null) {
                    return
                }
                mCurrentRoomUserList.add(updateUser)
                runOnMainThread {
                    mJoyServiceListener?.onUserListDidChanged(mCurrentRoomUserList)
                }
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                JoyLogger.d(TAG, "subscribeUserChanged onDeleted:${item.id}")
                val index = mCurrentRoomUserList.indexOfFirst { it.objectId == item.id }
                if (index >= 0) {
                    mCurrentRoomUserList.removeAt(index)
                }
                runOnMainThread {
                    mJoyServiceListener?.onUserListDidChanged(mCurrentRoomUserList)
                }
            }

            override fun onSubscribeError(ex: SyncManagerException) {
                errorHandler.invoke(ex)
            }
        }
        mSceneReference?.collection(SYNC_SCENE_ROOM_USER_COLLECTION)?.subscribe(listener)
        mRoomSubscribeListener.add(listener)
    }


    override fun leaveRoom(roomInfo: JoyRoomInfo, completion: (error: Exception?) -> Unit) {
        // 重置体验时间事件
        mMainHandler.removeCallbacks(mTimerRoomEndRun)
        val sceneReference = mSceneReference ?: return
        // 取消所有订阅
        mRoomSubscribeListener.forEach {
            sceneReference.unsubscribe(it)
        }
        mRoomSubscribeListener.clear()

        innerRemoveUserInfo(roomInfo.roomId, completion = {

        })
        val user = UserManager.getInstance().user
        if (roomInfo.ownerId == user.id.toInt() ||
            TimeUtils.currentTimeMillis() - roomInfo.createdAt >= JoyServiceProtocol.ROOM_AVAILABLE_DURATION
        ) {
            sceneReference.delete(object : Sync.Callback {
                override fun onSuccess() {
                    JoyLogger.d(TAG, "leaveRoom onSuccess ${roomInfo.roomId}")
                    mCurrRoomNo = ""
                    mSceneReference = null
                    runOnMainThread {
                        completion.invoke(null)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    JoyLogger.e(TAG, "leaveRoom onFail ${exception?.message}")
                    runOnMainThread { completion.invoke(exception) }
                }
            })
        } else {
            mSceneReferenceMap.remove(roomInfo.roomId)
        }
    }

    override fun subscribeListener(listener: JoyServiceListenerProtocol) {
        mJoyServiceListener = listener
    }
}