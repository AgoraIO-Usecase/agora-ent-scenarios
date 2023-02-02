package io.agora.scene.voice.spatial.ui.adapter.viewholder

import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoom2dBotMicBinding
import io.agora.scene.voice.spatial.model.BotMicInfoBean
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter

class Room2DBotMicViewHolder(binding: VoiceSpatialItemRoom2dBotMicBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoom2dBotMicBinding, BotMicInfoBean>(binding) {
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