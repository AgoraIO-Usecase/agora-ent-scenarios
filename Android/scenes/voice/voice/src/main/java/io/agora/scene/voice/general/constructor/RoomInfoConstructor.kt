package io.agora.scene.voice.general.constructor

import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import io.agora.scene.voice.bean.RoomKitBean
import io.agora.scene.voice.service.VoiceBuddyFactory
import io.agora.scene.voice.service.VoiceMicInfoModel
import io.agora.scene.voice.service.VoiceRoomModel
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
     * 扩展麦位数据
     */
    fun extendMicInfoList(vMicInfoList: List<VoiceMicInfoModel>, roomType: Int, ownerUid: String): List<VoiceMicInfoModel> {
        val micInfoList = mutableListOf<VoiceMicInfoModel>()
        val interceptIndex = if (roomType == ConfigConstants.RoomType.Common_Chatroom) 5 else 4
        for (i in vMicInfoList.indices) {
            if (i > interceptIndex) break
            val serverMicInfo = vMicInfoList[i]
            val micInfo = VoiceMicInfoModel().apply {
                micIndex = serverMicInfo.micIndex
                serverMicInfo.member?.let { roomUser ->
                    member = roomUser
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
                        ownerTag = !TextUtils.isEmpty(ownerUid) && TextUtils.equals(ownerUid, roomUser.userId)
                    }
                }
                micInfoBeanMap[index] = micInfo
            }
        }
        return micInfoBeanMap
    }
}