package io.agora.scene.base.api;

import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.agora.scene.base.BuildConfig;
import io.agora.scene.base.ServerConfig;
import io.agora.scene.base.api.apiutils.GsonUtils;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.CommonBean;
import io.agora.scene.base.bean.FeedbackUploadResBean;
import io.agora.scene.base.manager.UserManager;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class ApiManager {
    private Gson mGson;
    private OkHttpClient httpClient;
    private ApiManagerService apiManagerService;
    public static String token;

    private ApiManager() throws NoSuchAlgorithmException, KeyManagementException {
        if (mGson == null) {
            mGson = new GsonBuilder().serializeNulls()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(String.class, new GsonUtils.StringConverter()).create();
        }
        OkHttpClient.Builder httpClientBuilder = SecureOkHttpClient.createWithSeconds(30)
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();
                    builder.addHeader("appProject", "agora_ent_demo");  // "appProject" "agora_ent_demo"
                    builder.addHeader("appOs", "android");               // "appOs" "android"
                    builder.addHeader("versionName", BuildConfig.APP_VERSION_NAME); // "versionName" "3.0.0"
                    builder.addHeader("versionCode", BuildConfig.APP_VERSION_CODE); // "versionCode" "5"
                    if (!TextUtils.isEmpty(token)) {
                        builder.addHeader("Authorization", token);
                    } else {
                        if (UserManager.getInstance().getUser() != null) {
                            token = UserManager.getInstance().getUser().token;
                        }
                        if (!TextUtils.isEmpty(token)) {
                            builder.addHeader("Authorization", token);
                        }
                    }
                    return chain.proceed(builder.build());
                });
        httpClient = httpClientBuilder.build();


        Retrofit sRetrofit = new Retrofit.Builder()
                .baseUrl(ServerConfig.getServerHost())
                .addConverterFactory(ResponseConverterFactory.Companion.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build();
        apiManagerService = sRetrofit.create(ApiManagerService.class);
    }

    private static class SingletonHolder {
        private static final ApiManager INSTANCE;

        static {
            try {
                INSTANCE = new ApiManager();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ApiManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Observable<BaseResponse<String>> requestSendVerCode(String phone) {
        return apiManagerService.requestSendVerCode(phone).flatMap(it -> Observable.just(it));
    }

    /**
     * login
     *
     * @param phone
     * @param vCode
     * @return
     */
    public Observable<BaseResponse<User>> requestLogin(String phone, String vCode) {
        return apiManagerService.requestLogin(phone, vCode).flatMap(it -> Observable.just(it));
    }

    /**
     * get uer info
     *
     * @param userNo
     * @return
     */
    public Observable<BaseResponse<User>> requestUserInfo(String userNo) {
        return apiManagerService.requestUserInfo(userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * upload photo
     *
     * @param file
     * @return
     */
    public Observable<BaseResponse<CommonBean>> requestUploadPhoto(File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part partFile = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        return apiManagerService.requestUploadPhoto(partFile).flatMap(it -> Observable.just(it));
    }

    /**
     * cancel user
     *
     * @param userNo 用户id
     */
    public Observable<BaseResponse<String>> requestCancellationUser(String userNo) {
        return apiManagerService.requestCancellationUser(userNo).flatMap(it -> Observable.just(it));
    }

    /**
     * update user info
     *
     * @param headUrl
     * @param name
     * @param sex
     * @param userNo
     * @return
     */
    public Observable<BaseResponse<User>> requestUserUpdate(
            String headUrl,
            String name,
            String sex,
            String userNo) {
        ArrayMap<String, String> params = new ArrayMap<>();
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

    /**
     * request report device
     *
     * @param userNo
     * @param sceneId
     * @return
     */
    public Observable<BaseResponse<String>> requestReportDevice(String userNo, String sceneId) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("appVersion", BuildConfig.APP_VERSION_NAME);
        params.put("platform", "Android");
        params.put("model", Build.MODEL);
        params.put("manufacture", Build.MANUFACTURER);
        params.put("osVersion", Build.VERSION.RELEASE);
        return apiManagerService.requestReportDevice(userNo, sceneId, BuildConfig.AGORA_APP_ID, "agora_ent_demo",
                getRequestBody(params)).flatMap(it -> Observable.just(it));
    }

    /**
     * request report action
     *
     * @param userNo
     * @param action
     * @return
     */
    public Observable<BaseResponse<String>> requestReportAction(String userNo, String action) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("action", action);
        return apiManagerService.requestReportAction(userNo, action, BuildConfig.AGORA_APP_ID, "agora_ent_demo",
                getRequestBody(params)).flatMap(it -> Observable.just(it));

    }

    /**
     * request upload log
     *
     * @param file
     * @return
     */
    public Observable<BaseResponse<CommonBean>> requestUploadLog(File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part partFile = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        return apiManagerService.requestUploadLog(partFile).flatMap(it -> Observable.just(it));
    }

    /**
     * request feedback upload
     *
     * @param screenshotURLs
     * @param tags
     * @param description
     * @param logURL
     * @return
     */
    public Observable<BaseResponse<FeedbackUploadResBean>> requestFeedbackUpload(Map<String, String> screenshotURLs,
                                                                                 String[] tags, String description, String logURL) {
        ArrayMap<String, Object> params = new ArrayMap<>();
        params.put("screenshotURLs", screenshotURLs);
        params.put("tags", tags);
        params.put("description", description);
        params.put("logURL", logURL);
        return apiManagerService.requestFeedbackUpload(getRequestBody1(params)).flatMap(it -> Observable.just(it));
    }

    /**
     * request real name auth
     *
     * @param realName
     * @param idCard
     * @return
     */
    public Observable<BaseResponse<Void>> requestRealNameAuth(String realName, String idCard) {
        ArrayMap<String, Object> params = new ArrayMap<>();
        params.put("realName", realName);
        params.put("idCard", idCard);
        return apiManagerService.requestRealNameAuth(getRequestBody1(params)).flatMap(it -> Observable.just(it));

    }

    private RequestBody getRequestBody(ArrayMap<String, String> params) {
        return RequestBody.create(
                MediaType.parse("application/json;charset=UTF-8"),
                GsonUtils.Companion.getGson().toJson(params)
        );
    }

    private RequestBody getRequestBody1(ArrayMap<String, Object> params) {
        return RequestBody.create(
                MediaType.parse("application/json;charset=UTF-8"),
                GsonUtils.Companion.getGson().toJson(params)
        );
    }
}
