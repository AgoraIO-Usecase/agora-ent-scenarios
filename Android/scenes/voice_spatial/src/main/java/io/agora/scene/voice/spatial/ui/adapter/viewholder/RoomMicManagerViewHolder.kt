package io.agora.scene.voice.spatial.ui.adapter.viewholder

import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoomMicManagerBinding
import io.agora.scene.voice.spatial.model.MicManagerBean
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.utils.ResourcesTools

class RoomMicManagerViewHolder(binding: VoiceSpatialItemRoomMicManagerBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoomMicManagerBinding, MicManagerBean>(binding) {
    override fun binding(data: MicManagerBean?, selectedIndex: Int) {
        data?.let {
            mBinding.mtMicManagerOperate.text = it.name
            if (it.enable) {
                mBinding.mtMicManagerOperate.setTextColor(
                    ResourcesTools.getColor(context.resources, R.color.voice_main_color_156ef3)
                )
            } else {
                mBinding.mtMicManagerOperate.setTextColor(
                    ResourcesTools.getColor(context.resources, R.color.voice_dark_grey_color_979cbb)
                )
            }
        }
    }
}