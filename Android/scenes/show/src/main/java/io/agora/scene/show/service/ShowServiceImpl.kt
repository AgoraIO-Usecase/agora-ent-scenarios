package io.agora.scene.show.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.agora.rtmsyncmanager.ISceneResponse
import io.agora.rtmsyncmanager.RoomExpirationPolicy
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUICommonConfig
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.rtmsyncmanager.service.IAUIUserService.AUIUserRespObserver
import io.agora.rtmsyncmanager.service.callback.AUIException
import io.agora.rtmsyncmanager.service.callback.AUIRoomCallback
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ThreadManager
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.show.RtcEngineInstance
import io.agora.scene.show.ShowLogger
import io.agora.scene.show.service.cloudplayer.CloudPlayerService
import io.agora.scene.show.service.rtmsync.ApplyInfo
import io.agora.scene.show.service.rtmsync.InteractionInfo
import io.agora.scene.show.service.rtmsync.InteractionType
import io.agora.scene.show.service.rtmsync.InvitationInfo
import io.agora.scene.show.service.rtmsync.InvitationType
import io.agora.scene.show.service.rtmsync.PKInfo
import io.agora.scene.show.service.rtmsync.PKType
import io.agora.scene.show.service.rtmsync.RoomPresenceInfo
import io.agora.scene.show.service.rtmsync.RoomPresenceStatus
import io.agora.scene.show.service.rtmsync.cleanExServices
import io.agora.scene.show.service.rtmsync.destroyExtensions
import io.agora.scene.show.service.rtmsync.getExApplyService
import io.agora.scene.show.service.rtmsync.getExInteractionService
import io.agora.scene.show.service.rtmsync.getExInvitationService
import io.agora.scene.show.service.rtmsync.getExMessageRetainer
import io.agora.scene.show.service.rtmsync.getExPKService
import io.agora.scene.show.service.rtmsync.getExRoomManager
import io.agora.scene.show.service.rtmsync.getExRoomPresenceService
import io.agora.scene.show.service.rtmsync.getExRoomService
import io.agora.scene.show.service.rtmsync.isExConnected
import io.agora.scene.show.service.rtmsync.isExRoomOwner
import io.agora.scene.show.service.rtmsync.setupExtensions
import io.agora.scene.show.service.rtmsync.subscribeExConnectionState

const val kRoomSceneId = "scene_show_5.0.0_vd"
const val kRoomPresenceChannelName = "scene_show_5_0_0_9999999_vd"
const val kRobotUid = 2000000001
val kRobotAvatars = listOf("https://download.shengwang.cn/demo/release/bot1.png")
val kRobotVideoRoomIds = arrayListOf(2023004, 2023005, 2023006)
val kRobotVideoStreamUrls = arrayListOf(
    "https://download.agora.io/demo/release/agora_test_video_20_music.mp4",
    "https://download.agora.io/demo/release/agora_test_video_21_music.mp4",
    "https://download.agora.io/demo/release/agora_test_video_22_music.mp4"
)

class ShowServiceImpl(context: Context) : ShowServiceProtocol {

    private val tag = "ShowServiceImpl"
    private val appId: String = BuildConfig.AGORA_APP_ID
    private val appCert: String = BuildConfig.AGORA_APP_CERTIFICATE

    private var shouldRetryLogin = false
    private val syncManager by lazy {
        // 初始化SyncManager
        val config = AUICommonConfig()
        config.appId = appId
        config.appCert = appCert
        val owner = AUIUserThumbnailInfo()
        owner.userId = UserManager.getInstance().user.id.toString()
        owner.userName = UserManager.getInstance().user.name
        owner.userAvatar = UserManager.getInstance().user.headUrl
        config.owner = owner
        config.host = BuildConfig.SERVER_HOST
        AUIRoomContext.shared().setCommonConfig(config)
        SyncManager(context, null, config)
    }

    private val cloudPlayerService by lazy {
        CloudPlayerService()
    }

    init {
        syncManager.setupExtensions(
            kRoomPresenceChannelName,
            RoomExpirationPolicy().apply {
                expirationTime = ShowServiceProtocol.ROOM_AVAILABLE_DURATION
            },
            roomHostUrl = BuildConfig.ROOM_MANAGER_SERVER_HOST,
            loggerConfig = AUILogger.Config(
                context,
                "ShowLiveSyncExtensions",
                logCallback = object : AUILogger.AUILogCallback {
                    override fun onLogDebug(tag: String, message: String) {
                        ShowLogger.d(tag, message)
                    }

                    override fun onLogInfo(tag: String, message: String) {
                        ShowLogger.d(tag, message)
                    }

                    override fun onLogWarning(tag: String, message: String) {
                        ShowLogger.d(tag, message)
                    }

                    override fun onLogError(tag: String, message: String) {
                        ShowLogger.e(tag, message = message)
                    }
                })
        )

        loginSync()
    }

