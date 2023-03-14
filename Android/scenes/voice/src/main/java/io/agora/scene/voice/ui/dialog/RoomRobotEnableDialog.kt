package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.scene.voice.databinding.VoiceDialogSwitchRobotBinding
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class RoomRobotEnableDialog constructor(
    private val onRobotClickListener: OnClickBtnListener
): BaseSheetDialog<VoiceDialogSwitchRobotBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogSwitchRobotBinding {
        return VoiceDialogSwitchRobotBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.root?.let {
            setOnApplyWindowInsets(it)
        }
        initListener()
    }

    private fun initListener(){
        binding.let {
            it?.leftBtn?.setOnClickListener {
                onRobotClickListener.onClickCloseBtn()
                dismiss()
            }
            it?.rightBtn?.setOnClickListener{
                onRobotClickListener.onClickSettingBtn()
                dismiss()
            }
        }
    }

    interface OnClickBtnListener {
        fun onClickCloseBtn()
        fun onClickSettingBtn()
    }

}