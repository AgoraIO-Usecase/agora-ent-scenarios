package io.agora.rtmsyncmanager

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.agora.rtm.RtmConstants.RtmConnectionChangeReason
import io.agora.rtm.RtmConstants.RtmConnectionState
import io.agora.rtm.RtmConstants.RtmErrorCode
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter
import io.agora.rtmsyncmanager.service.arbiter.AUIArbiterCallback
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.collection.IAUICollection
import io.agora.rtmsyncmanager.service.imp.AUIUserServiceImpl
import io.agora.rtmsyncmanager.service.rtm.AUIRtmErrorRespObserver
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmManager
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.ObservableHelper

class Scene constructor(
    val channelName: String,
    private var rtmManager: AUIRtmManager
) {

    private val tag = "AUIScene"

    private val kRoomInfoKey = "scene_room_info"
    private val kRoomInfoRoomId = "room_id"
    private val kRoomInfoRoomOwnerId = "room_owner_id"
    private val kRoomInfoPayloadId = "room_payload_id"

    private var collectionMap = mutableMapOf<String, IAUICollection>()

    public val userService = AUIUserServiceImpl(channelName, rtmManager).apply {
        registerRespObserver(object: IAUIUserService.AUIUserRespObserver {
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
        })
    }

    private val roomCollection: AUIMapCollection by lazy {
        getCollection(kRoomInfoKey) { channelName, sceneKey, rtmManager ->
            AUIMapCollection(channelName, sceneKey, rtmManager)
        }
    }

    private var enterRoomCompletion: ((Map<String, Any>?, AUIRtmException?)-> Unit)? = null
    private var respHandlers = ObservableHelper<ISceneResponse>()
    private var roomPayload: Map<String, Any>? = null

    private var subscribeDate: Long? = null
    private var lockOwnerRetrived = false
        set(value) {
            field = value
            checkRoomValid()
        }

    private var lockOwnerAcquireSuccess = false
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
            runOnUiThread { completion(payload, err) }
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
        getArbiter().acquire {
            if (it == null) {
                //fail 走onError(channelName: String, error: NSError)，这里不处理
                lockOwnerAcquireSuccess = true
            }
        }
        rtmManager.subscribeError(errorRespObserver)
        getArbiter().subscribeEvent(arbiterObserver)
        rtmManager.subscribe(channelName) { error ->
            if (error != null && error.code != RtmErrorCode.getValue(RtmErrorCode.DUPLICATE_OPERATION)) {
                runOnUiThread {
                    enterRoomCompletion?.invoke(null, error)
                    enterRoomCompletion = null
                }
                return@subscribe
            }
            subscribeSuccess = true
        }
    }

    /// 离开scene
    fun leave() {
        AUILogger.logger().d(tag,"leave")
        getArbiter().release()
        cleanSDK()
        AUIRoomContext.shared().cleanRoom(channelName)
    }

    /// 销毁scene，清理所有缓存（包括rtm的所有metadata）
    fun delete() {
        AUILogger.logger().d(tag,"delete")
        cleanScene()
        getArbiter().destroy()
        cleanSDK()
        AUIRoomContext.shared().cleanRoom(channelName)
    }

    /// 获取一个collection，例如let collection: AUIMapCollection = scene.getCollection("musicList")
    /// - Parameter sceneKey: <#sceneKey description#>
    /// - Returns: <#description#>
    fun <T : IAUICollection>getCollection(key: String, create: ((String, String, AUIRtmManager) -> T) ): T {
        val collection = collectionMap[key]
        if (collection != null) {
            return collection as T
        }
        val scene = create.invoke(channelName, key, rtmManager)
        collectionMap[key] = scene
        return scene
    }

    private fun notifyError(error: AUIRtmException) {
        AUILogger.logger().e(tag,"join fail: ${error.message}")
        if (enterRoomCompletion != null) {
            enterRoomCompletion?.invoke(null, error)
            enterRoomCompletion = null
        }
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
    // 如果subscribe成功、锁也获取到，并且锁主获取到锁成功(acquire的callback成功收到)、用户列表也获取到，可以检查是否是脏房间并且清理
    private fun checkRoomValid() {
        if (subscribeSuccess && lockOwnerRetrived && ownerId.isNotEmpty()) else { return }
        //如果是锁主，需要判断有没有acquire成功回调，回调后有本地对比，没有成功回调前setmetadata会失败-12008
        if (getArbiter().isArbiter() && !lockOwnerAcquireSuccess) return
        if (enterRoomCompletion != null) {
            enterRoomCompletion?.invoke(roomPayload, null)
            enterRoomCompletion = null
        }
        val userList = userSnapshotList ?: return
        if (userList.firstOrNull { AUIRoomContext.shared().isRoomOwner(channelName, it?.userId) } == null) {
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
        _cleanScene()
    }

    private fun _cleanScene() {
        AUILogger.logger().d(tag, "_cleanScene")
        //每个collection都清空，让所有人收到onMsgRecvEmpty
        rtmManager.cleanAllMetadata(channelName = channelName, lockName = "") {
        }
        //roomCollection.cleanMetaData {  }
        getArbiter().destroy()
    }

    private fun cleanSDK() {
        rtmManager.unSubscribe(channelName)
        rtmManager.unSubscribeError(errorRespObserver)
        getArbiter().unSubscribeEvent(arbiterObserver)
        //TODO: syncmanager 需要logout
//        rtmManager.logout()
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

    private val arbiterObserver = object: AUIArbiterCallback {
        override fun onArbiterDidChange(channelName: String, arbiterId: String) {
            if (arbiterId.isEmpty()) {return}
            lockOwnerRetrived = true
        }

        override fun onError(channelName: String, error: AUIRtmException) {
            //如果锁不存在，也认为是房间被销毁的一种
            if (error.code == RtmErrorCode.getValue(RtmErrorCode.LOCK_NOT_EXIST)) {
                _cleanScene()
            }
            notifyError(error)
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