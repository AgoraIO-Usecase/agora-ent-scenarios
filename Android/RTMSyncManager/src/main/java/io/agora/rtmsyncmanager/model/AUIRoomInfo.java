package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AUIRoomInfo implements Serializable {

    public @NonNull String roomId = "";       //房间Id
    public @NonNull String roomName = "";       //房间名称
    public @Nullable AUIUserThumbnailInfo roomOwner; // 房主信息
    public Map<String, Object> customPayload = new HashMap<String, Object>();
    public Long createTime;
}
