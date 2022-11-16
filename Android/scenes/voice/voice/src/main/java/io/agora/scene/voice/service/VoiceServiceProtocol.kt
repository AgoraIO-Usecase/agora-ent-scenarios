package io.agora.scene.voice.service

import io.agora.scene.voice.bean.GiftBean
import io.agora.voice.buddy.tool.LogTools.logE

/**
 * @author create by zhangwei03
 *
 * voice chat room protocol define
 */
interface VoiceServiceProtocol {

    enum class VoiceChatSubscribe {
        VoiceSubscribeCreated, //创建
        VoiceSubscribeDeleted, //删除
        VoiceSubscribeUpdated, //更新
    }

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
     * 获取房间列表
     * @param page 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
     * @param type 房间类型
     */
    fun fetchRoomList(
        page: Int = 0,
        type: Int = 0,
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
     * @param password 房间密码
     * @param needConvertConfig 是否需要重新获取token && im 配置，创建房间后加入不需要(false), 直接加入需要(true)
     */
    fun joinRoom(
        roomId: String,
        password: String = "",
        needConvertConfig: Boolean = false,
        completion: (error: Int, result: Boolean) -> Unit
    )

    /**
     * 获取房间详情
     * @param roomId 房间id
     */
    fun fetchRoomDetail(roomId: String, completion: (error: Int, result: VoiceRoomModel) -> Unit)

    /**
     * 离开房间
     * @param roomId 房间id
     * @param isOwner 是否是房主
     */
    fun leaveRoom(roomId: String, isOwner: Boolean, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 邀请用户上麦
     * @param roomId 房间id
     * @param userId 用户id
     */
    fun inviteUserToMic(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 拒绝上麦
     * @param roomId 房间id
     * @param userId 用户id
     */
    fun refuseInviteToMic(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 禁言指定麦位置
     * @param roomId 房间id
     * @param micIndex 麦位index
     */
    fun forbidMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 取消禁言指定麦位置
     * @param roomId 房间id
     * @param micIndex 麦位index
     */
    fun unForbidMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 锁麦
     * @param roomId 房间id
     * @param micIndex 麦位index
     */
    fun lockMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 取消锁麦
     * @param roomId 房间id
     * @param micIndex 麦位index
     */
    fun unLockMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 踢用户下麦
     * @param roomId 房间id
     * @param micIndex 麦位index
     */
    fun kickOff(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 下麦
     * @param roomId 房间id
     * @param micIndex 麦位index
     */
    fun leaveMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * mute
     * @param roomId 房间id
     * @param micIndex 麦位index
     */
    fun muteLocal(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * unMute
     * @param roomId 房间id
     * @param micIndex 麦位index
     */
    fun unMuteLocal(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 换麦
     * @param roomId 房间id
     * @param userId 用户id
     * @param oldIndex 老麦位index
     * @param newIndex 新麦位index
     */
    fun changeMic(
        roomId: String,
        userId: String,
        oldIndex: Int,
        newIndex: Int,
        completion: (error: Int, result: Boolean) -> Unit
    )

    /**
     * 取消邀请
     * @param roomId 房间id
     * @param userId 用户id
     */
    fun refuseInvite(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 接受邀请
     * @param roomId 房间id
     * @param userId 用户id
     */
    fun agreeInvite(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 申请上麦
     * @param roomId 房间id
     * @param userId 用户id
     */
    fun submitApply(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 取消上麦
     * @param roomId 房间id
     * @param userId 用户id
     */
    fun cancelApply(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 发送礼物上麦
     * @param roomId 房间id
     * @param giftInfo 礼物
     */
    fun sendGift(roomId: String, giftInfo: GiftBean, completion: (error: Int, result: Boolean) -> Unit)

    /**
     * 获取排行榜列表
     * @param roomId 房间id
     */
    fun fetchGiftContribute(roomId: String, completion: (error: Int, result: VoiceRankUserModel) -> Unit)

    /**
     * 获取用户列表
     * @param roomId 房间id
     */
    fun fetchRoomMembers(roomId: String, completion: (error: Int, result: VoiceMemberModel) -> Unit)

    /**
     * 激活机器人,修改公告，修改机器人音量
     * @param roomId 房间id
     * @param key 需要修改的key
     * @param value 需要修改的value
     */
    fun modifyRoomInfo(roomId: String, key: String, value: String, completion: (error: Int, result: Boolean) -> Unit)
}