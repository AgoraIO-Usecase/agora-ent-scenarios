package io.agora.scene.show.widget.link

import android.view.View
import io.agora.scene.base.GlideApp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLiveLinkRequestMessageBinding
import io.agora.scene.show.widget.UserItem
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

/**
 * 连麦-连麦申请列表适配器
 */
class LiveLinkRequestViewAdapter: BindingSingleAdapter<UserItem, ShowLiveLinkRequestMessageBinding>() {
    override fun onBindViewHolder(
        holder: BindingViewHolder<ShowLiveLinkRequestMessageBinding>,
        position: Int
    ) {
        val userItem = getItem(position)!!
        val binding = holder.binding
        binding.titleItemUserStatus.setText("")
        binding.coverUserIcon.setVisibility(View.VISIBLE)
        GlideApp.with(binding.coverUserIcon).load("")
            .fallback(R.mipmap.show_default_icon)
            .error(R.mipmap.show_default_icon)
            .transform(CenterCropRoundCornerTransform(10))
            .into(binding.coverUserIcon);
        if (userItem.isAccepted) {
            binding.btnItemAgreeRequest.setEnabled(false)
            binding.btnItemAgreeRequest.setText(R.string.show_is_onseat)
            binding.btnItemAgreeRequest.setOnClickListener(null)
        } else {
            binding.btnItemAgreeRequest.setEnabled(true)
            binding.btnItemAgreeRequest.setText(R.string.show_is_onseat)
            binding.btnItemAgreeRequest.setOnClickListener {
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