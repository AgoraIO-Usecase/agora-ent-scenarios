package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.show.R
import io.agora.scene.show.VideoSetting
import io.agora.scene.show.databinding.ShowSettingPresetDialogBinding

class PresetDialog constructor(context: Context, deviceScore: Int, rtcConnection: RtcConnection) : BottomFullDialog(context) {

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

    private val rtcConnection by lazy {
        rtcConnection
    }

    init {
        setContentView(mBinding.root)

        if (AgoraApplication.the().isDebugModeOpen) {
            mBinding.deviceSelector.isVisible = true
        }

        mBinding.ivClose.isVisible = false
        mBinding.tvConfirm.setOnClickListener {
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

            // 机型设置
            if (AgoraApplication.the().isDebugModeOpen) {
                val showSelectPosition = getGroupSelectedItem(
                    mBinding.showChooseItemLowDevice,
                    mBinding.showChooseItemMediumDevice,
                    mBinding.showChooseItemHighDevice
                )
                onPresetNetworkModeSelected(networkSelectPosition, broadcastStrategySelectPosition, showSelectPosition)
            } else {
                onPresetNetworkModeSelected(networkSelectPosition, broadcastStrategySelectPosition, null)
            }
            dismiss()
        }
        groupItems(
            {}, 0,
            mBinding.basicChooseItemGoodNetwork,
            mBinding.basicChooseItemNormalNetwork
        )

        groupItems(
            {
                when (it) {
                    0 -> mBinding.networkView.isVisible = true
                    1 -> mBinding.networkView.isVisible = false
                }
            }, 0,
            mBinding.broadcastStrategyItemSmooth,
            mBinding.broadcastStrategyItemClear
        )

        groupItems(
            {}, -1,
            mBinding.showChooseItemLowDevice,
            mBinding.showChooseItemMediumDevice,
            mBinding.showChooseItemHighDevice
        )

        val deviceLevel = if (deviceScore >= 90) {
            "高端机"
        } else if (deviceScore >= 75) {
            "中端机"
        } else {
            mBinding.tvBeautyTips.isVisible = true
            "低端机"
        }
        mBinding.tvDeviceScore.text = "设备检测结果：$deviceLevel（$deviceScore）"
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

    private fun onPresetNetworkModeSelected(networkLevel: Int, broadcastStrategyLevel: Int, device: Int?){
        if (networkLevel < 0 || broadcastStrategyLevel < 0) {
            // 没有选择默认使用好网络配置
            return
        }

        val broadcastStrategy = if (broadcastStrategyLevel == 0) VideoSetting.BroadcastStrategy.Smooth else VideoSetting.BroadcastStrategy.Clear
        val network = if (networkLevel == 0) VideoSetting.NetworkLevel.Good else VideoSetting.NetworkLevel.Normal

        device?.let {
            val deviceLevel = when (it) {
                // 低端机
                0 -> {
                    VideoSetting.DeviceLevel.Low
                }
                // 中端机
                1 -> {
                    VideoSetting.DeviceLevel.Medium
                }
                // 高端机
                2 -> {
                    VideoSetting.DeviceLevel.High
                }
                else -> {
                    VideoSetting.DeviceLevel.High
                }
            }

            VideoSetting.updateBroadcastSetting(deviceLevel, network, broadcastStrategy, isJoinedRoom = false, isByAudience = false, rtcConnection)
            ToastDialog(context).apply {
                dismissDelayShort()
                showMessage(context.getString(R.string.show_setting_preset_done))
            }
            return
        }

        val deviceLevel = if (deviceScore >= 90) {
            VideoSetting.DeviceLevel.High
        } else if (deviceScore >= 75) {
            VideoSetting.DeviceLevel.Medium
        } else {
            VideoSetting.DeviceLevel.Low
        }

        VideoSetting.updateBroadcastSetting(deviceLevel, network, broadcastStrategy, isJoinedRoom = false, isByAudience = false, rtcConnection)
        ToastDialog(context).apply {
            dismissDelayShort()
            showMessage(context.getString(R.string.show_setting_preset_done))
        }
    }
}