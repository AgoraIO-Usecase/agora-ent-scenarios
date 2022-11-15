package io.agora.scene.voice.ui.mic.flat

import io.agora.scene.voice.bean.BotMicInfoBean
import io.agora.scene.voice.bean.MicInfoBean
import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.baseui.adapter.OnItemChildClickListener
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.buddy.config.ConfigConstants
import io.agora.voice.buddy.tool.DeviceTools
import io.agora.voice.buddy.tool.DeviceTools.dp
import io.agora.secnceui.annotation.MicStatus
import io.agora.scene.voice.databinding.VoiceItemRoom2dBotMicBinding

class Room2DBotMicAdapter constructor(
    dataList: List<BotMicInfoBean>,
    listener: OnItemClickListener<BotMicInfoBean>?,
    childListener: OnItemChildClickListener<BotMicInfoBean>?,
    viewHolderClass: Class<Room2DBotMicViewHolder>
) :
    BaseRecyclerViewAdapter<VoiceItemRoom2dBotMicBinding, BotMicInfoBean, Room2DBotMicViewHolder>(
        dataList, listener, childListener, viewHolderClass
    ) {

    override fun onBindViewHolder(holder: Room2DBotMicViewHolder, position: Int) {
        val layoutParams = holder.mBinding.root.layoutParams
        val size = ((DeviceTools.getDisplaySize().width - 28.dp) / 2).toInt()
        layoutParams.width = size
        holder.mBinding.root.layoutParams = layoutParams
        super.onBindViewHolder(holder, position)
    }

    fun activeBot(active: Boolean) {
        if (active) {
            dataList[0].blueBot.micStatus = MicStatus.BotActivated
            dataList[0].redBot.micStatus = MicStatus.BotActivated
            dataList[0].blueBot.audioVolumeType = ConfigConstants.VolumeType.Volume_None
            dataList[0].redBot.audioVolumeType = ConfigConstants.VolumeType.Volume_None
        } else {
            dataList[0].blueBot.micStatus = MicStatus.BotInactive
            dataList[0].redBot.micStatus = MicStatus.BotInactive
            dataList[0].blueBot.audioVolumeType = ConfigConstants.VolumeType.Volume_None
            dataList[0].redBot.audioVolumeType = ConfigConstants.VolumeType.Volume_None
        }
        notifyItemChanged(0)
    }

    /**更新音量*/
    fun updateVolume(speaker: Int, volume: Int) {
        when (speaker) {
            ConfigConstants.BotSpeaker.BotBlue -> {
                dataList[0].blueBot.audioVolumeType = volume
            }
            ConfigConstants.BotSpeaker.BotRed -> {
                dataList[0].redBot.audioVolumeType = volume
            }
            else -> {
                dataList[0].blueBot.audioVolumeType = volume
                dataList[0].redBot.audioVolumeType = volume
            }
        }
        notifyItemChanged(0)
    }

    fun receiverAttributeMap(newMicMap: Map<Int, MicInfoBean>) {
        if (newMicMap.containsKey(ConfigConstants.MicConstant.KeyIndex6)) {
            val value = newMicMap[ConfigConstants.MicConstant.KeyIndex6]
            activeBot(value?.micStatus == MicStatus.BotActivated)
        }
    }
}