    private fun loginSync(success: (() -> Unit)? = null, error: ((Exception) -> Unit)? = null) {
        TokenGenerator.generateTokens(
            "",
            UserManager.getInstance().user.id.toString(),
            TokenGenerator.TokenGeneratorType.token007,
            arrayOf(TokenGenerator.AgoraTokenType.rtc, TokenGenerator.AgoraTokenType.rtm),
            success = {
                syncManager.login(it) { ex ->
                    if (ex != null) {
                        ShowLogger.e(tag, message = "login syncManager failed: ${ex.message}")
                        shouldRetryLogin = true
                        error?.invoke(RuntimeException(ex.message))
                        return@login
                    }
                    success?.invoke()
                    ShowLogger.d(
                        tag,
                        message = "login syncManager success: ${UserManager.getInstance().user.id}"
                    )
                }
            },
            failure = {
                shouldRetryLogin = true
                error?.invoke(it ?: RuntimeException("generateToken failed"))
                ShowLogger.e(tag, message = "generateToken failed: $it")
            }
        )
    }

    private fun runOnLogined(success: () -> Unit, error: ((Exception) -> Unit)? = null) {
        if (shouldRetryLogin) {
            ShowLogger.d(tag, "retry login")
            shouldRetryLogin = false
            loginSync(success, error)
        } else {
            success.invoke()
        }
    }

    fun destroy() {
        ShowLogger.d(tag, message = "destroy")
        syncManager.destroyExtensions()
        syncManager.logout()
        syncManager.release()
    }

