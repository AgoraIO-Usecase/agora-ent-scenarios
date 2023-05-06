package io.agora.scene.ktv.grasp.live.fragment.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.ktv.grasp.databinding.FragmentEffectVoiceBinding;
import io.agora.scene.ktv.grasp.live.RoomLivingActivity;
import io.agora.scene.ktv.grasp.widget.MusicSettingBean;

public class EffectVoiceFragment extends BaseViewBindingFragment<FragmentEffectVoiceBinding> {
    public static final String TAG = "EffectVoiceFragment";
    private final MusicSettingBean mSetting;

    @NonNull
    @Override
    protected FragmentEffectVoiceBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        return FragmentEffectVoiceBinding.inflate(layoutInflater);
    }

    public EffectVoiceFragment(MusicSettingBean mSetting) {
        this.mSetting = mSetting;
    }

    private int params1 = 0;

    @Override
    public void initListener() {
        if (!AgoraApplication.the().isDebugModeOpen()) {
            getBinding().cbAudioDumpText.setVisibility(View.GONE);
            getBinding().cbStartAudioDump.setVisibility(View.GONE);
        }

        getBinding().ivBackIcon.setOnClickListener(view -> {
            ((RoomLivingActivity) requireActivity()).closeMenuDialog();
        });
        getBinding().cbStartElectricSound.setOnCheckedChangeListener((compoundButton, b) -> {
            getBinding().cbGentleWind.setEnabled(b);
            getBinding().cbMajor.setEnabled(b);
            getBinding().cbMinor.setEnabled(b);
            if (b) {
                mSetting.setAudioEffectParameters(params1, 4);
            } else {
                mSetting.setAudioEffectParameters(0, 4);
            }
        });

        getBinding().cbGentleWind.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                params1 = 3;
                getBinding().ivRoundBG.setRotation(0);
                getBinding().cbMinor.setChecked(false);
                getBinding().cbMajor.setChecked(false);
                mSetting.setAudioEffectParameters(params1, 4);
            }
        });
        getBinding().cbMinor.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                params1 = 2;
                getBinding().ivRoundBG.setRotation(90);
                getBinding().cbGentleWind.setChecked(false);
                getBinding().cbMajor.setChecked(false);
                mSetting.setAudioEffectParameters(params1, 4);
            }
        });
        getBinding().cbMajor.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                params1 = 1;
                getBinding().ivRoundBG.setRotation(180);
                getBinding().cbGentleWind.setChecked(false);
                getBinding().cbMinor.setChecked(false);
                mSetting.setAudioEffectParameters(params1, 4);
            }
        });
        params1 = mSetting.getAudioEffectParams1();
        getBinding().cbStartElectricSound.setChecked(mSetting.getAudioEffectParams1() != 0);
        if (mSetting.getAudioEffectParams1() == 3) {
            getBinding().cbGentleWind.setChecked(true);
        } else if (mSetting.getAudioEffectParams1() == 2) {
            getBinding().cbMinor.setChecked(true);
        } else if (mSetting.getAudioEffectParams1() == 1) {
            getBinding().cbMajor.setChecked(true);
        }
    }
}
