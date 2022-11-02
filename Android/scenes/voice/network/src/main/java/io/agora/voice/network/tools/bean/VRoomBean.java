package io.agora.voice.network.tools.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import io.agora.voice.buddy.config.ConfigConstants;

public class VRoomBean implements Serializable {
   /**
    * total : 14
    * cursor : NQ==
    * rooms : [{"name":"createroom787","owner":{"uid":"rOVLNPgy4JFEZrC3L9LWjQO=","name":"test1","portrait":"https://beidou-ktv-user.oss-cn-beijing.aliyuncs.com/1658044795022NaEWFm.png"},"type":0,"room_id":"aQCOA_Ay8S8B1WNHbjSewg==","channel_id":"673965dea3fd4a0085ee89fc562f6bf3","chatroom_id":"192760762138625","is_private":true,"created_at":1663200589000,"member_count":2},{"name":"createroom953","owner":{"uid":"rOVLNPgy4JFEZrC3L9LWjQO=","name":"test1","portrait":"https://beidou-ktv-user.oss-cn-beijing.aliyuncs.com/1658044795022NaEWFm.png"},"type":0,"room_id":"G3ew9PWGD7XDZsTMSiTKFw==","channel_id":"68774db1178b4157a9c06c3c71b35e82","chatroom_id":"192753402183681","is_private":false,"created_at":1663193570000,"member_count":2},{"name":"testRoom8","owner":{"uid":"LBRAtHE37B3I5mtHMUfGwA==","name":"test2","portrait":"avatar2"},"type":0,"room_id":"D2kYcCKssLoZP3Z2iu6OvQ==","channel_id":"d8ce543f119f440899f06a1ea1bb710f","chatroom_id":"192744408547329","is_private":false,"created_at":1663184993000,"member_count":0},{"name":"testRoom7","owner":{"uid":"LBRAtHE37B3I5mtHMUfGwA==","name":"test2","portrait":"avatar2"},"type":0,"room_id":"9BUSWPYsMWTaNWXv0kDFwA==","channel_id":"42a9e9668bcf467cb960c2036e975814","chatroom_id":"192679627522049","is_private":true,"created_at":1663123213000,"member_count":12},{"name":"testRoom6","owner":{"uid":"LBRAtHE37B3I5mtHMUfGwA==","name":"test2","portrait":"avatar2"},"type":0,"room_id":"iJ2G0gY7rIPTMXwcA_HZFA==","channel_id":"24d4c62e8ad643e7999e6b0d59c03fc8","chatroom_id":"192679614939137","is_private":true,"created_at":1663123202000,"member_count":0},{"name":"testRoom5","owner":{"uid":"LBRAtHE37B3I5mtHMUfGwA==","name":"test2","portrait":"avatar2"},"type":0,"room_id":"TwKVbYCQbp_GBq9vj13zMA==","channel_id":"72149fca5e6b48f08df71e69719f1329","chatroom_id":"192678635569153","is_private":true,"created_at":1663122268000,"member_count":0},{"name":"testRoom4","owner":{"uid":"LBRAtHE37B3I5mtHMUfGwA==","name":"test2","portrait":"avatar2"},"type":0,"room_id":"quLNe5LUxzKZr1ZKVPWNcg==","channel_id":"41bfce25c6e2463393580b4b5bdcd656","chatroom_id":"192678581043201","is_private":true,"created_at":1663122215000,"member_count":0},{"name":"testRoom3","owner":{"uid":"LBRAtHE37B3I5mtHMUfGwA==","name":"test2","portrait":"avatar2"},"type":0,"room_id":"z9OOnOBHLN_Ps7bDZHJtQF==","channel_id":"03b8c58fce1e4169a2c160a7367e195b","chatroom_id":"192677721210881","is_private":true,"created_at":1663121395000,"member_count":0},{"name":"testRoom2","owner":{"uid":"LBRAtHE37B3I5mtHMUfGwA==","name":"test2","portrait":"avatar2"},"type":0,"room_id":"H_L3PItek7hyM3jHgZuaAL==","channel_id":"40f9d687e39c4dd9beff767b10706df2","chatroom_id":"192677285003265","is_private":true,"created_at":1663120979000,"member_count":1},{"name":"testRoom3","owner":{"uid":"LBRAtHE37B3I5mtHMUfGwA==","name":"test2","portrait":"avatar2"},"type":0,"room_id":"YAWZXSYLNqOzIz8mswhICAY=","channel_id":"3ae1607ceffc4fc4ab85497c29ef2da2","chatroom_id":"192674919415809","is_private":true,"created_at":1663118723000,"member_count":0}]
    */

   private int total;
   private String cursor;
   private List<RoomsBean> rooms;

