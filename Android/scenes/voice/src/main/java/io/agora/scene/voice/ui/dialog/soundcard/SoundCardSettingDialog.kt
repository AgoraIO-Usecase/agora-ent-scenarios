package io.agora.scene.voice.ui.dialog.soundcard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import io.agora.scene.voice.rtckit.AgoraPresetSound
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogSoundCardBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.rtckit.AgoraSoundCardManager
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class SoundCardSettingDialog: BaseSheetDialog<VoiceDialogSoundCardBinding>() {

    companion object {
        const val TAG: String = "SoundCardFragment"
    }

    private val mReceiver = HeadphoneReceiver()

    private lateinit var mManager: AgoraSoundCardManager

    // 有无有线耳机
    private var isPlugIn = false

    var mOnSoundCardChange: (() -> Unit)? = null

    var onClickSoundCardType: (() -> Unit)? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogSoundCardBinding {
        return VoiceDialogSoundCardBinding.inflate(inflater)
    }

    override fun onDestroyView() {
        context?.unregisterReceiver(mReceiver)
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mManager = AgoraRtcEngineController.get().soundCardManager() ?: run {
            dismiss()
            return
        }
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isPlugIn = audioManager.isWiredHeadsetOn
        super.onViewCreated(view, savedInstanceState)
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context?.registerReceiver(mReceiver, filter)
        setupView()
    }

    private fun setupView() {
        binding?.apply {
            if (isPlugIn) {
                groupSoundCardSettings.visibility = View.VISIBLE
                if (mManager.isEnable()) {
                    setupPresetSoundView(mManager.presetSound())
                    setupGainView(mManager.gainValue())
                    setupPresetView(mManager.presetValue())
                    vPramsMark.visibility = View.INVISIBLE
                    clSoundCardParams.alpha = 1f
                } else {
                    vPramsMark.visibility = View.VISIBLE
                    clSoundCardParams.alpha = 0.4f
                }
                groupSoundCardAbnormal.isVisible = false
                mcbSoundCardSwitch.isChecked = mManager.isEnable()
            } else {
                groupSoundCardSettings.visibility = View.INVISIBLE
                clSoundCardParams.visibility = if (mManager.isEnable()) View.VISIBLE else View.INVISIBLE
                groupSoundCardAbnormal.visibility = View.VISIBLE
                mcbSoundCardSwitch.isChecked = false
            }
            ivBackIcon.setOnClickListener {
                dismiss()
            }
            mcbSoundCardSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    vPramsMark.visibility = View.INVISIBLE
                    clSoundCardParams.alpha = 1f
                } else {
                    vPramsMark.visibility = View.VISIBLE
                    clSoundCardParams.alpha = 0.4f
                }
                mManager.enable(isChecked, force = true, callback = {
                    if (isChecked) {
                        setupPresetSoundView(mManager.presetSound())
                        setupGainView(mManager.gainValue())
                        setupPresetView(mManager.presetValue())
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
                        mManager.setGainValue(gainValue)
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
                        tvMicType.text = if (micType == -1) getString(R.string.voice_sound_preset_off) else micType.toString()
                        mManager.setPresetValue(micType)
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
                            mManager.setGainValue(value)
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
            tvMicType.text = if (presetValue == -1) getString(R.string.voice_sound_preset_off) else presetValue.toString()
        }
    }

    private fun setupPresetSoundView(presetSound: AgoraPresetSound) {
        binding?.apply {
            when (presetSound) {
                AgoraPresetSound.Sound2001 -> {
                    val text = "${getString(R.string.voice_preset_sound_dashu)}（${getString(R.string.voice_preset_sound_dashu_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2002 -> {
                    val text = "${getString(R.string.voice_preset_sound_mum)}（${getString(R.string.voice_preset_sound_mum_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2003 -> {
                    val text = "${getString(R.string.voice_preset_sound_qingshu)}（${getString(R.string.voice_preset_sound_qingshu_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2004 -> {
                    val text = "${getString(R.string.voice_preset_sound_yuma)}（${getString(R.string.voice_preset_sound_yuma_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2005 -> {
                    val text = "${getString(R.string.voice_preset_sound_qingnian)}（${getString(R.string.voice_preset_sound_qingnian_tips)}）"
                    tvSoundTypeSelect.text = text
                }
                AgoraPresetSound.Sound2006 -> {
                    val text = "${getString(R.string.voice_preset_sound_shaoyu)}（${getString(R.string.voice_preset_sound_shaoyu_tips)}）"
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
            binding?.let { binding ->
                if (isPlugIn) {
                    // 插入有线耳机
                    binding.groupSoundCardSettings.visibility = View.VISIBLE
                    binding.groupSoundCardAbnormal.visibility = View.INVISIBLE
                    binding.mcbSoundCardSwitch.isChecked = mManager.isEnable()
                } else {
                    // 未插入有线耳机
                    binding.groupSoundCardSettings.visibility = View.INVISIBLE
                    binding.groupSoundCardAbnormal.visibility = View.VISIBLE
                    binding.mcbSoundCardSwitch.isChecked = false
                    mManager.enable(false, force = true, callback = {
                    })
                }
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