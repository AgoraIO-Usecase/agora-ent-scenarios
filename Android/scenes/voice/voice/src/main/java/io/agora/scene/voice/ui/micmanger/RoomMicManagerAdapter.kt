package io.agora.scene.voice.ui.micmanger

import io.agora.scene.voice.bean.MicManagerBean
import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.buddy.tool.DeviceTools
import io.agora.scene.voice.databinding.VoiceItemRoomMicManagerBinding

class RoomMicManagerAdapter constructor(
    dataList: List<MicManagerBean>,
    listener: OnItemClickListener<MicManagerBean>?,
    viewHolderClass: Class<RoomMicManagerViewHolder>
) :
    BaseRecyclerViewAdapter<VoiceItemRoomMicManagerBinding, MicManagerBean, RoomMicManagerViewHolder>(
        dataList, listener, viewHolderClass
    ) {

    override fun onBindViewHolder(holder: RoomMicManagerViewHolder, position: Int) {

        val layoutParams = holder.mBinding.root.layoutParams
        when (dataList.size) {
            1 -> {
                layoutParams.width = DeviceTools.getDisplaySize().width
            }
            2 -> {
                layoutParams.width = DeviceTools.getDisplaySize().width/2
            }
            else -> {
                layoutParams.width = DeviceTools.getDisplaySize().width / 3
            }
        }
        holder.mBinding.root.layoutParams = layoutParams
        super.onBindViewHolder(holder, position)
    }
}