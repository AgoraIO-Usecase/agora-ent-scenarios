package io.agora.scene.voice.ui.adapter.viewholder

import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.scene.voice.databinding.VoiceItemRoom2dMicBinding
import io.agora.scene.voice.model.VoiceMicInfoModel

class Room2DMicViewHolder(binding: VoiceItemRoom2dMicBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoom2dMicBinding, VoiceMicInfoModel>(binding) {
    override fun binding(data: VoiceMicInfoModel?, selectedIndex: Int) {
        data?.let {
            mBinding.mic2dView.binding(it)
        }
    }
}