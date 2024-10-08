package com.agora.entfulldemo.home;

import android.text.TextUtils;
import android.util.Log;

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
import io.agora.scene.base.utils.ToastUtils;
import io.reactivex.disposables.Disposable;

public class MainViewModel extends BaseRequestViewModel {

    /**
     * 获取用户信息
     * @param syncUi 是否通知更新 ui
     */
    public void requestUserInfo(String userNo, boolean syncUi) {
        ApiManager.getInstance().requestUserInfo(userNo)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<User>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<User> data) {
                                UserManager.getInstance().getUser().name = data.getData().name;
                                UserManager.getInstance().getUser().headUrl = data.getData().headUrl;
                                UserManager.getInstance().saveUserInfo(UserManager.getInstance().getUser());
                                if (syncUi){
                                    getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_REQUEST_USER_INFO, null);
                                }
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                ToastUtils.showToast(t.getMessage());
                            }
                        }
                );
    }

    /**
     * 修改用户信息
     */
    public void requestEditUserInfo(String headUrl,
                                    String name,
                                    String sex) {
        ApiManager.getInstance().requestUserUpdate(headUrl, name, sex, UserManager.getInstance().getUser().userNo).
                compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<User>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<User> data) {
                                ToastUtils.showToast(R.string.app_edit_success);
                                if (!TextUtils.isEmpty(name)) {
                                    UserManager.getInstance().getUser().name = name;
                                }
                                if (!TextUtils.isEmpty(headUrl)) {
                                    UserManager.getInstance().getUser().headUrl = headUrl;
                                }
                                UserManager.getInstance().saveUserInfo(UserManager.getInstance().getUser());
                                getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_USER_INFO_CHANGE, null);
                                requestUserInfo(UserManager.getInstance().getUser().userNo, false);
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                // 恢复ui
                                getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_USER_INFO_CHANGE, null);
                                if (t.errCode == Constant.CALLBACK_TYPE_UPLOAD_ILLEGAL_CONTENT) {
                                    ToastUtils.showToast(R.string.app_upload_illegal_content_error);
                                } else {
                                    ToastUtils.showToast(t.getMessage());
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
                                ToastUtils.showToast(t.getMessage());
                            }
                        }
                );
    }

    /**
     * 注销用户
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
                                ToastUtils.showToast(t.getMessage());
                            }
                        }
                );
    }

    public void requestReportDevice(String userNo, String sceneId) {
        ApiManager.getInstance().requestReportDevice(userNo, sceneId)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<String>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<String> data) {
                                Log.d("requestReportDevice", "onSuccess");
                            }

                            @Override
                            public void onFailure(@Nullable ApiException t) {
                                Log.d("requestReportDevice", "onFailure:" + t.getMessage());
                            }
                        }
                );
    }

    public void requestReportAction(String userNo, String action) {
        ApiManager.getInstance().requestReportAction(userNo, action)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<String>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                addDispose(d);
                            }

                            @Override
                            public void onSuccess(BaseResponse<String> stringBaseResponse) {
                                Log.d("requestReportAction", "onSuccess");
                            }

                            @Override
                            public void onFailure(ApiException t) {
                                Log.d("requestReportAction", "onFailure:" + t.getMessage());
                            }
                        }
                );
    }

    public void fetchSceneConfig() {
        SceneConfigManager.INSTANCE.fetchSceneConfig(null, null);
    }
}