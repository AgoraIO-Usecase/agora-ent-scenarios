package io.agora.scene.base;

public class Constant {
    public static final String CURRENT_USER = "current_user";
    public static final String URL = "url";
    public static final String PARAMS_WITH_BROWSER = "params_with_browser";

    public static final String IS_AGREE = "is_agree";

    public static final String KEY_CODE = "key_code";
    public static final int PARAMS_EXIT = 100;

    /* User info changed */
    public static final int CALLBACK_TYPE_USER_INFO_CHANGE = 900;
    /* Account deactivation */
    public static final int CALLBACK_TYPE_USER_LOGOFF = 901;
    /* User logout */
    public static final int CALLBACK_TYPE_USER_LOGOUT = 902;
    /* User info fetch success */
    public static final int CALLBACK_TYPE_REQUEST_USER_INFO = 903;


    /* Login success */
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_SUCCESS = 403;
    /* Login failed */
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_FAIL = 404;

    /* Verification code sent successfully */
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_CODE_SUCCESS = 405;
    /* Verification code sending failed */
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_CODE_FAIL = 406;

    /* Illegal content uploaded */
    public static final int CALLBACK_TYPE_UPLOAD_ILLEGAL_CONTENT = 90002;

    // Super resolution setting flag
    public static final String IS_SET_SETTING = "isSetSetting";

    // Current audience device level (High, Medium, Low)
    public static final String CURR_AUDIENCE_DEVICE_LEVEL = "currAudienceDeviceLevel";

    // Current broadcaster network quality (Good, Normal)
    public static final String CURR_BROADCAST_NETWORK_LEVEL = "currBroadcastNetworkLevel";

    // Audience playback settings
    public static final String CURR_AUDIENCE_PLAY_SETTING = "currAudiencePlaySetting";

    // Super resolution switch
    public static final String CURR_AUDIENCE_ENHANCE_SWITCH ="currAudienceEnhanceSwitch";

    // Broadcast settings
    public static final String CURR_BROADCAST_SETTING = "currBroadcastSetting";

    // Low stream settings
    public static final String CURR_LOW_STREAM_SETTING = "currLowStreamSetting";

    // Audience settings
    public static final String CURR_AUDIENCE_SETTING = "currAudienceSetting";
}
