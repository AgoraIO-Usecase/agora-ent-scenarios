package io.agora.scene.widget.utils;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StatusBarUtil {

    public static void hideStatusBar(Window window, boolean darkText) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        int flag = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && darkText) {
            flag |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        window.getDecorView().setSystemUiVisibility(flag);
    }
}
