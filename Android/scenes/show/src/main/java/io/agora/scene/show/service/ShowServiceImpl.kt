package io.agora.scene.show.service

import android.content.Context
import android.util.Log
import io.agora.rtm.RtmConstants
import io.agora.rtmsyncmanager.ISceneResponse
import io.agora.rtmsyncmanager.RoomExpirationPolicy
import io.agora.rtmsyncmanager.RoomService
import io.agora.rtmsyncmanager.SyncManager
import io.agora.rtmsyncmanager.model.AUICommonConfig
import io.agora.rtmsyncmanager.model.AUIRoomContext
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.rtmsyncmanager.model.AUIUserThumbnailInfo
import io.agora.rtmsyncmanager.service.IAUIUserService.AUIUserRespObserver
import io.agora.rtmsyncmanager.service.callback.AUIException
import io.agora.rtmsyncmanager.service.callback.AUIRoomCallback
import io.agora.rtmsyncmanager.service.collection.AUIListCollection
import io.agora.rtmsyncmanager.service.collection.AUIMapCollection
import io.agora.rtmsyncmanager.service.http.HttpManager
import io.agora.rtmsyncmanager.service.room.AUIRoomManager
import io.agora.rtmsyncmanager.service.rtm.AUIRtmErrorRespObserver
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools
import io.agora.rtmsyncmanager.utils.ThreadManager
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.show.service.ShowServiceProtocol.Companion.ROOM_AVAILABLE_DURATION
import io.agora.scene.show.service.cloudplayer.CloudPlayerService
import io.agora.scene.show.service.rtmsync.CollectionProvider
import io.agora.scene.show.service.rtmsync.MessageRetainer
import io.agora.scene.show.service.rtmsync.MessageRetainerProvider
import io.agora.scene.show.service.rtmsync.RoomPresenceService


const val kRoomSceneId = "scene_show_5.0.0_2"
const val kRoomInteractionChannelName = "9999999999"
const val kRobotUid = 2000000001
val kRobotAvatars = listOf("https://download.shengwang.cn/demo/release/bot1.png")
val kRobotVideoRoomIds = arrayListOf(2023004, 2023005, 2023006)
val kRobotVideoStreamUrls = arrayListOf(
    "https://download.shengwang.cn/demo/test/agora_test_video_10.mp4",
    "https://download.shengwang.cn/demo/test/agora_test_video_11.mp4",
    "https://download.shengwang.cn/demo/test/agora_test_video_12.mp4"
)
const val kMessageTypeChat = 1 // 聊天
const val kMessageTypeLinking = 2 // 连麦
const val kMessageTypePK = 3 // PK
const val kCollectionKeyInteraction = "show_interaction_collection"
const val kCollectionKeyMicSeatApply = "show_mic_seat_apply_collection"

class ShowServiceImpl(context: Context) : ShowServiceProtocol {

    private val appId: String = BuildConfig.AGORA_APP_ID
    private val appCert: String = BuildConfig.AGORA_APP_CERTIFICATE

    private val syncManager: SyncManager
    private val roomManager: AUIRoomManager
    private val roomService: RoomService
    private val roomPresenceService: RoomPresenceService
    private val messageRetainerProvider: MessageRetainerProvider
    private val collectionProvider: CollectionProvider
    private val cloudPlayerService by lazy { CloudPlayerService() }

    init {
        HttpManager.setBaseURL(BuildConfig.ROOM_MANAGER_SERVER_HOST)
        AUILogger.initLogger(
            AUILogger.Config(
                context,
                "show",
                logCallback = object : AUILogger.AUILogCallback {
                    override fun onLogDebug(tag: String, message: String) {
                        Log.d(tag, message)
                    }

                    override fun onLogInfo(tag: String, message: String) {
                        Log.i(tag, message)
                    }

                    override fun onLogWarning(tag: String, message: String) {
                        Log.w(tag, message)
                    }

                    override fun onLogError(tag: String, message: String) {
                        Log.e(tag, message)
                    }
                })
        )

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
        syncManager = SyncManager(context, null, config)

        TokenGenerator.generateToken(
            "",
            owner.userId,
            TokenGenerator.TokenGeneratorType.token007,
            TokenGenerator.AgoraTokenType.rtm,
            success = {
                syncManager.login(it) { ex ->
                    if (ex != null) {
                        AUILogger.logger().e("login", "login failed: ${ex.message}")
                    }
                }
            },
            failure = {
                AUILogger.logger().e("login", "login failed: $it")
            }
        )

        roomManager = AUIRoomManager()
        roomService = RoomService(RoomExpirationPolicy().apply {
            expirationTime = ROOM_AVAILABLE_DURATION
            isAssociatedWithOwnerOffline = true
        }, roomManager, syncManager)


        roomPresenceService =
            RoomPresenceService(kRoomInteractionChannelName, syncManager.rtmManager)

        messageRetainerProvider = MessageRetainerProvider(syncManager.rtmManager)
        collectionProvider = CollectionProvider(syncManager)
    }

