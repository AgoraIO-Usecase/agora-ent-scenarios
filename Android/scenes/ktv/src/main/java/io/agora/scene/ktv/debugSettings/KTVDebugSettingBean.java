package io.agora.scene.ktv.debugSettings;

import io.agora.scene.ktv.widget.MusicSettingDialog;

public class KTVDebugSettingBean {
    private final KTVDebugSettingsDialog.Callback mCallback;

    private boolean isAudioDumpEnabled = false;

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
}
