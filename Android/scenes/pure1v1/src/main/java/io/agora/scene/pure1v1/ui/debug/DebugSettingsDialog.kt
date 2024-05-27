package io.agora.scene.pure1v1.ui.debug

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1DebugDialogBinding

class DebugSettingsDialog(
    context: Context
): Dialog(context, R.style.Pure1v1Theme_Dialog_Bottom) {

    interface DebugSettingsListener {
        fun onAudioDumpEnable(enable: Boolean)
    }

    private val mBinding by lazy { Pure1v1DebugDialogBinding.inflate(LayoutInflater.from(context)) }

    private var listener: DebugSettingsListener? = null

    init {
        setContentView(mBinding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.dumpAudio.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                listener?.onAudioDumpEnable(isChecked)
            }
        }
        mBinding.titleView.leftIcon.setOnClickListener {
            dismiss()
        }
    }

    fun setListener(listener: DebugSettingsListener) {
        this.listener = listener
    }
}