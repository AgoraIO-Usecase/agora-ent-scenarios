package io.agora.rtmsyncmanager.service.callback;

import androidx.annotation.Nullable;
import java.util.List;
import io.agora.rtmsyncmanager.model.AUIRoomInfo;


public interface AUIRoomListCallback {
    void onResult(@Nullable AUIException error, @Nullable List<AUIRoomInfo> roomList, @Nullable Long ts);
}

