package io.agora.rtmsyncmanager.service.callback;

import androidx.annotation.Nullable;
import java.util.List;
import io.agora.rtmsyncmanager.model.AUIRoomInfo;

/**
 * AUIRoomListCallback is an interface that defines a method to handle the result of a room list operation.
 * It includes a method to handle the event when a room list operation completes, either successfully or with an error.
 */
public interface AUIRoomListCallback {

    /**
     * Called when a room list operation completes.
     * @param error If the operation was successful, this will be null. If the operation failed, this will be an instance of AUIException describing the error.
     * @param roomList If the operation was successful, this will be a list of AUIRoomInfo instances describing the rooms. If the operation failed, this will be null.
     * @param ts If the operation was successful, this will be a timestamp indicating when the operation completed. If the operation failed, this will be null.
     */
    void onResult(@Nullable AUIException error, @Nullable List<AUIRoomInfo> roomList, @Nullable Long ts);
}