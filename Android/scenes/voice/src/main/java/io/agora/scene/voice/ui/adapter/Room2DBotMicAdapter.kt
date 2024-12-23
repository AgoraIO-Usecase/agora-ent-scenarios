package io.agora.scene.voice.ui.adapter

import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.OnItemChildClickListener
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.displayWidth
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.model.BotMicInfoBean
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.model.annotation.MicStatus
import io.agora.scene.voice.databinding.VoiceItemRoom2dBotMicBinding
import io.agora.scene.voice.ui.adapter.viewholder.Room2DBotMicViewHolder

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
        val size = ((displayWidth() - 28.dp) / 2).toInt()
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
}