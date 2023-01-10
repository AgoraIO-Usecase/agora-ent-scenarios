package io.agora.voice.common.net;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VRHttpClientConfig {
    private static final String TAG = VRHttpClientConfig.class.getSimpleName();
    public static String EM_TIME_OUT_KEY = "em_timeout";
    public static int EM_DEFAULT_TIMEOUT = 60*1000;

    public static String processUrl(String remoteUrl){
        if (remoteUrl.contains("+")) {
            remoteUrl = remoteUrl.replaceAll("\\+", "%2B");
        }

        if (remoteUrl.contains("#")) {
            remoteUrl = remoteUrl.replaceAll("#", "%23");
        }

        return remoteUrl;
    }

    public static int getTimeout(Map<String,String> headers){
        int timeout = VRHttpClientConfig.EM_DEFAULT_TIMEOUT;

        if(headers != null && headers.get(VRHttpClientConfig.EM_TIME_OUT_KEY) != null){
            timeout = Integer.valueOf(headers.get(VRHttpClientConfig.EM_TIME_OUT_KEY));
            headers.remove(VRHttpClientConfig.EM_TIME_OUT_KEY);
        }

        return timeout;
    }

    private static TrustManager trustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    static void checkAndProcessSSL(String url, HttpURLConnection conn) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//            conn.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManager);
        } catch ( NoSuchAlgorithmException | KeyManagementException |IllegalStateException e) {
            e.printStackTrace();
        }

    }




}
