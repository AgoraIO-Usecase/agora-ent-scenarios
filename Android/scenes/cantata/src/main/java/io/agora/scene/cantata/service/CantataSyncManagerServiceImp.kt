package io.agora.scene.cantata.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import io.agora.scene.base.AgoraTokenType
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.cantata.CantataLogger
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.TokenGeneratorType
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.api.model.User
import io.agora.scene.base.manager.UserManager
import io.agora.scene.cantata.service.CantataServiceProtocol.Companion.ROOM_AVAILABLE_DURATION
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.*
import kotlin.random.Random

/*
 * service 模块
 * 简介：这个模块的作用是负责前端业务模块和业务服务器的交互(包括房间列表+房间内的业务数据同步等)
 * 实现原理：该场景的业务服务器是包装了一个 rethinkDB 的后端服务，用于数据存储，可以认为它是一个 app 端上可以自由写入的 DB，房间列表数据、房间内的业务数据等在 app 上构造数据结构并存储在这个 DB 里
 * 当 DB 内的数据发生增删改时，会通知各端，以此达到业务数据同步的效果
 * TODO 注意⚠️：该场景的后端服务仅做场景演示使用，无法商用，如果需要上线，您必须自己部署后端服务或者云存储服务器（例如leancloud、环信等）并且重新实现这个模块！！！！！！！！！！！
 */
