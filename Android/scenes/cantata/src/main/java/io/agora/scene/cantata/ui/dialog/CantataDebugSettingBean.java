package io.agora.scene.cantata.ui.dialog;

public class CantataDebugSettingBean {
    private final CantataDebugSettingsDialog.Callback mCallback;

    private boolean isAudioDumpEnabled = false;

    private int mScoringLevel = 10; // According to Karaoke.getScoreLevel
    private int mScoringOffset = 0;

    public CantataDebugSettingBean(CantataDebugSettingsDialog.Callback mCallback) {
        this.mCallback = mCallback;
    }

    public CantataDebugSettingsDialog.Callback getCallback() {
        return mCallback;
    }

    public boolean isAudioDumpEnabled() {
        return isAudioDumpEnabled;
    }

    public void enableAudioDump(boolean enable) {
        this.isAudioDumpEnabled = enable;
        this.mCallback.onAudioDumpEnable(enable);
    }

    public int getScoringLevel() {
        return mScoringLevel;
    }

    public int getScoringOffset() {
        return mScoringOffset;
    }

    public void setScoringControl(int level, int offset) {
        this.mScoringLevel = level;
        this.mScoringOffset = offset;

        this.mCallback.onScoringControl(level, offset);
    }

    public void setParameters(String parameters) {
        this.mCallback.onSetParameters(parameters);
    }
}
