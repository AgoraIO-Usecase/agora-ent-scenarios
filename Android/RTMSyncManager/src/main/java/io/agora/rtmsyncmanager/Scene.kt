package io.agora.rtmsyncmanager

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.agora.rtm.RtmConstants.RtmConnectionChangeReason
import io.agora.rtm.RtmConstants.RtmConnectionState
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.collection.IAUICollection
import io.agora.rtmsyncmanager.service.imp.AUIUserServiceImpl
import io.agora.rtmsyncmanager.service.rtm.AUIRtmErrorRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmLockRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.ObservableHelper

class Scene constructor(
    val channelName: String,
    private var rtmManager: AUIRtmManager
) {

    val tag = "AUIScene"

    private val kRoomInfoKey = "scene_room_info"
    private val kRoomInfoRoomId = "room_id"
    private val kRoomInfoRoomOwnerId = "room_owner_id"
    private val kRoomInfoPayloadId = "room_payload_id"

    private var collectionMap = mutableMapOf<String, IAUICollection>()

    public val userService = AUIUserServiceImpl(channelName, rtmManager).apply {
        registerRespObserver(userRespObserver)
    }

    private val roomCollection: AUIMapCollection by lazy {
        getCollection(kRoomInfoKey) { channelName, sceneKey, rtmManager ->
            AUIMapCollection(channelName, sceneKey, rtmManager)
        }
    }

    private var enterRoomCompletion: ((Map<String, Any>?, Exception?)-> Unit)? = null
    private var respHandlers = ObservableHelper<ISceneResponse>()
    private var roomPayload: Map<String, Any>? = null

    private var subscribeDate: Long? = null
    private var lockRetrived = false
        set(value) {
            field = value
            checkRoomValid()
        }

    private var subscribeSuccess = false
        set(value) {
            field = value
            checkRoomValid()
        }

    private var userSnapshotList: List<AUIUserInfo?>? = null
        set(value) {
            field = value
            checkRoomValid()
        }

    private var ownerId = ""
        set(value) {
            field = value
            AUIRoomContext.shared().roomOwnerMap[channelName] = ownerId
            checkRoomValid()
        }

    init {
        AUIRoomContext.shared().roomArbiterMap[channelName] = AUIArbiter(channelName, rtmManager, AUIRoomContext.shared().currentUserInfo.userId)
    }

    fun bindRespDelegate(handler: ISceneResponse) {
        respHandlers.subscribeEvent(handler)
    }

    fun unbindRespDelegate(handler: ISceneResponse) {
        respHandlers.unSubscribeEvent(handler)
    }

    fun create(payload: Map<String, Any>?, completion: (AUIRtmException?)->Unit) {
        if (!rtmManager.isLogin) {
            completion.invoke(AUIRtmException(-1, "create fail! not login", ""))
            return
        }
        ownerId = AUIRoomContext.shared().currentUserInfo.userId
        val roomInfo = mutableMapOf(
            kRoomInfoRoomId to channelName,
            kRoomInfoRoomOwnerId to ownerId
        )
        if (payload != null) {
            roomInfo[kRoomInfoPayloadId] = Gson().toJson(payload)
        }
        roomCollection.initMetaData(channelName, roomInfo) { err ->
            if (err != null) {
                runOnUiThread { completion.invoke(err) }
                return@initMetaData
            }
            runOnUiThread { completion.invoke(null) }
        }
        getArbiter().create()
    }

    fun enter(completion: (Map<String, Any>?, AUIRtmException?)->Unit) {
        if (!rtmManager.isLogin) {
            completion.invoke(null, AUIRtmException(-1, "create fail! not login", ""))
            return
        }
        subscribeDate = System.currentTimeMillis()
        enterRoomCompletion = { payload, err ->
            Log.d(tag, "[Benchmark]enterRoomCompletion: ${System.currentTimeMillis() - (subscribeDate ?: 0)}ms")
            runOnUiThread { completion(payload, null) }
        }
        if (ownerId.isEmpty()) {
            roomCollection.getMetaData { err, metadata ->
                val map = metadata as? Map<String, Any> ?: run {
                    ownerId = "owner unknown"
                    return@getMetaData
                }
                val ownerId = map[kRoomInfoRoomOwnerId] as? String ?: run {
                    ownerId = "owner unknown"
                    return@getMetaData
                }

                val payloadStr = map[kRoomInfoPayloadId] as? String
                if (payloadStr != null) {
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    try {
                        roomPayload = Gson().fromJson(payloadStr, type)
                    } catch (_: Exception) { }
                }
                this.ownerId = ownerId
            }
        }
        getArbiter().acquire()
        rtmManager.subscribeError(errorRespObserver)
        rtmManager.subscribeLock(channelName, rtmManager.kRTM_Referee_LockName, lockRespObserver)
        rtmManager.subscribe(channelName) { error ->
            error?.let {
                runOnUiThread { completion.invoke(null, error) }
                return@subscribe
            }
            subscribeSuccess = true
        }
    }

    /// 离开scene
    fun leave() {
        getArbiter().release()
        cleanSDK()
    }

    /// 销毁scene，清理所有缓存（包括rtm的所有metadata）
    fun delete() {
        cleanScene()
        getArbiter().destroy()
        AUIRoomContext.shared().cleanRoom(channelName)
        cleanSDK()
    }

    /// 获取一个collection，例如let collection: AUIMapCollection = scene.getCollection("musicList")
    /// - Parameter sceneKey: <#sceneKey description#>
    /// - Returns: <#description#>
    fun <T : IAUICollection>getCollection(sceneKey: String, create: ((String, String, AUIRtmManager) -> T) ): T {
        val collection = collectionMap[sceneKey]
        if (collection != null) {
            return collection as T
        }
        val scene = create.invoke(channelName, sceneKey, rtmManager)
        collectionMap[sceneKey] = scene
        return scene
    }
    private fun getArbiter(): AUIArbiter {
        val a = AUIRoomContext.shared().roomArbiterMap[channelName]
        if (a != null) {
            return a
        }
        val arbiter = AUIArbiter(channelName, rtmManager, AUIRoomContext.shared().currentUserInfo.userId)
        AUIRoomContext.shared().roomArbiterMap[channelName] = arbiter
        return arbiter
    }
    //如果subscribe成功、锁也获取到、用户列表也获取到，可以检查是否是脏房间并且清理
    private fun checkRoomValid() {
        if (subscribeSuccess && lockRetrived && ownerId.isNotEmpty()) else { return }
        if (enterRoomCompletion != null) {
            enterRoomCompletion?.invoke(roomPayload, null)
            enterRoomCompletion = null
        }
        val userList = userSnapshotList ?: return
        if (userList.firstOrNull { AUIRoomContext.shared().isRoomOwner(channelName, it?.userId) } != null) {
            //room owner not found, clean room
            cleanScene()
            return
        }
    }

    private fun cleanUserInfo(userId: String) {
        //TODO: 用户离开后，需要清理这个用户对应在collection里的信息，例如上麦信息、点歌信息等
    }

    private fun cleanScene() {
        if (!getArbiter().isArbiter()) {
            return
        }
        val removeKeys = collectionMap.keys.toList()
        //每个collection都清空，让所有人收到onMsgRecvEmpty
        rtmManager.cleanBatchMetadata(channelName = channelName, lockName = "", remoteKeys = removeKeys, fetchImmediately = true) {
        }
        roomCollection.cleanMetaData {  }
        getArbiter().destroy()
    }

    private fun cleanSDK() {
        rtmManager.unSubscribe(channelName)
        rtmManager.unSubscribeError(errorRespObserver)
        rtmManager.unsubscribeLock(lockRespObserver)
        //TODO: syncmanager 需要logout
//        rtmManager.logout()
    }

    private val lockRespObserver = object: AUIRtmLockRespObserver {
        override fun onReceiveLock(channelName: String, lockName: String, lockOwner: String) {
            lockRetrived = true
        }

        override fun onReleaseLock(channelName: String, lockName: String, lockOwner: String) {
        }
    }

    private val userRespObserver = object: IAUIUserService.AUIUserRespObserver {
        override fun onRoomUserSnapshot(roomId: String, userList: List<AUIUserInfo?>?) {
            userSnapshotList = userList
        }

        override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {}

        override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
            if (AUIRoomContext.shared().isRoomOwner(roomId, userInfo.userId)) else {
                cleanUserInfo(userInfo.userId)
                return
            }
            cleanScene()
        }

        override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {}

        override fun onUserAudioMute(userId: String, mute: Boolean) {}

        override fun onUserVideoMute(userId: String, mute: Boolean) {}
    }

    private val errorRespObserver = object: AUIRtmErrorRespObserver {
        override fun onTokenPrivilegeWillExpire(channelName: String?) {
            respHandlers.notifyEventHandlers { handler ->
                handler.onTokenPrivilegeWillExpire(channelName)
            }
        }
        override fun onMsgReceiveEmpty(channelName: String) {
            respHandlers.notifyEventHandlers { handler ->
                handler.onSceneDestroy(channelName)
            }
        }
        override fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {
            if (channelName == null) {
                return
            }
            if (RtmConnectionChangeReason.getEnum(reason) == RtmConnectionChangeReason.REJOIN_SUCCESS) {
                getArbiter().acquire()
            }
            if (RtmConnectionState.getEnum(state) == RtmConnectionState.FAILED &&
                RtmConnectionChangeReason.getEnum(reason) == RtmConnectionChangeReason.BANNED_BY_SERVER) {
                return
            }
            respHandlers.notifyEventHandlers { handler ->
                handler.onSceneUserBeKicked(channelName, AUIRoomContext.shared().currentUserInfo.userId)
            }
        }
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            runnable.run()
        } else {
            mHandler.post(runnable)
        }
    }
}