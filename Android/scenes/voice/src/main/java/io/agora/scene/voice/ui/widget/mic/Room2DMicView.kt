package io.agora.scene.voice.ui.widget.mic

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
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

    private val animatorSet = AnimatorSet()

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
        addAnimation()
    }

    override fun binding(micInfo: VoiceMicInfoModel) {
        mBinding.apply {
            if (micInfo.micStatus == MicStatus.BotActivated || micInfo.micStatus == MicStatus.BotInactive) { // 机器人
                ivMicTag.isVisible = false
                ivMicInfo.setBackgroundResource(R.drawable.voice_bg_oval_white)
                val botDrawable = ResourcesTools.getDrawableId(context, micInfo.member?.portrait ?: "")
                ImageTools.loadImage(ivMicInfo, botDrawable)
                mtMicUsername.text = micInfo.member?.nickName ?: ""
                mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.voice_icon_room_mic_robot_tag, 0, 0, 0
                )
                mtMicRotActive.isGone = micInfo.micStatus == MicStatus.BotActivated
                ivMicBotFloat.isGone = micInfo.micStatus == MicStatus.BotActivated
            } else {
                if (micInfo.member == null) { // 没人
                    vWave1.isVisible = false
                    vWave2.isVisible = false
                    mtMicUsername.text = resources.getString(R.string.voice_room_mic_number, micInfo.micIndex + 1)
                    mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    when (micInfo.micStatus) {
                        MicStatus.ForceMute -> {
                            ivMicTag.isVisible = true
                            ivMicInfo.setImageResource(R.drawable.voice_ic_mic_empty)
                        }
                        MicStatus.Lock -> {
                            ivMicInfo.setImageResource(R.drawable.voice_ic_mic_close)
                            ivMicTag.isVisible = false
                        }
                        MicStatus.LockForceMute -> {
                            ivMicInfo.setImageResource(R.drawable.voice_ic_mic_close)
                            ivMicTag.isVisible = true
                        }
                        else -> {
                            ivMicTag.isVisible = false
                            ivMicInfo.setImageResource(R.drawable.voice_ic_mic_empty)
                        }
                    }
                } else { // 有人
                    vWave1.isVisible = true
                    vWave2.isVisible = true
                    ImageTools.loadImage(ivMicInfo, micInfo.member?.portrait)
                    mtMicUsername.text = micInfo.member?.nickName ?: ""
                    if (micInfo.micIndex == 0) {
                        mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.voice_icon_room_mic_owner_tag, 0, 0, 0
                        )
                    } else {
                        mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                    when (micInfo.micStatus) {
                        MicStatus.Mute,
                        MicStatus.ForceMute -> {
                            ivMicTag.isVisible = true
                        }
                        else -> {
                            ivMicTag.isVisible = micInfo.member?.micStatus == 0
                        }
                    }
                }
            }
            if (micInfo.micStatus == MicStatus.Normal || micInfo.micStatus == MicStatus.BotActivated) {
                when (micInfo.audioVolumeType) {
                    ConfigConstants.VolumeType.Volume_Low,
                    ConfigConstants.VolumeType.Volume_Medium,
                    ConfigConstants.VolumeType.Volume_High,
                    ConfigConstants.VolumeType.Volume_Max -> {
                        if (!animatorSet.isRunning) {
                            animatorSet.start()
                        }
                    }
                    else -> {
                        if (animatorSet.isRunning) {
                            animatorSet.end()
                        }
                    }
                }
            } else if (micInfo.micStatus == MicStatus.BotInactive) {
                if (animatorSet.isRunning) {
                    animatorSet.end()
                }
            }
        }
    }

    private fun addAnimation() {
        val animator1 = ObjectAnimator.ofPropertyValuesHolder(
            mBinding.vWave1,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f, 0.3f)
        )
        animator1.repeatCount = ObjectAnimator.INFINITE
        animator1.repeatMode = ObjectAnimator.RESTART
        animator1.interpolator = DecelerateInterpolator()
        animator1.duration = 1400

        val animator2 = ObjectAnimator.ofPropertyValuesHolder(
            mBinding.vWave2,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.4f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.4f),
            PropertyValuesHolder.ofFloat("alpha", 0.6f, 0.3f, 0f)
        )
        animator2.repeatCount = ObjectAnimator.INFINITE
        animator2.repeatMode = ObjectAnimator.RESTART
        animator2.interpolator = DecelerateInterpolator()
        animator2.duration = 1400
        animatorSet.playTogether(animator1, animator2)
    }
}