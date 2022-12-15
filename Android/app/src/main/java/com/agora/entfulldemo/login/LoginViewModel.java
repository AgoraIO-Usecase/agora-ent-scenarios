package com.agora.entfulldemo.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.Constant;
import io.agora.scene.base.api.ApiException;
import io.agora.scene.base.api.ApiManager;
import io.agora.scene.base.api.ApiSubscriber;
import io.agora.scene.base.api.apiutils.SchedulersUtil;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.component.BaseRequestViewModel;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.reactivex.disposables.Disposable;

public class LoginViewModel extends BaseRequestViewModel {

    /**
     * 登录
     *
     * @param account 账号
     * @param vCode   验证码
     */
    public void requestLogin(String account, String vCode) {
        if (!account.equals(phone)) {
            getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_FAIL, null);
            ToastUtils.showToast("验证码错误");
            return;
        }
        ApiManager.getInstance().requestLogin(account, vCode)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                new ApiSubscriber<BaseResponse<User>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addDispose(d);
                    }

                    @Override
                    public void onSuccess(BaseResponse<User> data) {
                        ToastUtils.showToast("登录成功");
                        ApiManager.token = (data.getData().token);
                        UserManager.getInstance().saveUserInfo(data.getData());
                        getISingleCallback().onSingleCallback(Constant.CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_SUCCESS, null);
                    }

                    @Override
                    public void onFailure(@Nullable ApiException t) {
                        ToastUtils.showToast(t.getMessage());
                    }
                }
        );
    }

    private String phone;

    /**
     * 发送验证码
     *
     * @param phone 手机号
     */
    public void requestSendVCode(String phone) {
        this.phone = phone;
        ApiManager.getInstance().requestSendVerCode(phone)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                new ApiSubscriber<BaseResponse<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        addDispose(d);
                    }

                    @Override
                    public void onSuccess(BaseResponse<String> stringBaseResponse) {
                        ToastUtils.showToast("验证码发送成功");
                    }

                    @Override
                    public void onFailure(@Nullable ApiException t) {
                        ToastUtils.showToast(t.getMessage());
                    }
                }
        );
    }
}
