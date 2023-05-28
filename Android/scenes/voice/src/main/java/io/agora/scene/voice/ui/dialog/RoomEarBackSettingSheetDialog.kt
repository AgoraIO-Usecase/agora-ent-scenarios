package io.agora.scene.voice.ui.dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomEarbackSettingBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class RoomEarBackSettingSheetDialog: BaseSheetDialog<VoiceDialogChatroomEarbackSettingBinding>() {

    private val mReceiver = HeadphoneReceiver()

    private var mOnEarBackStateChange: (() -> Unit)? = null
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomEarbackSettingBinding {
        return VoiceDialogChatroomEarbackSettingBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        context?.unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation

        setupHeadPhoneReceiver()
        setupView()
    }
    fun setOnEarBackStateChange(action: (() -> Unit)?) {
        mOnEarBackStateChange = action
    }
    private fun setupView() {
        val earBackManager = AgoraRtcEngineController.get().earBackManager
        setSwitchOn(earBackManager.params.isOn)
        binding?.cbSwitch?.setOnCheckedChangeListener { _, isOn ->
            setSwitchOn(isOn)
            earBackManager.setOn(isOn)
        }
        binding?.slVolume?.max = 100
        binding?.slVolume?.progress = earBackManager.params.volume
        binding?.tvVolume?.text = "${earBackManager.params.volume}"
        binding?.slVolume?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                AgoraRtcEngineController.get().bgmManager.setVolume(p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
                // Do Noting
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                // Do Noting
            }
        })
        val checked = when (earBackManager.params.mode) {
            0 -> R.id.tvModeOpenSL
            1 -> R.id.tvModeAuto
            else -> R.id.tvModeOboe
        }
        binding?.rgMode?.check(checked)
        binding?.rgMode?.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.tvModeAuto -> earBackManager.params.mode = 0
                R.id.tvModeOpenSL -> earBackManager.params.mode = 1
                R.id.tvModeOboe -> earBackManager.params.mode = 2
            }
        }
    }

    private fun setupHeadPhoneReceiver() {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context?.registerReceiver(mReceiver, filter)

        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setHeadPhonePlugin(audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn)
    }

    private fun setHeadPhonePlugin(isOn: Boolean) {
        val c = context ?: return
        if (isOn) {
            binding?.vSettingMark?.visibility = View.INVISIBLE
            binding?.tvTips?.setTextColor(ContextCompat.getColor(c, R.color.voice_dark_grey_color_979cbb))
            binding?.cbSwitch?.isEnabled = true
        } else {
            binding?.vSettingMark?.visibility = View.VISIBLE
            binding?.tvTips?.setTextColor(Color.rgb(255, 18, 22))
            binding?.cbSwitch?.isEnabled = false
        }
    }

    private fun setSwitchOn(isOn: Boolean) {
        binding?.cbSwitch?.isChecked = isOn
        binding?.vPingMark?.visibility = if (isOn) View.INVISIBLE else View.VISIBLE
    }

    private fun setPing(value: Int) {
        binding?.pbPing?.progress = value

        val drawable = binding?.pbPing?.progressDrawable
        if (value < 60) {
            drawable?.colorFilter = PorterDuffColorFilter(Color.parseColor("#57D73E"), PorterDuff.Mode.SRC_IN)
        } else if (value < 120) {
            drawable?.colorFilter = PorterDuffColorFilter(Color.parseColor("#FAAD15"), PorterDuff.Mode.SRC_IN)
        } else {
            drawable?.colorFilter = PorterDuffColorFilter(Color.parseColor("#FF1216"), PorterDuff.Mode.SRC_IN)
        }
    }

    private inner class HeadphoneReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", -1)
                if (state == 1) {
                    setHeadPhonePlugin(true)
                    //耳机插入
                    Log.d("HeadphoneReceiver", "headphone plugged in")
                } else if (state == 0) {
                    setHeadPhonePlugin(false)
                    //耳机拔出
                    Log.d("HeadphoneReceiver", "headphone removed")
                }
            }
        }
    }
}
