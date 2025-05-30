package io.agora.scene.widget.dialog

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.widget.databinding.DialogSecurityNoticeBinding

class SecurityNoticeDialog : BaseBottomSheetDialogFragment<DialogSecurityNoticeBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }
}