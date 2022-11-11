package io.agora.scene.voice.general.repositories;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import io.agora.scene.voice.general.net.ChatroomHttpManager;
import io.agora.voice.baseui.general.callback.ResultCallBack;
import io.agora.voice.baseui.general.net.Resource;
import io.agora.voice.network.tools.VRValueCallBack;
import io.agora.voice.network.tools.bean.VRGiftBean;
import io.agora.voice.network.tools.bean.VRMicListBean;
import io.agora.voice.network.tools.bean.VRoomUserBean;

public class ChatroomHandsRepository extends BaseRepository{

    public LiveData<Resource<VRMicListBean>> getRaisedList(Context context, String roomId, int pageSize, String cursor) {
        return new NetworkOnlyResource<VRMicListBean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<VRMicListBean>> callBack) {
                ChatroomHttpManager.getInstance(context).getApplyMicList(roomId, pageSize, cursor, new VRValueCallBack<VRMicListBean>() {
                    @Override
                    public void onSuccess(VRMicListBean var1) {
                        callBack.onSuccess(createLiveData(var1));
                    }

                    @Override
                    public void onError(int code, String desc) {
                        callBack.onError(code,desc);
                    }
                });
            }
        }.asLiveData();
    }

    public LiveData<Resource<VRoomUserBean>> getInvitedList(Context context, String roomId, int pageSize, String cursor) {
        return new NetworkOnlyResource<VRoomUserBean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<VRoomUserBean>> callBack) {
                ChatroomHttpManager.getInstance(context).getRoomMembers(roomId, pageSize, cursor, new VRValueCallBack<VRoomUserBean>() {
                    @Override
                    public void onSuccess(VRoomUserBean var1) {
                        callBack.onSuccess(createLiveData(var1));
                    }

                    @Override
                    public void onError(int code, String desc) {
                        callBack.onError(code,desc);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 礼物榜单
     */
    public LiveData<Resource<VRGiftBean>> getGifts(Context context, String roomId) {
        return new NetworkOnlyResource<VRGiftBean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<VRGiftBean>> callBack) {
                ChatroomHttpManager.getInstance(context).getGiftList(roomId, new VRValueCallBack<VRGiftBean>() {
                    @Override
                    public void onSuccess(VRGiftBean var1) {
                        callBack.onSuccess(createLiveData(var1));
                    }

                    @Override
                    public void onError(int code, String desc) {
                        callBack.onError(code,desc);
                    }
                });
            }
        }.asLiveData();
    }
}
