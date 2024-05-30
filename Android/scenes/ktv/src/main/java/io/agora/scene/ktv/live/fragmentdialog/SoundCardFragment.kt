package io.agora.scene.ktv.live.fragmentdialog

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvDialogSoundCardBinding
import io.agora.scene.ktv.live.RoomLivingActivity
import io.agora.scene.ktv.live.bean.SoundCardSettingBean

/**
 * Sound card fragment
 *
 * @property soundCardSetting
 * @constructor Create empty Sound card fragment
 */
class SoundCardFragment constructor(private val soundCardSetting: SoundCardSettingBean) :
    BaseViewBindingFragment<KtvDialogSoundCardBinding>() {

    companion object {
        const val TAG: String = "SoundCardFragment"
    }

    var mOnSoundCardChange: (() -> Unit)? = null

    var onClickSoundCardType: (() -> Unit)? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): KtvDialogSoundCardBinding {
        return KtvDialogSoundCardBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()
        binding?.apply {
            groupSoundCardSettings.visibility = View.VISIBLE
            if (soundCardSetting.isEnable()) {
                setupPresetSoundView(soundCardSetting.presetSound())
                setupGainView(soundCardSetting.gainValue())
                setupPresetView(soundCardSetting.presetValue())
                vPramsMark.visibility = View.INVISIBLE
                clSoundCardParams.alpha = 1f
            } else {
                vPramsMark.visibility = View.VISIBLE
                clSoundCardParams.alpha = 0.4f
            }
            groupSoundCardAbnormal.isVisible = false
            mcbSoundCardSwitch.isChecked = soundCardSetting.isEnable()

            ivBackIcon.setOnClickListener {
                (requireActivity() as RoomLivingActivity).closeMusicSettingsDialog()
            }
            mcbSoundCardSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    vPramsMark.visibility = View.INVISIBLE
                    clSoundCardParams.alpha = 1f
                } else {
                    vPramsMark.visibility = View.VISIBLE
                    clSoundCardParams.alpha = 0.4f
                }
                soundCardSetting.enable(isChecked, force = true, callback = {
                    if (isChecked) {
                        setupPresetSoundView(soundCardSetting.presetSound())
                        setupGainView(soundCardSetting.gainValue())
                        setupPresetView(soundCardSetting.presetValue())
                    }
                    mOnSoundCardChange?.invoke()
                })
            }
            pbGainAdjust.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.progress?.let { progress ->
                        val gainValue: Float = progress / 10.0f
                        etGainAdjust.setText(gainValue.toString())
                        soundCardSetting.setGainValue(gainValue)
                    }
                }
            })
            vPramsMark.setOnClickListener {
                // 空实现阻挡事件传递
            }
            tvSoundTypeSelect.setOnClickListener {
                onClickSoundCardType?.invoke()
            }
            pbMicType.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.progress?.let { value ->
                        val micType = value - 1
                        tvMicType.text = if (micType == -1) getString(R.string.ktv_sound_preset_off) else micType.toString()
                        soundCardSetting.setPresetValue(micType)
                    }
                }
            })
            activity?.window?.let { window ->
                val initialWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                root.viewTreeObserver.addOnGlobalLayoutListener {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val currentWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
                        if (currentWindowHeight < initialWindowHeight) {
                            Log.d(TAG, "current: $currentWindowHeight, initial: $initialWindowHeight, show: true")
                        } else {
                            var value = 1f
                            try {
                                val input = etGainAdjust.text.toString()
                                value = input.toFloat()
                            } catch (e: NumberFormatException) {}
                            if (value < 0) {
                                value = 1f
                            } else if (value > 4) {
                                value = 4f
                            }
                            setupGainView(value)
                            etGainAdjust.clearFocus()
                            soundCardSetting.setGainValue(value)
                        }
                    }, 300)
                }
            }
        }
    }

    private fun setupGainView(gainValue: Float) {
        binding?.apply {
            pbGainAdjust.progress = (gainValue * 10).toInt()
            etGainAdjust.setText(gainValue.toString())
        }
    }

    private fun setupPresetView(presetValue: Int) {
        binding?.apply {
            val sliderValue = presetValue + 1
            pbMicType.progress = sliderValue
            tvMicType.text = if (presetValue == -1) getString(R.string.ktv_sound_preset_off) else presetValue.toString()
        }
    }

    private fun setupPresetSoundView(presetSound: AgoraPresetSound) {
        binding?.apply {
            when (presetSound) {
                AgoraPresetSound.Sound2001 -> {
                    val text = "${getString(R.string.ktv_preset_sound_dashu)}（${getString(R.string.ktv_preset_sound_dashu_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2002 -> {
                    val text = "${getString(R.string.ktv_preset_sound_mum)}（${getString(R.string.ktv_preset_sound_mum_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2003 -> {
                    val text = "${getString(R.string.ktv_preset_sound_qingshu)}（${getString(R.string.ktv_preset_sound_qingshu_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2004 -> {
                    val text = "${getString(R.string.ktv_preset_sound_yuma)}（${getString(R.string.ktv_preset_sound_yuma_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2005 -> {
                    val text = "${getString(R.string.ktv_preset_sound_qingnian)}（${getString(R.string.ktv_preset_sound_qingnian_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2006 -> {
                    val text = "${getString(R.string.ktv_preset_sound_shaoyu)}（${getString(R.string.ktv_preset_sound_shaoyu_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                else -> {
                    tvSoundTypeSelect.text = ""
                }
            }
        }
    }


}

/**
 * Preset sound model
 *
 * @property type
 * @property name
 * @property tips
 * @property resId
 * @constructor Create empty Preset sound model
 */
data class PresetSoundModel constructor(
    val type: AgoraPresetSound,
    val name: String,
    val tips: String,
    @DrawableRes val resId: Int
) {
    override fun toString(): String {
        return name
    }
}

/**
 * Agora preset sound
 *
 * @property presetValue
 * @property gainValue
 * @property gender
 * @property effect
 * @constructor Create empty Agora preset sound
 */
enum class AgoraPresetSound constructor(
    val presetValue: Int,
    val gainValue: Float,
    val gender: Int,
    val effect: Int,
) {
    /**
     * Close
     *
     * @constructor Create empty Close
     */
    Close(-1,-1f,-1,-1),

    /**
     * Sound1001
     *
     * @constructor Create empty Sound1001
     */
    Sound1001(4,1f,0,0),

    /**
     * Sound1002
     *
     * @constructor Create empty Sound1002
     */
    Sound1002(4,1f,0,1),

    /**
     * Sound1003
     *
     * @constructor Create empty Sound1003
     */
    Sound1003(4,1f,1,0),

    /**
     * Sound1004
     *
     * @constructor Create empty Sound1004
     */
    Sound1004(4,1f,1,1),

    /**
     * Sound2001
     *
     * @constructor Create empty Sound2001
     */
    Sound2001(4,1f,0,2),

    /**
     * Sound2002
     *
     * @constructor Create empty Sound2002
     */
    Sound2002(4,1f,1,2),

    /**
     * Sound2003
     *
     * @constructor Create empty Sound2003
     */
    Sound2003(4,1f,0,3),

    /**
     * Sound2004
     *
     * @constructor Create empty Sound2004
     */
    Sound2004(4,1f,1,3),

    /**
     * Sound2005
     *
     * @constructor Create empty Sound2005
     */
    Sound2005(4,1f,0,4),

    /**
     * Sound2006
     *
     * @constructor Create empty Sound2006
     */
    Sound2006(4,1f,1,4)
}