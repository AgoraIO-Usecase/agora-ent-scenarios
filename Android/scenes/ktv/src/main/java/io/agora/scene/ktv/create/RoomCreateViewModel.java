package io.agora.scene.ktv.create;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.data.model.AgoraRoom;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.manager.RTMManager;
import io.agora.scene.ktv.manager.RoomManager;
import io.agora.scene.ktv.service.KTVCreateRoomInputModel;
import io.agora.scene.ktv.service.KTVJoinRoomInputModel;
import io.agora.scene.ktv.service.KTVServiceImp;
import io.agora.scene.ktv.service.KTVServiceProtocol;
import io.agora.scene.ktv.service.VLRoomListModel;

public class RoomCreateViewModel extends AndroidViewModel {
    public final static int ERROR_PASSWORD = 20009;

    private final KTVServiceProtocol ktvServiceProtocol;


    public final MutableLiveData<List<VLRoomListModel>> roomModelList = new MutableLiveData<>();
    public final MutableLiveData<Boolean> joinRoomResult = new MutableLiveData<>();
    public final MutableLiveData<Boolean> createRoomResult = new MutableLiveData<>();


    public RoomCreateViewModel(@NonNull Application application) {
        super(application);
        ktvServiceProtocol = new KTVServiceImp();
    }

    public void loginRTM() {
        RTMManager.getInstance().doLoginRTM();
    }

    public void logOutRTM() {
        RTMManager.getInstance().doLogoutRTM();
    }

    /**
     * 加载房间列表
     */
    public void loadRooms() {
        ktvServiceProtocol.getRoomListWithPage((e, vlRoomListModels) -> {
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
                "", icon, isPrivate, name, password, "", userNo
        ), (e, ktvCreateRoomOutputModel) -> {
            if (e == null && ktvCreateRoomOutputModel != null) {
                // success
                AgoraRoom room = new AgoraRoom();
                room.roomNo = ktvCreateRoomOutputModel.getRoomNo();
                room.creatorNo = UserManager.getInstance().getUser().userNo;
                room.isPrivate = isPrivate;
                room.name = name;
                room.belCanto = "0";
                room.icon = icon;
                // TODO remove RoomManager. Do not save room info, but just pass it to living room.
                RoomManager.getInstance().setAgoraRoom(room);

                // TODO login RTM. We should save the token?
                KtvConstant.RTM_TOKEN = ktvCreateRoomOutputModel.getAgoraRTMToken();
                KtvConstant.RTC_TOKEN = ktvCreateRoomOutputModel.getAgoraRTCToken();
                KtvConstant.PLAYER_TOKEN = ktvCreateRoomOutputModel.getAgoraPlayerRTCToken();
                loginRTM();

                createRoomResult.postValue(true);
            } else {
                // failed
                if (e != null) {
                    ToastUtils.showToast(e.getMessage());
                }
                createRoomResult.postValue(false);
            }
            return null;
        });
    }

    public void joinRoom(String roomNo, String password) {
        ktvServiceProtocol.joinRoomWithInput(new KTVJoinRoomInputModel(roomNo, password), (e, ktvJoinRoomOutputModel) -> {
            if(e == null && ktvJoinRoomOutputModel != null){
                // success

                AgoraRoom room = new AgoraRoom();
                room.roomNo = roomNo;
                room.creatorNo = ktvJoinRoomOutputModel.getCreator();
                RoomManager.getInstance().setAgoraRoom(room);

                KtvConstant.RTM_TOKEN = ktvJoinRoomOutputModel.getAgoraRTMToken();
                KtvConstant.RTC_TOKEN = ktvJoinRoomOutputModel.getAgoraRTCToken();
                KtvConstant.PLAYER_TOKEN = ktvJoinRoomOutputModel.getAgoraPlayerRTCToken();
                loginRTM();

                joinRoomResult.postValue(true);
            } else {
                // failed
                if (e != null) {
                    ToastUtils.showToast(e.getMessage());
                }
                joinRoomResult.postValue(false);
            }
            return null;
        });
    }

}
