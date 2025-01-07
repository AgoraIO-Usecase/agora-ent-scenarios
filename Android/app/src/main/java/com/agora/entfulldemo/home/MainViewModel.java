package com.agora.entfulldemo.home;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.R;

import java.io.File;

import io.agora.scene.base.Constant;
import io.agora.scene.base.SceneConfigManager;
import io.agora.scene.base.api.ApiException;
import io.agora.scene.base.api.ApiManager;
import io.agora.scene.base.api.ApiSubscriber;
import io.agora.scene.base.api.apiutils.SchedulersUtil;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.CommonBean;
import io.agora.scene.base.component.BaseRequestViewModel;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.widget.toast.CustomToast;
import io.reactivex.disposables.Disposable;

public class MainViewModel extends BaseRequestViewModel {

    /**
     * Get user information
     * @param syncUi whether to notify UI update
     */
    public void requestUserInfo(String userNo, boolean syncUi) {
        ApiManager.getInstance().requestUserInfo(userNo)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<User> data) {
                                if (data.isSuccess() && data.getData() != null) {
                                    UserManager.getInstance().saveUserInfo(data.getData(), false);
                                    if (syncUi) {
                                        getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_REQUEST_USER_INFO, null);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                if (t!=null && t.getMessage()!=null){
                                    CustomToast.show(t.getMessage(), Toast.LENGTH_SHORT);
                                }
                            }
                        }
                );
    }

    /**
     * Update user information
     */
    public void requestEditUserInfo(String headUrl,
                                    String name,
                                    String sex) {
        ApiManager.getInstance().requestUserUpdate(headUrl, name, sex, UserManager.getInstance().getUser().userNo).
                compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<User> data) {
                                CustomToast.show(R.string.app_edit_success,Toast.LENGTH_SHORT);
                                if (data.isSuccess() && data.getData() != null) {
                                    UserManager.getInstance().saveUserInfo(data.getData(), false);
                                }
                                getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_USER_INFO_CHANGE, null);
                                requestUserInfo(UserManager.getInstance().getUser().userNo, false);
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                // 恢复ui
                                getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_USER_INFO_CHANGE, null);
                                if (t.errCode == Constant.CALLBACK_TYPE_UPLOAD_ILLEGAL_CONTENT) {
                                    CustomToast.show(R.string.app_upload_illegal_content_error,Toast.LENGTH_SHORT);
                                } else {
                                    if (t.getMessage() != null) {
                                        CustomToast.show(t.getMessage(), Toast.LENGTH_SHORT);
                                    }
                                }
                            }
                        });
    }

    public void updatePhoto(File file) {
        ApiManager.getInstance().requestUploadPhoto(file)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<CommonBean>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<CommonBean> data) {
                                UserManager.getInstance().getUser().headUrl = data.getData().url;
                                requestEditUserInfo(data.getData().url, null, null);
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                if (t.getMessage() != null) {
                                    CustomToast.show(t.getMessage(), Toast.LENGTH_SHORT);
                                }
                            }
                        }
                );
    }

    /**
     * Deactivate user account
     */
    public void requestCancellation(String userNo) {
        ApiManager.getInstance().requestCancellationUser(userNo)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<String>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<String> data) {
                                UserManager.getInstance().logoff();
                                if (getISingleCallback() != null) {
                                    getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_USER_LOGOFF, null);
                                }
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                if (t.getMessage() != null) {
                                    CustomToast.show(t.getMessage(), Toast.LENGTH_SHORT);
                                }
                            }
                        }
                );
    }

    public void requestReportDevice(String userNo, String sceneId) {
        ApiManager.getInstance().requestReportDevice(userNo, sceneId)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<String> data) {
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                            }
                        }
                );
    }

    public void requestReportAction(String userNo, String action) {
        ApiManager.getInstance().requestReportAction(userNo, action)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<String> stringBaseResponse) {
                            }

                            @Override
                            public void onFailure(ApiException t) {
                            }
                        }
                );
    }

    public void fetchSceneConfig() {
        SceneConfigManager.INSTANCE.fetchSceneConfig(null, null);
    }
}