package io.agora.scene.widget.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.widget.R
import io.agora.scene.widget.databinding.DialogRoomDurationBinding

@JvmOverloads
fun FragmentActivity.showRoomDurationNotice(expireTime: Int){
    RoomDurationNoticeDialog(expireTime).show(this.supportFragmentManager,"showRoomDurationNotice")
}

class RoomDurationNoticeDialog constructor(val expireTime: Int) :
    BaseBottomSheetDialogFragment<DialogRoomDurationBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvContent.text = getString(R.string.comm_kind_reminder_content, expireTime / 60)
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }
}