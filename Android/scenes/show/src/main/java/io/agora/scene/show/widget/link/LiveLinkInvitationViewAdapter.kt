package io.agora.scene.show.widget.link

import android.view.View
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLiveLinkInvitationMessageBinding
import io.agora.scene.show.service.ShowInteractionStatus
import io.agora.scene.show.service.ShowUser
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class LiveLinkInvitationViewAdapter: BindingSingleAdapter<ShowUser, ShowLiveLinkInvitationMessageBinding>() {
    override fun onBindViewHolder(
        holder: BindingViewHolder<ShowLiveLinkInvitationMessageBinding>,
        position: Int
    ) {
        val userItem = getItem(position)!!
        val binding = holder.binding
        binding.titleItemUserStatus.text = userItem.userName
        binding.coverUserIcon.visibility = View.VISIBLE
        GlideApp.with(binding.coverUserIcon).load(userItem.avatar)
            .fallback(R.mipmap.show_default_icon)
            .error(R.mipmap.show_default_icon)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.coverUserIcon)
        when (userItem.status) {
            ShowInteractionStatus.linking -> {
                binding.btnItemInvite.isEnabled = false
                binding.btnItemInvite.setText(R.string.show_is_onseat)
                binding.btnItemInvite.setOnClickListener(null)
            }
            else -> {
                binding.btnItemInvite.isEnabled = true
                binding.btnItemInvite.setText(R.string.show_application)
                binding.btnItemInvite.setOnClickListener {
                    onClickListener.onClick(it, userItem, position)
                }
            }
        }
    }

    private lateinit var onClickListener : OnClickListener
    interface OnClickListener {
        fun onClick(view: View, userItem: ShowUser, position: Int)
    }
    fun setClickListener(listener : OnClickListener) {
        onClickListener = listener
    }
}