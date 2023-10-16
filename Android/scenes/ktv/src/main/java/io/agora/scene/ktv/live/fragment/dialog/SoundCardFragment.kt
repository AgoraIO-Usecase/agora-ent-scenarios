package io.agora.scene.ktv.live.fragment.dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvDialogSoundCardBinding
import io.agora.scene.ktv.live.AgoraPresetSound
import io.agora.scene.ktv.live.RoomLivingActivity

class SoundCardFragment constructor(private val soundCardSetting: SoundCardSettingBean) :
    BaseViewBindingFragment<KtvDialogSoundCardBinding>() {

    companion object {
        const val TAG: String = "SoundCardFragment"
    }

    private val mReceiver = HeadphoneReceiver()

    // 有无有线耳机
    private var isPlugIn = false

    var mOnSoundCardChange: (() -> Unit)? = null

    var onClickSoundCardType: (() -> Unit)? = null

    init {
        soundCardSetting.setEarPhoneCallback(object : EarPhoneCallback {
            override fun onHasEarPhoneChanged(hasEarPhone: Boolean) {
                view?.post { setHeadPhonePlugin(hasEarPhone) }
            }
        })
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): KtvDialogSoundCardBinding {
        return KtvDialogSoundCardBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isPlugIn = audioManager.isWiredHeadsetOn
        super.onViewCreated(view, savedInstanceState)
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context?.registerReceiver(mReceiver, filter)
    }

    override fun onDestroyView() {
        context?.unregisterReceiver(mReceiver)
        super.onDestroyView()
    }

    override fun initView() {
        super.initView()
        binding?.apply {
            if (isPlugIn) {
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
            } else {
                groupSoundCardSettings.visibility = View.INVISIBLE
                clSoundCardParams.visibility = if (soundCardSetting.isEnable()) View.VISIBLE else View.INVISIBLE
                groupSoundCardAbnormal.visibility = View.VISIBLE
                mcbSoundCardSwitch.isChecked = false
            }

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
            pbGainAdjustValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.progress?.let { progress ->
                        val gainValue: Float = progress / 10.0f
                        tvGainAdjustValue.text = gainValue.toString()
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
        }
    }

    override fun initListener() {
        super.initListener()
    }

    private fun setupGainView(gainValue: Float) {
        binding?.apply {
            pbGainAdjustValue.progress = (gainValue * 10).toInt()
            tvGainAdjustValue.text = gainValue.toString()
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

    private fun setHeadPhonePlugin(isPlug: Boolean) {
        if (isPlugIn != isPlug) {
            isPlugIn = isPlug
            if (isPlugIn) {
                // 插入有线耳机
                binding.groupSoundCardSettings.visibility = View.VISIBLE
                binding.groupSoundCardAbnormal.visibility = View.INVISIBLE
                binding.mcbSoundCardSwitch.isChecked = soundCardSetting.isEnable()
            } else {
                // 未插入有线耳机
                binding.groupSoundCardSettings.visibility = View.INVISIBLE
                binding.groupSoundCardAbnormal.visibility = View.VISIBLE
                binding.mcbSoundCardSwitch.isChecked = false
                soundCardSetting.enable(false, force = true, callback = {
                })
            }
        }
    }

    private inner class HeadphoneReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_HEADSET_PLUG) return
            val state = intent.getIntExtra("state", -1)
            if (state == 1) {
                setHeadPhonePlugin(true)
                mOnSoundCardChange?.invoke()
                //耳机插入
                Log.d("HeadphoneReceiver", "headphone plugged in")
            } else if (state == 0) {
                setHeadPhonePlugin(false)
                mOnSoundCardChange?.invoke()
                //耳机拔出
                Log.d("HeadphoneReceiver", "headphone removed")
            }
        }
    }
}