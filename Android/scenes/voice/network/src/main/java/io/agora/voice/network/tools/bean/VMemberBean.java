package io.agora.voice.network.tools.bean;

import java.io.Serializable;

/**
 * @author create by zhangwei03
 */
public class VMemberBean implements Serializable {
    /**
     * uid : string
     * chat_uid : string
     * name : string
     * portrait : string
     * rtc_uid : 0
     * mic_index : 0
     */

    private String uid;
    private String chat_uid;
    private String name;
    private String portrait;
    private int rtc_uid;
    private int mic_index;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getChat_uid() {
        return chat_uid;
    }

    public void setChat_uid(String chat_uid) {
        this.chat_uid = chat_uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public int getRtc_uid() {
        return rtc_uid;
    }

    public void setRtc_uid(int rtc_uid) {
        this.rtc_uid = rtc_uid;
    }

    public int getMic_index() {
        return mic_index;
    }

    public void setMic_index(int mic_index) {
        this.mic_index = mic_index;
    }
}