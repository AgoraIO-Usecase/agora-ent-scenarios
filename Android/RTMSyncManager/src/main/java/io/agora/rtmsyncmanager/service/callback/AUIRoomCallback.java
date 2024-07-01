package io.agora.rtmsyncmanager.service.callback;

import androidx.annotation.Nullable;
import io.agora.rtmsyncmanager.model.AUIRoomInfo;

/**
 * AUIRoomCallback is an interface that defines a method to handle the result of a room operation.
 * It includes a method to handle the event when a room operation completes, either successfully or with an error.
 */
public interface AUIRoomCallback {

    /**
     * Called when a room operation completes.
     * @param error If the operation was successful, this will be null. If the operation failed, this will be an instance of AUIException describing the error.
     * @param roomInfo If the operation was successful, this will be an instance of AUIRoomInfo describing the room. If the operation failed, this will be null.
     */
    void onResult(@Nullable AUIException error, @Nullable AUIRoomInfo roomInfo);
}