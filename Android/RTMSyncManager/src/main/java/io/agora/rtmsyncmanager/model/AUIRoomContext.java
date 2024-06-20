package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter;

/**
 * Singleton class that holds the context of a room.
 * This includes the room owner, room configuration, room arbiter, current user information, and common configuration.
 */
public final class AUIRoomContext {
    // Singleton instance of AUIRoomContext
    private static volatile AUIRoomContext instance = null;

    // Private constructor to prevent instantiation
    private AUIRoomContext() { }

    /**
     * Method to get the singleton instance of AUIRoomContext
     * @return instance of AUIRoomContext
     */
    public static AUIRoomContext shared() {
        if (instance == null) {
            synchronized (AUIRoomContext.class) {
                if (instance == null) {
                    instance = new AUIRoomContext();
                }
            }
        }
        return instance;
    }

    // Map to hold room owners
    public Map<String, String> roomOwnerMap = new HashMap<>();
    // Map to hold room arbiters
    public Map<String, AUIArbiter> roomArbiterMap = new HashMap<>();

    // Current user information
    public @NonNull AUIUserThumbnailInfo currentUserInfo = new AUIUserThumbnailInfo();
    // Common configuration
    public @Nullable AUICommonConfig mCommonConfig;
    // Map to hold room information
    private final Map<String, AUIRoomInfo> roomInfoMap = new HashMap<>();

    /**
     * Method to set the common configuration
     * @param config common configuration to be set
     */
    public void setCommonConfig(@NonNull AUICommonConfig config) {
        mCommonConfig = config;
        currentUserInfo = config.owner;
    }

    /**
     * Method to get the common configuration
     * @return common configuration
     */
    public @NonNull AUICommonConfig requireCommonConfig() {
        if (mCommonConfig == null) {
            throw new RuntimeException("mCommonConfig is null now!");
        }
        return mCommonConfig;
    }

    /**
     * Method to check if a user is the room owner
     * @param channelName name of the channel
     * @return true if user is room owner, false otherwise
     */
    public boolean isRoomOwner(String channelName) {
        return isRoomOwner(channelName, currentUserInfo.userId);
    }

    /**
     * Method to check if a user is the room owner
     * @param channelName name of the channel
     * @param userId id of the user
     * @return true if user is room owner, false otherwise
     */
    public boolean isRoomOwner(String channelName, String userId) {
        String roomOwnerId = roomOwnerMap.get(channelName);
        if (roomOwnerId == null || userId == null) {
            return false;
        }
        return roomOwnerId.equals(userId);
    }

    /**
     * Method to reset the room map
     * @param roomInfoList list of room information
     */
    public void resetRoomMap(@Nullable List<AUIRoomInfo> roomInfoList) {
        roomInfoMap.clear();
        if (roomInfoList == null || roomInfoList.isEmpty()) {
            return;
        }
        for (AUIRoomInfo info : roomInfoList) {
            roomInfoMap.put(info.roomId, info);
        }
    }

    /**
     * Method to insert room information
     * @param info room information to be inserted
     */
    public void insertRoomInfo(AUIRoomInfo info) {
        roomInfoMap.put(info.roomId, info);
    }

    /**
     * Method to clean a room
     * @param channelName name of the channel
     */
    public void cleanRoom(String channelName) {
        roomInfoMap.remove(channelName);
        roomOwnerMap.remove(channelName);
        AUIArbiter auiArbiter = roomArbiterMap.remove(channelName);
        if (auiArbiter != null) {
            auiArbiter.deInit();
        }
    }

    /**
     * Method to get the room owner
     * @param channelName name of the channel
     * @return id of the room owner
     */
    public String getRoomOwner(String channelName) {
        AUIRoomInfo roomInfo = roomInfoMap.get(channelName);
        if (roomInfo == null || roomInfo.roomOwner == null) {
            return "";
        }
        return roomInfo.roomOwner.userId;
    }

    /**
     * Method to get room information
     * @param channelName name of the channel
     * @return room information
     */
    public @Nullable AUIRoomInfo getRoomInfo(String channelName) {
        return roomInfoMap.get(channelName);
    }

    /**
     * Method to get the arbiter of a room
     * @param channelName name of the channel
     * @return arbiter of the room
     */
    public @Nullable AUIArbiter getArbiter(@NotNull String channelName) {
        return roomArbiterMap.get(channelName);
    }
}