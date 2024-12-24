package io.agora.scene.voice.spatial.ui.widget.mic

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialViewRoom3dMicBinding
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.annotation.MicStatus

/**
 * @author create by zhangwei03
 *
 * 3d麦位
 */
class Room3DMicView : ConstraintLayout, IRoomMicBinding {

    private lateinit var mBinding: VoiceSpatialViewRoom3dMicBinding

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
        val root = View.inflate(context, R.layout.voice_spatial_view_room_3d_mic, this)
        mBinding = VoiceSpatialViewRoom3dMicBinding.bind(root)
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
                ivMicInfo.setImageResource(0)
                mtMicUsername.text = micInfo.micIndex.toString()
                mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                when (micInfo.micStatus) {
                    MicStatus.ForceMute -> {
                        ivMicTag.isVisible = true
                        ivMicTag.setImageResource(R.drawable.voice_ic_mic_mute_tag)
                    }
                    MicStatus.Lock -> {
                        ivMicTag.isVisible = false
                    }
                    MicStatus.LockForceMute -> {
                        ivMicTag.isVisible = true
                        ivMicTag.setImageResource(R.drawable.voice_ic_mic_mute_tag)
                    }
                    else -> {
                        ivMicTag.isVisible = false
                    }
                }
            } else { // 有人
                ivMicTag.isVisible = true
                GlideApp.with(ivMicInfo)
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
            }
            if (micInfo.micStatus == MicStatus.Mute ||
                micInfo.micStatus == MicStatus.ForceMute ||
                micInfo.member?.micStatus == MicStatus.Mute) {
                ivMicTag.setImageResource(R.drawable.voice_ic_mic_mute_tag)
            } else {
                // 用户音量
                when (micInfo.audioVolumeType) {
                    ConfigConstants.VolumeType.Volume_None -> {
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open0)
                    }
                    ConfigConstants.VolumeType.Volume_Low -> {
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open1)
                    }
                    ConfigConstants.VolumeType.Volume_Medium -> {
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open2)
                    }
                    ConfigConstants.VolumeType.Volume_High -> {
                        ivMicTag.setImageResource(R.drawable.voice_icon_room_mic_open3)
                    }
                    ConfigConstants.VolumeType.Volume_Max -> {
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