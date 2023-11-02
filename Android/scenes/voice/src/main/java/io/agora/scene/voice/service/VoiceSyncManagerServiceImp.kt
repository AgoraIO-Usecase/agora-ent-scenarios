package io.agora.scene.voice.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import io.agora.CallBack
import io.agora.ValueCallBack
import io.agora.chat.ChatRoom
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.*
import io.agora.scene.voice.netkit.VoiceToolboxServerHttpManager
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.syncmanager.rtm.*
import io.agora.syncmanager.rtm.Sync.DataListCallback
import io.agora.syncmanager.rtm.Sync.JoinSceneCallback
import io.agora.voice.common.utils.GsonTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.ThreadManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author create by zhangwei03
 */
class VoiceSyncManagerServiceImp(
    private val context: Context,
    private val errorHandler: ((Exception?) -> Unit)?
) : VoiceServiceProtocol {

    private val TAG = "VC_SYNC_LOG"

    private val voiceSceneId = "scene_chatRoom_4.0.0"
    private val kRoomBGMCollection = "room_bgm"
    private var kRoomBGMId: String? = null

    private val roomChecker = RoomChecker(context)

    @Volatile
    private var syncUtilsInit = false

    private var mSceneReference: SceneReference? = null

    private val roomMap = mutableMapOf<String, VoiceRoomModel>() // key: roomNo
    private val objIdOfRoomNo = mutableMapOf<String, String>() // objectId of room no

    private val roomSubscribeListener = mutableListOf<Sync.EventListener>()

    private val roomServiceSubscribeDelegates = mutableListOf<VoiceRoomSubscribeDelegate>()

    // time limit
    private var roomTimeUpSubscriber: (() -> Unit)? = null
    private val ROOM_AVAILABLE_DURATION : Int = 20 * 60 * 1000 // 20min
    private val timerRoomEndRun = Runnable {
        ThreadManager.getInstance().runOnMainThread {
            roomTimeUpSubscriber?.invoke()
        }
    }

    /**
     * 注册订阅
     * @param delegate 聊天室内IM回调处理
     */
    override fun subscribeEvent(delegate: VoiceRoomSubscribeDelegate) {
        roomServiceSubscribeDelegates.add(delegate)
    }

    /**
     *  取消订阅
     */
    override fun unsubscribeEvent() {
        roomServiceSubscribeDelegates.clear()
    }

    override fun getSubscribeDelegates():MutableList<VoiceRoomSubscribeDelegate>{
        return roomServiceSubscribeDelegates
    }

    override fun reset() {
        if(syncUtilsInit){
            Sync.Instance().destroy()
            syncUtilsInit = false
        }
    }

    /**
     * 获取房间列表
     * @param page 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
     */
    override fun fetchRoomList(page: Int, completion: (error: Int, result: List<VoiceRoomModel>) -> Unit) {
        initScene {
            Sync.Instance().getScenes(object : DataListCallback {
                override fun onSuccess(result: MutableList<IObject>?) {
                    val ret = mutableListOf<VoiceRoomModel>()
                    result?.forEach { iObj ->
                        try {
                            val voiceRoom = iObj.toObject(VoiceRoomModel::class.java)
                            ret.add(voiceRoom)
                            roomMap[voiceRoom.roomId] = voiceRoom
                            objIdOfRoomNo[voiceRoom.roomId] = iObj.id
                        } catch (e: Exception) {
                            "voice room list get scene error: ${e.message}".logE()
                        }

                    }
                    //按照创建时间顺序排序
                    val comparator: Comparator<VoiceRoomModel> = Comparator { o1, o2 ->
                        o2.createdAt.compareTo(o1.createdAt)
                    }
                    ret.sortWith(comparator)
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(VoiceServiceProtocol.ERR_OK, ret)
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    val e = exception ?: return
                    val ret = mutableListOf<VoiceRoomModel>()
                    if (e.code == -VoiceServiceProtocol.ERR_ROOM_LIST_EMPTY) {
                        ThreadManager.getInstance().runOnMainThread {
                            completion.invoke(VoiceServiceProtocol.ERR_OK, ret)
                        }
                        return
                    }
                    ThreadManager.getInstance().runOnMainThread {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, emptyList())
                    }
                }
            })
        }
    }

    /**
     * 创建房间
     * @param inputModel 输入的房间信息
     */
    override fun createRoom(
        inputModel: VoiceCreateRoomModel, completion: (error: Int, result: VoiceRoomModel) -> Unit
    ) {
        // 1、根据用户输入信息创建房间信息
        val currentMilliseconds = System.currentTimeMillis()
        val voiceRoomModel = VoiceRoomModel().apply {
            roomId = currentMilliseconds.toString()
            channelId = currentMilliseconds.toString()
            soundEffect = inputModel.soundEffect
            isPrivate = inputModel.isPrivate
            roomName = inputModel.roomName
            createdAt = currentMilliseconds
            roomPassword = inputModel.password
            memberCount = 2 // 两个机器人
            clickCount = 2 // 两个机器人
        }
        val owner = VoiceMemberModel().apply {
            rtcUid = VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()
            chatUid = VoiceBuddyFactory.get().getVoiceBuddy().chatUserName()
            nickName = VoiceBuddyFactory.get().getVoiceBuddy().nickName()
            userId = VoiceBuddyFactory.get().getVoiceBuddy().userId()
            micIndex = 0
            portrait = VoiceBuddyFactory.get().getVoiceBuddy().headUrl()
        }
        voiceRoomModel.owner = owner
        // 2、置换token,获取im 配置，创建房间需要这里的数据
        VoiceToolboxServerHttpManager.get().requestToolboxService(
            channelId = voiceRoomModel.channelId,
            chatroomId = "",
            chatroomName = inputModel.roomName,
            chatOwner = VoiceBuddyFactory.get().getVoiceBuddy().chatUserName(),
            completion = { error, chatroomId ->
                if (error != VoiceServiceProtocol.ERR_OK) {
                    completion.invoke(error, voiceRoomModel)
                    return@requestToolboxService
                }
                voiceRoomModel.chatroomId = chatroomId
                // 3、创建房间
                initScene {
                    val scene = Scene()
                    scene.id = voiceRoomModel.roomId
                    scene.userId = owner.userId
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

    /**
     * 加入房间
     * @param roomId 房间id
     */
    override fun joinRoom(roomId: String, completion: (error: Int, result: VoiceRoomModel?) -> Unit) {
        initScene {
            val isRoomOwner = roomMap[roomId]?.owner?.userId == VoiceBuddyFactory.get().getVoiceBuddy().userId()
            Sync.Instance().joinScene(isRoomOwner, true, roomId, object : JoinSceneCallback {
                override fun onSuccess(sceneReference: SceneReference?) {
                    "syncManager joinScene onSuccess ${sceneReference?.id}".logD()
                    mSceneReference = sceneReference
                    if (roomMap[roomId] == null){
                        completion.invoke(VoiceServiceProtocol.ERR_ROOM_UNAVAILABLE, null)
                        " room is not existent ".logE()
                        return
                    }
                    val curRoomInfo = roomMap[roomId]?: return

                    if (roomChecker.joinRoom(roomId)) {
                        curRoomInfo.memberCount = curRoomInfo.memberCount + 1
                    }

                    curRoomInfo.clickCount = curRoomInfo.clickCount + 1
                    " joinRoom memberCount $curRoomInfo".logD()
                    val updateMap: HashMap<String, Any> = HashMap<String, Any>().apply {
                        putAll(GsonTools.beanToMap(curRoomInfo))
                    }
                    mSceneReference?.update(updateMap, object : Sync.DataItemCallback {
                        override fun onSuccess(result: IObject?) {
                            "syncManager update onSuccess ${result?.id}".logD()
                        }

                        override fun onFail(exception: SyncManagerException?) {
                            "syncManager update onFail ${exception?.message}".logE()
                        }
                    })
                    mSceneReference?.collection(kRoomBGMCollection)?.get(object: DataListCallback {
                        override fun onSuccess(result: MutableList<IObject>?) {
                            val item = result?.firstOrNull() ?: return
                            val bgmInfo = item.toObject(VoiceBgmModel::class.java) ?: return
                            kRoomBGMId = item.id
                            Log.d(TAG, "kRoomBGMCollection get: $bgmInfo")
                            GlobalScope.launch {
                                delay(3500)
                                remoteUpdateBGMInfo(bgmInfo)
                            }
                        }
                        override fun onFail(exception: SyncManagerException?) {
                        }
                    })
                    mSceneReference?.collection(kRoomBGMCollection)?.subscribe(object : Sync.EventListener {
                        override fun onUpdated(item: IObject?) {
                            Log.d(TAG, "kRoomBGMCollection updated callback: $item")
                            val bgmInfo = item?.toObject(VoiceBgmModel::class.java) ?: return
                            remoteUpdateBGMInfo(bgmInfo)
                        }
                        override fun onCreated(item: IObject?) {
                            kRoomBGMId = item?.id
                            Log.d(TAG, "kRoomBGMCollection created callback: $item")
                        }
                        override fun onDeleted(item: IObject?) {}
                        override fun onSubscribeError(ex: SyncManagerException?) {}
                    })
                    completion.invoke(VoiceServiceProtocol.ERR_OK, curRoomInfo)

                    if (TextUtils.equals(curRoomInfo.owner?.userId, VoiceBuddyFactory.get().getVoiceBuddy().userId())) {
                        ThreadManager.getInstance().runOnMainThreadDelay(timerRoomEndRun, ROOM_AVAILABLE_DURATION)
                    } else {
                        ThreadManager.getInstance().runOnMainThreadDelay(timerRoomEndRun, ROOM_AVAILABLE_DURATION - (System.currentTimeMillis() - curRoomInfo.createdAt).toInt())
                    }
                }

                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                    "syncManager joinScene onFail ${exception.toString()}".logD()
                }
            })
        }
    }
    private fun remoteUpdateBGMInfo(info: VoiceBgmModel) {
        val song = info.songName
        val singer = info.singerName
        val isOrigin = info.isOrigin
        AgoraRtcEngineController.get().bgmManager().remoteUpdateBGMInfo(song, singer, isOrigin)
    }
    /**
     * 离开房间
     * @param roomId 房间id
     */
    override fun leaveRoom(roomId: String, isRoomOwnerLeave: Boolean, completion: (error: Int, result: Boolean) -> Unit) {
        val cacheRoom = roomMap[roomId] ?: return
        roomChecker.leaveRoom(roomId)
        // 取消所有订阅
        roomSubscribeListener.forEach {
            mSceneReference?.unsubscribe(it)
        }
        roomSubscribeListener.clear()
        ThreadManager.getInstance().removeCallbacks(timerRoomEndRun)
        roomTimeUpSubscriber = null
        if (TextUtils.equals(cacheRoom.owner?.userId, VoiceBuddyFactory.get().getVoiceBuddy().userId())) {
            // 移除房间
            mSceneReference?.delete(object : Sync.Callback {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(roomId, true)
                        completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                    }
                    "syncManager delete onSuccess".logD()
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnIOThread {
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                    }
                    "syncManager delete onFail：${exception.toString()}".logE()
                }
            })
        } else {
            if (isRoomOwnerLeave) return
            val curRoomInfo = roomMap[roomId] ?: return
            curRoomInfo.memberCount = curRoomInfo.memberCount - 1
            val updateMap: HashMap<String, Any> = HashMap<String, Any>().apply {
                putAll(GsonTools.beanToMap(curRoomInfo))
            }
            " leaveRoom memberCount $curRoomInfo".logD()
            mSceneReference?.update(updateMap, object : Sync.DataItemCallback {

                override fun onSuccess(result: IObject?) {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(roomId, false)
                        completion.invoke(VoiceServiceProtocol.ERR_OK, true)
                    }
                    "syncManager update onSuccess".logD()
                }

                override fun onFail(exception: SyncManagerException?) {
                    ThreadManager.getInstance().runOnIOThread {
                        resetCacheInfo(roomId, false)
                        completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
                    }
                    "syncManager update onFail：${exception.toString()}".logE()
                }
            })
        }
    }

    /**
     * 获取房间详情
     * @param voiceRoomModel 房间概要
     */
    override fun fetchRoomDetail(
        voiceRoomModel: VoiceRoomModel,
        completion: (error: Int, result: VoiceRoomInfo?) -> Unit
    ) {
        ChatroomIMManager.getInstance().fetchRoomDetail(voiceRoomModel, object : ValueCallBack<VoiceRoomInfo> {
            override fun onSuccess(value: VoiceRoomInfo?) {
                if (value != null) {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, value)
                } else {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,null)
            }
        })
    }

    /**
     * 获取排行榜列表
     */
    override fun fetchGiftContribute(completion: (error: Int, result: List<VoiceRankUserModel>?) -> Unit) {
        ChatroomIMManager.getInstance().fetchGiftContribute(object :
            ValueCallBack<MutableList<VoiceRankUserModel>>{
            override fun onSuccess(value: MutableList<VoiceRankUserModel>) {
                ThreadManager.getInstance().runOnMainThread{
                    completion.invoke(VoiceServiceProtocol.ERR_OK,value)
                }
            }

            override fun onError(error: Int, errorMsg: String?) {
                ThreadManager.getInstance().runOnMainThread{
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED,null)
                }
            }
        })
    }

    /**
     * 获取邀请列表（过滤已在麦位成员）
     */
    override fun fetchRoomInvitedMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
        val  memberList = ChatroomIMManager.getInstance().fetchRoomInviteMembers()
        if (memberList != null ){
            completion.invoke(VoiceServiceProtocol.ERR_OK,memberList)
        }else{
            completion.invoke(VoiceServiceProtocol.ERR_FAILED,mutableListOf())
        }
    }

    /**
     * 获取房间成员列表
     */
    override fun fetchRoomMembers(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
        val  memberList = ChatroomIMManager.getInstance().fetchRoomMembers()
        if (memberList != null ){
            completion.invoke(VoiceServiceProtocol.ERR_OK,memberList)
        }else{
            completion.invoke(VoiceServiceProtocol.ERR_FAILED,mutableListOf())
        }
    }

    override fun kickMemberOutOfRoom(chatUidList: MutableList<String>, completion: (error: Int, result: Boolean) -> Unit) {
        //房主踢用户(踢出房间)
        ChatroomIMManager.getInstance().removeMemberToRoom(chatUidList,object :
            ValueCallBack<ChatRoom>{
            override fun onSuccess(value: ChatRoom?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK,true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,false)
            }
        })
    }

    /**
     * 更新用户列表
     */
    override fun updateRoomMembers(completion: (error: Int, result: Boolean) -> Unit){
        ChatroomIMManager.getInstance().updateRoomMembers(object : CallBack{
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK,true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,false)
            }
        })
    }

    /**
     * 申请列表
     */
    override fun fetchApplicantsList(completion: (error: Int, result: List<VoiceMemberModel>) -> Unit) {
       val raisedList = ChatroomIMManager.getInstance().fetchRaisedList()
        if (raisedList != null){
            completion.invoke(VoiceServiceProtocol.ERR_OK,raisedList)
        }else{
            completion.invoke(VoiceServiceProtocol.ERR_FAILED, mutableListOf())
        }
    }

    /**
     * 申请上麦
     * @param micIndex 麦位index
     */
    override fun startMicSeatApply(micIndex: Int?, completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().startMicSeatApply(micIndex ?: -1, object : CallBack {
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * 同意申请
     * @param chatUid 环信用户id
     */
    override fun acceptMicSeatApply(micIndex: Int?, chatUid: String, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().acceptMicSeatApply(micIndex ?: -1,chatUid,object :
            ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK,value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,null)
            }
        })
    }

    /**
     * 取消上麦
     * @param chatUid im uid
     */
    override fun cancelMicSeatApply(chatroomId: String, chatUid: String, completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().cancelMicSeatApply(chatroomId, chatUid, object : CallBack{
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK,true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,false)
            }
        })
    }

    /**
     * 邀请用户上麦
     * @param chatUid im uid
     */
    override fun startMicSeatInvitation(
        chatUid: String,
        micIndex: Int?,
        completion: (error: Int, result: Boolean) -> Unit
    ) {
        ChatroomIMManager.getInstance().invitationMic(chatUid,micIndex?:-1,object : CallBack{
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK,true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,false)
            }
        })
    }

    /**
     * 接受邀请
     */
    override fun acceptMicSeatInvitation(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().acceptMicSeatInvitation(micIndex, object :
            ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK,value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,null)
            }
        })
    }

    /**
     * 拒绝邀请
     */
    override fun refuseInvite(completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().refuseInvite(VoiceBuddyFactory.get().getVoiceBuddy().chatUserName(), object : CallBack {
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK,true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK,false)
            }
        })
    }

    /**
     * mute
     * @param micIndex 麦位index
     */
    override fun muteLocal(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().muteLocal(micIndex,object :
            ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK,value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,null)
            }
        })
    }

    /**
     * unMute
     * @param micIndex 麦位index
     */
    override fun unMuteLocal(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().unMuteLocal(micIndex,object :
            ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK,value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED,null)
            }
        })
    }

    /**
     * 禁言指定麦位置
     * @param micIndex 麦位index
     */
    override fun forbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().forbidMic(micIndex,object :
            ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * 取消禁言指定麦位置
     * @param micIndex 麦位index
     */
    override fun unForbidMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().unForbidMic(micIndex,object :
            ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * 锁麦
     * @param micIndex 麦位index
     */
    override fun lockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().lockMic(micIndex, object :ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel) {
                ThreadManager.getInstance().runOnIOThread {
                    completion.invoke(VoiceServiceProtocol.ERR_OK, value)
                }
            }

            override fun onError(error: Int, errorMsg: String?) {
                ThreadManager.getInstance().runOnIOThread {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
                }
            }

        })
    }

    /**
     * 取消锁麦
     * @param micIndex 麦位index
     */
    override fun unLockMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().unLockMic(micIndex,object :
            ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * 踢用户下麦
     * @param micIndex 麦位index
     */
    override fun kickOff(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().kickOff(micIndex,object : ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * 下麦
     * @param micIndex 麦位index
     */
    override fun leaveMic(micIndex: Int, completion: (error: Int, result: VoiceMicInfoModel?) -> Unit) {
        ChatroomIMManager.getInstance().leaveMic(micIndex,object : ValueCallBack<VoiceMicInfoModel>{
            override fun onSuccess(value: VoiceMicInfoModel) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * 换麦
     * @param oldIndex 老麦位index
     * @param newIndex 新麦位index
     */
    override fun changeMic(
        oldIndex: Int,
        newIndex: Int,
        completion: (error: Int, result: Map<Int, VoiceMicInfoModel>?) -> Unit
    ) {
        ChatroomIMManager.getInstance().changeMic(oldIndex,newIndex,object :
            ValueCallBack<MutableMap<Int, VoiceMicInfoModel>>{
            override fun onSuccess(value: MutableMap<Int, VoiceMicInfoModel>?) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, null)
            }
        })
    }

    /**
     * 更新公告
     * @param content 公告内容
     */
    override fun updateAnnouncement(content: String, completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().updateAnnouncement(content,object : CallBack{
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    override fun updateBGMInfo(info: VoiceBgmModel, completion: (error: Int) -> Unit) {
        if (mSceneReference == null) {
            completion.invoke(VoiceServiceProtocol.ERR_FAILED)
            return
        }
        if (kRoomBGMId != null) {
            mSceneReference?.collection(kRoomBGMCollection)?.update(kRoomBGMId, info, object: Sync.Callback {
                override fun onSuccess() {
                    Log.d(TAG, "kRoomBGMCollection update: $kRoomBGMId")
                    completion.invoke(VoiceServiceProtocol.ERR_OK)
                }
                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED)
                }
            })
        } else {
            mSceneReference?.collection(kRoomBGMCollection)?.add(info, object: Sync.DataItemCallback{
                override fun onSuccess(result: IObject?) {
                    Log.d(TAG, "kRoomBGMCollection add: $result")
                    kRoomBGMId = result?.id
                    completion.invoke(VoiceServiceProtocol.ERR_OK)
                }
                override fun onFail(exception: SyncManagerException?) {
                    completion.invoke(VoiceServiceProtocol.ERR_FAILED)
                }
            })
        }
    }

    /**
     * 是否启用机器人
     * @param enable true 启动机器人，false 关闭机器人
     */
    override fun enableRobot(enable: Boolean, completion: (error: Int, result:Boolean) -> Unit) {
        ChatroomIMManager.getInstance().enableRobot(enable,object :
            ValueCallBack<Boolean>{
            override fun onSuccess(value: Boolean) {
                completion.invoke(VoiceServiceProtocol.ERR_OK, value)
            }

            override fun onError(error: Int, errorMsg: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    /**
     * 更新机器人音量
     * @param value 音量
     */
    override fun updateRobotVolume(value: Int, completion: (error: Int, result: Boolean) -> Unit) {
        ChatroomIMManager.getInstance().updateRobotVolume(value,object : CallBack{
            override fun onSuccess() {
                completion.invoke(VoiceServiceProtocol.ERR_OK, true)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(VoiceServiceProtocol.ERR_FAILED, false)
            }
        })
    }

    override fun subscribeRoomTimeUp(onRoomTimeUp: () -> Unit) {
        roomTimeUpSubscriber = onRoomTimeUp
    }

    private fun initScene(complete: () -> Unit) {
        if (syncUtilsInit) {
            complete.invoke()
            return
        }
        val handler = Handler(Looper.getMainLooper())
        Sync.Instance().init(
            RethinkConfig(VoiceBuddyFactory.get().getVoiceBuddy().rtcAppId(), voiceSceneId),
            object : Sync.Callback {
                override fun onSuccess() {
                    handler.post {
                        Sync.Instance().joinScene(voiceSceneId, object: JoinSceneCallback{
                            override fun onSuccess(sceneReference: SceneReference?) {
                                sceneReference?.subscribe(object:Sync.EventListener{
                                    override fun onCreated(item: IObject?) {

                                    }

                                    override fun onUpdated(item: IObject?) {
                                        item ?: return
                                        val roomInfo = item.toObject(VoiceRoomModel::class.java)
                                        roomMap[roomInfo.roomId] = roomInfo
                                        "syncManager RoomChanged onUpdated:${roomInfo}".logD()
                                    }

                                    override fun onDeleted(item: IObject?) {
                                        item ?: return
                                        val roomInfo = roomMap[item.id] ?: return
                                        resetCacheInfo(roomInfo.roomId, true)
                                        "syncManager RoomChanged onDeleted:${roomInfo}".logD()
                                    }

                                    override fun onSubscribeError(ex: SyncManagerException?) {
                                        errorHandler?.invoke(ex)
                                    }

                                })
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
                        })
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

    private fun resetCacheInfo(roomId: String, isRoomDestroyed: Boolean = false) {
        if (isRoomDestroyed) {
            roomMap.remove(roomId)
        }
        mSceneReference = null
    }
}