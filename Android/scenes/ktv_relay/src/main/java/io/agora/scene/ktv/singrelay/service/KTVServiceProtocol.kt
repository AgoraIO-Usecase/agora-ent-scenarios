package io.agora.scene.ktv.singrelay.service

import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.ktv.singrelay.KTVLogger

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */
interface KTVServiceProtocol {

    enum class KTVSubscribe {
        KTVSubscribeCreated,      //创建
        KTVSubscribeDeleted,      //删除
        KTVSubscribeUpdated,      //更新
    }

    companion object {

        private var instance : KTVServiceProtocol? = null
            get() {
                if (field == null) {
                    field = KTVSyncManagerServiceImp(AgoraApplication.the()){ error ->
                        error?.message?.let { KTVLogger.e("SyncManager", it) }
                    }
                }
                return field
            }

        @Synchronized
        fun getImplInstance(): KTVServiceProtocol = instance!!

        @Synchronized
        fun destroy() {
            (instance as? KTVSyncManagerServiceImp)?.reset()
            instance = null
        }
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
        changedBlock: (KTVServiceProtocol.KTVSubscribe, RoomSeatModel?) -> Unit
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

    // =================== 抢唱游戏相关相关 =========================
    fun prepareSingRelayGame(completion: (error: Exception?) -> Unit)

    fun startSingRelayGame(
        completion: (error: Exception?) -> Unit
    )

    fun finishSingRelayGame(
        rank: Map<String, RankModel>,
        completion: (error: Exception?) -> Unit
    )

    fun getSingRelayGameInfo(
        completion: (error: Exception?, info: SingRelayGameModel?) -> Unit
    )

    fun updateSongModel(
        songCode: String,
        winner: String,
        winnerName: String,
        headUrl: String,
        completion: (error: Exception?) -> Unit
    )

    fun subscribeSingRelayGame(
        changedBlock: (KTVSubscribe, SingRelayGameModel?) -> Unit
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