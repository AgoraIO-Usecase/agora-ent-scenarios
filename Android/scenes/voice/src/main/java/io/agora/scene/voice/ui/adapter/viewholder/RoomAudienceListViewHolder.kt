package io.agora.scene.voice.ui.adapter.viewholder

import android.view.View
import io.agora.scene.voice.databinding.VoiceItemRoomAudienceListBinding
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.utils.ResourcesTools
import io.agora.scene.voice.R
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.annotation.MicClickAction
import io.agora.voice.common.utils.ImageTools

class RoomAudienceListViewHolder constructor(private val binding: VoiceItemRoomAudienceListBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoomAudienceListBinding, VoiceMemberModel>(binding) {

    override fun binding(data: VoiceMemberModel?, selectedIndex: Int) {
        data?.let { audienceInfo ->
            binding.mtAudienceUsername.text = audienceInfo.nickName
            ImageTools.loadImage(binding.ivAudienceAvatar, audienceInfo.portrait)
            if ( ChatroomIMManager.getInstance().isOwner ){
                binding.mtAudienceAction.apply {
                    isClickable = true
                    text = binding.root.context.getString(R.string.voice_member_count_action_kick)
                    setTextColor(ResourcesTools.getColor(context.resources, R.color.voice_white))
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