package io.agora.scene.voice.spatial.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.agora.scene.voice.spatial.model.VoiceRoomModel
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.net.SingleSourceLiveData
import io.agora.scene.voice.spatial.viewmodel.repositories.VoiceCreateRepository

/**
 * Create room && room list
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
     * Get room list
     * @param page Page number, not used yet
     */
    fun getRoomList(page: Int) {
        _roomListObservable.setSource(voiceRoomRepository.fetchRoomList(page))
    }

    /**
     * Private room password verification, local simulation verification
     * @param roomId Room ID
     * @param password Room password
     * @param userInput User input
     */
    fun checkPassword(roomId: String, password: String, userInput: String) {
        _checkPasswordObservable.setSource(voiceRoomRepository.checkPassword(roomId, password, userInput))
    }

    /**
     * Create normal room
     * @param roomName Room name
     * @param soundEffect Room sound effect type
     * @param password Private room, with secret
     */
    fun createRoom(roomName: String, soundEffect: Int = 0, password: String? = null) {
        _createRoomObservable.setSource(voiceRoomRepository.createRoom(roomName, soundEffect, 0, password))
    }

    /**
     * Create 3d audio room
     * @param roomName Room name
     * @param soundEffect Room sound effect type
     * @param password Private room, with secret
     */
    fun createSpatialRoom(roomName: String, soundEffect: Int = 0, password: String? = null) {
        _createRoomObservable.setSource(voiceRoomRepository.createRoom(roomName, soundEffect, 0, password))
    }

    /**
     * Join room
     * @param roomId Room ID
     */
    fun joinRoom(roomId: String) {
        _joinRoomObservable.setSource(voiceRoomRepository.joinRoom(roomId))
    }
}