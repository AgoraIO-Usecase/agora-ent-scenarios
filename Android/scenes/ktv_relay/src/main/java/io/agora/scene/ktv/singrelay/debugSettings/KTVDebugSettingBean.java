package io.agora.scene.ktv.singrelay.debugSettings;

public class KTVDebugSettingBean {
    private final KTVDebugSettingsDialog.Callback mCallback;

    private boolean isAudioDumpEnabled = false;

    private int mScoringLevel = 15; // According to Karaoke.getScoreLevel
    private int mScoringOffset = 0;

    public KTVDebugSettingBean(KTVDebugSettingsDialog.Callback mCallback) {
        this.mCallback = mCallback;
    }

    public KTVDebugSettingsDialog.Callback getCallback() {
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
}
