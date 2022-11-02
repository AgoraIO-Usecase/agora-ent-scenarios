package io.agora.scene.ktv.create;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.api.ApiException;
import io.agora.scene.base.api.ApiManager;
import io.agora.scene.base.api.ApiSubscriber;
import io.agora.scene.base.api.apiutils.SchedulersUtil;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.bean.RoomListModel;
import io.agora.scene.base.component.BaseRequestViewModel;
import io.agora.scene.base.data.model.AgoraRoom;
import io.agora.scene.base.manager.RTMManager;
import io.agora.scene.base.manager.RoomManager;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.reactivex.disposables.Disposable;

public class RoomCreateViewModel extends BaseRequestViewModel {
    private int current = 1;
    private int size = 30;

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
        ApiManager.getInstance().requestRoomList(current, size)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<RoomListModel>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<RoomListModel> data) {
                                getISingleCallback().onSingleCallback(KtvConstant.CALLBACK_TYPE_ROOM_GET_ROOM_LIST_SUCCESS
                                        , data.getData().records);

                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                //创建失败
                                getISingleCallback().onSingleCallback(KtvConstant.CALLBACK_TYPE_ROOM_GET_ROOM_LIST_FAIL, null);
                                ToastUtils.showToast(t.getMessage());
                            }
                        }
                );
    }

    public void getRoomToken(String roomNo, String password) {
        ApiManager.getInstance().requestGetRoomInfo(roomNo, password)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<AgoraRoom>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<AgoraRoom> data) {
                                KtvConstant.RTM_TOKEN = data.getData().agoraRTMToken;
                                KtvConstant.RTC_TOKEN = data.getData().agoraRTCToken;
                                KtvConstant.PLAYER_TOKEN = data.getData().agoraPlayerRTCToken;
                                loginRTM();
                                if (isCreator) {
                                    //创建成功 直接加入房间
                                    getISingleCallback().onSingleCallback(KtvConstant.CALLBACK_TYPE_ROOM_CREATE_SUCCESS, null);
                                    ToastUtils.showToast("创建房间成功");
                                    isCreator = false;
                                } else {
                                    getISingleCallback().onSingleCallback(KtvConstant.CALLBACK_TYPE_ROOM_RTM_RTC_TOKEN, null);
                                }

                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                if (t != null && t.errCode == 20009) {
                                    getISingleCallback().onSingleCallback(KtvConstant.CALLBACK_TYPE_ROOM_PASSWORD_ERROR, null);
                                }
                            }
                        }
                );
    }

    private boolean isCreator = false;

    /**
     * 创建房间
     *
     * @param isPrivate 是否是私密 1 是 0 否
     * @param name      房间名称
     * @param password  房间密码
     * @param userNo    用户id
     * @param icon      icon图
     */
    public void requestCreateRoom(int isPrivate, String name,
                                  String password, String userNo, String icon) {
        ApiManager.getInstance().requestCreateRoom(isPrivate, name, password, userNo, icon)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<String>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<String> data) {
                                AgoraRoom room = new AgoraRoom();
                                room.roomNo = data.getData();
                                room.creatorNo = UserManager.getInstance().getUser().userNo;
                                room.isPrivate = isPrivate;
                                room.name = name;
                                room.belCanto = "0";
                                room.icon = icon;
//                                if (isPrivate == 1) {
//                                    room.password = password;
//                                }
                                RoomManager.getInstance().setAgoraRoom(room);

                                isCreator = true;
                                getRoomToken(room.roomNo, null);
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                //创建失败
                                getISingleCallback().onSingleCallback(KtvConstant.CALLBACK_TYPE_ROOM_CREATE_FAIL, null);
                                ToastUtils.showToast(t.getMessage());
                            }
                        }
                );
    }


}
