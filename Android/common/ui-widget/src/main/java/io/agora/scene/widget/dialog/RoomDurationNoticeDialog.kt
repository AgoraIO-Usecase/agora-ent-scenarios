package io.agora.scene.widget.dialog

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.widget.R
import io.agora.scene.widget.databinding.DialogRoomDurationBinding

class RoomDurationNoticeDialog constructor(val expireTime: Int) :
    BaseBottomSheetDialogFragment<DialogRoomDurationBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvContent.text = getString(R.string.comm_kind_reminder_content, expireTime / 60)
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }
}