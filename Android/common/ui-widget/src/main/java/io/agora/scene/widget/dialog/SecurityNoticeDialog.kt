package io.agora.scene.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.widget.databinding.DialogSecurityNoticeBinding

class SecurityNoticeDialog : BaseImmersiveBottomSheetDialogFragment() {

    private var binding : DialogSecurityNoticeBinding? = null
    private val mBinding get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSecurityNoticeBinding.inflate(LayoutInflater.from(context))
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup immersive mode
        setupImmersiveMode(mBinding.root)
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }
}