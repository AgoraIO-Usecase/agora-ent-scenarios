package io.agora.scene.ktv.service

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.text.TextUtils
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.manager.UserManager
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback
import io.agora.syncmanager.rtm.Sync.EventListener
import java.util.concurrent.CountDownLatch


/**
 * 使用SyncManager进行数据交互
 *
 *
 */
class KTVSyncManagerServiceImp(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : KTVServiceProtocol {
    private val kSceneId = "scene_ktv"
    private val kCollectionIdChooseSong = "choose_song"
    private val kCollectionIdSeatInfo = "seat_info"
    private val kCollectionIdUser = "userCollection"

    private data class VLLoginModel(
        val userNo: String,
    )

    @Volatile
    private var syncUtilsInited = false

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val workingHandlerThread by lazy {
        val thread = HandlerThread("WorkingThread")
        thread.start()
        thread
    }
    private val workingHandler by lazy {
        Handler(workingHandlerThread.looper)
    }

    private var mSceneReference: SceneReference? = null

    // subscribers
    private var roomStatusSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomListModel?) -> Unit)? =
        null
    private var roomUserCountSubscriber: ((Int) -> Unit)? =
        null
    private var seatListChangeSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit)? =
        null
    private var chooseSongSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomSelSongModel?) -> Unit)? =
        null

    // cache objectId
    private val objIdOfRoomNo = HashMap<String, String>() // objectId of room no
    private val objIdOfSongNo = ArrayList<String>() // objectId of song no
    private val objIdOfSeatIndex = HashMap<Int, String>() // objectId of seat index
    private val objIdOfUserNo = HashMap<String, String>() // objectId of user no

    // cache data
    private val roomSubscribeListener = mutableListOf<EventListener>()
    private val roomMap = mutableMapOf<String, VLRoomListModel>()
    private val userMap = mutableMapOf<String, VLLoginModel?>()
    private val seatMap = mutableMapOf<String, VLRoomSeatModel?>()
    private val songChosenList = ArrayList<VLRoomSelSongModel>()

    private var currRoomNo: String = ""


    // ========= 房间相关 =====================

    override fun getRoomList(
        completion: (error: Exception?, list: List<VLRoomListModel>?) -> Unit
    ) {
        initSync {
            Sync.Instance().getScenes(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    val ret = ArrayList<VLRoomListModel>()
                    result?.forEach {
                        val obj = it.toObject(VLRoomListModel::class.java)
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

    override fun createRoomWithInput(
        inputModel: KTVCreateRoomInputModel,
        completion: (error: Exception?, out: KTVCreateRoomOutputModel?) -> Unit
    ) {
        initSync {
            val vlRoomListModel = VLRoomListModel(
                roomNo = (100000..999999).random().toString(),
                name = inputModel.name,
                icon = inputModel.icon,
                isPrivate = inputModel.isPrivate != 0,
                password = inputModel.password,
                creatorNo = UserManager.getInstance().user.userNo,
                bgOption = (1..2).random().toString(),
            )
            val scene = Scene()
            scene.id = vlRoomListModel.roomNo
            scene.userId = inputModel.userNo

            scene.property = GsonUtils.covertToMap(vlRoomListModel)

            Sync.Instance().createScene(scene, object : Sync.Callback {
                override fun onSuccess() {
                    roomMap[vlRoomListModel.roomNo] = vlRoomListModel
                    runOnMainThread {
                        completion.invoke(
                            null,
                            KTVCreateRoomOutputModel(
                                vlRoomListModel.roomNo,
                                vlRoomListModel.password
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

    override fun joinRoomWithInput(
        inputModel: KTVJoinRoomInputModel,
        completion: (error: Exception?, out: KTVJoinRoomOutputModel?) -> Unit
    ) {
        if (!TextUtils.isEmpty(currRoomNo)) {
            completion.invoke(RuntimeException("The room $currRoomNo has been joined!"), null);
        }
        val cacheRoom = roomMap[inputModel.roomNo]
        if (cacheRoom == null) {
            completion.invoke(RuntimeException("The room is not available!"), null);
            return
        }
        if (cacheRoom.isPrivate && cacheRoom.password != inputModel.password) {
            completion.invoke(RuntimeException("The password is error!"), null);
            return
        }
        initSync {
            Sync.Instance().joinScene(inputModel.roomNo, object : Sync.JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    mSceneReference = sceneReference
                    currRoomNo = inputModel.roomNo

                    _subscribeRoomChanged()
                    _subscribeChooseSong {}
                    _addUserIfNeed { _, _ -> }
                    _autoOnSeatIfNeed { error, seats ->
                        if (error != null) {
                            completion.invoke(error, null)
                            return@_autoOnSeatIfNeed
                        }

                        val kTVJoinRoomOutputModel = KTVJoinRoomOutputModel(
                            cacheRoom.name,
                            inputModel.roomNo,
                            cacheRoom.creatorNo,
                            cacheRoom.bgOption,
                            seats,
                            cacheRoom.roomPeopleNum,
                            "", //TODO fetchToken
                            "", //TODO fetchToken
                            "", //TODO fetchToken,
                        )
                        runOnMainThread {
                            completion.invoke(null, kTVJoinRoomOutputModel)
                        }
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception, null) }
                }
            })
        }
    }

    override fun leaveRoomWithCompletion(completion: (error: Exception?) -> Unit) {
        val cacheRoom = roomMap[currRoomNo] ?: return
        currRoomNo = ""


        // 取消所有订阅
        roomSubscribeListener.forEach {
            mSceneReference?.unsubscribe(it)
        }
        roomSubscribeListener.clear()

        // 减少用户数，并清空用户信息
        _removeUser {}
        _updateUserCount(userMap.size - 1)
        userMap.clear()

        // 如果上麦了要下麦，并清空麦位信息
        seatMap.forEach {
            it.value?.let { seat ->
                if (seat.userNo == UserManager.getInstance().user.userNo) {
                    _removeSeat(seat) {}
                    return@forEach
                }
            }
        }
        seatMap.clear()


        if (cacheRoom.creatorNo == UserManager.getInstance().user.userNo) {
            // 移除房间
            roomMap.remove(cacheRoom.roomNo)
            mSceneReference?.delete(object : Sync.Callback {
                override fun onSuccess() {
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
            runOnMainThread {
                completion.invoke(null)
            }
        } else {
            // leave room
            runOnMainThread {
                completion.invoke(null)
            }
        }

        mSceneReference = null
    }

    override fun changeMVCoverWithInput(
        inputModel: KTVChangeMVCoverInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val roomInfo = roomMap[currRoomNo] ?: return
        mSceneReference?.update(
            HashMap(
                GsonUtils.covertToMap(
                    VLRoomListModel(
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
            object : Sync.DataItemCallback {
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

    override fun subscribeRoomStatusWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomListModel?) -> Unit) {
        roomStatusSubscriber = changedBlock
    }

    override fun subscribeUserListCountWithChanged(changedBlock: (count: Int) -> Unit) {
        roomUserCountSubscriber = changedBlock
    }

    // =================== 麦位相关 ===============================

    override fun onSeatWithInput(
        inputModel: KTVOnSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        seatMap.forEach {
            it.value?.let { seat ->
                if (seat.userNo == UserManager.getInstance().user.userNo) {
                    return
                }
            }
        }
        val seatInfo = _genUserSeatInfo(inputModel.seatIndex)
        _addSeatInfo(seatInfo, completion)
    }

    override fun outSeatWithInput(
        inputModel: KTVOutSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val seatInfo = seatMap[inputModel.userOnSeat.toString()]
        if (seatInfo != null) {
            _removeSeat(seatInfo, {})
        }
        completion(null)
    }

    override fun muteWithMuteStatus(isSelfMuted: Int, completion: (error: Exception?) -> Unit) {
        seatMap.forEach {
            if (it.value?.userNo == UserManager.getInstance().user.userNo) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = VLRoomSeatModel(
                        originSeatInfo.isMaster,
                        originSeatInfo.headUrl,
                        originSeatInfo.userNo,
                        originSeatInfo.id,
                        originSeatInfo.name,
                        originSeatInfo.onSeat,
                        originSeatInfo.joinSing,
                        isSelfMuted, // update this
                        originSeatInfo.isVideoMuted,
                        originSeatInfo.ifSelTheSingSong,
                        originSeatInfo.ifJoinedChorus,
                    )
                    _updateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun openVideoStatusWithStatus(
        isVideoMuted: Int,
        completion: (error: Exception?) -> Unit
    ) {
        seatMap.forEach {
            if (it.value?.userNo == UserManager.getInstance().user.userNo) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = VLRoomSeatModel(
                        originSeatInfo.isMaster,
                        originSeatInfo.headUrl,
                        originSeatInfo.userNo,
                        originSeatInfo.id,
                        originSeatInfo.name,
                        originSeatInfo.onSeat,
                        originSeatInfo.joinSing,
                        originSeatInfo.isSelfMuted,
                        isVideoMuted, // update this
                        originSeatInfo.ifSelTheSingSong,
                        originSeatInfo.ifJoinedChorus
                    )
                    _updateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun subscribeSeatListWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit) {
        seatListChangeSubscriber = changedBlock
    }


    // ============= 歌曲相关 =============================

    override fun getChoosedSongsListWithCompletion(completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit) {
        _getChooseSongInfo(completion)
    }

    override fun chooseSongWithInput(
        inputModel: KTVChooseSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val song = VLRoomSelSongModel(
            inputModel.songName,
            inputModel.songNo,
            inputModel.songUrl,
            inputModel.singer,
            inputModel.lyric,
            inputModel.imageUrl,

            userNo = UserManager.getInstance().user.userNo,
            userId = UserManager.getInstance().user.id.toString(),
            name = UserManager.getInstance().user.name,
            sort = if (songChosenList.isEmpty()) 1 else songChosenList.last().sort + 1
        )
        //net request and notify others
        _addChooseSongInfo(song, completion)
    }

    override fun switchSongWithInput(
        inputModel: KTVSwitchSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        // remove the first song and move the song to first position
        if (songChosenList.size <= 0) {
            completion.invoke(RuntimeException("The chosen song list is empty!"))
            return
        }

        songChosenList.removeAt(0)
        val removeObjId = objIdOfSongNo.removeAt(0)
        _removeChooseSong(removeObjId) {}

        val filter = songChosenList.filter { it.songNo == inputModel.songNo }
        val targetSong = filter.getOrNull(0)
        if (targetSong == null) {
            completion.invoke(RuntimeException("The song no not found!"))
            return
        }
        val index = songChosenList.indexOf(targetSong)
        songChosenList.add(0, songChosenList.removeAt(index))
        objIdOfSongNo.add(0, objIdOfSongNo.removeAt(index))

        //net request and notify others
        _sortChooseSongList {
            chooseSongSubscriber?.invoke(
                KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                targetSong
            )
            completion.invoke(null)
        }

    }

    override fun makeSongTopWithInput(
        inputModel: KTVMakeSongTopInputModel,
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
        if (targetSong == songChosenList.first()) {
            completion.invoke(RuntimeException("The song is playing now!"))
            return
        }
        if (targetSong == songChosenList[1]) {
            completion.invoke(RuntimeException("The song has been top up!"))
            return
        }

        val indexOf = songChosenList.indexOf(targetSong)
        songChosenList.add(1, songChosenList.removeAt(indexOf))
        objIdOfSongNo.add(1, objIdOfSongNo.removeAt(indexOf))

        //net request and notify others
        _sortChooseSongList {
            chooseSongSubscriber?.invoke(
                KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                targetSong
            )
            completion.invoke(it)
        }
    }

    override fun removeSongWithInput(
        inputModel: KTVRemoveSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        if (songChosenList.size <= 0) {
            completion.invoke(RuntimeException("The chosen song list is empty!"))
            return
        }
        val targetSong = songChosenList.getOrNull(inputModel.sort - 1)
        if (targetSong == null) {
            completion.invoke(RuntimeException("The song no not found!"))
            return
        }


        val indexOf = songChosenList.indexOf(targetSong)
        songChosenList.removeAt(indexOf)
        val removeAt = objIdOfSongNo.removeAt(indexOf)

        //net request and notify others
        _removeChooseSong(removeAt) {}

        _sortChooseSongList {
            chooseSongSubscriber?.invoke(
                KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                targetSong
            )
            completion.invoke(it)
        }
    }

    override fun joinChorusWithInput(
        inputModel: KTVJoinChorusInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        // set the chorus user to the first chosen song
        if (songChosenList.size <= 0) {
            completion.invoke(RuntimeException("The chosen song list is empty!"))
            return
        }
        val targetSong = songChosenList[0]
        if (!TextUtils.isEmpty(targetSong.chorusNo)) {
            completion.invoke(RuntimeException("The song has chorus user already!"))
            return
        }
        songChosenList.remove(targetSong)
        val newSong = VLRoomSelSongModel(
            targetSong.songName,
            targetSong.songNo,
            targetSong.songUrl,
            targetSong.singer,
            targetSong.lyric,
            targetSong.imageUrl,

            userNo = targetSong.userNo,
            userId = targetSong.userId,
            name = targetSong.name,
            chorusNo = UserManager.getInstance().user.userNo,
            isChorus = true,
            isOriginal = targetSong.isOriginal,
            sort = targetSong.sort
        )
        songChosenList.add(0, newSong)

        //net request and notify others
        _updateChooseSong(
            objIdOfSongNo[0],
            newSong
        ) {
            chooseSongSubscriber?.invoke(
                KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                newSong
            )
            completion.invoke(it)
        }
    }

    override fun becomeSolo() {
        // unset the chorus user to the first chosen song
        if (songChosenList.size <= 0) {
            return
        }

        val targetSong = songChosenList[0]
        if (TextUtils.isEmpty(targetSong.chorusNo)) {
            return
        }
        songChosenList.remove(targetSong)
        val newSong = VLRoomSelSongModel(
            targetSong.songName,
            targetSong.songNo,
            targetSong.songUrl,
            targetSong.singer,
            targetSong.lyric,
            targetSong.imageUrl,

            userNo = targetSong.userNo,
            userId = targetSong.userId,
            name = targetSong.name,
            isOriginal = targetSong.isOriginal,
            sort = targetSong.sort
        )
        songChosenList.add(
            0, newSong
        )

        //net request and notify others
        _updateChooseSong(
            objIdOfSongNo[0],
            newSong
        ) {
            chooseSongSubscriber?.invoke(
                KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                newSong
            )
        }
    }

    override fun getSongDetailWithInput(
        inputModel: KTVSongDetailInputModel,
        completion: (error: Exception?, out: KTVSongDetailOutputModel?) -> Unit
    ) {
        // get the target song detail
        if (songChosenList.size <= 0) {
            completion.invoke(RuntimeException("The chosen song list is empty!"), null)
            return
        }

        val filter = songChosenList.filter { it.songNo == inputModel.songNo }
        val targetSong = filter.getOrNull(0)
        if (targetSong == null) {
            completion.invoke(RuntimeException("The song no not found!"), null)
            return
        }

        completion.invoke(
            null, KTVSongDetailOutputModel(
                targetSong.songNo,
                targetSong.lyric,
                targetSong.songUrl
            )
        )

    }


    override fun subscribeChooseSongWithChanged(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSelSongModel?) -> Unit) {
        chooseSongSubscriber = changedBlock
    }


    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mainHandler.looper.thread) {
            r.run()
        } else {
            mainHandler.post(r)
        }
    }

    private fun runOnWorkingThread(r: Runnable) {
        if (Thread.currentThread() == workingHandlerThread.looper.thread) {
            r.run()
        } else {
            workingHandler.post(r)
        }
    }

    private fun initSync(complete: () -> Unit) {
        if (syncUtilsInited) {
            complete.invoke()
            return
        }

        Sync.Instance().init(context,
            mapOf(
                Pair("appid", "aab8b8f5a8cd4469a63042fcfafe7063"),
                Pair("defaultChannel", kSceneId),
                // Pair("isUseRtm", "true"),
            ),
            object : Sync.Callback {
                override fun onSuccess() {
                    syncUtilsInited = true
                    runOnMainThread { complete.invoke() }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { errorHandler?.invoke(exception) }
                }
            }
        )
    }


    // ------------------------ User operation ------------------------
    // 订阅在线用户
    private fun _subscribeOnlineUsers(completion: () -> Unit) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                //将用户信息存在本地列表
                val userInfo = item?.toObject(VLLoginModel::class.java)
                if (!userMap.containsKey(userInfo?.userNo)) {
                    userMap[userInfo?.userNo.toString()] = userInfo
                }
                _updateUserCount(userMap.size)
            }

            override fun onDeleted(item: IObject?) {
                //将用户信息移除本地列表
                userMap.remove(item?.toObject(VLLoginModel::class.java)?.userNo)
                _updateUserCount(userMap.size)
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.subscribe(kCollectionIdUser, listener)
    }

    private fun _getUserInfo(completion: (error: Exception?, list: List<VLLoginModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdUser)?.get(object : Sync.DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VLLoginModel>()
                result?.forEach {
                    val obj = it.toObject(VLLoginModel::class.java)
                    objIdOfUserNo[obj.userNo] = it.id
                    ret.add(obj)

                    userMap[obj.userNo] = obj
                }
                _updateUserCount(userMap.count())
                runOnMainThread { completion.invoke(null, ret) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception, null) }
            }
        })
    }

    private fun _addUserInfo(completion: () -> Unit) {
        val localUserInfo = VLLoginModel(UserManager.getInstance().user.userNo)
        mSceneReference?.collection(kCollectionIdUser)
            ?.add(localUserInfo, object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject) {
                    runOnMainThread { completion.invoke() }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke() }
                }
            })
    }

    private fun _removeUser(completion: (error: Exception?) -> Unit) {
        val objectId = objIdOfUserNo[UserManager.getInstance().user.userNo] ?: return
        mSceneReference?.collection(kCollectionIdUser)?.delete(objectId, object : Sync.Callback {
            override fun onSuccess() {
                objIdOfUserNo.remove(UserManager.getInstance().user.userNo)
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun _addUserIfNeed(completion: (Exception?, Int?) -> Unit) {
        _subscribeOnlineUsers {}
        _getUserInfo { error, list ->
            if (error != null || list == null) {
                completion.invoke(error, null)
                return@_getUserInfo
            }
            if (!userMap.containsKey(UserManager.getInstance().user.userNo)) {
                _addUserInfo {
                    _getUserInfo { error2, list2 ->
                        if (error2 != null || list2 == null) {
                            completion.invoke(error2, null)
                            return@_getUserInfo
                        }
                        completion.invoke(null, list2.size)
                    }
                }
            } else {
                completion.invoke(null, list.size)
            }
        }
    }

    private fun _updateUserCount(count: Int) {
        val roomInfo = roomMap[currRoomNo] ?: return
        if (count == roomInfo.roomPeopleNum) {
            return
        }
        mSceneReference?.update(
            HashMap(
                GsonUtils.covertToMap(
                    VLRoomListModel(
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
            object : Sync.DataItemCallback {
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

    private fun _genUserSeatInfo(seatIndex: Int): VLRoomSeatModel {
        val vLRoomSeatModel = VLRoomSeatModel(
            roomMap[currRoomNo]?.creatorNo == UserManager.getInstance().user.userNo,
            UserManager.getInstance().getUser().headUrl,
            UserManager.getInstance().getUser().userNo,
            UserManager.getInstance().getUser().id.toString(),
            UserManager.getInstance().getUser().name,
            seatIndex,
            false,
            0,
            0,
            ifSelTheSingSong = false,
            ifJoinedChorus = false
        )
        return vLRoomSeatModel
    }

    private fun _autoOnSeatIfNeed(completion: (error: Exception?, seat: List<VLRoomSeatModel>?) -> Unit) {
        val cacheRoom = roomMap[currRoomNo] ?: return
        _subscribeSeats {}
        _getSeatInfo { err, list ->
            if (err != null) {
                completion.invoke(err, null)
                return@_getSeatInfo
            }
            var hasMaster = false
            val outList = ArrayList<VLRoomSeatModel>()
            seatMap.forEach {
                it.value?.let { seat ->
                    outList.add(seat)
                    seatListChangeSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                        seat
                    )
                    if (seat.isMaster) {
                        hasMaster = true
                    }
                }
            }
            if (!hasMaster && cacheRoom.creatorNo == UserManager.getInstance().user.userNo) {
                val targetSeatInfo = _genUserSeatInfo(0)
                _addSeatInfo(targetSeatInfo) { error ->
                    if (error != null) {
                        completion.invoke(error, null)
                        return@_addSeatInfo
                    }
                    outList.add(targetSeatInfo)
                    seatListChangeSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                        targetSeatInfo
                    )
                    completion.invoke(null, outList)
                }
            } else {
                completion.invoke(null, outList)
            }
        }
    }


    private fun _getSeatInfo(completion: (error: Exception?, list: List<VLRoomSeatModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdSeatInfo)?.get(object : Sync.DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VLRoomSeatModel>()
                result?.forEach {
                    val obj = it.toObject(VLRoomSeatModel::class.java)
                    objIdOfSeatIndex[obj.onSeat] = it.id
                    ret.add(obj)

                    // 储存在本地map中
                    seatMap[obj.onSeat.toString()] = obj
                }
                runOnMainThread { completion.invoke(null, ret) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception, null) }
            }
        })
    }

    private fun _updateSeat(seatInfo: VLRoomSeatModel, completion: (error: Exception?) -> Unit) {
        val objectId = objIdOfSeatIndex[seatInfo.onSeat] ?: return
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.update(objectId, seatInfo, object : Sync.Callback {
                override fun onSuccess() {
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun _removeSeat(seatInfo: VLRoomSeatModel, completion: (error: Exception?) -> Unit) {
        val objectId = objIdOfSeatIndex[seatInfo.onSeat] ?: return
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.delete(objectId, object : Sync.Callback {
                override fun onSuccess() {
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun _addSeatInfo(seatInfo: VLRoomSeatModel, completion: (error: Exception?) -> Unit) {
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.add(seatInfo, object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject) {
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun _subscribeSeats(completion: () -> Unit) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {
                // TODO Nothing?
            }

            override fun onUpdated(item: IObject?) {
                val obj = item?.toObject(VLRoomSeatModel::class.java) ?: return
                objIdOfSeatIndex[obj.onSeat] = obj.id

                if (seatMap.containsKey(obj.onSeat.toString())) {
                    seatMap[obj.onSeat.toString()] = obj
                    seatListChangeSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                        obj
                    )
                } else {
                    seatMap[obj.onSeat.toString()] = obj
                    seatListChangeSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                        obj
                    )
                }
            }

            override fun onDeleted(item: IObject?) {
                item ?: return

                seatMap.forEach { entry ->
                    entry.value?.let { seat ->
                        if (objIdOfSeatIndex[seat.onSeat] == item.id) {
                            seatMap.remove(entry.key)
                            seatListChangeSubscriber?.invoke(
                                KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                                seat
                            )
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
        mSceneReference?.subscribe(kCollectionIdSeatInfo, listener)
    }

    // ------------ Choose song operation ---------------------

    private fun _sortChooseSongList(completion: (error: Exception?) -> Unit) {
        val firstSong = songChosenList.getOrNull(0) ?: return
        val firstObjId = objIdOfSongNo[0]
        val firstNewSong = VLRoomSelSongModel(
            firstSong.songName,
            firstSong.songNo,
            firstSong.songUrl,
            firstSong.singer,
            firstSong.lyric,
            firstSong.imageUrl,

            firstSong.userNo,
            firstSong.userId,
            firstSong.name,
            firstSong.chorusNo,
            firstSong.isChorus,
            firstSong.isOriginal,
            1
        )
        _updateChooseSong(
            firstObjId, firstNewSong
        ) { error ->
            if (error != null) {
                completion.invoke(error)
                return@_updateChooseSong
            }
            songChosenList[0] = firstNewSong

            val songsSise = songChosenList.size
            val letchCount = CountDownLatch(songsSise - 1)
            for (i in 1 until songsSise) {
                songChosenList[i].let { song ->
                    val newSong = VLRoomSelSongModel(
                        song.songName,
                        song.songNo,
                        song.songUrl,
                        song.singer,
                        song.lyric,
                        song.imageUrl,

                        song.userNo,
                        song.userId,
                        song.name,
                        song.chorusNo,
                        song.isChorus,
                        song.isOriginal,
                        i + 1
                    )
                    songChosenList[i] = newSong
                    _updateChooseSong(
                        objIdOfSongNo[i],
                        newSong
                    ) {
                        letchCount.countDown()
                    }
                }
            }
            runOnMainThread {
                letchCount.await()
                completion.invoke(null)
            }

        }
    }


    private fun _getChooseSongInfo(completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdChooseSong)?.get(object : Sync.DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VLRoomSelSongModel>()
                val retObjId = ArrayList<String>()
                result?.forEach {
                    val obj = it.toObject(VLRoomSelSongModel::class.java)
                    ret.add(obj)
                    retObjId.add(it.id)
                }
                ret.sortBy { it.sort }
                songChosenList.clear()
                songChosenList.addAll(ret)
                objIdOfSongNo.clear()
                objIdOfSongNo.addAll(retObjId)
                runOnMainThread { completion.invoke(null, ret) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception, null) }
            }
        })
    }

    private fun _updateChooseSong(
        objectId: String,
        songInfo: VLRoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        mSceneReference?.collection(kCollectionIdChooseSong)
            ?.update(objectId, songInfo, object : Sync.Callback {
                override fun onSuccess() {
                    completion.invoke(null)
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(exception)
                }
            })
    }

    private fun _addChooseSongInfo(
        songInfo: VLRoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        mSceneReference?.collection(kCollectionIdChooseSong)
            ?.add(songInfo, object : Sync.DataItemCallback {
                override fun onSuccess(result: IObject) {

                    songChosenList.add(songInfo)
                    objIdOfSongNo.add(result.id)

                    chooseSongSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                        songInfo
                    )

                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun _removeAllUsersChooseSong() {
        songChosenList.filter { it.userNo == UserManager.getInstance().user.userNo }
            .forEach {
                val indexOf = songChosenList.indexOf(it)
                songChosenList.removeAt(indexOf)
                _removeChooseSong(objIdOfSongNo.removeAt(indexOf), {})
            }
    }

    private fun _removeChooseSong(objectId: String, completion: (error: Exception?) -> Unit) {
        mSceneReference?.collection(kCollectionIdChooseSong)
            ?.delete(objectId, object : Sync.Callback {
                override fun onSuccess() {
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun _subscribeRoomChanged() {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                val roomInfo = item.toObject(VLRoomListModel::class.java)
                roomMap[roomInfo.roomNo] = roomInfo
                runOnMainThread {
                    roomStatusSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                        roomInfo
                    )
                }
            }

            override fun onDeleted(item: IObject?) {
                val roomInfo = roomMap[item?.id] ?: return
                runOnMainThread {
                    roomStatusSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted,
                        roomInfo
                    )
                }
                roomMap.remove(roomInfo.roomNo)
            }

            override fun onSubscribeError(ex: SyncManagerException?) {

            }

        }
        roomSubscribeListener.add(listener)
        mSceneReference?.subscribe(listener)
    }

    private fun _subscribeChooseSong(completion: () -> Unit) {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {
                // TODO Nothing?
            }

            override fun onUpdated(item: IObject?) {
                val songInfo = item?.toObject(VLRoomSelSongModel::class.java) ?: return
                chooseSongSubscriber?.invoke(
                    KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                    songInfo
                )
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
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
        mSceneReference?.subscribe(kCollectionIdChooseSong, listener)
    }
}