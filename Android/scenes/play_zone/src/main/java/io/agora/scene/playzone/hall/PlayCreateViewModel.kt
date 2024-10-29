package io.agora.scene.playzone.hall

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.playzone.R
import io.agora.scene.playzone.service.PlayChatRoomService
import io.agora.scene.playzone.service.PlayCreateRoomModel
import io.agora.scene.playzone.service.PlayZoneServiceProtocol
import io.agora.scene.playzone.service.api.PlayApiManager
import io.agora.scene.playzone.service.api.PlayGameInfoModel
import io.agora.scene.playzone.service.api.PlayGameListModel

class PlayCreateViewModel : ViewModel() {

    private val mPlayServiceProtocol by lazy { PlayZoneServiceProtocol.serviceProtocol }

    val loginImLiveData = MutableLiveData<Boolean>()
    val roomModelListLiveData = MutableLiveData<List<AUIRoomInfo>?>()
    val createRoomInfoLiveData = MutableLiveData<AUIRoomInfo?>()
    val joinRoomInfoLiveData = MutableLiveData<AUIRoomInfo?>()

    val mGameListLiveData = MutableLiveData<List<PlayGameListModel>>()

    private val mChatRoomService by lazy {
        PlayChatRoomService.chatRoomService
    }

    // 登录 IM
    fun checkLoginIm() {
        mChatRoomService.imManagerService.loginChat { error ->
            loginImLiveData.postValue(error == null)
            error?.message?.let {
                ToastUtils.showToast(it)
            }
        }
    }

    fun getGameList(vendor: GameVendor) {
        // only test
//        playApiManager.getSubGameApiInfo { error, gameApi ->
//            if (gameApi != null) {
//                playApiManager.getSubGameList(gameApi.api.get_mg_list) { gameError, list ->
//                    if (gameError == null) {
//                    } else {
//                        ToastUtils.showToast(gameError.message ?: "获取游戏列表失败")
//                    }
//                }
//            } else if (error != null) {
//                ToastUtils.showToast(error.message ?: "未知错误")
//            }
//        }

        PlayApiManager.getGameList(vendor, completion = { error, gameList ->
            if (error == null && gameList != null) {
                mGameListLiveData.postValue(gameList!!)
            } else {
                error?.message?.let {
                    ToastUtils.showToast(it)
                }
            }
        })
    }

    fun getRoomList() {
        mPlayServiceProtocol.getRoomList { error, vlRoomListModels ->
            roomModelListLiveData.postValue(vlRoomListModels)
            error?.message?.let {
                ToastUtils.showToast(it)
            }
        }
    }

    fun createRoom(gameInfoModel: PlayGameInfoModel, roomName: String, password: String) {
        innerCreateChatRoom(roomName) { chatId, error ->
            if (error != null || chatId.isNullOrEmpty()) {
                createRoomInfoLiveData.postValue(null)
                ToastUtils.showToastLong(
                    AgoraApplication.the().getString(R.string.play_zone_create_room_failed, error?.message ?: "")
                )
                return@innerCreateChatRoom
            }
            val createRoomModel = PlayCreateRoomModel(
                roomName = roomName,
                password = password,
                gameId = gameInfoModel.gameId,
                gameName = gameInfoModel.gameName,
                chatRoomId = chatId
            )
            mPlayServiceProtocol.createRoom(createRoomModel, completion = { error, roomInfo ->
                if (error == null && roomInfo != null) {
                    createRoomInfoLiveData.postValue(roomInfo)
                } else {
                    createRoomInfoLiveData.postValue(null)
                    ToastUtils.showToastLong(
                        AgoraApplication.the().getString(R.string.play_zone_create_room_failed, error?.message ?: "")
                    )
                }
            })
        }
    }

    private fun innerCreateChatRoom(roomName: String, completion: (chatId: String?, error: Exception?) -> Unit) {
        mChatRoomService.imManagerService.createChatRoom(
            roomName = roomName,
            description = "welcome",
            completion = { chatId, error ->
                completion.invoke(chatId, error)
            })
    }

    fun joinRoom(roomInfo: AUIRoomInfo, password: String?) {
        mPlayServiceProtocol.joinRoom(roomInfo.roomId, password) { error ->
            if (error == null) { // success
                joinRoomInfoLiveData.postValue(roomInfo)
            } else {
                joinRoomInfoLiveData.postValue(null)
                ToastUtils.showToastLong(
                    AgoraApplication.the().getString(R.string.play_zone_join_room_failed, error?.message ?: "")
                )
            }
        }
    }
}