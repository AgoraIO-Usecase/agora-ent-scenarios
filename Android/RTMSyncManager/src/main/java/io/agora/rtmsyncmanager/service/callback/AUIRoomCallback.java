package io.agora.rtmsyncmanager.service.callback;

import androidx.annotation.Nullable;
import io.agora.rtmsyncmanager.model.AUIRoomInfo;

public interface AUIRoomCallback {
    void onResult(@Nullable AUIException error, @Nullable AUIRoomInfo roomInfo);
}
