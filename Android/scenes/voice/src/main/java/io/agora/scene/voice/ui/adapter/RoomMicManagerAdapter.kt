package io.agora.scene.voice.ui.adapter

import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.displayWidth
import io.agora.scene.voice.model.MicManagerBean
import io.agora.scene.voice.databinding.VoiceItemRoomMicManagerBinding
import io.agora.scene.voice.ui.adapter.viewholder.RoomMicManagerViewHolder

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
                layoutParams.width = displayWidth()
            }
            2 -> {
                layoutParams.width = displayWidth()/2
            }
            else -> {
                layoutParams.width = displayWidth() / 3
            }
        }
        holder.mBinding.root.layoutParams = layoutParams
        super.onBindViewHolder(holder, position)
    }
}