package io.agora.scene.voice.service

import android.content.Context
import android.os.SystemClock
import io.agora.scene.voice.BuildConfig
import io.agora.scene.voice.general.net.VRToolboxServerHttpManager
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback
import io.agora.voice.buddy.tool.LogTools.logD
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.network.http.toolbox.VRCreateRoomResponse
import io.agora.voice.network.http.toolbox.VRGenerateTokenResponse
import io.agora.voice.network.tools.VRDefaultValueCallBack
import java.util.*
import java.util.concurrent.atomic.AtomicReference

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
                            ThreadManager.getInstance().runOnMainThread {
                                completion.invoke(VoiceServiceProtocol.ERR_OK, ret)
                            }
                        }
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
        val roomInfoDetail = VoiceRoomModel().apply {
            roomId = currentMilliseconds.toString()
            channelId = currentMilliseconds.toString()
            soundEffect = inputModel.soundEffect
            isPrivate = inputModel.isPrivate
            roomName = inputModel.roomName
            createdAt = currentMilliseconds
            roomPassword = inputModel.password
            clickCount = 1 // 默认房主已经观看
        }
        val owner = VoiceMemberModel().apply {
            rtcUid = VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()
            name = VoiceBuddyFactory.get().getVoiceBuddy().userName()
            uid = VoiceBuddyFactory.get().getVoiceBuddy().userId()
            micIndex = 0
            portrait = VoiceBuddyFactory.get().getVoiceBuddy().headUrl()
        }
        // 2、置换token,获取im 配置
        requestToolboxService(
            channelName = inputModel.roomName,
            chatroomName = inputModel.roomName,
            completion = { voiceToolboxBean ->
                VoiceBuddyFactory.get().getVoiceBuddy()
                    .setupChatConfig(
                        voiceToolboxBean.userName,
                        voiceToolboxBean.chatToken,
                        voiceToolboxBean.rtcToken
                    )
                owner.chatUid = voiceToolboxBean.userName
                roomInfoDetail.owner = owner
                roomInfoDetail.chatroomId = voiceToolboxBean.chatRoomId
                // 3、创建场景
                initScene {
                    val scene = Scene()
                    scene.id = roomInfoDetail.channelId
                    scene.userId = owner.uid
                    Sync.Instance().createScene(scene, object : Sync.Callback {
                        override fun onSuccess() {
                            completion.invoke(VoiceServiceProtocol.ERR_OK, roomInfoDetail)
                        }

                        override fun onFail(exception: SyncManagerException?) {
                            completion.invoke(VoiceServiceProtocol.ERR_FAILED, roomInfoDetail)
                        }
                    })
                }

            },
            failure = {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, roomInfoDetail)
            })
    }

    override fun <T> joinRoom(roomId: String, completion: (error: Int, result: T) -> Unit) {
        initScene {
            Sync.Instance().joinScene(roomId, object : Sync.JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    mSceneReference = sceneReference
                }

                override fun onFail(exception: SyncManagerException?) {

                }
            })
        }
    }

    override fun <T> leaveRoom(roomId: String, isOwner: Boolean, completion: (error: Int, result: T) -> Unit) {

    }

    override fun fetchRoomDetail(roomId: String, completion: (error: Int, result: VoiceRoomModel) -> Unit) {

    }


    private fun initScene(complete: () -> Unit) {
        if (syncUtilsInit) {
            complete.invoke()
            return
        }
        Sync.Instance().init(context, mapOf(
            Pair("appid", VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId()),
            Pair("defaultChannel", voiceSceneId),
        ), object : Sync.Callback {
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
     */
    private fun requestToolboxService(
        channelName: String,
        chatroomName: String,
        completion: (voiceToolboxBean: VoiceToolboxBean) -> Unit,
        failure: (error: Int) -> Unit
    ) {
        val generateToken = AtomicReference<VRGenerateTokenResponse>(null)
        val createImRoom = AtomicReference<VRCreateRoomResponse>(null)
        val voiceToolboxBean = VoiceToolboxBean()
        VRToolboxServerHttpManager.get(context).generateToken(
            channelName,
            VoiceBuddyFactory.get().getVoiceBuddy().userId(),
            callBack = object : VRDefaultValueCallBack<VRGenerateTokenResponse> {
                override fun onSuccess(response: VRGenerateTokenResponse?) {
                    if (response?.isSuccess() == true) {
                        generateToken.set(response)
                        voiceToolboxBean.rtcToken = response.token
                    }
                    if (generateToken.get() != null && createImRoom.get() != null) {
                        completion.invoke(voiceToolboxBean)
                    }
                }

                override fun onError(var1: Int, var2: String?) {
                    "SyncToolboxService generate token error code:$var1,msg:$var2".logE()
                    failure.invoke(var1)
                }
            })
        VRToolboxServerHttpManager.get(context).createImRoom(
            chatroomName,
            "welcome",
            VoiceBuddyFactory.get().getVoiceBuddy().userId(),
            if (BuildConfig.voice_env_is_test) "test" else "release",
            UUID.randomUUID().toString(),
            VoiceBuddyFactory.get().getVoiceBuddy().userId(),
            "12345678",
            VoiceBuddyFactory.get().getVoiceBuddy().userName(),
            callBack = object : VRDefaultValueCallBack<VRCreateRoomResponse> {
                override fun onSuccess(response: VRCreateRoomResponse?) {
                    if (response?.isSuccess() == true) {
                        createImRoom.set(response)
                        voiceToolboxBean.chatRoomId = response.chatId
                        voiceToolboxBean.chatToken = response.token
                        voiceToolboxBean.userName = response.userName
                        voiceToolboxBean.chatUUid = response.uid
                    }
                    if (generateToken.get() != null && createImRoom.get() != null) {
                        completion.invoke(voiceToolboxBean)
                    }
                }

                override fun onError(var1: Int, var2: String?) {
                    "SyncToolboxService create room error code:$var1,msg:$var2".logE()
                    failure.invoke(var1)
                }
            })
    }
}