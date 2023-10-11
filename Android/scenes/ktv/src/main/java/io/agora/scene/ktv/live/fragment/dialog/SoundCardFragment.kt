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

    private fun appContext(): Application = AgoraApplication.the()

    private val presetSoundArray: Array<PresetSoundModel> = arrayOf(
        PresetSoundModel(
            AgoraPresetSound.Uncle, appContext().getString(R.string.ktv_preset_sound_uncle),
            appContext().getString(R.string.ktv_preset_sound_uncle_tips), io.agora.scene.base.R.mipmap.portrait01
        ),
        PresetSoundModel(
            AgoraPresetSound.Announcer, appContext().getString(R.string.ktv_preset_sound_announcer),
            appContext().getString(R.string.ktv_preset_sound_announcer_tips), io.agora.scene.base.R.mipmap.portrait01
        ),
        PresetSoundModel(
            AgoraPresetSound.Oba, appContext().getString(R.string.ktv_preset_sound_oba),
            appContext().getString(R.string.ktv_preset_sound_oba_tips), io.agora.scene.base.R.mipmap.portrait01
        ),
        PresetSoundModel(
            AgoraPresetSound.Lady, appContext().getString(R.string.ktv_preset_sound_lady),
            appContext().getString(R.string.ktv_preset_sound_lady_tips), io.agora.scene.base.R.mipmap.portrait01
        ),
        PresetSoundModel(
            AgoraPresetSound.Boy, appContext().getString(R.string.ktv_preset_sound_boy),
            appContext().getString(R.string.ktv_preset_sound_boy_tips), io.agora.scene.base.R.mipmap.portrait01
        ),
        PresetSoundModel(
            AgoraPresetSound.Sweet, appContext().getString(R.string.ktv_preset_sound_sweet),
            appContext().getString(R.string.ktv_preset_sound_sweet_tips), io.agora.scene.base.R.mipmap.portrait01
        )
    )

    private var curPresetSoundModel = presetSoundArray[0]

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
            pbMircoPhoneTypeValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.progress?.let { progress ->
                        val presetValue: Int = progress
                        mtMircoPhoneTypeValue.text = presetValue.toString()
                        soundCardSetting.setPresetValue(presetValue)
                    }
                }

            })
            val context = spinnerPresetSound.context
            spinnerPresetSound.adapter =
                ArrayAdapter(context, android.R.layout.simple_list_item_1, presetSoundArray)
            spinnerPresetSound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val presetSoundModel = presetSoundArray[position]
                    if (curPresetSoundModel == presetSoundModel) return
                    curPresetSoundModel = presetSoundModel
                    spinnerPresetSound.setSelection(-1)
                    soundCardSetting.setPresetSound(curPresetSoundModel.type, callback = {
                        setupPresetSoundView(soundCardSetting.presetSound())
                        setupGainView(soundCardSetting.gainValue())
                        setupPresetView(soundCardSetting.presetValue())
                    })
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // nothing
                }
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
            pbMircoPhoneTypeValue.progress = presetValue
            mtMircoPhoneTypeValue.text = presetValue.toString()
        }
    }

    private fun setupPresetSoundView(presetSound: AgoraPresetSound) {
        binding?.apply {
            when (presetSound) {
                AgoraPresetSound.Uncle -> {
                    mtPresetSoundType.setText(R.string.ktv_preset_sound_uncle)
                    mtPresetSoundTypeTips.setText(R.string.ktv_preset_sound_uncle_tips)
                }

                AgoraPresetSound.Announcer -> {
                    mtPresetSoundType.setText(R.string.ktv_preset_sound_announcer)
                    mtPresetSoundTypeTips.setText(R.string.ktv_preset_sound_announcer_tips)
                }

                AgoraPresetSound.Oba -> {
                    mtPresetSoundType.setText(R.string.ktv_preset_sound_oba)
                    mtPresetSoundTypeTips.setText(R.string.ktv_preset_sound_oba_tips)
                }

                AgoraPresetSound.Boy -> {
                    mtPresetSoundType.setText(R.string.ktv_preset_sound_boy)
                    mtPresetSoundTypeTips.setText(R.string.ktv_preset_sound_boy_tips)
                }

                AgoraPresetSound.Lady -> {
                    mtPresetSoundType.setText(R.string.ktv_preset_sound_lady)
                    mtPresetSoundTypeTips.setText(R.string.ktv_preset_sound_lady_tips)
                }

                AgoraPresetSound.Sweet -> {
                    mtPresetSoundType.setText(R.string.ktv_preset_sound_sweet)
                    mtPresetSoundTypeTips.setText(R.string.ktv_preset_sound_sweet_tips)
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