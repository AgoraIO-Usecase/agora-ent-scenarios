package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.R
import io.agora.scene.show.VideoSetting
import io.agora.scene.show.databinding.ShowSettingPresetDialogBinding

class PresetDialog constructor(context: Context, deviceScore: Int) : BottomFullDialog(context) {

    private val mBinding by lazy {
        ShowSettingPresetDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private val deviceScore by lazy {
        deviceScore
    }

    init {
        setContentView(mBinding.root)
        mBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mBinding.tvConfirm.setOnClickListener {
//            val showSelectPosition = getGroupSelectedItem(
//                mBinding.showChooseItemLowDevice,
//                mBinding.showChooseItemMediumDevice,
//                mBinding.showChooseItemHighDevice
//            )
//            if (showSelectPosition < 0) {
//                ToastDialog(context).apply {
//                    dismissDelayShort()
//                    showMessage(context.getString(R.string.show_setting_preset_no_choise_tip))
//                }
//                return@setOnClickListener
//            }
//            onPresetShowModeSelected(showSelectPosition)

            // 网络设置
            val networkSelectPosition = getGroupSelectedItem(
                mBinding.basicChooseItemGoodNetwork,
                mBinding.basicChooseItemNormalNetwork
            )
            if (networkSelectPosition < 0) {
                ToastDialog(context).apply {
                    dismissDelayShort()
                    showMessage(context.getString(R.string.show_setting_preset_no_choise_tip))
                }
                return@setOnClickListener
            }

            // 画质设置
            val broadcastStrategySelectPosition = getGroupSelectedItem(
                mBinding.broadcastStrategyItemSmooth,
                mBinding.broadcastStrategyItemClear
            )
            if (broadcastStrategySelectPosition < 0) {
                ToastDialog(context).apply {
                    dismissDelayShort()
                    showMessage(context.getString(R.string.show_setting_preset_no_choise_tip))
                }
                return@setOnClickListener
            }
            onPresetNetworkModeSelected(networkSelectPosition, broadcastStrategySelectPosition)
            dismiss()
        }
//        groupItems(
//            {}, -1,
//            mBinding.showChooseItemLowDevice,
//            mBinding.showChooseItemMediumDevice,
//            mBinding.showChooseItemHighDevice
//        )
        groupItems(
            {}, 0,
            mBinding.basicChooseItemGoodNetwork,
            mBinding.basicChooseItemNormalNetwork
        )

        groupItems(
            {}, 0,
            mBinding.broadcastStrategyItemSmooth,
            mBinding.broadcastStrategyItemClear
        )

        val deviceLevel = if (deviceScore >= 85) {
            "高端机"
        } else if (deviceScore >= 60) {
            "中端机"
        } else {
            "低端机"
        }
        mBinding.tvDeviceScore.text = "设备检测结果：$deviceLevel"
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

    private fun onPresetShowModeSelected(level: Int){
        val selectedLevel = level
        if (selectedLevel < 0) {
            // 没有选择默认使用低端机配置
            return
        }
        when (selectedLevel) {
            // 低端机
            0 -> {
                VideoSetting.updateBroadcastSetting(VideoSetting.DeviceLevel.Low)
            }
            // 中端机
            1 -> {
                VideoSetting.updateBroadcastSetting(VideoSetting.DeviceLevel.Medium)
            }
            // 高端机
            2 -> {
                VideoSetting.updateBroadcastSetting(VideoSetting.DeviceLevel.High)
            }
        }

        ToastDialog(context).apply {
            dismissDelayShort()
            showMessage(context.getString(R.string.show_setting_preset_done))
        }
    }

    private fun onPresetNetworkModeSelected(networkLevel: Int, broadcastStrategyLevel: Int){
        if (networkLevel < 0 || broadcastStrategyLevel < 0) {
            // 没有选择默认使用好网络配置
            return
        }

        val broadcastStrategy = if (broadcastStrategyLevel == 0) VideoSetting.BroadcastStrategy.Smooth else VideoSetting.BroadcastStrategy.Clear
        val network = if (networkLevel == 0) VideoSetting.NetworkLevel.Good else VideoSetting.NetworkLevel.Normal
        val deviceLevel = if (deviceScore >= 85) {
            VideoSetting.DeviceLevel.High
        } else if (deviceScore >= 60) {
            VideoSetting.DeviceLevel.Medium
        } else {
            VideoSetting.DeviceLevel.Low
        }

        VideoSetting.updateBroadcastSetting(deviceLevel, network, broadcastStrategy)
        ToastDialog(context).apply {
            dismissDelayShort()
            showMessage(context.getString(R.string.show_setting_preset_done))
        }
    }


}