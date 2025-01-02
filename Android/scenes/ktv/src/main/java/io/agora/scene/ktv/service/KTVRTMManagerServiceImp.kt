package io.agora.scene.ktv.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.reflect.TypeToken
import io.agora.rtmsyncmanager.ISceneResponse
import io.agora.rtmsyncmanager.RoomExpirationPolicy
import io.agora.rtmsyncmanager.RoomService
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUICommonConfig
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.rtmsyncmanager.service.IAUIUserService
import io.agora.rtmsyncmanager.service.collection.AUIAttributesModel
import io.agora.rtmsyncmanager.service.collection.AUICollectionException
import io.agora.rtmsyncmanager.service.collection.AUIListCollection
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmException
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ObservableHelper
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.ServerConfig
import io.agora.scene.base.manager.UserManager
import io.agora.scene.ktv.KTVLogger
import io.agora.scene.ktv.KtvCenter
import kotlin.random.Random

/**
 * Ktv sync manager service imp
 *
 * @property mContext
 * @property mErrorHandler
 * @constructor Create empty Ktv sync manager service imp
 */
class KTVSyncManagerServiceImp constructor(
    private val mContext: Context, private val mErrorHandler: ((Exception?) -> Unit)?
) : KTVServiceProtocol, ISceneResponse, IAUIUserService.AUIUserRespObserver {
    private val TAG = "KTV_Service_LOG"
    private val kSceneId = "scene_ktv_${BuildConfig.APP_VERSION_NAME}"
    private val kCollectionSeatInfo = "seat_info" // map collection
    private val kCollectionChosenSong = "choose_song" // list collection
    private val kCollectionChorusInfo = "chorister_info" // list collection

    // Seat mapCollection
    private fun getSeatCollection(roomId: String): AUIMapCollection? {
        if (roomId.isEmpty()) {
            return null
        }
        val scene = mSyncManager.getScene(roomId)
        return scene?.getCollection(kCollectionSeatInfo) { a, b, c -> AUIMapCollection(a, b, c) }
    }

    // Chosen song listCollection
    private fun getChosenSongCollection(roomId: String): AUIListCollection? {
        if (roomId.isEmpty()) {
            return null
        }
        val scene = mSyncManager.getScene(roomId)
        return scene?.getCollection(kCollectionChosenSong) { a, b, c -> AUIListCollection(a, b, c) }
    }

    // Chorus listCollection
    private fun getChorusCollection(roomId: String): AUIListCollection? {
        if (roomId.isEmpty()) {
            return null
        }
        val scene = mSyncManager.getScene(roomId)
        return scene?.getCollection(kCollectionChorusInfo) { a, b, c -> AUIListCollection(a, b, c) }
    }

    private val mMainHandler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * Run on main thread
     *
     * @param r
     */
    private fun runOnMainThread(r: Runnable) {
        if (Thread.currentThread() == mMainHandler.looper.thread) {
            r.run()
        } else {
            mMainHandler.post(r)
        }
    }

    /**
     * sync manager
     */
    private val mSyncManager: SyncManager

    /**
     * room manager
     */
    private val mRoomManager = AUIRoomManager()

    /**
     * room service
     */
    private val mRoomService: RoomService

    /**
     * current room no
     */
    @Volatile
    private var mCurRoomNo: String = ""

    private val roomList: MutableList<AUIRoomInfo> = mutableListOf()

    /**
     * current user
     */
    private val mCurrentUser: AUIUserThumbnailInfo get() = AUIRoomContext.shared().currentUserInfo

    /**
     * room user list
     */
    private val mUserList = mutableListOf<AUIUserInfo>()

    /**
     * room seat map
     */
    private val mSeatMap = mutableMapOf<Int, RoomMicSeatInfo>()

    /**
     * room song chosen list
     */
    private val mSongChosenList = mutableListOf<ChosenSongInfo>()

    /**
     * room chorister list
     */
    private val mChoristerList = mutableListOf<RoomChoristerInfo>()

    /**
     * Observable helper
     */
    private val mObservableHelper = ObservableHelper<KtvServiceListenerProtocol>()

    // time limit
    private val ROOM_AVAILABLE_DURATION: Long = 20 * 60 * 1000 // 20min

    init {
        HttpManager.setBaseURL(ServerConfig.roomManagerUrl)
        val rtmSyncTag = "KTV_RTM_LOG"
        AUILogger.initLogger(
            AUILogger.Config(mContext, "KTV", logCallback = object : AUILogger.AUILogCallback {
                override fun onLogDebug(tag: String, message: String) {
                    KTVLogger.d(rtmSyncTag, "$tag $message")
                }

                override fun onLogInfo(tag: String, message: String) {
                    KTVLogger.d(rtmSyncTag, "$tag $message")
                }

                override fun onLogWarning(tag: String, message: String) {
                    KTVLogger.w(rtmSyncTag, "$tag $message")
                }

                override fun onLogError(tag: String, message: String) {
                    KTVLogger.e(rtmSyncTag, "$tag $message")
                }

            })
        )

        val commonConfig = AUICommonConfig().apply {
            context = mContext
            appId = KtvCenter.mAppId
            owner = AUIUserThumbnailInfo().apply {
                userId = UserManager.getInstance().user.id.toString()
                userName = UserManager.getInstance().user.name
                userAvatar = UserManager.getInstance().user.headUrl
            }
            host = ServerConfig.roomManagerUrl
        }
        mSyncManager = SyncManager(mContext, null, commonConfig)

        val roomExpirationPolicy = RoomExpirationPolicy()
        roomExpirationPolicy.expirationTime = ROOM_AVAILABLE_DURATION
        roomExpirationPolicy.isAssociatedWithOwnerOffline = true
        mRoomService = RoomService(roomExpirationPolicy, mRoomManager, mSyncManager)
    }

    private fun startTimer() {
        mMainHandler.postDelayed(timerRoomCountDownTask, 1000)
    }

    private val timerRoomCountDownTask = object : Runnable {
        override fun run() {
            if (mCurRoomNo.isEmpty()) return
            val roomDuration = getCurrentDuration(mCurRoomNo)
            if (roomDuration >= ROOM_AVAILABLE_DURATION) {
                mMainHandler.removeCallbacks(this)
                onSceneExpire(mCurRoomNo)
            } else {
                mMainHandler.postDelayed(this, 1000)
            }
        }

    }

    /**
     * Init rtm sync
     *
     * @param completion
     * @receiver
     */
    private fun initRtmSync(completion: (exception: AUIRtmException?) -> Unit) {
        if (mSyncManager.rtmManager.isLogin) {
            completion.invoke(null)
            return
        }
        if (KtvCenter.mRtmToken.isEmpty()) {
            KTVLogger.d(TAG, "initRtmSync, renewToken start")
            KtvCenter.generateToken { rtmToken, exception ->
                val token = rtmToken ?: run {
                    KTVLogger.e(TAG, "initRtmSync, $exception")
                    completion.invoke(AUIRtmException(-1, exception?.message ?: "error", ""))
                    return@generateToken
                }
                mSyncManager.login(token, completion = {
                    if (it == null) {
                        completion.invoke(null)
                        KTVLogger.d(TAG, "initRtmSync, with renewToken loginRtm success")
                    } else {
                        completion.invoke(it)
                        KTVLogger.e(TAG, "initRtmSync, with renewToken loginRtm failed: $it")
                    }
                })
            }
        } else {
            mSyncManager.login(KtvCenter.mRtmToken, completion = {
                if (it == null) {
                    completion.invoke(null)
                    KTVLogger.d(TAG, "initRtmSync, without loginRtm success")
                } else {
                    completion.invoke(it)
                    KTVLogger.e(TAG, "initRtmSync, without renewToken loginRtm failed: $it")
                }
            })
        }
    }

    // Local time difference from restful server
    private var rsetfulDiffTs: Long = 0

    /**
     * Get room list
     *
     * @param completion
     * @receiver
     */
    override fun getRoomList(completion: (error: Exception?, roomInfoList: List<AUIRoomInfo>?) -> Unit) {
        KTVLogger.d(TAG, "getRoomList start")
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }
            mRoomService.getRoomList(KtvCenter.mAppId, kSceneId, 0, 50,
                cleanClosure = { auiRoomInfo ->
                    return@getRoomList auiRoomInfo.roomOwner?.userId == KtvCenter.mUser.id.toString()

                },
                completion = { uiException, ts, roomList ->
                    if (uiException == null) {
                        ts?.let { serverTs ->
                            rsetfulDiffTs = System.currentTimeMillis() - serverTs
                        }
                        val newRoomList = roomList?.sortedBy { -it.createTime } ?: emptyList()
                        this.roomList.clear()
                        this.roomList.addAll(newRoomList)
                        KTVLogger.d(TAG, "getRoomList success,serverTs:$ts roomCount:${newRoomList.size}")
                        runOnMainThread { completion.invoke(null, newRoomList) }
                    } else {
                        KTVLogger.e(TAG, "getRoomList error, $uiException")
                        runOnMainThread {
                            completion.invoke(uiException, null)
                        }
                    }
                })
        }
    }

    private fun getRandomThumbnailId(crateAt: Long) =
        Random(crateAt).nextInt(0, 5).toString()

    /**
     * Create room
     *
     * @param roomMame
     * @param password
     * @param completion
     * @receiver
     */
    override fun createRoom(
        roomName: String,
        password: String?,
        completion: (error: Exception?, out: AUIRoomInfo?) -> Unit
    ) {
        KTVLogger.d(TAG, "createRoom start")
        val roomId = (Random(System.currentTimeMillis()).nextInt(100000) + 1000000).toString()
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"), null)
                return@initRtmSync
            }
            val createAt = System.currentTimeMillis() - rsetfulDiffTs
            val roomInfo = AUIRoomInfo().apply {
                this.roomId = roomId
                this.roomName = roomName
                this.roomOwner = AUIUserThumbnailInfo().apply {
                    userId = mCurrentUser.userId
                    userName = mCurrentUser.userName
                    userAvatar = mCurrentUser.userAvatar
                }
                this.createTime = createAt
                this.customPayload[KTVParameters.ROOM_USER_COUNT] = 1
                this.customPayload[KTVParameters.THUMBNAIL_ID] = getRandomThumbnailId(createAt)
                this.customPayload[KTVParameters.PASSWORD] = password ?: ""
                this.customPayload[KTVParameters.IS_PRIVATE] = !password.isNullOrEmpty()
            }
            val scene = mSyncManager.createScene(roomInfo.roomId)
            scene.bindRespDelegate(this)
            scene.userService.registerRespObserver(this)
            innerSubscribeAll(roomId)
            mCurRoomNo = roomInfo.roomId
            mRoomService.createRoom(KtvCenter.mAppId, kSceneId, roomInfo, completion = { rtmException, _ ->
                if (rtmException == null) {
                    KTVLogger.d(TAG, "createRoom success: $roomInfo")
                    mCurRoomNo = roomInfo.roomId
                    startTimer()
                    runOnMainThread {
                        completion.invoke(null, roomInfo)
                    }
                } else {
                    mCurRoomNo = ""
                    KTVLogger.e(TAG, "createRoom failed: $rtmException")
                    runOnMainThread {
                        completion.invoke(Exception("${rtmException.message}(${rtmException.code})"), null)
                    }
                }
            })
        }
    }

    /**
     * Join room
     *
     * @param completion
     * @receiver
     */
    override fun joinRoom(roomId: String, password: String?, completion: (error: Exception?) -> Unit) {
        if (mCurRoomNo.isNotEmpty()) {
            completion.invoke(Exception("already join room $mCurRoomNo!"))
            return
        }
        val cacheRoom = roomList.firstOrNull { it.roomId == roomId }
        if (cacheRoom == null) {
            completion.invoke(Exception("room $mCurRoomNo null!"))
            return
        }
        val roomPassword = cacheRoom.customPayload[KTVParameters.PASSWORD] as? String
        if (!roomPassword.isNullOrEmpty() && roomPassword != password) {
            completion.invoke(Exception("password is wrong!"))
            return
        }
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"))
                return@initRtmSync
            }
            val scene = mSyncManager.createScene(roomId)
            scene.bindRespDelegate(this)
            scene.userService.registerRespObserver(this)
            innerSubscribeAll(roomId)
            mCurRoomNo = roomId
            mRoomService.enterRoom(KtvCenter.mAppId, kSceneId, roomId, completion = { rtmException ->
                if (rtmException == null) {
                    KTVLogger.d(TAG, "enterRoom success: ${cacheRoom.roomId}")
                    mCurRoomNo = cacheRoom.roomId
                    runOnMainThread {
                        completion.invoke(null)
                    }
                } else {
                    mCurRoomNo = ""
                    KTVLogger.e(TAG, "enterRoom failed: $rtmException")
                    runOnMainThread {
                        completion.invoke(Exception("${rtmException.message}(${rtmException.code})"))
                    }
                }
            })
        }
    }

    /**
     * Leave room
     *
     * @param completion
     * @receiver
     */
    override fun leaveRoom(completion: (error: Exception?) -> Unit) {
        mSyncManager.getScene(mCurRoomNo)?.let { scene ->
            scene.unbindRespDelegate(this)
            scene.userService.unRegisterRespObserver(this)
        }
        if (AUIRoomContext.shared().isRoomOwner(mCurRoomNo)) {
            mMainHandler.removeCallbacks(timerRoomCountDownTask)
        }
        initRtmSync {
            if (it != null) {
                completion.invoke(Exception("${it.message}"))
                return@initRtmSync
            } else {
                completion.invoke(null)
                mRoomService.leaveRoom(KtvCenter.mAppId, kSceneId, mCurRoomNo)
            }
        }
        mUserList.clear()
        mSeatMap.clear()
        mSongChosenList.clear()
        mChoristerList.clear()
        mCurRoomNo = ""

    }

    /**
     * On seat
     *
     * @param seatIndex
     * @param completion
     * @receiver
     */
    override fun enterSeat(seatIndex: Int?, completion: (error: Exception?) -> Unit) {
        val collection = getSeatCollection(mCurRoomNo) ?: return
        val targetSeat = RoomMicSeatInfo(
            seatIndex = seatIndex ?: -1,
            owner = AUIRoomContext.shared().currentUserInfo,
            isAudioMuted = seatIndex != null, // seatIndex=null automatically joins the seat, and the microphone needs to be enabled by default
            isVideoMuted = true
        )
        val seatMap = GsonTools.beanToMap(targetSeat).toMutableMap()
        // Remove the seatIndex field from the object, merger does not need to modify the index
        seatMap.remove("seatIndex")
        collection.mergeMetaData(
            valueCmd = RoomSeatCmd.enterSeatCmd.name,
            value = mapOf(seatIndex.toString() to seatMap),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "enterSeat success roomId:$mCurRoomNo")
                } else {
                    KTVLogger.e(TAG, "enterSeat failed roomId:$mCurRoomNo, $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    /**
     * leave seat
     *
     * @param completion
     * @receiver
     */
    override fun leaveSeat(completion: (error: Exception?) -> Unit) {
        val targetSeatInfo = mSeatMap.values.find { it.owner?.userId == mCurrentUser.userId }
        if (targetSeatInfo == null) {
            completion.invoke(Exception("Seat not found"))
        } else {
            val collection = getSeatCollection(mCurRoomNo) ?: return
            collection.mergeMetaData(
                valueCmd = RoomSeatCmd.leaveSeatCmd.name,
                value = mapOf(
                    targetSeatInfo.seatIndex.toString() to mapOf(
                        "owner" to GsonTools.beanToMap(AUIUserThumbnailInfo())
                    )
                ),
                callback = { collectionException ->
                    if (collectionException == null) {
                        KTVLogger.d(TAG, "leaveSeat success, seatInfo:$targetSeatInfo")
                    } else {
                        KTVLogger.e(TAG, "leaveSeat failed, $collectionException")
                    }
                    runOnMainThread {
                        completion.invoke(collectionException)
                    }
                })
        }
    }

    /**
     * Kick seat
     *
     * @param seatIndex
     * @param completion
     * @receiver
     */
    override fun kickSeat(seatIndex: Int, completion: (error: Exception?) -> Unit) {
        val collection = getSeatCollection(mCurRoomNo) ?: return
        collection.mergeMetaData(
            valueCmd = RoomSeatCmd.kickSeatCmd.name,
            value = mapOf(
                seatIndex.toString() to mapOf(
                    "owner" to GsonTools.beanToMap(AUIUserThumbnailInfo())
                )
            ),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "kickSeat success, seatInfo:$seatIndex")
                } else {
                    KTVLogger.e(TAG, "kickSeat failed, $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    /**
     * Update seat audio mute status
     *
     * @param mute
     * @param completion
     * @receiver
     */
    override fun updateSeatAudioMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit) {
        val targetSeatInfo = mSeatMap.values.find { it.owner?.userId == mCurrentUser.userId }
        if (targetSeatInfo == null) {
            completion.invoke(Exception("You are not on seat"))
        } else {
            val collection = getSeatCollection(mCurRoomNo) ?: return
            val seatIndex = targetSeatInfo.seatIndex
            collection.mergeMetaData(
                valueCmd = RoomSeatCmd.muteAudioCmd.name,
                value = mapOf(
                    seatIndex.toString() to mapOf("isAudioMuted" to mute)
                ),
                callback = { collectionException ->
                    if (collectionException == null) {
                        KTVLogger.d(TAG, "updateSeatAudioMuteStatus success  seatInfo:$targetSeatInfo")
                    } else {
                        KTVLogger.e(TAG, "updateSeatAudioMuteStatus failed, $collectionException")
                    }
                    runOnMainThread {
                        completion.invoke(collectionException)
                    }
                })
        }
    }

    /**
     * Update seat video mute status
     *
     * @param mute
     * @param completion
     * @receiver
     */
    override fun updateSeatVideoMuteStatus(mute: Boolean, completion: (error: Exception?) -> Unit) {
        val targetSeatInfo = mSeatMap.values.find { it.owner?.userId == mCurrentUser.userId }
        if (targetSeatInfo == null) {
            completion.invoke(Exception("You are not on seat"))
        } else {
            val collection = getSeatCollection(mCurRoomNo) ?: return
            val seatIndex = targetSeatInfo.seatIndex
            collection.mergeMetaData(
                valueCmd = RoomSeatCmd.muteVideoCmd.name,
                value = mapOf(
                    seatIndex.toString() to mapOf("isVideoMuted" to mute)
                ),
                callback = { collectionException ->
                    if (collectionException == null) {
                        KTVLogger.d(TAG, "updateSeatVideoMuteStatus success  seatInfo:$targetSeatInfo")
                    } else {
                        KTVLogger.e(TAG, "updateSeatVideoMuteStatus failed, $collectionException")
                    }
                    runOnMainThread {
                        completion.invoke(collectionException)
                    }
                })
        }
    }

    /**
     * Get chosen songs list
     *
     * @param completion
     * @receiver
     */
    override fun getChosenSongList(completion: (error: Exception?, list: List<ChosenSongInfo>?) -> Unit) {
        val collection = getChosenSongCollection(mCurRoomNo) ?: return
        collection.getMetaData { collectionException, value ->
            if (collectionException != null) {
                KTVLogger.e(TAG, "getChosenSongList $collectionException")
                runOnMainThread {
                    completion.invoke(collectionException, null)
                }
                return@getMetaData
            }
            try {
                val songList = GsonTools.toList(GsonTools.beanToString(value), ChosenSongInfo::class.java)
                KTVLogger.d(TAG, "getChosenSongList onSuccess size:${songList?.size ?: 0}")
                mSongChosenList.clear()
                mSongChosenList.addAll(songList ?: emptyList())
                runOnMainThread {
                    completion.invoke(null, mSongChosenList)
                }

            } catch (e: Exception) {
                KTVLogger.e(TAG, "getChosenSongList onFail roomId:$mCurRoomNo $e")
                runOnMainThread {
                    completion.invoke(e, null)
                }
            }
        }
    }

    /**
     * Choose song
     *
     * @param inputModel
     * @param completion
     * @receiver
     */
    override fun chooseSong(inputModel: ChooseSongInputModel, completion: (error: Exception?) -> Unit) {
        val collection = getChosenSongCollection(mCurRoomNo) ?: return
        val chosenSong = ChosenSongInfo(
            songName = inputModel.songName,
            songNo = inputModel.songNo,
            singer = inputModel.singer,
            imageUrl = inputModel.imageUrl,
            owner = mCurrentUser,
            status = PlayStatus.idle,
            createAt = getCurrentTs(mCurRoomNo)
        )
        collection.addMetaData(
            valueCmd = RoomSongCmd.chooseSongCmd.name,
            value = GsonTools.beanToMap(chosenSong),
            filter = listOf(mapOf("songNo" to chosenSong.songNo)),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "chooseSong success, song:$chosenSong")
                } else {
                    KTVLogger.e(TAG, "chooseSong failed, $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    /**
     * Pin song
     *
     * @param songCode
     * @param completion
     * @receiver
     */
    override fun pinSong(songCode: String, completion: (error: Exception?) -> Unit) {
        // move the song to second position
        if (mSongChosenList.size <= 0) {
            completion.invoke(Exception("The chosen song list is empty!"))
            return
        }
        if (mSongChosenList.size < 3) {
            completion.invoke(Exception("The chosen songs size is less then three, it is unnecessary to top up!"))
            return
        }
        val targetSong = mSongChosenList.firstOrNull { it.songNo == songCode } ?: run {
            completion.invoke(Exception("The song no not found!"))
            return
        }
        val collection = getChosenSongCollection(mCurRoomNo) ?: return
        val newSong = targetSong.copy(pinAt = getCurrentDuration(mCurRoomNo))
        collection.mergeMetaData(
            valueCmd = RoomSongCmd.pingSongCmd.name,
            value = mapOf("pinAt" to newSong.pinAt),
            filter = listOf(
                mapOf("songNo" to songCode)
            ),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "pinSong success, song:$newSong")
                } else {
                    KTVLogger.e(TAG, "pinSong failed roomId, $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    /**
     * Make song did play
     *
     * @param songCode
     * @param completion
     * @receiver
     */
    override fun makeSongDidPlay(songCode: String, completion: (error: Exception?) -> Unit) {
        if (mSongChosenList.size <= 0) {
            completion.invoke(Exception("The chosen song list is empty!"))
            return
        }
        val targetSong = mSongChosenList.firstOrNull { it.songNo == songCode } ?: run {
            completion.invoke(Exception("The song no not found!"))
            return
        }
        if (targetSong.status == PlayStatus.playing) {
            completion.invoke(Exception("The song is playing!"))
            return
        }
        val collection = getChosenSongCollection(mCurRoomNo) ?: return
        val newSong = targetSong.copy(status = PlayStatus.playing)
        collection.mergeMetaData(
            valueCmd = RoomSongCmd.updatePlayStatusCmd.name,
            value = mapOf("status" to newSong.status),
            filter = listOf(
                mapOf("songNo" to songCode)
            ),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "makeSongDidPlay success, song:$newSong")
                } else {
                    KTVLogger.e(TAG, "makeSongDidPlay failed, $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    override fun removeSong(songCode: String, completion: (error: Exception?) -> Unit) {
        if (mSongChosenList.size <= 0) {
            completion.invoke(Exception("The chosen song list is empty!"))
            return
        }
        val targetSong = mSongChosenList.firstOrNull { it.songNo == songCode } ?: run {
            completion.invoke(Exception("The song no not found!"))
            return
        }
        val collection = getChosenSongCollection(mCurRoomNo) ?: return
        collection.removeMetaData(
            valueCmd = RoomSongCmd.removeSongCmd.name,
            filter = listOf(
                mapOf("songNo" to songCode)
            ),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "removeSong success song:$targetSong")
                } else {
                    KTVLogger.e(TAG, "removeSong failed, $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    /**
     * Join chorus
     *
     * @param songCode
     * @param completion
     * @receiver
     */
    override fun joinChorus(songCode: String, completion: (error: Exception?) -> Unit) {
        val collection = getChorusCollection(mCurRoomNo) ?: return
        val choristerInfo = RoomChoristerInfo(mCurrentUser.userId, songCode)
        collection.addMetaData(
            valueCmd = RoomChorusCmd.joinChorusCmd.name,
            value = GsonTools.beanToMap(choristerInfo),
            filter = listOf(
                mapOf("chorusSongNo" to songCode, "userId" to mCurrentUser.userId)
            ),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "joinChorus success, choristerInfo:$choristerInfo")
                } else {
                    KTVLogger.e(TAG, "joinChorus failed,  $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    /**
     * Leave chorus
     *
     * @param songCode
     * @param completion
     * @receiver
     */
    override fun leaveChorus(songCode: String, completion: (error: Exception?) -> Unit) {
        val collection = getChorusCollection(mCurRoomNo) ?: run {
            completion.invoke(AUICollectionException.ErrorCode.unknown.toException())
            return
        }
        collection.removeMetaData(
            valueCmd = RoomChorusCmd.leaveChorusCmd.name,
            filter = listOf(mapOf("userId" to mCurrentUser.userId, "chorusSongNo" to songCode)),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "leaveChorus success songCode:$songCode")
                } else {
                    KTVLogger.e(TAG, "leaveChorus failed, $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    /**
     * Get current duration
     *
     * @param channelName
     * @return
     */
    override fun getCurrentDuration(channelName: String): Long {
        if (channelName.isEmpty()) return 0
        val scene = mSyncManager.getScene(channelName)
        return scene?.getRoomDuration() ?: 0L
    }

    /**
     * Get current ts
     *
     * @param channelName
     * @return
     */
    override fun getCurrentTs(channelName: String): Long {
        if (channelName.isEmpty()) return 0
        val scene = mSyncManager.getScene(channelName)
        return scene?.getCurrentTs() ?: 0L
    }

    /**
     * Subscribe listener
     *
     * @param listener
     */
    override fun subscribeListener(listener: KtvServiceListenerProtocol) {
        mObservableHelper.subscribeEvent(listener)
        if (mUserList.isNotEmpty()) {
            listener.onUserCountUpdate(mUserList.size)
        }
        if (mSongChosenList.isNotEmpty()) {
            listener.onChosenSongListDidChanged(mSongChosenList)
        }
        if (mSeatMap.isNotEmpty()) {
            listener.onMicSeatSnapshot(mSeatMap)
        }
    }

    /**
     * Unsubscribe listener
     *
     * @param listener
     */
    override fun unsubscribeListener(listener: KtvServiceListenerProtocol) {
        mObservableHelper.unSubscribeEvent(listener)
    }

    /**
     * On will init scene metadata
     *
     * @param channelName
     * @return
     */
    override fun onWillInitSceneMetadata(channelName: String): Map<String, Any> {
        val seatMap = mutableMapOf<String, Any>()
        for (i in 0 until 8) {
            val seat = RoomMicSeatInfo().apply {
                seatIndex = i
                if (i == 0) {
                    owner = AUIRoomContext.shared().currentUserInfo
                    isAudioMuted = false
                } else {
                    owner = AUIUserThumbnailInfo()
                    isAudioMuted = true
                }
            }
            seatMap[i.toString()] = seat
        }

        return mapOf(kCollectionSeatInfo to seatMap)
    }

    /**
     * On token privilege will expire
     *
     * @param channelName
     */
    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        KTVLogger.d(TAG, "onTokenPrivilegeWillExpire, $channelName")
        KtvCenter.generateToken { rtmToken, exception ->
            val token = rtmToken ?: run {
                KTVLogger.e(TAG, "onTokenPrivilegeWillExpire, with renewRtmToken failed: $exception")
                return@generateToken
            }
            mSyncManager.login(token, completion = { rtmException ->
                if (rtmException == null) {
                    KTVLogger.d(TAG, "onTokenPrivilegeWillExpire, with loginRtm success")
                } else {
                    KTVLogger.e(TAG, "onTokenPrivilegeWillExpire, with loginRtm failed: $rtmException")
                }
            })
        }
    }

    /**
     * On scene expire
     *
     * @param channelName
     */
    override fun onSceneExpire(channelName: String) {
        KTVLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            leaveRoom { }
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onRoomExpire()
            }
        }
    }

    /**
     * On scene destroy
     *
     * @param channelName
     */
    override fun onSceneDestroy(channelName: String) {
        KTVLogger.d(TAG, "onSceneExpire, channelName:$channelName")
        if (mCurRoomNo == channelName) {
            leaveRoom { }
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onRoomDestroy()
            }
        }
    }

    /**
     * On scene user be kicked
     *
     * @param channelName
     * @param userId
     */
    override fun onSceneUserBeKicked(channelName: String, userId: String) {
        KTVLogger.d(TAG, "onSceneUserBeKicked, channelName:$channelName, userId:$userId")
    }


    /**
     * On room user snapshot
     *
     * @param roomId
     * @param userList
     *
     */
    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserInfo>?) {
        KTVLogger.d(TAG, "onRoomUserSnapshot, roomId:$roomId, userList:${userList?.count()}")
        if (mCurRoomNo != roomId) {
            return
        }
        userList?.let {
            this.mUserList.clear()
            this.mUserList.addAll(it)
        }
        mObservableHelper.notifyEventHandlers { delegate ->
            delegate.onUserCountUpdate(mUserList.size)
        }
    }

    /**
     * On room user enter
     *
     * @param roomId
     * @param userInfo
     */
    override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        KTVLogger.d(TAG, "onRoomUserEnter, roomId:$roomId, userInfo:$userInfo")
        if (mCurRoomNo != roomId) {
            return
        }
        mUserList.removeIf { it.userId == userInfo.userId }
        mUserList.add(userInfo)
        mObservableHelper.notifyEventHandlers { delegate ->
            delegate.onUserCountUpdate(mUserList.size)
        }
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId) ?: return
        // Everyone can modify user count
        cacheRoom.customPayload[KTVParameters.ROOM_USER_COUNT] = mUserList.count()
        mRoomManager.updateRoomInfo(KtvCenter.mAppId, kSceneId, cacheRoom, callback = { auiException, roomInfo ->
            if (auiException == null) {
                KTVLogger.d(TAG, "onRoomUserEnter updateRoom success: $roomInfo")
            } else {
                KTVLogger.e(TAG, "onRoomUserEnter updateRoom failed: $mCurRoomNo $auiException")
            }
        })
    }

    /**
     * On room user leave
     *
     * @param roomId
     * @param userInfo
     * @param reason
     */
    override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        KTVLogger.d(TAG, "onRoomUserLeave, roomId:$roomId, userInfo:$userInfo")
        if (mCurRoomNo != roomId) {
            return
        }
        mUserList.removeIf { it.userId == userInfo.userId }
        mObservableHelper.notifyEventHandlers { delegate ->
            delegate.onUserCountUpdate(mUserList.size)
        }
        val cacheRoom = AUIRoomContext.shared().getRoomInfo(roomId) ?: return
        // Everyone can modify user count
        cacheRoom.customPayload[KTVParameters.ROOM_USER_COUNT] = mUserList.count()
        mRoomManager.updateRoomInfo(KtvCenter.mAppId, kSceneId, cacheRoom, callback = { auiException, roomInfo ->
            if (auiException == null) {
                KTVLogger.d(TAG, "onRoomUserLeave updateRoom success: $roomId, $roomInfo")
            } else {
                KTVLogger.d(TAG, "onRoomUserLeave updateRoom failed: $roomId $auiException")
            }
        })
        // Arbitrator remove seat
        if (AUIRoomContext.shared().getArbiter(roomId)?.isArbiter() == true) {
            mSeatMap.values.firstOrNull { it.owner?.userId == userInfo.userId }?.let { roomMicSeatInfo ->
                kickSeat(roomMicSeatInfo.seatIndex) {}
            }
        }
    }

    /**
     * On room user update
     *
     * @param roomId
     * @param userInfo
     */
    override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        KTVLogger.d(TAG, "onRoomUserUpdate, roomId:$roomId, userInfo:$userInfo")
    }

    // Subscribe to collection
    private fun innerSubscribeAll(roomId: String) {
        innerSubscribeSeat(roomId)
        innerSubscribeSong(roomId)
        innerSubscribeChorus(roomId)
    }

    // Subscribe to seat mapCollection
    private fun innerSubscribeSeat(roomId: String) {
        val seatCollection = getSeatCollection(roomId) ?: return

        fun getUserId(map: Any?): String? {
            val parentMap = map as? Map<*, *>
            return (parentMap?.get("owner") as? Map<*, *>)?.get("userId") as? String
        }

        seatCollection.subscribeAttributesDidChanged { channelName, observeKey, value ->
            if (observeKey != kCollectionSeatInfo) return@subscribeAttributesDidChanged
            KTVLogger.d(TAG, "attributesDidChanged roomId:$channelName key:$observeKey")

            val seats = value.getMap() ?: GsonTools.toBean(
                GsonTools.beanToString(value),
                object : TypeToken<Map<String, Any>>() {}.type
            )
            val seatMap = mutableMapOf<Int, RoomMicSeatInfo>()
            seats?.values?.forEach {
                GsonTools.toBean(GsonTools.beanToString(it), RoomMicSeatInfo::class.java)?.let { seatInfo ->
                    seatMap[seatInfo.seatIndex] = seatInfo
                }
            }
            if (mSeatMap.isEmpty()) {
                mSeatMap.putAll(seatMap)
                mObservableHelper.notifyEventHandlers { delegate ->
                    delegate.onMicSeatSnapshot(mSeatMap)
                }
            } else {
                seatMap.values.forEach { newSeatInfo ->
                    val index = newSeatInfo.seatIndex
                    val oldSeatInfo = mSeatMap[index]
                    mSeatMap[index] = newSeatInfo
                    val newSeatUserId = newSeatInfo.owner?.userId ?: ""
                    val oldSeatUserId = oldSeatInfo?.owner?.userId ?: ""
                    // Different user, or different audio status, or different video status
                    if (newSeatUserId != oldSeatUserId || newSeatInfo.isAudioMuted != oldSeatInfo?.isAudioMuted || newSeatInfo.isVideoMuted != oldSeatInfo.isVideoMuted
                    ) {
                        mObservableHelper.notifyEventHandlers { delegate ->
                            delegate.onUserSeatUpdate(newSeatInfo)
                        }
                    }
                    if (oldSeatUserId.isEmpty() && newSeatUserId.isNotEmpty()) {
                        KTVLogger.d(TAG, "onUserEnterSeat: $newSeatInfo")
                        newSeatInfo.owner?.let { newUser ->
                            mObservableHelper.notifyEventHandlers { delegate ->
                                delegate.onUserEnterSeat(index, newUser)
                            }
                        }
                    }
                    if (oldSeatUserId.isNotEmpty() && newSeatUserId.isEmpty()) {
                        KTVLogger.d(TAG, "onUserLeaveSeat: $newSeatInfo, originUserId:$oldSeatUserId")
                        oldSeatInfo?.owner?.let { oldUser ->
                            mObservableHelper.notifyEventHandlers { delegate ->
                                delegate.onUserLeaveSeat(index, oldUser)
                            }
                        }
                    }
                    if (oldSeatInfo?.isAudioMuted != newSeatInfo.isAudioMuted) {
                        KTVLogger.d(TAG, "onSeatAudioMute: $newSeatInfo")
                        mObservableHelper.notifyEventHandlers { delegate ->
                            delegate.onSeatAudioMute(index, newSeatInfo.isAudioMuted)
                        }
                    }
                    if (oldSeatInfo?.isVideoMuted != newSeatInfo.isVideoMuted) {
                        KTVLogger.d(TAG, "onSeatVideoMute: $newSeatInfo")
                        mObservableHelper.notifyEventHandlers { delegate ->
                            delegate.onSeatVideoMute(index, newSeatInfo.isVideoMuted)
                        }
                    }

                }
            }
        }

        seatCollection.subscribeWillMerge { publisherId, valueCmd, newValue, oldValue ->
            val seatCmd = enumValueOrNull<RoomSeatCmd>(valueCmd)
            if (seatCmd == null) {
                KTVLogger.e(TAG, "illegal seatCmd $valueCmd ")
                return@subscribeWillMerge AUICollectionException.ErrorCode.unsupportedAction.toException()
            }
            // Only support modifying one person
            if (newValue.size != 1) {
                return@subscribeWillMerge AUICollectionException.ErrorCode.unsupportedAction.toException()
            }
            val seatKey = newValue.keys.first()

            when (seatCmd) {
                RoomSeatCmd.enterSeatCmd -> {
                    val seatIndex = seatKey.toIntOrNull() ?: -1
                    if (seatIndex < 0 || seatIndex > 7) {
                        return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                    }
                    KTVLogger.d(TAG, "subscribeWillMerge seatKey:$seatKey")
                    val willEnterSeatUserId = getUserId(newValue[seatKey])
                    if (oldValue.values.any { getUserId(it) == willEnterSeatUserId }) { // User already in seat
                        return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "user already enter seat")
                    }
                    val oldSeatUserId = getUserId(oldValue[seatKey])
                    if (!oldSeatUserId.isNullOrEmpty()) { // Seat occupied
                        return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "seat already has user")
                    }
                    return@subscribeWillMerge null
                }

                RoomSeatCmd.leaveSeatCmd, RoomSeatCmd.kickSeatCmd -> {
                    val oldSeatUserId = getUserId(oldValue[seatKey]) ?: ""
                    val seatIndex = seatKey.toIntOrNull() ?: -1
                    if (seatIndex == 0) {
                        return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "room owner can't leave seat")
                    }
                    if (seatCmd == RoomSeatCmd.leaveSeatCmd) { // Leave seat or owner operation
                        val isOwner = AUIRoomContext.shared().isRoomOwner(mCurRoomNo, publisherId)
                        val canLeave = publisherId == oldSeatUserId || isOwner
                        if (!canLeave) {
                            return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                        }
                    }
                    if (seatCmd == RoomSeatCmd.kickSeatCmd) { // Only owner can kick user
                        val isOwner = AUIRoomContext.shared().isRoomOwner(mCurRoomNo, publisherId)
                        if (!isOwner) {
                            return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                        }
                    }
                    // Remove user's song, chorus
                    innerRemoveChosenSong(oldSeatUserId) {}
                    innerRemoveChorusByUserId(oldSeatUserId) {}
                    return@subscribeWillMerge null
                }

                RoomSeatCmd.muteAudioCmd, RoomSeatCmd.muteVideoCmd -> {
                    val oldSeatUserId = getUserId(oldValue[seatKey]) ?: ""
                    val isOwner = AUIRoomContext.shared().isRoomOwner(mCurRoomNo, publisherId)
                    val canMute = publisherId == oldSeatUserId || isOwner
                    if (!canMute) {
                        return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                    }
                    return@subscribeWillMerge null
                }

                else -> {
                    return@subscribeWillMerge AUICollectionException.ErrorCode.unsupportedAction.toException()
                }
            }
        }

        seatCollection.subscribeValueWillChange { publisherId, valueCmd, newItem ->
            val seatCmd = enumValueOrNull<RoomSeatCmd>(valueCmd)
            if (seatCmd != RoomSeatCmd.enterSeatCmd) {
                return@subscribeValueWillChange newItem
            }
            val seatValueMap = seatCollection.getLocalMetaData().getMap() ?: return@subscribeValueWillChange newItem

            if (newItem.size != 1 || seatValueMap.containsKey(newItem.keys.first())) {
                return@subscribeValueWillChange newItem
            }

            val tempItem = mutableMapOf<String, Any>()

            // Only allow modification of positions 1-7
            for (i in 1..7) {
                val value = seatValueMap["$i"]
                if (getUserId(value).isNullOrEmpty()) { // Find empty seat to join
                    val newValue = newItem.values.first()
                    tempItem["$i"] = newValue
                    return@subscribeValueWillChange tempItem
                }
            }
            return@subscribeValueWillChange newItem
        }
    }

    // Subscribe to selected song list collection
    private fun innerSubscribeSong(roomId: String) {
        val songCollection = getChosenSongCollection(roomId) ?: return

        fun getUserId(map: Any?): String? {
            val parentMap = map as? Map<*, *>
            return (parentMap?.get("owner") as? Map<*, *>)?.get("userId") as? String
        }

        fun songStatus(songValue: Map<String, Any>): Int {
            val status = songValue["status"]
            val statusValue = if (status is Long) {
                (status as? Long)?.toInt()
            } else {
                status as? Int
            }
            return statusValue ?: PlayStatus.idle
        }

        fun songNo(songValue: Map<String, Any>): String {
            return songValue["songNo"] as? String ?: ""
        }

        songCollection.subscribeAttributesDidChanged { channelName, observeKey, value ->
            if (observeKey != kCollectionChosenSong) return@subscribeAttributesDidChanged
            KTVLogger.d(TAG, "attributesDidChanged roomId:$channelName key:$observeKey")

            val songList = GsonTools.toList(GsonTools.beanToString(value.getList()), ChosenSongInfo::class.java)
            KTVLogger.d(TAG, "$kCollectionChosenSong songList: $songList")
            mSongChosenList.clear()
            mSongChosenList.addAll(songList ?: emptyList())
            mObservableHelper.notifyEventHandlers { delegate ->
                delegate.onChosenSongListDidChanged(songList ?: emptyList())
            }
        }

        songCollection.subscribeWillAdd { publisherId, valueCmd, value ->
            val songCmd = enumValueOrNull<RoomSongCmd>(valueCmd)
            if (songCmd == null) {
                KTVLogger.e(TAG, "illegal songCmd $valueCmd ")
                return@subscribeWillAdd AUICollectionException.ErrorCode.unsupportedAction.toException()
            }
            val seatValueMap = getSeatCollection(mCurRoomNo)?.getLocalMetaData()?.getMap() ?: run {
                return@subscribeWillAdd AUICollectionException.ErrorCode.unknown.toException()
            }
            when (songCmd) {
                RoomSongCmd.chooseSongCmd -> {
                    val userId = getUserId(value)
                    // Need to join seat to choose song
                    val onSeat = seatValueMap.values.any { getUserId(it) == userId }
                    if (!onSeat) {
                        return@subscribeWillAdd AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                    }
                    return@subscribeWillAdd null
                }

                else -> {
                    return@subscribeWillAdd AUICollectionException.ErrorCode.unsupportedAction.toException()
                }
            }
        }

        songCollection.subscribeWillMerge { publisherId, valueCmd, newValue, oldValue ->
            val songCmd = enumValueOrNull<RoomSongCmd>(valueCmd)
            if (songCmd == null) {
                KTVLogger.e(TAG, "illegal songCmd $valueCmd ")
                return@subscribeWillMerge AUICollectionException.ErrorCode.unsupportedAction.toException()
            }

            val seatValueMap = getSeatCollection(mCurRoomNo)?.getLocalMetaData()?.getMap() ?: run {
                return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException()
            }
            when (songCmd) {
                RoomSongCmd.pingSongCmd -> {
                    // Only owner can top
                    val isOwner = AUIRoomContext.shared().isRoomOwner(mCurRoomNo, publisherId)
                    if (!isOwner) {
                        return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                    }
                    return@subscribeWillMerge null
                }

                RoomSongCmd.updatePlayStatusCmd -> {  // Only in seat and song chooser can update status
                    val userId = getUserId(oldValue)
                    val seatValue = seatValueMap.values.firstOrNull { getUserId(it) == userId }
                        ?: return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")

                    if (songStatus(oldValue) == PlayStatus.playing) {
                        return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "the song is playing")
                    }
                    // Current playing song
                    val topSongValue = getChosenSongCollection(mCurRoomNo)?.getLocalMetaData()?.getList()?.first()
                    val canUpdate = topSongValue != null && songNo(topSongValue) == songNo(oldValue)
                    if (!canUpdate) {
                        return@subscribeWillMerge AUICollectionException.ErrorCode.unknown.toException(msg = "current song not first")
                    }
                    return@subscribeWillMerge null
                }

                else -> {
                    return@subscribeWillMerge AUICollectionException.ErrorCode.unsupportedAction.toException()
                }
            }
        }

        songCollection.subscribeWillRemove { publisherId, valueCmd, value ->
            val songCmd = enumValueOrNull<RoomSongCmd>(valueCmd)
            if (songCmd == null) {
                KTVLogger.e(TAG, "illegal songCmd $valueCmd ")
                return@subscribeWillRemove AUICollectionException.ErrorCode.unsupportedAction.toException()
            }

            when (songCmd) {
                RoomSongCmd.removeSongCmd -> {  // Only song chooser or owner can remove
                    val ownerSongUserId = getUserId(value)
                    val canRemove =
                        ownerSongUserId == publisherId || AUIRoomContext.shared().isRoomOwner(mCurRoomNo, publisherId)
                    if (!canRemove) {
                        return@subscribeWillRemove AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                    }
                    if (songStatus(value) == PlayStatus.playing) {
                        // Remove chorus list
                        innerRemoveAllChorus {}
                    }
                    return@subscribeWillRemove null
                }

                RoomSongCmd.removedUserSongsCmd -> return@subscribeWillRemove null

                else -> {
                    return@subscribeWillRemove AUICollectionException.ErrorCode.unsupportedAction.toException()
                }
            }
        }

        songCollection.subscribeAttributesWillSet { channelName, observeKey, valueCmd, value ->
            val songCmd = enumValueOrNull<RoomSongCmd>(valueCmd)

            if (songCmd == RoomSongCmd.pingSongCmd) {
                val songList = value.getList() ?: return@subscribeAttributesWillSet value
                return@subscribeAttributesWillSet AUIAttributesModel(list = sortChooseSongList(songList))
            } else {
                return@subscribeAttributesWillSet value
            }
        }
    }

    // Subscribe to chorus list collection
    private fun innerSubscribeChorus(roomId: String) {
        val chorusCollection = getChorusCollection(roomId) ?: return

        fun getUserId(map: Any?): String? {
            val parentMap = map as? Map<*, *>
            return (parentMap?.get("owner") as? Map<*, *>)?.get("userId") as? String
        }

        fun getSeatAudio(map: Any?): Boolean? {
            val parentMap = map as? Map<*, *>
            return parentMap?.get("isAudioMuted") as? Boolean
        }

        fun songStatus(songValue: Map<String, Any>): Int {
            val status = songValue["status"]
            val statusValue = if (status is Long) {
                (status as? Long)?.toInt()
            } else {
                status as? Int
            }
            return statusValue ?: PlayStatus.idle
        }

        chorusCollection.subscribeAttributesDidChanged { channelName, observeKey, value ->
            if (observeKey != kCollectionChorusInfo) return@subscribeAttributesDidChanged
            KTVLogger.d(TAG, "attributesDidChanged roomId:$channelName key:$observeKey")

            val choristerList = GsonTools.toList(GsonTools.beanToString(value.getList()), RoomChoristerInfo::class.java)

            KTVLogger.d(TAG, "$kCollectionChorusInfo choristerList: $choristerList")
            val oldChoristerList = mutableListOf<RoomChoristerInfo>()
            oldChoristerList.addAll(mChoristerList)
            this.mChoristerList.clear()
            this.mChoristerList.addAll(choristerList ?: emptyList())
            choristerList?.forEach { newChorister ->
                var hasChorister = false
                oldChoristerList.forEach { oldChorister ->
                    if (oldChorister.userId == newChorister.userId) {
                        hasChorister = true
                    }
                }
                if (!hasChorister) {
                    mObservableHelper.notifyEventHandlers { delegate ->
                        delegate.onChoristerDidEnter(newChorister)
                    }
                }
            }
            oldChoristerList.forEach { oldChorister ->
                var hasChorister = false
                choristerList?.forEach { newChorister ->
                    if (newChorister.userId == oldChorister.userId) {
                        hasChorister = true
                    }
                }
                if (!hasChorister) {
                    mObservableHelper.notifyEventHandlers { delegate ->
                        delegate.onChoristerDidLeave(oldChorister)
                    }
                }
            }
        }

        chorusCollection.subscribeWillAdd { publisherId, valueCmd, value ->
            val chorusCmd = enumValueOrNull<RoomChorusCmd>(valueCmd)
            if (chorusCmd == null) {
                KTVLogger.e(TAG, "illegal chorusCmd $valueCmd ")
                return@subscribeWillAdd AUICollectionException.ErrorCode.unsupportedAction.toException()
            }

            val seatValueMap = getSeatCollection(mCurRoomNo)?.getLocalMetaData()?.getMap() ?: run {
                return@subscribeWillAdd AUICollectionException.ErrorCode.unknown.toException()
            }

            // Current playing song
            val currentSongValue = getChosenSongCollection(mCurRoomNo)?.getLocalMetaData()?.getList()?.first() ?: run {
                return@subscribeWillAdd AUICollectionException.ErrorCode.unknown.toException()
            }

            when (chorusCmd) {
                RoomChorusCmd.joinChorusCmd -> {// Need to join seat to join chorus
                    val chorusUserId = value["userId"] as? String
                    var userSeatIndex = -1
                    seatValueMap.forEach { (key, seatValue) ->
                        val userId = getUserId(seatValue)
                        if (userId == chorusUserId) {
                            key.toIntOrNull()?.let { seatIndex ->
                                userSeatIndex = seatIndex
                            }
                        }

                    }
                    if (userSeatIndex < 0) {
                        return@subscribeWillAdd AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                    }

                    val sameSong = (currentSongValue["songNo"] as? String) == (value["chorusSongNo"] as? String)
                    val isPlaying = songStatus(currentSongValue) == PlayStatus.playing
                    val canJoin = sameSong && isPlaying
                    if (!canJoin) {
                        return@subscribeWillAdd AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                    }

                    // Join chorus, active microphone
                    seatValueMap["$userSeatIndex"]?.let { seatValue ->
                        val isAudioMuted = getSeatAudio(seatValue)
                        if (isAudioMuted != false) {
                            innerMuteAudio(userSeatIndex, false) {}
                        }
                    }
                    return@subscribeWillAdd null
                }

                else -> {
                    return@subscribeWillAdd AUICollectionException.ErrorCode.unsupportedAction.toException()
                }
            }
        }

        chorusCollection.subscribeWillRemove { publisherId, valueCmd, value ->
            val chorusCmd = enumValueOrNull<RoomChorusCmd>(valueCmd)
            if (chorusCmd == null) {
                KTVLogger.e(TAG, "illegal chorusCmd $valueCmd ")
                return@subscribeWillRemove AUICollectionException.ErrorCode.unsupportedAction.toException()
            }
            when (chorusCmd) {
                RoomChorusCmd.leaveChorusCmd -> {
                    val chorusUserId = value["userId"] as? String
                    val canLeave =
                        publisherId == chorusUserId || AUIRoomContext.shared().isRoomOwner(mCurRoomNo, publisherId)
                    if (!canLeave) {
                        return@subscribeWillRemove AUICollectionException.ErrorCode.unknown.toException(msg = "not permitted")
                    }

                    val seatValueMap = getSeatCollection(mCurRoomNo)?.getLocalMetaData()?.getMap()
                    seatValueMap?.forEach { (key, seatValue) ->
                        val userId = getUserId(seatValue)
                        if (userId == chorusUserId) {
                            key.toIntOrNull()?.let { seatIndex ->
                                // Mute seat
                                innerMuteAudio(seatIndex, true) {}
                            }
                        }
                    }

                    return@subscribeWillRemove null
                }

                RoomChorusCmd.kickAllOutOfChorusCmd,
                RoomChorusCmd.KickUserOutOfChorusCmd -> return@subscribeWillRemove null

                else -> return@subscribeWillRemove AUICollectionException.ErrorCode.unsupportedAction.toException()
            }
        }
    }

    // Remove specified user song information
    private fun innerRemoveChosenSong(userId: String, completion: (error: Exception?) -> Unit) {
        val collection = getChosenSongCollection(mCurRoomNo) ?: return
        collection.removeMetaData(
            valueCmd = RoomSongCmd.removedUserSongsCmd.name,
            filter = listOf(mapOf("owner" to mapOf("userId" to userId))),
            callback = {
                if (it == null) {
                    KTVLogger.d(TAG, "innerRemoveChosenSong success userId:$userId")
                } else {
                    KTVLogger.e(TAG, "innerRemoveChosenSong failed userId:$userId,$it")
                }
                runOnMainThread {
                    completion.invoke(it)
                }
            })
    }

    // Remove specified user chorus information
    private fun innerRemoveChorusByUserId(userId: String, completion: (error: Exception?) -> Unit) {
        val collection = getChorusCollection(mCurRoomNo) ?: return
        collection.removeMetaData(
            valueCmd = RoomChorusCmd.KickUserOutOfChorusCmd.name,
            filter = listOf(mapOf("userId" to userId)),
            callback = {
                if (it == null) {
                    KTVLogger.d(TAG, "innerRemoveChorusByUserId success userId:$userId")
                } else {
                    KTVLogger.e(TAG, "innerRemoveChorusByUserId failed userId:$userId,$it")
                }
                runOnMainThread {
                    completion.invoke(it)
                }
            })
    }

    // Remove all chorus
    private fun innerRemoveAllChorus(completion: (error: Exception?) -> Unit) {
        val collection = getChorusCollection(mCurRoomNo) ?: return
        collection.removeMetaData(
            valueCmd = RoomChorusCmd.kickAllOutOfChorusCmd.name,
            filter = null,
            callback = {
                if (it == null) {
                    KTVLogger.d(TAG, "innerRemoveAllChorus success")
                } else {
                    KTVLogger.e(TAG, "innerRemoveAllChorus failed,$it")
                }
                runOnMainThread {
                    completion.invoke(it)
                }
            })
    }

    // Mute microphone for specified seat
    private fun innerMuteAudio(seatIndex: Int, mute: Boolean, completion: (error: Exception?) -> Unit) {
        val collection = getSeatCollection(mCurRoomNo) ?: return
        collection.mergeMetaData(
            valueCmd = RoomSeatCmd.muteAudioCmd.name,
            value = mapOf(
                seatIndex.toString() to mapOf("isAudioMuted" to mute)
            ),
            callback = { collectionException ->
                if (collectionException == null) {
                    KTVLogger.d(TAG, "innerMuteAudio success  seatIndex:$seatIndex")
                } else {
                    KTVLogger.e(TAG, "innerMuteAudio failed, $collectionException")
                }
                runOnMainThread {
                    completion.invoke(collectionException)
                }
            })
    }

    private fun sortChooseSongList(songList: List<Map<String, Any>>): List<Map<String, Any>> {

        fun songStatus(songValue: Map<String, Any>): Int {
            val status = songValue["status"]
            val statusValue = if (status is Long) {
                (status as? Long)?.toInt()
            } else {
                status as? Int
            }
            return statusValue ?: PlayStatus.idle
        }

        fun songPin(songValue: Map<String, Any>): Long {
            return songValue["pinAt"] as? Long ?: 0
        }

        fun songCreateAt(songValue: Map<String, Any>): Long {
            return songValue["createAt"] as? Long ?: 0
        }
        // Playing song is first
        val playingList = songList.filter { songStatus(it) == PlayStatus.playing }
        // Top priority play, then top
        val pinList =
            songList.filter { songPin(it) > 0 && songStatus(it) != PlayStatus.playing }.sortedBy { songPin(it) * -1 }
        // First song is first
        val normalList =
            songList.filter { songPin(it) <= 0 && songStatus(it) != PlayStatus.playing }.sortedBy { songCreateAt(it) }

        val sortSongList = mutableListOf<Map<String, Any>>()
        sortSongList.addAll(playingList)
        sortSongList.addAll(pinList)
        sortSongList.addAll(normalList)
        return sortSongList
    }

    fun destroy() {
        KTVLogger.d(TAG, message = "destroy")
        mSyncManager.logout()
        mSyncManager.release()
    }
}