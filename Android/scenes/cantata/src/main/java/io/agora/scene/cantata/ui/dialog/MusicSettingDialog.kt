package io.agora.scene.cantata.ui.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.UiUtil
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataDialogMusicSettingBinding

interface MusicSettingCallback {
    fun onEarChanged(isEar: Boolean)
    fun onMicVolChanged(vol: Int)
    fun onMusicVolChanged(vol: Int)
    fun onEffectChanged(effect: Int)
    fun onBeautifierPresetChanged(effect: Int)
    fun setAudioEffectParameters(param1: Int, param2: Int)
    fun onToneChanged(newToneValue: Int)
    fun onRemoteVolumeChanged(volume: Int)
}

/**
 * 控制台
 */
class MusicSettingDialog constructor(private val mSetting: MusicSettingBean, private val isPause: Boolean) :
    BaseBottomSheetDialogFragment<CantataDialogMusicSettingBinding>() {

    companion object {
        const val TAG = "MusicSettingDialog"
    }
    private fun getCurrentPitch(value: Int): Int {
        return when (value) {
            12 -> 11
            10 -> 10
            8 -> 9
            6 -> 8
            4 -> 7
            2 -> 6
            -2 -> 4
            -4 -> 3
            -6 -> 2
            -8 -> 1
            -10 -> 0
            -12 -> -1
            0 -> 5
            else -> 5
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let { window ->
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v: View?, insets: WindowInsetsCompat ->
                val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                mBinding?.root?.apply {
                    setPadding(inset.left, 0, inset.right, inset.bottom)
                }
                WindowInsetsCompat.CONSUMED
            }
        }


        // 升降调
        tuningTone(null)
        mBinding?.apply {
            switchEar.isChecked = mSetting.isEar()
            sbVol1.progress = mSetting.getVolMic()
            sbVol2.progress = mSetting.getVolMusic()
            changeToneView.currentPitch = getCurrentPitch(mSetting.getToneValue())
            setSoundMode(this)
            btnToneDownDialogSetting.setOnClickListener { v: View? -> tuningTone(false) }
            btnToneUpDialogSetting.setOnClickListener { v: View? -> tuningTone(true) }
            if (isPause) {
                textRemoteVolume.isEnabled = false
                btnRemoteVolumeUpDialogSetting.isEnabled = false
                btnRemoteVolumeDownDialogSetting.isEnabled = false
                textRemoteVolume.setText("" + 100)
            } else {
                textRemoteVolume.isEnabled = true
                btnRemoteVolumeUpDialogSetting.isEnabled = true
                btnRemoteVolumeDownDialogSetting.isEnabled = true
                textRemoteVolume.setText("" + mSetting.remoteVolume)
            }
            switchEar.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                mSetting.setEar(isChecked)
            }
            sbVol1.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mSetting.setVolMic(i)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            sbVol2.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    mSetting.setVolMusic(i)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            btnRemoteVolumeDownDialogSetting.setOnClickListener { v: View? ->
                val volume = mSetting.remoteVolume
                val newVolume = volume - 1
                textRemoteVolume.setText("" + newVolume)
            }
            btnRemoteVolumeUpDialogSetting.setOnClickListener { v: View? ->
                val volume = mSetting.remoteVolume
                val newVolume = volume + 1
                textRemoteVolume.setText("" + newVolume)
            }
            textRemoteVolume.doAfterTextChanged { editable ->
                if (!editable.isNullOrEmpty()) {
                    val text = editable.toString()
                    var newVolume = text.toInt()
                    if (newVolume < 0) {
                        newVolume = 0
                    } else if (newVolume > 100) {
                        newVolume = 100
                    }
                    mSetting.remoteVolume = newVolume
                    textRemoteVolume.hint = "" + newVolume
                } else {
                    mSetting.remoteVolume = 15
                    textRemoteVolume.hint = "" + 15
                }
            }
        }

    }

    private fun setSoundMode(binding:CantataDialogMusicSettingBinding) {
        val margin = UiUtil.dp2px(10)
        val stringArray = resources.getStringArray(R.array.cantata_audioPreset)
        for (i in stringArray.indices) {
            val radioButton = layoutInflater.inflate(R.layout.cantata_btn_sound_mode, null) as RadioButton
            radioButton.text = stringArray[i]
            if (i % 4 == 0) {
                radioButton.setBackgroundResource(R.drawable.bg_rbtn_select_sound_mode4)
            } else if (i % 3 == 0) {
                radioButton.setBackgroundResource(R.drawable.bg_rbtn_select_sound_mode3)
            } else if (i % 2 == 0) {
                radioButton.setBackgroundResource(R.drawable.bg_rbtn_select_sound_mode2)
            } else {
                radioButton.setBackgroundResource(R.drawable.bg_rbtn_select_sound_mode1)
            }
            binding.radioGroup.addView(radioButton)
            (radioButton.layoutParams as LinearLayout.LayoutParams).setMargins(margin, 0, 0, 0)
            if (0 == i) {
                radioButton.isChecked = true
            } else if (i == stringArray.size - 1) {
                (radioButton.layoutParams as LinearLayout.LayoutParams).setMargins(margin, 0, margin, 0)
            }
            radioButton.setOnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
                if (b) {
                    mSetting.effect = i
                }
            }
        }
        (binding.radioGroup.getChildAt(mSetting.effect) as? RadioButton)?.isChecked = true
    }

    /**
     * IMediaPlayer.java
     * / **
     * Sets the pitch of the current media file.
     * pitch Sets the pitch of the local music file by chromatic scale. The default value is 0,
     * which means keeping the original pitch. The value ranges from -12 to 12, and the pitch value
     * between consecutive values is a chromatic value. The greater the absolute value of this
     * parameter, the higher or lower the pitch of the local music file.
     * *
     * - 0: Success.
     * - < 0: Failure.
     * int setAudioMixingPitch(int pitch);
     *
     * @param toneUp true -> +1 | false -> -1 | null -> update value
     */
    private fun tuningTone(toneUp: Boolean?) {
        var newToneValue = mSetting.getToneValue()
        toneUp?.let { tone ->
            if (tone) {
                mBinding?.changeToneView?.currentPitchPlus()
                newToneValue += 2
            } else {
                newToneValue -= 2
                mBinding?.changeToneView?.currentPitchMinus()
            }
            if (newToneValue > 12) newToneValue = 12
            if (newToneValue < -12) newToneValue = -12
            if (newToneValue != mSetting.getToneValue()) mSetting.setToneValue(newToneValue)
        }
        //        mBinding.textToneDialogSetting.setText(String.valueOf(newToneValue));
    }

    fun onStopPlayer() {
        mBinding?.textRemoteVolume?.hint = "" + 100
    }

    fun onResumePlayer() {
        mBinding?.textRemoteVolume?.hint = "" + mSetting.remoteVolume
    }


}