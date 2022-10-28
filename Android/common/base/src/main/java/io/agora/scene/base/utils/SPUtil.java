package io.agora.scene.base.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import io.agora.scene.base.component.AgoraApplication;

/**
 * SharedPreferences 工具类
 * Created by Zuo Chuanqiang on 2016/4/29 029.
 */
public class SPUtil {
    private final static String PREFERENCES_NAME="PREF_HEALTHAI_RECORD_SDK";
    //服务器当前时间
    public static String CURRENTTIME = "CUR_TIME";
    private static SharedPreferences mInstance;

    private SPUtil() {
    }


    /**
     * 获取SharedPreferences实例对象
     *
     * @return
     */
    private static SharedPreferences getSharedPreference() {
        String name = PREFERENCES_NAME;
        if (mInstance == null) {
            synchronized (SPUtil.class) {
                if (mInstance == null) {
                    mInstance = AgoraApplication.the().getSharedPreferences(name, Context.MODE_PRIVATE);
                }
            }
        }
        return mInstance;
    }

    /**
     * 保存一个Boolean类型的值！
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean putBoolean(String key, Boolean value) {
        SharedPreferences sharedPreference = getSharedPreference();
        Editor editor = sharedPreference.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * 保存一个int类型的值！
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean putInt(String key, int value) {
        SharedPreferences sharedPreference = getSharedPreference();
        Editor editor = sharedPreference.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * 保存一个float类型的值！
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean putFloat(String key, float value) {
        SharedPreferences sharedPreference = getSharedPreference();
        Editor editor = sharedPreference.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    /**
     * 保存一个long类型的值！
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean putLong(String key, long value) {
        SharedPreferences sharedPreference = getSharedPreference();
        Editor editor = sharedPreference.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * 保存一个String类型的值！
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean putString(String key, String value) {
        SharedPreferences sharedPreference = getSharedPreference();
        Editor editor = sharedPreference.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * 获取String的value
     *
     * @param key      名字
     * @param defValue 默认值
     * @return
     */
    public static String getString(String key, String defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getString(key, defValue);
    }

    /**
     * 获取int的value
     *
     * @param key      名字
     * @param defValue 默认值
     * @return
     */
    public static int getInt(String key, int defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getInt(key, defValue);
    }

    /**
     * 获取float的value
     *
     * @param key      名字
     * @param defValue 默认值
     * @return
     */
    public static float getFloat(String key, Float defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getFloat(key, defValue);
    }

    /**
     * 获取boolean的value
     *
     * @param key      名字
     * @param defValue 默认值
     * @return
     */
    public static boolean getBoolean(String key,
                                     Boolean defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getBoolean(key, defValue);
    }

    /**
     * 获取long的value
     *
     * @param key      名字
     * @param defValue 默认值
     * @return
     */
    public static long getLong(String key, long defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getLong(key, defValue);
    }

    public static void removeKey(String key) {
        try {
            SharedPreferences sharedPreference = getSharedPreference();
            Editor editor = sharedPreference.edit();
            editor.remove(key);
            editor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void clear() {
        SharedPreferences sharedPreference = getSharedPreference();
        Editor editor = sharedPreference.edit();
        editor.clear();
        editor.commit();
    }
}
