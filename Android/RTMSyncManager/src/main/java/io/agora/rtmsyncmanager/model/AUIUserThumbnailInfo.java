package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class AUIUserThumbnailInfo implements Serializable {

    public @NonNull String userId = "";      //用户Id
    public @NonNull String userName = "";    //用户名
    public @NonNull String userAvatar = "";  //用户头像

    @Override
    public String toString() {
        return "AUIUserThumbnailInfo{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                '}';
    }
}
