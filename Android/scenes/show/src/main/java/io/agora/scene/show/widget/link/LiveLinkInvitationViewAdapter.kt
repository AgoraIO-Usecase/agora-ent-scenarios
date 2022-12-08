package io.agora.scene.show.widget.link

import android.view.View
import io.agora.scene.base.GlideApp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLiveLinkInvitationMessageBinding
import io.agora.scene.show.service.ShowRoomRequestStatus
import io.agora.scene.show.service.ShowUser
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

class LiveLinkInvitationViewAdapter: BindingSingleAdapter<ShowUser, ShowLiveLinkInvitationMessageBinding>() {
    override fun onBindViewHolder(
        holder: BindingViewHolder<ShowLiveLinkInvitationMessageBinding>,
        position: Int
    ) {
        val userItem = getItem(position)!!
        val binding = holder.binding
        binding.titleItemUserStatus.setText(userItem.userName)
        binding.coverUserIcon.setVisibility(View.VISIBLE)
         //TODO avatar is 1、2？
        GlideApp.with(binding.coverUserIcon).load(userItem.avatar)
            .fallback(R.mipmap.show_default_icon)
            .error(R.mipmap.show_default_icon)
            .transform(CenterCropRoundCornerTransform(10))
            .into(binding.coverUserIcon);
        if (userItem.status == ShowRoomRequestStatus.accepted.value) {
            binding.btnItemInvite.setEnabled(false)
            binding.btnItemInvite.setText(R.string.show_is_onseat)
            binding.btnItemInvite.setOnClickListener(null)
        } else if (userItem.status == ShowRoomRequestStatus.idle.value) {
            binding.btnItemInvite.setEnabled(true)
            binding.btnItemInvite.setText(R.string.show_application)
            binding.btnItemInvite.setOnClickListener {
                if (userItem != null) {
                    onClickListener.onClick(userItem, position)
                }
            }
        } else if (userItem.status == ShowRoomRequestStatus.waitting.value) {
            binding.btnItemInvite.setEnabled(false)
            binding.btnItemInvite.setText(R.string.show_application_waitting)
            binding.btnItemInvite.setOnClickListener(null)
        } else if (userItem.status == ShowRoomRequestStatus.rejected.value) {
            binding.btnItemInvite.setEnabled(false)
            binding.btnItemInvite.setText(R.string.show_reject_onseat)
            binding.btnItemInvite.setOnClickListener(null)
        }
    }

    private lateinit var onClickListener : OnClickListener
    interface OnClickListener {
        fun onClick(userItem: ShowUser, position: Int)
    }
    fun setClickListener(listener : OnClickListener) {
        onClickListener = listener
    }
}