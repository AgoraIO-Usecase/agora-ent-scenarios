package io.agora.scene.base;

public class Constant {
    public static final String CURRENT_USER = "current_user";
    public static final String URL = "url";
    public static final String PARAMS_WITH_BROWSER = "params_with_browser";

    public static final String IS_AGREE = "is_agree";

    /* 用户信息改变 */
    public static final int CALLBACK_TYPE_USER_INFO_CHANGE = 900;
    /* 注销 */
    public static final int CALLBACK_TYPE_USER_LOGOFF = 901;
    /* 退出登录*/
    public static final int CALLBACK_TYPE_USER_LOGOUT = 902;
    /* 获取用户信息成功*/
    public static final int CALLBACK_TYPE_REQUEST_USER_INFO = 903;


    /* 登录成功*/
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_SUCCESS = 403;
    /* 登录失败*/
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_FAIL = 404;

    /* 验证码发送成功*/
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_CODE_SUCCESS = 405;
    /* 验证码发送失败*/
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_CODE_FAIL = 406;

    /* 上传了非法内容*/
    public static final int CALLBACK_TYPE_UPLOAD_ILLEGAL_CONTENT = 90002;

    // 是否设置了超分
    public static final String IS_SET_SETTING = "isSetSetting";

    // 当前观众设备等级（高、中、低）
    public static final String CURR_AUDIENCE_DEVICE_LEVEL = "currAudienceDeviceLevel";

    // 当前主播设置网络登记（好、普通）
    public static final String CURR_BROADCAST_NETWORK_LEVEL = "currBroadcastNetworkLevel";

    // 观众看播设置
    public static final String CURR_AUDIENCE_PLAY_SETTING = "currAudiencePlaySetting";

    // 超分开关
    public static final String CURR_AUDIENCE_ENHANCE_SWITCH ="currAudienceEnhanceSwitch";

    // xxx
    public static final String CURR_BROADCAST_SETTING = "currBroadcastSetting";

    // xxx
    public static final String CURR_LOW_STREAM_SETTING = "currLowStreamSetting";

    // xxx
    public static final String CURR_AUDIENCE_SETTING = "currAudienceSetting";
}
