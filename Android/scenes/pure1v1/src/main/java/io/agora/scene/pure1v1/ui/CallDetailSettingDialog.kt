package io.agora.scene.pure1v1.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1CallDetailSettingDialogBinding
import io.agora.scene.pure1v1.databinding.Pure1v1CallSendDialogBinding

class CallDetailSettingDialog(
    private val context: Context,
) : Dialog(context, R.style.Pure1v1Theme_Dialog_Bottom) {

    interface CallDetailSettingItemListener {
        // 点击了实时数据面板
        fun onClickDashboard()
    }

    private val binding = Pure1v1CallDetailSettingDialogBinding.inflate(LayoutInflater.from(context))

    private var listener: CallDetailSettingItemListener? = null

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.clContent.setOnClickListener {
        }
        binding.root.setOnClickListener {
            dismiss()
        }
        binding.llDashboard.setOnClickListener {
            dismiss()
            listener?.onClickDashboard()
        }
        val anim = AnimationUtils.loadAnimation(context, R.anim.pure1v1_slide_from_bottom)
        binding.clContent.startAnimation(anim)
    }

    fun setListener(l: CallDetailSettingItemListener) {
        listener = l
    }
}