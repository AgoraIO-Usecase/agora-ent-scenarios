package io.agora.scene.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.base.manager.PagePilotManager;

public class CustomWebView extends WebView {
    public CustomWebView(@NonNull Context context) {
        super(context);
        initView();
    }

    public CustomWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CustomWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        getSettings().setUseWideViewPort(false);
        getSettings().setDomStorageEnabled(true);
        setWebViewClient(new MyWebViewClient());
        getSettings().setAllowFileAccess(true);
        getSettings().setDatabaseEnabled(true);
        getSettings().setSaveFormData(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setSupportZoom(false);
        getSettings().setDefaultTextEncodingName("UTF-8");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }

    // 监听 所有点击的链接，如果拦截到我们需要的，就跳转到相对应的页面。
    public static class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp") || url.startsWith("file:///android_asset")) {
                if (url.startsWith("file:///android_asset")) {
                    PagePilotManager.pageWebView(url);
                    return true;
                }
                return false;
            }
            return true;
        }
    }

}
