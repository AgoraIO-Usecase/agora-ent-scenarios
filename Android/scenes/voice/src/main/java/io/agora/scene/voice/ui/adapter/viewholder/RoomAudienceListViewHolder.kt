package io.agora.scene.voice.ui.adapter.viewholder

import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.voice.databinding.VoiceItemRoomAudienceListBinding
import io.agora.scene.voice.R
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.annotation.MicClickAction

class RoomAudienceListViewHolder constructor(private val binding: VoiceItemRoomAudienceListBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoomAudienceListBinding, VoiceMemberModel>(binding) {

    override fun binding(data: VoiceMemberModel?, selectedIndex: Int) {
        data?.let { audienceInfo ->
            binding.mtAudienceUsername.text = audienceInfo.nickName
            GlideApp.with(binding.ivAudienceAvatar)
                .load(audienceInfo.portrait)
                .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivAudienceAvatar)
            if ( ChatroomIMManager.getInstance().isOwner ){
                binding.mtAudienceAction.apply {
                    isClickable = true
                    text = binding.root.context.getString(R.string.voice_member_count_action_kick)
                    setTextColor(ResourcesCompat.getColor(itemView.context.resources, io.agora.scene.widget.R.color.white,null))
                    setOnClickListener {
                        onItemChildClick(MicClickAction.KickOff, it)
                    }
                }
            }else{
//                binding.mtAudienceAction.apply {
//                    isClickable = true
//                    text = binding.root.context.getString(R.string.voice_member_count_action_kick)
//                    setTextColor(ResourcesTools.getColor(context.resources, R.color.voice_white))
//                }
                binding.mtAudienceAction.visibility = View.GONE
            }
        }
    }
}