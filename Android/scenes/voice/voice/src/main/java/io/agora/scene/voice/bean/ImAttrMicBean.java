package io.agora.scene.voice.bean;

import java.io.Serializable;

import io.agora.voice.network.tools.bean.VMemberBean;

/**
 * @author create by zhangwei03
 *
 * IM kv 属性
 */
public class ImAttrMicBean implements Serializable {

   /**
    * status : 0
    * member : {"uid":"string","chat_uid":"string","name":"string","portrait":"string","rtc_uid":0,"mic_index":0}
    */

   private int status;
   private VMemberBean member;

   public int getStatus() {
      return status;
   }

   public void setStatus(int status) {
      this.status = status;
   }

   public VMemberBean getMember() {
      return member;
   }

   public void setMember(VMemberBean member) {
      this.member = member;
   }
}
