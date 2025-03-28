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
 * Ear back fragment
 *
 * @property mSetting
 * @constructor Create empty Ear back fragment
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
        return  KtvDialogEarbackSettingBinding.inflate(layoutInflater,viewGroup,false)
    }

    override fun initView() {
        super.initView()
        binding?.apply {
            if (mSetting.mEarBackEnable) {
                enableDisableView(layoutEarBackVol,true)
                layoutEarBackVol.alpha = 1.0f
            } else {
                enableDisableView(layoutEarBackVol,false)
                layoutEarBackVol.alpha = 0.3f
            }
            //隐藏耳返模式
            layoutEarBackMode.visibility = View.INVISIBLE
            //隐藏耳返延时
            cvEarBackdelay.visibility = View.INVISIBLE
            btEarBackDown.setOnClickListener { v: View? -> tuningEarVolume(false) }
            btEarBackUp.setOnClickListener { v: View? -> tuningEarVolume(true) }
            sbEarBack.progress = mSetting.mEarBackVolume
            sbEarBack.doOnProgressChanged  { seekBar, progress, fromUser ->
                if (fromUser) {
                    mSetting.mEarBackVolume = progress
                }
            }
            when (mSetting.mEarBackMode) {
                EarBackMode.Auto -> rgEarBackMode.check(R.id.tvModeAuto)
                EarBackMode.OpenSL -> rgEarBackMode.check(R.id.tvModeOpenSL)
                else -> rgEarBackMode.check(R.id.tvModeOboe)
            }
            rgEarBackMode.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
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

    private fun enableDisableView(viewGroup: ViewGroup, enable: Boolean) {
        for (idx in 0 until viewGroup.childCount) {
            viewGroup.getChildAt(idx).isEnabled = enable
        }
    }

    private fun updateEarPhoneStatus(hasEarPhone: Boolean) {
        binding?.apply {
            if (hasEarPhone) {
                // 检测到耳机
                cbSwitch.isEnabled = true
                cbSwitch.alpha = 1.0f
                cbSwitch.isChecked = mSetting.mEarBackEnable
                cbSwitch.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                    mSetting.mEarBackEnable = isChecked
                    if (isChecked) {
                        enableDisableView(layoutEarBackVol,true)
                        layoutEarBackVol.alpha = 1.0f
                    } else {
                        enableDisableView(layoutEarBackVol,false)
                        layoutEarBackVol.alpha = 0.3f
                    }
                }
                tvTips1.visibility = View.VISIBLE
                tvTips2.visibility = View.VISIBLE
                tvTipsNoEarPhone.visibility = View.GONE
            } else {
                // 未检测到耳机
                cbSwitch.isEnabled = false
                cbSwitch.alpha = 0.3f
                mSetting.mEarBackEnable = false
                cbSwitch.isChecked = false
                tvTips1.visibility = View.GONE
                tvTips2.visibility = View.GONE
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
                    setDescText(getString(R.string.ktv_earback_opensl_tips))
                } else if (mode == EarBackMode.Oboe) {
                    setDescText(getString(R.string.ktv_earback_oboe_tips))
                }
                setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm))
                onButtonClickListener = object : OnButtonClickListener {
                    override fun onLeftButtonClick() {
                        this@EarBackFragment.binding?.apply {
                            when (mSetting.mEarBackMode) {
                                EarBackMode.Auto -> rgEarBackMode.check(R.id.tvModeAuto)
                                EarBackMode.OpenSL -> rgEarBackMode.check(R.id.tvModeOpenSL)
                                else -> rgEarBackMode.check(R.id.tvModeOboe)
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

/**
 * Ear phone callback
 *
 * @constructor Create empty Ear phone callback
 */
interface EarPhoneCallback {
    /**
     * On has ear phone changed
     *
     * @param hasEarPhone
     */
    fun onHasEarPhoneChanged(hasEarPhone: Boolean)

    /**
     * On ear monitor delay
     *
     * @param earsBackDelay
     */
    fun onEarMonitorDelay(earsBackDelay: Int)
}
