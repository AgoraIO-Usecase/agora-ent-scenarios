package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;

public class AUIRoomConfig {

    @NonNull public String channelName = "";     //正常rtm/rtc使用的频道
    @NonNull public String rtmToken = "";     //rtm login用
    @NonNull public String rtcToken = "";     //rtc join用
    @NonNull public String rtcChorusChannelName = "";  //rtc 合唱使用的频道
    @NonNull public String rtcChorusRtcToken = "";  //rtc 合唱join使用

    public AUIRoomConfig(@NonNull String roomId) {
        channelName = roomId;
        rtcChorusChannelName = roomId + "_rtc_ex";
    }
}
