package io.agora.scene.ktv.live.fragment.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvFragmentBeautyVoiceBinding;
import io.agora.scene.ktv.live.RoomLivingActivity;
import io.agora.scene.ktv.widget.MusicSettingBean;

public class BeautyVoiceFragment extends BaseViewBindingFragment<KtvFragmentBeautyVoiceBinding> {
    public static final String TAG = "BeautyVoiceFragment";
    private final MusicSettingBean mSetting;

    @NonNull
    @Override
    protected KtvFragmentBeautyVoiceBinding getViewBinding(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
        return KtvFragmentBeautyVoiceBinding.inflate(layoutInflater);
    }

    public BeautyVoiceFragment(MusicSettingBean mSetting) {
        this.mSetting = mSetting;
    }

    @Override
    public void initListener() {
        getBinding().ivBackIcon.setOnClickListener(view -> {
            ((RoomLivingActivity) requireActivity()).closeMenuDialog();
        });
        ((RadioButton) getBinding().radioGroup.getChildAt(mSetting.getBeautifier())).setChecked(true);
        getBinding().radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.rBtnBeautyNoting) {
                mSetting.setBeautifier(0);
            } else if (i == R.id.rBtnBigMan) {
                mSetting.setBeautifier(1);
            } else if (i == R.id.rBtnSmallMan) {
                mSetting.setBeautifier(2);
            } else if (i == R.id.rBtnBigWoman) {
                mSetting.setBeautifier(3);
            } else if (i == R.id.rBtnSmallWoman) {
                mSetting.setBeautifier(4);
            }
        });
    }
}
