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
import androidx.fragment.app.FragmentManager
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomEarbackSettingBinding
import io.agora.scene.voice.rtckit.AgoraEarBackMode
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.ui.dialog.common.CommonFragmentAlertDialog
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class RoomEarBackSettingSheetDialog: BaseSheetDialog<VoiceDialogChatroomEarbackSettingBinding>() {

    private val mReceiver = HeadphoneReceiver()

    private var mAlertFragmentManager: FragmentManager? = null

    private var mOnEarBackStateChange: (() -> Unit)? = null

    private var mSetBack = false

    var isPlugIn = false
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
        updateViewState()
    }
    fun setOnEarBackStateChange(action: (() -> Unit)?) {
        mOnEarBackStateChange = action
    }
    fun setFragmentManager(fragmentManager: FragmentManager) {
        mAlertFragmentManager = fragmentManager
    }
    private fun setupView() {
        val earBackManager = AgoraRtcEngineController.get().earBackManager() ?: return
        binding?.cbSwitch?.setOnCheckedChangeListener { _, isOn ->
            earBackManager.setOn(isOn)
            updateViewState()
        }
        binding?.slVolume?.max = 100
        binding?.slVolume?.progress = earBackManager.params.volume
        binding?.tvVolume?.text = "${earBackManager.params.volume}"
        binding?.slVolume?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding?.tvVolume?.text = "$p1"
                earBackManager.setVolume(p1)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
                // Do Noting
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                // Do Noting
            }
        })
        updateModeSegment()
//        binding?.rgMode?.setOnCheckedChangeListener { _, i ->
//            val mode = when (i) {
//                R.id.tvModeAuto -> AgoraEarBackMode.Default
//                R.id.tvModeOpenSL -> AgoraEarBackMode.OpenSL
//                R.id.tvModeOboe -> AgoraEarBackMode.Oboe
//                else -> AgoraEarBackMode.Default
//            }
//            showDialogWithMode(mode)
//        }
        setPing(earBackManager.params.delay)
        earBackManager.setOnEarBackDelayChanged { value ->
            binding?.root?.post {
                setPing(value)
            }
        }
        // 给maskView 添加事件阻止交互
        binding?.vSettingMark?.setOnClickListener {
        }
        binding?.vPingMark?.setOnClickListener {
        }
    }
    private fun showDialogWithMode(mode: AgoraEarBackMode) {
        if (mSetBack) {
            mSetBack = false
            return
        }
        if (mode == AgoraEarBackMode.Default) {
            AgoraRtcEngineController.get().earBackManager()?.setMode(mode)
            return
        }
        val c = context ?: return
        val f = mAlertFragmentManager ?: return
        val content = when (mode) {
            AgoraEarBackMode.OpenSL -> "切换后将强制使用OpenSL模式，确认？"
            AgoraEarBackMode.Oboe -> "切换后将强制使用Oboe模式，确认？"
            else -> ""
        }
        CommonFragmentAlertDialog().titleText(c.getString(R.string.voice_chatroom_prompt))
            .contentText(content).leftText(c.getString(R.string.voice_room_cancel))
            .rightText(c.getString(R.string.voice_room_confirm))
            .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    AgoraRtcEngineController.get().earBackManager()?.setMode(mode)
                }
                override fun onCancelClick() {
                    updateModeSegment()
                }
            }).show(f, "botActivatedDialog")
    }

    private fun setupHeadPhoneReceiver() {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        context?.registerReceiver(mReceiver, filter)

        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setHeadPhonePlugin(audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn)
    }

    private fun setHeadPhonePlugin(isPlug: Boolean) {
        if (isPlugIn != isPlug) {
            isPlugIn = isPlug
            if (!isPlug) { AgoraRtcEngineController.get().earBackManager()?.setOn(false) }
            updateViewState()
        }
    }

    private fun updateModeSegment() {
//        mSetBack = true
//        when (AgoraRtcEngineController.get().earBackManager().params.mode) {
//            AgoraEarBackMode.Default -> binding?.tvModeAuto?.isChecked = true
//            AgoraEarBackMode.OpenSL -> binding?.tvModeOpenSL?.isChecked = true
//            else -> binding?.tvModeOboe?.isChecked = true
//        }
    }

    private fun updateViewState() {
        val c = context ?: return
        if (!isPlugIn) {
            binding?.cbSwitch?.isChecked = false
            binding?.cbSwitch?.isEnabled = false
            binding?.tvTips?.setTextColor(Color.rgb(255, 18, 22))

            binding?.tvTips?.text = getString(R.string.voice_chatroom_settings_earback_waring)
            binding?.vSettingMark?.visibility = View.VISIBLE
            binding?.clSetting?.alpha = 0.3f
            binding?.vPingMark?.visibility = View.VISIBLE
            return
        }
        binding?.tvTips?.setTextColor(ContextCompat.getColor(c, R.color.voice_dark_grey_color_979cbb))
        binding?.tvTips?.text = getString(R.string.voice_chatroom_settings_earback_tip)
        binding?.cbSwitch?.isEnabled = true
        val isOn = AgoraRtcEngineController.get().earBackManager()?.params?.isOn ?: false
        binding?.cbSwitch?.isChecked = isOn
        binding?.vSettingMark?.visibility = if (isOn) View.INVISIBLE else View.VISIBLE
        binding?.clSetting?.alpha = if (isOn) 1f else 0.3f
//        binding?.vPingMark?.visibility = if (isOn) View.INVISIBLE else View.VISIBLE
        binding?.vPingMark?.visibility = View.VISIBLE
    }

    private fun setPing(value: Int) {
        binding?.pbPing?.progress = value
        binding?.tvPing?.text = "$value ms"

        val drawable = binding?.pbPing?.progressDrawable
        if (value <= 50) {
            drawable?.colorFilter = PorterDuffColorFilter(Color.parseColor("#57D73E"), PorterDuff.Mode.SRC_IN)
        } else if (value <= 99) {
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
