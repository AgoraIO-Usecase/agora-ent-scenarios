package io.agora.scene.joy.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.syncmanager.rtm.SceneReference
import io.agora.syncmanager.rtm.Sync

class JoySyncManagerServiceImp constructor(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) :JoyServiceProtocol {

    private val TAG = "Joy_Service_LOG"
    private val kSceneId = "scene_joy_4.10.0"

    private data class VLLoginModel(
        val id: String,
    )

    @Volatile
    private var syncUtilsInited = false
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var mSceneReference: SceneReference? = null

    // subscribers
    private var roomStatusSubscriber: ((JoyServiceProtocol.JoySubscribe, RoomListModel?) -> Unit)? = null
    private var roomUserCountSubscriber: ((Int) -> Unit)? = null
    private var onReconnectSubscriber: (() -> Unit)? = null
    private var roomTimeUpSubscriber: (() -> Unit)? = null

    // cache objectId
    private val objIdOfRoomNo = HashMap<String, String>() // objectId of room no
    private val objIdOfUserNo = HashMap<String, String>() // objectId of user no

    // cache data
    private val roomSubscribeListener = mutableListOf<Sync.EventListener>()
    private val roomMap = mutableMapOf<String, RoomListModel>() // key: roomNo
    private val userMap = mutableMapOf<String, VLLoginModel?>() // key: userNo

    override fun reset() {

    }

    override fun getRoomList(completion: (error: Exception?, list: List<RoomListModel>?) -> Unit) {
    }

    override fun createRoom(
        inputModel: CreateRoomInputModel,
        completion: (error: Exception?, out: CreateRoomOutputModel?) -> Unit
    ) {
    }

    override fun joinRoom(
        inputModel: JoinRoomInputModel,
        completion: (error: Exception?, out: JoinRoomOutputModel?) -> Unit
    ) {
    }

    override fun leaveRoom(completion: (error: Exception?) -> Unit) {
    }

    override fun subscribeRoomStatus(changedBlock: (JoyServiceProtocol.JoySubscribe, RoomListModel?) -> Unit) {
    }

    override fun subscribeUserListCount(changedBlock: (count: Int) -> Unit) {
    }

    override fun subscribeRoomTimeUp(onRoomTimeUp: () -> Unit) {
    }

    override fun subscribeReConnectEvent(onReconnect: () -> Unit) {
    }
}