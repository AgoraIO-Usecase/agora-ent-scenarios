package io.agora.scene.show.service

import android.content.Context
import io.agora.scene.base.BuildConfig
import io.agora.syncmanager.rtm.IObject
import io.agora.syncmanager.rtm.Sync
import io.agora.syncmanager.rtm.SyncManagerException

class ShowSyncManagerServiceImpl(
    private val context: Context,
    private val errorHandler: (Exception) -> Unit
) : ShowServiceProtocol {
    private val kSceneId = "scene_show"

    @Volatile
    private var syncInitialized = false

    override fun getRoomList(
        success: (List<ShowRoomListModel>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        initSync {
            Sync.Instance().getScenes(object : Sync.DataListCallback{
                override fun onSuccess(result: MutableList<IObject>?) {
                    val roomList = result!!.map { it.toObject(ShowRoomListModel::class.java).apply { objectId = it.id } }
                    success.invoke(roomList.sortedBy { it.crateAt })
                }

                override fun onFail(exception: SyncManagerException?) {
                    error?.invoke(exception!!)
                }
            })
        }
    }

    override fun createRoom(
        room: ShowRoomListModel,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun joinRoom(
        room: ShowRoomListModel,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun leaveRoom(success: (() -> Unit)?, error: ((Exception) -> Unit)?) {
        TODO("Not yet implemented")
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
            object : Sync.Callback{
                override fun onSuccess() {

                }

                override fun onFail(exception: SyncManagerException?) {
                    syncInitialized = false
                    errorHandler.invoke(exception!!)
                }
            }
        )
    }

}