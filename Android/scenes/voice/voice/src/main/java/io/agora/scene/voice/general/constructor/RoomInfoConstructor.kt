package io.agora.scene.voice.general.constructor

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import io.agora.scene.voice.bean.MicInfoBean
import io.agora.scene.voice.bean.RoomKitBean
import io.agora.scene.voice.bean.RoomRankUserBean
import io.agora.scene.voice.bean.RoomUserInfoBean
import io.agora.scene.voice.service.VoiceBuddyFactory
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

    fun RoomKitBean.convertByRoomInfo(roomInfo: VRoomBean.RoomsBean) {
        roomId = roomInfo.room_id ?: ""
        chatroomId = roomInfo.chatroom_id ?: ""
        channelId = roomInfo.channel_id ?: ""
        ownerId = roomInfo.owner?.uid ?: ""
        roomType = roomInfo.type
        isOwner = curUserIsHost(roomInfo.owner?.uid)
        soundEffect = roomInfo.soundSelection
    }

    fun RoomKitBean.convertByRoomDetailInfo(roomDetails: VRoomInfoBean.VRoomDetail) {
        roomId = roomDetails.room_id ?: ""
        chatroomId = roomDetails.chatroom_id ?: ""
        channelId = roomDetails.channel_id ?: ""
        ownerId = roomDetails.owner?.uid ?: ""
        roomType = roomDetails.type
        isOwner = curUserIsHost(roomDetails.owner?.uid)
        soundEffect = roomDetails.soundSelection
    }

    /** Check if you are a host */
    private fun curUserIsHost(ownerId: String?): Boolean {
        return TextUtils.equals(ownerId, VoiceBuddyFactory.get().getVoiceBuddy().userId())
    }

    /**
     * 服务端roomInfo io.agora.voice.imkit.bean 转 ui io.agora.voice.imkit.bean
     */
    fun serverRoomInfo2UiRoomInfo(roomDetail: VRoomInfoBean.VRoomDetail): io.agora.scene.voice.bean.RoomInfoBean {
        val roomInfo = io.agora.scene.voice.bean.RoomInfoBean().apply {
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
     * 服务端roomInfo io.agora.voice.imkit.bean 转 麦位 ui io.agora.voice.imkit.bean
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
     * micInfo map转换ui io.agora.voice.imkit.bean
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