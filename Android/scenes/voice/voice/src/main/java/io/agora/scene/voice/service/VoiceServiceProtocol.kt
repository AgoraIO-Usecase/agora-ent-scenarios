package io.agora.scene.voice.service

import io.agora.voice.buddy.tool.LogTools.logE

/**
 * @author create by zhangwei03
 *
 * voice chat room protocol define
 */
interface VoiceServiceProtocol {

    companion object {

        const val ERR_OK = 0
        const val ERR_FAILED = 1
        private val instance by lazy {
            // VoiceChatServiceImp()
            VoiceSyncManagerServiceImp(VoiceBuddyFactory.get().getVoiceBuddy().application()) { error ->
                error?.message?.let {
                    "voice chat protocol error：$it".logE()
                }
            }
        }

        @JvmStatic
        fun getImplInstance(): VoiceServiceProtocol = instance
    }

    /**
     * 注册订阅
     * @param delegate 聊天室内IM回调处理
     */
    fun subscribeEvent(delegate: VoiceRoomServiceSubscribeDelegate)

    /**
     *  取消订阅
     */
    fun unsubscribeEvent()

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
    fun createRoom(
        inputModel: VoiceCreateRoomModel,
        completion: (error: Int, result: VoiceRoomModel) -> Unit
    )

    /**
     * 加入房间
     * @param roomId 房间id
     * @param needConvertConfig 是否需要重新获取token && im 配置，创建房间后加入不需要(false), 直接加入需要(true)
     */
    fun joinRoom(
        roomId: String,
        needConvertConfig: Boolean = false,
        completion: (error: Int, result: Boolean) -> Unit
    )

    /**
     * 获取房间详情
     * @param voiceRoomModel 房间概要
     */
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel, completion: (error: Int, result: VoiceRoomInfo) -> Unit)

    /**
     * 离开房间
     * @param roomId 房间id
     */
    fun leaveRoom(roomId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 获取排行榜列表
     */
    fun fetchGiftContribute(completion: (error: Int, result: VoiceRankUserModel) -> Unit)

    /**
     * 获取用户列表
     */
    fun fetchRoomMembers(completion: (error: Int, result: VoiceMemberModel) -> Unit)

    /**
     * 邀请用户上麦
     * @param chatUid im uid
     */
    fun startMicSeatInvitation(chatUid: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 拒绝上麦
     * @param roomId 房间id
     * @param userId 用户id
     */
    fun refuseInviteToMic(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 禁言指定麦位置
     * @param micIndex 麦位index
     */
    fun forbidMic(micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 取消禁言指定麦位置
     * @param micIndex 麦位index
     */
    fun unForbidMic(micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 锁麦
     * @param micIndex 麦位index
     */
    fun lockMic(micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 取消锁麦
     * @param micIndex 麦位index
     */
    fun unLockMic(micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 踢用户下麦
     * @param micIndex 麦位index
     */
    fun kickOff(micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 下麦
     * @param micIndex 麦位index
     */
    fun leaveMic(micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * mute
     * @param micIndex 麦位index
     */
    fun muteLocal(micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * unMute
     * @param micIndex 麦位index
     */
    fun unMuteLocal(micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 换麦
     * @param oldIndex 老麦位index
     * @param newIndex 新麦位index
     */
    fun changeMic(oldIndex: Int, newIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 接受邀请
     */
    fun acceptMicSeatInvitation(completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 拒绝邀请
     */
    fun refuseInvite(completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 申请上麦
     */
    fun startMicSeatApply(micIndex: Int? = null, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 取消上麦
     * @param chatUid im uid
     */
    fun endMicSeatApply(chatUid: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 更新公告
     * @param content 公告内容
     */
    fun updateAnnouncement(content: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 是否启用机器人
     * @param enable true 启动机器人，false 关闭机器人
     */
    fun enableRobot(enable: Boolean, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 更新机器人音量
     * @param value 音量
     */
    fun updateRobotVolume(value: Int, completion: (error: Int, result: Boolean) -> Unit)
}