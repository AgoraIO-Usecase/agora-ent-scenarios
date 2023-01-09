package io.agora.voice.common.net.callback;

public interface VRHttpCallback {
 /**
  * 成功时的回调
  *
  * @param result
  */
 default void onSuccess(String result){}

 /**
  * 失败时的回调
  *
  * @param code
  * @param msg
  */
 default void onError(int code,String msg){}

}
