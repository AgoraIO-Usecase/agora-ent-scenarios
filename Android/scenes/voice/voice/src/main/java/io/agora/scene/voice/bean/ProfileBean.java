package io.agora.scene.voice.bean;

public class ProfileBean {
   private boolean isChecked;
   private int avatarResource;
   private String avatarName;

   public boolean isChecked() {
      return isChecked;
   }

   public void setChecked(boolean check) {
      isChecked = check;
   }

   public int getAvatarResource() {
      return avatarResource;
   }

   public void setAvatarResource(int avatarResource) {
      this.avatarResource = avatarResource;
   }

   public String getAvatarName() {
      return avatarName;
   }

   public void setAvatarName(String avatarName) {
      this.avatarName = avatarName;
   }
}