    override fun getRoomList(
        success: (List<ShowRoomDetailModel>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExRoomService().getRoomList(
            appId,
            kRoomSceneId,
            0,
            50,
            cleanClosure = {
                it.roomOwner?.userId == UserManager.getInstance().user.id.toString()
            },
        ) { ex, _, list ->
            if (ex != null) {
                error?.invoke(RuntimeException(ex))
            } else {
                success.invoke(appendRobotRooms(list?.map { it.toShowRoomDetailModel() } ?: emptyList()))
            }
        }
    }

    override fun createRoom(
        roomId: String,
        roomName: String,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        ShowLogger.d(tag, "createRoom: $roomId")
        runOnLogined({
            syncManager.getExRoomService().createRoom(
                appId,
                kRoomSceneId,
                ShowRoomDetailModel(
                    roomId,
                    roomName,
                    1,
                    UserManager.getInstance().user.id.toString(),
                    UserManager.getInstance().user.headUrl,
                    UserManager.getInstance().user.name,
                ).toAUIRoomInfo()
            ) { ex, roomInfo ->
                if (ex == null && roomInfo != null) {
                    ShowLogger.d(tag, "createRoom success: $roomId")
                    syncManager.getExRoomPresenceService().login {
                        syncManager.getExRoomPresenceService().setup(
                            RoomPresenceInfo(
                                roomId,
                                roomName,
                                UserManager.getInstance().user.id.toString(),
                                UserManager.getInstance().user.name,
                                UserManager.getInstance().user.headUrl,
                                RoomPresenceStatus.IDLE
                            )
                        )
                    }
                    success.invoke(roomInfo.toShowRoomDetailModel())
                } else {
                    ShowLogger.e(tag, message = "createRoom failed: $roomId, $ex")
                    error?.invoke(RuntimeException(ex))
                }
            }
        }, error)
    }


    override fun joinRoom(
        roomId: String,
        success: () -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        val robotRoom = roomId.isRobotRoom()
        ShowLogger.d(tag, "joinRoom:$roomId, isRobotRoom:$robotRoom")
        runOnLogined({
            if (robotRoom) {
                if (syncManager.isExConnected()) {
                    success.invoke()
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (syncManager.isExConnected()) {
                            success.invoke()
                        } else {
                            val ex = RuntimeException("robot room not connected")
                            ShowLogger.d(
                                tag,
                                "joinRoom error :$roomId, isRobotRoom:$robotRoom, ex:${ex.message}"
                            )
                            error?.invoke(ex)
                        }
                    }, 500)
                }
                return@runOnLogined
            }
            ShowLogger.d(tag, "enterRoom: $roomId")
            syncManager.getExRoomService().enterRoom(
                appId,
                kRoomSceneId,
                roomId
            ) { ex ->
                ShowLogger.d(tag, "enterRoom: $roomId, ex: $ex")
                if (ex != null) {
                    error?.invoke(RuntimeException(ex))
                    return@enterRoom
                }
                success.invoke()
            }
        }, error)
    }

    override fun subscribeCurrRoomEvent(
        roomId: String,
        onUpdate: (status: ShowSubscribeStatus, roomInfo: ShowRoomDetailModel?) -> Unit
    ) {
        ShowLogger.d(tag, "subscribeCurrRoomEvent: $roomId")
        val scene = syncManager.getScene(roomId)
        scene?.bindRespDelegate(object : ISceneResponse {
            override fun onSceneDestroy(channelName: String) {
                super.onSceneDestroy(channelName)
                if (roomId == channelName) {
                    onUpdate.invoke(ShowSubscribeStatus.deleted, null)
                }
            }
        })
    }

    override fun leaveRoom(roomId: String) {
        val robotRoom = roomId.isRobotRoom()
        ShowLogger.d(tag, "leaveRoom:$roomId, isRobotRoom:$robotRoom")

        if (robotRoom) {
            return
        }

        syncManager.cleanExServices(roomId)

        if (syncManager.isExRoomOwner(roomId)) {
            syncManager.getExRoomPresenceService().logout()
        }

        // 离开房间
        syncManager.getExRoomService().leaveRoom(
            appId,
            kRoomSceneId,
            roomId
        )
    }

    private fun updateRoomInfo(
        roomId: String,
        memberCountDiff: Int? = null,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        if (!AUIRoomContext.shared().isRoomOwner(roomId)) {
            error?.invoke(RuntimeException("only owner can update room info"))
            return
        }
        syncManager.getExRoomManager().getRoomInfo(
            appId,
            kRoomSceneId,
            roomId,
            object : AUIRoomCallback {
                override fun onResult(ex: AUIException?, roomInfo: AUIRoomInfo?) {
                    if (ex != null) {
                        error?.invoke(ex)
                        return
                    }

                    // 房主才能更新
                    if (roomInfo?.roomOwner?.userId != UserManager.getInstance().user.id.toString()) {
                        error?.invoke(RuntimeException("only owner can update room info"))
                        return
                    }

                    var info = roomInfo.toShowRoomDetailModel()
                    memberCountDiff?.let {
                        info = info.copy(
                            roomUserCount = info.roomUserCount + it
                        )
                    }
                    syncManager.getExRoomManager().updateRoomInfo(
                        appId,
                        kRoomSceneId,
                        info.toAUIRoomInfo(),
                        object : AUIRoomCallback {
                            override fun onResult(ex: AUIException?, roomInfo: AUIRoomInfo?) {
                                if (ex != null) {
                                    error?.invoke(ex)
                                    return
                                }
                                success?.invoke()
                            }
                        }
                    )
                }
            }
        )
    }


    override fun getAllUserList(
        roomId: String,
        success: (List<ShowUser>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        val scene = syncManager.getScene(roomId) ?: return
        scene.userService.getUserInfoList(roomId) { e, list ->
            if (e != null) {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(e))
                }
                return@getUserInfoList
            }

            // 获取用户的互动状态
            val interactionInfo =
                syncManager.getExInteractionService(roomId)
                    .getInteractionInfo()

            ThreadManager.getInstance().runOnMainThread {
                success.invoke((list ?: emptyList()).map { user ->
                    ShowUser(
                        user.userId,
                        user.userAvatar,
                        user.userName,
                        user.muteAudio,
                        if (interactionInfo?.userId == user.userId) {
                            when (interactionInfo.type) {
                                InteractionType.PK -> ShowInteractionStatus.pking
                                InteractionType.LINKING -> ShowInteractionStatus.linking
                                else -> ShowInteractionStatus.idle
                            }
                        } else {
                            ShowInteractionStatus.idle
                        },
                        isWaiting = false
                    )
                })
            }
        }
    }

    override fun subscribeUser(
        roomId: String,
        onUserChange: (ShowSubscribeStatus, ShowUser?) -> Unit
    ) {
        val userService = syncManager.getScene(roomId)?.userService ?: return
        userService.registerRespObserver(object : AUIUserRespObserver {
            override fun onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
                super.onRoomUserEnter(roomId, userInfo)
                onUserChange.invoke(
                    ShowSubscribeStatus.added, ShowUser(
                        userInfo.userId,
                        userInfo.userAvatar,
                        userInfo.userName,
                        userInfo.muteAudio
                    )
                )
            }

            override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
                super.onRoomUserUpdate(roomId, userInfo)
                onUserChange.invoke(
                    ShowSubscribeStatus.updated, ShowUser(
                        userInfo.userId,
                        userInfo.userAvatar,
                        userInfo.userName,
                        userInfo.muteAudio
                    )
                )
            }

            override fun onRoomUserLeave(
                roomId: String,
                userInfo: AUIUserInfo,
                reason: AUIRtmUserLeaveReason
            ) {
                super.onRoomUserLeave(roomId, userInfo, reason)
                onUserChange.invoke(
                    ShowSubscribeStatus.deleted, ShowUser(
                        userInfo.userId,
                        userInfo.userAvatar,
                        userInfo.userName,
                        userInfo.muteAudio
                    )
                )
            }

            override fun onUserAudioMute(userId: String, mute: Boolean) {
                super.onUserAudioMute(userId, mute)
                val userInfo = userService.getUserInfo(userId) ?: return
                onUserChange.invoke(
                    ShowSubscribeStatus.updated, ShowUser(
                        userInfo.userId,
                        userInfo.userName,
                        userInfo.userAvatar,
                        mute
                    )
                )
            }
        })
    }

    override fun muteAudio(
        roomId: String,
        mute: Boolean,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val scene = syncManager.getScene(roomId)
        scene?.userService?.muteUserAudio(mute) {
            if (it != null) {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
            } else {
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke()
                }
            }
        }
    }


    override fun sendChatMessage(
        roomId: String, message: String, success: (() -> Unit)?, error: ((Exception) -> Unit)?
    ) {
        val msg = ShowMessage(
            UserManager.getInstance().user.id.toString(),
            UserManager.getInstance().user.name,
            message
        )
        syncManager.getExMessageRetainer(
            roomId, "chatMessage"
        ).sendMessage(GsonTools.beanToString(msg) ?: "", "", success = {
            success?.invoke()
        }, error = {
            error?.invoke(RuntimeException(it))
        })
    }

    override fun subscribeMessage(
        roomId: String,
        onMessageChange: (ShowSubscribeStatus, ShowMessage) -> Unit
    ) {
        syncManager.getExMessageRetainer(
            roomId,
            "chatMessage"
        ).subscribe { msg ->
            onMessageChange.invoke(
                ShowSubscribeStatus.updated,
                GsonTools.toBean(msg.content, ShowMessage::class.java) ?: return@subscribe
            )
        }
    }


    override fun getInteractionInfo(
        roomId: String,
        success: ((ShowInteractionInfo?) -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExInteractionService(roomId)
            .getLatestInteractionInfo(
                success = {
                    ThreadManager.getInstance().runOnMainThread {
                        success?.invoke(it?.toShowInteraction())
                    }
                },
                failure = {
                    ThreadManager.getInstance().runOnMainThread {
                        error?.invoke(RuntimeException(it))
                    }
                }
            )
    }

    override fun subscribeInteractionChanged(
        roomId: String,
        onInteractionChanged: (ShowSubscribeStatus, ShowInteractionInfo?) -> Unit
    ) {
        syncManager.getExInteractionService(roomId)
            .subscribeInteractionEvent {
                onInteractionChanged.invoke(
                    ShowSubscribeStatus.updated,
                    it?.toShowInteraction()
                )
            }
    }

    override fun stopInteraction(
        roomId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExInteractionService(roomId)
            .stopInteraction(success) {
                error?.invoke(RuntimeException(it))
            }
    }


    override fun createMicSeatApply(
        roomId: String,
        success: ((ShowMicSeatApply) -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExApplyService(roomId)
            .addApply(
                UserManager.getInstance().user.id.toString(),
                success = {
                    success?.invoke(it.toShowApplyInfo())
                },
                failure = {
                    error?.invoke(RuntimeException(it))
                })
    }


    override fun getAllMicSeatApplyList(
        roomId: String,
        success: (List<ShowMicSeatApply>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExApplyService(roomId)
            .getApplyList(
                success = {
                    ThreadManager.getInstance().runOnMainThread {
                        success.invoke(it.map { it.toShowApplyInfo() })
                    }
                },
                failure = {
                    ThreadManager.getInstance().runOnMainThread {
                        error?.invoke(RuntimeException(it))
                    }
                })
    }

    override fun subscribeMicSeatApply(
        roomId: String,
        onMicSeatChange: (ShowSubscribeStatus, List<ShowMicSeatApply>) -> Unit
    ) {
        syncManager.getExApplyService(roomId)
            .subscribeApplyEvent(onUpdate = { list ->
                onMicSeatChange.invoke(
                    ShowSubscribeStatus.updated,
                    list.map { it.toShowApplyInfo() })
            })
    }


    override fun cancelMicSeatApply(
        roomId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExApplyService(roomId)
            .cancelApply(
                UserManager.getInstance().user.id.toString(),
                success = {
                    ThreadManager.getInstance().runOnMainThread {
                        success?.invoke()
                    }
                },
                failure = {
                    ThreadManager.getInstance().runOnMainThread {
                        error?.invoke(RuntimeException(it))
                    }
                })
    }

    override fun acceptMicSeatApply(
        roomId: String,
        userId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExApplyService(roomId)
            .acceptApply(
                userId,
                success = {
                    ThreadManager.getInstance().runOnMainThread {
                        success?.invoke()
                    }
                },
                failure = {
                    ThreadManager.getInstance().runOnMainThread {
                        error?.invoke(RuntimeException(it))
                    }
                })
    }

    override fun subscribeMicSeatInvitation(
        roomId: String,
        onMicSeatInvitationChange: (ShowSubscribeStatus, ShowMicSeatInvitation?) -> Unit
    ) {
        syncManager.getExInvitationService(roomId).subscribe {
            onMicSeatInvitationChange.invoke(
                ShowSubscribeStatus.updated,
                it.toShowInvitationInfo()
            )
        }
    }

    override fun createMicSeatInvitation(
        roomId: String,
        userId: String,
        success: ((ShowMicSeatInvitation) -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExInvitationService(roomId).sendInvitation(
            userId,
            success = {
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke(it.toShowInvitationInfo())
                }

            },
            failure = {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
            })
    }


    override fun acceptMicSeatInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExInvitationService(roomId).acceptInvitation(
            invitationId,
            success = {
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke()
                }
            },
            failure = {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
            })
    }

    override fun rejectMicSeatInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExInvitationService(roomId).rejectInvitation(
            invitationId,
            success = {
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke()
                }
            },
            failure = {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
            })
    }


    override fun getAllPKUserList(
        roomId: String,
        success: (List<ShowPKUser>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExRoomPresenceService().getAllRoomPresenceInfo(
            success = {
                val filterList = it.filter { info ->
                    info.ownerId != UserManager.getInstance().user.id.toString()
                }
                ThreadManager.getInstance().runOnMainThread {
                    success.invoke(filterList.map { info ->
                        info.toShowPKUserInfo()
                    })
                }
            },
            error = {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(it)
                }
            }
        )
    }

    override fun subscribePKInvitationChanged(
        roomId: String,
        onPKInvitationChanged: (ShowSubscribeStatus, ShowPKInvitation?) -> Unit
    ) {
        syncManager.getExPKService(roomId)
            .subscribe {
                onPKInvitationChanged.invoke(
                    ShowSubscribeStatus.updated,
                    it.toShowPKInfo()
                )
            }
    }

    override fun createPKInvitation(
        roomId: String,
        pkRoomId: String,
        success: ((ShowPKInvitation) -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExPKService(roomId).invitePK(
            pkRoomId,
            success = {
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke(it.toShowPKInfo())
                }
            },
            error = {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
            }
        )
    }

    override fun acceptPKInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExPKService(roomId).acceptPK(
            invitationId,
            success = {
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke()
                }
            },
            error = {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
            }
        )
    }

    override fun rejectPKInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        syncManager.getExPKService(roomId).rejectPK(
            invitationId,
            success = {
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke()
                }
            },
            error = {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
            }
        )
    }

    override fun subscribeReConnectEvent(roomId: String, onReconnect: () -> Unit) {
        syncManager.subscribeExConnectionState(roomId) { isConnected ->
            if (isConnected) {
                onReconnect.invoke()
            }
        }
    }

    private fun appendRobotRooms(roomList: List<ShowRoomDetailModel>): List<ShowRoomDetailModel> {


        val robotRoomIds = ArrayList(kRobotVideoRoomIds)
        val kRobotRoomStartId = kRobotVideoRoomIds[0]
        roomList.forEach { roomDetail ->
            val differValue = roomDetail.roomId.toInt() - kRobotRoomStartId
            if (differValue >= 0) {
                robotRoomIds.firstOrNull { robotRoomId -> robotRoomId == roomDetail.roomId.toInt() }
                    ?.let { id ->
                        robotRoomIds.remove(id)
                    }
            }
        }
        val retRoomList = mutableListOf<ShowRoomDetailModel>()
        for (i in 0 until robotRoomIds.size) {
            val robotRoomId = robotRoomIds[i]
            val robotId = robotRoomId % 10
            val roomInfo = ShowRoomDetailModel(
                robotRoomId.toString(), // roomId
                "Smooth $robotId", // roomName
                1,
                kRobotUid.toString(),
                kRobotAvatars[(robotId - 1) % kRobotAvatars.size],
                "Robot $robotId",
                TimeUtils.currentTimeMillis().toDouble(),
                TimeUtils.currentTimeMillis().toDouble()
            )
            retRoomList.add(roomInfo)
        }
        retRoomList.addAll(roomList)
        return retRoomList
    }


    override fun startCloudPlayer() {
        for (i in 0 until kRobotVideoRoomIds.size) {
            val roomId = kRobotVideoRoomIds[i]
            cloudPlayerService.startCloudPlayer(
                roomId.toString(),
                UserManager.getInstance().user.id.toString(),
                kRobotUid,
                kRobotVideoStreamUrls[i],
                "cn",
                success = {
                    cloudPlayerService.startHeartBeat(
                        roomId.toString(),
                        UserManager.getInstance().user.id.toString()
                    )
                },
                failure = { })
        }
    }
}

