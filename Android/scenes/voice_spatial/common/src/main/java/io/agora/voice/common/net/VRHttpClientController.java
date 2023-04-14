package io.agora.voice.common.net;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.agora.voice.common.net.model.VRHttpResponse;
import io.agora.voice.common.utils.LogTools;

class VRHttpClientController {
   private static final String TAG = VRHttpClientController.class.getSimpleName();
   private final Context mContext;
   private URL mURL;
   private HttpURLConnection mConn;
   private static int EM_DEFAULT_TIMEOUT = 60 * 1000;
   private static int EM_DEFAULT_READ_TIMEOUT = 60 * 1000;
   private static final String BOUNDARY = java.util.UUID.randomUUID().toString();
   private static final String TWO_HYPHENS = "--";
   private static final String LINE_END = "\r\n";

   public VRHttpClientController(Context mContext) {
      this.mContext = mContext;
   }

   /**
    * 设置URL
    * @param url
    * @throws IOException
    */
   public void setURL(String url) throws IOException {
      setURL(url, -1);
   }
   /**
    * 设置URL
    * @param url
    * @param port
    * @throws IOException
    */
   public void setURL(String url, int port) throws IOException {
      url = VRHttpClientConfig.processUrl(url);
      //设置http的默认端口号为80
      URL originUrl = new URL(url);
      String protocol = originUrl.getProtocol();
      int originPort = originUrl.getPort();
      //默认接口为-1，且originPort不为-1
      if(originPort != -1) {
         port = originPort;
      }
      mURL = new URL(protocol, originUrl.getHost(), port, originUrl.getFile());
      mConn = (HttpURLConnection) mURL.openConnection();
   }

   /**
    * 设置请求方法
    * @param requestMethod
    * @throws ProtocolException
    */
   public void setRequestMethod(String requestMethod) throws ProtocolException {
      mConn.setRequestMethod(requestMethod);
   }

   /**
    * 设置连接过期时间
    * @param timeout
    */
   public void setConnectTimeout(int timeout) {
      if (timeout <= 0) {
         timeout = EM_DEFAULT_TIMEOUT;
      }
      mConn.setConnectTimeout(timeout);
   }

   public void setReadTimeout(int timeout) {
      if (timeout <= 0) {
         timeout = EM_DEFAULT_READ_TIMEOUT;
      }
      mConn.setReadTimeout(timeout);
   }

   /**
    * 为请求头加上token
    */
   public void setToken() {
      mConn.setRequestProperty("Authorization", "Bearer " );
   }

   /**
    * 添加默认的请求头
    */
   public void setDefaultProperty() {
      mConn.setRequestProperty("Connection", "Keep-Alive");
   }

   /**
    * Get请求时的设置
    */
   public void setGetConnection() {
      mConn.setDoInput(true);
   }

   /**
    * Post等请求时的设置
    */
   public void setPostConnection() {
      mConn.setDoOutput(true);
      mConn.setDoInput(true);
      mConn.setUseCaches(false);
   }