    override fun destroy() {
        syncManager.logout()
        syncManager.release()
    }

    override fun getRoomList(
        success: (List<ShowRoomDetailModel>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        roomService.getRoomList(
            appId,
            kRoomSceneId,
            0,
            50
        ) { ex, _, list ->
            if (ex != null) {
                error?.invoke(RuntimeException(ex))
            } else {
                success.invoke(appendRobotRooms(list?.map { it.toShowRoomDetailModel() }
                    ?: emptyList()))
            }
        }
    }

    override fun createRoom(
        roomId: String,
        roomName: String,
        success: (ShowRoomDetailModel) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        roomService.createRoom(
            appId,
            kRoomSceneId,
            ShowRoomDetailModel(
                roomId,
                roomName,
                1,
                UserManager.getInstance().user.id.toString(),
                UserManager.getInstance().user.headUrl,
                UserManager.getInstance().user.name,
            ).toAUIRoomInfo(),
            revert = true,
            autoEnter = false
        ) { ex, roomInfo ->
            if (ex == null && roomInfo != null) {
                roomPresenceService.setRoomPresenceInfo(
                    RoomPresenceService.RoomPresenceInfo(
                        roomId,
                        roomName,
                        UserManager.getInstance().user.id.toString(),
                        UserManager.getInstance().user.name,
                        UserManager.getInstance().user.headUrl,
                        ShowInteractionStatus.idle,
                        "", ""
                    )
                )
                success.invoke(roomInfo.toShowRoomDetailModel())
            } else {
                error?.invoke(RuntimeException(ex))
            }
        }
    }


    override fun joinRoom(
        roomId: String,
        success: () -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        if (roomId.isRobotRoom()) {
            success.invoke()
            return
        }
        roomService.enterRoom(
            appId,
            kRoomSceneId,
            roomId,
            revert = true
        ) { ex ->
            if (ex != null) {
                error?.invoke(RuntimeException(ex))
                return@enterRoom
            }
            if (roomService.isRoomOwner(roomId)) {
                dealInteractionEvent(roomId)
            }
            success.invoke()
        }
    }

    private fun dealInteractionEvent(roomId: String) {
        // 监听互动状态变化
        var interaction: ShowInteractionInfo? = null
        subscribeInteractionChanged(roomId) { _, value ->
            interaction = value
            if (value?.interactStatus == ShowInteractionStatus.linking) {
                // 更新room presence
                roomPresenceService.updateRoomPresenceInfo(
                    roomId,
                    ShowInteractionStatus.linking,
                    value.userId,
                    value.userName
                )
            } else if (value == null) {
                roomPresenceService.updateRoomPresenceInfo(
                    roomId,
                    ShowInteractionStatus.idle
                )
            }
        }

        // 监听presence用户变化，当互动用户退出时停止互动
        subscribeUser(roomId) { status, user ->
            val userId = user?.userId ?: return@subscribeUser
            interaction?.let {
                // 还在互动中，当互动用户退出时停止互动
                if (status == ShowSubscribeStatus.deleted) {
                    if (it.userId == userId && it.interactStatus == ShowInteractionStatus.linking) {
                        roomPresenceService.updateRoomPresenceInfo(
                            roomId,
                            ShowInteractionStatus.idle
                        )
                    }
                }
            }

            // 清空离线用户的连麦申请信息
            if (status == ShowSubscribeStatus.deleted) {
                cleanMicSeatApply(roomId, userId)
            }

            // 更新restful房间人数
            if (status == ShowSubscribeStatus.added) {
                updateRoomInfo(roomId, 1)
            } else if (status == ShowSubscribeStatus.deleted) {
                updateRoomInfo(roomId, -1)
            }
        }

        // 监听邀请申请
        subscribeMicSeatInvitation(roomId) { _, value ->
            if (interaction != null && value?.type == ShowInvitationType.accept) {
                startInteraction(
                    roomId, ShowInteractionInfo(
                        value.userId,
                        value.userName,
                        roomId,
                        ShowInteractionStatus.linking,
                        TimeUtils.currentTimeMillis().toDouble()
                    )
                )
            } else if (value?.type == ShowInvitationType.end) {
                stopInteraction(roomId)
            }
        }

        // 监听互动状态变化
        roomPresenceService.subscribe(
            onUpdate = { info: RoomPresenceService.RoomPresenceInfo ->
                if (info.roomId != roomId) {
                    val myRoomInteractionInfo =
                        roomPresenceService.getRoomPresenceInfo(roomId) ?: return@subscribe
                    if (info.interactionStatus == ShowInteractionStatus.pking && info.interactorId == UserManager.getInstance().user.id.toString()) {
                        if (myRoomInteractionInfo.interactionStatus == ShowInteractionStatus.pking) {
                            startInteraction(
                                roomId, ShowInteractionInfo(
                                    info.ownerId,
                                    info.ownerName,
                                    info.roomId,
                                    ShowInteractionStatus.pking,
                                    TimeUtils.currentTimeMillis().toDouble()
                                )
                            )
                        } else {
                            roomPresenceService.updateRoomPresenceInfo(
                                roomId,
                                interactionStatus = ShowInteractionStatus.pking,
                                interactorId = info.ownerId,
                                interactorName = info.ownerName,
                                success = {
                                    startInteraction(
                                        roomId, ShowInteractionInfo(
                                            info.ownerId,
                                            info.ownerName,
                                            info.roomId,
                                            ShowInteractionStatus.pking,
                                            TimeUtils.currentTimeMillis().toDouble()
                                        )
                                    )
                                }
                            )
                        }
                    } else if (info.interactionStatus == ShowInteractionStatus.idle
                        && myRoomInteractionInfo.interactionStatus == ShowInteractionStatus.pking
                    ) {
                        stopInteraction(roomId)
                    }
                }
            }
        )
    }

    override fun subscribeCurrRoomEvent(
        roomId: String,
        onUpdate: (status: ShowSubscribeStatus, roomInfo: ShowRoomDetailModel?) -> Unit
    ) {
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
        if (roomId.isRobotRoom()) {
            return
        }

        roomPresenceService.unsubscribe()
        messageRetainerProvider.clean(roomId)
        collectionProvider.clean(roomId)

        // 离开房间
        roomService.leaveRoom(
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
        roomManager.getRoomInfo(
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
                    roomManager.updateRoomInfo(
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
            val interactionInfo = roomPresenceService.getRoomPresenceInfo(roomId)

            // 获取等待中信息
            val micMessages =
                messageRetainerProvider.getMicSeatInvitationMessageRetainer(roomId).getMessages()

            ThreadManager.getInstance().runOnMainThread {
                success.invoke((list ?: emptyList()).map { user ->
                    ShowUser(
                        user.userId,
                        user.userAvatar,
                        user.userName,
                        user.muteAudio,
                        if (interactionInfo?.interactorId == user.userId) {
                            interactionInfo.interactionStatus
                        } else {
                            ShowInteractionStatus.idle
                        },
                        isWaiting = micMessages.any { it.userId == user.userId }
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
                        userInfo.userName,
                        userInfo.userAvatar,
                        userInfo.muteAudio
                    )
                )
            }

            override fun onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
                super.onRoomUserUpdate(roomId, userInfo)
                onUserChange.invoke(
                    ShowSubscribeStatus.updated, ShowUser(
                        userInfo.userId,
                        userInfo.userName,
                        userInfo.userAvatar,
                        userInfo.muteAudio
                    )
                )
            }

            override fun onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
                super.onRoomUserLeave(roomId, userInfo)
                onUserChange.invoke(
                    ShowSubscribeStatus.deleted, ShowUser(
                        userInfo.userId,
                        userInfo.userName,
                        userInfo.userAvatar,
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
        roomId: String,
        message: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val showMessage = ShowMessage(
            UserManager.getInstance().user.userNo,
            UserManager.getInstance().user.name,
            message,
            System.currentTimeMillis().toDouble()
        )
        messageRetainerProvider.getChatMessageRetainer(
            roomId
        ).sendMessage(
            showMessage,
            "",
            success,
            error
        )
    }

    override fun subscribeMessage(
        roomId: String,
        onMessageChange: (ShowSubscribeStatus, ShowMessage) -> Unit
    ) {
        messageRetainerProvider.getChatMessageRetainer(
            roomId
        ).subscribe(
            onAdd = {
                onMessageChange.invoke(
                    ShowSubscribeStatus.updated,
                    it
                )
            }
        )
    }


    override fun getInteractionInfo(
        roomId: String,
        success: ((ShowInteractionInfo?) -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        collectionProvider.getRoomInteractionCollection(roomId)
            ?.getMetaData { e, data ->
                if (e != null) {
                    error?.invoke(RuntimeException(e))
                    return@getMetaData
                }
                val map = (data as? Map<*, *>)
                val bean = GsonTools.toBean(
                    GsonTools.beanToString(map),
                    ShowInteractionInfo::class.java
                )
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke(bean)
                }
            }
    }

    private fun startInteraction(
        roomId: String,
        info: ShowInteractionInfo,
        success: (() -> Unit)? = null,
        error: ((Exception) -> Unit)? = null
    ) {
        Log.d("ShowInteraction", "startInteraction: $info")
        collectionProvider.getRoomInteractionCollection(roomId)
            ?.addMetaData(
                "startInteraction",
                GsonTools.beanToMap(info)
            ) {
                if (it != null) {
                    ThreadManager.getInstance().runOnMainThread {
                        error?.invoke(RuntimeException(it))
                    }
                    return@addMetaData
                }
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke()
                }
            }
    }

    override fun subscribeInteractionChanged(
        roomId: String,
        onInteractionChanged: (ShowSubscribeStatus, ShowInteractionInfo?) -> Unit
    ) {
        val collection = collectionProvider.getRoomInteractionCollection(roomId) ?: return
        collectionProvider.subscribeMultiAttributesDidChanged(
            collection,
            roomId,
            ShowInteractionInfo::class.java,
        ) { info ->
            if (info == null) {
                onInteractionChanged.invoke(ShowSubscribeStatus.deleted, null)
            } else {
                onInteractionChanged.invoke(ShowSubscribeStatus.updated, info)
            }
        }
    }

    override fun stopInteraction(
        roomId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val collection = collectionProvider.getRoomInteractionCollection(roomId)
        if (collection == null) {
            error?.invoke(RuntimeException("collection is null"))
            return
        }
        collection.cleanMetaData {
            Log.d("ShowInteraction", "stopInteraction: $it")
            if (it != null) {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
                return@cleanMetaData
            }
            ThreadManager.getInstance().runOnMainThread {
                success?.invoke()
            }
        }
    }


    override fun createMicSeatApply(
        roomId: String,
        success: ((ShowMicSeatApply) -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        // 创建连麦申请
        val apply = ShowMicSeatApply(
            userId = UserManager.getInstance().user.id.toString(),
            avatar = UserManager.getInstance().user.headUrl,
            userName = UserManager.getInstance().user.name,
            createAt = TimeUtils.currentTimeMillis().toDouble()
        )
        collectionProvider.getMicSeatApplyCollection(roomId)
            ?.addMetaData(
                "createMicSeatApply",
                GsonTools.beanToMap(apply),
                listOf(
                    mapOf(
                        "userId" to UserManager.getInstance().user.id.toString()
                    )
                )
            ) {
                if (it != null) {
                    ThreadManager.getInstance().runOnMainThread {
                        error?.invoke(RuntimeException(it))
                    }
                    return@addMetaData
                }
                ThreadManager.getInstance().runOnMainThread {
                    success?.invoke(apply)
                }
            }
    }


    override fun getAllMicSeatApplyList(
        roomId: String,
        success: (List<ShowMicSeatApply>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        collectionProvider.getMicSeatApplyCollection(roomId)
            ?.getMetaData { ex, value ->
                if (ex != null) {
                    ThreadManager.getInstance().runOnMainThread {
                        error?.invoke(RuntimeException(ex))
                    }
                    return@getMetaData
                }
                val list = (value as? List<*>) ?: emptyList<Any>()
                ThreadManager.getInstance().runOnMainThread {
                    val ret = list.map {
                        GsonTools.toBean(GsonTools.beanToString(it), ShowMicSeatApply::class.java)!!
                    }
                    Log.d("ShowMicSeatApply", "getAllMicSeatApplyList: $ret")
                    success.invoke(ret)
                }
            }
    }

    override fun subscribeMicSeatApply(
        roomId: String,
        onMicSeatChange: (ShowSubscribeStatus, List<ShowMicSeatApply>) -> Unit
    ) {
        val collection = collectionProvider.getMicSeatApplyCollection(roomId) ?: return
        collectionProvider.subscribeMultiAttributesDidChanged(
            collection,
            roomId,
            ShowMicSeatApply::class.java
        ) {
            onMicSeatChange.invoke(ShowSubscribeStatus.updated, it)
        }
    }


    override fun cancelMicSeatApply(
        roomId: String,
        userId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val collection = collectionProvider.getMicSeatApplyCollection(roomId)
        if (collection == null) {
            error?.invoke(RuntimeException("collection is null"))
            return
        }
        collection.removeMetaData(
            "cancelMicSeatApply",
            listOf(mapOf("userId" to userId))
        ) {
            if (it != null) {
                ThreadManager.getInstance().runOnMainThread {
                    error?.invoke(RuntimeException(it))
                }
                return@removeMetaData
            }
            ThreadManager.getInstance().runOnMainThread {
                success?.invoke()
            }
        }
    }

    override fun acceptMicSeatApply(
        roomId: String,
        userId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val roomInteractionInfo = roomPresenceService.getRoomPresenceInfo(roomId)
        if (roomInteractionInfo == null) {
            error?.invoke(RuntimeException("roomInteractionInfo is null"))
            return
        }
        if (roomInteractionInfo.interactionStatus != ShowInteractionStatus.idle) {
            error?.invoke(RuntimeException("roomInteractionInfo is not idle"))
            return
        }
        if (!roomService.isRoomOwner(roomId)) {
            error?.invoke(RuntimeException("only owner can accept mic seat apply"))
            return
        }
        val collection = collectionProvider.getMicSeatApplyCollection(roomId)
        if (collection == null) {
            error?.invoke(RuntimeException("collection is null"))
            return
        }
        val userInfo = syncManager.getScene(roomId)?.userService?.getUserInfo(userId)
        if (userInfo == null) {
            error?.invoke(RuntimeException("user not found"))
            return
        }
        startInteraction(
            roomId, ShowInteractionInfo(
                userId,
                userInfo.userName,
                roomId,
                ShowInteractionStatus.linking,
                TimeUtils.currentTimeMillis().toDouble()
            ), {
                collection.removeMetaData(
                    "acceptMicSeatApply",
                    listOf(mapOf("userId" to userId))
                ) {}
                success?.invoke()
            }, error
        )
    }

    private fun cleanMicSeatApply(
        roomId: String,
        userId: String
    ) {
        collectionProvider.getMicSeatApplyCollection(roomId)
            ?.removeMetaData(
                "cleanMicSeatApply",
                listOf(mapOf("userId" to userId))
            ) {}
    }

    override fun subscribeMicSeatInvitation(
        roomId: String,
        onMicSeatInvitationChange: (ShowSubscribeStatus, ShowMicSeatInvitation?) -> Unit
    ) {
        messageRetainerProvider.getMicSeatInvitationMessageRetainer(roomId)
            .subscribe(
                onAdd = {
                    onMicSeatInvitationChange.invoke(
                        ShowSubscribeStatus.added,
                        it
                    )
                },
                onUpdate = {
                    onMicSeatInvitationChange.invoke(
                        ShowSubscribeStatus.updated,
                        it
                    )
                }
            )
    }

    override fun createMicSeatInvitation(
        roomId: String,
        userId: String,
        success: ((ShowMicSeatInvitation) -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val invitation = ShowMicSeatInvitation(
            userId = UserManager.getInstance().user.id.toString(),
            userName = UserManager.getInstance().user.name,
        )
        messageRetainerProvider.getMicSeatInvitationMessageRetainer(roomId)
            .sendMessage(
                invitation,
                userId,
                success = {
                    success?.invoke(invitation)
                },
                error = {
                    error?.invoke(it)
                }
            )
    }


    override fun acceptMicSeatInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val messageRetainer = messageRetainerProvider.getMicSeatInvitationMessageRetainer(roomId)
        val invitation = messageRetainer.removeMessage { it.id == invitationId }
        if (invitation == null) {
            error?.invoke(RuntimeException("invitation not found"))
            return
        }

        val roomPresenceInfo = roomPresenceService.getRoomPresenceInfo(roomId)
        if (roomPresenceInfo?.interactionStatus != ShowInteractionStatus.idle) {
            error?.invoke(RuntimeException("room is not idle"))
            return
        }

        val resp = ShowMicSeatInvitation(
            id = invitationId,
            UserManager.getInstance().user.id.toString(),
            UserManager.getInstance().user.name,
            type = ShowInvitationType.accept
        )
        messageRetainer.sendMessage(
            resp,
            invitation.userId,
            success = {
                success?.invoke()
            },
            error = {
                error?.invoke(it)
            }
        )
    }

    override fun rejectMicSeatInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val messageRetainer = messageRetainerProvider.getMicSeatInvitationMessageRetainer(roomId)
        val invitation = messageRetainer.removeMessage { it.id == invitationId }
        if (invitation == null) {
            error?.invoke(RuntimeException("invitation not found"))
            return
        }
        val resp = ShowMicSeatInvitation(
            invitationId,
            UserManager.getInstance().user.id.toString(),
            UserManager.getInstance().user.name,
            type = ShowInvitationType.reject
        )
        messageRetainer.sendMessage(
            resp,
            invitation.userId,
            success = {
                success?.invoke()
            },
            error = {
                error?.invoke(it)
            }
        )
    }


    override fun getAllPKUserList(
        roomId: String,
        success: (List<ShowPKUser>) -> Unit,
        error: ((Exception) -> Unit)?
    ) {
        val messageRetainer = messageRetainerProvider.getPKInvitationMessageRetainer(roomId)
        roomPresenceService.getAllRoomPresenceInfo(
            success = {
                val filterList = it.filter { info ->
                    info.ownerId != UserManager.getInstance().user.id.toString()
                }
                ThreadManager.getInstance().runOnMainThread {
                    val pkMessage = messageRetainer.getMessages()
                    success.invoke(filterList.map { info ->
                        ShowPKUser(
                            info.ownerId,
                            info.ownerName,
                            info.roomId,
                            info.ownerAvatar,
                            info.interactionStatus,
                            isWaiting = pkMessage.any { message ->
                                message.fromUserId == info.ownerId
                            }
                        )
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
        val messageRetainer = messageRetainerProvider.getPKInvitationMessageRetainer(roomId)
        messageRetainer.subscribe(
            onAdd = {
                onPKInvitationChanged.invoke(
                    ShowSubscribeStatus.added,
                    it
                )
            },
            onUpdate = {
                onPKInvitationChanged.invoke(
                    ShowSubscribeStatus.updated,
                    it
                )
            }
        )
    }

    override fun createPKInvitation(
        roomId: String,
        pkRoomId: String,
        success: ((ShowPKInvitation) -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val pkInfo = roomPresenceService.getRoomPresenceInfo(pkRoomId)
        if (pkInfo == null) {
            error?.invoke(RuntimeException("pk info not found"))
            return
        }
        val invitation = ShowPKInvitation(
            userId = pkInfo.ownerId,
            userName = pkInfo.ownerName,
            roomId = pkRoomId,
            fromUserId = UserManager.getInstance().user.id.toString(),
            fromUserName = UserManager.getInstance().user.name,
            fromRoomId = roomId,
            createAt = TimeUtils.currentTimeMillis().toDouble()
        )
        messageRetainerProvider.getPKInvitationMessageRetainer(roomId).sendMessage(
            invitation,
            pkInfo.ownerId,
            success = {
                success?.invoke(invitation)
            },
            error = {
                error?.invoke(RuntimeException(it))
            }
        )
    }

    override fun acceptPKInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val messageRetainer = messageRetainerProvider.getPKInvitationMessageRetainer(roomId)
        val pkInvitation = messageRetainer.removeMessage { it.id == invitationId }
        if (pkInvitation == null) {
            error?.invoke(RuntimeException("invitation not found"))
            return
        }

        val fromRoomPresenceInfo = roomPresenceService.getRoomPresenceInfo(pkInvitation.fromRoomId)
        if(fromRoomPresenceInfo?.interactionStatus != ShowInteractionStatus.idle){
            error?.invoke(RuntimeException("from room is not idle"))
            return
        }

        roomPresenceService.updateRoomPresenceInfo(
            roomId,
            interactionStatus = ShowInteractionStatus.pking,
            interactorId = pkInvitation.fromUserId,
            interactorName = pkInvitation.fromUserName,
            success = {
                success?.invoke()
            },
            error = {
                error?.invoke(RuntimeException(it))
            }
        )
    }

    override fun rejectPKInvitation(
        roomId: String,
        invitationId: String,
        success: (() -> Unit)?,
        error: ((Exception) -> Unit)?
    ) {
        val messageRetainer = messageRetainerProvider.getPKInvitationMessageRetainer(roomId)
        val pkInvitation = messageRetainer.removeMessage { it.id == invitationId }
        if (pkInvitation == null) {
            error?.invoke(RuntimeException("invitation not found"))
            return
        }

        messageRetainer.sendMessage(
            ShowPKInvitation(
                id = invitationId,
                userId = pkInvitation.fromUserId,
                userName = pkInvitation.fromUserName,
                roomId = pkInvitation.fromRoomId,
                fromUserId = pkInvitation.userId,
                fromUserName = pkInvitation.userName,
                fromRoomId = pkInvitation.roomId,
                createAt = TimeUtils.currentTimeMillis().toDouble(),
                type = ShowInvitationType.reject
            ),
            pkInvitation.fromUserId,
            success = {
                success?.invoke()
            },
            error = {
                error?.invoke(RuntimeException(it))
            }
        )
    }

    override fun subscribeReConnectEvent(roomId: String, onReconnect: () -> Unit) {
        val rtmManager = syncManager.rtmManager
        rtmManager.subscribeError(object : AUIRtmErrorRespObserver {
            override fun onTokenPrivilegeWillExpire(channelName: String?) {

            }

            override fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {
                super.onConnectionStateChanged(channelName, state, reason)
                if (state == RtmConstants.RtmConnectionState.getValue(RtmConstants.RtmConnectionState.CONNECTED)) {
                    onReconnect.invoke()
                }
            }
        })
    }

    private fun appendRobotRooms(roomList: List<ShowRoomDetailModel>): List<ShowRoomDetailModel> {
        val retRoomList = mutableListOf<ShowRoomDetailModel>()
        retRoomList.addAll(roomList)

        val robotRoomIds = ArrayList(kRobotVideoRoomIds)
        val kRobotRoomStartId = kRobotVideoRoomIds[0]
        retRoomList.forEach { roomDetail ->
            val differValue = roomDetail.roomId.toInt() - kRobotRoomStartId
            if (differValue >= 0) {
                robotRoomIds.firstOrNull { robotRoomId -> robotRoomId == roomDetail.roomId.toInt() }
                    ?.let { id ->
                        robotRoomIds.remove(id)
                    }
            }

        }
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


private fun CollectionProvider.getRoomInteractionCollection(channelName: String): AUIMapCollection? {
    val scene = syncManager.getScene(channelName) ?: return null
    return scene.getMapCollection(kCollectionKeyInteraction)
}

private fun CollectionProvider.getMicSeatApplyCollection(channelName: String): AUIListCollection? {
    val scene = syncManager.getScene(channelName) ?: return null
    return scene.getListCollection(kCollectionKeyMicSeatApply)
}


private fun MessageRetainerProvider.getMicSeatInvitationMessageRetainer(channelName: String): MessageRetainer<ShowMicSeatInvitation> {
    return getMessageRetainer(
        channelName,
        kMessageTypeLinking,
        ShowMicSeatInvitation::class.java
    ) { cache, received ->
        cache.id == received.id
    }
}

private fun MessageRetainerProvider.getPKInvitationMessageRetainer(channelName: String): MessageRetainer<ShowPKInvitation> {
    return getMessageRetainer(
        channelName,
        kMessageTypePK,
        ShowPKInvitation::class.java
    ) { cache, received ->
        cache.id == received.id
    }
}

private fun MessageRetainerProvider.getChatMessageRetainer(channelName: String): MessageRetainer<ShowMessage> {
    return getMessageRetainer(
        channelName,
        kMessageTypeChat,
        ShowMessage::class.java
    ) { cache, received ->
        cache.userId == received.userId
    }
}