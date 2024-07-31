package io.agora.scene.voice.spatial.ui.adapter

import android.content.res.Resources
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoomMicManagerBinding
import io.agora.scene.voice.spatial.model.MicManagerBean
import io.agora.scene.voice.spatial.ui.adapter.viewholder.RoomMicManagerViewHolder

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
                layoutParams.width = Resources.getSystem().displayMetrics.widthPixels
            }
            2 -> {
                layoutParams.width = Resources.getSystem().displayMetrics.widthPixels /2
            }
            else -> {
                layoutParams.width = Resources.getSystem().displayMetrics.widthPixels / 3
            }
        }
        holder.mBinding.root.layoutParams = layoutParams
        super.onBindViewHolder(holder, position)
    }
}