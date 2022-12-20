package io.agora.scene.show.widget.pk

import android.view.View
import io.agora.scene.base.GlideApp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLivePkRequestMessageBinding
import io.agora.scene.show.service.ShowInteractionStatus
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

class LivePKViewAdapter: BindingSingleAdapter<ShowRoomDetailModel, ShowLivePkRequestMessageBinding>() {
    override fun onBindViewHolder(
        holder: BindingViewHolder<ShowLivePkRequestMessageBinding>,
        position: Int
    ) {
        val roomItem = getItem(position)!!
        val binding = holder.binding
        binding.titleItemBoardcasterStatus.text = roomItem.ownerName
        binding.coverBoardcasterIcon.visibility = View.VISIBLE
        GlideApp.with(binding.coverBoardcasterIcon).load(roomItem.ownerAvater)
            .fallback(R.mipmap.show_default_icon)
            .error(R.mipmap.show_default_icon)
            .transform(CenterCropRoundCornerTransform(10))
            .into(binding.coverBoardcasterIcon)
        when (roomItem.interactStatus) {
            ShowInteractionStatus.idle.value -> {
                binding.btnItemRequest.isEnabled = true
                binding.btnItemRequest.setText(R.string.show_application)
                binding.btnItemRequest.setOnClickListener {
                    onClickListener.onClick(roomItem, position)
                }
            }
            ShowInteractionStatus.pking.value -> {
                binding.btnItemRequest.isEnabled = false
                binding.btnItemRequest.setText(R.string.show_pking)
                binding.btnItemRequest.setOnClickListener(null)
            }
            ShowInteractionStatus.onSeat.value -> {
                binding.btnItemRequest.isEnabled = false
                binding.btnItemRequest.setText(R.string.show_linking)
                binding.btnItemRequest.setOnClickListener(null)
            }
        }
    }

    private lateinit var onClickListener : OnClickListener
    interface OnClickListener {
        fun onClick(roomItem: ShowRoomDetailModel, position: Int)
    }
    fun setClickListener(listener : OnClickListener) {
        onClickListener = listener
    }
}