   public String getCursor() {
      return cursor;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof VRoomBean)) return false;
      VRoomBean vRoomBean = (VRoomBean) o;
      return Objects.equals(getRooms(), vRoomBean.getRooms());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getRooms());
   }

   public void setCursor(String cursor) {
      this.cursor = cursor;
   }

   public List<RoomsBean> getRooms() {
      return rooms;
   }

   public void setRooms(List<RoomsBean> rooms) {
      this.rooms = rooms;
   }

   public int getTotal() {
      return total;
   }

   public void setTotal(int total) {
      this.total = total;
   }

   public static class RoomsBean implements Serializable {
      /**
       * name : createroom787
       * owner : {"uid":"rOVLNPgy4JFEZrC3L9LWjQO=","name":"test1","portrait":"https://beidou-ktv-user.oss-cn-beijing.aliyuncs.com/1658044795022NaEWFm.png"}
       * type : 0
       * room_id : aQCOA_Ay8S8B1WNHbjSewg==
       * channel_id : 673965dea3fd4a0085ee89fc562f6bf3
       * chatroom_id : 192760762138625
       * is_private : true
       * created_at : 1663200589000
       * member_count : 2
       */

      private String name;
      private RoomsBean.OwnerBean owner;
      private int type;
      private String room_id;
      private String channel_id;
      private String chatroom_id;
      private boolean is_private;
      private long created_at;
      private int member_count;
      private boolean use_robot;
      private String sound_effect;
      private int robot_volume;

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof RoomsBean)) return false;
         RoomsBean bean = (RoomsBean) o;
         return Objects.equals(getChatroom_id(), bean.getChatroom_id());
      }

      @Override
      public int hashCode() {
         return Objects.hash(getChatroom_id());
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public RoomsBean.OwnerBean getOwner() {
         return owner;
      }

      public void setOwner(RoomsBean.OwnerBean owner) {
         this.owner = owner;
      }

      public int getType() {
         return type;
      }

      public void setType(int type) {
         this.type = type;
      }

      public String getRoom_id() {
         return room_id;
      }

      public void setRoom_id(String room_id) {
         this.room_id = room_id;
      }

      public String getChannel_id() {
         return channel_id;
      }

      public void setChannel_id(String channel_id) {
         this.channel_id = channel_id;
      }

      public String getChatroom_id() {
         return chatroom_id;
      }

      public void setChatroom_id(String chatroom_id) {
         this.chatroom_id = chatroom_id;
      }

      public boolean isIs_private() {
         return is_private;
      }

      public void setIs_private(boolean is_private) {
         this.is_private = is_private;
      }

      public long getCreated_at() {
         return created_at;
      }

      public void setCreated_at(long created_at) {
         this.created_at = created_at;
      }

      public int getMember_count() {
         return member_count;
      }

      public void setMember_count(int member_count) {
         this.member_count = member_count;
      }

      public boolean isUse_robot() {
         return use_robot;
      }

      public void setUse_robot(boolean use_robot) {
         this.use_robot = use_robot;
      }

       public String getSound_effect() {
           return sound_effect;
       }

       public void setSound_effect(String sound_effect) {
           this.sound_effect = sound_effect;
       }

      public int getRobot_volume() {
         return robot_volume;
      }

      public void setRobot_volume(int robot_volume) {
         this.robot_volume = robot_volume;
      }

      public static class OwnerBean implements Serializable {
         /**
          * uid : rOVLNPgy4JFEZrC3L9LWjQO=
          * name : test1
          * portrait : https://beidou-ktv-user.oss-cn-beijing.aliyuncs.com/1658044795022NaEWFm.png
          */

         private String uid;
         private String name;
         private String portrait;

         public String getUid() {
            return uid;
         }

         public void setUid(String uid) {
            this.uid = uid;
         }

         public String getName() {
            return name;
         }

         public void setName(String name) {
            this.name = name;
         }

         public String getPortrait() {
            return portrait;
         }

         public void setPortrait(String portrait) {
            this.portrait = portrait;
         }
      }

      public String getOwnerUid() {
         if (owner == null) return null;
         return owner.getUid();
      }

      public int getSoundSelection() {
         if (sound_effect.equals(ConfigConstants.SoundSelectionText.Social_Chat)) {
            return ConfigConstants.SoundSelection.Social_Chat;
         } else if (sound_effect.equals(ConfigConstants.SoundSelectionText.Karaoke)) {
            return ConfigConstants.SoundSelection.Karaoke;
         } else if (sound_effect.equals(ConfigConstants.SoundSelectionText.Gaming_Buddy)) {
            return ConfigConstants.SoundSelection.Gaming_Buddy;
         } else if (sound_effect.equals(ConfigConstants.SoundSelectionText.Professional_Broadcaster)) {
            return ConfigConstants.SoundSelection.Professional_Broadcaster;
         } else {
            return ConfigConstants.SoundSelection.Social_Chat;
         }
      }
   }

}
