package io.agora.scene.voice.ui.adapter.viewholder

import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.voice.model.BotMicInfoBean
import io.agora.scene.voice.databinding.VoiceItemRoom2dBotMicBinding

class Room2DBotMicViewHolder(binding: VoiceItemRoom2dBotMicBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoom2dBotMicBinding, BotMicInfoBean>(binding) {
    override fun binding(data: BotMicInfoBean?, selectedIndex: Int) {
        data?.let {
            mBinding.micBlueBot.binding(it.blueBot)
            mBinding.micBlueRed.binding(it.redBot)
            mBinding.micBlueBot.setOnClickListener { view ->
                onItemChildClick(it.blueBot, view)
            }
            mBinding.micBlueRed.setOnClickListener { view ->
                onItemChildClick(it.redBot, view)
            }
        }
    }
}