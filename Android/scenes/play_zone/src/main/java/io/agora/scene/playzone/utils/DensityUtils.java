package io.agora.scene.playzone.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

import com.blankj.utilcode.util.Utils;

import io.agora.scene.base.component.AgoraApplication;

public class DensityUtils {

    /** dp转px */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /** dp转px */
    public static int dp2px(float dpValue) {
        return dp2px(AgoraApplication.the(), dpValue);
    }

    /** px转dp */
    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /** 获取屏幕真实宽度 */
    public static int getScreenWidth() {
        return getScreenSize().x;
    }

    /** 获取屏幕真实高度 */
    public static int getScreenHeight() {
        return getScreenSize().y;
    }

    /** 获取应用展示宽度 */
    public static int getAppScreenWidth() {
        return getAppScreenSize().x;
    }

    /** 获取应用展示高度 */
    public static int getAppScreenHeight() {
        return getAppScreenSize().y;
    }

    /** 获取app展示的宽高 */
    public static Point getAppScreenSize() {
        WindowManager wm = (WindowManager) Utils.getApp().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point;
    }

    /** 获取屏幕真实宽高 */
    public static Point getScreenSize() {
        WindowManager wm = (WindowManager) Utils.getApp().getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point;
    }

}
