package io.agora.scene.base.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    /**
     * 检查手机号 正则表达式
     */
    public static boolean checkPhoneNum(String phoneNum) {
        if (TextUtils.isEmpty(phoneNum)) {
            return false;
        }
        String regExp =
                "^((13[0-9])|(14[5,7,9])|(15[0-3,5-9])|(166)|(17[0-9])|(18[0-9])|(19[8,9]))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(phoneNum);
        return m.matches();
    }

}
