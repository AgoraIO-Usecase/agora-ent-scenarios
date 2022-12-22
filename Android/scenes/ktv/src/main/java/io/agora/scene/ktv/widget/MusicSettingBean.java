package io.agora.scene.ktv.widget;

public class MusicSettingBean {
    private final MusicSettingDialog.Callback mCallback;
    private boolean isEar;
    private int volMic;
    private int volMusic;
    private int effect;
    private int beautifier = 0;
    private int toneValue;
    private int audioEffectParams1 = 0;
    private int audioEffectParams2 = 0;


    public MusicSettingBean(boolean isEar, int volMic, int volMusic, int toneValue, MusicSettingDialog.Callback mCallback) {
        this.isEar = isEar;
        this.volMic = volMic;
        this.volMusic = volMusic;
        this.mCallback = mCallback;
        this.toneValue = toneValue;
    }

    public MusicSettingDialog.Callback getCallback() {
        return mCallback;
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

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
        this.mCallback.onEffectChanged(effect);
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
}
