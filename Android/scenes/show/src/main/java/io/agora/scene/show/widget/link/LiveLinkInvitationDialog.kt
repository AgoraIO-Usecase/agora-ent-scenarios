package io.agora.scene.show.widget.link

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowInsets
import androidx.core.view.updatePadding
import io.agora.scene.show.databinding.ShowLiveLinkInvitationDialogBinding

class LiveLinkInvitationDialog(context: Context) : Dialog(context) {
    private val mBinding by lazy { ShowLiveLinkInvitationDialogBinding.inflate(
        LayoutInflater.from(
            context
        ))}
    private lateinit var mListener: Listener

    fun init() {
        super.setContentView(mBinding.root)
        mBinding.tvTitle.setText("主播邀请你加入连麦")
        mBinding.btnLeft.setOnClickListener {
            mListener.onCancelSeatInvitation()
            dismiss()
        }
        mBinding.btnRight.setOnClickListener {
            mListener.onAgreeSeatInvitation()
            dismiss()
        }
    }

    fun setListener(listener: LiveLinkInvitationDialog.Listener) {
        mListener = listener
    }

    interface Listener {
        fun onAgreeSeatInvitation()
        fun onCancelSeatInvitation()
    }
}