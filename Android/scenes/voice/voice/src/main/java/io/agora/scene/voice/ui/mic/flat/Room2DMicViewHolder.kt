package io.agora.scene.voice.ui.mic.flat

import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
import io.agora.scene.voice.databinding.VoiceItemRoom2dMicBinding
import io.agora.scene.voice.service.VoiceMicInfoModel

class Room2DMicViewHolder(binding: VoiceItemRoom2dMicBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoom2dMicBinding, VoiceMicInfoModel>(binding) {
    override fun binding(data: VoiceMicInfoModel?, selectedIndex: Int) {
        data?.let {
            mBinding.mic2dView.binding(it)
        }
    }
}