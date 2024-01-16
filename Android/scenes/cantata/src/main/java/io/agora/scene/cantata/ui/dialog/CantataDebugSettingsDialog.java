package io.agora.scene.cantata.ui.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.agora.scene.base.component.BaseBottomSheetDialogFragment;
import io.agora.scene.cantata.databinding.CantataDialogDebugOptionsBinding;

/**
 * 控制台
 */
public class CantataDebugSettingsDialog extends BaseBottomSheetDialogFragment<CantataDialogDebugOptionsBinding> {
    public static final String TAG = "KTVDebugSettingsDialog";
    private final CantataDebugSettingBean mSetting;
    private final String channelName;
    private final String sdkBuildNum;

    public CantataDebugSettingsDialog(CantataDebugSettingBean mSetting, String channelName, String sdkBuildNum) {
        this.mSetting = mSetting;
        this.channelName = channelName;
        this.sdkBuildNum = sdkBuildNum;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(
                requireDialog().getWindow().getDecorView(), (v, insets) -> {
                    Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    mBinding.getRoot().setPadding(inset.left, 0, inset.right, inset.bottom);
                    return WindowInsetsCompat.CONSUMED;
                });

        mBinding.dumpAudio.setChecked(mSetting.isAudioDumpEnabled());
        mBinding.dumpAudio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                mSetting.enableAudioDump(isChecked);
            }
        });

        mBinding.scoringLevelTune.setProgress(mSetting.getScoringLevel());
        mBinding.scoringLevelTune.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSetting.setScoringControl(progress, mSetting.getScoringOffset());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.scoringOffsetTune.setProgress(mSetting.getScoringOffset());
        mBinding.scoringOffsetTune.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSetting.setScoringControl(mSetting.getScoringLevel(), progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.btSetParams.setOnClickListener(v -> {
            String key = mBinding.etParms.getText().toString();
            String value = mBinding.etParmsNum.getText().toString();
            String parameters = "{\"" + key + "\":" + value;
            mSetting.setParameters(parameters);
        });

        mBinding.tvChannelName.setText("channelName: " + channelName);
        mBinding.tvSDKVersion.setText("agora sdk ver: " + sdkBuildNum);
    }

    public interface Callback {
        void onAudioDumpEnable(boolean enable);

        void onScoringControl(int level, int offset);

        void onSetParameters(String parameters);
    }
}
