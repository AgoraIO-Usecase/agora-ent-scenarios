//package io.agora.voice.network.http;
//
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import java.io.IOException;
//import java.security.KeyManagementException;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.TimeUnit;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//
//import io.agora.voice.network.http.listener.EventListenerFactory;
//import io.agora.voice.network.http.listener.NetEventModel;
//import io.agora.voice.buddy.tool.GsonTools;
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.EventListener;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class VRHttpServer {
//
//    private static final String TAG = "HttpServer";
//
//    private VRHttpServer() {
//    }
//
//    public static VRHttpServer get() {
//        return Holder.INSTANCE;
//    }
//
//    private static class Holder {
//        private static final VRHttpServer INSTANCE = new VRHttpServer();
//    }
//
//    private OkHttpClient okHttpClient;
//
//    private final Handler handler = new Handler(Looper.getMainLooper());
//
//    private OkHttpClient client() {
//        if (null != okHttpClient) {
//            return okHttpClient;
//        }
//        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
//                .readTimeout(30, TimeUnit.SECONDS)
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .eventListenerFactory(new EventListenerFactory());
//        try {
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
//            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//            okHttpClientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManager);
//        } catch ( NoSuchAlgorithmException|KeyManagementException|IllegalStateException e) {
//            e.printStackTrace();
//        }
//
//        okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
//            @Override
//            public boolean verify(String hostname, SSLSession session) {
//                return true;
//            }
//        });
//        okHttpClient = okHttpClientBuilder.build();
//        return okHttpClient;
//    }
//
//    private void runOnUiThread(Runnable runnable) {
//        handler.post(runnable);
//    }
//
//    public <T> void enqueueGet(String url, Map<String, Object> headers, Class<T> responseClazz, IHttpCallback<T> httpCallback) {
//        Request.Builder builder = new Request.Builder();
//        if (headers != null && !headers.isEmpty()) {
//            for (Map.Entry<String, Object> entry : headers.entrySet()) {
//                builder.addHeader(entry.getKey(), entry.getValue().toString());
//            }
//        }
//        Request request = builder.url(url).get().build();
//        enqueue(client().newCall(request), responseClazz, httpCallback);
//    }
//
//    public <T> void enqueuePost(String url, Map<String, String> headers, Map<String, Object> params, Class<T> responseClazz, IHttpCallback<T> httpCallback) {
//        RequestBody body = RequestBody.create(GsonTools.beanToString(params), MediaType.get("application/json; charset=utf-8"));
//        Request.Builder builder = new Request.Builder();
//        if (headers != null && !headers.isEmpty()) {
//            for (Map.Entry<String, String> entry : headers.entrySet()) {
//                builder.addHeader(entry.getKey(), entry.getValue().toString());
//            }
//        }
//        builder.tag(NetEventModel.class, new NetEventModel());
//        Request request = builder.url(url).post(body).build();
//        enqueue(client().newCall(request), responseClazz, httpCallback);
//    }
//
//    private <T> void enqueue(Call call, Class<T> responseClazz, IHttpCallback<T> httpCallback) {
//        Log.e("request enqueue: 1111",TAG);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.e(TAG,e.getMessage());
//                runOnUiThread(() -> {
//                    httpCallback.onFail(-1, e.getMessage());
//                });
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) {
//                try {
//                    String dataJson = Objects.requireNonNull(response.body()).string();
//                    Log.e(TAG,dataJson);
//                    T t = GsonTools.toBean(dataJson, responseClazz);
//                    runOnUiThread(() -> {
//                        httpCallback.onSuccess(dataJson, t);
//                    });
//                } catch (Exception e) {
//                    Log.e(TAG,e.getMessage());
//                    runOnUiThread(() -> {
//                        httpCallback.onFail(-1, e.getMessage());
//                    });
//                }
//            }
//        });
//    }
//
//    private TrustManager trustManager = new X509TrustManager() {
//        @Override
//        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//
//        }
//
//        @Override
//        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//
//        }
//
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//            return new X509Certificate[0];
//        }
//    };
//
//
//    public interface IHttpCallback<T> {
//
//        void onSuccess(String bodyString, T data);
//
//        void onFail(int code, String message);
//    }
//}
