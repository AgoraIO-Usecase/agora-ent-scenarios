package io.agora.rtmsyncmanager.service.callback;

import androidx.annotation.Nullable;
import java.util.List;
import io.agora.rtmsyncmanager.model.AUIUserInfo;

/**
 * AUIUserListCallback is an interface that defines a method to handle the result of a user list operation.
 * It includes a method to handle the event when a user list operation completes, either successfully or with an error.
 */
public interface AUIUserListCallback {

    /**
     * Called when a user list operation completes.
     * @param error If the operation was successful, this will be null. If the operation failed, this will be an instance of AUIException describing the error.
     * @param userList If the operation was successful, this will be a list of AUIUserInfo instances describing the users. If the operation failed, this will be null.
     */
    void onResult(@Nullable AUIException error, @Nullable List<AUIUserInfo> userList);
}