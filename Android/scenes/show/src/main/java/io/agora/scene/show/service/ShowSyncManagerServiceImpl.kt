package io.agora.scene.show.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.manager.UserManager
import io.agora.syncmanager.rtm.*
import kotlin.random.Random

class ShowSyncManagerServiceImpl(
    private val context: Context,
    private val errorHandler: (Exception) -> Unit
) : ShowServiceProtocol {
    private val kSceneId = "scene_show"

    @Volatile
    private var syncInitialized = false


    // global cache data
    private val roomMap = mutableMapOf<String, ShowRoomDetailModel>()

    // current room cache data
    private var currRoomNo : String = ""
    private var currSceneReference: SceneReference? = null

    override fun getRoomList(
        success: (List<ShowRoomDetailModel>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        initSync {
            Sync.Instance().getScenes(object : Sync.DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    val roomList = result!!.map {
                        it.toObject(ShowRoomDetailModel::class.java)
                    }
                    roomMap.clear()
                    roomList.forEach { roomMap[it.roomNo] = it.copy() }
                    success.invoke(roomList.sortedBy { it.crateAt })
                }

                override fun onFail(exception: SyncManagerException?) {
                    errorHandler.invoke(exception!!)
                    error?.invoke(exception!!)
                }
            })
        }
    }

    override fun createRoom(
        roomName: String,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        initSync {
            val roomDetail = ShowRoomDetailModel(
                (Random(System.currentTimeMillis()).nextInt(10000) + 100000).toString(),
                roomName,
                0,
                "",
                UserManager.getInstance().user.userNo,
                ShowRoomStatus.activity.value,
                System.currentTimeMillis().toDouble(),
                System.currentTimeMillis().toDouble()
            )
            val scene = Scene().apply {
                id = roomDetail.roomNo
                userId = roomDetail.ownerId
                property = roomDetail.toMap()
            }
            Sync.Instance().createScene(
                scene,
                object : Sync.Callback {
                    override fun onSuccess() {
                        roomMap[roomDetail.roomNo] = roomDetail.copy()
                        success.invoke(roomDetail)
                    }

                    override fun onFail(exception: SyncManagerException?) {
                        errorHandler.invoke(exception!!)
                        error?.invoke(exception!!)
                    }
                })
        }
    }

    override fun joinRoom(
        roomNo: String,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        if(currRoomNo.isNotEmpty()){
            error?.invoke(RuntimeException("There is a room joined or joining now!"))
            return
        }
        currRoomNo = roomNo
        initSync {
            Sync.Instance().joinScene(
                roomNo, object: Sync.JoinSceneCallback{
                    override fun onSuccess(sceneReference: SceneReference?) {

                        this@ShowSyncManagerServiceImpl.currSceneReference = sceneReference!!
                        success.invoke(roomMap[roomNo]!!)
                    }

                    override fun onFail(exception: SyncManagerException?) {
                        errorHandler.invoke(exception!!)
                        error?.invoke(exception!!)
                        currRoomNo = ""
                    }
                }
            )
        }
    }

    override fun leaveRoom() {
        if(currRoomNo.isEmpty()){
            return
        }
        val roomDetail = roomMap[currRoomNo] ?: return
        if(roomDetail.ownerId == UserManager.getInstance().user.userNo){
            val roomNo = currRoomNo
            currSceneReference?.delete(object:Sync.Callback{
                override fun onSuccess() {
                    roomMap.remove(roomNo)
                }

                override fun onFail(exception: SyncManagerException?) {
                    errorHandler.invoke(exception!!)
                }
            })
        }
        currRoomNo = ""
        currSceneReference = null
    }

    override fun getAllUserList(success: (List<ShowUser>) -> Unit, error: ((Exception) -> Unit)?) {
        TODO("Not yet implemented")
    }

    override fun subscribeUser(onUserChange: (ShowServiceProtocol.ShowSubscribeStatus, ShowUser) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun sendChatMessage(
        message: ShowMessage,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun subscribeMessage(onMessageChange: (ShowServiceProtocol.ShowSubscribeStatus, ShowMessage) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getAllMicSeatApplyList(
        success: (List<ShowMicSeatApply>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun subscribeMicSeatApply(onMicSeatChange: (ShowServiceProtocol.ShowSubscribeStatus, ShowMicSeatApply) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun createMicSeatApply(success: (() -> Unit)?, error: ((Exception) -> Unit)?) {
        TODO("Not yet implemented")
    }

    override fun cancelMicSeatApply(success: (() -> Unit)?, error: ((Exception) -> Unit)?) {
        TODO("Not yet implemented")
    }

    override fun acceptMicSeatApply(
        apply: ShowMicSeatApply,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun rejectMicSeatApply(
        apply: ShowMicSeatApply,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllMicSeatInvitationList(
        success: (List<ShowMicSeatInvitation>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun subscribeMicSeatInvitation(onMicSeatInvitationChange: (ShowServiceProtocol.ShowSubscribeStatus, ShowMicSeatInvitation) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun createMicSeatInvitation(
        user: ShowUser,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun cancelMicSeatInvitation(
        user: ShowUser,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun acceptMicSeatInvitation(
        invitation: ShowMicSeatInvitation,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun rejectMicSeatInvitation(
        invitation: ShowMicSeatInvitation,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }


    private fun initSync(complete: () -> Unit) {
        if (syncInitialized) {
            complete.invoke()
            return
        }
        syncInitialized = true
        Sync.Instance().init(
            context,
            mutableMapOf(Pair("appid", BuildConfig.AGORA_APP_ID), Pair("defaultChannel", kSceneId)),
            object : Sync.Callback {
                override fun onSuccess() {
                    Handler(Looper.getMainLooper()).post { complete.invoke() }
                }

                override fun onFail(exception: SyncManagerException?) {
                    syncInitialized = false
                    errorHandler.invoke(exception!!)
                }
            }
        )
    }

}