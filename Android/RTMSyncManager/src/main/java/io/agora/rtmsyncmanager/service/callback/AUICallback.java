package io.agora.rtmsyncmanager.service.callback;

import androidx.annotation.Nullable;

/**
 * AUICallback is an interface that defines a single method to handle the result of an operation.
 * It includes a method to handle the event when an operation completes, either successfully or with an error.
 */
public interface AUICallback {

    /**
     * Called when an operation completes.
     * @param error If the operation was successful, this will be null. If the operation failed, this will be an instance of AUIException describing the error.
     */
    void onResult(@Nullable AUIException error);
}