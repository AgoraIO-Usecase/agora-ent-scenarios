package io.agora.scene.voice.ui.adapter.viewholder

import android.content.res.AssetManager
import android.graphics.Typeface
import androidx.core.view.isVisible
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceItemContributionRankingBinding
import io.agora.scene.voice.model.VoiceRankUserModel

class RoomContributionRankingViewHolder(val binding: VoiceItemContributionRankingBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemContributionRankingBinding, VoiceRankUserModel>(binding) {

    override fun binding(data: VoiceRankUserModel?, selectedIndex: Int) {
        data?.let {
            setRankNumber()
            GlideApp.with(binding.ivAudienceAvatar)
                .load(it.portrait)
                .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivAudienceAvatar)

            binding.mtContributionUsername.text = it.name
            binding.mtContributionValue.text = it.amount.toString()
            val mgr: AssetManager = itemView.context.assets 
            val tf: Typeface = Typeface.createFromAsset(mgr, "fonts/RobotoNembersVF.ttf")
            binding.mtContributionNumber.typeface = tf 
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