package io.agora.scene.ktv.singrelay.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.manager.UserManager
import io.agora.scene.ktv.singrelay.KTVLogger
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.*
import java.util.concurrent.CountDownLatch
import kotlin.random.Random


/**
 * 使用SyncManager进行数据交互
 *
 *
 */
class KTVSyncManagerServiceImp(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : KTVServiceProtocol {
    private val TAG = "KTV_Service_LOG"
    private val kSceneId = "scene_singrelay_3.5.0"
    private val kCollectionIdChooseSong = "choose_song"
    private val kCollectionIdSeatInfo = "seat_info"
    private val kCollectionIdUser = "userCollection"
    private val kCollectionSingRelayGameInfo = "sing_battle_game_info"

    private data class VLLoginModel(
        val userNo: String,
        //val isWaitAutoOnSeat: Boolean = false
    )

    @Volatile
    private var syncUtilsInited = false

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var mSceneReference: SceneReference? = null

    // subscribers
    private var roomStatusSubscriber: ((KTVServiceProtocol.KTVSubscribe, RoomListModel?) -> Unit)? =
        null
    private var roomUserCountSubscriber: ((Int) -> Unit)? =
        null
    private var seatListChangeSubscriber: ((KTVServiceProtocol.KTVSubscribe, RoomSeatModel?) -> Unit)? =
        null
    private var chooseSongSubscriber: ((KTVServiceProtocol.KTVSubscribe, RoomSelSongModel?) -> Unit)? =
        null
    private var onReconnectSubscriber: (() -> Unit)? = null
    private var roomTimeUpSubscriber: (() -> Unit)? = null
    private var SingRelayGameSubscribe: ((KTVServiceProtocol.KTVSubscribe, SingRelayGameModel?) -> Unit)? = null

    // cache objectId
    private val objIdOfRoomNo = HashMap<String, String>() // objectId of room no
    private val objIdOfSongNo = ArrayList<String>() // objectId of song no
    private val objIdOfSeatIndex = HashMap<Int, String>() // objectId of seat index
    private val objIdOfUserNo = HashMap<String, String>() // objectId of user no

    // cache data
    private val roomSubscribeListener = mutableListOf<EventListener>()
    private val roomMap = mutableMapOf<String, RoomListModel>() // key: roomNo
    private val userMap = mutableMapOf<String, VLLoginModel?>() // key: userNo
    private val seatMap = mutableMapOf<String, RoomSeatModel?>() // key: seatIndex
    private val songChosenList = ArrayList<RoomSelSongModel>()

    private var SingRelayGameInfo: SingRelayGameModel? = null
    private var objIdOfSingRelayGameInfo: String? = null

    @Volatile
    private var currRoomNo: String = ""

    // time limit
    private val ROOM_AVAILABLE_DURATION : Long = 20 * 60 * 1000 // 20min
    private val timerRoomEndRun = Runnable {
        runOnMainThread {
            KTVLogger.d(TAG, "time up exit room!")
            roomTimeUpSubscriber?.invoke()
        }
    }

    override fun reset() {
        if (syncUtilsInited) {
            Instance().destroy()
            syncUtilsInited = false

            objIdOfRoomNo.clear()
            objIdOfSongNo.clear()
            objIdOfSeatIndex.clear()
            objIdOfUserNo.clear()
            objIdOfSingRelayGameInfo = null

            roomSubscribeListener.clear()
            roomMap.clear()
            userMap.clear()
            seatMap.clear()
            songChosenList.clear()
            SingRelayGameInfo = null
            currRoomNo = ""
        }
    }

    // ========= 房间相关 =====================

    override fun getRoomList(
        completion: (error: Exception?, list: List<RoomListModel>?) -> Unit
    ) {
        initSync {
            Instance().getScenes(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    roomMap.clear()
                    val ret = ArrayList<RoomListModel>()
                    result?.forEach {
                        val obj = it.toObject(RoomListModel::class.java)
                        objIdOfRoomNo[obj.roomNo] = it.id
                        ret.add(obj)

                        roomMap[obj.roomNo] = obj
                    }
                    //按照创建时间顺序排序
                    ret.sortBy { it.createdAt }
                    runOnMainThread { completion.invoke(null, ret) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception, null) }
                }
            })
        }
    }

    override fun createRoom(
        inputModel: CreateRoomInputModel,
        completion: (error: Exception?, out: CreateRoomOutputModel?) -> Unit
    ) {
        initSync {
            val roomListModel = RoomListModel(
                roomNo = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString(),
                name = inputModel.name,
                icon = inputModel.icon,
                isPrivate = inputModel.isPrivate != 0,
                password = inputModel.password,
                creatorNo = UserManager.getInstance().user.id.toString(),
                bgOption = "0",
            )
            val scene = Scene()
            scene.id = roomListModel.roomNo
            scene.userId = inputModel.userNo

            scene.property = GsonUtils.covertToMap(roomListModel)

            Instance().createScene(scene, object : Callback {
                override fun onSuccess() {
                    roomMap[roomListModel.roomNo] = roomListModel
                    runOnMainThread {
                        completion.invoke(
                            null,
                            CreateRoomOutputModel(
                                roomListModel.roomNo,
                                roomListModel.password
                            )
                        )
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception, null) }
                }
            })
        }
    }

    private var isJoined: Boolean = false
    override fun joinRoom(
        inputModel: JoinRoomInputModel,
        completion: (error: Exception?, out: JoinRoomOutputModel?) -> Unit
    ) {
        if (isJoined) {
            completion.invoke(RuntimeException("The room $currRoomNo has been joined!"), null)
            return
        }

        currRoomNo = ""
        if (!TextUtils.isEmpty(currRoomNo) && currRoomNo != inputModel.roomNo) return
        val cacheRoom = roomMap[inputModel.roomNo]
        if (cacheRoom == null) {
            completion.invoke(RuntimeException("The room is not available!"), null)
            return
        }
        if (cacheRoom.isPrivate && cacheRoom.password != inputModel.password) {
            completion.invoke(RuntimeException("The password is error!"), null)
            return
        }
        initSync {
            val isRoomOwner = cacheRoom.creatorNo == UserManager.getInstance().user.id.toString()
            Instance().joinScene(isRoomOwner, true, inputModel.roomNo, object : JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    mSceneReference = sceneReference
                    currRoomNo = inputModel.roomNo

                    TokenGenerator.generateTokens(
                        currRoomNo,
                        UserManager.getInstance().user.id.toString(),
                        TokenGenerator.TokenGeneratorType.token007,
                        arrayOf(
                            TokenGenerator.AgoraTokenType.rtc,
                            TokenGenerator.AgoraTokenType.rtm
                        ),
                        { ret ->
                            val rtcToken = ret[TokenGenerator.AgoraTokenType.rtc] ?: ""
                            val rtmToken = ret[TokenGenerator.AgoraTokenType.rtm] ?: ""
                            innerSubscribeRoomChanged()
                            innerSubscribeChooseSong {}
                            innerSubscribeSingRelayGameInfo {}
                            innerAddUserIfNeed { addUserError, userSize ->
                                if (addUserError != null) {
                                    completion.invoke(addUserError, null)
                                    return@innerAddUserIfNeed
                                }
                                innerAutoOnSeatIfNeed { error, seats ->
                                    if (error != null) {
                                        completion.invoke(error, null)
                                        return@innerAutoOnSeatIfNeed
                                    }

                                    TokenGenerator.generateToken(
                                        currRoomNo + "_ex",
                                        UserManager.getInstance().user.id.toString(),
                                        TokenGenerator.TokenGeneratorType.token007,
                                        TokenGenerator.AgoraTokenType.rtc,
                                        { chorusToken ->
                                            val kTVJoinRoomOutputModel = JoinRoomOutputModel(
                                                cacheRoom.name,
                                                inputModel.roomNo,
                                                cacheRoom.creatorNo,
                                                cacheRoom.bgOption,
                                                seats,
                                                userSize,
                                                rtmToken,
                                                rtcToken,
                                                chorusToken,
                                                cacheRoom.createdAt
                                            )
                                            runOnMainThread {
                                                completion.invoke(null, kTVJoinRoomOutputModel)
                                                isJoined = true
                                            }

                                            // 重置体验时间事件
                                            mainHandler.removeCallbacks(timerRoomEndRun)
                                            // 定时删除房间
                                            val expireLeftTime =
                                                ROOM_AVAILABLE_DURATION - (System.currentTimeMillis() - cacheRoom.createdAt.toLong())
                                            KTVLogger.d(TAG, "expireLeftTime: $expireLeftTime")
                                            mainHandler.postDelayed(timerRoomEndRun, expireLeftTime)
                                        },
                                        {
                                            completion.invoke(it, null)
                                        }
                                    )
                                }

                            }
                        },
                        {
                            completion.invoke(it, null)
                        }
                    )


                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception, null) }
                }
            })
        }
    }

    override fun leaveRoom(completion: (error: Exception?) -> Unit) {
        val cacheRoom = roomMap[currRoomNo] ?: return
        // 取消所有订阅
        roomSubscribeListener.forEach {
            mSceneReference?.unsubscribe(it)
        }
        roomSubscribeListener.clear()

        // 重置体验时间事件
        mainHandler.removeCallbacks(timerRoomEndRun)
        roomTimeUpSubscriber = null

        if (cacheRoom.creatorNo == UserManager.getInstance().user.id.toString()) {
            // 移除房间
            mSceneReference?.delete(object : Callback {
                override fun onSuccess() {
                    resetCacheInfo(true)
                    runOnMainThread {
                        completion.invoke(null)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
        } else {
            resetCacheInfo(false)
            // leave room
            runOnMainThread {
                completion.invoke(null)
            }
        }
    }

    private fun resetCacheInfo(isRoomDestroyed: Boolean = false) {

        // 减少用户数，并清空用户信息
        if (!isRoomDestroyed) {
            innerRemoveUser {}
            innerUpdateUserCount(userMap.size - 1)
        }

        userMap.clear()
        objIdOfUserNo.clear()

        // 如果上麦了要下麦，并清空麦位信息
        if (!isRoomDestroyed) {
            seatMap.forEach {
                it.value?.let { seat ->
                    if (seat.userNo == UserManager.getInstance().user.id.toString()) {
                        innerRemoveSeat(seat) {}
                        return@forEach
                    }
                }
            }
        }
        seatMap.clear()
        objIdOfSeatIndex.clear()

        // 删除点歌信息
        if (!isRoomDestroyed) {
            songChosenList.forEachIndexed { index: Int, songModel: RoomSelSongModel ->
                if (songModel.userNo.equals(UserManager.getInstance().user.id.toString())) {
                    innerRemoveChooseSong(objIdOfSongNo[index]) {}
                }
            }
        }
        objIdOfSongNo.clear()
        songChosenList.clear()

        if (isRoomDestroyed) {
            roomMap.remove(currRoomNo)
        }
        mSceneReference = null
        currRoomNo = ""
        isJoined = false
    }

    override fun changeMVCover(
        inputModel: ChangeMVCoverInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val roomInfo = roomMap[currRoomNo] ?: return
        mSceneReference?.update(
            HashMap(
                GsonUtils.covertToMap(
                    RoomListModel(
                        roomInfo.roomNo,
                        roomInfo.name,
                        roomInfo.icon,
                        roomInfo.isPrivate,
                        roomInfo.password,
                        roomInfo.creatorNo,
                        roomInfo.createdAt,
                        inputModel.mvIndex.toString(),
                        roomInfo.roomPeopleNum
                    )
                )
            ),
            object : DataItemCallback {
                override fun onSuccess(result: IObject?) {
                    runOnMainThread {
                        completion.invoke(null)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread {
                        completion.invoke(exception)
                    }
                }
            })
    }

    override fun subscribeRoomStatus(changedBlock: (KTVServiceProtocol.KTVSubscribe, RoomListModel?) -> Unit) {
        roomStatusSubscriber = changedBlock
    }

    override fun subscribeUserListCount(changedBlock: (count: Int) -> Unit) {
        roomUserCountSubscriber = changedBlock
    }

    override fun subscribeRoomTimeUp(onRoomTimeUp: () -> Unit) {
        roomTimeUpSubscriber = onRoomTimeUp
    }

    // =================== 麦位相关 ===============================

    override fun getSeatStatusList(
        completion: (error: Exception?, list: List<RoomSeatModel>?) -> Unit
    ) {
        innerGetSeatInfo(completion)
    }

    override fun onSeat(
        inputModel: OnSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        seatMap.forEach {
            it.value?.let { seat ->
                if (seat.userNo == UserManager.getInstance().user.id.toString()) {
                    return
                }
            }
        }
        val seatInfo = innerGenUserSeatInfo(inputModel.seatIndex)
        innerAddSeatInfo(seatInfo, completion)
    }

    override fun autoOnSeat(completion: (error: Exception?) -> Unit) {
        val list = mutableListOf<Int>(0, 1, 2, 3, 4, 5, 6, 7)
        seatMap.forEach {
            it.value?.let { seat ->
                list.removeIf { index ->
                    index == seat.seatIndex
                }
                if (seat.userNo == UserManager.getInstance().user.id.toString()) {
                    completion.invoke(null)
                    return
                }
            }
        }
        if (list.isEmpty()) {
            completion.invoke(java.lang.Exception("麦位已满，请在他人下麦后重试"))
        } else {
            val seatInfo = innerGenUserSeatInfo(list[0])
            innerAddSeatInfo(seatInfo, completion)
        }
    }

    override fun outSeat(
        inputModel: OutSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val seatInfo = seatMap[inputModel.userOnSeat.toString()]
        if (seatInfo != null) {
            // 移除歌曲
            innerRemoveAllUsersChooseSong(seatInfo.userNo)
            // 移除座位
            innerRemoveSeat(seatInfo) {}
        }
        completion(null)
    }

    override fun updateSeatAudioMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit) {
        seatMap.forEach {
            if (it.value?.userNo == UserManager.getInstance().user.id.toString()) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = RoomSeatModel(
                        originSeatInfo.isMaster,
                        originSeatInfo.headUrl,
                        originSeatInfo.userNo,
                        originSeatInfo.rtcUid,
                        originSeatInfo.name,
                        originSeatInfo.seatIndex,
                        originSeatInfo.chorusSongCode,
                        if (mute) RoomSeatModel.MUTED_VALUE_TRUE else RoomSeatModel.MUTED_VALUE_FALSE, // update this
                        originSeatInfo.isVideoMuted
                    )
                    innerUpdateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun updateSeatVideoMuteStatus(
        mute: Boolean,
        completion: (error: Exception?) -> Unit
    ) {
        seatMap.forEach {
            if (it.value?.userNo == UserManager.getInstance().user.id.toString()) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = RoomSeatModel(
                        originSeatInfo.isMaster,
                        originSeatInfo.headUrl,
                        originSeatInfo.userNo,
                        originSeatInfo.rtcUid,
                        originSeatInfo.name,
                        originSeatInfo.seatIndex,
                        originSeatInfo.chorusSongCode,
                        originSeatInfo.isAudioMuted,
                        if (mute) 1 else 0// update this
                    )
                    innerUpdateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun subscribeSeatList(changedBlock: (KTVServiceProtocol.KTVSubscribe, RoomSeatModel?) -> Unit) {
        seatListChangeSubscriber = changedBlock
    }


    // ============= 歌曲相关 =============================

    override fun getChoosedSongsList(completion: (error: Exception?, list: List<RoomSelSongModel>?) -> Unit) {
        innerGetChooseSongInfo(completion)
    }

    override fun chooseSong(
        inputModel: ChooseSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val song = RoomSelSongModel(
            inputModel.songName,
            inputModel.songNo,
            inputModel.singer,
            inputModel.imageUrl,

            userNo = UserManager.getInstance().user.id.toString(),
            name = UserManager.getInstance().user.name,

            status = RoomSelSongModel.STATUS_IDLE,
            createAt = System.currentTimeMillis().toLong(),
            pinAt = 0.0
        )
        //net request and notify others
        innerAddChooseSongInfo(song, completion)
    }

    override fun autoChooseSongAndStartGame(
        list: List<ChooseSongInputModel>,
        completion: (error: Exception?) -> Unit
    ) {

        val countDown = CountDownLatch(1)
        chooseSong(list[0]) {
            countDown.countDown()
        }

        Thread {
            countDown.await()
            startSingRelayGame(completion)
        }.start()
    }

    override fun makeSongTop(
        inputModel: MakeSongTopInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        // move the song to second position
        if (songChosenList.size <= 0) {
            completion.invoke(RuntimeException("The chosen song list is empty!"))
            return
        }
        if (songChosenList.size < 3) {
            completion.invoke(RuntimeException("The chosen songs size is less then three, it is unnecessary to top up!"))
            return
        }

        val filter = songChosenList.filter { it.songNo == inputModel.songNo }
        val targetSong = filter.getOrNull(0)
        if (targetSong == null) {
            completion.invoke(RuntimeException("The song no not found!"))
            return
        }

        //net request and notify others
        val indexOf = songChosenList.indexOf(targetSong)
        val newSong = RoomSelSongModel(
            targetSong.songName,
            targetSong.songNo,
            targetSong.singer,
            targetSong.imageUrl,

            userNo = targetSong.userNo,
            name = targetSong.name,
            isOriginal = targetSong.isOriginal,

            status = targetSong.status,
            createAt = targetSong.createAt,
            pinAt = System.currentTimeMillis().toDouble()
        )
        songChosenList[indexOf] = newSong

        innerPinSong(objIdOfSongNo[indexOf], newSong) {
            completion.invoke(it)
        }
    }

    override fun makeSongDidPlay(
        inputModel: RoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        if (songChosenList.size <= 0) {
            completion.invoke(RuntimeException("The chosen song list is empty!"))
            return
        }

        val filter = songChosenList.filter { it.songNo == inputModel.songNo }
        val targetSong = filter.getOrNull(0)
        if (targetSong == null) {
            completion.invoke(RuntimeException("The song no not found!"))
            return
        }
        if (targetSong.status == RoomSelSongModel.STATUS_PLAYING) {
            completion.invoke(RuntimeException("The song is playing now!"))
            return
        }

        val indexOf = songChosenList.indexOf(targetSong)
        val newSong = RoomSelSongModel(
            targetSong.songName,
            targetSong.songNo,
            targetSong.singer,
            targetSong.imageUrl,

            userNo = targetSong.userNo,
            name = targetSong.name,
            isOriginal = targetSong.isOriginal,
            "",

            status = RoomSelSongModel.STATUS_PLAYING,
            createAt = targetSong.createAt,
            pinAt = targetSong.pinAt
        )
        songChosenList[indexOf] = newSong


        innerDidPlaySong(objIdOfSongNo[indexOf], newSong) {
            completion.invoke(it)
        }

    }

    override fun removeSong(
        isSingingSong: Boolean,
        inputModel: RemoveSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        if (songChosenList.size <= 0) {
            completion.invoke(RuntimeException("The chosen song list is empty!"))
            return
        }
        val targetSong = songChosenList.filter { it.songNo == inputModel.songNo }.getOrNull(0)
        if (targetSong == null) {
            completion.invoke(RuntimeException("The song no not found!"))
            return
        }

        val indexOf = songChosenList.indexOf(targetSong)
        songChosenList.removeAt(indexOf)
        val removeAt = objIdOfSongNo.removeAt(indexOf)

//        if (isSingingSong) {
//            seatMap.forEach {
//                val originSeatInfo = it.value
//                if (originSeatInfo != null) {
//                    val seatInfo = RoomSeatModel(
//                        originSeatInfo.isMaster,
//                        originSeatInfo.headUrl,
//                        originSeatInfo.userNo,
//                        originSeatInfo.rtcUid,
//                        originSeatInfo.name,
//                        originSeatInfo.seatIndex,
//                        "",
//                        originSeatInfo.isAudioMuted,
//                        originSeatInfo.isVideoMuted
//                    )
//                    innerUpdateSeat(seatInfo, completion)
//                }
//            }
//        }

        //net request and notify others
        innerRemoveChooseSong(removeAt) {
            completion.invoke(it)
        }
    }

    override fun joinChorus(
        inputModel: RoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        //加入合唱
        innerGetSeatInfo { err, list ->
            if (err == null && list != null) {
                list.forEach { seat ->
                    if (seat.userNo == UserManager.getInstance().user.id.toString()) {
                        // 座位 joinSing -> true
                        val seatInfo = RoomSeatModel(
                            seat.isMaster,
                            seat.headUrl,
                            seat.userNo,
                            seat.rtcUid,
                            seat.name,
                            seat.seatIndex,
                            inputModel.songNo + inputModel.createAt,
                            RoomSeatModel.MUTED_VALUE_FALSE,
                            seat.isVideoMuted
                        )
                        innerUpdateSeat(seatInfo, completion)
                    }
                }
            }
        }
    }

    override fun leaveChorus(
        completion: (error: Exception?) -> Unit
    ) {
        seatMap.forEach {
            if (it.value?.userNo == UserManager.getInstance().user.id.toString()) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = RoomSeatModel(
                        originSeatInfo.isMaster,
                        originSeatInfo.headUrl,
                        originSeatInfo.userNo,
                        originSeatInfo.rtcUid,
                        originSeatInfo.name,
                        originSeatInfo.seatIndex,
                        "",
                        RoomSeatModel.MUTED_VALUE_TRUE,
                        originSeatInfo.isVideoMuted
                    )
                    innerUpdateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun subscribeChooseSong(changedBlock: (KTVServiceProtocol.KTVSubscribe, RoomSelSongModel?) -> Unit) {
        chooseSongSubscriber = changedBlock
    }

    override fun prepareSingRelayGame(completion: (error: Exception?) -> Unit) {
        val SingRelayGameModel = SingRelayGameModel(
            SingRelayGameStatus.waitting.value,
            null
        )
        if (objIdOfSingRelayGameInfo == null) {
            innerAddSingRelayGameInfo(SingRelayGameModel) {
                completion.invoke(it)
            }
        } else {
            innerUpdateSingRelayGameInfo(objIdOfSingRelayGameInfo!!, SingRelayGameModel) {
                completion.invoke(it)
            }
        }
    }

    override fun startSingRelayGame(
        completion: (error: Exception?) -> Unit
    ) {
        val SingRelayGameModel = SingRelayGameModel(
            SingRelayGameStatus.started.value,
            null
        )
        objIdOfSingRelayGameInfo?.let { objId ->
            innerUpdateSingRelayGameInfo(objId, SingRelayGameModel) {
                completion.invoke(it)
            }
        }
    }

    override fun finishSingRelayGame(
        rank: Map<String, RankModel>,
        completion: (error: Exception?) -> Unit
    ) {
        val SingRelayGameModel = SingRelayGameModel(
            SingRelayGameStatus.ended.value,
            rank
        )
        objIdOfSingRelayGameInfo?.let { objId ->
            innerUpdateSingRelayGameInfo(objId, SingRelayGameModel) {
                completion.invoke(it)
            }
        }
    }

    override fun getSingRelayGameInfo(completion: (error: Exception?, info: SingRelayGameModel?) -> Unit) {
        innerGetSingRelayGameInfo(completion)
    }

    override fun updateSongModel(
        songCode: String,
        winner: String,
        winnerName: String,
        headUrl: String,
        completion: (error: Exception?) -> Unit
    ) {
        val song = songChosenList.filter { it.songNo == songCode }.getOrNull(0) ?: return
        val index = songChosenList.indexOf(song)
        val newSong = RoomSelSongModel(
            song.songName,
            song.songNo,
            song.singer,
            headUrl,
            song.userNo,
            winnerName,
            song.isOriginal,
            winner,
            song.status,
            song.createAt,
            song.pinAt
        )
        innerUpdateChooseSong(objIdOfSongNo[index], newSong) {
            completion.invoke(it)
        }
    }

    override fun subscribeSingRelayGame(changedBlock: (KTVServiceProtocol.KTVSubscribe, SingRelayGameModel?) -> Unit) {
        SingRelayGameSubscribe = changedBlock
    }

    override fun subscribeReConnectEvent(onReconnect: () -> Unit) {
        onReconnectSubscriber = onReconnect
    }

    override fun getAllUserList(success: (userNum: Int) -> Unit, error: ((Exception) -> Unit)?) {
        innerGetUserInfo { err, list ->
            if (err != null) {
                error?.invoke(err)
                return@innerGetUserInfo
            }
            if (list != null) {
                success.invoke(list.size)
            }
        }
    }

    // ===================== 内部实现 =====================

    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }

    private fun initSync(complete: () -> Unit) {
        if (syncUtilsInited) {
            complete.invoke()
            return
        }

        Instance().init(
            RethinkConfig(io.agora.scene.base.BuildConfig.AGORA_APP_ID, kSceneId),
            object : Callback {
                override fun onSuccess() {
                    syncUtilsInited = true
                    runOnMainThread{
                        complete.invoke()
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { errorHandler?.invoke(exception) }
                }
            }
        )
        Instance().subscribeConnectState {
            if (it == ConnectionState.open) {
                runOnMainThread {
                    // 判断当前房间是否还存在
                    val oldRoomInfo = roomMap[currRoomNo]
                    if(oldRoomInfo != null){
                        getRoomList { _, _ ->
                            val roomInfo = roomMap[currRoomNo]
                            if (roomInfo == null) {
                                runOnMainThread {
                                    roomStatusSubscriber?.invoke(
                                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                                        oldRoomInfo
                                    )
                                }
                            }
                        }
                    }
                    onReconnectSubscriber?.invoke()
                }
            }
        }
    }


    // ------------------------ User operation ------------------------
    // 订阅在线用户
    private fun innerSubscribeOnlineUsers(completion: () -> Unit) {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                //将用户信息存在本地列表
                val userInfo = item.toObject(VLLoginModel::class.java)
                if (!userMap.containsKey(userInfo?.userNo)) {
                    userMap[userInfo?.userNo.toString()] = userInfo
                    objIdOfUserNo[userInfo?.userNo.toString()] = item.id
                }
                innerUpdateUserCount(userMap.size)
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                //将用户信息移除本地列表
                objIdOfUserNo.forEach { entry ->
                    if (entry.value == item.id) {
                        val removeUserNo = entry.key
                        userMap.remove(removeUserNo)
                        objIdOfUserNo.remove(entry.key)
                        runOnMainThread { roomUserCountSubscriber?.invoke(userMap.size) }
                        // TODO workaround: 暂时不更新防止房间被重建
                        //innerUpdateUserCount(userMap.size)
                        return
                    }
                }

            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.collection(kCollectionIdUser)?.subscribe(listener)
    }

    private fun innerGetUserInfo(completion: (error: Exception?, list: List<VLLoginModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdUser)?.get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VLLoginModel>()
                result?.forEach {
                    val obj = it.toObject(VLLoginModel::class.java)
                    objIdOfUserNo[obj.userNo] = it.id
                    ret.add(obj)

                    userMap[obj.userNo] = obj
                }
                innerUpdateUserCount(userMap.count())
                runOnMainThread { completion.invoke(null, ret) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception, null) }
            }
        })
    }

    private fun innerAddUserInfo(completion: () -> Unit) {
        val localUserInfo = VLLoginModel(UserManager.getInstance().user.id.toString())
        mSceneReference?.collection(kCollectionIdUser)
            ?.add(localUserInfo, object : DataItemCallback {
                override fun onSuccess(result: IObject) {
                    runOnMainThread { completion.invoke() }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke() }
                }
            })
    }

    private fun innerUpdateUserInfo(user: VLLoginModel, completion: (exception: SyncManagerException?) -> Unit) {
        val objectId = objIdOfUserNo[user.userNo] ?: return
        mSceneReference?.collection(kCollectionIdUser)
            ?.update(objectId, user, object : Callback {
                override fun onSuccess() {
                    completion.invoke(null)
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(exception)
                }
            })
    }

    private fun innerRemoveUser(completion: (error: Exception?) -> Unit) {
        val objectId = objIdOfUserNo[UserManager.getInstance().user.id.toString()] ?: return
        mSceneReference?.collection(kCollectionIdUser)?.delete(objectId, object : Callback {
            override fun onSuccess() {
                objIdOfUserNo.remove(UserManager.getInstance().user.id.toString())
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun innerAddUserIfNeed(completion: (Exception?, Int) -> Unit) {
        innerSubscribeOnlineUsers {}
        innerGetUserInfo { error, list ->
            if (error != null || list == null) {
                completion.invoke(error, 0)
                return@innerGetUserInfo
            }
            if (!userMap.containsKey(UserManager.getInstance().user.id.toString())) {
                innerAddUserInfo {
                    innerGetUserInfo { error2, list2 ->
                        if (error2 != null || list2 == null) {
                            completion.invoke(error2, 0)
                        } else {
                            completion.invoke(null, list2.size)
                        }
                    }
                }
            } else {
                completion.invoke(null, list.size)
            }
        }
    }

    private fun innerUpdateUserCount(count: Int) {
        val roomInfo = roomMap[currRoomNo] ?: return
        if (count == roomInfo.roomPeopleNum) {
            return
        }
        mSceneReference?.update(
            HashMap(
                GsonUtils.covertToMap(
                    RoomListModel(
                        roomInfo.roomNo,
                        roomInfo.name,
                        roomInfo.icon,
                        roomInfo.isPrivate,
                        roomInfo.password,
                        roomInfo.creatorNo,
                        roomInfo.createdAt,
                        roomInfo.bgOption,
                        count
                    )
                )
            ),
            object : DataItemCallback {
                override fun onSuccess(result: IObject?) {
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread {
                    }
                }
            })
        runOnMainThread { roomUserCountSubscriber?.invoke(count) }
    }

    // --------------------- Seat operation --------------------------

    private fun innerGenUserSeatInfo(seatIndex: Int): RoomSeatModel {
        return RoomSeatModel(
            roomMap[currRoomNo]?.creatorNo == UserManager.getInstance().user.id.toString(),
            UserManager.getInstance().user.headUrl,
            UserManager.getInstance().user.id.toString(),
            UserManager.getInstance().user.id.toString(),
            UserManager.getInstance().user.name,
            seatIndex,
            "",
            RoomSeatModel.MUTED_VALUE_TRUE,
            RoomSeatModel.MUTED_VALUE_TRUE
        )
    }

    private fun innerAutoOnSeatIfNeed(completion: (error: Exception?, seat: List<RoomSeatModel>?) -> Unit) {
        val cacheRoom = roomMap[currRoomNo] ?: return
        innerSubscribeSeats {}
        innerGetSeatInfo { err, _ ->
            if (err != null) {
                completion.invoke(err, null)
                return@innerGetSeatInfo
            }
            var hasMaster = false
            val outList = ArrayList<RoomSeatModel>()
            seatMap.forEach {
                it.value?.let { seat ->
                    outList.add(seat)
                    runOnMainThread {
                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                            seat
                        )
                    }
                    if (seat.isMaster) {
                        hasMaster = true
                    }
                }
            }
            if (!hasMaster && cacheRoom.creatorNo == UserManager.getInstance().user.id.toString()) {
                val targetSeatInfo = RoomSeatModel(
                    roomMap[currRoomNo]?.creatorNo == UserManager.getInstance().user.id.toString(),
                    UserManager.getInstance().user.headUrl,
                    UserManager.getInstance().user.id.toString(),
                    UserManager.getInstance().user.id.toString(),
                    UserManager.getInstance().user.name,
                    0,
                    "",
                    RoomSeatModel.MUTED_VALUE_FALSE,
                    RoomSeatModel.MUTED_VALUE_TRUE
                )
                innerAddSeatInfo(targetSeatInfo) { error ->
                    if (error != null) {
                        completion.invoke(error, null)
                        return@innerAddSeatInfo
                    }
                    outList.add(targetSeatInfo)
                    runOnMainThread{
                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                            targetSeatInfo
                        )
                        completion.invoke(null, outList)
                    }
                }
            } else {
                completion.invoke(null, outList)
            }
        }
    }


    private fun innerGetSeatInfo(completion: (error: Exception?, list: List<RoomSeatModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdSeatInfo)?.get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<RoomSeatModel>()
                result?.forEach {
                    val obj = it.toObject(RoomSeatModel::class.java)
                    objIdOfSeatIndex[obj.seatIndex] = it.id
                    ret.add(obj)

                    // 储存在本地map中
                    seatMap[obj.seatIndex.toString()] = obj
                }
                runOnMainThread { completion.invoke(null, ret) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception, null) }
            }
        })
    }

    private fun innerUpdateSeat(
        seatInfo: RoomSeatModel,
        completion: (error: Exception?) -> Unit
    ) {
        val objectId = objIdOfSeatIndex[seatInfo.seatIndex] ?: return
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.update(objectId, seatInfo, object : Callback {
                override fun onSuccess() {
                    completion.invoke(null)
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(exception)
                }
            })
    }

    private fun innerRemoveSeat(
        seatInfo: RoomSeatModel,
        completion: (error: Exception?) -> Unit
    ) {
        val objectId = objIdOfSeatIndex[seatInfo.seatIndex] ?: return
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.delete(objectId, object : Callback {
                override fun onSuccess() {
                    // objIdOfSeatIndex.remove(seatInfo.seatIndex)
                    // seatMap.remove(seatInfo.seatIndex.toString())
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerAddSeatInfo(
        seatInfo: RoomSeatModel,
        completion: (error: Exception?) -> Unit
    ) {
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.add(seatInfo, object : DataItemCallback {
                override fun onSuccess(result: IObject) {
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerSubscribeSeats(completion: () -> Unit) {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
            }

            override fun onUpdated(item: IObject?) {
                val obj = item?.toObject(RoomSeatModel::class.java) ?: return
                objIdOfSeatIndex[obj.seatIndex] = item.id

                if (seatMap.containsKey(obj.seatIndex.toString())) {
                    seatMap[obj.seatIndex.toString()] = obj
                    runOnMainThread{
                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                            obj
                        )
                    }
                } else {
                    seatMap[obj.seatIndex.toString()] = obj
                    runOnMainThread{
                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                            obj
                        )
                    }
                }
            }

            override fun onDeleted(item: IObject?) {
                item ?: return

                seatMap.forEach { entry ->
                    entry.value?.let { seat ->
                        if (objIdOfSeatIndex[seat.seatIndex] == item.id) {
                            seatMap.remove(entry.key)
                            runOnMainThread{
                                seatListChangeSubscriber?.invoke(
                                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                                    seat
                                )
                            }
                            return
                        }
                    }
                }
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.collection(kCollectionIdSeatInfo)?.subscribe(listener)
    }

    // ------------ Choose song operation ---------------------

    fun innerSortChooseSongList(targetList: List<RoomSelSongModel>): List<RoomSelSongModel> {
        val list = ArrayList<RoomSelSongModel>(targetList)
        val playingList = list.filter { it.status == RoomSelSongModel.STATUS_PLAYING }
        val pinList = list.filter { it.pinAt > 0 && it.status != RoomSelSongModel.STATUS_PLAYING }.sortedBy { it.pinAt * -1 }
        val normalList = list.filter { it.pinAt <= 0 && it.status != RoomSelSongModel.STATUS_PLAYING }.sortedBy { it.createAt }

        val out = ArrayList<RoomSelSongModel>()
        out.addAll(playingList)
        out.addAll(pinList)
        out.addAll(normalList)
        return out
    }

    private fun innerPinSong(objectId: String, song: RoomSelSongModel, completion: (error: Exception?) -> Unit) {
        KTVLogger.d(TAG, "innerPinSong: $song")
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdChooseSong)
            .update(objectId,
                song,
                object : Callback {
                    override fun onSuccess() {
                        completion.invoke(null)
                    }

                    override fun onFail(exception: SyncManagerException?) {
                        completion.invoke(exception)
                    }
                })
    }

    private fun innerDidPlaySong(objectId: String, song: RoomSelSongModel, completion: (error: Exception?) -> Unit) {
        KTVLogger.d(TAG, "innerDidPlaySong: $song")
        val sceneReference = mSceneReference ?: return
        sceneReference.collection(kCollectionIdChooseSong)
            .update(objectId,
                song,
                object : Callback {
                    override fun onSuccess() {
                        completion.invoke(null)
                    }

                    override fun onFail(exception: SyncManagerException?) {
                        completion.invoke(exception)
                    }
                })
    }


    private fun innerGetChooseSongInfo(completion: (error: Exception?, list: List<RoomSelSongModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdChooseSong)?.get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<RoomSelSongModel>()
                val retObjId = ArrayList<String>()
                result?.forEach {
                    val obj = it.toObject(RoomSelSongModel::class.java)
                    ret.add(obj)
                    retObjId.add(it.id)
                }
                songChosenList.clear()
                songChosenList.addAll(ret)
                objIdOfSongNo.clear()
                objIdOfSongNo.addAll(retObjId)

                val sortList = innerSortChooseSongList(songChosenList)
                completion.invoke(null, sortList)
            }

            override fun onFail(exception: SyncManagerException?) {
                completion.invoke(exception, null)
            }
        })
    }

    private fun innerUpdateChooseSong(
        objectId: String,
        songInfo: RoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        KTVLogger.d(TAG, "innerUpdateChooseSong: $songInfo")
        mSceneReference?.collection(kCollectionIdChooseSong)
            ?.update(objectId, songInfo, object : Callback {
                override fun onSuccess() {
                    completion.invoke(null)
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(exception)
                }
            })
    }

    private fun innerAddChooseSongInfo(
        songInfo: RoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        KTVLogger.d(TAG, "innerAddChooseSongInfo: $songInfo")
        mSceneReference?.collection(kCollectionIdChooseSong)
            ?.add(songInfo, object : DataItemCallback {
                override fun onSuccess(result: IObject) {

                    songChosenList.add(songInfo)
                    objIdOfSongNo.add(result.id)

                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerRemoveAllUsersChooseSong(userNo: String) {
        songChosenList.filter { it.userNo == userNo }
            .forEach {
                val indexOf = songChosenList.indexOf(it)
                songChosenList.removeAt(indexOf)
                innerRemoveChooseSong(objIdOfSongNo.removeAt(indexOf)) {}
            }
    }

    private fun innerRemoveChooseSong(objectId: String, completion: (error: Exception?) -> Unit) {
        mSceneReference?.collection(kCollectionIdChooseSong)
            ?.delete(objectId, object : Callback {
                override fun onSuccess() {
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    // --------------------- Song Battle Game operation ---------------------
    private fun innerAddSingRelayGameInfo(info: SingRelayGameModel,
                                           completion: (error: Exception?) -> Unit) {
        mSceneReference?.collection(kCollectionSingRelayGameInfo)
            ?.add(info, object : DataItemCallback {
                override fun onSuccess(result: IObject) {

                    SingRelayGameInfo = info
                    objIdOfSingRelayGameInfo = result.id

                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerUpdateSingRelayGameInfo(objectId: String,
                                              info: SingRelayGameModel,
                                              completion: (error: Exception?) -> Unit) {
        mSceneReference?.collection(kCollectionSingRelayGameInfo)
            ?.update(objectId, info, object : Callback {
                override fun onSuccess() {
                    completion.invoke(null)
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(exception)
                }
            })
    }

    private fun innerRemoveSingRelayGameInfo(objectId: String, completion: (error: Exception?) -> Unit) {
        mSceneReference?.collection(kCollectionSingRelayGameInfo)
            ?.delete(objectId, object : Callback {
                override fun onSuccess() {
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerGetSingRelayGameInfo(completion: (error: Exception?, SingRelayGameModel?) -> Unit) {
        mSceneReference?.collection(kCollectionSingRelayGameInfo)?.get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                if (result == null) return
                if (result.isEmpty()) {
                    SingRelayGameInfo = null
                    objIdOfSingRelayGameInfo = null
                }
                result.forEach {
                    val obj = it.toObject(SingRelayGameModel::class.java)
                    SingRelayGameInfo = obj
                    objIdOfSingRelayGameInfo = it.id
                }

                completion.invoke(null, SingRelayGameInfo)
            }

            override fun onFail(exception: SyncManagerException?) {
                completion.invoke(exception, null)
            }
        })
    }

    private fun innerSubscribeSingRelayGameInfo(completion: () -> Unit) {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
            }

            override fun onUpdated(item: IObject?) {
                val gameInfo = item?.toObject(SingRelayGameModel::class.java) ?: return
                SingRelayGameInfo = gameInfo
                objIdOfSingRelayGameInfo = item.id
                SingRelayGameSubscribe?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                    gameInfo
                )
            }

            override fun onDeleted(item: IObject?) {
                //item ?: return
                SingRelayGameInfo = null
                objIdOfSingRelayGameInfo = null
                SingRelayGameSubscribe?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                    null
                )
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.collection(kCollectionSingRelayGameInfo)?.subscribe(listener)
    }

    // --------------------- Subscribe ---------------------

    private fun innerSubscribeChooseSong(completion: () -> Unit) {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
            }

            override fun onUpdated(item: IObject?) {
                val songInfo = item?.toObject(RoomSelSongModel::class.java) ?: return
                chooseSongSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                    songInfo
                )
            }

            override fun onDeleted(item: IObject?) {
                //item ?: return
                chooseSongSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                    null
                )
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.collection(kCollectionIdChooseSong)?.subscribe(listener)
    }

    private fun innerSubscribeRoomChanged() {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                val roomInfo = item.toObject(RoomListModel::class.java)
                roomMap[roomInfo.roomNo] = roomInfo
                runOnMainThread {
                    roomStatusSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                        roomInfo
                    )
                }
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                val roomInfo = roomMap[item.id] ?: return

                if (item.id != currRoomNo) {
                    return
                }
                resetCacheInfo(true)
                runOnMainThread {
                    roomStatusSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                        roomInfo
                    )
                }

            }

            override fun onSubscribeError(ex: SyncManagerException?) {

            }

        }
        roomSubscribeListener.add(listener)
        Instance().subscribeScene(mSceneReference, listener)
    }
}