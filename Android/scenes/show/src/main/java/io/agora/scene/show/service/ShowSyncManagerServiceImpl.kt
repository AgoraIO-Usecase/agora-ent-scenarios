package io.agora.scene.show.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.manager.UserManager
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.EventListener
import kotlin.random.Random

class ShowSyncManagerServiceImpl(
    private val context: Context,
    private val errorHandler: (Exception) -> Unit
) : ShowServiceProtocol {

    private val kSceneId = "scene_show"
    private val kCollectionIdUser = "userCollection"
    private val kCollectionIdMessage = "messageInfo"

    @Volatile
    private var syncInitialized = false


    // global cache data
    private val roomMap = mutableMapOf<String, ShowRoomDetailModel>()
    private val objIdOfUserId = mutableMapOf<String, String>() // key: userId, value: objectId

    // current room cache data
    private var currRoomNo: String = ""
    private var currSceneReference: SceneReference? = null
    private val currEventListeners = mutableListOf<EventListener>()

    private var currUserChangeSubscriber: ((ShowServiceProtocol.ShowSubscribeStatus, ShowUser?) -> Unit)? =
        null

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
                    roomList.forEach { roomMap[it.roomId] = it.copy() }
                    success.invoke(roomList.sortedBy { it.createAt })
                }

                override fun onFail(exception: SyncManagerException?) {
                    error?.invoke(exception!!) ?: errorHandler.invoke(exception!!)
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
                UserManager.getInstance().user.id.toString(),
                ShowRoomStatus.activity.value,
                System.currentTimeMillis().toDouble(),
                System.currentTimeMillis().toDouble()
            )
            val scene = Scene().apply {
                id = roomDetail.roomId
                userId = roomDetail.ownerId
                property = roomDetail.toMap()
            }
            Sync.Instance().createScene(
                scene,
                object : Sync.Callback {
                    override fun onSuccess() {
                        roomMap[roomDetail.roomId] = roomDetail.copy()
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
        if (currRoomNo.isNotEmpty()) {
            error?.invoke(RuntimeException("There is a room joined or joining now!"))
            return
        }
        if (roomMap[roomNo] == null) {
            error?.invoke(RuntimeException("The room has been destroyed!"))
            return
        }
        currRoomNo = roomNo
        initSync {
            Sync.Instance().joinScene(
                roomNo, object : Sync.JoinSceneCallback {
                    override fun onSuccess(sceneReference: SceneReference?) {
                        this@ShowSyncManagerServiceImpl.currSceneReference = sceneReference!!
                        innerMayAddLocalUser({
                            innerSubscribeUserChange()
                            success.invoke(roomMap[roomNo]!!)
                        }, {
                            error?.invoke(it) ?: errorHandler.invoke(it)
                            currRoomNo = ""
                        })
                    }

                    override fun onFail(exception: SyncManagerException?) {
                        error?.invoke(exception!!) ?: errorHandler.invoke(exception!!)
                        currRoomNo = ""
                    }
                }
            )
        }
    }

    override fun leaveRoom() {
        if (currRoomNo.isEmpty()) {
            return
        }
        val roomDetail = roomMap[currRoomNo] ?: return
        val sceneReference = currSceneReference ?: return

        currEventListeners.forEach {
            sceneReference.unsubscribe(it)
        }
        currEventListeners.clear()

        if (roomDetail.ownerId == UserManager.getInstance().user.id.toString()) {
            val roomNo = currRoomNo
            sceneReference.delete(object : Sync.Callback {
                override fun onSuccess() {
                    roomMap.remove(roomNo)
                }

                override fun onFail(exception: SyncManagerException?) {
                    errorHandler.invoke(exception!!)
                }
            })
        }

        innerRemoveUser(
            UserManager.getInstance().user.id.toString(),
            {},
            { errorHandler.invoke(it) }
        )

        currRoomNo = ""
        currSceneReference = null
    }

    override fun getAllUserList(success: (List<ShowUser>) -> Unit, error: ((Exception) -> Unit)?) {
        innerGetUserList(success) {
            error?.invoke(it) ?: errorHandler.invoke(it)
        }
    }

    override fun subscribeUser(onUserChange: (ShowServiceProtocol.ShowSubscribeStatus, ShowUser?) -> Unit) {
        currUserChangeSubscriber = onUserChange;
    }

    override fun sendChatMessage(
        message: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val sceneReference = currSceneReference ?: return
        sceneReference.collection(kCollectionIdMessage)
            .add(ShowMessage(
                UserManager.getInstance().user.id.toString(),
                UserManager.getInstance().user.name,
                message,
                System.currentTimeMillis().toDouble()
            ), object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject?) {
                    success?.invoke()
                }

                override fun onFail(exception: SyncManagerException?) {
                    error?.invoke(exception!!) ?: errorHandler.invoke(exception!!)
                }
            })
    }

    override fun subscribeMessage(onMessageChange: (ShowServiceProtocol.ShowSubscribeStatus, ShowMessage) -> Unit) {
        val sceneReference = currSceneReference ?: return
        val listener = object: EventListener{
            override fun onCreated(item: IObject?) {
                // do nothing
            }

            override fun onUpdated(item: IObject?) {
                item?: return
                onMessageChange.invoke(ShowServiceProtocol.ShowSubscribeStatus.updated, item.toObject(ShowMessage::class.java))
            }

            override fun onDeleted(item: IObject?) {

            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                errorHandler.invoke(ex!!)
            }
        }
        currEventListeners.add(listener)
        sceneReference.collection(kCollectionIdMessage)
            .subscribe(listener)
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

    private fun innerUpdateRoomUserCount(
        userCount: Int,
        success: () -> Unit,
        error: (Exception) -> Unit
    ) {
        val roomInfo = roomMap[currRoomNo] ?: return
        val sceneReference = currSceneReference ?: return

        val nRoomInfo = ShowRoomDetailModel(
            roomInfo.roomId,
            roomInfo.roomName,
            userCount,
            roomInfo.thumbnailId,
            roomInfo.ownerId,
            roomInfo.roomStatus,
            roomInfo.createAt,
            roomInfo.updateAt
        )
        sceneReference.update(nRoomInfo.toMap(), object : Sync.DataItemCallback {
            override fun onSuccess(result: IObject?) {
                roomMap[currRoomNo] = nRoomInfo
                success.invoke()
            }

            override fun onFail(exception: SyncManagerException?) {
                error.invoke(exception!!)
            }
        })
    }

    private fun innerMayAddLocalUser(success: () -> Unit, error: (Exception) -> Unit) {
        val userId = UserManager.getInstance().user.id.toString()
        innerGetUserList({ list ->
            if (list.none { it.userId == it.toString() }) {
                innerAddUser(ShowUser(userId, "1", UserManager.getInstance().user.name),
                    {
                        objIdOfUserId[userId] = it
                        innerUpdateRoomUserCount(list.size + 1, {
                            success.invoke()
                        }, { ex ->
                            error.invoke(ex)
                        })
                    },
                    {
                        error.invoke(it)
                    })
            } else {
                success.invoke()
            }
        }, {
            error.invoke(it)
        })
    }

    private fun innerGetUserList(success: (List<ShowUser>) -> Unit, error: (Exception) -> Unit) {
        val sceneReference = currSceneReference ?: return
        sceneReference.collection(kCollectionIdUser)
            .get(object : Sync.DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    result ?: return
                    val map = result.map { it.toObject(ShowUser::class.java) }
                    success.invoke(map)
                }

                override fun onFail(exception: SyncManagerException?) {
                    error.invoke(exception!!)
                }
            })
    }

    private fun innerAddUser(
        user: ShowUser,
        success: (String) -> Unit,
        error: (Exception) -> Unit
    ) {
        val sceneReference = currSceneReference ?: return
        sceneReference.collection(kCollectionIdUser)
            .add(user, object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject?) {
                    success.invoke(result?.id!!)
                }

                override fun onFail(exception: SyncManagerException?) {
                    error.invoke(exception!!)
                }
            })
    }

    private fun innerRemoveUser(
        userId: String,
        success: () -> Unit,
        error: (Exception) -> Unit
    ) {
        val sceneReference = currSceneReference ?: return
        val objectId = objIdOfUserId[userId] ?: return
        sceneReference.collection(kCollectionIdUser)
            .delete(objectId, object : Sync.Callback {
                override fun onSuccess() {
                    success.invoke()
                }

                override fun onFail(exception: SyncManagerException?) {
                    error.invoke(exception!!)
                }
            })
    }

    private fun innerSubscribeUserChange() {
        val sceneReference = currSceneReference ?: return
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {
                // do nothing
            }

            override fun onUpdated(item: IObject?) {
                val user = item?.toObject(ShowUser::class.java) ?: return
                objIdOfUserId[user.userId] = item.id
                currUserChangeSubscriber?.invoke(
                    ShowServiceProtocol.ShowSubscribeStatus.updated,
                    user
                )
            }

            override fun onDeleted(item: IObject?) {
                currUserChangeSubscriber?.invoke(
                    ShowServiceProtocol.ShowSubscribeStatus.deleted,
                    null
                )
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                errorHandler.invoke(ex!!)
            }
        }
        currEventListeners.add(listener)
        sceneReference.collection(kCollectionIdUser)
            .subscribe(listener)
    }
}