package io.agora.scene.voice.spatial.model.constructor

import android.text.TextUtils
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.global.VSpatialCenter
import io.agora.scene.voice.spatial.model.RoomKitBean
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.VoiceRoomModel

/**
 * @author create by zhangwei03
 */
object RoomInfoConstructor {

    /** VoiceRoomModel convert RoomKitBean*/
    fun RoomKitBean.convertByVoiceRoomModel(voiceRoomModel: VoiceRoomModel) {
        roomId = voiceRoomModel.roomId
        channelId = voiceRoomModel.channelId
        ownerId = voiceRoomModel.owner?.userId ?: ""
        isOwner = curUserIsHost(voiceRoomModel.owner?.userId)
        soundEffect = voiceRoomModel.soundEffect
    }

    /** Check if you are a host */
    private fun curUserIsHost(ownerId: String?): Boolean {
        return TextUtils.equals(ownerId, VSpatialCenter.userId)
    }

    /**
     * 扩展麦位数据
     */
    fun extendMicInfoList(vMicInfoList: List<VoiceMicInfoModel>, roomType: Int, ownerUid: String): List<VoiceMicInfoModel> {
        val micInfoList = mutableListOf<VoiceMicInfoModel>()
        val interceptIndex =  4
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