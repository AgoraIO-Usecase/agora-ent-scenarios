package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import io.agora.scene.show.R
import io.agora.scene.show.VideoSetting
import io.agora.scene.show.databinding.ShowSettingAdvanceDialogAudienceBinding
import io.agora.scene.show.databinding.ShowSettingAdvanceItemSwitchBinding
import io.agora.scene.show.utils.toInt

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
            VideoSetting.getCurrAudienceSetting().video.SR.toInt()
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
        binding.switchCompat.isChecked = (defaultItemValues[itemId] ?: 0) > 0
        onSwitchChanged(itemId, binding.switchCompat.isChecked)
        binding.switchCompat.setOnCheckedChangeListener { btn, isChecked ->
            defaultItemValues[itemId] = if (isChecked) 1 else 0
            onSwitchChanged(itemId, isChecked)
        }
    }


    private fun onSwitchChanged(itemId: Int, isChecked: Boolean) {
        when (itemId) {
            ITEM_ID_SWITCH_QUALITY_ENHANCE -> VideoSetting.updateAudioSetting(SR = isChecked)
        }
    }

}

