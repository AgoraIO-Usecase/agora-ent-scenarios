package io.agora.scene.voice.spatial.ui.widget.mic

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialViewRoom2dMicBinding
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.annotation.MicStatus

/**
 * @author create by zhangwei03
 *
 * Normal mic
 */
class Room2DMicView : ConstraintLayout, IRoomMicBinding {

    private lateinit var mBinding: VoiceSpatialViewRoom2dMicBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.voice_spatial_view_room_2d_mic, this)
        mBinding = VoiceSpatialViewRoom2dMicBinding.bind(root)
    }

    override fun binding(micInfo: VoiceMicInfoModel) {
        mBinding.apply {
            if (micInfo.micStatus == MicStatus.BotActivated || micInfo.micStatus == MicStatus.BotInactive) { // robot

                ivMicInfo.setBackgroundResource(R.drawable.voice_bg_oval_white)
                val botDrawable =  context.resources.getIdentifier(micInfo.member?.portrait ?: "", "drawable", context.packageName)
                Glide.with(ivMicInfo)
                    .load(botDrawable)
                    .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivMicInfo)
                mtMicUsername.text = micInfo.member?.nickName ?: ""
                mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.voice_icon_room_mic_robot_tag, 0, 0, 0
                )
                ivMicTag.isVisible = micInfo.micStatus == MicStatus.BotActivated
                mtMicRotActive.isGone = micInfo.micStatus == MicStatus.BotActivated
                ivMicBotFloat.isGone = micInfo.micStatus == MicStatus.BotActivated
            } else {
                if (micInfo.member == null) { // No one
                    ivMicInfo.setImageResource(R.drawable.voice_ic_mic_empty)
                    mtMicUsername.text = micInfo.micIndex.toString()
                    mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    when (micInfo.micStatus) {
                        MicStatus.ForceMute -> {
                            ivMicTag.isVisible = true
                            ivMicTag.setImageResource(R.drawable.voice_ic_mic_mute_tag)
                            ivMicInfo.setImageResource(R.drawable.voice_ic_mic_empty)
                        }
                        MicStatus.Lock -> {
                            ivMicTag.isVisible = false
                            ivMicInfo.setImageResource(R.drawable.voice_ic_mic_close)
                        }
                        MicStatus.LockForceMute -> {
                            ivMicTag.isVisible = true
                            ivMicTag.setImageResource(R.drawable.voice_ic_mic_mute_tag)
                            ivMicInfo.setImageResource(R.drawable.voice_ic_mic_close)
                        }
                        else -> {
                            ivMicTag.isVisible = false
                            ivMicInfo.setImageResource(R.drawable.voice_ic_mic_empty)
                        }
                    }
                } else { // Someone
                    ivMicTag.isVisible = true
                    Glide.with(ivMicInfo)
                        .load(micInfo.member?.portrait)
                        .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivMicInfo)

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
                            ivMicTag.setImageResource(R.drawable.voice_ic_mic_mute_tag)
                        }
                        else -> {
                            if (micInfo.member?.micStatus == MicStatus.Normal) {
                                ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open0)
                            } else {
                                ivMicTag.setImageResource(R.drawable.voice_ic_mic_mute_tag)
                            }
                        }
                    }
                }
            }
            if (micInfo.member != null && micInfo.micStatus == MicStatus.Normal && micInfo.member?.micStatus == MicStatus.Normal || micInfo.micStatus == MicStatus.BotActivated) {
                // User volume
                when (micInfo.audioVolumeType) {
                    ConfigConstants.VolumeType.Volume_None -> {
                        ivMicTag.isVisible = true
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open0)
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

    fun changeAngle(angle: Float) {
        val layoutParams: LayoutParams = mBinding.ivMicArrowAnim.layoutParams as LayoutParams
        layoutParams.circleAngle = angle
        mBinding.ivMicArrowAnim.rotation = angle
        mBinding.ivMicArrowAnim.layoutParams = layoutParams
    }
}