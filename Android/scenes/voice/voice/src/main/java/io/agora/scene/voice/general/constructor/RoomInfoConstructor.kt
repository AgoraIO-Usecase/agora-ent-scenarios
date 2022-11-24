package io.agora.scene.voice.general.constructor

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import io.agora.scene.voice.bean.*
import io.agora.scene.voice.service.*
import io.agora.voice.buddy.tool.GsonTools
import io.agora.voice.buddy.config.ConfigConstants

/**
 * @author create by zhangwei03
 */
object RoomInfoConstructor {

    /** VoiceRoomModel convert RoomKitBean*/
    fun RoomKitBean.convertByVoiceRoomModel(voiceRoomModel: VoiceRoomModel) {
        roomId = voiceRoomModel.roomId
        chatroomId = voiceRoomModel.chatroomId
        channelId = voiceRoomModel.channelId
        ownerId = voiceRoomModel.owner?.userId ?: ""
        roomType = voiceRoomModel.roomType
        isOwner = curUserIsHost(voiceRoomModel.owner?.userId)
        soundEffect = voiceRoomModel.soundEffect
    }

    /** Check if you are a host */
    private fun curUserIsHost(ownerId: String?): Boolean {
        return TextUtils.equals(ownerId, VoiceBuddyFactory.get().getVoiceBuddy().userId())
    }

    /**
     * 服务端roomInfo bean 转 ui bean
     */
    fun voiceRoomModel2UiRoomInfo(roomModel: VoiceRoomModel): RoomInfoBean {
        val roomInfo = RoomInfoBean().apply {
            channelId = roomModel.channelId
            chatroomName = roomModel.roomName
            owner = roomModel.owner
            memberCount = roomModel.memberCount
            // 普通观众 memberCount +1
            if (owner?.rtcUid != VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
                memberCount += 1
            }
            soundSelection = roomModel.soundEffect
            roomType = roomModel.roomType
        }
        return roomInfo
    }

    /**
     * 服务端roomInfo bean 转 ui bean
     */
    fun serverRoomInfo2UiRoomInfo(roomDetail: VoiceRoomModel): RoomInfoBean {
        val roomInfo = RoomInfoBean().apply {
            channelId = roomDetail.channelId
            chatroomName = roomDetail.roomName
            owner = roomDetail.owner
            memberCount = roomDetail.memberCount
            // 普通观众 memberCount +1
            if (owner?.rtcUid != VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
                memberCount += 1
            }
            giftCount = roomDetail.giftAmount
            watchCount = roomDetail.clickCount
            soundSelection = roomDetail.soundEffect
            roomType = roomDetail.roomType
        }
        roomDetail.rankingList?.let { rankList ->
            roomInfo.topRankUsers = rankList
        }
        return roomInfo
    }

    /**
     * 服务端roomInfo io.agora.scene.voice.imkit.bean 转 麦位 ui io.agora.scene.voice.imkit.bean
     */
    fun convertMicUiBean(vMicInfoList: List<VoiceMicInfoModel>, roomType: Int, ownerUid: String): List<MicInfoBean> {
        val micInfoList = mutableListOf<MicInfoBean>()
        val interceptIndex = if (roomType == ConfigConstants.RoomType.Common_Chatroom) 5 else 4
        for (i in vMicInfoList.indices) {
            if (i > interceptIndex) break
            val serverMicInfo = vMicInfoList[i]
            val micInfo = MicInfoBean().apply {
                index = serverMicInfo.micIndex
                serverMicInfo.member?.let { roomUser ->
                    userInfo = roomUser
                    ownerTag = !TextUtils.isEmpty(ownerUid) && TextUtils.equals(ownerUid, roomUser.userId)
                    // 有人默认显示音量柱
                    audioVolumeType = ConfigConstants.VolumeType.Volume_None
                }
            }
            micInfo.micStatus = serverMicInfo.micStatus
            micInfoList.add(micInfo)
        }
        return micInfoList
    }

    /**
     * im kv 属性转 micInfo
     * key mic0 value micInfo
     */
    fun convertAttr2MicInfoMap(attributeMap: Map<String, String>): Map<String, VoiceMicInfoModel> {
        val micInfoMap = mutableMapOf<String, VoiceMicInfoModel>()
        attributeMap.entries.forEach { entry ->
            GsonTools.toBean<VoiceMicInfoModel>(entry.value, object : TypeToken<VoiceMicInfoModel>() {}.type)
                ?.let { attrBean ->
                    micInfoMap[entry.key] = attrBean
                }
        }
        return micInfoMap
    }

    /**
     * micInfo map转换ui io.agora.scene.voice.imkit.bean
     */
    fun convertMicInfoMap2UiBean(micInfoMap: Map<String, VoiceMicInfoModel>, ownerUid: String): Map<Int, MicInfoBean> {
        val micInfoBeanMap = mutableMapOf<Int, MicInfoBean>()
        micInfoMap.entries.forEach { entry ->
            var index = ConfigConstants.MicConstant.micMap[entry.key]
            if (index == null) index = -1
            entry.value.let { attrBean ->
                val micInfo = MicInfoBean().apply {
                    this.index = index
                    this.micStatus = attrBean.micStatus
                    attrBean.member?.let { roomUser ->
                        userInfo = roomUser
                        ownerTag = !TextUtils.isEmpty(ownerUid) && TextUtils.equals(ownerUid, roomUser.userId)
                    }
                }
                micInfoBeanMap[index] = micInfo
            }
        }
        return micInfoBeanMap
    }
}