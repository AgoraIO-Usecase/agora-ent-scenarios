package io.agora.scene.voice.spatial.service

import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.global.VoiceBuddyFactory
import io.agora.scene.voice.spatial.model.*

/**
 * @author create by zhangwei03
 *
 * voice chat room protocol define
 */
interface VoiceServiceProtocol {

    companion object {

        const val ERR_OK = 0
        const val ERR_FAILED = 1
        const val ERR_LOGIN_ERROR = 2
        const val ERR_LOGIN_SUCCESS = 3
        const val ERR_ROOM_UNAVAILABLE = 4
        const val ERR_ROOM_NAME_INCORRECT = 5
        const val ERR_ROOM_LIST_EMPTY = 1003
        private val instance by lazy {
            // VoiceChatServiceImp()
            VoiceSyncManagerServiceImp(VoiceBuddyFactory.get().getVoiceBuddy().application()) { error ->
                VoiceSpatialLogger.e("VoiceServiceProtocol ", "voice chat protocol error：${error?.message}")
            }
        }

        @JvmStatic
        fun getImplInstance(): VoiceServiceProtocol = instance
    }

    /**
     * 注册订阅
     * @param delegate 聊天室内IM回调处理
     */
    fun subscribeEvent(delegate: VoiceRoomSubscribeDelegate)

    /**
     *  取消订阅
     */
    fun unsubscribeEvent()

    fun reset()

    fun getSubscribeDelegates(): MutableList<VoiceRoomSubscribeDelegate>

    /**
     * 获取房间列表
     * @param page 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
     */
    fun fetchRoomList(
        page: Int = 0,
        completion: (error: Int, result: List<VoiceRoomModel>) -> Unit
    )

    /**
     * 创建房间
     * @param inputModel 输入的房间信息
     */
    fun createRoom(inputModel: VoiceCreateRoomModel, completion: (error: Int, result: VoiceRoomModel) -> Unit)

    /**
     * 加入房间
     * @param roomId 房间id
     */
    fun joinRoom(roomId: String, completion: (error: Int, result: VoiceRoomModel?) -> Unit)

    /**
     * 离开房间
     * @param roomId 房间id
     */
    fun leaveRoom(roomId: String, isRoomOwnerLeave: Boolean, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 获取房间详情
     * @param voiceRoomModel 房间概要
     */
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel, completion: (error: Int, result: VoiceRoomInfo?) -> Unit)

    /**
     * 获取用户列表
     */
    fun fetchRoomMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit)

    /**
     * 申请列表
     */
    fun fetchApplicantsList(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit)

    /**
     * 申请上麦
     * @param micIndex 麦位index
     */
    fun startMicSeatApply(micIndex: Int? = null, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 同意申请
     * @param userId 用户id
     */
    fun acceptMicSeatApply(userId: String, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * 取消上麦
     * @param chatUid im uid
     */
    fun cancelMicSeatApply(userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 邀请用户上麦
     * @param chatUid im uid
     */
    fun startMicSeatInvitation(userId: String, micIndex: Int?, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 接受邀请
     */
    fun acceptMicSeatInvitation(completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * 拒绝邀请
     */
    fun refuseInvite(completion: (error: Int, result: Boolean) -> Unit)

    /**
     * mute
     * @param mute
     */
    fun muteLocal(mute: Boolean, completion: (error: Int, result: VoiceMemberModel?) -> Unit)

    /**
     * 禁言指定麦位置
     * @param micIndex 麦位index
     */
    fun forbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * 取消禁言指定麦位置
     * @param micIndex 麦位index
     */
    fun unForbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * 锁麦
     * @param micIndex 麦位index
     */
    fun lockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * 取消锁麦
     * @param micIndex 麦位index
     */
    fun unLockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * 踢用户下麦
     * @param micIndex 麦位index
     */
    fun kickOff(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * 下麦
     * @param micIndex 麦位index
     */
    fun leaveMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit)

    /**
     * 换麦
     * @param oldIndex 老麦位index
     * @param newIndex 新麦位index
     */
    fun changeMic(
        oldIndex: Int,
        newIndex: Int,
        completion: (error: Int, result: Map<Int, VoiceMicInfoModel>?) -> Unit
    )

    /**
     * 更新公告
     * @param content 公告内容
     */
    fun updateAnnouncement(content: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 更新机器人配置
     * @param info 机器人配置
     */
    fun updateRobotInfo(info: RobotSpatialAudioModel, completion: (error: Int, result: Boolean) -> Unit)

    fun subscribeRoomTimeUp(
        onRoomTimeUp: () -> Unit
    )
}