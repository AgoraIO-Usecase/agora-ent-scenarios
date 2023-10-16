package io.agora.scene.ktv.live.fragment.dialog

import android.app.Application
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.core.view.isVisible
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvDialogSoundCardBinding
import io.agora.scene.ktv.live.AgoraPresetSound
import io.agora.scene.ktv.live.PresetSoundModel
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

    var onClickMicType: (() -> Unit)? = null

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
            if (true) {
                groupSoundCardSwitch.visibility = View.VISIBLE
                groupSoundCardSettings.visibility = if (soundCardSetting.isEnable()) View.VISIBLE else View.INVISIBLE
                groupSoundCardAbnormal.isVisible = false
                mcbSoundCardSwitch.isChecked = soundCardSetting.isEnable()
                if (soundCardSetting.isEnable()) {
                    setupPresetSoundView(soundCardSetting.presetSound())
                    setupGainView(soundCardSetting.gainValue())
                    setupPresetView(soundCardSetting.presetValue())
                }
            } else {
                groupSoundCardSwitch.visibility = View.INVISIBLE
                groupSoundCardSettings.visibility = View.INVISIBLE
                groupSoundCardAbnormal.visibility = View.VISIBLE
                mcbSoundCardSwitch.isChecked = false
            }

            ivBackIcon.setOnClickListener {
                (requireActivity() as RoomLivingActivity).closeMusicSettingsDialog()
            }
            mcbSoundCardSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    groupSoundCardSettings.visibility = View.VISIBLE
                } else {
                    groupSoundCardSettings.visibility = View.INVISIBLE
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
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.progress?.let { progress ->
                        val gainValue: Float = progress / 10.0f
                        mtGainAdjustValue.text = gainValue.toString()
                        soundCardSetting.setGainValue(gainValue)
                    }
                }
            })
            mtMicTypeSelect.setOnClickListener {
                onClickMicType?.invoke()
            }
            tvSoundTypeSelect.setOnClickListener {
                onClickSoundCardType?.invoke()
            }
        }
    }

    override fun initListener() {
        super.initListener()
    }

    private fun setupGainView(gainValue: Float) {
        binding?.apply {
            pbGainAdjustValue.progress = (gainValue * 10).toInt()
            mtGainAdjustValue.text = gainValue.toString()
        }
    }

    private fun setupPresetView(presetValue: Int) {
        binding?.apply {
            mtMicTypeSelect.text = presetValue.toString()
        }
    }

    private fun setupPresetSoundView(presetSound: AgoraPresetSound) {
        binding?.apply {
            when (presetSound) {
                AgoraPresetSound.Sound2001 -> {
                    val text = "${R.string.ktv_preset_sound_dashu}（${R.string.ktv_preset_sound_dashu_tips}）"
                    tvSoundType.text = text
                }
                AgoraPresetSound.Sound2002 -> {
                    val text = "${R.string.ktv_preset_sound_mum}（${R.string.ktv_preset_sound_mum_tips}）"
                    tvSoundType.text = text
                }
                AgoraPresetSound.Sound2003 -> {
                    val text = "${R.string.ktv_preset_sound_qingshu}（${R.string.ktv_preset_sound_qingshu_tips}）"
                    tvSoundType.text = text
                }
                AgoraPresetSound.Sound2004 -> {
                    val text = "${R.string.ktv_preset_sound_yuma}（${R.string.ktv_preset_sound_yuma_tips}）"
                    tvSoundType.text = text
                }
                AgoraPresetSound.Sound2005 -> {
                    val text = "${R.string.ktv_preset_sound_qingnian}（${R.string.ktv_preset_sound_qingnian_tips}）"
                    tvSoundType.text = text
                }
                AgoraPresetSound.Sound2006 -> {
                    val text = "${R.string.ktv_preset_sound_shaoyu}（${R.string.ktv_preset_sound_shaoyu_tips}）"
                    tvSoundType.text = text
                }
            }
        }
    }


    private fun setHeadPhonePlugin(isPlug: Boolean) {
        if (isPlugIn != isPlug) {
            isPlugIn = isPlug
            if (isPlugIn) {
                // 插入有线耳机
                binding.groupSoundCardSwitch.visibility = View.VISIBLE
                binding.groupSoundCardSettings.visibility = View.VISIBLE
                binding.groupSoundCardAbnormal.visibility = View.INVISIBLE
                binding.mcbSoundCardSwitch.isChecked = soundCardSetting.isEnable()
            } else {
                // 未插入有线耳机
                binding.groupSoundCardSwitch.visibility = View.INVISIBLE
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