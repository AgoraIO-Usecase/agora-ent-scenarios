//package io.agora.scene.aichat.create
//
//import android.app.Dialog
//import android.os.Bundle
//import android.view.KeyEvent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import io.agora.scene.aichat.databinding.AichatCreateGroupDialogBinding
//import io.agora.scene.base.component.BaseBottomFullDialogFragment
//
///**
// * Ai chat create group dialog
// *
// * @constructor Create empty Ai chat create group dialog
// */
//class AIChatCreateGroupDialog : BaseBottomFullDialogFragment<AichatCreateGroupDialogBinding>() {
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.setCanceledOnTouchOutside(false) // 禁用点击外部关闭对话框
//
//        dialog.setOnKeyListener { _, keyCode, event ->
//            // 屏蔽返回键
//            keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
//        }
//        return dialog
//    }
//
//    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatCreateGroupDialogBinding {
//        return AichatCreateGroupDialogBinding.inflate(inflater, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        mBinding?.apply {
//            titleView.setLeftClick {
//                dismiss()
//            }
//        }
//    }
//}