package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;

/**
 * AUIUserInfo is a class that extends AUIUserThumbnailInfo and holds additional information about a user.
 * This includes the mute audio status, mute video status, and custom payload.
 */
public class AUIUserInfo extends AUIUserThumbnailInfo {
    // Boolean indicating if the user's audio is muted
    public boolean muteAudio = false;
    // Boolean indicating if the user's video is muted
    public boolean muteVideo = true;
    // String to hold custom payload data
    public String customPayload;

    /**
     * Overrides the toString method from the superclass.
     * Returns a string representation of the AUIUserInfo object.
     * @return A string representation of the AUIUserInfo object.
     */
    @NonNull
    @Override
    public String toString() {
        return "AUIUserInfo{"
                + "muteAudio=" + muteAudio
                + ", muteVideo=" + muteVideo
                + ", customPayload=" + customPayload
                + "} "
                + super.toString();
    }
}