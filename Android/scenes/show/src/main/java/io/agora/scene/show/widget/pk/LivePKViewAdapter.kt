package io.agora.scene.show.widget.pk

import android.view.View
import io.agora.scene.base.GlideApp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLivePkRequestMessageBinding
import io.agora.scene.show.widget.UserItem
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

class LivePKViewAdapter: BindingSingleAdapter<UserItem, ShowLivePkRequestMessageBinding>() {
    override fun onBindViewHolder(
        holder: BindingViewHolder<ShowLivePkRequestMessageBinding>,
        position: Int
    ) {
        val userItem = getItem(position)!!
        val binding = holder.binding
        binding.titleItemBoardcasterStatus.setText("")
        binding.coverBoardcasterIcon.setVisibility(View.VISIBLE)
        GlideApp.with(binding.coverBoardcasterIcon).load("")
            .fallback(R.mipmap.show_default_icon)
            .error(R.mipmap.show_default_icon)
            .transform(CenterCropRoundCornerTransform(10))
            .into(binding.coverBoardcasterIcon);
        if (userItem.isAccepted) {
            binding.btnItemRequest.setEnabled(false)
            binding.btnItemRequest.setText(R.string.show_pking)
            binding.btnItemRequest.setOnClickListener(null)
        } else {
            binding.btnItemRequest.setEnabled(true)
            binding.btnItemRequest.setText(R.string.show_pking)
            binding.btnItemRequest.setOnClickListener {
                onClickListener.onClick(userItem, position)
            }
        }
    }

    private lateinit var onClickListener : OnClickListener
    interface OnClickListener {
        fun onClick(userItem: UserItem, position: Int)
    }
    fun setClickListener(listener : OnClickListener) {
        onClickListener = listener
    }
}