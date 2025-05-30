package io.agora.scene.voice.netkit.callback;

public interface VRHttpCallback {
 /**
  * Success callback
  *
  * @param result
  */
 default void onSuccess(String result){}

 /**
  * Callback on failure
  *
  * @param code
  * @param msg
  */
 default void onError(int code,String msg){}

}
