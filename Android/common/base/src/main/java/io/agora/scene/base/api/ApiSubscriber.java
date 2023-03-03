package io.agora.scene.base.api;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.concurrent.TimeoutException;

import io.agora.scene.base.Constant;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.event.UserLogoutEvent;
import io.agora.scene.base.utils.SPUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

public abstract class ApiSubscriber<T> implements Observer<T> {
    private final static String TAG = "ApiSubscriber";

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(@NonNull T t) {
        if (t instanceof BaseResponse) {
//            if (((BaseResponse<?>) t).getCode() == 401) {
//                ToastUtils.showToast("登录超时，请重新登录");
//                EventBus.getDefault().post(new UserLogoutEvent());
//                onFailure(new ApiException(((BaseResponse<?>) t).getCode(), ((BaseResponse<?>) t).getMessage()));
//            } else
            if (((BaseResponse<?>) t).getCode() != 0) {
                String error = ((BaseResponse<?>) t).getMessage();
                onFailure(new ApiException(((BaseResponse<?>) t).getCode(), error));
//                if (!TextUtils.isEmpty(error)) {
//                    ToastUtils.showToast(error);
//                }
            } else {
                onSuccess(t);
            }
        }
    }

    public void onNetWorkError() {

    }

    public abstract void onSuccess(T t);

    @Override
    public void onError(@NonNull Throwable t) {
        if (t instanceof ConnectException || t instanceof SocketTimeoutException
                || t instanceof TimeoutException || t instanceof UnknownHostException) {
            onFailure(wrapException(t));
            onNetWorkError();
        } else {
            onFailure(wrapException(t));
        }
    }

    public abstract void onFailure(ApiException t);

    @Override
    public void onComplete() {

    }

    private ApiException wrapException(Throwable e) {
        e.printStackTrace();
        int errorCode = 0;
        String errorMsg = null;
        if (e instanceof ConnectException || e instanceof SocketTimeoutException
                || e instanceof TimeoutException || e instanceof UnknownHostException) {//网络超时
            errorMsg = "网络连接异常";
            errorCode = ErrorCode.NETWORK_ERROR;
        } else if (e instanceof JsonParseException || e instanceof JSONException || e instanceof ParseException) {   //均视为解析错误
            errorMsg = "数据解析异常";
            errorCode = ErrorCode.SERVER_ERROR;
        } else if (e instanceof ResultException) {//服务器返回的错误信息
            errorMsg = e.getMessage();
            errorCode = ((ResultException) e).getErrCode();
        } else if (e instanceof IllegalArgumentException) {
            errorMsg = "参数错误";
            errorCode = ErrorCode.SERVER_ERROR;
        } else if (e instanceof HttpException) {
            if (((HttpException) e).code() == ErrorCode.TOKEN_ERROR) {
                errorMsg = "登录超时，请重新登录";
                errorCode = ErrorCode.TOKEN_ERROR;
                EventBus.getDefault().post(new UserLogoutEvent());
                SPUtil.putBoolean(Constant.IS_AGREE, false);
            } else {
                errorMsg = "错误 : errorCode = " + ((HttpException) e).code() + " ; errorMsg = " + e.getMessage();
            }
        } else {//未知错误
            errorMsg = "系统错误,稍后再试";
            errorCode = ErrorCode.UNKNOWN_ERROR;
        }
        return new ApiException(errorCode, errorMsg);
    }
}
