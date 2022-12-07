package io.agora.scene.widget.utils;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class StatusBarUtil {

    public static void hideStatusBar(Window window, boolean darkText){
        hideStatusBar(window, Color.TRANSPARENT, darkText);
    }

    public static void hideStatusBar(Window window, int statusBarColor, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        int winFlags = 0;
        winFlags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        window.addFlags(winFlags);
        window.setStatusBarColor(statusBarColor);

        int flag = window.getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(flag);
    }

    public static void hideNavigation(Window window){
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(window.getDecorView());
        if(windowInsetsController != null){
            windowInsetsController
                    .setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        window.setNavigationBarColor(Color.TRANSPARENT);

        int flag = window.getDecorView().getSystemUiVisibility();
        flag |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        window.getDecorView().setSystemUiVisibility(flag);
    }
}
