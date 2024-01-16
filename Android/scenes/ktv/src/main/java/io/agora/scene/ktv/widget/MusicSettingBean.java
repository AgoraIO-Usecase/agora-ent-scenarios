package io.agora.scene.ktv.widget;

import io.agora.rtc2.Constants;

public class MusicSettingBean {
    private final MusicSettingDialog.Callback mCallback;
    private EarPhoneCallback mEarPhoneCallback;
    private int volMic;
    private int volMusic;
    private int audioPreset;
    private int beautifier = 0;
    private int toneValue;
    private int audioEffectParams1 = 0;
    private int audioEffectParams2 = 0;
    private int remoteVolume = 30;
    private boolean professionalMode = false; // 专业模式
    private int aecLevel = 0; // 0(16K),1(24K),2(48K)
    private boolean lowLatencyMode = true;  // 低延时模式

    // 耳返设置
    private boolean isEar; // 耳返开关
    private int earBackVolume = 100; // 耳返音量
    private int earBackMode = 0; // 耳返模式：0(自动), 1(强制OpenSL), 2(强制Oboe)
    private boolean hasEarPhone = false; // 是否有耳机
    private int earBackDelay = 0; // 耳返延迟

    // 人声突出
    private String highLighterUid = "";

    // 背景音降噪
    private int ainsMode = 0; // 背景音降噪：0(关闭), 1(中), 2(高)

    // AIAEC
    private boolean aiaecOpen = false;
    private int mAIAECStrength = 0;

    public MusicSettingBean(int audioEffect, boolean isEar, int volMic, int volMusic, int toneValue,
                            MusicSettingDialog.Callback mCallback) {
        this.audioPreset = audioEffect;
        this.isEar = isEar;
        this.volMic = volMic;
        this.volMusic = volMusic;
        this.mCallback = mCallback;
        this.toneValue = toneValue;
    }

    public MusicSettingDialog.Callback getCallback() {
        return mCallback;
    }

    public void setEarPhoneCallback(EarPhoneCallback callback) {
        this.mEarPhoneCallback = callback;
    }

    public boolean isEar() {
        return isEar;
    }

    public void setEar(boolean ear) {
        isEar = ear;
        mCallback.onEarChanged(ear);
    }

    public int getVolMic() {
        return volMic;
    }

    public void setVolMic(int volMic) {
        this.volMic = volMic;
        this.mCallback.onMicVolChanged(volMic);
    }

    public int getVolMusic() {
        return volMusic;
    }

    public void setVolMusic(int volMusic) {
        this.volMusic = volMusic;
        this.mCallback.onMusicVolChanged(volMusic);
    }

    public int getAudioEffect() {
        return audioPreset;
    }

    public void setAudioEffect(int audioEffect) {
        if (this.audioPreset == audioEffect) return;
        this.audioPreset = audioEffect;
        this.mCallback.onEffectChanged(audioEffect);
    }

    public void updateAudioEffect(int audioEffect) {
        this.audioPreset = audioEffect;
    }

    public int getBeautifier() {
        return beautifier;
    }

    public void setBeautifier(int beautifier) {
        this.beautifier = beautifier;
        this.mCallback.onBeautifierPresetChanged(beautifier);
    }

    public int getAudioEffectParams1() {
        return audioEffectParams1;
    }

    public int getAudioEffectParams2() {
        return audioEffectParams2;
    }

    public void setAudioEffectParameters(int params1, int params2) {
        this.audioEffectParams1 = params1;
        this.audioEffectParams2 = params2;
        this.mCallback.setAudioEffectParameters(params1, params2);
    }

    public int getToneValue() {
        return toneValue;
    }

    public void setToneValue(int newToneValue) {
        this.toneValue = newToneValue;
        this.mCallback.onToneChanged(newToneValue);
    }

    public int getRemoteVolume() {
        return remoteVolume;
    }

