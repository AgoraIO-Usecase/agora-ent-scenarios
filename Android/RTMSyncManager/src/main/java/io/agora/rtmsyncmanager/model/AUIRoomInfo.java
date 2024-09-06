package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * AUIRoomInfo is a class that holds information about a room.
 * This includes the room ID, room name, room owner, custom payload, and creation time.
 */
public class AUIRoomInfo implements Serializable {

    // The ID of the room
    @NonNull public String roomId = "";
    // The name of the room
    @NonNull public String roomName = "";
    // The information about the room owner
    @Nullable public AUIUserThumbnailInfo roomOwner;
    // A map to hold custom payload data
    public Map<String, Object> customPayload = new HashMap<>();
    // The time the room was created
    public Long createTime;

    @NonNull
    @Override
    public String toString() {
        return "AUIRoomInfo{"
                + "roomId='"
                + roomId
                + '\''
                + ", roomName='" + roomName
                + '\''
                + ", roomOwner=" + roomOwner
                + ", customPayload=" + customPayload
                + ", createTime=" + createTime
                + '}';
    }
}