package io.agora.scene.pure1v1.ui.living

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1CallDetailSettingDialogBinding

/*
 * 1v1 互动中设置页面
 */
class CallDetailSettingDialog(
    private val context: Context,
    private val cameraOn: Boolean,
    private val micOn: Boolean,
) : Dialog(context, R.style.Pure1v1Theme_Dialog_Bottom) {

    interface CallDetailSettingItemListener {
        // 点击了实时数据面板
        fun onClickDashboard()
        // 点击了摄像头开关
        fun onCameraSwitch(isCameraOn: Boolean)
        // 点击了麦克风开关
        fun onMicSwitch(isMicOn: Boolean)
    }

    private val binding = Pure1v1CallDetailSettingDialogBinding.inflate(LayoutInflater.from(context))

    private var listener: CallDetailSettingItemListener? = null

    private var innerCameraOn = true
    private var innerMicOn = true

    init {
        setContentView(binding.root)
        innerCameraOn = cameraOn
        innerMicOn = micOn
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.root.setOnClickListener {
            dismiss()
        }
        binding.llDashboard.setOnClickListener {
            dismiss()
            listener?.onClickDashboard()
        }
        binding.llCamera.setOnClickListener {
            innerCameraOn = !innerCameraOn
            listener?.onCameraSwitch(innerCameraOn)
            switchCameraUI()
        }
        binding.llMic.setOnClickListener {
            innerMicOn = !innerMicOn
            listener?.onMicSwitch(innerMicOn)
            switchMicUI()
        }
        switchCameraUI()
        switchMicUI()
        val anim = AnimationUtils.loadAnimation(context, R.anim.pure1v1_slide_from_bottom)
        binding.clContent.startAnimation(anim)
    }

    fun setListener(l: CallDetailSettingItemListener) {
        listener = l
    }

    private fun switchCameraUI() {
        if (innerCameraOn) {
            binding.lvCamera.background = context.getDrawable(R.drawable.pure1v1_setting_ic_video_on)
            binding.tvCamera.text = context.getText(R.string.pure1v1_dashboard_item_camera)
        } else {
            binding.lvCamera.background = context.getDrawable(R.drawable.pure1v1_setting_ic_video_off)
            binding.tvCamera.text = context.getText(R.string.pure1v1_dashboard_item_camera_off)
        }
    }

    private fun switchMicUI() {
        if (innerMicOn) {
            binding.ivMic.background = context.getDrawable(R.drawable.pure1v1_setting_ic_mic_on)
            binding.tvMic.text = context.getText(R.string.pure1v1_dashboard_item_mic)
        } else {
            binding.ivMic.background = context.getDrawable(R.drawable.pure1v1_setting_ic_mic_off)
            binding.tvMic.text = context.getText(R.string.pure1v1_dashboard_item_mic_off)
        }
    }
}