package io.agora.scene.ktv.live.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.ktv.databinding.FragmentProfileBinding;
import io.agora.scene.ktv.widget.MusicSettingBean;

public class ProfileFragment extends BaseViewBindingFragment<FragmentProfileBinding> {
    public static final String TAG = "ProfileFragment";
    private final MusicSettingBean mSetting;

    public ProfileFragment(MusicSettingBean mSetting) {
        this.mSetting = mSetting;
    }

    @Override
    protected FragmentProfileBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void initListener() {
        super.initListener();

        getBinding().cbStartProfessionalMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSetting.setProfessionalMode(isChecked);
            }
        });
        getBinding().cbStartProfessionalMode.setChecked(mSetting.getProfessionalMode());

        getBinding().spAEC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSetting.getAECLevel() != position) {
                    mSetting.setAECLevel(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getBinding().spAEC.setSelection(mSetting.getAECLevel());

        getBinding().cbLowLatency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSetting.setLowLatencyMode(isChecked);
            }
        });
        getBinding().cbLowLatency.setChecked(mSetting.getLowLatencyMode());
    }
}
