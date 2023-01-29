package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import io.agora.scene.show.R
import io.agora.scene.show.VideoSetting
import io.agora.scene.show.databinding.ShowSettingPresetAudienceDialogBinding

class PresetAudienceDialog(context: Context, showCloseBtn: Boolean = true) : BottomFullDialog(context) {

    var callBack: OnPresetAudienceDialogCallBack? = null
    private val mBinding by lazy {
        ShowSettingPresetAudienceDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    init {
        setContentView(mBinding.root)
        mBinding.ivClose.visibility = if (showCloseBtn) View.VISIBLE else View.INVISIBLE
        mBinding.ivClose.setOnClickListener {
            onPresetShowModeSelected(-1)
            dismiss()
        }
        mBinding.tvConfirm.setOnClickListener {
            val showSelectPosition = getGroupSelectedItem(
                mBinding.enhanceChooseItemLowDevice,
                mBinding.enhanceChooseItemMediumDevice,
                mBinding.enhanceChooseItemHighDevice,
                mBinding.basicChooseItemLowDevice,
                mBinding.basicChooseItemMediumDevice,
                mBinding.basicChooseItemHighDevice
            )
            if (showSelectPosition < 0) {
                ToastDialog(context).apply {
                    dismissDelayShort()
                    showMessage(context.getString(R.string.show_setting_preset_no_choise_tip))
                }
                return@setOnClickListener
            }
            onPresetShowModeSelected(showSelectPosition)
            callBack?.onClickConfirm()
            dismiss()
        }
        groupItems(
            {}, -1,
            mBinding.enhanceChooseItemLowDevice,
            mBinding.enhanceChooseItemMediumDevice,
            mBinding.enhanceChooseItemHighDevice,
            mBinding.basicChooseItemLowDevice,
            mBinding.basicChooseItemMediumDevice,
            mBinding.basicChooseItemHighDevice
        )
    }

    private fun getGroupSelectedItem(vararg itemViews: View): Int {
        itemViews.forEachIndexed { index, view ->
            if (view.isActivated) {
                return index
            }
        }
        return -1
    }

    private fun groupItems(
        onSelectChanged: (Int) -> Unit,
        activateIndex: Int,
        vararg itemViews: View
    ) {
        itemViews.forEachIndexed { index, view ->
            view.isActivated = activateIndex == index
            view.setOnClickListener {
                if (view.isActivated) {
                    return@setOnClickListener
                }
                itemViews.forEach { it.isActivated = it == view }
                onSelectChanged.invoke(index)
            }
        }
    }

    private fun onPresetShowModeSelected(level: Int) {
        val selectedLevel = level
        if (selectedLevel < 0) {
            // 没有选择默认使用低端机配置
            return
        }
        when (selectedLevel) {
            // 低端机：画质增强
            0 -> {
                VideoSetting.updateAudioSetting(SR = VideoSetting.SuperResolution.SR_NONE)
                VideoSetting.updateBroadcastSetting(
                    deviceLevel = VideoSetting.DeviceLevel.Low,
                    isByAudience = true
                )
            }
            // 中端机：画质增强
            1 -> {
                VideoSetting.updateAudioSetting(SR = VideoSetting.SuperResolution.SR_1)
                VideoSetting.updateBroadcastSetting(
                    deviceLevel = VideoSetting.DeviceLevel.Medium,
                    isByAudience = true
                )
            }
            // 高端机：画质增强
            2 -> {
                VideoSetting.updateAudioSetting(SR = VideoSetting.SuperResolution.SR_1_5)
                VideoSetting.updateBroadcastSetting(
                    deviceLevel = VideoSetting.DeviceLevel.High,
                    isByAudience = true
                )
            }
            // 低端机：基础
            3 -> {
                VideoSetting.updateAudioSetting(SR = VideoSetting.SuperResolution.SR_NONE)
                VideoSetting.updateBroadcastSetting(
                    deviceLevel = VideoSetting.DeviceLevel.Low,
                    isByAudience = true
                )
            }
            // 中端机：基础
            4 -> {
                VideoSetting.updateAudioSetting(SR = VideoSetting.SuperResolution.SR_NONE)
                VideoSetting.updateBroadcastSetting(
                    deviceLevel = VideoSetting.DeviceLevel.Medium,
                    isByAudience = true
                )
            }
            // 高端机：基础
            5 -> {
                VideoSetting.updateAudioSetting(SR = VideoSetting.SuperResolution.SR_NONE)
                VideoSetting.updateBroadcastSetting(
                    deviceLevel = VideoSetting.DeviceLevel.High,
                    isByAudience = true
                )
            }
        }

        ToastDialog(context).apply {
            dismissDelayShort()
            showMessage(context.getString(R.string.show_setting_preset_done))
        }
    }


}

interface OnPresetAudienceDialogCallBack {

    // 用户点击了确认按钮(并且有选择配置)
    fun onClickConfirm()

}