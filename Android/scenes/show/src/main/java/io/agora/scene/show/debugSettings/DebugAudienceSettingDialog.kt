package io.agora.scene.show.debugSettings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import com.google.android.material.switchmaterial.SwitchMaterial
import io.agora.scene.show.RtcEngineInstance
import io.agora.scene.show.ShowLogger
import io.agora.scene.show.databinding.ShowWidgetDebugAudienceSettingDialogBinding
import io.agora.scene.show.widget.BottomFullDialog

class DebugAudienceSettingDialog constructor(context: Context) : BottomFullDialog(context) {
    private val TAG = "DebugSettings"
    private val mBinding by lazy {
        ShowWidgetDebugAudienceSettingDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    init {
        setContentView(mBinding.root)

        mBinding.ivBack.setOnClickListener {
            dismiss()
        }

        // 镜像
        setEnable(mBinding.srSwitchCompat, RtcEngineInstance.debugSettingModel.srEnabled)
        // hit / hidden
        when (RtcEngineInstance.debugSettingModel.srType) {
            1.0 -> {
                setSelect(mBinding.srRadioBox, 0)
            }
            1.33 -> {
                setSelect(mBinding.srRadioBox, 1)
            }
            1.5 -> {
                setSelect(mBinding.srRadioBox, 2)
            }
            2.0 -> {
                setSelect(mBinding.srRadioBox, 3)
            }
        }


        mBinding.tvSure.visibility = View.GONE

        // SR
        mBinding.srSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.debugSettingModel.srEnabled = isOpen
            RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":$isOpen, \"mode\": 2}}")
            ShowLogger.d(TAG, "rtc.video.enable_sr: $isOpen")
        }

        // SR mode
        mBinding.srRadioBox.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":false, \"mode\": 2}}")
                when (p2) {
                    0 -> {
                        // hidden
                        RtcEngineInstance.debugSettingModel.srType = 1.0
                        RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.sr_type\":6}")
                        ShowLogger.d(TAG, "rtc.video.sr_type: 1.0")
                    }
                    1 -> {
                        // fit
                        RtcEngineInstance.debugSettingModel.srType = 1.33
                        RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.sr_type\":7}")
                        ShowLogger.d(TAG, "rtc.video.sr_type: 1.33")
                    }
                    2 -> {
                        // fit
                        RtcEngineInstance.debugSettingModel.srType = 1.5
                        RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.sr_type\":8}")
                        ShowLogger.d(TAG, "rtc.video.sr_type: 1.5")
                    }
                    3 -> {
                        // fit
                        RtcEngineInstance.debugSettingModel.srType = 2.0
                        RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.sr_type\":3}")
                        ShowLogger.d(TAG, "rtc.video.sr_type: 2.0")
                    }
                }
                RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":true, \"mode\": 2}}")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun setText(editText: EditText, content: String) {
        editText.setText(content)
        editText.setSelection(content.length)
    }

    private fun setEnable(switch: SwitchMaterial, enabled: Boolean) {
        switch.isChecked = enabled
    }

    private fun setSelect(select: Spinner, num: Int) {
        select.setSelection(num)
    }
}