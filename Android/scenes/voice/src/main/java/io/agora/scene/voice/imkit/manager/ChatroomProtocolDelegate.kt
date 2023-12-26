package io.agora.scene.voice.imkit.manager

import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import io.agora.CallBack
import io.agora.ValueCallBack
import io.agora.chat.ChatClient
import io.agora.chat.ChatRoomManager
import io.agora.scene.voice.model.annotation.MicClickAction
import io.agora.scene.voice.model.annotation.MicStatus
import io.agora.scene.voice.global.VoiceBuddyFactory
import io.agora.scene.voice.imkit.bean.ChatMessageData
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper
import io.agora.scene.voice.imkit.custorm.CustomMsgType
import io.agora.scene.voice.imkit.custorm.OnMsgCallBack
import io.agora.scene.voice.model.*
import io.agora.scene.voice.rtckit.AgoraBGMManager
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.utils.GsonTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.ThreadManager

class ChatroomProtocolDelegate constructor(
    private val roomId: String
) {
    companion object {
        private const val TAG = "ChatroomProtocolDelegate"
    }

    private var roomManager: ChatRoomManager = ChatClient.getInstance().chatroomManager()
    private var ownerBean = VoiceMemberModel()

    /////////////////////// mic ///////////////////////////

    /**
     * 初始化麦位信息
     */
    fun initMicInfo(roomType: Int, ownerBean: VoiceMemberModel, callBack: CallBack) {
        val attributeMap = mutableMapOf<String, String>()
        this@ChatroomProtocolDelegate.ownerBean = ownerBean
        if (roomType == ConfigConstants.RoomType.Common_Chatroom) {
            attributeMap["use_robot"] = "0"
            attributeMap["robot_volume"] = "50"
            attributeMap["mic_0"] = GsonTools.beanToString(VoiceMicInfoModel(0, ownerBean, MicStatus.Normal)).toString()
            for (i in 1..7) {
                var key = "mic_$i"
                var status = MicStatus.Idle
                if (i >= 6) status = MicStatus.BotInactive
                var mBean = GsonTools.beanToString(VoiceMicInfoModel(i, null, status))
                if (mBean != null) {
                    attributeMap[key] = mBean
                }
            }
        } else if (roomType == ConfigConstants.RoomType.Spatial_Chatroom) {

        }
        roomManager.asyncSetChatroomAttributesForced(
            roomId, attributeMap, true
        ) { code, result_map ->
            if (code == 0 && result_map.isEmpty()) {
                callBack.onSuccess()
                ChatroomCacheManager.cacheManager.setMicInfo(attributeMap)
                "initMicInfo update result onSuccess roomId:$roomId,".logD(TAG)
            } else {
                callBack.onError(code, result_map.toString())
                "initMicInfo update result onError roomId:$roomId, $code $result_map ".logE(TAG)
            }
        }
    }

    /**
     * 获取详情，kv 组装
     */
    fun fetchRoomDetail(voiceRoomModel: VoiceRoomModel, callback: ValueCallBack<VoiceRoomInfo>){
        val keyList: MutableList<String> =
            mutableListOf("ranking_list", "member_list", "gift_amount", "robot_volume", "use_robot", "room_bgm")
        for (i in 0..7) {
            keyList.add("mic_$i")
        }
        this.ownerBean = voiceRoomModel.owner ?: VoiceMemberModel()
        val voiceRoomInfo = VoiceRoomInfo()
        voiceRoomInfo.roomInfo = voiceRoomModel
        roomManager.asyncFetchChatRoomAnnouncement(roomId, object : ValueCallBack<String> {
            override fun onSuccess(value: String?) {
                voiceRoomModel.announcement = value ?: ""
            }
            override fun onError(error: Int, errorMsg: String?) {
            }
        })
        roomManager.asyncFetchChatroomAttributesFromServer(roomId, keyList,
            object : ValueCallBack<Map<String, String>> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onSuccess(result: Map<String, String>) {
                    val micInfoList = mutableListOf<VoiceMicInfoModel>()
                    val micMap = mutableMapOf<String,String>()
                    result.entries.forEach {
                        val key = it.key
                        val value = it.value
                        if (key.startsWith("mic_")){
                            micMap[key] = value
                        }else if (key=="ranking_list"){
                            val rankList = GsonTools.toList(value, VoiceRankUserModel::class.java)
                            rankList?.let { rankUsers ->
                                voiceRoomInfo.roomInfo?.rankingList = rankUsers
                                rankUsers.forEach { rank ->
                                    ChatroomCacheManager.cacheManager.setRankList(rank)
                                }
                            }
                        } else if (key == "member_list") {
                            val memberList = GsonTools.toList(value, VoiceMemberModel::class.java)
                            memberList?.let { members ->
                                "member_list($members) fetchRoomDetail onSuccess: ".logD(TAG)
                                addMemberListBySelf(members, object : ValueCallBack<List<VoiceMemberModel>> {
                                        override fun onSuccess(value: List<VoiceMemberModel>) {
                                            voiceRoomInfo.roomInfo?.memberList = value
                                            value.forEach { member ->
                                                if (!member.chatUid.equals(ownerBean.chatUid)){
                                                    ChatroomCacheManager.cacheManager.setMemberList(member)
                                                }
                                            }
                                        }

                                    override fun onError(code: Int, error: String?) {
                                        voiceRoomInfo.roomInfo?.memberList = memberList
                                        memberList.forEach { member ->
                                            if (!member.chatUid.equals(ownerBean.chatUid)){
                                                ChatroomCacheManager.cacheManager.setMemberList(member)
                                            }
                                        }
                                    }
                                })
                            }
                        }else if (key=="gift_amount"){
                            value.toIntOrNull()?.let {
                                voiceRoomInfo.roomInfo?.giftAmount = it
                                ChatroomCacheManager.cacheManager.setGiftAmountCache(it)
                            }
                        }else if (key=="robot_volume"){
                            value.toIntOrNull()?.let {
                                voiceRoomInfo.roomInfo?.robotVolume = it
                            }
                        }else if (key=="use_robot"){
                            voiceRoomInfo.roomInfo?.useRobot = value == "1"
                        } else if (key == "room_bgm") {
                            voiceRoomInfo.bgmInfo = GsonTools.toBean(value, VoiceBgmModel::class.java)
                        }
                    }
                    ChatroomCacheManager.cacheManager.clearMicInfo()
                    ChatroomCacheManager.cacheManager.setMicInfo(micMap)
                    for (entry in micMap.entries) {
                        GsonTools.toBean(entry.value, VoiceMicInfoModel::class.java)?.let {
                            micInfoList.add(it)
                        }
                    }
                    voiceRoomInfo.micInfo = micInfoList.sortedBy { it.micIndex }
                    "fetchRoomDetail onSuccess roomId:$roomId, $micInfoList".logD(TAG)
                    callback.onSuccess(voiceRoomInfo)
                }

                override fun onError(error: Int, desc: String?) {
                    "fetchRoomDetail onError roomId:$roomId, $error $desc".logE(TAG)
                    callback.onError(error,desc)
                }
            })
    }

    /**
     * 从服务端获取所有麦位信息
     */
    fun getMicInfoFromServer(callback: ValueCallBack<List<VoiceMicInfoModel>>) {
        val keyList: MutableList<String> = mutableListOf()
        for (i in 0..7) {
            keyList.add("mic_$i")
        }
        roomManager.asyncFetchChatroomAttributesFromServer(roomId, keyList,
            object : ValueCallBack<Map<String, String>> {
                override fun onSuccess(value: Map<String, String>) {
                    val micInfoList = mutableListOf<VoiceMicInfoModel>()
                    ChatroomCacheManager.cacheManager.clearMicInfo()
                    ChatroomCacheManager.cacheManager.setMicInfo(value)
                    for (entry in value.entries) {
                       GsonTools.toBean(entry.value, VoiceMicInfoModel::class.java)?.let {
                           micInfoList.add(it)
                       }
                    }
                    "getMicInfoFromServer onSuccess: $micInfoList".logD(TAG)
                    callback.onSuccess(micInfoList.sortedBy { it.micIndex })
                }

                override fun onError(error: Int, desc: String?) {
                    "getMicInfoFromServer onError: $error $desc".logE(TAG)
                    callback.onError(error,desc)
                }
            })
    }

    /**
     * 从本地缓存获取所有麦位信息
     */
    fun getMicInfo(): MutableMap<String, VoiceMicInfoModel> {
        val micInfoMap = mutableMapOf<String, VoiceMicInfoModel>()
        var localMap = ChatroomCacheManager.cacheManager.getMicInfoMap()
        if (localMap != null) {
            for (entry in localMap.entries) {
               GsonTools.toBean(entry.value, VoiceMicInfoModel::class.java)?.let {
                   micInfoMap[entry.key] = it
               }
            }
        }
        return micInfoMap
    }

    /**
     * 从本地获取指定麦位信息
     */
    private fun getMicInfo(micIndex: Int): VoiceMicInfoModel? {
        return ChatroomCacheManager.cacheManager.getMicInfoByIndex(micIndex)
    }

    /**
     * 从服务端获取指定麦位信息
     */
    fun getMicInfoByIndexFromServer(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        val keyList: MutableList<String> = mutableListOf(getMicIndex(micIndex))
        roomManager.asyncFetchChatroomAttributesFromServer(roomId, keyList, object :
            ValueCallBack<Map<String, String>> {
            override fun onSuccess(value: Map<String, String>) {
                val micBean = GsonTools.toBean(value[getMicIndex(micIndex)], VoiceMicInfoModel::class.java)
                callback.onSuccess(micBean)
            }

            override fun onError(error: Int, desc: String?) {
                "getMicInfoByIndex onError: $error $desc".logE(TAG)
                callback.onError(error, desc)
            }
        })
    }

    /**
     * 下麦
     */
    fun leaveMic(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        updateMicByResult(null,micIndex, MicClickAction.OffStage, true, callback)
    }

    /**
     * 交换麦位
     */
    fun changeMic(fromMicIndex: Int, toMicIndex: Int, callback: ValueCallBack<Map<Int, VoiceMicInfoModel>>) {
        val attributeMap = mutableMapOf<String, String>()
        val fromKey = getMicIndex(fromMicIndex)
        val toKey = getMicIndex(toMicIndex)
        val fromBean = getMicInfo(fromMicIndex)
        val toMicBean = getMicInfo(toMicIndex)
        if (toMicBean != null && fromBean != null && (toMicBean.micStatus == MicStatus.Idle || toMicBean.micStatus == MicStatus.Mute || toMicBean.micStatus == MicStatus.ForceMute)) {
            val fromMicStatus = fromBean.micStatus
            val toMicStatus = toMicBean.micStatus
            fromBean.member?.micIndex  = toMicIndex
            fromBean.micIndex = toMicIndex
            when (toMicStatus) {
                MicStatus.ForceMute -> {
                    fromBean.micStatus = MicStatus.ForceMute
                }
                else -> {
                    fromBean.micStatus = MicStatus.Normal
                }
            }

            toMicBean.micIndex = fromMicIndex
            when (fromMicStatus) {
                MicStatus.ForceMute -> {
                    toMicBean.micStatus = MicStatus.ForceMute
                }
                else -> {
                    toMicBean.micStatus = MicStatus.Idle
                }
            }
            val fromBeanValue = GsonTools.beanToString(fromBean)
            val toBeanValue = GsonTools.beanToString(toMicBean)
            if (toBeanValue != null) {
                attributeMap[fromKey] = toBeanValue
            }
            if (fromBeanValue != null) {
                attributeMap[toKey] = fromBeanValue
            }
            roomManager.asyncSetChatroomAttributesForced(
                roomId, attributeMap, false
            ) { code, result_map ->
                if (code == 0 && result_map.isEmpty()) {
                    val map = mutableMapOf<Int, VoiceMicInfoModel>()
                    map[fromMicIndex] = toMicBean
                    map[toMicIndex] = fromBean
                    attributeMap.let { ChatroomCacheManager.cacheManager.setMicInfo(it) }
                    callback.onSuccess(map)
                    "changeMic update result onSuccess: ".logD(TAG)
                } else {
                    callback.onError(code, result_map.toString())
                    "changeMic update result onError: $code $result_map ".logE(TAG)
                }
            }
        }
    }

    /**
     * 关麦
     */
    fun muteLocal(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        updateMicByResult(null,micIndex, MicClickAction.Mute, true, callback)
    }

    /**
     * 取消关麦
     */
    fun unMuteLocal(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        updateMicByResult(null,micIndex, MicClickAction.UnMute, true, callback)
    }

    /**
     * 禁言指定麦位
     */
    fun forbidMic(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        updateMicByResult(null,micIndex, MicClickAction.ForbidMic, true, callback)
    }

    /**
     * 取消指定麦位禁言
     */
    fun unForbidMic(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        updateMicByResult(null,micIndex, MicClickAction.UnForbidMic, true, callback)
    }

    /**
     * 踢用户下麦
     */
    fun kickOff(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        updateMicByResult(null,micIndex, MicClickAction.KickOff, true, callback)
    }

    /**
     * 锁麦
     */
    fun lockMic(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        updateMicByResult(null,micIndex, MicClickAction.Lock, true, callback)
    }

    /**
     * 取消锁麦
     */
    fun unLockMic(micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        updateMicByResult(null,micIndex, MicClickAction.UnLock, true, callback)
    }

    /**
     * 获取上麦申请列表
     */
    fun fetchRaisedList(): MutableList<VoiceMemberModel> {
        return ChatroomCacheManager.cacheManager.getSubmitMicList()
    }

    /**
     * 申请上麦
     */
    fun startMicSeatApply(micIndex: Int, callback: CallBack) {
        val attributeMap = mutableMapOf<String, String>()
        val voiceRoomApply = VoiceRoomApply()
        val memberBean = VoiceMemberModel().apply {
            userId = VoiceBuddyFactory.get().getVoiceBuddy().userId()
            chatUid = VoiceBuddyFactory.get().getVoiceBuddy().chatUserName()
            rtcUid = VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()
            nickName = VoiceBuddyFactory.get().getVoiceBuddy().nickName()
            portrait = VoiceBuddyFactory.get().getVoiceBuddy().headUrl()
        }
        if (micIndex != -1) {
            voiceRoomApply.index = micIndex
            memberBean.micIndex = micIndex
        }
        voiceRoomApply.member = memberBean
        "startMicSeatApply:$memberBean".logD(TAG)
        voiceRoomApply.created_at = System.currentTimeMillis()
        attributeMap["user"] = GsonTools.beanToString(voiceRoomApply).toString()
        attributeMap["chatroomId"] = ChatroomIMManager.getInstance().currentRoomId
        sendChatroomEvent(true, ownerBean.chatUid, CustomMsgType.CHATROOM_APPLY_SITE, attributeMap, callback)
    }

    /**
     * 同意上麦申请
     */
    fun acceptMicSeatApply(chatUid:String,micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        val memberBean = ChatroomCacheManager.cacheManager.getMember(chatUid)
        if (memberBean != null) {
            if (micIndex != -1) {
                val micInfoModel = getMicInfo(micIndex)
                // 指定的麦位没人，则在指定麦位，有人则找空闲位置
                memberBean.micIndex = if (micInfoModel?.micStatus == MicStatus.Idle || micInfoModel?.micStatus==MicStatus.ForceMute) micIndex else getFirstFreeMic()
            } else {
                memberBean.micIndex = getFirstFreeMic()
            }
        }
        ThreadManager.getInstance().runOnIOThread {
            if (checkMemberIsOnMic(memberBean)) return@runOnIOThread
            memberBean?.let {
                updateMicByResult(memberBean,
                    it.micIndex, MicClickAction.Accept, true, callback)
            }
        }
    }

    /**
     * check 用户是否在麦位上
     */
    private fun checkMemberIsOnMic(memberModel: VoiceMemberModel?): Boolean {
        memberModel ?: return true
        val micMap = ChatroomCacheManager.cacheManager.getMicInfoMap()
        micMap?.forEach { (t, u) ->
            val micMember = GsonTools.toBean(u, VoiceMicInfoModel::class.java)
            if (TextUtils.equals(memberModel.chatUid, micMember?.member?.chatUid)) {
                return true
            }
        }
        return false
    }

    /**
     * 拒绝上麦申请
     */
    fun rejectSubmitMic() {
        // TODO: 本期暂无 拒绝上麦申请
    }

    /**
     * 撤销上麦申请
     */
    fun cancelSubmitMic(chatroomId: String, chatUid: String, callback: CallBack) {
        val attributeMap = mutableMapOf<String, String>()
        val userBeam = ChatroomCacheManager.cacheManager.getMember(chatUid)
        attributeMap["user"] = GsonTools.beanToString(userBeam).toString()
        attributeMap["chatroomId"] = chatroomId
        sendChatroomEvent(true, ownerBean.chatUid, CustomMsgType.CHATROOM_CANCEL_APPLY_SITE, attributeMap, callback)
    }

    /**
     * 邀请上麦列表(过滤已在麦位的成员)
     */
    fun fetchRoomInviteMembers():MutableList<VoiceMemberModel> {
        return ChatroomCacheManager.cacheManager.getInvitationList()
    }

    /**
     * 获取观众列表
     */
    fun fetchRoomMembers():MutableList<VoiceMemberModel> {
        return ChatroomCacheManager.cacheManager.getMemberList()
    }

    /**
     * 邀请上麦
     */
    fun invitationMic(chatUid: String, micIndex: Int, callback: CallBack) {
        val attributeMap = mutableMapOf<String, String>()
        val userBeam = ChatroomCacheManager.cacheManager.getMember(chatUid)
        userBeam?.micIndex = micIndex
        attributeMap["user"] = GsonTools.beanToString(userBeam).toString()
        attributeMap["chatroomId"] = ChatroomIMManager.getInstance().currentRoomId
        sendChatroomEvent(true, chatUid, CustomMsgType.CHATROOM_INVITE_SITE, attributeMap, callback)
    }

    /**
     * 用户拒绝上麦邀请
     */
    fun refuseInviteToMic(chatUid: String, callback: CallBack) {
        val attributeMap = mutableMapOf<String, String>()
        attributeMap["chatroomId"] = ChatroomIMManager.getInstance().currentRoomId
        sendChatroomEvent(true, ownerBean.chatUid, CustomMsgType.CHATROOM_INVITE_REFUSED_SITE, attributeMap, callback)
    }

    /**
     * 用户同意上麦邀请
     */
    fun acceptMicSeatInvitation(chatUid: String,micIndex: Int, callback: ValueCallBack<VoiceMicInfoModel>) {
        val memberBean = ChatroomCacheManager.cacheManager.getMember(chatUid)
        if (memberBean != null) {
            if (micIndex != -1) {
                val micInfoModel = getMicInfo(micIndex)
                // 指定的麦位没人，则在指定麦位，有人则找空闲位置
                memberBean.micIndex = if (micInfoModel?.micStatus == MicStatus.Idle || micInfoModel?.micStatus==MicStatus.ForceMute) micIndex else getFirstFreeMic()
            } else {
                memberBean.micIndex = getFirstFreeMic()
            }
        }
        ThreadManager.getInstance().runOnIOThread {
            if (checkMemberIsOnMic(memberBean)) return@runOnIOThread
            memberBean?.let {
                updateMicByResult(memberBean,
                    it.micIndex, MicClickAction.Accept, true, callback)
            }
        }
    }

    /////////////////////////// room ///////////////////////////////

    /**
     * 更新公告
     */
    fun updateAnnouncement(content: String, callback: CallBack) {
        roomManager.asyncUpdateChatRoomAnnouncement(roomId, content, object : CallBack {
            override fun onSuccess() {
                callback.onSuccess()
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code, error)
            }
        })
    }

    /**
     * 是否启用机器人
     * @param enable true 启动机器人，false 关闭机器人
     */
    fun enableRobot(enable: Boolean, callback: ValueCallBack<Boolean>) {
        val attributeMap = mutableMapOf<String, String>()
        val currentUser = VoiceBuddyFactory.get().getVoiceBuddy().chatUserName()
        var robot6 = VoiceMicInfoModel()
        var robot7 = VoiceMicInfoModel()
        var isEnable: String
        if (TextUtils.equals(ownerBean.chatUid, currentUser)) {
            if (enable) {
                robot6.micIndex = 6
                robot6.micStatus = MicStatus.BotActivated
                robot7.micIndex = 7
                robot7.micStatus = MicStatus.BotActivated
                isEnable = "1"
            } else {
                robot6.micIndex = 6
                robot6.micStatus = MicStatus.BotInactive
                robot7.micIndex = 7
                robot7.micStatus = MicStatus.BotInactive
                isEnable = "0"
            }
            attributeMap["mic_6"] = GsonTools.beanToString(robot6).toString()
            attributeMap["mic_7"] = GsonTools.beanToString(robot7).toString()
            attributeMap["use_robot"] = isEnable
            roomManager.asyncSetChatroomAttributesForced(
                roomId, attributeMap, true
            ) { code, result ->
                if (code == 0 && result.isEmpty()) {
                    val map = mutableMapOf<Int, VoiceMicInfoModel>()
                    map[6] = robot6
                    map[7] = robot7
                    ChatroomCacheManager.cacheManager.setMicInfo(attributeMap)
                    callback.onSuccess(true)
                    "enableRobot($enable) update result onSuccess: ".logD(TAG)
                } else {
                    callback.onError(code, result.toString())
                    "enableRobot($enable) update result onError: $code $result ".logE(TAG)
                }
            }
        }
    }

    /**
     * 更新机器人音量
     * @param value 音量
     */
    fun updateRobotVolume(value: Int, callback: CallBack) {
        roomManager.asyncSetChatroomAttribute(roomId, "robot_volume", value.toString(), true, object :
            CallBack {
            override fun onSuccess() {
                callback.onSuccess()
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code, error)
            }
        })
    }

    /**
     * 更新指定麦位信息并返回更新成功的麦位信息
     * 0:正常状态 1:闭麦 2:禁言 3:锁麦 4:锁麦和禁言 -1:空闲 5:机器人专属激活状态 -2:机器人专属关闭状态
     */
    private fun updateMicByResult(member: VoiceMemberModel? = null,
                                  micIndex: Int, @MicClickAction clickAction: Int, isForced: Boolean, callback: ValueCallBack<VoiceMicInfoModel>
    ) {
        val voiceMicInfo = getMicInfo(micIndex) ?: return
        updateMicStatusByAction(voiceMicInfo, clickAction, member)
        voiceMicInfo.micIndex = micIndex
        val value = GsonTools.beanToString(voiceMicInfo) ?: return
        if (isForced) {
            roomManager.asyncSetChatroomAttributeForced(roomId, getMicIndex(micIndex),
                value, false, object : CallBack {
                    override fun onSuccess() {
                        val attributeMap = mutableMapOf<String, String>()
                        attributeMap[getMicIndex(micIndex)] = value
                        ChatroomCacheManager.cacheManager.setMicInfo(attributeMap)
                        callback.onSuccess(voiceMicInfo)
                        "Forced updateMic onSuccess: ".logD(TAG)
                    }

                    override fun onError(code: Int, desc: String?) {
                        callback.onError(code, desc)
                        "Forced updateMic onError: $code $desc".logE(TAG)
                    }
                })
        } else {
            roomManager.asyncSetChatroomAttribute(roomId, getMicIndex(micIndex),
                value, false, object : CallBack {
                    override fun onSuccess() {
                        val attributeMap = mutableMapOf<String, String>()
                        attributeMap[getMicIndex(micIndex)] = value
                        ChatroomCacheManager.cacheManager.setMicInfo(attributeMap)
                        callback.onSuccess(voiceMicInfo)
                        "updateMic onSuccess: ".logD(TAG)
                    }

                    override fun onError(code: Int, desc: String?) {
                        callback.onError(code, desc)
                        "updateMic onError: $code $desc".logE(TAG)
                    }
                })
        }
    }

    /**
     * 根据麦位原状态与action 更新麦位状态
     */
    private fun updateMicStatusByAction(micInfo: VoiceMicInfoModel, @MicClickAction action: Int, memberBean: VoiceMemberModel? = null) {
        when (action) {
            MicClickAction.ForbidMic -> {
                // 禁言（房主操作）
                if (micInfo.micStatus == MicStatus.Lock) {
                    micInfo.micStatus = MicStatus.LockForceMute
                } else {
                    micInfo.micStatus = MicStatus.ForceMute
                }
            }
            // 取消禁言（房主操作）
            MicClickAction.UnForbidMic -> {
                if (micInfo.micStatus == MicStatus.LockForceMute) {
                    micInfo.micStatus = MicStatus.Lock
                } else {
                    if (micInfo.member == null) {
                        micInfo.micStatus = MicStatus.Idle
                    } else {
                        micInfo.micStatus = MicStatus.Normal
                    }
                }
            }
            // 关麦（麦位用户操作包括房主操作自己）
            MicClickAction.Mute -> {
                micInfo.member?.micStatus = 0
            }
            // 开麦（麦位用户操作包括房主操作自己）
            MicClickAction.UnMute -> {
                if (micInfo.member == null) {
                    micInfo.micStatus = MicStatus.Idle
                } else {
                    micInfo.micStatus = MicStatus.Normal
                    micInfo.member?.micStatus = 1
                }
            }
            // 关闭座位（房主操作）
            MicClickAction.Lock -> {
                if (micInfo.micStatus == MicStatus.ForceMute) {
                    micInfo.micStatus = MicStatus.LockForceMute
                } else {
                    micInfo.micStatus = MicStatus.Lock
                }
                micInfo.member = null
            }
            // 打开座位（房主操作）
            MicClickAction.UnLock -> {
                if (micInfo.micStatus == MicStatus.LockForceMute) {
                    micInfo.micStatus = MicStatus.ForceMute
                } else {
                    if (micInfo.member == null) {
                        micInfo.micStatus = MicStatus.Idle
                    } else {
                        micInfo.micStatus = MicStatus.Normal
                    }
                }
            }
            // 强制下麦（房主操作）
            MicClickAction.KickOff -> {
                if (micInfo.micStatus == MicStatus.ForceMute) {
                    micInfo.micStatus = MicStatus.ForceMute
                } else {
                    micInfo.micStatus = MicStatus.Idle
                }
                micInfo.member = null
            }
            // 下麦（嘉宾操作）
            MicClickAction.OffStage -> {
                if (micInfo.micStatus == MicStatus.ForceMute) {
                    micInfo.micStatus = MicStatus.ForceMute
                } else {
                    micInfo.micStatus = MicStatus.Idle
                }
                micInfo.member = null
            }
            // 接受邀请/接受申请
            MicClickAction.Accept -> {
                "MicClickAction.Accept: ${micInfo.micStatus}".logD(TAG)
                if (micInfo.micStatus == -1){
                    micInfo.micStatus = MicStatus.Normal
                }
                if (memberBean != null){
                    micInfo.member = memberBean
                }
            }
            MicClickAction.Invite -> {
                "MicClickAction.Invite: ${micInfo.micStatus}".logD(TAG)
                if (micInfo.micStatus == -1){
                    micInfo.micStatus = MicStatus.Normal
                }
                if (memberBean != null){
                    micInfo.member = memberBean
                }
            }
        }
    }

    /**
     * 更新榜单
     */
    fun updateRankList(chatUid:String, giftBean: VoiceGiftModel, callback: CallBack){
        //首先拿到所有的数据
        val rankMap = ChatroomCacheManager.cacheManager.getRankMap()
        //创建一个新列表
        val rankingList = mutableListOf<VoiceRankUserModel>()
        //获取指定id的对象
        var voiceRankModel = rankMap[chatUid]
        if (voiceRankModel == null){
            voiceRankModel = VoiceRankUserModel()
        }
        var newAmount = 0
        val name = giftBean.userName
        val portrait = giftBean.portrait
        val count = giftBean.gift_count?.toInt()
        val price = giftBean.gift_price?.toInt()
        if (count != null && price !=null){
            newAmount = count * price
        }
        val oldAmount = voiceRankModel.amount
        voiceRankModel.amount = oldAmount + newAmount
        voiceRankModel.chatUid = chatUid
        voiceRankModel.name = name
        voiceRankModel.portrait = portrait
        rankMap[chatUid] = voiceRankModel
        for (entry in rankMap.entries) {
            rankingList.add(entry.value)
        }
        roomManager.asyncSetChatroomAttributeForced(roomId,"ranking_list", GsonTools.beanToString(rankingList),
            false,object : CallBack{
            override fun onSuccess() {
                ChatroomCacheManager.cacheManager.setRankList(voiceRankModel)
                callback.onSuccess()
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code,error)
            }
        })
    }

    /**
     * 更新礼物总数
     */
    fun updateGiftAmount(chatUid: String,newAmount:Int,callback: CallBack){
        if (TextUtils.equals(ownerBean.chatUid,chatUid)){
            var giftAmount = ChatroomCacheManager.cacheManager.getGiftAmountCache()
            giftAmount += newAmount
            roomManager.asyncSetChatroomAttribute(roomId,"gift_amount",giftAmount.toString(),true,object :
                CallBack{
                override fun onSuccess() {
                    "update giftAmount onSuccess: $giftAmount".logD(TAG)
                    ChatroomCacheManager.cacheManager.updateGiftAmountCache(newAmount)
                    callback.onSuccess()
                }

                override fun onError(code: Int, error: String?) {
                    "update giftAmount onError: $code $error".logE(TAG)
                    callback.onError(code,error)
                }
            })
        }
    }

    /**
     * 从服务端获取榜单
     */
    fun fetchGiftContribute(callback: ValueCallBack<List<VoiceRankUserModel>>){
        val rankingList = ChatroomCacheManager.cacheManager.getRankList()
        if (rankingList.isEmpty()){
            val keyList: MutableList<String> = java.util.ArrayList()
            keyList.add("ranking_list")
            roomManager.asyncFetchChatroomAttributesFromServer(roomId,keyList,object :
                ValueCallBack<MutableMap<String, String>>{
                override fun onSuccess(value: MutableMap<String, String>) {
                    ThreadManager.getInstance().runOnMainThread{
                        value["ranking_list"]?.let {
                            val rankList = GsonTools.toList(it, VoiceRankUserModel::class.java)
                            rankList?.forEach { bean ->
                                ChatroomCacheManager.cacheManager.setRankList(bean)
                            }
                            callback.onSuccess(rankList)
                        }
                        "getRankList onSuccess: $value".logD(TAG)
                    }
                }

                override fun onError(code: Int, errorMsg: String?) {
                    ThreadManager.getInstance().runOnMainThread{
                        callback.onError(code,errorMsg)
                        "getRankList onError: $code $errorMsg".logE(TAG)
                    }
                }
            })
        }else{
            callback.onSuccess(rankingList)
        }
    }

    /**
     * 从服务端获取成员列表
     */
    fun getMemberFromServer(callback: ValueCallBack<List<VoiceMemberModel>>){
        ChatroomCacheManager.cacheManager.getMemberList()
        val keyList: MutableList<String> = mutableListOf("member_list")
        roomManager.asyncFetchChatroomAttributesFromServer(roomId,keyList,object :
            ValueCallBack<MutableMap<String, String>>{
            override fun onSuccess(value: MutableMap<String, String>) {
                GsonTools.toList(value["member_list"], VoiceMemberModel::class.java)?.let { memberList ->
                    callback.onSuccess(memberList)
                }
            }

            override fun onError(code: Int, errorMsg: String?) {
                callback.onError(code,errorMsg)
            }
        })
    }

    /**
     * 获取当前用户实体信息
     */
     fun getMySelfModel(): VoiceMemberModel {
        var micIndex : Int = -1
        if (TextUtils.equals(ownerBean.chatUid, VoiceBuddyFactory.get().getVoiceBuddy().chatUserName())) {
            micIndex = 0
        }
        return VoiceMemberModel(
            userId = VoiceBuddyFactory.get().getVoiceBuddy().userId(),
            chatUid = VoiceBuddyFactory.get().getVoiceBuddy().chatUserName(),
            nickName = VoiceBuddyFactory.get().getVoiceBuddy().nickName(),
            portrait = VoiceBuddyFactory.get().getVoiceBuddy().headUrl(),
            rtcUid = VoiceBuddyFactory.get().getVoiceBuddy().rtcUid(),
            micIndex = micIndex).also {
                "getMySelfModel:$it".logD(TAG)
        }
    }

    /**
     * 向成员列表中添加自己(每个新加入房间的人需要调用一次)
     */
    fun addMemberListBySelf(memberList:List<VoiceMemberModel>, callback: ValueCallBack<List<VoiceMemberModel>>){
        val newMemberList = memberList.toMutableList()
        newMemberList.add(getMySelfModel())
        val member = GsonTools.beanToString(memberList)
        roomManager.asyncSetChatroomAttributeForced(roomId,
            "member_list",member,false,object : CallBack{
            override fun onSuccess() {
                callback.onSuccess(newMemberList)
                "addMemberListBySelf onSuccess: ".logD(TAG)
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code,error)
                "addMemberListBySelf onError: $code $error".logE(TAG)
            }
        })
    }

    /**
     * 更新成员列表
     */
    fun updateRoomMember(memberList:List<VoiceMemberModel>,callback: CallBack){
        if (TextUtils.equals(ownerBean.chatUid, VoiceBuddyFactory.get().getVoiceBuddy().chatUserName())) {
            val member = GsonTools.beanToString(memberList)
            roomManager.asyncSetChatroomAttributeForced(roomId,
                "member_list",member,false,object : CallBack{
                    override fun onSuccess() {
                        callback.onSuccess()
                        "updateRoomMember onSuccess: ".logD(TAG)
                    }

                    override fun onError(code: Int, error: String?) {
                        callback.onError(code,error)
                        "updateRoomMember onError: $code $error".logE(TAG)
                    }
                })
        }
    }

    private fun sendChatroomEvent(
        isSingle: Boolean, chatUid: String?, eventType: CustomMsgType,
        params: MutableMap<String, String>, callback: CallBack
    ) {
        if (isSingle) {
            CustomMsgHelper.getInstance().sendCustomSingleMsg(chatUid,
                eventType.getName(), params, object : OnMsgCallBack() {
                    override fun onSuccess(message: ChatMessageData?) {
                        callback.onSuccess()
                        "sendCustomSingleMsg onSuccess: $message".logD(TAG)
                    }

                    override fun onError(messageId: String?, code: Int, desc: String?) {
                        callback.onError(code, desc)
                        "sendCustomSingleMsg onError: $code $desc".logE(TAG)
                    }
                })
        } else {
            CustomMsgHelper.getInstance().sendCustomMsg(roomId, params, object : OnMsgCallBack() {
                override fun onSuccess(message: ChatMessageData?) {
                    callback.onSuccess()
                    "sendCustomMsg onSuccess: $message".logD(TAG)
                }

                override fun onError(messageId: String?, code: Int, desc: String?) {
                    super.onError(messageId, code, desc)
                    callback.onError(code, desc)
                    "sendCustomMsg onError: $code $desc".logE(TAG)
                }
            })
        }

    }

    /**
     *  按麦位顺序查询空麦位
     */
    private fun getFirstFreeMic(): Int {
        val indexList: MutableList<Int> = mutableListOf<Int>()
        val micInfo = ChatroomCacheManager.cacheManager.getMicInfoMap()
        if (micInfo != null) {
            for (mutableEntry in micInfo) {
                val bean = GsonTools.toBean(mutableEntry.value, VoiceMicInfoModel::class.java)
                if (bean != null ) {
                    "getFirstFreeMic: ${bean.micIndex}  ${bean.micStatus}".logE(TAG)
                    if(bean.micStatus == -1 || bean.micStatus == 2){
                        indexList.add(bean.micIndex)
                    }
                }
            }
        }
        indexList.sortBy { it }
        return indexList[0]
    }

    private fun getMicIndex(index: Int): String {
        return "mic_$index"
    }

    fun clearCache(){
        ChatroomCacheManager.cacheManager.clearAllCache()
    }

    fun updateMicInfoCache(kvMap: Map<String,String>){
        ChatroomCacheManager.cacheManager.setMicInfo(kvMap)
    }
}