package io.agora.rtmsyncmanager.model;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * AUICommonConfig is a class that holds configuration details for the application.
 * This includes the application context, application ID, application certificate, host, user information, and IM details.
 */
public class AUICommonConfig {
    // The application context
    public @NonNull Context context;
    // The application ID from Agora
    public @NonNull String appId = "";
    // The application certificate from Agora (optional, if not using backend token generation service, this can be unset)
    public @NonNull String appCert = "";
    // The host (optional, if not using backend service, this can be unset)
    public @NonNull String host = "";
    // The user information
    public @NonNull AUIUserThumbnailInfo owner;

    // The IM application key
    public @NonNull String imAppKey = "";
    // The IM client ID
    public @NonNull String imClientId = "";
    // The IM client secret
    public @NonNull String imClientSecret = "";
}