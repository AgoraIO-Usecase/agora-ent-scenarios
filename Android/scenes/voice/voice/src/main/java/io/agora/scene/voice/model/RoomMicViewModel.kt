package io.agora.scene.voice.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import io.agora.scene.voice.general.repositories.RoomMicRepository
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.network.tools.bean.VRMicBean
import io.agora.voice.network.tools.bean.VRMicListBean

/**
 * @author create by zhangwei03
 */
class RoomMicViewModel constructor(application: Application) : AndroidViewModel(application) {

    private val mRepository by lazy { RoomMicRepository() }

    private val _applyMicListObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<VRMicListBean>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _micInfoObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<VRMicBean>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _closeMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _cancelCloseMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _leaveMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _muteMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _cancelMuteMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _kickMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _rejectMicInvitationObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Boolean>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _lockMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _cancelLockMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Pair<Int, Boolean>>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _invitationMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Boolean>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _applySubmitMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Boolean>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()
    private val _rejectSubmitMicObservable: io.agora.scene.voice.general.livedatas.SingleSourceLiveData<Resource<Boolean>> =
        io.agora.scene.voice.general.livedatas.SingleSourceLiveData()

    fun applyMicListObservable(): LiveData<Resource<VRMicListBean>> = _applyMicListObservable
    fun micInfoObservable(): LiveData<Resource<VRMicBean>> = _micInfoObservable
    fun closeMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _closeMicObservable
    fun cancelCloseMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _cancelCloseMicObservable
    fun leaveMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _leaveMicObservable
    fun muteMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _muteMicObservable
    fun cancelMuteMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _cancelMuteMicObservable
    fun kickMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _kickMicObservable
    fun rejectMicInvitationObservable(): LiveData<Resource<Boolean>> = _rejectMicInvitationObservable
    fun lockMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _lockMicObservable
    fun cancelLockMicObservable(): LiveData<Resource<Pair<Int, Boolean>>> = _cancelLockMicObservable
    fun invitationMicObservable(): LiveData<Resource<Boolean>> = _invitationMicObservable
    fun applySubmitMicObservable(): LiveData<Resource<Boolean>> = _applySubmitMicObservable
    fun rejectSubmitMicObservable(): LiveData<Resource<Boolean>> = _rejectSubmitMicObservable

    // 获取上麦申请列表
    fun getApplyMicList(context: Context, roomId: String, cursor: String, micIndex: Int) {
        _applyMicListObservable.setSource(mRepository.getApplyMicList(context, roomId, cursor, micIndex))
    }

    // 获取麦位信息
    fun getMicInfo(context: Context, roomId: String) {
        _micInfoObservable.setSource(mRepository.getMicInfo(context, roomId))
    }

    // 关麦
    fun closeMic(context: Context, roomId: String, micIndex: Int) {
        _closeMicObservable.setSource(mRepository.closeMic(context, roomId, micIndex))
    }

    // 取消关麦
    fun cancelCloseMic(context: Context, roomId: String, micIndex: Int) {
        _cancelCloseMicObservable.setSource(mRepository.cancelCloseMic(context, roomId, micIndex))
    }

    // 下麦
    fun leaveMicMic(context: Context, roomId: String, micIndex: Int) {
        _leaveMicObservable.setSource(mRepository.leaveMicMic(context, roomId, micIndex))
    }

    // 禁言指定麦位
    fun muteMic(context: Context, roomId: String, micIndex: Int) {
        _muteMicObservable.setSource(mRepository.muteMic(context, roomId, micIndex))
    }

    // 取消指定麦位禁言
    fun cancelMuteMic(context: Context, roomId: String, micIndex: Int) {
        _cancelMuteMicObservable.setSource(mRepository.cancelMuteMic(context, roomId, micIndex))
    }

    // 踢用户下麦
    fun kickMic(context: Context, roomId: String, userId: String, micIndex: Int) {
        _kickMicObservable.setSource(mRepository.kickMic(context, roomId, userId, micIndex))
    }

    // 用户拒绝上麦申请
    fun rejectMicInvitation(context: Context, roomId: String) {
        _rejectMicInvitationObservable.setSource(mRepository.rejectMicInvitation(context, roomId))
    }

    // 锁麦
    fun lockMic(context: Context, roomId: String, micIndex: Int) {
        _lockMicObservable.setSource(mRepository.lockMic(context, roomId, micIndex))
    }

    // 取消锁麦
    fun cancelLockMic(context: Context, roomId: String, micIndex: Int) {
        _cancelLockMicObservable.setSource(mRepository.cancelLockMic(context, roomId, micIndex))
    }

    //  邀请上麦
    fun invitationMic(context: Context, roomId: String, userId: String) {
        _invitationMicObservable.setSource(mRepository.invitationMic(context, roomId, userId))
    }

    // 同意上麦申请
    fun applySubmitMic(context: Context, roomId: String, userId: String, micIndex: Int) {
        _applySubmitMicObservable.setSource(mRepository.applySubmitMic(context, roomId, userId, micIndex))
    }

    // 拒绝上麦申请
    fun rejectSubmitMic(context: Context, roomId: String, userId: String) {
        _rejectSubmitMicObservable.setSource(mRepository.rejectSubmitMic(context, roomId, userId))
    }
}