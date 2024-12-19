package io.agora.scene.base.api;

import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moczul.ok2curl.CurlInterceptor;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class ApiManager {
    private Gson mGson;
    private final static long TIMEOUT = 5;
    private OkHttpClient httpClient;
    private ApiManagerService apiManagerService;
    public static String token;

    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            }
    };

    private ApiManager() throws NoSuchAlgorithmException, KeyManagementException {
        if (mGson == null) {
            mGson = new GsonBuilder().serializeNulls()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(String.class, new GsonUtils.StringConverter()).create();
        }
        // 设定 SSL 上下文来忽略证书验证
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().addInterceptor(chain -> {
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
                })
                .addInterceptor(new HttpLoggingInterceptor())
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true);
        if (BuildConfig.DEBUG) {
            httpClientBuilder.addInterceptor(new CurlInterceptor(s -> {
                Log.d("CurlInterceptor", s);
            }));
        }
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

    public Observable<BaseResponse<String>> requestReportDevice(String userNo, String sceneId) {
        ArrayMap<String, String> params = new ArrayMap<>();
        //todo 这几个参数query?
//        params.put("userNo", userNo);
//        params.put("sceneId", sceneId);
//        params.put("appId", BuildConfig.AGORA_APP_ID);
//        params.put("projectId", "agora_ent_demo");
        params.put("appVersion", BuildConfig.APP_VERSION_NAME);
        params.put("platform", "Android");
        params.put("model", Build.MODEL);
        params.put("manufacture", Build.MANUFACTURER);
        params.put("osVersion", Build.VERSION.RELEASE);
        return apiManagerService.requestReportDevice(userNo, sceneId, BuildConfig.AGORA_APP_ID, "agora_ent_demo",
                getRequestBody(params)).flatMap(it -> Observable.just(it));

    }

    public Observable<BaseResponse<String>> requestReportAction(String userNo, String action) {
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("action", action);
        return apiManagerService.requestReportAction(userNo, action, BuildConfig.AGORA_APP_ID, "agora_ent_demo",
                getRequestBody(params)).flatMap(it -> Observable.just(it));

    }

    public Observable<BaseResponse<CommonBean>> requestUploadLog(File file) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part partFile = MultipartBody.Part.createFormData("file", file.getName(), fileBody);
        return apiManagerService.requestUploadLog(partFile).flatMap(it -> Observable.just(it));
    }

    public Observable<BaseResponse<FeedbackUploadResBean>> requestFeedbackUpload(Map<String,String> screenshotURLs,
                                                                                 String[] tags, String description, String logURL) {
        ArrayMap<String, Object> params = new ArrayMap<>();
        params.put("screenshotURLs", screenshotURLs);
        params.put("tags", tags);
        params.put("description", description);
        params.put("logURL", logURL);
        return apiManagerService.requestFeedbackUpload(getRequestBody1(params)).flatMap(it -> Observable.just(it));

    }

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
