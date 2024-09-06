package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * AUIUserThumbnailInfo is a class that holds basic information about a user.
 * This includes the user ID, user name, and user avatar.
 */
public class AUIUserThumbnailInfo implements Serializable {

    // The ID of the user
    public @NonNull String userId = "";
    // The name of the user
    public @NonNull String userName = "";
    // The avatar of the user
    public @NonNull String userAvatar = "";

    /**
     * Overrides the toString method from the superclass.
     * Returns a string representation of the AUIUserThumbnailInfo object.
     * @return A string representation of the AUIUserThumbnailInfo object.
     */
    @NonNull
    @Override
    public String toString() {
        return "AUIUserThumbnailInfo{"
                + "userId='" + userId + '\''
                + ", userName='" + userName
                + '\''
                + ", userAvatar='" + userAvatar
                + '\''
                + '}';
    }
}