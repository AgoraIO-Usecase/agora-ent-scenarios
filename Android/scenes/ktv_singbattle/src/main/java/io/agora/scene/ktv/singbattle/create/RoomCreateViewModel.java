package io.agora.scene.ktv.singbattle.create;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.singbattle.service.CreateRoomInputModel;
import io.agora.scene.ktv.singbattle.service.CreateRoomOutputModel;
import io.agora.scene.ktv.singbattle.service.JoinRoomInputModel;
import io.agora.scene.ktv.singbattle.service.JoinRoomOutputModel;
import io.agora.scene.ktv.singbattle.service.KTVServiceProtocol;
import io.agora.scene.ktv.singbattle.service.RoomListModel;
import io.reactivex.internal.observers.LambdaObserver;
import kotlin.jvm.internal.Lambda;

public class RoomCreateViewModel extends AndroidViewModel {
    private final KTVServiceProtocol ktvServiceProtocol = KTVServiceProtocol.Companion.getImplInstance();

    public final MutableLiveData<List<RoomListModel>> roomModelList = new MutableLiveData<>();
    public final MutableLiveData<JoinRoomOutputModel> joinRoomResult = new MutableLiveData<>();
    public final MutableLiveData<CreateRoomOutputModel> createRoomResult = new MutableLiveData<>();

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
        ktvServiceProtocol.createRoom(new CreateRoomInputModel(
                icon, isPrivate, name, password, userNo
        ), (e, createRoomOutputModel) -> {
            if (e == null && createRoomOutputModel != null) {
                // success
                createRoomResult.postValue(createRoomOutputModel);
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
        ktvServiceProtocol.joinRoom(new JoinRoomInputModel(roomNo, password),
                (e, joinRoomOutputModel) -> {
                    if (e == null && joinRoomOutputModel != null) {
                        // success
                        joinRoomResult.postValue(joinRoomOutputModel);
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
