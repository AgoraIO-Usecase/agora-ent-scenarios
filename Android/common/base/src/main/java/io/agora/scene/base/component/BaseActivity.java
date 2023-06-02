package io.agora.scene.base.component;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initView(savedInstanceState);
        setDarkStatusIcon(isBlackDarkStatus());
        requestData();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initListener();
    }

    protected void init() {
        if (getLayoutId() > 0) {
            setContentView(getLayoutId());
        } else {
            setContentView(getLayoutView());
        }
    }


    public void initView(Bundle savedInstanceState) {

    }

    public void initListener() {
    }

    public void requestData() {
    }

    public View getLayoutView() {
        return null;
    }

    abstract int getLayoutId();

    public boolean isBlackDarkStatus() {
        return true;
    }

    /**
     * bDark true status bar text为黑色
     */
    public void setDarkStatusIcon(boolean bDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                View decorView = getWindow().getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                //在6.0增加了View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR，
                // 这个字段就是把状态栏标记为浅色，然后状态栏的字体颜色自动转换为深色
                if (bDark) {
                    option = option | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(option);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                WindowManager.LayoutParams attributes = getWindow().getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                attributes.flags = attributes.flags | flagTranslucentStatus;
                getWindow().setAttributes(attributes);
            }
        }
    }
}