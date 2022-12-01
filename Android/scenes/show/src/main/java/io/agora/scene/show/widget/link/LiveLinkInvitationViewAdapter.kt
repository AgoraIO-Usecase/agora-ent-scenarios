package io.agora.scene.show.widget.link

import android.view.View
import io.agora.scene.base.GlideApp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLiveLinkInvitationMessageBinding
import io.agora.scene.show.databinding.ShowLiveLinkRequestMessageBinding
import io.agora.scene.show.widget.UserItem
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

class LiveLinkInvitationViewAdapter: BindingSingleAdapter<UserItem, ShowLiveLinkInvitationMessageBinding>() {
    override fun onBindViewHolder(
        holder: BindingViewHolder<ShowLiveLinkInvitationMessageBinding>,
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
            binding.btnItemInvite.setEnabled(false)
            binding.btnItemInvite.setText(R.string.show_is_onseat)
            binding.btnItemInvite.setOnClickListener(null)
        } else {
            binding.btnItemInvite.setEnabled(true)
            binding.btnItemInvite.setText(R.string.show_is_onseat)
            binding.btnItemInvite.setOnClickListener {
                if (userItem != null) {
                    onClickListener.onClick(userItem, position)
                }
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