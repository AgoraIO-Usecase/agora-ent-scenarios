package io.agora.scene.joy.ui.widget

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.joy.databinding.JoyDialogGameRulesLayoutBinding

class JoyGameRulesDialog : BaseBottomSheetDialogFragment<JoyDialogGameRulesLayoutBinding>() {

    companion object {
        const val Key_Content = "key_content"
    }

    private val mContent by lazy {
        arguments?.getString(Key_Content) ?: ""
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvIntroduceContent.text = mContent
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
        }

    }
}