package io.agora.scene.joy.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.joy.utils.JoyLogger
import io.agora.syncmanager.rtm.IObject
import io.agora.syncmanager.rtm.RethinkConfig
import io.agora.syncmanager.rtm.Scene
import io.agora.syncmanager.rtm.SceneReference
import io.agora.syncmanager.rtm.Sync
import io.agora.syncmanager.rtm.Sync.DataItemCallback
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
    private val mCurrentRoomUserMap = mutableMapOf<String, JoyUserInfo>()

    private val mUser: User
        get() = UserManager.getInstance().user

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


    private fun innerReset(isRoomDestroy: Boolean) {
        if (!isRoomDestroy) {
            innerRemoveUserInfo {}
            innerUpdateUserCount(mCurrentRoomUserMap.size - 1)
        }
        mCurrentRoomUserMap.clear()
        objIdOfUserNo.clear()

        if (isRoomDestroy){
            mRoomMap.remove(mCurrRoomNo)
        }
        mSceneReference = null
        mCurrRoomNo = ""
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

    override fun updateRoom(roomInfo: JoyRoomInfo, completion: (error: Exception?) -> Unit) {
        initSync {
            mSceneReference?.update(
                HashMap(GsonUtils.covertToMap(roomInfo)),
                object : DataItemCallback {
                    override fun onSuccess(result: IObject?) {
                        runOnMainThread {
                            completion.invoke(null)
                        }
                    }

                    override fun onFail(exception: SyncManagerException?) {
                        runOnMainThread {
                            completion.invoke(exception)
                        }
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
                assistantUid = 1000000000 + (user.id).toInt(),
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
                    innerSubscribeRoomChanged()
                    innerSubscribeUserChanged()
                    // 重置体验时间事件
                    mMainHandler.removeCallbacks(mTimerRoomEndRun)
                    // 定时删除房间
                    val expireLeftTime =
                        JoyServiceProtocol.ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - roomInfo.createdAt)
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
        // get
        mSceneReference?.collection(SYNC_SCENE_ROOM_USER_COLLECTION)?.get(object : Sync.DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                JoyLogger.d(TAG, "getUserList onSuccess roomId:$roomNo userCount:${result?.size}")
                val ret = mutableListOf<JoyUserInfo>()
                result?.forEach {
                    val obj = it.toObject(JoyUserInfo::class.java)
                    obj.objectId = it.id
                    objIdOfUserNo[obj.userId.toString()] = it.id
                    ret.add(obj)
                    mCurrentRoomUserMap[obj.userId.toString()] = obj
                }
                // not in --> add user
                if (!mCurrentRoomUserMap.containsKey(mUser.id.toString())) {
                    innerAddUserInfo(roomNo, completion = { objectId, error ->

                    })
                } else {

                }
                runOnMainThread {
                    mJoyServiceListener?.onUserListDidChanged(mCurrentRoomUserMap.values.toList())
                }
            }

            override fun onFail(exception: SyncManagerException?) {
                JoyLogger.d(TAG, "getUserList onFail roomId:$roomNo ${exception?.message}")
            }
        })
    }

    private fun innerAddUserInfo(roomId: String, completion: (objectId: String?, error: Exception?) -> Unit) {
        val userInfo = JoyUserInfo(
            userId = mUser.id.toInt(),
            userName = mUser.name,
            avatar = mUser.headUrl,
            createdAt = TimeUtils.currentTimeMillis()
        )
        mSceneReference?.collection(SYNC_SCENE_ROOM_USER_COLLECTION)?.add(userInfo, object : Sync.DataItemCallback {
            override fun onSuccess(result: IObject?) {
                result ?: return
                JoyLogger.d(TAG, "innerAddUserInfo onSuccess roomId:$roomId objectId:${result.id}")
                val addUser = result.toObject(JoyUserInfo::class.java)
                objIdOfUserNo[addUser.userId.toString()] = result.id
                mCurrentRoomUserMap[addUser.userId.toString()]
                runOnMainThread {
                    mJoyServiceListener?.onUserListDidChanged(mCurrentRoomUserMap.values.toList())
                }
                completion.invoke(result.id, null)
            }

            override fun onFail(exception: SyncManagerException?) {
                JoyLogger.e(TAG, "innerAddUserInfo onFail roomId:$roomId ${exception?.toString()}")
                completion.invoke(null, exception)
            }

        })
    }

    private fun innerRemoveUserInfo(completion: (error: Exception?) -> Unit) {
        val objectId = objIdOfUserNo[mUser.id.toString()] ?: return
        mSceneReference?.collection(SYNC_SCENE_ROOM_USER_COLLECTION)?.delete(objectId, object : Sync.Callback {
            override fun onSuccess() {
                JoyLogger.d(TAG, "innerRemoveUserInfo onSuccess  objectId:$objectId")
                completion.invoke(null)
                objIdOfUserNo.remove(mUser.id.toString())
            }

            override fun onFail(exception: SyncManagerException?) {
                JoyLogger.e(TAG, "innerRemoveUserInfo onFail  ${exception?.toString()}")
                completion.invoke(exception)
            }
        })
    }

    private fun innerSubscribeRoomChanged() {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                JoyLogger.d(TAG, "innerSubscribeRoomChanged onUpdated:${item}")
                val roomInfo = item.toObject(JoyRoomInfo::class.java)
                mRoomMap[roomInfo.roomId] = roomInfo
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                JoyLogger.d(TAG, "innerSubscribeRoomChanged subscribe onDeleted:${item}")
                val roomInfo = mRoomMap[item.id] ?: return
                if (item.id != mCurrRoomNo) return
                innerReset(true)
                runOnMainThread {
                    mJoyServiceListener?.onRoomDidDestroy(roomInfo)
                }

            }

            override fun onSubscribeError(ex: SyncManagerException) {
                errorHandler.invoke(ex)
            }

        }
        mSceneReference?.subscribe(listener)
        mRoomSubscribeListener.add(listener)
    }

    private fun innerSubscribeUserChanged() {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                JoyLogger.d(TAG, "innerSubscribeUserChanged onUpdated:${item}")
                val updateUser = item.toObject(JoyUserInfo::class.java)
                updateUser.objectId = item.id

                if (!mCurrentRoomUserMap.containsKey(updateUser.userId.toString())) {
                    objIdOfUserNo[updateUser.userId.toString()] = item.id
                    mCurrentRoomUserMap[updateUser.userId.toString()] = updateUser
                }
                runOnMainThread {
                    mJoyServiceListener?.onUserListDidChanged(mCurrentRoomUserMap.values.toList())
                }
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                JoyLogger.d(TAG, "subscribeUserChanged onDeleted:${item.id}")
                //将用户信息移除本地列表
                objIdOfUserNo.forEach { entry ->
                    if (entry.value == item.id) {
                        val removeUserNo = entry.key
                        mCurrentRoomUserMap.remove(removeUserNo)
                        objIdOfUserNo.remove(entry.key)
                        return
                    }
                }
                runOnMainThread {
                    mJoyServiceListener?.onUserListDidChanged(mCurrentRoomUserMap.values.toList())
                }
            }

            override fun onSubscribeError(ex: SyncManagerException) {
                errorHandler.invoke(ex)
            }
        }
        mSceneReference?.collection(SYNC_SCENE_ROOM_USER_COLLECTION)?.subscribe(listener)
        mRoomSubscribeListener.add(listener)
    }

    private fun innerUpdateUserCount(count: Int) {
        val roomInfo = mRoomMap[mCurrRoomNo]?.copy() ?: return
        if (count == roomInfo.roomUserCount) {
            return
        }
        roomInfo.roomUserCount = mCurrentRoomUserMap.size
        updateRoom(roomInfo, completion = {

        })
    }


    override fun leaveRoom(roomInfo: JoyRoomInfo, completion: (error: Exception?) -> Unit) {
        // 重置体验时间事件
        mMainHandler.removeCallbacks(mTimerRoomEndRun)
        // 取消所有订阅
        mRoomSubscribeListener.forEach {
            mSceneReference?.unsubscribe(it)
        }
        mRoomSubscribeListener.clear()

        innerRemoveUserInfo(completion = {

        })
        val user = UserManager.getInstance().user
        if (roomInfo.ownerId == user.id.toInt() ||
            TimeUtils.currentTimeMillis() - roomInfo.createdAt >= JoyServiceProtocol.ROOM_AVAILABLE_DURATION
        ) {
            mSceneReference?.delete(object : Sync.Callback {
                override fun onSuccess() {
                    JoyLogger.d(TAG, "leaveRoom onSuccess ${roomInfo.roomId}")
                    innerReset(true)
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
            innerReset(false)
            runOnMainThread {
                completion.invoke(null)
            }
        }
    }

    override fun subscribeListener(listener: JoyServiceListenerProtocol) {
        mJoyServiceListener = listener
    }
}