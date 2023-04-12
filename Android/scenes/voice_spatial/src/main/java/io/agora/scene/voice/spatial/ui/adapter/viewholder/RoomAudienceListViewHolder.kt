package io.agora.scene.voice.spatial.ui.adapter.viewholder

import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoomAudienceListBinding
import io.agora.scene.voice.spatial.model.VoiceMemberModel
import io.agora.scene.voice.spatial.model.annotation.MicClickAction
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.utils.ImageTools
import io.agora.voice.common.utils.ResourcesTools

class RoomAudienceListViewHolder constructor(private val binding: VoiceSpatialItemRoomAudienceListBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoomAudienceListBinding, VoiceMemberModel>(binding) {

    override fun binding(data: VoiceMemberModel?, selectedIndex: Int) {
        data?.let { audienceInfo ->
            binding.mtAudienceUsername.text = audienceInfo.nickName
            ImageTools.loadImage(binding.ivAudienceAvatar, audienceInfo.portrait)
            if (audienceInfo.micIndex == -1) {
                // 不在麦位上
                binding.mtAudienceAction.apply {
                    isClickable = true
                    text = binding.root.context.getString(R.string.voice_room_invite)
                    setTextColor(ResourcesTools.getColor(context.resources, io.agora.voice.common.R.color.voice_white))
                    setBackgroundResource(R.drawable.voice_bg_rect_radius20_gradient_blue)
                    setOnClickListener {
                        onItemChildClick(MicClickAction.Invite, it)
                    }
                }
            } else {
                // 在麦位上
                binding.mtAudienceAction.apply {
                    isClickable = true
                    text = binding.root.context.getString(R.string.voice_room_kickoff)
                    setTextColor(ResourcesTools.getColor(context.resources, R.color.voice_main_color_156ef3))
                    setBackgroundResource(0)
                    setOnClickListener {
                        onItemChildClick(MicClickAction.KickOff, it)
                    }
                }
            }
        }
    }
}