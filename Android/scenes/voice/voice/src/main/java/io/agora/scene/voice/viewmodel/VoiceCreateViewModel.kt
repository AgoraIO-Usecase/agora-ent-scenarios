package io.agora.scene.voice.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.scene.voice.viewmodel.repositories.VoiceCreateRepository
import io.agora.voice.common.net.Resource
import io.agora.voice.common.viewmodel.SingleSourceLiveData

/**
 * 创建房间 && 房间列表等
 *
 * @author create by zhangwei03
 */
class VoiceCreateViewModel constructor(application: Application) : AndroidViewModel(application) {

    private val voiceRoomRepository by lazy { VoiceCreateRepository() }

    private val _roomListObservable: SingleSourceLiveData<Resource<List<VoiceRoomModel>>> =
        SingleSourceLiveData()

    private val _checkPasswordObservable: SingleSourceLiveData<Resource<Boolean>> =
        SingleSourceLiveData()

    private val _createRoomObservable: SingleSourceLiveData<Resource<VoiceRoomModel>> =
        SingleSourceLiveData()

    private val _joinRoomObservable: SingleSourceLiveData<Resource<VoiceRoomModel>> =
        SingleSourceLiveData()

    fun roomListObservable(): LiveData<Resource<List<VoiceRoomModel>>> = _roomListObservable

    fun checkPasswordObservable(): LiveData<Resource<Boolean>> = _checkPasswordObservable

    fun createRoomObservable(): LiveData<Resource<VoiceRoomModel>> = _createRoomObservable

    fun joinRoomObservable(): LiveData<Resource<VoiceRoomModel>> = _joinRoomObservable

    /**
     * 获取房间列表
     * @param page 第几页，暂未用到
     */
    fun getRoomList(page: Int) {
        _roomListObservable.setSource(voiceRoomRepository.fetchRoomList(page))
    }

    /**
     * 私密房间密码校验，本地模拟验证
     * @param roomId 房间id
     * @param password 房间密码
     * @param userInput 用户输入
     */
    fun checkPassword(roomId: String, password: String, userInput: String) {
        _checkPasswordObservable.setSource(voiceRoomRepository.checkPassword(roomId, password, userInput))
    }

    /**
     * 创建普通房间
     * @param roomName 房间名
     * @param soundEffect 房间音效类型
     * @param password  私有房间，有秘密
     */
    fun createRoom(roomName: String, soundEffect: Int = 0, password: String? = null) {
        _createRoomObservable.setSource(voiceRoomRepository.createRoom(roomName, soundEffect, 0, password))
    }

    /**
     * 创建3d音频房间
     * @param roomName 房间名
     * @param soundEffect 房间音效类型
     * @param password  私有房间，有秘密
     */
    fun createSpatialRoom(roomName: String, soundEffect: Int = 0, password: String? = null) {
        _createRoomObservable.setSource(voiceRoomRepository.createRoom(roomName, soundEffect, 0, password))
    }

    /**
     * 加入房间
     * @param roomId 房间id
     */
    fun joinRoom(roomId: String) {
        _joinRoomObservable.setSource(voiceRoomRepository.joinRoom(roomId))
    }
}