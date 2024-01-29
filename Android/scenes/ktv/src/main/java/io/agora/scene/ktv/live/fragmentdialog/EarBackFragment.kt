package io.agora.scene.ktv.live.fragmentdialog

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import io.agora.scene.base.component.BaseBindingFragment
import io.agora.scene.base.component.OnButtonClickListener
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvDialogEarbackSettingBinding
import io.agora.scene.ktv.live.RoomLivingActivity
import io.agora.scene.ktv.live.bean.EarBackMode
import io.agora.scene.ktv.live.bean.MusicSettingBean
import io.agora.scene.widget.dialog.CommonDialog
import io.agora.scene.widget.doOnProgressChanged

/**
 * 耳返设置
 */
class EarBackFragment constructor(private val mSetting: MusicSettingBean) :
    BaseBindingFragment<KtvDialogEarbackSettingBinding?>() {

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

    override fun getViewBinding(layoutInflater: LayoutInflater, viewGroup: ViewGroup?): KtvDialogEarbackSettingBinding {
        return KtvDialogEarbackSettingBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        binding?.apply {
            if (mSetting.mEarBackEnable) {
                vSettingMark.visibility = View.INVISIBLE
                vPingMark.visibility = View.INVISIBLE
            } else {
                vSettingMark.visibility = View.VISIBLE
                vPingMark.visibility = View.VISIBLE
                vSettingMark.setOnClickListener { v: View? -> }
            }
            btEarBackDown.setOnClickListener { v: View? -> tuningEarVolume(false) }
            btEarBackUp.setOnClickListener { v: View? -> tuningEarVolume(true) }
            sbEarBack.progress = mSetting.mEarBackVolume
            sbEarBack.doOnProgressChanged { seekBar, progress, fromUser ->
                if (fromUser) {
                    mSetting.mEarBackVolume = progress
                }
            }
            when (mSetting.mEarBackMode) {
                EarBackMode.Auto -> rgMode.check(R.id.tvModeAuto)
                EarBackMode.OpenSL -> rgMode.check(R.id.tvModeOpenSL)
                else -> rgMode.check(R.id.tvModeOboe)
            }
            rgMode.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
                when (checkedId) {
                    R.id.tvModeAuto -> mSetting.mEarBackMode = EarBackMode.Auto
                    R.id.tvModeOpenSL -> showSwitchEarModeDialog(EarBackMode.OpenSL)
                    else -> showSwitchEarModeDialog(EarBackMode.Oboe)
                }
            }
            ivBackIcon.setOnClickListener { view: View? -> (requireActivity() as RoomLivingActivity).closeMusicSettingsDialog() }
        }
        updateEarPhoneStatus(mSetting.mHasEarPhone)
        updateEarPhoneDelay(mSetting.mEarBackDelay)
    }

    private fun updateEarPhoneStatus(hasEarPhone: Boolean) {
        binding?.apply {
            if (hasEarPhone) {
                // 检测到耳机
                cbSwitch.isEnabled = true
                cbSwitch.isChecked = mSetting.mEarBackEnable
                cbSwitch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                    mSetting.mEarBackEnable = isChecked
                    if (isChecked) {
                        vSettingMark.visibility = View.INVISIBLE
                        vPingMark.visibility = View.INVISIBLE
                    } else {
                        vSettingMark.visibility = View.VISIBLE
                        vPingMark.visibility = View.VISIBLE
                        vSettingMark.setOnClickListener { v: View? -> }
                    }
                }
                tvTips1.visibility = View.VISIBLE
                tvTips2.visibility = View.VISIBLE
                ivTips2.visibility = View.VISIBLE
                clSettings.visibility = View.VISIBLE
                tvTipsNoEarPhone.visibility = View.INVISIBLE
            } else {
                // 未检测到耳机
                cbSwitch.isEnabled = false
                mSetting.mEarBackEnable = false
                cbSwitch.isChecked = false
                tvTips1.visibility = View.INVISIBLE
                tvTips2.visibility = View.INVISIBLE
                ivTips2.visibility = View.INVISIBLE
                clSettings.visibility = View.INVISIBLE
                tvTipsNoEarPhone.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateEarPhoneDelay(earPhoneDelay: Int) {
        binding?.apply {
            pbPing.progress = earPhoneDelay
            tvPing.text = "${earPhoneDelay}ms"
            if (earPhoneDelay <= 50) {
                pbPing.progressDrawable = root.resources.getDrawable(R.drawable.ktv_ear_ping_progress_good)
            } else if (earPhoneDelay <= 100) {
                pbPing.progressDrawable = root.resources.getDrawable(R.drawable.ktv_ear_ping_progress)
            } else {
                pbPing.progressDrawable = root.resources.getDrawable(R.drawable.ktv_ear_ping_progress_bad)
            }
        }
    }

    private fun tuningEarVolume(volumeUp: Boolean) {
        var earBackVolume = mSetting.mEarBackVolume
        if (volumeUp) {
            earBackVolume += 1
        } else {
            earBackVolume -= 1
        }
        if (earBackVolume > 100) earBackVolume = 100
        if (earBackVolume < 0) earBackVolume = 0
        mSetting.mEarBackVolume = earBackVolume
        binding?.apply {
            sbEarBack.progress = earBackVolume
        }
    }

    private fun showSwitchEarModeDialog(mode: EarBackMode) {
        val context = context ?: return
        if (switchEarModeDialog == null) {
            switchEarModeDialog = CommonDialog(context).apply {
                setDialogTitle(getString(R.string.ktv_notice))
                if (mode == EarBackMode.OpenSL) {
                    setDescText(getString(R.string.ktv_ear_back_opensl))
                } else if (mode == EarBackMode.Oboe) {
                    setDescText(getString(R.string.ktv_ear_back_oboe))
                }
                setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        this@EarBackFragment.binding?.apply {
                            when (mSetting.mEarBackMode) {
                                EarBackMode.Auto -> rgMode.check(R.id.tvModeAuto)
                                EarBackMode.OpenSL -> rgMode.check(R.id.tvModeOpenSL)
                                else -> rgMode.check(R.id.tvModeOboe)
                            }
                        }
                    }

                    override fun onRightButtonClick() {
                        mSetting.mEarBackMode = mode
                    }
                }
            }
        }
        switchEarModeDialog?.show()
    }
}

interface EarPhoneCallback {
    fun onHasEarPhoneChanged(hasEarPhone: Boolean)
    fun onEarMonitorDelay(earsBackDelay: Int)
}
