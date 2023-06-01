package io.agora.scene.ktv.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import io.agora.scene.base.component.BaseBottomSheetDialogFragment;
import io.agora.scene.base.component.BaseRecyclerViewAdapter;
import io.agora.scene.base.component.BaseViewBindingFragment;
import io.agora.scene.base.component.OnItemClickListener;
import io.agora.scene.base.utils.UiUtil;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.bean.EffectVoiceBean;
import io.agora.scene.ktv.databinding.KtvDialogMusicSettingBinding;
import io.agora.scene.ktv.databinding.KtvItemEffectvoiceBinding;
import io.agora.scene.ktv.live.fragment.dialog.BeautyVoiceFragment;
import io.agora.scene.ktv.live.fragment.dialog.EarBackFragment;
import io.agora.scene.ktv.live.holder.EffectVoiceHolder;
import io.agora.scene.widget.DividerDecoration;

/**
 * 控制台
 */
public class MusicSettingDialog extends BaseBottomSheetDialogFragment<KtvDialogMusicSettingBinding> implements OnItemClickListener<EffectVoiceBean> {
    public static final String TAG = "MusicSettingDialog";
    private MusicSettingBean mSetting;
    private Boolean isPause = false;
    private BaseRecyclerViewAdapter<KtvItemEffectvoiceBinding, EffectVoiceBean, EffectVoiceHolder> adapter;

