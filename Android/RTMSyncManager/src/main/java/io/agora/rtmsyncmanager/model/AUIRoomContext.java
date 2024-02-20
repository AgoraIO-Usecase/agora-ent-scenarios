package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtmsyncmanager.service.arbiter.AUIArbiter;

public class AUIRoomContext {
    private static volatile AUIRoomContext instance = null;
    private AUIRoomContext() {
        // 私有构造函数
    }
    public static AUIRoomContext shared() {
        if (instance == null) {
            synchronized (AUIRoomContext.class){
                if(instance == null){
                    instance = new AUIRoomContext();
                }
            }
        }
        return instance;
    }

    public Map<String, String> roomOwnerMap = new HashMap<>();
    public Map<String, AUIRoomConfig> roomConfigMap = new HashMap<>();
    public Map<String, AUIArbiter> roomArbiterMap = new HashMap<>();

    public @NonNull AUIUserThumbnailInfo currentUserInfo = new AUIUserThumbnailInfo();
    public @Nullable AUICommonConfig mCommonConfig;
    private final Map<String, AUIRoomInfo> roomInfoMap = new HashMap<>();

    public void setCommonConfig(@NonNull AUICommonConfig config) {
        mCommonConfig = config;
        currentUserInfo = config.owner;
    }

    public @NonNull AUICommonConfig requireCommonConfig() {
        if(mCommonConfig == null){
            throw new RuntimeException("mCommonConfig is null now!");
        }
        return mCommonConfig;
    }

    public boolean isRoomOwner(String channelName){
        return isRoomOwner(channelName, currentUserInfo.userId);
    }

    public boolean isRoomOwner(String channelName, String userId){
        AUIRoomInfo roomInfo = roomInfoMap.get(channelName);
        if(roomInfo == null || roomInfo.owner == null){
            return false;
        }
        return roomInfo.owner.userId.equals(userId);
    }

    public void resetRoomMap(@Nullable List<AUIRoomInfo> roomInfoList) {
        roomInfoMap.clear();
        if (roomInfoList == null || roomInfoList.size() == 0) {
            return;
        }
        for (AUIRoomInfo info : roomInfoList) {
            roomInfoMap.put(info.roomId, info);
        }
    }

    public void insertRoomInfo(AUIRoomInfo info) {
        roomInfoMap.put(info.roomId, info);
    }

    public void cleanRoom(String channelName){
        roomInfoMap.remove(channelName);
        roomConfigMap.remove(channelName);
        AUIArbiter auiArbiter = roomArbiterMap.remove(channelName);
        if(auiArbiter != null){
            auiArbiter.deInit();
        }
    }

    public String getRoomOwner(String channelName){
        AUIRoomInfo roomInfo = roomInfoMap.get(channelName);
        if(roomInfo == null || roomInfo.owner == null){
            return "";
        }
        return roomInfo.owner.userId;
    }

    public @Nullable AUIRoomInfo getRoomInfo(String channelName) {
        return roomInfoMap.get(channelName);
    }

    public @Nullable AUIArbiter getArbiter(@NotNull String channelName) {
        return roomArbiterMap.get(channelName);
    }
}