class CantataSyncManagerServiceImp constructor(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : CantataServiceProtocol {
    private val TAG = "KTV_Service_LOG"
    private val kSceneId = "scene_cantata_${BuildConfig.APP_VERSION_NAME}"
    private val kCollectionIdChooseSong = "choose_song"
    private val kCollectionIdSeatInfo = "seat_info"
    private val kCollectionIdUser = "userCollection"

    private data class VLLoginModel constructor(
        val id: String,
        //val isWaitAutoOnSeat: Boolean = false
    )

    @Volatile
    private var syncUtilsInited = false

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var mSceneReference: SceneReference? = null

    // subscribers
    private var roomStatusSubscriber: ((CantataServiceProtocol.KTVSubscribe, RoomListModel?) -> Unit)? =
        null
    private var roomUserCountSubscriber: ((Int) -> Unit)? =
        null
    private var seatListChangeSubscriber: ((CantataServiceProtocol.KTVSubscribe, RoomSeatModel?) -> Unit)? =
        null
    private var chooseSongSubscriber: ((CantataServiceProtocol.KTVSubscribe, RoomSelSongModel?) -> Unit)? =
        null
    private var onReconnectSubscriber: (() -> Unit)? = null
    private var roomTimeUpSubscriber: (() -> Unit)? = null

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

    private val mUser: User
        get() = UserManager.getInstance().user

    @Volatile
    private var currRoomNo: String = ""

    private val timerRoomEndRun = Runnable {
        runOnMainThread {
            CantataLogger.d(TAG, "time up exit room!")
            roomTimeUpSubscriber?.invoke()
        }
    }

    override fun reset() {
        if (syncUtilsInited) {
            Sync.Instance().destroy()
            syncUtilsInited = false

            objIdOfRoomNo.clear()
            objIdOfSongNo.clear()
            objIdOfSeatIndex.clear()
            objIdOfUserNo.clear()

            roomSubscribeListener.clear()
            roomMap.clear()
            userMap.clear()
            seatMap.clear()
            songChosenList.clear()
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
                creatorNo = mUser.id.toString(),
                creatorName = mUser.name,
                creatorAvatar = mUser.headUrl,
                bgOption = "0",
                streamMode = inputModel.delayType
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
        // fix FT-1055
        if (isJoined && currRoomNo.isNotEmpty()) {
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
            val isRoomOwner = cacheRoom.creatorNo == mUser.id.toString()
            Instance().joinScene(isRoomOwner, true, inputModel.roomNo, object : JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    mSceneReference = sceneReference
                    currRoomNo = inputModel.roomNo

                    TokenGenerator.generateTokens(
                        currRoomNo + "_ad",
                        mUser.id.toString(),
                        TokenGeneratorType.Token007,
                        arrayOf(
                            AgoraTokenType.Rtc,
                            AgoraTokenType.Rtm
                        ),
                        { rtcRtmToken ->
                            innerSubscribeRoomChanged()
                            innerSubscribeChooseSong {}
                            innerAddUserIfNeed { addUserError, userSize ->
                                if (addUserError != null) {
                                    completion.invoke(addUserError, null)
                                    return@innerAddUserIfNeed
                                }
                                innerSubscribeSeats {}
                                innerAutoOnSeatIfNeed { error, seats ->
                                    if (error != null) {
                                        completion.invoke(error, null)
                                        return@innerAutoOnSeatIfNeed
                                    }

                                    TokenGenerator.generateToken(
                                        currRoomNo,
                                        mUser.id.toString(),
                                        TokenGeneratorType.Token006,
                                        AgoraTokenType.Rtc,
                                        { chorusToken ->
                                            TokenGenerator.generateToken(
                                                currRoomNo,
                                                "2023",
                                                TokenGeneratorType.Token006,
                                                AgoraTokenType.Rtc,
                                                { musicToken ->
                                                    val kTVJoinRoomOutputModel = JoinRoomOutputModel(
                                                        cacheRoom.name,
                                                        inputModel.roomNo,
                                                        cacheRoom.creatorNo,
                                                        cacheRoom.creatorAvatar,
                                                        cacheRoom.bgOption,
                                                        seats,
                                                        userSize,
                                                        rtcRtmToken,
                                                        rtcRtmToken,
                                                        chorusToken,
                                                        musicToken,
                                                        cacheRoom.createdAt,
                                                        cacheRoom.streamMode
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
                                                    CantataLogger.d(TAG, "expireLeftTime: $expireLeftTime")
                                                    mainHandler.postDelayed(timerRoomEndRun, expireLeftTime)
                                                },
                                                {
                                                    completion.invoke(it, null)
                                                },
                                                SceneConfigManager.cantataAppId
                                            )
                                        },
                                        {
                                            completion.invoke(it, null)
                                        },
                                        SceneConfigManager.cantataAppId
                                    )
                                }

                            }
                        },
                        {
                            completion.invoke(it, null)
                        },
                        SceneConfigManager.cantataAppId
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

        if (cacheRoom.creatorNo == mUser.id.toString()) {
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
                    if (seat.userNo == mUser.id.toString()) {
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
                if (songModel.userNo.equals(mUser.id.toString())) {
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
                        roomInfo.creatorName,
                        roomInfo.creatorAvatar,
                        roomInfo.createdAt,
                        inputModel.mvIndex.toString(),
                        roomInfo.roomPeopleNum,
                        roomInfo.streamMode
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

    override fun subscribeRoomStatusChanged(changedBlock: (CantataServiceProtocol.KTVSubscribe, RoomListModel?) -> Unit) {
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

    override fun onSeat(inputModel: OnSeatInputModel, completion: (error: Exception?) -> Unit) {
        seatMap.forEach {
            it.value?.let { seat ->
                if (seat.userNo == mUser.id.toString()) {
                    return
                }
            }
        }
        val seatInfo = innerGenUserSeatInfo(inputModel.score)
        innerAddSeatInfo(seatInfo, completion)
    }

    override fun leaveSeat(inputModel: OutSeatInputModel, completion: (error: Exception?) -> Unit) {
        val seatInfo = seatMap[inputModel.userId]
        if (seatInfo != null) {
//            // 移除歌曲
//            innerRemoveAllUsersChooseSong(seatInfo.userNo)
            // 移除座位
            innerRemoveSeat(seatInfo) {}
        }
        completion(null)
    }

    override fun leaveSeatWithoutRemoveSong(inputModel: OutSeatInputModel, completion: (error: Exception?) -> Unit) {
        val seatInfo = seatMap[inputModel.userId]
        if (seatInfo != null) {
            // 移除座位
            innerRemoveSeat(seatInfo) {}
        }
        completion(null)
    }

    override fun updateSeatAudioMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit) {
        seatMap.forEach {
            if (it.value?.userNo == mUser.id.toString()) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = originSeatInfo.copy()
                    seatInfo.isAudioMuted =
                        if (mute) RoomSeatModel.MUTED_VALUE_TRUE else RoomSeatModel.MUTED_VALUE_FALSE

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
            if (it.value?.userNo == mUser.id.toString()) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = originSeatInfo.copy()
                    seatInfo.isVideoMuted =
                        if (mute) RoomSeatModel.MUTED_VALUE_TRUE else RoomSeatModel.MUTED_VALUE_FALSE

                    innerUpdateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun updateSeatScoreStatus(score: Int, completion: (error: Exception?) -> Unit) {
        seatMap.forEach {
            if (it.value?.userNo == mUser.id.toString()) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = originSeatInfo.copy()
                    seatInfo.score = score
                    innerUpdateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun subscribeSeatListChanged(changedBlock: (CantataServiceProtocol.KTVSubscribe, RoomSeatModel?) -> Unit) {
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

            userNo = mUser.id.toString(),
            name = mUser.name,

            status = RoomSelSongModel.STATUS_IDLE,
            createAt = System.currentTimeMillis().toLong(),
            pinAt = 0.0,
            musicEnded = false
        )
        //net request and notify others
        innerAddChooseSongInfo(song, completion)
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
            pinAt = System.currentTimeMillis().toDouble(),
            musicEnded = targetSong.musicEnded
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
            completion.invoke(null)
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

            status = RoomSelSongModel.STATUS_PLAYING,
            createAt = targetSong.createAt,
            pinAt = targetSong.pinAt,
            musicEnded = targetSong.musicEnded
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

        //net request and notify others
        innerRemoveChooseSong(removeAt) {
            completion.invoke(it)
        }
    }

    override fun markSongEnded(inputModel: RoomSelSongModel, completion: (error: Exception?) -> Unit) {
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
            pinAt = targetSong.pinAt,
            musicEnded = true
        )
        songChosenList[indexOf] = newSong


        innerDidPlaySong(objIdOfSongNo[indexOf], newSong) {
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
                    if (seat.userNo == mUser.id.toString()) {
                        // 座位 joinSing -> true
                        val seatInfo = seat.copy()
                        seatInfo.chorusSongCode = inputModel.songNo + inputModel.createAt
                        seatInfo.isAudioMuted = RoomSeatModel.MUTED_VALUE_FALSE
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
            if (it.value?.userNo == mUser.id.toString()) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = originSeatInfo.copy()
                    seatInfo.chorusSongCode = ""
                    seatInfo.isAudioMuted = RoomSeatModel.MUTED_VALUE_TRUE

                    innerUpdateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun subscribeChooseSongChanged(changedBlock: (CantataServiceProtocol.KTVSubscribe, RoomSelSongModel?) -> Unit) {
        chooseSongSubscriber = changedBlock
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
            RethinkConfig(
                if (SceneConfigManager.cantataAppId == "") BuildConfig.AGORA_APP_ID else SceneConfigManager.cantataAppId,
                kSceneId),
            object : Callback {
                override fun onSuccess() {
                    syncUtilsInited = true
                    runOnMainThread {
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
                    if (oldRoomInfo != null) {
                        getRoomList { _, _ ->
                            val roomInfo = roomMap[currRoomNo]
                            if (roomInfo == null) {
                                runOnMainThread {
                                    roomStatusSubscriber?.invoke(
                                        CantataServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
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
        Instance().subscribeLog(object :LogCallback{
            override fun onLogInfo(message: String?) {
                CantataLogger.d(TAG, message ?: "")
            }

            override fun onLogWarning(message: String?) {
                CantataLogger.w(TAG, message ?: "")
            }

            override fun onLogError(message: String?) {
                CantataLogger.e(TAG, message ?: "")
            }

        })
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
                if (!userMap.containsKey(userInfo?.id)) {
                    userMap[userInfo?.id.toString()] = userInfo
                    objIdOfUserNo[userInfo?.id.toString()] = item.id
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
                    objIdOfUserNo[obj.id] = it.id
                    ret.add(obj)

                    userMap[obj.id] = obj
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
        val localUserInfo = VLLoginModel(mUser.id.toString())
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
        val objectId = objIdOfUserNo[user.id] ?: return
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
        val objectId = objIdOfUserNo[mUser.id.toString()] ?: return
        mSceneReference?.collection(kCollectionIdUser)?.delete(objectId, object : Callback {
            override fun onSuccess() {
                objIdOfUserNo.remove(mUser.id.toString())
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
            if (!userMap.containsKey(mUser.id.toString())) {
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
            runOnMainThread { roomUserCountSubscriber?.invoke(count) }
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
                        roomInfo.creatorName,
                        roomInfo.creatorAvatar,
                        roomInfo.createdAt,
                        roomInfo.bgOption,
                        count,
                        roomInfo.streamMode
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

    private fun innerGenUserSeatInfo(score: Int): RoomSeatModel {
        return RoomSeatModel(
            isMaster = roomMap[currRoomNo]?.creatorNo == mUser.id.toString(),
            headUrl = mUser.headUrl,
            userNo = mUser.id.toString(),
            rtcUid = mUser.id.toString(),
            name = mUser.name,
            seatIndex = 0,
            chorusSongCode = "",
            isAudioMuted = RoomSeatModel.MUTED_VALUE_FALSE,
            isVideoMuted = RoomSeatModel.MUTED_VALUE_TRUE,
            score = score,
            isOwner = false
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
            val outList = ArrayList<RoomSeatModel>()
            seatMap.forEach {
                it.value?.let { seat ->
                    outList.add(seat)
                    runOnMainThread {
                        seatListChangeSubscriber?.invoke(
                            CantataServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                            seat
                        )
                    }
                }
            }
            completion.invoke(null, outList)
        }
    }


    private fun innerGetSeatInfo(completion: (error: Exception?, list: List<RoomSeatModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdSeatInfo)?.get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<RoomSeatModel>()
                result?.forEach {
                    val obj = it.toObject(RoomSeatModel::class.java)
                    objIdOfSeatIndex[obj.rtcUid.toInt()] = it.id
                    ret.add(obj)

                    // 储存在本地map中
                    seatMap[obj.rtcUid] = obj
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
        val objectId = objIdOfSeatIndex[seatInfo.rtcUid.toInt()] ?: return
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
        objIdOfSeatIndex.remove(seatInfo.seatIndex)
        seatMap.remove(seatInfo.seatIndex.toString())
        val objectId = objIdOfSeatIndex[seatInfo.rtcUid.toInt()] ?: return
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
                objIdOfSeatIndex[obj.rtcUid.toInt()] = item.id

                if (seatMap.containsKey(obj.rtcUid)) {
                    seatMap[obj.rtcUid] = obj
                    runOnMainThread {
                        seatListChangeSubscriber?.invoke(
                            CantataServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                            obj
                        )
                    }
                } else {
                    seatMap[obj.rtcUid] = obj
                    runOnMainThread {
                        seatListChangeSubscriber?.invoke(
                            CantataServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                            obj
                        )
                    }
                }
            }

            override fun onDeleted(item: IObject?) {
                item ?: return

                seatMap.forEach { entry ->
                    entry.value?.let { seat ->
                        if (objIdOfSeatIndex[seat.rtcUid.toInt()] == item.id) {
                            seatMap.remove(entry.key)
                            runOnMainThread {
                                seatListChangeSubscriber?.invoke(
                                    CantataServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
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

    private fun innerSortChooseSongList(): List<RoomSelSongModel> {
        val list = ArrayList<RoomSelSongModel>(songChosenList)
        val playingList = list.filter { it.status == RoomSelSongModel.STATUS_PLAYING }
        val pinList =
            list.filter { it.pinAt > 0 && it.status != RoomSelSongModel.STATUS_PLAYING }.sortedBy { it.pinAt * -1 }
        val normalList =
            list.filter { it.pinAt <= 0 && it.status != RoomSelSongModel.STATUS_PLAYING }.sortedBy { it.createAt }

        val out = ArrayList<RoomSelSongModel>()
        out.addAll(playingList)
        out.addAll(pinList)
        out.addAll(normalList)
        return out
    }

    private fun innerPinSong(objectId: String, song: RoomSelSongModel, completion: (error: Exception?) -> Unit) {
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

                val sortList = innerSortChooseSongList()
                runOnMainThread { completion.invoke(null, sortList) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception, null) }
            }
        })
    }

    private fun innerUpdateChooseSong(
        objectId: String,
        songInfo: RoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
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

    private fun innerSubscribeChooseSong(completion: () -> Unit) {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
            }

            override fun onUpdated(item: IObject?) {
                val songInfo = item?.toObject(RoomSelSongModel::class.java) ?: return
                chooseSongSubscriber?.invoke(
                    CantataServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                    songInfo
                )
            }

            override fun onDeleted(item: IObject?) {
                //item ?: return
                chooseSongSubscriber?.invoke(
                    CantataServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
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
                        CantataServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
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
                        CantataServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
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