    public void setRemoteVolume(int newValue) {
        this.remoteVolume = newValue;
        this.mCallback.onRemoteVolumeChanged(newValue);
    }

    public boolean getProfessionalMode() {
        return professionalMode;
    }

    public void setProfessionalMode(boolean mode) {
        this.professionalMode = mode;
        this.mCallback.onProfessionalModeChanged(mode);
    }

    public int getAECLevel() {
        return aecLevel;
    }

    public void setAECLevel(int level) {
        this.aecLevel = level;
        this.mCallback.onAECLevelChanged(level);
    }

    public boolean getLowLatencyMode() {
        return lowLatencyMode;
    }

    public void setLowLatencyMode(boolean mode) {
        this.lowLatencyMode = mode;
        this.mCallback.onLowLatencyModeChanged(mode);
    }

    public int getEarBackVolume() {
        return earBackVolume;
    }

    public void setEarBackVolume(int volume) {
        this.earBackVolume = volume;
        this.mCallback.onEarBackVolumeChanged(volume);
    }

    public int getEarBackMode() {
        return earBackMode;
    }

    public void setEarBackMode(int mode) {
        this.earBackMode = mode;
        this.mCallback.onEarBackModeChanged(mode);
    }

    public boolean hasEarPhone() {
        return hasEarPhone;
    }

    public void setHasEarPhone(boolean hasEarPhone) {
        this.hasEarPhone = hasEarPhone;
        if (mEarPhoneCallback != null) {
            mEarPhoneCallback.onHasEarPhoneChanged(hasEarPhone);
        }
    }

    public int getEarBackDelay() {
        return earBackDelay;
    }

    public void setEarBackDelay(int earBackDelay) {
        this.earBackDelay = earBackDelay;
        if (mEarPhoneCallback != null) {
            mEarPhoneCallback.onEarMonitorDelay(earBackDelay);
        }
    }

    public String getHighLighterUid() {
        return highLighterUid;
    }

    public void setHighLighterUid(String uid) {
        this.highLighterUid = uid;
    }

    public interface EarPhoneCallback {
        void onHasEarPhoneChanged(boolean hasEarPhone);

        void onEarMonitorDelay(int earsBackDelay);
    }

    public int getAinsMode() {
        return ainsMode;
    }

    public void setAinsMode(int mode) {
        this.ainsMode = mode;
        mCallback.onAINSModeChanged(mode);
    }

    public boolean isAIAECOpen() {
        return aiaecOpen;
    }

    public void setAIAECMode(boolean open) {
        this.aiaecOpen = open;
        this.mCallback.onAIAECChanged(open);
    }

    public int getAIAECStrength() {
        return mAIAECStrength;
    }

    public void setAIAECStrength(int strength) {
        this.mAIAECStrength = strength;
        this.mCallback.onAIAECStrengthSelect(strength);
    }

    public int getEffectIndex(int index) {
        switch (index) {
            // KTV
            case 0:
                return Constants.ROOM_ACOUSTICS_KTV;
            // 原声
            case 1:
                return Constants.AUDIO_EFFECT_OFF;
            // 演唱会
            case 2:
                return Constants.ROOM_ACOUSTICS_VOCAL_CONCERT;
            // 录音棚
            case 3:
                return Constants.ROOM_ACOUSTICS_STUDIO;
            // 留声机
            case 4:
                return Constants.ROOM_ACOUSTICS_PHONOGRAPH;
            // 空旷
            case 5:
                return Constants.ROOM_ACOUSTICS_SPACIAL;
            // 空灵
            case 6:
                return Constants.ROOM_ACOUSTICS_ETHEREAL;
            // 流行
            case 7:
                return Constants.STYLE_TRANSFORMATION_POPULAR;
            // R&B
            case 8:
                return Constants.STYLE_TRANSFORMATION_RNB;
        }
        // 默认 ktv
        return Constants.ROOM_ACOUSTICS_KTV;
    }
}
