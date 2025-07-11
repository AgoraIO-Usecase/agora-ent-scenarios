package io.agora.scene.cantata.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.agora.scene.cantata.service.CreateRoomInputModel
import io.agora.scene.cantata.service.CreateRoomOutputModel
import io.agora.scene.cantata.service.CantataServiceProtocol.Companion.getImplInstance
import io.agora.scene.cantata.service.JoinRoomInputModel
import io.agora.scene.cantata.service.JoinRoomOutputModel
import io.agora.scene.cantata.service.RoomListModel
import io.agora.scene.widget.toast.CustomToast

class RoomCreateViewModel constructor(application: Application) : AndroidViewModel(application) {
    private val ktvServiceProtocol = getImplInstance()
    val roomModelList = MutableLiveData<List<RoomListModel>?>()
    val joinRoomResult = MutableLiveData<JoinRoomOutputModel?>()
    val createRoomResult = MutableLiveData<CreateRoomOutputModel?>()

    /**
     * 加载房间列表
     */
    fun loadRooms() {
        ktvServiceProtocol.getRoomList { e: Exception?, vlRoomListModels: List<RoomListModel>? ->
            if (e != null) {
                roomModelList.postValue(null)
            }
            roomModelList.postValue(vlRoomListModels)
        }
    }

    fun createRoom(
        isPrivate: Int,
        name: String, password: String,
        userNo: String, icon: String,
        delayType: Int
    ) {
        ktvServiceProtocol.createRoom(
            CreateRoomInputModel(icon, isPrivate, name, password, userNo, delayType)
        ) { e: Exception?, createRoomOutputModel: CreateRoomOutputModel? ->
            if (e == null && createRoomOutputModel != null) {
                // success
                createRoomResult.postValue(createRoomOutputModel)
            } else {
                // failed
                e?.message?.let {  exception ->
                    CustomToast.show(exception)
                }
                createRoomResult.postValue(null)
            }
        }
    }

    fun joinRoom(roomNo: String, password: String?) {
        ktvServiceProtocol.joinRoom(
            JoinRoomInputModel(roomNo, password)
        ) { e: Exception?, joinRoomOutputModel: JoinRoomOutputModel? ->
            if (e == null && joinRoomOutputModel != null) {
                // success
                joinRoomResult.postValue(joinRoomOutputModel)
            } else {
                // failed
                e?.message?.let {  exception ->
                    CustomToast.show(exception)
                }
                joinRoomResult.postValue(null)
            }
        }
    }
}