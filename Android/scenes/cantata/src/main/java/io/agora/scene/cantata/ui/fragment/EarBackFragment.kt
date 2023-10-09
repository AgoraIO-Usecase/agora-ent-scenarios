package io.agora.scene.cantata.ui.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataDialogEarbackSettingBinding
import io.agora.scene.cantata.databinding.CantataFragmentSongListBinding
import io.agora.scene.cantata.ui.activity.RoomLivingActivity
import io.agora.scene.cantata.ui.dialog.EarPhoneCallback
import io.agora.scene.cantata.ui.dialog.MusicSettingBean
import io.agora.scene.widget.dialog.CommonDialog

class EarBackFragment constructor(private val mSetting: MusicSettingBean) :
    BaseViewBindingFragment<CantataDialogEarbackSettingBinding>() {

    companion object {
        const val TAG = "EarBackFragment"
    }

    private var switchEarModeDialog: CommonDialog? = null


    init {
        mSetting.mEarPhoneCallback = object : EarPhoneCallback {
            override fun onHasEarPhoneChanged(hasEarPhone: Boolean) {
                view?.post { updateEarPhoneStatus(hasEarPhone) }
            }

            override fun onEarMonitorDelay(earsBackDelay: Int) {
                view?.post { updateEarPhoneDelay(earsBackDelay) }
            }
        }
    }

    override fun getViewBinding(layoutInflater: LayoutInflater, viewGroup: ViewGroup?): CantataDialogEarbackSettingBinding {
        return CantataDialogEarbackSettingBinding.inflate(layoutInflater)
    }


    override fun initListener() {
        super.initListener()
        updateEarPhoneStatus(mSetting.hasEarPhone)
        if (mSetting.isEar()) {
            binding.vSettingMark.visibility = View.INVISIBLE
            binding.vPingMark.visibility = View.INVISIBLE
        } else {
            binding.vSettingMark.visibility = View.VISIBLE
            binding.vPingMark.visibility = View.VISIBLE
            binding.vSettingMark.setOnClickListener { v: View? -> }
        }
        binding.btEarBackDown.setOnClickListener { v: View? -> tuningEarVolume(false) }
        binding.btEarBackUp.setOnClickListener { v: View? -> tuningEarVolume(true) }
        binding.sbEarBack.progress = mSetting.earBackVolume
        binding.sbEarBack.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mSetting.earBackVolume = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        when (mSetting.earBackMode) {
            0 -> {
                binding.rgMode.check(R.id.tvModeAuto)
            }
            1 -> {
                binding.rgMode.check(R.id.tvModeOpenSL)
            }
            else -> {
                binding.rgMode.check(R.id.tvModeOboe)
            }
        }
        binding.rgMode.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.tvModeAuto -> {
                    mSetting.earBackMode = 0
                }
                R.id.tvModeOpenSL -> {
                    showSwitchEarModeDialog(1)
                }
                else -> {
                    showSwitchEarModeDialog(2)
                }
            }
        }
        updateEarPhoneDelay(mSetting.earBackDelay)
        binding.ivBackIcon.setOnClickListener { view: View? -> (requireActivity() as RoomLivingActivity).closeMusicSettingsDialog() }
    }

    private fun updateEarPhoneStatus(hasEarPhone: Boolean) {
        if (hasEarPhone) {
            // 检测到耳机
            binding.cbSwitch.isEnabled = true
            binding.cbSwitch.isChecked = mSetting.isEar()
            binding.cbSwitch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                mSetting.setEar(isChecked)
                if (isChecked) {
                    binding.vSettingMark.visibility = View.INVISIBLE
                    binding.vPingMark.visibility = View.INVISIBLE
                } else {
                    binding.vSettingMark.visibility = View.VISIBLE
                    binding.vPingMark.visibility = View.VISIBLE
                    binding.vSettingMark.setOnClickListener { v: View? -> }
                }
            }
            binding.tvTips.visibility = View.VISIBLE
            binding.tvTipsNoEarPhone.visibility = View.INVISIBLE
        } else {
            // 未检测到耳机
            binding.cbSwitch.isEnabled = false
            mSetting.setEar(false)
            binding.cbSwitch.isChecked = false
            binding.tvTips.visibility = View.INVISIBLE
            binding.tvTipsNoEarPhone.visibility = View.VISIBLE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateEarPhoneDelay(earPhoneDelay: Int) {
        binding.pbPing.progress = earPhoneDelay
        binding.tvPing.text = earPhoneDelay.toString() + "ms"
        if (earPhoneDelay <= 50) {
            binding.pbPing.progressDrawable =
                binding.root.resources.getDrawable(R.drawable.cantata_ear_ping_progress_good)
        } else if (earPhoneDelay <= 100) {
            binding.pbPing.progressDrawable =
                binding.root.resources.getDrawable(R.drawable.cantata_ear_ping_progress)
        } else {
            binding.pbPing.progressDrawable =
                binding.root.resources.getDrawable(R.drawable.cantata_ear_ping_progress_bad)
        }
    }

    private fun tuningEarVolume(volumeUp: Boolean) {
        var earBackVolume = mSetting.earBackVolume
        if (volumeUp) {
            earBackVolume += 1
        } else {
            earBackVolume -= 1
        }
        if (earBackVolume > 100) earBackVolume = 100
        if (earBackVolume < 0) earBackVolume = 0
        if (earBackVolume != mSetting.earBackVolume) {
            mSetting.earBackVolume = earBackVolume
        }
        binding.sbEarBack.progress = earBackVolume
    }

    private fun showSwitchEarModeDialog(mode: Int) {
        val cxt = context?:return
        if (switchEarModeDialog == null) {
            switchEarModeDialog = CommonDialog(cxt).apply {
                setDialogTitle("提示")
                if (mode == 1) {
                    setDescText("切换后将强制使用OpenSL模式，\n确认？")
                } else if (mode == 2) {
                    setDescText("切换后将强制使用Oboe模式，\n确认？")
                }
                setDialogBtnText(
                    getString(R.string.cantata_cancel),
                    getString(R.string.cantata_confirm)
                )
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        when (mSetting.earBackMode) {
                            0 -> {
                                this@EarBackFragment.binding.rgMode.check(R.id.tvModeAuto)
                            }
                            1 -> {
                                this@EarBackFragment.binding.rgMode.check(R.id.tvModeOpenSL)
                            }
                            else -> {
                                this@EarBackFragment.binding.rgMode.check(R.id.tvModeOboe)
                            }
                        }
                    }

                    override fun onRightButtonClick() {
                        mSetting.earBackMode = mode
                    }
                }
            }

        }
        switchEarModeDialog?.show()
    }


}