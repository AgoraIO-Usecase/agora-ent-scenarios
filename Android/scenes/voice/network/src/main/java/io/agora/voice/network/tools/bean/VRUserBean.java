package io.agora.voice.network.tools.bean;

public class VRUserBean {
    private String uid;
    private String name;
    private String portrait;
    private String authorization;
    private String chat_uid;
    private String im_token;
    private int rtc_uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getChat_uid() {
        return chat_uid;
    }

    public void setChat_uid(String chat_uid) {
        this.chat_uid = chat_uid;
    }

    public String getIm_token() {
        return im_token;
    }

    public void setIm_token(String im_token) {
        this.im_token = im_token;
    }

    public int getRtc_uid() {
        return rtc_uid;
    }

    public void setRtc_uid(int rtc_uid) {
        this.rtc_uid = rtc_uid;
    }
}
