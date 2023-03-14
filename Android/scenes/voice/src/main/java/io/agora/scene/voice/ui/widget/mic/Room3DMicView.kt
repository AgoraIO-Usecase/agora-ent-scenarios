package io.agora.scene.voice.ui.widget.mic

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.scene.voice.databinding.VoiceViewRoom3dMicBinding
import io.agora.voice.common.constant.ConfigConstants
import io.agora.scene.voice.R
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.annotation.MicStatus
import io.agora.voice.common.utils.ImageTools

/**
 * @author create by zhangwei03
 *
 * 3d麦位
 */
class Room3DMicView : ConstraintLayout, IRoomMicBinding {

    private lateinit var mBinding: VoiceViewRoom3dMicBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context)
    }

    private var arrowAnim: AnimationDrawable? = null

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.voice_view_room_3d_mic, this)
        mBinding = VoiceViewRoom3dMicBinding.bind(root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        arrowAnim = mBinding.ivMicArrowAnim.background as AnimationDrawable?
        mBinding.ivMicArrowAnim.rotation = 360f
        arrowAnim?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        arrowAnim?.stop()
        arrowAnim = null
    }

    override fun binding(micInfo: VoiceMicInfoModel) {
        mBinding.apply {
            if (micInfo.member == null) { // 没人
                ivMicInnerIcon.isVisible = true
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
                        ivMicTag.isVisible = true
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_mute_tag)
                    }
                    else -> {
                        ivMicTag.isVisible = false
                    }
                }
            }
            // 用户音量
            when (micInfo.audioVolumeType) {
                ConfigConstants.VolumeType.Volume_None -> ivMicTag.isVisible = false
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

    fun changeAngle(angle: Float) {
        val layoutParams: LayoutParams = mBinding.ivMicArrowAnim.layoutParams as LayoutParams
        layoutParams.circleAngle = angle
        mBinding.ivMicArrowAnim.rotation = angle
        mBinding.ivMicArrowAnim.layoutParams = layoutParams
    }
}