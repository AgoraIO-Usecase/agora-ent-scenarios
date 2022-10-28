package io.agora.scene.widget.utils;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.view.DisplayCutout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NotchUtils {
    /**
     * 是否有刘海屏
     *
     * @return
     */
    public static boolean hasNotchInScreen(Activity activity) {
        try {
            // android P以上有标准 API 来判断是否有刘海屏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                DisplayCutout displayCutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
                if (displayCutout != null) {
                    // 说明有刘海屏
                    return true;
                }
            } else {
                // 通过其他方式判断是否有刘海屏  目前官方提供有开发文档的就 小米，vivo，华为（荣耀），oppo ， meizu
                String manufacturer = Build.MANUFACTURER;
                if (TextUtils.isEmpty(manufacturer)) {
                    return false;
                } else if (manufacturer.equalsIgnoreCase("HUAWEI")) {
                    return hasNotchHw(activity);
                } else if (manufacturer.equalsIgnoreCase("xiaomi")) {
                    return hasNotchXiaoMi(activity);
                } else if (manufacturer.equalsIgnoreCase("oppo")) {
                    return hasNotchOPPO(activity);
                } else if (manufacturer.equalsIgnoreCase("vivo")) {
                    return hasNotchVIVO(activity);
                } else if (manufacturer.equalsIgnoreCase("meizu")) {
                    return hasNotchMeizu(activity);
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 判断vivo是否有刘海屏
     * https://swsdl.vivo.com.cn/appstore/developer/uploadfile/20180328/20180328152252602.pdf
     *
     * @param activity
     * @return
     */
    private static boolean hasNotchVIVO(Activity activity) {
        try {
            Class<?> c = Class.forName("android.util.FtFeature");
            Method get = c.getMethod("isFeatureSupport", int.class);
            return (boolean) (get.invoke(c, 0x20));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断oppo是否有刘海屏
     * https://open.oppomobile.com/wiki/doc#id=10159
     *
     * @param activity
     * @return
     */
    private static boolean hasNotchOPPO(Activity activity) {
        return activity.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    /**
     * 判断xiaomi是否有刘海屏
     * https://dev.mi.com/console/doc/detail?pId=1293
     *
     * @param activity
     * @return
     */
    private static boolean hasNotchXiaoMi(Activity activity) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("getInt", String.class, int.class);
            return (int) (get.invoke(c, "ro.miui.notch", 0)) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断华为是否有刘海屏
     * https://devcenter-test.huawei.com/consumer/cn/devservice/doc/50114
     *
     * @param activity
     */
    private static boolean hasNotchHw(Activity activity) {

        try {
            ClassLoader cl = activity.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            return (boolean) get.invoke(HwNotchSizeUtil);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断魅族是否有刘海屏
     * @param activity
     */
    private static boolean hasNotchMeizu(Activity activity) {
        try {
            Class clazz = Class.forName("flyme.config.FlymeFeature");
            Field field = clazz.getDeclaredField("IS_FRINGE_DEVICE");
            return (boolean) field.get(null);
        } catch (Exception e) {
            return false;
        }
    }
}