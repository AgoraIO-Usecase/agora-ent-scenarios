package io.agora.scene.grandchorus.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.grandchorus.service.CreateRoomInputModel
import io.agora.scene.grandchorus.service.CreateRoomOutputModel
import io.agora.scene.grandchorus.service.GrandChorusServiceProtocol.Companion.getImplInstance
import io.agora.scene.grandchorus.service.JoinRoomInputModel
import io.agora.scene.grandchorus.service.JoinRoomOutputModel
import io.agora.scene.grandchorus.service.RoomListModel

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
                ToastUtils.showToast(e.message)
                roomModelList.postValue(null)
            }
            roomModelList.postValue(vlRoomListModels)
        }
    }

    fun createRoom(
        isPrivate: Int,
        name: String, password: String,
        userNo: String, icon: String
    ) {
        ktvServiceProtocol.createRoom(
            CreateRoomInputModel(icon, isPrivate, name, password, userNo)
        ) { e: Exception?, createRoomOutputModel: CreateRoomOutputModel? ->
            if (e == null && createRoomOutputModel != null) {
                // success
                createRoomResult.postValue(createRoomOutputModel)
            } else {
                // failed
                e?.let {  exception ->
                    ToastUtils.showToast(exception.message)
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
                e?.let {  exception ->
                    ToastUtils.showToast(exception.message)
                }
                joinRoomResult.postValue(null)
            }
        }
    }
}