private fun AUIRoomInfo.toShowRoomDetailModel(): ShowRoomDetailModel {
    return ShowRoomDetailModel(
        roomId,
        roomName,
        customPayload["roomUserCount"] as? Int ?: 1,
        roomOwner?.userId ?: "",
        roomOwner?.userAvatar ?: "",
        roomOwner?.userName ?: "",
        createdAt = createTime?.toDouble() ?: TimeUtils.currentTimeMillis().toDouble(),
        updatedAt = createTime?.toDouble() ?: TimeUtils.currentTimeMillis().toDouble()
    )
}

private fun ShowRoomDetailModel.toAUIRoomInfo(): AUIRoomInfo {
    val roomInfo = AUIRoomInfo()
    roomInfo.roomId = roomId
    roomInfo.roomName = roomName
    val owner = AUIUserThumbnailInfo()
    owner.userId = ownerId
    owner.userName = ownerName
    owner.userAvatar = ownerAvatar
    roomInfo.roomOwner = owner
    roomInfo.customPayload["roomUserCount"] = roomUserCount
    roomInfo.createTime = createdAt.toLong()
    return roomInfo
}

private fun InteractionInfo.toShowInteraction(): ShowInteractionInfo {
    return ShowInteractionInfo(
        userId,
        userName,
        roomId,
        when (type) {
            InteractionType.LINKING -> ShowInteractionStatus.linking
            InteractionType.PK -> ShowInteractionStatus.pking
            else -> ShowInteractionStatus.idle
        },
        createdAt
    )
}

