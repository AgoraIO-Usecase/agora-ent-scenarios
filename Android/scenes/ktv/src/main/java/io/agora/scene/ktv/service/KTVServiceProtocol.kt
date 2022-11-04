package io.agora.scene.ktv.service


interface KTVServiceProtocol {

    /**
     * 获取房间列表
     */
    fun getRoomListWithPage(
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
     * 切换MV封面
     */
    fun changeMVCoverWithInput(
        inputModel: KTVChangeMVCoverInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 上麦
     */
    fun onSeatWithInput(
        inputModel: KTVOnSeatInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 下麦
     */
    fun outSeatWithInput(
        inputModel: KTVOnSeatInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 离开房间
     */
    fun leaveRoomWithCompletion(
        completion: (error: Exception?) -> Unit
    )

    /**
     * 删除房间
     */
    fun removeRoomWithCompletion(
        completion: (error: Exception?) -> Unit
    )

    /**
     * 删除歌曲
     */
    fun removeSongWithInput(
        inputModel: KTVRemoveSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 获取选择歌曲列表
     */
    fun getChoosedSongsListWithCompletion(
        completion: (error: Exception?, list: List<VLRoomSelSongModel>) -> Unit
    )

    /**
     * 加入合唱
     */
    fun joinChorusWithInput(
        inputModel: KTVJoinChorusInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 歌曲详情
     */
    fun getSongDetailWithInput(
        inputModel: KTVSongDetailInputModel,
        completion: (error: Exception?, out: KTVSongDetailOutputModel) -> Unit
    )

    /**
     * 主唱告诉后台当前播放的歌曲
     */
    fun markSongDidPlayWithInput(
        inputModel: VLRoomSelSongModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 点歌
     */
    fun chooseSongWithInput(
        inputModel: KTVChooseSongInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * 置顶歌曲
     */
    fun makeSongTopWithInput(
        inputModel: KTVMakeSongTopInputModel, completion: (error: Exception?) -> Unit
    )

    /**
     * user count did changed
     */
    fun subscribeUserListCountWithChanged(
        changedBlock: (count: Int) -> Unit
    )

    /**
     * seat list did changed
     */
    fun subscribeSeatListWithChanged(
        changedBlock: (Int, VLRoomSeatModel) -> Unit
    )

    /**
     * room status did changed
     */
    fun subscribeRoomStatusWithChanged(
        changedBlock: (Int, VLRoomListModel) -> Unit
    )

    /**
     * song did changed
     */
    fun subscribeChooseSongWithChanged(
        changedBlock: (Int, VLRoomSelSongModel) -> Unit
    )

    fun muteWithMuteStatus(
        mute: Boolean, completion: (error: Exception?) -> Unit
    )

    fun openVideoStatusWithStatus(
        openStatus: Boolean, completion: (error: Exception?) -> Unit
    )

    fun publishChooseSongEvent()

    fun leaveChannel()

    fun publishMuteEventWithMuteStatus(
        muteStatus: Boolean, completion: (error: Exception?) -> Unit
    )

    fun publishVideoOpenEventWithOpenStatus(
        openStatus: Boolean, completion: (error: Exception?) -> Unit
    )

    fun publishSongDidChangedEventWithOwnerStatus(
        isMaster: Boolean
    )

    fun publishToSoloEvent()

    fun publishJoinToChorusWithCompletion(
        completion: (error: Exception?) -> Unit
    )

    fun publishSongOwnerWithOwnerId(
        userNo: String
    )

    fun publishSingingScoreWithTotalVolume(
        totalVolume: Double
    )

}