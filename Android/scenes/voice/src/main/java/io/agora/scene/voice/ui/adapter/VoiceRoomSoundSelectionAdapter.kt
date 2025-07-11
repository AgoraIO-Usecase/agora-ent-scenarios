package io.agora.scene.voice.ui.adapter

import android.view.View
import android.widget.ImageView
import io.agora.scene.voice.model.SoundSelectionBean
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.dp
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceItemSoundSelectionBinding

class VoiceRoomSoundSelectionAdapter constructor(
    dataList: List<SoundSelectionBean>,
    listener: OnItemClickListener<SoundSelectionBean>?,
    viewHolderClass: Class<SoundSelectViewHolder>
) : BaseRecyclerViewAdapter<VoiceItemSoundSelectionBinding, SoundSelectionBean, VoiceRoomSoundSelectionAdapter.SoundSelectViewHolder>(
    dataList, listener, viewHolderClass
) {

    init {
        selectedIndex = 0
    }

    fun setSelectedPosition(position: Int) {
        selectedIndex = position
        notifyDataSetChanged()
    }

    class SoundSelectViewHolder constructor(private val binding: VoiceItemSoundSelectionBinding) :
        BaseViewHolder<VoiceItemSoundSelectionBinding, SoundSelectionBean>(binding) {

        override fun binding(soundSelectionBean: SoundSelectionBean?, selectedIndex: Int) {
            soundSelectionBean?.let {
                setData( it, selectedIndex)
            }
        }

        private fun setData(bean: SoundSelectionBean, selectedPosition: Int) {
            binding.soundName.text = bean.soundName
            binding.soundDesc.text = bean.soundIntroduce
            val context = binding.item.context
            bean.customer?.forEach { customerBean ->
                val imageView = ImageView(context)
                val marginLayoutParams =
                    LinearLayoutCompat.LayoutParams(20.dp.toInt(), 20.dp.toInt())
                marginLayoutParams.rightMargin = 10.dp.toInt()
                imageView.setImageResource(customerBean.avatar)
                imageView.layoutParams = marginLayoutParams
                if (binding.llSoundCustomerUsage.childCount < bean.customer.size) {
                    binding.llSoundCustomerUsage.addView(imageView)
                }
            }
            if (selectedPosition == bindingAdapterPosition) {
                binding.ivSoundSelected.visibility = View.VISIBLE
                binding.mcvSoundSelectionContent.strokeColor =
                    ResourcesCompat.getColor(context.resources, R.color.voice_color_009fff, null)
            } else {
                binding.ivSoundSelected.visibility = View.GONE
                binding.mcvSoundSelectionContent.strokeColor =
                    ResourcesCompat.getColor(context.resources, R.color.voice_color_d8d8d8, null)
            }
        }
    }
}