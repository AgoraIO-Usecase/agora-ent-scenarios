package io.agora.scene.base.api;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.agora.scene.base.BuildConfig;
import io.agora.scene.base.api.apiutils.GsonUtils;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.common.NetConstants;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.CommonBean;
import io.agora.scene.base.manager.UserManager;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class ApiManager {
    private Gson mGson;
    private final static long TIMEOUT = 5;
    private OkHttpClient httpClient;
    private ApiManagerService apiManagerService;
    public static String token;

    private ApiManager() {
        if (mGson == null) {
            mGson = new GsonBuilder().serializeNulls()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(String.class, new GsonUtils.StringConverter()).create();
        }
        httpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();
                    builder.addHeader(NetConstants.HEADER_APP_OS, "android");
                    builder.addHeader(NetConstants.HEADER_VERSION_NAME, BuildConfig.APP_VERSION_NAME);
                    builder.addHeader(NetConstants.HEADER_VERSION_CODE, String.valueOf(BuildConfig.APP_VERSION_CODE));
                    if (!TextUtils.isEmpty(token)) {
                        builder.addHeader(NetConstants.AUTHORIZATION, token);
                    } else {
                        if (UserManager.getInstance().getUser() != null) {
                            token = UserManager.getInstance().getUser().token;
                        }
                        if (!TextUtils.isEmpty(token)) {
                            builder.addHeader(NetConstants.AUTHORIZATION, token);
                        }
                    }
                    return chain.proceed(builder.build());
                })
                .addInterceptor(new HttpLoggingInterceptor())
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();


        Retrofit sRetrofit = new Retrofit.Builder()
                .baseUrl(UrlConstants.BASE_URL)
                .addConverterFactory(ResponseConverterFactory.Companion.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build();
        apiManagerService = sRetrofit.create(ApiManagerService.class);
    }

    private static class SingletonHolder {
        private static final ApiManager INSTANCE = new ApiManager();
    }

    public static ApiManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Observable<BaseResponse<String>> requestSendVerCode(String phone) {
        return apiManagerService.requestSendVerCode(phone).flatMap(it -> Observable.just(it));
    }

    /**
     * 登录
     *
     * @param phone 手机号
     * @param vCode 验证码
     */
    public Observable<BaseResponse<User>> requestLogin(String phone, String vCode) {
        return apiManagerService.requestLogin(phone, vCode).flatMap(it -> Observable.just(it));
    }

    /**
     * 获取用户信息
     *
     * @param userNo 用户id
     */
    public Observable<BaseResponse<User>> requestUserInfo(String userNo) {
        return apiManagerService.requestUserInfo(userNo).flatMap(it -> Observable.just(it));
    }

    /**
     *
     */
    public Observable<BaseResponse<CommonBean>> requestUploadPhoto(File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part partFile = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        return apiManagerService.requestUploadPhoto(partFile).flatMap(it -> Observable.just(it));
    }

    /**
     * 注销用户
     *
     * @param userNo 用户id
     */
    public Observable<BaseResponse<String>> requestCancellationUser(String userNo) {
        return apiManagerService.requestCancellationUser(userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * 修改
     */
    public Observable<BaseResponse<User>> requestUserUpdate(
            String headUrl,
            String name,
            String sex,
            String userNo) {
        ArrayMap<String, String> params = new ArrayMap();
        if (!TextUtils.isEmpty(headUrl)) {
            params.put("headUrl", headUrl);
        }
        if (!TextUtils.isEmpty(name)) {
            params.put("name", name);
        }
        if (!TextUtils.isEmpty(sex)) {
            params.put("sex", sex);
        }
        if (!TextUtils.isEmpty(userNo)) {
            params.put("userNo", userNo);
        }
        return apiManagerService.requestUserUpdate(getRequestBody(params)).flatMap(it -> Observable.just(it));
    }

    private RequestBody getRequestBody(ArrayMap<String, String> params) {
        return RequestBody.create(
                MediaType.parse("application/json;charset=UTF-8"),
                GsonUtils.Companion.getGson().toJson(params)
        );
    }
}
