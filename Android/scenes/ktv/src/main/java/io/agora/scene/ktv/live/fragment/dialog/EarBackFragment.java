package io.agora.scene.ktv.live.fragment.dialog;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnButtonClickListener;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvDialogEarbackSettingBinding;
import io.agora.scene.ktv.widget.MusicSettingBean;
import io.agora.scene.widget.dialog.CommonDialog;

public class EarBackFragment extends BaseViewBindingFragment<KtvDialogEarbackSettingBinding> {
    public static final String TAG = "EarBackFragment";

    private final MusicSettingBean mSetting;
    private CommonDialog switchEarModeDialog;

    public EarBackFragment(MusicSettingBean mSetting) {
        this.mSetting = mSetting;
        mSetting.setEarPhoneCallback(new MusicSettingBean.EarPhoneCallback() {
            @Override
            public void onHasEarPhoneChanged(boolean hasEarPhone) {
                updateEarPhoneStatus(hasEarPhone);
            }

            @Override
            public void onEarMonitorDelay(int earsBackDelay) {
                updateEarPhoneDelay(earsBackDelay);
            }
        });
    }

    @NonNull
    @Override
    protected KtvDialogEarbackSettingBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        return KtvDialogEarbackSettingBinding.inflate(layoutInflater);
    }

    @Override
    public void initListener() {
        super.initListener();

        updateEarPhoneStatus(mSetting.hasEarPhone());
        if (this.mSetting.isEar()) {
            getBinding().vSettingMark.setVisibility(View.INVISIBLE);
            getBinding().vPingMark.setVisibility(View.INVISIBLE);
        } else {
            getBinding().vSettingMark.setVisibility(View.VISIBLE);
            getBinding().vPingMark.setVisibility(View.VISIBLE);
            getBinding().vSettingMark.setOnClickListener(v -> {});
        }

        getBinding().btEarBackDown.setOnClickListener(v -> tuningEarVolume(false));
        getBinding().btEarBackUp.setOnClickListener(v -> tuningEarVolume(true));
        getBinding().sbEarBack.setProgress(this.mSetting.getEarBackVolume());
        getBinding().sbEarBack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSetting.setEarBackVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (this.mSetting.getEarBackMode() == 0) {
            getBinding().rgMode.check(R.id.tvModeAuto);
        } else if (this.mSetting.getEarBackMode() == 1) {
            getBinding().rgMode.check(R.id.tvModeOpenSL);
        } else {
            getBinding().rgMode.check(R.id.tvModeOboe);
        }
        getBinding().rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.tvModeAuto) {
                mSetting.setEarBackMode(0);
            } else if (checkedId == R.id.tvModeOpenSL) {
                showSwitchEarModeDialog(1);
            } else {
                showSwitchEarModeDialog(2);
            }
        });

        updateEarPhoneDelay(mSetting.getEarBackDelay());
    }

    private void updateEarPhoneStatus(boolean hasEarPhone) {
        if (hasEarPhone) {
            // 检测到耳机
            getBinding().cbSwitch.setChecked(mSetting.isEar());
            getBinding().cbSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mSetting.setEar(isChecked);
                if (isChecked) {
                    getBinding().vSettingMark.setVisibility(View.INVISIBLE);
                    getBinding().vPingMark.setVisibility(View.INVISIBLE);
                } else {
                    getBinding().vSettingMark.setVisibility(View.VISIBLE);
                    getBinding().vPingMark.setVisibility(View.VISIBLE);
                    getBinding().vSettingMark.setOnClickListener(v -> {});
                }
            });
            getBinding().tvTips.setVisibility(View.VISIBLE);
            getBinding().tvTipsNoEarPhone.setVisibility(View.INVISIBLE);
        } else {
            // 未检测到耳机
            getBinding().cbSwitch.setEnabled(false);
            mSetting.setEar(false);
            getBinding().cbSwitch.setChecked(false);
            getBinding().tvTips.setVisibility(View.INVISIBLE);
            getBinding().tvTipsNoEarPhone.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateEarPhoneDelay(int earPhoneDelay) {
        getBinding().pbPing.setProgress(earPhoneDelay);
        getBinding().tvPing.setText(earPhoneDelay + "ms");
        if (earPhoneDelay <= 50) {
            getBinding().pbPing.setProgressDrawable(getBinding().getRoot().getResources().getDrawable(R.drawable.ktv_ear_ping_progress_good));
        } else if (earPhoneDelay <= 100) {
            getBinding().pbPing.setProgressDrawable(getBinding().getRoot().getResources().getDrawable(R.drawable.ktv_ear_ping_progress));
        } else {
            getBinding().pbPing.setProgressDrawable(getBinding().getRoot().getResources().getDrawable(R.drawable.ktv_ear_ping_progress_bad));
        }
    }

    private void tuningEarVolume(boolean volumeUp) {
        int earBackVolume = this.mSetting.getEarBackVolume();
        if (volumeUp) {
            earBackVolume += 1;
        } else {
            earBackVolume -= 1;
        }
        if (earBackVolume > 100)
            earBackVolume = 100;

        if (earBackVolume < 0)
            earBackVolume = 0;
        if (earBackVolume != this.mSetting.getEarBackVolume()) {
            this.mSetting.setEarBackVolume(earBackVolume);
        }
        getBinding().sbEarBack.setProgress(earBackVolume);
    }

    private void showSwitchEarModeDialog(int mode) {
        if (switchEarModeDialog == null) {
            switchEarModeDialog = new CommonDialog(getContext());

            switchEarModeDialog.setDialogTitle("提示");
            if (mode == 1) {
                switchEarModeDialog.setDescText("切换后将强制使用OpenSL模式，\n确认？");
            } else if (mode == 2) {
                switchEarModeDialog.setDescText("切换后将强制使用Oboe模式，\n确认？");
            }
            switchEarModeDialog.setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm));
            switchEarModeDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    if (mSetting.getEarBackMode() == 0) {
                        getBinding().rgMode.check(R.id.tvModeAuto);
                    } else if (mSetting.getEarBackMode() == 1) {
                        getBinding().rgMode.check(R.id.tvModeOpenSL);
                    } else {
                        getBinding().rgMode.check(R.id.tvModeOboe);
                    }
                }

                @Override
                public void onRightButtonClick() {
                    mSetting.setEarBackMode(mode);
                }
            });
        }
        switchEarModeDialog.show();
    }
}