   /**
    * Delete等请求时的设置(android 4.4版本以上才支持setDoOutput方法)
    */
   public void setDeleteConnection() {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
         mConn.setDoOutput(true);
         mConn.setDoInput(true);
         mConn.setUseCaches(false);
      }
   }

   public HttpURLConnection getHttpURLConnection() {
      return mConn;
   }

   /**
    * 添加请求头
    * @param headers
    */
   public void addHeader(Map<String, String> headers) {
      if (headers != null && headers.size() > 0) {
         for (Map.Entry<String, String> item : headers.entrySet()) {
            mConn.setRequestProperty(item.getKey(), item.getValue());
         }
      }
   }

   /**
    * 添加参数
    * @param params
    * @param out
    * @throws IOException
    */
   public void addParams(Map<String, String> params, OutputStream out) throws IOException {
      LogTools.d(TAG, "request Map params = "+params.toString());
      if (params == null || params.size() <= 0) {
         return;
      }
      String paramsString = getParamsString(params);
      if (TextUtils.isEmpty(paramsString)) {
         return;
      }
      out.write(paramsString.getBytes());
      out.flush();
   }

   /**
    * 添加参数
    * @param params
    * @param out
    * @throws IOException
    */
   public void addParams(String params, OutputStream out) throws IOException {
      LogTools.d(TAG, "request String params = "+params);
      if(TextUtils.isEmpty(params)) {
         return;
      }
      out.write(params.getBytes());
      out.flush();
   }


   /**
    * 连接
    * @throws IOException
    */
   public HttpURLConnection connect() throws IOException {
      printRequestInfo(true);
      mConn.connect();
      return mConn;
   }

   /**
    * 打印请求信息
    * @param showInfo
    * @throws IllegalStateException
    */
   private void printRequestInfo(boolean showInfo) throws IllegalStateException{
      if(showInfo && mConn != null) {
         LogTools.d(TAG, "request start =========================== ");
         LogTools.d(TAG, "request url = "+mConn.getURL());
         LogTools.d(TAG, "request method = "+mConn.getRequestMethod());
         LogTools.d(TAG, "request header = "+mConn.getRequestProperties().toString());
         LogTools.d(TAG, "request end =========================== ");
      }
   }

   /**
    * 获取请求相应
    * @return
    * @throws IOException
    */
   public VRHttpResponse getHttpResponse() throws IOException {
      VRHttpResponse response = new VRHttpResponse();
      response.code = mConn.getResponseCode();
      if(response.code == HttpURLConnection.HTTP_OK) {
         response.contentLength = mConn.getContentLength();
         response.inputStream = mConn.getInputStream();
         response.content = parseStream(response.inputStream);
      }else {
         response.errorStream = mConn.getErrorStream();
         response.content = parseStream(response.errorStream);
      }
      printResponseInfo(true, response);
      return response;
   }


   /**
    * 打印相应信息
    * @param showInfo
    * @param response
    */
   private void printResponseInfo(boolean showInfo, VRHttpResponse response) {
      if(mConn == null || response == null) {
         return;
      }
      if(showInfo) {//展示详细信息
         LogTools.d(TAG, "response ==========================start =================");
         LogTools.d(TAG, "content: "+response.content);
         LogTools.d(TAG, "url: "+mConn.getURL().toString());
         LogTools.d(TAG, "headers: "+mConn.getHeaderFields().toString());
         LogTools.d(TAG, "response ==========================end =================");
      }else {//只展示相应码及错误信息
         LogTools.d(TAG, "response code: "+response.code);
         if(response.code != HttpURLConnection.HTTP_OK) {
            LogTools.d(TAG, "error message: "+response.content);
         }
      }
   }

   private String getParamsString(Map<String, String> paramsMap) {
      if(paramsMap == null || paramsMap.size() <= 0) {
         return null;
      }
      StringBuffer strBuf = new StringBuffer();
      for (String key : paramsMap.keySet()){
         strBuf.append(TWO_HYPHENS);
         strBuf.append(BOUNDARY);
         strBuf.append(LINE_END);
         strBuf.append("Content-Disposition: form-data; name=\"" + key + "\"");
         strBuf.append(LINE_END);

         strBuf.append("Content-Type: " + "text/plain" );
         strBuf.append(LINE_END);
         strBuf.append("Content-Length: "+paramsMap.get(key).length());
         strBuf.append(LINE_END);
         strBuf.append(LINE_END);
         strBuf.append(paramsMap.get(key));
         strBuf.append(LINE_END);
      }
      return strBuf.toString();
   }

   /**
    * 获取Exception response
    * @param e
    * @return
    * @throws IOException
    */
   public VRHttpResponse getExceptionResponse(Exception e) throws IOException{
      VRHttpResponse response = new VRHttpResponse();
      if(mConn != null) {
         response.code = mConn.getResponseCode();
         response.contentLength = mConn.getContentLength();
         response.errorStream = mConn.getErrorStream();
         mConn.disconnect();
      }
      response.exception = e;
      return response;
   }

   /**
    * 解析流信息
    * @param is
    * @return
    */
   private String parseStream(InputStream is) {
      String buf;
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
         StringBuilder sb = new StringBuilder();
         String line = "";
         while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
         }
         buf = sb.toString();
         return buf;

      } catch (Exception e) {
         return null;
      }
   }

   private void checkAndProcessSSL(String url) {
      VRHttpClientConfig.checkAndProcessSSL(url, mConn);
   }

   public static class HttpParams {
      public final Context mContext;
      public String mRequestMethod;
      public int mPort = -1;//https默认443，http默认80
      public int mConnectTimeout;
      public int mReadTimeout;
      public boolean canRetry;//是否可以重试
      public int mRetryTimes;//重试次数

      public Map<String, String> mHeaders = new HashMap<>();
      public Map<String, String> mParams = new HashMap<>();
      public String mParamsString;
      public String mUrl;


      public HttpParams(Context mContext) {
         this.mContext = mContext;
      }

      public void apply(VRHttpClientController controller) throws IOException {
         if (mPort != -1) {
            controller.setURL(mUrl, mPort);
         } else {
            controller.setURL(mUrl);
         }
         controller.setRequestMethod(mRequestMethod);

         if ("GET".equalsIgnoreCase(mRequestMethod)) {
            controller.setGetConnection();
         } else if ("DELETE".equalsIgnoreCase(mRequestMethod)) {
            controller.setDeleteConnection();
         } else {
            controller.setPostConnection();
         }

         controller.setConnectTimeout(mConnectTimeout);
         controller.setReadTimeout(mReadTimeout);
         controller.setDefaultProperty();
         controller.checkAndProcessSSL(mUrl);
         checkToken();
         controller.addHeader(mHeaders);
      }

      public VRHttpResponse getResponse(VRHttpClientController controller) throws IOException {
         return controller.getHttpResponse();
      }

      public VRHttpResponse getExceptionResponse(VRHttpClientController controller, IOException e) throws IOException {
         if (controller != null) {
            return controller.getExceptionResponse(e);
         }
         return null;
      }

      public void checkToken() {
         if (mHeaders.keySet().contains("Authorization")) {
            if (TextUtils.isEmpty(mHeaders.get("Authorization"))) {

            }
         }
      }
   }
}
