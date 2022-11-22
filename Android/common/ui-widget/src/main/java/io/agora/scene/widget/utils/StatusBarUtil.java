package io.agora.scene.widget.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StatusBarUtil {

    public static void hideStatusBar(Window window, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        window.getDecorView().setSystemUiVisibility(flag |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static void setDarkStatusIcon(Window window, boolean bDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                //在6.0增加了View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR，
                // 这个字段就是把状态栏标记为浅色，然后状态栏的字体颜色自动转换为深色
                if (bDark) {
                    option = option | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            } else {
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                attributes.flags = attributes.flags | flagTranslucentStatus;
                window.setAttributes(attributes);
            }
        }
    }

    private static boolean init = false;
    private static int statusBarHeight = 50;

    public static synchronized int getStatusBarHeight(final Context context) {
        if (!init) {
            int resourceId = context.getResources().
                    getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
                init = true;
                Log.d("StatusBarUtil", String.format("Get status bar height %d", statusBarHeight));
            }
        }

        return statusBarHeight;
    }
}
