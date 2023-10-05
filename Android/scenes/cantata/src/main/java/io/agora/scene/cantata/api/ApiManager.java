package io.agora.scene.cantata.api;

import android.util.Log;

import androidx.annotation.NonNull;
import com.moczul.ok2curl.CurlInterceptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import io.agora.media.RtcTokenBuilder;
import io.agora.scene.base.BuildConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiManager {

    private ApiManager() {
    }

    public static ApiManager getInstance() {
        return InstanceHolder.apiManager;
    }

    static class InstanceHolder {
        private static ApiManager apiManager = new ApiManager();
    }

    private final String domain = "http://218.205.37.50:16000";
    private final String testIp = "218.205.37.50";

    private final String TAG = "ApiManager";

    private String tokenName = "";
    private String taskId = "";

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(new CurlInterceptor(s -> Log.d(TAG, s)))
            .build();

    public String fetchCloudToken() {
        String token = "";
        try {
            JSONObject acquireOjb = new JSONObject();
            acquireOjb.put("instanceId", System.currentTimeMillis() + "");
            acquireOjb.put("testIp", testIp);
            Request request = new Request.Builder()
                    .url(getTokenUrl(domain, BuildConfig.AGORA_APP_ID))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", getBasicAuth())
                    .post(RequestBody.create(MediaType.parse("application/json"), acquireOjb.toString()))
                    .build();

            Response responseToken = okHttpClient.newCall(request).execute();
            Log.d(TAG, responseToken.toString());
            if (responseToken.isSuccessful()) {
                ResponseBody body = responseToken.body();
                assert body != null;
                String bodyString = body.string();
                Log.d(TAG, bodyString);
                JSONObject jsonToken = new JSONObject(bodyString);
                if (jsonToken.has("tokenName")) {
                    token = jsonToken.getString("tokenName");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getToken error " + e.getMessage());
        }
        return token;
    }

    public void fetchStartCloud(String mainChannel, int cloudRtcUid) {
        String token = fetchCloudToken();
        if (token.isEmpty()) {
            Log.e(TAG, "云端合流uid 请求报错 token is null");
            return;
        } else {
            tokenName = token;
        }
        String taskId = "";
        try {
            JSONObject transcoderObj = new JSONObject();
            JSONObject inputRetObj = new JSONObject()
                    .put("rtcUid", 0)
                    .put("rtcToken", BuildConfig.AGORA_APP_ID)
                    .put("rtcChannel", mainChannel);
            JSONObject intObj = new JSONObject()
                    .put("rtc", inputRetObj);
            transcoderObj.put("audioInputs", new JSONArray().put(intObj));

            transcoderObj.put("idleTimeout", 30);

            JSONObject audioOptionObj = new JSONObject()
                    .put("profileType", "AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO")
                    .put("fullChannelMixer", "native-mixer-weighted");
            JSONObject outputRetObj = new JSONObject()
                    .put("rtcUid", cloudRtcUid)
                    .put("rtcToken", BuildConfig.AGORA_APP_ID)
                    .put("rtcChannel", mainChannel + "_ad");
            JSONObject dataStreamObj = new JSONObject()
                    .put("source", new JSONObject().put("dataStream", true))
                    .put("sink", new JSONObject());
            JSONObject outputsObj = new JSONObject()
                    .put("audioOption", audioOptionObj)
                    .put("rtc", outputRetObj)
                    .put("dataStreamOption", dataStreamObj);

            transcoderObj.put("outputs", new JSONArray().put(outputsObj));

            JSONObject postBody = new JSONObject()
                    .put("services", new JSONObject()
                            .put("cloudTranscoder", new JSONObject()
                                    .put("serviceType", "cloudTranscoderV2")
                                    .put("config", new JSONObject()
                                            .put("transcoder", transcoderObj))));

            Request request = new Request.Builder()
                    .url(startTaskUrl(domain, BuildConfig.AGORA_APP_ID, tokenName))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", getBasicAuth())
                    .post(RequestBody.create(MediaType.parse("application/json"), postBody.toString()))
                    .build();

            Response responseStart = okHttpClient.newCall(request).execute();
            Log.d(TAG, responseStart.toString());
            if (responseStart.isSuccessful()) {
                ResponseBody body = responseStart.body();
                assert body != null;
                String bodyString = body.string();
                Log.d(TAG, bodyString);
                JSONObject jsonUid = new JSONObject(bodyString);
                if (jsonUid.has("taskId")) {
                    taskId = jsonUid.getString("taskId");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "云端合流uid 请求报错 " + e.getMessage());
        }
        if (!taskId.isEmpty()) {
            this.taskId = taskId;
        }
    }

    public void fetchStopCloud() {
        if (taskId.isEmpty() || tokenName.isEmpty()) {
            Log.e(TAG, "云端合流任务停止失败 taskId || tokenName is null");
            return;
        }
        try {
            Request request = new Request.Builder()
                    .url(deleteTaskUrl(domain, BuildConfig.AGORA_APP_ID, taskId, tokenName))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", getBasicAuth())
                    .delete()
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            Log.d(TAG, response.toString());
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                assert body != null;
                String bodyString = body.string();
                Log.d(TAG, bodyString);
            }
        } catch (Exception e) {
            Log.e(TAG, "云端合流任务停止失败 " + e.getMessage());
        }
    }


    private String getTokenUrl(String domain, String appId) {
        return String.format("%s/v1/projects/%s/rtsc/cloud-transcoder/builderTokens", domain, appId);
    }

    private String startTaskUrl(String domain, String appId, String tokenName) {
        return String.format("%s/v1/projects/%s/rtsc/cloud-transcoder/tasks?builderToken=%s", domain, appId, tokenName);
    }

    private String deleteTaskUrl(String domain, String appid, String taskid, String tokenName) {
        return String.format("%s/v1/projects/%s/rtsc/cloud-transcoder/tasks/%s?builderToken=%s", domain, appid, taskid, tokenName);
    }

//    private String getRtcToken(String channelId, int uid) {
//        String rtcToken = "";
//        try {
//            rtcToken = new RtcTokenBuilder().buildTokenWithUid(
//                    BuildConfig.RTC_APP_ID, BuildConfig.RTC_APP_CERT, channelId, uid,
//                    RtcTokenBuilder.Role.Role_Publisher, 0
//            );
//        } catch (Exception e) {
//            Log.e("getRtcToken", "rtc token build error " + e.getMessage());
//        }
//        return rtcToken;
//    }

    private String getBasicAuth() {
        // 拼接客户 ID 和客户密钥并使用 base64 编码
        String plainCredentials = BuildConfig.AGORA_APP_ID + ":" + BuildConfig.AGORA_APP_CERTIFICATE;
        String base64Credentials = null;
        base64Credentials = new String(Base64.getEncoder().encode(plainCredentials.getBytes()));
        // 创建 authorization header
        return "Basic " + base64Credentials;
    }
}

