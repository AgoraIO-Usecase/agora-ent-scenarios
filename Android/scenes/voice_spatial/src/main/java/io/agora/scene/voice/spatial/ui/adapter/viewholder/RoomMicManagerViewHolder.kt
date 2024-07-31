package io.agora.scene.voice.spatial.ui.adapter.viewholder

import androidx.core.content.res.ResourcesCompat
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoomMicManagerBinding
import io.agora.scene.voice.spatial.model.MicManagerBean

class RoomMicManagerViewHolder(binding: VoiceSpatialItemRoomMicManagerBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoomMicManagerBinding, MicManagerBean>(binding) {
    override fun binding(data: MicManagerBean?, selectedIndex: Int) {
        data?.let {
            mBinding.mtMicManagerOperate.text = it.name
            if (it.enable) {
                mBinding.mtMicManagerOperate.setTextColor(
                    ResourcesCompat.getColor(mBinding.root.context.resources, R.color.voice_main_color_156ef3,null)
                )
            } else {
                mBinding.mtMicManagerOperate.setTextColor(
                    ResourcesCompat.getColor(mBinding.root.context.resources, R.color.voice_dark_grey_color_979cbb,null)
                )
            }
        }
    }
}