package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AUICreateRoomInfo implements Serializable {
    public @NonNull String roomId = "";       //房间Id
    public @NonNull String roomName = "";       //房间名称
    @SerializedName("roomThumbnail")
    public @NonNull String thumbnail = "";      //房间列表上的缩略图
    @SerializedName("roomSeatCount")
    public int micSeatCount = 8;                   //麦位个数
    public @Nullable String password;           //房间密码
    public String micSeatStyle = "";            //麦位样式
}
