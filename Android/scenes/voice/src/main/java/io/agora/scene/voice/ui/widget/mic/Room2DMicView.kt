package io.agora.scene.voice.ui.widget.mic

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import io.agora.voice.common.utils.ResourcesTools
import io.agora.voice.common.constant.ConfigConstants
import io.agora.scene.voice.R
import io.agora.scene.voice.model.annotation.MicStatus
import io.agora.scene.voice.databinding.VoiceViewRoom2dMicBinding
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.voice.common.utils.ImageTools

/**
 * @author create by zhangwei03
 *
 * 普通麦位
 */
class Room2DMicView : ConstraintLayout, IRoomMicBinding {

    private lateinit var mBinding: VoiceViewRoom2dMicBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.voice_view_room_2d_mic, this)
        mBinding = VoiceViewRoom2dMicBinding.bind(root)
    }

    override fun binding(micInfo: VoiceMicInfoModel) {
        mBinding.apply {
            if (micInfo.micStatus == MicStatus.BotActivated || micInfo.micStatus == MicStatus.BotInactive) { // 机器人

                ivMicInnerIcon.isVisible = false
                ivMicInfo.setBackgroundResource(R.drawable.voice_bg_oval_white)
                val botDrawable = ResourcesTools.getDrawableId(context, micInfo.member?.portrait ?: "")
                ImageTools.loadImage(ivMicInfo, botDrawable)
                mtMicUsername.text = micInfo.member?.nickName ?: ""
                mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.voice_icon_room_mic_robot_tag, 0, 0, 0
                )
                ivMicTag.isVisible = micInfo.micStatus == MicStatus.BotActivated
                mtMicRotActive.isGone = micInfo.micStatus == MicStatus.BotActivated
                ivMicBotFloat.isGone = micInfo.micStatus == MicStatus.BotActivated
            } else {
                if (micInfo.member == null) { // 没人
                    ivMicInnerIcon.isVisible = true
                    ivMicInfo.setImageResource(0)
                    mtMicUsername.text = micInfo.micIndex.toString()
                    mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    when (micInfo.micStatus) {
                        MicStatus.ForceMute -> {
                            ivMicTag.isVisible = false
                            ivMicInnerIcon.setImageResource(R.drawable.voice_icon_room_mic_mute)
                        }
                        MicStatus.Lock -> {
                            ivMicInnerIcon.setImageResource(R.drawable.voice_icon_room_mic_close)
                            ivMicTag.isVisible = false
                        }
                        MicStatus.LockForceMute -> {
                            ivMicInnerIcon.setImageResource(R.drawable.voice_icon_room_mic_close)
                            ivMicTag.isVisible = true
                            ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_mute_tag)
                        }
                        else -> {
                            ivMicTag.isVisible = false
                            ivMicInnerIcon.setImageResource(R.drawable.voice_icon_room_mic_add)
                        }
                    }
                } else { // 有人
                    ivMicTag.isVisible = true
                    ivMicInnerIcon.isVisible = false
                    ImageTools.loadImage(ivMicInfo, micInfo.member?.portrait)
                    mtMicUsername.text = micInfo.member?.nickName ?: ""
                    if (micInfo.ownerTag) {
                        mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.voice_icon_room_mic_owner_tag, 0, 0, 0
                        )
                    } else {
                        mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                    when (micInfo.micStatus) {
                        MicStatus.Mute,
                        MicStatus.ForceMute -> {
                            ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_mute_tag)
                        }
                        else -> {
                            if (micInfo.member?.micStatus == 0){
                                ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_mute_tag)
                            }else{
                                ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open0)
                            }
                        }
                    }
                }
            }
            if (micInfo.micStatus == MicStatus.Normal || micInfo.micStatus == MicStatus.BotActivated) {
                // 用户音量
                when (micInfo.audioVolumeType) {
                    ConfigConstants.VolumeType.Volume_None -> {
                        ivMicTag.isVisible = true
                        if (micInfo.member?.micStatus == 1){
                            ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open0)
                        }else{
                            ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_mute_tag)
                        }
                    }
                    ConfigConstants.VolumeType.Volume_Low -> {
                        ivMicTag.isVisible = true
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open1)
                    }
                    ConfigConstants.VolumeType.Volume_Medium -> {
                        ivMicTag.isVisible = true
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open2)
                    }
                    ConfigConstants.VolumeType.Volume_High -> {
                        ivMicTag.isVisible = true
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open3)
                    }
                    ConfigConstants.VolumeType.Volume_Max -> {
                        ivMicTag.isVisible = true
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open4)
                    }
                    else -> {

                    }
                }
            }
        }
    }
}