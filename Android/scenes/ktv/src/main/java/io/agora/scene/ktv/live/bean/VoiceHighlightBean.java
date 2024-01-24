package io.agora.scene.ktv.live.bean;

import io.agora.scene.ktv.service.RoomSeatModel;

/**
 * 人声突出
 */
public class VoiceHighlightBean {
    public RoomSeatModel user;

    private boolean select;


    public boolean isSelect() {
        return select;
    }

    public VoiceHighlightBean setSelect(boolean select) {
        this.select = select;
        return this;
    }
}
