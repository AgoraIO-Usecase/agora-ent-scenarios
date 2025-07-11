package io.agora.scene.show.widget.link

import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.base.manager.UserManager
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowLiveLinkRequestMessageBinding
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

/**
 * Linking - Linking request list adapter
 */
class LiveLinkRequestViewAdapter: BindingSingleAdapter<ShowMicSeatApply, ShowLiveLinkRequestMessageBinding>() {
    private var isRoomOwner : Boolean = true
    override fun onBindViewHolder(
        holder: BindingViewHolder<ShowLiveLinkRequestMessageBinding>,
        position: Int
    ) {
        val seatApply = getItem(position)!!
        val binding = holder.binding
        binding.titleItemUserStatus.text = seatApply.userName
        binding.coverUserIcon.visibility = View.VISIBLE
        GlideApp.with(binding.coverUserIcon).load(seatApply.avatar)
            .fallback(R.mipmap.show_default_icon)
            .error(R.mipmap.show_default_icon)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.coverUserIcon);
        if (isRoomOwner) {
            binding.userNum.isVisible = false
//            if (seatApply.status == ShowRoomRequestStatus.accepted.value) {
//                binding.btnItemAgreeRequest.isEnabled = false
//                binding.btnItemAgreeRequest.setText(R.string.show_is_onseat)
//                binding.btnItemAgreeRequest.setOnClickListener(null)
//            } else {
                binding.btnItemAgreeRequest.isEnabled = true
                binding.btnItemAgreeRequest.setText(R.string.show_agree_onseat)
                binding.btnItemAgreeRequest.setOnClickListener {
                    onClickListener.onClick(it, seatApply, position)
                }
//            }
        } else {
            if (seatApply.userId == UserManager.getInstance().user.id.toString()) {
                binding.titleItemUserStatus.setTextColor(R.color.show_text)
            }
            binding.userNum.isVisible = true
            binding.userNum.text = (position+1).toString()
            binding.btnItemAgreeRequest.visibility = View.GONE
        }
    }

    private lateinit var onClickListener : OnClickListener
    interface OnClickListener {
        fun onClick(view: View, seatApply: ShowMicSeatApply, position: Int)
    }

    fun setClickListener(listener : OnClickListener) {
        onClickListener = listener
    }

    fun setIsRoomOwner(value: Boolean) {
        isRoomOwner = value
    }
}