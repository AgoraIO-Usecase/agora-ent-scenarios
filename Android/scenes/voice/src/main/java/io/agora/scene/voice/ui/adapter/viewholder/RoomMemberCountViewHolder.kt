package io.agora.scene.voice.ui.adapter.viewholder

import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceItemHandsRaisedBinding
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.utils.ImageTools.loadImage

class RoomMemberCountViewHolder (binding: VoiceItemHandsRaisedBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemHandsRaisedBinding, VoiceMemberModel>(binding){
    override fun binding(data: VoiceMemberModel?, selectedIndex: Int) {
        loadImage(mBinding.ivAudienceAvatar, data?.portrait)
        mBinding.mtAudienceUsername.text = data?.nickName
        mBinding.mtAudienceAction.text = context.resources.getString(R.string.voice_member_count_action_kick)
        mBinding.mtAudienceAction.setOnClickListener {
            onItemChildClick(data, it)
        }
    }
}