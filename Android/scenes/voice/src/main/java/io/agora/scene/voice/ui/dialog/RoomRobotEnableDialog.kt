package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.View
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.voice.databinding.VoiceDialogSwitchRobotBinding

class RoomRobotEnableDialog constructor(
    private val onRobotClickListener: OnClickBtnListener
): BaseBottomSheetDialogFragment<VoiceDialogSwitchRobotBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener(){
        mBinding.let {
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