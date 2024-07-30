package io.agora.scene.voice.model.constructor

import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.scene.voice.model.chatroomId
import io.agora.scene.voice.model.clickCount
import io.agora.scene.voice.model.isPrivate
import io.agora.scene.voice.model.memberCount
import io.agora.scene.voice.model.roomPassword
import io.agora.scene.voice.model.soundEffect
import io.agora.voice.common.constant.ConfigConstants

/**
 * @author create by zhangwei03
 */
object RoomInfoConstructor {

    fun VoiceRoomModel.convertByRoomInfo(roomInfo: AUIRoomInfo) {
        owner = VoiceMemberModel().apply {
            userId = roomInfo.roomOwner?.userId ?: ""
            chatUid = roomInfo.roomOwner?.userId ?: ""
            nickName = roomInfo.roomOwner?.userName ?: ""
            portrait = roomInfo.roomOwner?.userAvatar ?: ""
            rtcUid = roomInfo.roomOwner?.userId?.toIntOrNull() ?: 0
        }
        roomId = roomInfo.roomId
        isPrivate = roomInfo.isPrivate()
        memberCount = roomInfo.memberCount()
        clickCount = roomInfo.clickCount()
        roomName = roomInfo.roomName
        soundEffect = roomInfo.soundEffect()
        chatroomId = roomInfo.chatroomId()
        createdAt = roomInfo.createTime
        roomPassword = roomInfo.roomPassword()
    }

    /**
     * 扩展麦位数据
     */
    fun extendMicInfoList(
        vMicInfoList: List<VoiceMicInfoModel>,
        ownerUid: String
    ): List<VoiceMicInfoModel> {
        val micInfoList = mutableListOf<VoiceMicInfoModel>()
        val interceptIndex = 5
        for (i in vMicInfoList.indices) {
            if (i > interceptIndex) break
            val serverMicInfo = vMicInfoList[i]
            val micInfo = VoiceMicInfoModel().apply {
                micIndex = serverMicInfo.micIndex
                serverMicInfo.member?.let { roomUser ->
                    member = roomUser
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
     * 扩展麦位数据
     */
    fun extendMicInfoMap(micInfoMap: Map<String, VoiceMicInfoModel>, ownerUid: String): Map<Int, VoiceMicInfoModel> {
        val micInfoBeanMap = mutableMapOf<Int, VoiceMicInfoModel>()
        micInfoMap.entries.forEach { entry ->
            var index = ConfigConstants.MicConstant.micMap[entry.key]
            if (index == null) index = -1
            entry.value.let { attrBean ->
                val micInfo = VoiceMicInfoModel().apply {
                    this.micIndex = index
                    this.micStatus = attrBean.micStatus
                    attrBean.member?.let { roomUser ->
                        member = roomUser
                    }
                }
                micInfoBeanMap[index] = micInfo
            }
        }
        return micInfoBeanMap
    }
}