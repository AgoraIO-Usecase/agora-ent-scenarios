package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomWelcomeBinding

class Room3DWelcomeSheetDialog : BaseBottomSheetDialogFragment<VoiceSpatialDialogRoomWelcomeBinding>() {

    // xxx
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.apply {
            // Event listener
            mbNext.setOnClickListener {
                dismiss()
            }
            dialog?.window?.let {
                ViewCompat.setOnApplyWindowInsetsListener(root) { v: View?, insets: WindowInsetsCompat ->
                    val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    root.setPadding(0, 0, 0, root.paddingBottom + systemInset.bottom)
                    WindowInsetsCompat.CONSUMED
                }
            }
        }
    }
}