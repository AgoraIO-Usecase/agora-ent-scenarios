package io.agora.scene.voice.service

import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import io.agora.scene.voice.bean.GiftBean
import io.agora.scene.voice.general.net.VRToolboxServerHttpManager
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback
import io.agora.voice.buddy.tool.GsonTools
import io.agora.voice.buddy.tool.LogTools.logD
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.network.http.toolbox.VRCreateRoomResponse
import io.agora.voice.network.http.toolbox.VRGenerateTokenResponse
import io.agora.voice.network.tools.VRDefaultValueCallBack
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author create by zhangwei03
 */
class VoiceSyncManagerServiceImp(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : VoiceServiceProtocol {

    private val voiceSceneId = "scene_chatRoom"

    @Volatile
    private var syncUtilsInit = false

    private var mSceneReference: SceneReference? = null

    private val objIdOfRoomNo = mutableMapOf<String, String>() // objectId of room no
    private val roomMap = mutableMapOf<String, VoiceRoomModel>() // key: roomNo

    private val roomSubscribeListener = mutableListOf<Sync.EventListener>()

    override fun fetchRoomList(
        page: Int, type: Int, completion: (error: Int, result: List<VoiceRoomModel>) -> Unit
    ) {
        initScene {
            Sync.Instance().getScenes(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    val ret = mutableListOf<VoiceRoomModel>()
                    result?.forEach { iObj ->
                        iObj.toObject(VoiceRoomModel::class.java)?.let { voiceRoomModel ->
                            objIdOfRoomNo[voiceRoomModel.roomId] = iObj.id
                            ret.add(voiceRoomModel)
                            roomMap[voiceRoomModel.roomId] = voiceRoomModel
                            //按照创建时间顺序排序
                            ret.sortBy { it.createdAt }
                        }
                    }
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(VoiceServiceProtocol.ERR_OK, ret)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, emptyList())
                    }
                }
            })
        }
    }

    override fun createRoom(
        inputModel: VoiceCreateRoomModel,
        completion: (error: Int, result: VoiceRoomModel) -> Unit
    ) {
        // 1、根据用户输入信息创建房间信息
        val currentMilliseconds = SystemClock.elapsedRealtime()
        val voiceRoomModel = VoiceRoomModel().apply {
            roomId = currentMilliseconds.toString()
            channelId = currentMilliseconds.toString()
            soundEffect = inputModel.soundEffect
            isPrivate = inputModel.isPrivate
            roomName = inputModel.roomName
            createdAt = currentMilliseconds
            roomPassword = inputModel.password
            memberCount = 0
        }
        val owner = VoiceMemberModel().apply {
            rtcUid = VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()
            nickName = VoiceBuddyFactory.get().getVoiceBuddy().nickName()
            uid = VoiceBuddyFactory.get().getVoiceBuddy().userId()
            micIndex = 0
            portrait = VoiceBuddyFactory.get().getVoiceBuddy().headUrl()
        }
        // 2、置换token,获取im 配置，创建房间需要这里的数据
        requestToolboxService(
            channelName = inputModel.roomName,
            chatroomName = inputModel.roomName,
            completion = { error, chatroomId ->
                if (error != VoiceServiceProtocol.ERR_OK) {
                    completion.invoke(error, voiceRoomModel)
                    return@requestToolboxService
                }
                owner.chatUid = VoiceBuddyFactory.get().getVoiceBuddy().chatUid()
                voiceRoomModel.owner = owner
                voiceRoomModel.chatroomId = chatroomId
                // 3、创建房间
                initScene {
                    val scene = Scene()
                    scene.id = voiceRoomModel.channelId
                    scene.userId = owner.uid
                    scene.property = GsonTools.beanToMap(voiceRoomModel)
                    Sync.Instance().createScene(scene, object : Sync.Callback {
                        override fun onSuccess() {
                            roomMap[voiceRoomModel.roomId] = voiceRoomModel
                            completion.invoke(VoiceServiceProtocol.ERR_OK, voiceRoomModel)
                        }

                        override fun onFail(exception: SyncManagerException?) {
                            completion.invoke(VoiceServiceProtocol.ERR_FAILED, voiceRoomModel)
                        }
                    })
                }

            })
    }

    override fun joinRoom(
        roomId: String,
        password: String,
        needConvertConfig: Boolean,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        initScene {
            Sync.Instance().joinScene(roomId, object : Sync.JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    mSceneReference = sceneReference
                    val curRoomInfo = roomMap[roomId] ?: return
                    mSceneReference?.update("memberCount", curRoomInfo.memberCount + 1, object : Sync.DataItemCallback {
                        override fun onSuccess(result: IObject?) {
                            "syncManager update on onSuccess ${result?.id}".logE()
                        }

                        override fun onFail(exception: SyncManagerException?) {
                            "syncManager update onFail ${exception?.message}".logE()
                        }
                    })
                    innerSubscribeRoomChanged()
                    if (needConvertConfig) {
                        requestToolboxService(
                            channelName = curRoomInfo.channelId,
                            chatroomName = curRoomInfo.roomName,
                            completion = { error, chatroomId ->
                                completion.invoke(error, error == VoiceServiceProtocol.ERR_OK)
                            })
                    } else {
                        completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                }
            })
        }
    }

    override fun leaveRoom(roomId: String, isOwner: Boolean, completion: (error: Int, result: Boolean) -> Unit) {
        val cacheRoom = roomMap[roomId] ?: return
        // 取消所有订阅
        roomSubscribeListener.forEach {
            mSceneReference?.unsubscribe(it)
        }
        roomSubscribeListener.clear()
        if (TextUtils.equals(cacheRoom.owner?.uid, VoiceBuddyFactory.get().getVoiceBuddy().userId())) {
            // 移除房间
            mSceneReference?.delete(object : Sync.Callback {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(roomId, true)
                        completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnIOThread {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                    }
                }
            })
        } else {
            ThreadManager.getInstance().runOnIOThread {
                resetCacheInfo(roomId, false)
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }
        }
    }

    private fun resetCacheInfo(roomId: String, isRoomDestroyed: Boolean = false) {
        if (isRoomDestroyed) {
            roomMap.remove(roomId)
        }
        mSceneReference = null
    }

    override fun fetchRoomDetail(roomId: String, completion: (error: Int, result: VoiceRoomModel) -> Unit) {
    }

    override fun inviteUserToMic(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun refuseInviteToMic(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun forbidMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun unForbidMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun lockMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun unLockMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun kickOff(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun leaveMic(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun muteLocal(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun unMuteLocal(roomId: String, micIndex: Int, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun changeMic(
        roomId: String,
        userId: String,
        oldIndex: Int,
        newIndex: Int,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
    }

    override fun refuseInvite(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun agreeInvite(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun submitApply(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun cancelApply(roomId: String, userId: String, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun sendGift(roomId: String, giftInfo: GiftBean, completion: (error: Int, result: Boolean) -> Unit) {
    }

    override fun fetchGiftContribute(roomId: String, completion: (error: Int, result: VoiceRankUserModel) -> Unit) {
    }

    override fun fetchRoomMembers(roomId: String, completion: (error: Int, result: VoiceMemberModel) -> Unit) {
    }

    override fun modifyRoomInfo(
        roomId: String,
        key: String,
        value: String,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
    }


    private fun initScene(complete: () -> Unit) {
        if (syncUtilsInit) {
            complete.invoke()
            return
        }
        Sync.Instance().init(context,
            mapOf(
                Pair("appid", VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId()),
                Pair("defaultChannel", voiceSceneId)
            ),
            object : Sync.Callback {
                override fun onSuccess() {
                    syncUtilsInit = true
                    ThreadManager.getInstance().runOnMainThread {
                        "SyncManager init success".logD()
                        complete.invoke()
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnMainThread {
                        "SyncManager init error: ${exception?.message}".logE()
                        errorHandler?.invoke(exception)
                    }
                }
            }
        )
    }

    /**
     * toolbox service api 置换token, 获取im 配置
     * @param channelName
     */
    private fun requestToolboxService(
        channelName: String,
        chatroomName: String,
        completion: (error: Int, chatroomId: String) -> Unit,
    ) {
        val generateToken = AtomicBoolean(false)
        val createImRoom = AtomicBoolean(false)
        var chatRoomId = ""
        VRToolboxServerHttpManager.get().generateToken(
            channelName,
            VoiceBuddyFactory.get().getVoiceBuddy().userId(),
            callBack = object : VRDefaultValueCallBack<VRGenerateTokenResponse> {
                override fun onSuccess(response: VRGenerateTokenResponse?) {
                    response?.let {
                        generateToken.set(true)
                        VoiceBuddyFactory.get().getVoiceBuddy().setupRtcToken(it.token)
                        if (generateToken.get() && createImRoom.get()) {
                            completion.invoke(VoiceServiceProtocol.ERR_OK, chatRoomId)
                        }
                    }
                }

                override fun onError(var1: Int, var2: String?) {
                    "SyncToolboxService generate token error code:$var1,msg:$var2".logE()
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, chatRoomId)
                }
            })
        VRToolboxServerHttpManager.get().createImRoom(
            chatroomName = chatroomName,
            chatroomOwner = VoiceBuddyFactory.get().getVoiceBuddy().userId(),
            traceId = UUID.randomUUID().toString(),
            username = VoiceBuddyFactory.get().getVoiceBuddy().userId(),
            nickname = VoiceBuddyFactory.get().getVoiceBuddy().nickName(),
            callBack = object : VRDefaultValueCallBack<VRCreateRoomResponse> {
                override fun onSuccess(response: VRCreateRoomResponse?) {
                    response?.let {
                        createImRoom.set(true)
                        VoiceBuddyFactory.get().getVoiceBuddy().setupChatConfig(response.userName, response.token)
                        chatRoomId = response.chatId
                        if (generateToken.get() && createImRoom.get()) {
                            completion.invoke(VoiceServiceProtocol.ERR_OK, chatRoomId)
                        }
                    }
                }

                override fun onError(var1: Int, var2: String?) {
                    "SyncToolboxService create room error code:$var1,msg:$var2".logE()
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, chatRoomId)
                }
            })
    }

    /**订阅房间变化*/
    private fun innerSubscribeRoomChanged() {
        val listener = object : Sync.EventListener {
            override fun onCreated(item: IObject?) {

            }

            override fun onUpdated(item: IObject?) {
                item ?: return
                val roomInfo = item.toObject(VoiceRoomModel::class.java)
                roomMap[roomInfo.roomId] = roomInfo
            }

            override fun onDeleted(item: IObject?) {
                item ?: return
                val roomInfo = roomMap[item.id] ?: return
                resetCacheInfo(roomInfo.roomId, true)
            }

            override fun onSubscribeError(ex: SyncManagerException?) {

            }

        }
        mSceneReference?.subscribe(listener)
        roomSubscribeListener.add(listener)
    }
}