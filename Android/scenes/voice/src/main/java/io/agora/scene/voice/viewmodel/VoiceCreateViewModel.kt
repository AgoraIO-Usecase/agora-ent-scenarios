package io.agora.scene.voice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.CallBack
import io.agora.chat.adapter.EMAError
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.voice.R
import io.agora.scene.voice.global.VoiceCenter
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.VoiceCreateRoomModel
import io.agora.scene.voice.netkit.CHATROOM_CREATE_TYPE_USER
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.widget.toast.CustomToast

/**
 * Create room & Room list etc.
 *
 * @author create by zhangwei03
 */
class VoiceCreateViewModel : ViewModel() {

    /**
     * voice chat protocol
     */
    private val voiceServiceProtocol by lazy {
        VoiceServiceProtocol.serviceProtocol
    }

    private val _roomListObservable: SingleSourceLiveData<List<AUIRoomInfo>?> =
        SingleSourceLiveData()

    private val _createRoomObservable: SingleSourceLiveData<AUIRoomInfo?> =
        SingleSourceLiveData()

    private val _joinRoomObservable: SingleSourceLiveData<AUIRoomInfo?> =
        SingleSourceLiveData()

    val roomListObservable: LiveData<List<AUIRoomInfo>?> get() = _roomListObservable

    val createRoomObservable: LiveData<AUIRoomInfo?> get() = _createRoomObservable

    val joinRoomObservable: LiveData<AUIRoomInfo?> get() = _joinRoomObservable

    fun checkLoginIm(completion: (error: Exception?) -> Unit) {
        VoiceToolboxServerHttpManager.createImRoom(
            roomName = "",
            roomOwner = "",
            chatroomId = "",
            type = CHATROOM_CREATE_TYPE_USER){ resp, error ->
            if (error == null && resp != null) {
                resp.chatToken?.let {
                    VoiceCenter.chatToken =it
                }
                ChatroomIMManager.getInstance().login(VoiceCenter.chatUid,  VoiceCenter.chatToken, object : CallBack {
                    override fun onSuccess() {
                        completion.invoke(null)
                    }

                    override fun onError(code: Int, desc: String) {
                        if (code == EMAError.USER_ALREADY_LOGIN) {
                            completion.invoke(null)
                        } else {
                            completion.invoke(Exception(desc))
                            CustomToast.show(R.string.voice_room_login_exception)
                        }
                    }
                })
            } else {
                completion.invoke(error)
            }
        }
    }

    /**
     * Get room list
     */
    fun getRoomList() {
        voiceServiceProtocol.getRoomList(completion = { error, result ->
            _roomListObservable.postValue(result)
            error?.message?.let {
                CustomToast.show(it)
            }
        })
    }

    /**
     * Create normal room
     * @param roomName Room name
     * @param soundEffect Room sound effect type
     * @param password Private room password
     */
    fun createRoom(roomName: String, soundEffect: Int = 0, password: String? = null) {
        val voiceCreateRoomModel = VoiceCreateRoomModel(
            roomName = roomName,
            soundEffect = soundEffect,
            password = password ?: "",
        )
        voiceServiceProtocol.createRoom(voiceCreateRoomModel, completion = { err, result ->
            if (err == null && result != null) {
                _createRoomObservable.postValue(result)
            } else {
                _createRoomObservable.postValue(null)
                CustomToast.show(
                    AgoraApplication.the().getString(R.string.voice_create_room_failed, err?.message ?: "")
                )
            }
        })
    }

    /**
     * Join room
     * @param roomId Room ID
     */
    fun joinRoom(roomId: String, password: String? = null) {
        checkLoginIm(completion = { error ->
            if (error == null) {
                voiceServiceProtocol.joinRoom(roomId, password, completion = { err, result ->
                    if (err == null && result != null) { // success
                        _joinRoomObservable.postValue(result)
                    } else {
                        _joinRoomObservable.postValue(null)
                        CustomToast.show(
                            AgoraApplication.the().getString(R.string.voice_join_room_failed, err?.message ?: "")
                        )
                    }
                })
            } else {
                _joinRoomObservable.postValue(null)
                CustomToast.show(AgoraApplication.the().getString(R.string.voice_join_room_failed, error))
            }
        })
    }
}