package io.agora.scene.voice.ui.adapter.viewholder

import androidx.core.content.res.ResourcesCompat
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.voice.R
import io.agora.scene.voice.model.MicManagerBean
import io.agora.scene.voice.databinding.VoiceItemRoomMicManagerBinding

class RoomMicManagerViewHolder(binding: VoiceItemRoomMicManagerBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoomMicManagerBinding, MicManagerBean>(binding) {
    override fun binding(data: MicManagerBean?, selectedIndex: Int) {
        data?.let {
            mBinding.mtMicManagerOperate.text = it.name
            if (it.enable) {
                mBinding.mtMicManagerOperate.setTextColor(
                    ResourcesCompat.getColor(mBinding.root.context.resources, R.color.voice_main_color_156ef3, null)
                )
            } else {
                mBinding.mtMicManagerOperate.setTextColor(
                    ResourcesCompat.getColor(
                        mBinding.root.context.resources,
                        R.color.voice_dark_grey_color_979cbb,
                        null
                    )
                )
            }
        }
    }
}