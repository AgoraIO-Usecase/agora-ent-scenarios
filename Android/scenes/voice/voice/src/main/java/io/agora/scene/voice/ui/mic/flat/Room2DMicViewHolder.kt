package io.agora.scene.voice.ui.mic.flat

import io.agora.scene.voice.bean.MicInfoBean
import io.agora.voice.baseui.adapter.BaseRecyclerViewAdapter
import io.agora.scene.voice.databinding.VoiceItemRoom2dMicBinding

class Room2DMicViewHolder(binding: VoiceItemRoom2dMicBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoom2dMicBinding, MicInfoBean>(binding) {
    override fun binding(data: MicInfoBean?, selectedIndex: Int) {
        data?.let {
            mBinding.mic2dView.binding(it)
        }
    }
}