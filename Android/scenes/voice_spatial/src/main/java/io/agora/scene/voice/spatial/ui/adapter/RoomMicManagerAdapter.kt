package io.agora.scene.voice.spatial.ui.adapter

import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoomMicManagerBinding
import io.agora.scene.voice.spatial.model.MicManagerBean
import io.agora.scene.voice.spatial.ui.adapter.viewholder.RoomMicManagerViewHolder
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.DeviceTools

class RoomMicManagerAdapter constructor(
    dataList: List<MicManagerBean>,
    listener: OnItemClickListener<MicManagerBean>?,
    viewHolderClass: Class<RoomMicManagerViewHolder>
) :
    BaseRecyclerViewAdapter<VoiceSpatialItemRoomMicManagerBinding, MicManagerBean, RoomMicManagerViewHolder>(
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