package io.agora.scene.voice.spatial.ui.adapter.viewholder

import android.text.TextUtils
import androidx.core.view.isVisible
import com.google.android.material.textview.MaterialTextView
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.*
import io.agora.scene.voice.spatial.model.AINSModeBean
import io.agora.scene.voice.spatial.model.AINSSoundsBean
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.utils.ResourcesTools

class RoomAINSModeViewHolder(binding: VoiceSpatialItemRoomAgoraAinsBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoomAgoraAinsBinding, AINSModeBean>(binding) {
    override fun binding(data: AINSModeBean?, selectedIndex: Int) {
        data?.let {
            mBinding.mtNoiseSuppressionName.text = it.anisName
            mBinding.mtChatroomHigh.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_High, view)
            }
            mBinding.mtChatroomMedium.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_Medium, view)
            }
            mBinding.mtChatroomOff.setOnClickListener { view ->
                onItemChildClick(ConfigConstants.AINSMode.AINS_Off, view)
            }
//            mBinding.mtChatroomHigh.tag = AINSModeType.High
//            mBinding.mtChatroomMedium.tag = AINSModeType.Medium
//            mBinding.mtChatroomOff.tag = AINSModeType.Off
            when (it.anisMode) {
                ConfigConstants.AINSMode.AINS_High -> {
                    setViewHighlight(mBinding.mtChatroomHigh)
                    resetViewDefault(mBinding.mtChatroomMedium)
                    resetViewDefault(mBinding.mtChatroomOff)
                }
                ConfigConstants.AINSMode.AINS_Medium -> {
                    setViewHighlight(mBinding.mtChatroomMedium)
                    resetViewDefault(mBinding.mtChatroomHigh)
                    resetViewDefault(mBinding.mtChatroomOff)
                }
                ConfigConstants.AINSMode.AINS_Off -> {
                    setViewHighlight(mBinding.mtChatroomOff)
                    resetViewDefault(mBinding.mtChatroomHigh)
                    resetViewDefault(mBinding.mtChatroomMedium)
                }
            }
        }
    }

    private fun resetViewDefault(textView: MaterialTextView) {
        textView.setBackgroundResource(R.drawable.voice_bg_rect_radius4_grey)
        textView.setTextColor(ResourcesTools.getColor(context.resources, R.color.voice_dark_grey_color_979cbb))
    }

    private fun setViewHighlight(textView: MaterialTextView) {
        textView.setBackgroundResource(R.drawable.voice_bg_rect_stoke4_blue)
        textView.setTextColor(ResourcesTools.getColor(context.resources, R.color.voice_main_color_156ef3))
    }
}

class RoomAINSSoundsViewHolder(binding: VoiceSpatialItemRoomAinsAuditionBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoomAinsAuditionBinding, AINSSoundsBean>(binding) {
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
        textView.setTextColor(ResourcesTools.getColor(context.resources, R.color.voice_dark_grey_color_979cbb))
    }

    private fun setViewHighlight(textView: MaterialTextView) {
        textView.setBackgroundResource(R.drawable.voice_bg_rect_stoke4_blue)
        textView.setTextColor(ResourcesTools.getColor(context.resources, R.color.voice_main_color_156ef3))
    }
}

class ChatroomAINSTitleViewHolder(binding: VoiceSpatialItemRoomAnisTitleBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoomAnisTitleBinding, String>(binding) {
    override fun binding(data: String?, selectedIndex: Int) {
        data?.let {
            mBinding.mtChatroomAinsTitle.text = it
        }
    }
}

class ChatroomAINSContentViewHolder(binding: VoiceSpatialItemRoomAnisContentBinding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoomAnisContentBinding, String>(binding) {
    override fun binding(data: String?, selectedIndex: Int) {
        data?.let {
            mBinding.mtChatroomAinsContent.text = it
        }
    }
}

class ChatroomAINSGapViewHolder(binding: VoiceSpatialItemRoomAnisGap10Binding) :
    BaseRecyclerViewAdapter.BaseViewHolder<VoiceSpatialItemRoomAnisGap10Binding, String>(binding) {
    override fun binding(data: String?, selectedIndex: Int) {
    }
}