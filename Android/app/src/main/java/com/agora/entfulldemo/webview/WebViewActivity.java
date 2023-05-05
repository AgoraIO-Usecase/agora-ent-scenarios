package com.agora.entfulldemo.webview;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.agora.entfulldemo.R;
import com.agora.entfulldemo.databinding.AppActivityWebviewBinding;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import io.agora.scene.base.Constant;
import io.agora.scene.base.PagePathConstant;
import io.agora.scene.base.component.BaseViewBindingActivity;
import kotlin.jvm.JvmField;

@Route(path = PagePathConstant.pageWebView)
public class WebViewActivity extends BaseViewBindingActivity<AppActivityWebviewBinding> {
    /**
     * h5地址
     */
    @JvmField
    @Autowired(name = Constant.URL)
    String url = "https://www.agora.io/cn/about-us/";


    @Override
    protected AppActivityWebviewBinding getViewBinding(@NonNull LayoutInflater layoutInflater) {
        return AppActivityWebviewBinding.inflate(layoutInflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        setOnApplyWindowInsetsListener(getBinding().superLayout);
        ARouter.getInstance().inject(this);
        if (url.contains("user_agreement")) {
            getBinding().titleView.setTitle(getString(R.string.app_user_agreement));
        } else if (url.contains("about-us")) {
            getBinding().titleView.setTitle(getString(R.string.app_about_us));
        } else if (url.contains("privacy_policy")) {
            getBinding().titleView.setTitle(getString(R.string.app_privacy_agreement));
        }
        getBinding().webView.loadUrl(url);
    }
}
