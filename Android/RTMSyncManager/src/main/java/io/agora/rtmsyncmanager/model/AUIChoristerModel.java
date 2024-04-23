package io.agora.rtmsyncmanager.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class AUIChoristerModel {
    @SerializedName("userId")
    public @NonNull String userId = "";

    @SerializedName("chorusSongNo")
    public @NonNull String chorusSongNo = "";    //合唱者演唱歌曲

}
