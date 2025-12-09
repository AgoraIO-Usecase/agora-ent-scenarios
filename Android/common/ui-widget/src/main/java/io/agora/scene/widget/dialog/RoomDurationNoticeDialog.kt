package io.agora.scene.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import io.agora.scene.widget.R
import io.agora.scene.widget.databinding.DialogRoomDurationBinding

@JvmOverloads
fun FragmentActivity.showRoomDurationNotice(expireTime: Int){
    RoomDurationNoticeDialog(expireTime).show(this.supportFragmentManager,"showRoomDurationNotice")
}

class RoomDurationNoticeDialog constructor(val expireTime: Int) :
    BaseImmersiveBottomSheetDialogFragment() {

    private var binding : DialogRoomDurationBinding? = null
    private val mBinding get() = binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogRoomDurationBinding.inflate(LayoutInflater.from(context))
        return mBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup immersive mode
        setupImmersiveMode(mBinding.root)
        mBinding.tvContent.text = getString(R.string.comm_kind_reminder_content, expireTime / 60)
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }
}