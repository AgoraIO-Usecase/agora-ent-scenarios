package io.agora.scene.base.event;

public class NetWorkEvent {
    public int txQuality;
    public int rxQuality;

    public NetWorkEvent(int txQuality, int rxQuality) {
        this.txQuality = txQuality;
        this.rxQuality = rxQuality;
    }

}
