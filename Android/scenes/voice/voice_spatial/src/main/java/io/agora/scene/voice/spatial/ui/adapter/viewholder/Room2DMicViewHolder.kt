package io.agora.scene.voice.spatial.ui.adapter.viewholder

import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoom2dMicBinding
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter

class Room2DMicViewHolder(binding: VoiceSpatialItemRoom2dMicBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoom2dMicBinding, VoiceMicInfoModel>(binding) {
    override fun binding(data: VoiceMicInfoModel?, selectedIndex: Int) {
        data?.let {
            mBinding.mic2dView.binding(it)
        }
    }
}