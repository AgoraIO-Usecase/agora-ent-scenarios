package io.agora.scene.ktv.debugSettings;

/**
 * debug setting bean
 */
public class KTVDebugSettingBean {
    private final KTVDebugSettingsDialog.Callback mCallback;

    private boolean isAudioDumpEnabled = false;

    private int mScoringLevel = 15; // According to Karaoke.getScoreLevel
    private int mScoringOffset = 0;

    /**
     * Instantiates a new Ktv debug setting bean.
     *
     * @param mCallback the m callback
     */
    public KTVDebugSettingBean(KTVDebugSettingsDialog.Callback mCallback) {
        this.mCallback = mCallback;
    }

    /**
     * Gets callback.
     *
     * @return the callback
     */
    public KTVDebugSettingsDialog.Callback getCallback() {
        return mCallback;
    }

    /**
     * Is audio dump enabled boolean.
     *
     * @return the boolean
     */
    public boolean isAudioDumpEnabled() {
        return isAudioDumpEnabled;
    }

    /**
     * Enable audio dump.
     *
     * @param enable the enable
     */
    public void enableAudioDump(boolean enable) {
        this.isAudioDumpEnabled = enable;
        this.mCallback.onAudioDumpEnable(enable);
    }

    /**
     * Gets scoring level.
     *
     * @return the scoring level
     */
    public int getScoringLevel() {
        return mScoringLevel;
    }

    /**
     * Gets scoring offset.
     *
     * @return the scoring offset
     */
    public int getScoringOffset() {
        return mScoringOffset;
    }

    /**
     * Sets scoring control.
     *
     * @param level  the level
     * @param offset the offset
     */
    public void setScoringControl(int level, int offset) {
        this.mScoringLevel = level;
        this.mScoringOffset = offset;

        this.mCallback.onScoringControl(level, offset);
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    public void setParameters(String parameters) {
        this.mCallback.onSetParameters(parameters);
    }
}
