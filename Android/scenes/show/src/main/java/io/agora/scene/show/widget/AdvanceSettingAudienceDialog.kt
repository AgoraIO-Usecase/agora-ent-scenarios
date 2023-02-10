package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.R
import io.agora.scene.show.VideoSetting
import io.agora.scene.show.databinding.ShowSettingAdvanceDialogAudienceBinding
import io.agora.scene.show.databinding.ShowSettingAdvanceItemSwitchBinding

/**
 * 高级设置弹窗
 */
class AdvanceSettingAudienceDialog(context: Context) : BottomFullDialog(context) {

    companion object {
        private const val ITEM_ID_SWITCH_BASE = 0x00000001
        const val ITEM_ID_SWITCH_QUALITY_ENHANCE = ITEM_ID_SWITCH_BASE + 1

    }

    private val mBinding by lazy {
        ShowSettingAdvanceDialogAudienceBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private val defaultItemValues = mutableMapOf<Int, Int>().apply {
        put(
            ITEM_ID_SWITCH_QUALITY_ENHANCE,
            VideoSetting.getCurrAudienceSetting().video.SR.value
        )
    }

    init {
        setContentView(mBinding.root)

        mBinding.ivBack.setOnClickListener {
            dismiss()
        }
        setupSwitchItem(ITEM_ID_SWITCH_QUALITY_ENHANCE, mBinding.qualityEnhance, R.string.show_setting_advance_quality_enhance, View.NO_ID)
    }

    private fun setupSwitchItem(
        itemId: Int,
        binding: ShowSettingAdvanceItemSwitchBinding,
        @StringRes title: Int,
        @StringRes tip: Int
    ) {
        binding.tvTitle.text = context.getString(title)
        binding.ivTip.isVisible = tip != View.NO_ID
        binding.ivTip.setOnClickListener {
            ToastDialog(context).showTip(context.getString(tip))
        }
        binding.switchCompat.setOnCheckedChangeListener(null)
        binding.switchCompat.isChecked = VideoSetting.getCurrAudienceEnhanceSwitch()
        onSwitchChanged(itemId, binding.switchCompat.isChecked)
        binding.switchCompat.setOnCheckedChangeListener { btn, isChecked ->
            if (isChecked && (VideoSetting.getCurrAudiencePlaySetting() == VideoSetting.AudiencePlaySetting.BASE_LOW || VideoSetting.getCurrAudiencePlaySetting() == VideoSetting.AudiencePlaySetting.ENHANCE_LOW)) {
                binding.switchCompat.isChecked = false
                ToastUtils.showToast(context.getString(R.string.show_setting_quality_enhance_tip))
                return@setOnCheckedChangeListener
            }
            defaultItemValues[itemId] = if (isChecked) 1 else 0
            onSwitchChanged(itemId, isChecked)
        }
    }


    private fun onSwitchChanged(itemId: Int, isChecked: Boolean) {
        when (itemId) {
            ITEM_ID_SWITCH_QUALITY_ENHANCE -> {
                VideoSetting.setCurrAudienceEnhanceSwitch(isChecked)
                VideoSetting.updateAudioSetting(SR = VideoSetting.getCurrAudienceSetting().video.SR)
            }
        }
    }

}

