package io.agora.scene.voice.ui.adapter.viewholder

import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceItemHandsRaisedBinding
import io.agora.scene.voice.model.VoiceMemberModel

class RoomMemberCountViewHolder (binding: VoiceItemHandsRaisedBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemHandsRaisedBinding, VoiceMemberModel>(binding){
    override fun binding(data: VoiceMemberModel?, selectedIndex: Int) {
        GlideApp.with(mBinding.ivAudienceAvatar)
            .load(data?.portrait)
            .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(mBinding.ivAudienceAvatar)
        mBinding.mtAudienceUsername.text = data?.nickName
        mBinding.mtAudienceAction.text = itemView.context.resources.getString(R.string.voice_member_count_action_kick)
        mBinding.mtAudienceAction.setOnClickListener {
            onItemChildClick(data, it)
        }
    }
}