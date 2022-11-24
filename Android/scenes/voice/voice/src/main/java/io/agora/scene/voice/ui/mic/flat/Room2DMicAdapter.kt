package io.agora.scene.voice.ui.mic.flat

import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.buddy.tool.DeviceTools
import io.agora.voice.buddy.tool.DeviceTools.dp
import io.agora.scene.voice.annotation.MicClickAction
import io.agora.scene.voice.annotation.MicStatus
import io.agora.scene.voice.databinding.VoiceItemRoom2dMicBinding
import io.agora.scene.voice.service.VoiceMicInfoModel

class Room2DMicAdapter constructor(
    dataList: List<VoiceMicInfoModel>?,
    listener: OnItemClickListener<VoiceMicInfoModel>?,
    viewHolderClass: Class<Room2DMicViewHolder>
) :
    BaseRecyclerViewAdapter<VoiceItemRoom2dMicBinding, VoiceMicInfoModel, Room2DMicViewHolder>(
        dataList, listener, viewHolderClass
    ) {

    override fun onBindViewHolder(holder: Room2DMicViewHolder, position: Int) {
        val layoutParams = holder.mBinding.root.layoutParams
        val size = ((DeviceTools.getDisplaySize().width - 28.dp) / 4).toInt()
        layoutParams.width = size
        holder.mBinding.root.layoutParams = layoutParams
        super.onBindViewHolder(holder, position)
    }

    fun onSeatUpdated(newMicMap: Map<Int, VoiceMicInfoModel>) {
        var needUpdate = false
        // 是否只更新一条
        val onlyOneUpdate = newMicMap.size == 1
        var onlyUpdateItemIndex = -1
        newMicMap.entries.forEach { entry ->
            val index = entry.key
            if (index >= 0 && index < dataList.size) {
                dataList[index] = entry.value
                needUpdate = true
                if (onlyOneUpdate) onlyUpdateItemIndex = index
            }
        }
        if (needUpdate) {
            if (onlyUpdateItemIndex >= 0) {
                notifyItemChanged(onlyUpdateItemIndex)
            } else {
                notifyItemRangeChanged(0,dataList.size,true)
            }
        }
    }

    fun onSeatUpdated(micInfoModel: VoiceMicInfoModel) {
        val index = micInfoModel.micIndex
        if (index>=0 && index<dataList.size){
            dataList[index] = micInfoModel
            notifyItemChanged(index)
        }
    }

    fun updateVolume(index: Int, volume: Int) {
        if (index >= 0 && index < dataList.size) {
            dataList[index].audioVolumeType = volume
            notifyItemChanged(index)
        }
    }
}