package io.agora.voice.network.tools.bean;

import java.io.Serializable;
import java.util.List;

public class VRoomUserBean implements Serializable {

   /**
    * total : string
    * cursor : string
    * users : [{"uid":"string","chat_uid":"string","name":"string","portrait":"string"}]
    */

   private int total;
   private String cursor;
   private List<VMemberBean> members;

   public int getTotal() {
      return total;
   }

   public void setTotal(int total) {
      this.total = total;
   }

   public String getCursor() {
      return cursor;
   }

   public void setCursor(String cursor) {
      this.cursor = cursor;
   }

   public List<VMemberBean> getMembers() {
      return members;
   }

   public void setMembers(List<VMemberBean> members) {
      this.members = members;
   }
}
