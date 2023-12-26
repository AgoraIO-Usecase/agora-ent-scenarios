package io.agora.scene.showTo1v1.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.databinding.ShowTo1v1CallDetailSettingDialogBinding

class CallDetailSettingDialog constructor(
    private val context: Context
) : Dialog(context, R.style.Show_to1v1Theme_Dialog_Bottom) {

    interface CallDetailSettingItemListener {
        // 点击了实时数据面板
        fun onClickDashboard()
    }

    private val binding = ShowTo1v1CallDetailSettingDialogBinding.inflate(LayoutInflater.from(context))

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
        val anim = AnimationUtils.loadAnimation(context, R.anim.show_to1v1_slide_from_bottom)
        binding.clContent.startAnimation(anim)
    }

    fun setListener(l: CallDetailSettingItemListener) {
        listener = l
    }
}