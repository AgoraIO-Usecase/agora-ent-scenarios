package io.agora.scene.grandchorus.service

import io.agora.scene.grandchorus.GrandChorusLogger
import io.agora.scene.base.component.AgoraApplication

interface GrandChorusServiceProtocol {

    enum class KTVSubscribe {
        KTVSubscribeCreated,      //创建
        KTVSubscribeDeleted,      //删除
        KTVSubscribeUpdated,      //更新
    }

    companion object {
        private val instance by lazy {
            // KTVServiceImp()
            GrandChorusSyncManagerServiceImp(AgoraApplication.the()) { error ->
                error?.message?.let { GrandChorusLogger.e("SyncManager", it) }
            }
        }

        fun getImplInstance(): GrandChorusServiceProtocol = instance
    }

    fun reset()

    // ============== 房间相关 ==============

    /**
     * 获取房间列表
     */
    fun getRoomList(completion: (error: Exception?, list: List<RoomListModel>?) -> Unit)

    /**
     * 创建房间
     */
    fun createRoom(
        inputModel: CreateRoomInputModel,
        completion: (error: Exception?, out: CreateRoomOutputModel?) -> Unit
    )

    /**
     * 加入房间
     */
    fun joinRoom(
        inputModel: JoinRoomInputModel,
        completion: (error: Exception?, out: JoinRoomOutputModel?) -> Unit
    )

    /**
     * 离开房间
     */
    fun leaveRoom(
        completion: (error: Exception?) -> Unit
    )

    /**
     * 切换MV封面
     */
    fun changeMVCover(
        inputModel: ChangeMVCoverInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * room status did changed
     */
    fun subscribeRoomStatus(
        changedBlock: (KTVSubscribe, RoomListModel?) -> Unit
    )

    /**
     * user count did changed
     */
    fun subscribeUserListCount(
        changedBlock: (count: Int) -> Unit
    )

    fun subscribeRoomTimeUp(
        onRoomTimeUp: () -> Unit
    )


    // ===================== 麦位相关 =================================

    /**
     * 获取麦位列表
     */
    fun getSeatStatusList(
        completion: (error: Exception?, list: List<RoomSeatModel>?) -> Unit
    )

    /**
     * 上麦
     */
    fun onSeat(
        inputModel: OnSeatInputModel,
        completion: (error: Exception?) -> Unit
    )

    fun autoOnSeat(
        completion: (error: Exception?) -> Unit
    )

    /**
     * 下麦
     */
    fun outSeat(
        inputModel: OutSeatInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 设置麦位静音
     */
    fun updateSeatAudioMuteStatus(
        mute: Boolean, completion: (error: Exception?) -> Unit
    )

    /**
     * 打开麦位摄像头
     */
    fun updateSeatVideoMuteStatus(
        mute: Boolean, completion: (error: Exception?) -> Unit
    )

    /**
     * seat list did changed
     */
    fun subscribeSeatList(
        changedBlock: (KTVSubscribe, RoomSeatModel?) -> Unit
    )

    // =================== 歌曲相关 =========================

    /**
     * 获取选择歌曲列表
     */
    fun getChoosedSongsList(
        completion: (error: Exception?, list: List<RoomSelSongModel>?) -> Unit
    )

    /**
     * 删除歌曲
     */
    fun removeSong(
        isSingingSong: Boolean, inputModel: RemoveSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 点歌
     */
    fun chooseSong(
        inputModel: ChooseSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 置顶歌曲
     */
    fun makeSongTop(
        inputModel: MakeSongTopInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 标识歌曲正在播放
     */
    fun makeSongDidPlay(inputModel: RoomSelSongModel, completion: (error: Exception?) -> Unit)

    /**
     * 加入合唱
     */
    fun joinChorus(
        inputModel: RoomSelSongModel,
        completion: (error: Exception?) -> Unit
    )

    /**
     * 合唱者离开合唱
     */
    fun leaveChorus(
        completion: (error: Exception?) -> Unit
    )

    /**
     * song did changed
     */
    fun subscribeChooseSong(
        changedBlock: (KTVSubscribe, RoomSelSongModel?) -> Unit
    )

    // =================== 断网重连相关 =========================

    /**
     * 订阅重连事件
     */
    fun subscribeReConnectEvent(onReconnect: () -> Unit)

    /**
     * 拉取房间内用户列表
     */
    fun getAllUserList(success: (userNum : Int) -> Unit, error: ((Exception) -> Unit)? = null)
}