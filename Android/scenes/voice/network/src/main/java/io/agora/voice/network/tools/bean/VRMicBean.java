package io.agora.voice.network.tools.bean;

import java.io.Serializable;

public class VRMicBean implements Serializable {

   /**
    * mic_index : 0
    * status : 0
    * member : {"uid":"string","chat_uid":"string","name":"string","portrait":"string","rtc_uid":0,"mic_index":0}
    */

   private int mic_index;
   private int status;
   private VMemberBean member;

   public int getMic_index() {
      return mic_index;
   }

   public void setMic_index(int mic_index) {
      this.mic_index = mic_index;
   }

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
