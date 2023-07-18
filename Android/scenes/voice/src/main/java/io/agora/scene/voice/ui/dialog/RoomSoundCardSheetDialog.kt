package io.agora.scene.voice.ui.dialog

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
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogVirtualSoundCardBinding
import io.agora.scene.voice.rtckit.AgoraPresetSound
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.rtckit.AgoraSoundCardManager
import io.agora.scene.voice.rtckit.PresetSoundModel
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import io.agora.voice.common.utils.onStopTrackingTouch

class RoomSoundCardSheetDialog constructor() : BaseSheetDialog<VoiceDialogVirtualSoundCardBinding>() {

    private val mReceiver = HeadphoneReceiver()

    // 有无有线耳机
    private var isPlugIn = false

    var mOnSoundCardChange: (() -> Unit)? = null

    private fun appContext(): Application = AgoraApplication.the()

    private val soundCardManager: AgoraSoundCardManager by lazy {
        AgoraRtcEngineController.get().soundCardManager()
    }

    private val presetSoundArray: Array<PresetSoundModel> = arrayOf(
        PresetSoundModel(
            AgoraPresetSound.Oba, appContext().getString(R.string.voice_chatroom_preset_sound_oba),
            appContext().getString(R.string.voice_chatroom_preset_sound_oba_tips), R.drawable.avatar1
        ),
        PresetSoundModel(
            AgoraPresetSound.Lady, appContext().getString(R.string.voice_chatroom_preset_sound_lady),
            appContext().getString(R.string.voice_chatroom_preset_sound_lady_tips), R.drawable.avatar1
        ),
        PresetSoundModel(
            AgoraPresetSound.Boy, appContext().getString(R.string.voice_chatroom_preset_sound_boy),
            appContext().getString(R.string.voice_chatroom_preset_sound_boy_tips), R.drawable.avatar1
        ),
        PresetSoundModel(
            AgoraPresetSound.Sweet, appContext().getString(R.string.voice_chatroom_preset_sound_sweet),
            appContext().getString(R.string.voice_chatroom_preset_sound_sweet_tips), R.drawable.avatar1
        )
    )

    private var curPresetSoundModel = presetSoundArray[0]

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogVirtualSoundCardBinding {
        return VoiceDialogVirtualSoundCardBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context?.registerReceiver(mReceiver, filter)
        binding?.apply {
            setOnApplyWindowInsets(root)
        }
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isPlugIn = audioManager.isWiredHeadsetOn
        soundCardManager.enable(isPlugIn, true, callback = {
            mOnSoundCardChange?.invoke()
        })
        initView()
    }

    private fun initView() {
        binding?.apply {
            if (soundCardManager.isEnable()) {
                groupSoundCardSwitch.visibility = View.VISIBLE
                groupSoundCardSettings.visibility = View.VISIBLE
                groupSoundCardAbnormal.isVisible = false
                mcbSoundCardSwitch.isChecked = true
                setupPresetSoundView(soundCardManager.presetSound())
                setupGainView(soundCardManager.gainValue())
                setupPresetView(soundCardManager.presetValue())
            } else {
                groupSoundCardSwitch.visibility = View.VISIBLE
                groupSoundCardSettings.visibility = View.INVISIBLE
                groupSoundCardAbnormal.visibility = View.INVISIBLE
                mcbSoundCardSwitch.isChecked = false
            }

            ivBottomSheetBack.setOnClickListener {
                onHandleOnBackPressed()
            }
            mcbSoundCardSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    groupSoundCardSettings.visibility = View.VISIBLE
                } else {
                    groupSoundCardSettings.visibility = View.INVISIBLE
                }
                soundCardManager.enable(isChecked, force = true, callback = {
                    if (isChecked) {
                        setupPresetSoundView(soundCardManager.presetSound())
                        setupGainView(soundCardManager.gainValue())
                        setupPresetView(soundCardManager.presetValue())
                    }
                    mOnSoundCardChange?.invoke()
                })
            }
            pbGainAdjustValue.onStopTrackingTouch {
                it?.progress?.let { progress ->
                    val gainValue: Float = progress / 10.0f
                    mtGainAdjustValue.text = gainValue.toString()
                    soundCardManager.setGainValue(gainValue)
                }
            }
            pbMircoPhoneTypeValue.onStopTrackingTouch {
                it?.progress?.let { progress ->
                    val presetValue: Int = progress
                    mtMircoPhoneTypeValue.text = presetValue.toString()
                    soundCardManager.setPresetValue(presetValue)
                }
            }
            val context = spinnerPresetSound.context
            spinnerPresetSound.adapter =
                ArrayAdapter(context, android.R.layout.simple_list_item_1, presetSoundArray)
            spinnerPresetSound.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val presetSoundModel = presetSoundArray[position]
                    if (curPresetSoundModel == presetSoundModel) return
                    curPresetSoundModel = presetSoundModel
                    spinnerPresetSound.setSelection(-1)
                    soundCardManager.setPresetSound(curPresetSoundModel.type, callback = {
                        setupPresetSoundView(soundCardManager.presetSound())
                        setupGainView(soundCardManager.gainValue())
                        setupPresetView(soundCardManager.presetValue())
                    })
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // nothing
                }
            }
        }
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
                AgoraPresetSound.Oba -> {
                    mtPresetSoundType.setText(R.string.voice_chatroom_preset_sound_oba)
                    mtPresetSoundTypeTips.setText(R.string.voice_chatroom_preset_sound_oba_tips)
                }

                AgoraPresetSound.Boy -> {
                    mtPresetSoundType.setText(R.string.voice_chatroom_preset_sound_boy)
                    mtPresetSoundTypeTips.setText(R.string.voice_chatroom_preset_sound_boy_tips)
                }

                AgoraPresetSound.Lady -> {
                    mtPresetSoundType.setText(R.string.voice_chatroom_preset_sound_lady)
                    mtPresetSoundTypeTips.setText(R.string.voice_chatroom_preset_sound_lady_tips)
                }

                AgoraPresetSound.Sweet -> {
                    mtPresetSoundType.setText(R.string.voice_chatroom_preset_sound_sweet)
                    mtPresetSoundTypeTips.setText(R.string.voice_chatroom_preset_sound_sweet_tips)
                }
            }
        }
    }

    override fun onDestroy() {
        context?.unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun dismiss() {
        super.dismiss()
    }

    private fun setHeadPhonePlugin(isPlug: Boolean) {
        if (isPlugIn != isPlug) {
            isPlugIn = isPlug
            if (isPlugIn) {
                // 插入有线耳机
                binding?.groupSoundCardSwitch?.visibility = View.VISIBLE
                binding?.groupSoundCardSettings?.visibility = View.VISIBLE
                binding?.groupSoundCardAbnormal?.visibility = View.INVISIBLE
                binding?.mcbSoundCardSwitch?.isChecked = true

            } else {
                // 未插入有线耳机
                binding?.groupSoundCardSwitch?.visibility = View.INVISIBLE
                binding?.groupSoundCardSettings?.visibility = View.INVISIBLE
                binding?.groupSoundCardAbnormal?.visibility = View.VISIBLE
                binding?.mcbSoundCardSwitch?.isChecked = false
            }
            soundCardManager.enable(isPlugIn, force = true, callback = {
                if (isPlugIn){
                    setupPresetSoundView(soundCardManager.presetSound())
                    setupGainView(soundCardManager.gainValue())
                    setupPresetView(soundCardManager.presetValue())
                }
            })
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