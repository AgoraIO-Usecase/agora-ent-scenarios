package io.agora.scene.ktv.create;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.service.KTVCreateRoomInputModel;
import io.agora.scene.ktv.service.KTVCreateRoomOutputModel;
import io.agora.scene.ktv.service.KTVJoinRoomInputModel;
import io.agora.scene.ktv.service.KTVJoinRoomOutputModel;
import io.agora.scene.ktv.service.KTVServiceProtocol;
import io.agora.scene.ktv.service.VLRoomListModel;

public class RoomCreateViewModel extends AndroidViewModel {
    private final KTVServiceProtocol ktvServiceProtocol = KTVServiceProtocol.Companion.getImplInstance();

    public final MutableLiveData<List<VLRoomListModel>> roomModelList = new MutableLiveData<>();
    public final MutableLiveData<KTVJoinRoomOutputModel> joinRoomResult = new MutableLiveData<>();
    public final MutableLiveData<KTVCreateRoomOutputModel> createRoomResult = new MutableLiveData<>();

    public RoomCreateViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 加载房间列表
     */
    public void loadRooms() {
        ktvServiceProtocol.getRoomList((e, vlRoomListModels) -> {
            if (e != null) {
                ToastUtils.showToast(e.getMessage());
                roomModelList.postValue(null);
                return null;
            }
            roomModelList.postValue(vlRoomListModels);
            return null;
        });
    }

    public void createRoom(int isPrivate,
                           String name, String password,
                           String userNo, String icon) {
        ktvServiceProtocol.createRoomWithInput(new KTVCreateRoomInputModel(
                icon, isPrivate, name, password, userNo, "", ""
        ), (e, ktvCreateRoomOutputModel) -> {
            if (e == null && ktvCreateRoomOutputModel != null) {
                // success
                createRoomResult.postValue(ktvCreateRoomOutputModel);
            } else {
                // failed
                if (e != null) {
                    ToastUtils.showToast(e.getMessage());
                }
                createRoomResult.postValue(null);
            }
            return null;
        });
    }

    public void joinRoom(String roomNo, String password) {
        ktvServiceProtocol.joinRoomWithInput(new KTVJoinRoomInputModel(roomNo, password),
                (e, ktvJoinRoomOutputModel) -> {
                    if (e == null && ktvJoinRoomOutputModel != null) {
                        // success
                        joinRoomResult.postValue(ktvJoinRoomOutputModel);
                    } else {
                        // failed
                        if (e != null) {
                            ToastUtils.showToast(e.getMessage());
                        }
                        joinRoomResult.postValue(null);
                    }
                    return null;
                });
    }

}
