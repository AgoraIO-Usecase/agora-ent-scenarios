package io.agora.scene.ktv.widget.voiceHighight;

import io.agora.scene.ktv.service.RoomSeatModel;

public interface OnVoiceHighlightDialogListener {
    void onUserListLoad();
    void onUserItemChosen(VoiceHighlightBean user);
}
