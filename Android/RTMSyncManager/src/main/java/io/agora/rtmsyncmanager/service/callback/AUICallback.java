package io.agora.rtmsyncmanager.service.callback;


import androidx.annotation.Nullable;

public interface AUICallback {

    /**
     * @param error null: success, notNull: fail
     */
    void onResult(@Nullable AUIException error);

}
