package io.agora.scene.voice.ui.adapter.viewholder

import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.google.android.material.textview.MaterialTextView
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.R
import io.agora.scene.voice.model.AINSModeBean
import io.agora.scene.voice.model.AINSSoundsBean
import io.agora.scene.voice.databinding.*
import io.agora.scene.voice.model.AINSType

class RoomAINSModeViewHolder(binding: VoiceItemRoomAgoraAinsBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoomAgoraAinsBinding, AINSModeBean>(binding) {
    override fun binding(data: AINSModeBean?, selectedIndex: Int) {
        data?.let {
            if (it.type == AINSType.AINS_Default) {
                mBinding.mtTraditionalStrong.isVisible = true
                mBinding.mtTraditionalWeakness.isVisible = true
            } else {
                mBinding.mtTraditionalStrong.isVisible = false
                mBinding.mtTraditionalWeakness.isVisible = false
            }
            mBinding.mtNoiseSuppressionName.text = it.anisName
            mBinding.mtTraditionalStrong.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_Tradition_Strong, view)
            }
            mBinding.mtTraditionalWeakness.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_Tradition_Weakness, view)
            }
            mBinding.mtAiStrong.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_AI_Strong, view)
            }
            mBinding.mtAiWeakness.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_AI_Weakness, view)
            }
            mBinding.mtSettingCustom.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_Custom, view)
            }
            mBinding.mtSettingOff.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_Off, view)
            }

            resetViewDefault(mBinding.mtTraditionalStrong)
            resetViewDefault(mBinding.mtTraditionalWeakness)
            resetViewDefault(mBinding.mtAiStrong)
            resetViewDefault(mBinding.mtAiWeakness)
            resetViewDefault(mBinding.mtSettingCustom)
            resetViewDefault(mBinding.mtSettingOff)
            when (it.anisMode) {
                ConfigConstants.AINSMode.AINS_Tradition_Strong -> setViewHighlight(mBinding.mtTraditionalStrong)
                ConfigConstants.AINSMode.AINS_Tradition_Weakness -> setViewHighlight(mBinding.mtTraditionalWeakness)
                ConfigConstants.AINSMode.AINS_AI_Strong -> setViewHighlight(mBinding.mtAiStrong)
                ConfigConstants.AINSMode.AINS_AI_Weakness -> setViewHighlight(mBinding.mtAiWeakness)
                ConfigConstants.AINSMode.AINS_Custom -> setViewHighlight(mBinding.mtSettingCustom)
                ConfigConstants.AINSMode.AINS_Off -> setViewHighlight(mBinding.mtSettingOff)
            }
        }
    }

    private fun resetViewDefault(textView: MaterialTextView) {
        textView.setBackgroundResource(R.drawable.voice_bg_rect_radius4_grey)
        textView.setTextColor(
            ResourcesCompat.getColor(
                itemView.context.resources, R.color
                    .voice_dark_grey_color_979cbb, null
            )
        )
    }

    private fun setViewHighlight(textView: MaterialTextView) {
        textView.setBackgroundResource(R.drawable.voice_bg_rect_stoke4_blue)
        textView.setTextColor(
            ResourcesCompat.getColor(
                itemView.context.resources,
                R.color.voice_main_color_156ef3,
                null
            )
        )
    }
}

class RoomAINSSoundsViewHolder(binding: VoiceItemRoomAinsAuditionBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoomAinsAuditionBinding, AINSSoundsBean>(binding) {
    override fun binding(data: AINSSoundsBean?, selectedIndex: Int) {
        data?.let {
            mBinding.mtChatroomAinsName.text = it.soundName
            mBinding.mtChatroomAins.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_High, view)
            }
            mBinding.mtChatroomAinsNone.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_Off, view)
            }
            if (TextUtils.isEmpty(it.soundSubName)) {
                mBinding.mtChatroomAinsSubName.text = ""
                mBinding.mtChatroomAinsSubName.isVisible = false
            } else {
                mBinding.mtChatroomAinsSubName.text = it.soundSubName
                mBinding.mtChatroomAinsSubName.isVisible = true
            }
            when (it.soundMode) {
                ConfigConstants.AINSMode.AINS_High -> {
                    setViewHighlight(mBinding.mtChatroomAins)
                    resetViewDefault(mBinding.mtChatroomAinsNone)
                }

                ConfigConstants.AINSMode.AINS_Off -> {
                    setViewHighlight(mBinding.mtChatroomAinsNone)
                    resetViewDefault(mBinding.mtChatroomAins)
                }

                else -> {
                    resetViewDefault(mBinding.mtChatroomAinsNone)
                    resetViewDefault(mBinding.mtChatroomAins)
                }
            }
        }
    }

    private fun resetViewDefault(textView: MaterialTextView) {
        textView.setBackgroundResource(R.drawable.voice_bg_rect_radius4_grey)
        textView.setTextColor(
            ResourcesCompat.getColor(
                itemView.context.resources, R.color
                    .voice_dark_grey_color_979cbb, null
            )
        )
    }

    private fun setViewHighlight(textView: MaterialTextView) {
        textView.setBackgroundResource(R.drawable.voice_bg_rect_stoke4_blue)
        textView.setTextColor(
            ResourcesCompat.getColor(
                itemView.context.resources,
                R.color.voice_main_color_156ef3,
                null
            )
        )
    }
}

class ChatroomAINSTitleViewHolder(binding: VoiceItemRoomAnisTitleBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoomAnisTitleBinding, String>(binding) {
    override fun binding(data: String?, selectedIndex: Int) {
        data?.let {
            mBinding.mtChatroomAinsTitle.text = it
        }
    }
}

class ChatroomAINSContentViewHolder(binding: VoiceItemRoomAnisContentBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoomAnisContentBinding, String>(binding) {
    override fun binding(data: String?, selectedIndex: Int) {
        data?.let {
            mBinding.mtChatroomAinsContent.text = it
        }
    }
}

class ChatroomAINSGapViewHolder(binding: VoiceItemRoomAnisGap10Binding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceItemRoomAnisGap10Binding, String>(binding) {
    override fun binding(data: String?, selectedIndex: Int) {
    }
}