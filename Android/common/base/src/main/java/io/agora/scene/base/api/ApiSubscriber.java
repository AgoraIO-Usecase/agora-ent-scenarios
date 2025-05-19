package io.agora.scene.base.api;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.concurrent.TimeoutException;

import io.agora.scene.base.R;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.manager.PagePilotManager;
import io.agora.scene.base.manager.UserManager;
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
            if (((BaseResponse<?>) t).getCode() != 0) {
                String error = ((BaseResponse<?>) t).getMessage();
                onFailure(new ApiException(((BaseResponse<?>) t).getCode(), error));
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
        int errorCode = 0;
        String errorMsg = null;
        Context context = AgoraApplication.the();
        if (e instanceof ConnectException || e instanceof SocketTimeoutException
                || e instanceof TimeoutException || e instanceof UnknownHostException) {
            errorMsg = context.getString(R.string.api_error_connect);
            errorCode = ErrorCode.NETWORK_ERROR;
        } else if (e instanceof JsonParseException || e instanceof JSONException || e instanceof ParseException) {   //均视为解析错误
            errorMsg = context.getString(R.string.api_error_parse);
            errorCode = ErrorCode.SERVER_ERROR;
        } else if (e instanceof ApiException) {
            errorMsg = e.getMessage();
            errorCode = ((ApiException) e).errCode;
        } else if (e instanceof IllegalArgumentException) {
            errorMsg = context.getString(R.string.api_error_argument);
            errorCode = ErrorCode.SERVER_ERROR;
        } else if (e instanceof HttpException) {
            if (((HttpException) e).code() == ErrorCode.TOKEN_ERROR) {
                errorMsg = context.getString(R.string.api_error_token);
                errorCode = ErrorCode.TOKEN_ERROR;
                UserManager.getInstance().logout();
                PagePilotManager.pageWelcomeClear();
            } else {
                errorMsg = context.getString(R.string.api_error_http,((HttpException) e).code(), e.getMessage());
            }
        } else {
            errorMsg = context.getString(R.string.api_error_unknown);
            errorCode = ErrorCode.UNKNOWN_ERROR;
        }
        return new ApiException(errorCode, errorMsg);
    }
}
