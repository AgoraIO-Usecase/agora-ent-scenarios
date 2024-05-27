package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;

public class AUIUserInfo extends AUIUserThumbnailInfo {
    public boolean muteAudio = false;  //是否静音状态
    public boolean muteVideo = true;   //是否关闭视频状态
    public String customPayload;       //扩展信息

    @NonNull
    @Override
    public String toString() {
        return "AUIUserInfo{" +
                "muteAudio=" + muteAudio +
                ", muteVideo=" + muteVideo +
                ", customPayload=" + customPayload +
                "} " + super.toString();
    }
}
