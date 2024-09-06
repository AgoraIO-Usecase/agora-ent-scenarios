package io.agora.rtmsyncmanager.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import io.agora.rtmsyncmanager.model.AUIUserInfo;
import io.agora.rtmsyncmanager.service.callback.AUICallback;
import io.agora.rtmsyncmanager.service.callback.AUIUserListCallback;
import io.agora.rtmsyncmanager.service.rtm.AUIRtmUserLeaveReason;

/**
 * IAUIUserService is an interface that defines the abstract protocol for room services.
 * It includes methods for getting user information, muting/unmuting user audio and video, and observing user responses.
 */
public interface IAUIUserService extends IAUICommonService<IAUIUserService.AUIUserRespObserver> {

    /**
     * Get the information of a specific user. If the user ID is null, get the information of all users in the room.
     * @param roomId The unique ID of the room.
     * @param callback The callback to be invoked when the request is complete.
     */
    void getUserInfoList(@NonNull String roomId, @Nullable AUIUserListCallback callback);

    /**
     * Get the information of a specific user.
     * @param userId The ID of the user.
     * @return The user information.
     */
    @Nullable
    AUIUserInfo getUserInfo(@NonNull String userId);

    /**
     * Mute/unmute the user's audio.
     * @param isMute The switch to mute/unmute the audio.
     * @param callback The callback to be invoked when the request is complete.
     */
    void muteUserAudio(boolean isMute, @Nullable AUICallback callback);

    /**
     * Disable/enable the user's video.
     * @param isMute The switch to disable/enable the video.
     * @param callback The callback to be invoked when the request is complete.
     */
    void muteUserVideo(boolean isMute, @Nullable AUICallback callback);

    /**
     * AUIUserRespObserver is an interface that defines the methods for observing user responses.
     * It includes methods for handling user snapshots, user entry, user exit, user updates, and user audio/video mute.
     */
    interface AUIUserRespObserver {
        /**
         * Handle the snapshot of all users after a user enters a room.
         * @param roomId The unique ID of the room.
         * @param userList The list of all users.
         */
        default void onRoomUserSnapshot(@NonNull String roomId, @Nullable List<AUIUserInfo> userList) { }

        /**
         * Handle a user entering a room.
         * @param roomId The unique ID of the room.
         * @param userInfo The information of the user.
         */
        default void onRoomUserEnter(@NonNull String roomId, @NonNull AUIUserInfo userInfo) { }

        /**
         * Handle a user leaving a room.
         * @param roomId The unique ID of the room.
         * @param userInfo The information of the user.
         * @param reason The reason for the user leaving.
         */
        default void onRoomUserLeave(@NonNull String roomId, @NonNull AUIUserInfo userInfo, @NonNull AUIRtmUserLeaveReason reason) { }

        /**
         * Handle a user update.
         * @param roomId The unique ID of the room.
         * @param userInfo The updated user information.
         */
        default void onRoomUserUpdate(@NonNull String roomId, @NonNull AUIUserInfo userInfo) { }

        /**
         * Handle a user muting their audio.
         * @param userId The unique ID of the user.
         * @param mute Whether the user is muted.
         */
        default void onUserAudioMute(@NonNull String userId, boolean mute) { }

        /**
         * Handle a user disabling their video.
         * @param userId The unique ID of the user.
         * @param mute Whether the user's video is disabled.
         */
        default void onUserVideoMute(@NonNull String userId, boolean mute) { }
    }
}