    public MusicSettingDialog(MusicSettingBean mSetting, boolean isPause) {
        this.mSetting = mSetting;
        this.isPause = isPause;
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

        // 升降调
        tuningTone(null);
        //mBinding.switchEar.setChecked(this.mSetting.isEar());

        mBinding.sbVol1.setProgress(this.mSetting.getVolMic());
        mBinding.sbVol2.setProgress(this.mSetting.getVolMusic());

        mBinding.changeToneView.setProgress(getCurrentPitch(mSetting.getToneValue()));
        setSoundMode();
        mBinding.btnToneDownDialogSetting.setOnClickListener(v -> tuningTone(false));
        mBinding.btnToneUpDialogSetting.setOnClickListener(v -> tuningTone(true));

        if (isPause) {
            mBinding.sbRemoteVol.setEnabled(false);
            mBinding.btnRemoteVolumeUpDialogSetting.setEnabled(false);
            mBinding.btnRemoteVolumeDownDialogSetting.setEnabled(false);
            mBinding.sbRemoteVol.setProgress(100);
        } else {
            mBinding.sbRemoteVol.setEnabled(true);
            mBinding.btnRemoteVolumeUpDialogSetting.setEnabled(true);
            mBinding.btnRemoteVolumeDownDialogSetting.setEnabled(true);
            mBinding.sbRemoteVol.setProgress(this.mSetting.getRemoteVolume());
        }

        //mBinding.switchEar.setOnCheckedChangeListener((buttonView, isChecked) -> this.mSetting.setEar(isChecked));
        if (this.mSetting.isEar()) {
            mBinding.switchEar.setText("开启");
        } else {
            mBinding.switchEar.setText("关闭");
        }
        mBinding.switchEar.setOnClickListener(this::showEarBackPage);

        mBinding.btVol1Down.setOnClickListener(v -> tuningMicVolume(false));
        mBinding.btVol1Up.setOnClickListener(v -> tuningMicVolume(true));
        mBinding.sbVol1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.isPressed()) {
                    mSetting.setVolMic(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mBinding.btVol2Down.setOnClickListener(v -> tuningMusicVolume(false));
        mBinding.btVol2Up.setOnClickListener(v -> tuningMusicVolume(true));
        mBinding.sbVol2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.isPressed()) {
                    mSetting.setVolMusic(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mBinding.btnRemoteVolumeDownDialogSetting.setOnClickListener(v -> {
            int volume = mSetting.getRemoteVolume();
            int newVolume = volume - 1;
            mBinding.sbRemoteVol.setProgress(newVolume);
        });
        mBinding.btnRemoteVolumeUpDialogSetting.setOnClickListener(v -> {
            int volume = mSetting.getRemoteVolume();
            int newVolume = volume + 1;
            mBinding.sbRemoteVol.setProgress(newVolume);
        });
        mBinding.sbRemoteVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.isPressed()) {
                    mSetting.setRemoteVolume(i);
                    mBinding.sbRemoteVol.setProgress(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        List<EffectVoiceBean> list = new ArrayList<>();
        list.add(new EffectVoiceBean(0, R.mipmap.bg_sound_mode_1, "原声"));
        list.add(new EffectVoiceBean(1, R.mipmap.bg_sound_mode_2, "KTV"));
        list.add(new EffectVoiceBean(2, R.mipmap.bg_sound_mode_3, "演唱会"));
        list.add(new EffectVoiceBean(3, R.mipmap.bg_sound_mode_4, "录音棚"));
        list.add(new EffectVoiceBean(4, R.mipmap.bg_sound_mode_1, "留声机"));
        list.add(new EffectVoiceBean(5, R.mipmap.bg_sound_mode_2, "空旷"));
        list.add(new EffectVoiceBean(6, R.mipmap.bg_sound_mode_3, "空灵"));
        list.add(new EffectVoiceBean(7, R.mipmap.bg_sound_mode_4, "流行"));
        list.add(new EffectVoiceBean(8, R.mipmap.bg_sound_mode_1, "R&B"));
        for (EffectVoiceBean item : list) {
            item.setSelect(mSetting.getEffect() == item.getId());
        }

        adapter = new BaseRecyclerViewAdapter<>(list, this, EffectVoiceHolder.class);

        mBinding.rvVoiceEffectList.setAdapter(adapter);
        mBinding.rvVoiceEffectList.addItemDecoration(new DividerDecoration(10, 20, 0));
    }

    private void setSoundMode() {
        int margin = UiUtil.dp2px(10);
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

    private void showEarBackPage(View v) {
        mBinding.getRoot().removeAllViews();
        BaseViewBindingFragment<?> earBackFragment = new EarBackFragment(mSetting);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(mBinding.getRoot().getId(), earBackFragment, EarBackFragment.TAG);
        ft.commit();
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
                newToneValue += 2;
            } else {
                newToneValue -= 2;
            }
            if (newToneValue > 12)
                newToneValue = 12;

            if (newToneValue < -12)
                newToneValue = -12;

            if (newToneValue != this.mSetting.getToneValue())
                this.mSetting.setToneValue(newToneValue);
            }
        mBinding.changeToneView.setProgress(newToneValue);
    }

    private void tuningMicVolume(Boolean volumeUp) {
        int newVocalVolume = this.mSetting.getVolMic();
        if (volumeUp) {
            newVocalVolume += 1;
        } else {
            newVocalVolume -= 1;
        }
        if (newVocalVolume > 100)
            newVocalVolume = 100;

        if (newVocalVolume < 0)
            newVocalVolume = 0;
        if (newVocalVolume != this.mSetting.getVolMic()) {
            this.mSetting.setVolMic(newVocalVolume);
        }
        mBinding.sbVol1.setProgress(newVocalVolume);
    }

    private void tuningMusicVolume(Boolean volumeUp) {
        int newMusicVolume = this.mSetting.getVolMusic();
        if (volumeUp) {
            newMusicVolume += 1;
        } else {
            newMusicVolume -= 1;
        }
        if (newMusicVolume > 100)
            newMusicVolume = 100;

        if (newMusicVolume < 0)
            newMusicVolume = 0;
        if (newMusicVolume != this.mSetting.getVolMusic()) {
            this.mSetting.setVolMusic(newMusicVolume);
        }
        mBinding.sbVol2.setProgress(newMusicVolume);
    }

    public void onStopPlayer() {
        mBinding.sbRemoteVol.setProgress(100);
    }

    public void onResumePlayer() {
        mBinding.sbRemoteVol.setProgress(this.mSetting.getRemoteVolume());
    }

    @Override
    public void onItemClick(@NonNull EffectVoiceBean data, View view, int position, long viewType) {
        OnItemClickListener.super.onItemClick(data, view, position, viewType);
        Log.e("liu0223", "onItemClick    " + position);

        for (int i = 0; i < adapter.dataList.size(); i++) {
            adapter.dataList.get(i).setSelect(i == position);
            adapter.notifyItemChanged(i);
        }
        mSetting.setEffect(data.getId());
    }

    public interface Callback {
        void onEarChanged(boolean isEar);

        void onMicVolChanged(int vol);

        void onMusicVolChanged(int vol);

        void onEffectChanged(int effect);

        void onBeautifierPresetChanged(int effect);

        void setAudioEffectParameters(int param1, int param2);

        void onToneChanged(int newToneValue);

        void onRemoteVolumeChanged(int volume);

        void onProfessionalModeChanged(boolean enable);

        void onAECLevelChanged(int level);

        void onLowLatencyModeChanged(boolean enable);

        void onEarBackVolumeChanged(int volume);

        void onEarBackModeChanged(int mode);

        void onAINSModeChanged(int mode);
    }
}
