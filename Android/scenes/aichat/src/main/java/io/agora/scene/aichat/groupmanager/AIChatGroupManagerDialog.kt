package io.agora.scene.aichat.groupmanager

import android.os.Bundle
import android.view.View
import io.agora.scene.aichat.databinding.AichatDialogGroupManagerBinding
import io.agora.scene.base.component.BaseBottomSheetDialogFragment

class AIChatGroupManagerDialog constructor() : BaseBottomSheetDialogFragment<AichatDialogGroupManagerBinding>() {

    var deleteListener: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.mtDelete.setOnClickListener {
            deleteListener?.invoke()
        }
    }
}