package io.agora.scene.ktv.widget;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.agora.scene.base.component.BaseBottomSheetDialogFragment;
import io.agora.scene.base.utils.KTVUtil;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvDialogMusicSettingBinding;

/**
 * 控制台
 */
public class MusicSettingDialog extends BaseBottomSheetDialogFragment<KtvDialogMusicSettingBinding> {
    public static final String TAG = "MusicSettingDialog";
    private MusicSettingBean mSetting;

    public MusicSettingDialog(MusicSettingBean mSetting) {
        this.mSetting = mSetting;
    }

    private int getCurrentPitch(int value) {
        switch (value) {
            case 12:
                return 11;
            case 10:
                return 10;
            case 8:
                return 9;
            case 6:
                return 8;
            case 4:
                return 7;
            case 2:
                return 6;
            case -2:
                return 4;
            case -4:
                return 3;
            case -6:
                return 2;
            case -8:
                return 1;
            case -10:
                return 0;
            case -12:
                return -1;
            default:
            case 0:
                return 5;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(
                requireDialog().getWindow().getDecorView(), (v, insets) -> {
                    Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    mBinding.getRoot().setPadding(inset.left, 0, inset.right, inset.bottom);
                    return WindowInsetsCompat.CONSUMED;
                });

        // 升降调
        tuningTone(null);
        mBinding.switchEar.setChecked(this.mSetting.isEar());

        mBinding.sbVol1.setProgress(this.mSetting.getVolMic());
        mBinding.sbVol2.setProgress(this.mSetting.getVolMusic());

        mBinding.changeToneView.currentPitch = getCurrentPitch(mSetting.getToneValue());
        setSoundMode();
        mBinding.btnToneDownDialogSetting.setOnClickListener(v -> tuningTone(false));
        mBinding.btnToneUpDialogSetting.setOnClickListener(v -> tuningTone(true));

        mBinding.switchEar.setOnCheckedChangeListener((buttonView, isChecked) -> this.mSetting.setEar(isChecked));
        mBinding.sbVol1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mSetting.setVolMic(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBinding.sbVol2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mSetting.setVolMusic(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setSoundMode() {
        int margin = KTVUtil.dp2px(10);
        String[] stringArray = getResources().getStringArray(R.array.ktv_audioPreset);
        for (int i = 0; i < stringArray.length; i++) {
            RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(R.layout.btn_sound_mode, null);
            radioButton.setText(stringArray[i]);
            if (i % 4 == 0) {
                radioButton.setBackgroundResource(R.drawable.bg_rbtn_select_sound_mode4);
            } else if (i % 3 == 0) {
                radioButton.setBackgroundResource(R.drawable.bg_rbtn_select_sound_mode3);
            } else if (i % 2 == 0) {
                radioButton.setBackgroundResource(R.drawable.bg_rbtn_select_sound_mode2);
            } else {
                radioButton.setBackgroundResource(R.drawable.bg_rbtn_select_sound_mode1);
            }
            mBinding.radioGroup.addView(radioButton);
            ((LinearLayout.LayoutParams) radioButton.getLayoutParams()).setMargins(margin, 0, 0, 0);
            if (0 == i) {
                radioButton.setChecked(true);
            } else if (i == stringArray.length - 1) {
                ((LinearLayout.LayoutParams) radioButton.getLayoutParams()).setMargins(margin, 0, margin, 0);
            }
            int finalI = i;
            radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    mSetting.setEffect(finalI);
                }
            });
        }
        ((RadioButton) mBinding.radioGroup.getChildAt(mSetting.getEffect())).setChecked(true);
    }

    /**
     * IMediaPlayer.java
     * /**
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
    private void tuningTone(Boolean toneUp) {
        int newToneValue = this.mSetting.getToneValue();
        if (toneUp != null) {
            if (toneUp) {
                mBinding.changeToneView.currentPitchPlus();
                newToneValue += 2;
            } else {
                newToneValue -= 2;
                mBinding.changeToneView.currentPitchMinus();
            }
            if (newToneValue > 12)
                newToneValue = 12;

            if (newToneValue < -12)
                newToneValue = -12;

            if (newToneValue != this.mSetting.getToneValue())
                this.mSetting.setToneValue(newToneValue);
        }
//        mBinding.textToneDialogSetting.setText(String.valueOf(newToneValue));
    }


    public interface Callback {
        void onEarChanged(boolean isEar);

        void onMicVolChanged(int vol);

        void onMusicVolChanged(int vol);

        void onEffectChanged(int effect);

        void onBeautifierPresetChanged(int effect);

        void setAudioEffectParameters(int param1, int param2);

        void onToneChanged(int newToneValue);
    }
}
