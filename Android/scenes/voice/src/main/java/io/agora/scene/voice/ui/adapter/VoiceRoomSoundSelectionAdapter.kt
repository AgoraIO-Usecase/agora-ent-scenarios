package io.agora.scene.voice.ui.adapter

import android.view.View
import android.widget.ImageView
import io.agora.voice.common.utils.DeviceTools
import io.agora.voice.common.utils.ResourcesTools
import io.agora.scene.voice.model.SoundSelectionBean
import androidx.appcompat.widget.LinearLayoutCompat
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceItemSoundSelectionBinding
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener

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
                    LinearLayoutCompat.LayoutParams(DeviceTools.dp2px(context, 20f), DeviceTools.dp2px(context, 20f))
                marginLayoutParams.rightMargin = DeviceTools.dp2px(context, 10f)
                imageView.setImageResource(customerBean.avatar)
                imageView.layoutParams = marginLayoutParams
                if (binding.llSoundCustomerUsage.childCount < bean.customer.size) {
                    binding.llSoundCustomerUsage.addView(imageView)
                }
            }
            if (selectedPosition == bindingAdapterPosition) {
                binding.ivSoundSelected.visibility = View.VISIBLE
                binding.mcvSoundSelectionContent.strokeColor =
                    ResourcesTools.getColor(context.resources, R.color.voice_color_009fff, null)
            } else {
                binding.ivSoundSelected.visibility = View.GONE
                binding.mcvSoundSelectionContent.strokeColor =
                    ResourcesTools.getColor(context.resources, R.color.voice_color_d8d8d8, null)
            }
        }
    }
}