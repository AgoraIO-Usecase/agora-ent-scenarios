package io.agora.scene.aichat.create

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.aichat.databinding.AichatCreateAgentDialogBinding
import io.agora.scene.base.component.BaseBottomFullDialogFragment

class AIChatCreateAgentDialog : BaseBottomFullDialogFragment<AichatCreateAgentDialogBinding>() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false) // 禁用点击外部关闭对话框

        dialog.setOnKeyListener { _, keyCode, event ->
            // 屏蔽返回键
            keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
        }
        return dialog
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatCreateAgentDialogBinding {
        return AichatCreateAgentDialogBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.apply {
            ivBackIcon.setOnClickListener {
                dismiss()
            }
        }
    }
}