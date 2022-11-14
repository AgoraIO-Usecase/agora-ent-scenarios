package io.agora.scene.ktv.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.api.apiutils.GsonUtils
import io.agora.scene.base.manager.UserManager
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback
import io.agora.syncmanager.rtm.Sync.EventListener
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
    private val roomMap = mutableMapOf<String, VLRoomListModel>() // key: roomNo
    private val userMap = mutableMapOf<String, VLLoginModel?>() // key: userNo
    private val seatMap = mutableMapOf<String, VLRoomSeatModel?>() // key: seatIndex
    private val songChosenList = ArrayList<VLRoomSelSongModel>()

    @Volatile
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

    override fun createRoom(
        inputModel: KTVCreateRoomInputModel,
        completion: (error: Exception?, out: KTVCreateRoomOutputModel?) -> Unit
    ) {
        initSync {
            val vlRoomListModel = VLRoomListModel(
                roomNo = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString(),
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

    override fun joinRoom(
        inputModel: KTVJoinRoomInputModel,
        completion: (error: Exception?, out: KTVJoinRoomOutputModel?) -> Unit
    ) {
        if (!TextUtils.isEmpty(currRoomNo)) {
            completion.invoke(RuntimeException("The room $currRoomNo has been joined!"), null)
            return
        }
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
            Sync.Instance().joinScene(inputModel.roomNo, object : Sync.JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    mSceneReference = sceneReference
                    currRoomNo = inputModel.roomNo

                    TokenGenerator.generateTokens(
                        currRoomNo,
                        UserManager.getInstance().user.id.toString(),
                        TokenGenerator.TokenGeneratorType.token006,
                        arrayOf(
                            TokenGenerator.AgoraTokenType.rtc,
                            TokenGenerator.AgoraTokenType.rtm
                        ),
                        { ret ->
                            val rtcToken = ret[TokenGenerator.AgoraTokenType.rtc] ?: ""
                            val rtmToken = ret[TokenGenerator.AgoraTokenType.rtm] ?: ""
                            innerSubscribeRoomChanged()
                            innerSubscribeChooseSong {}
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

                                    val kTVJoinRoomOutputModel = KTVJoinRoomOutputModel(
                                        cacheRoom.name,
                                        inputModel.roomNo,
                                        cacheRoom.creatorNo,
                                        cacheRoom.bgOption,
                                        seats,
                                        userSize,
                                        rtmToken,
                                        rtcToken,
                                        "", //TODO fetchToken,
                                    )
                                    runOnMainThread {
                                        completion.invoke(null, kTVJoinRoomOutputModel)
                                    }
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

        if (cacheRoom.creatorNo == UserManager.getInstance().user.userNo) {
            // 移除房间
            mSceneReference?.delete(object : Sync.Callback {
                override fun onSuccess() {
                    runOnMainThread {
                        resetCacheInfo(true)
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
        if(!isRoomDestroyed){
            innerRemoveUser {}
            innerUpdateUserCount(userMap.size - 1)
        }

        userMap.clear()
        objIdOfUserNo.clear()

        // 如果上麦了要下麦，并清空麦位信息
        if(!isRoomDestroyed){
            seatMap.forEach {
                it.value?.let { seat ->
                    if (seat.userNo == UserManager.getInstance().user.userNo) {
                        innerRemoveSeat(seat) {}
                        return@forEach
                    }
                }
            }
        }
        seatMap.clear()
        objIdOfSeatIndex.clear()

        // 删除点歌信息
        if(!isRoomDestroyed){
            songChosenList.forEachIndexed { index: Int, songModel: VLRoomSelSongModel ->
                if (songModel.userNo.equals(UserManager.getInstance().user.userNo)) {
                    innerRemoveChooseSong(objIdOfSongNo[index]) {}
                }
            }
        }
        objIdOfSongNo.clear()
        songChosenList.clear()

        if(isRoomDestroyed){
            roomMap.remove(currRoomNo)
        }
        mSceneReference = null
        currRoomNo = ""
    }

    override fun changeMVCover(
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

    override fun subscribeRoomStatus(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomListModel?) -> Unit) {
        roomStatusSubscriber = changedBlock
    }

    override fun subscribeUserListCount(changedBlock: (count: Int) -> Unit) {
        roomUserCountSubscriber = changedBlock
    }

    // =================== 麦位相关 ===============================

    override fun onSeat(
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
        val seatInfo = innerGenUserSeatInfo(inputModel.seatIndex)
        innerAddSeatInfo(seatInfo, completion)
    }

    override fun outSeat(
        inputModel: KTVOutSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val seatInfo = seatMap[inputModel.userOnSeat.toString()]
        if (seatInfo != null) {
            // 移除歌曲
            innerRemoveAllUsersChooseSong()
            // 移除座位
            innerRemoveSeat(seatInfo) {}
        }
        completion(null)
    }

    override fun openAudioStatus(isSelfMuted: Int, completion: (error: Exception?) -> Unit) {
        seatMap.forEach {
            if (it.value?.userNo == UserManager.getInstance().user.userNo) {
                val originSeatInfo = it.value
                if (originSeatInfo != null) {
                    val seatInfo = VLRoomSeatModel(
                        originSeatInfo.isMaster,
                        originSeatInfo.headUrl,
                        originSeatInfo.userNo,
                        originSeatInfo.rtcUid,
                        originSeatInfo.name,
                        originSeatInfo.seatIndex,
                        originSeatInfo.joinSing,
                        isSelfMuted, // update this
                        originSeatInfo.isVideoMuted,
                        originSeatInfo.ifSelTheSingSong,
                        originSeatInfo.ifJoinedChorus,
                    )
                    innerUpdateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun openVideoStatus(
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
                        originSeatInfo.rtcUid,
                        originSeatInfo.name,
                        originSeatInfo.seatIndex,
                        originSeatInfo.joinSing,
                        originSeatInfo.isSelfMuted,
                        isVideoMuted, // update this
                        originSeatInfo.ifSelTheSingSong,
                        originSeatInfo.ifJoinedChorus
                    )
                    innerUpdateSeat(seatInfo, completion)
                }
            }
        }
    }

    override fun subscribeSeatList(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit) {
        seatListChangeSubscriber = changedBlock
    }


    // ============= 歌曲相关 =============================

    override fun getChoosedSongsList(completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit) {
        innerGetChooseSongInfo(completion)
    }

    override fun chooseSong(
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

            isChorus = inputModel.isChorus > 0,
            userNo = UserManager.getInstance().user.userNo,
            userId = UserManager.getInstance().user.id.toString(),
            name = UserManager.getInstance().user.name,
            sort = if (songChosenList.isEmpty()) 1 else songChosenList.last().sort + 1
        )
        //net request and notify others
        innerAddChooseSongInfo(song, completion)
    }

    override fun makeSongTop(
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
        innerSortChooseSongList {
            completion.invoke(it)
        }
    }

    override fun removeSong(
        inputModel: KTVRemoveSongInputModel,
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
            if(it != null){
                completion.invoke(it)
            }else{
                innerSortChooseSongList { error->
                    completion.invoke(error)
                }
            }
        }
    }

    override fun joinChorus(
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
        innerUpdateChooseSong(
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
        innerUpdateChooseSong(
            objIdOfSongNo[0],
            newSong
        ) {
            chooseSongSubscriber?.invoke(
                KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                newSong
            )
        }
    }


    override fun subscribeChooseSong(changedBlock: (KTVServiceProtocol.KTVSubscribe, VLRoomSelSongModel?) -> Unit) {
        chooseSongSubscriber = changedBlock
    }


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

        Sync.Instance().init(context,
            mapOf(
                Pair("appid", io.agora.scene.base.BuildConfig.AGORA_APP_ID),
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
                        innerUpdateUserCount(userMap.size)
                        return
                    }
                }

            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        }
        roomSubscribeListener.add(listener)
        mSceneReference?.subscribe(kCollectionIdUser, listener)
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

    private fun innerRemoveUser(completion: (error: Exception?) -> Unit) {
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

    private fun innerAddUserIfNeed(completion: (Exception?, Int) -> Unit) {
        innerSubscribeOnlineUsers {}
        innerGetUserInfo { error, list ->
            if (error != null || list == null) {
                completion.invoke(error, 0)
                return@innerGetUserInfo
            }
            if (!userMap.containsKey(UserManager.getInstance().user.userNo)) {
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

    private fun innerGenUserSeatInfo(seatIndex: Int): VLRoomSeatModel {
        return VLRoomSeatModel(
            roomMap[currRoomNo]?.creatorNo == UserManager.getInstance().user.userNo,
            UserManager.getInstance().user.headUrl,
            UserManager.getInstance().user.userNo,
            UserManager.getInstance().user.id.toString(),
            UserManager.getInstance().user.name,
            seatIndex,
            false,
            0,
            0,
            ifSelTheSingSong = false,
            ifJoinedChorus = false
        )
    }

    private fun innerAutoOnSeatIfNeed(completion: (error: Exception?, seat: List<VLRoomSeatModel>?) -> Unit) {
        val cacheRoom = roomMap[currRoomNo] ?: return
        innerSubscribeSeats {}
        innerGetSeatInfo { err, _ ->
            if (err != null) {
                completion.invoke(err, null)
                return@innerGetSeatInfo
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
                val targetSeatInfo = innerGenUserSeatInfo(0)
                innerAddSeatInfo(targetSeatInfo) { error ->
                    if (error != null) {
                        completion.invoke(error, null)
                        return@innerAddSeatInfo
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


    private fun innerGetSeatInfo(completion: (error: Exception?, list: List<VLRoomSeatModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdSeatInfo)?.get(object : DataListCallback {
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VLRoomSeatModel>()
                result?.forEach {
                    val obj = it.toObject(VLRoomSeatModel::class.java)
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
        seatInfo: VLRoomSeatModel,
        completion: (error: Exception?) -> Unit
    ) {
        val objectId = objIdOfSeatIndex[seatInfo.seatIndex] ?: return
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

    private fun innerRemoveSeat(
        seatInfo: VLRoomSeatModel,
        completion: (error: Exception?) -> Unit
    ) {
        val objectId = objIdOfSeatIndex[seatInfo.seatIndex] ?: return
        mSceneReference?.collection(kCollectionIdSeatInfo)
            ?.delete(objectId, object : Sync.Callback {
                override fun onSuccess() {
                    objIdOfSeatIndex.remove(seatInfo.seatIndex)
                    seatMap.remove(seatInfo.seatIndex.toString())
                    runOnMainThread { completion.invoke(null) }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception) }
                }
            })
    }

    private fun innerAddSeatInfo(
        seatInfo: VLRoomSeatModel,
        completion: (error: Exception?) -> Unit
    ) {
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

    private fun innerSubscribeSeats(completion: () -> Unit) {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
            }

            override fun onUpdated(item: IObject?) {
                val obj = item?.toObject(VLRoomSeatModel::class.java) ?: return
                objIdOfSeatIndex[obj.seatIndex] = item.id

                if (seatMap.containsKey(obj.seatIndex.toString())) {
                    seatMap[obj.seatIndex.toString()] = obj
                    seatListChangeSubscriber?.invoke(
                        KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated,
                        obj
                    )
                } else {
                    seatMap[obj.seatIndex.toString()] = obj
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
                        if (objIdOfSeatIndex[seat.seatIndex] == item.id) {
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

    private fun innerSortChooseSongList(completion: (error: Exception?) -> Unit) {
        val firstSong = songChosenList.getOrNull(0)
        if(firstSong == null){
            completion.invoke(null)
            return
        }
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
        innerUpdateChooseSong(
            firstObjId, firstNewSong
        ) { error ->
            if (error != null) {
                completion.invoke(error)
                return@innerUpdateChooseSong
            }
            songChosenList[0] = firstNewSong

            val songsSize = songChosenList.size
            val letchCount = CountDownLatch(songsSize - 1)
            for (i in 1 until songsSize) {
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
                    innerUpdateChooseSong(
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


    private fun innerGetChooseSongInfo(completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit) {
        mSceneReference?.collection(kCollectionIdChooseSong)?.get(object : DataListCallback {
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

    private fun innerUpdateChooseSong(
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

    private fun innerAddChooseSongInfo(
        songInfo: VLRoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        mSceneReference?.collection(kCollectionIdChooseSong)
            ?.add(songInfo, object : Sync.DataItemCallback {
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

    private fun innerRemoveAllUsersChooseSong() {
        songChosenList.filter { it.userNo == UserManager.getInstance().user.userNo }
            .forEach {
                val indexOf = songChosenList.indexOf(it)
                songChosenList.removeAt(indexOf)
                innerRemoveChooseSong(objIdOfSongNo.removeAt(indexOf)){}
            }
    }

    private fun innerRemoveChooseSong(objectId: String, completion: (error: Exception?) -> Unit) {
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

    private fun innerSubscribeRoomChanged() {
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
        mSceneReference?.subscribe(listener)
    }

    private fun innerSubscribeChooseSong(completion: () -> Unit) {
        val listener = object : EventListener {
            override fun onCreated(item: IObject?) {
                // do Nothing
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