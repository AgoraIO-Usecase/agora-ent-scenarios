package io.agora.voice.network.http;

public interface VRHttpCallback {
 /**
  * 成功时的回调
  *
  * @param result
  */
 void onSuccess(String result);

 /**
  * 失败时的回调
  *
  * @param code
  * @param msg
  */
 void onError(int code,String msg);

}
