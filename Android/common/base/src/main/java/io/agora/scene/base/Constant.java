package io.agora.scene.base;

public class Constant {
    public static final String CURRENT_USER = "current_user";
    public static final String URL = "url";

    public static final String IS_AGREE = "is_agree";

    /* 用户信息改变 */
    public static final int CALLBACK_TYPE_USER_INFO_CHANGE = 900;
    /* 注销 */
    public static final int CALLBACK_TYPE_USER_CANCEL_ACCOUNTS = 901;
    /* 退出登录*/
    public static final int CALLBACK_TYPE_USER_LOGOUT = 902;


    /* 登录成功*/
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_SUCCESS = 403;
    /* 登录成功*/
    public static final int CALLBACK_TYPE_LOGIN_REQUEST_LOGIN_FAIL = 404;
}
