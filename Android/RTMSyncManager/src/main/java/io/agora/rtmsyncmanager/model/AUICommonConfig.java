package io.agora.rtmsyncmanager.model;

import android.content.Context;

import androidx.annotation.NonNull;

public class AUICommonConfig {
    public @NonNull Context context;
    // 声网AppId
    public @NonNull String appId = "";
    // 声网App证书(可选，如果没有用到后端token生成服务可以不设置)
    public @NonNull String appCert = "";
    // 域名(可选，如果没有用到后端服务可以不设置)
    public @NonNull String host = "";
    // 用户信息
    public @NonNull AUIUserThumbnailInfo owner;

    public @NonNull String imAppKey = "";
    public @NonNull String imClientId = "";
    public @NonNull String imClientSecret = "";
}
