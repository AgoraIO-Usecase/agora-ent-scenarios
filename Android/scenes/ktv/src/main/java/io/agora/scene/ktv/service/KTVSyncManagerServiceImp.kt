package io.agora.scene.ktv.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.manager.UserManager
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback


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


    @Volatile
    private var syncUtilsInited = false

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var mSceneReference : SceneReference? = null

    private var selSongModel: VLRoomSelSongModel? = null

    private var roomStatusSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomListModel?) -> Unit)? =
        null
    private var roomUserCountSubscriber: ((Int) -> Unit)? =
        null
    private var seatListChangeSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomSeatModel?) -> Unit)? =
        null
    private var chooseSongSubscriber: ((KTVServiceProtocol.KTVSubscribe, VLRoomSelSongModel?) -> Unit)? =
        null

    private var roomNo: String = ""
    private var roomMap: MutableMap<String, VLRoomListModel> = mutableMapOf<String, VLRoomListModel>()
    private var userMap: MutableMap<String, VLLoginModel?> = mutableMapOf<String, VLLoginModel?>()
    private var seatMap: MutableMap<String, VLRoomSeatModel?> = mutableMapOf<String, VLRoomSeatModel?>()
    private var songList: ArrayList<VLRoomSelSongModel> = ArrayList<VLRoomSelSongModel>()

    // ========= 房间相关 =====================

    override fun getRoomList(
        completion: (error: Exception?, list: List<VLRoomListModel>?) -> Unit
    ) {
        initScene {
            Sync.Instance().getScenes(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    val ret = ArrayList<VLRoomListModel>()
                    result?.forEach {
                        val obj = it.toObject(VLRoomListModel::class.java)
                        obj.objectId = it.id
                        ret.add(obj)

                        roomMap[obj.roomNo] = obj
                    }
                    //按照创建时间顺序排序 TODO
                    //val roomList = ret.sortedWith(compareBy({ it.createdAt })) as ArrayList<VLRoomListModel>
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
        initScene {
            val isPrivate = if (inputModel.isPrivate == 0) false else true
            val vlRoomListModel = VLRoomListModel(
                inputModel.name,
                isPrivate,
                inputModel.password,
                UserManager.getInstance().user.userNo,
                (100000.. 999999).random().toString(),
                0,
                (1..2).random().toString(),
                "",
                "",
                "", //TODO TIME
                "",
                0,
                "",
                0,
                "",
                UserManager.getInstance().user.userNo,
                null
            )
            val scene = Scene()
            scene.id = inputModel.name
            scene.userId = inputModel.userNo

            val property: Map<String, String?> = mapOf(
                "name" to vlRoomListModel.name,
                "isPravite" to isPrivate.toString(),
                "password" to vlRoomListModel.password,
                "creator" to vlRoomListModel.creator,
                "roomNo" to vlRoomListModel.roomNo,
                "creatorNo" to vlRoomListModel.creator,
                "isChorus" to vlRoomListModel.isChorus.toString(),
                "bgOption" to vlRoomListModel.bgOption,
                "soundEffect" to vlRoomListModel.soundEffect,
                "belCanto" to vlRoomListModel.belCanto,
                "createdAt" to vlRoomListModel.createdAt,
                "updatedAt" to vlRoomListModel.updatedAt,
                "status" to vlRoomListModel.status.toString(),
                "deletedAt" to vlRoomListModel.deletedAt,
                "roomPeopleNum" to vlRoomListModel.roomPeopleNum.toString(),
                "icon" to vlRoomListModel.icon,
            )
            scene.property = property

            Sync.Instance().createScene(scene, object: Sync.Callback{
                override fun onSuccess() {
                    roomMap[vlRoomListModel.roomNo] = vlRoomListModel
                    runOnMainThread { completion.invoke(null, KTVCreateRoomOutputModel(vlRoomListModel.roomNo, vlRoomListModel.password)) }
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
        initScene {
            Sync.Instance().joinScene(inputModel.roomNo, object: Sync.JoinSceneCallback{
                override fun onSuccess(sceneReference: SceneReference?) {
                    mSceneReference = sceneReference
                    roomNo = inputModel.roomNo
                    val room = roomMap[roomNo]

                    runOnMainThread {
                        // 空白麦位
                        val seatsArray = _emptySeats()

                        val kTVJoinRoomOutputModel = KTVJoinRoomOutputModel(
                            room!!.name,
                            inputModel.roomNo,
                            room!!.creatorNo,
                            "",
                            seatsArray,
                            "", //TODO fetchToken
                            "", //TODO fetchToken
                        "" //TODO fetchToken
                        )
                        completion.invoke(null, kTVJoinRoomOutputModel)
                        // 订阅用户数量事件
//                        _addUserIfNeed()
//                        _autoOnSeatIfNeed()
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread { completion.invoke(exception, null) }
                }
            })
        }
    }

    override fun leaveRoomWithCompletion(completion: (error: Exception?) -> Unit) {
        val room = roomMap[roomNo]
        if (room!!.creatorNo == UserManager.getInstance().user.userNo) {
            // 移除房间
            _removeUser { error -> run {} }
            _updateUserCount(userMap.count() - 1) // TODO max(userMap.count() - 1, 0)
            roomMap.remove(roomNo)
            //Sync.Instance().destroy();

            mSceneReference?.delete(object: Sync.Callback{
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
    }

    override fun changeMVCoverWithInput(
        inputModel: KTVChangeMVCoverInputModel,
        completion: (error: Exception?) -> Unit
    ) {
//        val roomInfo = roomMap[roomNo]
//        roomInfo!!.bgOption = inputModel.mvIndex.toString()
        // TODO key? value?
        mSceneReference?.update("bgOption", inputModel.mvIndex.toString(), object: Sync.DataItemCallback {
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
        _addUserIfNeed()
        _autoOnSeatIfNeed()
    }

    // =================== 麦位相关 ===============================

    override fun onSeatWithInput(
        inputModel: KTVOnSeatInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val seatInfo = _getUserSeatInfo(inputModel.seatIndex)
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
                        originSeatInfo.objectId
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
                        originSeatInfo.ifJoinedChorus,
                        originSeatInfo.objectId
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

    override fun getSongDetailWithInput(
        inputModel: KTVSongDetailInputModel,
        completion: (error: Exception?, out: KTVSongDetailOutputModel) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun removeSongWithInput(
        inputModel: KTVRemoveSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        _removeChooseSong(inputModel.objectId, completion)
    }

    override fun getChoosedSongsListWithCompletion(completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit) {
        _getChooseSongInfo(completion)
    }

    override fun switchSongWithInput(
        inputModel: KTVSwitchSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun markSongDidPlayWithInput(
        inputModel: VLRoomSelSongModel,
        completion: (error: Exception?) -> Unit
    ) {
        _updateChooseSong(inputModel, completion)
    }

    override fun chooseSongWithInput(
        inputModel: KTVChooseSongInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        var songInfo = VLRoomSelSongModel(
            inputModel.songName,
            inputModel.songNo,
            inputModel.songUrl,
            inputModel.singer,
            "",
            0,
            inputModel.imageUrl,
        )
        _addChooseSongInfo(songInfo, { error ->
            run {
                completion(error)
            }
        })
    }

    override fun makeSongTopWithInput(
        inputModel: KTVMakeSongTopInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        val topSong = songList[0]
        val userSongList = songList.filter { it.objectId == inputModel?.objectId } as ArrayList<VLRoomSelSongModel>
        if (userSongList.count() > 0) {
            val song = userSongList[0]

            // mark input song to top
            val targetSort = 0//(_minSort() ?? 0) -1
            val songInfo = VLRoomSelSongModel(
                topSong.songName,
                topSong.songNo,
                topSong.songUrl,
                topSong.singer,
                topSong.lyric,
                topSong.status,
                topSong.imageUrl,

                objectId = topSong.objectId
            )

            if (topSong.objectId != song.objectId) {
                val songInfo = VLRoomSelSongModel(
                    topSong.songName,
                    topSong.songNo,
                    topSong.songUrl,
                    topSong.singer,
                    topSong.lyric,
                    topSong.status,
                    topSong.imageUrl,

                    objectId = song.objectId
                )
                _updateChooseSong(songInfo, {})
            }

            _updateChooseSong(songInfo, {})
        }
    }

    override fun joinChorusWithInput(
        inputModel: KTVJoinChorusInputModel,
        completion: (error: Exception?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun becomeSolo() {
        TODO("Not yet implemented")
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

    private fun initScene(complete: () -> Unit) {
        if (syncUtilsInited) {
            complete.invoke()
            return
        }

        Sync.Instance().init(context,
            mapOf(
                Pair("appid", BuildConfig.AGORA_APP_ID),
                Pair("defaultChannel", kSceneId),
            ),
            object : Sync.Callback {
                override fun onSuccess() {
                    syncUtilsInited = true
                    runOnMainThread { complete.invoke() }
                }

                override fun onFail(exception: SyncManagerException?) {
                    runOnMainThread{ errorHandler?.invoke(exception) }
                }
            }
        )
    }

    // ------------------------ User operation ------------------------
    // 订阅在线用户
    private fun _subscribeOnlineUsers(completion: () -> Unit) {
        mSceneReference?.subscribe("userCollection", object: Sync.EventListener {
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
        })
    }

    private fun _getUserInfo(completion: (error: Exception?, list: List<VLLoginModel>?) -> Unit) {
        mSceneReference?.collection("userCollection")?.get(object: Sync.DataListCallback{
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VLLoginModel>()
                result?.forEach {
                    val obj = it.toObject(VLLoginModel::class.java)
                    obj.objectId = it.id
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
        mSceneReference?.collection("userCollection")?.add(localUserInfo, object: Sync.DataItemCallback{
            override fun onSuccess(result: IObject) {
                runOnMainThread { completion.invoke() }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke() }
            }
        })
    }

    private fun _removeUser(completion: (error: Exception?) -> Unit) {
        val objectId = userMap[UserManager.getInstance().user.userNo]?.objectId
        mSceneReference?.collection("userCollection")?.delete(objectId, object: Sync.Callback{
            override fun onSuccess() {
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun _addUserIfNeed() {
        _subscribeOnlineUsers {}
        _getUserInfo {error, list ->
            run {
                if (!userMap.containsKey(UserManager.getInstance().user.userNo)) {
                    _addUserInfo {
                        _getUserInfo { error, list ->
                            run {}
                        }
                    }
                }
            }
        }
    }

    private fun _updateUserCount(count: Int) {
        var roomInfo = roomMap[roomNo]
        if (count == roomInfo!!.roomPeopleNum) {
            return
        }
//        roomInfo?.updatedAt = ""
//        roomInfo?.roomPeopleNum = count

        // TODO key? value?
        mSceneReference?.update("roomPeopleNum", count, object: Sync.DataItemCallback {
            override fun onSuccess(result: IObject?) {
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread {
                }
            }
        })

        roomUserCountSubscriber?.invoke(count)
    }

    // --------------------- Seat operation --------------------------
    //空白麦位
    private fun _emptySeats() : List<VLRoomSeatModel> {
        val seatsArray = ArrayList<VLRoomSeatModel>()
        for (i in 0..7) {
            val vLRoomSeatModel = VLRoomSeatModel(
                false,
                "",
                "",
                "",
                "",
                i,
                false,
                0,
                0,
                false,
                false
            )
            seatsArray.add(vLRoomSeatModel)
        }
        return seatsArray
    }

    private fun _getUserSeatInfo(seatIndex: Int) : VLRoomSeatModel {
        val vLRoomSeatModel = VLRoomSeatModel(
            false,
            UserManager.getInstance().getUser().headUrl,
            UserManager.getInstance().getUser().userNo,
            UserManager.getInstance().getUser().id.toString(),
            UserManager.getInstance().getUser().name,
            seatIndex,
            false,
            0,
            0,
            false,
            false
        )
        return vLRoomSeatModel
    }

    private fun _autoOnSeatIfNeed() {
        _subscribeSeats {}
        _getSeatInfo { error, list ->
            run {
                seatMap.forEach {
                    if (it.value?.objectId != null) {
                        seatListChangeSubscriber?.invoke(
                            KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated,
                            it.value
                        )
                    }

                    // update seat info (user avater/nick name did changed) if seat existed
                    //val seatInfo = seatMap.filter { it.value?.userNo == UserManager.getInstance().user.userNo }.values
                    seatMap.forEach {
                        if (it.value?.userNo == UserManager.getInstance().user.userNo) {
                            val seatInfo = it.value
                            if (seatInfo != null) {
                                val targetSeatInfo = _getUserSeatInfo(seatInfo.onSeat)
                                targetSeatInfo.objectId = seatInfo.objectId
                                _updateSeat(targetSeatInfo, {})
                            }
                        }
                    }

                    // add master to first seat
                    val targetSeatInfo = _getUserSeatInfo(0)
                    _addSeatInfo(targetSeatInfo, {})
                }
            }
        }
    }

    private fun _getSeatInfo(completion: (error: Exception?, list: List<VLRoomSeatModel>?) -> Unit) {
        mSceneReference?.collection("seat_info")?.get(object: Sync.DataListCallback{
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VLRoomSeatModel>()
                result?.forEach {
                    val obj = it.toObject(VLRoomSeatModel::class.java)
                    obj.objectId = it.id
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
        val objectId = seatInfo.objectId
        mSceneReference?.collection("seat_info")?.update(objectId, seatInfo, object: Sync.Callback{
            override fun onSuccess() {
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun _removeSeat(seatInfo: VLRoomSeatModel, completion: (error: Exception?) -> Unit) {
        val objectId = seatInfo.objectId
        mSceneReference?.collection("seat_info")?.delete(objectId, object: Sync.Callback{
            override fun onSuccess() {
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun _addSeatInfo(seatInfo: VLRoomSeatModel, completion: (error: Exception?) -> Unit) {
        mSceneReference?.collection("seat_info")?.add(seatInfo, object: Sync.DataItemCallback{
            override fun onSuccess(result: IObject) {
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun _subscribeSeats(completion: () -> Unit) {
        mSceneReference?.subscribe("seat_info", object: Sync.EventListener {
            override fun onCreated(item: IObject?) {
                // TODO Nothing?
            }

            override fun onUpdated(item: IObject?) {
                val obj = item?.toObject(VLRoomSeatModel::class.java)
                obj?.objectId = item?.id
                seatMap[obj?.onSeat.toString()] = obj
                seatListChangeSubscriber?.invoke(KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated, obj)
            }

            override fun onDeleted(item: IObject?) {
                seatMap.forEach {
                    if (it.value?.objectId == item?.toObject(VLRoomSeatModel::class.java)?.objectId) {
                        val originSeat = it.value
                        if (originSeat != null) {
                            val vLRoomSeatModel = VLRoomSeatModel(
                                false,
                                "",
                                "",
                                "",
                                "",
                                originSeat.onSeat,
                                false,
                                0,
                                0,
                                false,
                                false
                            )
                            seatMap[originSeat?.onSeat.toString()] = vLRoomSeatModel
                            seatListChangeSubscriber?.invoke(KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted, originSeat)
                        }
                    }
                }
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        })
    }

    // ------------ Choose song operation ---------------------
    private fun _minSort() : Int? {
        return 0
    }

    private fun _sortChooseSongList() {

    }

    private fun _getChooseSongInfo(completion: (error: Exception?, list: List<VLRoomSelSongModel>?) -> Unit) {
        mSceneReference?.collection("choose_song")?.get(object: Sync.DataListCallback{
            override fun onSuccess(result: MutableList<IObject>?) {
                val ret = ArrayList<VLRoomSelSongModel>()
                result?.forEach {
                    val obj = it.toObject(VLRoomSelSongModel::class.java)
                    obj.objectId = it.id
                    ret.add(obj)
                }
                songList = ret
                runOnMainThread { completion.invoke(null, ret) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception, null) }
            }
        })
    }

    private fun _updateChooseSong(songInfo: VLRoomSelSongModel, completion: (error: Exception?) -> Unit) {
        val objectId = songInfo.objectId
        mSceneReference?.collection("choose_song")?.update(objectId, songInfo, object: Sync.Callback{
            override fun onSuccess() {
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun _addChooseSongInfo(songInfo: VLRoomSelSongModel, completion: (error: Exception?) -> Unit) {
        //TODO SetSort
        //SyncManager collectionKey:SYNC_MANAGER_CHOOSE_SONG_INFO
        mSceneReference?.collection("choose_song")?.add(songInfo, object: Sync.DataItemCallback{
            override fun onSuccess(result: IObject) {
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun _removeAllUsersChooseSong() {
        val userSongList = songList.filter { it.userNo == UserManager.getInstance().user.userNo }.forEach {
            _removeChooseSong(it.objectId, {})
        }
    }

    private fun _removeChooseSong(songId: String?, completion: (error: Exception?) -> Unit) {
        val objectId = songId
        mSceneReference?.collection("choose_song")?.delete(objectId, object: Sync.Callback{
            override fun onSuccess() {
                runOnMainThread { completion.invoke(null) }
            }

            override fun onFail(exception: SyncManagerException?) {
                runOnMainThread { completion.invoke(exception) }
            }
        })
    }

    private fun _markCurrentSongIfNeed() {
        if (songList.size > 0) {
            val topSong = songList[0]
            if (topSong.status == 0 && topSong.isChorus == false && topSong.userNo == UserManager.getInstance().user.userNo) {
                val songInfo = VLRoomSelSongModel(
                    topSong.songName,
                    topSong.songNo,
                    topSong.songUrl,
                    topSong.singer,
                    topSong.lyric,
                    topSong.status,
                    topSong.imageUrl,
                    objectId = topSong.objectId
                )
                _updateChooseSong(songInfo, {})
            }
        }
    }

    private fun _markSoloSongIfNeed() {
        if (songList.size > 0) {
            val topSong = songList[0]
            if (topSong.isChorus == false && topSong.userNo == UserManager.getInstance().user.userNo) {
                val songInfo = VLRoomSelSongModel(
                    topSong.songName,
                    topSong.songNo,
                    topSong.songUrl,
                    topSong.singer,
                    topSong.lyric,
                    topSong.status,
                    topSong.imageUrl,
                    objectId = topSong.objectId
                )
                _updateChooseSong(songInfo, {})
            }
        }
    }

    private fun _subscribeChooseSong(completion: () -> Unit) {
        mSceneReference?.subscribe("choose_song", object: Sync.EventListener {
            override fun onCreated(item: IObject?) {
                // TODO Nothing?
            }

            override fun onUpdated(item: IObject?) {
                val songInfo = item?.toObject(VLRoomSelSongModel::class.java)
                if (songInfo != null) {
                    songList = songList.filter { it.objectId != songInfo.objectId } as ArrayList<VLRoomSelSongModel>
                    songList.add(songInfo)
                    _sortChooseSongList()
                    chooseSongSubscriber?.invoke(KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated, songInfo)
                    _markCurrentSongIfNeed()
                }
            }

            override fun onDeleted(item: IObject?) {
                val userSongList = songList.filter { it.objectId == item?.id } as ArrayList<VLRoomSelSongModel>
                if (userSongList.count() > 0) {
                    val originSong = userSongList[0]
                    songList = songList.filter { it.objectId != originSong.objectId } as ArrayList<VLRoomSelSongModel>
                    chooseSongSubscriber?.invoke(KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted, originSong)
                    _markCurrentSongIfNeed()
                }
            }

            override fun onSubscribeError(ex: SyncManagerException?) {
                completion()
            }
        })
    }
}