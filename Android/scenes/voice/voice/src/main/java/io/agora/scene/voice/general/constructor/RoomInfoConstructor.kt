package io.agora.scene.voice.general.constructor

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import io.agora.scene.voice.bean.*
import io.agora.scene.voice.service.VoiceBuddyFactory
import io.agora.scene.voice.service.VoiceMemberModel
import io.agora.scene.voice.service.VoiceRoomModel
import io.agora.voice.buddy.tool.GsonTools
import io.agora.voice.buddy.config.ConfigConstants
import io.agora.voice.network.tools.bean.*

/**
 * @author create by zhangwei03
 */
object RoomInfoConstructor {

    /** VoiceRoomModel convert RoomKitBean*/
    fun RoomKitBean.convertByVoiceRoomModel(voiceRoomModel: VoiceRoomModel) {
        roomId = voiceRoomModel.roomId
        chatroomId = voiceRoomModel.chatroomId
        channelId = voiceRoomModel.channelId
        ownerId = voiceRoomModel.owner?.uid ?: ""
        roomType = voiceRoomModel.roomType
        isOwner = curUserIsHost(voiceRoomModel.owner?.uid)
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
            owner = voiceMemberUser2UiUser(roomModel.owner)
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

    private fun voiceMemberUser2UiUser(vUser: VoiceMemberModel?): RoomUserInfoBean? {
        if (vUser == null) return null
        return RoomUserInfoBean().apply {
            userId = vUser.uid ?: ""
            chatUid = vUser.chatUid ?: ""
            rtcUid = vUser.rtcUid
            username = vUser.nickName ?: ""
            userAvatar = vUser.portrait ?: ""
        }
    }

    /**
     * 服务端roomInfo bean 转 ui bean
     */
    fun serverRoomInfo2UiRoomInfo(roomDetail: VRoomInfoBean.VRoomDetail): RoomInfoBean {
        val roomInfo = RoomInfoBean().apply {
            channelId = roomDetail.channel_id ?: ""
            chatroomName = roomDetail.name ?: ""
            owner = serverUser2UiUser(roomDetail.owner)
            memberCount = roomDetail.member_count
            // 普通观众 memberCount +1
            if (owner?.rtcUid != VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
                memberCount += 1
            }
            giftCount = roomDetail.gift_amount
            watchCount = roomDetail.click_count
            soundSelection = roomDetail.soundSelection
            roomType = roomDetail.type
        }
        roomDetail.ranking_list?.let { rankList ->
            val rankUsers = mutableListOf<RoomRankUserBean>()
            for (i in rankList.indices) {
                // 取前三名
                if (i > 2) break
                serverRoomRankUserToUiBean(rankList[i])?.let { rankUser ->
                    rankUsers.add(rankUser)
                }
            }
            roomInfo.topRankUsers = rankUsers
        }
        return roomInfo
    }

    private fun serverUser2UiUser(vUser: VMemberBean?): RoomUserInfoBean? {
        if (vUser == null) return null
        return RoomUserInfoBean().apply {
            userId = vUser.uid ?: ""
            chatUid = vUser.chat_uid ?: ""
            rtcUid = vUser.rtc_uid
            username = vUser.name ?: ""
            userAvatar = vUser.portrait ?: ""
        }
    }

    private fun serverRoomRankUserToUiBean(vUser: VRankingMemberBean?): RoomRankUserBean? {
        if (vUser == null) return null
        return RoomRankUserBean().apply {
            username = vUser.name ?: ""
            userAvatar = vUser.portrait ?: ""
            amount = vUser.amount
        }
    }

    /**
     * 服务端roomInfo io.agora.scene.voice.imkit.bean 转 麦位 ui io.agora.scene.voice.imkit.bean
     */
    fun convertMicUiBean(vRoomMicInfoList: List<VRMicBean>, roomType: Int, ownerUid: String): List<MicInfoBean> {
        val micInfoList = mutableListOf<MicInfoBean>()
        val interceptIndex = if (roomType == ConfigConstants.RoomType.Common_Chatroom) 5 else 4
        for (i in vRoomMicInfoList.indices) {
            if (i > interceptIndex) break
            val serverMicInfo = vRoomMicInfoList[i]
            val micInfo = MicInfoBean().apply {
                index = serverMicInfo.mic_index
                serverMicInfo.member?.let { roomUser ->
                    userInfo = serverUser2UiUser(roomUser)
                    ownerTag = !TextUtils.isEmpty(ownerUid) && TextUtils.equals(ownerUid, roomUser.uid)
                    // 有人默认显示音量柱
                    audioVolumeType = ConfigConstants.VolumeType.Volume_None
                }
            }
            micInfo.micStatus = serverMicInfo.status
            micInfoList.add(micInfo)
        }
        return micInfoList
    }

    /**
     * im kv 属性转 micInfo
     * key mic0 value micInfo
     */
    fun convertAttr2MicInfoMap(attributeMap: Map<String, String>): Map<String, VRMicBean> {
        val micInfoMap = mutableMapOf<String, VRMicBean>()
        attributeMap.entries.forEach { entry ->
            GsonTools.toBean<VRMicBean>(entry.value, object : TypeToken<VRMicBean>() {}.type)?.let { attrBean ->
                micInfoMap[entry.key] = attrBean
            }
        }
        return micInfoMap
    }

    /**
     * micInfo map转换ui io.agora.scene.voice.imkit.bean
     */
    fun convertMicInfoMap2UiBean(micInfoMap: Map<String, VRMicBean>, ownerUid: String): Map<Int, MicInfoBean> {
        val micInfoBeanMap = mutableMapOf<Int, MicInfoBean>()
        micInfoMap.entries.forEach { entry ->
            var index = ConfigConstants.MicConstant.micMap[entry.key]
            if (index == null) index = -1
            entry.value.let { attrBean ->
                val micInfo = MicInfoBean().apply {
                    this.index = index
                    this.micStatus = attrBean.status
                    attrBean.member?.let { roomUser ->
                        userInfo = serverUser2UiUser(roomUser)
                        ownerTag = !TextUtils.isEmpty(ownerUid) && TextUtils.equals(ownerUid, roomUser.uid)
                    }
                }
                micInfoBeanMap[index] = micInfo
            }
        }
        return micInfoBeanMap
    }

    fun convertServerRankToUiRank(rankList: List<VRankingMemberBean>): List<RoomRankUserBean> {
        val rankUsers = mutableListOf<RoomRankUserBean>()

        for (i in rankList.indices) {
            // 取前三名
            if (i > 2) break
            serverRoomRankUserToUiBean(rankList[i])?.let { rankUser ->
                rankUsers.add(rankUser)
            }
        }
        return rankUsers
    }
}