private fun ApplyInfo.toShowApplyInfo(): ShowMicSeatApply {
    return ShowMicSeatApply(
        userId,
        userAvatar,
        userName
    )
}

private fun InvitationInfo.toShowInvitationInfo(): ShowMicSeatInvitation {
    return ShowMicSeatInvitation(
        id,
        userId,
        userName,
        when (type) {
            InvitationType.ACCEPT -> ShowInvitationType.accept
            InvitationType.REJECT -> ShowInvitationType.reject
            else -> ShowInvitationType.invitation
        }
    )
}

private fun PKInfo.toShowPKInfo(): ShowPKInvitation {
    return ShowPKInvitation(
        id,
        userId,
        userName,
        roomId,
        fromUserId,
        fromUserName,
        fromRoomId,
        when (type) {
            PKType.ACCEPT -> ShowInvitationType.accept
            PKType.REJECT -> ShowInvitationType.reject
            PKType.END -> ShowInvitationType.end
            else -> ShowInvitationType.invitation
        }
    )
}

private fun RoomPresenceInfo.toShowPKUserInfo(): ShowPKUser {
    return ShowPKUser(
        ownerId,
        ownerName,
        roomId,
        ownerAvatar,
        when (status) {
            RoomPresenceStatus.INTERACTING_PK -> ShowInteractionStatus.pking
            RoomPresenceStatus.INTERACTING_LINKING -> ShowInteractionStatus.linking
            else -> ShowInteractionStatus.idle
        },
        isWaiting = false
    )
}