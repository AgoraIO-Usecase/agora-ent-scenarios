package com.agora.entfulldemo.home;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.databinding.AppFragmentHomeFindBinding;

import io.agora.scene.base.component.BaseViewBindingFragment;

public class HomeFindFragment extends BaseViewBindingFragment<AppFragmentHomeFindBinding> {
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @NonNull
    @Override
    protected AppFragmentHomeFindBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return AppFragmentHomeFindBinding.inflate(inflater);
    }

    @Override
    public void initView() {
        getBinding().webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void initListener() {
        getBinding().webView.setWebChromeClient(new MyWebChromeClient());
    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onHideCustomView() {
            //退出全屏
            hideCustomView();
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            //进入全屏
            showCustomView(view, callback);
        }
    }

    /**
     * 显示自定义控件
     */
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (fullscreenContainer != null) {
            callback.onCustomViewHidden();
            return;
        }
        fullscreenContainer = new FrameLayout(requireActivity());
        fullscreenContainer.setBackgroundColor(Color.WHITE);
        customViewCallback = callback;
        fullscreenContainer.addView(view);
        FrameLayout decorView = (FrameLayout) requireActivity().getWindow().getDecorView();
        // 收到需求，WebView不允许横屏
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        decorView.addView(fullscreenContainer);
    }

    /**
     * 隐藏自定义控件
     */
    private void hideCustomView() {
        if (fullscreenContainer == null) {
            return;
        }

        FrameLayout decorView = (FrameLayout) requireActivity().getWindow().getDecorView();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        fullscreenContainer.removeAllViews();
        decorView.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customViewCallback.onCustomViewHidden();
        customViewCallback = null;
    }

}
