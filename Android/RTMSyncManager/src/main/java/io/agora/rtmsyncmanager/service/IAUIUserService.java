package io.agora.rtmsyncmanager.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import io.agora.rtmsyncmanager.model.AUIUserInfo;
import io.agora.rtmsyncmanager.service.callback.AUICallback;
import io.agora.rtmsyncmanager.service.callback.AUIUserListCallback;

/**
 * 房间Service抽象协议
 */
public interface IAUIUserService extends IAUICommonService<IAUIUserService.AUIUserRespObserver> {

    /**
     * 获取指定 userId 的用户信息，如果为 null，则获取房间内所有人的信息
     *
     * @param roomId     房间唯一id
     * @param callback   成功/失败回调
     */
    void getUserInfoList(@NonNull String roomId, @Nullable AUIUserListCallback callback);

    /**
     * 获取指定 userId 的用户信息
     *
     * @param userId     要获取的用户id
     */
    @Nullable
    AUIUserInfo getUserInfo(@NonNull String userId);

    /**
     * 对自己静音/解除静音
     *
     * @param isMute   开关
     */
    void muteUserAudio(boolean isMute, @Nullable AUICallback callback);

    /**
     * 对自己禁摄像头/解禁摄像头
     *
     * @param isMute     开关
     */
    void muteUserVideo(boolean isMute, @Nullable AUICallback callback);

    interface AUIUserRespObserver {
        /**
         * 用户进入房间后获取到的所有用户信息
         *
         * @param roomId   房间唯一id
         * @param userList 所有用户信息
         */
        default void onRoomUserSnapshot(@NonNull String roomId, @Nullable List<AUIUserInfo> userList) {}

        /**
         * 用户进入房间回调
         *
         * @param roomId   房间唯一id
         * @param userInfo 用户信息
         */
        default void onRoomUserEnter(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {}

        /**
         * 用户离开房间回调
         *
         * @param roomId   房间唯一id
         * @param userInfo 用户信息
         */
        default void onRoomUserLeave(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {}

        /**
         * 用户信息修改
         *
         * @param roomId   房间唯一id
         * @param userInfo 用户信息
         */
        default void onRoomUserUpdate(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {}

        /**
         * 用户是否静音
         *
         * @param userId   用户唯一id
         * @param mute  是否禁用
         */
        default void onUserAudioMute(@NonNull String userId, boolean mute) {}

        /**
         * 用户是否禁用摄像头
         *
         * @param userId   用户唯一id
         * @param mute  是否禁用
         */
        default void onUserVideoMute(@NonNull String userId, boolean mute) {}
    }
}
