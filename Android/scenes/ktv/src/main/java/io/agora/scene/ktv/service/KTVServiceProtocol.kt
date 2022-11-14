package io.agora.scene.ktv.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.ToastUtils


interface KTVServiceProtocol {

    enum class KTVSubscribe {
        KTVSubscribeCreated,      //创建
        KTVSubscribeDeleted,      //删除
        KTVSubscribeUpdated,      //更新
    }

    companion object {
        private val instance by lazy {
            // KTVServiceImp()
            KTVSyncManagerServiceImp(AgoraApplication.the()) { error ->
                error?.message?.let { ToastUtils.showToast(it) }
            }
        }

        fun getImplInstance(): KTVServiceProtocol = instance
    }

    // ============== 房间相关 ==============

    /**
     * 获取房间列表
     */
    fun getRoomList(
        completion: (error: Exception?, list: List<VLRoomListModel>?) -> Unit
    )

    /**
     * 创建房间
     */
    fun createRoom(
        inputModel: KTVCreateRoomInputModel,
        completion: (error: Exception?, out: KTVCreateRoomOutputModel?) -> Unit
    )

    /**
     * 加入房间
     */
    fun joinRoom(
        inputModel: KTVJoinRoomInputModel,
        completion: (error: Exception?, out: KTVJoinRoomOutputModel?) -> Unit
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
        inputModel: KTVChangeMVCoverInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * room status did changed
     */
    fun subscribeRoomStatus(
        changedBlock: (KTVSubscribe, VLRoomListModel?) -> Unit
    )

    /**
     * user count did changed
     */
    fun subscribeUserListCount(
        changedBlock: (count: Int) -> Unit
    )


    // ===================== 麦位相关 =================================

    /**
     * 上麦
     */
    fun onSeat(
        inputModel: KTVOnSeatInputModel,
        completion: (error: Exception?) -> Unit
    )

    /**
     * 下麦
     */
    fun outSeat(
        inputModel: KTVOutSeatInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 设置麦位静音
     */
    fun openAudioStatus(
        isSelfMuted: Int, completion: (error: Exception?) -> Unit
    )

    /**
     * 打开麦位摄像头
     */
    fun openVideoStatus(
        isVideoMuted: Int, completion: (error: Exception?) -> Unit
    )

    /**
     * seat list did changed
     */
    fun subscribeSeatList(
        changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit
    )

    // =================== 歌曲相关 =========================

    /**
     * 获取选择歌曲列表
     */
    fun getChoosedSongsList(
        completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit
    )

    /**
     * 删除歌曲
     */
    fun removeSong(
        inputModel: KTVRemoveSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 切歌
     */
    fun switchSong(
        inputModel: KTVSwitchSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 点歌
     */
    fun chooseSong(
        inputModel: KTVChooseSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 置顶歌曲
     */
    fun makeSongTop(
        inputModel: KTVMakeSongTopInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 加入合唱
     */
    fun joinChorus(
        inputModel: KTVJoinChorusInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 当前歌曲合唱改为独唱
     */
    fun becomeSolo()

    /**
     * song did changed
     */
    fun subscribeChooseSong(
        changedBlock: (KTVSubscribe, VLRoomSelSongModel?) -> Unit
    )

}