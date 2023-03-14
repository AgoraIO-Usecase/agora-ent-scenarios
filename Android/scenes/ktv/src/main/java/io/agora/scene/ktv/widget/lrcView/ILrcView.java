package io.agora.scene.ktv.widget.lrcView;

public interface ILrcView {
    void onUpdatePitch(Float pitch);
    void onUpdateProgress(Long progress);
    void onDownloadLrcData(String url);
}
