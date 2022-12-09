package io.agora.scene.ktv.service


interface KTVServiceProtocol {

    enum class KTVSubscribe {
        KTVSubscribeCreated,      //创建
        KTVSubscribeDeleted,      //删除
        KTVSubscribeUpdated,      //更新
    }

    companion object {
        private val instance by lazy { KTVServiceImp() }
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
    fun createRoomWithInput(
        inputModel: KTVCreateRoomInputModel,
        completion: (error: Exception?, out: KTVCreateRoomOutputModel?) -> Unit
    )

    /**
     * 加入房间
     */
    fun joinRoomWithInput(
        inputModel: KTVJoinRoomInputModel,
        completion: (error: Exception?, out: KTVJoinRoomOutputModel?) -> Unit
    )

    /**
     * 离开房间
     */
    fun leaveRoomWithCompletion(
        completion: (error: Exception?) -> Unit
    )

    /**
     * 切换MV封面
     */
    fun changeMVCoverWithInput(
        inputModel: KTVChangeMVCoverInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * room status did changed
     */
    fun subscribeRoomStatusWithChanged(
        changedBlock: (KTVSubscribe, VLRoomListModel?) -> Unit
    )

    /**
     * user count did changed
     */
    fun subscribeUserListCountWithChanged(
        changedBlock: (count: Int) -> Unit
    )


    // ===================== 麦位相关 =================================

    /**
     * 上麦
     */
    fun onSeatWithInput(
        inputModel: KTVOnSeatInputModel,
        completion: (error: Exception?) -> Unit
    )

    /**
     * 下麦
     */
    fun outSeatWithInput(
        inputModel: KTVOutSeatInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 设置麦位静音
     */
    fun muteWithMuteStatus(
        isSelfMuted: Int, completion: (error: Exception?) -> Unit
    )

    /**
     * 打开麦位摄像头
     */
    fun openVideoStatusWithStatus(
        isVideoMuted: Int, completion: (error: Exception?) -> Unit
    )

    /**
     * seat list did changed
     */
    fun subscribeSeatListWithChanged(
        changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit
    )

    // =================== 歌曲相关 =========================

    /**
     * 获取选择歌曲列表
     */
    fun getChoosedSongsListWithCompletion(
        completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit
    )

    /**
     * 删除歌曲
     */
    fun removeSongWithInput(
        inputModel: KTVRemoveSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 歌曲详情
     */
    fun getSongDetailWithInput(
        inputModel: KTVSongDetailInputModel,
        completion: (error: Exception?, out: KTVSongDetailOutputModel) -> Unit
    )


    /**
     * 切歌
     */
    fun switchSongWithInput(
        inputModel: KTVSwitchSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 点歌
     */
    fun chooseSongWithInput(
        inputModel: KTVChooseSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 主唱告诉后台当前播放的歌曲
     */
    fun markSongDidPlayWithInput(
        inputModel: VLRoomSelSongModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 置顶歌曲
     */
    fun makeSongTopWithInput(
        inputModel: KTVMakeSongTopInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 加入合唱
     */
    fun joinChorusWithInput(
        inputModel: KTVJoinChorusInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 当前歌曲合唱改为独唱
     */
    fun becomeSolo()

    /**
     * song did changed
     */
    fun subscribeChooseSongWithChanged(
        changedBlock: (KTVSubscribe, VLRoomSelSongModel?) -> Unit
    )

}