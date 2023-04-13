package io.agora.scene.voice.spatial.ui.adapter.viewholder

import android.content.res.AssetManager
import android.graphics.Typeface
import androidx.core.view.isVisible
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemContributionRankingBinding
import io.agora.scene.voice.spatial.model.VoiceRankUserModel
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.utils.ImageTools

class RoomContributionRankingViewHolder(val binding: VoiceSpatialItemContributionRankingBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemContributionRankingBinding, VoiceRankUserModel>(binding) {

    override fun binding(data: VoiceRankUserModel?, selectedIndex: Int) {
        data?.let {
            setRankNumber()
            ImageTools.loadImage(binding.ivAudienceAvatar, it.portrait)
            binding.mtContributionUsername.text = it.name
            binding.mtContributionValue.text = it.amount.toString()
            val mgr: AssetManager = context.assets //得到AssetManager
            val tf: Typeface = Typeface.createFromAsset(mgr, "fonts/RobotoNembersVF.ttf") //根据路径得到Typeface
            binding.mtContributionNumber.typeface = tf //设置字体
        }
    }

    private fun setRankNumber() {
        val num = bindingAdapterPosition + 1
        when (bindingAdapterPosition) {
            0 -> {
                binding.ivContributionNumber.isVisible = true
                binding.ivContributionNumber.setImageResource(R.drawable.voice_icon_bang1)
                binding.mtContributionNumber.text = num.toString()
            }
            1 -> {
                binding.ivContributionNumber.isVisible = true
                binding.ivContributionNumber.setImageResource(R.drawable.voice_icon_room_bang2)
                binding.mtContributionNumber.text = num.toString()
            }
            2 -> {
                binding.ivContributionNumber.isVisible = true
                binding.ivContributionNumber.setImageResource(R.drawable.voice_icon_room_bang3)
                binding.mtContributionNumber.text = num.toString()
            }
            else -> {
                binding.ivContributionNumber.isVisible = false
                binding.mtContributionNumber.text = num.toString()
            }
        }
    }
}