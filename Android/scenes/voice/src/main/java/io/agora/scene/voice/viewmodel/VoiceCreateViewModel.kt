package io.agora.scene.voice.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.CallBack
import io.agora.chat.adapter.EMAError
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.voice.R
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.VoiceCreateRoomModel
import io.agora.scene.voice.netkit.CHATROOM_CREATE_TYPE_USER
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.service.VoiceServiceProtocol

/**
 * 创建房间 && 房间列表等
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
                    VoiceBuddyFactory.get().getVoiceBuddy().setupChatToken(it)
                }
                val chatUsername = VoiceBuddyFactory.get().getVoiceBuddy().chatUserName()
                val chatToken = VoiceBuddyFactory.get().getVoiceBuddy().chatToken()

                ChatroomIMManager.getInstance().login(chatUsername, chatToken, object : CallBack {
                    override fun onSuccess() {
                        completion.invoke(null)
                    }

                    override fun onError(code: Int, desc: String) {
                        if (code == EMAError.USER_ALREADY_LOGIN) {
                            completion.invoke(null)
                        } else {
                            completion.invoke(Exception(desc))
                            ToastUtils.showToast(R.string.voice_room_login_exception)
                        }
                    }
                })
            } else {
                completion.invoke(error)
            }
        }
    }

    /**
     * 获取房间列表
     */
    fun getRoomList() {
        voiceServiceProtocol.getRoomList(completion = { error, result ->
            _roomListObservable.postValue(result)
            error?.message?.let {
                ToastUtils.showToast(it)
            }
        })
    }

    /**
     * 创建普通房间
     * @param roomName 房间名
     * @param soundEffect 房间音效类型
     * @param password  私有房间，有秘密
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
                ToastUtils.showToast(
                    AgoraApplication.the().getString(R.string.voice_create_room_failed, err?.message ?: "")
                )
            }
        })
    }

    /**
     * 加入房间
     * @param roomId 房间id
     */
    fun joinRoom(roomId: String, password: String? = null) {
        checkLoginIm(completion = { error ->
            if (error == null) {
                voiceServiceProtocol.joinRoom(roomId, password, completion = { err, result ->
                    if (err == null && result != null) { // success
                        _joinRoomObservable.postValue(result)
                    } else {
                        _joinRoomObservable.postValue(null)
                        ToastUtils.showToast(
                            AgoraApplication.the().getString(R.string.voice_join_room_failed, err?.message ?: "")
                        )
                    }
                })
            } else {
                _joinRoomObservable.postValue(null)
                ToastUtils.showToast(AgoraApplication.the().getString(R.string.voice_join_room_failed, error))
            }
        })
    }
}